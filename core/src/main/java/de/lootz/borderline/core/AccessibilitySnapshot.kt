package de.lootz.borderline.core

data class AccessibilitySnapshot(
    val packageName: String = "unknown",
    val className: String = "unknown",
    val eventType: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)
