# BookOS UI Accessibility Checklist

Use this checklist before marking a UI milestone complete.

## Keyboard

- Tab reaches sidebar menu toggle, navigation links, top search, current book selector, primary actions, right rail controls, and page content controls.
- Shift+Tab follows the same order in reverse without trapping focus.
- Enter and Space activate buttons, nav links, note cards, graph nodes, lens rows, and table/card actions.
- Escape closes open top-nav menus, sidebar mobile drawer, prompt modal, and confirmation dialogs.
- Focus rings are visible against ivory surfaces and dark teal sidebar surfaces.

## Screen Reader

- App shell exposes a skip link, navigation, header, main content, and contextual aside.
- Active navigation uses `aria-current="page"`.
- Collapsible panels and mobile drawer controls expose `aria-expanded`.
- Icon-only buttons have accessible names.
- Form fields do not rely on placeholders only.
- Progress bars expose label, minimum, maximum, current value, and text value.
- Graph previews have text alternatives and do not rely only on visual node placement.

## Visual Accessibility

- Body text, metadata, buttons, and badges target WCAG AA contrast where practical.
- Interactive targets are at least 44px where practical.
- Status is communicated through text labels, badges, shape, or checked state, not color alone.
- Reduced motion is respected through the global `prefers-reduced-motion` baseline.
- Text remains readable at mobile widths and does not require horizontal scrolling.

## Content Safety

- AI suggestions remain labeled as drafts.
- AI suggestions require explicit accept action and do not overwrite user content automatically.
- Source reference remains visible in the right rail or below main content on smaller screens.
- Private/user-generated content must not be exposed through public routes or unauthenticated APIs.

