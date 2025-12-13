package me.rafaelldi.dotnet.warehouse.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.warehouse.receivingHub.InboundCargoModel
import me.rafaelldi.dotnet.warehouse.receivingHub.InboundCargoRid
import me.rafaelldi.dotnet.warehouse.receivingHub.InboundCargoType
import me.rafaelldi.dotnet.warehouse.receivingHub.InboundCargoVersion
import me.rafaelldi.dotnet.warehouse.receivingHub.ReceivingHub

class DownloadDotnetReleaseAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        val service = ReceivingHub.getInstance(project)
        currentThreadCoroutineScope().launch {
            service.receiveInboundCargo(
                InboundCargoModel(
                    InboundCargoVersion.Version10,
                    InboundCargoType.Sdk,
                    InboundCargoRid.LinuxX64
                )
            )
        }
    }

    override fun update(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}