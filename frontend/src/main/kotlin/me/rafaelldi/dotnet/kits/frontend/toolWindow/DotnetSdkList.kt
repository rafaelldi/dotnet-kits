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
            DotnetArtifactCard(
                artifact = sdk,
                onOpenFolder = { viewModel.onOpenArtifactFolder(it) },
                onDeleteFolder = { viewModel.onDeleteArtifactFolder(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
