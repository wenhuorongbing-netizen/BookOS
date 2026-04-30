# BookOS Recovery Report

## Prompt 1 — P0 Recovery: report.md, Source-of-Truth, Endpoint Inventory Repair

Date and local time: 2026-04-30 10:55:11 +02:00.

Current branch: `main`.

Current SHA before work: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`.

Current SHA after work if available: `9951c85b42b170c54b009c3c6a8e4bf50336e73a` plus local documentation-only changes. No commit was created.

### P0 Issues Confirmed

- `report.md` was missing on current `origin/main`.
- `docs/current-state.md` still said Project mode remained future work, while `README.md`, `frontend/src/router/index.ts`, and `backend/src/main/java/com/bookos/backend/project/controller/ProjectController.java` show Project Mode exists at MVP depth.
- `docs/api-endpoint-inventory.md` omitted the complete Project Mode API section even though `ProjectController` exposes project endpoints.
- `docs/api-endpoint-inventory.md` also omitted `PUT /api/entity-links/{id}` even though `EntityLinkController` exposes it.
- `docs/data-model-overview.md` under-documented Project Mode, learning/review/mastery, import/export DTOs, and the optional OpenAI-compatible provider configuration.
- `docs/mvp-release-candidate.md` referenced an older reviewed SHA and still listed Project Mode as future roadmap work.

### P0 Issues Fixed

- Created `report.md` and added this required Prompt 1 section.
- Updated `docs/current-state.md` to the verified current `origin/main` SHA and changed Project Mode from future-only to implemented/partial.
- Added implemented backend/frontend state for Project Mode, learning/review/mastery, and import/export to `docs/current-state.md`.
- Added the complete `ProjectController` endpoint set to `docs/api-endpoint-inventory.md`.
- Added `PUT /api/entity-links/{id}` to `docs/api-endpoint-inventory.md`.
- Expanded `docs/data-model-overview.md` with Project Mode entities, learning/review/mastery entities, import/export DTOs, and optional AI provider configuration objects.
- Updated `docs/mvp-release-candidate.md` current SHA and roadmap language so Project Mode is no longer listed as an unbuilt future module.

### Files Inspected

- `README.md`
- `report.md`
- `docs/current-state.md`
- `docs/api-endpoint-inventory.md`
- `docs/data-model-overview.md`
- `docs/mvp-release-candidate.md`
- `backend/src/main/java/com/bookos/backend/project/controller/ProjectController.java`
- `backend/src/main/java/com/bookos/backend/search/controller/SearchController.java`
- `backend/src/main/java/com/bookos/backend/graph/controller/GraphController.java`
- `backend/src/main/java/com/bookos/backend/ai/controller/AISuggestionController.java`
- `frontend/src/router/index.ts`
- `frontend/src/api`
- `backend/src/main/resources/db/migration`
- `backend/src/main/java/com/bookos/backend/project/entity`
- `backend/src/main/java/com/bookos/backend/learning`
- `backend/src/main/java/com/bookos/backend/backup`
- `backend/src/main/java/com/bookos/backend/ai/service`

### Files Created

- `report.md`

### Files Modified

- `docs/current-state.md`
- `docs/api-endpoint-inventory.md`
- `docs/data-model-overview.md`
- `docs/mvp-release-candidate.md`

### Endpoint Inventory Changes

- Added Project Mode endpoints for project CRUD/archive, problems, applications, decisions, playtest plans, playtest findings, knowledge links, lens reviews, source/quote/concept/knowledge-object apply flows, and daily prototype-task creation.
- Added missing `PUT /api/entity-links/{id}`.
- Verified search, graph, and AI endpoint listings against their controllers. No new product endpoints were added.

### Data Model Docs Changes

- Added `GameProject`, `ProjectProblem`, `ProjectApplication`, `DesignDecision`, `PlaytestPlan`, `PlaytestSession`, `PlaytestFinding`, `IterationLog`, `ProjectKnowledgeLink`, and `ProjectLensReview`.
- Added `ReadingSession`, `ReviewSession`, `ReviewItem`, and `KnowledgeMastery`.
- Added import/export DTOs: `ImportRequest`, `ImportPreviewResponse`, `ImportRecordPreview`, and `ImportCommitResponse`.
- Added optional AI provider documentation for `AIProvider`, `MockAIProvider`, `OpenAICompatibleProvider`, `AIProviderProperties`, and `AISuggestionValidator`.
- Clarified that import/export objects are service/DTO objects rather than durable import-job entities in the current schema.

### Commands Run

- `git remote -v`
- `git status --short`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git fetch origin main`
- `git rev-parse origin/main`
- `git rev-list --left-right --count HEAD...origin/main`
- `Test-Path report.md`
- `Get-Content` on required docs/controllers/router files
- `rg -n "@(Get|Post|Put|Delete|Patch)Mapping" backend/src/main/java/com/bookos/backend -g "*Controller.java"`
- `rg -n "Project mode remains future work|Project Mode remains future work|future work" ...`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`

### Test Results

- Backend tests: PASS. `.\mvnw.cmd test` completed with 50 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed after stopping stale local Vite/Node processes that had locked Rolldown's native Windows binary.
- Frontend typecheck: PASS. `npm run typecheck` completed successfully.
- Frontend production build: PASS. `npm run build` completed successfully.
- Browser E2E: PASS. `npm run e2e` completed with 2 Playwright tests passing in 31.8 seconds.

### Remaining Risks

- The local Windows workspace can fail `npm ci` with `EPERM` if a Vite/Node process is holding `node_modules/@rolldown/...`. Stop local frontend dev processes before clean install verification.
- Documentation is now aligned with inspected controllers/routes, but a generated endpoint inventory would reduce future drift.
- No commit was created in this prompt.

### Next Recommended Prompt

Perform a strict release-candidate documentation drift check that compares all controller mappings and frontend routes against `README.md`, `docs/api-endpoint-inventory.md`, and `docs/current-state.md`, then produce a final pre-tag report.

## Prompt 2 — Endpoint-to-Controller Audit and Runtime API Verification

### Date and Local Time

2026-04-30 11:06:28 +02:00

### Current SHA

`9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### Controllers Audited

- `ActionItemController`
- `OntologyAdminController`
- `AISuggestionController`
- `DataExportController`
- `DataImportController`
- `BookController`
- `UserBookController`
- `RawCaptureController`
- `HealthController`
- `DailyController`
- `ForumController`
- `GraphController`
- `BookConceptController`
- `ConceptController`
- `KnowledgeObjectController`
- `AnalyticsController`
- `MasteryController`
- `ReadingSessionController`
- `ReviewController`
- `BacklinkController`
- `EntityLinkController`
- `BookNoteController`
- `ParserController`
- `ProjectController`
- `QuoteController`
- `SearchController`
- `SourceReferenceController`
- `AuthController`
- `UserController`

### Endpoints Verified

- 183 application `/api/**` controller mappings are now covered by `EndpointContractIntegrationTest`.
- `GET /actuator/health` is documented as a Spring Boot Actuator endpoint rather than an application controller method.
- Full backend runtime suite passed after adding the contract test.

### Documented-Only Endpoints Found

- None remaining after cleanup.
- Normalized stale documentation for `POST /api/notes/{noteId}/blocks` to the actual controller route `POST /api/notes/{id}/blocks`.
- Normalized `GET /api/search?q=&type=&bookId=` to `GET /api/search` with query parameters.

### Code-Only Endpoints Found

- None remaining after cleanup.
- The contract test explicitly covers existing code mappings including `GET /api/forum/templates`, `PUT /api/forum/threads/{id}/moderation`, `PUT /api/forum/reports/{id}/resolve`, `DELETE /api/projects/{id}`, and `PUT /api/entity-links/{id}`.

### Broken Endpoints Found

- None detected by the controller-mapping contract test or full backend test suite.

### Docs Updated

- Created `docs/api-contract-audit.md` with method, path, controller class, controller method, auth requirement, request DTO, response DTO, and status.
- Updated `docs/api-endpoint-inventory.md` with the contract-test verification note and route normalization.
- Updated `README.md` to match the actual note-block and search route forms.

### Tests Added

- Added `EndpointContractIntegrationTest`.
- The test fails if documented application endpoints lose controller mappings.
- The test also fails if a new `/api/**` controller mapping is added without being represented in the contract list.

### Commands Run

- `git status --short`
- `git rev-parse HEAD`
- `git rev-parse --abbrev-ref HEAD`
- `rg -n "@(Get|Post|Put|Delete|Patch)Mapping" backend/src/main/java/com/bookos/backend -g "*Controller.java"`
- `rg -n "public .*\\(" backend/src/main/java/com/bookos/backend`
- `rg -n "api/notes/\\{noteId\\}/blocks|api/search\\?q=|api/forum/categories/\\{slug\\}/threads|api/forum/threads/\\{id\\}/lock|api/forum/threads/\\{id\\}/hide|POST /api/forum/reports/\\{id\\}/resolve" README.md docs/api-endpoint-inventory.md docs/api-contract-audit.md`
- `.\mvnw.cmd -Dtest=EndpointContractIntegrationTest test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`

### Test Results

- Endpoint contract test: PASS. 2 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 52 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.

### Remaining API Risks

- `EndpointContractIntegrationTest` verifies route-to-controller mapping, not every endpoint's full business behavior. Behavior is covered by existing integration suites by module.
- Spring Actuator endpoints are outside `RequestMappingHandlerMapping`; the audit documents them separately.
- Maven logs still show Mockito dynamic-agent warnings under the current JDK. This is not a failing API issue but should be addressed before future JDK upgrades.
- Some documentation edits from the previous checkpoint remain uncommitted in the working tree; this prompt preserved them and only added the endpoint-audit changes.

### Next Recommended Prompt

Perform a strict frontend route-to-view/API-adapter audit and add a frontend route contract document so sidebar routes, router entries, view files, and API adapters stay synchronized.

## Prompt 3 — Full E2E Evidence Run and Browser Flow Repair

### Date and Local Time

2026-04-30 11:14:52 +02:00

### Current SHA

`9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### E2E Files Inspected

- `frontend/package.json`
- `frontend/playwright.config.ts`
- `frontend/e2e/admin-ontology.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `frontend/e2e/support/api.ts`
- `.github/workflows/ci.yml`
- `frontend/src/views/ProjectsView.vue`
- `frontend/src/views/ProjectNewView.vue`
- `frontend/src/views/ProjectDetailView.vue`
- `frontend/src/views/ProjectProblemsView.vue`
- `frontend/src/views/ProjectDecisionsView.vue`
- `frontend/src/views/ProjectPlaytestsView.vue`
- `frontend/src/views/ImportExportView.vue`
- `frontend/src/components/project/ProjectFormPanel.vue`
- `frontend/src/components/project/ApplyToProjectDialog.vue`
- `frontend/src/api/projects.ts`

### E2E Files Created

- `.github/workflows/e2e.yml`

### E2E Files Modified

- `frontend/e2e/mvp-core-loop.spec.ts`
- `report.md`

### Browser Flows Passed

- Register fresh user.
- Log out and log back in through the browser login form.
- Confirm dashboard loads.
- Create a book.
- Add the book to the personal library.
- Update reading status, current page, and rating through API-backed setup and verify the book detail UI.
- Open book detail.
- Create a note through real API setup and verify it on the note detail route.
- Create captures with emoji markers, page marker, tag, and `[[Concept]]`.
- Verify parsed capture metadata.
- Convert captures to note, quote, and action item.
- Open created quote detail.
- Open source from quote.
- Complete and reopen action item.
- Review parsed concept and create concept from capture.
- Open Cmd/Ctrl+K global search and search for the created book.
- Open book graph page.
- Generate MockAIProvider note-summary, action-extraction, and concept-extraction suggestions.
- Edit, accept, and reject mock AI suggestions.
- Create project.
- Apply quote to project.
- Create project problem.
- Create design decision.
- Create playtest plan and playtest finding.
- Open project graph page.
- Create forum thread attached to the test book.
- Add forum comment.
- Export user data as JSON from the import/export page.
- Log out.
- Run admin ontology dry-run import without production secrets.

### Browser Flows Failed

- None in the final Playwright run.

### Browser Flows Not Covered

- Import preview and commit are not covered by the smoke flow; export page and JSON download are covered.
- Direct concept-to-project application is not covered; quote-to-project application is covered.
- Some setup steps use backend APIs for deterministic E2E speed, especially note/capture preparation, while the critical browser routes and conversion workflows are still verified through the UI.

### Bugs Found

- No product P0/P1 runtime bugs were found during the final E2E run.
- Coverage gaps were found for explicit browser re-login, Project Mode, project graph, project application, and export.

### Bugs Fixed

- No product behavior was changed.
- Expanded the MVP core-loop Playwright spec to cover the missing browser flows.
- Added an optional `workflow_dispatch` GitHub Actions E2E workflow that does not block main CI and does not require production secrets.

### Commands Run

- `Get-Command npx | Select-Object -ExpandProperty Source`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `Get-Content frontend/package.json`
- `Get-Content frontend/playwright.config.ts`
- `Get-ChildItem -Recurse frontend/e2e`
- `npx playwright install chromium`
- `npm run e2e`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `git ls-files | rg "(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err)$|frontend/dist|backend/target|frontend/node_modules"`

### Test Results

- Backend full suite: PASS. 52 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Chromium browser install: PASS.
- Playwright E2E: PASS. 2 tests passed.
  - `admin-ontology.spec.ts`: PASS.
  - `mvp-core-loop.spec.ts`: PASS.

### Remaining E2E Risks

- E2E currently runs against the Playwright-managed Spring Boot test profile with H2, not Docker MySQL.
- The optional GitHub Actions E2E workflow was added but not run on GitHub from this local session.
- The smoke suite intentionally favors stable role/label/text selectors, but larger UI redesigns may still require selector maintenance.
- Import commit remains outside the smoke path and should be covered by a focused import/export E2E or API integration test.

### Next Recommended Prompt

Run the optional GitHub Actions E2E workflow on `main`, inspect artifacts if it fails, and fix only CI-environment blockers without changing product scope.

## Prompt 4 — Security, Ownership, Privacy, and AI Safety Audit

### Date and Local Time

- 2026-04-30 11:26:24 +02:00

### Current SHA

- Branch: `main`
- SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### Security Areas Audited

- Auth, JWT, roles, admin-only routes, user profile, books, user library, notes, captures, quotes, action items, concepts, knowledge objects, source references, entity links, backlinks, daily, forum, search, graph, projects, analytics, review, mastery, import/export, AI suggestions, optional OpenAI-compatible provider config, frontend logout/session state, admin navigation, and Markdown rendering.

### Vulnerabilities Found

- No backend P0 cross-user data leak was confirmed in the audited endpoints and integration tests.
- Frontend session hygiene gap: logout cleared auth token/user state but did not explicitly reset capture inbox state, right-rail source/AI/action context, or capture bookmark localStorage entries. This could leave stale private context visible inside the same SPA session after account switching.
- Documentation gap: no centralized security authorization matrix existed for implemented modules.
- Test coverage gap: concept/project/import-export/review/mastery/analytics/provider-secret boundaries were not covered in one focused security boundary pass.

### Vulnerabilities Fixed

- Added a security authorization matrix documenting ownership, admin/moderator capability, public/shared behavior, source reference behavior, search/graph visibility, and AI input/output behavior.
- Added explicit frontend private-state reset hooks for capture and right-rail stores.
- Updated logout to clear capture/right-rail private state and capture bookmark localStorage entries.
- Added cross-user tests for concepts, projects, project graph, book export, all-data export, review sessions, mastery, reading analytics, and project concept application.
- Added provider status test proving OpenAI-compatible API key material is not exposed by `/api/ai/status`.

### Tests Added

- `SecurityBoundaryIntegrationTest#crossUserKnowledgeProjectImportExportAndLearningBoundariesAreDenied`
- `AIProviderSecretStatusIntegrationTest#providerStatusDoesNotExposeOpenAICompatibleSecrets`

### Tests Failed

- First targeted Maven test command failed because PowerShell parsed the comma in `-Dtest=SecurityBoundaryIntegrationTest,AIProviderSecretStatusIntegrationTest`; rerunning with the property quoted passed.
- No product test failure remained after rerun.

### Files Created

- `backend/src/test/java/com/bookos/backend/ai/AIProviderSecretStatusIntegrationTest.java`
- `docs/security-authorization-matrix.md`

### Files Modified

- `backend/src/test/java/com/bookos/backend/security/SecurityBoundaryIntegrationTest.java`
- `frontend/src/stores/auth.ts`
- `frontend/src/stores/capture.ts`
- `frontend/src/stores/rightRail.ts`
- `report.md`

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `rg "class .*Controller|@RestController|@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|@PatchMapping" backend/src/main/java/com/bookos/backend`
- `rg "Security|Forbidden|AccessDenied|owner|userId|currentUser|Authentication|Principal" backend/src/main/java/com/bookos/backend backend/src/test/java/com/bookos/backend`
- `rg "v-html|markdown|sanitize|DOMPurify|logout|localStorage|sessionStorage|role|admin|forbidden|permission" frontend/src`
- `.\mvnw.cmd -Dtest=SecurityBoundaryIntegrationTest,AIProviderSecretStatusIntegrationTest test`
- `.\mvnw.cmd "-Dtest=SecurityBoundaryIntegrationTest,AIProviderSecretStatusIntegrationTest" test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`
- `git ls-files | rg "(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err)$|frontend/dist|backend/target|frontend/node_modules"`

### Verification Result

- Targeted backend security tests: PASS after quoted PowerShell property. 6 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 54 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.
- Tracked artifact check: PASS. No tracked `backend.zip`, `.7z`, logs, `.out`, `.err`, `backend/target`, `frontend/dist`, or `frontend/node_modules` paths were found.

### Remaining Security Risks

- Admin/global book catalog behavior is documented but should be revisited before multi-tenant production to make private versus shared catalog semantics stricter and more explicit.
- The frontend Markdown sanitizer is intentionally minimal; use a vetted sanitizer library before public beta exposure to untrusted Markdown at scale.
- Playwright E2E runs against the Spring Boot test profile with H2, not Docker MySQL.
- The optional E2E GitHub workflow was not executed on GitHub from this local session.
- Mockito dynamic-agent warnings appear in backend tests and should be addressed before future JDK upgrades make dynamic agent loading stricter.

### Next Recommended Prompt

Run a Docker MySQL full-stack security smoke test with two browser users and verify private source references, graph/search, import/export, and logout state isolation against the production-like compose profile.

## Prompt 5 — Project Mode Deep Acceptance and Game Design Workflow Completion

### Date and Local Time

- 2026-04-30 11:31:18 +02:00

### Current SHA

- Branch: `main`
- SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### Project Flows Verified

- Create project, edit project, archive project.
- Create project problem.
- Apply source reference to project.
- Apply quote to project.
- Apply concept to project.
- Apply knowledge object to project.
- Create source-backed design decision.
- Create and update playtest plan.
- Create, update, and delete playtest finding.
- Create project knowledge link.
- Create project lens review.
- Create prototype task from daily prompt.
- Open source from project application UI.
- Open project graph.
- Search project records.
- Create forum thread from project context with private source/project context hidden from unauthorized users.

### Project Flows Fixed

- Strengthened `ProjectModeIntegrationTest` to explicitly cover direct `POST /api/projects/{projectId}/apply/source-reference` source preservation.
- Strengthened `ProjectModeIntegrationTest` to explicitly cover source-backed design decision creation.
- No product behavior change was required; the refinement was test/evidence coverage for existing MVP behavior.

### Optional Iteration Log Status

- `IterationLog` entity and repository already exist, so the prompt's optional condition to add IterationLog only if missing was not triggered.
- Current state remains entity/repository-only: no public IterationLog controller endpoints or frontend workspace are exposed in the current API inventory.

### Source Preservation Findings

- Project applications preserve `SourceReference` for manual source-backed creation, direct source-reference application, quote application, concept application, knowledge-object application, and daily prompt prototype-task creation where a source exists.
- Design decisions, project problems, playtest findings, project knowledge links, and project lens reviews support optional owned source references.
- Unknown page numbers remain controlled by existing `SourceReference` records; no fake project page numbers were introduced.

### Tests Added

- Added assertions inside `ProjectModeIntegrationTest#projectApplicationPreservesSourceReferenceAndQuoteCanBeApplied` for:
  - Direct source reference application preserving `sourceReference.id` and `pageStart`.
  - Source-backed design decision creation preserving `sourceReference.id` and `pageStart`.

### Files Created

- None in this prompt.

### Files Modified

- `backend/src/test/java/com/bookos/backend/project/ProjectModeIntegrationTest.java`
- `report.md`

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `Get-ChildItem -Recurse -File backend/src/main/java/com/bookos/backend/project,backend/src/test/java/com/bookos/backend`
- `Get-ChildItem -Recurse -File frontend/src/views,frontend/src/api,frontend/src/components`
- `rg -n "@(Get|Post|Put|Delete|Patch)Mapping|@RequestMapping|class ProjectController|Iteration" backend/src/main/java/com/bookos/backend/project/controller/ProjectController.java backend/src/main/java/com/bookos/backend/project/service/ProjectService.java`
- `Get-Content backend/src/test/java/com/bookos/backend/project/ProjectModeIntegrationTest.java`
- `Get-Content frontend/src/api/projects.ts`
- `rg -n "PROJECT|Project|project|relatedProject|apply|prototype" backend/src/main/java/com/bookos/backend/search backend/src/main/java/com/bookos/backend/graph backend/src/main/java/com/bookos/backend/forum backend/src/main/java/com/bookos/backend/daily backend/src/test/java/com/bookos/backend`
- `Get-Content backend/src/main/java/com/bookos/backend/project/service/ProjectService.java`
- `Get-Content backend/src/main/java/com/bookos/backend/forum/service/ForumService.java`
- `Get-Content backend/src/main/java/com/bookos/backend/project/entity/IterationLog.java`
- `Get-Content backend/src/main/java/com/bookos/backend/project/repository/IterationLogRepository.java`
- `Get-Content frontend/src/views/ProjectDetailView.vue`
- `Get-Content frontend/src/views/ProjectApplicationsView.vue`
- `Get-Content frontend/src/components/project/ApplyToProjectDialog.vue`
- `.\mvnw.cmd "-Dtest=ProjectModeIntegrationTest" test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`
- `git ls-files | rg "(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err)$|frontend/dist|backend/target|frontend/node_modules"`

### Verification Result

- Targeted Project Mode test: PASS. 6 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 54 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.
- Tracked artifact check: PASS. No tracked `backend.zip`, `.7z`, logs, `.out`, `.err`, `backend/target`, `frontend/dist`, or `frontend/node_modules` paths were found.

### Remaining Project Risks

- IterationLog is not exposed through controller endpoints or frontend UI yet; it remains model groundwork rather than a usable project workflow.
- Project-specific action items are intentionally not implemented; Project Detail labels this honestly and uses applications/playtest findings as the MVP task surface.
- E2E covers quote-to-project and project graph minimally, but direct concept/knowledge-object/source-reference application is currently covered by backend integration tests, not browser tests.
- E2E still runs against the test profile with H2, not Docker MySQL.
- Existing uncommitted documentation, E2E, security, and endpoint-audit changes from earlier prompts remain in the worktree and were preserved.

### Next Recommended Prompt

Run a production-like Docker MySQL Project Mode smoke test with two browser users: create private source-backed project applications, verify graph/search/forum privacy from the second user, and decide whether IterationLog should become a public MVP workflow or stay internal.

## Prompt 6 — Import Export Controller Verification and Data Roundtrip Hardening

### Date and Local Time

- 2026-04-30 11:40:55 +02:00

### Current SHA

- Branch: `main`
- SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### Controllers Found

- `backend/src/main/java/com/bookos/backend/backup/controller/DataExportController.java`
- `backend/src/main/java/com/bookos/backend/backup/controller/DataImportController.java`
- `backend/src/main/java/com/bookos/backend/backup/service/ImportExportService.java`
- No separate `DataExportService` or `DataImportService` exists; import and export behavior is implemented in the unified `ImportExportService`.

### Endpoints Verified

- `GET /api/export/json`
- `GET /api/export/book/{bookId}/json`
- `GET /api/export/book/{bookId}/markdown`
- `GET /api/export/quotes/csv`
- `GET /api/export/action-items/csv`
- `GET /api/export/concepts/csv`
- `POST /api/import/preview`
- `POST /api/import/commit`

### Documented-Only Endpoints Found

- None for the required import/export endpoints. The endpoint inventory now maps these endpoints to `DataExportController`, `DataImportController`, and `ImportExportService`.

### Implementation Changes

- Added backend integration coverage for single-book JSON export, action-item CSV export, concept CSV export, and cross-user single-book export denial.
- Confirmed all export paths are current-user scoped and that single-book export requires the book to be in the current user's library.
- Confirmed import preview is read-only and non-writing.
- Confirmed import commit writes only after explicit `/api/import/commit`.
- Normalized Markdown page-marker warning detection in `ImportExportService` to use lowercased English markers and Unicode-safe Chinese page marker checks.
- Added `docs/import-export.md`.
- Updated `docs/api-endpoint-inventory.md` with controller-method mappings and current MVP caveats.
- Updated `docs/backup-restore.md` to separate operational MySQL backups from user-owned import/export portability.

### Roundtrip Tests

- JSON backup export and import commit roundtrip with source references.
- Cross-user all-data export isolation.
- Cross-user single-book export denial.
- Import preview writes no records.
- Import commit creates records only after confirmation payload.
- Duplicate detection for Markdown imports.
- Malformed CSV page numbers produce warnings and remain `null`.
- Book Markdown export is readable.
- Quotes CSV export is readable.
- Action item CSV export is current-user scoped and includes source-reference evidence.
- Concept CSV export is current-user scoped and readable.

### Files Created

- `docs/import-export.md`

### Files Modified

- `backend/src/main/java/com/bookos/backend/backup/service/ImportExportService.java`
- `backend/src/test/java/com/bookos/backend/backup/ImportExportIntegrationTest.java`
- `docs/api-endpoint-inventory.md`
- `docs/backup-restore.md`
- `report.md`

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `rg -n "import|export|Import|Export|backup|Backup" backend/src/main/java backend/src/test/java frontend/src docs README.md`
- `Get-ChildItem -Recurse -File backend/src/main/java/com/bookos/backend/backup`
- `Get-Content backend/src/main/java/com/bookos/backend/backup/controller/DataExportController.java`
- `Get-Content backend/src/main/java/com/bookos/backend/backup/controller/DataImportController.java`
- `Get-Content backend/src/main/java/com/bookos/backend/backup/service/ImportExportService.java`
- `Get-Content backend/src/test/java/com/bookos/backend/backup/ImportExportIntegrationTest.java`
- `Get-Content frontend/src/api/importExport.ts`
- `Get-Content frontend/src/views/ImportExportView.vue`
- `Get-Content docs/api-endpoint-inventory.md`
- `Get-Content docs/backup-restore.md`
- `Get-Content docs/import-export.md`
- `.\mvnw.cmd "-Dtest=ImportExportIntegrationTest" test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`
- `git ls-files | rg "(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err)$|frontend/dist|backend/target|frontend/node_modules"`

### Verification Result

- Targeted import/export backend test: PASS. 4 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 55 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.
- Tracked artifact check: PASS. No tracked `backend.zip`, `.7z`, logs, `.out`, `.err`, `backend/target`, `frontend/dist`, or `frontend/node_modules` paths were found.

### Remaining Data Portability Risks

- `BOOKOS_JSON` export includes project applications, authored forum threads, and daily reflections for archival portability, but MVP import does not recreate those record types yet.
- Import conflict handling is skip-only; there is no user-facing merge/update mode.
- Import jobs are not persisted as durable audit records.
- Frontend E2E covers the import/export page at smoke level; backend integration tests provide the deeper roundtrip evidence.
- The backend request DTO caps import content at 1 MB, which is safe for MVP but insufficient for large libraries.
- Automated tests run against H2/test profile, not Docker MySQL.

### Next Recommended Prompt

Implement export/import coverage for project applications, user-authored forum thread metadata, and daily reflections, or explicitly split them into export-only archival records in the product spec and UI.

## Prompt 7 — Analytics, Review, Mastery Real-Data Productization

### Date and Local Time

- 2026-04-30 11:51:36 +02:00

### Current SHA

- Branch: `main`
- SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### Analytics Endpoints Verified

- `GET /api/analytics/reading`
- `GET /api/analytics/knowledge`
- `GET /api/analytics/books/{bookId}`
- Verified analytics are computed from current-user persisted records, including reading sessions, quotes, reviews, mastery, and project applications.
- Verified a fresh user receives honest zero counts and empty arrays instead of seeded or fake analytics.
- Verified cross-user book analytics returns a permission denial and does not expose private records.

### Review Endpoints Verified

- `GET /api/review/sessions`
- `POST /api/review/sessions`
- `GET /api/review/sessions/{id}`
- `POST /api/review/sessions/{id}/items`
- `PUT /api/review/items/{id}`
- `POST /api/review/generate-from-book`
- `POST /api/review/generate-from-concept`
- `POST /api/review/generate-from-project`
- Verified review generation from book, concept, and project sources uses only accessible records.
- Verified source references are preserved for generated and manually added review items.
- Verified cross-user session mutation and project review generation are blocked.

### Mastery Endpoints Verified

- `GET /api/mastery`
- `GET /api/mastery/target`
- `PUT /api/mastery/target`
- Verified completed review items can upsert user-scoped mastery records.
- Verified source-backed daily reflection can update mastery with the same source reference.
- Verified cross-user mastery updates are blocked.

### Fake/static Data Removed

- No backend fake analytics or invented scores were found.
- Updated the analytics UI so a zero-data account shows an honest empty state instead of a dashboard of zero-heavy charts.
- No fake charts, fake counts, or invented mastery scores were added.

### Tests Added

- Added empty analytics assertions in `LearningIntegrationTest#emptyAnalyticsResponsesAreHonestZeros`.
- Added concept/project review generation and source-preservation coverage in `LearningIntegrationTest#conceptProjectReviewGenerationAndManualItemsPreserveOwnedSources`.
- Strengthened analytics user-scoping in `LearningIntegrationTest#analyticsUseCurrentUserDataAndMasteryIsScoped`.
- Strengthened daily integration coverage so source-backed prompt reflection verifies mastery creation in `DailyIntegrationTest#dailySelectionIsStableCanRegenerateAndPreservesSourceReferences`.

### Files Created

- None.

### Files Modified

- `backend/src/test/java/com/bookos/backend/learning/LearningIntegrationTest.java`
- `backend/src/test/java/com/bookos/backend/daily/DailyIntegrationTest.java`
- `docs/api-endpoint-inventory.md`
- `docs/data-model-overview.md`
- `docs/current-state.md`
- `frontend/src/views/AnalyticsView.vue`
- `frontend/src/views/ReviewView.vue`
- `frontend/src/views/ReviewSessionView.vue`
- `report.md`

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `rg -n "class .*Analytics|class .*Review|class .*Mastery|class .*ReadingSession|@RequestMapping|@GetMapping|@PostMapping|@PutMapping|DailyReflection|KnowledgeMastery|ReviewSession|ReadingSession" backend/src/main/java/com/bookos/backend backend/src/test/java/com/bookos/backend`
- `rg -n "AnalyticsView|ReviewView|ReviewSessionView|MasteryView|DailyView|analytics|review|mastery|reading-sessions" frontend/src`
- `rg -n "Analytics|Review|Mastery|ReadingSession|DailyReflection|learning|review|mastery|analytics" docs README.md report.md`
- `.\mvnw.cmd "-Dtest=LearningIntegrationTest,DailyIntegrationTest" test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npx --version`
- `npm run e2e`
- `git ls-files | rg '(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err)$|frontend/dist|backend/target|frontend/node_modules'`

### Verification Result

- Targeted learning/daily backend tests: PASS. 5 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 57 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.
- Tracked artifact check: PASS. No tracked `backend.zip`, `.7z`, logs, `.out`, `.err`, `backend/target`, `frontend/dist`, or `frontend/node_modules` paths were found.

### Remaining Learning-System Risks

- Review generation UI still expects numeric source identifiers; a picker-driven workflow would reduce user error.
- Mastery scoring is intentionally lightweight and not a full spaced repetition engine.
- Analytics are computed live from current records; no historical analytics snapshot or trend table exists yet.
- E2E covers the full MVP smoke loop but does not deeply exercise analytics/review/mastery pages; backend integration tests cover the critical data and ownership boundaries.
- Automated backend tests run against H2/test profile, not Docker MySQL.
- Mockito dynamic-agent warnings remain during test execution.

### Next Recommended Prompt

Run a browser smoke test focused specifically on Analytics, Review, and Mastery UX: create source-backed records, generate review sessions from book, concept, and project sources, complete review items, verify mastery updates, and decide whether to add selector-driven generation UI instead of numeric IDs.

## Prompt 8 — Graph Relationship Editor and Source Traceability Completion

### Date and Local Time

- 2026-04-30 12:02:47 +02:00

### Current SHA

- Branch: `main`
- SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

### Graph Endpoints Verified

- `GET /api/graph`
- `GET /api/graph/book/{bookId}`
- `GET /api/graph/concept/{conceptId}`
- `GET /api/graph/project/{projectId}`
- Verified graph responses are built from persisted user-owned or visible records only.
- Verified concept graph source-reference nodes and cross-user concept graph denial.
- Verified project graph includes project entities, owned source references, and user-created manual `EntityLink` edges when those links touch visible project graph nodes.
- Verified project graph does not introduce fake nodes or fake edges.

### Entity Link Endpoints Verified

- `GET /api/entity-links`
- `POST /api/entity-links`
- `PUT /api/entity-links/{id}`
- `DELETE /api/entity-links/{id}`
- Verified owner-scoped create, update, and delete behavior.
- Verified cross-user entity link deletion is blocked.
- Verified system-created links remain protected by service logic.

### Source Traceability Paths Verified

- Quote to source.
- Action item to source through existing source-reference, security, and E2E coverage.
- Note block and raw capture to source through existing source-reference and capture tests.
- Concept and knowledge object to source through graph/source coverage.
- Daily item to source through existing daily tests.
- Forum thread to source through existing forum/security tests.
- Project problem, project application, design decision, playtest finding, and project lens review to source through new project traceability assertions.
- Graph source-reference nodes and search-result open-source paths are supported by frontend source drawer/open-source wiring.

### Relationship Editor Changes

- Project graph now includes user-created manual entity links when the link touches a node visible in the project graph.
- Source-reference lookup now supports project sub-entities: `PROJECT_PROBLEM`, `PROJECT_APPLICATION`, `DESIGN_DECISION`, `PLAYTEST_FINDING`, `PROJECT_LENS_REVIEW`, and `PROJECT_KNOWLEDGE_LINK`.
- Backlink resolution now supports project and project sub-entity targets.
- Frontend open-source lookup now supports project sub-entity source references.
- Source drawer and backlink labels now display `Page unknown` instead of implying a missing source page is an error or invented value.

### Tests Added

- Added `SearchGraphAIIntegrationTest#projectSourceTraceabilityBacklinksAndGraphRemainOwnerScoped`.
- Strengthened `SearchGraphAIIntegrationTest#graphFiltersAndManualEntityLinksAreOwnerScopedAndEditable` with concept graph source-node and cross-user denial assertions.

### Files Created

- None.

### Files Modified

- `README.md`
- `backend/src/main/java/com/bookos/backend/graph/service/GraphService.java`
- `backend/src/main/java/com/bookos/backend/link/service/BacklinkService.java`
- `backend/src/main/java/com/bookos/backend/source/service/SourceReferenceService.java`
- `backend/src/test/java/com/bookos/backend/integration/SearchGraphAIIntegrationTest.java`
- `docs/api-endpoint-inventory.md`
- `docs/current-state.md`
- `docs/data-model-overview.md`
- `frontend/src/components/source/BacklinksSection.vue`
- `frontend/src/components/source/SourceReferenceDrawer.vue`
- `frontend/src/composables/useOpenSource.ts`
- `report.md`

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `rg -n "class .*Graph|class .*EntityLink|class .*Backlink|class .*SourceReference|@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping" backend/src/main/java/com/bookos/backend backend/src/test/java/com/bookos/backend`
- `rg -n "GraphView|SourceReferenceDrawer|useOpenSource|entity-links|backlinks|graph|sourceReference|openSource" frontend/src docs README.md report.md`
- `.\mvnw.cmd "-Dtest=SearchGraphAIIntegrationTest" test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`
- `git ls-files | rg '(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err)$|frontend/dist|backend/target|frontend/node_modules'`

### Verification Result

- Targeted graph/search/AI backend tests: PASS. 7 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 58 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.
- Tracked artifact check: PASS. No tracked `backend.zip`, `.7z`, logs, `.out`, `.err`, `backend/target`, `frontend/dist`, or `frontend/node_modules` paths were found.

### Remaining Graph/Source Risks

- Project sub-entity backlinks can open their source reference, but `BacklinkResponse` does not include `projectId`, so direct routing to exact project sub-workspaces remains limited.
- The graph relationship editor still requires numeric entity IDs; a selector-driven UI would reduce invalid manual relationships.
- Graph visualization remains a lightweight workspace, not an advanced layout or graph analytics tool.
- Backend verification uses H2/test profile, not Docker MySQL.
- Mockito dynamic-agent warnings remain during backend test execution.

### Next Recommended Prompt

Run a browser-focused graph/source smoke test: create a project application and manual relationship from the UI, open it in `/graph/project/:id`, verify source drawer and page-unknown behavior, and decide whether `BacklinkResponse` should include `projectId` for direct project subroute navigation.

## Prompt 9 — AI Provider Strict Acceptance and Secret Safety Review

### Date and Local Time

- 2026-04-30 13:15:42 +02:00

### Current SHA

- Branch: `main`
- SHA: `f71d53ffdf58d9f2c7b8e3429af8605b4b8ad3ae`

### Provider Modes Verified

- `MockAIProvider` remains the default test/local provider through `AI_PROVIDER=mock`.
- `OpenAICompatibleProvider` is optional and selected only through environment-backed configuration.
- Missing OpenAI-compatible key returns a safe unavailable-provider response and does not make external calls.
- OpenAI-compatible provider unit coverage uses a local mocked HTTP server only.
- `AI_ENABLED=false` is documented as hard-off mode.

### Endpoints Verified

- `GET /api/ai/status`
- `POST /api/ai/suggestions/note-summary`
- `POST /api/ai/suggestions/extract-actions`
- `POST /api/ai/suggestions/extract-concepts`
- `POST /api/ai/suggestions/design-lenses`
- `POST /api/ai/suggestions/project-applications`
- `POST /api/ai/suggestions/forum-thread`
- `GET /api/ai/suggestions`
- `PUT /api/ai/suggestions/{id}/accept`
- `PUT /api/ai/suggestions/{id}/reject`
- `PUT /api/ai/suggestions/{id}/edit`

### Safety Rules Verified

- AI suggestions are persisted as `DRAFT`.
- Accepting a suggestion changes only suggestion status/timestamps and does not overwrite source content.
- Editing a suggestion changes only the draft fields and requires the suggestion to still be `DRAFT`.
- Rejecting a suggestion changes only suggestion status/timestamps.
- Source references used as AI input are recorded on the suggestion.
- Cross-user suggestion list/edit/accept access is blocked by `suggestionId + userId` lookups.
- Source material resolution requires owned source reference, note, capture, or accessible book context.
- Provider request input metadata omits secrets and records only provider/task/book/source metadata.

### Secret Handling Result

- Provider status response hides API keys, authorization headers, and raw secret variable names.
- No real external AI call is required or executed by tests.
- Tracked artifact scan found no tracked `.env`, archive, log, `target`, `dist`, or `node_modules` artifact.
- Secret-pattern scan found only the expected placeholder configuration line in `application.yml`: `${OPENAI_COMPATIBLE_API_KEY:}`.

### JSON Validation Result

- Malformed JSON is rejected.
- Invented page numbers are rejected.
- JSON with overwrite targets is rejected.
- JSON `type` is now required to match the requested `AISuggestionType`.
- A schema/runtime mismatch was found and fixed: the database enum only allowed the original three AI suggestion types while the controller exposes six tasks.

### Tests Added

- Added validator coverage for mismatched suggestion type and nested overwrite target rejection.
- Added integration coverage for all six AI generation endpoints creating validated draft suggestions with source references.
- Existing/provider tests cover mocked OpenAI-compatible HTTP behavior, missing-key fail-safe behavior, provider status secret hiding, cross-user suggestion denial, and no-overwrite accept/edit/reject lifecycle.

### Files Created

- `backend/src/main/resources/db/migration/V9__expand_ai_suggestion_types.sql`
- `docs/ai-safety.md`

### Files Modified

- `README.md`
- `backend/src/main/java/com/bookos/backend/ai/service/AISuggestionValidator.java`
- `backend/src/test/java/com/bookos/backend/ai/AIProviderServiceTest.java`
- `backend/src/test/java/com/bookos/backend/integration/SearchGraphAIIntegrationTest.java`
- `docs/database-migrations.md`
- `docs/environment-variables.md`
- `report.md`

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short`
- `rg -n "AIProvider|MockAI|OpenAI|AISuggestion|AIInteraction|/api/ai|ai\\.provider|AI_ENABLED|OPENAI|Json|JSON|status" backend/src/main/java backend/src/test/java frontend/src docs README.md .env.example backend/src/main/resources -S`
- `rg --files backend/src/main/java/com/bookos/backend | rg "(/ai/|/config/|/security/)"`
- `rg --files frontend/src | rg "(RightRail|rightRail|ai|AI|api)"`
- `rg --files docs | rg "(ai|environment|security|current|api)"`
- `.\mvnw.cmd "-Dtest=AIProviderServiceTest,AIProviderConfigurationIntegrationTest,AIProviderSecretStatusIntegrationTest,SearchGraphAIIntegrationTest,SecurityBoundaryIntegrationTest" test`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`
- `git ls-files | rg '(^|/)(backend\\.zip|.*\\.7z|.*\\.log|.*\\.out|.*\\.err|\\.env)$|frontend/dist|backend/target|frontend/node_modules'`
- `rg -n "sk-[A-Za-z0-9_-]{20,}|OPENAI_COMPATIBLE_API_KEY\\s*=\\s*[^\\s#]+|api-key\\s*:\\s*[^\\s#]+" --glob '!backend/target/**' --glob '!frontend/dist/**' --glob '!frontend/node_modules/**' .`

### Verification Result

- Initial targeted AI/security test run exposed a real endpoint/schema bug: `POST /api/ai/suggestions/design-lenses` returned 500 because the database enum did not include all implemented AI suggestion types.
- After `V9__expand_ai_suggestion_types.sql`, targeted AI/security tests: PASS. 19 tests, 0 failures, 0 errors.
- Backend full suite: PASS. 60 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. `npm ci` completed with 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.
- Tracked artifact/environment check: PASS. No tracked archives, logs, `.env`, backend `target`, frontend `dist`, or `node_modules`.

### Remaining AI Risks

- OpenAI-compatible provider has mocked HTTP coverage only; no real provider smoke test was run by design.
- Provider status intentionally reports whether an external provider is configured, but not which key or secret value.
- AI generation still accepts freeform user-selected text; the backend truncates input but does not perform semantic privacy classification.
- Right Rail exposes the draft workflow, but deeper browser assertions for each AI task selector remain limited to the existing E2E smoke path.
- Mockito dynamic-agent warnings remain in backend test output.

### Next Recommended Prompt

Run a browser-focused AI workflow smoke test: configure local MockAIProvider, generate each draft type from an explicit source reference in the Right Rail, confirm the source drawer opens, edit/accept/reject drafts, and verify no source content changes after acceptance.

## Prompt 10 — Public Beta 0.1 Final Release Gate and PO-Ready Report

### Date and Local Time

2026-04-30 13:24:20 +02:00

### Current SHA

- Branch: `main`
- SHA: `f71d53ffdf58d9f2c7b8e3429af8605b4b8ad3ae`
- Working tree state: release documentation changes are local/uncommitted at the end of this gate.

### Executive Summary

BookOS Public Beta 0.1 is functionally ready for a controlled beta release with caveats. The release gate verified backend tests, frontend install/typecheck/build, browser E2E smoke, Docker Compose config, full-stack Compose config, direct health endpoints, repo hygiene, release documentation, AI draft safety, and source-reference documentation.

No P0 release blocker was found. The recommendation is **Release with caveats**, not unconditional release, because broad public exposure still needs final human mobile/tablet QA, real deployment environment smoke, and operator backup/monitoring setup.

### Product Goal

BookOS is a reading record system, book notes library, game design knowledge operating system, source-referenced knowledge graph, structured forum, and project application cockpit. The beta goal is to prove the core loop: reading material becomes notes, captures, quotes, action items, concepts, source references, search/graph context, forum discussion, and project design actions.

### What Is Implemented

- Auth, user roles, JWT access, BCrypt password hashing, current-user flow.
- Book library, user library, reading progress, status, rating, book detail cockpit.
- Notes, note blocks, Quick Capture, deterministic emoji/page/tag/`[[Concept]]` parser, capture inbox, and conversion.
- Quotes, action items, concepts, knowledge objects, source references, entity links, backlinks, search, and graph.
- Daily sentence/design prompt MVP, structured forum MVP, Game Project Mode MVP, analytics/review/mastery MVP, import/export MVP.
- Draft-only AI suggestions using MockAIProvider by default and optional OpenAI-compatible provider configuration.
- Flyway migrations through `V9`, Dockerfiles, local MySQL compose, full-stack compose, CI workflows, E2E smoke tests, and beta release docs.

### What Is Verified

- Backend full test suite passes: 60 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install passes: 147 packages, 0 vulnerabilities.
- Frontend typecheck and production build pass.
- Playwright E2E passes: 2 browser tests.
- `docker compose config` passes.
- `docker compose -f docker-compose.full.yml config` passes with a non-secret validation `JWT_SECRET`.
- `GET /api/health` returns `UP` service data.
- `GET /actuator/health` returns `UP`.
- No tracked `.env`, archives, logs, `backend/target`, `frontend/dist`, or `frontend/node_modules`.
- Secret-pattern scan found no high-confidence committed secrets.

### What Is Not Verified

- GitHub-hosted CI was not remotely executed in this prompt; local-equivalent commands passed and workflows exist.
- Full Docker image build and full-stack container runtime smoke were not rerun in this prompt; release docs preserve the previous successful full-stack Docker health smoke.
- Real external OpenAI-compatible provider was not called by design.
- Full human responsive/mobile/tablet QA is not complete.
- Production-like backup restore on a real beta MySQL volume was not executed.

### Test Results

| Check | Command | Result |
| --- | --- | --- |
| Backend tests | `.\mvnw.cmd test` | PASS, 60 tests |
| Frontend install | `npm ci` | PASS, 147 packages, 0 vulnerabilities |
| Frontend typecheck | `npm run typecheck` | PASS |
| Frontend build | `npm run build` | PASS |
| Playwright browser install | `npx playwright install chromium` | PASS |
| E2E smoke | `npm run e2e` | PASS, 2 tests |
| Local MySQL compose config | `docker compose config` | PASS |
| Full-stack compose config | `docker compose -f docker-compose.full.yml config` | PASS with validation `JWT_SECRET` |
| API health | `GET /api/health` on test-profile backend | PASS |
| Actuator health | `GET /actuator/health` on test-profile backend | PASS |
| Repo hygiene | `git ls-files` artifact/env scan | PASS |
| Secret scan | token regex scan excluding generated folders | PASS |

### E2E Results

- `admin-ontology.spec.ts`: PASS. Admin can dry-run default ontology import without production secrets.
- `mvp-core-loop.spec.ts`: PASS. Browser MVP loop covers registration/login, dashboard, book/library, note/capture/parser/conversion, quote/action/source, concept review, search, graph, MockAIProvider draft flow, project mode, forum, import/export route access, and logout.

### CI Status

- `.github/workflows/ci.yml` exists for backend and frontend verification.
- `.github/workflows/e2e.yml` exists for optional browser smoke.
- CI requires no production secrets.
- Local-equivalent commands passed in this gate.

### Docker Status

- `docker-compose.yml` renders valid local MySQL config.
- `docker-compose.full.yml` renders valid MySQL/backend/frontend config when `JWT_SECRET` is supplied.
- `backend/Dockerfile` and `frontend/Dockerfile` are present.
- Full-stack Compose healthchecks are configured.
- No secrets are baked into images by docs/config.

### Security Status

- Password hashing, JWT external secret, CORS configuration, Markdown sanitization, admin ontology protection, user-scoped search/graph/source/AI/project behavior, and owner-scoped tests are documented.
- `docs/security-checklist.md` was added for PO/operator release review.
- No P0 data leak was found during this release gate.

### AI Safety Status

- MockAIProvider remains default.
- External OpenAI-compatible provider is optional and environment-controlled.
- App works without external AI key.
- Provider status hides secrets.
- Suggestions are draft-only.
- Accept/edit/reject does not overwrite source content automatically.
- No real external AI call was made.

### Source Reference Status

- Source reference rules are documented as release invariants.
- Unknown page numbers remain `null`; page numbers must not be invented.
- Source references are preserved across core derived objects where implemented.
- Source-opening and source drawer behavior are covered by backend/frontend paths and E2E smoke at the core-loop level.

### P0 Blockers

None found.

### P1 Issues

- Full human mobile/tablet responsive QA remains before broad public exposure.
- Real target-environment deployment smoke, TLS/proxy validation, and backup schedule verification remain operator tasks.
- E2E is available and passing locally but is not yet a mandatory CI gate.

### P2 Issues

- Graph workspace is real-data-backed but still lightweight for large graphs.
- Review/mastery is MVP-level and not advanced spaced repetition.
- Forum moderation is basic and not realtime.
- Import conflict resolution UX is limited.
- External AI provider has mocked HTTP coverage only; no real-provider smoke is included by design.
- Mockito dynamic-agent warnings appear in backend test output but do not fail tests.

### Module Readiness Table

| Module | Status | Score | Notes |
| --- | --- | ---: | --- |
| Auth | PASS | 5 | Register/login/me, JWT, roles, tests. |
| Books | PASS | 5 | CRUD and book detail foundations are usable. |
| User library | PASS | 5 | Status, progress, rating, and library flows. |
| Notes | PASS | 4 | Usable notes and source-linked detail; editor remains MVP-level. |
| Captures | PASS | 5 | Quick Capture, inbox, parser metadata, conversions. |
| Parser | PASS | 5 | Deterministic emoji/page/tag/concept parser with tests. |
| Quotes | PASS | 5 | CRUD, archive, detail, source-open. |
| Action items | PASS | 5 | CRUD, complete/reopen, archive, source-open. |
| Source references | PASS | 5 | Core invariant implemented and documented. |
| Entity links | PASS | 4 | Safe relationship model; editor UX remains technical. |
| Backlinks | PASS | 4 | Key pages show related links; breadth can improve. |
| Concepts | PASS | 4 | Review/list/detail/source workflows. |
| Knowledge objects | PASS | 4 | MVP workspace and ontology import support. |
| Daily | PASS | 4 | Source-backed/template-labeled daily MVP. |
| Forum | PASS | 4 | Structured MVP with source context and basic moderation. |
| Search | PASS | 4 | Cross-module search with ownership scoping. |
| Graph | PASS | 4 | Real-data graph and filters; not advanced analysis. |
| Projects | PASS | 4 | Project Mode MVP is usable and source-linked. |
| Analytics | PASS | 4 | Real-data analytics MVP with honest empty states. |
| Review | PASS | 4 | Source-backed review sessions are usable. |
| Mastery | PASS | 3 | Lightweight MVP scoring and updates. |
| Import/export | PASS | 4 | User-owned JSON/Markdown/CSV MVP; conflict UX limited. |
| AI | PASS | 4 | Draft-only mock/default plus optional external provider config. |
| Admin ontology | PASS | 4 | Admin-only dry-run/import workflow and E2E smoke. |

### MVP Readiness Score

88 / 100

Rationale: the MVP product loop is implemented, tested, and browser-smoked. Remaining gaps are mostly operational maturity, responsive human QA, and depth of advanced modules.

### Public Beta Readiness Score

84 / 100

Rationale: appropriate for controlled public beta with clear caveats. Not ready for unsupervised broad production rollout until deployment/backup/monitoring/mobile QA are completed in the target environment.

### Release Recommendation

Release with caveats.

Do not tag yet unless the release owner explicitly approves after reviewing the updated release docs and `report.md`.

### Next Milestone Recommendation

Run a final human QA pass on desktop/tablet/mobile against the release checklist, then execute a real target-environment deployment smoke with MySQL backup/restore verification. If clean, create `v0.1.0-beta`.

## Prompt 1 — Product Usability Audit: Bloat, Navigation, Use Case Gaps

### Date and Local Time

2026-04-30 14:12:39 +02:00

### Current SHA

- Branch: `main`
- Local SHA: `3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`
- `origin/main` SHA after fetch: `3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Pages Audited

- Sidebar navigation
- Dashboard
- Library
- Book Detail
- Capture Inbox
- Notes
- Quotes
- Action Items
- Concepts
- Knowledge Objects
- Daily
- Projects
- Project Detail
- Analytics
- Review
- Mastery
- Import/Export
- Graph
- Forum
- AI Right Rail
- Admin Ontology
- Search

### Biggest Usability Problems

- The sidebar is module-first and exposes too many implemented areas as equal choices.
- Dashboard gives useful information but does not yet force a single "next best action" for a new user.
- Capture Inbox is central but named like passive storage instead of an active processing workflow.
- Concepts, Knowledge, Graph, Analytics, Mastery, Forum, and AI can overwhelm new users before they experience the core reading loop.
- Projects is valuable, but the first practical game-design use case needs clearer guidance: create project, apply one source, define one problem, make one decision.

### Use Case Gaps

- First 10-minute onboarding is partial: users can complete the workflow, but the product does not yet guide them step-by-step.
- Capture-to-conversion is implemented, but the UI should frame it as the central bridge between reading and knowledge.
- Source reopening exists, but should be more visually central on every source-backed detail page.
- Review and Mastery are split; Review should be the action, Mastery should be the result.
- Graph and Analytics are useful after data exists, but should not compete with first-day work.

### Navigation Simplification Proposal

Primary:

- Dashboard
- Library
- Capture
- Notes
- Projects
- Review

Secondary:

- Quotes
- Actions
- Concepts
- Knowledge
- Daily
- Forum

Advanced / More:

- Knowledge Graph
- Analytics
- Learning Progress
- Import / Export
- Ontology Import
- AI Settings

Recommended label changes:

- `Capture Inbox` -> `Process Captures`
- `Action Items` -> `Actions`
- `Knowledge Objects` -> `Knowledge`
- `Mastery` -> `Learning Progress`
- `Graph` -> `Knowledge Graph`

### Files Created

- `docs/product-usability-audit.md`
- `docs/use-case-gap-analysis.md`
- `docs/navigation-simplification-plan.md`

### Files Modified

- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `git ls-files`
- `Get-Content frontend/src/router/index.ts`
- `Get-Content frontend/src/components/Sidebar.vue`
- `Get-Content frontend/src/views/DashboardView.vue`
- `Get-Content frontend/src/layouts/AppLayout.vue`
- `Get-Content frontend/src/components/TopNav.vue`
- `rg -n "<template>|const .*nav|name:|path:|AppSectionHeader|title|empty|loading|error|Create|New|Quick|Capture|Search|More|AI|Mock" frontend/src/views frontend/src/components -S`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`

### Verification Result

- Backend tests: PASS. 60 tests, 0 failures, 0 errors, 0 skipped.
- Frontend install: PASS. `npm ci` completed with 147 packages and 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- No backend features were added.
- No navigation or UI behavior was changed.
- No features were removed or hidden.
- No fake data was introduced.

### Remaining Risks

- The audit identifies product changes but does not implement them yet.
- `docs/current-state.md` still records an older review SHA; it was not updated because this prompt requested product usability deliverables only.
- The next implementation pass should change labels/grouping carefully without breaking route names or E2E selectors.
- Actual cognitive-load reduction requires browser review after navigation grouping is implemented.

### Next Recommended Prompt

Implement Phase 1 of the BookOS usability simplification plan: group sidebar navigation into Primary, Secondary, and More; rename visible labels only; keep all existing routes reachable; add no backend features; update E2E selectors only if label changes require it; run backend tests, frontend typecheck/build, and E2E.

## Prompt 2 - Usability Simplification Phase 1: Sidebar Grouping and Labels

### Date and Local Time

2026-04-30 14:16:06 +02:00

### Current SHA

- Branch: `main`
- SHA: `3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Changes Implemented

- Grouped the sidebar into `Primary`, `Secondary`, and `More` sections.
- Added `Dashboard` as a first-class Primary navigation item while preserving the existing BookOS brand link to Dashboard.
- Renamed visible navigation labels:
  - `Captures` -> `Capture`
  - `Action Items` -> `Actions`
  - `Mastery` -> `Learning Progress`
  - `Graph` -> `Knowledge Graph`
  - `Admin` -> `Ontology Import`
- Kept all existing routes unchanged and reachable.
- Kept `Search` as a command-style sidebar action that opens the existing global search dialog.
- Replaced disabled future-feature nav buttons with a clear `Planned Later` rail card for Lenses, Diagnostics, and Exercises, avoiding fake completion claims.
- Preserved admin-only visibility for Ontology Import.

### Files Modified

- `frontend/src/components/Sidebar.vue`
- `report.md`

### Backend Changes

None.

### Commands Run

- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git status --short --branch`
- `Get-Content frontend/src/components/Sidebar.vue`
- `rg -n "Action Items|Captures|Knowledge Objects|Mastery|Graph|Admin|Import / Export|Analytics|Quotes|Concepts|Forum|Review|Projects|Library|Notes|Search|sidebar|navigation" frontend/e2e frontend/src -S`
- `.\mvnw.cmd test`
- `npm ci`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`

### Verification Result

- Backend tests: PASS. 60 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. 147 packages, 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 2 tests passed.

### Remaining Risks

- This pass reduces sidebar overload but does not yet implement dashboard next-best-action onboarding.
- Mobile navigation still uses the existing drawer rather than a dedicated bottom-navigation model.
- Page titles and route metadata still use some older labels, such as `Action Items` and `Mastery`; only sidebar visible labels changed in this phase to avoid broader route/title churn.

### Next Recommended Prompt

Implement Phase 2 of the usability simplification plan: add a state-based Dashboard "Next best action" panel for empty account, has book/no capture, has unprocessed captures, and has source-backed knowledge states. Keep backend unchanged unless an existing API adapter is missing, preserve all modules, and run backend tests, frontend typecheck/build, and E2E.

## Prompt 2 — First-Run Onboarding Wizard and User Intent Selection

### Date and Local Time

2026-04-30 14:31:12 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Onboarding Behavior Added

- New users register with `onboardingCompleted=false` and are routed to `/onboarding` before entering the main app workflow.
- Existing migrated user profiles default to `onboardingCompleted=true`, so existing users are not forcibly interrupted.
- The onboarding wizard explains BookOS in one sentence, asks for primary use case, asks for starting mode, shows the first recommended object/workflow, then shows the next three actions.
- Users can skip onboarding and return to Dashboard.
- Completed users can restart onboarding from Dashboard.
- Reader Mode and Game Designer Mode completion route users into the first practical workflow instead of leaving them on a module-heavy dashboard.

### Backend Changes

- Added onboarding preference fields to `UserProfile`: `onboardingCompleted`, `primaryUseCase`, `startingMode`, and `preferredDashboardMode`.
- Added `OnboardingPreferenceRequest`.
- Extended `CurrentUserResponse` and `UserProfileResponse` with onboarding preferences.
- Added `PUT /api/users/me/onboarding`.
- Added Flyway migration `V10__add_user_onboarding_preferences.sql`.
- Set demo seed users to completed onboarding to avoid disrupting seeded local accounts.
- Added backend integration coverage for new-user default onboarding state and persisted onboarding preference update.

### Frontend Changes

- Added `/onboarding` route and route guard for first-run users.
- Added `OnboardingView.vue` with the five-step wizard.
- Added onboarding API/store support in `frontend/src/api/auth.ts` and `frontend/src/stores/auth.ts`.
- Extended frontend auth types with onboarding preference fields.
- Added Dashboard preferred-workflow card and restart-onboarding action.
- Updated the existing E2E core loop to skip onboarding for its fresh test user.
- Added onboarding E2E coverage for skip, Reader Mode completion, Game Designer Mode completion, and persisted preferences.

### Tests Added

- Backend: `AuthFlowIntegrationTest.userCanSaveOnboardingPreferences`.
- Frontend E2E: `frontend/e2e/onboarding.spec.ts`.
- Endpoint contract: added `PUT /api/users/me/onboarding`.

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `rg "UserProfile|onboarding|CurrentUserResponse|UserProfileResponse" backend frontend docs -n`
- `npm ci`
- `.\mvnw.cmd test`
- `npm run typecheck`
- `npm run build`
- `npm run e2e`

### Verification Result

- Backend tests: PASS after fixing the V10 migration to use H2-compatible separate `ALTER TABLE` statements and updating endpoint contract coverage. Final result: 61 tests, 0 failures, 0 errors, 0 skipped.
- Frontend clean install: PASS. 147 packages, 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS after tightening onboarding test selectors. Final result: 5 tests passed.

### Remaining Risks

- The wizard routes users into the relevant creation workflow; it does not create the first book/project/note inline inside the wizard.
- Mode-specific Dashboard guidance is intentionally lightweight; deeper state-based next-best-action logic is still a follow-up.
- Existing users are protected by migration default `true`, but any manually created profile row after migration that bypasses application registration must set onboarding state intentionally.

### Next Recommended Prompt

Implement a state-based Dashboard "Next best action" panel that adapts to empty account, has book/no capture, has unprocessed captures, has source-backed concepts/actions, and has project application states. Preserve existing modules, do not add fake data, and run backend tests, frontend typecheck/build, and E2E.

## Prompt 3 - Dashboard Next Best Action Panel

### Date and Local Time

2026-04-30 14:37:10 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Dashboard Behavior Added

- Added a real-data "Next best action" panel to Dashboard.
- The panel adapts to empty account, has library/no source-backed knowledge, unprocessed capture inbox, source-backed quote/concept/action states, and project application states.
- The panel uses existing frontend API adapters only and does not add backend endpoints.
- The panel includes loading, backend-unavailable, routeable primary action, secondary action, real signal counts, and a short checklist.
- Project application signals are sampled from the first six active projects to keep Dashboard loading bounded.

### Backend Changes

None.

### Frontend Changes

- Extended `DashboardView.vue` to load capture inbox, quotes, action items, concepts, projects, and project applications.
- Added computed next-action state selection based on real user data.
- Added responsive styling for the next-action card, metrics, checklist, and action row.

### Files Modified

- `frontend/src/views/DashboardView.vue`
- `report.md`

### Commands Run

- `git status --short --branch`
- `rg --files frontend/src/api frontend/src/views frontend/src/components`
- `Get-Content frontend/src/views/DashboardView.vue`
- `Get-Content frontend/src/api/captures.ts`
- `Get-Content frontend/src/api/quotes.ts`
- `Get-Content frontend/src/api/actionItems.ts`
- `Get-Content frontend/src/api/knowledge.ts`
- `Get-Content frontend/src/api/projects.ts`
- `npm run typecheck`
- `.\mvnw.cmd test`
- `npm run build`
- `npm run e2e`
- `git diff --check`

### Verification Result

- Backend tests: PASS. 61 tests, 0 failures, 0 errors, 0 skipped.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 5 tests passed.
- Diff whitespace check: PASS. Only existing line-ending warnings were reported.

### Remaining Risks

- The Dashboard panel routes users into workflows; it does not perform inline capture, conversion, or project application creation.
- Project application state uses a bounded sample of active projects, so very large accounts could have applications outside the sampled set.
- Visual browser QA was covered indirectly through Playwright route smoke tests, not through screenshot comparison.

### Next Recommended Prompt

Add focused Dashboard E2E coverage for each next-best-action state using API-seeded fixtures, then run browser visual QA at desktop, tablet, and mobile widths.

## Prompt 3 - Use Case Templates and Hands-On Scenario Library

### Date and Local Time

2026-04-30 14:49:04 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Use Cases Added

- Track a book from start to finish.
- Capture an idea while reading.
- Turn a capture into a quote.
- Turn a capture into an action item.
- Turn a concept marker into a reviewed concept.
- Open source from a quote or action.
- Apply a quote to a game project.
- Run a design lens review on a project.
- Create a playtest finding from project work.
- Use Daily Prompt to create a project task.
- Start a forum discussion from a quote or concept.
- Search and rediscover old knowledge.
- Use graph to inspect knowledge connections.
- Export your reading knowledge.
- Use Mock AI safely as a draft helper.

### Routes Added

- `/use-cases`
- `/use-cases/:slug`

### Components Added

- `frontend/src/components/use-case/UseCaseCard.vue`
- `frontend/src/components/use-case/UseCaseDetail.vue`
- `frontend/src/components/use-case/UseCaseStepList.vue`
- `frontend/src/components/use-case/UseCaseActionButton.vue`
- `frontend/src/components/use-case/UseCaseProgress.vue`
- `frontend/src/components/use-case/UseCaseRelatedObjects.vue`

### Docs Created

- `docs/hands-on-use-cases.md`

### Tests Added

- `frontend/e2e/use-cases.spec.ts`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `npm run typecheck`
- `npm run build`
- `.\mvnw.cmd test`
- `npm run e2e`
- `git diff --check`

### Verification Result

- Backend tests: PASS. 61 tests, 0 failures, 0 errors, 0 skipped.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 6 tests passed, including the use-case route smoke.
- Diff whitespace check: PASS. Only existing line-ending warnings were reported.

### Remaining Risks

- Use cases are instructional templates; they do not track persisted per-user completion state.
- Some use-case steps route users to the relevant workspace when the exact action requires a specific existing object, such as a selected project or quote.
- The scenario library is static safe guidance; it does not yet adapt to each user's actual data state.

### Next Recommended Prompt

Add contextual "Start this use case" entry points from Dashboard, empty states, Book Detail, Quote Detail, Project Detail, Graph, and Import/Export so users see the right hands-on scenario exactly where they get stuck.

## Prompt 4 - Contextual Use Case Entry Points

### Date and Local Time

2026-04-30 14:56:48 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Entry Points Added

- Dashboard now surfaces a compact "Learn BookOS through one concrete workflow" panel with first-day workflows.
- Book Detail now points users toward capture, quote, action item, and concept-review workflows.
- Quote Detail now points users toward open-source, apply-to-project, and forum-discussion workflows.
- Project Detail now points users toward project application, lens review, and playtest finding workflows.
- Graph now points users toward graph exploration, concept review, and search rediscovery workflows.
- Import/Export now points users toward export, rediscovery, and Mock AI safety workflows.
- Graph empty state now offers explicit next actions instead of leaving users at a dead end.

### Files Created

- `frontend/src/components/use-case/UseCaseSuggestionPanel.vue`

### Files Modified

- `frontend/src/views/DashboardView.vue`
- `frontend/src/views/BookDetailView.vue`
- `frontend/src/views/QuoteDetailView.vue`
- `frontend/src/views/ProjectDetailView.vue`
- `frontend/src/views/GraphView.vue`
- `frontend/src/views/ImportExportView.vue`
- `frontend/e2e/use-cases.spec.ts`
- `report.md`

### Tests Added or Updated

- Updated `frontend/e2e/use-cases.spec.ts` to verify dashboard contextual use-case entry, graph guidance, and import/export guidance.

### Commands Run

- `git fetch origin main`
- `git rev-parse HEAD`
- `git status --short --branch`
- `npm run typecheck`
- `npm run build`
- `.\mvnw.cmd test`
- `npm run e2e`
- `git diff --check`

### Verification Result

- Backend tests: PASS. 61 tests, 0 failures, 0 errors, 0 skipped.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright E2E: PASS. 6 tests passed.
- Diff whitespace check: PASS. Only line-ending conversion warnings were reported.

### Remaining Risks

- Contextual panels are static route guidance, not personalized recommendations based on the user's current data.
- Not every secondary/detail page has contextual use-case guidance yet.
- Use-case completion remains instructional only; persisted progress tracking is still not implemented.

### Next Recommended Prompt

Implement task-first page simplification: reorganize sidebar groups, collapse advanced modules under More, and add first-action empty states across Library, Notes, Capture Inbox, Projects, Review, and Search without removing any existing feature.

## Prompt 4 — Simplified Dashboard: Today, Continue, Capture, Apply

### Date and Local Time

2026-04-30 15:09:14 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Dashboard Problems Fixed

- Replaced the module-heavy dashboard with a task-first command center focused on today, reading, capture, inbox processing, project application, and review.
- Made the simplest next step explicit for new users, readers, game designers, researchers, community users, and advanced users.
- De-emphasized Graph, Import/Export, Admin, and Mock AI on the dashboard unless the user selected Advanced Mode.
- Added real dashboard Quick Capture using the existing capture API instead of static guidance.
- Added honest loading, error, and empty states for missing daily items, no current book, empty inbox, no source-backed knowledge, no project, and no review data.

### Sections Implemented

- Today: real daily quote/prompt when available, honest empty/template states otherwise, and a clear CTA.
- Continue Reading: real current-reading books with progress, status, and Open Book/Capture Note actions.
- Quick Capture: real save flow with current book selector, multiline input, parser hint examples, and parsed result preview.
- Inbox: real unprocessed captures with parsed type, source book, page badge, tags, concepts, and suggested conversion action.
- Apply Knowledge: real recent quote, concept, or knowledge object with a project-oriented CTA.
- Project Focus: real active project summary with problems and applications counts.
- Learning Loop: real review session or weak mastery item when available, with review CTA.
- Advanced Mode Tools: graph, import/export, and Mock AI shortcuts only for Advanced Mode.

### Personalization Behavior

- Dashboard reads `preferredDashboardMode` or `startingMode` from the authenticated user.
- Reader Mode emphasizes adding a book, continuing reading, and quick capture.
- Game Designer Mode emphasizes applying source-backed knowledge to projects.
- Researcher Mode emphasizes concepts, knowledge objects, and review.
- Advanced Mode exposes graph, import/export, and Mock AI as dashboard shortcuts.

### Tests Added

- Added a Playwright dashboard smoke test that verifies empty-state guidance, creates a real book through the API, confirms reading progress, saves a real capture, checks parsed metadata, creates a real project, and verifies the Project Focus section.
- Updated existing E2E dashboard expectations to match the new task-first copy.

### Files Created

- `frontend/e2e/dashboard.spec.ts`

### Files Modified

- `frontend/src/views/DashboardView.vue`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `frontend/e2e/onboarding.spec.ts`
- `frontend/e2e/use-cases.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run e2e`
- `git diff --check`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Playwright E2E: PASS, 7 tests passed.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- Dashboard Quick Capture previews parsed metadata after save; it does not yet offer a separate pre-save parser preview.
- Apply Knowledge routes to existing source/project destinations; it does not open an Apply to Project modal directly from the dashboard.
- Project Focus summarizes the selected active project only.
- Advanced modules remain available in navigation; this checkpoint only de-emphasizes them on the dashboard.

### Next Recommended Prompt

Implement task-first simplification for core workspace pages: Library, Capture Inbox, Notes, Projects, and Review should each show one primary action, honest empty states, and contextual use-case guidance without removing existing features.

## Prompt 5 — Task-First Core Workspace Simplification

### Date and Local Time

2026-04-30 15:21:44 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Workspace Problems Fixed

- Library, Capture Inbox, Notes, Projects, and Review now start with a clear "next action" instead of dropping users into module-heavy screens.
- Added honest count summaries based on real loaded records; no fake counts, fake page numbers, or placeholder production data were introduced.
- Added contextual use-case guidance to the five core workspaces so users can learn the practical workflow where they are likely to get stuck.
- Preserved existing feature surfaces, filters, forms, tables, conversion menus, and routes.

### Pages Simplified

- Library: emphasizes adding one real book, continuing the active book, and using the library as the beginning of the reading loop.
- Capture Inbox: emphasizes processing one capture into a note, quote, action item, or reviewed concept.
- Notes: emphasizes choosing a source book first, writing one source-backed markdown note, and reviewing parsed blocks.
- Projects: emphasizes creating one real design project and applying source-backed knowledge to it.
- Review: emphasizes continuing an open review or creating one small source-backed review session.

### Tests Added

- Added `frontend/e2e/task-first-workspaces.spec.ts` to verify the five core workspaces present one obvious first action for a new user session.
- The test uses real auth/onboarding APIs and does not seed fake production data.

### Files Created

- `frontend/e2e/task-first-workspaces.spec.ts`

### Files Modified

- `frontend/src/style.css`
- `frontend/src/views/MyLibraryView.vue`
- `frontend/src/views/CaptureInboxView.vue`
- `frontend/src/views/NotesView.vue`
- `frontend/src/views/ProjectsView.vue`
- `frontend/src/views/ReviewView.vue`
- `frontend/e2e/task-first-workspaces.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run e2e`
- `git diff --check`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Playwright E2E: PASS, 8 tests passed.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- The workspace next-action panels are rule-based from currently loaded records, not a fully personalized recommendation engine.
- Notes and Review "scroll to form" actions use in-page navigation; they do not create records automatically.
- Capture Inbox promotes concept review before conversion when concepts are present, but deeper concept workflows still depend on the existing review dialog.

### Next Recommended Prompt

Perform a focused UX pass on detail pages: Book Detail, Note Detail, Quote Detail, Action Item Detail, Concept Detail, and Project Detail should each expose one primary next action, source/backlink visibility, and a clear path back to the core reading-to-project loop.

## Prompt 6 — Detail Page Primary Action and Source Loop UX Pass (Canonical Append)

### Date and Local Time

2026-04-30 15:47:16 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Summary

- Added `DetailNextStepCard` and wired it into Book, Note, Quote, Action Item, Concept, and Project detail pages.
- Preserved existing routes, source actions, backlink sections, and advanced workflows.
- Added Project Detail source/backlink visibility via the shared `BacklinksSection`.
- Updated the MVP browser smoke test to assert the new detail-page next-action affordances.
- Initial E2E run found a duplicate valid `Apply to Project` button on Quote Detail; the test was repaired to target the workflow card specifically.

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Playwright E2E: PASS, 15 tests passed.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- The next-step logic is intentionally deterministic and may need later personalization.
- Knowledge Object Detail was not included in this focused pass.
- This canonical section is appended at the end because an earlier detailed Prompt 6 entry was inserted above the previous Prompt 5 block in an already non-linear report tail.

### Next Recommended Prompt

Audit the full first-day user journey with a real browser: new user onboarding, first book, first capture, first conversion, first project application, and return-to-dashboard guidance. Remove any remaining duplicate or competing primary CTAs found during that hands-on walkthrough.

## Prompt 7 - First-Day Quick Capture Workflow Audit and Duplicate CTA Cleanup (Canonical Append)

### Date and Local Time

2026-04-30 16:09:19 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Summary

- Audited the first-day Quick Capture loop with focused browser smoke coverage.
- Removed a duplicate CTA pattern where the same just-saved capture appeared in both the post-save action panel and Recent Note Blocks.
- Preserved the post-save panel as the single immediate conversion path for a newly saved capture.
- Updated E2E coverage so the browser flow converts through the post-save action region and asserts the action CTA is not duplicated.
- No backend features were added, no fake capture data is saved by examples, and no page numbers are invented.

### Files Modified

- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/e2e/capture-guide.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd backend; .\mvnw.cmd test`
- `git diff --check`
- `cd frontend; npm run build`
- `cd frontend; npx playwright test e2e/capture-guide.spec.ts e2e/mvp-core-loop.spec.ts --workers=1`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Focused Playwright E2E: PASS, 2 tests passed serially.
- Diff whitespace check: PASS with line-ending warnings only.

### Report Note

- A detailed Prompt 7 entry also exists earlier in this non-linear `report.md` history because the same recommended prompt text appeared more than once. This canonical append keeps the latest checkpoint evidence at the actual report tail without deleting prior content.

### Remaining Risks

- Running Playwright specs that each start backend web servers in parallel can race Maven/Spring Boot startup. The focused Quick Capture specs are stable with `--workers=1`.
- The full Playwright suite was not rerun in this checkpoint after the focused duplicate-CTA cleanup.
- The post-save panel currently remains the canonical immediate conversion path; later UX work should decide whether it should auto-dismiss after conversion or navigation.

### Next Recommended Prompt

Run the full Playwright suite serially with a single shared backend/frontend webServer strategy, then document and repair any remaining cross-spec instability before changing product UI further.

## Prompt 7 - First-Day Quick Capture Workflow Audit and Duplicate CTA Cleanup (Canonical Append)

### Date and Local Time

2026-04-30 16:09:19 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Summary

- Audited the first-day Quick Capture loop with focused browser smoke coverage.
- Removed a duplicate CTA pattern where the same just-saved capture appeared in both the post-save action panel and Recent Note Blocks.
- Preserved the post-save panel as the single immediate conversion path for a newly saved capture.
- Updated E2E coverage so the browser flow converts through the post-save action region and asserts the action CTA is not duplicated.
- No backend features were added, no fake capture data is saved by examples, and no page numbers are invented.

### Files Modified

- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/e2e/capture-guide.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd backend; .\mvnw.cmd test`
- `git diff --check`
- `cd frontend; npm run build`
- `cd frontend; npx playwright test e2e/capture-guide.spec.ts e2e/mvp-core-loop.spec.ts --workers=1`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Focused Playwright E2E: PASS, 2 tests passed serially.
- Diff whitespace check: PASS with line-ending warnings only.

### Report Note

- A detailed Prompt 7 entry also exists earlier in this non-linear `report.md` history because the same recommended prompt text appeared more than once. This canonical append keeps the latest checkpoint evidence at the actual report tail without deleting prior content.

### Remaining Risks

- Running Playwright specs that each start backend web servers in parallel can race Maven/Spring Boot startup. The focused Quick Capture specs are stable with `--workers=1`.
- The full Playwright suite was not rerun in this checkpoint after the focused duplicate-CTA cleanup.
- The post-save panel currently remains the canonical immediate conversion path; later UX work should decide whether it should auto-dismiss after conversion or navigation.

### Next Recommended Prompt

Run the full Playwright suite serially with a single shared backend/frontend webServer strategy, then document and repair any remaining cross-spec instability before changing product UI further.

## Prompt 7 - First-Day Quick Capture Workflow Audit and Duplicate CTA Cleanup

### Date and Local Time

2026-04-30 16:09:19 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Scope

- Audited the first-day Quick Capture loop in the browser-focused E2E path: create/open a book, use guided capture, save a parsed capture, convert to quote, and continue through the broader MVP route smoke.
- Verified the Capture Guide flow still teaches examples without saving them as real user data.
- Kept the work limited to UX cleanup and test alignment. No backend feature scope was added.

### Browser Flows Verified

- Quick Capture guide examples render and insert into the input without saving fake records automatically.
- Beginner/guided capture produces live parser metadata for type, page, tags, concepts, and source context.
- Saved capture exposes exactly one immediate post-save conversion action path.
- MVP core-loop route smoke still reaches the Book Detail capture workflow and converts the saved quote capture through real APIs.

### Bug Found and Fixed

- The just-saved capture appeared both in the post-save next-step panel and in Recent Note Blocks, creating duplicate conversion CTAs for the same object.
- `BookCaptureSection` now hides the active just-saved capture from the recent block list while the post-save action region is visible.
- The E2E flow now targets the post-save action region for immediate conversion and asserts the action CTA is not duplicated.

### Files Modified

- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/e2e/capture-guide.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd backend; .\mvnw.cmd test`
- `git diff --check`
- `cd frontend; npm run build`
- `cd frontend; npx playwright test e2e/capture-guide.spec.ts e2e/mvp-core-loop.spec.ts --workers=1`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Focused Playwright E2E: PASS, 2 tests passed serially.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- Running multiple Playwright specs that each start backend web servers in parallel can race Maven/Spring Boot startup. The focused Quick Capture specs are stable with `--workers=1`.
- The full Playwright suite was not rerun in this checkpoint after the focused duplicate-CTA cleanup.
- The post-save panel currently remains the canonical immediate conversion path; later UX work should decide whether it should auto-dismiss after conversion or navigation.

### Next Recommended Prompt

Run the full Playwright suite serially with a single shared backend/frontend webServer strategy, then document and repair any remaining cross-spec instability before changing product UI further.

## Prompt 6 — Guided Quick Capture and Parser Learning Flow

### Date and Local Time

2026-04-30 16:04:15 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Capture Guide Changes

- Added an in-app Capture Guide panel on Book Detail Quick Capture with safe original examples for quote, action item, idea, concept, project idea, question, tags, concept links, and Chinese page markers.
- Added one-click example insertion for quote, action, concept, and project idea examples. Example insertion only populates the input and does not save records.
- Added a beginner mode structured capture form for type, page/location, content, tags, and concepts, with generated raw capture text that users can inspect before saving.
- Added a post-save next-step region with Convert to Note, Convert to Quote, Convert to Action, Review Concept, and Open Inbox actions.
- Added a Dashboard warning when no book is selected so users understand source references require book context.

### Parser Preview Behavior

- Live preview now uses the current raw or structured capture text and shows parsed type, parsed page, tags, concepts, and source reference preview.
- Unknown pages are explicitly shown as page unknown and remain null.
- Malformed page markers such as `p.xx` produce a visible warning instead of silently implying a source page.
- Chinese page markers such as `第80页` are previewed and verified through the browser smoke test.

### Beginner Mode Behavior

- Structured capture mode generates equivalent raw syntax with the selected emoji marker, normalized numeric page shorthand where appropriate, tags, and `[[Concept]]` links.
- Users can copy the generated syntax back to raw mode without saving.
- Submit still uses the backend capture and parser flow, preserving source references through the existing API.

### Tests Added

- Added `frontend/e2e/capture-guide.spec.ts`.
- The smoke test verifies no-book guidance, example insertion without save, malformed page warnings, Chinese page marker parsing, structured capture generation, save feedback, post-save next-step actions, and one saved capture in the inbox.

### Files Created

- `docs/quick-capture-guide.md`
- `frontend/e2e/capture-guide.spec.ts`

### Files Modified

- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/src/views/DashboardView.vue`
- `report.md`

### Commands Run

- `npx --version`
- `git fetch origin main`
- `git rev-parse --show-toplevel`
- `git status --short --branch`
- `cd frontend; npx playwright test e2e/capture-guide.spec.ts`
- `cd frontend; npm run typecheck`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run build`
- `git diff --check`

### Verification Result

- Focused Playwright capture guide smoke: PASS after fixing Element Plus selector ambiguity and scoping duplicate conversion-button assertions to the post-save action region.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- Quick Capture still uses the existing backend parser API for persistence; the local malformed-page warning is intentionally lightweight and may need to be centralized with backend parser validation later.
- The post-save action region surfaces multiple conversion paths, but deeper conversion UX still depends on existing Capture Inbox and conversion APIs.
- Full Playwright suite was not rerun in this prompt; the focused capture guide smoke was run as required.

### Next Recommended Prompt

Run a real-browser first-day workflow audit focused on Quick Capture: create first book, use beginner capture, save, convert to quote/action/note, review concept, open source, and return to Dashboard. Remove any remaining duplicate CTAs or ambiguous labels found in that hands-on loop.

## Prompt 6 — Detail Page Primary Action and Source Loop UX Pass

### Date and Local Time

2026-04-30 15:47:16 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Detail Page UX Changes

- Added a shared `DetailNextStepCard` component for one visible next-best action on detail pages.
- Book Detail now recommends the correct next step: add to library, capture the first thought, review action items, or create a review from the book.
- Note Detail now points users toward adding a parsed source-backed block instead of leaving notes as undifferentiated Markdown.
- Quote Detail now emphasizes applying the quote to a project while keeping Open Source visible as the secondary action.
- Action Item Detail now emphasizes completing or reopening the source-backed task and keeps source reopening one click away.
- Concept Detail now emphasizes applying the concept to a project or creating a review session.
- Project Detail now recommends the next practical design workflow step: add problem, add application, add decision, add finding, or open the project graph.

### Source and Backlink Visibility

- Project Detail now includes the shared `BacklinksSection` with source references gathered from project problems, applications, decisions, findings, lens reviews, and knowledge links.
- Existing Book, Note, Quote, Action Item, and Concept backlink/source sections were preserved.
- No fake source references or page numbers were introduced.

### Tests Updated

- Updated the MVP browser smoke test to assert the new next-action headings on Book, Note, Quote, Action Item, Concept, and Project detail pages.
- Adjusted the quote Apply to Project E2E selector to target the new workflow card explicitly after the page gained a second valid Apply to Project button.

### Files Created

- `frontend/src/components/workflow/DetailNextStepCard.vue`

### Files Modified

- `frontend/src/views/BookDetailView.vue`
- `frontend/src/views/NoteDetailView.vue`
- `frontend/src/views/QuoteDetailView.vue`
- `frontend/src/views/ActionItemDetailView.vue`
- `frontend/src/views/ConceptDetailView.vue`
- `frontend/src/views/ProjectDetailView.vue`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run e2e`
- `git diff --check`
- `git status --short`
- `git diff --stat`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Playwright E2E: PASS, 15 tests passed after tightening the quote Apply to Project selector for the new detail workflow card.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- The next-step logic is deterministic and intentionally simple; it does not yet account for every user preference or advanced workflow state.
- Knowledge Object Detail was not included in this focused pass because the checkpoint named the six detail pages above.
- The Project Detail source/backlink section depends on existing backlink/source APIs and will show honest empty states where no project source links exist.

### Next Recommended Prompt

Audit the full first-day user journey with a real browser: new user onboarding, first book, first capture, first conversion, first project application, and return-to-dashboard guidance. Remove any remaining duplicate or competing primary CTAs found during that hands-on walkthrough.

## Prompt 5 — Navigation Simplification and Progressive Disclosure

### Date and Local Time

2026-04-30 15:36:16 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Navigation Changes

- Refactored the sidebar from a flat module list into Primary, Secondary, and Advanced groups.
- Added a collapsed Advanced section for Knowledge Graph, Analytics, Import / Export, AI Drafts, and admin ontology tooling.
- Added a current mode indicator in the sidebar and top bar so users can understand why navigation priority changes.
- Added breadcrumbs for deep pages and a "Back to main workflow" panel on advanced pages.
- Added route shortcuts to global search so hidden or advanced routes remain reachable without exposing every module in the sidebar.

### Mode Behavior

- Reader Mode prioritizes Dashboard, Library, Capture, Notes, and Review.
- Game Designer Mode prioritizes Dashboard, Library, Capture, Projects, and Review.
- Researcher Mode prioritizes Dashboard, Library, Notes, Concepts, and Review.
- Advanced Mode expands advanced tools and keeps all major route groups visible.
- The user profile menu can update the persisted navigation mode through the existing onboarding preference API.

### Routes Preserved

- No router entries were removed.
- Deep links to Graph, Analytics, Import / Export, Admin Ontology, Use Cases, and existing workspace pages still resolve.
- Admin Ontology remains protected by the router role guard and backend authorization; the sidebar only hides it as a usability affordance for non-admin users.

### Accessibility Improvements

- Advanced navigation uses a real button with `aria-controls` and `aria-expanded`.
- Sidebar links preserve `aria-current` on active routes.
- The profile mode picker uses radio-style menu items with `aria-checked`.
- Global search route shortcuts are keyboard-activatable listbox options.
- Mobile navigation keeps the existing menu toggle and now exposes primary mode-aware routes plus search.

### Tests Added

- Added `frontend/e2e/navigation-progressive-disclosure.spec.ts`.
- Updated `frontend/e2e/use-cases.spec.ts` so hidden Use Cases navigation is verified through global route search instead of requiring a permanent sidebar link.
- E2E now verifies sidebar rendering across Reader, Game Designer, Researcher, and Advanced modes; advanced route access; mode switching; deep-link preservation; normal-user admin link hiding; and mobile menu usability.

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm ci`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npx playwright install chromium`
- `cd frontend; npm run e2e`
- `git diff --check`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Playwright E2E: PASS, 15 tests passed after fixing a duplicate Advanced Mode `Concepts` sidebar entry and stabilizing the Use Cases test setup.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- Mode personalization is still a simple persisted preference, not a full adaptive recommendation system.
- The Advanced route return panel is intentionally broad; individual advanced pages may still need more specific guidance.
- Settings remains disabled, so mode changes currently live in the profile menu rather than a dedicated settings page.

### Next Recommended Prompt

Perform a focused UX pass on detail pages: Book Detail, Note Detail, Quote Detail, Action Item Detail, Concept Detail, and Project Detail should each expose one primary next action, source/backlink visibility, and a clear path back to the core reading-to-project loop.

## Prompt 6 — Detail Page Primary Action and Source Loop UX Pass (Canonical Append)

### Date and Local Time

2026-04-30 15:47:16 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Summary

- Added `DetailNextStepCard` and wired it into Book, Note, Quote, Action Item, Concept, and Project detail pages.
- Preserved existing routes, source actions, backlink sections, and advanced workflows.
- Added Project Detail source/backlink visibility via the shared `BacklinksSection`.
- Updated the MVP browser smoke test to assert the new detail-page next-action affordances.
- Initial E2E run found a duplicate valid `Apply to Project` button on Quote Detail; the test was repaired to target the workflow card specifically.

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Playwright E2E: PASS, 15 tests passed.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- The next-step logic is intentionally deterministic and may need later personalization.
- Knowledge Object Detail was not included in this focused pass.
- This canonical section is appended at the end because an earlier detailed Prompt 6 entry was inserted above the previous Prompt 5 block in an already non-linear report tail.

### Next Recommended Prompt

Audit the full first-day user journey with a real browser: new user onboarding, first book, first capture, first conversion, first project application, and return-to-dashboard guidance. Remove any remaining duplicate or competing primary CTAs found during that hands-on walkthrough.

## Prompt 7 - First-Day Quick Capture Workflow Audit and Duplicate CTA Cleanup (Final EOF Append)

### Date and Local Time

2026-04-30 16:09:19 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Summary

- Audited the first-day Quick Capture loop with focused browser smoke coverage.
- Removed a duplicate CTA pattern where the same just-saved capture appeared in both the post-save action panel and Recent Note Blocks.
- Preserved the post-save panel as the single immediate conversion path for a newly saved capture.
- Updated E2E coverage so the browser flow converts through the post-save action region and asserts the action CTA is not duplicated.
- No backend features were added, no fake capture data is saved by examples, and no page numbers are invented.

### Files Modified

- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/e2e/capture-guide.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd frontend; npm run typecheck`
- `cd backend; .\mvnw.cmd test`
- `git diff --check`
- `cd frontend; npm run build`
- `cd frontend; npx playwright test e2e/capture-guide.spec.ts e2e/mvp-core-loop.spec.ts --workers=1`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend tests: PASS, 61 tests run with 0 failures and 0 errors.
- Focused Playwright E2E: PASS, 2 tests passed serially.
- Diff whitespace check: PASS with line-ending warnings only.

### Report Note

- Earlier Prompt 7 entries also exist in the non-linear `report.md` history because the same recommended prompt text appeared more than once. This final EOF append keeps the latest checkpoint evidence at the actual report tail without deleting prior content.

### Remaining Risks

- Running Playwright specs that each start backend web servers in parallel can race Maven/Spring Boot startup. The focused Quick Capture specs are stable with `--workers=1`.
- The full Playwright suite was not rerun in this checkpoint after the focused duplicate-CTA cleanup.
- The post-save panel currently remains the canonical immediate conversion path; later UX work should decide whether it should auto-dismiss after conversion or navigation.

### Next Recommended Prompt

Run the full Playwright suite serially with a single shared backend/frontend webServer strategy, then document and repair any remaining cross-spec instability before changing product UI further.

---

## Prompt 7 — Project Mode Hands-On Wizard: From Quote to Design Decision

### Date and Local Time

2026-04-30 16:51:52 +02:00

### Current SHA

3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0

### Wizard Steps Implemented

- Added `/projects/:id/wizard/apply-knowledge` as a guided Project Mode workflow.
- Implemented source selection from quotes, concepts, knowledge objects, source references, and daily prompts.
- Added steps for project problem definition, knowledge application, design decision, playtest plan, playtest finding, iteration note, and final confirmation.
- Added draft save/restore through browser local storage.
- Added cancel behavior that returns to the project cockpit without creating records.
- Added CTAs from Project Detail, Quote Detail, Concept Detail, and Project Workspace navigation.

### Source Preservation Behavior

- The wizard does not create records until the user confirms on the final step.
- Quote, concept, knowledge object, source-reference, and source-backed daily prompt selections preserve the selected source reference where the existing project APIs support it.
- Unknown pages remain null and are displayed as page unknown.
- The iteration note is stored through an existing `ProjectKnowledgeLink` with relationship type `ITERATION_NOTE`, avoiding new backend surface area in this prompt.
- Template daily prompts are labeled as template/source-free and do not invent source pages.

### Tests Added

- Added `frontend/e2e/project-wizard.spec.ts`.
- The E2E covers cancel-without-creation, draft restore, final confirmation, project problem creation, project application creation, design decision creation, playtest plan creation, playtest finding creation, iteration note link creation, source reference preservation, and visibility in Project Detail.

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `npm run typecheck`
- `npx playwright test e2e/project-wizard.spec.ts --workers=1`
- `npm run build`
- `.\mvnw.cmd test`
- `git diff --check`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend test suite: PASS, 61 tests run with 0 failures and 0 errors.
- Focused Project Mode wizard E2E: PASS, 1 test passed.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- Full Playwright suite was not rerun after adding the focused wizard smoke test.
- The wizard uses multiple existing project endpoints rather than a backend transaction, so a network failure during final confirmation can create a partial set of records.
- Iteration notes are represented as project knowledge links until a dedicated iteration log API is exposed to the frontend.

### Next Recommended Prompt

Run the full Playwright suite serially and then decide whether Project Mode needs a backend transactional command endpoint for the multi-record guided flow.

---

## Prompt 8 — Full Playwright Serial Suite and Project Wizard Transaction Assessment

### Date and Local Time

2026-04-30 16:55:57 +02:00

### Current SHA

3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0

### Scope

- Ran the complete Playwright browser E2E suite serially with one worker.
- Verified the existing backend/frontend webServer strategy starts a test-profile backend and production-preview frontend reliably.
- Evaluated whether the Project Mode guided wizard needs a backend transactional command endpoint before the next product iteration.

### E2E Result

- Full Playwright suite: PASS.
- Tests run: 17.
- Failed tests: 0.
- Covered flows included admin ontology dry run, quick capture guide, dashboard, MVP core reading loop, navigation modes, onboarding, project wizard, task-first workspaces, and use-case library.

### Transaction Endpoint Assessment

- No immediate backend transaction endpoint was added because the existing guided wizard passes E2E and preserves source references through current APIs.
- A transactional command endpoint remains a P1 hardening improvement, not a current P0 blocker.
- Recommended future endpoint shape: one authenticated project command that validates ownership, source reference visibility, and all step payloads before creating problem, application, decision, playtest plan, finding, and iteration note in one database transaction.
- Until then, the wizard honestly warns when final confirmation fails and avoids creating records before confirmation.

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `npm run e2e -- --workers=1`
- `npm run typecheck`
- `.\mvnw.cmd test`
- `npm run build`
- `git diff --check`

### Verification Result

- Full E2E: PASS, 17 tests passed.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Backend test suite: PASS, 61 tests run with 0 failures and 0 errors.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- Full E2E is stable serially, but parallel Playwright execution is not proven.
- The wizard still has partial-create risk if a network failure occurs during final multi-call confirmation.
- The repo contains a broad dirty working set from previous checkpoints; this prompt did not clean or revert unrelated work.

### Next Recommended Prompt

Add a backend transactional Project Wizard command endpoint and migrate the frontend wizard confirmation to use it, with integration tests proving all-or-nothing creation and source-reference preservation.

---

## Prompt 8 — In-App Help, Empty States, and Contextual Tooltips

### Date and Local Time

2026-04-30 17:05:45 +02:00

### Current SHA

3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0

### Help Topics Added

- Added a central in-app glossary source for Source Reference, Quick Capture, Capture Inbox, Quote, Action Item, Concept, Knowledge Object, Design Lens, Daily Prompt, Project Application, Design Decision, Playtest Finding, Graph, Backlink, Review Session, Mastery, AI Draft, and Import Preview.
- Added routeable help pages at `/help` and `/help/:topic`.
- Added a route-query help drawer driven by `?help=<topic>`.

### Empty States Improved

- Capture Inbox empty state now links to help and the capture workflow.
- Review empty state now links to review help.
- Mastery empty state now explains that mastery is not invented from empty activity and links to Review and help.

### Tooltips Added

- Added keyboard-focusable contextual help triggers to Dashboard, Book Detail, Quick Capture, Capture Inbox, Source Reference Drawer, Graph, Daily, Review, Mastery, Import/Export, Project Detail, and the AI draft right rail.
- Tooltip clicks open the non-blocking help drawer rather than creating records or hiding errors.

### Tests Added

- Added focused Playwright smoke coverage for `/help`, `/help/source-reference`, and the Dashboard Quick Capture help drawer.

### Files Created

- `docs/glossary.md`
- `docs/help-system.md`
- `frontend/e2e/help.spec.ts`
- `frontend/src/components/help/HelpDrawer.vue`
- `frontend/src/components/help/HelpTooltip.vue`
- `frontend/src/data/helpTopics.ts`
- `frontend/src/views/HelpView.vue`

### Files Modified

- `frontend/src/components/RightRail.vue`
- `frontend/src/components/Sidebar.vue`
- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/src/components/source/SourceReferenceDrawer.vue`
- `frontend/src/layouts/AppLayout.vue`
- `frontend/src/router/index.ts`
- `frontend/src/views/BookDetailView.vue`
- `frontend/src/views/CaptureInboxView.vue`
- `frontend/src/views/DailyView.vue`
- `frontend/src/views/DashboardView.vue`
- `frontend/src/views/GraphView.vue`
- `frontend/src/views/ImportExportView.vue`
- `frontend/src/views/MasteryView.vue`
- `frontend/src/views/ProjectDetailView.vue`
- `frontend/src/views/ReviewView.vue`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `npm run typecheck`
- `npm run build`
- `git diff --check`
- `npx playwright test e2e/help.spec.ts`

### Verification Result

- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Focused help E2E: PASS, 1 test passed.
- Diff whitespace check: PASS with line-ending warnings only.
- Backend tests were not rerun because this prompt made frontend/docs-only changes.

### Remaining Risks

- Help coverage is intentionally targeted, not every minor label has a tooltip.
- The full Playwright suite was not rerun after this help pass.
- The repository remains broadly dirty from previous checkpoints; unrelated existing changes were preserved.

### Next Recommended Prompt

Run a no-new-features usability pass in the browser to confirm help density is useful rather than noisy, then run the full Playwright suite if the visual review passes.

## Prompt 9 — Help Touch Target Hardening and Full Playwright Verification

### Date and Local Time

2026-04-30 17:12:04 +02:00

### Current SHA

3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0

### Verification Scope

- Re-checked current branch and SHA against `origin/main`.
- Reviewed the in-app help tooltip and help drawer implementation for accessibility and excessive noise.
- Ran backend tests, frontend typecheck, frontend production build, and the full Playwright browser suite.

### Help Usability Findings

- The help system is routeable and contextual without inserting fake data or masking errors.
- Help triggers open a drawer instead of blocking the user flow.
- One accessibility hardening issue was found: the help trigger was smaller than the app touch target.

### Changes Made

- Increased `HelpTooltip` trigger size to use the shared `--touch-target` token.
- No new product feature scope was added.

### Files Created

- None in this checkpoint.

### Files Modified

- `frontend/src/components/help/HelpTooltip.vue`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd frontend; npm run e2e -- --workers=1`
- `git diff --check`

### Verification Result

- Backend tests: PASS, 61 tests.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Full Playwright E2E: PASS, 18 tests.
- Diff whitespace check: PASS with existing line-ending warnings only.

### Remaining Risks

- The full Playwright suite was run serially; parallel stability was not revalidated.
- This was a code-level and automated-browser pass, not a headed visual density review.
- The repository remains broadly dirty from prior checkpoints; unrelated existing changes were preserved.

### Next Recommended Prompt

Perform a headed browser visual QA pass focused on help density, empty states, and mobile navigation, then prepare the dirty working tree for a clean review/commit boundary.

## Prompt 9 — Demo Workspace and Safe Sample Data for Hands-On Learning

### Date and Local Time

2026-04-30 17:38:53 +02:00

### Current SHA

3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0

### Demo Mode Behavior

- Added authenticated Demo Workspace APIs for current-user status, start, reset, and delete.
- Added `/demo` frontend route with explicit demo labeling, safety copy, record count summary, and links into the demo book, project, quote, action item, graph, and forum thread.
- Added dashboard, sidebar, global search, and hands-on use-case links to make the demo workspace discoverable without removing existing modules.
- Normal analytics exclude demo records by default. `includeDemo=true` opts analytics into demo records for learning verification.

### Demo Data Created

- Original demo book: `Demo Game Design Notebook` by `BookOS Demo`.
- Original sample captures: idea, quote-like sentence, action item, and concept marker.
- Original demo quote, action item, concepts, knowledge objects, project, project problem, project application, design decision, playtest finding, source-linked forum thread, source references, and entity links.
- Demo records are tracked in `demo_records` by user, entity type, and entity id.

### Safety Rules

- No copyrighted passages were seeded.
- No fake page numbers were created; demo source references and derived records keep page values `null`.
- Demo source confidence is `LOW`.
- Demo records are current-user scoped and clearly labeled/tagged as demo.
- Reset/delete removes only current-user demo records and also cleans demo-derived daily records before deleting demo books or knowledge objects.

### Tests Added

- `DemoWorkspaceIntegrationTest` verifies demo workspace status/start/reset/delete, cross-user isolation, and analytics exclusion/default opt-in behavior.
- `EndpointContractIntegrationTest` covers the `/api/demo/**` controller mappings.
- `demo-workspace.spec.ts` verifies browser-level start, navigation, analytics exclusion, reset, and delete behavior.

### Files Created

- `backend/src/main/java/com/bookos/backend/demo/controller/DemoWorkspaceController.java`
- `backend/src/main/java/com/bookos/backend/demo/dto/DemoWorkspaceStatusResponse.java`
- `backend/src/main/java/com/bookos/backend/demo/entity/DemoRecord.java`
- `backend/src/main/java/com/bookos/backend/demo/repository/DemoRecordRepository.java`
- `backend/src/main/java/com/bookos/backend/demo/service/DemoWorkspaceService.java`
- `backend/src/main/resources/db/migration/V11__demo_workspace_records.sql`
- `backend/src/test/java/com/bookos/backend/demo/DemoWorkspaceIntegrationTest.java`
- `docs/demo-workspace.md`
- `frontend/e2e/demo-workspace.spec.ts`
- `frontend/src/api/demo.ts`
- `frontend/src/views/DemoWorkspaceView.vue`

### Files Modified

- `README.md`
- `backend/src/main/java/com/bookos/backend/book/repository/BookAuthorRepository.java`
- `backend/src/main/java/com/bookos/backend/book/repository/BookTagRepository.java`
- `backend/src/main/java/com/bookos/backend/daily/repository/DailyHistoryRepository.java`
- `backend/src/main/java/com/bookos/backend/daily/repository/DailyReflectionRepository.java`
- `backend/src/main/java/com/bookos/backend/learning/controller/AnalyticsController.java`
- `backend/src/main/java/com/bookos/backend/learning/service/LearningService.java`
- `backend/src/test/java/com/bookos/backend/integration/EndpointContractIntegrationTest.java`
- `docs/api-contract-audit.md`
- `docs/api-endpoint-inventory.md`
- `docs/current-state.md`
- `docs/data-model-overview.md`
- `docs/hands-on-use-cases.md`
- `frontend/e2e/support/api.ts`
- `frontend/src/components/Sidebar.vue`
- `frontend/src/components/search/GlobalSearchDialog.vue`
- `frontend/src/data/useCases.ts`
- `frontend/src/router/index.ts`
- `frontend/src/types/index.ts`
- `frontend/src/views/DashboardView.vue`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --show-toplevel`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd backend; .\mvnw.cmd -Dtest=DemoWorkspaceIntegrationTest test`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd frontend; npx playwright test e2e/demo-workspace.spec.ts`
- `git diff --check`

### Verification Result

- Backend focused demo test: PASS.
- Backend full suite: PASS, 62 tests.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Focused Demo Workspace Playwright smoke: PASS, 1 test.
- Diff whitespace check: PASS with line-ending warnings only.

### Remaining Risks

- The full Playwright suite was not rerun after the demo workspace addition; only the focused demo smoke was run.
- Demo records are excluded from analytics by default, but ordinary list pages may still show demo rows with clear demo labels until deeper per-page filtering is added.
- The working tree remains broadly dirty from previous checkpoints; unrelated existing changes were preserved.

### Next Recommended Prompt

Run the full Playwright suite serially and perform a review-boundary cleanup that separates Demo Workspace changes from prior uncommitted onboarding/help/navigation work.

---

## Prompt 10 — Full Playwright Suite and Review Boundary Verification

### Date and Local Time

2026-04-30 17:49:21 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Scope

- Re-ran the current full browser smoke suite serially after the Demo Workspace checkpoint.
- Verified backend tests, frontend typecheck, frontend production build, and whitespace hygiene.
- Fixed only one E2E selector/state assumption; no product feature scope was added.

### E2E Failure Found

- `frontend/e2e/capture-guide.spec.ts` expected the dashboard Quick Capture no-book warning to be visible immediately.
- In the full serial suite, the dashboard can have a default capture book selected because local/test seed books are available.
- Product behavior was valid; the test was brittle because it assumed empty book context.

### E2E Fix Applied

- Updated the capture-guide test to scroll to the dashboard capture section and explicitly select `Select a book` before asserting the no-book source-reference warning.
- This keeps the product requirement covered while making the test independent of seeded or prior context.

### Browser Flows Passed

- Admin ontology dry-run.
- Guided Quick Capture and parser learning.
- Task-first dashboard.
- Demo Workspace start/reset/delete.
- Help/glossary route and contextual drawer.
- MVP reading loop through real APIs.
- Mode-aware navigation and command palette route discovery.
- Onboarding skip, Reader Mode, and Game Designer Mode.
- Project apply-knowledge wizard.
- Task-first workspaces.
- Use-case library routes.

### Files Created

- None.

### Files Modified

- `frontend/e2e/capture-guide.spec.ts`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --show-toplevel`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `git status --ignored --short | Select-String -Pattern 'backend.zip|\\.7z$|\\.log$|\\.out$|\\.err$|backend/target|frontend/dist|frontend/test-results|frontend/node_modules'`
- `cd frontend; npm run e2e -- --workers=1` (initial failure, 18 passed / 1 failed)
- `cd frontend; npx playwright test e2e/capture-guide.spec.ts --workers=1`
- `cd frontend; npm run e2e -- --workers=1`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `git diff --check`

### Test Results

- Focused capture-guide Playwright test after repair: PASS, 1 test.
- Full Playwright suite after repair: PASS, 19 tests.
- Backend test suite: PASS, 62 tests.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Diff whitespace check: PASS with line-ending warnings only.

### Hygiene Result

- No tracked archive/log/build artifacts were introduced by this pass.
- Ignored generated folders exist locally after verification: `backend/target/`, `frontend/dist/`, `frontend/node_modules/`, and `frontend/test-results/`.
- The working tree remains intentionally dirty from prior checkpoints; unrelated changes were preserved.

### Remaining Risks

- The full E2E suite now passes serially, but it still uses accumulated test data rather than a full database cleanup step per run.
- Line-ending warnings remain on Windows; no whitespace errors were reported.
- Review-boundary separation is still a manual review task because previous checkpoints remain uncommitted in the same working tree.

### Next Recommended Prompt

Perform a review-boundary diff audit: separate Demo Workspace, onboarding/help/navigation/dashboard/use-case changes into logical commit groups, then run the same backend/frontend/E2E verification before committing.

---

## Prompt 10 — Hands-On Public Beta UX Gate and PO Usability Report

### Date and Local Time

2026-04-30 17:56:03 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Usability Scenarios Tested

This was a product QA and heuristic usability gate, not a formal external user research study. No observed-user claims are made.

- Scenario 1: First-time reader.
- Scenario 2: Note-taker.
- Scenario 3: Game designer.
- Scenario 4: Researcher.
- Scenario 5: Community user.
- Scenario 6: Advanced user.

### Scenario Scores

| Scenario | Discoverability | Clarity | Completion | Cognitive load | Source-reference visibility | User confidence | Verdict |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| First-time reader | 5 | 4 | 5 | 4 | 5 | 4 | PASS |
| Note-taker | 4 | 4 | 5 | 4 | 5 | 4 | PASS |
| Game designer | 4 | 4 | 5 | 3 | 5 | 4 | PASS |
| Researcher | 3 | 3 | 4 | 3 | 4 | 3 | PARTIAL |
| Community user | 3 | 3 | 4 | 3 | 4 | 3 | PARTIAL |
| Advanced user | 3 | 3 | 4 | 2 | 4 | 3 | PARTIAL |

### What Changed in This Usability Sprint

- Created PO-facing hands-on usability gate docs.
- Created a first-15-minute guide that centers the first valuable loop: add book -> capture -> convert -> open source.
- Created a use-case playbook for first-time reader, note-taker, game designer, researcher, community, and advanced paths.
- Updated manual release QA with hands-on use case scenarios.
- No product feature scope was added.

### What a New User Can Now Do

- Register and choose a mode.
- Add a first book.
- Track reading state.
- Capture a thought while reading.
- Learn parser syntax in-app.
- Convert captures into notes, quotes, actions, and reviewed concepts.
- Open source from derived records.
- Practice safely in Demo Workspace.
- Apply source-backed knowledge to a game project.
- Use MockAIProvider as draft-only assistance.

### What Still Feels Bloated

- Graph, Analytics, Mastery, Import/Export, AI, and Admin Ontology remain too much for default first-day navigation.
- Knowledge Objects, Entity Links, and Mastery are still implementation-heavy labels.
- Project Mode is valuable but broad.
- Forum is strongest from source context, not as an empty global destination.

### Modules That Should Remain Advanced or Collapsed

- Knowledge Graph.
- Analytics.
- Mastery / Learning Progress.
- Import / Export.
- AI draft/settings surfaces.
- Admin Ontology.
- Global Knowledge Object management until the user has source-backed concepts/lenses.

### First-Class Use Cases

- Track a book.
- Capture while reading.
- Process Capture Inbox.
- Convert capture to quote/action/note/concept.
- Open source.
- Apply source-backed knowledge to a project.
- Start review from source-backed items.

### Still-Confusing Use Cases

- Starting from global Graph with no data.
- Creating Knowledge Objects without first using capture/concept review.
- Understanding Mastery before completing review sessions.
- Creating useful forum threads without source context.
- Knowing when to use AI drafts instead of normal note/capture workflows.

### P0 Usability Blockers

- None found.

### P1 Usability Issues

- Researcher and community paths need stronger contextual "start here" CTAs.
- Project wizard should eventually use a backend transaction to avoid partial creation on network failure.
- `Capture Inbox` should continue moving toward `Process Captures` language across page titles and docs.
- Knowledge Object terminology should be replaced with user-facing labels such as `Design Knowledge`.

### P2 Polish Issues

- Add more scoped graph entry points from source/detail pages.
- Collapse empty advanced cards more aggressively on Dashboard and Book Detail.
- Merge Review and Mastery into one learner-facing loop.
- Add optional per-user checklist progress for first-day tasks.

### Files Created

- `docs/hands-on-beta-ux-report.md`
- `docs/use-case-playbook.md`
- `docs/first-15-minutes.md`
- `docs/po-usability-review.md`

### Files Modified

- `docs/manual-release-qa.md`
- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm ci`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd frontend; npx playwright install chromium`
- `cd frontend; npm run e2e`
- `docker compose config`
- `docker compose -f docker-compose.full.yml config`
- `$env:JWT_SECRET='local-config-check-secret-with-at-least-32-chars'; docker compose -f docker-compose.full.yml config`
- `git diff --check`

### Verification Result

- Backend tests: PASS, 62 tests.
- Frontend clean install: PASS, 147 packages, 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright Chromium install: PASS.
- Playwright E2E: PASS, 19 tests.
- Local MySQL Compose config: PASS.
- Full-stack Compose config without `JWT_SECRET`: expected fail-safe error because `JWT_SECRET` is required.
- Full-stack Compose config with temporary local `JWT_SECRET`: PASS.
- Diff whitespace check: PASS with line-ending warnings only.

### PO Recommendation

Release with caveats.

The core first-reader, note-taker, and game-designer use cases are coherent enough for Public Beta 0.1. Researcher, community, graph, import/export, and AI workflows should remain secondary or advanced because they are still concept-heavy for a first-15-minute experience.

### Next Recommended Milestone

Product Slimming 0.2:

- Rename remaining implementation-heavy labels.
- Make `Process Captures` the central taught workflow.
- Collapse advanced empty states by default.
- Add a transactional Project Wizard command endpoint.
- Add per-mode first-task checklist.
- Run moderated usability sessions with 3-5 target users.

## Prompt 11 — Review Boundary Diff Audit and Commit Grouping Plan

### Date and Local Time

2026-04-30 17:58:58 +02:00

### Current SHA

`3584d4bab35c3b6d8a2a2ec2193bf8ca84c278e0`

### Current Branch

`main`

### Repository State

- Local `main` matches `origin/main`.
- Tracked changed files: 54.
- Untracked source/doc/test files: 51.
- Ignored generated folders present locally:
  - `backend/target/`
  - `frontend/dist/`
  - `frontend/node_modules/`
  - `frontend/test-results/`

### Audit Scope

This prompt did not add product features. It created a review-boundary document that organizes the current mixed dirty tree into safer review and commit groups.

### Review Groups Created

- Source-of-truth documentation and release reporting.
- Backend onboarding preferences.
- Frontend onboarding flow.
- Task-first dashboard, navigation, and use cases.
- Guided quick capture and parser learning.
- In-app help, empty states, and contextual education.
- Project hands-on wizard and project workflow polish.
- Demo workspace.
- Existing workflow surface polish.
- Backend query and analytics support adjustments.

### Files Created

- `docs/review-boundary-diff-audit.md`

### Files Modified

- `report.md`

### Commands Run

- `git fetch origin main`
- `git rev-parse --show-toplevel`
- `git rev-parse --abbrev-ref HEAD`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git status --short --branch`
- `git status --ignored --short`
- `git diff --stat`
- `git ls-files --others --exclude-standard`
- `git status --short`
- `Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"`
- `cd backend; .\mvnw.cmd test`
- `cd frontend; npm ci`
- `cd frontend; npm run typecheck`
- `cd frontend; npm run build`
- `cd frontend; npx playwright install chromium`
- `cd frontend; npm run e2e`
- `docker compose config`
- `docker compose -f docker-compose.full.yml config`
- `$env:JWT_SECRET='local-config-check-secret-with-at-least-32-chars'; docker compose -f docker-compose.full.yml config`
- `git diff --check`

### Verification Result

- Backend tests: PASS, 62 tests.
- Frontend clean install: PASS, 147 packages, 0 vulnerabilities.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Playwright Chromium install: PASS.
- Playwright E2E: PASS, 19 tests.
- Local MySQL Compose config: PASS.
- Full-stack Compose config without `JWT_SECRET`: expected fail-safe error because `JWT_SECRET` is required.
- Full-stack Compose config with temporary local `JWT_SECRET`: PASS.
- Diff whitespace check: PASS with line-ending warnings only.

### P0 Blockers

- None found.

### P1 Issues

- The dirty tree is too large for a single safe review or commit.
- `frontend/src/views/DashboardView.vue` is a large mixed UI change and needs targeted review before staging.
- `report.md` is large by design and should be reviewed separately from product code.

### P2 Issues

- Local generated folders exist but are ignored: `backend/target/`, `frontend/dist/`, `frontend/node_modules/`, and `frontend/test-results/`.
- Git reports LF-to-CRLF conversion warnings on touched files; `git diff --check` still passes.
- E2E data cleanup should be kept under review because browser tests create realistic workflow records.

### Next Recommended Prompt

Review and stage the current dirty tree in logical groups using `docs/review-boundary-diff-audit.md`. Do not create one giant commit. Start with onboarding backend/preferences and migration `V10`, then frontend onboarding, then dashboard/navigation/use-cases/help, then quick capture guide, project wizard, demo workspace, E2E, and finally docs/report updates.
