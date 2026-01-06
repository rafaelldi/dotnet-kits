@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.kits.frontend.toolWindow

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import me.rafaelldi.dotnet.kits.frontend.common.DotnetKitsTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun DotnetArtifactVersion(dotnetArtifact: DotnetArtifact) {
    Text(
        text = dotnetArtifact.version,
        style = DotnetKitsTheme.Typography.artifactVersionStyle(),
        modifier = Modifier.padding(
            top = DotnetKitsTheme.Spacing.xxSmall,
            bottom = DotnetKitsTheme.Spacing.xSmall
        )
    )
}

@Composable
internal fun DotnetArtifactPath(dotnetArtifact: DotnetArtifact) {
    Text(
        text = dotnetArtifact.pathString,
        style = DotnetKitsTheme.Typography.artifactPathStyle()
    )
}
