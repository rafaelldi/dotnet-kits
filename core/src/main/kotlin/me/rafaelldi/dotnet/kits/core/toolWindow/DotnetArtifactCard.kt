@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.intellij.util.ui.JBUI
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import org.jetbrains.jewel.bridge.toComposeColor
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

/**
 * Generic card component for displaying .NET artifacts with context menu support.
 */
@Composable
internal fun <T : DotnetArtifact> DotnetArtifactCard(
    artifact: T,
    deleteMenuText: String,
    onDelete: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(8.dp)

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
                .background(JBUI.CurrentTheme.Banner.INFO_BACKGROUND.toComposeColor(), cardShape)
                .border(1.dp, JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR.toComposeColor(), cardShape)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DotnetArtifactVersion(artifact)

                IconActionButton(
                    key = AllIconsKeys.Actions.More,
                    contentDescription = "Options",
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
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            actionButtonPosition.value = coordinates.positionInWindow()
                            actionButtonSize.value = coordinates.size
                        },
                    tooltip = { Text("Show options") },
                )
            }

            DotnetArtifactPath(artifact)
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
                deleteMenuText,
                AllIconsKeys.General.Delete
            ) {
                popupState.dismiss()
                onDelete(artifact)
            }
        }
    }
}
