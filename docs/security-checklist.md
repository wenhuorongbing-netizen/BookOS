# BookOS Public Beta 0.1 Security Checklist

Last updated: 2026-04-30.

Use this checklist before tagging or deploying BookOS Public Beta 0.1.

## Authentication And Sessions

| Check | Status | Evidence |
| --- | --- | --- |
| Passwords are hashed with BCrypt. | PASS | Spring Security password encoder and auth integration tests. |
| JWT secret is externalized. | PASS | `JWT_SECRET` is read from environment configuration. |
| Full-stack Compose requires `JWT_SECRET`. | PASS | `docker-compose.full.yml` uses required variable expansion. |
| `/api/auth/me` requires authentication. | PASS | Backend auth flow tests and E2E smoke. |

## Ownership And Privacy

| Check | Status | Evidence |
| --- | --- | --- |
| Notes, captures, quotes, action items, source references, concepts, projects, analytics, review, mastery, import/export, search, graph, and AI suggestions are user-scoped. | PASS | `SecurityBoundaryIntegrationTest`, `SearchGraphAIIntegrationTest`, module integration tests. |
| Forum private source context does not leak to unauthorized users. | PASS | Security boundary tests. |
| Admin ontology import is admin-only. | PASS | Admin ontology integration/E2E tests. |
| Source references do not invent page numbers. | PASS | Parser/source tests and product rule documentation. |

## Input Safety

| Check | Status | Evidence |
| --- | --- | --- |
| Request DTOs use validation annotations where practical. | PASS | Controller/service validation and endpoint tests. |
| Markdown rendering is sanitized in frontend rendering paths. | PASS | `renderSafeMarkdown` pattern and security docs. |
| Import preview does not write records. | PASS | Import/export integration tests. |
| AI JSON validates shape, task type, page fields, and overwrite-target fields. | PASS | `AIProviderServiceTest` and AI integration tests. |

## Secrets And Artifacts

| Check | Status | Evidence |
| --- | --- | --- |
| No `.env` file is committed. | PASS | Tracked artifact scan. |
| No archives/logs/build outputs are committed. | PASS | Tracked artifact scan. |
| OpenAI-compatible API key is not exposed by provider status. | PASS | `AIProviderSecretStatusIntegrationTest`. |
| Tests do not call real external AI providers. | PASS | Mock provider and mocked local HTTP server only. |

## Network And Deployment

| Check | Status | Evidence |
| --- | --- | --- |
| CORS origins are environment-controlled. | PASS | `application.yml`, `docker-compose.full.yml`, environment docs. |
| Actuator health is available without exposing details. | PASS | Health endpoint config and release verification. |
| TLS is not bundled in the app. | PARTIAL | Operator must terminate HTTPS at reverse proxy or hosting platform. |
| Production monitoring is not built in. | PARTIAL | Operator must configure logs/metrics/alerts. |

## Beta Security Caveats

- Public Beta 0.1 is suitable for controlled beta use, not unsupervised multi-tenant production.
- Admin and moderator workflows are basic.
- No rate limiting, abuse protection, or advanced audit log UI is included.
- Optional external AI must be configured only after a privacy review of provider terms and data handling.

