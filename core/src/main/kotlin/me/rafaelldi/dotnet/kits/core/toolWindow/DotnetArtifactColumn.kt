@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer

@Composable
internal fun <T : DotnetArtifact> DotnetArtifactColumn(
    artifacts: List<T>,
    listState: LazyListState,
    emptyPlaceholderText: String,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = modifier) {
        if (artifacts.isEmpty()) {
            EmptyArtifactListPlaceholder(emptyPlaceholderText)
        } else {
            VerticallyScrollableContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeContentPadding(),
                scrollState = listState,
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(artifacts, key = { it.pathString }) { artifact ->
                        itemContent(artifact)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyArtifactListPlaceholder(
    placeholderText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = placeholderText,
            style = JewelTheme.defaultTextStyle.copy(
                color = JewelTheme.globalColors.text.disabled,
                fontSize = 16.sp
            )
        )
    }
}
