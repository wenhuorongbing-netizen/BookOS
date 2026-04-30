# BookOS First 15 Minutes Guide

Last reviewed: 2026-04-30.

Reviewed SHA: `3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`.

## Goal

In the first 15 minutes, a new user should complete one valuable loop:

Add book -> capture reading thought -> convert it -> open source.

Game designers should then extend the loop:

Apply source-backed idea -> create project decision.

## Minute 0-2: Register and Choose Mode

1. Register.
2. Read the onboarding sentence: "BookOS helps you turn reading into source-backed notes, knowledge, projects, and discussions."
3. Choose a mode:
   - Reader Mode if you want reading progress and capture.
   - Note-Taker Mode if you want notes and capture processing.
   - Game Designer Mode if you want project application.
   - Researcher Mode if concepts/review matter most.
   - Advanced Mode only if you already know you need graph, import/export, or AI drafts.

Expected result: Dashboard opens with a practical next step.

## Minute 2-5: Add First Book

1. Click Add Book.
2. Enter title and author.
3. Add it to your library.
4. Set status to Reading.
5. Open Book Detail.

Expected result: the book title, progress area, and Quick Capture are visible.

## Minute 5-8: Capture One Thought

Use original text, not copied copyrighted passages.

Examples:

```text
💡 p.12 This might improve my onboarding loop #idea [[Onboarding]]
✅ page 20 Test a clearer feedback cue #todo [[Feedback Loop]]
❓ 第42页 Why does this rule create tension? #question [[Meaningful Choice]]
```

If you do not know the page, omit it. BookOS should keep page unknown instead of guessing.

Expected result: parser preview shows type, page if known, tags, concepts, and source context.

## Minute 8-11: Process the Capture

1. Open Capture Inbox or use the post-save next-step buttons.
2. Convert the capture:
   - Quote if it should be resurfaced later.
   - Action if it should become a task.
   - Note if it should become a longer thought.
   - Concept Review if it includes `[[Concept]]`.
3. Open the created object.

Expected result: the capture becomes a source-backed record and no longer stays incorrectly actionable.

## Minute 11-13: Open Source

1. Click Open Source from the quote/action/note/concept.
2. Confirm the source book is shown.
3. Confirm page is shown only if supplied.
4. Confirm unknown page remains unknown.

Expected result: the user trusts where the idea came from.

## Minute 13-15: Choose Next Path

Reader:

- Continue reading.
- Capture another thought.
- Start Review when you have a few source-backed items.

Note-taker:

- Create a Markdown note.
- Process remaining captures.
- Review concepts.

Game designer:

- Create a project.
- Apply the quote/concept to the project.
- Create a design decision or playtest plan.

Researcher:

- Open Concept Detail.
- Inspect backlinks.
- Open scoped graph.
- Start a review session.

Community user:

- Start a source-linked forum discussion from a quote, concept, or book.

Advanced user:

- Open Graph from a scoped entity.
- Export data.
- Generate a MockAIProvider draft and confirm it does not overwrite source content.

## What to Avoid in the First 15 Minutes

- Do not start with global Graph unless you already have data.
- Do not start with Import/Export unless data portability is your first concern.
- Do not start with AI drafts until you understand source references.
- Do not create Knowledge Objects manually before trying capture and concept review.

## Success Criteria

The first 15 minutes are successful if the user can answer:

- Which book am I working on?
- What did I capture?
- What did BookOS parse?
- What did I convert it into?
- Where did the idea come from?
- What should I do next?
