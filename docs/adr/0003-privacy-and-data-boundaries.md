# ADR 0003: Local-first data boundaries

- **Status:** Accepted for the MVP
- **Date:** 2026-11-24

## Context

A keyboard can observe text entered into other applications. Typing history, clipboard content, and contextual prediction can expose sensitive information.

The core keyboard does not need a server to transform Telex or VNI input.

## Decision

The MVP will:

- Require no account.
- Require no network permission for core typing.
- Store no typing history.
- Keep composing text in memory only for the active editor session.
- Disable learning and candidates for password and sensitive fields.
- Respect the editor's no-personalized-learning request.
- Avoid automatic clipboard reads and clipboard history.
- Exclude analytics and typed-content telemetry.
- Keep settings local.

## Future feature gate

A feature that introduces network access, persistent user text, clipboard history, or personal learning requires:

1. A documented user benefit.
2. Data-flow and threat-model review.
3. Explicit user control and clear defaults.
4. A retention and deletion policy.
5. Tests proving that sensitive fields are excluded.
6. An update to the privacy documentation.

## Consequences

The MVP has fewer dependencies and a smaller data-collection surface. Advanced suggestions, synchronization, and online tools may be less capable until their privacy model is designed and approved.

This ADR describes required behavior; implementation and verification are still pending.
