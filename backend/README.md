# BookOS Backend

Spring Boot 3.5 backend for BookOS using Java 21, Spring Security, Spring Data JPA, Hibernate, Bean Validation, Flyway, Maven, and MySQL.

## Implemented Domains

- `auth`, `user`, `security`: register, login, current user, JWT, roles.
- `book`: catalog and personal library state.
- `note`, `capture`, `parser`: notes, note blocks, quick capture, deterministic emoji/page/tag/concept parsing.
- `source`, `link`: source references and entity backlinks.
- `quote`, `action`: source-backed quotes and action items.
- `knowledge`: concepts and minimal knowledge objects.
- `daily`: deterministic daily sentence and design prompt resurfacing.
- `forum`: structured categories, threads, comments, likes, bookmarks, reports, templates.
- `search`: global search across owned/visible records.
- `graph`: real-data graph exploration endpoints.
- `ai`: draft-only suggestions through MockAIProvider by default or optional OpenAI-compatible provider configuration.
- `admin`: local/dev ontology seed import and dry-run review.

## Environment

Use root `.env.example` or `backend/.env.example` as a starting point. Do not commit secrets.

Seed accounts are disabled by default. Set `APP_SEED_ENABLED=true` only for local development.

AI defaults to `AI_PROVIDER=mock` and requires no external key. To use an OpenAI-compatible provider, set
`AI_PROVIDER=openai-compatible`, `OPENAI_COMPATIBLE_BASE_URL`, `OPENAI_COMPATIBLE_API_KEY`,
`OPENAI_COMPATIBLE_MODEL`, and optionally `OPENAI_COMPATIBLE_TIMEOUT_SECONDS`. Provider status is available at
`GET /api/ai/status` and never exposes the API key.

Flyway is enabled by default through `FLYWAY_ENABLED=true`. Keep `FLYWAY_BASELINE_ON_MIGRATE=false` for new databases. For an existing local database created before Flyway was introduced, back it up and start once with `FLYWAY_BASELINE_ON_MIGRATE=true`, then switch it back to `false`.

## Database Migrations

Migration files live in `src/main/resources/db/migration`.

- `V1__create_bookos_schema.sql` creates the current BookOS schema.
- `V2__seed_core_roles_and_forum_defaults.sql` inserts stable roles, forum categories, and structured post templates.
- `V3__add_ontology_seed_metadata.sql` adds ontology layer, source confidence, creator, and concept tag metadata.
- `V4__forum_structured_moderation.sql` adds structured forum moderation and report status fields.
- Normal development uses Hibernate `ddl-auto=validate`; startup fails if entity mappings and migrations diverge.
- Tests use isolated H2 databases and run the same Flyway migrations. Test Hibernate DDL validation is disabled because H2 reports MySQL `LONGTEXT` columns as a different JDBC type.

Run migrations by starting the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Reset a local development database only when data loss is intended:

```powershell
Set-Location ..
docker compose down -v
docker compose up -d mysql
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Add future migrations by creating the next versioned SQL file, for example `V3__add_reading_sessions.sql`. Do not edit migration files that may already have run in another developer or production database.

## Run

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

## Test

Windows:

```powershell
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

## Rules

- Unknown source page numbers remain `null`.
- Seed data must not include copyrighted book passages.
- AI suggestions are drafts and never overwrite user content automatically.
- MockAIProvider is local/deterministic and performs no external AI calls.
- Optional OpenAI-compatible AI is disabled/fail-safe unless environment configuration is complete.
- Private user content must remain scoped to the authenticated owner.

## Health And Logs

Health endpoints:

- `GET /api/health`
- `GET /actuator/health`

Only health/info Actuator endpoints are exposed. Console logs use a simple key-value pattern suitable for local and container log collection.

## Docker

Build the backend image from the repository root:

```powershell
docker build -t bookos-backend .\backend
```

Run as part of the full local stack:

```powershell
docker compose -f ..\docker-compose.full.yml up --build backend
```
