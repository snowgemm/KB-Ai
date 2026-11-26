# Technical References

This page records primary documentation and dependencies reviewed for the current implementation. Versions and recommendations must be rechecked when implementation changes.

**Last reviewed:** 2026-11-26

## Android input methods

- [Create an input method](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method) — Android IME implementation guide.
- [InputMethodService API](https://developer.android.com/reference/android/inputmethodservice/InputMethodService) — IME lifecycle and service API.
- [InputConnection API](https://developer.android.com/reference/android/view/inputmethod/InputConnection) — composing, commit, selection, deletion, and editor interaction.
- [InputConnection.setComposingText](https://developer.android.com/reference/android/view/inputmethod/InputConnection#setComposingText(java.lang.CharSequence,int)) — replaces the current composing span with the supplied preedit.
- [EditorInfo API](https://developer.android.com/reference/android/view/inputmethod/EditorInfo) — editor type and IME options.
- [Android 16 setup](https://developer.android.com/about/versions/16/setup-sdk) — stable API 36 SDK setup.
- [Android Gradle Plugin release information](https://developer.android.com/build/releases/about-agp) — AGP / Gradle compatibility and built-in Kotlin support.
- [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker) — user-selected image access for future local keyboard themes.

## Vietnamese input engine

- [Bamboo Core repository](https://github.com/bamboo-vi/bamboo-core) — Rust Vietnamese input method engine.
- [Bamboo Core 0.6.1 on crates.io](https://crates.io/crates/bamboo-core/0.6.1) — exact dependency version used by kb-core.
- [Bamboo Core documentation](https://docs.rs/bamboo-core/0.6.1/bamboo_core/) — Engine, InputMethod, Mode, Config, undo, and composing APIs.
- [Bamboo Core CHANGELOG](https://github.com/bamboo-vi/bamboo-core/blob/master/CHANGELOG.md) — release-specific behavior and migration notes.
- License: MIT. See upstream LICENSE and the KB-Ai third-party notices.

## Pinned toolchain

| Component | Selected version | Primary reference |
|---|---|---|
| JDK | 17 | [AGP release information](https://developer.android.com/build/releases/about-agp) |
| Gradle | 9.9.2 | [Gradle release notes](https://docs.gradle.org/current/release-notes.html) |
| Gradle wrapper checksum | Official 9.9.2 checksum | [Gradle release checksums](https://gradle.org/release-checksums/) |
| Android Gradle Plugin | 9.5.2 | [AGP release information](https://developer.android.com/build/releases/about-agp) |
| Kotlin support | Built into AGP 9 | [AGP 9 built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin) |
| Android SDK | API 36 | [Android 16 SDK setup](https://developer.android.com/about/versions/16/setup-sdk) |
| Android NDK | 30.0.16248370 | [Android NDK release notes](https://developer.android.com/ndk/downloads/revision_history) |
| Rust | 1.100.0 | [Rust 1.100.0 release](https://blog.rust-lang.org/2026/11/12/Rust-1.100.0/) |
| cargo-ndk | 4.1.2 | [cargo-ndk repository](https://github.com/bbqsrc/cargo-ndk/tree/v4.1.2) |
| JNI crate | 0.22.4 | [jni crate documentation](https://docs.rs/jni/0.22.4/jni/) |
| Bamboo Core | 0.6.1 | [crates.io](https://crates.io/crates/bamboo-core/0.6.1) |

AGP 9 supplies built-in Kotlin support. Do not also apply the legacy org.jetbrains.kotlin.android plugin to this project.

## Rust and Android native integration

- [Rust Android platform support](https://doc.rust-lang.org/rustc/platform-support/android.html) — supported Android Rust targets and NDK requirements.
- [jni crate](https://docs.rs/jni/0.22.4/jni/) — Rust bindings for the Java Native Interface.
- [cargo-ndk](https://github.com/bbqsrc/cargo-ndk/tree/v4.1.2) — builds Android native libraries and arranges output in the expected jniLibs directory.
- [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) — wrapper usage and verification.
- [Gradle build security](https://docs.gradle.org/current/userguide/securing_build_tool.html) — wrapper JAR verification and distribution checksums.

## Unicode

- [Unicode Standard Annex #29](https://www.unicode.org/reports/tr29/) — grapheme, word, and sentence boundary rules.
- [unicode-segmentation crate](https://docs.rs/unicode-segmentation/latest/unicode_segmentation/) — candidate Rust implementation for future grapheme-aware editing.

## CI actions

The workflow pins actions by full commit SHA. Review each action's upstream release and permissions before changing the pin:

- [actions/checkout](https://github.com/actions/checkout/releases)
- [actions/setup-java](https://github.com/actions/setup-java/releases)
- [android-actions/setup-android](https://github.com/android-actions/setup-android/releases)
- [dtolnay/rust-toolchain](https://github.com/dtolnay/rust-toolchain)
- [gradle/actions](https://github.com/gradle/actions/releases)
- [actions/upload-artifact](https://github.com/actions/upload-artifact/releases)

## Product references

The interaction direction studies user-visible patterns from Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. These are product references only. Their proprietary source code, assets, dictionaries, and branding are not project dependencies.

## Dependency review rule

A link in this file is a research reference, not approval to add a dependency. Before integration, check:

- Current maintenance and release activity.
- License and attribution requirements.
- Android compatibility and ABI behavior.
- Transitive dependencies and package size.
- Security advisories.
- Performance on a physical device.
- Whether a smaller platform-native implementation is sufficient.
