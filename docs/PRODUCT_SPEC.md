# Product Specification

**Product:** KB-Ai  
**Platform:** Android first  
**Status:** Phase 1 functional QWERTY prototype; production keyboard features remain in progress  
**Primary language:** Vietnamese  
**Secondary language:** English

## 1. Product summary

KB-Ai is an independent Android keyboard designed to combine accurate Vietnamese input, a familiar multi-tool keyboard experience, meaningful customization, and privacy-preserving defaults.

The product may study interaction patterns found in Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. It must develop its own visual identity and implementation. It must not copy proprietary assets, source code, private dictionaries, or branding.

The word "AI" in the project name is not a promise of cloud AI. The core keyboard must work without an account, network access, or remote text processing.

## 2. Current status

The repository currently contains a Phase 1 functional QWERTY prototype:

- Android app with system IME setup shortcuts.
- QWERTY alphabetic layout.
- Shift, Backspace, Space, punctuation, and a symbol layout.
- Numeric and phone layouts selected from editor input type.
- Editor action labels and actions.
- Test fields for text, email, URL, number, password, and multiline input.
- Kotlin-to-Rust JNI status diagnostic.
- Basic password and no-personalized-learning editor policies.

This is not yet a production-ready keyboard. It does not implement Telex, VNI, composing text, dictionaries, predictions, clipboard history, emoji, or AI text tools. CI and physical-device verification are still required.

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
| Accessibility | Keys and toolbar actions must expose accessible labels and actions. |

## 5. Planned scope

### 5.1 Functional QWERTY prototype

- Android IME registration and activation flow.
- QWERTY letter layout.
- Shift and one-shot uppercase behavior.
- Symbol layout.
- Numeric layout for number/date-time editors.
- Phone layout for phone editors.
- Space, punctuation, Backspace, and editor action key.
- Email and URL punctuation adjustments.
- Test fields for common editor types.
- Basic password and no-personalized-learning policy checks.

### 5.2 Core keyboard milestone still required

- Vietnamese Telex and VNI input modes.
- English and Vietnamese input-mode switching with correct state handling.
- Composing text and candidate display.
- Selection-aware editing and cursor controls.
- Long-press alternatives.
- Optional number row.
- Keyboard height and appearance settings.
- Robust Unicode and grapheme-aware deletion.
- Correct handling of sensitive input fields across more editor variations.

### 5.3 Planned after the core is stable

- Dictionary packs and personal dictionary.
- Word suggestions and autocorrection.
- Clipboard history, explicitly opt-in.
- Emoji browser and search.
- Toolbar customization and multiple tool pages.
- Theme editor and local image backgrounds.
- One-handed and floating layouts.
- Cursor gestures and swipe-to-delete.
- Swipe typing.
- GIF and sticker insertion where the target editor supports the required MIME types.
- Voice input, with processing location disclosed.
- Handwriting and translation.

### 5.4 Explicit non-goals for the first stable keyboard release

- Cloud AI rewriting or chat.
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
- Privacy: typed text must not appear in logs, analytics, crash reports, or network payloads.
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
