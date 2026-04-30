# BookOS Current State

Last reviewed: 2026-04-30.

This document records the implementation state of the current `main` branch. Older planning and UI audit documents in `docs/` are historical unless explicitly marked as current.

## Repository State

- Current verified branch: `main`.
- Current verified SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`.
- Current `origin/main` SHA at review time: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`.
- Working tree status at Prompt 8 review start: contained uncommitted checkpoint changes from prior audit/build prompts plus new graph/source traceability edits.
- MVP release-candidate infrastructure, Dockerfiles, CI, endpoint inventory, data model overview, graph workspace, admin ontology import, and release docs are committed on `main`.

## Implemented Backend Modules

- Authentication and current-user APIs.
- Book catalog and personal library APIs.
- Notes, note blocks, deterministic parser, and quick capture APIs.
- Source references and entity links/backlinks, including project application, design decision, playtest finding, project problem, lens review, and project knowledge link traceability lookups.
- Quotes and action items, including capture conversion.
- Concept review, concepts, and minimal knowledge objects.
- Daily sentence, daily design prompt, reflections, skip/regenerate, and prototype task creation.
- Structured forum categories, threads, comments, likes, bookmarks, reports, and templates.
- Global search across owned/visible books, notes, captures, quotes, action items, concepts, knowledge objects, and forum threads.
- Graph exploration endpoints for workspace, books, concepts, and projects using real links/source-backed objects. Manual user-created entity links are included in graph scopes when they touch visible nodes.
- Game Project Mode APIs for projects, problems, applications, design decisions, playtest plans/findings, knowledge links, lens reviews, and source-backed apply-to-project flows.
- Reading sessions, review sessions/items, knowledge mastery, and analytics APIs using current-user records.
- Import/export APIs for user-owned JSON, book Markdown, quote/action/concept CSV, import preview, and import commit.
- Draft-only AI suggestions with MockAIProvider by default and optional OpenAI-compatible provider configuration.

## Implemented Frontend Modules

- Auth pages and route guards.
- Book library and book detail cockpit.
- Notes, capture inbox, quotes, action items, concepts, knowledge objects, and forum workspaces.
- Source reference drawer and backlinks sections.
- Right rail with source reference, real action items, provider status, and draft-only AI suggestions.
- Cmd/Ctrl+K global search dialog.
- Real graph preview wiring on book detail.
- Game Project Mode workspaces for project list/detail, problems, applications, decisions, playtests, and lens reviews.
- Analytics, review, mastery, and import/export workspaces with loading, empty, and error states.
- Responsive BookOS design system and cockpit shell.

## Known Limitations

- Graph exploration uses a lightweight SVG workspace and is not an advanced force-directed graph editor.
- MockAIProvider is the default local provider. OpenAI-compatible AI is optional and enabled only by environment variables.
- Accepting an AI suggestion only records the draft decision; it does not overwrite or create user content automatically.
- Forum moderation is basic: lock, hide, reopen, report, and resolve are implemented; realtime notifications are not implemented.
- Project Mode is implemented at MVP depth for owned projects, problems, applications, decisions, playtest plans/findings, knowledge links, lens reviews, and source-backed apply-to-project flows. Advanced project analytics, collaboration, and planning automation remain future work.
- Import/export is implemented at MVP depth. Large-scale migration tooling and conflict-resolution UX remain limited.
- Reading analytics, review sessions, and mastery tracking use real local data, preserve owned source references where available, and are intentionally lightweight. Advanced spaced-repetition scheduling and predictive analytics are not implemented.

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
