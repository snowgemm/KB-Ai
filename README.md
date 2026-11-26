# KB-Ai

**A Vietnamese-first Android keyboard with a Rust input core.**

KB-Ai is an independent Android keyboard focused on accurate Vietnamese input, a familiar multi-tool experience, meaningful customization, and privacy-preserving defaults.

> **Current status: Vietnamese input-engine integration prototype.** The Android IME has QWERTY, symbol, numeric, and phone layouts. The Rust core now integrates Bamboo Core 0.6.1 for Telex and VNI composition, with a VI/EN mode toggle. CI and physical-device verification are still required before this can be considered stable.

## Product principles

- Vietnamese-first: Telex and VNI are core input methods.
- Android-native: Android owns IME lifecycle and focused-editor mutations.
- Rust input core: Vietnamese composition is isolated from Android UI.
- Private by design: no typing-history collection, transmission, or persistence.
- Offline-first: ordinary typing and Vietnamese transformation do not need network access.
- Responsive: no network or disk work in the synchronous key path.
- Customizable without clutter.
- Accessible controls with individual labels.

## Implemented source scope

### Keyboard UI

- QWERTY letters, Shift, Backspace, Space, punctuation, and symbol layout.
- Numeric layout for number/date-time editors and phone layout for phone editors.
- Editor actions: Go, Search, Send, Next, Previous, and Done.
- Email and URL punctuation adjustments.
- VI/EN language toggle for supported text fields.
- Test fields for text, email, URL, number, password, and multiline input.

### Vietnamese input engine

- Bamboo Core 0.6.1, MIT licensed.
- Combined Telex + VNI input method.
- Live preedit through Android InputConnection composing text.
- Tone/shape transformations and physical-key undo through the Rust engine.
- English bypass mode.
- Forced composition-boundary signal handling.
- Separate per-editor Rust session lifecycle.
- Committed text tracking disabled in Bamboo Core; the engine does not retain completed words or raw keystrokes.

### Current limitations

- No dictionary, candidate suggestions, autocorrection, swipe typing, or personal learning.
- No clipboard history or emoji search.
- No AI text tools.
- No advanced cursor editing or grapheme-aware deletion outside the active preedit.
- The current editor policy excludes password, email, and URL fields from Vietnamese composition. Numeric and phone fields use their own layouts.
- Physical-device and third-party editor compatibility testing remain open.

This is an integration prototype, not a stable daily-use release. Do not use it for real passwords or sensitive content.

## Architecture

- **Kotlin / Android:** InputMethodService, keyboard UI, EditorInfo policy, lifecycle, and InputConnection.
- **Rust / kb-core:** per-editor InputSession wrapping Bamboo Core.
- **Rust / JNI:** session creation, key processing, undo, commit, and destruction.
- **Android editor:** source of truth for committed text and selection.

The Rust engine holds only active composition state. Committed text tracking is disabled. Kotlin applies composing text to the focused editor and finishes composition at spaces, punctuation, editor changes, and other boundaries.

## Build requirements

| Tool | Version |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Android SDK Platform | 36 |
| Android Build Tools | 36.0.0 |
| Android NDK | 30.0.16248370 |
| Rust | 1.100.0 |
| cargo-ndk | 4.1.2 |
| Bamboo Core | 0.6.1 |

Install the Android SDK Platform 36, Build Tools 36.0.0, and NDK 30.0.16248370 using Android Studio SDK Manager or sdkmanager. Set ANDROID_SDK_ROOT (or ANDROID_HOME) and ANDROID_NDK_HOME.

Install the pinned cargo-ndk version:

    cargo install cargo-ndk --version 4.1.2 --locked

## Build and test

    cargo fmt --all --manifest-path native/Cargo.toml -- --check
    cargo test --workspace --manifest-path native/Cargo.toml
    cargo clippy --workspace --manifest-path native/Cargo.toml --all-targets -- -D warnings
    ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug

On Windows, use gradlew.bat for the Gradle command. The Gradle task builds the Rust workspace for ARM64 and x86_64 from the native/ directory and packages both libraries into the APK.

Debug APK output:

    app/build/outputs/apk/debug/app-debug.apk

## First-run test

1. Install the debug APK on an Android device or emulator.
2. Open KB-Ai and enable it in Android's input-method settings.
3. Select KB-Ai from the system keyboard picker.
4. In a normal text field, type `tieengs` and confirm the composing result is `tiếng`.
5. Type `tie6ng1` and confirm the result is `tiếng`.
6. Use the VI/EN key to switch to English and type `just`; switch back to VI to continue Vietnamese typing.
7. Try Backspace during an unfinished Vietnamese word.
8. Test email, URL, number, phone, password, and multiline fields separately. Use only dummy text in the password sample.

## Documentation

- Product specification: docs/PRODUCT_SPEC.md
- Architecture: docs/ARCHITECTURE.md
- UI/UX specification: docs/UI_UX_SPEC.md
- Security and privacy: docs/SECURITY_PRIVACY.md
- Test plan: docs/TEST_PLAN.md
- Roadmap: docs/ROADMAP.md
- Technical references: docs/REFERENCES.md
- Third-party notices: THIRD_PARTY_NOTICES.md
- Contributing: CONTRIBUTING.md
- Security reporting: SECURITY.md

## Inspiration and independence

The interaction direction studies common patterns in Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. KB-Ai is independent and does not use their branding, proprietary assets, private dictionaries, or source code.

## License

The repository is distributed under the MIT License. See LICENSE.
