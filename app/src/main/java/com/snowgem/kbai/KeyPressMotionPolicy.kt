package com.snowgem.kbai

internal object KeyPressMotionPolicy {
    private const val PRESSED_SCALE = 0.975f
    private const val PRESS_DURATION_MILLIS = 70L
    private const val RELEASE_DURATION_MILLIS = 95L

    fun scale(pressed: Boolean, animationsEnabled: Boolean): Float =
        if (pressed && animationsEnabled) PRESSED_SCALE else 1f

    fun durationMillis(pressed: Boolean, animationsEnabled: Boolean): Long =
        if (!animationsEnabled) {
            0L
        } else if (pressed) {
            PRESS_DURATION_MILLIS
        } else {
            RELEASE_DURATION_MILLIS
        }
}
