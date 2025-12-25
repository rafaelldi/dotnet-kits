package me.rafaelldi.dotnet.kits.core.dotnetDownload

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import me.rafaelldi.dotnet.kits.core.DotnetKitsCoreBundle

internal class DownloadDotnetArtifactDialog(
    project: Project,
    version: DotnetDownloadVersion,
    type: DotnetDownloadType,
    rid: DotnetDownloadRid
) : DialogWrapper(project) {

    private val model = DownloadDotnetArtifactDialogModel(version, type, rid)

    init {
        init()
        title = DotnetKitsCoreBundle.message("dialog.download.dotnet.title")
        setOKButtonText(DotnetKitsCoreBundle.message("dialog.download.dotnet.ok.button"))
    }

    override fun createCenterPanel() = panel {
        row(DotnetKitsCoreBundle.message("dialog.download.dotnet.version")) {
            comboBox(DotnetDownloadVersion.entries.toList(), SimpleListCellRenderer.create("") { it.version })
                .bindItem(model::version.toNullableProperty())
        }
        row(DotnetKitsCoreBundle.message("dialog.download.dotnet.type")) {
            comboBox(DotnetDownloadType.entries.toList(), SimpleListCellRenderer.create("") { it.id })
                .bindItem(model::type.toNullableProperty())
        }
        row(DotnetKitsCoreBundle.message("dialog.download.dotnet.rid")) {
            comboBox(DotnetDownloadRid.entries.toList(), SimpleListCellRenderer.create("") { it.id })
                .bindItem(model::rid.toNullableProperty())
        }
    }

    fun getModel() = model
}

internal data class DownloadDotnetArtifactDialogModel(
    var version: DotnetDownloadVersion,
    var type: DotnetDownloadType,
    var rid: DotnetDownloadRid
)