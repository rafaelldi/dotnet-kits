@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.kits.core.omnisharp

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.platform.eel.EelPlatform
import com.intellij.platform.eel.fs.createTemporaryDirectory
import com.intellij.platform.eel.fs.createTemporaryFile
import com.intellij.platform.eel.fs.move
import com.intellij.platform.eel.isArm64
import com.intellij.platform.eel.isLinux
import com.intellij.platform.eel.isMac
import com.intellij.platform.eel.isWindows
import com.intellij.platform.eel.isX86
import com.intellij.platform.eel.isX86_64
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.getOrThrowFileSystemException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.prepareGet
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.streams.asByteWriteChannel
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.ApiStatus
import kotlin.io.path.outputStream

@Service(Service.Level.PROJECT)
@ApiStatus.Internal
class OmnisharpManagementService(private val project: Project) {
    companion object {
        fun getInstance(project: Project): OmnisharpManagementService = project.service()

        private val LOG = logger<OmnisharpManagementService>()

        private const val OMNISHARP = "omnisharp"
        private const val OMNISHARP_REPO = "OmniSharp/omnisharp-roslyn"
        private const val OMNISHARP_API_RELEASES = "https://api.github.com/repos/$OMNISHARP_REPO/releases"
        private const val OMNISHARP_RELEASES = "https://roslynomnisharp.blob.core.windows.net/releases"
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun downloadLatestRelease() {
        val eelApi = project.getEelDescriptor().toEelApi()
        val latestVersion = "latest"
        val extension = if (eelApi.platform.isWindows) ".zip" else ".tar.gz"
        val downloadUrl = getDownloadUrl(latestVersion, extension, eelApi.platform) ?: return

        val tempFile = eelApi.fs.createTemporaryFile()
            .prefix("dotnet-kits-omnisharp")
            .suffix(extension)
            .deleteOnExit(true)
            .eelIt()
            .getOrThrowFileSystemException()

        LOG.trace { "Temporary file for the release download: ${tempFile.fileName}" }

        val tempFileWriteChannel = tempFile
            .asNioPath()
            .outputStream()
            .asByteWriteChannel()

        client.prepareGet(downloadUrl).execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.body()
            channel.copyAndClose(tempFileWriteChannel)
        }

        val tempDir = eelApi.fs.createTemporaryDirectory()
            .prefix("dotnet-kits-omnisharp-extract")
            .deleteOnExit(true)
            .eelIt()
            .getOrThrowFileSystemException()

        eelApi.archive.extract(tempFile, tempDir)

        val targetPath = eelApi.userInfo.home.resolve(".omnisharp")

        eelApi.fs
            .move(tempDir, targetPath)
            .replaceEverything()
            .getOrThrowFileSystemException()
    }

    //https://github.com/OmniSharp/omnisharp-roslyn?tab=readme-ov-file#downloading-omnisharp
    private fun getDownloadUrl(version: String, extension: String, platform: EelPlatform): String? = when {
        platform.isLinux && platform.isX86_64 -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-linux-x64$extension"
        platform.isLinux && platform.isX86 -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-linux-x86$extension"
        platform.isLinux && platform.isArm64 -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-linux-arm64$extension"
        platform.isWindows && platform.isX86_64 -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-win-x64$extension"
        platform.isWindows && platform.isX86 -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-win-x86$extension"
        platform.isWindows && platform.isArm64 -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-win-arm64$extension"
        platform.isMac -> "$OMNISHARP_RELEASES/$version/$OMNISHARP-osx$extension"
        else -> null
    }
}