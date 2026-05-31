/**
 * Formats a number with thousands separators using narrow no-break spaces.
 * `12345` → `12 345`. Locale-independent on purpose (we want fixed look).
 */
export function formatInt(n: number | null | undefined): string {
  if (n == null) return '—'
  return n.toLocaleString('en-US').replace(/,/g, ' ')
}

/**
 * Returns a relative time string from an epoch-millis timestamp.
 * `now` → `now`, `35s` → `35s ago`, `5m` → `5m ago`, `3h` → `3h ago`, etc.
 */
export function formatRelative(epochMs: number | null | undefined, now = Date.now()): string {
  if (epochMs == null) return '—'
  const diff = Math.max(0, now - epochMs)
  const s = Math.floor(diff / 1000)
  if (s < 5) return 'ahora mismo'
  if (s < 60) return `hace ${s}s`
  const m = Math.floor(s / 60)
  if (m < 60) return `hace ${m} min`
  const h = Math.floor(m / 60)
  if (h < 24) return `hace ${h} h`
  const d = Math.floor(h / 24)
  if (d < 30) return `hace ${d} d`
  const mo = Math.floor(d / 30)
  if (mo < 12) return `hace ${mo} meses`
  return `hace ${Math.floor(mo / 12)} años`
}

/**
 * Compact duration in seconds → `45s` / `12m` / `3h` / `2d`. Used for "X ago" stamps
 * where the backend already returns a relative-seconds value (no need to compute a diff).
 */
export function formatDuration(seconds: number | null | undefined): string {
  if (seconds == null) return '—'
  if (seconds < 60) return `${seconds}s`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
  if (seconds < 86_400) return `${Math.floor(seconds / 3600)}h`
  return `${Math.floor(seconds / 86_400)}d`
}

/**
 * Short absolute date for tooltips: `2026-05-30 22:18`.
 */
export function formatAbsolute(epochMs: number | null | undefined): string {
  if (epochMs == null) return '—'
  const d = new Date(epochMs)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * `cobblemon:bulbasaur` → `bulbasaur`. Strips namespace.
 */
export function stripNamespace(id: string): string {
  const idx = id.indexOf(':')
  return idx >= 0 ? id.slice(idx + 1) : id
}

/**
 * `cobblemon:bulbasaur` → `Bulbasaur`. Strips namespace and capitalizes.
 */
export function prettySpecies(id: string): string {
  return prettify(id)
}

/**
 * General "clean-up" for backend ids: strip namespace, split on `-` / `_` / space,
 * title-case each piece. `cobblemon:poke_ball` → `Poke Ball`, `naughty` → `Naughty`.
 */
export function prettify(id: string | null | undefined): string {
  if (!id) return ''
  return stripNamespace(id)
    .split(/[-_\s]+/)
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

/**
 * `cobblemon:fire` → `fire`. For Showdown type ids we strip the namespace.
 */
export function stripType(id: string): string {
  return stripNamespace(id).toLowerCase()
}
