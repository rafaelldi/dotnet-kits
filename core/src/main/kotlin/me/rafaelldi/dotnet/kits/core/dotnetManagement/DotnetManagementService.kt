@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.kits.core.dotnetManagement

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.platform.eel.EelApi
import com.intellij.platform.eel.EelExecApi
import com.intellij.platform.eel.EelPlatform
import com.intellij.platform.eel.ExecuteProcessOptionsBuilder
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.awaitProcessResult
import com.intellij.platform.eel.provider.utils.stdoutString
import com.intellij.util.PathUtil
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

@ApiStatus.Internal
interface DotnetManagementApi {
    suspend fun findDotnetSdks(): List<DotnetSdk>
    suspend fun findDotnetRuntimes(): List<DotnetRuntime>
    suspend fun openArtifactFolder(artifact: DotnetArtifact)
    suspend fun deleteArtifactFolder(dotnetArtifact: DotnetArtifact)
}

@Service(Service.Level.PROJECT)
@ApiStatus.Internal
class DotnetManagementService(private val project: Project) : DotnetManagementApi {
    companion object {
        fun getInstance(project: Project): DotnetManagementService = project.service()

        private val LOG = logger<DotnetManagementService>()

        private const val LIST_SDKS_OPTION = "--list-sdks"
        private const val LIST_RUNTIMES_OPTION = "--list-runtimes"
    }

    override suspend fun findDotnetSdks(): List<DotnetSdk> {
        val eelApi = project.getEelDescriptor().toEelApi()
        val executablePaths = getDotnetExecutablePaths(eelApi) + getRiderDotnetExecutablePaths(eelApi)
        return buildList {
            for ((executablePath, installationType) in executablePaths) {
                if (!executablePath.exists()) continue
                val sdks = findDotnetSdks(eelApi.exec, executablePath, installationType)
                addAll(sdks)
            }
        }.sortedWith(compareBy({ it.major }, { it.minor }, { it.patch }, { it.preRelease }))
    }

    override suspend fun findDotnetRuntimes(): List<DotnetRuntime> {
        val eelApi = project.getEelDescriptor().toEelApi()
        val executablePaths = getDotnetExecutablePaths(eelApi) + getRiderDotnetExecutablePaths(eelApi)
        return buildList {
            for ((executablePath, installationType) in executablePaths) {
                if (!executablePath.exists()) continue
                val runtimes = findDotnetRuntimes(eelApi.exec, executablePath, installationType)
                addAll(runtimes)
            }
        }.sortedWith(compareBy({ it.major }, { it.minor }, { it.patch }, { it.preRelease }))
    }

    override suspend fun openArtifactFolder(artifact: DotnetArtifact) {
        RevealFileAction.openDirectory(artifact.path)
    }

    override suspend fun deleteArtifactFolder(dotnetArtifact: DotnetArtifact) {
        val eelApi = project.getEelDescriptor().toEelApi()
        val artifactFolder = dotnetArtifact.path.asEelPath()
        eelApi.fs.delete(artifactFolder, true)
    }

    private suspend fun findDotnetSdks(
        execApi: EelExecApi,
        executablePath: Path,
        installationFolder: InstallationFolder
    ): List<DotnetSdk> {
        val executionResult = executeDotnetCommand(execApi, executablePath, LIST_SDKS_OPTION)
            ?: return emptyList()

        return buildList {
            for (line in executionResult.lines()) {
                val spaceIndex = line.indexOf(' ')
                if (spaceIndex <= 0 || spaceIndex >= line.length) continue

                val version = line.take(spaceIndex)
                val pathString = line.substring(spaceIndex + 2, line.length - 1)
                val sdk = DotnetSdk(
                    version,
                    Path.of(pathString).resolve(version),
                    installationFolder
                )

                add(sdk)
            }
        }
    }

    private suspend fun findDotnetRuntimes(
        execApi: EelExecApi,
        executablePath: Path,
        installationFolder: InstallationFolder
    ): List<DotnetRuntime> {
        val executionResult = executeDotnetCommand(execApi, executablePath, LIST_RUNTIMES_OPTION)
            ?: return emptyList()

        return buildList {
            for (line in executionResult.lines()) {
                val firstSpaceIndex = line.indexOf(' ')
                val secondSpaceIndex = line.lastIndexOf(' ')
                if (firstSpaceIndex <= 0 || firstSpaceIndex >= line.length) continue
                if (secondSpaceIndex <= 0 || secondSpaceIndex >= line.length) continue

                val type = line.take(firstSpaceIndex)
                val version = line.substring(firstSpaceIndex + 1, secondSpaceIndex)
                val pathString = line.substring(secondSpaceIndex + 2, line.length - 1)
                val runtime = DotnetRuntime(
                    type,
                    version,
                    Path.of(pathString).resolve(version),
                    installationFolder
                )

                add(runtime)
            }
        }
    }


    // https://learn.microsoft.com/en-us/dotnet/core/install/how-to-detect-installed-versions?pivots=os-linux#check-for-install-folders
    private fun getDotnetExecutablePaths(eelApi: EelApi): List<Pair<Path, InstallationFolder>> {
        when (eelApi.platform) {
            is EelPlatform.Windows -> {
                return buildList {
                    add(Path.of("C:\\Program Files\\dotnet\\dotnet.exe") to InstallationFolder.Default)
                }
            }

            is EelPlatform.Linux -> {
                return buildList {
                    val userHome = eelApi.userInfo.home.asNioPath()
                    add(userHome.resolve(".dotnet/dotnet") to InstallationFolder.Manual)
                    add(Path.of("/usr/lib/dotnet/dotnet") to InstallationFolder.Manual)
                    add(Path.of("/usr/share/dotnet/dotnet") to InstallationFolder.Manual)
                    add(Path.of("/usr/lib64/dotnet/dotnet") to InstallationFolder.Default)
                }
            }

            else -> {
                return buildList {
                    val userHome = eelApi.userInfo.home.asNioPath()
                    add(userHome.resolve(".dotnet/dotnet") to InstallationFolder.Manual)
                    add(Path.of("/usr/local/share/dotnet/dotnet") to InstallationFolder.Default)
                }
            }
        }
    }

    private fun getRiderDotnetExecutablePaths(eelApi: EelApi): List<Pair<Path, InstallationFolder>> {
        val dotnetCmdPath = when (eelApi.platform) {
            is EelPlatform.Windows -> {
                val appData = System.getenv("LOCALAPPDATA")
                Path.of(appData).resolve("JetBrains/dotnet-cmd")
            }

            else -> {
                val userHome = eelApi.userInfo.home.asNioPath()
                userHome.resolve(".local/share/JetBrains/dotnet-cmd")
            }
        }

        if (!dotnetCmdPath.exists()) return emptyList()

        val executable = if (eelApi.platform is EelPlatform.Windows) "dotnet.exe" else "dotnet"

        return buildList {
            for (folder in dotnetCmdPath.listDirectoryEntries().filter { it.isDirectory() }) {
                add(folder.resolve(executable) to InstallationFolder.Rider)
            }
        }
    }

    private suspend fun executeDotnetCommand(
        execApi: EelExecApi,
        executablePath: Path,
        parameters: String
    ): String? {
        try {
            val processOptions = ExecuteProcessOptionsBuilder(executablePath.toString())
                .args(parameters)
                .workingDirectory(project.basePath?.toNioPathOrNull()?.asEelPath())
                .env(
                    mapOf(
                        "DOTNET_SKIP_FIRST_TIME_EXPERIENCE" to "true",
                        "DOTNET_NOLOGO" to "true"
                    )
                )
                .build()

            val processResult = execApi
                .spawnProcess(processOptions)
                .awaitProcessResult()

            return if (processResult.exitCode == 0) processResult.stdoutString
            else null
        } catch (e: Exception) {
            LOG.warn("Unable to execute dotnet command", e)
            return null
        }
    }
}