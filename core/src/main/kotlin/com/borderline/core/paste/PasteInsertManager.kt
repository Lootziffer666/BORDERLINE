package com.borderline.core.paste

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.borderline.core.models.CopyStatus
import com.borderline.core.models.SnippetObject
import com.borderline.core.storage.SnippetStorageEngine

/**
 * Paste/Insert Mode 1: Copy → Return → User pastes manually.
 *
 * Borderline CANNOT inject text into other apps without:
 * - IME integration (locked for MVP)
 * - Accessibility Service (locked for MVP)
 *
 * Instead, the flow is:
 * 1. User selects a snippet in Borderline overlay
 * 2. Borderline copies content to clipboard
 * 3. Borderline minimizes/closes the panel
 * 4. User returns to their app and pastes (long-press → Paste)
 *
 * This is honest about Android's limitations.
 * The user always knows what happened via [PasteResult].
 */
class PasteInsertManager(private val context: Context) {

    /**
     * Copy a snippet's content to the system clipboard and prepare for user paste.
     *
     * @param snippet The snippet to prepare for pasting
     * @param storageEngine Engine to read file-backed content
     * @return [PasteResult] describing what happened
     */
    fun prepareForPaste(
        snippet: SnippetObject,
        storageEngine: SnippetStorageEngine
    ): PasteResult {
        val fullContent = storageEngine.readFullContent(snippet)
            ?: return PasteResult(
                status = CopyStatus.CLIPBOARD_FAILED,
                instruction = "Content could not be read. Check storage.",
                contentLength = 0,
                wasFullContent = false
            )

        return copyToClipboard(fullContent, snippet)
    }

    /**
     * Copy arbitrary text to the system clipboard.
     */
    fun prepareTextForPaste(text: String): PasteResult {
        return copyToClipboard(text, label = "borderline_text")
    }

    private fun copyToClipboard(
        content: String,
        snippet: SnippetObject? = null,
        label: String = "borderline"
    ): PasteResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return PasteResult(
                status = CopyStatus.CLIPBOARD_FAILED,
                instruction = "Clipboard service unavailable.",
                contentLength = content.length,
                wasFullContent = false
            )

        return try {
            if (content.length > MAX_CLIPBOARD_SIZE) {
                // Content too large for clipboard — offer file reference
                val refText = snippet?.fullContentPath ?: content.take(FALLBACK_PREVIEW_SIZE)
                clipboard.setPrimaryClip(ClipData.newPlainText(label, refText))

                PasteResult(
                    status = CopyStatus.COPIED_FILE_REFERENCE,
                    instruction = buildString {
                        append("Content is too large for clipboard (")
                        append(SnippetStorageEngine.formatSize(content.length.toLong()))
                        append("). ")
                        if (snippet?.fullContentPath != null) {
                            append("File path copied instead. ")
                            append("Open from: ${snippet.fullContentPath}")
                        } else {
                            append("First ${FALLBACK_PREVIEW_SIZE} characters copied. ")
                            append("Full content saved in Borderline storage.")
                        }
                    },
                    contentLength = content.length,
                    wasFullContent = false,
                    filePath = snippet?.fullContentPath
                )
            } else {
                // Normal copy — full content
                clipboard.setPrimaryClip(ClipData.newPlainText(label, content))

                PasteResult(
                    status = CopyStatus.COPIED_FULL_TEXT,
                    instruction = "Copied. Return to your app and paste (long-press → Paste).",
                    contentLength = content.length,
                    wasFullContent = true
                )
            }
        } catch (e: SecurityException) {
            PasteResult(
                status = CopyStatus.ANDROID_BLOCKED,
                instruction = "Android blocked clipboard access. Try opening Borderline from the app where you want to paste.",
                contentLength = content.length,
                wasFullContent = false
            )
        } catch (e: Exception) {
            PasteResult(
                status = CopyStatus.CLIPBOARD_FAILED,
                instruction = "Clipboard operation failed: ${e.message}",
                contentLength = content.length,
                wasFullContent = false
            )
        }
    }

    companion object {
        /** Maximum text size we'll attempt to put in the system clipboard. */
        const val MAX_CLIPBOARD_SIZE = 500_000  // ~500 KB

        /** Fallback preview size when file path is not available. */
        const val FALLBACK_PREVIEW_SIZE = 10_000
    }
}

/**
 * Result of a paste preparation attempt.
 * Always tells the user exactly what happened and what to do next.
 */
data class PasteResult(
    /** What happened with the clipboard. */
    val status: CopyStatus,
    /** Human-readable instruction for the user. */
    val instruction: String,
    /** Original content length in characters. */
    val contentLength: Int,
    /** True if the full content was placed in the clipboard. */
    val wasFullContent: Boolean,
    /** File path if content was too large and a file reference was used. */
    val filePath: String? = null
) {
    val isSuccess: Boolean get() = status == CopyStatus.COPIED_FULL_TEXT ||
                                    status == CopyStatus.COPIED_FILE_REFERENCE
}

/**
 * Android paste limitations, documented and visible.
 */
object AndroidPasteLimitations {
    /**
     * What Borderline MVP CAN do:
     * - Copy text to system clipboard
     * - Copy file references for large content
     * - Show clear status of what happened
     *
     * What Borderline MVP CANNOT do (by design, not by bug):
     * - Inject text directly into other apps' text fields
     * - Act as an IME (keyboard) to type text
     * - Use Accessibility Service to paste
     * - Background clipboard monitoring (API 30+ blocks this)
     *
     * The "Copy & Return" flow is the honest approach:
     * 1. Select content in Borderline
     * 2. Content is copied to clipboard
     * 3. Return to target app
     * 4. Long-press → Paste
     */
    const val DOCUMENTATION = "See docs/BORDERLINE_MVP_LOCKS.md"
}
