# BookOS Product Slimming Baseline

Last reviewed: 2026-04-30.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Purpose

This document establishes the baseline for the Product Slimming sprint. It is a source-of-truth usability planning document, not evidence of external user validation.

No external moderated usability sessions have been conducted for this baseline. Findings are based on repository inspection, implemented routes, internal product review, and existing automated smoke coverage.

## Verified Usability Routes

The following routes are implemented in `frontend/src/router/index.ts` and resolve to existing view files:

| Feature | Route | View | Status |
| --- | --- | --- | --- |
| First-run onboarding | `/onboarding` | `OnboardingView.vue` | Verified |
| Use case library | `/use-cases` | `UseCasesView.vue` | Verified |
| Use case detail | `/use-cases/:slug` | `UseCaseDetailView.vue` | Verified |
| Help home | `/help` | `HelpView.vue` | Verified |
| Help topic | `/help/:topic` | `HelpView.vue` | Verified |
| Demo Workspace | `/demo` | `DemoWorkspaceView.vue` | Verified |
| Project apply-knowledge wizard | `/projects/:id/wizard/apply-knowledge` | `ProjectApplyKnowledgeWizardView.vue` | Verified |

## Slimming Principle

Do not delete the system's power. Reduce default cognitive load.

The first screen should teach one loop:

Add book -> capture while reading -> process capture -> open source -> review or apply to project.

Everything else should appear when it helps that loop or when the user asks for advanced workflows.

## Feature Classification

| Feature | Current Route or Surface | Classification | Default Visibility Recommendation |
| --- | --- | --- | --- |
| Dashboard | `/dashboard` | Essential first-day | Primary navigation |
| Library | `/my-library`, `/books/new`, `/books/:id` | Essential first-day | Primary navigation |
| Book Detail Cockpit | `/books/:id` | Essential first-day | Enter from Library and Continue Reading |
| Quick Capture | Book Detail and Dashboard surfaces | Essential first-day | Prominent in Dashboard and Book Detail |
| Capture Inbox | `/captures/inbox` | Essential first-day | Primary as `Process Captures` |
| Onboarding | `/onboarding` | Essential first-day | First-run only; restart from Dashboard/Settings |
| Use Cases | `/use-cases` | Essential first-day | Dashboard card and Help entry, not dominant sidebar item |
| Demo Workspace | `/demo` | Essential first-day for learning | Contextual CTA for new users; visible in Help/Use Cases |
| Notes | `/notes`, `/notes/:id` | Essential weekly | Primary or secondary depending on mode |
| Quotes | `/quotes`, `/quotes/:id` | Contextual | Secondary; surface inside Book Detail and Search |
| Action Items | `/action-items`, `/action-items/:id` | Essential weekly | Secondary as `Actions`; primary for action-heavy users later |
| Concepts | `/concepts`, `/concepts/:id` | Contextual | Secondary; introduce through concept review |
| Knowledge Objects | `/knowledge`, `/knowledge/:id` | Advanced | Move under More as `Design Knowledge` |
| Daily | `/daily` | Essential weekly | Surface on Dashboard; secondary nav |
| Review Sessions | `/review`, `/review/:id` | Essential weekly | Primary as `Review` |
| Mastery | `/mastery` | Advanced | Fold into Review as `Learning Progress` |
| Projects | `/projects`, `/projects/:id` | Essential first-day for Game Designer Mode; essential weekly otherwise | Primary only in Game Designer/Advanced modes; otherwise contextual |
| Project Problems | `/projects/:id/problems` | Contextual | Project subnav only |
| Project Applications | `/projects/:id/applications` | Contextual | Project subnav only |
| Project Decisions | `/projects/:id/decisions` | Contextual | Project subnav only |
| Project Playtests | `/projects/:id/playtests` | Contextual | Project subnav only |
| Project Lens Reviews | `/projects/:id/lens-reviews` | Contextual | Project subnav only |
| Project Apply Wizard | `/projects/:id/wizard/apply-knowledge` | Essential first-day for Game Designer Mode | CTA from Project, Quote, Concept, Source |
| Source Link Drawer | Contextual component | Contextual | Open from source-backed records |
| Related Links | Detail-page sections | Contextual | Show only when links exist |
| Knowledge Graph Preview | Book/Concept/Project detail surfaces | Contextual | Collapsed or scoped entry |
| Knowledge Graph Workspace | `/graph`, `/graph/book/:bookId`, `/graph/concept/:conceptId`, `/graph/project/:projectId` | Advanced | More menu; contextual buttons only |
| Forum | `/forum`, `/forum/new`, `/forum/threads/:id` | Contextual | Secondary; emphasize source-linked thread creation |
| Search Dialog | Cmd/Ctrl+K | Essential first-day utility | Topbar shortcut, always available |
| Analytics | `/analytics` | Advanced | More menu; dashboard summary only after data exists |
| Import/Export | `/import-export` | Advanced | More or Settings |
| Draft Assistant | Right rail | Contextual advanced | Collapsed until source context exists |
| AI Provider Settings | Settings/status surfaces | Advanced | More/Settings only |
| Ontology Import | `/admin/ontology` | Admin-only | Admin-only navigation and backend role enforcement |
| Five-Star Books | `/five-star` | Keep hidden until data exists | Library filter, not primary nav |
| Currently Reading | `/currently-reading` | Essential weekly | Dashboard/Library shortcut |
| Anti-Library | `/anti-library` | Contextual | Library filter, not primary nav |

## Essential First-Day Flow

Show these first:

1. Dashboard next action.
2. Add or open a book.
3. Quick Capture.
4. Process Captures.
5. Open Source.
6. Demo Workspace if the user wants practice data.
7. Project apply wizard only for Game Designer Mode.

Do not show Knowledge Graph, Analytics, Import/Export, Ontology Import, or AI Settings as first-day peers.

## Essential Weekly Flow

Show after the user has records:

1. Continue Reading.
2. Notes.
3. Review.
4. Actions.
5. Daily.
6. Projects for users with active project records.
7. Concepts after concept review has produced at least one concept.

## Contextual Features

These should be launched from records rather than primary navigation:

- Open Source from quote, note, capture, action, concept, daily item, forum thread, and project application.
- Apply to Project from quote, concept, design knowledge, source link, or daily prompt.
- Discuss Source from quote, concept, book, project, or source drawer.
- Open Knowledge Graph for current book, concept, or project.
- Draft Assistant from selected source context.
- Related links from detail pages only when related links exist.

## Advanced Features

Keep these under More or Advanced Mode:

- Full Knowledge Graph workspace.
- Analytics.
- Learning Progress.
- Import/Export.
- Design Knowledge management.
- AI provider/settings/status.
- Ontology Import for admins only.

## Keep Hidden Until Data Exists

Hide, collapse, or replace with setup CTAs:

- Empty graph cards with no links.
- Learning Progress summary before review items exist.
- Analytics charts before enough activity exists.
- Forum "popular threads" before real activity exists.
- Project subnav sections before a project exists.
- Draft Assistant prompts when no source context is selected.
- Related Links sections with no related links.
- Five-star and anti-library shortcuts before matching books exist.

## Ten Most Confusing Terms

| Current Term | Proposed Label | Reason |
| --- | --- | --- |
| Capture Inbox | Process Captures | Describes the job, not the storage bucket. |
| Knowledge Object | Design Knowledge | User-facing and less implementation-heavy. |
| Entity Link | Relationship | Plain language for linked records. |
| Backlink | Related Links | Explains what the user will see. |
| Source Reference | Source Link | Clearer for readers; keep technical term in docs. |
| Daily Sentence | Daily Quote | Matches visible user value. |
| Daily Design Prompt | Today's Design Prompt | More immediate and actionable. |
| Mastery | Learning Progress | Less academic and less score-heavy. |
| Ontology | Knowledge Map | More approachable outside admin context. |
| AI Suggestion | Draft Assistant | Reinforces draft-only safety. |

## Top Ten UI Areas With Too Many Visible Choices

| Area | Current Issue | Slimming Direction |
| --- | --- | --- |
| Sidebar | Too many modules appear as equal peers. | Primary, Secondary, More, Admin-only groups. |
| Dashboard | Many cards can compete with the next action. | One dominant "Do this today" path per mode. |
| Book Detail | Rich cockpit can feel dense for first-time users. | Keep capture/source visible; collapse advanced Knowledge Graph/ontology sections when empty. |
| Capture Inbox | Conversion, archive, filters, source, concepts all appear at once. | Lead with recommended conversion; tuck alternates into menu. |
| Project Detail | Problems, applications, decisions, playtests, lens reviews, Knowledge Graph, forum can overload. | Start with "Apply one source" and project next step. |
| Knowledge Graph Workspace | Filters, relationship editor, source drawer, graph canvas are advanced. | More menu plus scoped detail-page Knowledge Graph buttons. |
| Concepts | Concepts, sources, related links, Knowledge Graph, project application compete. | Introduce through reviewed concepts and next action. |
| Design Knowledge | Ontology types are abstract. | Group as Design Knowledge and expose by use case. |
| Daily | Reflection, regenerate, skip, apply, review, prototype can split focus. | One primary action based on source availability. |
| Right Rail Draft Assistant | Assistant draft actions compete with source/action panels. | Collapse until source context exists; label as optional draft helper. |

## Baseline Risks

- The app has many real modules; hiding them incorrectly could make users think data is missing. Use progressive disclosure, not route removal.
- Admin links must remain protected by backend authorization; frontend hiding is only convenience.
- Renaming UI labels must not break route names, API contracts, or documentation searchability.
- Demo data must stay clearly labeled and excluded from normal analytics unless explicitly included.
- "Release with caveats" remains the honest product stance until real external usability sessions are conducted.

## Next Sprint Definition

The Product Slimming sprint should implement only these categories first:

1. Rename confusing labels where the backend contract is unaffected.
2. Reduce visible choices in Sidebar and Dashboard.
3. Collapse advanced/empty sections until relevant data exists.
4. Add first-task checklist per onboarding mode.
5. Keep advanced routes reachable through More and Cmd/Ctrl+K.

Do not remove routes, remove data, or claim external validation without actual user sessions.

## Sprint 5 Implementation Notes

Implemented after the terminology pass:

- Quotes, Concepts, Design Knowledge, and Forum now start with one task-first next-step card before filters, category browsing, or dense grids.
- Daily now promotes one primary path: write a reflection first, then apply to a project only when useful.
- Daily secondary operations such as regenerate, skip, prototype task, project problem, and lens review are collapsed under "More" actions.
- Canonical technical routes and API names remain unchanged.
- Advanced workspaces still exist; the UI now explains what to do first instead of presenting every control as equally important.

Remaining product-slimming work:

- Audit whether every detail page has one visible primary next action.
- Collapse dense data tables on mobile where the table itself is not the primary task.
- Consider making advanced filters collapsed by default across secondary workspaces once saved filters exist.
