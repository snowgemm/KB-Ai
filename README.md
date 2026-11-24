# KB-Ai

**A Vietnamese-first Android keyboard with a Rust input core.**

KB-Ai is an independent Android keyboard project focused on accurate Vietnamese input, a familiar multi-tool experience, meaningful customization, and privacy-preserving defaults.

> **Current status: Phase 0 integration prototype.** The repository contains a minimal Android application, a test-only IME, and a Rust/JNI smoke path. It does **not** yet provide a QWERTY keyboard, Telex, VNI, predictions, or a production-ready input method.

## Product principles

- **Vietnamese-first:** Telex and VNI are core product requirements.
- **Android-native:** use Android's input method framework for system integration.
- **Rust core:** keep input-state and text-processing logic independent of Android UI.
- **Private by design:** do not collect, transmit, or retain typed content.
- **Offline-first:** core typing must not require network access.
- **Fast and predictable:** the synchronous key path must not wait for disk, network, or background suggestions.
- **Customizable without clutter:** expose useful tools without overcrowding the typing surface.
- **Accessible:** keyboard controls must remain usable with assistive technologies.

## Current Phase 0 prototype

The prototype is deliberately limited to validating the Android/Rust boundary:

- An Android app with links to Android's IME settings and keyboard picker.
- A test text field.
- A test-only IME view with a button.
- A Kotlin-to-Rust JNI call returning a fixed status string.
- An explicit button action that inserts that fixed status string into the focused editor.
- A basic policy that disables the test action for recognized password fields.
- A Rust unit test and Android unit tests for the password-field policy.

The prototype is not intended for daily typing. It has no QWERTY layout, Telex, VNI, word suggestions, clipboard, emoji, or theme editor yet.

## Planned product capabilities

| Area | Planned scope |
|---|---|
| Core typing | QWERTY, Vietnamese Telex, Vietnamese VNI, English |
| Layouts | Number row, symbols, punctuation, editor-specific layouts |
| Editing | Composing text, selection-aware editing, cursor controls |
| Personalization | Light/dark/system, keyboard height, themes, local image backgrounds |
| Tools | Emoji, clipboard, text editing, shortcuts |
| Advanced tools | GIF, stickers, voice input, handwriting, translation |
| Privacy | No typing-history collection, password-field protections, local controls |

A listed capability is a plan, not an implemented feature. See the [roadmap](docs/ROADMAP.md).

## Architecture

- **Kotlin / Android:** IME lifecycle, Android framework integration, keyboard UI, settings, and platform adapters.
- **Rust:** input session state and Vietnamese/text-processing logic.
- **JNI:** a deliberately small Android-native boundary.
- **InputConnection:** the Kotlin-only route used to edit the focused application.

See [Architecture](docs/ARCHITECTURE.md) and [Architecture Decision Records](docs/adr/).

## Build requirements

The versions below are pinned for reproducible builds:

| Tool | Version |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Android built-in Kotlin | Provided by AGP 9 |
| Android SDK Platform | 37 |
| Android Build Tools | 37.0.0 |
| Android NDK | 30.0.16248370 |
| Rust | 1.100.0 |
| cargo-ndk | 4.1.2 |

Install the Android SDK Platform 37, Build Tools 37.0.0, and NDK 30.0.16248370 using Android Studio SDK Manager or `sdkmanager`. Set `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) and, when needed, `ANDROID_NDK_HOME`.

The Rust version and Android targets are declared in `rust-toolchain.toml`. Install the pinned cargo-ndk version:

```sh
cargo install cargo-ndk --version 4.1.2 --locked
```

The repository includes the official Gradle 9.9.2 wrapper scripts and wrapper JAR. The wrapper distribution checksum is configured in `gradle/wrapper/gradle-wrapper.properties`.

## Build and test

On Linux or macOS:

```sh
cargo fmt --all --manifest-path native/Cargo.toml -- --check
cargo test --workspace --manifest-path native/Cargo.toml
cargo clippy --workspace --manifest-path native/Cargo.toml --all-targets -- -D warnings
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

On Windows, use `gradlew.bat` for the Gradle command. Rust and cargo-ndk must also be available in PATH.

The Gradle build invokes cargo-ndk to build `arm64-v8a` and `x86_64` native libraries and packages them into the APK.

The debug APK, when the build succeeds, is written to:

`app/build/outputs/apk/debug/app-debug.apk`

## First-run test

1. Install the debug APK on an Android device or emulator.
2. Open KB-Ai and select **Bật KB-Ai trong Cài đặt**.
3. Enable KB-Ai in Android's input-method settings.
4. Return to the app and select **Chọn bàn phím KB-Ai**.
5. Focus the test field.
6. Tap **Kiểm tra Rust JNI** in the app, or use the test-only IME button to insert the fixed status string.

The test-only IME is intentionally not a normal keyboard. Do not enter private information into test fields when reporting defects.

## Documentation

- [Product specification](docs/PRODUCT_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)
- [UI/UX specification](docs/UI_UX_SPEC.md)
- [Security and privacy](docs/SECURITY_PRIVACY.md)
- [Test plan](docs/TEST_PLAN.md)
- [Roadmap](docs/ROADMAP.md)
- [Technical references](docs/REFERENCES.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
- [Contributing](CONTRIBUTING.md)
- [Security reporting](SECURITY.md)

## Inspiration and independence

The product studies common interaction patterns found in Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. KB-Ai is an independent project. It does not use their branding, proprietary assets, private dictionaries, or source code.

## License

The repository is distributed under the MIT License. See [LICENSE](LICENSE).
