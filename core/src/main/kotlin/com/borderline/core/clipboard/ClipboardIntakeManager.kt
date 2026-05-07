package com.borderline.core.clipboard

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

/**
 * Clipboard Intake Mode 1: Minimal clipboard reading flow.
 *
 * User copies → Borderline reads (when possible) → classifies → stores.
 *
 * Android clipboard access rules:
 * - API 26-29: Apps can read clipboard freely
 * - API 30+: Only the app in focus or with the default IME can read clipboard
 * - API 33+: System shows a toast when any app reads clipboard
 *
 * Borderline does NOT run a background clipboard listener.
 * Reading happens only when the user explicitly opens the Clipboard+ panel.
 */
class ClipboardIntakeManager(private val context: Context) {

    /**
     * Attempt to read the current clipboard content.
     *
     * @return [IntakeResult] with content and status, or an error status.
     */
    fun readClipboard(): IntakeResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return IntakeResult.error(IntakeStatus.ANDROID_BLOCKED, "ClipboardManager unavailable")

        if (!clipboard.hasPrimaryClip()) {
            return IntakeResult.error(IntakeStatus.EMPTY, "Clipboard is empty")
        }

        val clip = clipboard.primaryClip
            ?: return IntakeResult.error(IntakeStatus.EMPTY, "No clip data")

        if (clip.itemCount == 0) {
            return IntakeResult.error(IntakeStatus.EMPTY, "Clip has no items")
        }

        return try {
            val item = clip.getItemAt(0)
            val description = clip.description

            // Determine content type from MIME
            val mimeType = if (description != null && description.mimeTypeCount > 0) {
                description.getMimeType(0) ?: "text/plain"
            } else {
                "text/plain"
            }

            when {
                // Text content
                description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true ||
                description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true ||
                description?.hasMimeType("text/*") == true -> {
                    val text = item.coerceToText(context)?.toString()
                    if (text.isNullOrBlank()) {
                        IntakeResult.error(IntakeStatus.EMPTY, "Clipboard text is blank")
                    } else {
                        val contentType = ContentClassifier.classify(text)
                        IntakeResult.success(
                            content = text,
                            contentType = contentType,
                            mimeType = mimeType,
                            sizeBytes = text.toByteArray(Charsets.UTF_8).size.toLong()
                        )
                    }
                }

                // URI content (image, file)
                item.uri != null -> {
                    val uri = item.uri.toString()
                    val isImage = mimeType.startsWith("image/")
                    IntakeResult.success(
                        content = uri,
                        contentType = if (isImage) ContentType.IMAGE else ContentType.FILE,
                        mimeType = mimeType,
                        sizeBytes = 0L, // Size unknown for URIs without reading
                        isUri = true
                    )
                }

                // Fallback: try coercing to text
                else -> {
                    val text = item.coerceToText(context)?.toString()
                    if (text.isNullOrBlank()) {
                        IntakeResult.error(IntakeStatus.UNREADABLE, "Content not readable")
                    } else {
                        IntakeResult.success(
                            content = text,
                            contentType = ContentClassifier.classify(text),
                            mimeType = mimeType,
                            sizeBytes = text.toByteArray(Charsets.UTF_8).size.toLong()
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            IntakeResult.error(IntakeStatus.ANDROID_BLOCKED, "Android blocked clipboard access: ${e.message}")
        } catch (e: Exception) {
            IntakeResult.error(IntakeStatus.UNREADABLE, "Failed to read clipboard: ${e.message}")
        }
    }

    /**
     * Check if clipboard reading is likely to succeed on this Android version.
     */
    fun canReadClipboard(): ClipboardAccessInfo {
        val sdk = Build.VERSION.SDK_INT
        return when {
            sdk < 29 -> ClipboardAccessInfo(
                canRead = true,
                restriction = "None — free clipboard access",
                needsFocus = false
            )
            sdk < 33 -> ClipboardAccessInfo(
                canRead = true,
                restriction = "App must be in foreground or have focus",
                needsFocus = true
            )
            else -> ClipboardAccessInfo(
                canRead = true,
                restriction = "App must have focus; system shows toast on read",
                needsFocus = true
            )
        }
    }
}

/**
 * Result of a clipboard read attempt.
 */
data class IntakeResult(
    val status: IntakeStatus,
    val content: String? = null,
    val contentType: ContentType = ContentType.UNKNOWN,
    val mimeType: String = "",
    val sizeBytes: Long = 0L,
    val isUri: Boolean = false,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean get() = status == IntakeStatus.SUCCESS

    companion object {
        fun success(
            content: String,
            contentType: ContentType,
            mimeType: String,
            sizeBytes: Long,
            isUri: Boolean = false
        ) = IntakeResult(
            status = IntakeStatus.SUCCESS,
            content = content,
            contentType = contentType,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            isUri = isUri
        )

        fun error(status: IntakeStatus, message: String) = IntakeResult(
            status = status,
            errorMessage = message
        )
    }
}

/**
 * Status of a clipboard intake attempt.
 */
enum class IntakeStatus(val displayText: String) {
    SUCCESS("✓ Content read"),
    EMPTY("Clipboard is empty"),
    ANDROID_BLOCKED("Android blocked access"),
    UNREADABLE("Content not readable"),
    TOO_LARGE("Content too large for clipboard")
}

/**
 * Information about clipboard access capabilities on this device.
 */
data class ClipboardAccessInfo(
    val canRead: Boolean,
    val restriction: String,
    val needsFocus: Boolean
)
