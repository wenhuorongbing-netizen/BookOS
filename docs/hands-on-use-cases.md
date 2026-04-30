# BookOS Hands-On Use Cases

BookOS is easier to learn by workflow than by module. These scenarios teach concrete paths from reading to source-backed notes, knowledge, project action, discussion, search, graph exploration, export, and safe draft assistance.

Rules for every use case:

- Use original notes and examples only.
- Do not add copyrighted book passages as demo content.
- Do not invent page numbers.
- Unknown page numbers must remain null or visibly unknown.
- Source-backed objects should preserve book, capture, note, page if known, and source confidence where available.
- AI suggestions are drafts only and must never overwrite source content automatically.

## Practice Safely in the Demo Workspace

Who this is for: New users who want to learn BookOS without polluting their real library, concepts, analytics, or projects.

Goal: Explore the reading-to-knowledge-to-project loop with clearly labeled original demo records.

Time required: 5-10 minutes.

Prerequisites:

- A logged-in BookOS account.
- No real book or project data is required.

Steps:

1. Open `/demo`.
2. Click `Start Demo Workspace`.
3. Open the demo book, quote, action item, project, graph, or forum thread from the demo page.
4. Inspect source references and confirm unknown pages are shown as unknown.
5. Reset the demo when you want a clean practice state, or delete demo data when finished.

What you will create:

- Demo-labeled book, captures, quote, action item, concepts, knowledge objects, project records, forum thread, and graph links.

Source references preserved: demo source references are original sample records with `LOW` confidence and `null` page numbers when no page is known.

Where to go next: repeat any hands-on use case using demo records, then create your first real book or project when ready.

## Track a Book From Start to Finish

Who this is for: Readers who want BookOS to become a reliable reading record before using advanced knowledge tools.

Goal: Make the current book context visible and measurable.

Time required: 5-8 minutes.

Prerequisites:

- A book title and author you want to track.
- Optional total page count if you know it.

Steps:

1. Open `/books/new` and create the book record.
2. Open the book detail page and add the book to your personal library.
3. Set reading state from the book detail or library workflow.
4. Update progress and rating as you read.

What you will create:

- Book record.
- Personal library entry.
- Reading status.
- Progress and rating data.

Source references preserved: no derived source reference is created until you add notes, captures, quotes, or actions.

Where to go next: capture while reading, create a note, or open current reading.

## Capture an Idea While Reading

Who this is for: Readers who need the fastest path from reading thought to source-backed inbox item.

Goal: Save the raw thought without losing source context.

Time required: 2-4 minutes.

Prerequisites:

- At least one book in your library.
- A short original thought you want to capture.

Steps:

1. Open the current book from `/my-library` or `/currently-reading`.
2. Use quick capture with an emoji marker, known page if any, original thought, tag, and concept marker.
3. Open `/captures/inbox` and verify parsed type, page, tags, and `[[Concept]]` markers.

What you will create:

- Raw capture.
- Parsed type.
- Tags.
- Concept markers.
- Book source context.

Source references preserved: book ID is preserved; page stays null when unknown; known page marker is preserved without inventing extra pages.

Where to go next: convert to quote, convert to action item, or review concept marker.

## Turn a Capture Into a Quote

Who this is for: Readers who want memorable passages or short observations to be searchable and source-linked.

Goal: Create a quote without losing the original capture source.

Time required: 3-5 minutes.

Prerequisites:

- A capture marked as quote or a capture you want to preserve as a quote.
- Known page only if you actually captured it.

Steps:

1. Open `/captures/inbox`.
2. Choose `Convert to Quote`.
3. Open `/quotes` and confirm the quote appears with source book and page if available.

What you will create:

- Quote.
- Quote source reference.
- Searchable quote record.

Source references preserved: raw capture link, book context, and page only if supplied.

Where to go next: open source from quote or apply quote to project.

## Turn a Capture Into an Action Item

Who this is for: Readers and designers who want notes to become visible next actions.

Goal: Create an action item that stays linked to the book or capture that inspired it.

Time required: 3-5 minutes.

Prerequisites:

- A capture with an action marker or a capture that should become a task.

Steps:

1. Open `/captures/inbox`.
2. Confirm or edit the action title in the conversion dialog.
3. Open `/action-items` to complete, reopen, edit, archive, or open source.

What you will create:

- Action item.
- Completion state.
- Action source reference.

Source references preserved: book and raw capture links are preserved; unknown page remains unknown.

Where to go next: open source from action or apply to project.

## Turn a Concept Marker Into a Reviewed Concept

Who this is for: Researchers and game designers building a controlled vocabulary over time.

Goal: Avoid auto-creating messy concepts while preserving the source that mentioned them.

Time required: 4-7 minutes.

Prerequisites:

- A capture or note block containing a `[[Concept Name]]` marker.

Steps:

1. Open `/captures/inbox`.
2. Open concept review for a capture with parsed concept markers.
3. Accept an existing concept, rename before saving, create a new concept, or skip.
4. Open `/concepts` and confirm source references and backlinks.

What you will create:

- Reviewed concept.
- Concept tags.
- Concept source link.

Source references preserved: capture source is preserved; page data comes only from the original capture.

Where to go next: inspect graph connections or apply concept to project.

## Open Source From a Quote or Action

Who this is for: Anyone who needs trustable source traceability instead of disconnected notes.

Goal: Verify where an idea came from before using it.

Time required: 2-3 minutes.

Prerequisites:

- At least one quote or action item with a source reference.

Steps:

1. Open `/quotes` or `/action-items`.
2. Choose a derived record that shows source information.
3. Click `Open Source`.
4. Verify book, page if known, source text, and confidence.

What you will create: no new object; this is a verification workflow.

Source references preserved: existing source reference is read and opened; unknown page is displayed as unknown instead of guessed.

Where to go next: apply quote to project or start a source-linked forum discussion.

## Apply a Quote to a Game Project

Who this is for: Game designers who want reading to influence project decisions.

Goal: Connect a quote to a practical design note for a project.

Time required: 5-8 minutes.

Prerequisites:

- At least one quote.
- At least one project.
- A design reason for applying the quote.

Steps:

1. Open `/quotes` and pick a source-backed quote.
2. Click `Apply to Project`.
3. Choose project, application type, title, and design note.
4. Open `/projects` and inspect the Project Cockpit.
5. Confirm the application source reference is visible.

What you will create:

- Project application.
- Project knowledge link where supported.
- Source-backed design note.

Source references preserved: quote source reference is preserved; book and page remain traceable when available.

Where to go next: create a design decision or run a lens review.

## Run a Design Lens Review on a Project

Who this is for: Game designers evaluating whether a reading-derived lens changes a design choice.

Goal: Create a source-aware review answer that can influence the project.

Time required: 8-12 minutes.

Prerequisites:

- At least one project.
- A design lens or diagnostic knowledge object.

Steps:

1. Open `/projects`.
2. Open the relevant project.
3. Open Lens Reviews.
4. Select a real lens or diagnostic question.
5. Write the review answer and attach source context when appropriate.

What you will create:

- Project lens review.
- Optional score.
- Source-backed review answer.

Source references preserved: lens source is preserved when attached; project review remains user-owned.

Where to go next: create a design decision or playtest finding.

## Create a Playtest Finding From Project Work

Who this is for: Designers validating whether source-backed reading ideas work in practice.

Goal: Turn playtest observations into trackable evidence.

Time required: 5-10 minutes.

Prerequisites:

- At least one project.
- A real observation from project work or playtesting.

Steps:

1. Open `/projects`.
2. Navigate to the playtest area for the relevant project.
3. Create a finding with observation, severity, recommendation, and status.
4. Attach a source reference only when a reading source directly informed the finding.

What you will create:

- Playtest finding.
- Recommendation.
- Optional source-backed evidence link.

Source references preserved: source reference is optional; no page is invented when the finding comes from project work only.

Where to go next: inspect project graph or start a project critique thread.

## Use Daily Prompt to Create a Project Task

Who this is for: Readers who want daily resurfacing to produce project momentum.

Goal: Convert a prompt into a project application, problem, or task where supported.

Time required: 4-8 minutes.

Prerequisites:

- A daily prompt.
- A project if you want to apply the prompt to design work.

Steps:

1. Open `/daily`.
2. Review the daily sentence and design prompt.
3. Open source if the prompt is source-backed.
4. Open `/projects` and create a project application or task only when the UI exposes a real action.

What you will create:

- Daily reflection.
- Optional project application or task when supported.

Source references preserved: template prompts are labeled as templates; source-backed prompts preserve their source reference.

Where to go next: open Projects or Review.

## Start a Forum Discussion From a Quote or Concept

Who this is for: Readers and designers who want discussion to stay tied to evidence.

Goal: Start a structured thread without turning the forum into a disconnected comment dump.

Time required: 4-7 minutes.

Prerequisites:

- A quote, concept, book, project, or source reference worth discussing.

Steps:

1. Choose source context from `/quotes`, `/concepts`, `/my-library`, or `/projects`.
2. Open `/forum/new`.
3. Use a structured template such as Quote Discussion, Concept Discussion, or Project Critique.
4. Add a focused comment or question.

What you will create:

- Forum thread.
- Forum comment.
- Optional related source context.

Source references preserved: private source context must not be exposed to users who cannot access it; thread context should link to real entities only.

Where to go next: search discussions or apply discussion outcomes to a project.

## Search and Rediscover Old Knowledge

Who this is for: Users with enough material that browsing by module is slower than search.

Goal: Find the right object and reopen its source or detail page.

Time required: 1-3 minutes.

Prerequisites:

- At least one created record, such as a book, quote, concept, or project.

Steps:

1. Open Dashboard or use the sidebar Search action.
2. Press Cmd/Ctrl+K if available.
3. Search by title, text, concept, project, or source-backed phrase you created.
4. Open the result or source where available.

What you will create: no new object; this is a rediscovery workflow.

Source references preserved: search results expose source reference IDs only when the user can access them.

Where to go next: inspect graph or export knowledge.

## Use Graph to Inspect Knowledge Connections

Who this is for: Users who want to understand how reading knowledge connects across the system.

Goal: Explore real relationships without relying on fake nodes.

Time required: 3-6 minutes.

Prerequisites:

- Real links from concepts, source references, project applications, or entity links.

Steps:

1. Open `/graph`.
2. Filter by entity type, relationship type, book, concept, project, or source confidence.
3. Open a node detail page or source reference.
4. Create a manual relationship only when the graph workspace supports it and both entities are accessible.

What you will create:

- Optional manual relationship when supported by the graph workspace.

Source references preserved: graph edges must come from real source references or explicit entity links; unknown pages remain unknown.

Where to go next: review concepts or open projects.

## Export Your Reading Knowledge

Who this is for: Users who need backup, portability, or offline review.

Goal: Get user-owned data out of BookOS without leaking other users data.

Time required: 3-5 minutes.

Prerequisites:

- User-owned records to export.
- A clear export scope such as all data, one book, quotes, actions, or concepts.

Steps:

1. Open `/import-export/export`.
2. Choose export mode, scope, and format.
3. Review what is included.
4. Download safely.

What you will create:

- JSON backup.
- Markdown export.
- CSV export where useful.

Source references preserved: export includes only source references the current user can access; unknown pages remain null or unknown in export output.

Safety note: exports may include private user-generated content. Treat downloaded files as sensitive.

Where to go next: import data preview or search exported knowledge first.

## Use Mock AI Safely as a Draft Helper

Who this is for: Users who want optional assistance without automatic content changes.

Goal: Create, inspect, edit, accept, or reject a draft suggestion safely.

Time required: 4-6 minutes.

Prerequisites:

- A note, source-backed object, or current book context.
- MockAIProvider enabled in local/dev.

Steps:

1. Open a source-backed context from `/my-library`, `/notes`, `/quotes`, or `/projects`.
2. Generate a draft only where the UI clearly labels the provider as MockAIProvider or configured provider.
3. Review before accepting.
4. Edit, accept, or reject the suggestion.

What you will create:

- AI suggestion draft.
- AI interaction record.
- Optional accepted draft state.

Source references preserved: source inputs used for the suggestion should be recorded; drafts do not become authoritative content automatically.

Safety note: AI suggestions are drafts only. BookOS must work without any external AI key and must never overwrite source content automatically.

Where to go next: open source or review the generated draft history.
