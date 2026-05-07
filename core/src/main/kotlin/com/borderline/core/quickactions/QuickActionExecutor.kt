package com.borderline.core.quickactions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.borderline.core.models.CopyStatus

/**
 * Executes QuickActions by dispatching intents or clipboard operations.
 *
 * No AI, no network calls, no background processing.
 * Just intent dispatch and clipboard management.
 */
class QuickActionExecutor(private val context: Context) {

    /**
     * Execute a quick action.
     *
     * @return [QuickActionResult] describing what happened.
     */
    fun execute(action: QuickAction, content: String): QuickActionResult {
        return when (action.type) {
            QuickActionType.OPEN_URL,
            QuickActionType.CALL,
            QuickActionType.SMS,
            QuickActionType.EMAIL,
            QuickActionType.OPEN_MAP,
            QuickActionType.NAVIGATE,
            QuickActionType.CALENDAR_EVENT -> {
                executeIntent(action)
            }

            QuickActionType.SHARE -> {
                executeShare(content)
            }

            QuickActionType.SAVE_SNIPPET -> {
                // This is handled by the panel layer — it creates a SnippetObject
                QuickActionResult(
                    success = true,
                    status = "Ready to save",
                    requiresPanelAction = true,
                    panelAction = PanelAction.OPEN_CREATE_SNIPPET
                )
            }

            QuickActionType.COPY_CLEAN -> {
                executeCopyClean(action)
            }
        }
    }

    private fun executeIntent(action: QuickAction): QuickActionResult {
        val intentAction = action.intentAction ?: return QuickActionResult(
            success = false,
            status = "No intent action configured"
        )
        val intentData = action.intentData

        return try {
            val intent = Intent(intentAction).apply {
                if (intentData != null) {
                    data = Uri.parse(intentData)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Verify there's an app to handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                QuickActionResult(
                    success = true,
                    status = "${action.label} — opened"
                )
            } else {
                QuickActionResult(
                    success = false,
                    status = "No app found for ${action.label}"
                )
            }
        } catch (e: Exception) {
            QuickActionResult(
                success = false,
                status = "Failed: ${e.message}"
            )
        }
    }

    private fun executeShare(content: String): QuickActionResult {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            QuickActionResult(success = true, status = "Share dialog opened")
        } catch (e: Exception) {
            QuickActionResult(success = false, status = "Share failed: ${e.message}")
        }
    }

    private fun executeCopyClean(action: QuickAction): QuickActionResult {
        val cleanContent = action.cleanContent ?: return QuickActionResult(
            success = false,
            status = "No clean content available"
        )

        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("borderline_clean", cleanContent))
            QuickActionResult(
                success = true,
                status = "Cleaned content copied: ${cleanContent.take(50)}${if (cleanContent.length > 50) "…" else ""}"
            )
        } catch (e: Exception) {
            QuickActionResult(
                success = false,
                status = "Copy failed: ${e.message}"
            )
        }
    }
}

/**
 * Result of executing a QuickAction.
 */
data class QuickActionResult(
    val success: Boolean,
    val status: String,
    val requiresPanelAction: Boolean = false,
    val panelAction: PanelAction? = null
)

/**
 * Actions that require the overlay panel to do something.
 */
enum class PanelAction {
    OPEN_CREATE_SNIPPET,
    OPEN_EDIT_SNIPPET,
    CLOSE_PANEL
}
