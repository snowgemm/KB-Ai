# Architecture

**Status:** Telex/VNI Rust engine integrated in source; CI and physical-device verification are pending.  
**Scope:** Android application and native Rust input engine.

## 1. Architectural goals

1. Keep Android framework integration in Kotlin.
2. Keep Vietnamese transformation and input-session logic testable in Rust.
3. Keep the native boundary small.
4. Treat the focused Android editor as the authority for committed text and selection.
5. Keep core typing offline and free of typed-content persistence.
6. Avoid splitting the project into too many modules before ownership boundaries are proven.

## 2. Current implementation boundary

The repository currently contains:

- One Android application module.
- QWERTY, symbol, numeric, and phone layouts in a Kotlin InputMethodService.
- VI/EN toggle for eligible text fields.
- A Rust kb-core wrapper around Bamboo Core 0.6.1.
- A Rust JNI library with per-editor session handles.
- Telex and VNI composition through InputSession.
- Android composing-text updates through InputConnection.setComposingText.
- Per-key undo inside the active composition.
- Explicit finish/commit at word boundaries and editor lifecycle boundaries.
- Test fields for text, email, URL, number, password, and multiline input.
- Password, email, URL, and numeric fields bypass the Vietnamese composition engine.

## 3. System overview

~~~mermaid
flowchart TB
    USER["User"]
    subgraph ANDROID["Android / Kotlin"]
        APP["Application and IME setup"]
        SERVICE["KeyboardInputMethodService"]
        VIEW["QWERTY / symbols / numeric UI"]
        POLICY["Editor policy"]
        ADAPTER["InputConnection composing API"]
    end
    subgraph NATIVE["Rust native library"]
        JNI["JNI session API"]
        SESSION["kb-core InputSession"]
        BAMBOO["Bamboo Core 0.6.1"]
    end
    USER --> APP
    USER --> SERVICE
    SERVICE --> POLICY
    SERVICE --> VIEW
    VIEW --> JNI
    JNI --> SESSION
    SESSION --> BAMBOO
    VIEW --> ADAPTER
    ADAPTER --> EDITOR["Focused Android editor"]
~~~

Kotlin owns editor mutations. Rust owns the active Vietnamese input composition. The Rust engine does not receive Android framework objects or the editor's full text.

## 4. Ownership boundaries

| State or responsibility | Owner |
|---|---|
| Committed text | Focused Android editor |
| Current selection and editor action | Android editor / InputConnection |
| InputMethodService lifecycle | Kotlin |
| Keyboard layout and touch handling | Kotlin UI |
| Active Vietnamese composition | Rust InputSession |
| Vietnamese transformation rules | Bamboo Core |
| Theme and user preferences | Kotlin / DataStore |
| Tool-panel visibility and navigation | Kotlin |
| Clipboard data | Android Clipboard APIs; no history in MVP |

Bamboo Core committed-text tracking is explicitly disabled. The engine retains only the active in-memory composition needed for transformations; committed words and raw keystrokes are not retained in its full-text buffers.

No two layers should independently own and mutate the same data without an explicit synchronization contract.

## 5. Android layer

### Application

Owns onboarding, system settings navigation, language configuration, appearance, privacy controls, and theme management.

### IME service

Uses Android InputMethodService. It creates and destroys a Rust session per eligible editor input session, selects the appropriate layout from EditorInfo, observes selection changes, and coordinates the keyboard view with the focused editor.

### Keyboard UI

The current prototype uses native Android Buttons arranged in rows. Keys expose content descriptions and have stable touch targets. This keeps individual keys discoverable by assistive technologies.

### Editor adapter

Keep InputConnection calls in Kotlin.

- setComposingText replaces the current composing span with the engine's latest preedit.
- finishComposingText finalizes the active preedit.
- commitText inserts literal punctuation, spaces, or characters that bypass the Vietnamese engine.
- performEditorAction handles Go, Search, Send, Next, Previous, and Done.
- sendDownUpKeyEvents is used for Backspace outside an active composition.

The adapter observes selection updates. If the user moves the cursor or selection away from the composition end, it finalizes the composition rather than continuing to edit at a stale location.

Do not call InputConnection from a Rust worker thread.

## 6. Rust workspace

| Crate | Responsibility |
|---|---|
| kb-core | Per-editor InputSession wrapper around Bamboo Core |
| kb-ai-native | Android cdylib, JNI methods, and session registry |
| bamboo-core 0.6.1 | Upstream Telex/VNI transformation engine |

The Rust wrapper creates a combined Telex + VNI engine and disables extended phonetic onsets by default to reduce accidental transformations of ordinary English words beginning with f, j, or w.

Bamboo's full-text tracking is disabled. The wrapper returns a borrowed preedit slice; the JNI layer creates the Java string while the session lock is held, avoiding an additional owned Rust String allocation on each printable key.

## 7. JNI contract

The JNI methods are:

- nativeCreateSession(): Long
- nativeProcessKey(handle, codePoint, vietnamese): String
- nativeWasForcedCommit(handle): Boolean
- nativeUndoKey(handle): String
- nativeCommit(handle): Unit
- nativeDestroySession(handle): Unit
- nativeStatus(): String (diagnostic)

Session handles are generated in native code and stored in a process-local registry. They are removed when the editor input session finishes or the service is destroyed.

Do not pass Android framework objects, Views, Context, InputConnection, bitmaps, or whole editor contents to Rust.

## 8. Session lifecycle and synchronization

- Create a Rust session for eligible text fields.
- Do not create a Vietnamese composition session for password, email, URL, numeric, or phone fields.
- Keep the active composition in the Rust session only while it belongs to the current editor.
- Finish composing text when punctuation or space is committed, language mode changes, or the editor session ends.
- Observe selection changes; finalize composition if the caret moves outside its expected end position.
- Destroy the Rust session when input finishes or the service is destroyed.
- Never carry a composition buffer from one editor to another.

The Rust composition buffer is bounded. Bamboo can signal a forced word boundary; Kotlin must finish the old composing span before applying the new preedit.

## 9. Threading and performance

### Synchronous path

The key path performs bounded text transformation, a session lookup, a JNI call, and an InputConnection operation. It performs no disk or network I/O and does not load dictionaries.

The Rust engine reuses its internal preedit buffer. JNI still creates a Java String for Android's composing API, but the wrapper avoids an additional owned Rust String per key.

### Background path

Future dictionary loading and advanced prediction may run off the UI thread. Every result must be tagged with the active session and revision. Stale results must be discarded.

Record actual p50/p95 timing and memory measurements on physical devices before setting numeric performance claims.

## 10. Unicode and editor offsets

Rust strings use UTF-8 byte offsets. Android text APIs use UTF-16 code-unit offsets. Unicode code points and grapheme clusters are different units.

The current letter-key path sends one Unicode scalar value per call and uses the complete preedit string in setComposingText. Advanced cursor movement and grapheme-aware deletion outside the active preedit remain future work.

Requirements for the future editing engine:

- Never use a Rust byte offset directly as an Android selection or deletion length.
- Use grapheme-aware boundaries for text owned by the Rust composing buffer.
- Convert offsets explicitly at the Kotlin/JNI boundary.
- Test combining marks, surrogate pairs, regional indicator flags, skin-tone modifiers, and zero-width-joiner emoji.
- Do not normalize the entire external editor contents.

## 11. Pinned toolchain

| Component | Version / setting |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Kotlin | AGP 9 built-in Kotlin support |
| Compile SDK | 36 |
| Build Tools | 36.0.0 |
| Target SDK | 36 |
| Minimum SDK | 23 |
| NDK | 30.0.16248370 |
| Rust | 1.100.0 |
| cargo-ndk | 4.1.2 |
| Android ABI | arm64-v8a and x86_64 |
| Bamboo Core | 0.6.1 |

## 12. Build integration

Gradle invokes cargo-ndk from the Rust workspace to build the JNI cdylib into a generated jniLibs directory. The Android preBuild task depends on the native build task, and the generated directory is registered as a JNI library source.

## 13. Architectural constraints

- No cloud dependency for core typing.
- No analytics or typed-content telemetry.
- No persistent typing history.
- No automatic clipboard access.
- No plugin system before there are real extensions to support.
- No speculative cross-platform abstraction while Android is the only target.
- Never call Android InputConnection from Rust.
