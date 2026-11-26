package com.snowgem.kbai

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
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
        assertTrue(EditorPolicy.isSensitive(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertTrue(EditorPolicy.isSensitive(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD))
        assertTrue(EditorPolicy.isSensitive(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
    }

    @Test
    fun numericPasswordIsSensitive() {
        assertTrue(EditorPolicy.isSensitive(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
    }

    @Test
    fun ordinaryNumberIsNotSensitive() {
        assertFalse(EditorPolicy.isSensitive(InputType.TYPE_CLASS_NUMBER))
    }

    @Test
    fun editorCanDisablePersonalizationWithoutBeingPassword() {
        assertTrue(
            EditorPolicy.disablesPersonalization(
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            )
        )
        assertFalse(EditorPolicy.disablesPersonalization(InputType.TYPE_CLASS_TEXT, 0))
    }

    @Test
    fun passwordAlwaysDisablesPersonalization() {
        assertTrue(
            EditorPolicy.disablesPersonalization(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                0
            )
        )
    }

    @Test
    fun normalTextAndMultilineTextSupportVietnameseComposition() {
        assertTrue(EditorPolicy.supportsVietnameseComposition(InputType.TYPE_CLASS_TEXT))
        assertTrue(
            EditorPolicy.supportsVietnameseComposition(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            )
        )
    }

    @Test
    fun passwordEmailAndUrlFieldsBypassVietnameseComposition() {
        assertFalse(
            EditorPolicy.supportsVietnameseComposition(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            )
        )
        assertFalse(
            EditorPolicy.supportsVietnameseComposition(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            )
        )
        assertFalse(
            EditorPolicy.supportsVietnameseComposition(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            )
        )
        assertFalse(
            EditorPolicy.supportsVietnameseComposition(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            )
        )
    }

    @Test
    fun numericAndPhoneFieldsBypassVietnameseComposition() {
        assertFalse(EditorPolicy.supportsVietnameseComposition(InputType.TYPE_CLASS_NUMBER))
        assertFalse(EditorPolicy.supportsVietnameseComposition(InputType.TYPE_CLASS_PHONE))
    }

    @Test
    fun numericAndPhoneEditorsChooseMatchingLayouts() {
        assertEquals(
            KeyboardLayout.NUMERIC,
            KeyboardLayoutPolicy.initialLayout(InputType.TYPE_CLASS_NUMBER)
        )
        assertEquals(
            KeyboardLayout.PHONE,
            KeyboardLayoutPolicy.initialLayout(InputType.TYPE_CLASS_PHONE)
        )
    }

    @Test
    fun textEditorUsesAlphabeticLayout() {
        assertEquals(
            KeyboardLayout.ALPHABETIC,
            KeyboardLayoutPolicy.initialLayout(InputType.TYPE_CLASS_TEXT)
        )
    }
}
