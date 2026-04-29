# BookOS Public Beta 0.1 Readiness Review

Last updated: 2026-04-29.

Current checked-out SHA: `1734e5399d5edc6f8fcd683228d9b26d58f1b847`.

Release tag target: `v0.1.0-beta`.

## Release Scope

BookOS Public Beta 0.1 packages the current MVP release candidate for repeatable local and simple-server deployment. The beta includes the full-stack reading, note-taking, source reference, knowledge, forum, project, analytics, import/export, graph, search, and draft-only AI workflows currently implemented in the repository.

This release does not claim production-hosted operations, advanced graph analysis, advanced project analytics, automated moderation, or mandatory external AI support.

## Backend Status

- Java 21 Spring Boot backend is present.
- Maven wrapper is present.
- Flyway migrations are present through `V8`.
- Hibernate normal runtime mode validates the migrated schema.
- Actuator health endpoint is enabled.
- Authentication, ownership checks, validation, source references, search/graph scoping, forum permissions, and AI draft-only workflow are implemented in code and covered by existing backend tests where practical.

## Frontend Status

- Vue 3 + TypeScript + Vite frontend is present.
- Routes and views exist for the major beta modules: dashboard, library, book detail, notes, captures, quotes, action items, concepts, knowledge, daily, forum, graph, projects, analytics, review/mastery, import/export, and admin ontology import.
- UI uses the BookOS cockpit design system and right rail/source drawer patterns.
- Playwright E2E smoke tests are present and documented, but are not yet a required CI gate.

## Database and Migration Status

- MySQL is the target runtime database.
- `docker-compose.yml` starts local MySQL.
- `docker-compose.full.yml` starts MySQL, backend, and frontend.
- Flyway migrations are repeatable on empty databases.
- Production/shared data must never be dropped for routine release operations.

## Docker Status

- `backend/Dockerfile` builds the backend with Maven and runs a JRE image as a non-root user.
- `frontend/Dockerfile` builds the frontend and serves it with Nginx.
- `docker-compose.full.yml` requires `JWT_SECRET` and passes database, CORS, seed, Flyway, and AI environment variables.
- Docker Compose config validation is part of the release verification commands.

## CI Status

- `.github/workflows/ci.yml` exists.
- Backend job uses Java 21 and runs Maven tests.
- Frontend job uses Node 24, `npm ci`, typecheck, and production build.
- CI does not require production secrets.
- E2E is intentionally documented as local/optional until stable enough for required CI gating.

## Security Status

- `.env` files are ignored.
- Password hashing uses BCrypt.
- JWT secret is externalized.
- CORS is environment-controlled.
- Markdown rendering is sanitized in frontend rendering paths.
- Private data scoping is a release-critical rule for notes, captures, quotes, action items, source references, search, graph, AI suggestions, projects, and forum private context.
- Admin ontology import is admin-only.

## AI Safety Status

- MockAIProvider remains the default local provider.
- OpenAI-compatible provider is optional and environment-controlled.
- Missing external provider key fails safe.
- AI suggestions are drafts.
- Accept/edit/reject workflow is explicit.
- Accepted suggestions do not overwrite original user content automatically.
- Tests must not call external providers.

## Source Reference Status

- Source references are central release invariants.
- Unknown page numbers remain `null`.
- Page numbers must never be invented.
- Derived objects should preserve book, note, note block, raw capture, quote/action/concept, page range, source text, and source confidence where available.

## Project Mode Status

- Project Mode MVP is implemented for project records, problems, applications, design decisions, playtest plans/findings, knowledge links, and lens reviews.
- Project data is user-owned.
- Advanced project analytics and automated project planning remain future work.

## Import/Export Status

- User-owned export/import MVP exists for JSON, Markdown, and CSV-oriented flows.
- Import preview must be reviewed before commit.
- Import must preserve source references where supplied.
- Unknown imported pages remain `null`.
- Conflict resolution is limited and remains a known beta risk.

## Known Limitations

- Public beta deployment still requires operator-managed TLS, database backup policy, environment variables, and monitoring.
- E2E browser tests are not mandatory CI gates yet.
- Graph workspace is practical but not advanced.
- Forum moderation is basic and not realtime.
- Review/mastery is lightweight and not advanced spaced repetition.
- External AI provider support is optional and should be treated as beta.
- Manual browser QA must be completed before tagging.

## Latest Local Verification

Run date: 2026-04-29.

| Check | Result | Notes |
| --- | --- | --- |
| Current SHA | PASS | `1734e5399d5edc6f8fcd683228d9b26d58f1b847` |
| Backend tests | PASS | `.\mvnw.cmd test`, 50 tests passed. |
| Frontend typecheck | PASS | `npm run typecheck` passed. |
| Frontend production build | PASS | `npm run build` passed. |
| Playwright E2E smoke | PASS | `npm run e2e`, 2 tests passed in 32.1s. |
| Compose config | PASS | `docker compose -f docker-compose.yml config` and `docker compose -f docker-compose.full.yml config` passed with `JWT_SECRET` set. |
| Full-stack Docker build | PASS | `docker compose -f docker-compose.full.yml build` passed after lockfile sync. |
| Full-stack Docker health smoke | PASS | Isolated project `bookos_beta_check` started MySQL/backend/frontend; backend health returned `{"status":"UP","groups":["liveness","readiness"]}` and frontend returned HTTP 200. |
| Secret pattern scan | PASS | No common GitHub/OpenAI/Slack-style token patterns found in source/docs scan. |
| Committed artifact scan | PASS | No committed `.env`, archives, logs, `backend/target`, `frontend/dist`, or `frontend/node_modules` paths found. |
| Manual browser QA | PARTIAL | Automated E2E covers the main MVP path; full human mobile/tablet checklist remains pre-tag work. |

Docker caveat: an existing local database volume from a pre-Flyway BookOS build can fail startup with Flyway's non-empty-schema warning. Back up the data, then either migrate/baseline intentionally or use a clean Compose project/volume for beta verification. Do not drop shared or production data.

## Release Tag Instructions

After all checks pass and the working tree is intentionally reviewed:

```bash
git tag -a v0.1.0-beta -m "BookOS Public Beta 0.1"
git push origin v0.1.0-beta
```

Do not create the tag automatically from this document.

## Post-Release Monitoring Checklist

- Backend health endpoint remains healthy.
- Login and token refresh behavior remain stable.
- Flyway migration logs show no failures.
- Search and graph response times are acceptable on beta data.
- Source drawer/source-open actions do not error.
- Forum report/moderation actions work for admin/moderator roles.
- AI provider status reports mock/disabled/external configuration without exposing secrets.
- MySQL backup job completes.
- Frontend error logs do not show recurring route or API adapter failures.
