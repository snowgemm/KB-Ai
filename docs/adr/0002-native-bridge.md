# ADR 0002: Use a thin JNI boundary for the Android-first phase

- **Status:** Accepted for Phase 0; validate during the integration spike
- **Date:** 2026-11-24

## Context

The current product target is Android only. Kotlin must call a Rust shared library from an Android application. The boundary is latency-sensitive because key events are frequent.

Two options were considered:

1. Direct JNI bindings from Kotlin to Rust.
2. UniFFI-generated Kotlin bindings.

UniFFI is a capable binding generator, but its current Kotlin/Gradle guide documents manual binding-generation wiring and a JNA runtime dependency. This is additional build and runtime machinery for an Android-only boundary.

## Decision

Start with a thin direct JNI bridge, using the Rust jni crate.

The bridge must:

- Expose a small number of typed operations.
- Pass semantic key events and compact results, not Android objects.
- Avoid native-to-Kotlin callbacks in the MVP.
- Keep each JNI call coarse enough to avoid unnecessary transitions.
- Validate all inputs and convert errors into explicit results.
- Ensure Rust panics never unwind across JNI.
- Keep session ownership and UI-thread dispatch in Kotlin.

## Why this decision

The decision favors explicit control of the Android-only boundary and fewer binding-generation layers. It does **not** claim that JNI has been benchmarked as faster than UniFFI. Performance and implementation complexity must be measured during Phase 0.

## Alternatives

### UniFFI

Reconsider if generated bindings materially reduce maintenance and the JNA / Gradle integration cost is acceptable after a working prototype.

### Kotlin-only engine

Not selected because the project requirement is to keep the core Vietnamese input logic in Rust.

## Validation

The integration spike must verify:

- Native library loading.
- Supported ABI packaging.
- Kotlin-to-Rust calls.
- String and enum conversion.
- Error propagation.
- Repeated session creation and destruction.
- Key-event latency on a physical device.

If the JNI implementation becomes unsafe, difficult to maintain, or fails the measured performance budget, revisit this ADR before expanding the native API.
