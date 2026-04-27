# BookOS Frontend

Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus.

Milestone 1 frontend scope:

- Login
- Register
- Dashboard
- My Library
- Add Book
- Edit Book
- Book Detail
- Five-Star Books
- Currently Reading
- Anti-Library

## Run

```powershell
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173`.

## Environment

Copy [frontend/.env.example](</D:/【指挥中心】/节操都市/项目/BookOS/frontend/.env.example>) if you want to override the default API base URL.

Default:

- `VITE_API_BASE_URL=/api`
- `VITE_API_PROXY_TARGET=http://localhost:8080`

The Vite config proxies `/api` to `VITE_API_PROXY_TARGET`, which defaults to `http://localhost:8080`.

## Build

```powershell
npm run build
```
