@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.kits.frontend.toolWindow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetSdk
import me.rafaelldi.dotnet.kits.frontend.DotnetKitsFrontendBundle
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

@Composable
internal fun DotnetSdkList(viewModel: DotnetKitsViewModelApi) {
    val dotnetSdks by viewModel.dotnetSdkFlow.collectAsState(emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onReloadLocalSdks()
    }

    Column(
        Modifier
            .fillMaxWidth()
    ) {
        DotnetArtifactColumn(
            artifacts = dotnetSdks,
            listState = listState,
            emptyPlaceholderText = "Unable to find local .NET SDKs.",
            modifier = Modifier.fillMaxSize()
        ) { sdk ->
            DotnetSdkItem(
                sdk,
                viewModel,
                Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DotnetSdkItem(
    dotnetSdk: DotnetSdk,
    viewModel: DotnetKitsViewModelApi,
    modifier: Modifier = Modifier
) {
    DotnetArtifactCard(
        artifact = dotnetSdk,
        deleteMenuText = DotnetKitsFrontendBundle.message("local.sdk.bubble.context.menu.delete.option"),
        onDelete = { viewModel.onDeleteSdk(it) },
        modifier = modifier
    )
}
