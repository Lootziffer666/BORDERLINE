package com.borderline.core.storage

import com.borderline.core.models.SnippetKind
import com.borderline.core.models.SnippetObject
import com.borderline.core.models.StorageMode
import java.io.File
import java.util.UUID

/**
 * Manages snippet persistence with automatic inline/file storage decisions.
 *
 * - Small text (< 64 KB): stored inline in JSON metadata.
 * - Larger text: written to a dedicated file; metadata keeps only a preview.
 * - Images/files: always stored as files; metadata keeps the path.
 * - Content is **never** silently truncated.
 *
 * @param storageRoot The root directory for snippet files (e.g., app internal storage / snippets /).
 */
class SnippetStorageEngine(private val storageRoot: File) {

    init {
        if (!storageRoot.exists()) {
            storageRoot.mkdirs()
        }
    }

    /**
     * Save a text snippet, choosing inline or file storage based on size.
     *
     * @return A fully populated [SnippetObject] with correct storageMode and paths.
     */
    fun saveText(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        source: String = "",
        id: String = UUID.randomUUID().toString()
    ): SnippetObject {
        val contentBytes = content.toByteArray(Charsets.UTF_8)
        val sizeBytes = contentBytes.size.toLong()
        val checksum = SnippetObject.computeChecksum(contentBytes)
        val now = System.currentTimeMillis()

        return if (sizeBytes <= SnippetObject.INLINE_THRESHOLD_BYTES) {
            // Small text: store inline
            SnippetObject(
                id = id,
                title = title,
                kind = SnippetKind.TEXT,
                createdAt = now,
                updatedAt = now,
                source = source,
                sizeBytes = sizeBytes,
                storageMode = StorageMode.INLINE,
                previewText = content.take(PREVIEW_LENGTH),
                inlineContent = content,
                fullContentPath = null,
                checksum = checksum,
                tags = tags
            )
        } else {
            // Large text: write to file
            val file = snippetFile(id)
            file.writeBytes(contentBytes)
            SnippetObject(
                id = id,
                title = title,
                kind = SnippetKind.TEXT,
                createdAt = now,
                updatedAt = now,
                source = source,
                sizeBytes = sizeBytes,
                storageMode = StorageMode.FILE,
                previewText = content.take(PREVIEW_LENGTH),
                inlineContent = null,
                fullContentPath = file.absolutePath,
                checksum = checksum,
                tags = tags
            )
        }
    }

    /**
     * Save a binary file (image, PDF, etc.).
     *
     * @return A [SnippetObject] with FILE storage mode.
     */
    fun saveFile(
        title: String,
        data: ByteArray,
        kind: SnippetKind,
        mimeType: String = "",
        tags: List<String> = emptyList(),
        source: String = "",
        id: String = UUID.randomUUID().toString()
    ): SnippetObject {
        val file = snippetFile(id)
        file.writeBytes(data)
        val checksum = SnippetObject.computeChecksum(data)
        val now = System.currentTimeMillis()

        return SnippetObject(
            id = id,
            title = title,
            kind = kind,
            createdAt = now,
            updatedAt = now,
            source = source,
            sizeBytes = data.size.toLong(),
            storageMode = StorageMode.FILE,
            previewText = "[$kind file, ${formatSize(data.size.toLong())}]",
            inlineContent = null,
            fullContentPath = file.absolutePath,
            checksum = checksum,
            tags = tags
        )
    }

    /**
     * Save a reference (external URI, shared content).
     */
    fun saveReference(
        title: String,
        uri: String,
        previewText: String = "",
        tags: List<String> = emptyList(),
        source: String = ""
    ): SnippetObject {
        val now = System.currentTimeMillis()
        return SnippetObject(
            id = UUID.randomUUID().toString(),
            title = title,
            kind = SnippetKind.LINK,
            createdAt = now,
            updatedAt = now,
            source = source,
            sizeBytes = 0L,
            storageMode = StorageMode.REFERENCE,
            previewText = previewText.ifBlank { uri },
            inlineContent = null,
            fullContentPath = uri,
            checksum = null,
            tags = tags
        )
    }

    /**
     * Read the full content of a snippet.
     *
     * @return The full content string, or null if the file is missing/unreadable.
     */
    fun readFullContent(snippet: SnippetObject): String? {
        return when (snippet.storageMode) {
            StorageMode.INLINE -> snippet.inlineContent
            StorageMode.FILE -> {
                val path = snippet.fullContentPath ?: return null
                val file = File(path)
                if (file.exists()) file.readText(Charsets.UTF_8) else null
            }
            StorageMode.REFERENCE -> snippet.fullContentPath
        }
    }

    /**
     * Read the full content as bytes.
     */
    fun readFullBytes(snippet: SnippetObject): ByteArray? {
        return when (snippet.storageMode) {
            StorageMode.INLINE -> snippet.inlineContent?.toByteArray(Charsets.UTF_8)
            StorageMode.FILE -> {
                val path = snippet.fullContentPath ?: return null
                val file = File(path)
                if (file.exists()) file.readBytes() else null
            }
            StorageMode.REFERENCE -> null
        }
    }

    /**
     * Delete the backing file for a snippet, if any.
     */
    fun deleteBackingFile(snippet: SnippetObject): Boolean {
        if (snippet.storageMode == StorageMode.FILE && snippet.fullContentPath != null) {
            return File(snippet.fullContentPath).delete()
        }
        return true
    }

    /**
     * Verify content integrity via checksum.
     */
    fun verifyIntegrity(snippet: SnippetObject): Boolean {
        if (snippet.checksum == null) return true
        val bytes = readFullBytes(snippet) ?: return false
        return SnippetObject.computeChecksum(bytes) == snippet.checksum
    }

    /**
     * Get the storage directory size in bytes.
     */
    fun storageSizeBytes(): Long {
        return storageRoot.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun snippetFile(id: String): File = File(storageRoot, "snippet_$id.dat")

    companion object {
        /** Preview text length for file-backed snippets. */
        const val PREVIEW_LENGTH = 500

        fun formatSize(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024L * 1024 * 1024)} GB"
        }
    }
}
