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

    private var nativeBridge: NativeBridge? = null
    private var nativeSession = 0L
    private var composingText = ""
    private var expectedCursorPosition: Int? = null
    private var awaitingSelectionSync = false
    private var lastSelectionStart = -1
    private var lastSelectionEnd = -1
    private var vietnameseMode = true

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

        if (restarting) {
            finishComposition()
        } else {
            // A new editor owns a separate composition. Never flush the
            // previous app's preedit into the newly focused editor.
            composingText = ""
            expectedCursorPosition = null
            awaitingSelectionSync = false
        }
        destroyNativeSession()

        activeEditor = attribute
        activeLayout = KeyboardLayoutPolicy.initialLayout(attribute.inputType)
        shifted = attribute.initialCapsMode != 0
        symbolsVisible = false
        vietnameseMode = supportsVietnameseInput(attribute)
        lastSelectionStart = attribute.initialSelStart
        lastSelectionEnd = attribute.initialSelEnd

        if (supportsVietnameseInput(attribute)) createNativeSession()
        renderKeyboard()
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        activeEditor = info
        activeLayout = KeyboardLayoutPolicy.initialLayout(info.inputType)
        renderKeyboard()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )

        if (composingText.isNotEmpty()) {
            when {
                newSelStart != newSelEnd -> finishComposition()

                awaitingSelectionSync -> {
                    // setComposingText places the cursor after the preedit.
                    // Capture the editor's actual UTF-16 cursor position when
                    // the initial selection was unavailable.
                    expectedCursorPosition = newSelEnd
                    awaitingSelectionSync = false
                }

                expectedCursorPosition != null &&
                    newSelEnd != expectedCursorPosition -> {
                    // Do not keep editing at a stale location after the user
                    // moves the cursor away from the end of the composition.
                    finishComposition()
                }

                expectedCursorPosition == null -> {
                    expectedCursorPosition = newSelEnd
                }
            }
        }

        lastSelectionStart = newSelStart
        lastSelectionEnd = newSelEnd
    }

    override fun onFinishInput() {
        finishComposition()
        destroyNativeSession()
        activeEditor = null
        activeLayout = KeyboardLayout.ALPHABETIC
        symbolsVisible = false
        composingText = ""
        expectedCursorPosition = null
        awaitingSelectionSync = false
        lastSelectionStart = -1
        lastSelectionEnd = -1
        super.onFinishInput()
    }

    override fun onDestroy() {
        finishComposition()
        destroyNativeSession()
        super.onDestroy()
    }

    private fun createNativeSession() {
        val bridge = runCatching { NativeBridge() }.getOrNull() ?: return
        val handle = runCatching { bridge.nativeCreateSession() }.getOrDefault(0L)
        if (handle > 0L) {
            nativeBridge = bridge
            nativeSession = handle
        }
    }

    private fun destroyNativeSession() {
        val bridge = nativeBridge
        val handle = nativeSession
        nativeBridge = null
        nativeSession = 0L

        if (bridge != null && handle > 0L) {
            runCatching { bridge.nativeDestroySession(handle) }
        }
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
        val bottomRow = mutableListOf(
            KeyboardKey("?123", 1.35f, KeyTone.FUNCTION, "Symbols") {
                symbolsVisible = true
                renderKeyboard()
            }
        )

        if (canSwitchLanguage()) {
            bottomRow += languageToggleKey()
        }

        bottomRow += KeyboardKey(
            if (emailField) "@" else if (urlField) "/" else ",",
            0.85f,
            description = if (emailField) "At sign" else if (urlField) "Slash" else "Comma"
        ) {
            insertText(if (emailField) "@" else if (urlField) "/" else ",")
        }
        bottomRow += KeyboardKey("␣", 4.2f, KeyTone.ACCENT, "Space") { insertText(" ") }
        bottomRow += KeyboardKey(".", 0.85f, description = "Period") { insertText(".") }
        bottomRow += actionKey()
        addRow(root, bottomRow)
    }

    private fun renderSymbolsLayout(root: LinearLayout) {
        addRow(root, "1234567890".map { characterKey(it.toString()) })
        addRow(
            root,
            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
                .map { characterKey(it) }
        )
        addRow(
            root,
            listOf("*", "\"", "'", ":", ";", "!", "?").map { characterKey(it) } +
                KeyboardKey("⌫", 1.45f, KeyTone.FUNCTION, "Backspace", ::deleteBackward)
        )

        val bottomRow = mutableListOf(
            KeyboardKey("ABC", 1.35f, KeyTone.FUNCTION, "Letters") {
                symbolsVisible = false
                renderKeyboard()
            }
        )
        if (canSwitchLanguage()) bottomRow += languageToggleKey()
        bottomRow += KeyboardKey(",", 0.85f, description = "Comma") { insertText(",") }
        bottomRow += KeyboardKey("␣", 4.2f, KeyTone.ACCENT, "Space") { insertText(" ") }
        bottomRow += KeyboardKey(".", 0.85f, description = "Period") { insertText(".") }
        bottomRow += actionKey()
        addRow(root, bottomRow)
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
        if (supportsDecimal || supportsDateTime) {
            finalRow += characterKey(if (supportsDateTime) "/" else ".")
        }
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

    private fun languageToggleKey(): KeyboardKey {
        val nextMode = if (vietnameseMode) "EN" else "VI"
        return KeyboardKey(
            if (vietnameseMode) "VI" else "EN",
            0.9f,
            KeyTone.FUNCTION,
            "Switch to $nextMode"
        ) {
            finishComposition()
            vietnameseMode = !vietnameseMode
            renderKeyboard()
        }
    }

    private fun canSwitchLanguage(): Boolean =
        nativeBridge != null && nativeSession > 0L && supportsVietnameseInput(activeEditor)

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
        return KeyboardKey(label, 1.35f, KeyTone.ACCENT, label) {
            val connection = currentInputConnection ?: return@KeyboardKey
            finishComposition()
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
        if (value.isEmpty()) return
        val codePointCount = value.codePointCount(0, value.length)
        val codePoint = value.codePointAt(0)
        val isWordCharacter = codePointCount == 1 && Character.isLetterOrDigit(codePoint)

        if (
            activeLayout == KeyboardLayout.ALPHABETIC &&
            supportsVietnameseInput(activeEditor) &&
            nativeBridge != null &&
            nativeSession > 0L &&
            isWordCharacter
        ) {
            processInputCharacter(codePoint)
        } else {
            commitLiteral(value)
        }
    }

    private fun processInputCharacter(codePoint: Int) {
        val bridge = nativeBridge ?: return commitLiteral(String(Character.toChars(codePoint)))
        val handle = nativeSession
        val next = runCatching {
            bridge.nativeProcessKey(handle, codePoint, vietnameseMode)
        }.getOrElse {
            commitLiteral(String(Character.toChars(codePoint)))
            return
        }

        val forcedCommit = runCatching {
            bridge.nativeWasForcedCommit(handle)
        }.getOrDefault(false)

        if (forcedCommit) {
            currentInputConnection?.finishComposingText()
            composingText = ""
            expectedCursorPosition = null
            awaitingSelectionSync = false
        }

        applyComposingText(next)
    }

    private fun applyComposingText(next: String) {
        val connection = currentInputConnection ?: run {
            composingText = ""
            expectedCursorPosition = null
            awaitingSelectionSync = false
            nativeBridge?.let { bridge ->
                if (nativeSession > 0L) runCatching { bridge.nativeCommit(nativeSession) }
            }
            return
        }
        val oldText = composingText
        val anchor = when {
            oldText.isNotEmpty() && expectedCursorPosition != null ->
                expectedCursorPosition!! - oldText.length

            lastSelectionStart >= 0 && lastSelectionEnd >= 0 ->
                minOf(lastSelectionStart, lastSelectionEnd)

            else -> null
        }

        val applied = runCatching {
            connection.setComposingText(next, 1)
        }.getOrDefault(false)

        if (!applied) {
            composingText = ""
            expectedCursorPosition = null
            awaitingSelectionSync = false
            nativeBridge?.let { bridge ->
                if (nativeSession > 0L) runCatching { bridge.nativeCommit(nativeSession) }
            }
            return
        }

        composingText = next
        expectedCursorPosition = anchor?.plus(next.length)
        awaitingSelectionSync = expectedCursorPosition == null

        if (next.isEmpty()) {
            connection.finishComposingText()
            expectedCursorPosition = null
            awaitingSelectionSync = false
        }
    }

    private fun commitLiteral(value: String) {
        finishComposition()
        currentInputConnection?.commitText(value, 1)
    }

    private fun finishComposition() {
        if (composingText.isNotEmpty()) {
            currentInputConnection?.finishComposingText()
        }
        composingText = ""
        expectedCursorPosition = null
        awaitingSelectionSync = false

        val bridge = nativeBridge
        val handle = nativeSession
        if (bridge != null && handle > 0L) {
            runCatching { bridge.nativeCommit(handle) }
        }
    }

    private fun deleteBackward() {
        val bridge = nativeBridge
        if (composingText.isNotEmpty() && bridge != null && nativeSession > 0L) {
            val next = runCatching {
                bridge.nativeUndoKey(nativeSession)
            }.getOrNull()
            if (next != null) {
                applyComposingText(next)
                return
            }
        }

        if (currentInputConnection != null) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        }
    }

    private fun supportsVietnameseInput(info: EditorInfo?): Boolean =
        info != null && EditorPolicy.supportsVietnameseComposition(info.inputType)

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
