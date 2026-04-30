# BookOS Manual Release QA

Last updated: 2026-04-30.

Use this template before tagging BookOS Public Beta 0.1.

## Run Metadata

- Tester:
- Date:
- Source SHA:
- Browser:
- Backend environment:
- Frontend environment:
- Database:
- AI provider mode:

## Automated Checks

Record `PASS`, `FAIL`, or `NOT RUN`.

- Backend tests: PASS on 2026-04-30, `.\mvnw.cmd test`, 62 tests.
- Frontend typecheck: PASS on 2026-04-30, `npm run typecheck`.
- Frontend production build: PASS on 2026-04-30, `npm run build`.
- Docker Compose config: PASS on 2026-04-30 for local MySQL and full stack.
- Docker backend image build: PASS on 2026-04-29 via `docker compose -f docker-compose.full.yml build`.
- Docker frontend image build: PASS on 2026-04-29 via `docker compose -f docker-compose.full.yml build`.
- Full-stack Docker health smoke: PASS on 2026-04-29 with isolated Compose project `bookos_beta_check`.
- Playwright E2E smoke: PASS on 2026-04-30, `npm run e2e -- --workers=1`, 19 tests.
- Secret/artifact scan: PASS on 2026-04-30 for common token patterns and committed artifact paths.

Note: full manual browser QA and responsive checks still need human sign-off before tagging.

## Browser Smoke Checklist

- Register a fresh user.
- Log in.
- Confirm dashboard loads.
- Log out and log in again.
- Create a book.
- Add the book to personal library.
- Update reading status to `READING`.
- Update current page and rating.
- Open book detail.
- Create a note.
- Create a quick capture with emoji, page marker, tag, and `[[Concept]]`.
- Verify parser metadata appears.
- Convert one capture to note.
- Convert one capture to quote.
- Convert one capture to action item.
- Confirm converted captures are no longer incorrectly actionable.
- Visit Quotes page and open created quote.
- Open source from quote.
- Visit Action Items page.
- Complete and reopen action item.
- Review parsed concept and create or accept concept.
- Open concept detail.
- Open related source references.
- Load Daily page/cards.
- Regenerate daily item if supported.
- Save daily reflection if supported.
- Open Forum page.
- Create source-linked thread.
- Add comment.
- Report thread.
- Open Cmd/Ctrl+K global search.
- Search books, notes, quotes, action items, concepts, forum, and projects where data exists.
- Open result.
- Open Graph page.
- Confirm graph uses real links or honest empty state.
- Generate MockAIProvider note summary suggestion.
- Edit, accept, and reject AI suggestions.
- Confirm accepted suggestion does not overwrite original note or source content.
- Create a game project.
- Apply a quote/concept/source reference to project.
- Create project problem, design decision, playtest finding, and lens review.
- Export user data.
- Preview import.
- Confirm import preview does not write records.
- Open admin ontology page as admin.
- Load default ontology JSON.
- Dry-run import.
- Confirm non-admin cannot access admin import.

## Hands-On Use Case QA

Use `docs/first-15-minutes.md`, `docs/use-case-playbook.md`, and `docs/hands-on-beta-ux-report.md` for scenario instructions and scoring.

Record `PASS`, `PARTIAL`, `FAIL`, or `NOT VERIFIED`.

### Scenario 1: First-Time Reader

- Register a fresh user.
- Choose Reader Mode.
- Add a book.
- Add it to the personal library.
- Set reading status to Reading.
- Open Book Detail.
- Save one source-backed Quick Capture.
- Convert the capture to Quote.
- Open the Quote.
- Open Source from the Quote.
- Result:
- Notes:

### Scenario 2: Note-Taker

- Choose Note-Taker Mode.
- Create a note for a book.
- Open the Quick Capture guide.
- Insert an example and confirm it does not save fake data automatically.
- Save one real action capture.
- Open Capture Inbox.
- Convert the capture to Action Item.
- Result:
- Notes:

### Scenario 3: Game Designer

- Choose Game Designer Mode.
- Add a game design book.
- Create a project.
- Create or open a quote/concept.
- Apply the quote/concept to the project.
- Create a design decision.
- Open Project Cockpit.
- Verify source reference visibility.
- Result:
- Notes:

### Scenario 4: Researcher

- Create a note or capture with `[[Concept Name]]`.
- Open Concept Review.
- Create or accept the concept.
- Open Concept Detail.
- Verify source references and backlinks.
- Open scoped Graph.
- Start a review session where available.
- Result:
- Notes:

### Scenario 5: Community User

- Open Forum.
- Create a source-linked thread from a book, quote, concept, project, or source reference where available.
- Add a comment.
- Open the attached source context.
- Confirm private source context is not leaked to unauthorized users.
- Result:
- Notes:

### Scenario 6: Advanced User

- Open Knowledge Graph.
- Confirm graph uses real data or an honest empty state.
- Open Import/Export.
- Export user data.
- Generate a MockAIProvider draft.
- Edit, accept, and reject drafts.
- Confirm source content is not overwritten.
- Result:
- Notes:

## Responsive Smoke

- Desktop width: three-zone cockpit does not overlap.
- Tablet width: right rail collapses or stacks safely.
- Mobile width: no horizontal overflow.
- Quick capture remains usable.
- Tables/lists remain readable or scroll safely.

## Accessibility Smoke

- Keyboard can reach main navigation, top search, forms, source drawer, and dialogs.
- Focus ring is visible.
- Escape closes dialogs/drawers where applicable.
- Icon-only buttons have accessible names.
- Forms have labels and validation messages.
- Progress bars expose values.
- Status is not communicated by color alone.
- Reduced motion baseline is respected.

## Security Smoke

- Private source references are not visible to another user.
- Search does not return another user's private data.
- Graph does not include another user's private nodes.
- Forum private context is hidden or unavailable when unauthorized.
- AI suggestion list is user-scoped.
- Admin ontology import is admin-only.
- Markdown content does not execute scripts.

## Performance Baseline

Record observed local values:

- Backend startup time:
- Frontend production build time:
- Frontend gzip bundle summary:
- Dashboard load behavior:
- Book detail load behavior:
- Search response on local demo data:
- Graph response on local demo data:
- Obvious N+1 or repeated request issue observed:

## Release Decision

- P0 blockers:
- P1 blockers:
- P2 issues accepted:
- Release recommendation:
- Approver:
