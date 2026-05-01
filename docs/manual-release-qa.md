# BookOS Manual Release QA

Last updated: 2026-05-01.

Use this template before tagging or broadening the BookOS beta. This is a QA checklist, not evidence of human research unless completed with real participants.

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

- Backend tests: `cd backend && .\mvnw.cmd test`.
- Frontend install: `cd frontend && npm ci`.
- Frontend typecheck: `npm run typecheck`.
- Frontend production build: `npm run build`.
- Full E2E smoke: `npm run e2e`.
- Product Slimming usability gate: `npm run e2e:usability`.
- Docker local MySQL config: `docker compose config`.
- Docker full-stack config: `docker compose -f docker-compose.full.yml config`.
- Secret/artifact scan:

Latest internal gate on SHA `c62e9eaa163e9ae7192046dceda09a6bf2470091`:

- Backend tests: PASS, 71 tests.
- Frontend typecheck: PASS.
- Frontend build: PASS.
- Full E2E smoke: PASS, 31 tests.
- Usability E2E: PASS, 8 tests.

## Browser Smoke Checklist

- Register a fresh user.
- Log in.
- Confirm Dashboard loads.
- Log out and log in again.
- Create a book.
- Add the book to personal library.
- Update reading status to Reading.
- Update current page and rating.
- Open Book Detail.
- Create a note.
- Create a quick capture with marker, page, tag, and `[[Concept Name]]`.
- Verify parser metadata appears.
- Convert one capture to note.
- Convert one capture to quote.
- Convert one capture to action.
- Confirm converted captures are no longer incorrectly actionable.
- Visit Quotes and open created quote.
- Open source from quote.
- Visit Actions.
- Complete and reopen action.
- Review parsed concept and create or accept concept.
- Open Concept Detail.
- Open related source links.
- Load Daily page or cards.
- Open Forum.
- Create source-linked thread.
- Add comment.
- Open Cmd/Ctrl+K global search.
- Search books, notes, quotes, actions, concepts, forum, and projects where data exists.
- Open result.
- Open Knowledge Graph.
- Confirm graph uses real links or honest empty state.
- Generate MockAIProvider draft.
- Edit, accept, and reject drafts.
- Confirm accepted draft does not overwrite source content.
- Create a game project.
- Apply quote, concept, or source to project.
- Create project problem, design decision, playtest finding, and lens review where supported.
- Export user data.
- Preview import if testing import.
- Confirm import preview does not write records.
- Open Ontology Import as admin.
- Confirm non-admin cannot access admin import.

## Hands-On Product Slimming Scenarios

Use `docs/first-15-minutes.md`, `docs/use-case-playbook.md`, `docs/hands-on-beta-ux-report.md`, and `docs/usability-scorecard.md`.

Score each scenario:

- Discoverability: 0-5.
- Clarity: 0-5.
- Completion: 0-5.
- Cognitive load: 0-5 where higher means lower overload.
- Source visibility: 0-5.
- Confidence: 0-5.

Mark result as `PASS`, `PARTIAL`, `FAIL`, or `NOT VERIFIED`.

### Scenario 1: First Valuable Loop

- Register fresh user.
- Complete onboarding.
- Add book.
- Capture original thought.
- Process capture.
- Open source.
- Complete or inspect use-case checklist.
- Result:
- Scores:
- Notes:

### Scenario 2: Reader Mode

- Choose Reader Mode.
- Confirm Dashboard shows three primary actions.
- Add book.
- Set reading state.
- Capture thought.
- Convert capture to quote.
- Open source.
- Result:
- Scores:
- Notes:

### Scenario 3: Note-Taker Mode

- Choose Note-Taker Mode.
- Create book.
- Create note.
- Use capture guide.
- Convert capture to action.
- Process capture checklist.
- Result:
- Scores:
- Notes:

### Scenario 4: Game Designer Mode

- Choose Game Designer Mode.
- Create book.
- Create project.
- Apply source-backed quote or concept to project.
- Create design decision.
- Open project cockpit.
- Verify source visibility.
- Result:
- Scores:
- Notes:

### Scenario 5: Researcher Mode

- Choose Researcher Mode.
- Capture with `[[Concept Name]]`.
- Review concept.
- Open Concept Detail.
- Open scoped graph or honest empty state.
- Start review session.
- Result:
- Scores:
- Notes:

### Scenario 6: Community Mode

- Choose Community Mode.
- Create or choose source-backed book, quote, or concept.
- Create source-linked forum thread.
- Add comment.
- Open source context.
- Search forum or source discussion if data exists.
- Result:
- Scores:
- Notes:

### Scenario 7: Demo Workspace

- Start Demo Workspace.
- Confirm demo safety copy is visible.
- Open demo record.
- Reset demo.
- Delete demo.
- Confirm demo data does not appear as real personal data.
- Result:
- Scores:
- Notes:

### Scenario 8: Advanced Mode

- Choose Advanced Mode.
- Open Knowledge Graph.
- Confirm real data or honest empty state.
- Export data.
- Generate MockAIProvider draft.
- Accept or reject draft safely.
- Result:
- Scores:
- Notes:

## Responsive Smoke

- Desktop width: cockpit layout does not overlap.
- Tablet width: right rail collapses or stacks safely.
- Mobile width: no horizontal overflow.
- Quick Capture remains usable.
- Tables/lists remain readable or scroll safely.

## Accessibility Smoke

- Keyboard can reach main navigation, top search, forms, source drawer, and dialogs.
- Focus ring is visible.
- Escape closes dialogs/drawers where applicable.
- Icon-only buttons have accessible names.
- Forms have labels and validation messages.
- Progress bars expose values.
- Status is not communicated by color alone.

## Security Smoke

- Private source links are not visible to another user.
- Search does not return another user's private data.
- Graph does not include another user's private nodes.
- Forum private context is hidden or unavailable when unauthorized.
- AI suggestion list is user-scoped.
- Ontology Import is admin-only.
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
- Product Slimming score:
- First 15 Minutes readiness score:
- Release recommendation:
- Approver:
