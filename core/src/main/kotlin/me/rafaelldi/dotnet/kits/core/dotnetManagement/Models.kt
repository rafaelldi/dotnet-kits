package me.rafaelldi.dotnet.kits.core.dotnetManagement

import me.rafaelldi.dotnet.kits.core.util.extractPreRelease
import me.rafaelldi.dotnet.kits.core.util.parseSemanticVersion
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@ApiStatus.Internal
interface DotnetArtifact {
    val version: String
    val major: Int
    val minor: Int
    val patch: Int
    val preRelease: String?
    val pathString: String
    val installationFolder: InstallationFolder
}

@ApiStatus.Internal
data class DotnetSdk(
    override val version: String,
    val path: Path,
    override val installationFolder: InstallationFolder
) : DotnetArtifact {
    override val pathString: String = path.absolutePathString()

    override val major: Int
    override val minor: Int
    override val patch: Int
    override val preRelease: String?

    init {
        val (maj, min, pat) = parseSemanticVersion(version)
        major = maj
        minor = min
        patch = pat
        preRelease = extractPreRelease(version)
    }
}

@ApiStatus.Internal
data class DotnetRuntime(
    val type: String,
    override val version: String,
    val path: Path,
    override val installationFolder: InstallationFolder
) : DotnetArtifact {
    override val pathString: String = path.absolutePathString()

    override val major: Int
    override val minor: Int
    override val patch: Int
    override val preRelease: String?

    init {
        val (maj, min, pat) = parseSemanticVersion(version)
        major = maj
        minor = min
        patch = pat
        preRelease = extractPreRelease(version)
    }
}