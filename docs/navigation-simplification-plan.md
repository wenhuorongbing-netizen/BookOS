# BookOS Navigation Simplification Plan

Last reviewed: 2026-04-30.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

Status: audit and plan only. No navigation implementation has been applied in this prompt.

## Problem

The current sidebar accurately exposes the implemented modules, but it exposes too many conceptual areas at the same level. This creates a product-comprehension problem:

- First-day users need a simple reading workflow.
- Weekly users need capture processing, notes, projects, and review.
- Advanced users need graph, analytics, import/export, ontology, and AI configuration.

The navigation should reflect user tasks, not backend modules.

## Design Principles

1. Primary navigation should answer "What do I do today?"
2. Secondary navigation should answer "Where are my extracted objects?"
3. Advanced navigation should answer "How do I inspect, manage, or configure the system?"
4. Contextual actions should be preferred over global pages for advanced concepts.
5. Empty or data-dependent modules should not look equally important before they contain data.
6. Existing routes and features should remain accessible.

## Proposed Navigation Model

### Primary

| Label | Route | Reason |
| --- | --- | --- |
| Dashboard | `/dashboard` | Daily command center and onboarding path. |
| Library | `/my-library` | First concrete user object: books. |
| Capture | `/captures/inbox` | Core reading-to-knowledge bridge. |
| Notes | `/notes` | Main authored knowledge workspace. |
| Projects | `/projects` | Core game design application workflow. |
| Review | `/review` | Weekly retention and source-backed reflection. |

### Secondary

| Label | Route | Reason |
| --- | --- | --- |
| Quotes | `/quotes` | Extracted object workspace after quote data exists. |
| Actions | `/action-items` | Reading-derived tasks; rename from Action Items. |
| Concepts | `/concepts` | Extracted concept review/library after data exists. |
| Knowledge | `/knowledge` | Higher-order design knowledge and ontology objects. |
| Daily | `/daily` | Dedicated resurfacing page; cards also surface elsewhere. |
| Forum | `/forum` | Structured discussions, strongest from source context. |

### Advanced / More

| Label | Route | Reason |
| --- | --- | --- |
| Knowledge Graph | `/graph` | Powerful but high-cognitive-load exploration. |
| Analytics | `/analytics` | Useful after meaningful activity exists. |
| Learning Progress | `/mastery` | Outcome view; should be nested under Review or More. |
| Import / Export | `/import-export` | Trust/data portability, not daily work. |
| Ontology Import | `/admin/ontology` | Admin-only. |
| AI Settings | Future settings route | AI provider status/config belongs outside daily nav. |

## Route Mapping

| Current Route | Proposed Surface | Notes |
| --- | --- | --- |
| `/dashboard` | Primary: Dashboard | Add onboarding/next-action emphasis later. |
| `/my-library` | Primary: Library | Keep. |
| `/books/new` | Contextual from Dashboard/Library/Topbar | Not a sidebar item. |
| `/books/:id` | Contextual cockpit from Library/Search/Dashboard | Should remain central. |
| `/notes` | Primary: Notes | Keep. |
| `/captures/inbox` | Primary: Capture | Visible label should be "Capture" or "Process Captures." |
| `/quotes` | Secondary: Quotes | Hide until quote exists or keep under Secondary. |
| `/action-items` | Secondary: Actions | Rename visible label to "Actions." |
| `/concepts` | Secondary: Concepts | Introduce via capture concept review. |
| `/knowledge` | Secondary: Knowledge | Keep available, avoid "Objects" label in nav. |
| `/daily` | Secondary: Daily | Daily cards remain on Dashboard/Book Detail. |
| `/projects` | Primary: Projects | Keep for game designer value proposition. |
| `/review` | Primary: Review | Keep as weekly action. |
| `/analytics` | More: Analytics | Data-dependent. |
| `/mastery` | More or nested under Review | Rename to Learning Progress. |
| `/import-export` | More: Import / Export | Keep for trust and portability. |
| `/graph` | More: Knowledge Graph | Prefer scoped contextual graph entry. |
| `/forum` | Secondary: Forum | Keep but emphasize source-linked discussion. |
| `/admin/ontology` | More/Admin | Admin-only. |
| `/five-star`, `/currently-reading`, `/anti-library` | Library subfilters | Remove from global nav; keep as library tabs/cards. |

## Progressive Disclosure Rules

### Show Immediately

- Dashboard.
- Library.
- Capture.
- Notes.
- Projects.
- Review.
- Search shortcut.

### Show After Data Exists

- Quotes after first quote.
- Actions after first action.
- Concepts after first parsed or reviewed concept.
- Daily after first source-backed quote/capture/note, with template prompt clearly labeled if no source exists.
- Analytics after first reading/capture/note activity.
- Learning Progress after first review item is completed.

### Always Contextual

- Open Source.
- Apply to Project.
- Discuss this.
- Open Knowledge Graph for this.
- Generate Draft Assistant output from this source.

### Admin Only

- Ontology Import.
- Future provider/admin settings.

## Proposed Dashboard Information Architecture

### Empty Account

1. Hero: "Start your reading cockpit."
2. Primary CTA: "Add your first book."
3. Secondary CTA: "Import existing notes."
4. Three-step explanation: Add book -> Capture -> Process.

### Has Book, No Captures

1. Hero: "Open your current book."
2. Primary CTA: "Capture one thought."
3. Example marker card.

### Has Captures

1. Hero: "Process your captures."
2. Primary CTA: "Review capture queue."
3. Secondary cards: current book, daily prompt.

### Has Source-Backed Knowledge

1. Hero: "Review and apply today."
2. Primary CTA: "Start review."
3. Secondary CTA: "Apply to project."
4. Stats become supporting content, not the lead.

## Proposed Sidebar Structure

```text
BookOS
Game Design Knowledge OS

Primary
- Dashboard
- Library
- Capture
- Notes
- Projects
- Review

Secondary
- Quotes
- Actions
- Concepts
- Knowledge
- Daily
- Forum

More
- Knowledge Graph
- Analytics
- Learning Progress
- Import / Export
- Ontology Import (admin only)
- AI Settings (future)
```

## Mobile Navigation Plan

Mobile should not expose the full list by default.

Recommended mobile bottom actions:

- Dashboard.
- Library.
- Capture.
- Projects.
- More.

Inside More:

- Notes.
- Review.
- Quotes.
- Actions.
- Concepts.
- Knowledge.
- Daily.
- Forum.
- Knowledge Graph.
- Analytics.
- Import/Export.

Rationale: Capture must remain one tap away on mobile because reading capture is the highest-frequency mobile action.

## Rename Plan

| Current Label | Proposed Label | Reason |
| --- | --- | --- |
| Captures | Capture | Action-oriented. |
| Capture Inbox | Process Captures | Communicates job-to-be-done. |
| Action Items | Actions | Shorter and more natural. |
| Knowledge Objects | Knowledge | Avoid implementation term. |
| Mastery | Learning Progress | Less abstract and less intimidating. |
| Graph | Knowledge Graph | Clearer when shown under More. |
| Admin | Ontology Import | More precise for admins. |

## Onboarding Flow Candidates

### Onboarding A: First Book

Trigger: no user books.

Steps:

1. Add book.
2. Add to library.
3. Mark as Reading.
4. Open Book Detail.

### Onboarding B: First Capture

Trigger: user has at least one current book and no captures.

Steps:

1. Show Quick Capture example.
2. User submits raw capture.
3. Parser preview confirms emoji/page/tag/concept.

### Onboarding C: First Conversion

Trigger: user has unprocessed capture.

Steps:

1. Open Process Captures.
2. Convert to note/quote/action.
3. Open source link.

### Onboarding D: First Project Application

Trigger: user has a quote, concept, or knowledge object and no project application.

Steps:

1. Create project.
2. Apply source-backed object.
3. Create problem or decision.

## What Not To Do

- Do not delete implemented modules.
- Do not pretend hidden features are incomplete.
- Do not show fake sample data as real activity.
- Do not move admin-only features into normal onboarding.
- Do not make AI a first-run requirement.
- Do not replace contextual source actions with a generic graph-first workflow.

## Implementation Phases For Later

### Phase 1: Labels and Grouping

- Group sidebar into Primary, Secondary, More.
- Rename visible labels only.
- Keep routes unchanged.

Acceptance: no route breakage; all existing pages remain reachable.

### Phase 2: Dashboard Next Action

- Add state-based "Next best action."
- Make stats secondary.
- Add empty-account onboarding.

Acceptance: a new user has one clear CTA.

### Phase 3: Contextual Entry Points

- Make Open Source, Apply to Project, Discuss This, Open Knowledge Graph visible on detail pages.
- Reduce need to visit advanced pages cold.

Acceptance: advanced features are entered through context.

### Phase 4: Progressive Data-Based Visibility

- Hide or collapse data-empty advanced links/cards.
- Show honest empty states with creation path.

Acceptance: no page feels like a blank database table.

### Phase 5: Mobile-First Capture

- Add mobile bottom nav or compact primary actions.
- Keep Capture one tap away.

Acceptance: capture remains fast on phone/tablet.
