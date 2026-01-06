package me.rafaelldi.dotnet.kits.frontend.actions

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DownloadDotnetArtifactService
import me.rafaelldi.dotnet.kits.frontend.DotnetKitsFrontendBundle
import me.rafaelldi.dotnet.kits.frontend.dotnetDownload.DownloadDotnetArtifactDialog
import java.nio.file.Path

internal class DownloadDotnetArtifactAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return

        currentThreadCoroutineScope().launch {
            val service = DownloadDotnetArtifactService.getInstance(project)
            val defaultModel = service.getDefaultDownloadModel() ?: return@launch

            val model = withContext(Dispatchers.EDT) {
                val dialog = DownloadDotnetArtifactDialog(project, defaultModel)
                val result = dialog.showAndGet()
                if (result) dialog.getModel() else null
            } ?: return@launch

            val releaseFolder =  withBackgroundProgress(project, DotnetKitsFrontendBundle.message("progress.download.dotnet")) {
                service.download(model)
            }
            showResultNotification(releaseFolder, project)
        }
    }

    private suspend fun showResultNotification(releaseFolder: Result<Path>, project: Project) {
        withContext(Dispatchers.EDT) {
            releaseFolder.fold({
                Notification(
                    "Dotnet Kits",
                    DotnetKitsFrontendBundle.message("notification.download.dotnet.succeeded"),
                    "",
                    NotificationType.INFORMATION
                )
                    .addAction(object :
                        NotificationAction(DotnetKitsFrontendBundle.message("notification.download.dotnet.succeeded.action")) {
                        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                            RevealFileAction.openFile(it)
                        }
                    })
                    .notify(project)
            }, {
                Notification(
                    "Dotnet Kits",
                    DotnetKitsFrontendBundle.message("notification.download.dotnet.failed"),
                    it.message ?: "",
                    NotificationType.ERROR
                )
                    .notify(project)
            })
        }
    }

    override fun update(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}