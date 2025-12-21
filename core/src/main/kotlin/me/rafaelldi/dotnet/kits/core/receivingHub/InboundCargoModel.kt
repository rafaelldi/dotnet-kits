package me.rafaelldi.dotnet.kits.core.receivingHub

internal data class InboundCargoModel(
    val version: InboundCargoVersion,
    val type: InboundCargoType,
    val rid: InboundCargoRid
)

internal enum class InboundCargoVersion(val version: String) {
    Version10("10.0"),
    Version9("9.0"),
    Version8("8.0"),
    Version7("7.0"),
    Version6("6.0"),
    Version5("5.0")
}

internal enum class InboundCargoType(val id: String) {
    Sdk("dotnet-sdk"),
    Runtime("dotnet-runtime"),
    AspNetRuntime("aspnetcore-runtime")
}

internal enum class InboundCargoRid(val id: String, val fileExtension: String) {
    LinuxX64("linux-x64", ".tar.gz"),
    LinuxArm64("linux-arm64", ".tar.gz"),
    WinX64("win-x64", ".zip"),
    WinArm64("win-arm64", ".zip"),
    MacOsX64("osx-x64", ".tar.gz"),
    MacOsArm64("osx-arm64", ".tar.gz")
}