package de.lootz.borderline.core

import com.borderline.core.models.SnippetKind
import com.borderline.core.models.SnippetObject
import com.borderline.core.models.StorageMode
import com.borderline.core.storage.SnippetStorageEngine
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class SnippetObjectStorageTest {

    private lateinit var storageRoot: File
    private lateinit var engine: SnippetStorageEngine

    @Before
    fun setup() {
        storageRoot = File(System.getProperty("java.io.tmpdir"), "borderline_test_${System.currentTimeMillis()}")
        storageRoot.mkdirs()
        engine = SnippetStorageEngine(storageRoot)
    }

    @After
    fun teardown() {
        storageRoot.deleteRecursively()
    }

    @Test
    fun `small text is stored inline`() {
        val content = "Hello, this is a small snippet."
        val snippet = engine.saveText("Small", content)

        assertEquals(StorageMode.INLINE, snippet.storageMode)
        assertEquals(content, snippet.inlineContent)
        assertNull(snippet.fullContentPath)
        assertEquals(content.toByteArray().size.toLong(), snippet.sizeBytes)
        assertNotNull(snippet.checksum)
    }

    @Test
    fun `1KB text stored inline`() {
        val content = "A".repeat(1024)
        val snippet = engine.saveText("1KB", content)

        assertEquals(StorageMode.INLINE, snippet.storageMode)
        assertEquals(content, snippet.inlineContent)
        assertEquals(1024L, snippet.sizeBytes)
    }

    @Test
    fun `200KB markdown stored as file`() {
        val content = "# Heading\n" + "Lorem ipsum dolor sit amet. ".repeat(8000)
        val snippet = engine.saveText("200KB Markdown", content)

        assertEquals(StorageMode.FILE, snippet.storageMode)
        assertNull(snippet.inlineContent)
        assertNotNull(snippet.fullContentPath)
        assertTrue(snippet.sizeBytes > 64 * 1024)

        // Full content is preserved
        val readBack = engine.readFullContent(snippet)
        assertEquals(content, readBack)
    }

    @Test
    fun `5MB text stored as file and fully preserved`() {
        val content = "B".repeat(5 * 1024 * 1024)
        val snippet = engine.saveText("5MB", content)

        assertEquals(StorageMode.FILE, snippet.storageMode)
        assertEquals(5L * 1024 * 1024, snippet.sizeBytes)

        val readBack = engine.readFullContent(snippet)
        assertNotNull(readBack)
        assertEquals(content.length, readBack!!.length)
        assertEquals(content, readBack)
    }

    @Test
    fun `100MB dummy text stored as file`() {
        // This test creates a 100 MB string — may need sufficient heap
        val chunkSize = 1024 * 1024  // 1 MB chunks
        val chunks = 100
        val chunk = "C".repeat(chunkSize)
        val sb = StringBuilder(chunkSize * chunks)
        repeat(chunks) { sb.append(chunk) }
        val content = sb.toString()

        val snippet = engine.saveText("100MB", content)

        assertEquals(StorageMode.FILE, snippet.storageMode)
        assertEquals(100L * 1024 * 1024, snippet.sizeBytes)

        // Verify file exists and has correct size
        val file = File(snippet.fullContentPath!!)
        assertTrue(file.exists())
        assertEquals(100L * 1024 * 1024, file.length())
    }

    @Test
    fun `content survives simulated restart`() {
        val content = "Persistent content across restart"
        val snippet = engine.saveText("Restart Test", content)

        // Create a new engine instance pointing to the same directory
        val engine2 = SnippetStorageEngine(storageRoot)
        val readBack = engine2.readFullContent(snippet)
        assertEquals(content, readBack)
    }

    @Test
    fun `no content truncation for file-backed snippets`() {
        val content = "X".repeat(100_000)
        val snippet = engine.saveText("No Truncation", content)

        assertEquals(StorageMode.FILE, snippet.storageMode)
        val readBack = engine.readFullContent(snippet)
        assertNotNull(readBack)
        assertEquals(content.length, readBack!!.length)
        // Verify every character
        assertTrue(readBack.all { it == 'X' })
    }

    @Test
    fun `checksum verifies content integrity`() {
        val content = "Checksum test content"
        val snippet = engine.saveText("Checksum", content)

        assertTrue(engine.verifyIntegrity(snippet))
    }

    @Test
    fun `preview is limited but full content preserved`() {
        val content = "Y".repeat(1000)
        val snippet = engine.saveText("Preview Test", content)

        // Preview is capped
        assertTrue(snippet.previewText.length <= SnippetStorageEngine.PREVIEW_LENGTH)
        // But full content is intact
        val fullContent = engine.readFullContent(snippet)
        assertEquals(1000, fullContent!!.length)
    }

    @Test
    fun `file deletion cleans up backing file`() {
        val content = "D".repeat(100_000)
        val snippet = engine.saveText("Delete Test", content)

        val file = File(snippet.fullContentPath!!)
        assertTrue(file.exists())

        engine.deleteBackingFile(snippet)
        assertFalse(file.exists())
    }

    @Test
    fun `binary file storage`() {
        val data = ByteArray(10_000) { (it % 256).toByte() }
        val snippet = engine.saveFile("Binary", data, SnippetKind.FILE)

        assertEquals(StorageMode.FILE, snippet.storageMode)
        assertEquals(SnippetKind.FILE, snippet.kind)
        assertEquals(10_000L, snippet.sizeBytes)

        val readBack = engine.readFullBytes(snippet)
        assertNotNull(readBack)
        assertArrayEquals(data, readBack)
    }

    @Test
    fun `reference snippet stores URI`() {
        val uri = "content://com.example/doc/123"
        val snippet = engine.saveReference("External Doc", uri)

        assertEquals(StorageMode.REFERENCE, snippet.storageMode)
        assertEquals(SnippetKind.LINK, snippet.kind)
        assertEquals(uri, snippet.fullContentPath)
    }
}
