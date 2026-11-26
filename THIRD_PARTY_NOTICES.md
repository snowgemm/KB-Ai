# Third-party notices

This project includes or references the following third-party components. Re-check versions and notices whenever a component is upgraded.

## Gradle Wrapper 9.9.2

- Source: https://github.com/gradle/gradle/tree/v9.9.2
- Files: gradlew, gradlew.bat, and gradle/wrapper/gradle-wrapper.jar
- License: Apache License 2.0
- The upstream license text is included at gradle/wrapper/LICENSE.
- Official wrapper JAR SHA-256: 621008afc4f5d59357fcf06ffa644dd5aade5e091bdc363742710445f73cdaf8.

## Rust JNI crate

- Package: jni 0.22.4
- Source: https://crates.io/crates/jni/0.22.4
- License: MIT OR Apache-2.0 (as declared by the upstream package).
- The dependency is fetched by Cargo and is not vendored into this repository.

## Bamboo Core

- Package: bamboo-core 0.6.1
- Source: https://github.com/bamboo-vi/bamboo-core
- Package page: https://crates.io/crates/bamboo-core/0.6.1
- License: MIT (as declared by the upstream package).
- Copyright notices in the upstream license: Luong Thanh Lam (2018) and nguien (2024).
- KB-Ai consumes the published crate through Cargo; the upstream source is not copied into this repository.
- KB-Ai disables Bamboo Core full-text tracking so committed words and raw keystrokes are not retained in the engine's full-text buffers.

## cargo-ndk

- Version: 4.1.2
- Source: https://github.com/bbqsrc/cargo-ndk/tree/v4.1.2
- License: MIT OR Apache-2.0 (as declared by the upstream project).
- This is a build-time tool installed in the build environment; it is not packaged into the app.

## Android SDK and NDK

Android SDK Platform, Build Tools, and NDK are downloaded using Google's SDK Manager. Their licenses are governed by the Android SDK license agreements accepted by the build environment.

## Review rule

No commercial keyboard assets, private dictionaries, or proprietary source code may be copied into KB-Ai. Every future dictionary, language model, font, theme, image, or other bundled asset must have a documented origin, version, and compatible license.
