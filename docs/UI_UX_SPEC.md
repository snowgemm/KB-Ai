# UI/UX Specification

**Status:** Proposed  
**Reference direction:** Familiar multi-tool Android keyboard patterns with an original KB-Ai identity.

## 1. Experience goals

- Make Vietnamese typing the primary experience.
- Keep the typing surface visually quiet and predictable.
- Make common tools reachable without showing every tool at once.
- Make customization discoverable without making initial setup complicated.
- Maintain legibility across themes, images, screen sizes, and orientations.
- Avoid decorative complexity, gradients, and unnecessary animation.

## 2. Keyboard surface

### Default letter layout

The default keyboard contains:

1. A compact utility strip or candidate row.
2. An optional number row.
3. Three QWERTY letter rows.
4. Shift and Backspace controls.
5. A bottom row with symbol switch, punctuation, language/IME switch where appropriate, space, and the editor action key.

The number row is optional. Key labels, secondary characters, and long-press behavior must be consistent across layouts.

### Candidate and toolbar behavior

- Show composing text and candidates when they are useful.
- Do not show an empty candidate row that consumes height.
- Keep a compact toolbar entry point available.
- The expanded tool panel must not permanently occupy the typing surface.
- Candidate results must be associated with the active editor session and revision.

### Number and symbol layouts

Provide a dedicated numeric layout where the editor requests numeric input. Provide separate symbol pages for general text input. The final symbol map must be tested against Vietnamese, English, URL, email, and numeric editor types.

## 3. Expanded toolbar

The toolbar opens a paged tool panel. Users can later pin, hide, and reorder supported tools.

Planned tools include:

| Tool | Availability |
|---|---|
| Theme shortcut | Core settings navigation |
| Clipboard | Later; history requires explicit opt-in |
| Emoji | Later |
| GIF | Later; requires compatible editor content support |
| Stickers | Later; requires compatible editor content support |
| Text editing | Later |
| Text shortcuts | Later |
| One-handed layout | Later |
| Floating layout | Later |
| Voice input | Later; processing location must be disclosed |
| Translation | Later; online/offline behavior must be explicit |
| Handwriting | Later |

Tools must show a clear unavailable or unsupported state rather than silently failing.

## 4. Emoji, GIF, and sticker panel

The planned content panel includes:

- Search.
- Recent and frequently used categories, with storage behavior disclosed.
- Emoji categories.
- GIF and sticker tabs.
- A clear way to return to the letter keyboard.

GIF and sticker insertion must check whether the target editor advertises support for the relevant MIME type. A keyboard must not promise that rich content can be inserted into every application.

## 5. Companion application navigation

The settings application should use a small number of clear destinations:

| Destination | Purpose |
|---|---|
| Home | Setup status, preview, test typing |
| Themes | Built-in themes and theme editor |
| Languages | Installed languages and input methods |
| Tools | Tool availability and configuration |
| Settings | Layout, sound, vibration, and behavior |
| Privacy | Data controls and privacy explanation |

The home screen should answer two questions immediately: whether the keyboard is enabled and how to test it.

## 6. Theme system

Planned theme properties:

- Light, dark, and follow-system modes.
- Key background and foreground colors.
- Key shape and corner radius.
- Borders and secondary-character styling.
- Keyboard height and spacing within safe limits.
- Optional local image background.
- An overlay that preserves text and key contrast.
- Preview before applying.

A user-selected image must be accessed through Android's system photo picker where supported. Do not request broad photo-library access merely to set a keyboard background.

No theme may reduce key-label contrast below the accessibility target selected during implementation.

## 7. Interaction and gestures

Planned interactions:

| Interaction | Behavior |
|---|---|
| Tap | Enter the key's primary character |
| Long press | Show a secondary character or key alternatives |
| Spacebar gesture | Cursor movement, after gesture conflicts are tested |
| Backspace gesture | Optional accelerated deletion |
| Toolbar tap | Open tools |
| Horizontal panel swipe | Change tool page |
| Long-press tool | Pin or rearrange, when customization is implemented |
| ABC / layout switch | Return to the letter layout |

Gestures must be configurable and must not conflict with swipe typing, accessibility services, or system navigation.

## 8. Responsive layout

- Support portrait and landscape layouts.
- Recalculate key geometry when the available window size changes.
- Avoid fixed screen-width assumptions.
- Keep action keys reachable.
- Support adjustable keyboard height with safe minimum and maximum values.
- Test split-screen and resizable windows where the device supports them.

## 9. Accessibility

- Expose each key as an accessible control with a meaningful label.
- Provide sufficient touch targets and visible pressed/focused states.
- Do not rely on color alone to convey state.
- Support font scaling where it does not break the key layout.
- Verify keyboard navigation and TalkBack behavior.
- Respect system animation settings.

## 10. Visual direction

Use a clean, compact, high-contrast design. The product should have its own identity rather than copying another keyboard's exact layout or artwork.

Avoid gradients, excessive blur, unnecessary glow, and heavy animations. Prefer subtle state transitions and stable key geometry.
