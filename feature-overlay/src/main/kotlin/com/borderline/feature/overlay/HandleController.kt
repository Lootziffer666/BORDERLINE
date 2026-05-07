package com.borderline.feature.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

/**
 * Positions and manages overlay handles.
 *
 * Four handle positions (ergonomically adjustable):
 * - Top-left (Snippets)
 * - Top-right (Clipboard+)
 * - Lower-third-left (Shortcuts)
 * - Lower-third-right (QuickActions)
 *
 * Handles are very slim (6dp wide, ~80dp tall), nearly invisible.
 * They respond to swipe gestures to open panels.
 *
 * Keyboard behavior: lower handles shift up when keyboard is visible
 * (if technically reliable — best-effort, not guaranteed).
 */
class HandleController(
    private val context: Context,
    private val windowManager: WindowManager
) {

    private val handles = mutableListOf<HandleView>()
    private var onHandleActivated: ((HandlePosition) -> Unit)? = null
    private val screenWidth: Int
    private val screenHeight: Int

    init {
        val dm = context.resources.displayMetrics
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
    }

    fun setOnHandleActivated(listener: (HandlePosition) -> Unit) {
        onHandleActivated = listener
    }

    fun showHandles() {
        HandlePosition.values().forEach { position ->
            val handle = createHandle(position)
            addHandleToWindow(handle)
            handles.add(handle)
        }
    }

    fun removeAllHandles() {
        handles.forEach { handle ->
            try {
                windowManager.removeView(handle.view)
            } catch (_: Exception) {}
        }
        handles.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createHandle(position: HandlePosition): HandleView {
        val handleWidth = dpToPx(HANDLE_WIDTH_DP)
        val handleHeight = dpToPx(HANDLE_HEIGHT_DP)

        val view = View(context).apply {
            background = GradientDrawable().apply {
                setColor(HANDLE_COLOR)
                cornerRadius = dpToPx(3).toFloat()
                alpha = HANDLE_ALPHA
            }
        }

        // Swipe detection
        var startX = 0f
        var startY = 0f
        var startTime = 0L

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    startTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    val dt = System.currentTimeMillis() - startTime

                    val isSwipe = abs(dx) > SWIPE_THRESHOLD_DP || abs(dy) > SWIPE_THRESHOLD_DP
                    val isTap = dt < TAP_TIMEOUT_MS && abs(dx) < dpToPx(10) && abs(dy) < dpToPx(10)

                    if (isSwipe || isTap) {
                        hapticFeedback()
                        onHandleActivated?.invoke(position)
                    }
                    true
                }
                else -> false
            }
        }

        return HandleView(
            view = view,
            position = position,
            width = handleWidth,
            height = handleHeight
        )
    }

    private fun addHandleToWindow(handle: HandleView) {
        val (x, y, gravity) = calculatePosition(handle.position, handle.width, handle.height)

        val params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = handle.width
            height = handle.height
            this.x = x
            this.y = y
            this.gravity = gravity
        }

        windowManager.addView(handle.view, params)
    }

    private fun calculatePosition(position: HandlePosition, width: Int, height: Int): Triple<Int, Int, Int> {
        val margin = dpToPx(4)
        return when (position) {
            HandlePosition.TOP_LEFT -> Triple(
                margin,
                screenHeight / 4,
                Gravity.TOP or Gravity.START
            )
            HandlePosition.TOP_RIGHT -> Triple(
                margin,
                screenHeight / 4,
                Gravity.TOP or Gravity.END
            )
            HandlePosition.LOWER_LEFT -> Triple(
                margin,
                screenHeight * 2 / 3,
                Gravity.TOP or Gravity.START
            )
            HandlePosition.LOWER_RIGHT -> Triple(
                margin,
                screenHeight * 2 / 3,
                Gravity.TOP or Gravity.END
            )
        }
    }

    /**
     * Shift lower handles up when keyboard is visible.
     * Best-effort — not all devices report keyboard height reliably.
     */
    fun onKeyboardShown(keyboardHeight: Int) {
        handles.filter {
            it.position == HandlePosition.LOWER_LEFT || it.position == HandlePosition.LOWER_RIGHT
        }.forEach { handle ->
            try {
                val params = handle.view.layoutParams as WindowManager.LayoutParams
                val (_, originalY, _) = calculatePosition(handle.position, handle.width, handle.height)
                params.y = originalY - keyboardHeight - dpToPx(16)
                windowManager.updateViewLayout(handle.view, params)
            } catch (_: Exception) {}
        }
    }

    fun onKeyboardHidden() {
        handles.filter {
            it.position == HandlePosition.LOWER_LEFT || it.position == HandlePosition.LOWER_RIGHT
        }.forEach { handle ->
            try {
                val params = handle.view.layoutParams as WindowManager.LayoutParams
                val (_, originalY, _) = calculatePosition(handle.position, handle.width, handle.height)
                params.y = originalY
                windowManager.updateViewLayout(handle.view, params)
            } catch (_: Exception) {}
        }
    }

    private fun hapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(30)
                }
            }
        } catch (_: Exception) {}
    }

    private fun dpToPx(dp: Int): Int = (dp * context.resources.displayMetrics.density).toInt()

    companion object {
        /** Handle width in dp — very slim, not like buttons */
        const val HANDLE_WIDTH_DP = 6

        /** Handle height in dp */
        const val HANDLE_HEIGHT_DP = 80

        /** Handle color — subtle, not loud */
        const val HANDLE_COLOR = 0x40FFFFFF  // White with low alpha

        /** Handle background alpha (0-255) */
        const val HANDLE_ALPHA = 80

        /** Minimum swipe distance to trigger */
        const val SWIPE_THRESHOLD_DP = 20

        /** Maximum tap duration in ms */
        const val TAP_TIMEOUT_MS = 300L
    }
}

/**
 * Handle position on screen.
 */
enum class HandlePosition(val displayName: String, val moduleIndex: Int) {
    TOP_LEFT("Snippets", 1),
    TOP_RIGHT("Clipboard+", 2),
    LOWER_LEFT("Shortcuts", 3),
    LOWER_RIGHT("QuickActions", 4)
}

/**
 * Internal handle view wrapper.
 */
data class HandleView(
    val view: View,
    val position: HandlePosition,
    val width: Int,
    val height: Int
)
