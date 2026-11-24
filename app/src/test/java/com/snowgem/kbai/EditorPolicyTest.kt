package com.snowgem.kbai

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorPolicyTest {
    @Test
    fun ordinaryTextIsNotSensitive() {
        assertFalse(EditorPolicy.isSensitive(InputType.TYPE_CLASS_TEXT))
    }

    @Test
    fun passwordTextVariationsAreSensitive() {
        assertTrue(
            EditorPolicy.isSensitive(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            )
        )
        assertTrue(
            EditorPolicy.isSensitive(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            )
        )
        assertTrue(
            EditorPolicy.isSensitive(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            )
        )
    }

    @Test
    fun numericPasswordIsSensitive() {
        assertTrue(
            EditorPolicy.isSensitive(
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            )
        )
    }

    @Test
    fun ordinaryNumberIsNotSensitive() {
        assertFalse(EditorPolicy.isSensitive(InputType.TYPE_CLASS_NUMBER))
    }
}
