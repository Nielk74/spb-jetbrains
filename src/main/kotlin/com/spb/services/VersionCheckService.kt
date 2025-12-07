package com.spb.services

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.spb.settings.SpbSettings
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

@Service
class VersionCheckService {

    private val logger = Logger.getInstance(VersionCheckService::class.java)

    companion object {
        private val CHECK_INTERVAL_MS = TimeUnit.DAYS.toMillis(1)

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
            val remoteLastModified = fetchLastModified(settings.cliUpdateUrl)

            if (remoteLastModified != null && remoteLastModified > settings.lastCliModified) {
                ApplicationManager.getApplication().invokeLater {
                    showUpdateNotification(project, remoteLastModified, settings)
                }
            } else {
                logger.info("CLI tool is up to date (remote: $remoteLastModified, local: ${settings.lastCliModified})")
            }
        } catch (e: Exception) {
            logger.warn("Failed to check for CLI updates", e)
        }
    }

    private fun fetchLastModified(url: String): Long? {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val lastModified = connection.lastModified
                logger.info("Remote Last-Modified: $lastModified for $url")
                return if (lastModified > 0) lastModified else null
            } else {
                logger.warn("HTTP $responseCode when checking $url")
                return null
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun showUpdateNotification(project: Project?, remoteLastModified: Long, settings: SpbSettings) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(
                "SPB CLI Update Available",
                "A new version of the CLI tool (tracker.exe) is available. Would you like to download it?",
                NotificationType.INFORMATION
            )

        notification.addAction(NotificationAction.createSimple("Download Update") {
            downloadUpdate(project, remoteLastModified, settings)
            notification.expire()
        })

        notification.addAction(NotificationAction.createSimple("Dismiss") {
            notification.expire()
        })

        notification.notify(project)
    }

    private fun downloadUpdate(project: Project?, remoteLastModified: Long, settings: SpbSettings) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Downloading SPB CLI Update") {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.text = "Downloading tracker.exe..."

                try {
                    val connection = URL(settings.cliUpdateUrl).openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 30000

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("HTTP ${connection.responseCode}")
                    }

                    val totalSize = connection.contentLength
                    var downloadedSize = 0

                    val targetFile = File(settings.cliDownloadPath)
                    targetFile.parentFile?.mkdirs()

                    connection.inputStream.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                if (indicator.isCanceled) {
                                    throw InterruptedException("Download cancelled")
                                }
                                output.write(buffer, 0, bytesRead)
                                downloadedSize += bytesRead
                                if (totalSize > 0) {
                                    indicator.fraction = downloadedSize.toDouble() / totalSize
                                }
                            }
                        }
                    }

                    settings.lastCliModified = remoteLastModified

                    ApplicationManager.getApplication().invokeLater {
                        notifyDownloadComplete(project, targetFile.path)
                    }
                } catch (e: InterruptedException) {
                    logger.info("Download cancelled by user")
                } catch (e: Exception) {
                    logger.error("Failed to download CLI update", e)
                    ApplicationManager.getApplication().invokeLater {
                        notifyDownloadFailed(project, e.message ?: "Unknown error")
                    }
                }
            }
        })
    }

    private fun notifyDownloadComplete(project: Project?, path: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(
                "Download Complete",
                "CLI tool downloaded to: $path",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun notifyDownloadFailed(project: Project?, error: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SPB Build Notifications")
            .createNotification(
                "Download Failed",
                "Failed to download CLI update: $error",
                NotificationType.ERROR
            )
            .notify(project)
    }
}
