package me.rafaelldi.dotnet.kits.core.dotnetDownload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DotnetReleaseIndex(
    @SerialName("releases-index")
    val versions: List<DotnetReleaseVersion>
)

@Serializable
internal data class DotnetReleaseVersion(
    @SerialName("channel-version")
    val channelVersion: String,
    @SerialName("latest-release")
    val latestRelease: String,
    @SerialName("latest-release-date")
    val latestReleaseDate: String,
    @SerialName("latest-runtime")
    val latestRuntime: String,
    @SerialName("latest-sdk")
    val latestSdk: String,
    @SerialName("release-type")
    val releaseType: String,
    @SerialName("releases.json")
    val releasesJson: String
)

@Serializable
internal data class DotnetReleaseVersionIndex(
    @SerialName("channel-version")
    val channelVersion: String,
    @SerialName("latest-release")
    val latestRelease: String,
    @SerialName("latest-release-date")
    val latestReleaseDate: String,
    @SerialName("latest-runtime")
    val latestRuntime: String,
    @SerialName("latest-sdk")
    val latestSdk: String,
    @SerialName("release-type")
    val releaseType: String,
    @SerialName("releases")
    val releases: List<DotnetRelease>
)

@Serializable
internal data class DotnetRelease(
    @SerialName("release-date")
    val releaseDate: String,
    @SerialName("release-version")
    val releaseVersion: String,
    @SerialName("sdk")
    val sdk: DotnetReleaseSdk,
    @SerialName("runtime")
    val runtime: DotnetReleaseRuntime,
    @SerialName("aspnetcore-runtime")
    val aspNetCoreRuntime: DotnetReleaseAspNetCoreRuntime,
)

@Serializable
internal data class DotnetReleaseSdk(
    @SerialName("version")
    val version: String,
    @SerialName("files")
    val files: List<DotnetReleaseFile>
)

@Serializable
internal data class DotnetReleaseRuntime(
    @SerialName("version")
    val version: String,
    @SerialName("files")
    val files: List<DotnetReleaseFile>
)

@Serializable
internal data class DotnetReleaseAspNetCoreRuntime(
    @SerialName("version")
    val version: String,
    @SerialName("files")
    val files: List<DotnetReleaseFile>
)

@Serializable
internal data class DotnetReleaseFile(
    @SerialName("name")
    val name: String,
    @SerialName("rid")
    val rid: String,
    @SerialName("url")
    val url: String
)