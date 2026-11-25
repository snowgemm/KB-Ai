package com.snowgem.kbai

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(24))
            isFocusableInTouchMode = true
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_title)
            textSize = 28f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.rgb(24, 34, 27))
        }
        addContent(content, title, 4)

        val phaseTitle = TextView(this).apply {
            text = getString(R.string.phase1_title)
            textSize = 17f
            setTextColor(Color.rgb(40, 75, 52))
        }
        addContent(content, phaseTitle, 10)

        val description = TextView(this).apply {
            text = getString(R.string.phase1_description)
            textSize = 15f
            setTextColor(Color.rgb(75, 88, 78))
        }
        addContent(content, description, 22)

        val bridgeStatus = TextView(this).apply {
            text = getString(R.string.native_status_pending)
            textSize = 14f
            setTextColor(Color.rgb(40, 75, 52))
        }
        addContent(content, bridgeStatus)

        val bridgeButton = Button(this).apply {
            text = getString(R.string.native_test_button)
            isAllCaps = false
            setOnClickListener {
                val result = runCatching { NativeBridge().nativeStatus() }.getOrNull()
                bridgeStatus.text = if (result != null) {
                    getString(R.string.native_status_success, result)
                } else {
                    getString(R.string.native_status_error)
                }
            }
        }
        addContent(content, bridgeButton, 18)

        val settingsButton = Button(this).apply {
            text = getString(R.string.open_ime_settings)
            isAllCaps = false
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }
        addContent(content, settingsButton)

        val pickerButton = Button(this).apply {
            text = getString(R.string.show_ime_picker)
            isAllCaps = false
            setOnClickListener {
                getSystemService(InputMethodManager::class.java).showInputMethodPicker()
            }
        }
        addContent(content, pickerButton, 22)

        val testTitle = TextView(this).apply {
            text = getString(R.string.test_field_title)
            textSize = 17f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.rgb(24, 34, 27))
        }
        addContent(content, testTitle, 8)

        addTestField(
            content,
            R.string.test_text_label,
            R.string.test_text_hint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
            EditorInfo.IME_ACTION_NEXT
        )
        addTestField(
            content,
            R.string.test_email_label,
            R.string.test_email_hint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            EditorInfo.IME_ACTION_NEXT
        )
        addTestField(
            content,
            R.string.test_url_label,
            R.string.test_url_hint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            EditorInfo.IME_ACTION_NEXT
        )
        addTestField(
            content,
            R.string.test_number_label,
            R.string.test_number_hint,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                InputType.TYPE_NUMBER_FLAG_DECIMAL,
            EditorInfo.IME_ACTION_NEXT
        )
        addTestField(
            content,
            R.string.test_password_label,
            R.string.test_password_hint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            EditorInfo.IME_ACTION_DONE
        )
        addTestField(
            content,
            R.string.test_multiline_label,
            R.string.test_multiline_hint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
            EditorInfo.IME_ACTION_NONE,
            multiline = true
        )

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        })

        content.requestFocus()
    }

    private fun addTestField(
        parent: LinearLayout,
        labelResource: Int,
        hintResource: Int,
        inputType: Int,
        imeAction: Int,
        multiline: Boolean = false
    ) {
        val label = TextView(this).apply {
            text = getString(labelResource)
            textSize = 14f
            setTextColor(Color.rgb(58, 70, 61))
        }
        addContent(parent, label, 4)

        val field = EditText(this).apply {
            hint = getString(hintResource)
            this.inputType = inputType
            imeOptions = imeAction
            isSaveEnabled = false
            if (multiline) {
                minLines = 2
                maxLines = 4
                gravity = Gravity.TOP or Gravity.START
            } else {
                setSingleLine(true)
            }
        }
        addContent(parent, field, 12)
    }

    private fun addContent(
        parent: LinearLayout,
        child: View,
        bottomSpacing: Int = 12
    ) {
        parent.addView(
            child,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(bottomSpacing)
            }
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
