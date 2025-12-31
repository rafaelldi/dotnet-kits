@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.kits.core.toolWindow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.rafaelldi.dotnet.kits.core.DotnetKitsCoreBundle
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetRuntime
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

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
    DotnetArtifactCard(
        artifact = dotnetRuntime,
        deleteMenuText = DotnetKitsCoreBundle.message("local.runtime.bubble.context.menu.delete.option"),
        onDelete = { viewModel.onDeleteRuntime(it) },
        modifier = modifier
    )
}
