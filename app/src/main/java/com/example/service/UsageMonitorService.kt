package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AuditedPackage
import com.example.model.ForegroundAppInfo
import com.example.model.ProhibitedAppEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UsageMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null
    private var lastAuditedTime = 0L
    private val reportedSideloads = mutableSetOf<String>()

    companion object {
        const val CHANNEL_ID = "ugt_usage_monitor_channel"
        const val NOTIFICATION_ID = 2001

        fun startService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildForegroundNotification("Active: Monitoring applications & sideloaded APKs")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        AppMonitorHub.setServiceRunning(true)
        startMonitoringLoop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "UGT StudyGuard Real-Time Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors installed applications and prohibits distracting apps"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UGT StudyGuard Monitor")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val pm = packageManager

            while (isActive) {
                try {
                    // 1. Query Real-Time Foreground App using UsageStatsManager
                    if (usageStatsManager != null && UsagePermissionHelper.hasUsageStatsPermission(this@UsageMonitorService)) {
                        val now = System.currentTimeMillis()
                        val events = usageStatsManager.queryEvents(now - 6000L, now)
                        val event = UsageEvents.Event()
                        var topPackage: String? = null

                        while (events.hasNextEvent()) {
                            events.getNextEvent(event)
                            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                                topPackage = event.packageName
                            }
                        }

                        if (topPackage != null) {
                            processForegroundPackage(topPackage, pm)
                        }
                    }

                    // 2. Audit Installed Packages & Sideloaded APKs every 8 seconds
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAuditedTime > 8000L) {
                        lastAuditedTime = currentTime
                        auditInstalledApplications(pm)
                    }

                } catch (e: Exception) {
                    // Safe error absorption to guarantee zero crash in background daemon
                }

                delay(1200L)
            }
        }
    }

    private fun processForegroundPackage(packageName: String, pm: PackageManager) {
        val myPkg = applicationContext.packageName
        if (packageName == myPkg) {
            // Our app is in foreground
            AppMonitorHub.updateForegroundApp(
                ForegroundAppInfo(
                    packageName = packageName,
                    appName = "UGT StudyGuard (Active Hub)",
                    lastResumedTimestamp = System.currentTimeMillis(),
                    isProhibited = false,
                    isWhitelisted = true,
                    statusText = "Permitted: Core Discipline Controller"
                )
            )
            return
        }

        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val isSystem = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }

        val isWhitelisted = AppMonitorHub.whitelistedPackages.contains(packageName)
        val isExplicitDistraction = AppMonitorHub.knownDistractionPackages.contains(packageName)

        // Rule constraint: only whitelisted browsers (Chrome, Brave), whitelisted game (Chess),
        // whitelisted socials (WhatsApp, Telegram) are permitted.
        val isProhibited = isExplicitDistraction || (!isWhitelisted && !isSystem)

        AppMonitorHub.updateForegroundApp(
            ForegroundAppInfo(
                packageName = packageName,
                appName = appName,
                lastResumedTimestamp = System.currentTimeMillis(),
                isProhibited = isProhibited,
                isWhitelisted = isWhitelisted,
                statusText = if (isProhibited) "PROHIBITED: Distracting application detected" else "Permitted: Allowed study/system tool"
            )
        )

        if (isProhibited) {
            AppMonitorHub.notifyProhibitedAppDetected(
                ProhibitedAppEvent(
                    packageName = packageName,
                    appName = appName,
                    reason = "Prohibited distracting application '$appName' launched in foreground during strict study mode."
                )
            )
        }
    }

    private fun auditInstalledApplications(pm: PackageManager) {
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val auditedList = mutableListOf<AuditedPackage>()

        for (app in installedApps) {
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val pkgName = app.packageName

            // Inspect installer package name to detect sideloaded APKs
            val installerPkg: String? = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    pm.getInstallSourceInfo(pkgName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(pkgName)
                }
            } catch (e: Exception) {
                null
            }

            // Sideloaded if installer is NOT official Google Play Store and not preinstalled system app
            val isSideloaded = !isSystem && installerPkg != "com.android.vending" && pkgName != applicationContext.packageName

            val isProhibited = isSideloaded || AppMonitorHub.knownDistractionPackages.contains(pkgName)
            val appName = pm.getApplicationLabel(app).toString()

            val category = when {
                AppMonitorHub.whitelistedPackages.contains(pkgName) -> "Whitelisted Study/App"
                isSideloaded -> "Sideloaded APK (Outside Play Store)"
                isProhibited -> "Prohibited Distraction"
                isSystem -> "System Framework"
                else -> "Play Store App"
            }

            auditedList.add(
                AuditedPackage(
                    packageName = pkgName,
                    appName = appName,
                    isSystemApp = isSystem,
                    installerPackage = installerPkg ?: "Unknown / Direct Sideload",
                    isSideloaded = isSideloaded,
                    isProhibited = isProhibited,
                    category = category
                )
            )

            // Alert immediately on new sideloaded APK
            if (isSideloaded && !reportedSideloads.contains(pkgName)) {
                reportedSideloads.add(pkgName)
                AppMonitorHub.notifyProhibitedAppDetected(
                    ProhibitedAppEvent(
                        packageName = pkgName,
                        appName = appName,
                        reason = "Sideloaded APK detected: '$appName' ($pkgName) installed outside Google Play Store (Installer: ${installerPkg ?: "Direct"})."
                    )
                )
            }
        }

        AppMonitorHub.updateAuditedPackages(auditedList)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AppMonitorHub.setServiceRunning(false)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
