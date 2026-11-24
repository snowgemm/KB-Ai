# Product Specification

**Product:** KB-Ai  
**Platform:** Android first  
**Status:** Proposed specification; not implemented  
**Primary language:** Vietnamese  
**Secondary language:** English

## 1. Product summary

KB-Ai is an Android keyboard designed to combine accurate Vietnamese input, a familiar multi-tool keyboard experience, meaningful customization, and privacy-preserving defaults.

The product may study interaction patterns from Gboard, Laban Key, Microsoft SwiftKey, and Samsung Keyboard. It must develop its own visual identity and implementation. It must not copy proprietary assets, source code, private dictionaries, or branding.

The word "AI" in the project name is not a promise of cloud AI. The core keyboard must work without an account, network access, or remote text processing.

## 2. Target users and primary jobs

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

## 3. Product principles

| Principle | Requirement |
|---|---|
| Correctness before intelligence | Input transformation must be reliable before prediction is introduced. |
| Privacy by default | No typing history, personalization, or clipboard history by default. |
| Offline core | Basic typing must work without Internet access. |
| Responsive input | The synchronous key path must not perform disk or network I/O. |
| User control | Suggestions, learning, clipboard, and online tools must be independently controllable. |
| Familiar, not copied | Use understandable keyboard conventions with an original KB-Ai visual identity. |
| Progressive disclosure | Advanced tools belong in expandable panels, not permanently on the main key surface. |
| Accessibility | Keys and tool controls must expose accessible labels and actions. |

## 4. Scope

### 4.1 Core MVP

The first usable milestone must include:

- Android IME registration and activation flow.
- QWERTY letter layout.
- Vietnamese Telex and VNI input modes.
- English QWERTY mode.
- Language and input-mode switching.
- Optional number row.
- Number and symbol layouts.
- Space, punctuation, Backspace, Shift, and editor action keys.
- Composing text and commit behavior through InputConnection.
- Basic candidate/composition display; dictionary-based prediction is not required for the first integration milestone.
- Keyboard height setting with safe bounds.
- Light, dark, and system appearance.
- Correct handling of password and other sensitive input fields.
- Local-only settings and privacy controls.

### 4.2 Planned after the core is stable

- Dictionary packs and personal dictionary.
- Word suggestions and autocorrection.
- Clipboard history, explicitly opt-in.
- Emoji browser and search.
- Toolbar customization and multiple tool pages.
- Theme editor and local image backgrounds.
- One-handed and floating layouts.
- Cursor gestures and swipe-to-delete.
- Swipe typing.
- GIF and sticker insertion, where the target editor supports the required MIME types.
- Voice input, with the processing location disclosed.
- Handwriting and translation.

### 4.3 Explicit non-goals for the MVP

- Cloud AI rewriting or chat.
- Required account or sign-in.
- Cloud synchronization of typed content.
- Typing telemetry or analytics.
- Automatic clipboard monitoring.
- Remote dictionaries or network-dependent Vietnamese input.
- A desktop or iOS version.
- A promise of compatibility with every third-party editor.

## 5. Functional requirements

| ID | Requirement | Priority |
|---|---|---|
| FR-001 | The user can enable KB-Ai through Android system settings. | MVP |
| FR-002 | The user can select KB-Ai as the active IME. | MVP |
| FR-003 | The keyboard can enter and commit ordinary text. | MVP |
| FR-004 | The keyboard supports Vietnamese Telex. | MVP |
| FR-005 | The keyboard supports Vietnamese VNI. | MVP |
| FR-006 | The user can switch between Vietnamese and English. | MVP |
| FR-007 | The keyboard supports letter, number, and symbol layouts. | MVP |
| FR-008 | The keyboard responds to the editor's requested action key. | MVP |
| FR-009 | The keyboard handles composing text and selection updates safely. | MVP |
| FR-010 | Sensitive fields disable suggestions and learning. | MVP |
| FR-011 | The user can select Light, Dark, or System appearance. | MVP |
| FR-012 | The user can adjust keyboard height within supported bounds. | MVP |
| FR-013 | The user can preview the keyboard before completing setup. | MVP |
| FR-014 | The user can browse and search emoji. | Later |
| FR-015 | The user can opt in to local clipboard history. | Later |
| FR-016 | The user can create and save themes. | Later |
| FR-017 | The user can use online media or translation tools only through explicit actions. | Later |

## 6. Non-functional requirements

- **Reliability:** no lost or duplicated characters during ordinary typing.
- **Text integrity:** editing must not corrupt combining marks, surrogate pairs, or emoji sequences.
- **Performance:** no disk I/O, network I/O, dictionary loading, or unbounded work in the synchronous key path.
- **Lifecycle:** starting, finishing, or switching an editor must not leak composing state into the next editor.
- **Privacy:** typed text must not appear in logs, analytics, crash reports, or network payloads.
- **Accessibility:** keyboard keys and toolbar actions must expose meaningful semantics.
- **Maintainability:** input rules must be testable without Android UI.
- **Compatibility:** supported Android API levels and ABIs must be explicitly documented after the device baseline is selected.

## 7. Primary user flows

### First-time setup

1. Open KB-Ai.
2. Read a concise explanation of keyboard privacy.
3. Open Android's system screen to enable KB-Ai.
4. Open Android's system picker to select KB-Ai.
5. Open the test field and verify input.
6. Finish setup without requiring an account or network connection.

### Vietnamese typing

1. Focus a text field.
2. Select Vietnamese and Telex or VNI.
3. Enter a syllable.
4. See the current composing text.
5. Commit with Space, punctuation, or the editor action.
6. Continue typing without changing already committed text unexpectedly.

### Customization

1. Open the KB-Ai app.
2. Open Themes.
3. Preview a built-in theme.
4. Adjust supported properties.
5. Save and apply the theme.
6. Return to the keyboard and verify that the theme is applied.

## 8. Acceptance gates

The MVP is not complete until:

- Telex and VNI pass the maintained test corpus.
- Key input works in ordinary text, email, URL, numeric, and password fields.
- Editor actions such as Search, Next, and Done are mapped appropriately.
- Selection changes and editor lifecycle events do not corrupt composing state.
- Unicode regression tests pass.
- No typed content is written to logs or transmitted.
- A physical Android device test has been completed.
- Known limitations are documented before a release is called stable.
