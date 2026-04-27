# BookOS UI Refactor Plan

## Refactor Principles

- Do not change the frontend framework.
- Do not change the backend.
- Preserve existing routes and working library flows.
- Theme and wrap Element Plus instead of fighting it.
- Build in phases.
- Use frontend-local mock data only where backend APIs do not exist.
- AI suggestions must remain drafts until accepted.
- Every future knowledge object must preserve source references.
- Improve accessibility as part of each component, not as a final pass.

## Current Stack To Preserve

- Vue 3 SFCs.
- Vite.
- TypeScript.
- Vue Router.
- Pinia.
- Axios API wrappers.
- Element Plus.
- Global CSS variables plus scoped component CSS.

## Current Status

### Completed Foundation

- Centralized design tokens in `frontend/src/style.css`.
- Warm ivory, cream, teal, orange, semantic status colors.
- UI sans and book-title serif stacks.
- Spacing, radius, shadow, focus, motion, and touch-target tokens.
- Element Plus input/select/table theming.
- Visible focus ring.
- Reduced motion baseline.
- Skip link in `AppLayout.vue`.
- Semantic app shell landmarks.
- Reusable design-system primitives in `frontend/src/components/ui/`.

### Completed Shell Direction

- `Sidebar.vue` uses a deep teal cockpit sidebar.
- Implemented route links remain active.
- Planned knowledge modules appear as disabled placeholders.
- Project focus and recent concepts panels exist as empty states.
- `TopNav.vue` includes global search placeholder.
- `TopNav.vue` includes current book selector placeholder.
- Notification/profile icon buttons have accessible labels.

### Still Missing

- Actual Book Knowledge Cockpit dashboard.
- Current book hero.
- Quick capture.
- Resurfaced quote.
- Daily design prompt.
- Ontology preview.
- Knowledge graph preview.
- Extracted concepts list.
- Active design lenses.
- Recent note blocks.
- Right source reference rail.
- AI suggestion actions.
- Action item completion.
- Real global search.
- Real current book selector.
- Notes, quotes, lenses, diagnostics, exercises, projects, forum, and settings routes.

## Design System Layer

The following components exist and should continue to be used:

1. `AppCard`
2. `AppButton`
3. `AppIconButton`
4. `AppBadge`
5. `AppProgressBar`
6. `AppStat`
7. `AppSectionHeader`
8. `AppEmptyState`
9. `AppLoadingState`
10. `AppErrorState`
11. `AppTooltip`
12. `AppDivider`

### Usage Rules

- Use `AppCard` for cockpit cards, rail panels, source cards, and interactive concept cards.
- Use `AppButton` for all app-level actions unless a raw Element Plus button is required by a complex Element Plus component.
- Use `AppIconButton` for icon-only actions and always provide `label`.
- Use `AppBadge` for statuses, tags, concept types, lenses, and review states.
- Use `AppProgressBar` for reading/project progress.
- Use `AppEmptyState`, `AppLoadingState`, and `AppErrorState` for stable async surfaces.
- Keep Element Plus for forms, selects, inputs, tables, ratings, and complex controls.

## Component Refactor Plan

### AppCard

Current state:

- Exists with `default`, `muted`, `highlight`, `interactive`, and `rail` variants.
- Supports focusable/interactive keyboard activation, selected state, disabled state, and safer ARIA handling.

Next usage:

- Dashboard cockpit modules.
- Book hero.
- Source reference rail.
- AI suggestions.
- Action items.

### AppButton

Current state:

- Exists with primary, secondary, accent, ghost, danger, subtle, and text variants.
- Supports loading, disabled, selected, checked, and expanded ARIA states.

Next usage:

- Quick capture submit.
- Open source.
- Accept/edit/discard AI draft.
- Complete/defer action item.

### AppIconButton

Current state:

- Exists and requires accessible `label`.
- Supports tooltip, selected, checked, expanded, disabled, and loading state.

Next usage:

- Favorite book.
- Notifications.
- Profile menu trigger.
- Source open.
- Graph expand.
- AI draft actions.

### AppBadge

Current state:

- Exists with neutral, primary, accent, success, warning, danger, and info variants.
- Supports selected/checked state without relying only on color.

Next usage:

- Concept type.
- Lens chip.
- AI draft status.
- Source count.
- Review status.

### AppProgressBar

Current state:

- Exists with progress semantics and themed tones.

Next usage:

- Book hero reading progress.
- Sidebar project progress.
- Exercise/prototype completion.

### AppStat

Current state:

- Used for dashboard library stats.

Next usage:

- Book hero stats: notes, quotes, lenses, last read, current page.

### AppSectionHeader

Current state:

- Used by dashboard sections.

Next usage:

- Today.
- Knowledge.
- Concepts.
- Active Lenses.
- Recent Notes.
- Source Rail sections.

### AppEmptyState

Current state:

- Exists and used by dashboard.

Next usage:

- Notes empty.
- Quotes empty.
- Concepts empty.
- Search empty.
- AI draft empty.

### AppLoadingState

Current state:

- Exists with polite live-region semantics.

Next usage:

- Cockpit cards that fetch or generate data.
- Future AI draft loading state.

### AppErrorState

Current state:

- Exists with alert semantics and optional retry action.

Next usage:

- Dashboard data failure.
- Source loading failure.
- AI suggestion failure.

### AppTooltip

Current state:

- Wraps Element Plus tooltip with consistent delays.

Next usage:

- Icon buttons.
- Metadata abbreviations.
- Graph controls.

### AppDivider

Current state:

- Exists.

Next usage:

- Right rail panels.
- Book hero metadata groups.
- Source card sections.

## Page Refactor Order

### Phase 1: Foundation

Status: complete.

Files:

- `frontend/src/style.css`
- `frontend/src/components/ui/*`

### Phase 2: App Shell

Status: mostly complete as placeholder shell.

Files:

- `frontend/src/layouts/AppLayout.vue`
- `frontend/src/components/Sidebar.vue`
- `frontend/src/components/TopNav.vue`

Remaining shell follow-ups:

- Replace text/kicker placeholders with a consistent icon system.
- Convert current book placeholder into a real selector when data model is ready.
- Convert global search placeholder into real search when APIs/local index exist.
- Add profile/notification menu behavior when scope allows.

### Phase 3: Dashboard Cockpit

Status: next priority.

Files:

- `frontend/src/views/DashboardView.vue`
- `frontend/src/layouts/DashboardLayout.vue`
- optional `frontend/src/components/cockpit/*`

Goals:

- Replace stats-first dashboard with cockpit grid.
- Keep existing library API data for books and current reading.
- Add frontend-local mock data for notes, quotes, lenses, concepts, source reference, AI suggestions, and action items.
- Add current book hero.
- Add Today row.
- Add Knowledge row.
- Add Concepts/Lenses row.
- Add Quick Capture panel.
- Add Recent Notes section.
- Add right contextual rail.

Recommended component split:

- `CockpitBookHero.vue`
- `CockpitTodayQuote.vue`
- `CockpitDailyPrompt.vue`
- `CockpitOntologyPreview.vue`
- `CockpitGraphPreview.vue`
- `CockpitConceptList.vue`
- `CockpitLensPanel.vue`
- `CockpitQuickCapture.vue`
- `CockpitRecentNotes.vue`
- `CockpitSourceRail.vue`
- `CockpitAiDrafts.vue`
- `CockpitActionItems.vue`

Use this split only if it keeps files understandable. A single first-pass `DashboardView.vue` implementation is acceptable if the code remains readable.

### Phase 4: Book Detail And Source Context

Files:

- `frontend/src/views/BookDetailView.vue`
- `frontend/src/components/BookProgressBar.vue`
- `frontend/src/components/BookStatusBadge.vue`
- `frontend/src/components/BookCard.vue`

Goals:

- Make book detail the source context page.
- Show reading status, page/location, progress, notes, quotes, concepts, and source actions.
- Preserve edit/add/status/progress/rating functionality.

### Phase 5: Library Tables And Filters

Files:

- `frontend/src/views/MyLibraryView.vue`
- `frontend/src/components/BookTable.vue`
- `frontend/src/components/BookFilterBar.vue`

Goals:

- Add visible or accessible labels to all filters.
- Standardize empty/loading/error states.
- Improve table density and row actions.
- Add table captions or equivalent context.
- Avoid ambiguous action labels.

### Phase 6: Real Knowledge Routes

Files:

- Router additions only when the user explicitly approves the route expansion.
- Future views under `frontend/src/views/`.

Goals:

- Notes.
- Quotes.
- Lenses.
- Diagnostics.
- Exercises.
- Projects.
- Forum.
- Settings.

## First Pages And Components To Change

1. `frontend/src/views/DashboardView.vue`
2. `frontend/src/layouts/DashboardLayout.vue`
3. `frontend/src/components/cockpit/*` if cockpit components are split out
4. `frontend/src/views/BookDetailView.vue`
5. `frontend/src/components/BookFilterBar.vue`
6. `frontend/src/components/BookTable.vue`
7. `frontend/src/components/TopNav.vue`
8. `frontend/src/components/Sidebar.vue`

## Accessibility Refactor Checklist

Already done:

- Skip link.
- `header`, `nav`, `main`, and `aside` shell landmarks.
- Visible global focus ring.
- Reduced motion baseline.
- Icon-button accessible names through `AppIconButton`.
- Progress semantics through `AppProgressBar`.
- Loading and error live-region semantics.
- Touch target token and 44px app buttons/icon buttons.

Still needed:

- Ensure one page-level `h1` per routed view.
- Add labels to `BookFilterBar` controls.
- Add labels to quick capture fields.
- Add right rail as semantic `aside`.
- Add text fallback for graph preview.
- Add table captions/context.
- Verify sidebar contrast manually.
- Ensure AI/action/source controls have explicit labels.
- Ensure completed/selected states are not color-only.

## Usability Refactor Checklist

Already partial:

- Current book context placeholder is always visible.
- Global search placeholder is always visible.
- Empty/loading/error primitives exist.

Still needed:

- Real current book selector.
- Real global search.
- Dashboard quick capture.
- Sticky source reference.
- Open source action on quote/note/action cards.
- AI suggestion accept/edit/discard.
- Action item completion.
- Daily prompt response/capture action.
- Useful mobile cockpit ordering.

## Interaction Model For Target Cockpit

### Current Book Selector

- Opens a popover or dropdown.
- Lists current library books.
- Supports keyboard navigation.
- Selecting a book updates dashboard context.
- Does not change backend data unless the user explicitly saves preference later.

### Quick Capture

- User types a thought.
- User selects type: note, quote, task, question, concept.
- Source chip defaults to current book/page context.
- Submit creates a local pending item in the first cockpit phase.
- Later backend integration persists the item.

### Source Reference

- Source rail follows selected quote/note/concept.
- Open source routes to book detail until a dedicated reader/source route exists.
- Page/location remains visible.

### AI Draft Suggestions

- Suggestions are clearly marked as drafts.
- Accept applies suggestion to local state.
- Edit opens inline editor.
- Discard removes or archives suggestion.
- Loading and error states are explicit.

### Action Items

- Complete toggles checked state.
- Open source reveals origin.
- Defer can be added later.
- Completed state uses text/icon plus visual change.

## Risk Notes

- Backend currently supports books and user library workflows, not full notes/quotes/lenses/AI/source graph functionality.
- Phase 3 should use frontend-local mock data for unavailable modules and keep that mock data isolated.
- Do not add backend assumptions to frontend API clients before endpoints exist.
- Do not make AI suggestions overwrite user content.
- Do not add public routes that expose private notes.

## Acceptance Criteria For Next Implementation Phase

- Existing routes still render.
- Existing book library operations still work.
- Dashboard visually reads as a Book Knowledge Cockpit.
- Current book hero is primary.
- Quick capture is visible and keyboard usable.
- Right source rail exists on desktop and stacks on smaller screens.
- AI and action item cards show draft/local interaction patterns only.
- Source references are visible on quote/note/action mock data.
- No backend behavior is changed.
