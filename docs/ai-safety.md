# BookOS AI Safety

Last updated: 2026-04-30.

BookOS AI assistance is optional and draft-only. The application must remain usable with no external AI key.

## Provider Modes

| Mode | Configuration | External call | Intended use |
| --- | --- | --- | --- |
| Disabled | `AI_ENABLED=false` | No | Hard-off deployments. AI suggestion creation returns a safe unavailable-provider error. |
| Mock | `AI_ENABLED=true`, `AI_PROVIDER=mock` | No | Default local development, tests, demos, and offline use. |
| OpenAI-compatible | `AI_ENABLED=true`, `AI_PROVIDER=openai-compatible` plus base URL, model, and API key | Yes | Optional deployment mode only. Tests must use mocked HTTP servers, never real external providers. |

## Environment Variables

| Variable | Required | Notes |
| --- | --- | --- |
| `AI_ENABLED` | No | Defaults to `true`. Set `false` to disable provider generation. |
| `AI_PROVIDER` | No | Defaults to `mock`. Supported values are `mock` and `openai-compatible`. Unknown values fall back to mock. |
| `AI_MAX_INPUT_CHARS` | No | Caps source text passed to the provider. |
| `OPENAI_COMPATIBLE_BASE_URL` | Only for external provider | Provider base URL. Must not contain secrets. |
| `OPENAI_COMPATIBLE_API_KEY` | Only for external provider | Secret. Never commit, log, or expose through provider status. |
| `OPENAI_COMPATIBLE_MODEL` | Only for external provider | Model name sent to `/chat/completions`. |
| `OPENAI_COMPATIBLE_TIMEOUT_SECONDS` | No | Bounded request timeout. |

## Draft-Only Rule

- AI output is stored as `AISuggestion` with status `DRAFT`.
- Accepting a suggestion changes only the suggestion status and timestamp.
- Editing a suggestion changes only the draft text or draft JSON on that suggestion.
- Rejecting a suggestion changes only the suggestion status and timestamp.
- AI suggestions must never overwrite notes, captures, quotes, action items, concepts, knowledge objects, projects, forum posts, or source references automatically.

## Source Scope

- AI input may use only content the authenticated user owns or can access.
- Source references are resolved by `sourceReferenceId + userId`.
- Note and capture source inputs are resolved by current user ownership.
- The stored `AIInteraction.inputJson` records metadata such as provider, book id, source reference id, and max input chars. It does not store API keys.
- Source references used as input are preserved on `AISuggestion.sourceReferenceId`.

## JSON Validation

All provider output is validated before an `AISuggestion` is saved.

Validation rules:

- Draft text and draft JSON are required.
- JSON must parse to an object.
- `provider` is required.
- `type` is required and must match the requested `AISuggestionType`.
- Task-specific fields are required:
  - `NOTE_SUMMARY`: `summary`
  - `EXTRACT_ACTIONS`: non-empty `actions[].title`
  - `EXTRACT_CONCEPTS`: non-empty `concepts[]`
  - `SUGGEST_DESIGN_LENSES`: non-empty `lenses[].name`
  - `SUGGEST_PROJECT_APPLICATIONS`: non-empty `applications[].title`
  - `FORUM_THREAD_DRAFT`: `title` and `bodyMarkdown`
- Page fields must be `null` or exactly match the source reference page values.
- JSON containing overwrite intent fields such as `overwrite`, `overwriteTargetId`, or `targetEntityId` is rejected.

## Secret Handling

- `GET /api/ai/status` exposes provider availability and mode only; it must not expose API keys, authorization headers, or raw secret variable names.
- API keys are read only from environment-backed configuration.
- Do not commit `.env` files, real provider keys, logs containing provider requests, or provider response dumps.
- Do not log full user source content by default.

## Endpoints

- `GET /api/ai/status`
- `POST /api/ai/suggestions/note-summary`
- `POST /api/ai/suggestions/extract-actions`
- `POST /api/ai/suggestions/extract-concepts`
- `POST /api/ai/suggestions/design-lenses`
- `POST /api/ai/suggestions/project-applications`
- `POST /api/ai/suggestions/forum-thread`
- `GET /api/ai/suggestions`
- `PUT /api/ai/suggestions/{id}/accept`
- `PUT /api/ai/suggestions/{id}/reject`
- `PUT /api/ai/suggestions/{id}/edit`

## Testing Without External AI

Required test coverage:

- Mock provider generation.
- Missing OpenAI-compatible key returns a safe unavailable-provider error.
- OpenAI-compatible provider uses a mocked local HTTP server only.
- Provider status does not expose secrets.
- Malformed JSON is rejected.
- Wrong suggestion type is rejected.
- Invented page numbers are rejected.
- Overwrite-target JSON is rejected.
- Draft accept/edit/reject does not overwrite source content.
- Cross-user suggestion access is denied.

