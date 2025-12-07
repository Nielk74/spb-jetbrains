package com.spb.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.spb.services.SpbBuildService

class SpbBuildProjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val projectFile = findProjectFile(file)
        if (projectFile != null) {
            SpbBuildService.buildWithVsTelemetry(project, projectFile, isSolution = false)
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isProjectFile = file != null && isProjectFile(file)
        e.presentation.isEnabledAndVisible = e.project != null && isProjectFile
    }

    private fun findProjectFile(file: VirtualFile): VirtualFile? {
        if (isProjectFile(file)) {
            return file
        }
        // If a directory is selected, look for project files inside
        if (file.isDirectory) {
            return file.children.firstOrNull { isProjectFile(it) }
        }
        return null
    }

    private fun isProjectFile(file: VirtualFile): Boolean {
        val extension = file.extension?.lowercase() ?: return false
        return extension in listOf("csproj", "vbproj", "fsproj", "vcxproj")
    }
}
