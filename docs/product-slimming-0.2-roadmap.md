# BookOS Product Slimming 0.2 Roadmap

Last reviewed: 2026-05-01.

Reviewed branch: `main`.

Reviewed SHA: `c62e9eaa163e9ae7192046dceda09a6bf2470091`.

## Roadmap Status

This is the next-milestone plan after the Product Slimming E2E and heuristic scoring gate. It does not claim external usability research.

## Recommendation

Continue controlled beta with caveats, pause broad feature expansion, and start Workflow Hardening 0.3.

Product Slimming 0.2 has made BookOS usable enough for guided beta walkthroughs, but the product should not broaden release messaging until human usability sessions validate first-day comprehension.

## Product Goal

Make BookOS feel like a hands-on reading-to-knowledge workflow:

Add book -> capture -> process -> open source -> review or apply to project.

## Guiding Rules

- Do not remove advanced capabilities.
- Do not present every capability as equally important.
- Prefer task language over implementation language.
- Show advanced modules only when users have data or explicitly choose Advanced Mode.
- Keep source traceability visible in every core workflow.
- Keep Draft Assistant optional and draft-only.
- Do not invent page numbers.
- Do not claim human research until real sessions happen.

## Workstream 1: Advanced Mode Containment

Goal: make Advanced Mode feel intentional instead of overwhelming.

Planned work:

- Keep Knowledge Graph, Import/Export, Draft Assistant, Analytics, and Ontology Import in Advanced or More by default.
- Show Advanced tools on Dashboard only when Advanced Mode is selected or source-backed data exists.
- Add copy explaining why advanced tools may be empty.
- Keep global graph secondary to scoped graph entry points from book, concept, project, and source.

Success criteria:

- Non-advanced users are not distracted by global graph/import/AI cards.
- Advanced users can still find every route through More and search.

## Workstream 2: Researcher Path Hardening

Goal: move Researcher Mode from PARTIAL to PASS.

Planned work:

- Add more examples for `[[Concept Name]]` on Researcher Dashboard and concept empty states.
- Make concept review success summarize what was created and where the source link is.
- Start review sessions from selected concept/book context without requiring technical IDs.
- Keep graph scoped to the concept or book until relationships exist.

Success criteria:

- Users understand how capture creates concept candidates.
- Users can review a concept and start review without external explanation.

## Workstream 3: Community Path Hardening

Goal: make source-linked discussion feel natural.

Planned work:

- Add source-context badges on forum thread cards.
- Promote forum creation from book, quote, concept, project, and source pages.
- Keep global forum empty states educational, not blank.
- Make generic thread creation available but not the default first action.

Success criteria:

- Community users can start from a source-backed object and return to that source.
- Empty forum does not look like failed social proof.

## Workstream 4: Review and Learning Progress Unification

Goal: reduce confusion between Review, Review Session, and Learning Progress.

Planned work:

- Explain Learning Progress as a result of review activity.
- Keep Review as the primary navigation label.
- Show Learning Progress after review data exists.
- Add source-backed review entry points from book, concept, quote, and project context.

Success criteria:

- Users know what to review next and why.
- Learning Progress does not compete with first-day capture.

## Workstream 5: Project Mode Compression

Goal: keep Game Designer Mode strong while reducing submodule density.

Planned work:

- Keep Apply Reading Knowledge as the dominant project CTA.
- Present problem, application, decision, and playtest as one guided sequence.
- Keep lower-level CRUD pages available but secondary.
- Improve created-record summary after wizard completion.

Success criteria:

- Game designers can explain how a reading source became a design decision.
- Project subpages do not overwhelm first project creation.

## Workstream 6: Measurement and Human Study

Goal: replace internal assumptions with observed user evidence.

Planned work:

- Run the study in `docs/usability-study-plan.md`.
- Use `docs/usability-test-script.md`.
- Capture notes in `docs/usability-observation-form.md`.
- Track time to first book, capture, conversion, and source open.
- Convert findings into P0/P1/P2 work.

Success criteria:

- No external validation claims are made before sessions happen.
- PO decisions use observed task completion, wrong turns, and confidence ratings.

## Next 10 Development Priorities

1. Polish Advanced Mode and keep it optional by default.
2. Add source-context badges to forum thread cards.
3. Improve concept review completion copy and next actions.
4. Let users start review from selected book/concept context.
5. Keep scoped graph entry points stronger than global graph.
6. Merge Review and Learning Progress language for normal users.
7. Reduce Project Mode submodule density.
8. Add better explanations for hidden advanced sections.
9. Run moderated usability sessions.
10. Slice the dirty worktree into reviewable commits before release tagging.

## Milestone Exit Criteria

- Product Slimming score reaches at least 88/100.
- First 15 Minutes readiness reaches at least 90/100.
- Reader, Note-Taker, and Game Designer pass automated and human-observed flows.
- Researcher and Community improve from PARTIAL to PASS or are explicitly positioned as secondary beta workflows.
- No P0 usability blockers remain.

## Release Recommendation

Continue controlled beta. Do not broaden public beta release messaging until Workflow Hardening 0.3 and at least one real usability-study round are complete.
