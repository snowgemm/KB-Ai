# Test Plan

**Status:** Telex/VNI engine tests are included in source; CI and physical-device verification are pending.

## 1. Test strategy

Test the Rust input engine independently from Android, then test the JNI boundary, Android editor adapter, full IME lifecycle, and real-device behavior.

A passing unit-test suite is not sufficient to call an Android keyboard stable.

## 2. Tests implemented in source

### Rust

The kb-core wrapper currently tests:

- Deterministic Phase 0 native status.
- Telex composition: tieengs → tiếng and vieetj → việt.
- VNI composition: tie6ng1 → tiếng and vie6t5 → việt.
- Repeated tone-key restoration.
- English bypass behavior for the word just.
- Undo of the most recent physical keystroke.
- Backspace delegation when there is no active composition.
- Commit clearing the active preedit.
- Reset clearing the active preedit.

Bamboo Core itself supplies a broader upstream regression suite. KB-Ai must still maintain product-specific test vectors for any configuration or integration behavior it depends on.

### Android

- EditorPolicy tests cover ordinary text, password variations, numeric passwords, and the no-personalized-learning editor flag.
- KeyboardLayoutPolicy tests cover text, numeric, and phone input types.
- The app includes sample text, email, URL, number, password, and multiline fields.
- The QWERTY IME includes alphabetic, symbols, numeric, and phone layouts.
- The IME maps common editor actions to their requested actions.

Source presence is not evidence that these checks pass; inspect the latest CI result before marking them passed.

## 3. Vietnamese input corpus still required

Maintain deterministic test vectors for:

- Telex tone and shape keys.
- VNI tone and shape keys.
- Repeated-key behavior and restoration rules.
- Backspace during an unfinished syllable.
- Space and punctuation commit behavior.
- Switching between VI and EN mid-session.
- Mixed Vietnamese and English text.
- Uppercase and capitalization.
- Invalid or incomplete sequences.
- Forced composition-buffer word boundaries.

Representative vectors:

| Input method | Input | Expected result |
|---|---|---|
| Telex | tieengs | tiếng |
| Telex | vieetj | việt |
| VNI | tie6ng1 | tiếng |
| VNI | vie6t5 | việt |
| Telex | ass | as |

The complete corpus must be reviewed and versioned. These examples are not a substitute for a full Vietnamese orthography test suite.

## 4. JNI tests

- Kotlin can load the native library on every supported ABI.
- Each exported method validates handles and character values.
- Rust results map to Kotlin values deterministically.
- Native errors are handled without crashing the IME.
- No panic or unhandled Java exception crosses the boundary.
- Session handles cannot be used after destruction.
- Repeated create/destroy cycles do not leak session state.

## 5. Android integration tests

Test at least these editor types:

- Ordinary single-line text.
- Multiline text.
- Search.
- Email.
- URL.
- Numeric.
- Phone.
- Password and numeric password.
- Fields with Next, Done, Search, and Send actions.
- Editors with selection changes and cursor movement.

Test lifecycle and composition cases:

- Start input and create a session.
- Type Telex and VNI sequences.
- Undo during an unfinished composition.
- Commit with space and punctuation.
- Switch VI/EN mode mid-word.
- Move the caret and select text during composition.
- Switch between applications.
- Finish input and destroy the session.
- Rotate the device and recreate the keyboard view.
- Switch to another IME and back.
- Lose or replace the InputConnection.

## 6. UI and accessibility tests

- Letter, number, and symbol layouts.
- Shift and one-shot uppercase.
- VI/EN toggle visibility and action.
- Portrait and landscape.
- Small and large display sizes.
- Theme and keyboard-height persistence when implemented.
- TalkBack labels and actions.
- Touch target and contrast checks.
- Font scaling and system animation settings.

## 7. Privacy tests

- Inspect the manifest for unnecessary permissions.
- Search logs for typed content and composing text.
- Confirm password fields do not create native input sessions.
- Confirm Bamboo full-text tracking is disabled.
- Confirm no content is written to disk during ordinary typing.
- Confirm core typing works without network access.
- Confirm no network requests occur in the core MVP.
- Confirm clipboard is not read automatically.
- Inspect crash and diagnostic payloads for content leakage.

## 8. Performance tests

Record on physical devices:

- Time from IME request to first usable keyboard frame.
- Key-event processing time, including the JNI boundary.
- UI frame timing during rapid typing.
- Memory after repeated editor sessions.
- Native library load time.
- Cost of session creation and destruction.

Record p50 and p95 values, device model, Android version, ABI, build type, and test conditions. Establish numeric budgets only after a reproducible baseline exists.

The synchronous key path must not perform file, database, network, or unbounded dictionary work.

## 9. CI checks

The workflow is intended to run:

- Rust formatting.
- Rust tests.
- Rust Clippy.
- Android unit tests.
- Debug APK assembly.
- Native builds for ARM64 and x86_64.

Workflow results must be checked before marking these gates as passed.

## 10. Device matrix

Before release, test:

- At least one physical ARM64 Android device.
- At least one lower-resource device within the declared support range.
- An emulator build for the supported x86_64 development target.
- The minimum supported Android API level.
- A current Android version available at release time.

The minimum supported API level is currently 23 for the prototype and should be re-evaluated before the production keyboard release.
