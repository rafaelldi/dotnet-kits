@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.kits.core.util

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.EelApi
import com.intellij.platform.eel.fs.EelFileSystemApi
import com.intellij.platform.eel.fs.createTemporaryDirectory
import com.intellij.platform.eel.fs.createTemporaryFile
import com.intellij.platform.eel.fs.move
import com.intellij.platform.eel.path.EelPath
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.getOrThrowFileSystemException
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
internal class DownloadArchiveService(private val project: Project) {
    companion object {
        fun getInstance(project: Project): DownloadArchiveService = project.service()
        private val LOG = logger<DownloadArchiveService>()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun downloadArchive(
        downloadFilePrefix: String,
        downloadFileExtension: String,
        downloadUrl: String,
        selectPathFromArchive: suspend (archiveDir: EelPath, fs: EelFileSystemApi) -> EelPath,
        targetPath: EelPath,
    ): Result<Path> {
        try {
            val eelApi = project.getEelDescriptor().toEelApi()
            val downloadedArchive = downloadArchive(downloadFilePrefix, downloadFileExtension, downloadUrl, eelApi)
            LOG.trace { "Downloaded archive: ${downloadedArchive.fileName}" }
            unpackArchive(downloadedArchive, downloadFilePrefix, selectPathFromArchive, targetPath, eelApi)
            LOG.trace { "Unpacked archive to: ${targetPath.fileName}" }

            return Result.success(targetPath.asNioPath())
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LOG.warn("Failed to download archive", e)
            return Result.failure(e)
        }
    }

    private suspend fun downloadArchive(
        downloadFilePrefix: String,
        downloadFileExtension: String,
        downloadUrl: String,
        eelApi: EelApi,
    ): EelPath {
        val tempFile = eelApi.fs.createTemporaryFile()
            .prefix(downloadFilePrefix)
            .suffix(downloadFileExtension)
            .deleteOnExit(true)
            .eelIt()
            .getOrThrowFileSystemException()

        LOG.trace { "Temporary file for the archive download: ${tempFile.fileName}" }

        val tempFileWriteChannel = tempFile
            .asNioPath()
            .outputStream()
            .asByteWriteChannel()

        client.prepareGet(downloadUrl).execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.body()
            channel.copyAndClose(tempFileWriteChannel)
        }

        return tempFile
    }

    private suspend fun unpackArchive(
        downloadedArchive: EelPath,
        downloadFilePrefix: String,
        selectPathFromArchive: suspend (archiveDir: EelPath, fs: EelFileSystemApi) -> EelPath,
        targetPath: EelPath,
        eelApi: EelApi,
    ) {
        val tempDir = eelApi.fs.createTemporaryDirectory()
            .prefix("$downloadFilePrefix-extract")
            .deleteOnExit(true)
            .eelIt()
            .getOrThrowFileSystemException()

        LOG.trace { "Temporary directory for the archive extraction: ${tempDir.fileName}" }

        eelApi.archive.extract(downloadedArchive, tempDir)

        val sourcePath = selectPathFromArchive(tempDir, eelApi.fs)

        eelApi.fs
            .move(sourcePath, targetPath)
            .replaceEverything()
            .getOrThrowFileSystemException()
    }
}