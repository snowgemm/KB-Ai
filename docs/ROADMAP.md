# Roadmap

**Status:** Planning roadmap. Milestones are ordered by dependency and risk, not by calendar date. No release dates are promised.

## Phase 0 — Repository and integration spike

**Goal:** prove the Android/Rust toolchain before building product features.

- Establish the Gradle application skeleton.
- Establish the smallest Rust workspace.
- Build a Rust shared library for Android.
- Load the native library from Kotlin.
- Exercise a minimal JNI method.
- Verify ABI packaging on an emulator and a physical device.
- Record toolchain versions and a reproducible build path.
- Decide the minimum Android API level from the support target.

**Exit criteria:** a reproducible debug build loads the native library and returns a validated test result on the selected Android targets.

## Phase 1 — Android IME skeleton

- Declare and register InputMethodService.
- Complete the enable/select onboarding flow.
- Show a basic keyboard view.
- Insert ordinary characters through InputConnection.
- Handle editor actions and lifecycle callbacks.

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
