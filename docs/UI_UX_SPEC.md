# UI/UX Specification

**Status:** Core keyboard layout implemented as a prototype; advanced surfaces remain proposed.  
**Reference direction:** Familiar multi-tool Android keyboard patterns with an original KB-Ai identity.

## 1. Experience goals

- Make Vietnamese typing the primary experience.
- Keep the typing surface visually quiet and predictable.
- Make common tools reachable without showing every tool at once.
- Make customization discoverable without making initial setup complicated.
- Maintain legibility across themes, screen sizes, and orientations.
- Avoid decorative complexity, gradients, and unnecessary animation.

## 2. Keyboard surface

### Default letter layout

The current prototype contains:

1. Three QWERTY letter rows.
2. Shift and Backspace controls.
3. A bottom row with symbol switch, punctuation, VI/EN toggle where supported, space, and the editor action key.

The optional number row and long-press alternatives are not implemented yet. Key labels and touch behavior must remain consistent across layouts.

### Vietnamese and English modes

- Eligible text fields start in Vietnamese mode.
- Telex and VNI are supported by the combined Rust input engine.
- The VI/EN key switches the active engine mode.
- English mode bypasses Vietnamese transformations.
- Switching modes finalizes the current composition before changing modes.
- Password, email, URL, number, and phone fields do not create a Vietnamese composition session.

### Composing text

- Show the active preedit in the focused editor using Android's composing-text API.
- Space and punctuation finalize the current preedit before inserting the boundary character.
- Backspace inside an active composition undoes the last physical keystroke.
- Backspace outside the active composition is delegated to the Android editor.
- If the cursor or selection moves away from the composition end, finalize the composition rather than editing at a stale position.

### Candidate and toolbar behavior

- Candidate suggestions are not implemented yet.
- Do not show an empty candidate row that consumes height.
- Keep a compact toolbar entry point available when tools are implemented.
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
| ABC / layout switch | Return to the letter keyboard |

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
