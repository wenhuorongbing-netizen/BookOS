# BookOS Codex Agent Guide

This file is the handoff guide for Codex or other coding agents working in this repository.

## Project Structure

- `backend/`: Spring Boot backend, Java 21, Maven wrapper, Flyway migrations, integration tests.
- `frontend/`: Vue/Vite frontend, TypeScript, Playwright E2E tests.
- `docs/`: product, release, security, API, usability, and handoff documentation.
- `.github/workflows/`: CI workflow definitions.
- `docker-compose.yml`: local MySQL.
- `docker-compose.full.yml`: optional full-stack local deployment.
- `report.md`: cumulative Codex work log. Append only.

## Install Dependencies

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm ci
```

## Start Commands

Local MySQL:

```powershell
docker compose up -d mysql
```

Backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd frontend
npm run dev
```

Full-stack compose config requires `JWT_SECRET`:

```powershell
$env:JWT_SECRET='replace-with-local-development-secret'
docker compose -f docker-compose.full.yml config
```

Do not commit real secrets.

## Test and Verification Commands

Backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend typecheck/build:

```powershell
cd frontend
npm ci
npm run typecheck
npm run build
```

Browser E2E:

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

Whitespace check:

```powershell
git diff --check
```

## Codex Rules

- Do not use cached summaries as source of truth. Inspect the current tree first.
- Do not use `.7z` archives.
- Do not delete or overwrite `report.md`; append only.
- Do not commit secrets, `.env`, private keys, local Codex state, logs, build outputs, archives, `node_modules`, `target`, or `dist`.
- `.env.example` files are template files only and must not contain real secrets.
- Do not invent page numbers. Unknown pages must remain null or display as page unknown.
- Do not use copyrighted book passages as seed/demo content.
- Do not call real external AI in tests or local verification.
- MockAIProvider must work without an external key.
- AI output must remain draft-only; never overwrite user content automatically.
- Preserve Source Link / Source Reference traceability for derived objects.
- Keep user-owned data scoped to the owner. Search, graph, source links, import/export, demo, and AI suggestions must not leak cross-user data.
- Do not remove routes or break deep links.
- Do not hide unfinished features by claiming they are complete.
- Do not claim human user research unless real sessions occurred.
- During Product Slimming / Workflow Hardening, do not add broad new modules unless required to fix a P0 usability blocker.

## Files and Directories Not To Modify Casually

- `report.md`: append only.
- `backend/src/main/resources/db/migration/`: add new Flyway migrations only; do not rewrite applied migrations casually.
- `.github/workflows/`: keep CI secret-free.
- `.env.example`, `backend/.env.example`, `frontend/.env.example`: templates only; no real credentials.
- Generated directories: `backend/target/`, `frontend/dist/`, `frontend/node_modules/`, Playwright reports.
- Local runtime archives under `tools/`.

## Before Finishing Any Task

Run the smallest relevant verification first, then full checks when the change is cross-cutting:

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm ci
npm run typecheck
npm run build
npm run e2e
npm run e2e:usability
cd ..
git diff --check
git status -sb
```

If a command fails, document the exact failure and do not claim completion.

## Codex Migration Notes

- Keep local Codex app state out of the project branch: `.codex/`, `.codex-transfer/`, `codex-app-transfer-*`, `codex-conversations-*`, `codex-app-conversations-*`, `*.tgz`, `*.tgz.enc`, `*.zip`, and `*.zip.enc` must not be committed to `main`.
- The only allowed place for an encrypted Codex conversation archive is the temporary branch `codex-conversation-transfer`.
- Never commit or package `auth.json`, cookies, system credentials, SSH keys, API keys, tokens, passwords, private keys, logs, caches, or raw unencrypted Codex archives.
- Before pushing migration-related changes, run `git status -sb`, `git diff --stat`, and a tracked-file sensitive filename scan.
