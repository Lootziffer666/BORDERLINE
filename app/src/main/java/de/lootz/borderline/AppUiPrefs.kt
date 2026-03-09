package de.lootz.borderline

import android.content.Context
import android.content.SharedPreferences

class AppUiPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSetupComplete(): Boolean = prefs.getBoolean(KEY_SETUP_COMPLETE, false)

    fun markSetupComplete(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, completed).apply()
    }

    fun resetSetup() {
        markSetupComplete(false)
    }

    companion object {
        private const val PREFS_NAME = "borderline_ui_prefs"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
    }
}
