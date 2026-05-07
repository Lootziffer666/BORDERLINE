package de.lootz.borderline.core

import com.borderline.core.clipboard.ContentClassifier
import com.borderline.core.clipboard.ContentType
import org.junit.Assert.*
import org.junit.Test

class ContentClassifierTest {

    @Test
    fun `classifies URL`() {
        assertEquals(ContentType.URL, ContentClassifier.classify("https://example.com/path"))
        assertEquals(ContentType.URL, ContentClassifier.classify("http://test.org"))
    }

    @Test
    fun `classifies phone number`() {
        assertEquals(ContentType.PHONE_NUMBER, ContentClassifier.classify("+491701234567"))
        assertEquals(ContentType.PHONE_NUMBER, ContentClassifier.classify("0170-1234567"))
    }

    @Test
    fun `classifies email`() {
        assertEquals(ContentType.EMAIL, ContentClassifier.classify("user@example.com"))
    }

    @Test
    fun `classifies amount`() {
        assertEquals(ContentType.AMOUNT, ContentClassifier.classify("€ 42.50"))
        assertEquals(ContentType.AMOUNT, ContentClassifier.classify("$100"))
    }

    @Test
    fun `classifies address`() {
        assertEquals(ContentType.ADDRESS, ContentClassifier.classify("Hauptstraße 5, 10115 Berlin"))
    }

    @Test
    fun `classifies long text`() {
        val longText = "A".repeat(600)
        assertEquals(ContentType.LONG_TEXT, ContentClassifier.classify(longText))
    }

    @Test
    fun `classifies short text`() {
        assertEquals(ContentType.TEXT, ContentClassifier.classify("Hello World"))
    }

    @Test
    fun `blank returns unknown`() {
        assertEquals(ContentType.UNKNOWN, ContentClassifier.classify(""))
        assertEquals(ContentType.UNKNOWN, ContentClassifier.classify("   "))
    }
}
