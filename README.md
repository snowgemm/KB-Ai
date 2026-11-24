# KB-Ai

**A Vietnamese-first Android keyboard built around a native Rust input engine.**

KB-Ai is a planned Android input method focused on accurate Vietnamese typing, a familiar and customizable keyboard experience, and privacy-preserving on-device processing.

> **Project status: Planning / specification.** This repository currently contains project documentation and repository conventions. The Android application, Rust engine, builds, and releases have not been implemented yet.

## Product principles

- **Vietnamese-first:** Telex and VNI are core requirements, not optional add-ons.
- **Android-native:** use Android's input method framework for system integration.
- **Rust core:** keep input-state and text-processing logic independent of Android UI.
- **Private by design:** do not collect, transmit, or retain typed content by default.
- **Offline-first:** core typing must not require network access.
- **Fast and predictable:** typing must never wait for dictionaries, network calls, or background suggestions.
- **Customizable without clutter:** expose useful tools without overcrowding the typing surface.
- **Accessible:** keyboard controls must remain usable with assistive technologies.

## Planned capabilities

| Area | Planned scope |
|---|---|
| Core typing | QWERTY, Vietnamese Telex, Vietnamese VNI, English |
| Layouts | Number row, symbols, punctuation, editor-specific layouts |
| Editing | Composing text, selection-aware editing, cursor controls |
| Personalization | Light/dark/system, keyboard height, themes, local image backgrounds |
| Tools | Emoji, clipboard, text editing, shortcuts |
| Advanced tools | GIF, stickers, voice input, handwriting, translation |
| Privacy | No typing-history collection, password-field protections, local controls |

Features are staged in the [roadmap](docs/ROADMAP.md). A feature listed here is planned, not implemented.

## Architecture at a glance

- **Kotlin / Android:** IME lifecycle, Android framework integration, keyboard UI, settings, and local platform storage.
- **Rust:** input session state, Vietnamese transformation rules, and text-processing logic.
- **JNI:** a deliberately small boundary between Kotlin and Rust for the Android-only first phase.
- **Android InputConnection:** the only route used by the IME to edit the focused application.

See [Architecture](docs/ARCHITECTURE.md) and the [architecture decisions](docs/adr/).

## Documentation

- [Product specification](docs/PRODUCT_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)
- [UI/UX specification](docs/UI_UX_SPEC.md)
- [Security and privacy](docs/SECURITY_PRIVACY.md)
- [Test plan](docs/TEST_PLAN.md)
- [Roadmap](docs/ROADMAP.md)
- [Technical references](docs/REFERENCES.md)
- [Contributing](CONTRIBUTING.md)
- [Security reporting](SECURITY.md)

## Development status

There is no installable APK, production IME, or executable build at this stage. Do not use this repository as an installed keyboard or assume any planned feature is available.

Implementation starts with a small Rust-to-Android integration spike. The project will add build workflows and setup instructions only after the real build structure exists and can be tested.

## Inspiration and independence

The product studies common interaction patterns found in established Android keyboards, including Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. KB-Ai is an independent project. It does not use their branding, proprietary assets, private datasets, or source code.

## License

The repository is distributed under the MIT License. See [LICENSE](LICENSE).
