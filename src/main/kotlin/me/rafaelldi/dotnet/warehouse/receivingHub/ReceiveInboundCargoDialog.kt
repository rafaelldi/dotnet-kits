package me.rafaelldi.dotnet.warehouse.receivingHub

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import me.rafaelldi.dotnet.warehouse.WarehouseBundle

internal class ReceiveInboundCargoDialog(
    project: Project,
    version: InboundCargoVersion,
    type: InboundCargoType,
    rid: InboundCargoRid
) : DialogWrapper(project) {

    private val model = ReceiveInboundCargoModel(version, type, rid)

    init {
        init()
        title = WarehouseBundle.message("dialog.download.dotnet.title")
        setOKButtonText(WarehouseBundle.message("dialog.download.dotnet.ok.button"))
    }

    override fun createCenterPanel() = panel {
        row(WarehouseBundle.message("dialog.download.dotnet.version")) {
            comboBox(InboundCargoVersion.entries.toList(), SimpleListCellRenderer.create("") { it.version })
                .bindItem(model::version.toNullableProperty())
        }
        row(WarehouseBundle.message("dialog.download.dotnet.type")) {
            comboBox(InboundCargoType.entries.toList(), SimpleListCellRenderer.create("") { it.id })
                .bindItem(model::type.toNullableProperty())
        }
        row(WarehouseBundle.message("dialog.download.dotnet.rid")) {
            comboBox(InboundCargoRid.entries.toList(), SimpleListCellRenderer.create("") { it.id })
                .bindItem(model::rid.toNullableProperty())
        }
    }

    fun getModel() = model
}

internal data class ReceiveInboundCargoModel(
    var version: InboundCargoVersion,
    var type: InboundCargoType,
    var rid: InboundCargoRid
)