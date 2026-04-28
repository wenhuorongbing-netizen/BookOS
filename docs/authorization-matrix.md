# BookOS Authorization Matrix

BookOS treats personal reading data as private by default. Service-layer checks must enforce ownership even when controllers already require authentication.

## Roles

| Role | Scope |
| --- | --- |
| `USER` | Own library, notes, captures, quotes, action items, concepts, knowledge objects, daily items, source references, AI drafts, and own forum content. |
| `MODERATOR` | User forum access plus moderation-level edit/delete on forum threads and comments. |
| `ADMIN` | Moderator access plus admin-only user listing and catalog management where explicitly implemented. |

## Resource Rules

| Module | Read | Create | Update/Delete | Notes |
| --- | --- | --- | --- | --- |
| Auth | Public register/login, authenticated `/me` | Public register | Own session only | Passwords are hashed; JWT is stateless. |
| Users | Own profile | Registration only | Admin user listing only | Private profiles are not exposed through other APIs. |
| Books | Owner, admin, `PUBLIC`, `SHARED` | Authenticated user owns created book | Owner or admin | Adding to library requires the book to be visible to the user. |
| User books | Owner only | Owner only | Owner only | Status, progress, and rating are always user-scoped. |
| Notes / note blocks | Owner only | Requires book in user's library | Owner only | Markdown is stored as user content; frontend renders through escaped safe Markdown. |
| Raw captures | Owner only | Requires book in user's library | Owner only | Conversion requires capture ownership and `INBOX` status. |
| Quotes | Owner only | Requires owned library book and owned source reference if supplied | Owner only | Source references are resolved through `findByIdAndUserId`. |
| Action items | Owner only | Requires owned library book and owned source reference if supplied | Owner only | Complete, reopen, and archive use owner-scoped lookup. |
| Concepts | Owner only | Owner only; source reference must be owned | Owner only | Parsed `[[Concept]]` review never auto-converts without user action. |
| Knowledge objects | Owner only | Owner only; related note/concept/source must be owned | Owner only | Unknown source pages remain `null`. |
| Source references | Owner only | Created by notes/captures/services for owner | Replaced only through owner-owned parent objects | Source text must not cross users. |
| Entity links | Owner only | Source and target must be owned or linkable by current user | Owner only | Private entity IDs must not be accepted blindly. |
| Daily | Owner only | Deterministic per user/date | Owner only | Candidate source references are user-scoped. Template prompts are labeled as templates. |
| Forum | `PUBLIC`/`SHARED` threads visible to authenticated users; private threads only author | Authenticated users | Author, moderator, or admin | Generic related entity IDs are validated; hidden related source/entity IDs are not returned to unauthorized viewers. |
| Search | Current user's visible/owned records only | N/A | N/A | Forum result source IDs and book data are hidden unless viewer can access them. |
| Graph | Current user's visible book plus owned linked data only | N/A | N/A | Graph nodes must be derived from real owner-scoped records. |
| AI suggestions | Owner only | Owner only, MockAIProvider only | Owner only while `DRAFT` | Accept/edit/reject records draft decision only; no source content is overwritten. |

## Error Policy

| Case | Status | Payload |
| --- | --- | --- |
| Missing/invalid auth | `401` | `ApiResponse`-shaped JSON with `success=false`. |
| Forbidden authenticated access | `403` | Generic message: `You are not allowed to access this resource.` |
| Missing owner-scoped resource | `404` | Resource-specific not-found message. |
| Validation failure | `400` | `ValidationErrorResponse` with field errors. |
| Bad enum/body JSON | `400` | Generic invalid request body message. |
| Unexpected error | `500` | Generic server error; no stack trace or sensitive details. |

## Markdown Safety

- User Markdown is never rendered with raw HTML execution.
- Frontend `renderSafeMarkdown` escapes HTML before applying a small allowlist of formatting tags.
- Forum backend also strips raw HTML tags before storing thread/comment Markdown.
- Do not introduce `v-html` for user content unless it passes through `renderSafeMarkdown` or an equivalent sanitizer.
