package com.snowgem.kbai

internal class NativeBridge {
    external fun nativeStatus(): String
    external fun nativeCreateSession(): Long
    external fun nativeProcessKey(
        handle: Long,
        keyCodePoint: Int,
        vietnamese: Boolean
    ): String
    external fun nativeWasForcedCommit(handle: Long): Boolean
    external fun nativeUndoKey(handle: Long): String
    external fun nativeCommit(handle: Long)
    external fun nativeDestroySession(handle: Long)

    companion object {
        init {
            System.loadLibrary("kb_ai_native")
        }
    }
}
