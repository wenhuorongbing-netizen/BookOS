# BookOS Use-Case Gap Analysis

Last reviewed: 2026-04-30.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Purpose

BookOS has many implemented modules. The current product risk is not missing capability; it is that users cannot immediately see which concrete job to perform. This document maps core user use cases to the current product surface and identifies gaps that should be solved before adding more features.

## Core Product Loop

Target loop:

1. Add a book.
2. Track reading.
3. Capture while reading.
4. Parse emoji/page/tag/`[[Concept]]`.
5. Process capture into note, quote, action, or concept.
6. Preserve and reopen source.
7. Review resurfaced knowledge.
8. Apply to a game project.
9. Discuss source-backed ideas when useful.

## Use-Case Coverage Matrix

| Use Case | Current Coverage | UX Gap | Impact | Recommended Product Fix |
| --- | --- | --- | --- | --- |
| First 10-minute onboarding | Partial | User lands on Dashboard with many choices and no single guided path. | New users may bounce before capturing anything. | Add a guided checklist: Add book -> Capture -> Process -> Open source. |
| Add first book | Good | Primary action exists, but Dashboard competes with stats/daily/cards. | Low. | Make "Add first book" the only hero action when library is empty. |
| Continue current book | Partial | Current book context depends on right rail source, not a deliberate pinned book. | Users may not know where to resume. | Add explicit "Continue reading" card sourced from currently reading user book. |
| Quick capture while reading | Good in Book Detail, partial globally | Capture is visible in Book Detail; global/topbar capture is not primary. | Users may leave book context to capture. | Add a compact "Capture" primary nav and global shortcut later. |
| Understand parser output | Good | Metadata is present, but first-time users need examples. | Parser feels magical/opaque. | Add examples near empty Capture view: `💬 p.42 ... #quote [[Concept]]`. |
| Process captures | Good capability, naming gap | "Capture Inbox" sounds passive; conversion job needs stronger framing. | Users may collect raw captures without processing. | Rename UX to "Process Captures"; show conversion queue. |
| Convert to note | Good | Conversion is one of several paths; could be clearer as default for untyped captures. | Low. | Keep as fallback conversion. |
| Convert to quote/action | Good | Parsed-type defaults help, but route should explain why quote/action exists. | Low-medium. | In capture cards, use "Because this starts with 💬..." helper text. |
| Review parsed concepts | Good capability, discovery gap | Concept review exists, but Concepts page can feel empty/abstract. | Medium. | Launch review from captures and notes; delay standalone Concepts emphasis. |
| Reopen source | Good | Source drawer exists, but source reference is not always visually dominant. | Medium. | Every detail page should pin "Open Source" near primary content. |
| Write structured notes | Usable | Markdown editing is MVP-level; not the strongest first-day action. | Medium. | Encourage capture-first workflow, then convert to notes. |
| Revisit quotes | Good | Quotes workspace is useful after data exists. | Low. | Move to Secondary; daily resurfaces quotes automatically. |
| Complete reading-derived actions | Good | "Action Items" term is heavy. | Low-medium. | Rename visible label to "Actions"; surface open actions in Dashboard/Right Rail. |
| Daily reflection | Partial-good | Daily cards exist but need better "do this today" framing. | Medium. | Show one daily quote + one design prompt on Dashboard after source data exists. |
| Weekly review | Good foundation | Review is separate from Dashboard; mastery is separate too. | Medium. | Dashboard should show "Start weekly review" when review items exist. |
| Understand mastery | Partial | Mastery scores are abstract and not obviously actionable. | Medium-high. | Move under Review; explain "confidence after review" instead of standalone mastery. |
| Apply reading to a project | Good capability, onboarding gap | Projects exists, but users need a first project application recipe. | High for game designers. | Add guided flow: Create project -> Apply quote/concept -> Create problem/decision. |
| Discuss a source | Good capability, discovery gap | Forum home can feel generic; strongest entry is from source detail. | Medium. | Prefer "Discuss this" from Book/Quote/Concept/Project pages. |
| Explore graph | Good advanced capability | Graph is advanced and can overwhelm early. | High. | Move under More; entry via scoped book/concept/project graph buttons. |
| Import existing notes | Good foundation | Import/export is important but not first-day unless migrating. | Low-medium. | Move under More; add "Import existing notes" onboarding branch. |
| Trust data portability | Good | Export docs exist; UI should reassure source/page behavior. | Low. | Keep export visible under More/Settings and copy "your data stays exportable." |
| Use AI safely | Good backend/UI safety | AI panel can be confusing if shown too early. | Medium. | Collapse AI until source context exists; label as draft-only assistant. |
| Seed game design ontology | Good admin capability | Not relevant to normal users. | Low for admins, high confusion for users if exposed. | Keep admin-only under More/Admin. |

## Major Use-Case Gaps

### Gap 1: No Single "Start Here" Path

Current state: Dashboard provides several useful areas but no single first step.

User consequence: New users may interpret BookOS as a complex admin system rather than a reading companion.

Recommended fix:

- If no user books: show "Add your first book."
- If user has a book but no captures: show "Open book and capture one thought."
- If captures exist: show "Process your captures."
- If source-backed objects exist: show "Review today" and "Apply to project."

### Gap 2: Capture Processing Is Not Framed as the Core Workflow

Current state: Capture Inbox is one route among many.

User consequence: Users may not understand that raw captures are the bridge between reading and structured knowledge.

Recommended fix:

- Rename visible label to "Capture" in Primary nav.
- Page title can be "Process Captures."
- Show unprocessed count in nav later.
- Use parsed marker explanations on each card.

### Gap 3: Projects Are Implemented but Need a Practical Entry Point

Current state: Project Mode exists as a broad subsystem.

User consequence: A game designer may not know whether to start in Projects, Concepts, Actions, or Book Detail.

Recommended fix:

- First project recipe: Create project -> Add design problem -> Apply one quote/concept -> Create one decision.
- Add contextual "Apply to Project" as the primary bridge from reading pages.
- Project Detail should highlight "source-backed applications" before deeper planning sections.

### Gap 4: Advanced Knowledge Surfaces Compete With Basic Work

Current state: Concepts, Knowledge, Graph, Analytics, Mastery, and Forum appear as peer areas.

User consequence: Users see an ontology management product before they experience the simple capture/review loop.

Recommended fix:

- Concepts/Knowledge/Forum stay Secondary.
- Graph/Analytics/Mastery/Import-Export/Admin stay under More.
- Use contextual entry points before global workspaces.

### Gap 5: Empty States Need Stronger Next Actions

Current state: many empty states are honest, but they often describe absence rather than guide a workflow.

User consequence: An empty advanced page feels like a dead end.

Recommended fix:

- Empty Quotes: "Capture a line with 💬 or convert a quote capture."
- Empty Concepts: "Type `[[Concept Name]]` in a capture."
- Empty Project Applications: "Apply a quote or concept from a source page."
- Empty Graph: "Create notes, captures, concepts, or manual links first."

### Gap 6: Review and Mastery Are Split Conceptually

Current state: Review and Mastery have separate pages.

User consequence: Users may not know whether "review" or "mastery" is the action.

Recommended fix:

- Treat Review as the action.
- Treat Mastery as an outcome/score shown inside Review, Concept Detail, and Analytics.

## Proposed First-Day Use Case

Title: "Read one page and capture one idea."

Steps:

1. Add a book.
2. Add it to library as Reading.
3. Open Book Detail.
4. Use Quick Capture with an example marker.
5. See parsed metadata.
6. Convert to note/quote/action.
7. Open source reference.
8. Optional: create or apply to a project.

Success metric:

- User creates one source-backed object in under 10 minutes.

## Proposed Weekly Use Case

Title: "Process, review, and apply."

Steps:

1. Open Dashboard.
2. Process unconverted captures.
3. Review daily quote/prompt.
4. Complete open actions.
5. Run a review session.
6. Apply one idea to an active project.

Success metric:

- User turns at least one reading object into a project application, decision, or action.

## Proposed Advanced Use Case

Title: "Curate knowledge graph."

Steps:

1. Open a concept or book.
2. Open scoped graph.
3. Inspect source-backed nodes.
4. Add manual relationship if useful.
5. Use graph result to review or apply to project.

Success metric:

- User adds a meaningful relationship without seeing fake nodes or invented pages.

## Product Metrics to Add Later

- Time from registration to first book.
- Time from first book to first capture.
- Time from first capture to first conversion.
- Percentage of captures processed within 7 days.
- Source-open clicks per source-backed object.
- Project applications created from quotes/concepts.
- Review sessions completed per week.
- Search-to-open success rate.
