@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.kits.frontend.toolWindow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

/**
 * Generic card component for displaying .NET artifacts with interactive states.
 *
 * @param artifact The artifact to display
 * @param deleteMenuText Text for the delete context menu option
 * @param onDelete Callback invoked when delete is selected
 * @param modifier Modifier for the card container
 */
@Composable
internal fun <T : DotnetArtifact> DotnetArtifactCard(
    artifact: T,
    deleteMenuText: String,
    onDelete: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val popupState = rememberPopupState()
    val itemPosition = remember { mutableStateOf(Offset.Zero) }
    val actionButtonPosition = remember { mutableStateOf(Offset.Zero) }
    val actionButtonSize = remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isHovered -> DotnetKitsTheme.Colors.cardHoverBackground()
        else -> DotnetKitsTheme.Colors.cardBackground()
    }

    Row(
        modifier = modifier
            .padding(
                horizontal = DotnetKitsTheme.Spacing.cardPaddingHorizontal,
                vertical = DotnetKitsTheme.Spacing.cardPaddingVertical
            )
            .contextMenuHandler(popupState, itemPosition),
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .hoverable(interactionSource = interactionSource)
                .background(backgroundColor, DotnetKitsTheme.Shapes.cardShape)
                .border(
                    DotnetKitsTheme.Sizes.borderWidthDefault,
                    DotnetKitsTheme.Colors.cardBorder(),
                    DotnetKitsTheme.Shapes.cardShape
                )
                .padding(DotnetKitsTheme.Spacing.cardInnerPadding)
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
                    tooltip = {
                        Text(
                            "Show options",
                            style = DotnetKitsTheme.Typography.tooltipStyle()
                        )
                    },
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
