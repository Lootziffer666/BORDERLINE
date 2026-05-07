package com.borderline.core.clipboard

/**
 * Classifies clipboard content into types for QuickActions and storage decisions.
 * Heuristic-based, no AI required.
 */
object ContentClassifier {

    fun classify(text: String): ContentType {
        if (text.isBlank()) return ContentType.UNKNOWN

        // Order matters: more specific patterns first
        return when {
            isUrl(text) -> ContentType.URL
            isPhoneNumber(text) -> ContentType.PHONE_NUMBER
            isEmail(text) -> ContentType.EMAIL
            isAmount(text) -> ContentType.AMOUNT
            isAddress(text) -> ContentType.ADDRESS
            isCalendarText(text) -> ContentType.CALENDAR_TEXT
            text.length > 500 -> ContentType.LONG_TEXT
            else -> ContentType.TEXT
        }
    }

    private fun isUrl(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.matches(Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)) ||
            trimmed.matches(Regex("^www\\.\\S+$", RegexOption.IGNORE_CASE))
    }

    private fun isPhoneNumber(text: String): Boolean {
        val trimmed = text.trim().replace("\\s".toRegex(), "")
        return trimmed.matches(Regex("^[+]?[0-9]{7,15}$")) ||
            trimmed.matches(Regex("^[0-9]{3,5}[/-][0-9]{4,10}$"))
    }

    private fun isEmail(text: String): Boolean {
        return text.trim().matches(
            Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
        )
    }

    private fun isAmount(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.matches(Regex(".*[€$£¥]\\s?[0-9].*")) ||
            trimmed.matches(Regex(".*[0-9]\\s?[€$£¥].*")) ||
            trimmed.matches(Regex(".*\\b[0-9]+[,.]\\d{2}\\b.*"))
    }

    private fun isAddress(text: String): Boolean {
        val lower = text.lowercase()
        // German address patterns
        val hasStreet = lower.matches(Regex(".*\\b(straße|str\\.|weg|platz|allee|gasse|ring)\\b.*"))
        val hasPostalCode = lower.matches(Regex(".*\\b\\d{4,5}\\b.*"))
        return hasStreet && hasPostalCode
    }

    private fun isCalendarText(text: String): Boolean {
        val lower = text.lowercase()
        val dateKeywords = listOf("termin", "meeting", "um ", "uhr", "am ", "vom ", "bis ")
        val datePatterns = lower.matches(Regex(".*\\d{1,2}[./]\\d{1,2}[./]\\d{2,4}.*"))
        return datePatterns && dateKeywords.any { lower.contains(it) }
    }
}

/**
 * Content type classification result.
 */
enum class ContentType(val displayName: String) {
    TEXT("Text"),
    URL("URL"),
    PHONE_NUMBER("Phone number"),
    EMAIL("Email"),
    ADDRESS("Address"),
    AMOUNT("Amount / Invoice"),
    CALENDAR_TEXT("Calendar entry"),
    LONG_TEXT("Long text"),
    IMAGE("Image"),
    FILE("File"),
    UNKNOWN("Unknown")
}
