@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.kits.core.dotnetDownload

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.EelApi
import com.intellij.platform.eel.fs.createTemporaryFile
import com.intellij.platform.eel.getOrNull
import com.intellij.platform.eel.path.EelPath
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.outputStream

@Service(Service.Level.PROJECT)
internal class DotnetDownloadService(private val project: Project) {
    companion object {
        fun getInstance(project: Project): DotnetDownloadService = project.service()

        private const val DOTNET_FEED_URL =
            "https://builds.dotnet.microsoft.com/dotnet/release-metadata/releases-index.json"

        private val LOG = logger<DotnetDownloadService>()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun download(model: DotnetDownloadModel): Result<Path> {
        val eelApi = project.getEelDescriptor().toEelApi()
        val defaultDotnetTargetFolder = eelApi.userInfo.home.resolve(".dotnet")
        return download(model, defaultDotnetTargetFolder.asNioPath())
    }

    suspend fun download(model: DotnetDownloadModel, targetFolder: Path): Result<Path> {
        try {
            val eelApi = project.getEelDescriptor().toEelApi()

            val latestRelease = getLatestReleaseOfModel(model)
            if (latestRelease == null) {
                LOG.warn("Failed to find latest release for model: $model")
                return Result.failure(IllegalStateException("Failed to find latest release for model: $model"))
            }

            val targetPath = calculateTargetPath(targetFolder, model.type, latestRelease.releaseVersion).asEelPath()
            val foldersInTargetPath = eelApi.fs.listDirectory(requireNotNull(targetPath.parent)).getOrNull()
            if (foldersInTargetPath?.contains(latestRelease.releaseVersion) == true) {
                LOG.info("Release version ${latestRelease.releaseVersion} already exists at target path, skipping downloading")
                return Result.failure(IllegalStateException("Release version ${latestRelease.releaseVersion} already exists at target path, skipping downloading"))
            }

            val downloadedArchive = downloadReleaseArchive(model, latestRelease, eelApi)
            if (downloadedArchive == null) {
                LOG.warn("Unable to download dotnet release archive for model: $model")
                return Result.failure(IllegalStateException("Unable to download dotnet release archive"))
            }

            unpackReleaseArchive(downloadedArchive, targetPath, eelApi)

            return Result.success(targetPath.asNioPath())
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LOG.warn("Failed to download dotnet release", e)
            return Result.failure(e)
        }
    }

    private suspend fun getLatestReleaseOfModel(model: DotnetDownloadModel): DotnetRelease? {
        val index = receiveDotnetReleaseIndex()
        if (index == null) {
            LOG.warn("Failed to receive dotnet release index")
            return null
        }

        val releaseVersion = index.versions.firstOrNull { it.channelVersion == model.version.version }
        if (releaseVersion == null) {
            LOG.warn("Failed to find release index for version: ${model.version}")
            return null
        }

        val versionIndex = receiveDotnetReleaseVersionIndex(releaseVersion.releasesJson)
        if (versionIndex == null) {
            LOG.warn("Failed to receive dotnet release version index for version: ${model.version}")
            return null
        }

        val latestRelease = versionIndex.releases.firstOrNull { it.releaseVersion == releaseVersion.latestRelease }
        if (latestRelease == null) {
            LOG.warn("Failed to find latest release for version: ${model.version}")
            return null
        }

        return latestRelease
    }

    private suspend fun receiveDotnetReleaseIndex(): DotnetReleaseIndex? {
        val response = client.get(DOTNET_FEED_URL)
        if (response.status.value !in 200..299) {
            LOG.warn("Failed to receive dotnet release index. Status: ${response.status}")
            return null
        }

        val releaseIndex: DotnetReleaseIndex = response.body()
        LOG.trace { "Received dotnet release index: $releaseIndex" }

        return releaseIndex
    }

    private suspend fun receiveDotnetReleaseVersionIndex(url: String): DotnetReleaseVersionIndex? {
        val response = client.get(url)
        if (response.status.value !in 200..299) {
            LOG.warn("Failed to receive dotnet release version index. Status: ${response.status}")
            return null
        }

        val releaseVersionIndex: DotnetReleaseVersionIndex = response.body()
        LOG.trace { "Received dotnet release index: $releaseVersionIndex" }

        return releaseVersionIndex
    }

    private fun calculateTargetPath(targetFolder: Path, type: DotnetDownloadType, releaseVersion: String): Path =
        when (type) {
            DotnetDownloadType.Sdk -> targetFolder
                .resolve("sdk")
                .resolve(releaseVersion)

            DotnetDownloadType.Runtime -> targetFolder
                .resolve("shared")
                .resolve("Microsoft.NETCore.App")
                .resolve(releaseVersion)

            DotnetDownloadType.AspNetRuntime -> targetFolder
                .resolve("shared")
                .resolve("Microsoft.AspNetCore.App")
                .resolve(releaseVersion)
        }

    private suspend fun downloadReleaseArchive(
        model: DotnetDownloadModel,
        latestRelease: DotnetRelease,
        eelApi: EelApi,
    ): EelPath? {
        val filesToDownload = when (model.type) {
            DotnetDownloadType.Sdk -> latestRelease.sdk.files
            DotnetDownloadType.Runtime -> latestRelease.runtime.files
            DotnetDownloadType.AspNetRuntime -> latestRelease.aspNetCoreRuntime.files
        }
        val fileNameToDownload = buildString {
            append(model.type.id)
            append("-")
            append(model.rid.id)
            append(model.rid.fileExtension)
        }
        LOG.trace { "File to download: $fileNameToDownload" }

        val archiveToDownload = filesToDownload.firstOrNull { it.name == fileNameToDownload }
        if (archiveToDownload == null) {
            LOG.warn("Failed to find a file to download for version: ${model.version}")
            return null
        }

        val downloadedArchive = downloadReleaseArchive(archiveToDownload, model.rid.fileExtension, eelApi)
        if (downloadedArchive == null) {
            LOG.warn("Unable to download dotnet release archive")
            return null
        }

        return downloadedArchive
    }

    private suspend fun downloadReleaseArchive(
        dotnetReleaseFile: DotnetReleaseFile,
        extension: String,
        eelApi: EelApi,
    ): EelPath? {
        val tempFile = eelApi.fs.createTemporaryFile()
            .prefix("dotnet-kits")
            .suffix(extension)
            .deleteOnExit(true)
            .eelIt()
            .getOrNull()
        if (tempFile == null) {
            LOG.warn("Failed to create a temporary file")
            return null
        }

        LOG.trace { "Temporary file for the release download: ${tempFile.fileName}" }

        val tempFileWriteChannel = tempFile
            .asNioPath()
            .outputStream()
            .asByteWriteChannel()

        client.prepareGet(dotnetReleaseFile.url).execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.body()
            channel.copyAndClose(tempFileWriteChannel)
        }

        return tempFile
    }

    private suspend fun unpackReleaseArchive(
        releaseArchive: EelPath,
        targetPath: EelPath,
        eelApi: EelApi,
    ) {
        eelApi.archive.extract(releaseArchive, targetPath)
    }
}