# BookOS Public Beta 0.1 Known Limitations

Last updated: 2026-04-30.

This document lists limitations accepted for Public Beta 0.1. Placeholder pages, static UI, or undocumented behavior should not be treated as complete features.

## Release And Operations

- Public Beta 0.1 is not a hosted production SaaS release.
- Operators must provide TLS termination, database backup policy, monitoring, log retention, and secret management.
- E2E smoke tests are available and documented, but are optional/manual in CI rather than required on every push.
- Backend tests use H2/test profile. MySQL is the target runtime database and is covered through Flyway-compatible migrations, but not every test runs against MySQL.

## Product Scope

- Graph workspace uses real data, but visualization is lightweight and not an advanced graph analytics tool.
- Entity-link relationship editing still relies on numeric entity IDs in some flows; selector-driven relationship creation remains future work.
- Project Mode is MVP-usable, but does not include advanced planning automation, analytics dashboards, collaboration, or real-time workflows.
- Review/mastery is intentionally lightweight and is not a full spaced-repetition engine.
- Analytics are computed from real persisted records, but historical analytics snapshots and trend forecasting are not implemented.
- Forum moderation is basic. Automated moderation, spam controls, and real-time notifications are not implemented.

## Data Portability

- Import/export supports current-user JSON, book Markdown, and CSV flows for core records.
- Import preview is required before commit, but conflict-resolution UX remains limited.
- MVP JSON export includes some archival sections that MVP import does not fully recreate, such as project applications, authored forum threads, and daily reflections.
- Unknown or malformed page numbers remain `null`; the system must not invent pages.

## AI

- MockAIProvider is the default and requires no external key.
- OpenAI-compatible provider is optional and environment-controlled.
- Tests use MockAIProvider or mocked local HTTP only; no real provider is called.
- AI suggestions are drafts only and do not overwrite user content.
- AI input truncation exists, but semantic privacy classification for freeform selected text is not implemented.

## UI And Accessibility

- The BookOS cockpit UI is responsive and usable, but full manual mobile/tablet QA still needs human sign-off before public tag creation.
- The graph visualization and some dense tables may need additional usability polish for very small screens.
- Browser E2E covers the MVP path, but does not deeply test every secondary modal or every AI task selector.

