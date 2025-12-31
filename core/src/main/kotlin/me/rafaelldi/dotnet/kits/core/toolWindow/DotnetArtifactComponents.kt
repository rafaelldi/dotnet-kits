@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun DotnetArtifactVersion(dotnetArtifact: DotnetArtifact) {
    Text(
        text = dotnetArtifact.version,
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = JewelTheme.globalColors.text.normal,
            lineHeight = 20.sp
        ),
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
internal fun DotnetArtifactPath(dotnetArtifact: DotnetArtifact) {
    Text(
        text = dotnetArtifact.pathString,
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = JewelTheme.globalColors.text.info,
            lineHeight = 20.sp
        )
    )
}
