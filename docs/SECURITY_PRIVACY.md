# Security and Privacy Design

**Status:** Telex/VNI composition is local and in-memory; full security and privacy verification is pending.

## 1. Threat model

A keyboard can observe text entered into many applications. That makes its data boundary more sensitive than an ordinary utility app.

KB-Ai must assume that users may enter passwords, authentication codes, private messages, financial details, or other sensitive content.

The design follows data minimization: do not collect information that the keyboard does not need to provide the requested input behavior.

## 2. Current source behavior

- Does not request Android Internet permission.
- Uses InputConnection to insert composing and committed text into the focused editor.
- Does not read the text already present in the focused editor.
- Does not read or monitor Clipboard content.
- Creates a Rust composition session only for eligible text fields.
- Password, email, URL, numeric, and phone fields bypass the Vietnamese composition engine.
- Bamboo Core full-text tracking is disabled; completed words and raw keystrokes are not retained in its committed-text buffers.
- Active composition remains in memory only for the current editor session.
- Destroys the native session when input finishes or the service is destroyed.
- Does not implement suggestions, personalization, analytics, or network services.

The current build is an early prototype. Do not use it for real passwords or other sensitive information until the release privacy and device-test gates are complete.

This is a source-level design description. CI and device testing must still verify the implementation.

## 3. Default data policy

| Data | Default behavior |
|---|---|
| Text entered in an editor | Process only the current composition needed for Telex/VNI; do not persist |
| Active composing buffer | In-memory Rust session for the current editor; cleared on finish/reset |
| Committed text | Stored by the target application; not retained in Bamboo Core |
| Raw keystroke history | Not retained after composition commit |
| Typing history | Not collected |
| Personalized learning | Not implemented |
| Password content | No native input session, learning, suggestion, logging, or persistence |
| Clipboard | No automatic monitoring |
| Clipboard history | Not implemented; opt-in if introduced |
| Personal dictionary | Later, local-only and user-controlled |
| Analytics / telemetry | Not included |
| Cloud synchronization | Not included |
| Network access | Not required for the current keyboard |

## 4. Sensitive editor fields

Kotlin derives the keyboard layout from EditorInfo inputType. EditorPolicy detects password variations. The composition engine is not created for password, email, URL, numeric, and phone fields.

Future candidate or learning features must also respect IME_FLAG_NO_PERSONALIZED_LEARNING. This editor flag is a request from the app; it is not a substitute for detecting password input types.

Any future field type excluded from composition must be covered by explicit tests.

## 5. Logging and diagnostics

Never log:

- Typed text or composing text.
- Text returned by getTextBeforeCursor or getSelectedText.
- Clipboard contents.
- Passwords, authentication codes, or personal dictionary entries.
- Full EditorInfo dumps if they can expose sensitive information.

Diagnostics may record non-content metadata such as event names, error categories, app version, and timing measurements. Any diagnostic collection must be documented and reviewed before implementation.

Debug builds must follow the same no-typed-content rule.

## 6. Clipboard

The current prototype does not implement clipboard history.

If clipboard history is added later:

- Reading clipboard content must be user-initiated.
- History must be off by default.
- Provide clear, delete, and retention controls.
- Do not store content marked or identified as sensitive.
- Do not send clipboard data to a server.
- Do not include clipboard data in diagnostics.
- Respect Android clipboard privacy behavior and platform restrictions.

## 7. Network and online features

The core keyboard should not request the Android Internet permission.

A later online feature must have:

1. A documented user benefit.
2. An explicit user action.
3. A clear explanation of what data leaves the device.
4. A documented provider and retention policy.
5. A failure mode that does not break ordinary typing.
6. A separate security and privacy review.

Do not send typed text to a remote AI service by default.

## 8. Local storage

- Store only settings and data needed for enabled features.
- Do not persist the active composing buffer.
- Use Android application-private storage.
- Provide deletion controls for user-created local data.
- Avoid collecting data merely because it might improve future prediction.

## 9. Native boundary

- Validate all JNI handles and character values.
- Convert native failures into controlled error results.
- Do not allow Rust panics to unwind across the JVM boundary.
- Do not leave unexpected Java exceptions pending.
- Do not pass Android framework objects into the Rust core.
- Do not create native background threads that call back into Kotlin in the MVP.
- Keep native dependencies minimal and review their licenses.

## 10. Assets and datasets

Every dictionary, language model, test corpus, font, image, emoji asset, and theme asset must have:

- A source URL or documented origin.
- A recorded license.
- Attribution requirements, if any.
- A version or revision.
- A documented update path.

Do not copy proprietary dictionaries or assets from commercial keyboard applications.

## 11. Release security checklist

Before a release:

- Review AndroidManifest permissions.
- Inspect dependencies and licenses.
- Confirm no typed content appears in logs.
- Inspect network behavior and verify that core typing works offline.
- Test sensitive fields.
- Test clipboard behavior.
- Review native error handling.
- Verify release signing and secret handling.
- Publish a clear privacy notice that matches the implementation.

This document is a design requirement, not evidence that the implementation already meets it.
