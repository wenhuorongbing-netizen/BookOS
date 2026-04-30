# BookOS Product Slimming 0.2 Roadmap

Last reviewed: 2026-05-01.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Roadmap Status

This is a planning document for the next milestone. It does not claim that external usability research has been performed.

## Recommended Milestone

Product Slimming 0.2.

Pause broad feature expansion until the first-day user journey is simpler and the strongest workflows are easier to discover.

## Product Goal

Make BookOS feel like a hands-on reading-to-knowledge tool instead of a collection of modules.

The default experience should teach:

Add book -> capture -> process capture -> open source -> review or apply to project.

## Guiding Rules

- Do not remove the advanced system.
- Do not show every capability as equally important.
- Prefer task labels over implementation labels.
- Show advanced modules only when users have data or explicitly choose Advanced Mode.
- Keep source traceability visible in every core workflow.
- Keep AI draft-only and optional.
- Do not invent page numbers or demo content.

## Workstream 1: Navigation and Mode Defaults

Goal: reduce visible choices without breaking deep links.

Changes:

- Keep Dashboard, Library, Capture, Notes, Projects, and Review as the primary navigation set.
- Keep Quotes, Actions, Concepts, Knowledge, Daily, and Forum secondary.
- Keep Knowledge Graph, Analytics, Import/Export, AI, and Ontology Import in More or Advanced.
- Make mode switching easier to find and explain why the Dashboard changes by mode.
- Keep Admin Ontology visible only for admins.

Success criteria:

- New users can identify the primary action in under 10 seconds.
- Advanced users can still access every route.
- Normal users do not see admin-only controls.

## Workstream 2: First Valuable Loop

Goal: make the first 15 minutes repeatable and measurable.

Changes:

- Make Guided First Loop the main empty-state CTA.
- Promote Add Book, Capture, Process Captures, and Open Source.
- Add per-step recovery when users exit the flow.
- Keep conversion suggestions simple: Quote, Action, Note, Concept Review.

Success criteria:

- At least 80% of test participants complete Add Book -> Capture -> Convert -> Open Source.
- Median time to source open is under 15 minutes.

## Workstream 3: Quick Capture and Process Captures

Goal: make the parser feel learnable.

Changes:

- Continue moving "Capture Inbox" copy to "Process Captures."
- Keep one-click examples, live parser preview, and beginner structured capture.
- Show malformed page warnings.
- Keep examples as input-only; never save fake captures automatically.

Success criteria:

- Users understand emoji, page, tag, and concept markers without external explanation.
- Unknown page remains visibly unknown.

## Workstream 4: Project Mode as a Guided Workflow

Goal: make Game Designer Mode demonstrate BookOS value.

Changes:

- Lead with Apply Knowledge to Project.
- Keep project problem, application, decision, playtest, and iteration as a guided sequence.
- Keep the transactional wizard final review.
- Improve success/failure copy so users know exactly what was created.

Success criteria:

- Game designers can explain how reading knowledge became a design action.
- Wizard failures create no partial records.

## Workstream 5: Researcher and Community Context

Goal: keep less common workflows useful without overwhelming first-day users.

Changes:

- Launch Researcher workflows from Concept Review, Review, and scoped Knowledge Graph.
- Launch Community workflows from source-backed records rather than empty forum pages.
- Provide stronger forum templates for Quote Discussion, Concept Discussion, and Project Critique.

Success criteria:

- Researcher and Community paths improve from PARTIAL to PASS in usability scoring.

## Workstream 6: Advanced Surfaces

Goal: hide complexity until useful.

Changes:

- Collapse Graph, Analytics, Import/Export, AI Settings, and Ontology Import by default.
- Show graph entry points from book, concept, project, and source contexts.
- Show analytics only when real data exists.
- Show Draft Assistant only with source context or explicit Advanced Mode.

Success criteria:

- Advanced features do not distract from first loop completion.
- Empty states explain how to create useful data.

## Workstream 7: Terminology Cleanup

Goal: replace implementation terms with task language.

Priority renames:

1. Capture Inbox -> Process Captures.
2. Action Items -> Actions.
3. Knowledge Objects -> Design Knowledge.
4. Source Reference -> Source Link.
5. Entity Links -> Relationships.
6. Backlinks -> Related Links.
7. Mastery -> Learning Progress.
8. AI Suggestions -> Draft Assistant.
9. Admin Ontology -> Ontology Import.
10. Graph -> Knowledge Graph.

Success criteria:

- Help text and page headers use user-facing labels.
- Technical docs continue to map user labels to backend entity names.

## Workstream 8: Measurement and Human Study

Goal: validate with real users instead of internal assumptions.

Changes:

- Run the usability study described in `docs/usability-study-plan.md`.
- Use `docs/usability-test-script.md`.
- Capture observations in `docs/usability-observation-form.md`.
- Convert findings into P0, P1, and P2 product changes.

Success criteria:

- Real moderated sessions are completed.
- Findings are not presented as research until sessions occur.
- PO decision is based on observed task completion and friction metrics.

## Top 10 Next UX Changes

1. Make Guided First Loop the dominant empty Dashboard action.
2. Rename remaining "Capture Inbox" surfaces to "Process Captures."
3. Fold Learning Progress into the Review journey for normal users.
4. Keep Knowledge Graph scoped to current book, concept, or project unless Advanced Mode is selected.
5. Add a clearer "Why am I seeing this?" explanation on mode-specific Dashboards.
6. Make Demo Workspace tutorials point to executable checklists.
7. Improve Project Wizard final review, retry, and created-record summary copy.
8. Turn Forum start points into source-context actions.
9. Keep Import/Export in More and explain it as backup/portability.
10. Add telemetry-free timing checkpoints for usability tests where safe.

## Top 10 Bugs or Friction Points to Watch

1. Users may still confuse raw capture, note, quote, and action.
2. Project Mode can feel like too many sub-workflows at once.
3. Researcher Mode lacks a single obvious first action.
4. Forum is weak when started without source context.
5. Graph can feel empty or abstract before relationships exist.
6. Import/Export is powerful but not first-day relevant.
7. Draft Assistant may still sound more authoritative than intended.
8. Review and Learning Progress can feel like separate systems.
9. Admin Ontology must stay hidden from non-admin users.
10. Dirty worktree size remains a release-management risk until sliced.

## Release Recommendation

Controlled beta can continue with caveats. Broad public-beta expansion should wait for Product Slimming 0.2 and at least one real usability-study round.

