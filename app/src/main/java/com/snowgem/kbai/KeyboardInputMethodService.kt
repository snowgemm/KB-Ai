package com.snowgem.kbai

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class KeyboardInputMethodService : InputMethodService() {
    private var insertButton: Button? = null
    private var statusView: TextView? = null
    private var sensitiveField = false

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(16))
            setBackgroundColor(Color.rgb(245, 247, 245))
        }

        val title = TextView(this).apply {
            text = getString(R.string.ime_test_title)
            textSize = 18f
            setTextColor(Color.rgb(24, 34, 27))
        }
        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val description = TextView(this).apply {
            text = getString(R.string.ime_test_description)
            textSize = 13f
            setTextColor(Color.rgb(75, 88, 78))
            setPadding(0, dp(6), 0, dp(8))
        }
        root.addView(
            description,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        statusView = TextView(this).apply {
            text = getString(R.string.ime_ready)
            textSize = 13f
            setTextColor(Color.rgb(40, 75, 52))
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(
            statusView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        insertButton = Button(this).apply {
            text = getString(R.string.ime_insert_status)
            isAllCaps = false
            isEnabled = !sensitiveField
            setOnClickListener {
                if (sensitiveField) {
                    statusView?.text = getString(R.string.ime_sensitive_blocked)
                    return@setOnClickListener
                }

                val status = runCatching { NativeBridge().nativeStatus() }.getOrNull()
                if (status == null) {
                    statusView?.text = getString(R.string.native_status_error)
                    return@setOnClickListener
                }

                val inserted = runCatching {
                    currentInputConnection?.commitText(status, 1) == true
                }.getOrDefault(false)

                statusView?.text = getString(
                    if (inserted) R.string.ime_insert_success
                    else R.string.ime_insert_failed
                )
            }
        }
        root.addView(
            insertButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        return root
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        sensitiveField = EditorPolicy.isSensitive(info.inputType)
        insertButton?.isEnabled = !sensitiveField
        statusView?.text = getString(
            if (sensitiveField) R.string.ime_sensitive_blocked else R.string.ime_ready
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
