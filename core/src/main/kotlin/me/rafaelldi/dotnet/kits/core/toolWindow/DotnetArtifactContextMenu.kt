@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

/**
 * Modifier that handles right-click detection and triggers popup at click position.
 *
 * @param popupState The state holder for popup visibility and position
 * @param itemPosition Mutable state to track the item's window position
 */
internal fun Modifier.contextMenuHandler(
    popupState: PopupState,
    itemPosition: MutableState<Offset>
): Modifier = this
    .onGloballyPositioned { coordinates ->
        itemPosition.value = coordinates.positionInWindow()
    }
    .onPointerEvent(PointerEventType.Press) { pointerEvent ->
        if (!pointerEvent.buttons.isSecondaryPressed) return@onPointerEvent

        val pos = itemPosition.value
        val clickOffset = pointerEvent.changes.first().position
        popupState.show(
            IntOffset(
                x = (pos.x + clickOffset.x).toInt(),
                y = (pos.y + clickOffset.y).toInt()
            )
        )
    }

/**
 * Create a PopupPositionProvider from the popup state.
 */
@Composable
internal fun rememberPopupPositionProvider(popupState: PopupState): PopupPositionProvider {
    return remember(popupState.position) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = popupState.position
        }
    }
}
