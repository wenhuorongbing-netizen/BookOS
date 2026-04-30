# BookOS Hands-On Beta UX Report

Last reviewed: 2026-04-30.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Purpose

This report is a product QA and heuristic usability gate, not a formal user research study. No external users were observed, and no user research claims are made. The goal is to answer whether a new user can understand BookOS and complete meaningful source-backed use cases in the first 15 minutes using the current app, docs, and automated browser smoke coverage.

## Verdict Summary

BookOS is beta-usable for the core reading loop if the user follows onboarding, Dashboard CTAs, Quick Capture guidance, or the Use Cases library.

The app still feels broad. Research, forum, graph, import/export, and AI workflows should remain secondary or advanced until more guided entry points and data-dependent progressive disclosure are added.

PO decision: **Release with caveats**.

## Score Scale

| Score | Meaning |
| --- | --- |
| 0 | Missing or unusable. |
| 1 | Present but confusing or stub-like. |
| 2 | Usable only with prior knowledge. |
| 3 | Usable with friction. |
| 4 | Clear enough for beta. |
| 5 | Strong first-run experience. |

For Cognitive load, a higher score means lower cognitive load.

## Scenario Scores

| Scenario | Discoverability | Clarity | Completion | Cognitive load | Source-reference visibility | User confidence | Verdict |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| First-time reader | 5 | 4 | 5 | 4 | 5 | 4 | PASS |
| Note-taker | 4 | 4 | 5 | 4 | 5 | 4 | PASS |
| Game designer | 4 | 4 | 5 | 3 | 5 | 4 | PASS |
| Researcher | 3 | 3 | 4 | 3 | 4 | 3 | PARTIAL |
| Community user | 3 | 3 | 4 | 3 | 4 | 3 | PARTIAL |
| Advanced user | 3 | 3 | 4 | 2 | 4 | 3 | PARTIAL |

## Scenario 1: First-Time Reader

Goal: register, choose Reader Mode, add a book, set reading status, capture first note, convert capture to quote, and open source.

What works:

- First-run onboarding can route a new user into Reader Mode.
- Dashboard gives a clear first action when the account is empty.
- Library and Book Detail provide understandable book context.
- Quick Capture supports source-backed capture and parser preview.
- Capture conversion to Quote and Open Source are covered by E2E.

Friction:

- A new reader may still see advanced terms in the cockpit after the first book.
- "Capture Inbox" is understandable only after the Quick Capture guide or use-case docs.

Verdict: PASS.

## Scenario 2: Note-Taker

Goal: choose Note-Taker Mode, create note, use parser guide, create action item, and review Capture Inbox.

What works:

- Note-Taker Mode promotes reading, capture, notes, and review.
- Note creation, note detail, parsed note blocks, and Capture Inbox are implemented.
- Guided Quick Capture teaches emoji markers, page markers, tags, concepts, and Chinese page markers.
- Action item conversion and Action Items workspace are implemented.

Friction:

- The user needs to understand the difference between a note, note block, raw capture, quote, and action item.
- Capture Inbox is a strong workflow but still reads like a module name rather than a task.

Verdict: PASS.

## Scenario 3: Game Designer

Goal: choose Game Designer Mode, add a game design book, create project, apply quote or concept to project, create design decision, and open project cockpit.

What works:

- Game Designer Mode changes navigation priority.
- Projects are first-class navigation.
- Quote Detail and Project Detail include apply-to-project guidance.
- The guided project wizard explains source -> problem -> application -> decision -> playtest -> iteration.
- Source references are preserved through project application paths where existing APIs support them.

Friction:

- The Project Cockpit is broad and can still feel dense.
- The multi-step wizard now submits final creation through one backend transaction with an idempotency key, but the Project Cockpit is still broad enough to need guided entry points.

Verdict: PASS for beta, with no remaining P1 issue around partial project-wizard creation.

## Scenario 4: Researcher

Goal: create concept, link note or quote to concept, open graph, and start review session.

What works:

- `[[Concept]]` review prevents uncontrolled automatic concept creation.
- Concept Detail, Knowledge, Graph, Review, and Mastery surfaces exist.
- Graph uses real links or honest empty states.
- Review sessions and mastery are source-aware.

Friction:

- Researcher workflow is still less discoverable than reader or designer workflows.
- Graph and review are understandable after data exists, but are not yet a low-friction first-15-minute path.
- "Knowledge Object" remains an implementation-heavy concept.

Verdict: PARTIAL.

## Scenario 5: Community User

Goal: open forum, create source-linked thread, add comment, and open source context.

What works:

- Forum categories, thread creation, comments, source-linked thread context, reports, and source safety are implemented.
- Forum appears as a secondary navigation item.
- Source-linked forum creation is strongest from detail/source contexts.

Friction:

- A new community user may not know what discussion type to start without already having a source-backed quote, concept, book, or project context.
- The forum is useful, but not yet the primary first-run product loop.

Verdict: PARTIAL.

## Scenario 6: Advanced User

Goal: open graph, export data, generate MockAIProvider draft, accept/reject draft safely.

What works:

- Graph, Import/Export, and MockAIProvider workflows exist.
- AI is clearly draft-only and local mock by default.
- Export/import pages exist with safety copy.
- Advanced routes remain available through More and command palette.

Friction:

- These features are intentionally advanced and should not dominate first-run experience.
- Graph, import/export, and AI require more product explanation than core reading/capture.

Verdict: PARTIAL by design. Keep advanced/collapsed.

## P0 Usability Blockers

None found in this gate.

## P1 Usability Issues

- Researcher, forum, graph, import/export, and AI workflows remain too abstract for a new user unless launched from contextual use-case guidance.
- Project apply-knowledge wizard now uses a transactional final submit; continue monitoring usability around the final review and retry copy.
- Some labels still expose implementation concepts, especially Knowledge Objects, Mastery, Entity Links, and Source References.
- Capture Inbox should continue moving toward "Process Captures" language across page titles and docs.

## P2 Polish Issues

- Dashboard and Book Detail could use more data-dependent collapsing so empty advanced panels do not compete with the first task.
- Forum should offer stronger templates from source-backed contexts.
- Review/Mastery should be visually merged into one learning loop for non-advanced users.
- Graph should default to scoped contexts instead of encouraging global graph exploration too early.

## Recommendation

Continue beta with caveats. Do not add more modules before tightening the first 15 minutes:

1. Add one guided "first valuable loop" path per mode.
2. Reduce visible advanced surfaces for default users.
3. Make Capture -> Convert -> Open Source the product's primary taught loop.
4. Make Project Mode teach one concrete source-to-design-decision path before exposing every project subsystem.
