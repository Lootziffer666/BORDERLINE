package com.borderline.feature.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * Foreground service that shows overlay handles via draw-over-apps permission.
 *
 * Uses TYPE_APPLICATION_OVERLAY (not TYPE_ACCESSIBILITY_OVERLAY).
 * No Accessibility Service required.
 *
 * Handle positions:
 * - Top-left: Snippets
 * - Top-right: Clipboard+
 * - Lower-third-left: Shortcuts
 * - Lower-third-right: QuickActions
 *
 * Handles are very slim, non-intrusive edge strips.
 * Swipe gestures from handles open the corresponding panel.
 */
class OverlayHandleService : Service() {

    private lateinit var windowManager: WindowManager
    private val handleViews = mutableListOf<View>()
    private var handleController: HandleController? = null

    // Callback for panel open requests — set by the component that starts this service
    var onHandleActivated: ((HandlePosition) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        startForegroundWithNotification()
        handleController = HandleController(this, windowManager)
        handleController?.showHandles()
    }

    override fun onDestroy() {
        handleController?.removeAllHandles()
        handleController = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                handleController?.removeAllHandles()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_REFRESH -> {
                handleController?.removeAllHandles()
                handleController?.showHandles()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val channelId = "borderline_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Borderline Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps overlay handles active"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Borderline")
                .setContentText("Overlay handles active")
                .setSmallIcon(android.R.drawable.ic_menu_crop)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Borderline")
                .setContentText("Overlay handles active")
                .setSmallIcon(android.R.drawable.ic_menu_crop)
                .setOngoing(true)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_STOP = "com.borderline.STOP_OVERLAY"
        const val ACTION_REFRESH = "com.borderline.REFRESH_OVERLAY"
        private const val NOTIFICATION_ID = 1001
    }
}
