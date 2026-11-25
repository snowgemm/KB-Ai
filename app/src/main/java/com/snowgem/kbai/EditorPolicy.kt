package com.snowgem.kbai

import android.text.InputType
import android.view.inputmethod.EditorInfo

internal object EditorPolicy {
    fun isSensitive(inputType: Int): Boolean {
        val fieldClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        return when (fieldClass) {
            InputType.TYPE_CLASS_TEXT -> variation in setOf(
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            )

            InputType.TYPE_CLASS_NUMBER ->
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

            else -> false
        }
    }

    fun disablesPersonalization(inputType: Int, imeOptions: Int): Boolean =
        isSensitive(inputType) ||
            imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0
}
