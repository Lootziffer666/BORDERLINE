package com.borderline.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Provisioning Screen: The app icon opens this.
 *
 * Purpose: Set up once, then forget. Daily use happens through overlay handles.
 *
 * Shows:
 * - Module status overview (which modules are ready)
 * - Permission status (draw-over-apps, files, notifications)
 * - Setup actions for missing permissions
 *
 * This is NOT a dashboard. NOT a full app experience.
 * It can be closed without affecting daily overlay usage.
 */
class ProvisioningActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var moduleContainer: LinearLayout
    private lateinit var permissionContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildLayout())
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun buildLayout(): View {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(0xFF121212.toInt())
            setPadding(dp(24), dp(48), dp(24), dp(24))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Title
        root.addView(TextView(this).apply {
            text = "Borderline"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        })

        // Status line
        statusText = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFFB0B0B0.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24))
        }
        root.addView(statusText)

        // Module section
        root.addView(sectionHeader("Modules"))
        moduleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(16))
        }
        root.addView(moduleContainer)

        // Permission section
        root.addView(sectionHeader("Permissions"))
        permissionContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(16))
        }
        root.addView(permissionContainer)

        // Close hint
        root.addView(TextView(this).apply {
            text = "Close this screen — Borderline works through overlay handles."
            textSize = 13f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, 0)
        })

        scroll.addView(root)
        return scroll
    }

    private fun refreshStatus() {
        val overlayGranted = Settings.canDrawOverlays(this)

        // Module status
        data class ModuleStatus(val name: String, val status: String, val ready: Boolean)
        val modules = listOf(
            ModuleStatus("Snippets", if (overlayGranted) "Active" else "Needs overlay permission", overlayGranted),
            ModuleStatus("Clipboard+", if (overlayGranted) "Active" else "Needs overlay permission", overlayGranted),
            ModuleStatus("Shortcuts", if (overlayGranted) "Configured" else "Needs overlay permission", overlayGranted),
            ModuleStatus("QuickActions", if (overlayGranted) "Configured" else "Needs overlay permission", overlayGranted)
        )

        val readyCount = modules.count { it.ready }
        statusText.text = if (readyCount == modules.size) {
            "All set up ✓"
        } else {
            "$readyCount of ${modules.size} modules ready"
        }
        statusText.setTextColor(
            if (readyCount == modules.size) 0xFF4CAF50.toInt() else 0xFFFF9800.toInt()
        )

        // Render modules
        moduleContainer.removeAllViews()
        modules.forEach { module ->
            moduleContainer.addView(moduleRow(module.name, module.status, module.ready))
        }

        // Permissions
        permissionContainer.removeAllViews()

        // Draw-over-apps
        permissionContainer.addView(
            permissionRow(
                name = "Draw over apps",
                granted = overlayGranted,
                onRequest = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            )
        )

        // File access (optional, for future large snippet storage)
        // Not strictly required for MVP but shown for transparency
        if (Build.VERSION.SDK_INT >= 30) {
            val hasFileAccess = android.os.Environment.isExternalStorageManager()
            permissionContainer.addView(
                permissionRow(
                    name = "File access (optional)",
                    granted = hasFileAccess,
                    onRequest = {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback for devices that don't support the direct intent
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            startActivity(intent)
                        }
                    },
                    optional = true
                )
            )
        }
    }

    private fun moduleRow(name: String, status: String, ready: Boolean): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(0xFF1E1E1E.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }

            addView(TextView(context).apply {
                text = if (ready) "●" else "○"
                textSize = 14f
                setTextColor(if (ready) 0xFF4CAF50.toInt() else 0xFF666666.toInt())
                setPadding(0, 0, dp(12), 0)
            })

            addView(TextView(context).apply {
                text = name
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(context).apply {
                text = status
                textSize = 13f
                setTextColor(if (ready) 0xFF4CAF50.toInt() else 0xFFFF9800.toInt())
            })
        }
    }

    private fun permissionRow(
        name: String,
        granted: Boolean,
        onRequest: () -> Unit,
        optional: Boolean = false
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(0xFF1E1E1E.toInt())
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }

            addView(TextView(context).apply {
                text = if (granted) "✓" else if (optional) "○" else "✗"
                textSize = 16f
                setTextColor(
                    when {
                        granted -> 0xFF4CAF50.toInt()
                        optional -> 0xFF666666.toInt()
                        else -> 0xFFFF5252.toInt()
                    }
                )
                setPadding(0, 0, dp(12), 0)
            })

            addView(TextView(context).apply {
                text = name + if (optional) " (optional)" else ""
                textSize = 15f
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            if (!granted) {
                addView(Button(context).apply {
                    text = "Grant"
                    textSize = 13f
                    setTextColor(0xFFFFFFFF.toInt())
                    setBackgroundColor(0xFF333333.toInt())
                    setPadding(dp(16), dp(8), dp(16), dp(8))
                    setOnClickListener { onRequest() }
                })
            }
        }
    }

    private fun sectionHeader(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(0xFF888888.toInt())
            setPadding(0, dp(16), 0, dp(4))
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
