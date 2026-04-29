# BookOS Data Model Overview

Last updated: 2026-04-28.

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

- `AIInteraction`: local MockAIProvider interaction record with source context and JSON payload.
- `AISuggestion`: draft suggestion lifecycle record. Suggestions can be edited, accepted, or rejected, but accepting a suggestion does not overwrite user-authored content.

## Relationship Summary

- Users own notes, captures, quotes, action items, concepts, knowledge objects, source references, links, daily items, AI suggestions, and private library state.
- Books can be public/shared/private; user-specific library state lives in `UserBook`.
- Source references connect derived knowledge back to books, notes, note blocks, captures, and page ranges.
- Entity links provide the data source for backlinks and graph previews.
