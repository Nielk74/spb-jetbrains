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
    private var downloadPathField: TextFieldWithBrowseButton? = null
    private var updateCheckUrlField: JBTextField? = null
    private var enableAutoUpdateCheckBox: JBCheckBox? = null

    override fun getDisplayName(): String = "SPB Build"

    override fun createComponent(): JComponent {
        vsTelemetrySessionField = JBTextField()
        downloadPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select Download Path",
                "Select the directory where SPB tools will be downloaded",
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
            )
        }
        updateCheckUrlField = JBTextField()
        enableAutoUpdateCheckBox = JBCheckBox("Enable automatic update checks (once per day)")

        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("VSTelemetrySession value:"), vsTelemetrySessionField!!, 1, false)
            .addLabeledComponent(JBLabel("Download path:"), downloadPathField!!, 1, false)
            .addLabeledComponent(JBLabel("Update check URL:"), updateCheckUrlField!!, 1, false)
            .addComponent(enableAutoUpdateCheckBox!!, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = SpbSettings.getInstance()
        return vsTelemetrySessionField?.text != settings.vsTelemetrySessionValue ||
                downloadPathField?.text != settings.downloadPath ||
                updateCheckUrlField?.text != settings.updateCheckUrl ||
                enableAutoUpdateCheckBox?.isSelected != settings.enableAutoUpdateCheck
    }

    override fun apply() {
        val settings = SpbSettings.getInstance()
        settings.vsTelemetrySessionValue = vsTelemetrySessionField?.text ?: "SPB"
        settings.downloadPath = downloadPathField?.text ?: System.getProperty("user.home") + "/spb-tools"
        settings.updateCheckUrl = updateCheckUrlField?.text ?: ""
        settings.enableAutoUpdateCheck = enableAutoUpdateCheckBox?.isSelected ?: true
    }

    override fun reset() {
        val settings = SpbSettings.getInstance()
        vsTelemetrySessionField?.text = settings.vsTelemetrySessionValue
        downloadPathField?.text = settings.downloadPath
        updateCheckUrlField?.text = settings.updateCheckUrl
        enableAutoUpdateCheckBox?.isSelected = settings.enableAutoUpdateCheck
    }

    override fun disposeUIResources() {
        mainPanel = null
        vsTelemetrySessionField = null
        downloadPathField = null
        updateCheckUrlField = null
        enableAutoUpdateCheckBox = null
    }
}
