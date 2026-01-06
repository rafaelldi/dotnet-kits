package me.rafaelldi.dotnet.kits.frontend.dotnetDownload

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetArtifactModel
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadRid
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadType
import me.rafaelldi.dotnet.kits.core.dotnetDownload.DotnetDownloadVersion
import me.rafaelldi.dotnet.kits.frontend.DotnetKitsFrontendBundle
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class DownloadDotnetArtifactDialog(
    private val project: Project,
    model: DotnetArtifactModel
) : DialogWrapper(project) {

    private var dotnetBaseFolder = model.dotnetBaseFolder.absolutePathString()
    private var version = model.version
    private var type = model.type
    private var rid = model.rid

    init {
        init()
        title = DotnetKitsFrontendBundle.message("dialog.download.dotnet.title")
        setOKButtonText(DotnetKitsFrontendBundle.message("dialog.download.dotnet.ok.button"))
    }

    @Suppress("UnstableApiUsage")
    override fun createCenterPanel() = panel {
        row(DotnetKitsFrontendBundle.message("dialog.download.dotnet.folder")) {
            textFieldWithBrowseButton(FileChooserDescriptorFactory.singleDir(), project)
                .bindText(::dotnetBaseFolder)
        }
        row(DotnetKitsFrontendBundle.message("dialog.download.dotnet.version")) {
            comboBox(DotnetDownloadVersion.entries.toList(), SimpleListCellRenderer.create("") { it.version })
                .bindItem(::version.toNullableProperty())
        }
        row(DotnetKitsFrontendBundle.message("dialog.download.dotnet.type")) {
            comboBox(DotnetDownloadType.entries.toList(), SimpleListCellRenderer.create("") { it.id })
                .bindItem(::type.toNullableProperty())
        }
        row(DotnetKitsFrontendBundle.message("dialog.download.dotnet.rid")) {
            comboBox(DotnetDownloadRid.entries.toList(), SimpleListCellRenderer.create("") { it.id })
                .bindItem(::rid.toNullableProperty())
        }
    }

    fun getModel() = DotnetArtifactModel(
        Path.of(dotnetBaseFolder),
        version,
        type,
        rid
    )
}