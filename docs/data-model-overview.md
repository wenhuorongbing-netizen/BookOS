# BookOS Data Model Overview

Last updated: 2026-04-30.

## Identity And Access

- `User`: authenticated account with email, password hash, role, and profile.
- `Role`: role name, currently `USER`, `MODERATOR`, and `ADMIN`.
- `UserProfile`: display profile for a user.

## Books And Reading State

- `Book`: catalog book record with metadata, visibility, owner, category, authors, and tags.
- `Author`: normalized author name and slug.
- `BookAuthor`: join table between books and authors.
- `Tag`: normalized tag.
- `BookTag`: join table between books and tags.
- `UserBook`: personal library state for a user and book, including status, current page, rating, favorite state, and reading progress.

## Notes, Captures, And Parsing

- `BookNote`: user-authored Markdown note linked to a book.
- `NoteBlock`: atomic note block derived from a note or capture, with deterministic parsed type, page range, tags, concepts, raw text, and source references.
- `RawCapture`: quick capture record with raw text, parsed type, parsed page, tags, concepts, conversion state, and source references.

## Source References And Links

- `SourceReference`: source pointer for derived objects. It may include book, note, note block, raw capture, page range, location label, source text, and source confidence.
- `EntityLink`: user-scoped relationship edge between entities such as `MENTIONS`, `SOURCE_OF`, `DERIVED_FROM`, `RELATED_TO`, `DISCUSSES`, and `APPLIES_TO`.

Unknown page numbers must remain `null`. The system must not invent source pages.

## Quotes And Action Items

- `Quote`: source-backed or manually created quotation/reference text with book, optional note/capture lineage, tags, concepts, visibility, and archive state.
- `ActionItem`: source-backed or manual task with title, description, priority, completion state, source reference, visibility, and archive state.

## Concepts And Knowledge Objects

- `Concept`: reviewed `[[Concept]]` record with normalized slug, description, first book/source, ontology layer, source confidence, tags, visibility, and archive state.
- `KnowledgeObject`: minimal knowledge OS unit. Supported types include concept, principle, design lens, diagnostic question, exercise, prototype task, pattern, anti-pattern, method, example case, and synthesis.

Seeded ontology records use original descriptions only. If source pages are unknown, `pageStart` and `pageEnd` remain `null` and `sourceConfidence` must not imply page-level certainty.

## Daily System

- `DailySentence`: deterministic per-user daily resurfaced sentence/quote candidate.
- `DailyDesignPrompt`: deterministic per-user daily design prompt candidate.
- `DailyReflection`: user reflection saved against a daily item.
- `DailyHistory`: skip/regenerate/history record.

## Forum

- `ForumCategory`: structured forum category.
- `StructuredPostTemplate`: source-aware discussion template.
- `ForumThread`: structured thread with category, author, Markdown body, visibility, optional related entity, book, concept, and source reference.
- `ForumComment`: comment with author, parent comment id, and archive state.
- `ForumLike`: user/thread like edge.
- `ForumBookmark`: user/thread bookmark edge.
- `ForumReport`: user report with reason, detail, and open/resolved status.

Forum source context is only visible to viewers allowed to access the attached private entity/source.

## Mock AI

- `AIInteraction`: provider interaction record with source context, provider/task metadata, and JSON payload.
- `AISuggestion`: draft suggestion lifecycle record. Suggestions can be edited, accepted, or rejected, but accepting a suggestion does not overwrite user-authored content.
- `AIProvider`: provider interface used by the suggestion service.
- `MockAIProvider`: deterministic local/default provider used for development and tests.
- `OpenAICompatibleProvider`: optional external provider enabled only through environment variables.
- `AIProviderProperties`: environment-backed provider configuration for `AI_ENABLED`, `AI_PROVIDER`, OpenAI-compatible base URL, model, timeout, max input chars, and API key presence. Secrets are not persisted in the application database.
- `AISuggestionValidator`: validates structured draft payloads before suggestions are saved.

## Demo Workspace

- `DemoRecord`: current-user scoping record for demo-created entities. It stores user, entity type, entity id, label, and created timestamp.

Demo content is created as normal domain records so users can practice real flows. The `demo_records` table records which rows belong to the demo workspace, enabling reset/delete operations and default analytics exclusion. Demo records are clearly titled/tagged as demo, use original BookOS sample content, store unknown pages as `null`, and use low-confidence source references.

## Game Project Mode

- `GameProject`: user-owned game project with title, slug, description, genre, platform, stage, visibility, progress percentage, and archive timestamp.
- `ProjectProblem`: project-scoped design problem with status, priority, description, and optional source reference.
- `ProjectApplication`: source-backed application of reading/knowledge material to a project. It stores source entity type/id, optional source reference, application type, title, description, and status.
- `DesignDecision`: project decision record with decision text, rationale, tradeoffs, status, and optional source reference.
- `PlaytestPlan`: project playtest plan with hypothesis, target players, tasks, success criteria, and status.
- `PlaytestSession`: session record linked to a playtest plan and project, with session date, participant label, and notes.
- `PlaytestFinding`: project finding with observation, severity, recommendation, status, optional session, and optional source reference.
- `IterationLog`: project iteration record with summary, changes made, and evidence. This entity is present in the model; no dedicated controller endpoints are exposed in the current API inventory.
- `ProjectKnowledgeLink`: project-owned link to a target entity such as book, note, quote, action item, concept, knowledge object, source reference, or forum thread.
- `ProjectLensReview`: project review using a design lens/knowledge object with question, answer, optional score, status, and optional source reference.

Project Mode data is user-owned. Project applications, decisions, findings, links, and lens reviews preserve source references when derived from reading material. Unknown pages remain `null`.

## Reading Analytics, Review, And Mastery

- `ReadingSession`: user/book reading session with start/end time, optional page range, minutes read, notes count, captures count, and reflection.
- `ReviewSession`: user-owned review session with title, mode, scope type/id, start/completion timestamps, and summary.
- `ReviewItem`: source-backed review prompt linked to a review session, target entity, optional source reference, response, status, and confidence score.
- `KnowledgeMastery`: user-owned mastery record for a target entity with familiarity, usefulness, last reviewed time, optional next review time, and optional source reference.

Analytics responses are computed from persisted user-owned records such as books, notes, captures, quotes, action items, concepts, projects, reading sessions, review sessions, and mastery records. Empty accounts return zero counts and empty lists; the UI must not display fake charts or sample counts as real analytics. No separate analytics snapshot table is present in the current migration set.

Review generation is deterministic and source-aware:

- Book review generation uses the current user's quotes, action items, and concepts for the selected book.
- Concept review generation uses a current-user concept and its first source reference when available.
- Project review generation uses current-user project applications, project lens reviews, and playtest findings.
- Manually added review items validate both the target entity and optional source reference before saving.

Completing a review item with a confidence score can upsert `KnowledgeMastery` for that target. Saving a source-backed daily reflection can also upsert a lightweight mastery signal for the daily target. These updates are scoped to the current user and preserve owned source references where available.

## Import And Export

- `ImportRequest`: import payload wrapper containing import type, mode, and submitted content.
- `ImportPreviewResponse`: dry-run response summarizing records to create, possible duplicates, warnings, unsupported fields, and source/page issues.
- `ImportRecordPreview`: per-record import preview item.
- `ImportCommitResponse`: commit result summary after the user confirms import.

Import/export service objects are DTO/service-layer records rather than durable import-job entities in the current schema. Import preview must not write records. Imported unknown page numbers remain `null`, and source references are preserved when supplied.

## Relationship Summary

- Users own notes, captures, quotes, action items, concepts, knowledge objects, source references, links, daily items, AI suggestions, game projects, learning/review/mastery records, and private library state.
- Books can be public/shared/private; user-specific library state lives in `UserBook`.
- Source references connect derived knowledge back to books, notes, note blocks, captures, and page ranges. Project problems, applications, design decisions, playtest findings, lens reviews, and project knowledge links store owned `SourceReference` relationships when they are derived from reading material.
- Entity links provide the data source for backlinks and graph previews. User-created links are editable/deletable by the owner; system-created links are protected from silent deletion.
- Graph nodes and edges are built from real owned/visible records only. Unknown pages remain `null` and are displayed as unknown rather than invented.
