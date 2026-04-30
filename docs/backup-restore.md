# BookOS Backup and Restore Guide

Last updated: 2026-04-30.

BookOS stores durable user data in MySQL. Back up MySQL before release deployments, migration tests, or destructive local resets.

Database backups are the operational recovery mechanism. BookOS import/export is a user-owned data portability workflow, not a full database restore replacement.

## Local Docker Backup

Create a SQL dump from the local MySQL container:

```powershell
docker compose exec mysql mysqldump -u root -p bookos > bookos-backup.sql
```

If using full-stack compose:

```powershell
docker compose -f docker-compose.full.yml exec mysql mysqldump -u root -p bookos > bookos-backup.sql
```

Store backups outside the repository or in an encrypted backup location. Do not commit dumps.

## Local Docker Restore

Stop backend first:

```powershell
docker compose -f docker-compose.full.yml stop backend
```

Restore:

```powershell
Get-Content .\bookos-backup.sql | docker compose -f docker-compose.full.yml exec -T mysql mysql -u root -p bookos
```

Restart backend:

```powershell
docker compose -f docker-compose.full.yml up -d backend
```

Verify:

- `GET /actuator/health`
- Login works.
- Book library and source references load.
- Flyway schema history is present.

## Managed MySQL Backup

For hosted MySQL:

1. Prefer provider-native automated snapshots.
2. Enable point-in-time recovery if available.
3. Run an explicit pre-release snapshot before deploying beta updates.
4. Export periodic logical dumps for portability.
5. Test restore into a non-production database before relying on it.

## Local Reset

Only when local data loss is intended:

```powershell
docker compose down -v
docker compose up -d mysql
Set-Location backend
.\mvnw.cmd spring-boot:run
```

This removes local Docker volume data. Do not use this on shared or production systems.

## Backup Contents

Backups must include:

- Users, roles, profiles, and auth-related records.
- Books, library state, notes, captures, quotes, action items, concepts, knowledge objects.
- Source references and entity links.
- Daily history/reflections.
- Forum categories, threads, comments, reports, bookmarks, likes.
- AI interactions and draft suggestions.
- Game projects and project application records.
- Reading sessions, review sessions, mastery, analytics source records.
- Import/export related persisted records, if any.

## App-Level Import/Export

Use `/import-export` or the import/export API when a user wants to move their own knowledge records between BookOS instances:

- `GET /api/export/json` exports the current user's portable BookOS JSON package.
- `GET /api/export/book/{bookId}/json` and `GET /api/export/book/{bookId}/markdown` export one book the current user has in their library.
- `GET /api/export/quotes/csv`, `GET /api/export/action-items/csv`, and `GET /api/export/concepts/csv` export current-user CSV files.
- `POST /api/import/preview` validates and previews a payload without writing records.
- `POST /api/import/commit` writes supported non-duplicate records only after explicit confirmation.

Portability exports are owner-scoped and intentionally do not include another user's private data. Unknown or malformed imported page numbers remain `null`; BookOS must not invent page numbers. Source references are preserved or remapped where possible, but MVP import does not restore every archival export section yet, such as project applications, authored forum threads, or daily reflections.

## Restore Risks

- Restoring an old backup may remove later user-created data.
- Restoring a backup across incompatible Flyway versions can fail.
- Always pair restore target with the compatible application version or run a tested forward migration.
