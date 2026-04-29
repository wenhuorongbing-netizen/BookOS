# BookOS Current State

Last reviewed: 2026-04-29.

This document records the implementation state of the current `main` branch. Older planning and UI audit documents in `docs/` are historical unless explicitly marked as current.

## Repository State

- Current verified branch: `main`.
- Current verified SHA: `1734e5399d5edc6f8fcd683228d9b26d58f1b847`.
- Current `origin/main` SHA at review time: `1734e5399d5edc6f8fcd683228d9b26d58f1b847`.
- Working tree status at review start: clean.
- MVP release-candidate infrastructure, Dockerfiles, CI, endpoint inventory, data model overview, graph workspace, admin ontology import, and release docs are committed on `main`.

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
- Graph exploration endpoints for workspace, books, concepts, and projects using real links/source-backed objects.
- Draft-only AI suggestions with MockAIProvider by default and optional OpenAI-compatible provider configuration.

## Implemented Frontend Modules

- Auth pages and route guards.
- Book library and book detail cockpit.
- Notes, capture inbox, quotes, action items, concepts, knowledge objects, and forum workspaces.
- Source reference drawer and backlinks sections.
- Right rail with source reference, real action items, provider status, and draft-only AI suggestions.
- Cmd/Ctrl+K global search dialog.
- Real graph preview wiring on book detail.
- Responsive BookOS design system and cockpit shell.

## Known Limitations

- Graph exploration uses a lightweight SVG workspace and is not an advanced force-directed graph editor.
- MockAIProvider is the default local provider. OpenAI-compatible AI is optional and enabled only by environment variables.
- Accepting an AI suggestion only records the draft decision; it does not overwrite or create user content automatically.
- Forum moderation is basic: lock, hide, reopen, report, and resolve are implemented; realtime notifications are not implemented.
- Project mode remains future work.

## Release Infrastructure

- GitHub Actions workflow exists at `.github/workflows/ci.yml`.
- Backend and frontend Dockerfiles exist.
- Local full-stack compose file exists at `docker-compose.full.yml`.
- Deployment guide, endpoint inventory, data model overview, and MVP release candidate checklist are in `docs/`.

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
npm ci
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
