# Technical References

This page records primary documentation reviewed for the proposed architecture. Versions and recommendations must be rechecked when implementation begins.

**Last reviewed:** 2026-11-24

## Android input methods

- [Create an input method](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method) — Android IME implementation guide.
- [InputMethodService API](https://developer.android.com/reference/android/inputmethodservice/InputMethodService) — IME lifecycle and service API.
- [InputConnection API](https://developer.android.com/reference/android/view/inputmethod/InputConnection) — composing, commit, selection, deletion, and editor interaction.
- [EditorInfo API](https://developer.android.com/reference/android/view/inputmethod/EditorInfo) — editor type and IME options.
- [EditorInfoCompat API](https://developer.android.com/reference/androidx/core/view/inputmethod/EditorInfoCompat) — compatibility constants, including the no-personalized-learning request.
- [InputConnection.commitContent](https://developer.android.com/reference/android/view/inputmethod/InputConnection#commitContent(android.view.inputmethod.InputContentInfo,int,android.os.Bundle)) — rich-content insertion for future GIF and sticker support.
- [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker) — user-selected image access for future local keyboard themes.

## Rust and Android native integration

- [Rust Android platform support](https://doc.rust-lang.org/rustc/platform-support/android.html) — supported Android Rust targets and NDK requirements.
- [jni crate](https://docs.rs/jni/latest/jni/) — Rust bindings for the Java Native Interface.
- [cargo-ndk](https://github.com/bbqsrc/cargo-ndk) — candidate build helper for producing Android native libraries and ABI output directories.
- [UniFFI Kotlin / Gradle integration](https://mozilla.github.io/uniffi-rs/latest/kotlin/gradle.html) — evaluated alternative; it currently documents manual binding-generation wiring and a JNA dependency.

## Unicode

- [Unicode Standard Annex #29](https://www.unicode.org/reports/tr29/) — grapheme, word, and sentence boundary rules.
- [unicode-segmentation crate](https://docs.rs/unicode-segmentation/latest/unicode_segmentation/) — candidate Rust implementation of grapheme and word segmentation.

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
