package com.snowgem.kbai

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyPressMotionPolicyTest {
    @Test
    fun pressedKeyUsesSubtleScaleFeedback() {
        assertEquals(0.975f, KeyPressMotionPolicy.scale(pressed = true, animationsEnabled = true))
    }

    @Test
    fun releasedKeyReturnsToItsOriginalScale() {
        assertEquals(1f, KeyPressMotionPolicy.scale(pressed = false, animationsEnabled = true))
    }

    @Test
    fun disabledSystemAnimationsKeepKeysAtTheirOriginalScale() {
        assertEquals(1f, KeyPressMotionPolicy.scale(pressed = true, animationsEnabled = false))
        assertEquals(0L, KeyPressMotionPolicy.durationMillis(pressed = true, animationsEnabled = false))
    }

    @Test
    fun pressAndReleaseUseShortDurations() {
        assertEquals(70L, KeyPressMotionPolicy.durationMillis(pressed = true, animationsEnabled = true))
        assertEquals(95L, KeyPressMotionPolicy.durationMillis(pressed = false, animationsEnabled = true))
    }
}
