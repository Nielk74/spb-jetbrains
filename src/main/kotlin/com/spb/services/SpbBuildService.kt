package com.spb.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.build.BuildContentManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.spb.settings.SpbSettings

object SpbBuildService {

    private const val VSTELEMETRY_SESSION_VAR = "VSTelemetrySession"

    fun buildWithVsTelemetry(project: Project, file: VirtualFile, isSolution: Boolean) {
        val settings = SpbSettings.getInstance()
        val targetType = if (isSolution) "solution" else "project"

        notifyInfo(project, "Starting SPB build for $targetType: ${file.name}")

        try {
            val commandLine = createMsBuildCommandLine(file, settings)
            executeBuild(project, commandLine, file.name)
        } catch (e: Exception) {
            notifyError(project, "Failed to start build: ${e.message}")
        }
    }

    private fun createMsBuildCommandLine(file: VirtualFile, settings: SpbSettings): GeneralCommandLine {
        val commandLine = GeneralCommandLine()

        // Use dotnet build or msbuild depending on the environment
        // For cross-platform compatibility, we use "dotnet build"
        commandLine.exePath = "dotnet"
        commandLine.addParameter("build")
        commandLine.addParameter(file.path)

        // Set the working directory to the file's parent
        commandLine.workDirectory = file.parent?.let { java.io.File(it.path) }

        // Set the VSTelemetrySession environment variable
        commandLine.environment[VSTELEMETRY_SESSION_VAR] = settings.vsTelemetrySessionValue

        return commandLine
    }

    private fun executeBuild(project: Project, commandLine: GeneralCommandLine, fileName: String) {
        val processHandler = OSProcessHandler(commandLine)

        processHandler.addProcessListener(object : ProcessAdapter() {
            private val output = StringBuilder()

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                val text = event.text
                output.append(text)

                // Log to build tool window if available
                if (outputType === ProcessOutputTypes.STDOUT || outputType === ProcessOutputTypes.STDERR) {
                    // Output is captured for potential future use
                }
            }

            override fun processTerminated(event: ProcessEvent) {
                val exitCode = event.exitCode
                if (exitCode == 0) {
                    notifyInfo(project, "SPB Build completed successfully: $fileName")
                } else {
                    notifyError(project, "SPB Build failed with exit code $exitCode: $fileName")
                }
            }
        })

        processHandler.startNotify()
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
