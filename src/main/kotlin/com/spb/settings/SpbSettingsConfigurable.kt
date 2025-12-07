package com.spb.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class SpbSettingsConfigurable : Configurable {

    private var mainPanel: JPanel? = null
    private var vsTelemetrySessionField: JBTextField? = null
    private var cliUpdateUrlField: JBTextField? = null
    private var cliDownloadPathField: TextFieldWithBrowseButton? = null
    private var enableAutoUpdateCheckBox: JBCheckBox? = null

    override fun getDisplayName(): String = "SPB Build"

    override fun createComponent(): JComponent {
        vsTelemetrySessionField = JBTextField()
        cliUpdateUrlField = JBTextField()
        cliDownloadPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select Download Path",
                "Select the file path where tracker.exe will be downloaded",
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }
        enableAutoUpdateCheckBox = JBCheckBox("Enable automatic update checks (once per day)")

        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("VSTEL_CurrentSolutionBuildID value:"), vsTelemetrySessionField!!, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("CLI update URL:"), cliUpdateUrlField!!, 1, false)
            .addLabeledComponent(JBLabel("CLI download path:"), cliDownloadPathField!!, 1, false)
            .addComponent(enableAutoUpdateCheckBox!!, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = SpbSettings.getInstance()
        return vsTelemetrySessionField?.text != settings.vsTelemetrySessionValue ||
                cliUpdateUrlField?.text != settings.cliUpdateUrl ||
                cliDownloadPathField?.text != settings.cliDownloadPath ||
                enableAutoUpdateCheckBox?.isSelected != settings.enableAutoUpdateCheck
    }

    override fun apply() {
        val settings = SpbSettings.getInstance()
        settings.vsTelemetrySessionValue = vsTelemetrySessionField?.text ?: "SPB"
        settings.cliUpdateUrl = cliUpdateUrlField?.text ?: ""
        settings.cliDownloadPath = cliDownloadPathField?.text ?: "D:\\superbuilder\\new_tracker.exe"
        settings.enableAutoUpdateCheck = enableAutoUpdateCheckBox?.isSelected ?: true
    }

    override fun reset() {
        val settings = SpbSettings.getInstance()
        vsTelemetrySessionField?.text = settings.vsTelemetrySessionValue
        cliUpdateUrlField?.text = settings.cliUpdateUrl
        cliDownloadPathField?.text = settings.cliDownloadPath
        enableAutoUpdateCheckBox?.isSelected = settings.enableAutoUpdateCheck
    }

    override fun disposeUIResources() {
        mainPanel = null
        vsTelemetrySessionField = null
        cliUpdateUrlField = null
        cliDownloadPathField = null
        enableAutoUpdateCheckBox = null
    }
}
