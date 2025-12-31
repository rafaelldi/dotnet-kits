@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.util.ui.JBUI
import me.rafaelldi.dotnet.kits.core.DotnetKitsCoreBundle
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetRuntime
import org.jetbrains.jewel.bridge.toComposeColor
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.ActionButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

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
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DotnetRuntimeItem(
    dotnetRuntime: DotnetRuntime,
    viewModel: DotnetKitsViewModelApi,
    modifier: Modifier = Modifier
) {
    val runtimeShape = RoundedCornerShape(8.dp)

    val popupState = rememberPopupState()
    val itemPosition = remember { mutableStateOf(Offset.Zero) }
    val actionButtonPosition = remember { mutableStateOf(Offset.Zero) }
    val actionButtonSize = remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .contextMenuHandler(popupState, itemPosition),
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .background(JBUI.CurrentTheme.Banner.INFO_BACKGROUND.toComposeColor(), runtimeShape)
                .border(1.dp, JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR.toComposeColor(), runtimeShape)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DotnetArtifactVersion(dotnetRuntime)

                ActionButton(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Transparent)
                        .onGloballyPositioned { coordinates ->
                            actionButtonPosition.value = coordinates.positionInWindow()
                            actionButtonSize.value = coordinates.size
                        },
                    tooltip = { Text("Show options") },
                    onClick = {
                        val pos = actionButtonPosition.value
                        val size = actionButtonSize.value
                        popupState.show(
                            IntOffset(
                                x = pos.x.toInt(),
                                y = (pos.y + size.height).toInt()
                            )
                        )
                    },
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.More,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }

            DotnetRuntimeType(dotnetRuntime)

            DotnetArtifactPath(dotnetRuntime)
        }
    }

    if (popupState.isVisible) {
        val popupPositionProvider = rememberPopupPositionProvider(popupState)

        ContextPopupMenu(
            popupPositionProvider,
            onDismissRequest = {
                popupState.dismiss()
                itemPosition.value = Offset.Zero
            }
        ) {
            ContextPopupMenuItem(
                DotnetKitsCoreBundle.message("local.runtime.bubble.context.menu.delete.option"),
                AllIconsKeys.General.Delete
            ) {
                popupState.dismiss()
                viewModel.onDeleteRuntime(dotnetRuntime)
            }
        }
    }
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
