# Product Specification

**Product:** KB-Ai  
**Platform:** Android first  
**Status:** Telex/VNI integration prototype; production keyboard features remain in progress  
**Primary language:** Vietnamese  
**Secondary language:** English

## 1. Product summary

KB-Ai is an independent Android keyboard designed to combine accurate Vietnamese input, a familiar multi-tool keyboard experience, meaningful customization, and privacy-preserving defaults.

The product may study interaction patterns found in Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. It must develop its own visual identity and implementation. It must not copy proprietary assets, source code, private dictionaries, or branding.

The word "AI" in the project name is not a promise of cloud AI. The core keyboard must work without an account, network access, or remote text processing.

## 2. Current implementation

The current source contains:

- Android QWERTY, symbols, numeric, and phone layouts.
- Shift, Backspace, Space, punctuation, and editor actions.
- VI/EN toggle in eligible text fields.
- Bamboo Core 0.6.1 integration for combined Telex and VNI.
- Live composing text through InputConnection.
- Rust per-editor sessions, composition undo, commit, and destruction.
- Password, email, URL, and numeric fields bypass the Vietnamese composition engine.

This is not a production-ready keyboard. CI and physical-device verification are still required. It does not yet implement dictionaries, candidates, prediction, autocorrection, clipboard history, emoji search, or AI text tools.

## 3. Target users and primary jobs

### Primary users

- People who regularly type Vietnamese on Android.
- People who switch between Vietnamese and English.
- People who want a customizable keyboard with useful built-in tools.
- People who prefer typing to remain on their device.

### Primary jobs

1. Type Vietnamese using Telex or VNI.
2. Switch between Vietnamese and English without losing the current input state.
3. Enter numbers, symbols, punctuation, and editor-specific values.
4. Correct or edit text without unexpected changes.
5. Customize the keyboard's appearance and size.
6. Access optional tools without overcrowding the typing surface.
7. Understand and control what data the keyboard stores or accesses.

## 4. Product principles

| Principle | Requirement |
|---|---|
| Correctness before intelligence | Input transformation must be reliable before prediction is introduced. |
| Privacy by default | No typing history, personalization, or clipboard history by default. |
| Offline core | Basic typing must work without Internet access. |
| Responsive input | The synchronous key path must not perform disk or network I/O. |
| User control | Suggestions, learning, clipboard, and online tools must be independently controllable. |
| Familiar, not copied | Use understandable keyboard conventions with an original KB-Ai visual identity. |
| Progressive disclosure | Advanced tools belong in expandable panels, not permanently on the main key surface. |
| Accessibility | Keys and toolbar actions must expose accessible semantics. |

## 5. Planned scope

### 5.1 Current core typing scope

- Android IME registration and activation flow.
- QWERTY letter layout.
- Vietnamese Telex and VNI composition through Bamboo Core.
- VI/EN input-mode toggle.
- Composing text, commit, and per-keystroke undo.
- Number, phone, and symbol layouts.
- Space, punctuation, Backspace, Shift, and editor action key.
- Password, email, URL, and numeric field handling.
- Local-only input processing.

### 5.2 Core keyboard work still required

- Expanded Telex and VNI test corpus.
- Selection-aware editing and cursor controls.
- Long-press alternatives.
- Keyboard height and appearance settings.
- Grapheme-aware deletion outside the active composition.
- More editor compatibility and lifecycle tests.
- Accessibility and responsive layout hardening.

### 5.3 Planned after the core is stable

- Dictionary packs and personal dictionary.
- Word suggestions and autocorrection.
- Clipboard history, explicitly opt-in.
- Emoji browser and search.
- Toolbar customization and multiple tool pages.
- Theme editor and local image backgrounds.
- One-handed and floating layouts.
- Swipe typing.
- GIF and sticker insertion where the target editor supports the required MIME types.
- Voice input, with processing location disclosed.
- Handwriting and translation.
- User-triggered AI text operations.

### 5.4 Explicit non-goals for the first stable keyboard release

- Required account or sign-in.
- Cloud synchronization of typed content.
- Typing telemetry or analytics.
- Automatic clipboard monitoring.
- Remote dictionaries or network-dependent Vietnamese input.
- Desktop or iOS version.
- A promise of compatibility with every third-party editor.

## 6. Functional requirements

| ID | Requirement | Priority |
|---|---|---|
| FR-001 | The user can enable KB-Ai through Android system settings. | Core |
| FR-002 | The user can select KB-Ai as the active IME. | Core |
| FR-003 | The keyboard can enter and commit ordinary text. | Core |
| FR-004 | The keyboard supports Vietnamese Telex. | Core |
| FR-005 | The keyboard supports Vietnamese VNI. | Core |
| FR-006 | The user can switch between Vietnamese and English. | Core |
| FR-007 | The keyboard supports letter, number, and symbol layouts. | Core |
| FR-008 | The keyboard responds to the editor's requested action key. | Core |
| FR-009 | The keyboard handles composing text and selection updates safely. | Core |
| FR-010 | Sensitive fields disable suggestions and learning. | Core |
| FR-011 | The user can select Light, Dark, or System appearance. | Core |
| FR-012 | The user can adjust keyboard height within supported bounds. | Core |
| FR-013 | The user can preview the keyboard before completing setup. | Core |
| FR-014 | The user can browse and search emoji. | Later |
| FR-015 | The user can opt in to local clipboard history. | Later |
| FR-016 | The user can create and save themes. | Later |
| FR-017 | Online tools require an explicit action and clear data disclosure. | Later |

## 7. Non-functional requirements

- Reliability: no lost or duplicated characters during ordinary typing.
- Text integrity: editing must not corrupt combining marks, surrogate pairs, or emoji sequences.
- Performance: no disk I/O, network I/O, dictionary loading, or unbounded work in the synchronous key path.
- Lifecycle: starting, finishing, or switching an editor must not leak composing state into the next editor.
- Privacy: typed content must not appear in logs, analytics, crash reports, or network payloads.
- Accessibility: keyboard keys and toolbar actions must expose meaningful semantics.
- Maintainability: input rules must be testable without Android UI.
- Compatibility: supported Android API levels and ABIs must be explicitly documented.

## 8. Acceptance gates

A usable keyboard release is not complete until:

- Telex and VNI pass a maintained test corpus.
- Key input works in ordinary text, email, URL, numeric, and password fields.
- Editor actions such as Search, Next, and Done are mapped appropriately.
- Selection changes and editor lifecycle events do not corrupt composing state.
- Unicode regression tests pass.
- No typed content is written to logs or transmitted.
- Physical Android device testing is complete.
- Known limitations are documented before a release is called stable.
