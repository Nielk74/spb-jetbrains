package com.spb.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import java.io.File
import java.util.UUID

object SpbBuildService {

    private const val SPB_ACTIVE_FILE = "spb_active"

    // Rider's build action IDs
    private const val BUILD_SOLUTION_ACTION = "BuildSolutionAction"
    private const val BUILD_CURRENT_PROJECT_ACTION = "BuildCurrentProject"

    fun triggerBuildWithEnvVar(project: Project, dataContext: DataContext, isSolution: Boolean) {
        // Get solution directory from the project's base path
        val solutionDir = project.basePath

        if (solutionDir == null) {
            notifyError(project, "Could not determine solution directory")
            return
        }

        val spbActiveFile = File(solutionDir, SPB_ACTIVE_FILE)

        // Check if spb_active file exists - if not, this solution doesn't use superbuild
        if (!spbActiveFile.exists()) {
            notifyInfo(project, "No $SPB_ACTIVE_FILE file found - running normal build")
            triggerNativeBuild(project, dataContext, isSolution)
            return
        }

        // Generate a random build ID
        val randomBuildId = UUID.randomUUID().toString()

        // Write the build ID into spb_active file
        try {
            spbActiveFile.writeText(randomBuildId)
        } catch (e: Exception) {
            notifyError(project, "Failed to write build ID to $SPB_ACTIVE_FILE: ${e.message}")
            return
        }

        notifyInfo(project, "Superbuilding with ID: $randomBuildId")

        // Trigger the native build
        triggerNativeBuild(project, dataContext, isSolution)
    }

    private fun triggerNativeBuild(project: Project, dataContext: DataContext, isSolution: Boolean) {
        val actionId = if (isSolution) BUILD_SOLUTION_ACTION else BUILD_CURRENT_PROJECT_ACTION
        val action = ActionManager.getInstance().getAction(actionId)

        if (action != null) {
            val event = AnActionEvent.createFromDataContext(
                "Superbuild",
                Presentation(),
                dataContext
            )
            action.actionPerformed(event)
        } else {
            // Fallback: try alternative action IDs
            val fallbackAction = ActionManager.getInstance().getAction("Build")
                ?: ActionManager.getInstance().getAction("CompileDirty")

            if (fallbackAction != null) {
                val event = AnActionEvent.createFromDataContext(
                    "Superbuild",
                    Presentation(),
                    dataContext
                )
                fallbackAction.actionPerformed(event)
            } else {
                notifyError(project, "Could not find build action")
            }
        }
    }

    private fun notifyInfo(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }

    private fun notifyError(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(message, NotificationType.ERROR)
            .notify(project)
    }
}
