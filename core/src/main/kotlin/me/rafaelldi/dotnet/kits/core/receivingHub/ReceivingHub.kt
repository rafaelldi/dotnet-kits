@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.kits.core.receivingHub

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.fs.createTemporaryFile
import com.intellij.platform.eel.getOrNull
import com.intellij.platform.eel.path.EelPath
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
internal class ReceivingHub(private val project: Project) {
    companion object {
        fun getInstance(project: Project): ReceivingHub = project.service()

        private const val DOTNET_FEED_URL =
            "https://builds.dotnet.microsoft.com/dotnet/release-metadata/releases-index.json"

        private val LOG = logger<ReceivingHub>()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun receiveInboundCargo(model: InboundCargoModel): Path? {
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

        val filesToDownload = when (model.type) {
            InboundCargoType.Sdk -> latestRelease.sdk.files
            InboundCargoType.Runtime -> latestRelease.runtime.files
            InboundCargoType.AspNetRuntime -> latestRelease.aspNetCoreRuntime.files
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
            LOG.warn("Failed to find file to download for version: ${model.version}")
            return null
        }

        val downloadedArchive = downloadReleaseArchive(archiveToDownload, model.rid.fileExtension)
        if (downloadedArchive == null) {
            LOG.warn("Unable to download dotnet release archive")
            return null
        }

        val releaseFolder = unpackReleaseArchive(downloadedArchive, model.type, releaseVersion.latestRelease)

        return releaseFolder?.asNioPath()
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

    private suspend fun downloadReleaseArchive(dotnetReleaseFile: DotnetReleaseFile, extension: String): EelPath? {
        try {
            val eelApi = project.getEelDescriptor().toEelApi()
            val tempFile = eelApi.fs.createTemporaryFile()
                .prefix("dotnet-warehouse")
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
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LOG.warn("Failed to download the dotnet release to a temporary file", e)
            return null
        }
    }

    private suspend fun unpackReleaseArchive(
        releaseArchive: EelPath,
        type: InboundCargoType,
        releaseVersion: String
    ): EelPath? {
        try {
            val eelApi = project.getEelDescriptor().toEelApi()

            val userHome = eelApi.userInfo.home.resolve(".warehouse")
            val releaseTypePath = when (type) {
                InboundCargoType.Sdk -> userHome.resolve("sdk")
                InboundCargoType.Runtime -> userHome.resolve("runtime")
                InboundCargoType.AspNetRuntime -> userHome.resolve("aspnetcore")
            }
            val targetPath = releaseTypePath.resolve(releaseVersion)

            eelApi.archive.extract(releaseArchive, targetPath)

            return targetPath
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LOG.warn("Failed to unpack the temporary dotnet release archive", e)
            return null
        }
    }
}