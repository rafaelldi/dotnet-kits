package me.rafaelldi.dotnet.kits.core.toolWindow

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetManagementApi
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetSdk

internal interface DotnetKitsViewModelApi : Disposable {
    val dotnetSdkFlow: StateFlow<List<DotnetSdk>>
    fun onReloadLocalSdks()
    fun onDeleteSdk(dotnetSdk: DotnetSdk)
}

internal class DotnetKitsViewModel(
    private val viewModelScope: CoroutineScope,
    private val dotnetManagement: DotnetManagementApi
) : DotnetKitsViewModelApi {

    private var currentReloadSdksJob: Job? = null

    private val _dotnetSdkFlow = MutableStateFlow(emptyList<DotnetSdk>())
    override val dotnetSdkFlow: StateFlow<List<DotnetSdk>> = _dotnetSdkFlow.asStateFlow()

    init {
        dotnetManagement
            .dotnetSdkFlow
            .onEach { _dotnetSdkFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    override fun onReloadLocalSdks() {
        currentReloadSdksJob?.cancel()

        currentReloadSdksJob = viewModelScope.launch {
            dotnetManagement.reloadDotnetSdks()
        }
    }

    override fun onDeleteSdk(dotnetSdk: DotnetSdk) {
        viewModelScope.launch {
            dotnetManagement.deleteSdk(dotnetSdk)
        }
        onReloadLocalSdks()
    }

    override fun dispose() {
        viewModelScope.cancel()
    }
}