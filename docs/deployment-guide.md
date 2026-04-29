# BookOS Deployment Guide

Last updated: 2026-04-28.

## Release Candidate Scope

BookOS is currently deployable as a Spring Boot backend, Vue/Vite frontend, and MySQL database. The system is not yet a hardened production SaaS platform, but it is suitable for a serious local or single-server MVP release candidate when the verification checklist passes.

## Required Environment Variables

Backend:

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `SERVER_PORT`
- `JWT_ISSUER`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `APP_CORS_ALLOWED_ORIGIN`
- `APP_CORS_ALLOWED_ORIGIN_ALT`
- `APP_SEED_ENABLED`
- `FLYWAY_ENABLED`
- `FLYWAY_BASELINE_ON_MIGRATE`

Frontend build:

- `VITE_API_BASE_URL`
- `VITE_API_PROXY_TARGET` for local Vite development only.

Do not commit real secrets. `JWT_SECRET` must be supplied by the runtime environment for any shared deployment.

## Local Database Only

Start MySQL:

```powershell
docker compose up -d mysql
```

Stop MySQL:

```powershell
docker compose down
```

Reset local MySQL data only when data loss is intended:

```powershell
docker compose down -v
docker compose up -d mysql
```

## Full Local Stack With Docker Compose

Copy `.env.example` to `.env` and set a real local `JWT_SECRET`.

```powershell
docker compose -f docker-compose.full.yml up --build
```

Default URLs:

- Frontend: `http://localhost:8081`
- Backend: `http://localhost:8080`
- Backend health: `http://localhost:8080/actuator/health`
- Compatibility health: `http://localhost:8080/api/health`

Stop the stack:

```powershell
docker compose -f docker-compose.full.yml down
```

Reset the full local stack database:

```powershell
docker compose -f docker-compose.full.yml down -v
docker compose -f docker-compose.full.yml up --build
```

## Simple Server Deployment

1. Provision MySQL 8.4 or compatible.
2. Create a database and restricted database user.
3. Set backend environment variables, especially `JWT_SECRET`.
4. Build backend image from `backend/Dockerfile`.
5. Build frontend image from `frontend/Dockerfile`.
6. Put the frontend container or reverse proxy in front of the backend.
7. Expose only the frontend publicly unless direct API access is required.
8. Configure HTTPS at the edge proxy.
9. Keep `APP_SEED_ENABLED=false` unless deliberately running a local/demo environment.

## Migrations

Flyway runs on backend startup. Normal development and deployment use Hibernate `ddl-auto=validate`, so startup fails if migrations and entity mappings diverge.

Do not drop production or shared databases during deployment. For local pre-Flyway databases only, back up first and run once with `FLYWAY_BASELINE_ON_MIGRATE=true`, then set it back to `false`.

## Backups

For MySQL local development:

```powershell
docker exec bookos-mysql mysqldump -ubookos -pbookos bookos > bookos-local-backup.sql
```

Restore to a fresh local database:

```powershell
docker compose down -v
docker compose up -d mysql
Get-Content bookos-local-backup.sql | docker exec -i bookos-mysql mysql -ubookos -pbookos bookos
```

For production, use managed MySQL backups or scheduled `mysqldump`/snapshot automation outside the app repository.

## Debugging

Backend:

- Check `/actuator/health`.
- Check `/api/health`.
- Inspect structured console logs.
- Confirm Flyway migration version in `flyway_schema_history`.
- Confirm CORS origins match the frontend URL.

Frontend:

- Check browser console for global error handler messages.
- Confirm `VITE_API_BASE_URL` points at `/api` or the deployed API URL.
- Confirm the reverse proxy forwards `/api/*` to the backend.

## CI/CD

GitHub Actions runs:

- Backend Java 21 Maven tests.
- Frontend Node `npm ci`, `npm run typecheck`, and `npm run build`.

The CI pipeline uses test profile/in-memory test databases and does not require production secrets.
