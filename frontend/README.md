# BookOS Frontend

Vue 3 + TypeScript + Vite frontend for the BookOS reading, notes, knowledge, and forum application.

## Implemented UI

- Auth pages: login and register.
- Library pages: my library, add/edit book, book detail, five-star, currently reading, anti-library.
- Knowledge cockpit shell: left sidebar, top global search, current book context, right rail, source drawer.
- Workspaces: notes, capture inbox, quotes, action items, concepts, knowledge objects, forum.
- Book detail dashboard: hero, insight cards, graph/concept/lens preview, quick capture, recent blocks, right rail.
- Global search dialog: Cmd/Ctrl+K, typeahead, type filter, open result/source.
- Mock AI right rail: generate, edit, accept, and reject draft suggestions.

## Environment

Defaults:

- `VITE_API_BASE_URL=/api`
- `VITE_API_PROXY_TARGET=http://localhost:8080`

Copy `frontend/.env.example` if you need local overrides.

## Run

```powershell
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173`.

## Verify

```powershell
npm run typecheck
npm run build
```

## Rules

- Source-backed UI must not invent page numbers.
- AI suggestions must stay drafts until the user accepts or rejects them.
- Accepting a MockAIProvider suggestion must not overwrite user-authored content.
