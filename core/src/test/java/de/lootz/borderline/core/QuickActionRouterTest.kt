package de.lootz.borderline.core

import com.borderline.core.clipboard.ContentType
import com.borderline.core.quickactions.QuickActionRouter
import com.borderline.core.quickactions.QuickActionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuickActionRouterTest {

    private lateinit var router: QuickActionRouter

    @Before
    fun setup() {
        router = QuickActionRouter()
    }

    @Test
    fun `URL suggests open browser and share`() {
        val result = router.classifyAndSuggest("https://example.com")

        assertEquals(ContentType.URL, result.contentType)
        val types = result.actions.map { it.type }
        assertTrue(types.contains(QuickActionType.OPEN_URL))
        assertTrue(types.contains(QuickActionType.SHARE))
        assertTrue(types.contains(QuickActionType.SAVE_SNIPPET))
    }

    @Test
    fun `phone number suggests call and SMS`() {
        val result = router.classifyAndSuggest("+491701234567")

        assertEquals(ContentType.PHONE_NUMBER, result.contentType)
        val types = result.actions.map { it.type }
        assertTrue(types.contains(QuickActionType.CALL))
        assertTrue(types.contains(QuickActionType.SMS))
    }

    @Test
    fun `email suggests send email`() {
        val result = router.classifyAndSuggest("user@example.com")

        assertEquals(ContentType.EMAIL, result.contentType)
        val types = result.actions.map { it.type }
        assertTrue(types.contains(QuickActionType.EMAIL))
    }

    @Test
    fun `address suggests maps and navigation`() {
        val result = router.classifyAndSuggest("Hauptstraße 5, 10115 Berlin")

        assertEquals(ContentType.ADDRESS, result.contentType)
        val types = result.actions.map { it.type }
        assertTrue(types.contains(QuickActionType.OPEN_MAP))
        assertTrue(types.contains(QuickActionType.NAVIGATE))
    }

    @Test
    fun `long text suggests save and trim`() {
        val longText = "A".repeat(600)
        val result = router.classifyAndSuggest(longText)

        assertEquals(ContentType.LONG_TEXT, result.contentType)
        val types = result.actions.map { it.type }
        assertTrue(types.contains(QuickActionType.SAVE_SNIPPET))
        assertTrue(types.contains(QuickActionType.COPY_CLEAN))
    }

    @Test
    fun `plain text suggests share and web search`() {
        val result = router.classifyAndSuggest("Hello World")

        assertEquals(ContentType.TEXT, result.contentType)
        val types = result.actions.map { it.type }
        assertTrue(types.contains(QuickActionType.SHARE))
        assertTrue(types.contains(QuickActionType.OPEN_URL))  // web search
    }

    @Test
    fun `all suggestions include save_snippet`() {
        val texts = listOf(
            "https://example.com",
            "+491701234567",
            "user@example.com",
            "Hello",
            "A".repeat(600)
        )
        texts.forEach { text ->
            val result = router.classifyAndSuggest(text)
            assertTrue(
                "Expected SAVE_SNIPPET for: $text",
                result.actions.any { it.type == QuickActionType.SAVE_SNIPPET }
            )
        }
    }

    @Test
    fun `actions are sorted by priority descending`() {
        val result = router.classifyAndSuggest("https://example.com")
        val priorities = result.actions.map { it.priority }
        assertEquals(priorities, priorities.sortedDescending())
    }

    @Test
    fun `phone number clean copy removes non-digits`() {
        val result = router.classifyAndSuggest("+49 170-1234567")
        val cleanAction = result.actions.find { it.type == QuickActionType.COPY_CLEAN }
        assertNotNull(cleanAction)
        assertEquals("+491701234567", cleanAction!!.cleanContent)
    }
}
