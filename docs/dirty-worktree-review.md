# BookOS Dirty Worktree Review

Last reviewed: 2026-05-01.

Current branch: `main`

Current SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`

## Purpose

This document records a focused review of the current local dirty worktree and proposes reviewable commit or PR-sized slices. It does not introduce product scope and does not mark any feature complete by itself.

## Current Worktree Summary

- Total changed paths from `git status --porcelain -uall`: 136.
- Tracked modified paths: 109.
- Untracked paths: 27.
- Largest modified areas: frontend UX/workflow changes, backend project/use-case/demo support, E2E tests, and release/usability documentation.
- No tracked `backend.zip`, `.7z`, logs, `.out`, `.err`, `backend/target`, `frontend/dist`, or `node_modules` paths were found by the tracked artifact scan.
- `.gitignore` includes protections for archives, logs, backend target, frontend dist, frontend node_modules, and Playwright artifacts.

## Untracked Paths

The untracked paths are source, migration, test, or documentation files that must be intentionally reviewed before staging:

- Backend project wizard DTO/entity/repository files.
- Backend use-case progress controller, DTOs, entities, repositories, and services.
- Flyway migrations `V12`, `V13`, and `V14`.
- Backend use-case progress integration test.
- Product slimming and terminology docs.
- Guided first-loop and usability E2E specs.
- Frontend use-case progress API adapter.
- Frontend guided first-loop view.

## Recommended Review Slices

### Slice 1 - Use Case Progress Tracking

Purpose: make hands-on use cases executable with persisted progress.

Include:
- `backend/src/main/java/com/bookos/backend/usecase/**`
- `backend/src/main/resources/db/migration/V12__user_use_case_progress.sql`
- `backend/src/main/resources/db/migration/V13__user_use_case_events.sql`
- `backend/src/test/java/com/bookos/backend/usecase/**`
- `frontend/src/api/useCaseProgress.ts`
- `frontend/src/components/use-case/**`
- `frontend/src/views/UseCasesView.vue`
- `frontend/src/views/UseCaseDetailView.vue`
- `frontend/src/data/useCases.ts`
- `frontend/e2e/use-cases.spec.ts`

Review focus:
- User scoping for progress and events.
- No fake completion.
- Idempotent progress updates.
- Route links point to real app routes.

### Slice 2 - Guided First Valuable Loop and Onboarding Recovery

Purpose: guide new or returning users from zero to one source-backed record.

Include:
- `frontend/src/views/GuidedFirstLoopView.vue`
- `frontend/src/views/OnboardingView.vue`
- `frontend/src/views/DashboardView.vue`
- `frontend/src/router/index.ts`
- `frontend/src/types/index.ts`
- `frontend/src/layouts/AppLayout.vue`
- `frontend/src/components/Sidebar.vue`
- `frontend/src/components/TopNav.vue`
- `frontend/e2e/guided-first-loop.spec.ts`
- `frontend/e2e/onboarding.spec.ts`
- `frontend/e2e/dashboard.spec.ts`
- `frontend/e2e/navigation-progressive-disclosure.spec.ts`

Review focus:
- Existing users are not forced through onboarding again.
- Mode changes persist.
- The first-loop flow keeps unknown page numbers null.
- Advanced modules remain accessible but do not dominate first-run UX.

### Slice 3 - Quick Capture, Help, Terminology, and Empty States

Purpose: make parser, capture, source links, and advanced terms easier to learn.

Include:
- `frontend/src/components/book-detail/BookCaptureSection.vue`
- `frontend/src/views/CaptureInboxView.vue`
- `frontend/src/data/helpTopics.ts`
- `frontend/src/views/HelpView.vue`
- `frontend/e2e/capture-guide.spec.ts`
- `frontend/e2e/help.spec.ts`
- `docs/terminology-guide.md`
- Relevant sections of `docs/hands-on-use-cases.md`

Review focus:
- Examples only populate inputs and do not create records automatically.
- User-facing labels are simpler while API and route names remain stable.
- Empty states guide action without pretending data exists.

### Slice 4 - Demo Workspace Training Ground

Purpose: provide clearly labeled safe practice data without polluting real data.

Include:
- `backend/src/main/java/com/bookos/backend/demo/**`
- Repository filtering changes used by demo status/counts.
- `backend/src/test/java/com/bookos/backend/demo/DemoWorkspaceIntegrationTest.java`
- `frontend/src/views/DemoWorkspaceView.vue`
- `frontend/e2e/demo-workspace.spec.ts`
- `docs/demo-workspace.md`

Review focus:
- Demo records are user-scoped and labeled.
- Demo reset/delete only affects demo records.
- Demo records do not pollute analytics unless explicitly included.
- Demo content avoids copyrighted passages and fake source pages.

### Slice 5 - Project Apply-Knowledge Transaction

Purpose: make the project wizard safe against partial record creation.

Include:
- `backend/src/main/java/com/bookos/backend/project/dto/ProjectApplyKnowledgeWizardRequest.java`
- `backend/src/main/java/com/bookos/backend/project/dto/ProjectApplyKnowledgeWizardResponse.java`
- `backend/src/main/java/com/bookos/backend/project/dto/ProjectWizardCreatedRecordResponse.java`
- `backend/src/main/java/com/bookos/backend/project/entity/ProjectWizardSubmission.java`
- `backend/src/main/java/com/bookos/backend/project/repository/ProjectWizardSubmissionRepository.java`
- `backend/src/main/resources/db/migration/V14__project_wizard_submissions.sql`
- `backend/src/main/java/com/bookos/backend/project/controller/ProjectController.java`
- `backend/src/main/java/com/bookos/backend/project/service/ProjectService.java`
- `backend/src/test/java/com/bookos/backend/project/ProjectModeIntegrationTest.java`
- `frontend/src/api/projects.ts`
- `frontend/src/views/ProjectApplyKnowledgeWizardView.vue`
- `frontend/e2e/project-wizard.spec.ts`

Review focus:
- Transaction creates all requested records or none.
- Ownership is checked before writes.
- Idempotency key prevents duplicate submissions.
- Source references are preserved.

### Slice 6 - Project Mode UX and Cross-Module Apply Flows

Purpose: make project mode more usable from quotes, concepts, knowledge, daily, source drawer, and project cockpit.

Include:
- `frontend/src/components/project/**`
- `frontend/src/components/quotes/QuoteFormDialog.vue`
- `frontend/src/components/concept/ConceptReviewDialog.vue`
- `frontend/src/views/ProjectDetailView.vue`
- `frontend/src/views/ProjectsView.vue`
- `frontend/src/views/ProjectApplicationsView.vue`
- `frontend/src/views/QuoteDetailView.vue`
- `frontend/src/views/ConceptDetailView.vue`
- `frontend/src/views/KnowledgeObjectDetailView.vue`
- `frontend/src/views/DailyView.vue`
- `docs/project-mode-hands-on.md`

Review focus:
- No fake project data.
- Source preservation is visible.
- Project action CTAs are clear.
- Existing project APIs remain compatible.

### Slice 7 - Graph, Source, Search, Import/Export, and Advanced UX

Purpose: improve advanced workflows while keeping them honest and data-backed.

Include:
- `frontend/src/views/GraphView.vue`
- `frontend/src/views/ImportExportView.vue`
- `frontend/src/components/search/GlobalSearchDialog.vue`
- `frontend/src/components/source/**`
- `frontend/src/composables/useOpenSource.ts`
- Related graph/source/import sections in docs.

Review focus:
- No fake graph nodes or edges.
- Source open paths preserve query context.
- Import/export states are honest.
- Advanced features stay available but not over-promoted.

### Slice 8 - E2E, Release Gate, and QA Evidence

Purpose: make browser verification explicit and release-gate friendly.

Include:
- `.github/workflows/e2e.yml`
- `frontend/package.json`
- `frontend/e2e/usability-first-15-minutes.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`
- `frontend/e2e/task-first-workspaces.spec.ts`
- `docs/e2e-smoke-tests.md`
- `docs/release-test-results.md`
- `docs/public-beta-release.md`
- README E2E references.

Review focus:
- `e2e:usability` remains separate from the full suite.
- Manual GitHub Actions workflow can select `full` or `usability`.
- Docs do not overstate human usability validation.
- No external AI or production secrets are required.

### Slice 9 - API, Current State, and Product Slimming Documentation

Purpose: keep source-of-truth docs aligned with implemented modules.

Include:
- `docs/api-contract-audit.md`
- `docs/api-endpoint-inventory.md`
- `docs/current-state.md`
- `docs/data-model-overview.md`
- `docs/product-slimming-baseline.md`
- `docs/product-usability-audit.md`
- `docs/use-case-gap-analysis.md`
- `docs/navigation-simplification-plan.md`
- `docs/first-15-minutes.md`
- `docs/hands-on-beta-ux-report.md`
- `docs/po-usability-review.md`

Review focus:
- Docs match real routes/controllers.
- No fake completion claims.
- No human research claims without actual sessions.
- Product slimming recommendations remain distinct from implemented behavior.

### Slice 10 - Report Appendix

Purpose: preserve requested prompt-by-prompt audit history.

Include:
- `report.md`

Review focus:
- Append-only rule has been preserved.
- Duplicated prompt sections are expected if the user re-ran a prompt.
- Encoding should be reviewed in a UTF-8-aware editor before publishing.

## Suggested Order

1. Use Case Progress Tracking.
2. Guided First Valuable Loop and Onboarding Recovery.
3. Quick Capture, Help, Terminology, and Empty States.
4. Demo Workspace Training Ground.
5. Project Apply-Knowledge Transaction.
6. Project Mode UX and Cross-Module Apply Flows.
7. Graph, Source, Search, Import/Export, and Advanced UX.
8. E2E, Release Gate, and QA Evidence.
9. API, Current State, and Product Slimming Documentation.
10. Report Appendix.

This order keeps database migrations and backend contracts ahead of the frontend flows that depend on them.

## Verification Baseline

The most recent verification recorded before this review:

- Backend tests: PASS, 68 tests.
- Frontend typecheck: PASS.
- Frontend production build: PASS.
- Usability E2E: PASS, 6 tests.

No new product code was changed as part of this dirty-worktree review.

## Risks

- The worktree is too large to review as one PR.
- Some slices share frontend layout and routing files, so staging must be careful.
- Flyway migrations must remain ordered and must be committed with their corresponding backend entities.
- `report.md` is large and append-only; treat it as audit evidence rather than product documentation.
- Line-ending warnings are present on Windows but no line-ending normalization was performed in this review.
