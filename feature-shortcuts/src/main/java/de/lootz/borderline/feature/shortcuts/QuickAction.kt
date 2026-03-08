package de.lootz.borderline.feature.shortcuts

data class QuickAction(
    val id: String,
    val label: String,
    val description: String,
    val handler: () -> String
)
