# BookOS Public Beta Deployment Runbook

Last updated: 2026-04-29.

Target release: BookOS Public Beta 0.1.

## Preconditions

- Current source reviewed at SHA `1734e5399d5edc6f8fcd683228d9b26d58f1b847` or a later intentional release SHA.
- No generated artifacts, archives, logs, `target`, `dist`, or `node_modules` committed.
- `JWT_SECRET` and database credentials are supplied by environment variables.
- MySQL backup exists before upgrading an existing environment.
- CI backend and frontend jobs pass.
- Manual release QA is complete or explicitly accepted as a beta risk.

## Local MySQL Development Deployment

Start MySQL:

```powershell
docker compose up -d mysql
```

Start backend:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Start frontend:

```powershell
Set-Location frontend
npm install
npm run dev
```

Verify:

- Backend health: `http://localhost:8080/actuator/health`
- Frontend: `http://localhost:5173`
- Register/login works.
- Flyway migrations complete before JPA validation.

## Full-Stack Docker Compose Deployment

Create `.env` from `.env.example` and set at minimum:

```env
JWT_SECRET=<long-random-secret>
MYSQL_PASSWORD=<strong-local-password>
MYSQL_ROOT_PASSWORD=<strong-local-root-password>
APP_CORS_ALLOWED_ORIGIN=http://localhost:8081
```

Start:

```powershell
docker compose -f docker-compose.full.yml up --build -d
```

Verify:

```powershell
docker compose -f docker-compose.full.yml ps
docker compose -f docker-compose.full.yml logs backend --tail=100
docker compose -f docker-compose.full.yml logs frontend --tail=100
```

Endpoints:

- Frontend: `http://localhost:8081`
- Backend health: `http://localhost:8080/actuator/health`
- MySQL: `localhost:3306`

For an isolated release smoke that must not reuse an existing local Compose volume:

```powershell
$env:JWT_SECRET="<long-random-secret>"
$env:MYSQL_PORT="23306"
$env:BACKEND_PORT="28080"
$env:FRONTEND_PORT="28081"
docker compose -p bookos_beta_check -f docker-compose.full.yml up --build -d
curl.exe -fsS http://127.0.0.1:28080/actuator/health
curl.exe -I -fsS http://127.0.0.1:28081
docker compose -p bookos_beta_check -f docker-compose.full.yml down
docker volume rm bookos_beta_check_bookos-mysql-data
```

If startup fails because Flyway reports a non-empty schema without `flyway_schema_history`, the database volume likely predates Flyway. Back it up before choosing a reset or explicit baseline. Do not set `FLYWAY_BASELINE_ON_MIGRATE=true` against shared or production data without a reviewed migration plan.

## Simple Server Deployment Pattern

1. Provision MySQL 8-compatible database.
2. Create a least-privilege database user for BookOS.
3. Configure environment variables from `docs/environment-variables.md`.
4. Build backend image from `backend/Dockerfile`.
5. Build frontend image from `frontend/Dockerfile`.
6. Run backend with Flyway enabled and `spring.jpa.hibernate.ddl-auto=validate`.
7. Run frontend behind HTTPS-capable reverse proxy.
8. Restrict CORS to the public frontend origin.
9. Confirm health checks.
10. Run manual smoke QA.

## Migration Policy

- Flyway runs automatically on backend startup.
- Do not disable Flyway in shared environments.
- Do not use Hibernate create/drop in normal development or deployment.
- Do not run destructive database resets on production/shared databases.
- Use `FLYWAY_BASELINE_ON_MIGRATE=true` only for backed-up legacy local databases that predate Flyway.

## Post-Deployment Verification

- Register a beta test user.
- Log in and confirm `/api/auth/me`.
- Create a book and add it to library.
- Create note/capture/quote/action item.
- Open source reference.
- Open search and graph.
- Generate, edit, accept, and reject a MockAIProvider suggestion.
- Confirm no external AI key is required.
- Review backend logs for migration or authorization errors.

## Release Tag

After all checks pass:

```bash
git tag -a v0.1.0-beta -m "BookOS Public Beta 0.1"
git push origin v0.1.0-beta
```

Do not tag automatically from an unverified local working tree.
