package com.borderline.feature.overlay.panels

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.borderline.core.models.CopyStatus
import com.borderline.core.models.SnippetKind
import com.borderline.core.models.SnippetObject
import com.borderline.core.models.StorageMode
import com.borderline.core.storage.SnippetObjectRepository
import com.borderline.core.storage.SnippetStorageEngine

/**
 * Snippet Manager v0 — Overlay panel for snippet CRUD.
 *
 * Features:
 * - Create new snippets
 * - Edit existing snippets
 * - Delete snippets
 * - Duplicate snippets
 * - Search snippets (title + preview)
 * - Copy snippet content to clipboard
 * - Open/view file-backed snippet content
 * - Recently used
 * - Favorites / Pins
 *
 * UI Principle: Not a notes app. More like:
 *   Search → Select → Copy/Open
 *
 * Large snippets are NOT fully rendered in the list.
 * Copy status is always shown clearly.
 */
@SuppressLint("ClickableViewAccessibility")
class SnippetManagerPanel(
    private val context: Context,
    private val repository: SnippetObjectRepository,
    private val storageEngine: SnippetStorageEngine
) {

    private var rootView: FrameLayout? = null
    private var listContainer: LinearLayout? = null
    private var searchField: EditText? = null
    private var statusText: TextView? = null
    private var currentMode: PanelMode = PanelMode.LIST
    private var editingSnippetId: String? = null
    private var currentQuery: String = ""

    enum class PanelMode { LIST, CREATE, EDIT, DETAIL }

    /**
     * Create the panel view. Call once, then show/hide as needed.
     */
    fun createView(): View {
        val root = FrameLayout(context).apply {
            background = GradientDrawable().apply {
                setColor(0xF0181818.toInt())
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Header
        layout.addView(headerRow())

        // Search field
        searchField = EditText(context).apply {
            hint = "Search snippets..."
            setHintTextColor(0xFF666666.toInt())
            setTextColor(0xFFDDDDDD.toInt())
            textSize = 14f
            background = GradientDrawable().apply {
                setColor(0xFF252525.toInt())
                cornerRadius = dp(8).toFloat()
            }
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
            setSingleLine(true)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    currentQuery = s?.toString() ?: ""
                    refreshList()
                }
            })
        }
        layout.addView(searchField)

        // Status text (shows copy results)
        statusText = TextView(context).apply {
            textSize = 12f
            setTextColor(0xFF4CAF50.toInt())
            visibility = View.GONE
            setPadding(0, 0, 0, dp(4))
        }
        layout.addView(statusText)

        // Scrollable list
        val scroll = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
        }
        listContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(listContainer)
        layout.addView(scroll)

        root.addView(layout)
        rootView = root

        refreshList()
        return root
    }

    private fun headerRow(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))

            addView(TextView(context).apply {
                text = "Snippets"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            // Add button
            addView(TextView(context).apply {
                text = "＋"
                textSize = 22f
                setTextColor(0xFF4CAF50.toInt())
                setPadding(dp(12), 0, dp(4), 0)
                setOnClickListener { showCreateMode() }
            })
        }
    }

    fun refreshList() {
        val container = listContainer ?: return
        container.removeAllViews()

        val snippets = if (currentQuery.isBlank()) {
            repository.snippets.value
        } else {
            repository.search(currentQuery)
        }

        if (snippets.isEmpty()) {
            container.addView(TextView(context).apply {
                text = if (currentQuery.isBlank()) "No snippets yet. Tap ＋ to create one."
                       else "No matches for \"$currentQuery\""
                textSize = 13f
                setTextColor(0xFF666666.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dp(32), 0, 0)
            })
            return
        }

        // Show pinned first, then by last used / created
        val sorted = snippets.sortedWith(
            compareByDescending<SnippetObject> { it.pinned }
                .thenByDescending { it.lastUsedAt ?: it.createdAt }
        )

        sorted.forEach { snippet ->
            container.addView(snippetRow(snippet))
        }
    }

    private fun snippetRow(snippet: SnippetObject): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(0xFF222222.toInt())
                cornerRadius = dp(6).toFloat()
            }
            setPadding(dp(10), dp(8), dp(10), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }

            // Title row with pin indicator
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                if (snippet.pinned) {
                    addView(TextView(context).apply {
                        text = "📌 "
                        textSize = 12f
                    })
                }

                addView(TextView(context).apply {
                    text = snippet.title
                    textSize = 14f
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, Typeface.BOLD)
                    maxLines = 1
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })

                // Size indicator for large snippets
                if (snippet.isFileBacked) {
                    addView(TextView(context).apply {
                        text = SnippetStorageEngine.formatSize(snippet.sizeBytes)
                        textSize = 11f
                        setTextColor(0xFF888888.toInt())
                        setPadding(dp(8), 0, 0, 0)
                    })
                }
            })

            // Preview text (limited, not full content)
            addView(TextView(context).apply {
                text = snippet.displayPreview(150)
                textSize = 12f
                setTextColor(0xFFAAAAAA.toInt())
                maxLines = 2
                setPadding(0, dp(2), 0, dp(4))
            })

            // Action buttons row
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END

                // Copy button
                addView(actionButton("Copy") {
                    copySnippet(snippet)
                })

                // Pin/Unpin
                addView(actionButton(if (snippet.pinned) "Unpin" else "Pin") {
                    repository.togglePin(snippet.id)
                    refreshList()
                })

                // Edit
                addView(actionButton("Edit") {
                    showEditMode(snippet)
                })

                // Duplicate
                addView(actionButton("Dup") {
                    duplicateSnippet(snippet)
                })

                // Delete
                addView(actionButton("Del") {
                    repository.delete(snippet.id)
                    refreshList()
                })
            })

            // Tap on row = copy
            setOnClickListener { copySnippet(snippet) }
        }
    }

    private fun actionButton(label: String, onClick: () -> Unit): View {
        return TextView(context).apply {
            text = label
            textSize = 11f
            setTextColor(0xFF888888.toInt())
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { onClick() }
        }
    }

    private fun copySnippet(snippet: SnippetObject) {
        repository.markUsed(snippet.id)

        val fullContent = storageEngine.readFullContent(snippet)
        if (fullContent == null) {
            showStatus(CopyStatus.CLIPBOARD_FAILED)
            return
        }

        // Try to copy to clipboard
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // For very large content, copy a file reference instead
            if (fullContent.length > MAX_CLIPBOARD_TEXT_LENGTH && snippet.isFileBacked) {
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("borderline_ref", snippet.fullContentPath ?: fullContent.take(MAX_CLIPBOARD_TEXT_LENGTH))
                )
                showStatus(CopyStatus.COPIED_FILE_REFERENCE)
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("borderline", fullContent))
                showStatus(CopyStatus.COPIED_FULL_TEXT)
            }
            hapticFeedback()
        } catch (e: Exception) {
            showStatus(CopyStatus.CLIPBOARD_FAILED)
        }

        refreshList()
    }

    private fun duplicateSnippet(snippet: SnippetObject) {
        val fullContent = storageEngine.readFullContent(snippet)
        if (fullContent != null) {
            val newSnippet = storageEngine.saveText(
                title = "${snippet.title} (copy)",
                content = fullContent,
                tags = snippet.tags,
                source = "duplicated from ${snippet.id}"
            )
            repository.add(newSnippet)
            refreshList()
        }
    }

    private fun showCreateMode() {
        currentMode = PanelMode.CREATE
        editingSnippetId = null
        showEditor("", "", "Create Snippet")
    }

    private fun showEditMode(snippet: SnippetObject) {
        currentMode = PanelMode.EDIT
        editingSnippetId = snippet.id
        val fullContent = storageEngine.readFullContent(snippet) ?: snippet.previewText
        showEditor(snippet.title, fullContent, "Edit Snippet")
    }

    private fun showEditor(title: String, content: String, headerText: String) {
        val container = listContainer ?: return
        container.removeAllViews()

        // Header
        container.addView(TextView(context).apply {
            text = headerText
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        // Title input
        val titleInput = EditText(context).apply {
            hint = "Title"
            setText(title)
            setHintTextColor(0xFF666666.toInt())
            setTextColor(0xFFDDDDDD.toInt())
            textSize = 14f
            background = GradientDrawable().apply {
                setColor(0xFF252525.toInt())
                cornerRadius = dp(6).toFloat()
            }
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setSingleLine(true)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) }
        }
        container.addView(titleInput)

        // Content input (multiline)
        val contentInput = EditText(context).apply {
            hint = "Content (text, prompt, markdown...)"
            setText(content)
            setHintTextColor(0xFF666666.toInt())
            setTextColor(0xFFDDDDDD.toInt())
            textSize = 13f
            background = GradientDrawable().apply {
                setColor(0xFF252525.toInt())
                cornerRadius = dp(6).toFloat()
            }
            setPadding(dp(10), dp(8), dp(10), dp(8))
            minLines = 4
            maxLines = 10
            gravity = Gravity.TOP or Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }
        container.addView(contentInput)

        // Button row
        container.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END

            addView(TextView(context).apply {
                text = "Cancel"
                textSize = 14f
                setTextColor(0xFF888888.toInt())
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setOnClickListener {
                    currentMode = PanelMode.LIST
                    refreshList()
                }
            })

            addView(TextView(context).apply {
                text = "Save"
                textSize = 14f
                setTextColor(0xFF4CAF50.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setOnClickListener {
                    val t = titleInput.text.toString().trim()
                    val c = contentInput.text.toString()
                    if (t.isBlank()) {
                        titleInput.error = "Title required"
                        return@setOnClickListener
                    }

                    if (currentMode == PanelMode.CREATE) {
                        val snippet = storageEngine.saveText(title = t, content = c)
                        repository.add(snippet)
                    } else if (currentMode == PanelMode.EDIT && editingSnippetId != null) {
                        val existing = repository.getById(editingSnippetId!!)
                        if (existing != null) {
                            // Delete old backing file if content changed
                            storageEngine.deleteBackingFile(existing)
                            val updated = storageEngine.saveText(
                                title = t, content = c,
                                tags = existing.tags, source = existing.source,
                                id = existing.id
                            )
                            repository.update(updated.copy(
                                createdAt = existing.createdAt,
                                pinned = existing.pinned,
                                usageCount = existing.usageCount,
                                lastUsedAt = existing.lastUsedAt
                            ))
                        }
                    }

                    currentMode = PanelMode.LIST
                    editingSnippetId = null
                    refreshList()
                    showStatus(CopyStatus.SAVED_NOT_COPIED)
                }
            })
        })
    }

    private fun showStatus(status: CopyStatus) {
        statusText?.apply {
            text = status.displayText
            setTextColor(when (status) {
                CopyStatus.COPIED_FULL_TEXT -> 0xFF4CAF50.toInt()
                CopyStatus.COPIED_FILE_REFERENCE -> 0xFF2196F3.toInt()
                CopyStatus.SAVED_NOT_COPIED -> 0xFF888888.toInt()
                CopyStatus.ANDROID_BLOCKED -> 0xFFFF5252.toInt()
                CopyStatus.CLIPBOARD_FAILED -> 0xFFFF5252.toInt()
                CopyStatus.NEEDS_MANUAL_ACTION -> 0xFFFF9800.toInt()
            })
            visibility = View.VISIBLE
            // Auto-hide after 3 seconds
            postDelayed({ visibility = View.GONE }, 3000)
        }
    }

    private fun hapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                }
            }
        } catch (_: Exception) {}
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    companion object {
        /**
         * Maximum text length to put directly in the clipboard.
         * Beyond this, we copy a file reference instead and show the appropriate status.
         */
        const val MAX_CLIPBOARD_TEXT_LENGTH = 500_000  // ~500 KB of text
    }
}
