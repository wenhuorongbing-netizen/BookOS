# BookOS Database Migrations

BookOS uses Flyway for backend schema management.

## Runtime Policy

- Normal local development runs Flyway first, then Hibernate validates the migrated schema.
- `spring.jpa.hibernate.ddl-auto` is `validate` in the main profile.
- The application must not rely on Hibernate `create`, `create-drop`, or broad `update` behavior for normal startup.
- Production and shared databases must never be dropped as part of routine development or deployment.

## Migration Files

Migration files are stored in:

```text
backend/src/main/resources/db/migration
```

Current migrations:

- `V1__create_bookos_schema.sql`: creates the current schema for users, books, notes, captures, parser-derived records, quotes, action items, source references, entity links, concepts, knowledge objects, daily resurfacing, forum, and Mock AI suggestions.
- `V2__seed_core_roles_and_forum_defaults.sql`: seeds stable roles, default forum categories, and structured forum post templates.

The seed migration does not include copyrighted book passages. Unknown source pages must remain `null`; source confidence must be explicit when source-backed knowledge is added later.

## Running Migrations

Start MySQL:

```powershell
docker compose up -d mysql
```

Start the backend:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Flyway runs automatically before JPA initializes.

## Existing Local Databases

For a local database that already has tables from pre-Flyway Hibernate startup:

1. Back up the database.
2. Start the backend once with `FLYWAY_BASELINE_ON_MIGRATE=true`.
3. Confirm `flyway_schema_history` exists.
4. Switch `FLYWAY_BASELINE_ON_MIGRATE=false`.
5. Restart normally.

This baseline path is for local developer databases only. Do not use it as a substitute for planned production migrations.

## Resetting Local Development Data

Only run this when local data loss is intended:

```powershell
docker compose down -v
docker compose up -d mysql
Set-Location backend
.\mvnw.cmd spring-boot:run
```

The new empty database will be migrated from `V1` onward.

## Test Database Handling

Backend tests use an isolated H2 database per Spring context:

```yaml
spring:
  flyway:
    enabled: true
  jpa:
    hibernate:
      ddl-auto: none
```

The tests still execute the same Flyway migration files. Hibernate schema validation is disabled in the test profile because H2 reports MySQL `LONGTEXT` columns as a different JDBC type than MySQL.

## Adding Future Migrations

1. Add a new migration with the next integer version, for example `V3__add_reading_sessions.sql`.
2. Keep migration names descriptive and lowercase with underscores.
3. Do not edit already-shared migration files.
4. Add indexes and foreign keys explicitly.
5. Make seed data idempotent when practical.
6. Run `.\mvnw.cmd test`.
7. Test startup against an empty local database when schema-level changes are substantial.
