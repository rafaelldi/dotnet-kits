package me.rafaelldi.dotnet.kits.core.dotnetDownload

import java.nio.file.Path

data class DotnetArtifactModel(
    val dotnetBaseFolder: Path,
    val version: DotnetDownloadVersion,
    val type: DotnetDownloadType,
    val rid: DotnetDownloadRid
)

enum class DotnetDownloadVersion(val version: String) {
    Version10("10.0"),
    Version9("9.0"),
    Version8("8.0"),
    Version7("7.0"),
    Version6("6.0"),
    Version5("5.0")
}

enum class DotnetDownloadType(val id: String) {
    Sdk("dotnet-sdk"),
    Runtime("dotnet-runtime"),
    AspNetRuntime("aspnetcore-runtime")
}

enum class DotnetDownloadRid(val id: String, val fileExtension: String) {
    LinuxX64("linux-x64", ".tar.gz"),
    LinuxArm64("linux-arm64", ".tar.gz"),
    WinX64("win-x64", ".zip"),
    WinArm64("win-arm64", ".zip"),
    MacOsX64("osx-x64", ".tar.gz"),
    MacOsArm64("osx-arm64", ".tar.gz")
}