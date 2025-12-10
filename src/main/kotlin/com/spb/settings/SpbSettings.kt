package com.spb.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.spb.settings.SpbSettings",
    storages = [Storage("SpbBuildSettings.xml")]
)
class SpbSettings : PersistentStateComponent<SpbSettings> {

    var cliUpdateUrl: String = "http://winsuperbuilder.dev.echonet:15000/resources/tracker.exe.custom"
    var cliDownloadPath: String = "D:\\superbuilder\\tracker.exe"
    var lastCliModified: Long = 0
    var lastUpdateCheck: Long = 0
    var enableAutoUpdateCheck: Boolean = true

    override fun getState(): SpbSettings = this

    override fun loadState(state: SpbSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): SpbSettings {
            return ApplicationManager.getApplication().getService(SpbSettings::class.java)
        }
    }
}
