# BookOS Use Case Playbook

Last reviewed: 2026-04-30.

Reviewed SHA: `3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`.

## Purpose

This playbook describes the hands-on workflows a product owner, QA tester, or beta user should use to judge whether BookOS is understandable without engineering explanation.

Use this with:

- `docs/first-15-minutes.md`
- `docs/hands-on-beta-ux-report.md`
- `docs/manual-release-qa.md`
- `/use-cases` in the app
- `/demo` in the app

## Rules

- Use original notes and safe demo content only.
- Do not paste copyrighted book passages.
- Do not invent page numbers.
- If page is unknown, leave it unknown.
- Verify source references after every conversion or project application.
- Treat AI output as draft-only.

## Playbook 1: First-Time Reader

Primary user question: "Can I start tracking one book and capture one useful thing?"

Steps:

1. Register a fresh user.
2. Choose Reader Mode.
3. Add a book.
4. Add it to the personal library.
5. Set status to Reading.
6. Open Book Detail.
7. Use Quick Capture with original text, for example: `💬 p.12 A short original note about why this idea matters #quote [[Useful Idea]]`.
8. Convert the capture to Quote.
9. Open the Quote.
10. Click Open Source.

Success means:

- The user can tell which book they are working in.
- The capture is attached to the book.
- Parsed page/tag/concept information is visible.
- Quote conversion works.
- Open Source returns to the correct book/source context.

## Playbook 2: Note-Taker

Primary user question: "Can I write notes and process rough reading thoughts?"

Steps:

1. Choose Note-Taker Mode during onboarding or from the user menu.
2. Open or create a book.
3. Create a Markdown note.
4. Add a parsed note block or quick capture.
5. Open the Quick Capture guide.
6. Try one quote example and one action example.
7. Confirm example insertion does not save fake records.
8. Save one real capture.
9. Open Capture Inbox.
10. Convert one capture to Action Item.

Success means:

- Parser syntax is learnable in the UI.
- Example insertion only fills the input.
- Saved captures appear in the inbox.
- Action item conversion preserves source context.

## Playbook 3: Game Designer

Primary user question: "Can I turn reading into a game-design action?"

Steps:

1. Choose Game Designer Mode.
2. Add a game design book.
3. Capture or create one quote/concept from original text.
4. Create a project.
5. Open the quote or concept.
6. Choose Apply to Project or the guided project wizard.
7. Define a project problem.
8. Create a source-backed project application.
9. Create a design decision.
10. Open Project Cockpit.

Success means:

- Project creation is discoverable.
- Apply-to-project flow is visible from source-backed objects.
- Project records appear in Project Detail.
- Source reference is visible from project application/decision paths where supported.

## Playbook 4: Researcher

Primary user question: "Can I build a source-backed concept trail?"

Steps:

1. Create or open a book.
2. Create a note or capture with `[[Concept Name]]`.
3. Open Concept Review.
4. Create or accept the concept.
5. Open Concept Detail.
6. Verify source references and backlinks.
7. Open Graph scoped to the concept.
8. Start a Review session from the concept or book where available.

Success means:

- Concepts are reviewed before being saved.
- No concept is auto-created without user confirmation.
- Source references and backlinks remain visible.
- Graph shows real links or an honest empty state.

## Playbook 5: Community User

Primary user question: "Can I discuss a source-backed idea?"

Steps:

1. Open a book, quote, concept, or project source.
2. Choose a forum discussion action where available, or open Forum.
3. Create a source-linked thread.
4. Add a comment.
5. Open the related source context.

Success means:

- Forum is structured by category/template.
- Thread context can reference a book/source/concept/project.
- Comments work.
- Private source context is not leaked to unauthorized users.

## Playbook 6: Advanced User

Primary user question: "Can I inspect, export, and use draft assistance safely?"

Steps:

1. Open Knowledge Graph from More or a scoped entity.
2. Confirm graph uses real links or honest empty state.
3. Open Import/Export.
4. Export current user data.
5. Generate a MockAIProvider draft from the Right Rail.
6. Edit the draft.
7. Accept one draft.
8. Reject another draft.
9. Confirm original source content is not overwritten.

Success means:

- Advanced features are available but not primary.
- Export is user-owned.
- AI provider is clearly mock/draft-only.
- Accept/edit/reject changes the suggestion lifecycle, not original content.

## First-Class Use Cases

- Add a book and track reading.
- Capture while reading.
- Convert capture to quote/action/note/concept.
- Open source from derived objects.
- Apply source-backed knowledge to a project.
- Review source-backed material.

## Still-Confusing Use Cases

- Starting with global graph before source-backed records exist.
- Understanding Knowledge Objects without examples.
- Using Forum without a source context.
- Using Mastery without first completing review sessions.
- Import/export without reading the preview warnings.
- AI drafts without understanding the no-overwrite rule.
