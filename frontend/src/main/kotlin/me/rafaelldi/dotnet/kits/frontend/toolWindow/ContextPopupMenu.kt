package me.rafaelldi.dotnet.kits.frontend.toolWindow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupPositionProvider
import me.rafaelldi.dotnet.kits.frontend.common.DotnetKitsTheme
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icon.IconKey

@Composable
internal fun ContextPopupMenu(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    PopupContainer(
        popupPositionProvider = popupPositionProvider,
        modifier = Modifier.wrapContentSize(),
        onDismissRequest = { onDismissRequest() },
        horizontalAlignment = Alignment.Start
    ) {
        Column {
            content()
        }
    }
}

@Composable
internal fun ContextPopupMenuItem(
    actionText: String,
    actionIcon: IconKey? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .widthIn(min = DotnetKitsTheme.Sizes.menuMinWidth)
            .padding(DotnetKitsTheme.Spacing.menuItemPadding)
            .onClick { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (actionIcon != null) {
            Icon(
                actionIcon,
                contentDescription = null,
                modifier = Modifier.size(DotnetKitsTheme.Sizes.iconSmall)
            )

            Spacer(modifier = Modifier.width(DotnetKitsTheme.Spacing.iconSpacing))
        }

        Text(
            text = actionText,
            style = JewelTheme.defaultTextStyle
        )
    }
}