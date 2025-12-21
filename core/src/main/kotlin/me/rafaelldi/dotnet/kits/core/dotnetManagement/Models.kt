package me.rafaelldi.dotnet.kits.core.dotnetManagement

import me.rafaelldi.dotnet.kits.core.util.extractPreRelease
import me.rafaelldi.dotnet.kits.core.util.parseSemanticVersion
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal interface DotnetArtifact {
    val version: String
    val major: Int
    val minor: Int
    val patch: Int
    val preRelease: String?
    val pathString: String
    val installationType: InstallationType
}

internal data class DotnetSdk(
    override val version: String,
    val path: Path,
    override val installationType: InstallationType
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

internal data class DotnetRuntime(
    val type: String,
    override val version: String,
    val path: Path,
    override val installationType: InstallationType
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