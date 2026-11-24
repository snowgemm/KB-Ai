# Architecture

**Status:** Android/Rust Phase 0 prototype in repository; production input engine not implemented.  
**Scope:** Android application and native Rust input engine.

## 1. Architectural goals

1. Keep Android framework integration in Kotlin.
2. Keep Vietnamese transformation and input-session logic testable in Rust.
3. Keep the native boundary small.
4. Treat the focused Android editor as the authority for committed text and selection.
5. Keep core typing offline and free of typed-content persistence.
6. Avoid splitting the project into too many modules before ownership boundaries are proven.

## 2. Current implementation boundary

Phase 0 currently contains:

- One Android application module.
- One test-only InputMethodService.
- A Kotlin NativeBridge wrapper.
- A Rust workspace with `kb-core` and `kb-ai-native`.
- One JNI method, `nativeStatus()`, that returns a fixed status string.
- A button that can explicitly insert that fixed status string into a focused editor.
- A basic password-field policy that disables the test insertion button.

The current prototype is not a QWERTY keyboard. It does not implement Telex, VNI, composing text, cursor editing, candidates, dictionaries, or production keyboard layouts.

## 3. System overview

~~~mermaid
flowchart TB
    USER["User"]
    subgraph ANDROID["Android / Kotlin"]
        APP["Application and onboarding"]
        SERVICE["Test InputMethodService"]
        VIEW["Test-only IME View"]
        ADAPTER["InputConnection call"]
    end
    subgraph NATIVE["Rust native library"]
        JNI["JNI: nativeStatus()"]
        CORE["kb-core"]
    end
    USER --> APP
    USER --> SERVICE
    SERVICE --> VIEW
    VIEW --> JNI
    JNI --> CORE
    VIEW --> ADAPTER
    ADAPTER -->|"commitText after explicit tap"| EDITOR["Focused Android editor"]
~~~

This is a Phase 0 smoke path. The future architecture will separate the editor adapter, keyboard UI, Rust input session, and Vietnamese engine more fully.

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

Uses Android InputMethodService. It creates and tears down input sessions, selects the appropriate keyboard layout, observes EditorInfo, and coordinates the keyboard view with the focused editor.

### Keyboard UI

Use custom Android Views for the active keyboard surface. The first version should favor accessible View controls over a single canvas that draws every key without an equivalent accessibility tree.

Jetpack Compose may be used for the settings application and theme editor. It is not required for the latency-sensitive IME surface.

### Editor adapter

Keep InputConnection calls in Kotlin. The adapter is responsible for:

- setComposingText and commitText.
- finishComposingText.
- selection updates.
- editor action keys.
- bounded text queries required for a specific editing operation.
- safely paired beginBatchEdit and endBatchEdit calls.

Do not call InputConnection from a Rust worker thread. Balance a successful beginBatchEdit with endBatchEdit, including exceptional paths.

## 6. Rust workspace

The current workspace intentionally contains only two crates:

| Crate | Responsibility |
|---|---|
| kb-core | Minimal Phase 0 core status function; future input-session logic |
| kb-ai-native | Android cdylib and JNI exports |

The Vietnamese engine, dictionary, prediction, and Unicode modules are not implemented yet. Add separate crates only when they have independent responsibilities and real tests.

The native library is built as a shared library for Android and packaged by ABI.

## 7. JNI contract

The only JNI operation currently implemented is:

- `NativeBridge.nativeStatus(): String`

It calls the Rust core and returns a fixed diagnostic string. It is not a keyboard input API.

The intended future boundary will expose small typed operations for session creation, semantic key events, selection reconciliation, composition completion, and session destruction. Those APIs must not be documented as implemented until code and tests exist.

Do not pass Android framework objects, Views, Context, InputConnection, bitmaps, or whole editor contents to Rust.

Use `EnvUnowned::with_env` and a controlled JNI error policy so Rust panics do not unwind across the JVM boundary. Validate any future JNI input lengths and enum values.

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

The future key-event path may perform only bounded input transformation and composing-state updates. It must not perform network or disk I/O, load large dictionaries, or wait for background work.

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
- For backspace outside the composing buffer, use a bounded editor snapshot only when required, compute a safe UTF-16 deletion length, and do not persist or log the snapshot.
- If an editor cannot provide a usable snapshot, use a conservative fallback and document its limitation.

The unicode-segmentation crate is a candidate for grapheme-boundary work. Its suitability, version, and license must be checked before integration.

## 11. Pinned Phase 0 toolchain

| Component | Version / setting |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Kotlin | AGP 9 built-in Kotlin support; do not apply the legacy Kotlin Android plugin |
| Compile SDK | 37 |
| Build Tools | 37.0.0 |
| Target SDK | 36 |
| Minimum SDK | 23 |
| NDK | 30.0.16248370 |
| Rust | 1.100.0 |
| cargo-ndk | 4.1.2 |
| Android ABI | arm64-v8a and x86_64 |

The values are pinned in build files and CI. Re-evaluate them when changing the supported device baseline or Android release target.

## 12. Build integration

Gradle invokes cargo-ndk to build the Rust cdylib into a generated jniLibs directory. The generated libraries are packaged into the Android APK.

The Gradle wrapper distribution URL includes a SHA-256 checksum. The wrapper JAR is the official binary from the Gradle v9.9.2 source tag and its SHA-256 is recorded next to it.

## 13. Architectural constraints

- No cloud dependency for core typing.
- No analytics or typed-content telemetry.
- No database in the first integration milestone.
- No plugin system before there are real extensions to support.
- No speculative cross-platform abstraction while Android is the only target.
- Never call Android InputConnection from Rust.
