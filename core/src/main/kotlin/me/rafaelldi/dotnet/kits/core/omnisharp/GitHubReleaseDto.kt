package me.rafaelldi.dotnet.kits.core.omnisharp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String,
    @SerialName("prerelease")
    val prerelease: Boolean,
    @SerialName("draft")
    val draft: Boolean,
    @SerialName("published_at")
    val publishedAt: String
)
