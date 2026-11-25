# Architecture

**Status:** Phase 1 Kotlin QWERTY prototype; Rust core remains a minimal Phase 0 integration seam.  
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
- A functional QWERTY InputMethodService.
- Alphabetic, symbol, numeric, and phone keyboard layouts.
- Direct text insertion through Android InputConnection.
- Backspace through Android delete key events.
- Editor action handling based on EditorInfo.
- Test fields for text, email, URL, number, password, and multiline input.
- A Kotlin NativeBridge wrapper and a Rust workspace with kb-core and kb-ai-native.
- One JNI method, nativeStatus(), retained as a build/integration diagnostic.
- Basic password and no-personalized-learning policy helpers.

The current keyboard commits ordinary characters directly. It does not yet implement Telex, VNI, composing text, candidates, dictionaries, or prediction.

## 3. System overview

~~~mermaid
flowchart TB
    USER["User"]
    subgraph ANDROID["Android / Kotlin"]
        APP["Application and IME setup"]
        SERVICE["KeyboardInputMethodService"]
        VIEW["QWERTY / symbols / numeric UI"]
        POLICY["Editor policy"]
        ADAPTER["InputConnection and key events"]
    end
    subgraph NATIVE["Rust native library"]
        JNI["JNI: nativeStatus() diagnostic"]
        CORE["kb-core"]
    end
    USER --> APP
    USER --> SERVICE
    SERVICE --> POLICY
    SERVICE --> VIEW
    VIEW --> ADAPTER
    ADAPTER --> EDITOR["Focused Android editor"]
    APP --> JNI
    JNI --> CORE
~~~

The native diagnostic does not participate in the typing path. Android owns all editor mutations.

## 4. Ownership boundaries

| State or responsibility | Owner |
|---|---|
| Committed text | Focused Android editor |
| Current selection and editor action | Android editor / InputConnection |
| InputMethodService lifecycle | Kotlin |
| Keyboard layout and touch handling | Kotlin UI |
| Future composing buffer and input-rule state | Rust session |
| Vietnamese transformation rules | Rust |
| Theme and user preferences | Kotlin / DataStore |
| Tool-panel visibility and navigation | Kotlin |
| Clipboard data | Android Clipboard APIs; no history in MVP |

No two layers should independently own and mutate the same data without an explicit synchronization contract.

## 5. Android layer

### Application

Owns onboarding, system settings navigation, language configuration, appearance, privacy controls, and theme management.

### IME service

Uses Android InputMethodService. It creates and tears down input sessions, selects the appropriate keyboard layout from EditorInfo, and coordinates the keyboard view with the focused editor.

### Keyboard UI

The current prototype uses native Android Buttons arranged in rows. Keys expose content descriptions and have stable touch targets. This is intentionally simpler than a custom canvas so assistive technologies can discover individual keys.

Jetpack Compose may be used for the settings application and theme editor. It is not required for the latency-sensitive IME surface.

### Editor adapter

Keep InputConnection calls in Kotlin. The current prototype uses:

- commitText for ordinary characters and spaces.
- sendDownUpKeyEvents for Backspace and the default Enter behavior.
- performEditorAction for Go, Search, Send, Next, Previous, and Done.
- commitText with a newline for multiline Enter.

The next editing milestone must add:

- setComposingText and finishComposingText.
- selection updates and reconciliation.
- bounded text queries only for specific editing operations.
- safely paired beginBatchEdit and endBatchEdit calls where batching is needed.

Do not call InputConnection from a Rust worker thread.

## 6. Rust workspace

The current workspace intentionally contains only two crates:

| Crate | Responsibility |
|---|---|
| kb-core | Minimal Phase 0 core status function; future input-session logic |
| kb-ai-native | Android cdylib and JNI exports |

The Vietnamese engine, dictionary, prediction, and Unicode modules are not implemented yet. Add separate crates only when they have independent responsibilities and real tests.

## 7. JNI contract

The only JNI operation currently implemented is NativeBridge.nativeStatus(): String.

It calls the Rust core and returns a fixed diagnostic string. It is not part of keyboard text entry.

Do not pass Android framework objects, Views, Context, InputConnection, bitmaps, or whole editor contents to Rust.

Use EnvUnowned.with_env and a controlled JNI error policy so Rust panics do not unwind across the JVM boundary. Validate any future JNI input lengths and enum values.

## 8. Session lifecycle and synchronization — future requirement

- Create or reset the Rust session when an editor input session starts.
- Read the field's input type and IME options in Kotlin.
- Apply an EditorPolicy before enabling candidates, learning, or layout-specific behavior.
- Keep composing text in Rust only while it belongs to the active editor.
- Observe selection changes through IME lifecycle callbacks.
- If the cursor moves outside the composing range, invalidate or safely reconcile the current composition.
- Finish composition and release session resources when input finishes.
- Never carry a composing buffer from one editor to another.

Asynchronous candidate work must carry a session and revision identifier. Kotlin must discard results whose revision no longer matches the active session.

## 9. Threading and performance

### Synchronous path

The current key-event path performs bounded layout/state updates and an InputConnection operation. It does not perform disk or network I/O, load dictionaries, or wait for background suggestions.

### Background path

Future dictionary loading and advanced prediction may run off the UI thread. Every result must be tagged with the active session and revision. Stale results must be discarded.

Record actual p50/p95 timing and memory measurements on physical devices before setting numeric performance claims.

## 10. Unicode and editor offsets

Rust strings use UTF-8 byte offsets. Android text APIs use UTF-16 code-unit offsets. Unicode code points and grapheme clusters are different units.

Requirements for the future editing engine:

- Never use a Rust byte offset directly as an Android selection or deletion length.
- Use grapheme-aware boundaries for text owned by the Rust composing buffer.
- Convert offsets explicitly at the Kotlin/JNI boundary.
- Test combining marks, surrogate pairs, regional indicator flags, skin-tone modifiers, and zero-width-joiner emoji.
- Do not normalize the entire external editor contents.
- For backspace outside the composing buffer, retain Android key-event behavior unless a tested grapheme-aware adapter is introduced.

The unicode-segmentation crate is a candidate for grapheme-boundary work. Its suitability, version, and license must be checked before integration.

## 11. Pinned toolchain

| Component | Version / setting |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Kotlin | AGP 9 built-in Kotlin support; do not apply the legacy Kotlin Android plugin |
| Compile SDK | 36 |
| Build Tools | 36.0.0 |
| Target SDK | 36 |
| Minimum SDK | 23 |
| NDK | 30.0.16248370 |
| Rust | 1.100.0 |
| cargo-ndk | 4.1.2 |
| Android ABI | arm64-v8a and x86_64 |

The values are pinned in build files and CI. Re-evaluate them when changing the supported device baseline or Android release target.

## 12. Build integration

Gradle invokes cargo-ndk from the Rust workspace to build the Rust cdylib into a generated jniLibs directory. The Android preBuild task depends on the native build task, and the generated directory is registered as a JNI library source.

The Gradle wrapper distribution URL includes a SHA-256 checksum. The wrapper JAR is the official binary from the Gradle v9.9.2 source tag and its SHA-256 is recorded next to it.

## 13. Architectural constraints

- No cloud dependency for core typing.
- No analytics or typed-content telemetry.
- No database in the first integration milestone.
- No plugin system before there are real extensions to support.
- No speculative cross-platform abstraction while Android is the only target.
- Never call Android InputConnection from Rust.
