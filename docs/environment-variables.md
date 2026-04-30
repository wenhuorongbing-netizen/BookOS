# BookOS Environment Variables

Last updated: 2026-04-30.

This document is the public beta environment reference. Do not commit `.env` files or real secrets.

## Required For Full-Stack Runtime

| Variable | Required | Default | Scope | Notes |
| --- | --- | --- | --- | --- |
| `JWT_SECRET` | Yes for full-stack compose and production | empty | Backend | Must be a long random secret. `docker-compose.full.yml` refuses to start backend without it. |
| `MYSQL_HOST` | Yes | `localhost` | Backend | Use `mysql` inside full-stack Docker Compose. |
| `MYSQL_PORT` | Yes | `3306` | Backend | MySQL port. |
| `MYSQL_DATABASE` | Yes | `bookos` | Backend/MySQL | Database name. |
| `MYSQL_USER` | Yes | `bookos` | Backend/MySQL | Runtime database user. |
| `MYSQL_PASSWORD` | Yes | `bookos` locally | Backend/MySQL | Use a strong value outside local dev. |

## Backend Runtime

| Variable | Required | Default | Notes |
| --- | --- | --- | --- |
| `SERVER_PORT` | No | `8080` | Backend HTTP port. |
| `JWT_ISSUER` | No | `bookos-local` | JWT issuer claim. |
| `JWT_EXPIRATION_MINUTES` | No | `720` | Token lifetime. |
| `APP_CORS_ALLOWED_ORIGIN` | Yes for deployment | `http://localhost:5173` | Primary frontend origin. |
| `APP_CORS_ALLOWED_ORIGIN_ALT` | No | `http://127.0.0.1:5173` | Alternate local origin. |
| `APP_CORS_ALLOWED_ORIGIN_DOCKER` | No | `http://localhost:8081` | Full-stack Docker frontend origin. |
| `APP_SEED_ENABLED` | No | `false` | Enable only for local/demo data. Do not enable blindly in production. |
| `FLYWAY_ENABLED` | No | `true` | Flyway should remain enabled for normal runtime. |
| `FLYWAY_BASELINE_ON_MIGRATE` | No | `false` | Use only for carefully backed-up legacy local databases. |

## AI Provider

AI is optional. BookOS must work without external AI keys.

| Variable | Required | Default | Notes |
| --- | --- | --- | --- |
| `AI_ENABLED` | No | `true` | Allows AI suggestion APIs. Disable with `false` for hard-off mode. |
| `AI_PROVIDER` | No | `mock` | Supported beta values: `mock`, `openai-compatible`. |
| `AI_MAX_INPUT_CHARS` | No | `12000` | Caps content sent to the provider. |
| `OPENAI_COMPATIBLE_BASE_URL` | Only for external provider | empty | Example: provider base URL. Do not include secrets. |
| `OPENAI_COMPATIBLE_API_KEY` | Only for external provider | empty | Secret. Never commit. If missing, provider fails safe. |
| `OPENAI_COMPATIBLE_MODEL` | No | `gpt-4.1-mini` | Model name passed to compatible endpoint. |
| `OPENAI_COMPATIBLE_TIMEOUT_SECONDS` | No | `30` | Provider timeout. |

AI safety rules:

- MockAIProvider remains the safe local default.
- External provider calls are never required for tests.
- AI suggestions are drafts only.
- Accepting an AI suggestion records the acceptance and does not overwrite notes, quotes, concepts, action items, projects, or forum content automatically.
- Source references used as AI input must be recorded.
- Structured provider output must be valid JSON whose `type` matches the requested suggestion task.
- Detailed provider safety rules are documented in [ai-safety.md](ai-safety.md).

## Frontend

| Variable | Required | Default | Notes |
| --- | --- | --- | --- |
| `VITE_API_BASE_URL` | No | `/api` | Runtime API base used by frontend build. |
| `VITE_API_PROXY_TARGET` | No | `http://localhost:8080` | Vite dev proxy target. |

## Docker Compose Variables

| Variable | Required | Default | Notes |
| --- | --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | Local only | `root` | Local Docker MySQL root password. Use a strong value if exposed. |
| `BACKEND_PORT` | No | `8080` | Host port mapped to backend. |
| `FRONTEND_PORT` | No | `8081` | Host port mapped to frontend Nginx. |

## Secret Handling

- Keep real values in `.env`, platform secrets, or a secret manager.
- `.env` is ignored by git.
- Do not paste API keys into README, docs, CI YAML, Dockerfiles, or compose files.
- Rotate `JWT_SECRET` and provider keys if they are exposed.
