# BookOS E2E Smoke Tests

Last updated: 2026-04-29.

BookOS uses Playwright for browser-level MVP smoke tests. The tests are intentionally separate from the normal CI workflow until they have enough runtime history to be reliable in GitHub Actions.

## Scope

The E2E smoke suite covers:

- Fresh user registration through the browser.
- Dashboard load.
- Book creation through the browser.
- Personal library attachment.
- Reading status, progress, and rating updates through real APIs with browser verification.
- Markdown note and parsed note block creation.
- Quick Capture parser preview for emoji, page marker, tag, and `[[Concept]]`.
- Capture conversion to note, quote, and action item.
- Quote detail, action-item completion/reopen, and source opening.
- Parsed concept review and concept detail.
- Cmd/Ctrl+K global search.
- Graph page real-data or honest empty-state rendering.
- MockAIProvider draft generation, edit, accept, and reject.
- Source-linked forum thread creation and comments.
- Admin ontology default JSON load and dry-run import.

## Test Environment

`npm run e2e` starts:

- Backend on `http://127.0.0.1:18080` with Spring profile `test`.
- A production frontend build served by Vite preview on `http://127.0.0.1:5174`.

The backend test profile uses:

- H2 in-memory database.
- Flyway migrations.
- Local deterministic seed accounts and forum defaults.
- No production secrets.
- No external AI provider.

The database is recreated when the backend process restarts. Tests use unique per-run emails and titles, so they do not require cleanup endpoints.

## One-Time Browser Install

From `frontend/`:

```powershell
npx playwright install chromium
```

## Run Tests

From `frontend/`:

```powershell
npm run e2e
```

Headed mode:

```powershell
npm run e2e:headed
```

## Ports

Override ports if needed:

```powershell
$env:E2E_BACKEND_PORT=18081
$env:E2E_FRONTEND_PORT=5175
npm run e2e
```

## Artifacts

Failure artifacts are written under ignored frontend folders:

- `frontend/test-results/`
- `frontend/playwright-report/`
- `frontend/blob-report/`

Do not commit generated Playwright artifacts.

## Latest Public Beta Verification

Run date: 2026-04-29.

Command:

```powershell
npm run e2e
```

Result: PASS.

Summary:

- 2 Playwright tests passed.
- Runtime was 32.1 seconds on the local Windows development machine.
- Backend used the `test` profile on port `18080`.
- Frontend preview used port `5174`.
- No external AI provider or production secret was required.
