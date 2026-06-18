# hobby-frontend

Next.js 16 (App Router) + React 19 + TypeScript + Tailwind CSS 4 + TanStack React Query v5.

## Project structure

```
app/
  (auth)/          — unauthenticated pages (login, register)
  (main)/          — authenticated pages behind layout
  layout.tsx       — root layout
  globals.css
components/
  feed/            — feed UI
  modals/          — modal dialogs
  navigation/      — nav bar & sidebar
  notifications/   — notification bell & list
  post/            — post cards & detail
  profile/         — profile page components
  settings/        — settings page components
  ui/              — generic, reusable UI primitives
hooks/             — custom React hooks
queries/           — TanStack Query query/mutation definitions
lib/               — API clients, utilities
types/             — shared TypeScript types
constants/         — string/number constants
```

## Key conventions

- **App Router only** — no `pages/` directory. Route groups `(auth)` and `(main)` carry shared layouts; the `(main)` group renders the navigation shell.
- **Data fetching**: always use TanStack React Query (`useQuery` / `useMutation`) from `queries/`. Do not fetch inside `useEffect`.
- **HTTP client**: Axios (configured in `lib/`). Do not use `fetch` directly.
- **Icons**: Lucide React only. Do not add Heroicons, react-icons, or other icon libraries.
- **Animations**: Framer Motion for transitions. Keep animations short and purposeful.
- **Styling**: Tailwind CSS 4 utility classes. No CSS Modules, no styled-components.
- **Auth state**: cookies via `js-cookie`. The auth token is stored as a cookie; read it with `Cookies.get(...)`.
- **Environment variables**: `NEXT_PUBLIC_*` are baked in at Docker image build time. Never rely on them being injectable at runtime in production.

## Running locally

```bash
cd apps/hobby-frontend
npm install
npm run dev        # starts on http://localhost:19293
```

The dev server proxies API calls to services defined via `.env.local` (copy from `.env.example`).

## Testing

E2E tests live in `tests/` (repo root) and are run with Playwright against a fully running stack. There are no unit/component tests in this service — add Playwright specs for new user-facing features.

## Common patterns

- Query keys live in `queries/` alongside the query functions — keep them co-located.
- API base URLs come from `NEXT_PUBLIC_*` env vars; access them through a constant in `lib/` rather than reading `process.env` inline.
- Use the `modals/` pattern (context + portal) for any overlay that needs to be triggered from deeply nested components.
