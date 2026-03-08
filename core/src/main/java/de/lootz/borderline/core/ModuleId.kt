package de.lootz.borderline.core

enum class ModuleId(val prefKey: String, val displayName: String, val description: String) {
    ACCESSIBILITY("module_accessibility", "Accessibility Backbone", "Erfasst relevante Accessibility-Events und Statusdaten."),
    OVERLAY("module_overlay", "Overlay Panel", "Zeigt das Borderline-Panel als Accessibility-Overlay."),
    SHORTCUTS("module_shortcuts", "Quick Actions", "Stellt testbare Schnellaktionen im Panel bereit.")
}
