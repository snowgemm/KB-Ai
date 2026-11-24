# Architecture

**Status:** Proposed architecture  
**Scope:** Android application and native Rust input engine

## 1. Architectural goals

1. Keep Android framework integration in Kotlin.
2. Keep Vietnamese transformation and input-session logic testable in Rust.
3. Keep the native boundary small and synchronous only for bounded key transformations.
4. Treat the focused Android editor as the authority for committed text and selection.
5. Keep the MVP offline and free of typed-content persistence.
6. Avoid splitting the project into many modules before ownership boundaries are proven.

## 2. System overview

~~~mermaid
flowchart TB
    USER["User"]
    subgraph ANDROID["Android / Kotlin"]
        APP["Application and onboarding"]
        SERVICE["InputMethodService"]
        VIEW["Keyboard custom Views"]
        ADAPTER["Editor / InputConnection adapter"]
        SETTINGS["Settings and theme storage"]
    end
    subgraph NATIVE["Rust native library"]
        JNI["Thin JNI boundary"]
        CORE["Input session core"]
        VI["Vietnamese Telex / VNI engine"]
        TEXT["Unicode and text helpers"]
    end
    USER --> APP
    USER --> SERVICE
    SERVICE --> VIEW
    SERVICE --> ADAPTER
    VIEW --> JNI
    JNI --> CORE
    CORE --> VI
    CORE --> TEXT
    ADAPTER -->|"InputConnection"| EDITOR["Focused Android editor"]
    APP --> SETTINGS
~~~

The diagram describes responsibilities, not an implemented module graph.

## 3. Ownership boundaries

| State or responsibility | Owner |
|---|---|
| Committed text | Focused Android editor |
| Current selection and editor action | Android editor / InputConnection |
| InputMethodService lifecycle | Kotlin |
| Keyboard layout and touch handling | Kotlin UI |
| Active composing buffer and input-rule state | Rust session |
| Vietnamese transformation rules | Rust |
| Theme and user preferences | Kotlin / DataStore |
| Tool-panel visibility and navigation | Kotlin |
| Clipboard data | Android Clipboard APIs; no history in MVP |

No two layers should independently own the same mutable state without an explicit synchronization contract.

## 4. Android layer

### Application

Owns onboarding, system settings navigation, language configuration, appearance, privacy controls, and theme management.

### IME service

Uses Android InputMethodService. It creates and tears down input sessions, selects the appropriate keyboard layout, observes EditorInfo, and coordinates the keyboard view with the focused editor.

### Keyboard UI

Use custom Android Views for the active keyboard surface. The first version should favor ordinary accessible View controls over a single canvas that draws every key without an equivalent accessibility tree.

Jetpack Compose is appropriate for the settings application and theme editor. It is not required for the latency-sensitive IME surface.

### Editor adapter

Keep InputConnection calls in a Kotlin adapter. The adapter handles:

- setComposingText and commitText.
- finishComposingText.
- selection updates.
- editor action keys.
- bounded text queries required for a specific editing operation.
- beginBatchEdit and endBatchEdit, paired safely.

Always balance a successful beginBatchEdit with endBatchEdit, including exceptional paths. Do not call InputConnection from a Rust worker thread.

## 5. Rust workspace

Start with a small workspace:

| Crate | Responsibility |
|---|---|
| kb-core | Input session, event handling, composing state, result types |
| vi-engine | Telex, VNI, Vietnamese transformation rules |
| native-bridge | JNI exports and conversion between JNI and core types |

Unicode helpers should stay in the crate that owns the relevant text behavior until they have a genuinely independent consumer. A separate dictionary or prediction crate should be introduced only when that feature exists.

The native library is built as a shared library for Android and packaged by ABI. Android NDK support is documented in the Rust platform-support reference.

## 6. Input event flow

~~~mermaid
sequenceDiagram
    participant User
    participant View as Keyboard View
    participant Kotlin as IME Coordinator
    participant Rust as Rust Session
    participant Editor as InputConnection

    User->>View: Tap key
    View->>Kotlin: Key event
    Kotlin->>Rust: Process one semantic event
    Rust-->>Kotlin: Composition result and revision
    Kotlin->>Editor: Apply composing or commit operation
    Editor-->>Kotlin: Selection update
    Kotlin->>Rust: Reconcile session if required
~~~

Rust returns a text result and an action description. Kotlin decides which Android API call to execute. Android classes and InputConnection objects never cross JNI.

## 7. Session lifecycle and synchronization

- Create or reset the Rust session when an editor input session starts.
- Read the field's input type and IME options in Kotlin.
- Apply an EditorPolicy before enabling candidates, learning, or layout-specific behavior.
- Keep composing text in Rust only while it belongs to the active editor.
- Observe selection changes through the IME lifecycle callbacks.
- If the cursor moves outside the composing range, invalidate or safely reconcile the current composition.
- Finish composition and release session resources when input finishes.
- Never carry a composing buffer from one editor to another.

A revision number must accompany asynchronous candidate work. Kotlin discards results whose revision no longer matches the active session.

## 8. JNI contract

The first bridge should expose only a small typed surface:

- createSession(configuration)
- processKey(session, event)
- updateSelection(session, selectionState)
- finishComposition(session)
- resetSession(session)
- destroySession(session)

The exact data types are finalized during the integration spike. Do not pass Android framework objects, Views, Context, InputConnection, bitmaps, or whole editor contents to Rust.

Do not create a Rust-to-Kotlin callback path in the MVP. Kotlin owns threading and UI dispatch. A native call must return a controlled result; Rust panics must never unwind across JNI.

## 9. Threading and performance

### Synchronous path

The key-event path may perform only bounded input transformation and composing-state updates. It must not perform network or disk I/O, load large dictionaries, or wait for background work.

### Background path

Future dictionary loading and advanced prediction may run off the UI thread. Every result must be tagged with the active session and revision. Stale results must be discarded.

Avoid sending a stream of tiny JNI calls for one gesture. A future swipe recognizer should send a completed gesture representation as one operation.

Record actual p50/p95 timing and memory measurements on physical devices before setting numeric performance claims.

## 10. Unicode and editor offsets

Rust strings use UTF-8 byte offsets. Android text APIs use UTF-16 code-unit offsets. Unicode code points and grapheme clusters are different units.

Rules:

- Never use a Rust byte offset directly as an Android selection or deletion length.
- Use grapheme-aware boundaries for text owned by the Rust composing buffer.
- Convert offsets explicitly at the Kotlin/JNI boundary.
- Test combining marks, surrogate pairs, regional indicator flags, skin-tone modifiers, and zero-width-joiner emoji.
- Do not normalize the entire external editor contents.
- For backspace outside the composing buffer, use a bounded editor snapshot only when required, compute a safe UTF-16 deletion length, and do not persist or log the snapshot.
- If an editor cannot provide a usable snapshot, use a conservative fallback and document its limitation.

The unicode-segmentation crate is a candidate for grapheme-boundary work. Its suitability, version, and license must be checked when dependencies are introduced.

## 11. Build and compatibility

- Use the Android Gradle Plugin and Android NDK versions selected at implementation time.
- Build Rust with the Android NDK and a reproducible toolchain.
- Start with arm64-v8a for physical devices and x86_64 for emulator testing.
- Evaluate armeabi-v7a only after the supported-device baseline and performance cost are known.
- Do not set minSdk or targetSdk in this planning document. Record them in Gradle and update the compatibility policy when they are selected.
- Keep Cargo.lock committed for the application workspace.
- Do not add a third-party Gradle plugin until its maintenance, license, and build behavior have been evaluated.

## 12. Architectural constraints

- No cloud dependency for core typing.
- No analytics or typed-content telemetry.
- No database in the first integration milestone.
- No plugin system before there are real extensions to support.
- No speculative cross-platform abstraction while Android is the only target.
