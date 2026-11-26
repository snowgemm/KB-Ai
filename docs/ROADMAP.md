# Roadmap

**Status:** Telex/VNI engine integration implemented in source; CI and physical-device verification are pending. Milestones are ordered by dependency and risk, not by calendar date. No release dates are promised.

## Phase 0 — Repository and integration spike

**Goal:** prove the Android/Rust toolchain before building product features.

Implemented:

- Android app and InputMethodService.
- Rust workspace and JNI library.
- Rust formatting, tests, Clippy, Android unit tests, APK build in CI.
- ARM64 and x86_64 native library packaging.

The Phase 0 CI build passed on 2026-11-25. Physical-device evidence is still required.

## Phase 1 — Functional Android IME

Implemented in source:

- QWERTY letter layout.
- Shift and one-shot uppercase.
- Backspace, Space, punctuation, and symbol layout.
- Numeric and phone layouts based on EditorInfo.
- Editor actions: Go, Search, Send, Next, Previous, and Done.
- Email and URL punctuation.
- VI/EN toggle for eligible text fields.
- Test fields for text, email, URL, number, password, and multiline input.

Still required:

- Physical-device testing on supported Android versions.
- Validation in common third-party editors.
- Accessibility and touch-target review.
- Portrait/landscape and small-screen review.
- Regression tests for selection replacement, editor switching, and rapid typing.

## Phase 2 — Vietnamese input engine

Implemented in source:

- Bamboo Core 0.6.1 integration through the Rust kb-core wrapper.
- Combined Telex + VNI input mode.
- Live composition using Android InputConnection.setComposingText.
- Composition finalization at spaces and punctuation.
- Per-key undo inside the active composition.
- English bypass mode and VI/EN toggle.
- Forced-boundary handling for the bounded Rust composition buffer.
- Per-editor session creation and destruction.
- Disable Bamboo committed-text tracking to avoid retaining completed text.

Verification still required:

- CI build and all Rust/Android tests pass after integration.
- Expanded Telex/VNI corpus, including uppercase, repeated keys, invalid sequences, punctuation, and mixed English/Vietnamese.
- Test backspace during a composition and after commit.
- Confirm selection movement and app switching do not move or duplicate text.
- Physical-device testing in messaging, browser, notes, and search fields.

**Exit criteria:** the reviewed corpus passes, composition is stable across tested editors, and no known high-severity text corruption defect remains.

## Phase 3 — Unicode and editor hardening

- Grapheme-aware deletion and cursor controls.
- Explicit UTF-8 / UTF-16 offset conversion for native/editor boundaries.
- Selection reconciliation tests.
- Expanded sensitive-field handling.
- No-content logging and storage checks.
- Physical-device performance baselines.
- Accessibility review.

**Exit criteria:** all required regression, privacy, and device tests pass.

## Phase 4 — Core experience and customization

- Candidate row foundation.
- Toolbar and expandable tool pages.
- Light, dark, and system themes.
- Keyboard-height setting.
- Built-in themes and local image background.
- Theme preview and persistence.

## Phase 5 — Local language support

- Vietnamese and English dictionaries.
- Personal dictionary, if privacy and deletion behavior are complete.
- Suggestions and autocorrection.
- Explicit opt-in for local personalization.
- Stale-result cancellation and revision checks.

## Phase 6 — Everyday tools

- Emoji search and categories.
- Clipboard tools with opt-in history.
- Text editing shortcuts.
- Text shortcuts.
- Tool pinning and reordering.

## Phase 7 — AI and advanced features

Evaluate independently:

- User-triggered grammar correction and rewriting.
- Translation and summarization.
- Optional AI composition assistance.
- Swipe typing.
- GIF and stickers.
- Voice input.
- Handwriting.

Any online AI feature requires an explicit user action, clear disclosure of what text is sent, provider and retention review, and a failure mode that does not break ordinary typing.

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
