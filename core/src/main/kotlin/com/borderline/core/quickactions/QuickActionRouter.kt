package com.borderline.core.quickactions

import com.borderline.core.clipboard.ContentClassifier
import com.borderline.core.clipboard.ContentType

/**
 * QuickActions v0: Context router with heuristic pattern matching.
 *
 * When the user copies something and opens Borderline, QuickActions
 * suggests relevant actions based on what was copied.
 *
 * Example:
 * - Copied a URL → offer: "Open in Browser", "Save as Link Snippet", "Share"
 * - Copied a phone number → offer: "Call", "Save to Contacts", "Copy Clean"
 * - Copied an address → offer: "Open in Maps", "Save as Address Snippet"
 * - Copied a long text → offer: "Save as Snippet", "Summarize (future)", "Copy Clean"
 *
 * All actions are intents or clipboard operations — no AI, no network calls.
 */
class QuickActionRouter {

    /**
     * Given clipboard content and its classified type, suggest actions.
     *
     * @param content The clipboard text
     * @param contentType The classified content type (from ContentClassifier)
     * @return Ordered list of suggested QuickActions (most relevant first)
     */
    fun suggestActions(content: String, contentType: ContentType): List<QuickAction> {
        val actions = mutableListOf<QuickAction>()

        // Universal action: Save as snippet
        actions.add(QuickAction(
            id = "save_snippet",
            label = "Save as Snippet",
            icon = "💾",
            type = QuickActionType.SAVE_SNIPPET,
            priority = 50
        ))

        // Type-specific actions
        when (contentType) {
            ContentType.URL -> {
                actions.add(0, QuickAction(
                    id = "open_browser",
                    label = "Open in Browser",
                    icon = "🌐",
                    type = QuickActionType.OPEN_URL,
                    priority = 100,
                    intentAction = "android.intent.action.VIEW",
                    intentData = content.trim()
                ))
                actions.add(1, QuickAction(
                    id = "share_url",
                    label = "Share URL",
                    icon = "📤",
                    type = QuickActionType.SHARE,
                    priority = 80
                ))
            }

            ContentType.PHONE_NUMBER -> {
                val cleanNumber = content.trim().replace("[^+0-9]".toRegex(), "")
                actions.add(0, QuickAction(
                    id = "call",
                    label = "Call $cleanNumber",
                    icon = "📞",
                    type = QuickActionType.CALL,
                    priority = 100,
                    intentAction = "android.intent.action.DIAL",
                    intentData = "tel:$cleanNumber"
                ))
                actions.add(1, QuickAction(
                    id = "sms",
                    label = "Send SMS",
                    icon = "💬",
                    type = QuickActionType.SMS,
                    priority = 80,
                    intentAction = "android.intent.action.SENDTO",
                    intentData = "smsto:$cleanNumber"
                ))
                actions.add(QuickAction(
                    id = "copy_clean_number",
                    label = "Copy Clean Number",
                    icon = "📋",
                    type = QuickActionType.COPY_CLEAN,
                    priority = 60,
                    cleanContent = cleanNumber
                ))
            }

            ContentType.EMAIL -> {
                actions.add(0, QuickAction(
                    id = "send_email",
                    label = "Send Email",
                    icon = "✉️",
                    type = QuickActionType.EMAIL,
                    priority = 100,
                    intentAction = "android.intent.action.SENDTO",
                    intentData = "mailto:${content.trim()}"
                ))
            }

            ContentType.ADDRESS -> {
                val encodedAddress = content.trim().replace(" ", "+")
                actions.add(0, QuickAction(
                    id = "open_maps",
                    label = "Open in Maps",
                    icon = "🗺️",
                    type = QuickActionType.OPEN_MAP,
                    priority = 100,
                    intentAction = "android.intent.action.VIEW",
                    intentData = "geo:0,0?q=$encodedAddress"
                ))
                actions.add(1, QuickAction(
                    id = "navigate",
                    label = "Navigate Here",
                    icon = "🧭",
                    type = QuickActionType.NAVIGATE,
                    priority = 90,
                    intentAction = "android.intent.action.VIEW",
                    intentData = "google.navigation:q=$encodedAddress"
                ))
            }

            ContentType.AMOUNT -> {
                actions.add(0, QuickAction(
                    id = "save_amount",
                    label = "Save Amount/Invoice",
                    icon = "💰",
                    type = QuickActionType.SAVE_SNIPPET,
                    priority = 90
                ))
            }

            ContentType.CALENDAR_TEXT -> {
                actions.add(0, QuickAction(
                    id = "create_event",
                    label = "Create Calendar Event",
                    icon = "📅",
                    type = QuickActionType.CALENDAR_EVENT,
                    priority = 100,
                    intentAction = "android.intent.action.INSERT",
                    intentData = "content://com.android.calendar/events"
                ))
            }

            ContentType.LONG_TEXT -> {
                actions.add(0, QuickAction(
                    id = "save_long",
                    label = "Save Full Text",
                    icon = "📄",
                    type = QuickActionType.SAVE_SNIPPET,
                    priority = 100
                ))
                actions.add(QuickAction(
                    id = "copy_trimmed",
                    label = "Copy First 500 Chars",
                    icon = "✂️",
                    type = QuickActionType.COPY_CLEAN,
                    priority = 40,
                    cleanContent = content.take(500)
                ))
            }

            ContentType.TEXT -> {
                actions.add(QuickAction(
                    id = "share_text",
                    label = "Share",
                    icon = "📤",
                    type = QuickActionType.SHARE,
                    priority = 60
                ))
                // Web search
                val encoded = content.trim().replace(" ", "+")
                actions.add(QuickAction(
                    id = "web_search",
                    label = "Search Web",
                    icon = "🔍",
                    type = QuickActionType.OPEN_URL,
                    priority = 50,
                    intentAction = "android.intent.action.VIEW",
                    intentData = "https://www.google.com/search?q=$encoded"
                ))
            }

            ContentType.IMAGE, ContentType.FILE -> {
                actions.add(0, QuickAction(
                    id = "open_file",
                    label = "Open File",
                    icon = "📂",
                    type = QuickActionType.OPEN_URL,
                    priority = 100,
                    intentAction = "android.intent.action.VIEW",
                    intentData = content.trim()
                ))
            }

            ContentType.UNKNOWN -> {
                // No extra actions beyond save_snippet
            }
        }

        return actions.sortedByDescending { it.priority }
    }

    /**
     * Convenience: classify content and suggest actions in one step.
     */
    fun classifyAndSuggest(content: String): QuickActionSuggestion {
        val contentType = ContentClassifier.classify(content)
        val actions = suggestActions(content, contentType)
        return QuickActionSuggestion(
            content = content,
            contentType = contentType,
            actions = actions
        )
    }
}

/**
 * A single suggested action.
 */
data class QuickAction(
    val id: String,
    val label: String,
    val icon: String,
    val type: QuickActionType,
    val priority: Int = 50,
    val intentAction: String? = null,
    val intentData: String? = null,
    val cleanContent: String? = null
)

/**
 * Types of quick actions.
 */
enum class QuickActionType {
    OPEN_URL,
    CALL,
    SMS,
    EMAIL,
    OPEN_MAP,
    NAVIGATE,
    SHARE,
    SAVE_SNIPPET,
    COPY_CLEAN,
    CALENDAR_EVENT
}

/**
 * Bundle of content type + suggested actions.
 */
data class QuickActionSuggestion(
    val content: String,
    val contentType: ContentType,
    val actions: List<QuickAction>
)
