# BookOS Current State

Last reviewed: 2026-04-28.

This document records the implementation state of the current `main` branch. Older planning and UI audit documents in `docs/` are historical unless explicitly marked as current.

## Repository State

- Current verified branch: `main`.
- Current verified SHA: `ba9f33d40b0cfa7eac8ca0fff9103e0cf47f77c7`.
- Local `main` matches `origin/main` at the time of this review.

## Implemented Backend Modules

- Authentication and current-user APIs.
- Book catalog and personal library APIs.
- Notes, note blocks, deterministic parser, and quick capture APIs.
- Source references and entity links/backlinks.
- Quotes and action items, including capture conversion.
- Concept review, concepts, and minimal knowledge objects.
- Daily sentence, daily design prompt, reflections, skip/regenerate, and prototype task creation.
- Structured forum categories, threads, comments, likes, bookmarks, reports, and templates.
- Global search across owned/visible books, notes, captures, quotes, action items, concepts, knowledge objects, and forum threads.
- Graph preview endpoints for books and concepts using real links/source-backed objects.
- MockAIProvider suggestions with draft lifecycle: generate, edit, accept, reject.

## Implemented Frontend Modules

- Auth pages and route guards.
- Book library and book detail cockpit.
- Notes, capture inbox, quotes, action items, concepts, knowledge objects, and forum workspaces.
- Source reference drawer and backlinks sections.
- Right rail with source reference, real action items, and MockAIProvider draft suggestions.
- Cmd/Ctrl+K global search dialog.
- Real graph preview wiring on book detail.
- Responsive BookOS design system and cockpit shell.

## Known Limitations

- Graph preview is intentionally lightweight and is not an advanced graph editor.
- MockAIProvider does not call external AI providers and does not produce production-grade semantic extraction.
- Accepting a Mock AI suggestion only records the draft decision; it does not overwrite or create user content automatically.
- Forum moderation UI and realtime notifications are not complete.
- Project mode remains future work.

## Source And AI Rules

- Source references must preserve original context.
- Unknown page numbers must stay `null`; the system must not invent page numbers.
- AI suggestions are drafts until accepted, edited, or rejected by the user.
- AI output must never overwrite user-authored content automatically.

## Verification Commands

Backend:

```powershell
Set-Location backend
.\mvnw.cmd test
```

Frontend:

```powershell
Set-Location frontend
npm install
npm run typecheck
npm run build
```

## Hygiene Rules

The repository must not commit:

- `backend.zip`
- `.7z` or generated archive files
- `*.log`
- `*.out`
- `*.err`
- `backend/target`
- `frontend/dist`
- `frontend/node_modules`
- local JDK archives or extracted JDK folders under `tools/`
