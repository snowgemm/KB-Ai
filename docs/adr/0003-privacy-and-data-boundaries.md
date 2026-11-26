# ADR 0003: Local-first data boundaries

- **Status:** Accepted; initial implementation follows the local-only composition model
- **Date:** 2026-11-24
- **Implementation update:** 2026-11-26

## Context

A keyboard can observe text entered into other applications. Typing history, clipboard content, and contextual prediction can expose sensitive information.

The core keyboard does not need a server to transform Telex or VNI input.

## Decision

The MVP:

- Requires no account.
- Requires no Internet permission for core typing.
- Stores no typing history.
- Keeps the active composition in memory only for the current editor session.
- Disables Bamboo Core full-text tracking so committed words and raw keystrokes are not retained in its committed-text buffers.
- Does not create Vietnamese composition sessions for password, email, URL, numeric, or phone fields.
- Does not implement learning or candidates yet.
- Respects the editor's no-personalized-learning request for future learning features.
- Avoids automatic clipboard reads and clipboard history.
- Excludes analytics and typed-content telemetry.
- Keeps settings local.

## Future feature gate

A feature that introduces network access, persistent user text, clipboard history, or personal learning requires:

1. A documented user benefit.
2. Data-flow and threat-model review.
3. Explicit user control and clear defaults.
4. A retention and deletion policy.
5. Tests proving that sensitive fields are excluded.
6. An update to the privacy documentation.

## Consequences

The current keyboard can transform Vietnamese text offline without storing completed words in the input engine. The active preedit must remain in memory long enough to support transformations and undo. Once committed or the editor session ends, the native session is finalized and destroyed.

Advanced suggestions, synchronization, and online tools may be less capable until their privacy model is designed and approved.

Physical-device and runtime verification remain required; this ADR is not a certification of privacy or security.
