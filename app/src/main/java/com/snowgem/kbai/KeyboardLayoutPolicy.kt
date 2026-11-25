package com.snowgem.kbai

import android.text.InputType

internal enum class KeyboardLayout {
    ALPHABETIC,
    NUMERIC,
    PHONE
}

internal object KeyboardLayoutPolicy {
    fun initialLayout(inputType: Int): KeyboardLayout {
        return when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_DATETIME -> KeyboardLayout.NUMERIC
            InputType.TYPE_CLASS_PHONE -> KeyboardLayout.PHONE
            else -> KeyboardLayout.ALPHABETIC
        }
    }
}
