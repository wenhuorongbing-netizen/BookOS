# BookOS Visual Target

## Product Intent

BookOS should feel like a premium knowledge cockpit for game design reading. It is not only a book catalog. It is a workspace where a reader can keep the current book in view, capture thoughts quickly, resurface useful quotes, inspect concepts, apply design lenses, and convert reading into design action.

The reference direction is an academic/productivity dashboard: warm, clear, serious, dense enough for real work, and calm enough for reading.

## Visual North Star

Warm, focused, quietly sophisticated.

The target screen should communicate:

- A current game design book is actively being studied.
- Notes, quotes, concepts, lenses, and tasks are connected.
- Source references remain visible.
- AI suggestions are draft assistance, not automatic overwrites.
- Reading turns into game design decisions and project actions.

## Target Page Anatomy

### Desktop Shell

Use a four-part cockpit layout:

1. Left sidebar navigation.
2. Top bar across the working area.
3. Main center knowledge workspace.
4. Right contextual sidebar.

Recommended proportions:

- Left sidebar: 264-288px.
- Top bar: full width of working area.
- Main center: fluid, minimum 720px where possible.
- Right rail: 320-360px.
- Page padding: 20-28px.
- Card gap: 16-24px.

### Left Sidebar

The sidebar should include:

- BookOS logo and mark.
- Primary route: Library or Cockpit.
- Knowledge routes: Notes, Quotes, Lenses, Diagnostics, Exercises.
- Work routes: Projects, Forum, Settings.
- Current project progress panel.
- Recently viewed concepts panel.

Visual direction:

- Deep teal or ink-teal background.
- Soft selected state with a visible pill or left indicator.
- Navigation labels with icons when icon system is introduced.
- Support panels should be compact, readable, and lower emphasis than primary nav.

### Top Bar

The top bar should include:

- Global search.
- Current book selector.
- Notifications.
- User profile or account menu.
- Optional compact quick capture affordance later.

Visual direction:

- Cream/white card surface.
- Aligned with the same radius and shadow language as cards.
- Search should be wide enough to feel useful.
- Current book selector should be visible on every authenticated route.
- Icon-only controls must have labels and 44px targets.

### Main Center Area

Recommended hierarchy:

1. Book hero section.
2. Today row: resurfaced quote and daily design prompt.
3. Knowledge row: ontology preview and knowledge graph preview.
4. Concepts/lenses row.
5. Quick capture input.
6. Recent note blocks.

The current book hero is the focal point. It should anchor the dashboard and make the page feel like a reading cockpit rather than a metrics page.

### Right Sidebar

The right sidebar should be contextual and sticky on desktop:

1. Source reference card.
2. AI draft suggestions card.
3. Extracted action items card.

On tablet and mobile, this rail should stack below the hero or become collapsible sections. It should be a `complementary` landmark or a semantic `aside`.

## Color Tokens

Use CSS variables in the existing styling approach.

Current implemented direction:

```css
:root {
  --bookos-bg: #f6f0e4;
  --bookos-bg-paper: #fbf7ee;
  --bookos-surface: #fffdf8;
  --bookos-surface-strong: #ffffff;
  --bookos-surface-muted: #f0e8da;
  --bookos-border: #ded2c0;
  --bookos-border-strong: #cdbda7;
  --bookos-text-primary: #1b211e;
  --bookos-text-secondary: #56615d;
  --bookos-text-tertiary: #7a827c;
  --bookos-primary: #1f5f57;
  --bookos-primary-hover: #184d47;
  --bookos-primary-soft: #dcebe7;
  --bookos-accent: #c56b2c;
  --bookos-accent-hover: #a95620;
  --bookos-accent-soft: #f4dfc9;
  --bookos-success: #3f7a4d;
  --bookos-warning: #a66f14;
  --bookos-danger: #a44935;
  --bookos-info: #2f6f84;
  --bookos-focus: #0f766e;
}
```

Usage guidance:

- Warm ivory is the page background.
- Cream/white surfaces are the default card surfaces.
- Muted beige is for inset cards, empty states, filters, and low-priority panels.
- Deep teal is for primary actions, selected navigation, progress, current source, and active context.
- Warm orange is for resurfaced quote accents, daily prompt accents, and limited attention cues.
- Danger red is reserved for destructive or failed states.
- Avoid adding purple or neon colors unless a future product requirement explicitly needs them.

## Typography

### Font Direction

- UI font: system sans stack, optimized for readable interface text.
- Book title display: refined system serif stack for book hero titles and prominent source titles.

Current implemented variables:

```css
--font-ui: "Segoe UI", "PingFang SC", "Noto Sans SC", Arial, sans-serif;
--font-book-title: Georgia, "Times New Roman", "Noto Serif SC", serif;
```

### Type Scale

- Page title: 32-45px, 1.05 line-height.
- Book hero title: 34-48px, serif, 1.0-1.08 line-height.
- Section title: 18-24px, 1.15-1.25 line-height.
- Card title: 15-17px, 600-800 weight.
- Body: 14-16px, 1.5-1.65 line-height.
- Metadata: 12-13px, muted.
- Micro label: 11-12px, uppercase only when meaningful.

Guidance:

- Use the serif stack sparingly. It should signal book/source identity.
- Use the sans stack for labels, metadata, buttons, forms, tables, and dense cockpit UI.
- Avoid excessive negative letter spacing.

## Spacing And Density

Current spacing tokens:

```css
--space-1: 4px;
--space-2: 8px;
--space-3: 12px;
--space-4: 16px;
--space-5: 20px;
--space-6: 24px;
--space-8: 32px;
--space-10: 40px;
--space-12: 48px;
```

Usage:

- Dense metadata rows: 4-8px.
- Button and chip groups: 8-12px.
- Card internal gap: 12-16px.
- Standard card padding: 16-24px.
- Hero card padding: 24-32px.
- Major dashboard gaps: 16-24px.

The cockpit should be dense but not cramped. Use grouping, section headers, and card hierarchy instead of shrinking everything.

## Surfaces And Cards

Card language should be soft and premium without becoming bulky.

Current implemented card tokens:

```css
--radius-sm: 8px;
--radius-md: 12px;
--radius-lg: 16px;
--radius-xl: 20px;
--shadow-card: 0 12px 32px rgba(54, 42, 24, 0.08);
--shadow-card-hover: 0 18px 44px rgba(54, 42, 24, 0.12);
```

Card variants:

- Default: cream/white surface, warm border, soft shadow.
- Muted: subtle beige background, minimal shadow.
- Highlight: soft teal/orange wash for high-priority context.
- Interactive: hover border/shadow and keyboard focus.
- Rail: compact right/sidebar panel.

## Main Cockpit Modules

### Book Hero

Should include:

- Cover with stable aspect ratio.
- Title in serif display style.
- Author.
- Favorite icon button.
- Reading progress.
- Page/location status.
- Last read date.
- Note count.
- Quote count.
- Lens count.

Visual direction:

- Primary focal card.
- Calm but substantial.
- Deep teal progress.
- Warm accent metadata.
- Avoid too many equal-weight badges.

### Today's Resurfaced Quote

Should include:

- Quote text.
- Book/chapter/page or location source.
- Open source action.
- Save or dismiss action.

Use warm orange as a restrained resurfacing accent.

### Daily Design Prompt

Should include:

- Prompt text.
- Related lens or project context.
- Start response or capture action.

The prompt should help the reader apply the current book to design work.

### Ontology Preview

Should include:

- Key concept relationships.
- Concept counts by type.
- Source-backed relation preview.
- Expand/open action.

Avoid graph clutter. Keep it readable.

### Knowledge Graph Preview

Should include:

- Simple nodes and links.
- Current book or current concept highlighted.
- A text fallback or list summary.
- Expand action.

Do not rely only on color to communicate selection.

### Extracted Concepts List

Should include:

- Concept name.
- Type/category.
- Source count.
- Review state or confidence where relevant.
- One-click open.

### Active Design Lenses

Should include:

- Active lens names.
- Purpose or prompt focus.
- Selected state.
- Apply/change action.

### Quick Capture

Should include:

- Textarea or command-like input.
- Capture type selector: note, quote, task, question, concept.
- Current source chip.
- Submit action.
- Confirmation state.

This is a primary workflow, not a secondary widget.

### Recent Note Blocks

Should include:

- Block type.
- Short content preview.
- Source link.
- Last edited metadata.
- Quick actions.

### Source Reference Rail

Should include:

- Current book title.
- Page/chapter/location.
- Excerpt or quote.
- Open source action.
- Last selected note/concept context.

### AI Draft Suggestions

Should include:

- Draft suggestion text.
- Accept, edit, discard actions.
- Loading and error states.
- Clear "draft" status.

AI output must never overwrite user content automatically.

### Action Items

Should include:

- Checkable items.
- Source context.
- Complete/defer/open actions.
- Non-color-only completed state.

## Icon Style

Preferred direction:

- Use one consistent line-icon style.
- Prefer Element Plus icons if available and light enough for the app.
- Use 16-20px icons for navigation and metadata.
- Use 20-24px glyphs inside 44px icon buttons.
- Icons should support labels, not replace important text.
- Avoid mixing emoji-like symbols, filled icons, and outline icons in the same surface.

Current gap:

- The shell has text labels and short kickers but no refined icon system yet.

## Interaction States

Reusable components should support:

- Hover.
- Active.
- Disabled.
- Focus-visible.
- Loading.
- Selected.
- Checked.
- Expanded/collapsed.

Do not rely only on color. Use text, icons, shape, border, or check indicators.

## Accessibility Target

- Visible focus ring on every interactive element.
- WCAG AA target for text and controls.
- Accessible names for buttons and icon buttons.
- Labels for search, filter, capture, selector, and form fields.
- Touch targets at least 44px where practical.
- Reduced motion support.
- Semantic landmarks for sidebar, header, main, and right rail.
- Screen-reader-friendly progress, status, loading, and error states.
- Text fallback for graph-like visuals.

## Responsive Target

### Desktop

Full cockpit:

- Left nav.
- Top bar.
- Main grid.
- Sticky right rail.

### Tablet

- Sidebar can become a stacked top section, compact rail, or drawer.
- Right rail stacks under the primary hero and Today cards.
- Preserve search and current book context.

### Mobile

Single-column reading cockpit:

- Compact top current-book context.
- Search and capture quickly reachable.
- Navigation becomes drawer, compact rail, or bottom pattern.
- Cards stack in priority order.
- Graph becomes a simplified list or expandable panel.

## What To Preserve

- Vue 3, Vite, TypeScript, Pinia, Vue Router, Element Plus.
- Existing backend-connected library workflows.
- Existing authenticated routes.
- Warm paper-like atmosphere.
- Deep teal primary direction.
- Cream soft-card language.
- Design-system primitives under `frontend/src/components/ui/`.

## What To Change Next

- Replace `DashboardView.vue` stats-first content with the cockpit dashboard.
- Introduce right rail inside dashboard content.
- Add quick capture panel.
- Add current book hero.
- Add source-backed mock cards for quote/note/concept modules until backend APIs exist.
- Standardize icons.
- Continue migrating legacy `surface-card` and `tag-chip` usage.
