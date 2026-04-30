# BookOS Usability Test Script

Last reviewed: 2026-05-01.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Moderator Opening

Thank you for helping us test BookOS. We are testing the product, not you. If something is confusing, that is useful feedback.

BookOS helps you turn reading into source-backed notes, knowledge, projects, and discussions.

Please think aloud as you work. Say what you expect to happen, what you are looking for, and anything that feels unclear.

Before we begin:

- May we record the screen and audio for research notes?
- Please do not enter private or sensitive information.
- Please use original notes, not copyrighted book passages.
- AI features in this test use MockAIProvider only and should act as draft-only assistance.

## Pre-Test Questions

1. What best describes your main use case: reading, note-taking, game design, research, forum discussion, or advanced knowledge management?
2. What tool do you currently use for reading notes or project knowledge?
3. When reading, how often do you capture notes, quotes, or actions?
4. If you work on games or projects, how do reading notes currently influence design decisions?
5. What would make a tool like BookOS feel useful in the first 15 minutes?

## Task 1: Register and Choose Mode

Prompt:

Create a new account and choose the mode that best matches how you want to use BookOS.

Observe:

- Does the user understand the modes?
- Does the user hesitate over terminology?
- Does the next page explain what to do?

Success:

- User registers.
- User reaches Dashboard or the guided first loop.
- User can explain why they chose the mode.

## Task 2: Add a Book

Prompt:

Add a book you want to track. Use a real title if you want, but do not paste copyrighted passages.

Observe:

- Time to first book.
- Whether Add Book is discoverable.
- Whether the user understands personal library versus book metadata.

Success:

- Book exists.
- User can open the book detail page.

## Task 3: Capture an Idea

Prompt:

Capture one original thought while reading. If you know a page, include it. If not, leave the page out.

Example syntax if requested:

```text
💡 p.12 This idea could improve my onboarding loop #idea [[Onboarding]]
✅ page 20 Test a clearer feedback cue #todo [[Feedback Loop]]
❓ 第12页 Why does this rule create tension? #question [[Meaningful Choice]]
```

Observe:

- Does the user notice Quick Capture?
- Does the parser preview make sense?
- Does the user understand that unknown page should stay unknown?

Success:

- Capture is saved.
- Parsed type, tags, concepts, and page behavior are visible.

## Task 4: Convert the Capture

Prompt:

Turn your capture into the record type that makes the most sense: note, quote, action, or reviewed concept.

Observe:

- Does the user know where to process captures?
- Does the suggested conversion match the parsed type?
- Does the user understand the difference between quote, action, note, and concept review?

Success:

- Capture is converted.
- Converted record appears in the relevant workspace.
- Capture does not remain incorrectly actionable.

## Task 5: Open Source

Prompt:

Find where the converted record came from.

Observe:

- Does the user see Open Source or Source Link?
- Does the source drawer/page explain book and page?
- Does the user notice if page is unknown?

Success:

- User opens a source-backed view.
- User can identify the source book.
- Unknown page is not guessed.

## Task 6: Apply an Idea to a Project

Use this task for game designers and as an optional task for others.

Prompt:

Create or open a project and apply your quote, concept, or source-backed idea to it.

Observe:

- Does the user understand why project application exists?
- Does the apply-to-project flow preserve source context visibly?
- Does the user understand project problem, application, and design decision?

Success:

- Project exists.
- A source-backed project application or decision is created.
- User can open the project cockpit and find it.

## Task 7: Start Review

Prompt:

Start a review session from a book, concept, or available review page.

Observe:

- Does the user understand Review versus Learning Progress?
- Does the review item explain what is being reviewed?
- Can source be opened from the review item?

Success:

- Review session starts.
- At least one review item is answered or marked.

## Task 8: Search for an Object

Prompt:

Find something you created using search.

Observe:

- Does the user use Cmd/Ctrl+K, the sidebar, or topbar search?
- Do result labels make sense?
- Can the user open the result or source?

Success:

- User finds and opens a created book, quote, action, concept, project, or forum item.

## Task 9: Use Demo Workspace

Prompt:

Open the Demo Workspace and explain what it is for.

Observe:

- Does the user understand demo data is separate from real data?
- Does the user understand reset/delete?
- Does the user find tutorial cards useful?

Success:

- User can identify demo mode and its safety boundary.

## Task 10: Use Graph From Context

Prompt:

Open a graph view from a book, concept, project, or source context.

Observe:

- Does graph feel useful or premature?
- Does empty state explain how links are created?
- Does the user understand nodes and source links?

Success:

- User opens a scoped graph or understands the honest empty state.

## Task 11: Use Mock AI Safely

Prompt:

Generate a draft suggestion, inspect it, then accept, edit, or reject it.

Observe:

- Does the user understand it is a draft?
- Does the provider label make sense?
- Does the user expect source content to be overwritten?

Success:

- Draft is handled safely.
- Original source content remains unchanged.

## Post-Test Questions

1. What did you think BookOS was for after the first screen?
2. What was the clearest action?
3. What was the most confusing term?
4. Which page had too many choices?
5. Did source links increase trust?
6. Did demo mode feel clearly separate from real data?
7. Did AI feel safe and optional?
8. Would you use BookOS again for your own reading or project work?

## Ratings

Ask the participant to rate:

- Confidence using BookOS after this session: 1 to 5.
- Cognitive load: 1 to 5, where 5 means low load.
- Feature overwhelm: 1 to 5, where 5 means not overwhelming.
- Likelihood of completing the first loop alone next time: 1 to 5.

## Moderator Closing

Thank you. We will use this to improve the product flow. We will not describe this as completed user research until sessions have actually been conducted and synthesized.

