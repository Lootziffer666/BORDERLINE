package com.borderline.core.models

/**
 * Visible status after a copy/save operation.
 * The user must always know what happened — no silent failures.
 */
enum class CopyStatus(val displayText: String) {
    /** Full text was copied to the system clipboard. */
    COPIED_FULL_TEXT("Copied full text"),

    /** A file reference/path was copied instead of full content. */
    COPIED_FILE_REFERENCE("Copied file reference"),

    /** Content was saved to storage but not copied to clipboard. */
    SAVED_NOT_COPIED("Saved but not copied"),

    /** Android security blocked clipboard access (SecurityException). */
    ANDROID_BLOCKED("Android blocked access"),

    /** Clipboard operation failed (generic). */
    CLIPBOARD_FAILED("Clipboard failed"),

    /** User needs to manually copy or paste. */
    NEEDS_MANUAL_ACTION("Needs manual action")
}
