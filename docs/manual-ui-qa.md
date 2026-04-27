# BookOS Manual UI QA

Run these checks after `npm run typecheck` and `npm run build`.

## Viewports

- Desktop: 1440 x 900.
- Small desktop: 1280 x 800.
- Tablet: 1024 x 768.
- Mobile: 390 x 844.
- Narrow mobile: 360 x 740.

## Shell Checks

- Open Dashboard, My Library, Currently Reading, Five-Star Books, and a Book Detail page.
- Confirm the left sidebar, top bar, center content, and right rail do not overlap.
- Confirm no horizontal page overflow at every viewport.
- Confirm the sidebar Menu opens on tablet/mobile and Escape closes it.
- Confirm the skip link appears on focus and jumps to main content.

## Keyboard Checks

- Use Tab from the browser address bar through the app shell.
- Confirm focus order: sidebar, top bar search, current book, actions, main content, right rail.
- Open the current book selector, profile menu, prompt modal, and AI draft confirmation if available.
- Press Escape and confirm the open surface closes.
- Activate note blocks, page badges, graph nodes, and lens rows with keyboard.

## Book Detail Checks

- Confirm the book hero identifies title, author, progress, status, and stats.
- Confirm insight cards stack on mobile.
- Confirm graph preview and concept/lens panels remain readable.
- Type into Quick Capture and submit with Ctrl+Enter or Cmd+Enter.
- Confirm the recent note block appears, the source rail updates, and page/tag chips are reachable.
- Type a draft and attempt navigation; confirm the unsaved draft warning appears.

## Library Checks

- Confirm filters have keyboard-accessible fields and labels.
- Confirm desktop uses a table and mobile uses cards.
- Update status, progress, and rating from desktop and mobile layouts.
- Confirm empty states are understandable when filters return no rows.

## Visual Checks

- Confirm focus rings are visible on ivory cards and dark sidebar.
- Confirm touch targets are practical on mobile.
- Confirm badges include text labels and are not color-only.
- Confirm loading states, empty states, and error states are visible where implemented.

## Final Book Detail Visual Polish Check

- At 1440 x 900, confirm the first screen reads as a three-zone cockpit: teal sidebar, sticky search/book context top bar, main book dashboard, and right source rail.
- Confirm the book title is the dominant page anchor and the cover/title/progress cluster is visually stronger than secondary cards.
- Confirm the three insight cards sit directly under the hero and have equal visual weight without crowding.
- Confirm the graph/concept/lenses section reads as one middle knowledge layer: graph and concepts side by side on desktop, active lenses integrated below unless the viewport is very wide.
- Confirm Source Reference, AI Draft Suggestions, and Action Items Extracted remain visible in the right rail or below main content on smaller widths.
- Confirm Quick Capture is easy to find after the knowledge layer and the submit action is visually primary.
- Confirm Recent Note Blocks empty state is useful and does not look like missing content.
- Confirm seeded preview states are clearly labeled and do not look like user-authored production data.
