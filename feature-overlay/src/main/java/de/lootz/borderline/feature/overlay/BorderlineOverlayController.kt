package de.lootz.borderline.feature.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import de.lootz.borderline.core.AccessibilityStateStore
import de.lootz.borderline.core.ModuleId
import de.lootz.borderline.core.ModulePrefs
import de.lootz.borderline.feature.shortcuts.QuickActionRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BorderlineOverlayController(
    private val context: Context,
    private val modulePrefs: ModulePrefs
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var edgeHandle: View? = null
    private var panelView: View? = null
    private val overlayScope = CoroutineScope(Dispatchers.Main + Job())
    private var stateCollectionJob: Job? = null
    private var state = OverlaySessionState()

    fun ensureState() {
        if (!modulePrefs.isEnabled(ModuleId.OVERLAY)) {
            hideAll()
            return
        }
        if (edgeHandle == null) {
            showHandle()
        }
    }

    private fun showHandle() {
        val view = LayoutInflater.from(context).inflate(R.layout.view_edge_handle, null)
        val params = baseParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        view.findViewById<ImageView>(R.id.edgeHandleIcon).setOnClickListener {
            togglePanel()
        }
        windowManager.addView(view, params)
        edgeHandle = view
    }

    private fun togglePanel() {
        if (panelView == null) showPanel() else hidePanel()
    }

    private fun showPanel() {
        val view = LayoutInflater.from(context).inflate(R.layout.view_overlay_panel, null)
        val params = baseParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 160
        }
        val statusText = view.findViewById<TextView>(R.id.overlayStatus)
        val closeButton = view.findViewById<Button>(R.id.closeOverlayButton)
        val actionOne = view.findViewById<Button>(R.id.actionOneButton)
        val actionTwo = view.findViewById<Button>(R.id.actionTwoButton)

        closeButton.setOnClickListener { hidePanel() }

        val shortcutsEnabled = modulePrefs.isEnabled(ModuleId.SHORTCUTS)
        val actions = if (shortcutsEnabled) QuickActionRegistry(context).actions() else emptyList()
        actionOne.text = actions.getOrNull(0)?.label ?: "Quick Actions aus"
        actionTwo.text = actions.getOrNull(1)?.label ?: "Quick Actions aus"
        actionOne.isEnabled = shortcutsEnabled && actions.isNotEmpty()
        actionTwo.isEnabled = shortcutsEnabled && actions.size > 1

        actionOne.setOnClickListener {
            statusText.text = actions.getOrNull(0)?.handler?.invoke() ?: "Keine Aktion"
        }
        actionTwo.setOnClickListener {
            statusText.text = actions.getOrNull(1)?.handler?.invoke() ?: "Keine Aktion"
        }

        stateCollectionJob?.cancel()
        stateCollectionJob = AccessibilityStateStore.state.onEach { snapshot ->
            statusText.text = "${snapshot.packageName}\n${snapshot.className}"
        }.launchIn(overlayScope)

        windowManager.addView(view, params)
        panelView = view
        state = state.copy(visible = true)
    }

    private fun hidePanel() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        panelView?.let { windowManager.removeView(it) }
        panelView = null
        state = state.copy(visible = false)
    }

    private fun hideAll() {
        hidePanel()
        edgeHandle?.let { windowManager.removeView(it) }
        edgeHandle = null
    }

    fun dispose() {
        hideAll()
        overlayScope.cancel()
    }

    private fun baseParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
    }
}
