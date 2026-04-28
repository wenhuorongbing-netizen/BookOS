# BookOS

BookOS is a full-stack reading record system, book notes library, source-referenced knowledge OS, game design knowledge cockpit, and structured forum.

Current repository status: the app has moved beyond the original Milestone 1 foundation. It now includes authentication, personal book library, notes, quick capture, deterministic parser, source references, quotes, action items, concept review, knowledge objects, daily resurfacing, structured forum, global search, real graph preview endpoints, and draft-only MockAIProvider suggestions.

## Current Milestone Status

Implemented:

- Java 21 + Spring Boot backend with JWT authentication, role checks, validation, JPA/Hibernate, MySQL configuration, and H2-backed tests.
- Vue 3 + TypeScript + Vite frontend with Pinia, Vue Router, Element Plus, responsive BookOS cockpit shell, right rail, and design-system primitives.
- Auth, books, user library, notes, note blocks, captures, parser, source references, backlinks/entity links, quotes, action items, concepts, knowledge objects, daily quote/prompt resurfacing, structured forum, search, graph preview, and Mock AI draft APIs.
- Frontend workspaces for library, notes, captures, quotes, action items, concepts, knowledge objects, forum, book detail cockpit, global search, and source drawer.

Known limitations:

- Graph preview is a lightweight real-data preview, not a full graph editor or advanced visualization workspace.
- MockAIProvider is deterministic and local-only. It does not call external AI providers.
- AI suggestions are drafts. Accepting a suggestion records the decision but does not overwrite notes, quotes, concepts, action items, or user-authored content.
- Forum moderation is minimal; automated moderation and realtime notifications are not implemented.
- Project mode and advanced game-project application workflows remain future work.

## Product Rules

- Source references must preserve real source context and must not invent page numbers. Unknown page numbers stay `null`.
- Private user content must not leak through public or cross-user APIs.
- AI output must never overwrite user content automatically.
- External AI keys must not be required for local development and must not be committed.

## Project Structure

- `backend`: Spring Boot REST API.
- `frontend`: Vue application.
- `docs`: product specs, UI review, responsive/accessibility notes, and prompt traceability.
- `docker-compose.yml`: local MySQL service.
- `.env.example`: shared local environment template.

## Local Setup

Copy the root `.env.example` to `.env` if you want to customize local ports or credentials.

Start local MySQL:

```powershell
docker compose up -d mysql
```

Stop local MySQL:

```powershell
docker compose down
```

## Backend

The backend uses Spring Boot 3.5, Java 21, Maven, Spring Security, Spring Data JPA, Hibernate, Bean Validation, Flyway, and MySQL.

Database migrations:

- Flyway runs automatically on backend startup from `backend/src/main/resources/db/migration`.
- Normal local development uses `spring.jpa.hibernate.ddl-auto=validate`, so Hibernate validates the schema but does not create, update, or drop tables.
- Empty local MySQL databases are migrated automatically when the backend starts.
- Existing pre-Flyway local databases must be backed up first. Then start once with `FLYWAY_BASELINE_ON_MIGRATE=true` to create Flyway history without dropping data, and return it to `false` afterward.
- Never use destructive resets on production or shared data.

Start backend on Windows:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Start backend on macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run
```

Run backend tests on Windows:

```powershell
Set-Location backend
.\mvnw.cmd test
```

Run backend tests on macOS/Linux:

```bash
cd backend
./mvnw test
```

Backend URL defaults to `http://localhost:8080`.

Reset local MySQL data only when you intentionally want a fresh development database:

```powershell
docker compose down -v
docker compose up -d mysql
Set-Location backend
.\mvnw.cmd spring-boot:run
```

The test profile uses isolated in-memory H2 databases and applies the same Flyway migration files. Hibernate DDL validation is disabled only in tests because H2 reports `LONGTEXT` columns differently than MySQL.

To add a future schema change, create the next migration file using the pattern `backend/src/main/resources/db/migration/V{number}__short_description.sql`. Do not edit already-applied migrations after they have been shared.

## Frontend

The frontend uses Vue 3, TypeScript, Vite, Pinia, Vue Router, Element Plus, and Axios.

Install dependencies:

```powershell
Set-Location frontend
npm install
```

Start frontend:

```powershell
npm run dev
```

Run typecheck:

```powershell
npm run typecheck
```

Run production build:

```powershell
npm run build
```

Frontend URL defaults to `http://localhost:5173`. Vite proxies `/api` to `VITE_API_PROXY_TARGET`, which defaults to `http://localhost:8080`.

## Optional Local Seed Accounts

Seed accounts are disabled by default. Set `APP_SEED_ENABLED=true` only in local development if you want demo data.

- User: `designer@bookos.local` / `Password123!`
- Admin: `admin@bookos.local` / `Admin123!`

## API Overview

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Books and library:

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

Notes, captures, parser, and sources:

- `GET /api/books/{bookId}/notes`
- `POST /api/books/{bookId}/notes`
- `GET /api/notes/{id}`
- `PUT /api/notes/{id}`
- `DELETE /api/notes/{id}`
- `POST /api/notes/{noteId}/blocks`
- `PUT /api/note-blocks/{id}`
- `DELETE /api/note-blocks/{id}`
- `POST /api/captures`
- `GET /api/captures/inbox`
- `GET /api/captures/{id}`
- `PUT /api/captures/{id}`
- `PUT /api/captures/{id}/archive`
- `POST /api/captures/{id}/convert`
- `POST /api/captures/{id}/review/concepts`
- `POST /api/parser/preview`
- `GET /api/source-references`
- `GET /api/source-references/{id}`
- `GET /api/entity-links`
- `POST /api/entity-links`
- `DELETE /api/entity-links/{id}`

Quotes and action items:

- `GET /api/quotes`
- `POST /api/quotes`
- `GET /api/quotes/{id}`
- `PUT /api/quotes/{id}`
- `DELETE /api/quotes/{id}`
- `POST /api/captures/{id}/convert/quote`
- `GET /api/action-items`
- `POST /api/action-items`
- `GET /api/action-items/{id}`
- `PUT /api/action-items/{id}`
- `PUT /api/action-items/{id}/complete`
- `PUT /api/action-items/{id}/reopen`
- `DELETE /api/action-items/{id}`
- `POST /api/captures/{id}/convert/action-item`

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
- `GET /api/forum/threads/{id}/comments`
- `POST /api/forum/threads/{id}/comments`
- `PUT /api/forum/comments/{id}`
- `DELETE /api/forum/comments/{id}`
- `POST /api/forum/threads/{id}/bookmark`
- `DELETE /api/forum/threads/{id}/bookmark`
- `POST /api/forum/threads/{id}/like`
- `DELETE /api/forum/threads/{id}/like`
- `POST /api/forum/threads/{id}/report`

Search, graph, and Mock AI:

- `GET /api/search?q=&type=&bookId=`
- `GET /api/graph/book/{bookId}`
- `GET /api/graph/concept/{conceptId}`
- `POST /api/ai/suggestions/note-summary`
- `POST /api/ai/suggestions/extract-actions`
- `POST /api/ai/suggestions/extract-concepts`
- `GET /api/ai/suggestions`
- `PUT /api/ai/suggestions/{id}/accept`
- `PUT /api/ai/suggestions/{id}/reject`
- `PUT /api/ai/suggestions/{id}/edit`

## Verification

Current acceptance verification commands:

```powershell
Set-Location backend
.\mvnw.cmd test
Set-Location ..\frontend
npm install
npm run typecheck
npm run build
```

No `.7z` archives, `backend.zip`, logs, `.out`, `.err`, backend `target`, frontend `dist`, frontend `node_modules`, or local JDK archives should be committed.

Additional database migration details are documented in `docs/database-migrations.md`.
