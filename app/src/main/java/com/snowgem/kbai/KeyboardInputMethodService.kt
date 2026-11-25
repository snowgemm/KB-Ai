package com.snowgem.kbai

import android.content.res.ColorStateList
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

class KeyboardInputMethodService : InputMethodService() {
    private enum class KeyTone { NORMAL, FUNCTION, ACCENT }

    private data class KeyboardKey(
        val label: String,
        val weight: Float = 1f,
        val tone: KeyTone = KeyTone.NORMAL,
        val description: String = label,
        val action: () -> Unit
    )

    private var keyboardRoot: LinearLayout? = null
    private var activeEditor: EditorInfo? = null
    private var activeLayout = KeyboardLayout.ALPHABETIC
    private var shifted = false
    private var symbolsVisible = false

    override fun onCreateInputView(): View {
        keyboardRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(8))
            setBackgroundColor(Color.rgb(232, 236, 233))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        renderKeyboard()
        return keyboardRoot!!
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        activeEditor = attribute
        activeLayout = KeyboardLayoutPolicy.initialLayout(attribute.inputType)
        shifted = attribute.initialCapsMode != 0
        symbolsVisible = false
        renderKeyboard()
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        activeEditor = info
        activeLayout = KeyboardLayoutPolicy.initialLayout(info.inputType)
        shifted = info.initialCapsMode != 0
        symbolsVisible = false
        renderKeyboard()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        activeEditor = null
        activeLayout = KeyboardLayout.ALPHABETIC
        symbolsVisible = false
    }

    private fun renderKeyboard() {
        val root = keyboardRoot ?: return
        root.removeAllViews()

        when (activeLayout) {
            KeyboardLayout.NUMERIC -> renderNumericLayout(root)
            KeyboardLayout.PHONE -> renderPhoneLayout(root)
            KeyboardLayout.ALPHABETIC -> {
                if (symbolsVisible) renderSymbolsLayout(root)
                else renderAlphabeticLayout(root)
            }
        }
    }

    private fun renderAlphabeticLayout(root: LinearLayout) {
        addRow(root, "qwertyuiop".map { letterKey(it) })
        addRow(root, "asdfghjkl".map { letterKey(it) })

        val thirdRow = mutableListOf(
            KeyboardKey("⇧", 1.45f, KeyTone.FUNCTION, "Shift") {
                shifted = !shifted
                renderKeyboard()
            }
        )
        thirdRow += "zxcvbnm".map { letterKey(it) }
        thirdRow += KeyboardKey("⌫", 1.45f, KeyTone.FUNCTION, "Backspace", ::deleteBackward)
        addRow(root, thirdRow)

        val emailField = isEmailField(activeEditor)
        val urlField = isUrlField(activeEditor)
        addRow(
            root,
            listOf(
                KeyboardKey("?123", 1.45f, KeyTone.FUNCTION, "Symbols") {
                    symbolsVisible = true
                    renderKeyboard()
                },
                KeyboardKey(
                    if (emailField) "@" else if (urlField) "/" else ",",
                    1f,
                    description = if (emailField) "At sign" else if (urlField) "Slash" else "Comma"
                ) {
                    insertText(if (emailField) "@" else if (urlField) "/" else ",")
                },
                KeyboardKey("␣", 4.8f, KeyTone.ACCENT, "Space") {
                    insertText(" ")
                },
                KeyboardKey(".", 1f, description = "Period") { insertText(".") },
                actionKey()
            )
        )
    }

    private fun renderSymbolsLayout(root: LinearLayout) {
        addRow(root, "1234567890".map { characterKey(it.toString()) })
        addRow(root, listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/").map { characterKey(it) })
        addRow(
            root,
            listOf("*", "\"", "'", ":", ";", "!", "?").map { characterKey(it) } +
                KeyboardKey("⌫", 1.45f, KeyTone.FUNCTION, "Backspace", ::deleteBackward)
        )
        addRow(
            root,
            listOf(
                KeyboardKey("ABC", 1.45f, KeyTone.FUNCTION, "Letters") {
                    symbolsVisible = false
                    renderKeyboard()
                },
                KeyboardKey(",", 1f, description = "Comma") { insertText(",") },
                KeyboardKey("␣", 4.8f, KeyTone.ACCENT, "Space") { insertText(" ") },
                KeyboardKey(".", 1f, description = "Period") { insertText(".") },
                actionKey()
            )
        )
    }

    private fun renderNumericLayout(root: LinearLayout) {
        addRow(root, listOf("1", "2", "3").map { characterKey(it) })
        addRow(root, listOf("4", "5", "6").map { characterKey(it) })
        addRow(root, listOf("7", "8", "9").map { characterKey(it) })

        val inputType = activeEditor?.inputType ?: InputType.TYPE_CLASS_NUMBER
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val supportsDateTime = inputClass == InputType.TYPE_CLASS_DATETIME
        val supportsSign = inputType and InputType.TYPE_NUMBER_FLAG_SIGNED != 0
        val supportsDecimal = inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL != 0
        val finalRow = mutableListOf<KeyboardKey>()
        if (supportsSign || supportsDateTime) finalRow += characterKey("-")
        if (supportsDecimal || supportsDateTime) finalRow += characterKey(if (supportsDateTime) "/" else ".")
        finalRow += characterKey("0", 2f)
        finalRow += KeyboardKey("⌫", 1.2f, KeyTone.FUNCTION, "Backspace", ::deleteBackward)
        addRow(root, finalRow)
        addRow(root, listOf(actionKey()))
    }

    private fun renderPhoneLayout(root: LinearLayout) {
        addRow(root, listOf("1", "2", "3").map { characterKey(it) })
        addRow(root, listOf("4", "5", "6").map { characterKey(it) })
        addRow(root, listOf("7", "8", "9").map { characterKey(it) })
        addRow(
            root,
            listOf(
                characterKey("+"),
                characterKey("*"),
                characterKey("0"),
                characterKey("#"),
                KeyboardKey("⌫", 1.2f, KeyTone.FUNCTION, "Backspace", ::deleteBackward)
            )
        )
        addRow(root, listOf(actionKey()))
    }

    private fun letterKey(letter: Char): KeyboardKey {
        val shown = if (shifted) letter.uppercaseChar() else letter
        return KeyboardKey(shown.toString(), description = shown.toString()) {
            insertText(shown.toString())
            shifted = false
            renderKeyboard()
        }
    }

    private fun characterKey(character: String, weight: Float = 1f): KeyboardKey =
        KeyboardKey(character, weight, description = character) {
            if (character.isNotEmpty()) insertText(character)
        }

    private fun actionKey(): KeyboardKey {
        val options = activeEditor?.imeOptions ?: EditorInfo.IME_ACTION_NONE
        val action = if (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
            EditorInfo.IME_ACTION_NONE
        } else {
            options and EditorInfo.IME_MASK_ACTION
        }
        val label = when (action) {
            EditorInfo.IME_ACTION_GO -> "Đi"
            EditorInfo.IME_ACTION_SEARCH -> "Tìm"
            EditorInfo.IME_ACTION_SEND -> "Gửi"
            EditorInfo.IME_ACTION_NEXT -> "Tiếp"
            EditorInfo.IME_ACTION_PREVIOUS -> "Trước"
            EditorInfo.IME_ACTION_DONE -> "Xong"
            else -> if (isMultilineEditor(activeEditor)) "↵" else "Enter"
        }
        return KeyboardKey(label, 1.45f, KeyTone.ACCENT, label) {
            val connection = currentInputConnection ?: return@KeyboardKey
            when (action) {
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_PREVIOUS,
                EditorInfo.IME_ACTION_DONE -> connection.performEditorAction(action)

                else -> if (isMultilineEditor(activeEditor)) {
                    connection.commitText("\n", 1)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            }
        }
    }

    private fun addRow(root: LinearLayout, keys: List<KeyboardKey>) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        keys.forEach { key ->
            val button = Button(this).apply {
                text = key.label
                contentDescription = key.description
                textSize = if (key.label.length > 4) 11f else 16f
                isAllCaps = false
                minWidth = 0
                minHeight = 0
                setPadding(dp(1), 0, dp(1), 0)
                backgroundTintList = ColorStateList.valueOf(
                    when (key.tone) {
                        KeyTone.NORMAL -> Color.WHITE
                        KeyTone.FUNCTION -> Color.rgb(205, 214, 208)
                        KeyTone.ACCENT -> Color.rgb(183, 211, 192)
                    }
                )
                setTextColor(Color.rgb(27, 38, 30))
                setOnClickListener { key.action() }
            }
            row.addView(
                button,
                LinearLayout.LayoutParams(0, dp(48), key.weight).apply {
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
            )
        }
        root.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun insertText(value: String) {
        if (value.isNotEmpty()) currentInputConnection?.commitText(value, 1)
    }

    private fun deleteBackward() {
        if (currentInputConnection != null) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        }
    }

    private fun isEmailField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
    }

    private fun isUrlField(info: EditorInfo?): Boolean =
        info != null &&
            (info.inputType and InputType.TYPE_MASK_VARIATION) ==
            InputType.TYPE_TEXT_VARIATION_URI

    private fun isMultilineEditor(info: EditorInfo?): Boolean =
        info != null && (info.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
