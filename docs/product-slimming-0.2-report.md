# BookOS Product Slimming 0.2 Report

Last reviewed: 2026-05-01.

Reviewed branch: `main`.

Reviewed SHA: `c62e9eaa163e9ae7192046dceda09a6bf2470091`.

## Research Boundary

This report is based on repository inspection, automated browser E2E, backend/frontend verification, and heuristic QA scoring. It is not external user research. No human usability sessions have been completed or claimed.

## Executive Summary

BookOS has moved from a broad module collection toward a task-first product with clearer first-day paths. The strongest loop is now:

Add book -> capture thought -> process capture -> open source -> review or apply to project.

Reader, Note-Taker, Game Designer, and Demo Workspace paths are ready for controlled beta walkthroughs. Researcher, Community, and Advanced modes are usable but should remain secondary because they still require more explanation.

## Current SHA

`c62e9eaa163e9ae7192046dceda09a6bf2470091`

## What Improved Since Previous Usability Review

- Onboarding and mode selection give users an explicit starting intent.
- Dashboard now behaves as a task-first command center with mode-specific primary actions.
- First Valuable Loop gives new users a concrete path from zero to one source-backed record.
- Use cases are executable checklists instead of static documentation only.
- Quick Capture teaches parser syntax with examples and preview behavior.
- Demo Workspace provides safe original practice data that can be reset or deleted.
- Reader and Note-Taker flows are substantially clearer than the original module-first navigation.
- Game Designer Mode has a visible path from source-backed reading to project decision.
- Researcher Mode now starts from `[[Concept Name]]`, concept review, and scoped graph instead of global graph first.
- Community Mode now guides users toward source-linked discussion instead of a blank generic forum.

## What Remains Bloated

- Advanced Mode still contains several high-power surfaces: Knowledge Graph, Import/Export, Draft Assistant, Analytics, and admin import.
- Project Mode remains conceptually dense because problem, application, decision, playtest, lens review, and graph are separate mental models.
- Review and Learning Progress can still feel like separate systems for non-advanced users.
- Researcher Mode depends on users understanding concept-marker syntax.
- Community Mode still allows generic threads even though source-linked discussion is the stronger product loop.

## What Is Now First-Day Ready

- Register and choose mode.
- Add first book.
- Mark book as reading.
- Capture one original thought.
- Process capture into quote, action, note, or concept review.
- Open source from the created object.
- Follow First Valuable Loop.
- Practice safely in Demo Workspace.

## What Is Weekly-Use Ready

- Reader Mode for continuing books and processing captures.
- Note-Taker Mode for notes, actions, quotes, and review.
- Game Designer Mode for source-backed project application and design decisions.
- Use-case checklists for recurring workflows.
- Search and source opening once a user has data.

## What Remains Advanced Only

- Global Knowledge Graph.
- Import/Export.
- Draft Assistant and AI settings.
- Analytics and Learning Progress before review data exists.
- Admin Ontology Import.
- Manual relationship editing.
- Advanced project graph usage.

## What Should Stay Hidden Until Data Exists

- Graph previews when there are no source-backed links.
- Analytics cards when there are no books, captures, reviews, or projects.
- Learning Progress before at least one review session exists.
- Draft Assistant before the user has a source context or chooses Advanced Mode.
- Forum activity sections before real threads exist.
- Project focus before at least one project exists.

## Which Terms Were Improved

| Technical or Older Term | User-Facing Direction |
| --- | --- |
| Capture Inbox | Process Captures |
| Action Items | Actions |
| Knowledge Objects | Design Knowledge |
| Source Reference | Source Link or Source |
| Entity Links | Relationships |
| Backlinks | Related Links |
| Mastery | Learning Progress |
| AI Suggestions | Draft Assistant |
| Admin Ontology | Ontology Import |
| Graph | Knowledge Graph |

## Which Terms Remain Confusing

- Concept: still needs examples using `[[Concept Name]]`.
- Source Link: valuable, but users may not understand why it matters until they open one.
- Review Session: useful after data exists, but weak as a first action.
- Learning Progress: should be explained as the result of review, not a separate module.
- Project Application: accurate internally but should be framed as "apply this idea to a project."
- Design Knowledge: broad and still abstract without examples.
- Knowledge Graph: should remain contextual until the user has relationships.
- Draft Assistant: must keep reinforcing draft-only behavior.

## Use-Case Checklist Status

Status: PASS for automated route/checklist coverage.

The `npm run e2e:usability` gate includes use-case route coverage and verifies that checklists can start, reset, show automatic/manual status, and link to real app routes.

Current limitations:

- Some checklist steps remain manual by design.
- Researcher and Community paths pass automation but remain heuristic PARTIAL for human comprehension.
- Checklist completion is not a substitute for moderated user research.

## Demo Workspace Status

Status: PASS for automated safety behavior.

Verified behavior:

- Demo can start.
- Demo records are labeled.
- Demo records can be opened.
- Demo can reset.
- Demo can delete records.
- Demo data is excluded from normal analytics by default.
- Demo data uses original sample content and does not invent real page numbers.

Remaining risk:

- Demo source visibility score is lower than core source-backed workflows because demo content is intentionally artificial and labeled.

## Dashboard Mode Status

Status: PASS for Reader, Note-Taker, Game Designer, and Demo-oriented starts.

Status: PARTIAL for Researcher, Community, and Advanced modes.

Reason:

- Researcher and Community modes now have clearer starts, but still require more explanation than Reader or Game Designer.
- Advanced Mode should continue to be treated as a power-user mode, not first-day default.

## First Valuable Loop Status

Status: PASS.

The loop is now explicit and automated:

1. Register and choose mode.
2. Add or choose book.
3. Capture original thought.
4. Process capture.
5. Open source.
6. Choose next path.

Unknown pages remain unknown. No fake data is created unless the user explicitly starts Demo Workspace.

## E2E Usability Status

Latest local verification:

- Backend tests: PASS, 71 tests.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Full E2E smoke: PASS, 31 tests.
- Product Slimming usability E2E: PASS, 8 tests.

The first full E2E run found one stale copy assertion in `mvp-core-loop.spec.ts`; it was corrected to match the current Concept Detail heading, then the suite passed.

## P0 Blockers

None identified in the automated Product Slimming gate.

## P1 Usability Issues

- Researcher Mode still requires users to learn concept-marker syntax.
- Community Mode still permits generic forum threads, even though source-linked discussions are the clearer product value.
- Advanced Mode still has too many concepts for first-day users.
- Review and Learning Progress should be treated as one learning loop for normal users.
- Project Mode sub-workflows need continued simplification around one guided path.

## P2 Polish Issues

- Add source-context badges to forum thread cards.
- Add clearer success summary after concept review.
- Add more inline concept examples on Researcher surfaces.
- Add "why hidden" copy for Advanced tools when no source-backed data exists.
- Add optional screenshots to manual QA after visual verification.

## Product Slimming Score

82 out of 100.

Rationale:

- Core first-day path works and is automated.
- Reader, Note-Taker, Game Designer, and Demo flows are controlled-beta ready.
- Researcher, Community, and Advanced remain partially clear but not first-day strong.
- Human usability sessions have not yet been run.

## First 15 Minutes Readiness Score

84 out of 100.

Rationale:

- The first loop is concrete, test-covered, and source-backed.
- Empty and mode-based starts are much clearer.
- Cognitive load is still present when users leave the guided loop.

## PO Recommendation

Continue controlled beta.

Do not broaden public beta messaging yet. Pause broad feature expansion and start Workflow Hardening 0.3 focused on reducing friction inside the strongest loops.

## Next 10 Development Priorities

1. Polish Advanced Mode as an optional power-user area.
2. Make scoped graph entry points stronger and keep global graph secondary.
3. Add visible source-context badges to forum thread cards.
4. Improve concept review completion copy and next actions.
5. Merge Review and Learning Progress in normal-user language.
6. Continue reducing Project Mode submodule density.
7. Add better "why am I seeing this?" copy to dashboards.
8. Run moderated usability sessions using the study package.
9. Convert human findings into P0/P1/P2 issues.
10. Slice the existing dirty worktree into reviewable commits before release tagging.
