<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { api } from '@/api/client'
import { useAsyncResource } from '@/composables/useAsyncResource'
import LoadingState from '@/components/ui/LoadingState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import TabBar from '@/components/ui/TabBar.vue'
import ToggleChip from '@/components/ui/ToggleChip.vue'
import SearchInput from '@/components/ui/SearchInput.vue'
import SortSelector from '@/components/ui/SortSelector.vue'
import MultiSelect from '@/components/ui/MultiSelect.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import StatCard from '@/components/ui/StatCard.vue'
import PlayerAvatar from '@/components/player/PlayerAvatar.vue'
import BadgeCase from '@/components/player/BadgeCase.vue'
import Modal from '@/components/ui/Modal.vue'
import PokemonCard from '@/components/pokemon/PokemonCard.vue'
import PokemonGridCell from '@/components/pokemon/PokemonGridCell.vue'
import SpeciesDetailPanel from '@/components/pokemon/SpeciesDetailPanel.vue'
import TypePill from '@/components/ui/TypePill.vue'
import { formatAbsolute, formatDuration, formatInt, formatRelative, prettySpecies, stripNamespace } from '@/utils/format'
import { ApiError } from '@/api/client'
import type { PokedexKnowledge, PcSlotDto } from '@/types'
import { POKEMON_TYPES, typeBgColor, typeLabel, typeOnBgTextClass } from '@/utils/pokemon'
import { BADGES, BADGE_PRIORITY, FILTERABLE_BADGES, speciesBadges, type BadgeKind } from '@/utils/badges'
import { GYM_REGIONS } from '@/utils/gym-badges'
import { evoReadiness, type EvoReadiness } from '@/utils/evolution'
import { useSpeciesStore } from '@/stores/species'
import { cn } from '@/utils/cn'
import { ChevronLeft, Check, Copy, Egg, Fish, Footprints, Lock, Shield, Skull, Sparkles, Star, Sword, Swords, Target, TrendingUp, Trophy, Waves, Wind, Zap } from '@lucide/vue'

type Props = { uuid: string }
const props = defineProps<Props>()

type Tab = 'stats' | 'party' | 'pc' | 'pokedex' | 'minecraft'
const VALID_TABS: readonly Tab[] = ['stats', 'party', 'pc', 'pokedex', 'minecraft']
const router = useRouter()
const route = useRoute()
const tab = computed<Tab>({
  get: () => {
    const t = route.query.tab
    return (typeof t === 'string' && (VALID_TABS as readonly string[]).includes(t)) ? t as Tab : 'stats'
  },
  set: (value: Tab) => { void router.push({ query: { ...route.query, tab: value } }) },
})

const profile = useAsyncResource(() => api.player(props.uuid))

const activity = useAsyncResource(() => api.activity(props.uuid))
const advancements = useAsyncResource(() => api.advancements(props.uuid), { immediate: false })
const economy = useAsyncResource(() => api.economy(props.uuid))
const party = useAsyncResource(() => api.party(props.uuid))
const pokedex = useAsyncResource(() => api.pokedex(props.uuid))

const pcMeta = useAsyncResource(() => api.pcPaginated(props.uuid, 1, 0))
const pcAll = useAsyncResource(() => api.pcPaginated(props.uuid, 5000, 0), { immediate: false })

const allPcSlots = computed(() => pcAll.data.value?.data ?? [])

type AugmentedSlot = PcSlotDto & { inParty: boolean }

const allSlotsForPcView = computed<AugmentedSlot[]>(() => {
  const partySlots: AugmentedSlot[] = (party.data.value?.slots ?? []).map((s, i) => ({
    box_index: -1,
    slot_index: i,
    pokemon: s.pokemon,
    inParty: true,
  }))
  const pcSlots: AugmentedSlot[] = allPcSlots.value.map(s => ({ ...s, inParty: false }))
  return [...partySlots, ...pcSlots]
})

type PcSort = 'box' | 'dex' | 'name' | 'level_desc' | 'level_asc'
const PC_SORT_OPTIONS = [
  { value: 'box' as const, label: 'Caja + slot' },
  { value: 'dex' as const, label: 'Nº nacional' },
  { value: 'name' as const, label: 'Nombre A-Z' },
  { value: 'level_desc' as const, label: 'Nivel ↓' },
  { value: 'level_asc' as const, label: 'Nivel ↑' },
]

const pcQuery = ref('')
const pcShinyOnly = ref(false)
const pcFeOnly = ref(false)
const pcEvoFilter = ref<EvoReadiness | null>(null)
const pcDuplicatesOnly = ref(false)
const pcSelectedTypes = ref<string[]>([])
const pcSelectedRarities = ref<BadgeKind[]>([])
const pcSelectedGens = ref<number[]>([])
const pcSort = ref<PcSort>('box')
const selectedPokemonUuid = ref<string | null>(null)

const pcHasActiveFilters = computed(() =>
  pcQuery.value.trim().length > 0 ||
  pcShinyOnly.value ||
  pcFeOnly.value ||
  pcEvoFilter.value !== null ||
  pcDuplicatesOnly.value ||
  pcSelectedTypes.value.length > 0 ||
  pcSelectedRarities.value.length > 0 ||
  pcSelectedGens.value.length > 0,
)

function togglePcEvoFilter(filter: EvoReadiness) {
  pcEvoFilter.value = pcEvoFilter.value === filter ? null : filter
}

function pcClearFilters() {
  pcQuery.value = ''
  pcShinyOnly.value = false
  pcFeOnly.value = false
  pcEvoFilter.value = null
  pcDuplicatesOnly.value = false
  pcSelectedTypes.value = []
  pcSelectedRarities.value = []
  pcSelectedGens.value = []
  pcSort.value = 'box'
}

const pcFilteredSlots = computed(() => {
  const slots = allSlotsForPcView.value
  const query = pcQuery.value.trim().toLowerCase()
  const types = pcSelectedTypes.value
  const rarities = pcSelectedRarities.value
  const gens = pcSelectedGens.value
  const shiny = pcShinyOnly.value
  const fe = pcFeOnly.value
  const filtered = slots.filter(s => {
    const p = s.pokemon
    if (shiny && !p.shiny) return false
    if (query) {
      const slug = stripNamespace(p.species_id).toLowerCase()
      const nick = p.nickname?.toLowerCase() ?? ''
      if (!slug.includes(query) && !nick.includes(query)) return false
    }
    const info = speciesStore.info(p.species_id)
    if (fe && !info?.fully_evolved) return false
    if (gens.length && info?.generation !== undefined && !gens.includes(info.generation)) return false
    if (types.length) {
      if (!info) return false
      const has = types.includes(info.primary_type) || (info.secondary_type !== null && types.includes(info.secondary_type))
      if (!has) return false
    }
    if (rarities.length) {
      if (!info) return false
      const badges = speciesBadges(p.species_id, info)
      if (!rarities.some(r => badges.has(r))) return false
    }
    if (pcDuplicatesOnly.value && !duplicateSpeciesIds.value.has(p.species_id)) return false
    if (pcEvoFilter.value !== null) {
      const evos = speciesStore.detail(p.species_id)?.evolutions ?? []
      if (evoReadiness(p, evos) !== pcEvoFilter.value) return false
    }
    return true
  })
  switch (pcSort.value) {
    case 'dex':
      return [...filtered].sort((a, b) => (speciesStore.info(a.pokemon.species_id)?.national_dex ?? 99999) - (speciesStore.info(b.pokemon.species_id)?.national_dex ?? 99999))
    case 'name':
      return [...filtered].sort((a, b) => stripNamespace(a.pokemon.species_id).localeCompare(stripNamespace(b.pokemon.species_id)))
    case 'level_desc':
      return [...filtered].sort((a, b) => b.pokemon.level - a.pokemon.level)
    case 'level_asc':
      return [...filtered].sort((a, b) => a.pokemon.level - b.pokemon.level)
    default:
      return [...filtered].sort((a, b) => (a.box_index - b.box_index) || (a.slot_index - b.slot_index))
  }
})

const starterPokemon = computed(() => {
  const uuid = profile.data.value?.player.starter_uuid
  if (!uuid) return null
  const inParty = party.data.value?.slots.find(s => s.pokemon.pokemon_uuid === uuid)?.pokemon
  if (inParty) return inParty
  return allPcSlots.value.find(s => s.pokemon.pokemon_uuid === uuid)?.pokemon ?? null
})

const selectedPcSlot = computed(() => allSlotsForPcView.value.find(s => s.pokemon.pokemon_uuid === selectedPokemonUuid.value) ?? null)
const selectedPokemon = computed(() => selectedPcSlot.value?.pokemon ?? null)

function selectPcPokemon(uuid: string) {
  selectedPokemonUuid.value = selectedPokemonUuid.value === uuid ? null : uuid
}

function pcTypeCount(type: string): number {
  return allSlotsForPcView.value.filter(s => {
    const info = speciesStore.info(s.pokemon.species_id)
    return info?.primary_type.toLowerCase() === type || info?.secondary_type?.toLowerCase() === type
  }).length
}
function pcRarityCount(kind: BadgeKind): number {
  return allSlotsForPcView.value.filter(s => {
    const info = speciesStore.info(s.pokemon.species_id)
    return info && speciesBadges(s.pokemon.species_id, info).has(kind)
  }).length
}
function pcGenCount(gen: number): number {
  return allSlotsForPcView.value.filter(s => speciesStore.info(s.pokemon.species_id)?.generation === gen).length
}

const totalBoxes = computed(() => {
  const slots = allPcSlots.value
  if (!slots.length) return 0
  return Math.max(...slots.map(s => s.box_index)) + 1
})

const pcSpeciesCounts = computed(() => {
  const counts = new Map<string, number>()
  for (const s of allSlotsForPcView.value)
    counts.set(s.pokemon.species_id, (counts.get(s.pokemon.species_id) ?? 0) + 1)
  return counts
})

const duplicateSpeciesIds = computed(() => {
  const counts = new Map<string, number>()
  for (const s of allSlotsForPcView.value)
    counts.set(s.pokemon.species_id, (counts.get(s.pokemon.species_id) ?? 0) + 1)
  return new Set([...counts.entries()].filter(([, n]) => n > 1).map(([id]) => id))
})

const pcQuickStats = computed(() => {
  const slots = allSlotsForPcView.value
  if (!slots.length) return null
  const shinies = slots.filter(s => s.pokemon.shiny).length
  const avgLevel = Math.round(slots.reduce((sum, s) => sum + s.pokemon.level, 0) / slots.length)
  const sixIv = slots.filter(s => {
    const { hp, atk, def, spa, spd, spe } = s.pokemon.ivs
    return hp === 31 && atk === 31 && def === 31 && spa === 31 && spd === 31 && spe === 31
  }).length
  const counts = new Map<string, number>()
  for (const s of slots)
    counts.set(s.pokemon.species_id, (counts.get(s.pokemon.species_id) ?? 0) + 1)
  const top = [...counts.entries()].sort((a, b) => b[1] - a[1])[0]
  const topSpecies = top && top[1] > 1 ? { id: top[0], count: top[1] } : null
  return { shinies, avgLevel, sixIv, topSpecies }
})

watch(tab, (v) => {
  if (v === 'party' && !party.data.value && !party.isLoading.value) void party.reload()
  if (v === 'pc' || v === 'pokedex') {
    if (!pcAll.data.value && !pcAll.isLoading.value) void pcAll.reload()
    if (!party.data.value && !party.isLoading.value) void party.reload()
  }
  if (v === 'minecraft') {
    if (!advancements.data.value && !advancements.isLoading.value) void advancements.reload()
  }
}, { immediate: true })

const tabs = computed(() => [
  { id: 'stats' as const, label: 'Pokémon' },
  { id: 'party' as const, label: 'Equipo', count: party.data.value?.slots.length ?? null },
  { id: 'pc' as const, label: 'PC', count: pcMeta.data.value?.page.total ?? null },
  { id: 'pokedex' as const, label: 'Pokédex', count: pokedex.data.value?.entries.length ?? null },
  { id: 'minecraft' as const, label: 'Minecraft' },
])

const pokedexCounts = computed(() => {
  const acc = { CAUGHT: 0, ENCOUNTERED: 0, NONE: 0 } as Record<PokedexKnowledge, number>
  for (const e of pokedex.data.value?.entries ?? []) acc[e.knowledge]++
  return acc
})

const typeCaptures = computed(() => {
  const stats = profile.data.value?.stats.type_capture_counts ?? {}
  return POKEMON_TYPES
    .map((type) => ({ type, count: stats[type] ?? 0 }))
    .sort((a, b) => b.count - a.count || a.type.localeCompare(b.type))
})

const capturedTypeCount = computed(() => typeCaptures.value.filter((t) => t.count > 0).length)

const speciesStore = useSpeciesStore()

const caughtSpeciesIds = computed<Set<string>>(() => {
  const ids = new Set<string>()
  for (const e of pokedex.data.value?.entries ?? []) {
    if (e.knowledge === 'CAUGHT') ids.add(e.species_id)
  }
  return ids
})

const rarityProgression = computed(() =>
  FILTERABLE_BADGES.map((kind) => {
    const total = speciesStore.totalByBadge[kind] ?? 0
    let caught = 0
    for (const id of caughtSpeciesIds.value) {
      const info = speciesStore.info(id)
      if (info && speciesBadges(id, info).has(kind)) caught++
    }
    return { kind, caught, total }
  }).filter((r) => r.total > 0),
)

const selectedSpeciesId = ref<string | null>(null)

function selectSpecies(id: string) {
  selectedSpeciesId.value = selectedSpeciesId.value === id ? null : id
}

type PokedexSort = 'dex' | 'name' | 'caught_first'
const POKEDEX_SORT_OPTIONS = [
  { value: 'dex' as const, label: 'Nº nacional' },
  { value: 'name' as const, label: 'Nombre A-Z' },
  { value: 'caught_first' as const, label: 'Capturados primero' },
]

const showOnlyCaught = ref(false)
const showOnlyFullyEvolved = ref(false)
const showNotOwned = ref(false)
const pokedexSort = ref<PokedexSort>('caught_first')
const pokedexQuery = ref('')
const selectedRarities = ref<BadgeKind[]>([])
const selectedGenerations = ref<number[]>([])
const selectedTypes = ref<string[]>([])

function clearFilters() {
  showOnlyCaught.value = false
  showOnlyFullyEvolved.value = false
  showNotOwned.value = false
  pokedexSort.value = 'caught_first'
  pokedexQuery.value = ''
  selectedRarities.value = []
  selectedGenerations.value = []
  selectedTypes.value = []
}

const knowledgeMap = computed<Map<string, PokedexKnowledge>>(() => {
  const m = new Map<string, PokedexKnowledge>()
  for (const e of pokedex.data.value?.entries ?? []) m.set(e.species_id, e.knowledge)
  return m
})

type CombinedPokedexEntry = {
  species_id: string
  knowledge: PokedexKnowledge
  national_dex: number
  types: string[]
}

/** All species from the species registry merged with the player's knowledge level. */
const allPokedexEntries = computed<CombinedPokedexEntry[]>(() => {
  const list: CombinedPokedexEntry[] = []
  for (const [id, info] of Object.entries(speciesStore.byId)) {
    list.push({
      species_id: id,
      knowledge: knowledgeMap.value.get(id) ?? 'NONE',
      national_dex: info.national_dex,
      types: info.secondary_type ? [info.primary_type, info.secondary_type] : [info.primary_type],
    })
  }
  list.sort((a, b) => a.national_dex - b.national_dex || a.species_id.localeCompare(b.species_id))
  return list
})

const KNOWLEDGE_RANK: Record<PokedexKnowledge, number> = { CAUGHT: 0, ENCOUNTERED: 1, NONE: 2 }

const filteredPokedexEntries = computed(() => {
  const entries = allPokedexEntries.value
  const rarities = selectedRarities.value
  const gens = selectedGenerations.value
  const types = selectedTypes.value
  const caughtOnly = showOnlyCaught.value
  const feOnly = showOnlyFullyEvolved.value
  const query = pokedexQuery.value.trim().toLowerCase()
  const noFilters = !showNotOwned.value && !caughtOnly && !feOnly && !rarities.length && !gens.length && !types.length && !query
  const filtered = noFilters
    ? entries.filter((e) => e.knowledge !== 'NONE')
    : entries.filter((e) => {
        if (!showNotOwned.value && e.knowledge === 'NONE') return false
        if (caughtOnly && e.knowledge !== 'CAUGHT') return false
        if (query && !stripNamespace(e.species_id).toLowerCase().includes(query)) return false
        const info = speciesStore.info(e.species_id)
        if (!info) return false
        if (feOnly && !info.fully_evolved) return false
        if (gens.length && !gens.includes(info.generation)) return false
        if (types.length) {
          const hasType = types.includes(info.primary_type) || (info.secondary_type !== null && types.includes(info.secondary_type))
          if (!hasType) return false
        }
        if (rarities.length) {
          const badges = speciesBadges(e.species_id, info)
          if (!rarities.some((r) => badges.has(r))) return false
        }
        return true
      })
  switch (pokedexSort.value) {
    case 'name':
      return [...filtered].sort((a, b) =>
        stripNamespace(a.species_id).localeCompare(stripNamespace(b.species_id)),
      )
    case 'caught_first':
      return [...filtered].sort((a, b) =>
        KNOWLEDGE_RANK[a.knowledge] - KNOWLEDGE_RANK[b.knowledge] ||
        a.national_dex - b.national_dex,
      )
    default:
      return filtered
  }
})

function rarityLabel(kind: BadgeKind): string { return BADGES[kind].name }
function genLabel(g: number): string { return `Gen ${g}` }

function focusOnRarity(kind: BadgeKind) {
  clearFilters()
  if (kind === 'fully_evolved') showOnlyFullyEvolved.value = true
  else selectedRarities.value = [kind]
  tab.value = 'pokedex'
}

function focusOnGeneration(gen: number) {
  clearFilters()
  selectedGenerations.value = [gen]
  tab.value = 'pokedex'
}

const hasActiveFilters = computed(
  () =>
    showOnlyCaught.value ||
    showOnlyFullyEvolved.value ||
    showNotOwned.value ||
    pokedexSort.value !== 'caught_first' ||
    pokedexQuery.value.trim().length > 0 ||
    selectedRarities.value.length > 0 ||
    selectedGenerations.value.length > 0 ||
    selectedTypes.value.length > 0,
)

function dexTypeCount(type: string): number {
  return Object.values(speciesStore.byId).filter(info =>
    info.primary_type.toLowerCase() === type || info.secondary_type?.toLowerCase() === type,
  ).length
}
function dexRarityCount(kind: BadgeKind): number {
  return speciesStore.totalByBadge[kind] ?? 0
}
function dexGenCount(gen: number): number {
  return speciesStore.totalByGeneration[gen] ?? 0
}

const availableGenerations = computed(() => {
  const all: number[] = []
  for (let g = 1; g <= 9; g++) {
    if ((speciesStore.totalByGeneration[g] ?? 0) > 0) all.push(g)
  }
  return all
})

const generationProgression = computed(() => {
  const list: { gen: number; caught: number; total: number }[] = []
  for (let gen = 1; gen <= 9; gen++) {
    const total = speciesStore.totalByGeneration[gen] ?? 0
    if (total === 0) continue
    let caught = 0
    for (const id of caughtSpeciesIds.value) {
      if (speciesStore.info(id)?.generation === gen) caught++
    }
    list.push({ gen, caught, total })
  }
  return list
})

const shinyRatio = computed(() => {
  const s = profile.data.value?.stats
  if (!s || s.total_capture_count === 0) return null
  return ((s.total_shiny_capture_count / s.total_capture_count) * 100).toFixed(2)
})

const isNotFound = computed(
  () => profile.error.value instanceof ApiError && profile.error.value.status === 404,
)

const earnedBadgeIds = computed(() => new Set(activity.data.value?.badges ?? []))
const loadedBadgeSprites = reactive(new Set<string>())
const hoveredBadgeId = ref<string | null>(null)

const meaningfulAdvancements = computed(() =>
  (advancements.data.value?.completed ?? []).filter(id => !stripNamespace(id).startsWith('recipes/')),
)
</script>

<template>
  <div class="flex flex-col gap-8">
    <RouterLink
      to="/players"
      class="inline-flex w-fit items-center gap-1.5 font-sans text-sm text-muted transition-colors hover:text-ink"
    >
      <ChevronLeft :size="16" />
      <span>volver al listado</span>
    </RouterLink>

    <LoadingState v-if="profile.isLoading.value && !profile.data.value" label="Cargando entrenador..." />

    <div
      v-else-if="isNotFound"
      class="rounded-lg border border-yellow/40 bg-yellow-soft px-6 py-6 backdrop-blur-md"
    >
      <p class="font-display text-lg font-semibold text-yellow">Sin registro</p>
      <p class="mt-1 text-sm text-ink-soft">No existe snapshot para <span class="font-mono text-yellow">{{ uuid }}</span>. Quizá no haya entrado al servidor desde que el mod está activo.</p>
    </div>

    <ErrorState v-else-if="profile.error.value && !profile.data.value" :error="profile.error.value" :on-retry="profile.reload" />

    <template v-else-if="profile.data.value">
      <!-- HERO -->
      <section class="grid grid-cols-1 gap-6 rounded-lg border border-edge bg-white/[0.04] px-6 py-6 backdrop-blur-md md:grid-cols-[auto_1fr] md:gap-8 md:px-8 md:py-7">
        <div class="flex items-start justify-center md:block">
          <PlayerAvatar
            :name="profile.data.value.player.name"
            size="lg"
            variant="body"
          />
        </div>

        <div class="flex flex-col gap-4">
          <div class="flex justify-center md:justify-start">
            <span class="inline-flex items-center gap-2 rounded-full border border-edge-soft bg-white/[0.04] px-3 py-1">
              <StatusDot :online="profile.data.value.player.online" />
              <span
                :class="cn(
                  'font-sans text-xs',
                  profile.data.value.player.online ? 'text-green' : 'text-muted',
                )"
              >
                {{ profile.data.value.player.online ? 'en línea' : 'desconectado' }}
              </span>
            </span>
          </div>

          <div class="flex flex-col gap-1.5">
            <p class="font-sans text-sm text-muted">Ficha del entrenador</p>
            <h1 class="font-display text-4xl font-semibold leading-none tracking-tight text-ink sm:text-5xl">
              {{ profile.data.value.player.name }}
            </h1>
          </div>

          <dl class="grid grid-cols-1 gap-x-8 gap-y-3 border-t border-edge-soft pt-4 text-sm sm:grid-cols-2">
            <div class="flex justify-between gap-3">
              <dt class="text-muted">Visto</dt>
              <dd class="text-ink" :title="formatAbsolute(profile.data.value.player.last_seen)">
                {{ formatRelative(profile.data.value.player.last_seen) }}
              </dd>
            </div>
            <div v-if="starterPokemon" class="flex justify-between gap-3">
              <dt class="text-muted">Inicial</dt>
              <dd class="flex items-center gap-2 text-ink">
                {{ starterPokemon.nickname ?? prettySpecies(starterPokemon.species_id) }}
                <Lock v-if="profile.data.value.player.starter_locked" :size="12" class="text-yellow" />
              </dd>
            </div>
            <div class="flex justify-between gap-3">
              <dt class="text-muted">Último snapshot</dt>
              <dd class="text-ink" :title="formatAbsolute(profile.data.value.player.snapshot_at)">
                {{ formatRelative(profile.data.value.player.snapshot_at) }}
              </dd>
            </div>
            <div v-if="economy.data.value" class="flex justify-between gap-3">
              <dt class="text-muted">CobbleDólares</dt>
              <dd class="font-display font-semibold text-yellow">
                $ {{ formatInt(economy.data.value.cobble_dollars) }}
              </dd>
            </div>
          </dl>

          <!-- Compact badge strip -->
          <div v-if="activity.data.value" class="flex flex-col gap-3 border-t border-edge-soft pt-4">
            <div class="flex items-baseline justify-between">
              <p class="font-sans text-xs font-medium text-muted">Medallas de gimnasio</p>
              <p class="font-display text-xs font-semibold tabular-nums">
                <span class="text-ink">{{ earnedBadgeIds.size }}</span>
                <span class="text-faint"> / {{ GYM_REGIONS.reduce((n, r) => n + r.badges.length, 0) }}</span>
              </p>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div v-for="region in GYM_REGIONS" :key="region.key" class="flex items-center gap-2">
                <span class="w-12 shrink-0 font-sans text-[10px] text-faint">{{ region.label }}</span>
                <div class="flex flex-wrap gap-1">
                  <div
                    v-for="badge in region.badges"
                    :key="badge.id"
                    class="relative flex h-9 w-9 cursor-default items-center justify-center"
                    :title="`${badge.name} — ${badge.leader} · ${badge.city}`"
                    @mouseenter="hoveredBadgeId = badge.id"
                    @mouseleave="hoveredBadgeId = null"
                  >
                    <template v-if="badge.sprite">
                      <div
                        v-if="!loadedBadgeSprites.has(badge.id)"
                        class="absolute inset-0 animate-pulse rounded-full bg-white/[0.08]"
                      />
                      <img
                        :src="`https://cdn.jsdelivr.net/gh/PokeAPI/sprites@master/sprites/badges/${badge.sprite}.png`"
                        :alt="badge.name"
                        class="h-full w-full object-contain transition-all duration-200"
                        :class="loadedBadgeSprites.has(badge.id) ? 'opacity-100' : 'opacity-0'"
                        :style="earnedBadgeIds.has(badge.id) || hoveredBadgeId === badge.id
                          ? { filter: `drop-shadow(0 0 6px ${typeBgColor(badge.type)}cc)` }
                          : { filter: 'grayscale(100%) brightness(0.4)' }"
                        @load="loadedBadgeSprites.add(badge.id)"
                      />
                    </template>
                    <div
                      v-else
                      class="flex h-9 w-9 flex-col items-center justify-center gap-0 rounded-lg border-2 transition-all"
                      :style="earnedBadgeIds.has(badge.id) || hoveredBadgeId === badge.id
                        ? { backgroundColor: typeBgColor(badge.type) + '22', borderColor: typeBgColor(badge.type), boxShadow: `0 0 8px ${typeBgColor(badge.type)}80` }
                        : { backgroundColor: 'transparent', borderColor: 'rgba(255,255,255,0.08)' }"
                    >
                      <span
                        class="font-display text-[8px] font-bold leading-none tracking-wide"
                        :style="earnedBadgeIds.has(badge.id) || hoveredBadgeId === badge.id ? { color: typeBgColor(badge.type) } : { color: 'rgba(255,255,255,0.25)' }"
                      >{{ badge.city.split("'")[0].substring(0, 4).toUpperCase() }}</span>
                    </div>
                  </div>
                </div>
                <span
                  class="ml-auto shrink-0 font-display text-[10px] font-semibold tabular-nums"
                  :class="region.badges.some(b => earnedBadgeIds.has(b.id)) ? 'text-ink-soft' : 'text-faint'"
                >{{ region.badges.filter(b => earnedBadgeIds.has(b.id)).length }}/{{ region.badges.length }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- tabs -->
      <TabBar v-model="tab" :tabs="tabs" />

      <!-- STATS -->
      <section v-if="tab === 'stats'" class="flex flex-col gap-8">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <!-- CAPTURAS -->
          <article class="flex flex-col gap-4 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
            <div class="flex items-center gap-2">
              <Target :size="16" class="text-ink-soft" />
              <h3 class="font-display text-base font-semibold text-ink">Capturas</h3>
            </div>
            <dl class="flex flex-col gap-3">
              <div class="flex items-baseline justify-between">
                <dt class="text-sm text-muted">Total</dt>
                <dd class="font-display text-4xl font-semibold leading-none tracking-tight text-ink">
                  {{ formatInt(profile.data.value.stats.total_capture_count) }}
                </dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                <dt class="flex items-center gap-1.5 text-sm text-muted">
                  <Sparkles :size="12" class="text-yellow" /> Shinies
                </dt>
                <dd class="flex items-baseline gap-1.5">
                  <span class="font-display text-xl font-semibold text-ink">{{ profile.data.value.stats.total_shiny_capture_count }}</span>
                  <span v-if="shinyRatio" class="font-mono text-xs text-faint">({{ shinyRatio }}%)</span>
                </dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                <dt class="text-sm text-muted">Combates salvajes</dt>
                <dd class="font-display text-xl font-semibold text-ink">{{ profile.data.value.stats.total_pvw_battle_victory_count }}</dd>
              </div>
            </dl>
          </article>

          <!-- RENDIMIENTO COMBATE -->
          <article class="flex flex-col gap-4 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
            <div class="flex items-center gap-2">
              <Swords :size="16" class="text-ink-soft" />
              <h3 class="font-display text-base font-semibold text-ink">Rendimiento</h3>
            </div>
            <dl class="flex flex-col gap-3">
              <div class="flex items-baseline justify-between">
                <dt class="text-sm text-muted">Victorias totales</dt>
                <dd class="font-display text-4xl font-semibold leading-none tracking-tight text-ink">
                  {{ formatInt(profile.data.value.stats.total_battle_victory_count) }}
                </dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                <dt class="text-sm text-muted">PvP</dt>
                <dd class="font-display text-xl font-semibold text-ink">{{ profile.data.value.stats.total_pvp_battle_victory_count }}</dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                <dt class="text-sm text-muted">NPC</dt>
                <dd class="font-display text-xl font-semibold text-ink">{{ profile.data.value.stats.total_pvn_battle_victory_count }}</dd>
              </div>
            </dl>
          </article>

          <!-- CRIANZA Y TRADES -->
          <article class="flex flex-col gap-4 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
            <div class="flex items-center gap-2">
              <Egg :size="16" class="text-ink-soft" />
              <h3 class="font-display text-base font-semibold text-ink">Crianza y trades</h3>
            </div>
            <dl class="flex flex-col gap-3">
              <div class="flex items-baseline justify-between">
                <dt class="text-sm text-muted">Huevos (rec / ecl)</dt>
                <dd class="font-display text-2xl font-semibold leading-none tracking-tight text-ink">
                  {{ profile.data.value.stats.total_eggs_collected }}
                  <span class="text-faint">/</span>
                  {{ profile.data.value.stats.total_eggs_hatched }}
                </dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                <dt class="text-sm text-muted">Evoluciones</dt>
                <dd class="font-display text-xl font-semibold text-ink">{{ profile.data.value.stats.total_evolved_count }}</dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                <dt class="text-sm text-muted">Intercambios</dt>
                <dd class="font-display text-xl font-semibold text-ink">{{ profile.data.value.stats.total_traded_count }}</dd>
              </div>
            </dl>
          </article>
        </div>

        <div class="flex flex-col gap-3 pt-2">
          <div class="flex items-baseline justify-between gap-3">
            <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Capturas por tipo</h2>
            <span class="font-sans text-sm text-muted">
              <span class="font-semibold text-ink">{{ capturedTypeCount }}</span>
              <span class="text-faint"> / {{ POKEMON_TYPES.length }} tipos</span>
            </span>
          </div>
          <div class="flex flex-wrap gap-2">
            <div
              v-for="{ type, count } in typeCaptures"
              :key="type"
              :class="cn(
                'flex items-center gap-2.5 rounded-md border bg-white/[0.03] px-3 py-1.5 backdrop-blur-md transition-opacity',
                count === 0 ? 'border-edge-soft opacity-40' : 'border-edge-soft',
              )"
            >
              <TypePill :type="type" size="sm" />
              <span
                :class="cn(
                  'font-display text-base font-semibold',
                  count === 0 ? 'text-faint' : 'text-ink',
                )"
              >{{ count }}</span>
            </div>
          </div>
        </div>

        <!-- PROGRESIÓN POR RAREZA -->
        <div v-if="rarityProgression.length" class="flex flex-col gap-3 pt-2">
          <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Progresión por rareza</h2>
          <div class="grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-3">
            <button
              v-for="{ kind, caught, total } in rarityProgression"
              :key="kind"
              type="button"
              :class="cn(
                'flex items-center justify-between gap-3 rounded-md border bg-white/[0.03] px-3 py-2.5 text-left backdrop-blur-md transition-all hover:border-edge hover:bg-white/[0.06]',
                caught === 0 ? 'border-edge-soft opacity-50 hover:opacity-100' : 'border-edge-soft',
              )"
              @click="focusOnRarity(kind)"
            >
              <div class="flex items-center gap-2">
                <component :is="BADGES[kind].icon" :size="14" :class="BADGES[kind].textCls" />
                <span class="font-display text-sm font-semibold text-ink">{{ BADGES[kind].name }}</span>
              </div>
              <span class="font-display text-base font-semibold leading-none tracking-tight">
                <span :class="caught === 0 ? 'text-faint' : 'text-ink'">{{ caught }}</span>
                <span class="text-faint"> / {{ total }}</span>
              </span>
            </button>
          </div>
        </div>

        <!-- PROGRESIÓN POR GENERACIÓN -->
        <div v-if="generationProgression.length" class="flex flex-col gap-3 pt-2">
          <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Por generación</h2>
          <div class="grid grid-cols-3 gap-2 sm:grid-cols-5 md:grid-cols-9">
            <button
              v-for="{ gen, caught, total } in generationProgression"
              :key="gen"
              type="button"
              :class="cn(
                'flex flex-col items-center gap-1 rounded-md border bg-white/[0.03] px-3 py-3 backdrop-blur-md transition-all hover:border-edge hover:bg-white/[0.06]',
                caught === 0 ? 'border-edge-soft opacity-50 hover:opacity-100' : 'border-edge-soft',
              )"
              @click="focusOnGeneration(gen)"
            >
              <span class="font-sans text-xs text-muted">Gen {{ gen }}</span>
              <span class="font-display text-base font-semibold leading-none tracking-tight">
                <span :class="caught === 0 ? 'text-faint' : 'text-ink'">{{ caught }}</span>
                <span class="text-faint">/{{ total }}</span>
              </span>
            </button>
          </div>
        </div>

        <template v-if="activity.data.value">
          <!-- ENTRENADOR POKÉMON -->
          <div class="flex flex-col gap-4 pt-2">
            <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Entrenador Pokémon</h2>
            <div class="grid max-w-2xl grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
              <StatCard label="Subidas de nivel" :value="formatInt(activity.data.value.level_ups)" />
              <StatCard label="Evoluciones" :value="formatInt(activity.data.value.evolutions)" />
              <StatCard label="Monturas" :value="formatInt(activity.data.value.times_ridden)" />
              <StatCard label="Liberados" :value="formatInt(activity.data.value.released)" />
              <StatCard label="Huevos recogidos" :value="formatInt(activity.data.value.eggs_collected)" />
              <StatCard label="Huevos eclosionados" :value="formatInt(activity.data.value.eggs_hatched)" />
              <StatCard label="Lanzamientos caña" :value="formatInt(activity.data.value.rod_casts)" />
              <StatCard label="Pesca exitosa" :value="formatInt(activity.data.value.reel_ins)" />
            </div>
          </div>

          <!-- RAIDS -->
          <div v-if="activity.data.value.raids_joined > 0" class="flex flex-col gap-4">
            <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Raids</h2>
            <div class="rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
              <div class="mb-4 grid grid-cols-3 gap-4">
                <div class="flex flex-col gap-0.5">
                  <span class="text-xs text-muted">Completadas</span>
                  <span class="font-display text-3xl font-semibold leading-none text-ink">{{ activity.data.value.raids_completed }}</span>
                </div>
                <div class="flex flex-col gap-0.5">
                  <span class="text-xs text-muted">Unidas</span>
                  <span class="font-display text-3xl font-semibold leading-none text-ink">{{ activity.data.value.raids_joined }}</span>
                </div>
                <div class="flex flex-col gap-0.5">
                  <span class="text-xs text-muted">Organizadas</span>
                  <span class="font-display text-3xl font-semibold leading-none text-ink">{{ activity.data.value.raids_hosted }}</span>
                </div>
              </div>
              <div class="flex flex-wrap gap-2 border-t border-edge-soft pt-3">
                <div
                  v-for="(count, i) in [
                    activity.data.value.raid_tier1,
                    activity.data.value.raid_tier2,
                    activity.data.value.raid_tier3,
                    activity.data.value.raid_tier4,
                    activity.data.value.raid_tier5,
                  ]"
                  :key="i"
                  class="flex items-center gap-1.5 rounded border px-2.5 py-1"
                  :class="count > 0 ? 'border-yellow/40 bg-yellow/10' : 'border-edge-soft opacity-30'"
                >
                  <Trophy :size="11" :class="count > 0 ? 'text-yellow' : 'text-faint'" />
                  <span class="font-mono text-xs" :class="count > 0 ? 'text-yellow' : 'text-faint'">Tier {{ i + 1 }}</span>
                  <span class="font-display text-sm font-semibold" :class="count > 0 ? 'text-ink' : 'text-faint'">× {{ count }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </section>

      <!-- PARTY -->
      <section v-else-if="tab === 'party'" class="flex flex-col gap-6">
        <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Equipo activo</h2>
        <LoadingState v-if="party.isLoading.value && !party.data.value" label="Cargando equipo..." />
        <ErrorState v-else-if="party.error.value && !party.data.value" :error="party.error.value" :on-retry="party.reload" />
        <EmptyState v-else-if="!party.data.value?.slots.length" label="El equipo está vacío." />
        <div v-else class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          <PokemonCard
            v-for="slot in party.data.value.slots"
            :key="slot.pokemon.pokemon_uuid"
            :pokemon="slot.pokemon"
          />
        </div>
      </section>

      <!-- PC -->
      <section v-else-if="tab === 'pc'" class="flex flex-col gap-6">
        <h2 class="font-display text-xl font-semibold tracking-tight text-ink">
          PC
          <span v-if="pcMeta.data.value" class="font-display text-sm font-medium text-muted">
            — {{ pcMeta.data.value.page.total }} guardados en {{ totalBoxes }} {{ totalBoxes === 1 ? 'caja' : 'cajas' }}
          </span>
        </h2>
        <LoadingState v-if="pcAll.isLoading.value && !pcAll.data.value" label="Cargando PC..." />
        <ErrorState v-else-if="pcAll.error.value && !pcAll.data.value" :error="pcAll.error.value" :on-retry="pcAll.reload" />
        <template v-else-if="(pcMeta.data.value?.page.total ?? 0) === 0">
          <EmptyState label="El PC está vacío." hint="Este entrenador no ha guardado ningún pokémon todavía." />
        </template>
        <template v-else>
          <!-- STATS RÁPIDAS -->
          <div v-if="pcQuickStats" class="flex flex-wrap gap-2">
            <div class="flex items-center gap-1.5 rounded-md border border-edge-soft bg-white/[0.03] px-3 py-1.5">
              <Sparkles :size="12" class="text-yellow" />
              <span class="font-sans text-xs text-muted">Shinies</span>
              <span class="font-mono text-sm font-semibold text-ink">{{ pcQuickStats.shinies }}</span>
            </div>
            <div class="flex items-center gap-1.5 rounded-md border border-edge-soft bg-white/[0.03] px-3 py-1.5">
              <TrendingUp :size="12" class="text-faint" />
              <span class="font-sans text-xs text-muted">Nivel medio</span>
              <span class="font-mono text-sm font-semibold text-ink">{{ pcQuickStats.avgLevel }}</span>
            </div>
            <div class="flex items-center gap-1.5 rounded-md border border-edge-soft bg-white/[0.03] px-3 py-1.5">
              <Star :size="12" class="text-faint" />
              <span class="font-sans text-xs text-muted">6 IVs perfectos</span>
              <span class="font-mono text-sm font-semibold text-ink">{{ pcQuickStats.sixIv }}</span>
            </div>
            <div v-if="pcQuickStats.topSpecies" class="flex items-center gap-1.5 rounded-md border border-edge-soft bg-white/[0.03] px-3 py-1.5">
              <Copy :size="12" class="text-faint" />
              <span class="font-sans text-xs text-muted">Más repetido</span>
              <span class="font-sans text-sm font-semibold text-ink">{{ prettySpecies(pcQuickStats.topSpecies.id) }}</span>
              <span class="font-mono text-xs text-faint">×{{ pcQuickStats.topSpecies.count }}</span>
            </div>
          </div>

          <!-- FILTROS -->
          <div class="flex flex-col gap-3 rounded-lg border border-edge-soft bg-white/[0.03] p-4 backdrop-blur-md">
            <div class="flex flex-wrap items-center gap-3">
              <SearchInput v-model="pcQuery" placeholder="Buscar por nombre o mote..." />
              <ToggleChip
                :selected="pcShinyOnly"
                tone="yellow"
                :icon="Sparkles"
                @toggle="pcShinyOnly = !pcShinyOnly"
              >Solo shinies</ToggleChip>
              <ToggleChip
                :selected="pcFeOnly"
                tone="cyan"
                :icon="Check"
                @toggle="pcFeOnly = !pcFeOnly"
              >Solo evolución final</ToggleChip>
              <ToggleChip
                :selected="pcEvoFilter === 'ready'"
                tone="green"
                :icon="Zap"
                @toggle="togglePcEvoFilter('ready')"
              >Puede evolucionar</ToggleChip>
              <ToggleChip
                :selected="pcEvoFilter === 'needs_item'"
                tone="yellow"
                :icon="Zap"
                @toggle="togglePcEvoFilter('needs_item')"
              >Necesita objeto</ToggleChip>
              <ToggleChip
                :selected="pcEvoFilter === 'not_yet'"
                tone="blue"
                :icon="Zap"
                @toggle="togglePcEvoFilter('not_yet')"
              >Podrá evolucionar</ToggleChip>
              <ToggleChip
                :selected="pcDuplicatesOnly"
                tone="magenta"
                :icon="Copy"
                @toggle="pcDuplicatesOnly = !pcDuplicatesOnly"
              >Duplicados</ToggleChip>
              <button
                v-if="pcHasActiveFilters"
                type="button"
                class="font-sans text-xs text-faint underline-offset-2 transition-colors hover:text-ink hover:underline"
                @click="pcClearFilters"
              >
                Limpiar filtros
              </button>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <MultiSelect
                v-model="pcSelectedTypes"
                :options="POKEMON_TYPES"
                :label-fn="typeLabel"
                :count-fn="pcTypeCount"
                trigger-label="Tipo"
                search-placeholder="Buscar tipo..."
              >
                <template #option="{ option }">
                  <span
                    class="inline-flex items-center justify-center rounded px-1.5 py-0.5 font-sans text-xs font-medium"
                    :class="typeOnBgTextClass(option)"
                    :style="{ backgroundColor: typeBgColor(option) }"
                  >{{ typeLabel(option) }}</span>
                </template>
              </MultiSelect>

              <MultiSelect
                v-model="pcSelectedRarities"
                :options="BADGE_PRIORITY"
                :label-fn="rarityLabel"
                :count-fn="pcRarityCount"
                trigger-label="Rareza"
                search-placeholder="Buscar rareza..."
              >
                <template #option="{ option }">
                  <component :is="BADGES[option].icon" :size="12" :class="BADGES[option].textCls" />
                  <span :class="BADGES[option].textCls">{{ BADGES[option].name }}</span>
                </template>
              </MultiSelect>

              <MultiSelect
                v-model="pcSelectedGens"
                :options="availableGenerations"
                :label-fn="genLabel"
                :count-fn="pcGenCount"
                trigger-label="Generación"
                no-search
              />
            </div>

            <div class="flex flex-wrap items-center gap-2 border-t border-edge-soft pt-3">
              <SortSelector v-model="pcSort" :options="PC_SORT_OPTIONS" label="Orden" />
            </div>
          </div>

          <EmptyState
            v-if="!pcFilteredSlots.length"
            label="Ningún pokémon coincide con los filtros."
            hint="Prueba a quitar alguno."
          />
          <div v-else class="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
            <PokemonGridCell
              v-for="slot in pcFilteredSlots"
              :key="slot.pokemon.pokemon_uuid"
              :species-id="slot.pokemon.species_id"
              :shiny="slot.pokemon.shiny"
              :display-name="slot.pokemon.nickname ?? prettySpecies(slot.pokemon.species_id)"
              :sub-line="slot.inParty ? `equipo · nv. ${slot.pokemon.level}` : `caja ${slot.box_index + 1} · nv. ${slot.pokemon.level}`"
              :in-party="slot.inParty"
              :evo-readiness="evoReadiness(slot.pokemon, speciesStore.detail(slot.pokemon.species_id)?.evolutions ?? [])"
              @click="selectPcPokemon(slot.pokemon.pokemon_uuid)"
            />
          </div>
        </template>
      </section>

      <!-- POKEDEX -->
      <section v-else-if="tab === 'pokedex'" class="flex flex-col gap-6">
        <h2 class="font-display text-xl font-semibold tracking-tight text-ink">
          Pokédex
          <span class="font-display text-sm font-medium text-muted">
            — <span class="text-yellow">{{ pokedexCounts.CAUGHT }}</span> capturados ·
            <span class="text-blue">{{ pokedexCounts.ENCOUNTERED }}</span> vistos
          </span>
        </h2>
        <LoadingState v-if="pokedex.isLoading.value && !pokedex.data.value" label="Cargando pokédex..." />
        <ErrorState v-else-if="pokedex.error.value && !pokedex.data.value" :error="pokedex.error.value" :on-retry="pokedex.reload" />
        <template v-else>
          <!-- FILTROS -->
          <div class="flex flex-col gap-3 rounded-lg border border-edge-soft bg-white/[0.03] p-4 backdrop-blur-md">
            <div class="flex flex-wrap items-center gap-3">
              <SearchInput v-model="pokedexQuery" placeholder="Buscar pokémon..." />
              <ToggleChip
                :selected="showOnlyCaught"
                tone="yellow"
                :icon="Check"
                @toggle="showOnlyCaught = !showOnlyCaught"
              >Solo capturados</ToggleChip>
              <ToggleChip
                :selected="showOnlyFullyEvolved"
                tone="cyan"
                :icon="Check"
                @toggle="showOnlyFullyEvolved = !showOnlyFullyEvolved"
              >Solo evolución final</ToggleChip>
              <ToggleChip
                :selected="showNotOwned"
                tone="cyan"
                :icon="Lock"
                @toggle="showNotOwned = !showNotOwned"
              >Incluir no obtenidos</ToggleChip>
              <button
                v-if="hasActiveFilters"
                type="button"
                class="font-sans text-xs text-faint underline-offset-2 transition-colors hover:text-ink hover:underline"
                @click="clearFilters"
              >
                Limpiar filtros
              </button>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <MultiSelect
                v-model="selectedTypes"
                :options="POKEMON_TYPES"
                :label-fn="typeLabel"
                :count-fn="dexTypeCount"
                trigger-label="Tipo"
                search-placeholder="Buscar tipo..."
              >
                <template #option="{ option }">
                  <span
                    class="inline-flex items-center justify-center rounded px-1.5 py-0.5 font-sans text-xs font-medium"
                    :class="typeOnBgTextClass(option)"
                    :style="{ backgroundColor: typeBgColor(option) }"
                  >{{ typeLabel(option) }}</span>
                </template>
              </MultiSelect>

              <MultiSelect
                v-model="selectedRarities"
                :options="BADGE_PRIORITY"
                :label-fn="rarityLabel"
                :count-fn="dexRarityCount"
                trigger-label="Rareza"
                search-placeholder="Buscar rareza..."
              >
                <template #option="{ option }">
                  <component :is="BADGES[option].icon" :size="12" :class="BADGES[option].textCls" />
                  <span :class="BADGES[option].textCls">{{ BADGES[option].name }}</span>
                </template>
              </MultiSelect>

              <MultiSelect
                v-model="selectedGenerations"
                :options="availableGenerations"
                :label-fn="genLabel"
                :count-fn="dexGenCount"
                trigger-label="Generación"
                no-search
              />
            </div>

            <div class="flex flex-wrap items-center gap-2 border-t border-edge-soft pt-3">
              <SortSelector v-model="pokedexSort" :options="POKEDEX_SORT_OPTIONS" label="Orden" />
            </div>
          </div>

          <EmptyState
            v-if="!filteredPokedexEntries.length"
            label="Ningún pokémon coincide con los filtros."
            hint="Prueba a quitar alguno."
          />
          <div v-if="filteredPokedexEntries.length" class="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
            <PokemonGridCell
              v-for="entry in filteredPokedexEntries"
              :key="entry.species_id"
              :species-id="entry.species_id"
              :display-name="prettySpecies(entry.species_id)"
              :knowledge="entry.knowledge"
              :pc-count="pcSpeciesCounts.get(entry.species_id)"
              @click="selectSpecies(entry.species_id)"
            />
          </div>
        </template>
      </section>
      <!-- MINECRAFT -->
      <section v-else-if="tab === 'minecraft'" class="flex flex-col gap-8">
        <LoadingState v-if="activity.isLoading.value && !activity.data.value" label="Cargando actividad..." />
        <ErrorState v-else-if="activity.error.value && !activity.data.value" :error="activity.error.value" :on-retry="activity.reload" />

        <template v-else-if="activity.data.value">
          <!-- ESTADÍSTICAS MC -->
          <div class="flex flex-col gap-4">
            <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Actividad</h2>
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">

              <!-- Exploración -->
              <article class="flex flex-col gap-3 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
                <div class="flex items-center gap-2">
                  <Footprints :size="15" class="text-ink-soft" />
                  <h3 class="font-display text-sm font-semibold text-ink">Exploración</h3>
                </div>
                <dl class="flex flex-col gap-2.5">
                  <div class="flex items-baseline justify-between">
                    <dt class="text-xs text-muted">Tiempo jugado</dt>
                    <dd class="font-display text-xl font-semibold text-ink">
                      {{ Math.floor(activity.data.value.play_time_ticks / 72000) }}h
                      <span class="text-sm text-ink-soft">{{ Math.floor((activity.data.value.play_time_ticks % 72000) / 1200) }}m</span>
                    </dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Saltos</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.jumps) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Dormido en cama</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.sleep_in_bed) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Fish :size="11" /> Peces pescados</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.fish_caught) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Tratos con aldeanos</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.traded_with_villager) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Bloques minados</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.blocks_mined) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Ítems crafteados</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.items_crafted) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Ítems usados / rotos</dt>
                    <dd class="font-mono text-sm text-ink-soft">{{ formatInt(activity.data.value.items_used) }} / {{ formatInt(activity.data.value.items_broken) }}</dd>
                  </div>
                </dl>
              </article>

              <!-- Combate -->
              <article class="flex flex-col gap-3 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
                <div class="flex items-center gap-2">
                  <Sword :size="15" class="text-ink-soft" />
                  <h3 class="font-display text-sm font-semibold text-ink">Combate</h3>
                </div>
                <dl class="flex flex-col gap-2.5">
                  <div class="flex items-baseline justify-between">
                    <dt class="text-xs text-muted">Mobs matados</dt>
                    <dd class="font-display text-xl font-semibold text-ink">{{ formatInt(activity.data.value.mob_kills) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Skull :size="11" /> Muertes</dt>
                    <dd class="font-display text-base font-semibold text-ink">{{ formatInt(activity.data.value.deaths) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Daño infligido</dt>
                    <dd class="font-display text-base font-semibold text-ink">{{ formatInt(Math.round(activity.data.value.damage_dealt / 10)) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="text-xs text-muted">Daño recibido</dt>
                    <dd class="font-display text-base font-semibold text-ink">{{ formatInt(Math.round(activity.data.value.damage_taken / 10)) }}</dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Shield :size="11" /> Bloqueado</dt>
                    <dd class="font-display text-base font-semibold text-ink">{{ formatInt(Math.round(activity.data.value.damage_blocked / 10)) }}</dd>
                  </div>
                </dl>
              </article>

              <!-- Distancias -->
              <article class="flex flex-col gap-3 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
                <div class="flex items-center gap-2">
                  <Wind :size="15" class="text-ink-soft" />
                  <h3 class="font-display text-sm font-semibold text-ink">Distancias</h3>
                </div>
                <dl class="flex flex-col gap-2.5">
                  <div class="flex items-baseline justify-between">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Footprints :size="11" /> A pie</dt>
                    <dd class="font-mono text-xs text-ink-soft">
                      {{ (() => { const cm = activity.data.value.walk_cm + activity.data.value.sprint_cm; return cm >= 100000 ? `${(cm / 100000).toFixed(1)} km` : `${Math.round(cm / 100)} m` })() }}
                    </dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Wind :size="11" /> Volando</dt>
                    <dd class="font-mono text-xs text-ink-soft">
                      {{ (() => { const cm = activity.data.value.fly_cm; return cm >= 100000 ? `${(cm / 100000).toFixed(1)} km` : `${Math.round(cm / 100)} m` })() }}
                    </dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Waves :size="11" /> Nadando</dt>
                    <dd class="font-mono text-xs text-ink-soft">
                      {{ (() => { const cm = activity.data.value.swim_cm; return cm >= 100000 ? `${(cm / 100000).toFixed(1)} km` : `${Math.round(cm / 100)} m` })() }}
                    </dd>
                  </div>
                  <div class="flex items-baseline justify-between border-t border-edge-soft pt-2">
                    <dt class="flex items-center gap-1.5 text-xs text-muted"><Zap :size="11" /> En Pokémon</dt>
                    <dd class="font-mono text-xs text-ink-soft">
                      {{ (() => { const cm = activity.data.value.riding_land_cm + activity.data.value.riding_air_cm + activity.data.value.riding_liquid_cm; return cm >= 100000 ? `${(cm / 100000).toFixed(1)} km` : `${Math.round(cm / 100)} m` })() }}
                    </dd>
                  </div>
                </dl>
              </article>
            </div>
          </div>

          <!-- KILLS BREAKDOWN -->
          <div v-if="Object.keys(activity.data.value.kills_map).length > 0" class="flex flex-col gap-4">
            <h2 class="font-display text-xl font-semibold tracking-tight text-ink">Entidades derrotadas</h2>
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <!-- Top Pokémon derrotados -->
              <div class="rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
                <h3 class="mb-3 flex items-center gap-2 font-display text-sm font-semibold text-ink">
                  <Swords :size="14" class="text-ink-soft" /> Pokémon derrotados
                </h3>
                <ol class="flex flex-col gap-1.5">
                  <li
                    v-for="[id, count] in Object.entries(activity.data.value.kills_map)
                      .filter(([id]) => id.startsWith('cobblemon:'))
                      .sort(([,a],[,b]) => b - a)
                      .slice(0, 8)"
                    :key="id"
                    class="flex items-baseline justify-between"
                  >
                    <span class="font-sans text-xs text-muted">{{ prettySpecies(id) }}</span>
                    <span class="font-mono text-xs font-semibold text-ink">{{ formatInt(count) }}</span>
                  </li>
                </ol>
              </div>
              <!-- Top mobs vanilla + killed by -->
              <div class="flex flex-col gap-3">
                <div class="rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
                  <h3 class="mb-3 flex items-center gap-2 font-display text-sm font-semibold text-ink">
                    <Skull :size="14" class="text-ink-soft" /> Top mobs Minecraft
                  </h3>
                  <ol class="flex flex-col gap-1.5">
                    <li
                      v-for="[id, count] in Object.entries(activity.data.value.kills_map)
                        .filter(([id]) => id.startsWith('minecraft:'))
                        .sort(([,a],[,b]) => b - a)
                        .slice(0, 5)"
                      :key="id"
                      class="flex items-baseline justify-between"
                    >
                      <span class="font-sans text-xs text-muted">{{ stripNamespace(id) }}</span>
                      <span class="font-mono text-xs font-semibold text-ink">{{ formatInt(count) }}</span>
                    </li>
                  </ol>
                </div>
                <div v-if="Object.keys(activity.data.value.killed_by_map).length > 0" class="rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md">
                  <h3 class="mb-3 flex items-center gap-2 font-display text-sm font-semibold text-ink">
                    <Skull :size="14" class="text-red-400" /> Muerto por
                  </h3>
                  <ol class="flex flex-col gap-1.5">
                    <li
                      v-for="[id, count] in Object.entries(activity.data.value.killed_by_map)
                        .sort(([,a],[,b]) => b - a)
                        .slice(0, 5)"
                      :key="id"
                      class="flex items-baseline justify-between"
                    >
                      <span class="font-sans text-xs text-muted">{{ stripNamespace(id) }}</span>
                      <span class="font-mono text-xs font-semibold text-ink">× {{ formatInt(count) }}</span>
                    </li>
                  </ol>
                </div>
              </div>
            </div>
          </div>

        </template>
      </section>
    </template>

    <!-- MODAL: detalle de especie (pokédex) -->
    <Modal v-if="selectedSpeciesId" @close="selectedSpeciesId = null">
      <SpeciesDetailPanel :species-id="selectedSpeciesId" />
    </Modal>

    <!-- MODAL: detalle de pokemon (PC) -->
    <Modal v-if="selectedPokemon" @close="selectedPokemonUuid = null">
      <div class="flex flex-col gap-6">
        <PokemonCard
          :pokemon="selectedPokemon"
          :slot-label="selectedPcSlot ? (selectedPcSlot.inParty ? `equipo · slot ${selectedPcSlot.slot_index + 1}` : `caja ${selectedPcSlot.box_index + 1} · slot ${selectedPcSlot.slot_index + 1}`) : undefined"
        />
        <div class="border-t border-edge-soft pt-2">
          <SpeciesDetailPanel :species-id="selectedPokemon.species_id" hide-header />
        </div>
      </div>
    </Modal>
  </div>
</template>
