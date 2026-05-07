package com.borderline.core.models

import java.security.MessageDigest
import java.util.UUID

/**
 * A snippet treated as a first-class artifact, not a small note.
 *
 * Small text may be stored inline. Larger text, images, and files
 * are stored on the filesystem with [fullContentPath] pointing to the
 * backing file. Content is **never** silently truncated.
 */
data class SnippetObject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val kind: SnippetKind = SnippetKind.TEXT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val source: String = "",
    val sizeBytes: Long = 0L,
    val storageMode: StorageMode = StorageMode.INLINE,
    val previewText: String = "",
    val fullContentPath: String? = null,
    val inlineContent: String? = null,
    val checksum: String? = null,
    val tags: List<String> = emptyList(),
    val pinned: Boolean = false,
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null
) {
    /**
     * Returns true if the full content is stored as a file on disk
     * rather than inline in the data model.
     */
    val isFileBacked: Boolean get() = storageMode != StorageMode.INLINE

    /**
     * Returns the preview text, capped at [maxLength] characters.
     * This is for UI display only — the full content is always
     * available via [inlineContent] or [fullContentPath].
     */
    fun displayPreview(maxLength: Int = 200): String {
        return if (previewText.length > maxLength) {
            previewText.take(maxLength) + "…"
        } else {
            previewText
        }
    }

    companion object {
        /** Inline storage threshold: content below this size stays in memory/JSON. */
        const val INLINE_THRESHOLD_BYTES = 64 * 1024L  // 64 KB

        /** MVP test boundary — not a UI limit, but a technical test ceiling. */
        const val MVP_TEST_CEILING_BYTES = 100L * 1024 * 1024  // 100 MB

        /**
         * Compute SHA-256 checksum for content verification.
         */
        fun computeChecksum(content: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(content).joinToString("") { "%02x".format(it) }
        }
    }
}

/**
 * Content type of a snippet.
 */
enum class SnippetKind {
    /** Plain text, markdown, code, prompts */
    TEXT,
    /** Image file (png, jpg, webp, etc.) */
    IMAGE,
    /** Generic file (pdf, zip, etc.) */
    FILE,
    /** URL / link */
    LINK,
    /** Mixed content (text + attachments) */
    MIXED
}

/**
 * How the snippet's full content is stored.
 */
enum class StorageMode {
    /** Full content held in [SnippetObject.inlineContent]. Small text only. */
    INLINE,
    /** Full content written to a file at [SnippetObject.fullContentPath]. */
    FILE,
    /** Content exists elsewhere; [SnippetObject.fullContentPath] is a reference/URI. */
    REFERENCE
}
