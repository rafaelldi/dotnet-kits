package me.rafaelldi.dotnet.kits.frontend.toolWindow

import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset

/**
 * State holder for context menu popup positioning and visibility.
 * Replaces multiple separate MutableState variables with a cohesive API.
 */
@Stable
internal class PopupState {
    var isVisible by mutableStateOf(false)
        private set

    var position by mutableStateOf(IntOffset.Zero)
        private set

    /**
     * Show the popup at the specified position.
     */
    fun show(offset: IntOffset) {
        position = offset
        isVisible = true
    }

    /**
     * Dismiss the popup and reset the position.
     */
    fun dismiss() {
        isVisible = false
        position = IntOffset.Zero
    }
}

/**
 * Remember a PopupState across recompositions.
 */
@Composable
internal fun rememberPopupState(): PopupState {
    return remember { PopupState() }
}
