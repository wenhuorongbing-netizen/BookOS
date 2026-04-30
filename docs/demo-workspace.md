# BookOS Demo Workspace

The Demo Workspace is an optional practice area for learning BookOS without mixing practice records into a user's real reading and project knowledge base.

## Purpose

The workspace gives a new user safe, original sample records for the core BookOS loop:

- Read a demo book context.
- Inspect quick captures.
- Open a source-backed quote and action item.
- Review basic game design concepts.
- Open a demo project.
- Inspect source-backed project application, decision, and playtest finding.
- Open a source-linked forum thread.

## Safety Rules

- Demo mode is explicitly labeled in the UI.
- Demo content is original BookOS sample material.
- Demo records are tracked in `demo_records` by `user_id`, `entity_type`, and `entity_id`.
- Demo records are user-scoped and are not visible to other users.
- Demo page numbers are not invented. Unknown pages are stored as `null`.
- Source confidence for demo source references is `LOW`.
- Demo records are excluded from normal analytics by default.
- Analytics can include demo data only when the caller opts in with `includeDemo=true`.

## Demo Data

The backend creates the following sample records:

- Book: `Demo Game Design Notebook`
- Author: `BookOS Demo`
- Captures: original idea, quote-like sentence, action item, and concept marker samples
- Quote: an original BookOS sample sentence
- Action item: a demo readability task
- Concepts: `Core Loop`, `Feedback Loop`, `Game Feel`, `Meaningful Choice`
- Knowledge objects: a clear feedback design lens and one prototype task
- Project: `Demo Puzzle Adventure`
- Project records: problem, application, design decision, and playtest finding
- Forum thread: a clearly labeled demo discussion
- Entity links: real links connecting the demo book, quote, action item, concepts, knowledge objects, project, and forum thread

No copyrighted book passages are seeded.

## API Endpoints

- `GET /api/demo/status`: returns whether the current user has demo records and exposes key demo record IDs.
- `POST /api/demo/start`: creates the demo workspace if it does not already exist.
- `POST /api/demo/reset`: deletes the current user's demo records and recreates a fresh demo workspace.
- `DELETE /api/demo`: deletes only the current user's demo records.

All endpoints require authentication.

## Frontend Route

- `/demo`

The Demo Workspace page provides:

- Start Demo Workspace
- Reset Demo
- Delete Demo Data
- Graduate to Real Book
- Delete Demo and Start Real Workflow
- Training tutorial cards linked to executable use-case checklists
- Exit Demo
- Links into the demo book, project, quote, action item, graph, and forum thread when those records exist
- A safety panel explaining that demo records are labeled and isolated from normal analytics

The demo status panel shows:

- Active or inactive state
- Scoped demo record count
- Last reset time, based on the current demo record set creation time
- Included demo record types
- Analytics behavior, including that demo records are excluded from analytics by default

## Training Tutorials

The Demo Workspace is the recommended place to practice workflows before using real records. Tutorial cards on `/demo` link to executable use-case checklists:

- Demo: Capture and Convert -> `/use-cases/note-taker-capture-convert`
- Demo: Open Source -> `/use-cases/open-source-from-quote-or-action`
- Demo: Apply Quote to Project -> `/use-cases/apply-quote-to-game-project`
- Demo: Review Concept -> `/use-cases/review-concept-marker`
- Demo: Explore Graph -> `/use-cases/inspect-knowledge-graph`
- Demo: Forum Discussion -> `/use-cases/source-linked-forum-discussion`
- Demo: Export Demo Data -> `/use-cases/export-reading-knowledge`

Starting a tutorial does not create fake checklist completion. It opens the checklist and guides the user toward real demo records created only after the user explicitly starts Demo Workspace.

## Analytics Behavior

Normal analytics endpoints exclude demo records:

- `GET /api/analytics/reading`
- `GET /api/analytics/knowledge`
- `GET /api/analytics/books/{bookId}`

The same endpoints include demo records only when explicitly requested:

- `GET /api/analytics/reading?includeDemo=true`
- `GET /api/analytics/knowledge?includeDemo=true`
- `GET /api/analytics/books/{bookId}?includeDemo=true`

## Reset and Delete Behavior

Reset and delete remove only records listed in `demo_records` for the current user. They do not delete normal user records. Book author and tag join rows for demo books are cleaned before deleting the demo book to avoid foreign key failures.

## Limitations

- Demo scoping is currently implemented through the `demo_records` table, not per-entity `isDemo` columns.
- Demo records may still appear in normal list pages, but they are clearly titled/tagged as demo records.
- Demo records are excluded from analytics by default, but other feature-specific dashboards may still need explicit demo filters in later iterations.
