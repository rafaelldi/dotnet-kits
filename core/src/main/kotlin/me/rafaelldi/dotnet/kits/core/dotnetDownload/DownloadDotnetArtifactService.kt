@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.kits.core.dotnetDownload

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.*
import com.intellij.platform.eel.fs.EelFileSystemApi
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
import kotlinx.serialization.json.Json
import me.rafaelldi.dotnet.kits.core.util.DownloadArchiveService
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path

/**
 * Service responsible for downloading .NET artifacts from the official Microsoft .NET release feeds.
 *
 * The service supports downloading three types of .NET artifacts:
 * - .NET SDK
 * - .NET Runtime
 * - ASP.NET Core Runtime
 */
@ApiStatus.Internal
@Service(Service.Level.PROJECT)
class DownloadDotnetArtifactService(private val project: Project) {
    companion object {
        fun getInstance(project: Project): DownloadDotnetArtifactService = project.service()

        private const val DOTNET_FEED_URL =
            "https://builds.dotnet.microsoft.com/dotnet/release-metadata/releases-index.json"

        private val LOG = logger<DownloadDotnetArtifactService>()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getDefaultDownloadModel(): DotnetArtifactModel? {
        val eelApi = project.getEelDescriptor().toEelApi()

        val version = DotnetDownloadVersion.Version10
        val type = DotnetDownloadType.Sdk
        val rid = when {
            eelApi.platform.isLinux && (eelApi.platform.isX86_64 || eelApi.platform.isX86) -> DotnetDownloadRid.LinuxX64
            eelApi.platform.isLinux && eelApi.platform.isArm64 -> DotnetDownloadRid.LinuxArm64
            eelApi.platform.isWindows && (eelApi.platform.isX86_64 || eelApi.platform.isX86) -> DotnetDownloadRid.WinX64
            eelApi.platform.isWindows && eelApi.platform.isArm64 -> DotnetDownloadRid.WinArm64
            eelApi.platform.isMac && (eelApi.platform.isX86_64 || eelApi.platform.isX86) -> DotnetDownloadRid.MacOsX64
            eelApi.platform.isMac && eelApi.platform.isArm64 -> DotnetDownloadRid.MacOsArm64
            else -> null
        }

        if (rid == null) return null

        val dotnetBaseFolder = eelApi.userInfo.home.resolve(".dotnet")

        return DotnetArtifactModel(
            dotnetBaseFolder.asNioPath(),
            version,
            type,
            rid
        )
    }

    /**
     * Downloads the latest version of a .NET artifact to a specified target directory.
     *
     * This method queries the Microsoft .NET release feeds to find the latest version available for the
     * specified .NET version channel, downloads the appropriate archive for the target platform (RID),
     * and extracts it to the specified target folder.
     *
     * The artifact will be placed in subdirectories of the target folder following the standard .NET structure:
     * - SDK: `{targetFolder}/sdk/{version}/`
     * - Runtime: `{targetFolder}/shared/Microsoft.NETCore.App/{version}/`
     * - ASP.NET Runtime: `{targetFolder}/shared/Microsoft.AspNetCore.App/{version}/`
     *
     * @param model The artifact model specifying the .NET version, type (SDK/Runtime/ASP.NET Runtime),
     *              runtime identifier (RID) for the target platform,
     *              and base directory where the artifact should be installed.
     * @return A [Result] containing the [Path] to the installed artifact on success, or a failure with an exception.
     */
    suspend fun download(model: DotnetArtifactModel): Result<Path> {
        try {
            val eelApi = project.getEelDescriptor().toEelApi()

            val latestRelease = getLatestReleaseOfModel(model)
            if (latestRelease == null) {
                LOG.warn("Failed to find latest release for model: $model")
                return Result.failure(IllegalStateException("Failed to find latest release for model: $model"))
            }

            val versionToDownload = getVersionToDownload(latestRelease, model.type)
            val targetPath = calculateTargetPath(model.dotnetBaseFolder, model.type, versionToDownload).asEelPath()
            LOG.trace { "Target path for the release download: $targetPath" }

            val foldersAtTargetPath = eelApi.fs.listDirectory(requireNotNull(targetPath.parent)).getOrNull()
            if (foldersAtTargetPath?.contains(versionToDownload) == true) {
                LOG.info("Release version $versionToDownload already exists at target path, skipping downloading")
                return Result.failure(IllegalStateException("Release version $versionToDownload already exists at target path, skipping downloading"))
            }

            val downloadUrl = getDownloadUrl(model, latestRelease)
            if (downloadUrl == null) {
                LOG.warn("Unable to find download URL for model: $model")
                return Result.failure(IllegalStateException("Unable to find download URL for dotnet release"))
            }

            val downloadArchiveService = DownloadArchiveService.getInstance(project)
            val result = downloadArchiveService.downloadArchive(
                downloadFilePrefix = "dotnet-kits",
                downloadFileExtension = model.rid.fileExtension,
                downloadUrl = downloadUrl,
                selectPathFromArchive = { archiveDir: EelPath, fs: EelFileSystemApi ->
                    selectDotnetPathFromArchive(archiveDir, fs, versionToDownload, model.type)
                },
                targetPath = targetPath
            )

            return result
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LOG.warn("Failed to download dotnet release", e)
            return Result.failure(e)
        }
    }

    private suspend fun getLatestReleaseOfModel(model: DotnetArtifactModel): DotnetRelease? {
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

    private fun getVersionToDownload(release: DotnetRelease, type: DotnetDownloadType): String =
        when (type) {
            DotnetDownloadType.Sdk -> release.sdk.version
            DotnetDownloadType.Runtime -> release.runtime.version
            DotnetDownloadType.AspNetRuntime -> release.aspNetCoreRuntime.version
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

    private fun getDownloadUrl(model: DotnetArtifactModel, latestRelease: DotnetRelease): String? {
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

        return archiveToDownload.url
    }

    private suspend fun selectDotnetPathFromArchive(
        archiveDir: EelPath,
        fs: EelFileSystemApi,
        releaseVersion: String,
        type: DotnetDownloadType
    ): EelPath {
        val sourceSubfolder = when (type) {
            DotnetDownloadType.Sdk -> archiveDir.resolve("sdk")
            DotnetDownloadType.Runtime -> archiveDir.resolve("shared").resolve("Microsoft.NETCore.App")
            DotnetDownloadType.AspNetRuntime -> archiveDir.resolve("shared").resolve("Microsoft.AspNetCore.App")
        }

        val versionFolders = fs.listDirectory(sourceSubfolder).getOrNull()
        if (versionFolders.isNullOrEmpty()) {
            LOG.warn("No version folder found in extracted archive at $sourceSubfolder")
            error("No version folder found in extracted release archive")
        }

        if (!versionFolders.contains(releaseVersion)) {
            error("Unable to find version folder $releaseVersion in extracted release archive")
        }

        return sourceSubfolder.resolve(releaseVersion)
    }
}