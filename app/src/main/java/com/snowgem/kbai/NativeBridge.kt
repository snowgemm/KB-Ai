package com.snowgem.kbai

internal class NativeBridge {
    external fun nativeStatus(): String

    companion object {
        init {
            System.loadLibrary("kb_ai_native")
        }
    }
}
