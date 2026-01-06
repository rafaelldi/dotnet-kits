package me.rafaelldi.dotnet.kits.frontend.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.kits.core.omnisharp.OmnisharpManagementService
import me.rafaelldi.dotnet.kits.frontend.DotnetKitsFrontendBundle

internal class DownloadOmnisharpReleaseAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return

        currentThreadCoroutineScope().launch {
            val service = OmnisharpManagementService.getInstance(project)
            withBackgroundProgress(project, DotnetKitsFrontendBundle.message("progress.download.omnisharp")) {
                service.downloadLatestRelease()
            }
        }
    }

    override fun update(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}