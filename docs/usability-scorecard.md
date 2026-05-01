# BookOS Usability Scorecard

Status date: 2026-05-01

Current verified SHA: `c62e9eaa163e9ae7192046dceda09a6bf2470091`

This scorecard is a heuristic QA gate for Product Slimming. It is based on automated E2E coverage, source review, and internal UX inspection. It is not human user research and must not be cited as external validation.

## Scoring Model

Each category is scored from 0 to 5.

- `0`: Missing or unusable.
- `1`: Stub only.
- `2`: Partial and fragile.
- `3`: Usable but still cognitively heavy.
- `4`: Mostly clear for beta users.
- `5`: First-day ready with low friction.

Categories:

- `Discoverability`: A new user can find the starting point without knowing module names.
- `Clarity`: The page explains what to do and why.
- `Completion`: The E2E path can finish the intended task.
- `Cognitive load`: Higher score means lower visible overload.
- `Source visibility`: Source links remain visible and understandable.
- `Confidence`: The user receives enough feedback to know the action worked.

## Automated Gate Coverage

The Product Slimming usability gate is:

```bash
cd frontend
npm run e2e:usability
```

It runs:

- `e2e/usability-first-15-minutes.spec.ts`
- `e2e/demo-workspace.spec.ts`
- `e2e/use-cases.spec.ts`

Coverage:

- First Valuable Loop through onboarding, add book, capture, convert/open source, and checklist verification.
- Reader Mode first path.
- Note-Taker Mode first path.
- Game Designer Mode first path.
- Researcher Mode concept/review/scoped graph path.
- Community Mode source-linked thread/comment path.
- Advanced Mode graph/export/Draft Assistant path.
- Demo Workspace start/open/reset/delete path.
- Use-case checklist route and workflow-link coverage.

## Mode Scores

| Mode / Flow | Discoverability | Clarity | Completion | Cognitive load | Source visibility | Confidence | Verdict |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| First Valuable Loop | 4 | 4 | 4 | 4 | 4 | 4 | PASS |
| Reader Mode | 4 | 4 | 4 | 4 | 4 | 4 | PASS |
| Note-Taker Mode | 4 | 4 | 4 | 3 | 4 | 4 | PASS |
| Game Designer Mode | 4 | 4 | 4 | 3 | 4 | 4 | PASS |
| Researcher Mode | 3 | 4 | 3 | 3 | 4 | 3 | PARTIAL |
| Community Mode | 3 | 4 | 3 | 3 | 4 | 3 | PARTIAL |
| Advanced Mode | 3 | 3 | 3 | 2 | 3 | 3 | PARTIAL |
| Demo Workspace | 4 | 4 | 4 | 4 | 3 | 4 | PASS |

## P0 Blockers

None identified by the automated usability gate at this checkpoint.

## P1 Friction

- Researcher Mode still depends on the user understanding that `[[Concept Name]]` creates reviewable concept candidates.
- Community Mode still permits generic forum threads, even though the UI now recommends source-linked discussion.
- Advanced Mode still contains several power tools that require prior source-backed data to be useful.
- Note-Taker Mode still exposes note and capture concepts close together; the first path is workable, but terminology can still feel dense.
- Game Designer Mode completes the key loop, but applying knowledge still spans multiple concepts: quote, source, project application, decision.

## P2 Polish

- Add more inline examples to the Researcher Dashboard for concept-marker syntax.
- Add a source-context badge to forum thread cards when a discussion is source-linked.
- Add a compact “what just happened” success summary after concept review.
- Add a brief explanation of why Advanced tools are hidden or empty until source-backed records exist.
- Add optional screenshots to manual QA docs after visual verification.

## Release Gate Interpretation

Product Slimming can continue without broad feature expansion. Reader, Note-Taker, Game Designer, and Demo Workspace paths are suitable for beta-facing walkthroughs. Researcher and Community paths are now test-covered but should remain secondary until terminology and source-linked starts are validated with real users.

## Next Recommended UX Work

- Polish Advanced Mode as a power-user area rather than a first-day workflow.
- Add more visible source-linked labels in Forum and Search.
- Make concept review success states more explanatory.
- Run real human usability sessions using `docs/usability-study-plan.md` before claiming external validation.
