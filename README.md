# KB-Ai

**A Vietnamese-first Android keyboard with a Rust input core.**

KB-Ai is an independent Android keyboard project focused on accurate Vietnamese input, a familiar multi-tool experience, meaningful customization, and privacy-preserving defaults.

> Current status: Phase 1 QWERTY prototype. The Android IME can enter ordinary text, switch between alphabetic, symbol, numeric, and phone layouts, and handle common editor actions. Vietnamese Telex/VNI, prediction, and production-ready editing are not implemented yet.

## Product principles

- Vietnamese-first: Telex and VNI are core product requirements.
- Android-native: use Android's input method framework for system integration.
- Rust core: keep future input-state and Vietnamese text-processing logic independent of Android UI.
- Private by design: do not collect, transmit, or retain typed content.
- Offline-first: core typing must not require network access.
- Fast and predictable: the synchronous key path must not wait for disk, network, or background suggestions.
- Customizable without clutter: expose useful tools without overcrowding the typing surface.
- Accessible: keyboard controls must remain usable with assistive technologies.

## Implemented Phase 1 scope

- Android app with links to Android's IME settings and keyboard picker.
- Functional QWERTY letter layout.
- Shift, Backspace, Space, punctuation, and symbol layout.
- Numeric layout for number/date-time editors and phone layout for phone editors.
- Editor action labels and actions for Go, Search, Send, Next, Previous, and Done.
- Email and URL punctuation adjustments.
- Test fields for text, email, URL, number, password, and multiline input.
- Kotlin-to-Rust JNI status diagnostic retained in the companion app.
- Basic editor policy for password fields and the no-personalized-learning editor flag.

The current keyboard commits typed characters directly through Android InputConnection. It does not yet implement a composing buffer, Telex, VNI, autocorrection, suggestions, swipe typing, clipboard history, emoji search, or AI processing. It is an early functional prototype, not a stable daily-use release.

## Planned product capabilities

| Area | Planned scope |
|---|---|
| Core typing | QWERTY, Vietnamese Telex, Vietnamese VNI, English |
| Editing | Composing text, selection-aware editing, cursor controls |
| Language support | Vietnamese and English dictionaries |
| Intelligence | Local suggestions and autocorrection, with explicit privacy controls |
| Personalization | Light/dark/system, keyboard height, themes, local image backgrounds |
| Tools | Emoji, opt-in clipboard, text editing, shortcuts |
| Advanced tools | GIF, stickers, voice input, handwriting, translation |
| Privacy | No typing-history collection, sensitive-field protections, local controls |

A listed capability is a plan, not an implemented feature. See the roadmap.

## Architecture

- Kotlin / Android: IME lifecycle, Android framework integration, keyboard UI, settings, and platform adapters.
- Rust: future input-session state and Vietnamese/text-processing logic.
- JNI: a deliberately small Android-native boundary.
- InputConnection: the Kotlin-only route used to edit the focused application.

See docs/ARCHITECTURE.md and docs/adr/.

## Build requirements

| Tool | Version |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Android built-in Kotlin | Provided by AGP 9 |
| Android SDK Platform | 36 |
| Android Build Tools | 36.0.0 |
| Android NDK | 30.0.16248370 |
| Rust | 1.100.0 |
| cargo-ndk | 4.1.2 |

Install the Android SDK Platform 36, Build Tools 36.0.0, and NDK 30.0.16248370 using Android Studio SDK Manager or sdkmanager. Set ANDROID_SDK_ROOT (or ANDROID_HOME) and, when needed, ANDROID_NDK_HOME.

The Rust version and Android targets are declared in rust-toolchain.toml. Install the pinned cargo-ndk version:

    cargo install cargo-ndk --version 4.1.2 --locked

The repository includes the official Gradle 9.9.2 wrapper scripts and wrapper JAR. The wrapper distribution checksum is configured in gradle/wrapper/gradle-wrapper.properties.

## Build and test

On Linux or macOS:

    cargo fmt --all --manifest-path native/Cargo.toml -- --check
    cargo test --workspace --manifest-path native/Cargo.toml
    cargo clippy --workspace --manifest-path native/Cargo.toml --all-targets -- -D warnings
    ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug

On Windows, use gradlew.bat for the Gradle command. Rust and cargo-ndk must also be available in PATH.

The Gradle build invokes cargo-ndk from the native/ workspace to build arm64-v8a and x86_64 native libraries and packages them into the APK.

The debug APK, when the build succeeds, is written to app/build/outputs/apk/debug/app-debug.apk.

## First-run test

1. Install the debug APK on an Android device or emulator.
2. Open KB-Ai and select Bật KB-Ai trong Cài đặt.
3. Enable KB-Ai in Android's input-method settings.
4. Return to the app and select Chọn bàn phím KB-Ai.
5. Test typing in each sample field. Use the password sample only with dummy text.
6. Confirm number, phone, email, URL, and multiline fields display the appropriate layout or editor action.

The current build is an early prototype. Do not use it for passwords or other sensitive information until the release privacy and device test gates are complete.

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

The product studies common interaction patterns found in Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. KB-Ai is an independent project. It does not use their branding, proprietary assets, private dictionaries, or source code.

## License

The repository is distributed under the MIT License. See LICENSE.
