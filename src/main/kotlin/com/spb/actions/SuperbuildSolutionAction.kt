package com.spb.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.spb.SpbIcons
import com.spb.services.SpbBuildService

class SuperbuildSolutionAction : AnAction(
    "Superbuilding",
    "Build solution with VSTEL_CurrentSolutionBuildID variable",
    SpbIcons.Superbuild
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        SpbBuildService.triggerBuildWithEnvVar(project, e.dataContext, isSolution = true)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
