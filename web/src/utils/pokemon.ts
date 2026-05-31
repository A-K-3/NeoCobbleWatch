import type { SpeciesInfoDto } from '@/types'
import { stripNamespace } from './format'

export const POKEMON_TYPES = [
  'normal', 'fire', 'water', 'electric', 'grass', 'ice',
  'fighting', 'poison', 'ground', 'flying', 'psychic', 'bug',
  'rock', 'ghost', 'dragon', 'dark', 'steel', 'fairy',
] as const

export type PokemonType = (typeof POKEMON_TYPES)[number]

export function isKnownType(t: string): t is PokemonType {
  return (POKEMON_TYPES as readonly string[]).includes(t.toLowerCase())
}

const TYPE_LABELS_ES: Record<PokemonType, string> = {
  normal:   'Normal',
  fire:     'Fuego',
  water:    'Agua',
  electric: 'Eléctrico',
  grass:    'Planta',
  ice:      'Hielo',
  fighting: 'Lucha',
  poison:   'Veneno',
  ground:   'Tierra',
  flying:   'Volador',
  psychic:  'Psíquico',
  bug:      'Bicho',
  rock:     'Roca',
  ghost:    'Fantasma',
  dragon:   'Dragón',
  dark:     'Siniestro',
  steel:    'Acero',
  fairy:    'Hada',
}

/** Spanish display label for a type id. */
export function typeLabel(type: string): string {
  const t = type.toLowerCase()
  return isKnownType(t) ? TYPE_LABELS_ES[t] : type
}

const TYPE_COLORS: Record<PokemonType, string> = {
  normal:   '#a8a892',
  fire:     '#ff7a3a',
  water:    '#4a8bff',
  electric: '#ffd60a',
  grass:    '#4cd864',
  ice:      '#7ae8ec',
  fighting: '#ff4a4a',
  poison:   '#c462e0',
  ground:   '#f0c060',
  flying:   '#a896ff',
  psychic:  '#ff5a9a',
  bug:      '#c8e83a',
  rock:     '#c8a850',
  ghost:    '#9c7aff',
  dragon:   '#8c5aff',
  dark:     '#6a5c70',
  steel:    '#b6c0d0',
  fairy:    '#ff9ad8',
}

/** Direct hex background colour for a type — bypasses CSS variable / Tailwind scanning issues. */
export function typeBgColor(type: string): string {
  const t = type.toLowerCase()
  return isKnownType(t) ? TYPE_COLORS[t] : 'rgba(255,255,255,0.04)'
}

/** Returns the `text-type-<x>` class for a Pokemon type id. */
export function typeTextClass(type: string): string {
  const t = type.toLowerCase()
  return isKnownType(t) ? `text-type-${t}` : 'text-muted'
}

/** Types whose background colour is bright enough to need dark ink text (contrast ≥ 4.5:1). */
const LIGHT_TYPES: ReadonlySet<PokemonType> = new Set([
  'normal', 'electric', 'grass', 'ice', 'ground', 'flying',
  'psychic', 'bug', 'rock', 'ghost', 'steel', 'fairy',
])

/** Contrast-aware text colour to render ON TOP of a type-coloured background. */
export function typeOnBgTextClass(type: string): string {
  const t = type.toLowerCase()
  if (isKnownType(t) && LIGHT_TYPES.has(t)) return 'text-night'
  return 'text-ink'
}

/** Returns [primary] or [primary, secondary] for a species' types. */
export function speciesTypes(info: Pick<SpeciesInfoDto, 'primary_type' | 'secondary_type'> | null | undefined): string[] {
  if (!info) return []
  return info.secondary_type ? [info.primary_type, info.secondary_type] : [info.primary_type]
}

// Cobblemon names regional forms as {region}_{pokemon}; pokemondb uses {pokemon}-{region}.
const REGIONAL_SHORT: Record<string, string> = {
  alolan: 'alolan', galarian: 'galarian', hisuian: 'hisuian', paldean: 'paldean',
}

function toPokedbSlug(name: string): string {
  const h = name.replace(/_/g, '-')
  for (const [prefix, short] of Object.entries(REGIONAL_SHORT)) {
    if (h.startsWith(`${prefix}-`)) return `${h.slice(prefix.length + 1)}-${short}`
  }
  return h
}

/**
 * Sprite URL for a species. Prefers PokeAPI Home renders (no naming-collision issues).
 * Regional forms skip PokeAPI (same dex as base form) and go straight to pokemondb
 * with the reordered slug (e.g. alolan_exeggutor → exeggutor-alola).
 */
export function spriteUrl(speciesId: string, options?: { shiny?: boolean; dex?: number; aspects?: string[] }): string {
  const shiny = options?.shiny ?? false
  const rawName = stripNamespace(speciesId)

  // Aspect-based regional form takes priority (e.g. species=exeggutor + aspects=["alolan"])
  const regionalAspect = (options?.aspects ?? []).find(a => REGIONAL_SHORT[a.toLowerCase()])
  if (regionalAspect) {
    const base = rawName.replace(/_/g, '-')
    const short = REGIONAL_SHORT[regionalAspect.toLowerCase()]
    const variant = shiny ? 'shiny' : 'normal'
    return `https://img.pokemondb.net/sprites/home/${variant}/${base}-${short}.png`
  }

  // Species-id regional prefix (e.g. cobblemon:alolan_exeggutor → exeggutor-alola)
  const slug = toPokedbSlug(rawName)
  const isRegionalId = slug !== rawName.replace(/_/g, '-')
  if (options?.dex && options.dex > 0 && !isRegionalId) {
    const variant = shiny ? 'shiny/' : ''
    return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/${variant}${options.dex}.png`
  }
  const variant = shiny ? 'shiny' : 'normal'
  return `https://img.pokemondb.net/sprites/home/${variant}/${slug}.png`
}

/** Sprite URL for a mega/gmax form from pokemondb (official megas only). */
export function formSpriteUrl(speciesId: string, aspects: string[]): string {
  const base = stripNamespace(speciesId).replace(/_/g, '-')
  const suffix = aspects[0]?.replace(/_/g, '-')
  if (!suffix) return ''
  return `https://img.pokemondb.net/sprites/home/normal/${base}-${suffix}.png`
}

/**
 * Fallback sprite from pokepc.net — covers Legends Z-A megas not yet on pokemondb.
 * Only applies to plain `mega` aspect; ORAS variants (mega_x / mega_y / gmax) already work on pokemondb.
 */
export function formSpriteFallbackUrl(speciesId: string, aspects: string[], dex: number | undefined): string {
  if (aspects[0] !== 'mega' || !dex) return ''
  const dexPadded = String(dex).padStart(4, '0')
  return `https://static.pokepc.net/images/pokemon/home3d-icon-xl/regular/${dexPadded}-mega.webp`
}
