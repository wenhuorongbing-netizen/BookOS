# BookOS E2E Browser Tests

Last updated: 2026-05-01.

BookOS uses Playwright for browser-level MVP and usability-path tests. These tests remain outside the required push/pull-request CI workflow because they start both backend and frontend processes and take longer than the compile/test gate.

The E2E workflow is a manual release gate through `.github/workflows/e2e.yml`. It can run either:

- `full`: all Playwright browser tests.
- `usability`: only first-15-minutes usability paths across user modes.

## Full Suite Scope

The full E2E suite covers:

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
- First-15-minutes usability paths for Reader, Note-Taker, Game Designer, Researcher, Community, and Advanced modes.

## Usability Suite Scope

`npm run e2e:usability` focuses on whether a new user can complete meaningful first-session workflows:

- Reader Mode: add a book, set reading state, capture, convert to quote, and open source.
- Note-Taker Mode: create a note, use the capture guide, convert to action, and process captures.
- Game Designer Mode: create a project, apply a quote, create a design decision, and open the project cockpit.
- Researcher Mode: review a concept marker, open concept detail/graph, and start a review session.
- Community Mode: create a source-linked thread, add a comment, and open source context.
- Advanced Mode: open graph, export data, and use MockAIProvider drafts safely.

These tests are automated usability-path checks, not human user research.

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

First-15-minutes usability suite only:

```powershell
npm run e2e:usability
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

## GitHub Actions Release Gate

The manual `BookOS E2E Release Gate` workflow supports a `suite` input:

- `full` runs `npm run e2e`.
- `usability` runs `npm run e2e:usability`.

Do not make this workflow required for every PR until runtime and flake history are acceptable for the team.

## Latest Local Verification

Run date: 2026-05-01.

Commands:

```powershell
npm run e2e:usability
npm run e2e
```

Result: PASS.

Summary:

- Usability suite: 6 Playwright tests passed in 45.8 seconds.
- Full suite: 31 Playwright tests passed in 2.0 minutes.
- Backend used the `test` profile on port `18080`.
- Frontend preview used port `5174`.
- No external AI provider or production secret was required.
