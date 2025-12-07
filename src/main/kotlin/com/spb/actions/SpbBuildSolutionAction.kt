package com.spb.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.spb.services.SpbBuildService

class SpbBuildSolutionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val solutionFile = findSolutionFile(file)
        if (solutionFile != null) {
            SpbBuildService.buildWithVsTelemetry(project, solutionFile, isSolution = true)
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isSolutionFile = file != null && (isSolutionFile(file) || containsSolutionFile(file))
        e.presentation.isEnabledAndVisible = e.project != null && isSolutionFile
    }

    private fun findSolutionFile(file: VirtualFile): VirtualFile? {
        if (isSolutionFile(file)) {
            return file
        }
        // If a directory is selected, look for solution files inside
        if (file.isDirectory) {
            return file.children.firstOrNull { isSolutionFile(it) }
        }
        return null
    }

    private fun isSolutionFile(file: VirtualFile): Boolean {
        return file.extension?.lowercase() == "sln"
    }

    private fun containsSolutionFile(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        return file.children.any { isSolutionFile(it) }
    }
}
