# BookOS Usability Study Plan

Last reviewed: 2026-05-01.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Research Status

No external moderated usability sessions have been conducted for this plan. This document is a study package for future human testing, not a claim that user research has already been completed.

## Study Goal

Validate whether a new user can understand BookOS and complete one source-backed reading knowledge loop in the first 15 minutes:

Add book -> capture idea -> convert capture -> open source -> choose a next path.

For game designers, validate whether the user can extend that loop:

Apply source-backed idea -> create project action or decision.

## Target Participants

Recruit 5 to 8 participants for the first round. Include at least one participant from each target profile:

- Reader: tracks books and reading progress.
- Note-taker: captures and organizes book notes.
- Game designer: applies reading knowledge to a game project.
- Researcher or student: builds concepts, reviews knowledge, and uses search.
- Community or forum user: discusses books, quotes, concepts, or projects.

Optional second-pass emphasis:

- Add one extra reader if first-book creation or capture is weak.
- Add one extra game designer if Project Mode creates confusion.

## Environment

- Use a local or staging BookOS beta environment.
- Do not use production user accounts.
- Use fresh accounts per participant.
- Use MockAIProvider only.
- Do not call external AI providers.
- Do not import copyrighted book passages.
- Ask participants to use original notes or public-domain-safe short examples.
- Screen/audio recording requires explicit participant consent.
- Do not collect sensitive personal reading notes unless the participant knowingly chooses to enter them.

## Study Tasks

Each participant should attempt the shared core tasks. Persona-specific tasks can be added when relevant.

1. Register a fresh account and choose a mode.
2. Add a book.
3. Capture an idea while reading.
4. Convert the capture.
5. Open the source of the converted record.
6. Apply an idea to a project.
7. Start a review.
8. Search for a created object.
9. Use the Demo Workspace.
10. Use graph from context.
11. Generate and handle a safe AI draft.

## Required Metrics

Record these metrics for every session:

- Time to first book.
- Time to first capture.
- Time to first conversion.
- Time to source open.
- Number of times the user asks, "What is this?"
- Number of wrong turns.
- Task completion rate.
- Confidence rating.
- Cognitive load rating.
- Feature overwhelm rating.

## Rating Scale

Use a 1 to 5 scale unless otherwise stated:

- 1: Very poor or very high friction.
- 2: Weak.
- 3: Acceptable with friction.
- 4: Clear enough for beta.
- 5: Strong and self-explanatory.

For cognitive load and feature overwhelm, record both the raw participant rating and the moderator interpretation. Lower overwhelm is better.

## Success Thresholds

BookOS is ready to continue beta expansion if:

- At least 80% of participants complete Add Book -> Capture -> Convert -> Open Source without moderator intervention.
- Median time to source open is under 15 minutes.
- No P0 usability blocker is observed.
- Average confidence is at least 4 out of 5 for Reader and Note-Taker paths.
- Game Designer participants can explain how reading knowledge becomes a project action.
- Advanced features do not prevent completion of the first loop.

## Failure Thresholds

Pause beta expansion if any of these occur:

- Users cannot identify the first action from Dashboard or onboarding.
- Users cannot tell whether a record is real, demo, or draft.
- Users cannot find source links after conversion.
- Users believe AI drafts overwrite original notes automatically.
- Users are blocked by Graph, Analytics, Import/Export, or Admin terminology before completing the first loop.

## Data Handling

- Store observation notes in the provided observation form.
- Separate participant identity from session notes where possible.
- Do not paste private participant notes into public issues.
- Do not quote participants by name unless consent is explicit.
- Prefer paraphrased research findings over long verbatim excerpts.

## Outputs

After the study, produce:

- Completed observation forms.
- Task timing summary.
- Completion-rate summary.
- P0, P1, and P2 usability issue list.
- Recommended Product Slimming 0.2 changes.
- PO decision: release with caveats, product slimming, workflow hardening, or pause feature expansion.

