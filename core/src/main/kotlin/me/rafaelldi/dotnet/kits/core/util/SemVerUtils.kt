package me.rafaelldi.dotnet.kits.core.util

/**
 * Utilities for working with semantic versions.
 */

private val semanticVersionRegex = Regex("""(\d+)\.(\d+)\.(\d+)""")

internal fun parseSemanticVersion(version: String): Triple<Int, Int, Int> {
    val match = semanticVersionRegex.find(version.trim())
    return if (match != null) {
        val (majorStr, minorStr, patchStr) = match.destructured
        Triple(
            majorStr.toIntOrNull() ?: 0,
            minorStr.toIntOrNull() ?: 0,
            patchStr.toIntOrNull() ?: 0
        )
    } else Triple(0, 0, 0)
}

internal fun extractPreRelease(version: String): String? {
    val hyphenIndex = version.indexOf('-')
    return if (hyphenIndex >= 0) {
        version.substring(hyphenIndex + 1).takeIf { it.isNotBlank() }
    } else {
        null
    }
}
