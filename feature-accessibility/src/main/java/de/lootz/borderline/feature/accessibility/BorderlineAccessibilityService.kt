package de.lootz.borderline.feature.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import de.lootz.borderline.core.AccessibilitySnapshot
import de.lootz.borderline.core.AccessibilityStateStore
import de.lootz.borderline.core.BorderlineLogger
import de.lootz.borderline.core.ModuleId
import de.lootz.borderline.core.ModulePrefs
import de.lootz.borderline.feature.overlay.BorderlineOverlayController

class BorderlineAccessibilityService : AccessibilityService() {

    private lateinit var modulePrefs: ModulePrefs
    private lateinit var overlayController: BorderlineOverlayController

    override fun onServiceConnected() {
        super.onServiceConnected()
        modulePrefs = ModulePrefs(this)
        overlayController = BorderlineOverlayController(this, modulePrefs)
        overlayController.ensureState()
        BorderlineLogger.i("Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!modulePrefs.isEnabled(ModuleId.ACCESSIBILITY)) return

        val snapshot = AccessibilitySnapshot(
            packageName = event.packageName?.toString() ?: "unknown",
            className = event.className?.toString() ?: "unknown",
            eventType = event.eventType,
            timestamp = System.currentTimeMillis()
        )
        AccessibilityStateStore.update(snapshot)
        overlayController.ensureState()
    }

    override fun onInterrupt() {
        BorderlineLogger.w("Accessibility interrupted")
    }

    override fun onDestroy() {
        overlayController.dispose()
        super.onDestroy()
    }
}
