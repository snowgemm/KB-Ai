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
            text = getString(R.string.phase0_title)
            textSize = 17f
            setTextColor(Color.rgb(40, 75, 52))
        }
        addContent(content, phaseTitle, 14)

        val description = TextView(this).apply {
            text = getString(R.string.phase0_description)
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
            textSize = 16f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.rgb(24, 34, 27))
        }
        addContent(content, testTitle, 8)

        val testField = EditText(this).apply {
            hint = getString(R.string.test_field_hint)
            minLines = 2
            maxLines = 4
            gravity = Gravity.TOP or Gravity.START
            isSaveEnabled = false
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        addContent(content, testField, 10)

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
