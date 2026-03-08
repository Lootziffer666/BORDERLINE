package de.lootz.borderline.core

import android.content.Context
import android.content.SharedPreferences

class ModulePrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(moduleId: ModuleId): Boolean {
        val descriptor = ModuleRegistry.descriptor(moduleId)
        if (descriptor.required) return true

        val requested = prefs.getBoolean(moduleId.prefKey, defaultState(moduleId))
        if (!requested) return false

        return descriptor.dependsOn.all { dependency -> isEnabled(dependency) }
    }

    fun setEnabled(moduleId: ModuleId, enabled: Boolean) {
        if (ModuleRegistry.descriptor(moduleId).required) return
        prefs.edit().putBoolean(moduleId.prefKey, enabled).apply()
    }

    fun snapshot(): Map<ModuleId, Boolean> = ModuleRegistry.all.associate { it.id to isEnabled(it.id) }

    private fun defaultState(moduleId: ModuleId): Boolean = when (moduleId) {
        ModuleId.ACCESSIBILITY -> true
        ModuleId.OVERLAY -> true
        ModuleId.SHORTCUTS -> true
    }

    companion object {
        const val PREFS_NAME = "borderline_module_prefs"
    }
}
