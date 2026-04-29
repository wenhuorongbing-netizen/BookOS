# BookOS Rollback Guide

Last updated: 2026-04-29.

This guide covers rollback for BookOS Public Beta 0.1 deployments.

## Rollback Principles

- Preserve user data first.
- Never drop production or shared databases as a rollback shortcut.
- Database migrations are forward-only by default. Roll back application code only after checking whether the database schema remains compatible.
- Keep a verified MySQL backup before every release deployment.

## Immediate Application Rollback

Use when the new backend/frontend version is unhealthy but the database is still intact.

1. Stop new containers/processes.
2. Start the previous known-good backend image or commit.
3. Start the previous known-good frontend image or commit.
4. Keep the same environment variables unless the previous version requires a documented difference.
5. Verify `/actuator/health`.
6. Run the minimum auth/book/capture/source smoke test.

Docker example:

```powershell
docker compose -f docker-compose.full.yml down
# Checkout previous release tag or deploy previous image set.
docker compose -f docker-compose.full.yml up -d
```

## Database Rollback

Use only when a migration or data corruption issue requires restoring data.

1. Stop backend writes.
2. Export current broken-state database for forensic retention.
3. Restore the pre-release backup.
4. Deploy the backend version that matches the restored schema.
5. Verify Flyway history.
6. Run manual smoke QA before reopening access.

Do not restore over a live database while the backend is running.

## AI Safety Rollback

If AI provider behavior is unsafe or unavailable:

```env
AI_ENABLED=false
AI_PROVIDER=mock
OPENAI_COMPATIBLE_API_KEY=
```

Restart backend. Existing suggestions remain stored as drafts and do not overwrite user content.

## Frontend-Only Rollback

Use when UI build is broken but backend is healthy:

1. Rebuild or redeploy previous frontend image.
2. Confirm `VITE_API_BASE_URL` still points to the intended backend.
3. Verify login, search, source drawer, and core navigation.

## Emergency CORS/JWT Misconfiguration

- If login fails after deployment, confirm `JWT_SECRET` is stable across backend restarts.
- If browser API calls fail, confirm `APP_CORS_ALLOWED_ORIGIN` matches the exact frontend origin.
- Do not weaken CORS to `*` for credentialed production deployments.

## Communication Checklist

- Record the failed release SHA.
- Record rollback target SHA/tag.
- Record whether database restore was performed.
- Record user-visible impact.
- Record follow-up P0/P1 fixes.
