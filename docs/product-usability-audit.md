# BookOS Product Usability Audit

Last reviewed: 2026-04-30.

Reviewed branch: `main`.

Reviewed SHA: `3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`.

## Audit Goal

BookOS is functionally broad enough for Public Beta 0.1, but the product surface now asks a new user to understand too many modules at once. The core product value is not "many pages." The core value is:

Register -> add a book -> read -> capture -> parse -> convert -> preserve source -> review -> apply to a game project.

This audit identifies where the UI is task-first, where it is module-first, and how to reduce cognitive overload without deleting existing capabilities.

## Classification Scale

| Classification | Meaning |
| --- | --- |
| Core first-day flow | Should be visible and guided immediately for a new user. |
| Core weekly flow | Important after the user has data; should remain discoverable but not dominate onboarding. |
| Advanced workflow | Powerful, but should be under More, an advanced mode, or contextual entry points. |
| Admin-only | Should be hidden from non-admin users and not part of normal product comprehension. |
| Optional / hidden until data exists | Useful only after relevant records exist. |
| Needs redesign | Current concept, labeling, or page structure is too abstract for practical use. |
| Should be moved under More | Implemented and useful, but not primary navigation. |

## Executive Findings

1. The sidebar is module-first, not use-case-first. It lists Library, Notes, Captures, Quotes, Action Items, Concepts, Knowledge, Daily, Analytics, Review, Mastery, Import/Export, Projects, Graph, Forum, Search, and disabled future modules as peers. This makes every feature look equally urgent.
2. Dashboard explains the cockpit idea, but the first action is split across Add Book, Open Library, daily cards, stats, and current reading. A new user needs one guided next step, not several equivalent widgets.
3. Book Detail is the strongest product surface. It already connects reading progress, daily insights, graph/concepts, quick capture, recent notes, right rail, source references, action items, and AI drafts. It should become the main hands-on cockpit rather than making users roam through separate modules.
4. Capture Inbox is central to the product loop, but it is currently named like a storage bucket. It should be framed as "Process Captures" or "Review Captures" because the job is conversion and cleanup.
5. Concepts, Knowledge, Graph, Analytics, Mastery, and Forum are useful after records exist, but they create early cognitive load when shown as primary peers.
6. Projects is core to the game design promise, but users need contextual "Apply to Project" flows before they need a full project subsystem.
7. AI is correctly draft-only, but the Right Rail should keep AI behind explicit source context and not compete with the core source/action panels.
8. Empty states mostly exist, but several pages still explain what the module is better than what the user should do next.

## Page-by-Page Audit

| Area | Classification | User Job | Task-First or Module-First | Main Action Obvious? | New-User Overload | Recommendation |
| --- | --- | --- | --- | --- | --- | --- |
| Sidebar navigation | Needs redesign | Choose where to work next. | Module-first. | Partly; too many equal links. | High. | Split Primary, Secondary, More; remove disabled future modules from main nav. |
| Dashboard | Core first-day flow | Understand what to do today. | Mixed. | Partly. | Medium. | Convert into guided "Start reading workflow" hub with one next action. |
| Library | Core first-day flow | Add/open books and track reading. | Mostly task-first. | Yes. | Low-medium. | Keep primary; add "Add first book" and "Open current book" emphasis. |
| Book Detail | Core first-day flow | Read, capture, convert, and inspect source-backed knowledge for one book. | Task-first. | Yes, but dense. | Medium. | Make it the primary cockpit; collapse advanced graph/ontology panels by default for first-time users. |
| Capture Inbox | Core first-day flow | Process raw captures into notes, quotes, actions, and concepts. | Task-first but naming is storage-first. | Mostly. | Medium. | Rename in UX to "Process Captures"; add batch "Start with newest" path. |
| Notes | Core weekly flow | Write/read/edit source-backed notes. | Mostly module-first. | Somewhat. | Medium. | Keep primary after first capture; add "Create note from current book" CTA and source filters. |
| Quotes | Secondary / optional until data exists | Review extracted or manually saved quotes. | Module-first. | Yes. | Medium. | Move to Secondary; surface quotes on Book Detail and Search first. |
| Action Items | Secondary / core weekly flow | Track reading-derived tasks. | Task-first. | Yes. | Low-medium. | Rename nav to "Actions"; keep secondary or primary only for action-heavy users. |
| Concepts | Secondary / optional until data exists | Review concepts extracted from captures/notes. | Module-first. | Partly. | High for new users. | Move to Secondary; introduce via concept review, not first-day nav. |
| Knowledge Objects | Advanced workflow | Manage principles, lenses, exercises, methods, and ontology objects. | Module-first. | Partly. | High. | Move under More or Advanced Knowledge; split "Lenses" as contextual project/review tool later. |
| Daily | Core weekly flow | Reflect on one resurfaced source and one prompt. | Task-first. | Yes. | Low-medium. | Keep secondary; surface daily cards on Dashboard/Book Detail instead of requiring nav visit. |
| Projects | Core first-day flow for game designers; core weekly for readers | Apply reading knowledge to game design work. | Mostly task-first. | Yes. | Medium. | Keep primary, but onboard through "Apply first quote/concept to project." |
| Project Detail | Core weekly flow | Turn source-backed knowledge into decisions, problems, playtests, and lens reviews. | Task-first but broad. | Partly. | Medium-high. | Use tabs/staged sections: Problems, Applications, Decisions, Playtests, Review. |
| Analytics | Advanced workflow | Understand activity trends from real data. | Module-first. | Yes but data-dependent. | Medium. | Move under More; show dashboard summaries only after enough activity. |
| Review | Core weekly flow | Revisit source-backed material and record confidence. | Task-first. | Yes. | Low-medium. | Keep primary; frame as "Review today" in Dashboard. |
| Mastery | Advanced / optional until data exists | Inspect familiarity/usefulness scores. | Module-first. | Partly. | High. | Move under Review or More; expose scores inside Concept/Review pages. |
| Import/Export | Advanced workflow | Bring data in or get data out. | Task-first. | Yes. | Low but not first-day. | Move under More/Settings; keep available for trust and portability. |
| Graph | Advanced workflow | Explore and curate relationships. | Advanced module-first. | Partly. | High. | Move under More; provide contextual "Open graph for this book/concept/project" entry points. |
| Forum | Secondary / optional until data exists | Discuss books, quotes, concepts, and project work. | Module-first. | Mostly. | Medium. | Move to Secondary; emphasize "Discuss this source" from detail pages. |
| AI Right Rail | Advanced contextual helper | Generate draft suggestions from selected source context. | Contextual. | Partly. | Medium. | Keep in Right Rail, collapsed by default until source selected; label as draft assistant. |
| Admin Ontology | Admin-only | Import safe ontology seed data. | Task-first for admins. | Yes. | Hidden for normal users. | Keep admin-only and under More/Admin. |
| Search | Core first-day utility | Find anything or jump to a source. | Task-first. | Yes. | Low. | Keep topbar shortcut and Primary nav optional entry; do not make it a normal page. |

## Detailed Area Notes

### Sidebar Navigation

What the user is trying to do: decide where to work.

Current issue: the sidebar exposes the internal data model. Notes, Captures, Quotes, Actions, Concepts, Knowledge, Daily, Analytics, Review, Mastery, Import/Export, Projects, Graph, Forum, Search, and disabled future modules compete for attention. This is accurate but not humane.

Recommended simplification:

- Primary: Dashboard, Library, Capture, Notes, Projects, Review.
- Secondary: Quotes, Actions, Concepts, Knowledge, Daily, Forum.
- Advanced / More: Graph, Analytics, Import/Export, Admin Ontology, AI Settings.
- Hide disabled Lenses/Diagnostics/Exercises until real routes exist or fold them into Knowledge/Review copy.

### Dashboard

What the user is trying to do: know what to do now.

Current issue: Dashboard shows stats, daily cards, workflow copy, active books, and recent catalog. It is coherent, but it still asks the user to choose among too many paths.

Recommended simplification:

- Make the top block a single "Continue Reading" or "Start your first book" task.
- If no books exist, show only "Add first book" and a three-step explanation: Add book -> Capture while reading -> Process captures.
- Move catalog stats below the first task.
- Keep daily cards hidden until at least one quote/capture/note exists, or show an honest "Daily starts after your first capture" state.

### Library

What the user is trying to do: manage books and choose one to read.

Current issue: mostly good. The library is one of the few concepts users already understand.

Recommended simplification:

- Keep as a Primary nav item.
- Add a first-run empty state that says "Add a book to start your cockpit."
- Make "currently reading" and "open current book" more prominent than five-star/anti-library shelves for first-day users.

### Book Detail

What the user is trying to do: work with one book.

Current issue: this page best expresses BookOS, but it is dense. It contains hero, insights, graph/concepts/lenses, quick capture, notes, right rail, actions, source, and AI.

Recommended simplification:

- Keep Book Detail as the main cockpit.
- Preserve advanced cards, but collapse graph/ontology/lenses by default until data exists.
- Keep Quick Capture above or near the fold.
- Make "Capture thought" and "Open source" the dominant actions.

### Capture Inbox

What the user is trying to do: process unstructured reading thoughts.

Current issue: "Inbox" sounds passive. The important job is deciding what each capture becomes.

Recommended simplification:

- Rename UX label to "Process Captures" while preserving route.
- Primary button should follow parsed type: Quote -> Convert to Quote; Action -> Convert to Action; Concept -> Review Concepts; otherwise Note.
- Show "Your next 5 unprocessed captures" by default, not every possible filter.

### Notes

What the user is trying to do: read, write, and edit source-backed notes.

Current issue: Notes is useful but abstract if no book context exists.

Recommended simplification:

- Keep primary after onboarding, but make empty state book-centric.
- Promote "Write note for current book."
- Show parsed blocks and backlinks as secondary panels, not competing top-level concepts.

### Quotes

What the user is trying to do: save and revisit meaningful passages.

Current issue: Quotes is understandable, but not a first-day destination until the user has captured or converted quotes.

Recommended simplification:

- Move to Secondary.
- Surface quote counts and latest quote in Book Detail and Daily.
- Keep manual creation, but recommend capture conversion as the default path.

### Action Items

What the user is trying to do: turn reading into tasks.

Current issue: useful and concrete, but "Action Items" can be shortened.

Recommended simplification:

- Rename sidebar label to "Actions."
- Keep visible in Secondary or configurable Primary for action-oriented users.
- Surface due/open actions in Dashboard and Project Detail.

### Concepts

What the user is trying to do: manage extracted ideas.

Current issue: concept management is powerful but initially abstract. It should be introduced through `[[Concept]]` review rather than a blank module page.

Recommended simplification:

- Move to Secondary.
- Empty state should tell users to capture `[[Concept Name]]`.
- Concept Detail should emphasize source references and "Apply to Project."

### Knowledge Objects

What the user is trying to do: manage higher-order design knowledge.

Current issue: "Knowledge Objects" is an implementation term. It is not a natural user phrase.

Recommended simplification:

- Rename visible label to "Knowledge" or "Design Knowledge."
- Move to Secondary or More depending on user role.
- Later split user-friendly surfaces: Lenses, Exercises, Methods.

### Daily

What the user is trying to do: get one thing to remember and one design question.

Current issue: daily is useful, but it should come to the user rather than require a page visit.

Recommended simplification:

- Keep Daily as Secondary.
- Show daily cards on Dashboard and Book Detail.
- Add a "Start review from today's items" path later.

### Projects and Project Detail

What the user is trying to do: apply reading to game design work.

Current issue: Project Mode is valuable but broad. Project Detail can become another cockpit with too many panels.

Recommended simplification:

- Keep Projects as Primary for the game design audience.
- On first use, guide: Create project -> apply one source -> create one problem -> make one decision.
- Project Detail should default to a "Today" view: open problems, recent source applications, next playtest/review action.
- Move deeper sections into tabs or project subnav.

### Analytics, Review, and Mastery

What the user is trying to do: understand and improve learning.

Current issue: Review is actionable. Analytics and Mastery are more interpretive and should not compete with the core workflow.

Recommended simplification:

- Keep Review Primary.
- Move Analytics and Mastery under More or inside Review.
- Dashboard can show one small "Review due" card instead of full analytics.

### Import / Export

What the user is trying to do: trust the product with their data.

Current issue: important for trust, but not central daily work.

Recommended simplification:

- Move under More or Settings.
- Keep strong copy around privacy, preview, and no invented pages.

### Graph

What the user is trying to do: understand relationships.

Current issue: graph is powerful but inherently high-cognitive-load.

Recommended simplification:

- Move under More.
- Keep contextual graph buttons on Book, Concept, Project, and Search results.
- Default graph view should start scoped to the current entity, never a global blank graph.

### Forum

What the user is trying to do: discuss a specific source, idea, or project problem.

Current issue: Forum as a destination can feel generic. It is strongest when launched from a source.

Recommended simplification:

- Move to Secondary.
- Keep "Start discussion from this quote/concept/project" entry points.
- Forum home should prioritize source-linked recent discussions.

### AI Right Rail

What the user is trying to do: get optional draft help from selected source material.

Current issue: AI is safe but can distract from the source/action panels.

Recommended simplification:

- Keep AI contextual in Right Rail.
- Collapse by default until a source reference is selected.
- Use labels like "Draft Assistant" and "MockAIProvider" to prevent false expectations.

### Admin Ontology

What the user is trying to do: seed safe original game design ontology data.

Current issue: admin-only and correctly hidden from normal users.

Recommended simplification:

- Keep admin-only.
- Move under More/Admin.
- Do not expose in onboarding.

### Search

What the user is trying to do: jump to anything fast.

Current issue: search is useful and already shortcut-driven.

Recommended simplification:

- Keep topbar search and shortcut.
- Keep optional Primary "Search" action on mobile if needed.
- Results should prefer current book/project context when available.

## Cross-Cutting Recommendations

### 1. Create a First-Day Guided Path

First-day path:

1. Add your first book.
2. Mark it as reading.
3. Capture one thought with `💡 p.12 ... [[Concept]] #tag`.
4. Process that capture into a note/quote/action.
5. Open the source reference.
6. Optional: apply it to a project.

### 2. Separate "Daily Work" From "Database Areas"

Daily work:

- Dashboard.
- Current Book.
- Capture.
- Notes.
- Projects.
- Review.

Database areas:

- Quotes.
- Actions.
- Concepts.
- Knowledge.
- Forum.
- Graph.
- Analytics.
- Import/Export.

### 3. Use Progressive Disclosure

Hide or collapse:

- Graph unless a user asks for relationship exploration.
- Analytics until enough data exists.
- Mastery until review records exist.
- Knowledge Objects until concepts or ontology seed exists.
- AI suggestions until source context exists.

### 4. Rename Implementation Terms

Recommended UX labels:

- `Capture Inbox` -> `Process Captures`.
- `Action Items` -> `Actions`.
- `Knowledge Objects` -> `Knowledge`.
- `Mastery` -> `Learning Progress` or move under `Review`.
- `Graph` -> `Knowledge Graph`, under More.
- `Admin Ontology` -> `Ontology Import`, admin-only.

### 5. Make Source Reference a Product Primitive

Every page should answer:

- Where did this idea come from?
- What can I do with it now?
- Can I apply it to a project?
- Can I review it later?

## Recommended Priority Order

1. Navigation simplification and progressive disclosure.
2. Dashboard first-run onboarding.
3. Book Detail density reduction.
4. Capture Inbox rename and conversion-first layout.
5. Project onboarding path.
6. Review-first weekly workflow.
7. Advanced surfaces under More.

