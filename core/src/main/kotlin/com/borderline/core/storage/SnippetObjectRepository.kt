package com.borderline.core.storage

import com.borderline.core.models.SnippetObject
import com.borderline.core.models.StorageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Repository for [SnippetObject] items with file-backed storage support.
 *
 * Metadata is persisted as JSON. Large content is stored on disk via
 * [SnippetStorageEngine]. Content is **never** silently truncated.
 *
 * @param metadataFile JSON file for snippet metadata.
 * @param storageEngine Engine for file-backed content.
 */
class SnippetObjectRepository(
    private val metadataFile: File,
    private val storageEngine: SnippetStorageEngine
) {

    private val _snippets = MutableStateFlow(loadAll())
    val snippets: StateFlow<List<SnippetObject>> = _snippets.asStateFlow()

    fun add(snippet: SnippetObject) {
        val list = _snippets.value.toMutableList()
        list.add(0, snippet)  // newest first
        persist(list)
    }

    fun update(snippet: SnippetObject) {
        val list = _snippets.value.map { if (it.id == snippet.id) snippet else it }
        persist(list)
    }

    fun delete(id: String) {
        val existing = _snippets.value.find { it.id == id }
        if (existing != null) {
            storageEngine.deleteBackingFile(existing)
        }
        val list = _snippets.value.filter { it.id != id }
        persist(list)
    }

    fun getById(id: String): SnippetObject? = _snippets.value.find { it.id == id }

    fun search(query: String): List<SnippetObject> {
        val q = query.lowercase()
        return _snippets.value.filter {
            it.title.lowercase().contains(q) ||
            it.previewText.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }

    fun getRecent(limit: Int = 10): List<SnippetObject> {
        return _snippets.value
            .sortedByDescending { it.lastUsedAt ?: it.createdAt }
            .take(limit)
    }

    fun getPinned(): List<SnippetObject> {
        return _snippets.value.filter { it.pinned }
    }

    fun markUsed(id: String) {
        val snippet = getById(id) ?: return
        update(snippet.copy(
            usageCount = snippet.usageCount + 1,
            lastUsedAt = System.currentTimeMillis()
        ))
    }

    fun togglePin(id: String) {
        val snippet = getById(id) ?: return
        update(snippet.copy(pinned = !snippet.pinned))
    }

    /**
     * Read the full content of a snippet. For file-backed snippets,
     * this reads from disk. For inline snippets, returns the inline content.
     */
    fun readFullContent(snippet: SnippetObject): String? {
        return storageEngine.readFullContent(snippet)
    }

    // ── JSON serialization ──────────────────────────────

    private fun persist(list: List<SnippetObject>) {
        val json = JSONArray()
        list.forEach { s ->
            json.put(JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("kind", s.kind.name)
                put("createdAt", s.createdAt)
                put("updatedAt", s.updatedAt)
                put("source", s.source)
                put("sizeBytes", s.sizeBytes)
                put("storageMode", s.storageMode.name)
                put("previewText", s.previewText)
                if (s.storageMode == StorageMode.INLINE) {
                    put("inlineContent", s.inlineContent ?: "")
                }
                put("fullContentPath", s.fullContentPath ?: "")
                put("checksum", s.checksum ?: "")
                put("tags", JSONArray(s.tags))
                put("pinned", s.pinned)
                put("usageCount", s.usageCount)
                put("lastUsedAt", s.lastUsedAt ?: 0L)
            })
        }
        metadataFile.parentFile?.mkdirs()
        metadataFile.writeText(json.toString(2))
        _snippets.value = list
    }

    private fun loadAll(): List<SnippetObject> {
        if (!metadataFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(metadataFile.readText())
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val tags = mutableListOf<String>()
                val tagsArr = obj.optJSONArray("tags")
                if (tagsArr != null) {
                    for (j in 0 until tagsArr.length()) {
                        tags.add(tagsArr.getString(j))
                    }
                }
                SnippetObject(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    kind = enumValueOrDefault(obj.optString("kind", "TEXT")),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                    source = obj.optString("source", ""),
                    sizeBytes = obj.optLong("sizeBytes", 0L),
                    storageMode = enumValueOrDefault(obj.optString("storageMode", "INLINE")),
                    previewText = obj.optString("previewText", ""),
                    inlineContent = obj.optString("inlineContent", "").ifBlank { null },
                    fullContentPath = obj.optString("fullContentPath", "").ifBlank { null },
                    checksum = obj.optString("checksum", "").ifBlank { null },
                    tags = tags,
                    pinned = obj.optBoolean("pinned", false),
                    usageCount = obj.optInt("usageCount", 0),
                    lastUsedAt = obj.optLong("lastUsedAt", 0L).let { if (it == 0L) null else it }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String): T {
        return try {
            enumValueOf<T>(name)
        } catch (e: IllegalArgumentException) {
            enumValues<T>().first()
        }
    }
}
