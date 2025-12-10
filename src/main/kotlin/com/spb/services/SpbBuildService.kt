package com.spb.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.util.UUID

object SpbBuildService {

    private const val VSTEL_BUILD_ID_VAR = "VSTEL_CurrentSolutionBuildID"

    // Rider's build action IDs
    private const val BUILD_SOLUTION_ACTION = "BuildSolutionAction"
    private const val BUILD_CURRENT_PROJECT_ACTION = "BuildCurrentProject"

    fun triggerBuildWithEnvVar(project: Project, dataContext: DataContext, isSolution: Boolean) {
        // Generate a random build ID for each superbuild
        val randomBuildId = UUID.randomUUID().toString()

        // Set the environment variable for the current process
        // This will be inherited by the build process
        setEnvironmentVariable(VSTEL_BUILD_ID_VAR, randomBuildId)

        notifyInfo(project, "Superbuilding with $VSTEL_BUILD_ID_VAR=$randomBuildId")

        // Trigger the native build action
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

    private fun setEnvironmentVariable(name: String, value: String) {
        try {
            // Use reflection to modify the environment (works for current process)
            val env = System.getenv()
            val field = env.javaClass.getDeclaredField("m")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val writableEnv = field.get(env) as MutableMap<String, String>
            writableEnv[name] = value
        } catch (e: Exception) {
            // Fallback: set as system property (less ideal but works)
            System.setProperty(name, value)
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
