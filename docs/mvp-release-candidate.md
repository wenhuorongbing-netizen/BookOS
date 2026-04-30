# BookOS MVP Release Candidate

Last updated: 2026-04-29.

Current reviewed `main` SHA: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`.

## CI/CD Status

- GitHub Actions workflow: `.github/workflows/ci.yml`.
- Backend job: Java 21, Maven cache, `./mvnw -B test`.
- Frontend job: Node 24, npm cache, `npm ci`, `npm run typecheck`, `npm run build`.
- CI uses test profile/in-memory databases and does not require production secrets.

## Release Acceptance Checklist

Use `PASS`, `PARTIAL`, `FAIL`, `MISSING`, or `NOT VERIFIED`.

- Backend tests pass.
- Frontend typecheck passes.
- Frontend production build passes.
- Backend starts against migrated MySQL schema.
- Frontend can connect to backend through `/api`.
- Auth register/login/current-user flow works.
- Book library CRUD and user-library state work.
- Notes, quick capture, parser, and capture conversion work.
- Quotes and action items can be created, edited, archived, and source-opened.
- Concepts and knowledge objects can be reviewed, listed, and source-opened.
- Daily quote/prompt cards load real or honest template-backed data.
- Forum categories, templates, threads, comments, reports, and moderator controls work.
- Search returns only owned/visible records.
- Graph previews use real links or honest empty states.
- AI suggestions are drafts and do not overwrite source content.
- Private source references do not leak cross-user.
- Markdown rendering is sanitized.
- No generated artifacts or archives are committed.
- No secrets are committed.

## Manual QA Checklist

Run the existing detailed checklist in `docs/manual-ui-qa.md`.

Minimum RC smoke path:

1. Register a fresh user.
2. Log in, log out, and log in again.
3. Create a book and add it to the library.
4. Update status, current page, and rating.
5. Create a note and note block.
6. Create captures with emoji, page marker, tags, and `[[Concept]]`.
7. Convert captures to note, quote, and action item.
8. Review parsed concepts and create/accept a concept.
9. Open source from quote, action item, note block, concept, daily card, and forum thread.
10. Search with Cmd/Ctrl+K and open a result.
11. Open graph preview for a book and concept.
12. Create a source-linked forum thread and comment.
13. Report a thread and resolve it as admin/moderator.
14. Generate, edit, accept, and reject draft-only AI suggestions.

## Accessibility Checklist

Run `docs/ui-accessibility-checklist.md`.

RC minimum:

- Keyboard can reach all primary navigation and page actions.
- Focus rings are visible.
- Icon-only buttons have accessible names.
- Form fields have labels.
- Progress bars expose values.
- Status badges include text, not color-only state.
- Reduced motion baseline is present.
- No horizontal overflow on common mobile widths.

## Security Checklist

- JWT secret is supplied via environment variables.
- Passwords are BCrypt-hashed.
- CORS origins match deployment.
- Private notes/captures/source references are owner-scoped.
- Search and graph are user-scoped.
- Forum attached private context is hidden from unauthorized users.
- Markdown rendering is sanitized; raw HTML execution is not allowed.
- AI suggestions are draft-only and never overwrite user content.
- External AI providers are optional, environment-controlled, and unavailable without a configured key.
- No `.env`, archives, logs, `target`, `dist`, or `node_modules` are committed.

## Known Limitations

- Graph is a real-data preview/workspace, not an advanced graph editor.
- MockAIProvider is deterministic, local-only, and the default provider.
- Forum moderation is basic and not automated.
- Project mode exists for MVP project records, project problems, applications, decisions, playtests, knowledge links, and lens reviews; advanced project analytics and automated planning remain future work.
- Import/export exists for MVP JSON, Markdown, and CSV-oriented flows; conflict-resolution UX remains limited.
- Advanced ontology curation/review workflow is admin JSON import plus dry-run, not a full editorial CMS.
- Browser runtime QA must still be performed before tagging a release.

## Roadmap After MVP

1. Expand browser automation smoke tests beyond the current MVP E2E suite.
2. Full graph editing and richer graph visualization.
3. Project mode collaboration, richer planning automation, and advanced project analytics.
4. Advanced forum moderation dashboard and audit trail.
5. Production monitoring and audit UI for optional external AI provider usage.
6. Import/export conflict-resolution UX, larger backup validation, and ontology seed pack review flows.
7. Production deployment hardening: HTTPS, managed secrets, database backups, and monitoring.

## Final Test Summary Template

Record each release run:

- Backend tests:
- Frontend typecheck:
- Frontend build:
- Docker backend build:
- Docker frontend build:
- Local MySQL migration:
- Full-stack Docker Compose smoke:
- Manual browser smoke:
- Accessibility smoke:
- Security/secrets scan:
