# BookOS PO Decision Report

Last reviewed: 2026-05-01.

Reviewed branch: `main`.

Reviewed SHA: `c62e9eaa163e9ae7192046dceda09a6bf2470091`.

## Research Boundary

This report is based on repository inspection, automated verification, and heuristic QA scoring. It does not claim external user research. Human sessions still need to be run using the usability study package.

## Executive Summary

BookOS is now strongest when framed as a practical source-backed loop:

Read -> capture -> process -> open source -> review or apply to project.

Product Slimming has improved the first-day journey enough for controlled beta. The PO decision is to continue controlled beta with caveats, pause broad feature expansion, and start Workflow Hardening 0.3.

## Current Scores

- Product Slimming score: 82/100.
- First 15 Minutes readiness score: 84/100.
- Automated backend tests: PASS, 71 tests.
- Full browser E2E smoke: PASS, 31 tests.
- Product Slimming usability E2E: PASS, 8 tests.

These are internal QA scores, not human research results.

## What Is Now Easier

- New users can choose Reader, Note-Taker, Game Designer, Researcher, Community, or Advanced Mode.
- Dashboard gives mode-specific primary actions instead of a flat module wall.
- First Valuable Loop guides users from zero to one source-backed object.
- Use cases are executable checklists with progress state.
- Demo Workspace offers safe, labeled practice data.
- Quick Capture is more learnable through examples, parser preview, and beginner copy.
- Reader, Note-Taker, and Game Designer paths pass automated first-15-minutes coverage.
- Researcher and Community paths now have explicit starts, though still heuristic PARTIAL.

## What Remains Bloated

- Advanced Mode remains dense.
- Project Mode still has many sub-workflows.
- Review and Learning Progress can still feel split.
- Researcher Mode depends on concept-marker syntax.
- Community Mode is strongest from a source but still permits generic threads.
- Graph, Import/Export, Draft Assistant, Analytics, and Ontology Import must stay secondary.

## First-Day Ready

- Register and choose mode.
- Add book.
- Mark reading.
- Capture original thought.
- Process capture.
- Open source.
- Practice in Demo Workspace.
- Follow First Valuable Loop.

## Weekly-Use Ready

- Reader Mode.
- Note-Taker Mode.
- Game Designer source-to-project loop.
- Use-case checklists.
- Search after data exists.
- Review sessions after source-backed records exist.

## Advanced Only

- Global Knowledge Graph.
- Import/Export.
- Draft Assistant and AI settings.
- Analytics.
- Learning Progress before review data exists.
- Ontology Import.
- Manual relationships.

## Terms Improved

| Older or Technical Term | User-Facing Direction |
| --- | --- |
| Capture Inbox | Process Captures |
| Action Items | Actions |
| Knowledge Objects | Design Knowledge |
| Source Reference | Source Link |
| Entity Links | Relationships |
| Backlinks | Related Links |
| Mastery | Learning Progress |
| AI Suggestions | Draft Assistant |
| Admin Ontology | Ontology Import |
| Graph | Knowledge Graph |

## Terms Still Confusing

- Concept.
- Source Link.
- Review Session.
- Learning Progress.
- Project Application.
- Design Knowledge.
- Knowledge Graph.
- Draft Assistant.

## Strongest User Modes

| Mode | Verdict | Reason |
| --- | --- | --- |
| Reader | PASS | Clear first loop: add book, capture, convert, open source. |
| Note-Taker | PASS | Capture, note, action, and quote workflows match intent. |
| Game Designer | PASS | Strong differentiated value: apply source-backed reading to project decisions. |
| Demo Workspace | PASS | Safe practice path with reset/delete behavior. |

## Modes Needing More Work

| Mode | Verdict | Primary Issue |
| --- | --- | --- |
| Researcher | PARTIAL | Requires understanding `[[Concept Name]]` and concept review. |
| Community | PARTIAL | Source-linked discussion is clear, but generic forum path remains available. |
| Advanced | PARTIAL | Useful for power users but too dense for first-day default. |

## P0 Blockers

None identified by the latest automated gate.

## P1 Usability Issues

- Researcher Mode needs better concept-marker education.
- Community Mode needs stronger source-linked thread affordances.
- Advanced Mode should remain collapsed unless explicitly selected.
- Review and Learning Progress should be unified in normal-user language.
- Project Mode needs continued compression around one guided workflow.

## P2 Polish Issues

- Add source-context badges to forum cards.
- Add clearer concept review success summary.
- Add more inline examples for Researcher Mode.
- Add "why hidden" text for advanced empty states.
- Add visual QA screenshots after manual browser review.

## PO Recommendation

Continue controlled beta.

Release with caveats only for a controlled audience. Pause broad feature expansion. Start Workflow Hardening 0.3 before broader public beta.

## Next 10 Development Priorities

1. Polish Advanced Mode as optional and contextual.
2. Add source-context badges to Forum and Search surfaces.
3. Improve concept review success and next-step copy.
4. Start Review from selected book/concept context.
5. Keep global Knowledge Graph behind scoped graph entry points.
6. Unify Review and Learning Progress copy.
7. Compress Project Mode sub-workflows around the guided wizard.
8. Add clearer explanations for hidden advanced modules.
9. Run real moderated usability sessions.
10. Slice and review the dirty worktree before release tagging.

## Release Recommendation

Controlled beta can continue. Do not broaden release messaging until real user sessions confirm that new users can complete the first source-backed loop in 15 minutes without explanation.
