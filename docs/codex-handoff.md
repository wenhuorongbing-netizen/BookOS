# BookOS Codex Handoff

Generated for migration on 2026-05-01.

Repository: `D:\【指挥中心】\节操都市\项目\BookOS`

GitHub remote: `https://github.com/wenhuorongbing-netizen/BookOS.git`

Branch before migration work: `main`

HEAD before migration work: `1fca7c65531790dd75c3a07025a0ba1597e3e2f7`

Git status before migration work: clean, up to date with `origin/main`.

## Project Goal

BookOS is a full-stack reading record, source-referenced knowledge, note-taking, review, project, and structured forum system. Its product direction is to turn reading into traceable knowledge, actions, project decisions, review loops, and source-backed discussions.

The current product emphasis is not broad feature expansion. The next work should reduce cognitive load, harden the first valuable loop, and keep advanced surfaces out of the default path until the user has enough data or explicitly chooses advanced mode.

## Current Project State

- Backend: Spring Boot 3.5, Java 21, Maven wrapper, Spring Security, JPA/Hibernate, Flyway, MySQL for local runtime, H2-backed tests.
- Frontend: Vue 3, TypeScript, Vite, Pinia, Vue Router, Element Plus, Playwright E2E tests.
- Core domains: auth, personal library, notes, captures, deterministic parsing, source references, quotes, actions, concepts, knowledge objects, daily resurfacing, structured forum, search, graph, project mode, reading review/mastery, import/export, demo workspace, and draft-only AI suggestions.
- Local development is designed to work without external AI credentials. `MockAIProvider` must remain usable without an external key.
- The repository had a clean working tree before this migration pass.

## Completed Changes In This Migration Pass

- Confirmed the actual project root is `D:\【指挥中心】\节操都市\项目\BookOS`; the Codex app shell started in a generated `C:\Users\Jack\Documents\Codex\...` directory, so all project work was explicitly run against the BookOS root.
- Confirmed `origin` is `https://github.com/wenhuorongbing-netizen/BookOS.git`.
- Checked tracked filenames for `.env`, private keys, PEM/key files, dependency folders, Codex local state, and migration archives.
- Ran a filename/content scan for common key patterns. Matches in frontend files were false positives from CSS/class names containing `task-...`, not credentials.
- Expanded `.gitignore` with generic build output, Codex state, and Codex migration archive rules.
- Replaced a previously mojibake-rendered handoff document with this ASCII handoff.
- Planned generation of `docs/codex-thread-index.md` as a sanitized index of local Codex sessions.

## Important Design Decisions

- Source references are first-class. Derived notes, quotes, actions, concepts, project applications, forum discussions, and AI drafts should preserve source traceability.
- Unknown pages must remain unknown. Do not invent page numbers; keep unknown page values as `null` or display them as page unknown.
- AI output is draft-only. It must not automatically overwrite user-owned notes, quotes, concepts, actions, projects, or forum content.
- External AI is optional. Tests and normal local verification must not require a real external provider.
- Private data must remain user-scoped. Search, graph, source links, import/export, demo workspace, and AI suggestions must not leak cross-user data.
- Demo data must be clearly labeled, resettable/deletable, and must not pollute normal analytics by default.
- Applied Flyway migrations should not be rewritten casually; add a new migration for schema changes.
- Product Slimming / Workflow Hardening should avoid adding broad new modules unless needed for a P0 usability blocker.

## Current Unfinished Tasks

- Continue Workflow Hardening 0.3.
- Keep Advanced Mode containment strong for graph, import/export, draft assistant, analytics, and ontology import.
- Improve Researcher Mode onboarding around `[[Concept]]` usage and concept review feedback.
- Improve Community Mode so source-linked discussion is the default path, not a generic forum dump.
- Unify Review and Learning Progress language for non-expert users.
- Compress Project Mode surfaces into fewer obvious next actions.
- Run real human usability sessions before claiming external user research.

## Known Issues Or Risks

- Some older docs may show mojibake in PowerShell depending on code page; verify file encoding before rewriting large Markdown files.
- Full-stack Docker config requires `JWT_SECRET`; use a temporary local value for config validation only and never commit it.
- Playwright E2E can take a few minutes. UI copy changes may require test label updates.
- Migration archives and local Codex state must never be committed to the main project branch.
- The encrypted Codex conversation archive is allowed only on the temporary `codex-conversation-transfer` branch.

## Files To Read First On The New Machine

- `README.md`
- `AGENTS.md`
- `docs/codex-handoff.md`
- `docs/codex-thread-index.md`, if present
- `docs/current-state.md`
- `docs/first-15-minutes.md`
- `docs/product-slimming-0.2-roadmap.md`
- `docs/po-decision-report.md`
- `docs/security-checklist.md`
- `backend/src/main/resources/application.yml`
- `frontend/package.json`
- `backend/pom.xml`

## Commands To Run First On The New Machine

```powershell
git status -sb
git remote -v
git branch --show-current
git rev-parse HEAD
git diff --stat
```

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm ci
npm run typecheck
npm run build
```

Optional E2E:

```powershell
cd frontend
npm run e2e
npm run e2e:usability
```

Docker config checks:

```powershell
docker compose config
$env:JWT_SECRET='bookos-local-config-check-secret-bookos-local-config-check-secret-1234'
docker compose -f docker-compose.full.yml config
Remove-Item Env:\JWT_SECRET
```

## Explicit Codex Constraints And Preferences From Earlier Work

- Inspect the current tree first; do not use cached summaries as source of truth.
- Do not use `.7z` archives for project handoff.
- Do not delete or overwrite `report.md`; append only.
- Do not commit secrets, `.env`, local Codex state, logs, build outputs, archives, `node_modules`, `target`, or `dist`.
- `.env.example` files are templates only and must not contain real credentials.
- Do not call real external AI in tests or local verification.
- Do not use copyrighted book passages as seed/demo content.
- Do not scrape books.
- Do not remove existing routes or break deep links.
- Do not hide unfinished features by claiming they are complete.
- Do not claim human user research unless real external sessions occurred.
- Prefer focused verification before completion; document exact failures if a command fails.
- Do not run destructive Git/file commands such as `git reset --hard` or `git checkout --` unless explicitly approved.

## Migration Notes

The Codex local conversation archive is not stored in this branch. It is encrypted and pushed separately to `codex-conversation-transfer`.
