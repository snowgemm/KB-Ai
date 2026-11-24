# ADR 0001: Android-first platform and layer boundaries

- **Status:** Accepted for the initial implementation
- **Date:** 2026-11-24

## Context

KB-Ai is currently intended as an Android keyboard. Android owns the input method lifecycle, focused editor connection, system keyboard selection, and editor metadata. Rust is selected for the input engine and text-processing core.

A single-language implementation would either make Android framework integration less natural or tie the input engine too tightly to Android UI.

## Decision

Use an Android-native Kotlin layer and an independent Rust core.

### Kotlin owns

- InputMethodService and Android lifecycle.
- InputConnection calls.
- EditorInfo interpretation and sensitive-field policy.
- Keyboard Views, touch events, and accessibility.
- Settings, themes, onboarding, and platform integrations.

### Rust owns

- Active input-session state.
- Vietnamese Telex and VNI transformations.
- Composing-buffer transformations.
- Text rules that can be tested without Android.
- Unicode-aware helpers required by the input engine.

### Explicit boundary

Rust does not own Android Views, Context, InputConnection, or the committed contents of another app's editor. Kotlin is the only layer allowed to call Android editor APIs.

Keep the initial Gradle structure small. Use packages and clear interfaces before adding multiple Android Gradle modules. Keep the Rust workspace to a few crates until independent ownership or testing justifies more.

## Consequences

- The core engine can be tested on a host without launching Android.
- Android-specific behavior remains close to the framework APIs.
- A thin native interface is required.
- Kotlin and Rust must agree on text offsets, event ordering, and session lifecycle.
- The first release targets Android only; cross-platform abstractions are out of scope.

## Deferred decisions

- Minimum and target Android API levels.
- Whether 32-bit ARM is supported.
- Exact Gradle module boundaries.
- Dictionary storage and prediction implementation.
