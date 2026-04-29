# BookOS API Endpoint Inventory

Last updated: 2026-04-28.

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Health:

- `GET /api/health`
- `GET /actuator/health`

Books and user library:

- `GET /api/books`
- `POST /api/books`
- `GET /api/books/{id}`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`
- `POST /api/books/{id}/add-to-library`
- `GET /api/user-books`
- `PUT /api/user-books/{id}/status`
- `PUT /api/user-books/{id}/progress`
- `PUT /api/user-books/{id}/rating`
- `GET /api/user-books/currently-reading`
- `GET /api/user-books/five-star`
- `GET /api/user-books/anti-library`

Notes, blocks, parser, captures:

- `GET /api/books/{bookId}/notes`
- `POST /api/books/{bookId}/notes`
- `GET /api/notes/{id}`
- `PUT /api/notes/{id}`
- `DELETE /api/notes/{id}`
- `POST /api/notes/{noteId}/blocks`
- `PUT /api/note-blocks/{id}`
- `DELETE /api/note-blocks/{id}`
- `POST /api/parser/preview`
- `POST /api/captures`
- `GET /api/captures/inbox`
- `GET /api/captures/{id}`
- `PUT /api/captures/{id}`
- `PUT /api/captures/{id}/archive`
- `POST /api/captures/{id}/convert`
- `POST /api/captures/{id}/convert/quote`
- `POST /api/captures/{id}/convert/action-item`
- `POST /api/captures/{id}/review/concepts`

Quotes:

- `GET /api/quotes`
- `POST /api/quotes`
- `GET /api/quotes/{id}`
- `PUT /api/quotes/{id}`
- `DELETE /api/quotes/{id}`

Action items:

- `GET /api/action-items`
- `POST /api/action-items`
- `GET /api/action-items/{id}`
- `PUT /api/action-items/{id}`
- `PUT /api/action-items/{id}/complete`
- `PUT /api/action-items/{id}/reopen`
- `DELETE /api/action-items/{id}`

Source references and links:

- `GET /api/source-references`
- `GET /api/source-references/{id}`
- `GET /api/entity-links`
- `POST /api/entity-links`
- `DELETE /api/entity-links/{id}`
- `GET /api/backlinks`

Concepts and knowledge objects:

- `GET /api/concepts`
- `POST /api/concepts`
- `GET /api/concepts/{id}`
- `PUT /api/concepts/{id}`
- `DELETE /api/concepts/{id}`
- `GET /api/books/{bookId}/concepts`
- `GET /api/knowledge-objects`
- `POST /api/knowledge-objects`
- `GET /api/knowledge-objects/{id}`
- `PUT /api/knowledge-objects/{id}`
- `DELETE /api/knowledge-objects/{id}`

Admin ontology import:

- `GET /api/admin/ontology/default`
- `POST /api/admin/ontology/import/default`
- `POST /api/admin/ontology/import`

Daily:

- `GET /api/daily/today`
- `POST /api/daily/regenerate`
- `POST /api/daily/skip`
- `POST /api/daily/reflections`
- `GET /api/daily/history`
- `POST /api/daily/create-prototype-task`

Forum:

- `GET /api/forum/categories`
- `POST /api/forum/categories`
- `GET /api/forum/templates`
- `GET /api/forum/threads`
- `POST /api/forum/threads`
- `GET /api/forum/threads/{id}`
- `PUT /api/forum/threads/{id}`
- `DELETE /api/forum/threads/{id}`
- `PUT /api/forum/threads/{id}/moderation`
- `GET /api/forum/threads/{id}/comments`
- `POST /api/forum/threads/{id}/comments`
- `PUT /api/forum/comments/{id}`
- `DELETE /api/forum/comments/{id}`
- `POST /api/forum/threads/{id}/bookmark`
- `DELETE /api/forum/threads/{id}/bookmark`
- `POST /api/forum/threads/{id}/like`
- `DELETE /api/forum/threads/{id}/like`
- `POST /api/forum/threads/{id}/report`
- `GET /api/forum/reports`
- `PUT /api/forum/reports/{id}/resolve`

Search and graph:

- `GET /api/search?q=&type=&bookId=`
- `GET /api/graph`
- `GET /api/graph/book/{bookId}`
- `GET /api/graph/concept/{conceptId}`

Mock AI:

- `POST /api/ai/suggestions/note-summary`
- `POST /api/ai/suggestions/extract-actions`
- `POST /api/ai/suggestions/extract-concepts`
- `GET /api/ai/suggestions`
- `PUT /api/ai/suggestions/{id}/accept`
- `PUT /api/ai/suggestions/{id}/reject`
- `PUT /api/ai/suggestions/{id}/edit`

Endpoint rules:

- Private user content is owner-scoped.
- Public/shared catalog behavior must be explicit.
- Source references must not invent page numbers; unknown pages stay `null`.
- Mock AI suggestions are drafts and do not overwrite user content.
