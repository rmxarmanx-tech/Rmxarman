package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.ui.components.LockoutOverlay
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SystemLockoutController {
    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(1800L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _lockoutReason = MutableStateFlow("Disciplinary 30-minute lockout enforced.")
    val lockoutReason: StateFlow<String> = _lockoutReason.asStateFlow()

    fun show(context: Context, reason: String, seconds: Long = 1800L) {
        _lockoutReason.value = reason
        _remainingSeconds.value = seconds
        _isOverlayActive.value = true

        if (OverlayPermissionHelper.canDrawOverlays(context)) {
            val intent = Intent(context, LockoutOverlayService::class.java).apply {
                action = LockoutOverlayService.ACTION_SHOW
                putExtra(LockoutOverlayService.EXTRA_REASON, reason)
                putExtra(LockoutOverlayService.EXTRA_SECONDS, seconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun updateSeconds(seconds: Long) {
        _remainingSeconds.value = seconds
    }

    fun dismiss(context: Context) {
        _isOverlayActive.value = false
        val intent = Intent(context, LockoutOverlayService::class.java).apply {
            action = LockoutOverlayService.ACTION_DISMISS
        }
        context.startService(intent)
    }
}

class LockoutOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var lifecycleHost: OverlayLifecycleHost? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "ugt_lockout_overlay_channel"
        const val NOTIFICATION_ID = 3001
        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val EXTRA_REASON = "EXTRA_REASON"
        const val EXTRA_SECONDS = "EXTRA_SECONDS"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_SHOW
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "Violation detected: 30-minute disciplinary lockdown."
        val seconds = intent?.getLongExtra(EXTRA_SECONDS, 1800L) ?: 1800L

        if (action == ACTION_DISMISS) {
            removeOverlayView()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildForegroundNotification(reason)
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

        attachOverlayView(reason, seconds)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "UGT System-Wide Lockout",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "System-wide full-screen disciplinary lockout notification"
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(reason: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "UGT_TUITION")
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UGT Disciplinary Lockout Active")
            .setContentText("Non-UGT apps blocked for 30 minutes: $reason")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun attachOverlayView(reason: String, initialSeconds: Long) {
        if (!OverlayPermissionHelper.canDrawOverlays(this)) return
        if (overlayView != null) return // Already attached

        val wm = windowManager ?: return

        val host = OverlayLifecycleHost().apply {
            handleCreate()
            handleStart()
        }
        lifecycleHost = host

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                MyApplicationTheme {
                    val remaining = SystemLockoutController.remainingSeconds.value
                    LockoutOverlay(
                        remainingSeconds = remaining,
                        lockoutReason = reason,
                        onOpenUgtTuition = {
                            // Launch UGT Tuition inside MainActivity
                            val launchIntent = Intent(this@LockoutOverlayService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("OPEN_TAB", "UGT_TUITION")
                            }
                            startActivity(launchIntent)
                            // Dismiss system overlay so user can study inside the app
                            SystemLockoutController.dismiss(this@LockoutOverlayService)
                        },
                        onAdminOverride = {
                            SystemLockoutController.dismiss(this@LockoutOverlayService)
                        }
                    )
                }
            }
        }

        try {
            wm.addView(composeView, layoutParams)
            overlayView = composeView
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun removeOverlayView() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            // Ignored
        } finally {
            overlayView = null
            lifecycleHost?.handleDestroy()
            lifecycleHost = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
