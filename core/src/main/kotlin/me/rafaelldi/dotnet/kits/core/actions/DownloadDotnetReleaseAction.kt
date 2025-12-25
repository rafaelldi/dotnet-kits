package me.rafaelldi.dotnet.kits.core.actions

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.kits.core.DotnetKitsCoreBundle
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadDialog
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadModel
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadRid
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadService
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadType
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadVersion

internal class DownloadDotnetReleaseAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return

        val version = DotnetDownloadVersion.Version10
        val type = DotnetDownloadType.Sdk
        val rid = DotnetDownloadRid.LinuxX64

        val dialog = DotnetDownloadDialog(project, version, type, rid)
        if (dialog.showAndGet()) {
            val model = dialog.getModel()

            val service = DotnetDownloadService.getInstance(project)
            currentThreadCoroutineScope().launch {
                val releaseFolder =
                    withBackgroundProgress(project, DotnetKitsCoreBundle.message("progress.download.dotnet")) {
                        service.download(
                            DotnetDownloadModel(model.version, model.type, model.rid)
                        )
                    }

                releaseFolder.fold({
                    Notification(
                        "Dotnet Kits",
                        DotnetKitsCoreBundle.message("notification.download.dotnet.succeeded"),
                        "",
                        NotificationType.INFORMATION
                    )
                        .addAction(object :
                            NotificationAction(DotnetKitsCoreBundle.message("notification.download.dotnet.succeeded.action")) {
                            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                                RevealFileAction.openFile(it)
                            }
                        })
                        .notify(project)
                }, {
                    Notification(
                        "Dotnet Kits",
                        DotnetKitsCoreBundle.message("notification.download.dotnet.failed"),
                        it.message ?: "",
                        NotificationType.ERROR
                    )
                        .notify(project)
                })
            }
        }
    }

    override fun update(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}