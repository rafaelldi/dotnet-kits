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
import me.rafaelldi.dotnet.kits.core.receivingHub.*

class DownloadDotnetReleaseAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return

        val version = InboundCargoVersion.Version10
        val type = InboundCargoType.Sdk
        val rid = InboundCargoRid.LinuxX64

        val dialog = ReceiveInboundCargoDialog(project, version, type, rid)
        if (dialog.showAndGet()) {
            val model = dialog.getModel()

            val service = ReceivingHub.getInstance(project)
            currentThreadCoroutineScope().launch {
                val releaseFolder =
                    withBackgroundProgress(project, DotnetKitsCoreBundle.message("progress.download.dotnet")) {
                        service.receiveInboundCargo(
                            InboundCargoModel(model.version, model.type, model.rid)
                        )
                    }

                if (releaseFolder != null) {
                    Notification(
                        "Dotnet Kits",
                        DotnetKitsCoreBundle.message("notification.download.dotnet.succeeded"),
                        "",
                        NotificationType.INFORMATION
                    )
                        .addAction(object :
                            NotificationAction(DotnetKitsCoreBundle.message("notification.download.dotnet.succeeded.action")) {
                            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                                RevealFileAction.openFile(releaseFolder)
                            }
                        })
                        .notify(project)
                } else {
                    Notification(
                        "Dotnet Kits",
                        DotnetKitsCoreBundle.message("notification.download.dotnet.failed"),
                        "",
                        NotificationType.ERROR
                    )
                        .notify(project)
                }
            }
        }
    }

    override fun update(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}