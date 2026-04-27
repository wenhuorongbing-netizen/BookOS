# BookOS UI/UX Review

Date: 2026-04-26

## Scope

This review covers the current BookOS frontend in `frontend/` and compares it against the target "Book Knowledge Cockpit" experience for a game design reading system.

No framework, backend, API, or route change is recommended in this audit phase. The goal is to document the current UI state, identify gaps, and define the safest next refactor sequence.

## Current Frontend Implementation

### Framework And Build

- Framework: Vue 3 single-file components.
- Language: TypeScript.
- Build tool: Vite.
- Router: `vue-router` with history mode.
- State: Pinia, currently used for authentication.
- HTTP: Axios wrappers in `frontend/src/api/`.
- UI library: Element Plus, resolved through `unplugin-vue-components`.
- Styling: global CSS tokens in `frontend/src/style.css` plus scoped Vue component CSS.
- Design system: reusable primitives exist in `frontend/src/components/ui/`.

### Route Structure

- Public routes:
  - `/login`
  - `/register`
  - catch-all not-found route.
- Authenticated routes under `AppLayout.vue`:
  - `/dashboard`
  - `/my-library`
  - `/books/new`
  - `/books/:id/edit`
  - `/books/:id`
  - `/five-star`
  - `/currently-reading`
  - `/anti-library`

### Component Structure

- Layouts:
  - `PublicLayout.vue`
  - `AppLayout.vue`
  - `DashboardLayout.vue`
- Shell:
  - `Sidebar.vue`
  - `TopNav.vue`
- Book and library components:
  - `BookCard.vue`
  - `BookTable.vue`
  - `BookFilterBar.vue`
  - `BookFormPanel.vue`
  - `BookProgressBar.vue`
  - `BookRating.vue`
  - `BookStatusBadge.vue`
- Design system primitives:
  - `AppCard`
  - `AppButton`
  - `AppIconButton`
  - `AppBadge`
  - `AppProgressBar`
  - `AppStat`
  - `AppSectionHeader`
  - `AppEmptyState`
  - `AppLoadingState`
  - `AppErrorState`
  - `AppTooltip`
  - `AppDivider`

### Styling Approach

- `frontend/src/style.css` defines the current BookOS tokens:
  - warm ivory background
  - cream surfaces
  - muted beige surfaces
  - warm gray borders
  - ink and slate text colors
  - deep teal primary
  - warm orange accent
  - success, warning, danger, and info tones
  - serif book-title stack
  - UI sans stack
  - spacing, radius, shadow, focus, motion, and touch-target tokens
- Element Plus is themed through CSS variables and targeted global overrides.
- Legacy utility classes such as `surface-card`, `page-header`, `responsive-grid`, and `tag-chip` still exist and are used by several non-refactored pages.

## Overall Assessment

BookOS currently has a functional Milestone 1 book library with a partially refactored BookOS shell and design-system foundation.

The UI has moved closer to the reference direction through a warm ivory palette, soft cards, deep teal sidebar, top-bar search/book placeholders, visible focus ring, reduced-motion baseline, and reusable app primitives.

The app still does not yet read as a complete Book Knowledge Cockpit because the dashboard remains a library/status page. The major missing piece is the center cockpit: current book hero, resurfaced quote, daily prompt, ontology, graph preview, quick capture, recent notes, source rail, AI suggestions, and action items.

## Usability Review

### Is The Main User Workflow Obvious?

Partially.

The current workflow for Milestone 1 is clear: register, log in, add books, add them to the personal library, update status/progress/rating, and browse filtered shelves.

The target workflow is not yet obvious: read a current book, capture a thought, connect it to source material, inspect extracted knowledge, and apply it to a game design project. The current dashboard still emphasizes counts and catalog actions rather than reading-to-knowledge flow.

### Can The User Quickly Capture A Thought?

No.

There is no quick capture input in the top bar, dashboard, book detail page, or right rail. This is the highest-priority missing cockpit affordance.

### Can The User Open The Source Of A Quote Or Note?

No.

Books can be opened, but quotes and notes do not exist in the UI yet. Source reference patterns need to be introduced in the cockpit using frontend-local mock data until backend APIs exist.

### Is The Current Book Context Always Visible?

Partially.

The top bar now includes a "Current book" area and routes to `/currently-reading`, but it is still a placeholder: it does not select a real current book, show progress, or update dashboard context.

### Are Primary Actions Easy To Find?

For the book library, yes. `Add Book`, book open actions, library filters, and shelf routes are available.

For the cockpit target, no. The future primary actions are quick capture, open source, accept/edit/discard AI draft, complete action item, select lens, and switch current book. These actions are not implemented yet.

### Is There Too Much Visual Noise?

No. The current UI is calm and visually restrained.

The next cockpit phase needs to increase density without clutter. The risk is adding too many equal-weight cards. The cockpit should prioritize the current book hero, quick capture, and source rail.

### Is Navigation Predictable?

Mostly.

Existing implemented routes are predictable. The sidebar now includes the cockpit direction with active library routes and disabled planned knowledge modules. The disabled knowledge chips are honest placeholders, but they are not yet true navigation.

## Accessibility Review

### Keyboard Navigation

- Element Plus controls provide a good baseline.
- `AppLayout.vue` includes a skip link to `#main-content`.
- `main` is present and focusable.
- `Sidebar.vue` uses an Element Plus menu for implemented routes.
- `TopNav.vue` uses custom `RouterLink` slots to avoid nesting links around buttons.
- `AppCard` now supports keyboard activation when focusable.

Remaining issues:

- Disabled knowledge module chips are not keyboard reachable, which is acceptable for disabled placeholders but should become real routes or omitted when routes exist.
- Current book selector is a button but only routes to the reading shelf; it is not a real popover/listbox yet.
- Filter controls in `BookFilterBar.vue` still rely on placeholders instead of visible or `aria-label` labels.

### Visible Focus States

Improved.

`frontend/src/style.css` defines a global `:focus-visible` ring for links, buttons, inputs, textareas, selects, Element Plus buttons, inputs, and selects. This is aligned with the BookOS design language.

Remaining issue:

- Focus styling should be browser-tested in the full shell, tables, custom book cards, and future graph controls.

### Color Contrast

Generally good for main content.

Warm ivory surfaces use ink and muted slate text. Primary teal and accent orange are used as strong tokens. Some sidebar secondary text and placeholder chips use translucent white on deep teal and should be manually checked at small sizes before final polish.

### ARIA Labels

Improved.

`AppIconButton` requires `label`, the top-bar notification/profile buttons are labeled, global search has an accessible label, and progress bars expose `role="progressbar"`.

Remaining issues:

- Future graph, source-open, AI accept/edit/discard, favorite, and action item controls need explicit labels.
- `BookFilterBar.vue` controls need accessible labels beyond placeholders.

### Semantic Headings

Mostly acceptable, but inconsistent.

Many pages use `page-header` with `h1`. `DashboardView.vue` uses section components but no obvious current-book `h1` yet. The future cockpit should have one clear page-level heading and then structured section headings.

### Button Labels

Visible labels are mostly understandable. Icon-only buttons are now labeled through `AppIconButton`.

Remaining issues:

- `!` is used as the visible notification glyph. It is labeled for screen readers, but visually it is not a refined icon style.
- Future AI actions should use clear names such as "Accept suggestion", "Edit suggestion", and "Discard suggestion".

### Form Labels

Book forms use Element Plus form item labels.

Issue:

- `BookFilterBar.vue` uses placeholder-only search/category/tag controls. Replace with visible labels or `aria-label` attributes in a later library-filter pass.

### Touch Target Sizes

Improved.

Global `--touch-target: 44px` exists. App buttons, icon buttons, menu items, and top-bar book selector target at least 44px where practical.

Remaining issue:

- Legacy tag chips and some table row controls may still be below 44px.

### Reduced Motion Support

Implemented.

`frontend/src/style.css` includes a `prefers-reduced-motion: reduce` baseline.

### Screen Reader Friendliness

Improved but incomplete.

Loading states use polite live-region semantics. Error states use alert semantics. Progress exposes value information.

Remaining issues:

- Tables should get clearer context labels/captions.
- Future graph preview should provide a text alternative or list fallback.
- Future source, quote, note, and action item cards need source metadata that is readable without relying on visual position.

## Convenience Review

### Global Search

Partially present.

The top bar includes a readonly global search placeholder. It is visually positioned correctly but is not functional.

### Book Switcher

Partially present.

The top bar includes a current-book placeholder. It routes to `/currently-reading` but does not open a real selector or bind to a selected book.

### Quick Capture

Missing.

Add this in the dashboard cockpit before building deeper notes/AI pages.

### Sticky Source Reference

Missing.

The target needs a right rail with current book, page/location, quote/excerpt, and source action. This can be frontend-local in Phase 3.

### One-Click Accept/Edit/Discard AI Suggestion

Missing.

The design-system primitives can support these controls, but no AI suggestion surface exists yet.

### One-Click Action Item Completion

Missing.

No action item UI exists yet. Use `checked` state and accessible labels when implemented.

### Useful Empty States

Improved.

`AppEmptyState` exists and is used in `DashboardView.vue`. Some older pages still use simple text empty states.

### Loading States

Improved.

`AppLoadingState` exists and is used by the dashboard. Legacy table/page loading remains inconsistent.

### Error States

Improved.

`AppErrorState` exists and is used by the dashboard. Some auth/form errors still use Element Plus alerts, which is acceptable, but shelf pages need recoverable error states.

## Visual Design Review

### Layout Grid

Partial.

The app shell has a left sidebar and top bar. The center area still uses simple responsive grids. There is no right contextual sidebar yet, and the dashboard does not use the target cockpit grid.

### Spacing Scale

Improved.

Spacing tokens exist. Some legacy components still use local `rem` values and should gradually migrate to tokens.

### Card Style

Improved.

`AppCard` supports default, muted, highlight, interactive, and rail variants. `BookCard` and dashboard stats use the design system. Legacy wrappers still use `surface-card`.

### Typography

Improved.

Book-title serif token exists through `.book-title` and `--font-book-title`. Current dashboard does not yet have a large book hero where the serif voice can carry the page.

### Color Palette

Strong.

The palette now matches the intended warm ivory, soft cream, deep teal, warm orange, and muted academic surface direction.

### Icon Style

Weak.

There is no consistent icon family. Current placeholders use short text/kickers and the notification button uses `!`. The next UI phase should standardize icons, preferably through existing Element Plus icon capabilities or lightweight inline SVGs if needed.

### Data Density

Current density is moderate and library-oriented. Target density should be higher but grouped into zones: book hero, Today, Knowledge, Lenses, Capture, Notes, and right rail.

### Visual Hierarchy

Partial.

The shell now feels closer to BookOS, but `DashboardView.vue` still reads as a library operations dashboard. The current book should become the primary focal point.

### Responsive Behavior

Basic but acceptable.

The shell stacks at tablet/mobile widths. A true mobile cockpit navigation model is not implemented. The right rail behavior remains to be designed.

## Interaction Problems

- Global search is readonly.
- Current book selector is a route shortcut, not a selector.
- Notifications and profile buttons are labeled but not functional menus.
- Quick capture is missing.
- Source reference is missing.
- AI accept/edit/discard actions are missing.
- Action item completion is missing.
- Ontology and graph previews are missing.
- Existing library filters need better labels and state handling.
- Legacy empty/loading/error patterns remain in non-dashboard pages.

## Aesthetic Issues

- Dashboard content is still milestone/library oriented.
- The BookOS shell is stronger than the dashboard content, creating a mismatch.
- Sidebar placeholders lack icons and real module destinations.
- Top-bar notification/profile controls need refined iconography and menu behavior.
- No right rail exists, so the cockpit lacks the reference image's command-center shape.
- Legacy components still use old `surface-card` and `tag-chip` utilities in places.

## Current Pages And Components To Change First

1. `DashboardView.vue`: highest priority. Convert from stats/library dashboard into the Book Knowledge Cockpit.
2. `DashboardLayout.vue`: remove or simplify any wrapper style that conflicts with cockpit grid behavior.
3. New cockpit components, if needed: `BookHeroPanel`, `TodayQuoteCard`, `DailyPromptCard`, `KnowledgeGraphPreview`, `QuickCapturePanel`, `SourceReferenceRail`, `AiDraftCard`, and `ActionItemsCard`.
4. `TopNav.vue`: make global search and current book selector functional once data model exists.
5. `BookDetailView.vue`: evolve into the source context page for notes/quotes/page references.
6. `BookFilterBar.vue`: add accessible labels and design-system polish.
7. `BookTable.vue`: improve density, captions, loading, empty, and row-action accessibility.

## Priority Recommendations

1. Implement the dashboard cockpit using existing primitives and frontend-local mock data for missing backend modules.
2. Keep the library data connected to existing APIs.
3. Add the right rail as a semantic `aside` inside the dashboard content, not the global shell yet.
4. Add quick capture as a frontend-only interactive panel first; do not connect it to backend until capture APIs exist.
5. Standardize iconography after the cockpit structure is in place.
6. Continue migrating legacy `surface-card`, `tag-chip`, and placeholder-only form patterns gradually.
