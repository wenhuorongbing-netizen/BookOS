# BookOS API Endpoint Inventory

Last updated: 2026-04-30.

Verification: application `/api/**` controller mappings are covered by
`EndpointContractIntegrationTest`. `GET /actuator/health` is provided by Spring
Boot Actuator rather than an application controller.

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Users:

- `GET /api/users/me/profile`
- `PUT /api/users/me/onboarding`
- `GET /api/users`

Health:

- `GET /api/health`
- `GET /actuator/health`

Demo Workspace:

- `GET /api/demo/status`
- `POST /api/demo/start`
- `POST /api/demo/reset`
- `DELETE /api/demo`

Demo endpoint rules:

- Demo endpoints require authentication and are scoped to the current user.
- Demo records are tracked in `demo_records` and clearly labeled as demo.
- Demo content is original BookOS sample material only.
- Demo pages are not invented; unknown page values stay `null`.
- Normal analytics exclude demo records unless `includeDemo=true`.

Books and user library:

- `GET /api/books`
- `POST /api/books`
- `GET /api/books/{id}`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`
- `POST /api/books/{id}/add-to-library`
- `GET /api/user-books`
- `PUT /api/user-books/{id}/status`
- `PUT /api/user-books/{id}/progress`
- `PUT /api/user-books/{id}/rating`
- `GET /api/user-books/currently-reading`
- `GET /api/user-books/five-star`
- `GET /api/user-books/anti-library`

Notes, blocks, parser, captures:

- `GET /api/books/{bookId}/notes`
- `POST /api/books/{bookId}/notes`
- `GET /api/notes/{id}`
- `PUT /api/notes/{id}`
- `DELETE /api/notes/{id}`
- `POST /api/notes/{id}/blocks`
- `PUT /api/note-blocks/{id}`
- `DELETE /api/note-blocks/{id}`
- `POST /api/parser/preview`
- `POST /api/captures`
- `GET /api/captures`
- `GET /api/captures/inbox`
- `GET /api/captures/{id}`
- `PUT /api/captures/{id}`
- `PUT /api/captures/{id}/archive`
- `POST /api/captures/{id}/convert`
- `POST /api/captures/{id}/convert/quote`
- `POST /api/captures/{id}/convert/action-item`
- `POST /api/captures/{id}/review/concepts`

Quotes:

- `GET /api/quotes`
- `POST /api/quotes`
- `GET /api/quotes/{id}`
- `PUT /api/quotes/{id}`
- `DELETE /api/quotes/{id}`

Action items:

- `GET /api/action-items`
- `POST /api/action-items`
- `GET /api/action-items/{id}`
- `PUT /api/action-items/{id}`
- `PUT /api/action-items/{id}/complete`
- `PUT /api/action-items/{id}/reopen`
- `DELETE /api/action-items/{id}`

Source references and links:

- `GET /api/source-references`
  - Supports `bookId`.
  - Supports `entityType` + `entityId` for `BOOK`, `NOTE`, `NOTE_BLOCK`, `RAW_CAPTURE`/`CAPTURE`, `QUOTE`, `ACTION_ITEM`, `CONCEPT`, `KNOWLEDGE_OBJECT`, `FORUM_THREAD`, `DAILY_PROMPT`/`DAILY_DESIGN_PROMPT`, `DAILY_SENTENCE`, `PROJECT_PROBLEM`, `PROJECT_APPLICATION`, `DESIGN_DECISION`, `PLAYTEST_FINDING`, `PROJECT_LENS_REVIEW`, `PROJECT_KNOWLEDGE_LINK`, and `SOURCE_REFERENCE`.
- `GET /api/source-references/{id}`
- `GET /api/books/{bookId}/source-references`
- `GET /api/notes/{noteId}/source-references`
- `GET /api/captures/{captureId}/source-references`
- `GET /api/entity-links`
  - Supports `sourceType` + `sourceId` or `targetType` + `targetId`.
- `POST /api/entity-links`
- `PUT /api/entity-links/{id}`
- `DELETE /api/entity-links/{id}`
- `GET /api/backlinks`
  - Supports `entityType` + `entityId` for core reading, knowledge, forum, daily prompt, and project entities.

Concepts and knowledge objects:

- `GET /api/concepts`
- `POST /api/concepts`
- `GET /api/concepts/{id}`
- `PUT /api/concepts/{id}`
- `DELETE /api/concepts/{id}`
- `GET /api/books/{bookId}/concepts`
- `GET /api/knowledge-objects`
- `POST /api/knowledge-objects`
- `GET /api/knowledge-objects/{id}`
- `PUT /api/knowledge-objects/{id}`
- `DELETE /api/knowledge-objects/{id}`

Admin ontology import:

- `GET /api/admin/ontology/default`
- `POST /api/admin/ontology/import/default`
- `POST /api/admin/ontology/import`

Daily:

- `GET /api/daily/today`
- `POST /api/daily/regenerate`
- `POST /api/daily/skip`
- `POST /api/daily/reflections`
- `GET /api/daily/history`
- `POST /api/daily/create-prototype-task`

Learning, review, mastery, and analytics:

Implementation source of truth: `ReadingSessionController`, `ReviewController`, `MasteryController`, `AnalyticsController`, and `LearningService`.

- `GET /api/reading-sessions` - `ReadingSessionController#list`; current user's reading sessions.
- `POST /api/reading-sessions/start` - `ReadingSessionController#start`; starts a current-user book reading session.
- `PUT /api/reading-sessions/{id}/finish` - `ReadingSessionController#finish`; finishes a current-user reading session.
- `GET /api/books/{bookId}/reading-sessions` - `ReadingSessionController#listForBook`; current user's sessions for a readable book.
- `GET /api/review/sessions` - `ReviewController#list`; current user's review sessions.
- `POST /api/review/sessions` - `ReviewController#create`; creates an empty current-user review session.
- `GET /api/review/sessions/{id}` - `ReviewController#get`; loads an owned review session with items.
- `POST /api/review/sessions/{id}/items` - `ReviewController#addItem`; adds a review item to an owned session and validates target/source ownership.
- `PUT /api/review/items/{id}` - `ReviewController#updateItem`; updates an owned review item and can update mastery when completed.
- `POST /api/review/generate-from-book` - `ReviewController#generateFromBook`; generates source-backed items from current-user book quotes, action items, and concepts.
- `POST /api/review/generate-from-concept` - `ReviewController#generateFromConcept`; generates a source-backed concept review item.
- `POST /api/review/generate-from-project` - `ReviewController#generateFromProject`; generates source-backed project application, lens review, and playtest finding items.
- `GET /api/mastery` - `MasteryController#list`; current user's mastery targets.
- `GET /api/mastery/target` - `MasteryController#getTarget`; one current-user mastery target by type/id.
- `PUT /api/mastery/target` - `MasteryController#updateTarget`; validates target/source ownership before upserting mastery.
- `GET /api/analytics/reading` - `AnalyticsController#reading`; real current-user reading/activity counts.
- `GET /api/analytics/knowledge` - `AnalyticsController#knowledge`; real current-user concept/review/mastery counts.
- `GET /api/analytics/books/{bookId}` - `AnalyticsController#book`; real current-user counts for a readable book.

Learning-system caveats:

- Analytics responses are computed on read from persisted records; there is no fake analytics seed or snapshot table.
- Empty accounts return zero counts and empty arrays.
- Demo Workspace records are excluded from analytics by default and included only with `includeDemo=true`.
- Review generation only creates items from records the user can access.
- Daily reflections can update mastery for source-backed daily sentence or prompt targets; they do not invent mastery scores from fake data.

Import/export and backups:

Implementation source of truth: `DataExportController`, `DataImportController`, and the unified `ImportExportService`.

- `GET /api/export/json` - `DataExportController#exportJson`; current user's BookOS JSON package.
- `GET /api/export/book/{bookId}/json` - `DataExportController#exportBookJson`; single book JSON package; requires the book to be in the current user's library.
- `GET /api/export/book/{bookId}/markdown` - `DataExportController#exportBookMarkdown`; single book Markdown export.
- `GET /api/export/quotes/csv` - `DataExportController#exportQuotesCsv`; current user's quote CSV.
- `GET /api/export/action-items/csv` - `DataExportController#exportActionItemsCsv`; current user's action item CSV.
- `GET /api/export/concepts/csv` - `DataExportController#exportConceptsCsv`; current user's concept CSV.
- `POST /api/import/preview` - `DataImportController#previewImport`; read-only import preview for `BOOKOS_JSON`, `MARKDOWN_NOTES`, `QUOTES_CSV`, and `ACTION_ITEMS_CSV`.
- `POST /api/import/commit` - `DataImportController#commitImport`; explicit import commit for supported non-duplicate records.

Current import/export caveats:

- `BOOKOS_JSON` export includes project applications, authored forum threads, and daily reflections for archival portability, but MVP import does not recreate those record types yet.
- Unknown or malformed imported page numbers stay `null`; they are reported as preview warnings and are never invented.
- Import conflict handling is skip-only. The API does not overwrite existing user content automatically.

Project Mode:

- `GET /api/projects`
- `POST /api/projects`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`
- `PUT /api/projects/{id}/archive`
- `GET /api/projects/{projectId}/problems`
- `POST /api/projects/{projectId}/problems`
- `PUT /api/project-problems/{id}`
- `DELETE /api/project-problems/{id}`
- `GET /api/projects/{projectId}/applications`
- `POST /api/projects/{projectId}/applications`
- `PUT /api/project-applications/{id}`
- `DELETE /api/project-applications/{id}`
- `GET /api/projects/{projectId}/decisions`
- `POST /api/projects/{projectId}/decisions`
- `PUT /api/design-decisions/{id}`
- `DELETE /api/design-decisions/{id}`
- `GET /api/projects/{projectId}/playtest-plans`
- `POST /api/projects/{projectId}/playtest-plans`
- `PUT /api/playtest-plans/{id}`
- `DELETE /api/playtest-plans/{id}`
- `GET /api/projects/{projectId}/playtest-findings`
- `POST /api/projects/{projectId}/playtest-findings`
- `PUT /api/playtest-findings/{id}`
- `DELETE /api/playtest-findings/{id}`
- `GET /api/projects/{projectId}/knowledge-links`
- `POST /api/projects/{projectId}/knowledge-links`
- `DELETE /api/project-knowledge-links/{id}`
- `GET /api/projects/{projectId}/lens-reviews`
- `POST /api/projects/{projectId}/lens-reviews`
- `PUT /api/project-lens-reviews/{id}`
- `DELETE /api/project-lens-reviews/{id}`
- `POST /api/projects/{projectId}/apply/source-reference`
- `POST /api/projects/{projectId}/apply/quote`
- `POST /api/projects/{projectId}/apply/concept`
- `POST /api/projects/{projectId}/apply/knowledge-object`
- `POST /api/projects/{projectId}/create-prototype-task-from-daily`

Forum:

- `GET /api/forum/categories`
- `POST /api/forum/categories`
- `GET /api/forum/templates`
- `GET /api/forum/threads`
- `POST /api/forum/threads`
- `GET /api/forum/threads/{id}`
- `PUT /api/forum/threads/{id}`
- `DELETE /api/forum/threads/{id}`
- `PUT /api/forum/threads/{id}/moderation`
- `GET /api/forum/threads/{id}/comments`
- `POST /api/forum/threads/{id}/comments`
- `PUT /api/forum/comments/{id}`
- `DELETE /api/forum/comments/{id}`
- `POST /api/forum/threads/{id}/bookmark`
- `DELETE /api/forum/threads/{id}/bookmark`
- `POST /api/forum/threads/{id}/like`
- `DELETE /api/forum/threads/{id}/like`
- `POST /api/forum/threads/{id}/report`
- `GET /api/forum/reports`
- `PUT /api/forum/reports/{id}/resolve`

Search and graph:

- `GET /api/search` with optional query parameters `q`, `type`, and `bookId`
- `GET /api/graph`
- `GET /api/graph/book/{bookId}`
- `GET /api/graph/concept/{conceptId}`
- `GET /api/graph/project/{projectId}`
  - Graph endpoints support real-data filters for entity type, relationship type, source confidence, created date range, depth, and limit.
  - Manual user-created `EntityLink` edges appear in workspace, book, concept, and project graph scopes when they touch visible nodes.

AI:

- `GET /api/ai/status`
- `POST /api/ai/suggestions/note-summary`
- `POST /api/ai/suggestions/extract-actions`
- `POST /api/ai/suggestions/extract-concepts`
- `POST /api/ai/suggestions/design-lenses`
- `POST /api/ai/suggestions/project-applications`
- `POST /api/ai/suggestions/forum-thread`
- `GET /api/ai/suggestions`
- `PUT /api/ai/suggestions/{id}/accept`
- `PUT /api/ai/suggestions/{id}/reject`
- `PUT /api/ai/suggestions/{id}/edit`

Endpoint rules:

- Private user content is owner-scoped.
- Public/shared catalog behavior must be explicit.
- Source references must not invent page numbers; unknown pages stay `null`.
- AI suggestions are drafts and do not overwrite user content.
- `MockAIProvider` is the default local provider. `OpenAICompatibleProvider` is optional and requires environment configuration.
