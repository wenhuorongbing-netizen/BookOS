# BookOS Technical Software Spec

Status: Draft for review before implementation  
Repository: `https://github.com/wenhuorongbing-netizen/BookOS`  
Inspected local path: `D:\【指挥中心】\节操都市\项目\BookOS`  
Branch: `main`  
Commit SHA: `755b09ec5b8ec02a1c73550112fb24a7e54d5856`  
Worktree status at inspection: clean  
Remote HEAD checked by `git ls-remote`: `755b09ec5b8ec02a1c73550112fb24a7e54d5856`

## 0. Repository Baseline

The current repository is not empty and must be preserved. It already contains a working full-stack Milestone 1 foundation.

Current implemented baseline:

- Backend: Java 21, Spring Boot 3.5, Maven, Spring Web, Spring Security, Spring Data JPA, Hibernate, Bean Validation, MySQL config, JWT auth.
- Backend modules already present: `common`, `config`, `security`, `user`, `book`, plus placeholder packages for `note`, `capture`, `knowledge`, `link`, `daily`, `project`, `forum`, `ai`, `media`, `admin`.
- Backend implemented APIs: auth, users, books, user library, health.
- Backend tests present: context, auth flow, book library integration.
- Frontend: Vue 3, TypeScript, Vite, Pinia, Vue Router, Element Plus, Axios.
- Frontend implemented pages: login, register, dashboard, my library, add/edit book, book detail, five-star, currently reading, anti-library.
- Frontend implemented UI: app shell, sidebar, top nav, right rail, book hero, insight cards, knowledge preview, Quick Capture local state, recent note blocks, design-system primitives.
- Database: MySQL via `docker-compose.yml`, test profile uses H2.
- Docs already present: UI/UX review, visual target, UI refactor plan, responsive behavior, accessibility checklist, manual UI QA.

Verification run before this spec:

- `frontend`: `npm run typecheck` passed.
- `frontend`: `npm run build` passed.
- `backend`: `.\mvnw.cmd test` passed with 3 tests, 0 failures.

Important repository notes:

- Do not use archive files as source. A `backend.zip` exists in the repository root, but implementation must use the working source tree only.
- Do not use `.7z` archives.
- The next build must continue from the current working foundation, not restart.
- The core missing product loop is notes, deterministic parsing, persisted quick capture, source references, knowledge objects, daily, forum, and AI draft workflows.

## 1. Product Summary

BookOS is a reading record system, book notes library, knowledge operating system, game design knowledge cockpit, structured forum, and optional AI-assisted extraction system.

BookOS solves the problem that reading notes often become disconnected text fragments with weak retrieval, no source chain, no discussion layer, and no path from reading to action. The product turns reading into searchable, linked, source-backed, discussable, and actionable knowledge.

For game design reading, BookOS should transform books into:

- Concepts.
- Principles.
- Design lenses.
- Diagnostic questions.
- Checklists.
- Methods.
- Patterns and anti-patterns.
- Exercises.
- Prototype tasks.
- Project applications.
- Forum discussions.

The product is not only a beautiful dashboard. The cockpit UI is the command surface for a real application: library, notes, parser, source references, capture inbox, daily reflection, forum, and optional AI draft assistance.

## 2. Product Scope

### MVP

The MVP must complete the smallest reliable loop:

Register -> Login -> Add book -> Add to personal library -> Track status/current page/rating -> Open book detail -> Write Markdown note -> Parse emoji/page/tag/`[[Concept]]` -> Generate quote/action/concept references -> Preserve source reference -> Quick Capture -> Review Capture Inbox -> Convert capture -> Daily quote/prompt -> Open source -> Discuss in forum.

MVP modules:

- Authentication with USER, ADMIN, MODERATOR roles.
- Book catalog and personal library.
- Reading progress and reading history.
- Book detail cockpit.
- Markdown book notes and note blocks.
- Deterministic emoji/page/tag/concept parser.
- Raw Capture and Capture Inbox.
- SourceReference for every derived object.
- Quotes and action items.
- Minimal concepts and knowledge objects.
- Daily quote and daily prompt.
- Minimal structured forum.
- Optional mock AI provider and AI suggestion draft lifecycle.

### Post-MVP

- Full-text search across books, notes, quotes, captures, forum, and knowledge objects.
- Rich Markdown editor with preview and safe rendering.
- Knowledge graph visualization with ECharts or Vue Flow.
- Better reading history timeline.
- Project application mode for applying book knowledge to game projects.
- User notification center.
- Moderator tools for forum reports.
- Import/export for notes.
- Advanced source reference merging.
- Batch parsing and extraction.

### Future Advanced Features

- Multi-book synthesis.
- Cross-book ontology and design pattern map.
- AI-assisted ontology expansion.
- Semantic search and embeddings.
- Collaborative reading groups.
- Versioned notes and source audit history.
- Public/private knowledge publishing.
- Spaced repetition for quotes/concepts.
- Advanced project prototype task planning.

## 3. User Personas

### Personal Reader

Wants to track reading, capture thoughts quickly, find past notes, and remember useful quotes without building a complex knowledge system manually.

### Game Designer

Wants to turn game design books into design actions, lenses, prototype tasks, and project critiques. Needs fast capture while reading and a cockpit for applying ideas to actual game projects.

### Researcher / Student

Needs source-backed notes, backlinks, page references, quotes, and structured concepts for study, essays, and long-term knowledge management.

### Forum Participant

Wants to discuss books, concepts, quotes, exercises, and project applications in structured threads rather than generic comment dumps.

### Admin / Moderator

Maintains seed data, manages users and reports, moderates structured discussions, and protects public surfaces from spam or private data leaks.

## 4. Core User Flows

### Register / Login

1. User opens public layout.
2. User registers with email, display name, and password.
3. Backend hashes password and creates USER role.
4. User logs in.
5. Frontend stores JWT securely enough for MVP and calls `/api/auth/me`.

### Add Book

1. User opens Add Book.
2. User enters title, authors, optional ISBN, cover URL, category, tags, total pages.
3. Backend validates and creates `Book`, `Author`, `BookAuthor`, `Tag`, `BookTag`.
4. User may add the book to personal library.

### Track Reading

1. User opens book detail or My Library.
2. User updates reading status, current page, progress, rating, favorite.
3. Backend updates `UserBook`.
4. Backend appends `ReadingHistory` later when reading progress changes materially.

### Write Note

1. User opens Book Detail -> Notes.
2. User writes Markdown note.
3. User may split note into blocks or save raw Markdown first.
4. Backend saves `BookNote` and `NoteBlock`.
5. Parser processes note blocks deterministically.

### Quick Capture

1. User types raw capture in Quick Capture, e.g. `💡 p.12 This could become a core loop [[Core Loop]] #prototype`.
2. Backend saves `RawCapture`.
3. Parser extracts marker type, page, tags, concepts, and source reference.
4. Capture appears in Capture Inbox.
5. User converts it to note, quote, action item, concept, prototype task, or forum thread.

### Parse Note

1. User submits raw text or note block.
2. Parser detects emoji marker, page reference, tags, and concepts.
3. Parser returns a structured result and warnings.
4. Backend stores parse output and links it to source reference.

### Open Source Reference

1. User opens quote/action/concept/AI suggestion/forum thread.
2. Right rail shows source book, chapter, page range, note block, raw capture, and source text.
3. User clicks Open Source.
4. Frontend navigates to source book/note/page anchor.

### Daily Quote / Prompt

1. User opens dashboard or book detail.
2. Backend returns deterministic daily quote and prompt for user/date.
3. User opens source or saves a reflection.
4. User may create an action item or prototype task from the prompt.

### Forum Discussion

1. User opens forum category.
2. User creates structured thread attached to book, quote, concept, design lens, exercise, prototype task, game project, or general topic.
3. Other users comment, like, bookmark, or report.
4. Moderators handle reports later.

### AI Suggestion Accept / Reject

1. User asks AI to summarize/extract/classify.
2. Backend creates `AIInteraction` and `AISuggestion` records.
3. AI output is validated JSON.
4. Suggestion appears as draft.
5. User accepts, edits, or rejects.
6. Only accepted suggestions create or update user-visible objects.

## 5. Information Architecture

### Main Navigation

- Dashboard.
- Library.
- Notes.
- Quotes.
- Capture Inbox.
- Concepts.
- Lenses.
- Diagnostics.
- Exercises.
- Projects.
- Forum.
- Settings.
- Admin, shown only for ADMIN/MODERATOR where appropriate.

### Page Hierarchy

- Public
  - Login.
  - Register.
- App Shell
  - Dashboard.
  - My Library.
  - Book Detail.
  - Add/Edit Book.
  - Notes.
  - Note Detail.
  - Capture Inbox.
  - Quote Detail.
  - Action Items.
  - Concepts.
  - Concept Detail.
  - Knowledge Graph.
  - Lenses.
  - Daily.
  - Forum.
  - Forum Category.
  - Forum Thread.
  - Projects.
  - Settings.
  - Admin.

## 6. Data Model

All entities should extend `BaseEntity` where practical: `id`, `createdAt`, `updatedAt`, `createdBy` later if needed.

### User

Purpose: authenticated account.

Key fields:

- `id`
- `email`
- `passwordHash`
- `displayName`
- `enabled`
- `locked`
- `lastLoginAt`

Relationships:

- Many-to-many roles through user roles or one active role for MVP.
- One-to-one `UserProfile`.
- One-to-many `UserBook`, `BookNote`, `RawCapture`, `ForumThread`, `ForumComment`, `AISuggestion`.

### Role

Purpose: authorization.

Key fields:

- `id`
- `name`: USER, ADMIN, MODERATOR.

Relationships:

- Many-to-many users.

### Book

Purpose: shared book catalog record.

Key fields:

- `id`
- `title`
- `subtitle`
- `description`
- `isbn`
- `publisher`
- `publicationYear`
- `coverUrl`
- `category`
- `visibility`
- `totalPages`
- `archived`

Relationships:

- Many-to-many authors through `BookAuthor`.
- Many-to-many tags through `BookTag`.
- One-to-many `UserBook`, `BookNote`, `RawCapture`, `SourceReference`, `ForumThread`.

### Author

Purpose: normalized author identity.

Key fields:

- `id`
- `name`
- `slug`

Relationships:

- Many-to-many books through `BookAuthor`.

### BookAuthor

Purpose: ordered join table between books and authors.

Key fields:

- `bookId`
- `authorId`
- `sortOrder`

### UserBook

Purpose: per-user reading state for a book.

Key fields:

- `id`
- `userId`
- `bookId`
- `readingStatus`: WANT_TO_READ, READING, PAUSED, FINISHED, ABANDONED, REFERENCE, ANTI_LIBRARY.
- `readingFormat`
- `ownershipStatus`
- `currentPage`
- `progressPercent`
- `rating`
- `favorite`
- `lastReadAt`
- `startedAt`
- `finishedAt`

Relationships:

- Belongs to `User`.
- Belongs to `Book`.

### BookNote

Purpose: user-authored note document attached to a book.

Key fields:

- `id`
- `userId`
- `bookId`
- `title`
- `markdown`
- `threeSentenceSummary`
- `visibility`
- `pinned`
- `archived`

Relationships:

- One-to-many `NoteBlock`.
- One-to-many `SourceReference`.
- One-to-many `EntityLink`.

### NoteBlock

Purpose: structured block inside a note.

Key fields:

- `id`
- `noteId`
- `bookId`
- `userId`
- `blockType`: NORMAL, QUOTE, ACTION_ITEM, QUESTION, CONCEPT, REFLECTION, WARNING, EXPERIMENT, DATA, LINK.
- `rawText`
- `markdown`
- `plainText`
- `sortOrder`
- `pageStart`
- `pageEnd`
- `parserType`
- `parserWarnings`

Relationships:

- Belongs to `BookNote`.
- One-to-one or one-to-many `SourceReference`.
- May create `Quote`, `ActionItem`, `Concept`, `KnowledgeObject`.

### RawCapture

Purpose: fast, low-friction capture before formal notes.

Key fields:

- `id`
- `userId`
- `bookId`
- `rawText`
- `parsedType`
- `pageStart`
- `pageEnd`
- `tagsJson`
- `conceptsJson`
- `status`: INBOX, CONVERTED, ARCHIVED, DISCARDED.
- `convertedEntityType`
- `convertedEntityId`

Relationships:

- Belongs to `User`.
- Optional belongs to `Book`.
- One-to-many `SourceReference`.

### Quote

Purpose: extracted or user-created quote.

Key fields:

- `id`
- `userId`
- `bookId`
- `noteBlockId`
- `rawCaptureId`
- `text`
- `speaker`
- `pageStart`
- `pageEnd`
- `favorite`

Relationships:

- Belongs to source `Book`, optional `NoteBlock`, optional `RawCapture`.
- Has `SourceReference`.
- Can be daily resurfaced.

### ActionItem

Purpose: actionable task extracted from reading.

Key fields:

- `id`
- `userId`
- `bookId`
- `title`
- `description`
- `status`: OPEN, DONE, DISMISSED.
- `priority`: LOW, MEDIUM, HIGH.
- `dueDate`
- `completedAt`

Relationships:

- Has source reference.
- May become `PrototypeTask`.
- May be linked to `GameProject`.

### Concept

Purpose: normalized concept node.

Key fields:

- `id`
- `name`
- `slug`
- `description`
- `createdByUserId`
- `visibility`

Relationships:

- Many-to-many notes, captures, quotes, knowledge objects through `EntityLink`.

### KnowledgeObject

Purpose: flexible knowledge record extracted or authored from books.

Types:

- CONCEPT
- PRINCIPLE
- DESIGN_LENS
- DIAGNOSTIC_QUESTION
- CHECKLIST
- METHOD
- PATTERN
- ANTI_PATTERN
- EXAMPLE_CASE
- EXERCISE
- PROTOTYPE_TASK
- CROSS_BOOK_SYNTHESIS

Key fields:

- `id`
- `userId`
- `type`
- `title`
- `description`
- `bodyMarkdown`
- `visibility`
- `sourceConfidence`: LOW, MEDIUM, HIGH.
- `tagsJson`

Relationships:

- Optional `bookId`, `noteId`, `noteBlockId`, `rawCaptureId`.
- Has `SourceReference`.
- Many-to-many `Concept` through `EntityLink`.

### DesignLens

Purpose: game design inspection lens.

Key fields:

- `id`
- `userId`
- `title`
- `description`
- `question`
- `tagsJson`
- `active`

Relationships:

- Can be backed by `KnowledgeObject`.
- Can link to books, concepts, prompts, projects.

### DiagnosticQuestion

Purpose: structured question for evaluating designs.

Key fields:

- `id`
- `userId`
- `question`
- `description`
- `lensId`
- `knowledgeObjectId`

### Exercise

Purpose: book-derived or user-authored learning task.

Key fields:

- `id`
- `userId`
- `title`
- `instructionsMarkdown`
- `difficulty`
- `estimatedMinutes`
- `sourceReferenceId`

### PrototypeTask

Purpose: design action to build/test something.

Key fields:

- `id`
- `userId`
- `projectId`
- `title`
- `description`
- `status`: TODO, DOING, DONE, ARCHIVED.
- `sourceReferenceId`
- `dueDate`

### SourceReference

Purpose: immutable pointer back to original material.

Key fields:

- `id`
- `userId`
- `sourceType`: BOOK, NOTE, NOTE_BLOCK, RAW_CAPTURE, QUOTE, ACTION_ITEM, KNOWLEDGE_OBJECT, AI_SUGGESTION, FORUM_THREAD.
- `bookId`
- `chapterId`
- `noteId`
- `noteBlockId`
- `rawCaptureId`
- `pageStart`
- `pageEnd`
- `locationLabel`
- `sourceText`
- `sourceConfidence`: LOW, MEDIUM, HIGH.

Rules:

- Unknown page numbers must be null.
- Never invent page numbers.
- AI-derived objects must preserve source material used for generation.

### EntityLink

Purpose: typed relationship graph between entities.

Key fields:

- `id`
- `fromType`
- `fromId`
- `toType`
- `toId`
- `relationType`: REFERENCES, DERIVES_FROM, CONTRADICTS, SUPPORTS, RELATED_TO, APPLIES_TO, DUPLICATES.
- `weight`

### DailySentence

Purpose: stable daily resurfaced quote/sentence per user/date.

Key fields:

- `id`
- `userId`
- `date`
- `quoteId`
- `sourceReferenceId`
- `selectionReason`
- `dismissed`

### DailyDesignPrompt

Purpose: stable daily prompt/reflection per user/date.

Key fields:

- `id`
- `userId`
- `date`
- `promptText`
- `linkedConceptId`
- `linkedLensId`
- `sourceReferenceId`
- `reflectionMarkdown`
- `regeneratedCount`

### ForumCategory

Purpose: structured forum area.

Key fields:

- `id`
- `name`
- `slug`
- `description`
- `sortOrder`
- `visible`

### ForumThread

Purpose: structured discussion.

Key fields:

- `id`
- `categoryId`
- `authorId`
- `title`
- `bodyMarkdown`
- `targetType`: BOOK, CHAPTER, QUOTE, CONCEPT, DESIGN_LENS, EXERCISE, PROTOTYPE_TASK, GAME_PROJECT, GENERAL.
- `targetId`
- `sourceReferenceId`
- `locked`
- `pinned`

### ForumComment

Purpose: reply in a thread.

Key fields:

- `id`
- `threadId`
- `authorId`
- `bodyMarkdown`
- `parentCommentId`
- `hidden`

### AISuggestion

Purpose: draft AI output awaiting user decision.

Key fields:

- `id`
- `userId`
- `suggestionType`: SUMMARY, QUOTE, ACTION_ITEM, CONCEPT, LENS, PROMPT, FORUM_THREAD, PROTOTYPE_TASK.
- `status`: DRAFT, ACCEPTED, EDITED_ACCEPTED, REJECTED, DISCARDED.
- `jsonPayload`
- `displayText`
- `sourceReferenceId`
- `aiInteractionId`

### AIInteraction

Purpose: audit record for AI request/response.

Key fields:

- `id`
- `userId`
- `provider`
- `model`
- `requestJson`
- `responseJson`
- `status`
- `errorMessage`
- `createdAt`

### GameProject

Purpose: user project where reading is applied.

Key fields:

- `id`
- `userId`
- `title`
- `description`
- `status`
- `progressPercent`

### ProjectApplication

Purpose: link between knowledge and a game project.

Key fields:

- `id`
- `projectId`
- `sourceType`
- `sourceId`
- `applicationMarkdown`
- `status`

## 7. Backend Architecture

### Package Structure

Use the existing package root `com.bookos.backend`.

Target packages:

- `common`: base entities, responses, exceptions, validation helpers.
- `config`: CORS, JWT, seed, AI, object mapper.
- `security`: JWT filter, services, annotations, authorization helpers.
- `user`: auth, user profile, role management.
- `book`: book catalog, authors, tags, user library.
- `note`: book notes and note blocks.
- `capture`: raw capture and capture inbox.
- `parser`: deterministic parser service and parser DTOs.
- `source`: source references.
- `quote`: quotes.
- `action`: action items.
- `knowledge`: concepts, knowledge objects, design lenses, diagnostics, exercises.
- `daily`: daily quote and prompt.
- `forum`: forum categories, threads, comments.
- `ai`: provider abstraction, mock provider, suggestions, interactions.
- `project`: game projects and project applications.
- `admin`: admin/moderator endpoints.

### Controllers

Controllers should be thin:

- Validate request DTOs.
- Resolve current user.
- Call service.
- Return `ApiResponse<T>`.

### Services

Services contain:

- Authorization checks.
- Transaction boundaries.
- Business rules.
- Source reference creation.
- Parser invocation.
- Conversion workflows.

### Repositories

Use Spring Data JPA repositories per aggregate. Keep query methods explicit and add `@EntityGraph` or fetch joins where needed to avoid N+1.

### DTOs

Use request/response DTOs. Do not expose entities directly.

### Security

- JWT MVP is acceptable.
- Passwords use BCrypt.
- `@AuthenticationPrincipal` or equivalent current-user helper.
- Every user-owned object must be filtered by owner.
- Public forum endpoints must never return private notes/captures.

### Validation

- Bean Validation on request DTOs.
- Server-side validation for page range, visibility, enum values, raw text length, Markdown length.
- Parser output validation before persistence.

### Error Handling

Use `GlobalExceptionHandler` for:

- Validation errors.
- Unauthorized/forbidden.
- Not found.
- Conflict.
- Bad parser input.
- AI provider errors.

### Source Reference Strategy

Create source references at the point of derivation:

- RawCapture saved -> create source reference.
- NoteBlock parsed -> create source reference.
- Quote/action/concept converted -> copy source reference with derived target metadata.
- AI suggestion -> source reference points to the exact note/capture/quote set used.

### Parser Strategy

Parser must be deterministic, pure, and unit-tested. It must not call AI.

### AI Provider Abstraction

Interface:

```java
interface AIProvider {
  AIProviderResult completeStructured(AIProviderRequest request);
}
```

Implementations:

- `MockAIProvider`: deterministic local fixtures, no key required.
- `OpenAICompatibleProvider`: placeholder using environment variables later.

## 8. Frontend Architecture

### Existing Stack

- Vue 3.
- TypeScript.
- Vite.
- Pinia.
- Vue Router.
- Element Plus.
- Axios.

### Layouts

- `PublicLayout`: login/register.
- `AppLayout`: three-zone cockpit with sidebar, topbar, main content, right rail.
- `DashboardLayout`: dashboard-specific shell if needed.

### Pages

MVP target pages:

- Login.
- Register.
- Dashboard.
- My Library.
- Book Detail.
- Notes.
- Note Detail.
- Capture Inbox.
- Quotes.
- Action Items.
- Concepts.
- Daily.
- Forum.
- Forum Thread.

### Components

Current design-system primitives should be preserved:

- `AppCard`
- `AppButton`
- `AppIconButton`
- `AppBadge`
- `AppProgressBar`
- `AppStat`
- `AppSectionHeader`
- `AppEmptyState`
- `AppLoadingState`
- `AppErrorState`
- `AppTooltip`
- `AppDivider`

Book cockpit components:

- `BookHero`
- `BookCover`
- `ReadingProgressBar`
- `BookMetaStats`
- `BookInsightCards`
- `BookKnowledgeSection`
- `BookCaptureSection`
- `RightRail`
- `ContextPanel`

Future components:

- `MarkdownNoteEditor`
- `NoteBlockList`
- `ParserPreview`
- `CaptureInboxTable`
- `SourceReferencePanel`
- `EntityLinkList`
- `ForumThreadCard`
- `AISuggestionCard`

### Stores

- `authStore`: auth token/current user.
- `rightRailStore`: selected source, AI drafts, action items.
- `captureStore`: currently local; must be replaced/extended with backend-backed capture API.
- `libraryStore` later for book/user-book state.
- `dailyStore` later.
- `forumStore` later.

### API Client

Continue Axios wrapper in `src/api/http.ts`.

Requirements:

- Attach JWT.
- Normalize backend `ApiResponse<T>`.
- Handle 401 by clearing session and redirecting.
- Keep typed API modules by domain.

### Routing

Continue guarded routes using Vue Router. Add routes incrementally as backend endpoints exist. Do not add placeholder routes and call them complete.

### Design Tokens

Preserve current visual direction:

- Warm ivory background.
- Soft cream cards.
- Deep teal primary.
- Warm orange accent.
- Warm gray borders.
- Serif book titles.
- Sans-serif UI.
- Visible focus rings.
- Reduced motion baseline.

### Responsive Behavior

- Desktop: sidebar + topbar + center + right rail.
- Tablet: right rail below content or drawer; sidebar collapses.
- Mobile: single-column cards, sidebar drawer, top search remains available.
- No horizontal page overflow.

### Accessibility

- Semantic `header`, `nav`, `main`, `aside`.
- Skip link.
- `aria-current` active nav.
- `aria-expanded` collapsible panels.
- Progressbar semantics.
- Icon-only button labels.
- Visible focus states.
- Keyboard operability.
- Markdown rendered safely.

## 9. API Specification

All authenticated endpoints require Bearer JWT unless marked public.

Response envelope:

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "timestamp": "2026-04-27T00:00:00Z"
}
```

### Auth

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register user | Public |
| POST | `/api/auth/login` | Login and issue token | Public |
| POST | `/api/auth/logout` | Client/server logout hook later | User |
| GET | `/api/auth/me` | Current user | User |

### Books

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/books` | List/search book catalog | User |
| POST | `/api/books` | Create book | User |
| GET | `/api/books/{id}` | Book detail | User |
| PUT | `/api/books/{id}` | Update book | Owner/Admin |
| DELETE | `/api/books/{id}` | Archive/delete book | Owner/Admin |

### User Books

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/books/{id}/add-to-library` | Add book to library | User |
| GET | `/api/user-books` | User library | User |
| PUT | `/api/user-books/{id}/status` | Update reading status | Owner |
| PUT | `/api/user-books/{id}/progress` | Update page/progress | Owner |
| PUT | `/api/user-books/{id}/rating` | Update rating/favorite | Owner |
| GET | `/api/user-books/currently-reading` | Reading shelf | User |
| GET | `/api/user-books/five-star` | Five-star books | User |
| GET | `/api/user-books/anti-library` | Anti-library | User |

### Notes

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/books/{bookId}/notes` | List notes for book | Owner |
| POST | `/api/books/{bookId}/notes` | Create note | Owner |
| GET | `/api/notes/{id}` | Note detail | Owner or visibility |
| PUT | `/api/notes/{id}` | Update note | Owner |
| DELETE | `/api/notes/{id}` | Archive note | Owner |
| POST | `/api/notes/{id}/blocks` | Add note block | Owner |
| PUT | `/api/note-blocks/{id}` | Update note block | Owner |
| DELETE | `/api/note-blocks/{id}` | Delete note block | Owner |

Create note request:

```json
{
  "title": "Chapter 1 notes",
  "markdown": "💬 p.42 Example quote #quote [[Game Feel]]",
  "visibility": "PRIVATE"
}
```

### Raw Captures

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/captures` | Create raw capture and parse | User |
| GET | `/api/captures/inbox` | Capture Inbox | User |
| GET | `/api/captures/{id}` | Capture detail | Owner |
| PUT | `/api/captures/{id}` | Edit raw capture | Owner |
| POST | `/api/captures/{id}/convert` | Convert capture | Owner |
| PUT | `/api/captures/{id}/archive` | Archive capture | Owner |

Create capture request:

```json
{
  "bookId": 12,
  "rawText": "✅ 第80页 Try this method tomorrow. #todo [[Habit Design]]"
}
```

### Parser

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/parser/preview` | Parse without saving | User |
| POST | `/api/parser/note-blocks/{id}/reparse` | Reparse block | Owner |
| POST | `/api/parser/captures/{id}/reparse` | Reparse capture | Owner |

Parser response:

```json
{
  "type": "ACTION_ITEM",
  "pageStart": 80,
  "pageEnd": null,
  "tags": ["todo"],
  "concepts": ["Habit Design"],
  "cleanText": "Try this method tomorrow.",
  "rawText": "✅ 第80页 Try this method tomorrow. #todo [[Habit Design]]",
  "warnings": []
}
```

### Quotes

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/books/{bookId}/quotes` | Book quotes | Owner |
| GET | `/api/quotes/{id}` | Quote detail | Owner |
| POST | `/api/quotes` | Create quote | User |
| PUT | `/api/quotes/{id}` | Update quote | Owner |
| DELETE | `/api/quotes/{id}` | Archive quote | Owner |

### Action Items

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/action-items` | List action items | User |
| POST | `/api/action-items` | Create action item | User |
| PUT | `/api/action-items/{id}` | Update action item | Owner |
| PUT | `/api/action-items/{id}/complete` | Complete item | Owner |
| PUT | `/api/action-items/{id}/reopen` | Reopen item | Owner |

### Concepts and Knowledge Objects

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/concepts` | Search concepts | User |
| POST | `/api/concepts` | Create concept | User |
| GET | `/api/concepts/{id}` | Concept detail | User |
| GET | `/api/books/{bookId}/concepts` | Concepts for book | Owner |
| GET | `/api/knowledge-objects` | List knowledge objects | User |
| POST | `/api/knowledge-objects` | Create knowledge object | User |
| GET | `/api/knowledge-objects/{id}` | Knowledge object detail | Owner/visibility |
| PUT | `/api/knowledge-objects/{id}` | Update knowledge object | Owner |

### Daily

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/daily/sentence` | Daily resurfaced quote | User |
| POST | `/api/daily/sentence/regenerate` | Regenerate daily quote | User |
| GET | `/api/daily/prompt` | Daily design prompt | User |
| POST | `/api/daily/prompt/reflection` | Save reflection | User |
| POST | `/api/daily/prompt/prototype-task` | Create prototype task | User |

### Forum

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/forum/categories` | List categories | User/Public later |
| GET | `/api/forum/categories/{slug}/threads` | Threads by category | User |
| POST | `/api/forum/threads` | Create thread | User |
| GET | `/api/forum/threads/{id}` | Thread detail | User |
| PUT | `/api/forum/threads/{id}` | Edit thread | Owner/Moderator |
| POST | `/api/forum/threads/{id}/comments` | Add comment | User |
| PUT | `/api/forum/comments/{id}` | Edit comment | Owner/Moderator |
| POST | `/api/forum/threads/{id}/bookmark` | Bookmark thread | User |
| POST | `/api/forum/threads/{id}/report` | Report thread | User |

### AI Suggestions

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/ai/suggestions` | Create draft suggestion | User |
| GET | `/api/ai/suggestions` | List suggestions | User |
| GET | `/api/ai/suggestions/{id}` | Suggestion detail | Owner |
| PUT | `/api/ai/suggestions/{id}` | Edit draft payload/text | Owner |
| POST | `/api/ai/suggestions/{id}/accept` | Accept suggestion | Owner |
| POST | `/api/ai/suggestions/{id}/reject` | Reject suggestion | Owner |

### Health

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/api/health` | Health check | Public |

## 10. Parser Specification

Parser must be deterministic and AI-free.

### Marker Mapping

| Marker | Type |
|---|---|
| 💡 | INSPIRATION |
| 🔑 | KEY_ARGUMENT |
| 💬 | QUOTE |
| 🗣️ | DISCUSSION_POINT |
| 🤯 | MIND_BLOWING_IDEA |
| ✅ | ACTION_ITEM |
| ❓ | QUESTION |
| 🧠 | MENTAL_MODEL |
| 🧩 | RELATED_CONCEPT |
| ⚠️ | WARNING |
| 📌 | IMPORTANT |
| 🧪 | EXPERIMENT |
| 📝 | PERSONAL_REFLECTION |
| 📊 | DATA_STATISTIC |
| 🔗 | LINK |
| 🧭 | READING_DIRECTION |

Default type if no marker: NOTE.

### Page Regex Strategy

Support:

- `p.42`
- `p42`
- `page 42`
- `page 42-43`
- `pp.42-43`
- `页42`
- `第42页`
- `第42-43页`

Recommended regex fragments:

```text
EN_PAGE = \b(?:p\.?|pp\.?|page)\s*(\d+)(?:\s*[-–]\s*(\d+))?\b
ZH_PAGE_A = 第\s*(\d+)(?:\s*[-–]\s*(\d+))?\s*页
ZH_PAGE_B = 页\s*(\d+)(?:\s*[-–]\s*(\d+))?
```

Rules:

- If start > end, keep start and add warning.
- If no page found, `pageStart = null`, `pageEnd = null`.
- Never infer page from reading progress.

### Tags

Regex:

```text
#([\p{L}\p{N}_-]+)
```

Rules:

- Normalize to lowercase for matching.
- Preserve original display if needed.
- Deduplicate.

### Concepts

Regex:

```text
\[\[([^\]]+)\]\]
```

Rules:

- Trim whitespace.
- Reject empty concepts.
- Deduplicate case-insensitively.
- Do not auto-create canonical concepts until user accepts conversion, unless configured.

### Clean Text

Remove:

- Leading emoji marker.
- Page references.
- Tags.
- `[[Concept]]` wrappers.

Keep original text in `rawText`.

### Edge Cases

- Multiple markers: first recognized marker determines primary type; store secondary markers as warnings or metadata.
- Multiple page references: use first as primary and store warnings.
- Hashtag in URL: avoid matching if preceded by `/` or alphanumeric URL fragment where feasible.
- Full-width punctuation should be tolerated.
- Quotation marks should not be stripped from quote text.

### Examples

Input:

```text
💬 p.42 “The impediment to action advances action.” #quote [[Stoicism]]
```

Output:

```json
{
  "type": "QUOTE",
  "pageStart": 42,
  "pageEnd": null,
  "tags": ["quote"],
  "concepts": ["Stoicism"],
  "rawText": "💬 p.42 “The impediment to action advances action.” #quote [[Stoicism]]"
}
```

Input:

```text
✅ 第80页 Try this method tomorrow. #todo [[Habit Design]]
```

Output:

```json
{
  "type": "ACTION_ITEM",
  "pageStart": 80,
  "pageEnd": null,
  "tags": ["todo"],
  "concepts": ["Habit Design"],
  "rawText": "✅ 第80页 Try this method tomorrow. #todo [[Habit Design]]"
}
```

## 11. AI Specification

AI is optional. BookOS must work without an AI key.

### Providers

`MockAIProvider`:

- Default for local development.
- Deterministic.
- No network.
- Returns valid JSON fixtures.

`OpenAICompatibleProvider`:

- Placeholder for later.
- Reads base URL, model, and key from environment variables.
- Must not be required to run the app.

### AI Suggestion Lifecycle

States:

- DRAFT
- ACCEPTED
- EDITED_ACCEPTED
- REJECTED
- DISCARDED

Workflow:

1. User requests AI operation.
2. Backend records `AIInteraction`.
3. Provider returns JSON.
4. Backend validates JSON schema.
5. Backend stores `AISuggestion` as DRAFT.
6. UI displays draft with source reference.
7. User accepts, edits, or rejects.
8. Only accepted/edit-accepted suggestions create real objects.

### No Automatic Overwrite Rule

AI must never overwrite user-authored content automatically. AI output must always be saved as a draft suggestion first.

### JSON Validation

Each suggestion type needs a schema. Invalid JSON or schema mismatch stores a failed `AIInteraction` and returns an actionable error.

## 12. UI/UX Specification

### Book Knowledge Cockpit

The primary product surface should feel like a premium academic productivity cockpit.

Visual direction:

- Warm ivory/paper background.
- Soft cream cards.
- Deep teal primary.
- Warm orange accent.
- Rounded cards.
- Subtle shadows.
- Serif display style for book titles.
- Clean sans-serif UI text.
- Dense but calm layout.

### Left Sidebar

Includes:

- BookOS logo.
- Subtitle: Game Design Knowledge Operating System.
- Library.
- Notes.
- Quotes.
- Lenses.
- Diagnostics.
- Exercises.
- Projects.
- Forum.
- Settings.
- Current Project progress.
- Recently Viewed.

### Top Bar

Includes:

- Global search.
- Shortcut hint: `Cmd/Ctrl + K`.
- Current book selector.
- Notifications.
- User profile menu.

### Center Workspace

Book Hero:

- Cover.
- Title.
- Subtitle.
- Author.
- Favorite icon.
- Reading progress.
- Page status.
- Last read date.
- Notes count.
- Quotes count.
- Lenses count.

Insight Cards:

- Today's Resurfaced Quote.
- Daily Design Prompt.
- Ontology Preview.

Middle:

- Knowledge Graph Preview.
- Extracted Concepts.
- Active Design Lenses.

Lower:

- Quick Capture.
- Recent Note Blocks.

### Right Rail

Includes:

- Source Reference.
- AI Draft Suggestions.
- Action Items Extracted.

### Forum Pages

Forum should use structured cards:

- Category list.
- Thread list.
- Thread detail.
- Structured post template selector.
- Source attachment panel.

### Mobile Behavior

- Sidebar collapses to drawer/top disclosure.
- Topbar stacks.
- Right rail moves below content.
- Cards stack.
- Tables become cards.
- Quick Capture remains easy to access.

### Accessibility

- Keyboard reachable controls.
- Visible focus rings.
- Form labels.
- Icon labels.
- Progress semantics.
- Source graph text summaries.
- Reduced motion.
- Color contrast target WCAG AA where practical.

## 13. Development Phases

### Phase 1 - Full-Stack Foundation

Goal: runnable backend/frontend/database foundation.

Likely files/modules:

- Root README, Docker Compose, env examples.
- Spring Boot project.
- Vue project.
- Base app shell.

Acceptance criteria:

- Backend starts.
- Frontend starts.
- MySQL config exists.
- Health check works.

What not to build yet:

- Notes, parser, AI, forum, graph.

Current status:

- Mostly complete in current repo.

### Phase 2 - Auth + Book Library MVP

Goal: users can manage books and personal reading state.

Likely files/modules:

- `user`, `security`, `book`.
- Frontend auth/library/book pages.

Acceptance criteria:

- Register/login/me.
- Add/edit/delete book.
- Add to library.
- Update status/current page/progress/rating.
- Five-star/currently-reading/anti-library views.

What not to build yet:

- Notes/parser/source refs.

Current status:

- Mostly complete, but reading status enum should be reconciled with target names before later phases.

### Phase 3 - Notes + Parser

Goal: persisted notes, note blocks, deterministic parser preview.

Likely files/modules:

- Backend: `note`, `parser`, `source`.
- Frontend: Notes page, note detail, note editor, parser preview.

Acceptance criteria:

- Create/list/update/delete book notes.
- Create/list/update/delete note blocks.
- Parser detects marker/page/tag/concept.
- Parser tests cover English and Chinese page syntax.
- SourceReference created for note blocks.

What not to build yet:

- AI extraction, advanced graph, forum moderation.

### Phase 4 - Quick Capture

Goal: backend-backed capture workflow.

Likely files/modules:

- Backend: `capture`, parser integration, source reference.
- Frontend: replace local capture store with API-backed store, Capture Inbox.

Acceptance criteria:

- Save raw capture.
- Parse capture.
- Show capture inbox.
- Convert capture to note/quote/action/concept.
- Preserve source reference.

What not to build yet:

- AI suggestions.

### Phase 5 - Source Reference + Knowledge Objects

Goal: every derived object opens its original source.

Likely files/modules:

- Backend: `source`, `knowledge`, `link`, `quote`, `action`.
- Frontend: right rail source panel, quote/action/concept pages.

Acceptance criteria:

- Quotes/action items/concepts have source references.
- Unknown pages remain null.
- Entity links create backlinks.

What not to build yet:

- Complex ontology or embeddings.

### Phase 6 - Book Knowledge Cockpit UI

Goal: connect real backend data to existing cockpit UI.

Likely files/modules:

- Frontend book detail, right rail, knowledge section, API adapters.
- Backend book detail aggregate DTOs.

Acceptance criteria:

- Book detail shows real counts, quotes, concepts, lenses, captures, source refs.
- Empty/loading/error states are real.

What not to build yet:

- Advanced graph engine.

### Phase 7 - Daily Sentence + Daily Design Prompt

Goal: deterministic daily resurfacing.

Likely files/modules:

- Backend: `daily`.
- Frontend: daily cards and reflection modal API.

Acceptance criteria:

- Same user/date returns stable quote/prompt.
- Open source works.
- Save reflection works.
- Create prototype task from prompt.

What not to build yet:

- AI-generated prompt system.

### Phase 8 - Forum

Goal: structured forum around books and knowledge.

Likely files/modules:

- Backend: `forum`.
- Frontend: forum routes/pages/components.

Acceptance criteria:

- Categories, threads, comments.
- Thread target links to book/concept/quote/lens/project.
- Basic bookmarks/reports.

What not to build yet:

- Advanced moderation workflows.

### Phase 9 - AI Assistance

Goal: optional draft suggestions.

Likely files/modules:

- Backend: `ai`, `AISuggestion`, `AIInteraction`.
- Frontend: AI draft suggestion cards and accept/edit/reject flows.

Acceptance criteria:

- App works with no AI key.
- Mock provider works.
- JSON is validated.
- Suggestions are drafts.
- Accept creates real objects only after user action.

What not to build yet:

- Embeddings or autonomous agents.

### Phase 10 - Search + Graph + Hardening

Goal: production hardening and richer navigation.

Likely files/modules:

- Search services.
- Graph endpoints.
- Frontend graph visualization.
- Security/performance tests.

Acceptance criteria:

- Search across core entities.
- Graph view opens concepts/source.
- Performance acceptable on seeded data.
- Accessibility and security QA complete.

What not to build yet:

- Large-scale collaborative/public publishing.

## 14. Testing Strategy

### Backend Unit Tests

- Parser unit tests.
- Source reference creation tests.
- AI JSON validation tests.
- Service authorization tests.

### Backend Integration Tests

- Auth flow.
- Book library.
- Notes CRUD.
- Capture conversion.
- Source reference open flow.
- Forum thread/comment flow.

### Frontend Tests Later

Add component tests after MVP workflows stabilize:

- App shell.
- Book detail cockpit.
- Quick Capture.
- Capture Inbox.
- Parser preview.
- Right rail.
- Forum thread.

### Parser Tests

Required cases:

- Each emoji marker.
- English pages.
- Chinese pages.
- Page ranges.
- Missing page.
- Tags.
- Concepts.
- Duplicate tags/concepts.
- Multiple markers/pages.

### API Tests

- Authenticated and unauthenticated access.
- Owner vs non-owner access.
- Validation failure responses.
- Conversion workflows.

### Security Tests

- Password hashing.
- JWT required.
- Private note isolation.
- Markdown sanitization.
- AI key never exposed.

### Manual UI QA

Use `docs/manual-ui-qa.md`, `docs/ui-accessibility-checklist.md`, and `docs/responsive-behavior.md`.

## 15. Security Requirements

- Passwords must be hashed with BCrypt or stronger.
- JWT secret must come from environment variables.
- Do not commit secrets.
- CORS limited to configured frontend origins.
- Validate all request DTOs.
- Sanitize rendered Markdown.
- Escape user content in UI.
- Enforce owner checks on notes, captures, quotes, action items, knowledge objects, AI suggestions, projects.
- Forum public surfaces must not leak private source references.
- AI keys only in backend environment variables.
- AI request/response logs must avoid secrets.
- Rate limit auth and AI endpoints later.

## 16. Acceptance Criteria

Use this grading:

- PASS: implemented, verified, and usable.
- PARTIAL: implemented but incomplete or with known gaps.
- FAIL: implemented but broken.
- MISSING: not implemented.
- NOT VERIFIED: implementation exists but was not tested.

Score scale:

- 0 = Missing.
- 1 = Stub only.
- 2 = Partial / fragile.
- 3 = Usable but incomplete.
- 4 = Mostly complete.
- 5 = MVP-quality.

Strict acceptance:

- Placeholder pages do not count as completed features.
- Local-only frontend mocks do not count as backend-backed product features.
- AI suggestions do not count unless draft lifecycle exists.
- Source reference is incomplete unless it can navigate back to book/note/capture.
- Parser is incomplete unless unit-tested and deterministic.

Current baseline grading:

| Area | Status | Score |
|---|---:|---:|
| Backend foundation | PASS | 5 |
| Frontend foundation | PASS | 5 |
| Auth | PASS | 4 |
| Book library | PASS | 4 |
| Book detail cockpit UI | PARTIAL | 3 |
| Notes | MISSING | 0 |
| Parser | PARTIAL frontend-local only | 2 |
| Quick Capture | PARTIAL frontend-local only | 2 |
| Source references | PARTIAL UI/local only | 2 |
| Knowledge objects | MISSING backend, PARTIAL UI preview | 1 |
| Daily | MISSING backend, PARTIAL UI fallback | 1 |
| Forum | MISSING | 0 |
| AI assistance | MISSING backend, PARTIAL UI empty state | 1 |

## 17. Implementation Rules

- Do not use `.7z` archives.
- Use the current GitHub repo only.
- Do not use cached repo summaries.
- Do not fake completed features.
- Do not invent page numbers.
- Do not commit secrets.
- Do not let AI overwrite user content.
- Do not jump phases.
- Preserve working code.
- Keep the app runnable at each phase.
- Work in checkpoints after the user says `build`.
- When the user says `continue`, continue from the exact previous checkpoint.
- Do not rewrite working files unnecessarily.
- Prefer small vertical slices that end with tests/build passing.

## 18. First Build Plan

### Current Repo Decision

The repository already has Phase 1 and most of Phase 2 implemented and verified. The correct next build is not another foundation rebuild.

Next phase after spec approval:

Phase 3 - Notes + Parser.

Reason:

- Core MVP loop requires notes and deterministic parser before persisted Quick Capture, source references, knowledge objects, daily, forum, or AI can be useful.
- Existing Quick Capture is frontend-local and should not be treated as complete.
- Existing cockpit UI should be preserved and connected to real backend data incrementally.

### First Build Checkpoint After User Says `build`

Goal:

Implement backend-backed notes and deterministic parser preview without changing backend architecture or replacing existing auth/book/library code.

Files/modules likely created:

- `backend/src/main/java/com/bookos/backend/parser/*`
- `backend/src/main/java/com/bookos/backend/note/controller/*`
- `backend/src/main/java/com/bookos/backend/note/dto/*`
- `backend/src/main/java/com/bookos/backend/note/entity/*`
- `backend/src/main/java/com/bookos/backend/note/repository/*`
- `backend/src/main/java/com/bookos/backend/note/service/*`
- `backend/src/main/java/com/bookos/backend/source/*`
- `backend/src/test/java/com/bookos/backend/parser/*`
- `backend/src/test/java/com/bookos/backend/note/*`
- `frontend/src/api/notes.ts`
- `frontend/src/api/parser.ts`
- `frontend/src/views/NotesView.vue`
- `frontend/src/views/NoteDetailView.vue`
- `frontend/src/components/notes/*`

Files/modules likely modified:

- `backend/src/main/java/com/bookos/backend/common/enums/NoteBlockType.java`
- `backend/src/main/java/com/bookos/backend/security/SecurityConfig.java`
- `frontend/src/router/index.ts`
- `frontend/src/components/Sidebar.vue`
- `frontend/src/views/BookDetailView.vue`
- `frontend/src/types/index.ts`

Acceptance criteria for first build checkpoint:

- Backend has `BookNote`, `NoteBlock`, and `SourceReference` persisted.
- Parser preview endpoint works.
- Parser supports requested emoji markers, English page syntax, Chinese page syntax, tags, and `[[Concept]]`.
- Parser tests pass.
- Authenticated user can create/list/update/delete notes for their own books.
- Book detail can navigate to notes.
- Existing auth/book/library tests still pass.
- Frontend typecheck/build pass.

What not to build in first checkpoint:

- AI.
- Forum.
- Advanced graph.
- Daily prompt generation.
- Capture conversion.
- Project mode.

