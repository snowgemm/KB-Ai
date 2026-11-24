# Test Plan

**Status:** Phase 0 test scaffolding exists; full keyboard tests are not implemented.

## 1. Test strategy

Test the Rust core independently from Android, then test the JNI boundary, Android editor adapter, full IME lifecycle, and real-device behavior.

A passing unit-test suite is not sufficient to call an Android keyboard stable.

## 2. Tests implemented in Phase 0

### Rust

- A deterministic core status function is covered by a Rust unit test.
- The JNI library exposes the status function through one native method.

### Android

- `EditorPolicyTest` covers ordinary text, password text variations, numeric passwords, and ordinary numeric fields.
- The app includes a test input field and controls for opening Android IME settings and the system keyboard picker.
- The test-only IME can call Rust and explicitly insert a fixed status string into a focused editor.

These checks validate the initial integration seam. They do not test actual text transformation or a production keyboard layout.

## 3. Rust tests required as the input engine is developed

### Vietnamese input

Maintain deterministic test vectors for:

- Telex tone and shape keys.
- VNI tone and shape keys.
- Repeated-key behavior and restoration rules.
- Backspace during an unfinished syllable.
- Space and punctuation commit behavior.
- Switching input mode.
- Mixed Vietnamese and English text.
- Uppercase and capitalization.
- Invalid or incomplete sequences.

Representative vectors:

| Input sequence | Expected result |
|---|---|
| Telex: tieengs Vieetj | tiếng Việt |
| VNI: tie6ng1 Vie6t5 | tiếng Việt |
| Telex: aa | â |
| Telex: aw | ă |
| Telex: dd | đ |

The complete corpus must be reviewed and versioned. These examples are not a substitute for a full Vietnamese orthography test suite.

### Unicode

Test:

- NFC and NFD examples.
- Vietnamese combining marks.
- Supplementary-plane characters.
- Emoji with skin-tone modifiers.
- Regional-indicator flag sequences.
- Zero-width-joiner emoji sequences.
- Grapheme-aware deletion.
- UTF-8 to UTF-16 offset conversion.
- Empty strings and malformed boundary inputs.

### State machine

Test session creation, key sequences, commit, reset, mode switch, backspace, editor changes, and stale revision rejection.

Use property-based or fuzz testing for text transformations when a suitable test harness is added. Never seed tests with private user text.

## 4. JNI tests

- Kotlin can load the native library on every supported ABI.
- Each exported method validates its arguments.
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

Test lifecycle cases:

- Start input.
- Finish input.
- Switch between applications.
- Rotate the device.
- Change orientation.
- Recreate the keyboard view.
- Change input mode while composing.
- Switch to another IME and back.
- Lose or replace the InputConnection.

## 6. UI and accessibility tests

- Letter, number, and symbol layouts.
- Number-row toggle.
- Toolbar open/close.
- Portrait and landscape.
- Small and large display sizes.
- Theme and keyboard-height persistence.
- TalkBack labels and actions.
- Touch target and contrast checks.
- Font scaling and system animation settings.

## 7. Privacy tests

- Inspect the manifest for unnecessary permissions.
- Search logs for typed content and composing text.
- Confirm password fields do not populate candidates.
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

The Phase 0 workflow is intended to run:

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

The minimum supported API level is currently 23 for the Phase 0 prototype and should be re-evaluated before the production keyboard release.
