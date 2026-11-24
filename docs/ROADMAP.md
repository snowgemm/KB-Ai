# Roadmap

**Status:** Phase 0 source prototype is being established. Milestones are ordered by dependency and risk, not by calendar date. No release dates are promised.

## Phase 0 — Repository and integration spike

**Goal:** prove the Android/Rust toolchain before building product features.

Current source scope:

- Gradle application skeleton.
- Rust workspace with a core crate and JNI cdylib.
- Android test app and test-only IME.
- Kotlin-to-Rust status call.
- Rust status unit test.
- Android sensitive-field policy unit tests.
- Pinned toolchain and CI workflow.

Remaining exit checks:

- CI builds the APK successfully.
- Rust formatting, tests, and Clippy pass.
- Android unit tests pass.
- Native libraries are packaged for ARM64 and x86_64.
- Install and test on a physical Android device.
- Record actual build and device-test evidence.

Do not treat documentation or source creation alone as completion of Phase 0.

## Phase 1 — Android IME skeleton

- Replace the Phase 0 test-only UI with a minimal letter keyboard.
- Handle ordinary key input through InputConnection.
- Handle editor actions and lifecycle callbacks.
- Add test fields for text, email, URL, number, and password input.

**Exit criteria:** the IME can be enabled, selected, shown, and used in a range of editor fields without losing or duplicating ordinary characters.

## Phase 2 — Keyboard layouts

- QWERTY layout.
- Shift and long-press alternatives.
- Space, punctuation, Backspace, and Enter/action key.
- Optional number row.
- Number and symbol layouts.
- Language and input-mode switching.

**Exit criteria:** layout transitions are reliable in portrait and landscape, and input actions match the focused editor.

## Phase 3 — Vietnamese input engine

- Telex engine.
- VNI engine.
- Composing and commit rules.
- Backspace and undo within the active composition.
- Capitalization and punctuation behavior.
- Versioned Vietnamese test corpus.

**Exit criteria:** the reviewed corpus passes and no known high-severity text corruption defect remains.

## Phase 4 — Unicode, lifecycle, and privacy hardening

- Grapheme-aware editing.
- UTF-8 / UTF-16 conversion tests.
- Editor selection reconciliation.
- Password and sensitive-field policies.
- No-content logging checks.
- Physical-device performance baseline.
- Accessibility review.

**Exit criteria:** all required regression, privacy, and device tests pass.

## Phase 5 — Core experience and customization

- Candidate row foundation.
- Toolbar and expandable tool pages.
- Light, dark, and system themes.
- Keyboard-height setting.
- Built-in themes and local image background.
- Theme preview and persistence.

**Exit criteria:** customization does not cause input loss, unacceptable frame jank, or accessibility regressions.

## Phase 6 — Local language support

- Vietnamese and English dictionaries.
- Personal dictionary, if privacy and deletion behavior are complete.
- Suggestions and autocorrection.
- Explicit opt-in for local personalization.
- Stale-result cancellation and revision checks.

**Exit criteria:** suggestions are correct enough for the reviewed corpus, can be disabled, and do not alter committed text without user action.

## Phase 7 — Everyday tools

- Emoji search and categories.
- Clipboard tools with opt-in history.
- Text editing shortcuts.
- Text shortcuts.
- Tool pinning and reordering.

**Exit criteria:** each tool has clear permissions, failure handling, privacy behavior, and tests.

## Phase 8 — Advanced and network-capable features

Evaluate independently:

- Swipe typing.
- GIF and stickers.
- Voice input.
- Translation.
- Handwriting.
- One-handed and floating modes.

Each feature requires an implementation proposal, compatibility test, privacy review, dependency/license review, and explicit user-facing behavior before it is scheduled.

## Release gates

A release candidate must have:

- Reproducible build instructions.
- Passing automated checks.
- Physical-device test evidence.
- Documented supported API levels and ABIs.
- A reviewed privacy notice.
- No unresolved critical or high-severity text corruption issue.
- A known-limitations section.

Do not mark a phase complete based only on UI mockups or documentation.
