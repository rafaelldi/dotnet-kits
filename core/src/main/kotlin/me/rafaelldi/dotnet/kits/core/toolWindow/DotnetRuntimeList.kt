@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.util.ui.JBUI
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetRuntime
import org.jetbrains.jewel.bridge.toComposeColor
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun DotnetRuntimeList(viewModel: DotnetKitsViewModelApi) {
    val dotnetRuntimes by viewModel.dotnetRuntimeFlow.collectAsState(emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onReloadLocalRuntimes()
    }

    Column(
        Modifier
            .fillMaxWidth()
    ) {
        DotnetArtifactColumn(
            artifacts = dotnetRuntimes,
            listState = listState,
            emptyPlaceholderText = "Unable to find local .NET Runtimes.",
            modifier = Modifier.fillMaxSize()
        ) { runtime ->
            DotnetRuntimeItem(
                dotnetRuntime = runtime,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DotnetRuntimeItem(
    dotnetRuntime: DotnetRuntime,
    modifier: Modifier = Modifier
) {
    val runtimeShape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .background(JBUI.CurrentTheme.Banner.INFO_BACKGROUND.toComposeColor(), runtimeShape)
                .border(1.dp, JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR.toComposeColor(), runtimeShape)
                .padding(16.dp)
        ) {
            DotnetArtifactVersion(dotnetRuntime)

            DotnetRuntimeType(dotnetRuntime)

            DotnetArtifactPath(dotnetRuntime)
        }
    }
}

@Composable
private fun DotnetArtifactVersion(dotnetArtifact: DotnetArtifact) {
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
private fun DotnetRuntimeType(dotnetRuntime: DotnetRuntime) {
    Text(
        text = dotnetRuntime.type,
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = JewelTheme.globalColors.text.info,
            lineHeight = 16.sp
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun DotnetArtifactPath(dotnetArtifact: DotnetArtifact) {
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
