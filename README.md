# BookOS

BookOS Milestone 1 is a working full-stack foundation for a game design reading system.

What is included:

- Spring Boot 3.5 backend with Java 21, MySQL config, JWT auth, role model, seed data, and personal library APIs
- Vue 3 + TypeScript + Vite frontend with login, register, dashboard, my library, add/edit book, book detail, five-star, currently-reading, and anti-library pages
- Shared book catalog plus per-user library state
- Local Docker Compose for MySQL

What is intentionally not included in Milestone 1:

- Notes
- AI extraction
- Forum
- Knowledge graph
- Project mode

## Project structure

- `backend`
  - Spring Boot API
- `frontend`
  - Vue application
- `docker-compose.yml`
  - local MySQL service
- `.env.example`
  - shared local environment template

## Seed accounts

- User: `designer@bookos.local` / `Password123!`
- Admin: `admin@bookos.local` / `Admin123!`

## Local setup

1. Copy `.env.example` to `.env`.
2. Start Docker Desktop or another local Docker daemon.
3. Start MySQL.
4. Start the backend.
5. Start the frontend.

## Start MySQL

Make sure the Docker daemon is running before starting MySQL.

```powershell
docker compose up -d mysql
```

## Start backend

This workspace includes a project-local JDK under `tools/jdk21-extracted`.

```powershell
$repo = (Resolve-Path .).Path
$env:JAVA_HOME = Join-Path $repo 'tools\jdk21-extracted\jdk-21.0.10+7'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Set-Location (Join-Path $repo 'backend')
.\mvnw.cmd spring-boot:run
```

Backend URL: `http://localhost:8080`

If port `8080` is already in use on your machine, override `SERVER_PORT` in `.env` and start the backend on a different port such as `18080`.

## Start frontend

```powershell
$repo = (Resolve-Path .).Path
Set-Location (Join-Path $repo 'frontend')
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

If the backend runs on a non-default port, set `VITE_API_PROXY_TARGET` in `.env` to match it, for example `http://localhost:18080`.

## Key APIs in Milestone 1

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
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

## Verification already completed

- Backend tests: `.\mvnw.cmd test`
- Frontend build: `npm run build`

See [backend/README.md](</D:/【指挥中心】/节操都市/项目/BookOS/backend/README.md>) and [frontend/README.md](</D:/【指挥中心】/节操都市/项目/BookOS/frontend/README.md>) for module-specific details.
