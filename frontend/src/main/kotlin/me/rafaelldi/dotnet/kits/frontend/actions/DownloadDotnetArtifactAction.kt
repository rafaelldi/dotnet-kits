package me.rafaelldi.dotnet.kits.frontend.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DownloadDotnetArtifactService

internal class DownloadDotnetArtifactAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return

        currentThreadCoroutineScope().launch {
            val service = DownloadDotnetArtifactService.getInstance(project)
            service.download()
        }
    }

    override fun update(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}