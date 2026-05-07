package de.lootz.borderline.core

import com.borderline.core.models.CopyStatus
import com.borderline.core.paste.PasteResult
import org.junit.Assert.*
import org.junit.Test

class PasteResultTest {

    @Test
    fun `successful full copy is success`() {
        val result = PasteResult(
            status = CopyStatus.COPIED_FULL_TEXT,
            instruction = "Copied.",
            contentLength = 100,
            wasFullContent = true
        )
        assertTrue(result.isSuccess)
        assertTrue(result.wasFullContent)
    }

    @Test
    fun `file reference is success but not full content`() {
        val result = PasteResult(
            status = CopyStatus.COPIED_FILE_REFERENCE,
            instruction = "File path copied.",
            contentLength = 1_000_000,
            wasFullContent = false,
            filePath = "/data/borderline/snippets/abc.dat"
        )
        assertTrue(result.isSuccess)
        assertFalse(result.wasFullContent)
        assertNotNull(result.filePath)
    }

    @Test
    fun `clipboard failed is not success`() {
        val result = PasteResult(
            status = CopyStatus.CLIPBOARD_FAILED,
            instruction = "Failed.",
            contentLength = 50,
            wasFullContent = false
        )
        assertFalse(result.isSuccess)
    }

    @Test
    fun `android blocked is not success`() {
        val result = PasteResult(
            status = CopyStatus.ANDROID_BLOCKED,
            instruction = "Blocked.",
            contentLength = 50,
            wasFullContent = false
        )
        assertFalse(result.isSuccess)
    }

    @Test
    fun `instruction is always non-empty`() {
        CopyStatus.values().forEach { status ->
            val result = PasteResult(
                status = status,
                instruction = status.displayText,
                contentLength = 0,
                wasFullContent = false
            )
            assertTrue("Instruction for $status should not be blank", result.instruction.isNotBlank())
        }
    }
}
