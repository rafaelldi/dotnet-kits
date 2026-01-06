package me.rafaelldi.dotnet.kits.frontend.toolWindow

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetArtifact
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetManagementApi
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetRuntime
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetSdk

internal interface DotnetKitsViewModelApi : Disposable {
    val dotnetSdkFlow: StateFlow<List<DotnetSdk>>
    val dotnetRuntimeFlow: StateFlow<List<DotnetRuntime>>
    fun onReloadLocalSdks()
    fun onReloadLocalRuntimes()
    fun onOpenArtifactFolder(artifact: DotnetArtifact)
    fun onDeleteArtifactFolder(artifact: DotnetArtifact)
}

internal class DotnetKitsViewModel(
    private val viewModelScope: CoroutineScope,
    private val dotnetManagement: DotnetManagementApi
) : DotnetKitsViewModelApi {

    private var currentReloadSdksJob: Job? = null
    private var currentReloadRuntimesJob: Job? = null

    private val _dotnetSdkFlow = MutableStateFlow(emptyList<DotnetSdk>())
    override val dotnetSdkFlow: StateFlow<List<DotnetSdk>> = _dotnetSdkFlow.asStateFlow()

    private val _dotnetRuntimeFlow = MutableStateFlow(emptyList<DotnetRuntime>())
    override val dotnetRuntimeFlow: StateFlow<List<DotnetRuntime>> = _dotnetRuntimeFlow.asStateFlow()

    override fun onReloadLocalSdks() {
        currentReloadSdksJob?.cancel()

        currentReloadSdksJob = viewModelScope.launch {
            val sdks = dotnetManagement.findDotnetSdks()
            _dotnetSdkFlow.value = sdks
        }
    }

    override fun onReloadLocalRuntimes() {
        currentReloadRuntimesJob?.cancel()

        currentReloadRuntimesJob = viewModelScope.launch {
            val runtimes = dotnetManagement.findDotnetRuntimes()
            _dotnetRuntimeFlow.value = runtimes
        }
    }

    override fun onOpenArtifactFolder(artifact: DotnetArtifact) {
        viewModelScope.launch {
            dotnetManagement.openArtifactFolder(artifact)
        }
    }

    override fun onDeleteArtifactFolder(artifact: DotnetArtifact) {
        viewModelScope.launch {
            dotnetManagement.deleteArtifactFolder(artifact)
        }
        when (artifact) {
            is DotnetSdk -> onReloadLocalSdks()
            is DotnetRuntime -> onReloadLocalRuntimes()
        }
    }

    override fun dispose() {
        viewModelScope.cancel()
    }
}