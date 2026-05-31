# neocobblewatch-web

Operations terminal frontend for the NeoCobbleWatch Cobblemon companion mod. Vue 3 + Vite + Pinia + Tailwind CSS v4 + TypeScript.

## Setup

```bash
cd F:/Coding/NeoCobbleWatch/web
npm install
cp .env.example .env       # adjust VITE_API_BASE if your mod is on another host/port
npm run dev
```

Dev server: `http://127.0.0.1:5173`.

The mod must be running (singleplayer world loaded with NeoCobbleWatch installed, or a dedicated server with the mod). Default backend: `http://127.0.0.1:8080/api/v1`.

If your backend is on a different machine, add the dev origin to `corsAllowedOrigins` in `<minecraft>/config/neocobblewatch.json` and restart the world. Example:

```json
{ "server": { "corsAllowedOrigins": ["http://127.0.0.1:5173"] } }
```

## Scripts

- `npm run dev` — Vite dev server on 5173
- `npm run build` — Production build to `dist/`
- `npm run preview` — Preview the production build on 4173
- `npm run type-check` — `vue-tsc --noEmit`

## Stack

- **Vue 3.5** with `<script setup>` + TypeScript
- **Tailwind CSS 4** — CSS-first config in `src/style.css` `@theme` block, integrated via `@tailwindcss/vite`
- **Pinia** for server state
- **Vue Router** for routes
- **clsx + tailwind-merge** via the `cn()` helper

## Architecture

```
src/
├── style.css             // @import "tailwindcss" + @theme tokens
├── App.vue               // app shell with header + footer
├── main.ts               // app entry
├── router/               // routes
├── api/client.ts         // fetch wrapper for /api/v1
├── stores/server.ts      // global server stats + online roster (Pinia)
├── composables/          // useAsyncResource
├── types/                // API DTO types (barrel from src/types/index.ts)
├── utils/                // cn(), format helpers, pokemon helpers, crafatar URLs
├── components/
│   ├── layout/           // AppHeader
│   ├── ui/               // StatCard, TypePill, TabBar, PaginationBar, EmptyState, LoadingState, ErrorState, ...
│   ├── player/           // PlayerAvatar, PlayerRow
│   └── pokemon/          // PokemonSprite, PokemonCard
└── views/                // HomeView, PlayersListView, PlayerProfileView, TopsView
```

## Conventions

- `<script setup lang="ts">` only, no Options API
- Always `type`, never `interface` (unless extending is required)
- `cn(...)` for conditional/variant class composition — no inline ternaries in template `class`
- No `<style>` blocks in components; styling via Tailwind utilities + tokens
- All semantic colors via the `@theme` tokens (`bg-surface`, `text-muted`, `border-edge`, `text-live`, `bg-type-fire`, etc.) — never hardcode `#hex`
- Shared types in `src/types/`, barrel `src/types/index.ts`
- API field names stay `snake_case` in TS to match the wire format — no remapping layer

## Design system

The aesthetic is **tactical broadcast / Pokemon league operations terminal**: dark, data-dense, sharp corners, mono everywhere except display headings (Anton), phosphor green for live/positive signals, amber for shinies/legendary, type colors for Pokemon types.

| Token | Meaning |
|---|---|
| `--color-base` | Page background (deepest) |
| `--color-surface` | Card / component background |
| `--color-raised` | Hovered / elevated card |
| `--color-overlay` | Inset / inner panel |
| `--color-edge` / `--color-edge-strong` | Borders |
| `--color-text` / `--color-muted` / `--color-faint` / `--color-dim` | Text hierarchy |
| `--color-live` | Phosphor green — online / active / positive trend |
| `--color-amber` | Amber — shiny / legendary / highlight |
| `--color-info` | Cyan — info / metadata |
| `--color-danger` | Red — errors |
| `--color-type-*` | 18 Pokemon type colors |

Fonts: **Anton** (display, condensed sport feel) + **JetBrains Mono** (data and body).

## Routes

| Path | View | Purpose |
|---|---|---|
| `/` | HomeView | Server stats overview + live roster |
| `/players` | PlayersListView | Paginated trainer roster with search |
| `/players/:uuid` | PlayerProfileView | Trainer profile with Stats / Party / PC / Pokédex tabs |
| `/tops/:metric?` | TopsView | Leaderboards (captures, shinies, pokédex, pvp_wins, eggs_hatched, trades) |
