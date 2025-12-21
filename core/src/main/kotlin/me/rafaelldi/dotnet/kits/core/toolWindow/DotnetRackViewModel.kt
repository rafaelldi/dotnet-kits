package me.rafaelldi.dotnet.kits.core.toolWindow

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.kits.core.forklift.DotnetForkliftApi
import me.rafaelldi.dotnet.kits.core.forklift.DotnetSdk

internal interface DotnetRackViewModelApi : Disposable {
    val dotnetSdkFlow: StateFlow<List<DotnetSdk>>
    fun onReloadLocalSdks()
    fun onDeleteSdk(dotnetSdk: DotnetSdk)
}

internal class DotnetRackViewModel(
    private val viewModelScope: CoroutineScope,
    private val dotnetForklift: DotnetForkliftApi
) : DotnetRackViewModelApi {

    private var currentReloadSdksJob: Job? = null

    private val _dotnetSdkFlow = MutableStateFlow(emptyList<DotnetSdk>())
    override val dotnetSdkFlow: StateFlow<List<DotnetSdk>> = _dotnetSdkFlow.asStateFlow()

    init {
        dotnetForklift
            .dotnetSdkFlow
            .onEach { _dotnetSdkFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    override fun onReloadLocalSdks() {
        currentReloadSdksJob?.cancel()

        currentReloadSdksJob = viewModelScope.launch {
            dotnetForklift.reloadDotnetSdks()
        }
    }

    override fun onDeleteSdk(dotnetSdk: DotnetSdk) {
        viewModelScope.launch {
            dotnetForklift.deleteSdk(dotnetSdk)
        }
        onReloadLocalSdks()
    }

    override fun dispose() {
        viewModelScope.cancel()
    }
}