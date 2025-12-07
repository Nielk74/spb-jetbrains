package com.spb.services

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.spb.settings.SpbSettings
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class VersionCheckService {

    private val logger = Logger.getInstance(VersionCheckService::class.java)

    companion object {
        private val CHECK_INTERVAL_MS = TimeUnit.DAYS.toMillis(1) // Once per day
        private const val CURRENT_VERSION = "1.0.0"

        fun getInstance(): VersionCheckService {
            return ApplicationManager.getApplication().getService(VersionCheckService::class.java)
        }
    }

    fun checkForUpdates(project: Project?) {
        val settings = SpbSettings.getInstance()

        if (!settings.enableAutoUpdateCheck) {
            return
        }

        val now = System.currentTimeMillis()
        if (now - settings.lastUpdateCheck < CHECK_INTERVAL_MS) {
            logger.info("Skipping update check, last check was less than a day ago")
            return
        }

        settings.lastUpdateCheck = now

        ApplicationManager.getApplication().executeOnPooledThread {
            performUpdateCheck(project, settings)
        }
    }

    private fun performUpdateCheck(project: Project?, settings: SpbSettings) {
        try {
            val latestVersion = fetchLatestVersion(settings.updateCheckUrl)

            if (latestVersion != null && isNewerVersion(latestVersion, CURRENT_VERSION)) {
                ApplicationManager.getApplication().invokeLater {
                    showUpdateNotification(project, latestVersion, settings.downloadPath)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to check for updates", e)
        }
    }

    /**
     * TODO: Implement actual HTTP fetch to get version info from the configured URL.
     *
     * Expected response format (JSON):
     * {
     *   "version": "1.2.0",
     *   "downloadUrl": "http://example.com/spb/spb-tool-1.2.0.exe",
     *   "releaseNotes": "Bug fixes and improvements"
     * }
     *
     * Mock implementation returns a simulated new version for testing.
     */
    private fun fetchLatestVersion(updateCheckUrl: String): String? {
        // TODO: Replace with actual HTTP request implementation
        // Example implementation:
        // val connection = URL(updateCheckUrl).openConnection() as HttpURLConnection
        // connection.requestMethod = "GET"
        // connection.connectTimeout = 5000
        // connection.readTimeout = 5000
        //
        // if (connection.responseCode == 200) {
        //     val response = connection.inputStream.bufferedReader().readText()
        //     val json = JsonParser.parseString(response).asJsonObject
        //     return json.get("version").asString
        // }
        // return null

        // Mock: Return a "new version" for demonstration
        // In production, this should fetch from updateCheckUrl
        logger.info("Mock version check - would fetch from: $updateCheckUrl")

        // For testing: uncomment the line below to simulate a new version available
        // return "1.1.0"

        return null // No update available (default mock behavior)
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestPart = latestParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }

            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }

        return false
    }

    private fun showUpdateNotification(project: Project?, latestVersion: String, downloadPath: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(
                "SPB Tool Update Available",
                "A new version ($latestVersion) is available. Current version: $CURRENT_VERSION",
                NotificationType.INFORMATION
            )

        notification.addAction(NotificationAction.createSimple("Download Update") {
            downloadUpdate(project, latestVersion, downloadPath)
            notification.expire()
        })

        notification.addAction(NotificationAction.createSimple("Dismiss") {
            notification.expire()
        })

        notification.notify(project)
    }

    /**
     * TODO: Implement actual download of the exe file.
     *
     * This should:
     * 1. Fetch the download URL from the version info endpoint
     * 2. Download the file to the configured downloadPath
     * 3. Show progress indicator during download
     * 4. Verify the download (checksum if available)
     * 5. Notify user when complete
     */
    private fun downloadUpdate(project: Project?, version: String, downloadPath: String) {
        // TODO: Implement actual download logic
        // Example implementation:
        // val downloadUrl = "http://example.com/spb/spb-tool-$version.exe"
        // val targetFile = File(downloadPath, "spb-tool-$version.exe")
        //
        // ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Downloading SPB Tool Update") {
        //     override fun run(indicator: ProgressIndicator) {
        //         indicator.isIndeterminate = false
        //         val connection = URL(downloadUrl).openConnection() as HttpURLConnection
        //         val totalSize = connection.contentLength
        //         var downloadedSize = 0
        //
        //         connection.inputStream.use { input ->
        //             FileOutputStream(targetFile).use { output ->
        //                 val buffer = ByteArray(8192)
        //                 var bytesRead: Int
        //                 while (input.read(buffer).also { bytesRead = it } != -1) {
        //                     output.write(buffer, 0, bytesRead)
        //                     downloadedSize += bytesRead
        //                     indicator.fraction = downloadedSize.toDouble() / totalSize
        //                 }
        //             }
        //         }
        //     }
        //
        //     override fun onSuccess() {
        //         notifyDownloadComplete(project, targetFile.path)
        //     }
        // })

        // Mock: Just create the directory and show a notification
        val downloadDir = File(downloadPath)
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(
                "Download Started (Mock)",
                "In production, version $version would be downloaded to: $downloadPath",
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}
