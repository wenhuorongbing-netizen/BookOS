# BookOS First 15 Minutes Guide

Last reviewed: 2026-05-01.

Reviewed SHA: `c62e9eaa163e9ae7192046dceda09a6bf2470091`.

## Purpose

This guide describes the intended first-session path for a new BookOS user. It is based on product design and automated QA, not external user research.

## First 15 Minutes Goal

Complete one valuable source-backed loop:

Add book -> capture reading thought -> process capture -> open source.

Game designers can then extend it:

Apply source-backed idea -> create project decision.

## Minute 0-2: Register and Choose Mode

1. Register a new account.
2. Read the onboarding sentence: "BookOS helps you turn reading into source-backed notes, knowledge, projects, and discussions."
3. Choose the closest mode:
   - Reader Mode: reading progress and capture.
   - Note-Taker Mode: notes, captures, quotes, and actions.
   - Game Designer Mode: project application.
   - Researcher Mode: concepts, review, and scoped graph.
   - Community Mode: source-linked discussion.
   - Advanced Mode: graph, import/export, and Draft Assistant.

Expected result: the app opens a guided or mode-focused path rather than a wall of modules.

## Minute 2-5: Add First Book

1. Choose Add Book.
2. Enter title and author.
3. Add the book to your library.
4. Set status to Reading.
5. Open Book Detail.

Expected result: the user sees the current book context and Quick Capture.

## Minute 5-8: Capture One Original Thought

Use original notes, not copied copyrighted passages.

Example capture patterns:

```text
idea p.12 This might improve my onboarding loop #idea [[Onboarding]]
todo page 20 Test a clearer feedback cue #todo [[Feedback Loop]]
question chapter 2 Why does this rule create tension? #question [[Meaningful Choice]]
```

Emoji markers can also be used if the UI supports them. If the page is unknown, omit it. BookOS must keep unknown page numbers as null instead of guessing.

Expected result: parser preview shows type, page if known, tags, concepts, and source context.

## Minute 8-11: Process the Capture

1. Open Process Captures or use the post-save next-step buttons.
2. Convert the capture:
   - Quote if it should be resurfaced or cited.
   - Action if it should become a task.
   - Note if it should become a longer thought.
   - Concept Review if it includes `[[Concept Name]]`.
3. Open the created object.

Expected result: the capture becomes a source-backed record and is no longer incorrectly actionable.

## Minute 11-13: Open Source

1. Click Open Source from the quote, action, note, or concept.
2. Confirm the source book is shown.
3. Confirm page is shown only if supplied.
4. Confirm unknown page remains unknown.

Expected result: the user can trust where the idea came from.

## Minute 13-15: Choose Next Path

Reader:

- Continue reading.
- Capture another thought.
- Start Review after a few source-backed items exist.

Note-Taker:

- Create a Markdown note.
- Process remaining captures.
- Convert captures into quote, action, note, or concept review.

Game Designer:

- Create a project.
- Apply a quote or concept to the project.
- Create a design decision or playtest plan.

Researcher:

- Capture with `[[Concept Name]]`.
- Review the concept.
- Open Concept Detail.
- Open scoped Knowledge Graph.
- Start a review session.

Community:

- Start a source-linked forum discussion from a book, quote, concept, project, or source.
- Add a comment.
- Reopen the attached source context.

Advanced:

- Open scoped graph after source-backed records exist.
- Export data.
- Generate a MockAIProvider Draft Assistant suggestion and confirm it does not overwrite source content.

## What to Avoid in the First 15 Minutes

- Do not start with global Knowledge Graph unless data exists.
- Do not start with Import/Export unless backup is the goal.
- Do not start with Draft Assistant until source links are understood.
- Do not create Design Knowledge manually before trying capture and concept review.
- Do not create fake user data unless Demo Workspace is explicitly started.

## Automated Coverage

The current Product Slimming gate is:

```bash
cd frontend
npm run e2e:usability
```

It covers:

- Reader Mode.
- Note-Taker Mode.
- Game Designer Mode.
- Researcher Mode.
- Community Mode.
- Advanced Mode.
- Demo Workspace.
- Use-case checklist routing.

Latest status: PASS, 8 tests, on SHA `c62e9eaa163e9ae7192046dceda09a6bf2470091`.

## Success Criteria

The first session succeeds when the user can answer:

- Which book am I working on?
- What did I capture?
- What did BookOS parse?
- What did I convert it into?
- Where did the idea come from?
- What should I do next?

## Current Readiness

First 15 Minutes readiness: 84/100 by internal heuristic QA.

This is not human research. Run the usability study package before making external validation claims.
