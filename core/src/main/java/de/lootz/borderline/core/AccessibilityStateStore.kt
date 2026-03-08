package de.lootz.borderline.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessibilityStateStore {
    private val mutableState = MutableStateFlow(AccessibilitySnapshot())
    val state: StateFlow<AccessibilitySnapshot> = mutableState.asStateFlow()

    fun update(snapshot: AccessibilitySnapshot) {
        mutableState.value = snapshot
    }
}
