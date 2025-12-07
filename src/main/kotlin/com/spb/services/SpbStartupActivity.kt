package com.spb.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SpbStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Check for updates once per day when the IDE starts with a project
        VersionCheckService.getInstance().checkForUpdates(project)
    }
}
