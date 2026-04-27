# BookOS Responsive Behavior

BookOS uses a three-zone cockpit on desktop and progressively collapses into a single-column reading workflow on smaller screens.

## Breakpoints

- Desktop cockpit: above 1280px.
- Tablet shell: 981px to 1280px.
- Compact tablet and mobile shell: 721px to 980px.
- Mobile: 720px and below.
- Narrow mobile: 480px and below.

## Desktop

- Left sidebar is 240px and sticky.
- Top bar is sticky inside the workspace.
- Center workspace uses the remaining width.
- Right rail is 320px to 360px and sticky.
- Cards use consistent soft surfaces, borders, and radius.

## Tablet

- Main app body collapses to one column.
- Right rail moves below the center workspace and uses auto-fit cards when space allows.
- Sidebar becomes a collapsible disclosure at the top of the app.
- Top search remains visible and keeps the `Ctrl / Cmd K` hint.
- Book detail hero, insight cards, knowledge graph, capture, and metadata sections stack vertically.

## Mobile

- Sidebar becomes a compact top drawer controlled by the Menu button.
- Top nav stacks title, search, current book selector, and actions.
- Tables switch to card layout below 760px.
- Quick Capture remains a full-width card with wrapped marker controls and full-width submit.
- Recent note blocks, right rail cards, graph panels, and library cards stack without horizontal page overflow.
- Global body overflow is clipped to prevent accidental sideways scrolling from wide controls.

## Implementation Rules

- Prefer grid/flex wrapping over fixed pixel widths.
- Use `minmax(0, 1fr)` in grid columns that contain long book titles or controls.
- Keep right rail content available below the main content instead of hiding it.
- Keep source anchors compatible with sticky header offset using global `:target` scroll margin.

