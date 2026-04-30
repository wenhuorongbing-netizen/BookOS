# BookOS Security Authorization Matrix

Current verified base: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

This matrix documents the intended security behavior for the implemented BookOS modules. It is based on the current Spring Security configuration, controller annotations, service-level ownership checks, frontend route guards, and integration tests.

## Global Rules

- All `/api/**` endpoints require authentication except `/api/auth/**`, `/api/health`, and `/actuator/health/**`.
- JWT authentication is stateless. Tokens carry subject email, issuer, expiration, and role claim.
- Passwords are hashed with BCrypt.
- API errors use the common `ApiResponse` envelope or `ValidationErrorResponse`; authentication and authorization errors avoid sensitive detail.
- Private source references are user-owned and must never be returned to another user.
- Unknown page numbers remain `null`; the system must not invent page numbers.
- User Markdown is sanitized or rendered through `renderSafeMarkdown`; raw HTML execution is not allowed.
- AI suggestions are drafts. Accept/edit/reject changes only the suggestion status/content, not source notes, captures, quotes, action items, concepts, projects, or forum content.
- Optional OpenAI-compatible provider configuration is environment-driven. Provider status must not return API keys or request authorization headers.

## Role Summary

| Role | Capabilities | Boundaries |
| --- | --- | --- |
| USER | Own library, notes, captures, quotes, action items, concepts, knowledge objects, projects, review/mastery/analytics, imports/exports, AI drafts, forum posts. | Cannot read or mutate another user's private records or private source references. |
| MODERATOR | Forum moderation where service checks `ADMIN` or `MODERATOR`. | Does not automatically receive private source visibility. |
| ADMIN | Admin ontology import; can perform forum moderation. Current book catalog service allows admin catalog-level read/manage behavior. | Does not automatically receive private project, note, capture, quote, action, source, AI, import/export, review, mastery, or analytics access unless a module explicitly implements it. |

## Module Matrix

| Module | User Own Records | Cross-User Private Records | Admin | Moderator | Public/Shared Behavior | Source References | Search/Graph Visibility | AI Behavior |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Auth/JWT | User can register, login, and call current-user endpoint with token. | Token subject maps to one user; unauthenticated calls receive JSON 401. | No special auth bypass. | No special auth bypass. | Public only for auth/health endpoints. | Not applicable. | Not indexed. | Not applicable. |
| User profile | User receives own current user/profile data. | Other user profile APIs are not exposed for normal users. | Admin user listing may exist only where documented by controller. | None. | Not public. | Not applicable. | Not indexed except author display in allowed forum/search contexts. | Not sent to AI except authenticated subject lookup. |
| Books | User can create private books and read public/shared books. | Private books require owner or explicit admin catalog access. | Can manage catalog/private books per current `BookService`. | No book-specific moderation. | `PUBLIC`/`SHARED` books are readable by authenticated users. | Book source lists are filtered by source owner. | Book search lists readable catalog items only; graph book access uses book access checks plus user-owned derived nodes. | Book context may be used for AI only when user can access it. |
| User library | User can add books and update status/progress/rating for own `UserBook`. | `UserBook` operations use user id and book id. | No automatic access to user library state. | None. | Catalog visibility does not expose another user's library progress/rating. | Library data does not create source refs by itself. | Search/analytics use only current user's library state. | Not directly used as AI input. |
| Notes / note blocks | User can create, read, update, delete own notes and parsed blocks. | Note lookups use `noteId + userId`; cross-user access returns not found. | No automatic private note access. | None. | No public note sharing implemented. | Note and block source refs are created with `user`, `book`, page fields from parser, and confidence. | Search/graph/backlinks use current user's notes and blocks. | AI can use owned note content only; suggestions remain drafts. |
| Captures | User can create, list, archive, and convert own captures. | Capture conversion and source listing use `captureId + userId`; cross-user access returns not found. | No automatic private capture access. | None. | No public capture sharing implemented. | Raw capture source refs preserve parsed page or `null`. | Search includes current user's inbox/converted captures only; graph uses current user's captures. | AI can use owned capture text only; suggestions remain drafts. |
| Quotes | User can create/edit/archive own quotes and convert from own captures. | Quote lookup/actions use `quoteId + userId`; cross-user access returns not found. | No automatic private quote access. | None. | No public quote sharing implemented outside explicit forum text. | Quote source refs remain owned and source-backed. | Search/graph include current user's quotes only. | AI can read owned quote text only; does not overwrite quote content. |
| Action items | User can create/edit/archive/complete/reopen own action items. | Action item mutation uses `actionItemId + userId`; cross-user actions return not found. | No automatic private action access. | None. | No public action sharing implemented. | Source refs preserved from capture/manual source. | Search/graph/right rail use current user's action items only. | AI extraction creates draft suggestions only. |
| Concepts | User can create/review/update/archive own concepts. | Concept lookup uses `conceptId + userId`; cross-user access returns not found. | Admin ontology import creates admin/system-owned seed records for admin user. | None. | No public concept sharing implemented. | Concept sources are linked through user-owned `EntityLink` and source refs. | Search/graph/backlinks use current user's concepts only. | AI concept extraction creates draft suggestions only; concept creation requires user action. |
| Knowledge objects | User can create/read/archive own knowledge objects. | Knowledge object lookup uses `id + userId`. | Admin ontology import creates seed objects under admin/system context. | None. | No public knowledge-object sharing implemented. | Optional source reference is validated as owned. | Search/graph/backlinks use current user's objects only. | AI suggestions do not create authoritative objects automatically. |
| Source references | User can list/get source refs they own. | Direct source lookup uses `sourceReferenceId + userId`; cross-user returns not found. | No automatic private source access. | None. | Shared forum context strips unavailable private source fields. | Source text, page range, confidence, raw capture/note ids stay owner-scoped. | Search and graph expose source ids only when the viewer owns the source. | AI input resolver requires owned source references. |
| Entity links / backlinks | User can create/list/update/delete manual links among accessible owned entities. | Link creation validates source and target visibility; cross-user private links are rejected. | No automatic private link access. | None. | Forum/shared entity visibility is separately checked. | Optional link sourceReferenceId must be owned. | Graph/backlinks read links by current user id. | Not directly used for AI except as source context if owned. |
| Daily | User can load/regenerate/skip/reflect on own daily items. | Daily repositories are user-scoped. | No automatic private daily access. | None. | Template prompts are labelled as such when no source exists. | Source-backed daily items include owned source refs only. | Daily prompt nodes appear in current user's graph only. | No external AI required; daily prompt does not overwrite content. |
| Forum | Authenticated users can create shared/public/private threads and comments. | Private source/book/concept/project context is stripped or denied for viewers lacking access. | Admin can moderate, hide/lock, resolve reports, and create categories. | Moderator can moderate where `isModerator` permits. | `PUBLIC`/`SHARED` threads are readable; private context fields are withheld if not visible. | Thread sourceReferenceId must be owned at creation; response hides source if viewer cannot access it. | Search includes visible forum threads and hides inaccessible book/source ids. Graph currently includes author-owned forum threads. | Forum-thread AI drafts are draft-only and source-scoped. |
| Search | User searches data they own plus readable catalog/forum records. | Repositories are filtered by user id; private source ids hidden for shared forum results. | No broad private search override. | No broad private search override. | Public/shared books and threads may appear when readable. | `sourceReferenceId` is present only when visible to current user. | Search itself is user-scoped. | Search is not an AI input unless a user later selects owned content. |
| Graph | User graph includes owned notes, captures, quotes, action items, concepts, knowledge objects, projects, links, daily prompts, and readable book roots. | Project/concept/source graph endpoints validate owner; cross-user returns not found/forbidden. | No automatic private graph override. | No automatic private graph override. | Book nodes can represent readable public/shared books; derived private nodes remain user-scoped. | Source nodes are added only when source belongs to current user. | Graph filters operate after current-user graph construction. | Graph does not call AI. |
| Projects | User owns projects, problems, applications, decisions, playtests, knowledge links, and lens reviews. | Project endpoints query by project owner id; cross-user access returns not found. | No automatic private project access. | No project-specific moderation. | Public/shared projects are not implemented as cross-user collaboration. | Project applications preserve owned source references only. | Project search/graph use owner id. | Project AI suggestions are drafts and do not write project records. |
| Analytics | User receives counts computed from own reading/library/notes/captures/quotes/actions/concepts/projects/review data. | Analytics services query by current user id. | No automatic private analytics access. | None. | Not public. | Counts may aggregate source-backed records without exposing foreign source text. | Search/graph not part of analytics output. | Not AI-backed. |
| Review / mastery | User creates source-backed review sessions/items and mastery targets for readable own targets. | Review/mastery target validation requires current-user ownership/access. | No automatic private review access. | None. | Not public. | Review items/mastery can carry owned source references. | Search/graph do not expose review sessions directly. | Daily reflection may update mastery; AI does not. |
| Import/export | User exports own data and imports into own account. | Exports are built from current user id; book export requires current user's library access. | No automatic export of other users' data. | None. | Imported records become private by default unless future explicit sharing is added. | Supplied source refs are remapped to imported/owned records; bad page numbers become `null` with warnings. | Imported records become searchable only for importing user. | Import does not trust AI output. |
| AI suggestions | User can generate/list/edit/accept/reject own suggestions. | Suggestion lookup uses `suggestionId + userId`; source material resolver checks owned source/note/capture. | No automatic private AI suggestion access. | None. | No public AI suggestions. | AI interaction records include sourceReferenceId when used; source is loaded by owner id. | AI suggestions may appear only in current user's UI. | Provider can be mock, disabled, or OpenAI-compatible. Output is validated JSON/draft-only and never overwrites source content. |

## Frontend Security Notes

- Route guards require authentication for app pages and require `role: ADMIN` for `/admin/ontology`.
- Sidebar hides the admin ontology link for non-admin users.
- `renderSafeMarkdown` escapes HTML before applying a small markdown subset; `v-html` usage is constrained to sanitized output.
- Auth logout clears token/user local storage plus private capture/right-rail in-memory state.
- Axios reads the token from local storage on each request, so logout removes the authorization header source for subsequent requests.
- API errors should be surfaced as permission-denied/error states rather than fake success.

## Current Test Coverage

- `SecurityBoundaryIntegrationTest`: unauthenticated JSON errors; cross-user note/capture/quote/action/source/search/graph denial; concept/project/import-export/review/mastery/analytics denial; forum private context stripping; AI suggestion ownership and no-overwrite.
- `AIProviderSecretStatusIntegrationTest`: OpenAI-compatible status hides configured API key.
- `OntologyImportIntegrationTest`: admin-only ontology import.
- `ImportExportIntegrationTest`: export is user-scoped, import preview does not write, duplicate/page warnings.
- `LearningIntegrationTest`: review/mastery/analytics are user-scoped and source-backed.
- `ProjectModeIntegrationTest`: project ownership, source-backed applications, project search/graph/forum/daily integration boundaries.
- `SearchGraphAIIntegrationTest`: search leakage blocked, graph uses real nodes, manual links are owner-scoped, AI draft lifecycle does not overwrite source content.
- `ForumIntegrationTest`: markdown tag stripping, structured forum source handling, own-content and moderation actions.

## Known Residual Risks

- Book catalog admin access currently permits admin management/read behavior for private catalog records; this is documented behavior and should be revisited before multi-tenant production.
- Markdown sanitization is intentionally minimal. It escapes frontend rendering and strips backend forum tags, but a vetted sanitizer library should be considered before broad public beta content ingestion.
- E2E tests run against the test profile/H2, not Docker MySQL.
- Source-reference privacy depends on continued service-level use of `userId` repository methods; future endpoints must follow the same pattern and add contract/security tests.
