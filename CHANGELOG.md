# BookOS Changelog

All notable public-beta changes are tracked here.

## 0.1.0-beta - Public Beta 0.1

Release date target: not tagged yet.

Current reviewed source SHA: `f71d53ffdf58d9f2c7b8e3429af8605b4b8ad3ae`.

### Added

- Full-stack BookOS MVP with Java 21, Spring Boot, MySQL/Flyway, Vue 3, TypeScript, Vite, Pinia, Vue Router, Element Plus, and Axios.
- Authentication, roles, JWT-based API access, current-user endpoint, and owner-scoped private data.
- Personal book library with reading status, progress, rating, favorites, and book detail cockpit.
- Notes, note blocks, Quick Capture, deterministic emoji/page/tag/`[[Concept]]` parser, capture inbox, and capture conversion.
- Source references and entity links for source-backed notes, captures, quotes, action items, concepts, knowledge objects, forum threads, daily items, AI suggestions, and project applications.
- Quotes and action items workspaces with create/edit/archive/source-open flows.
- Concept review workflow and knowledge object workspace.
- Daily sentence and daily design prompt MVP using real source-backed content or clearly labeled template prompts.
- Structured forum MVP with categories, threads, comments, likes, bookmarks, reports, and source-linked discussions.
- Global search and graph workspace using real user-owned data.
- Safe original Game Design ontology admin import/dry-run workflow.
- Game Project Mode with projects, project problems, project applications, design decisions, playtest plans/findings, knowledge links, and lens reviews.
- Reading sessions, review sessions, knowledge mastery, and real-data analytics pages.
- Import/export MVP for user-owned JSON, Markdown, and CSV workflows.
- Draft-only AI suggestion workflow with MockAIProvider default and optional OpenAI-compatible provider configuration.
- Dockerfiles, local MySQL compose, optional full-stack compose, CI workflow, release documentation, and E2E smoke test documentation.

### Changed

- Normal development schema management uses Flyway migrations and Hibernate validation instead of unsafe Hibernate create/drop behavior.
- README and migration docs now reflect the current implementation state through Public Beta 0.1 preparation.
- Full-stack compose now passes AI provider environment variables explicitly while keeping secrets external.

### Security

- Passwords are BCrypt-hashed.
- JWT secret is externalized and required for full-stack compose.
- CORS origins are environment-controlled.
- Markdown rendering is sanitized in frontend rendering paths.
- Search, graph, source reference, AI suggestion, project, and forum private-context paths are designed to be owner-scoped.
- AI suggestions remain drafts and must be accepted, edited, or rejected by the user. AI acceptance does not overwrite original user content automatically.

### Known Limitations

- Public beta is not a hosted production service; deployment still expects operator-managed environment variables, TLS termination, database backups, and monitoring.
- E2E browser smoke tests are documented and available locally but are not yet enforced as a required CI gate.
- Graph visualization is functional and real-data-backed, but not an advanced large-scale graph analysis tool.
- Project Mode is usable for MVP workflows, but advanced planning analytics and project dashboards are still future work.
- Review/mastery is intentionally lightweight and not a full spaced-repetition engine.
- Import conflict-resolution UX is limited; users must review import previews carefully.
- Optional external AI provider integration is present, but tests use mock/mocked HTTP only and no production provider is required.

### Release Tag

Create the public beta tag only after all release checks pass:

```bash
git tag -a v0.1.0-beta -m "BookOS Public Beta 0.1"
git push origin v0.1.0-beta
```
