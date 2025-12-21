package me.rafaelldi.dotnet.kits.core.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import me.rafaelldi.dotnet.kits.core.DotnetKitsService
import me.rafaelldi.dotnet.kits.core.dotnetManagement.DotnetManagementService
import org.jetbrains.jewel.bridge.addComposeTab


internal class DotnetKitsToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val viewModel = DotnetKitsViewModel(
            project.service<DotnetKitsService>().createScope(::DotnetKitsViewModel.name),
            DotnetManagementService.getInstance(project)
        )
        Disposer.register(toolWindow.disposable, viewModel)

        toolWindow.addComposeTab("SDKs", focusOnClickInside = true) {
            DotnetSdkList(viewModel)
        }
    }
}
