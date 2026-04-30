# BookOS Import/Export

Last verified: 2026-04-30 at `9951c85b42b170c54b009c3c6a8e4bf50336e73a`.

## Implementation Source Of Truth

- Export controller: `backend/src/main/java/com/bookos/backend/backup/controller/DataExportController.java`
- Import controller: `backend/src/main/java/com/bookos/backend/backup/controller/DataImportController.java`
- Service: `backend/src/main/java/com/bookos/backend/backup/service/ImportExportService.java`
- Frontend page: `frontend/src/views/ImportExportView.vue`
- Frontend API adapter: `frontend/src/api/importExport.ts`
- Integration tests: `backend/src/test/java/com/bookos/backend/backup/ImportExportIntegrationTest.java`

## Export Endpoints

All export endpoints require authentication and are scoped to the current user.

| Method | Path | Format | Scope |
| --- | --- | --- | --- |
| `GET` | `/api/export/json` | BookOS JSON backup | Current user's library, notes, captures, quotes, action items, concepts, knowledge objects, source references, projects, project applications, authored forum threads, and daily reflections. |
| `GET` | `/api/export/book/{bookId}/json` | BookOS JSON backup | Single book in the current user's library plus user-owned records linked to that book. |
| `GET` | `/api/export/book/{bookId}/markdown` | Markdown | Single book notes, note blocks, quotes, action items, concepts, source references, and backlink summary. |
| `GET` | `/api/export/quotes/csv` | CSV | Current user's archived-false quotes. |
| `GET` | `/api/export/action-items/csv` | CSV | Current user's archived-false action items. |
| `GET` | `/api/export/concepts/csv` | CSV | Current user's archived-false concepts. |

## Import Endpoints

All import endpoints require authentication. Preview is read-only; commit writes records only after the user explicitly confirms in the frontend.

| Method | Path | Behavior |
| --- | --- | --- |
| `POST` | `/api/import/preview` | Parses and validates the submitted payload, returns create counts, duplicate signals, warnings, unsupported fields, source-reference issues, and page-number issues. It does not write records. |
| `POST` | `/api/import/commit` | Rebuilds the import plan and writes supported non-duplicate records into the current user's account. Existing records are skipped, not overwritten. |

## Supported Import Types

| Import type | Supported records |
| --- | --- |
| `BOOKOS_JSON` | Books, source references, notes, captures, quotes, action items, concepts, knowledge objects, and projects. |
| `MARKDOWN_NOTES` | One imported book plus one Markdown note. Page markers are left in note content for deterministic parsing; missing pages remain `null`. |
| `QUOTES_CSV` | Books and quotes. Malformed page numbers become `null` and generate preview warnings. |
| `ACTION_ITEMS_CSV` | Books and action items. Malformed page numbers become `null` and generate preview warnings. |

## Current Limitations

- `BOOKOS_JSON` export includes project applications, authored forum threads, and daily reflections, but MVP import currently restores books, source references, notes, captures, quotes, action items, concepts, knowledge objects, and projects only.
- There is no concept CSV import endpoint or import type yet.
- Import conflict handling is skip-only; it does not merge or overwrite existing content.
- Import jobs are not persisted as durable audit records.
- File upload is handled in the browser and sent as text content with a 1 MB backend validation limit.

## Safety Rules

- Exports must never include another user's private data.
- Single-book export requires the book to be in the current user's library.
- Unknown or malformed page numbers remain `null`; BookOS must not invent page numbers.
- Source references are recreated or reused for the importing user when supplied by BookOS JSON.
- Imported records are private by default unless a future explicit sharing workflow is added.
- Preview must not write database records.
- Commit must not overwrite existing records automatically.
- Do not import copyrighted book passages as seed data.

## Verification Coverage

`ImportExportIntegrationTest` verifies:

- Current-user JSON export and JSON backup roundtrip with source references.
- Cross-user all-data export isolation.
- Cross-user single-book export denial.
- Import preview does not write records.
- Import commit writes after confirmation payload.
- Duplicate detection for Markdown imports.
- Malformed page numbers are reported and stay `null`.
- Single-book Markdown export is readable.
- Quotes, action items, and concepts CSV exports are scoped and readable.

The frontend `ImportExportView` verifies the user flow at implementation level by:

- Loading real books from the API.
- Calling real export endpoints.
- Showing preview counts and warnings before commit.
- Requiring an Element Plus confirmation dialog before commit.
- Showing actionable error states when backend calls fail.
