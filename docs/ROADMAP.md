# Roadmap

**Status:** Phase 1 QWERTY prototype implemented in source; CI and physical-device verification remain open. Milestones are ordered by dependency and risk, not by calendar date. No release dates are promised.

## Phase 0 — Repository and integration spike

**Goal:** prove the Android/Rust toolchain before building product features.

Implemented source scope:

- Gradle application skeleton.
- Rust workspace with a core crate and JNI cdylib.
- Android app and initial InputMethodService.
- Kotlin-to-Rust status call.
- Rust status unit test.
- Android editor-policy unit tests.
- Pinned toolchain and CI workflow.

Remaining exit checks:

- CI builds the APK successfully.
- Rust formatting, tests, and Clippy pass.
- Android unit tests pass.
- Native libraries are packaged for ARM64 and x86_64.
- Install and test on a physical Android device.
- Record actual build and device-test evidence.

Do not treat documentation or source creation alone as completion of Phase 0.

## Phase 1 — Functional Android IME

Implemented in the current source:

- QWERTY letter layout.
- Shift and one-shot uppercase behavior.
- Backspace through Android key events.
- Space, comma, period, and symbol layout.
- Numeric layout for number/date-time fields.
- Phone layout for phone fields.
- Email and URL punctuation adjustments.
- Editor action labels and handling for Go, Search, Send, Next, Previous, and Done.
- Test fields for text, email, URL, number, password, and multiline input.

Still required before Phase 1 can be called complete:

- CI build and test success.
- Physical-device testing on supported Android versions.
- Validation in common third-party editors.
- Accessibility and touch-target review.
- Portrait/landscape and small-screen review.
- Regression testing for selection replacement, editor switching, and rapid typing.

## Phase 2 — Vietnamese input engine and text editing

- Telex engine.
- VNI engine.
- Composing text and commit rules.
- Backspace and undo within unfinished syllables.
- Capitalization, punctuation, and restoration rules.
- Unicode grapheme-aware deletion and explicit UTF-8 / UTF-16 offset conversion.
- Versioned Vietnamese test corpus.

**Exit criteria:** the reviewed corpus passes and no known high-severity text corruption defect remains.

## Phase 3 — Core experience and customization

- Candidate row foundation.
- Toolbar and expandable tool pages.
- Light, dark, and system themes.
- Keyboard-height setting.
- Built-in themes and local image background.
- Theme preview and persistence.

**Exit criteria:** customization does not cause input loss, unacceptable frame jank, or accessibility regressions.

## Phase 4 — Local language support

- Vietnamese and English dictionaries.
- Personal dictionary, if privacy and deletion behavior are complete.
- Suggestions and autocorrection.
- Explicit opt-in for local personalization.
- Stale-result cancellation and revision checks.

**Exit criteria:** suggestions are correct enough for the reviewed corpus, can be disabled, and do not alter committed text without user action.

## Phase 5 — Everyday tools

- Emoji search and categories.
- Clipboard tools with opt-in history.
- Text editing shortcuts.
- Text shortcuts.
- Tool pinning and reordering.

**Exit criteria:** each tool has clear permissions, failure handling, privacy behavior, and tests.

## Phase 6 — AI and advanced features

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
