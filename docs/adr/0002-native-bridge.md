# ADR 0002: Use a thin JNI boundary for the Android-first phase

- **Status:** Accepted; initial session API implemented, device performance validation pending
- **Date:** 2026-11-24
- **Implementation update:** 2026-11-26

## Context

The current product target is Android only. Kotlin must call a Rust shared library from an Android application. The boundary is latency-sensitive because key events are frequent.

Two options were considered:

1. Direct JNI bindings from Kotlin to Rust.
2. UniFFI-generated Kotlin bindings.

UniFFI is a capable binding generator, but its Kotlin/Gradle integration adds binding-generation and runtime machinery for an Android-only boundary.

## Decision

Use a thin direct JNI bridge, using the Rust jni crate.

The bridge:

- Exposes typed session operations.
- Passes Unicode scalar values and compact preedit strings, not Android objects.
- Avoids native-to-Kotlin callbacks.
- Keeps session ownership and editor dispatch in Kotlin.
- Uses native handles to isolate editor sessions.
- Converts the active preedit to a Java String while the engine's reusable Rust buffer is borrowed.
- Does not pass full editor text or committed text to Rust.

## Why this decision

The decision favors explicit control of the Android-only boundary and fewer binding-generation layers. It does **not** claim that JNI has been benchmarked as faster than UniFFI. Performance and implementation complexity must be measured on physical devices.

## Current JNI API

- nativeCreateSession(): Long
- nativeProcessKey(handle, codePoint, vietnamese): String
- nativeWasForcedCommit(handle): Boolean
- nativeUndoKey(handle): String
- nativeCommit(handle): Unit
- nativeDestroySession(handle): Unit
- nativeStatus(): String (diagnostic)

The Rust registry is process-local and mutex-protected. Kotlin creates one engine session per eligible editor input session and destroys it when input finishes.

## Alternatives

### UniFFI

Reconsider if generated bindings materially reduce maintenance and the JNA / Gradle integration cost is acceptable after a working prototype.

### Kotlin-only engine

Not selected because the project requirement is to keep Vietnamese input transformation in Rust.

## Validation

The automated workflow must verify:

- Native library loading and supported ABI packaging.
- Kotlin-to-Rust JNI method resolution.
- Telex and VNI regression vectors.
- Session creation, composition, undo, commit, and destruction.
- Rust formatting, tests, and Clippy.
- Android unit tests and debug APK assembly.

Physical-device testing must still measure key-event latency, memory, editor lifecycle behavior, and compatibility with third-party applications.

If JNI becomes unsafe, difficult to maintain, or fails the measured performance budget, revisit this ADR before expanding the native API.
