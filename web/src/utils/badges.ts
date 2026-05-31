import type { Component } from 'vue'
import {
  Aperture,
  Atom,
  Baby,
  Bone,
  CircleArrowUp,
  Compass,
  Crown,
  Flame,
  Hourglass,
  MapPin,
  Sparkles,
  Star,
  Zap,
} from '@lucide/vue'
import type { SpeciesInfoDto } from '@/types'
import { stripNamespace } from './format'

export type BadgeKind =
  | 'legendary'
  | 'mythical'
  | 'ultra_beast'
  | 'paradox_past'
  | 'paradox_future'
  | 'pseudo_legendary'
  | 'baby'
  | 'starter'
  | 'fossil'
  | 'alolan'
  | 'galarian'
  | 'hisuian'
  | 'paldean'
  | 'mega'
  | 'gmax'
  | 'fully_evolved'

type BadgeDef = {
  /** Spanish display name. */
  name: string
  /** Static Tailwind classes — v4 needs them spelled out, not constructed at runtime. */
  textCls: string
  borderCls: string
  icon: Component
}

/**
 * Visual mapping for Cobblemon rarity / category labels. Order in object reflects priority
 * when picking a single badge to show (first match wins).
 */
export const BADGES: Record<BadgeKind, BadgeDef> = {
  legendary:        { name: 'Legendario',          textCls: 'text-yellow',  borderCls: 'border-yellow/40',  icon: Star },
  mythical:         { name: 'Mítico',              textCls: 'text-magenta', borderCls: 'border-magenta/40', icon: Sparkles },
  ultra_beast:      { name: 'Ultraente',           textCls: 'text-cyan',    borderCls: 'border-cyan/40',    icon: Aperture },
  paradox_past:     { name: 'Paradoja del pasado', textCls: 'text-red',     borderCls: 'border-red/40',     icon: Hourglass },
  paradox_future:   { name: 'Paradoja del futuro', textCls: 'text-blue',    borderCls: 'border-blue/40',    icon: Atom },
  pseudo_legendary: { name: 'Pseudolegendario',    textCls: 'text-blue',    borderCls: 'border-blue/40',    icon: Crown },
  mega:             { name: 'Mega',                textCls: 'text-magenta', borderCls: 'border-magenta/40', icon: Zap },
  gmax:             { name: 'Gigamax',             textCls: 'text-red',     borderCls: 'border-red/40',     icon: Flame },
  alolan:           { name: 'Alolan',              textCls: 'text-orange',  borderCls: 'border-orange/40',  icon: MapPin },
  galarian:         { name: 'Galariano',           textCls: 'text-cyan',    borderCls: 'border-cyan/40',    icon: MapPin },
  hisuian:          { name: 'Hisuiano',            textCls: 'text-green',   borderCls: 'border-green/40',   icon: MapPin },
  paldean:          { name: 'Paldeano',            textCls: 'text-yellow',  borderCls: 'border-yellow/40',  icon: MapPin },
  baby:             { name: 'Bebé',                textCls: 'text-magenta', borderCls: 'border-magenta/40', icon: Baby },
  starter:          { name: 'Inicial',             textCls: 'text-green',   borderCls: 'border-green/40',   icon: Compass },
  fossil:           { name: 'Fósil',               textCls: 'text-orange',  borderCls: 'border-orange/40',  icon: Bone },
  fully_evolved:    { name: 'Completamente evolucionado', textCls: 'text-ink-soft', borderCls: 'border-edge', icon: CircleArrowUp },
}

/** Priority order for picking the primary badge to show on a PokemonCard. */
export const BADGE_PRIORITY: BadgeKind[] = [
  'legendary',
  'mythical',
  'ultra_beast',
  'paradox_past',
  'paradox_future',
  'pseudo_legendary',
  'mega',
  'gmax',
  'alolan',
  'galarian',
  'hisuian',
  'paldean',
  'baby',
  'starter',
  'fossil',
]

/** Subset of badges that the player can "progress through" — shown in progression UI and filter. */
export const FILTERABLE_BADGES: BadgeKind[] = [
  'legendary',
  'mythical',
  'ultra_beast',
  'paradox_past',
  'paradox_future',
  'pseudo_legendary',
  'mega',
  'gmax',
  'alolan',
  'galarian',
  'hisuian',
  'paldean',
  'baby',
  'starter',
  'fossil',
  'fully_evolved',
]

const LABEL_TO_BADGE: Record<string, BadgeKind> = {
  legendary: 'legendary',
  mythical: 'mythical',
  ultra_beast: 'ultra_beast',
  ultrabeast: 'ultra_beast',
  pseudo_legendary: 'pseudo_legendary',
  pseudolegendary: 'pseudo_legendary',
  baby: 'baby',
  starter: 'starter',
  fossil: 'fossil',
  alolan: 'alolan',
  galarian: 'galarian',
  hisuian: 'hisuian',
  paldean: 'paldean',
  mega: 'mega',
  gmax: 'gmax',
}

const badgeCache = new Map<string, ReadonlySet<BadgeKind>>()

/**
 * Compute which badges apply to a species, including the heuristic paradox past/future split.
 * Cobblemon labels every paradox species with just `paradox`; species ids starting with `iron_`
 * are the Violet (future) line, everything else with `paradox` is Scarlet (past).
 * Memoized per speciesId — labels are stable across a session.
 */
export function speciesBadges(speciesId: string, info: SpeciesInfoDto | null): ReadonlySet<BadgeKind> {
  if (!info) return EMPTY_BADGES
  const cached = badgeCache.get(speciesId)
  if (cached) return cached
  const out = new Set<BadgeKind>()
  for (const l of info.labels) {
    const k = LABEL_TO_BADGE[l]
    if (k) out.add(k)
  }
  if (info.labels.includes('paradox')) {
    const path = stripNamespace(speciesId)
    if (path.startsWith('iron')) out.add('paradox_future')
    else out.add('paradox_past')
  }
  if (info.fully_evolved) out.add('fully_evolved')
  badgeCache.set(speciesId, out)
  return out
}

const EMPTY_BADGES: ReadonlySet<BadgeKind> = new Set()

/** First badge from BADGE_PRIORITY present in the species' computed badge set. */
export function primaryBadge(speciesId: string, info: SpeciesInfoDto | null): BadgeKind | null {
  if (!info) return null
  const set = speciesBadges(speciesId, info)
  return BADGE_PRIORITY.find((b) => set.has(b)) ?? null
}
