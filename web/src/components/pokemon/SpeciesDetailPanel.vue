<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import PokemonSprite from './PokemonSprite.vue'
import TypePill from '@/components/ui/TypePill.vue'
import LabelBadge from '@/components/ui/LabelBadge.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import { useSpeciesStore } from '@/stores/species'
import { renderRequirement } from '@/utils/requirements'
import { primaryBadge } from '@/utils/badges'
import { speciesTypes } from '@/utils/pokemon'
import { cn } from '@/utils/cn'
import { prettify, stripNamespace } from '@/utils/format'
import { formSpriteUrl, formSpriteFallbackUrl } from '@/utils/pokemon'
import type { EvolutionRequirementDto, EvolutionDto } from '@/types'
import { ArrowRight, ChevronsUp, MapPin, Package, Target, Zap } from '@lucide/vue'

type Props = { speciesId: string; hideHeader?: boolean }
const props = defineProps<Props>()

const speciesStore = useSpeciesStore()

// Load current detail
watch(() => props.speciesId, (id) => { void speciesStore.loadDetail(id) }, { immediate: true })

const detail = computed(() => speciesStore.detail(props.speciesId))
const isLoading = computed(() => speciesStore.isDetailLoading(props.speciesId))

// Load pre-evo + direct evolution details (to build full chain)
watch(detail, (d) => {
  if (!d) return
  if (d.pre_evolution?.species_id) void speciesStore.loadDetail(d.pre_evolution.species_id)
  for (const evo of d.evolutions) {
    if (evo.result_id) void speciesStore.loadDetail(evo.result_id)
  }
}, { immediate: true })

// Load 3rd-level evolutions (e.g. Rhydon → Rhyperior when viewing Rhyhorn)
const directEvolutionIds = computed(() => detail.value?.evolutions.map(e => e.result_id).filter(Boolean) as string[] ?? [])
watch(directEvolutionIds, (ids) => {
  for (const id of ids) {
    const d = speciesStore.detail(id)
    if (d) {
      for (const evo of d.evolutions) {
        if (evo.result_id) void speciesStore.loadDetail(evo.result_id)
      }
    }
  }
}, { deep: true })

// Also trigger when those details actually load
watch(() => directEvolutionIds.value.map(id => speciesStore.detail(id)), (details) => {
  for (const d of details) {
    if (!d) continue
    for (const evo of d.evolutions) {
      if (evo.result_id) void speciesStore.loadDetail(evo.result_id)
    }
  }
}, { deep: true })

type ChainNode = {
  species_id: string
  name: string
  is_current: boolean
  requirements: EvolutionRequirementDto[]
  aspects: string[]
}

// Build the full chain starting from the base form
const evolutionChain = computed((): ChainNode[][] => {
  const d = detail.value
  if (!d) return []

  // Find base: if current has a pre-evo, use that as root
  const preEvoId = d.pre_evolution?.species_id ?? null
  const baseDetail = preEvoId ? speciesStore.detail(preEvoId) : d
  const baseId = preEvoId ?? props.speciesId

  if (!baseDetail) {
    // Base not loaded yet — show current as sole node
    return [[{ species_id: props.speciesId, name: d.name, is_current: true, requirements: [], aspects: [] }]]
  }

  function buildPaths(fromId: string, fromName: string, isCurrent: boolean, reqs: EvolutionRequirementDto[], aspects: string[], evolutions: EvolutionDto[]): ChainNode[][] {
    const node: ChainNode = { species_id: fromId, name: fromName, is_current: isCurrent, requirements: reqs, aspects }
    if (!evolutions.length) return [[node]]

    const paths: ChainNode[][] = []
    for (const evo of evolutions) {
      if (!evo.result_id) continue
      const evoDetail = speciesStore.detail(evo.result_id)
      const nextEvos = evoDetail?.evolutions ?? []
      const subPaths = buildPaths(evo.result_id, evo.result_name ?? '???', evo.result_id === props.speciesId, evo.requirements, evo.result_aspects ?? [], nextEvos)
      for (const sub of subPaths) paths.push([node, ...sub])
    }
    return paths.length ? paths : [[node]]
  }

  const allPaths = buildPaths(baseId, baseDetail.name, baseId === props.speciesId, [], [], baseDetail.evolutions)
  const seen = new Set<string>()
  return allPaths.filter(path => {
    const key = path.map(n => `${n.species_id}:${n.name}`).join('|')
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
})

// Single path (no branches) or multiple (e.g. Eevee)
const isBranched = computed(() => evolutionChain.value.length > 1)
const hasChain = computed(() => evolutionChain.value.some(p => p.length > 1))

function requirementSummary(reqs: EvolutionRequirementDto[]): string {
  if (!reqs.length) return ''
  for (const req of reqs) {
    if (req.type === 'item_trigger' && req.item) return prettify(stripNamespace(req.item))
    if (req.type === 'level' && req.min_level) return `Nv. ${req.min_level}+`
    if (req.type === 'friendship') return 'Amistad'
    if (req.type === 'held_item' && req.item) return prettify(stripNamespace(req.item))
    if (req.type === 'time_range') return req.identifier === 'night' ? 'Noche' : 'Día'
    if (req.type === 'weather') return req.thundering ? 'Tormenta' : 'Lluvia'
    if (req.type === 'use_move' && req.move) return prettify(req.move)
    if (req.type === 'biome') return regionalBiomeLabel(req.tag ?? req.identifier ?? '')
    if (req.type === 'world') return 'Dimensión'
    if (req.type === 'any' && req.children?.length) return requirementSummary(req.children)
  }
  return renderRequirement(reqs[0])
}

function regionalBiomeLabel(tag: string): string {
  const t = tag.toLowerCase()
  if (t.includes('alola')) return 'Alola'
  if (t.includes('galar')) return 'Galar'
  if (t.includes('hisui')) return 'Hisui'
  if (t.includes('paldea')) return 'Paldea'
  if (t.includes('regional')) return 'Regional'
  return 'Bioma'
}

const types = computed(() => speciesTypes(detail.value))
const rarityBadge = computed(() => primaryBadge(props.speciesId, speciesStore.info(props.speciesId)))

const BUCKET_TEXT: Record<string, string> = {
  common: 'text-muted', uncommon: 'text-green', rare: 'text-blue', 'ultra-rare': 'text-magenta',
}
const BUCKET_BORDER: Record<string, string> = {
  common: 'border-edge-soft', uncommon: 'border-green/40', rare: 'border-blue/40', 'ultra-rare': 'border-magenta/40',
}
const BUCKET_LABEL: Record<string, string> = {
  common: 'común', uncommon: 'poco común', rare: 'raro', 'ultra-rare': 'ultra raro',
}

function bucketLabel(b: string) { return BUCKET_LABEL[b] ?? b }
function bucketText(b: string) { return BUCKET_TEXT[b] ?? 'text-muted' }
function bucketBorder(b: string) { return BUCKET_BORDER[b] ?? 'border-edge-soft' }

function prettyBiome(id: string): string {
  const ns = id.includes(':') ? id.split(':')[0] : ''
  return prettify(stripNamespace(id)) + (ns && ns !== 'minecraft' ? ` (${ns})` : '')
}

const specialForms = computed(() =>
  (detail.value?.forms ?? []).map(f => ({
    ...f,
    kind: f.labels.some(l => l.toLowerCase() === 'gmax') ? 'gmax' as const : 'mega' as const,
  }))
)

// Three independent sets — separating "which url" from "loaded state" avoids src-flip loops
const formUseFallback = ref<Set<number>>(new Set())
const formLoaded = ref<Set<number>>(new Set())
const formFailed = ref<Set<number>>(new Set())
watch(() => props.speciesId, () => {
  formUseFallback.value = new Set()
  formLoaded.value = new Set()
  formFailed.value = new Set()
})

function onFormLoad(i: number) {
  formLoaded.value = new Set([...formLoaded.value, i])
}
function onFormError(i: number) {
  if (formUseFallback.value.has(i)) {
    formFailed.value = new Set([...formFailed.value, i])
  } else {
    formUseFallback.value = new Set([...formUseFallback.value, i])
  }
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <LoadingState v-if="isLoading && !detail" label="Cargando detalle..." />
    <template v-else-if="detail">

      <!-- HEADER -->
      <header v-if="!hideHeader" class="flex items-start gap-4">
        <div class="grid h-20 w-20 shrink-0 place-items-center rounded-lg border border-edge-soft bg-white/[0.02]">
          <PokemonSprite :species-id="detail.id" size="sm" />
        </div>
        <div class="flex min-w-0 flex-1 flex-col gap-1.5">
          <div class="flex flex-wrap items-baseline gap-3">
            <h3 class="font-display text-2xl font-semibold leading-tight tracking-tight text-ink">{{ detail.name }}</h3>
            <span class="font-mono text-xs text-faint">#{{ String(detail.national_dex).padStart(4, '0') }}</span>
            <span class="font-sans text-xs text-faint">Gen {{ detail.generation }}</span>
          </div>
          <div class="flex flex-wrap items-center gap-1.5">
            <TypePill v-for="t in types" :key="t" :type="t" size="sm" />
            <LabelBadge v-if="rarityBadge" :kind="rarityBadge" size="sm" />
          </div>
        </div>
      </header>

      <!-- CATCH RATE (siempre visible) -->
      <div class="flex items-center gap-2 font-sans text-xs">
        <Target :size="12" class="text-faint" />
        <span class="text-muted">Catch rate</span>
        <span class="font-mono text-ink">{{ detail.catch_rate }}<span class="text-faint">/255</span></span>
      </div>

      <!-- RUTA EVOLUTIVA -->
      <section class="flex flex-col gap-3">
        <div class="flex items-center gap-2">
          <ChevronsUp :size="14" class="text-ink-soft" />
          <h4 class="font-display text-sm font-semibold tracking-tight text-ink">Ruta evolutiva</h4>
        </div>

        <div v-if="!hasChain" class="rounded-md border border-edge-soft bg-white/[0.02] px-3 py-2 font-sans text-sm text-muted">
          Forma final — no evoluciona más.
        </div>

        <div v-else class="flex flex-col gap-2 rounded-lg border border-edge-soft bg-white/[0.02] p-4">
          <!-- One row per evolutionary path (handles branches like Eevee) -->
          <div
            v-for="(path, pi) in evolutionChain"
            :key="pi"
            class="flex items-center justify-center gap-2 overflow-x-auto"
          >
            <template v-for="(node, ni) in path" :key="node.species_id + ni">
              <!-- Arrow + requirement label between nodes -->
              <div v-if="ni > 0" class="flex shrink-0 flex-col items-center gap-0.5 px-1">
                <span
                  v-if="requirementSummary(node.requirements)"
                  class="rounded border border-edge-soft bg-white/[0.04] px-1.5 py-0.5 font-sans text-[10px] leading-tight text-ink-soft"
                >{{ requirementSummary(node.requirements) }}</span>
                <ArrowRight :size="14" class="text-faint" />
              </div>

              <!-- Species node -->
              <div
                :class="cn(
                  'flex shrink-0 flex-col items-center gap-1.5 rounded-lg border px-3 py-2.5 text-center transition-colors',
                  node.is_current
                    ? 'border-yellow/50 bg-yellow/5 ring-1 ring-yellow/20'
                    : 'border-edge-soft bg-white/[0.02] opacity-70',
                )"
              >
                <PokemonSprite :species-id="node.species_id" :aspects="node.aspects" size="lg" />
                <span :class="cn('font-display text-xs font-semibold leading-tight', node.is_current ? 'text-ink' : 'text-ink-soft')">
                  {{ node.name }}
                </span>
                <div class="flex flex-wrap justify-center gap-0.5">
                  <TypePill
                    v-for="t in speciesTypes(speciesStore.info(node.species_id))"
                    :key="t"
                    :type="t"
                    size="xs"
                  />
                </div>
              </div>
            </template>
          </div>
        </div>
      </section>

      <!-- FORMAS ESPECIALES (Mega / Gmax) -->
      <section v-if="specialForms.length" class="flex flex-col gap-3">
        <div class="flex items-center gap-2">
          <Zap :size="14" class="text-ink-soft" />
          <h4 class="font-display text-sm font-semibold tracking-tight text-ink">Formas especiales</h4>
        </div>
        <ul class="flex flex-col gap-2">
          <li
            v-for="(form, i) in specialForms"
            :key="i"
            class="flex items-center justify-between gap-3 rounded-md border bg-white/[0.02] px-3 py-2"
            :class="form.kind === 'gmax' ? 'border-magenta/40' : 'border-yellow/40'"
          >
            <div class="flex items-center gap-3">
              <div class="relative h-16 w-16 shrink-0">
                <div
                  v-if="!formLoaded.has(i) && !formFailed.has(i)"
                  class="absolute inset-0 animate-pulse rounded-lg bg-white/[0.07]"
                />
                <img
                  v-if="!formFailed.has(i)"
                  :src="formUseFallback.has(i)
                    ? formSpriteFallbackUrl(detail!.id, form.aspects, speciesStore.info(detail!.id)?.national_dex)
                    : formSpriteUrl(detail!.id, form.aspects)"
                  :class="['absolute inset-0 h-full w-full object-contain transition-opacity duration-200', formLoaded.has(i) ? 'opacity-100' : 'opacity-0']"
                  :alt="form.name"
                  @load="onFormLoad(i)"
                  @error="onFormError(i)"
                />
                <div
                  v-if="formFailed.has(i)"
                  class="absolute inset-0 flex items-center justify-center rounded-lg border border-edge-soft"
                >
                  <Zap :size="22" class="text-faint" />
                </div>
              </div>
              <div class="flex flex-col gap-0.5">
                <div class="flex items-center gap-1.5">
                  <span
                    class="rounded px-1.5 py-0.5 font-mono text-[10px] font-bold uppercase leading-tight"
                    :class="form.kind === 'gmax' ? 'bg-magenta/20 text-magenta' : 'bg-yellow/20 text-yellow'"
                  >{{ form.kind === 'gmax' ? 'G-Max' : 'Mega' }}</span>
                  <span class="font-display text-sm font-semibold text-ink">{{ form.name }}</span>
                </div>
              </div>
            </div>
            <div class="flex flex-wrap items-center gap-1">
              <TypePill :type="form.primary_type" size="xs" />
              <TypePill v-if="form.secondary_type" :type="form.secondary_type" size="xs" />
            </div>
          </li>
        </ul>
      </section>

      <!-- DROPS -->
      <section v-if="detail.drops.length" class="flex flex-col gap-3">
        <div class="flex items-center gap-2">
          <Package :size="14" class="text-ink-soft" />
          <h4 class="font-display text-sm font-semibold tracking-tight text-ink">Drops al derrotar</h4>
        </div>
        <ul class="flex flex-col gap-1.5">
          <li
            v-for="(drop, i) in detail.drops"
            :key="i"
            class="flex items-center justify-between gap-3 rounded-md border border-edge-soft bg-white/[0.02] px-3 py-2"
          >
            <span class="font-display text-sm font-semibold text-ink">{{ prettify(stripNamespace(drop.item)) }}</span>
            <span class="font-sans text-xs text-muted">
              <span class="font-mono text-ink-soft">{{ drop.percentage }}%</span>
              <span class="text-faint"> · </span>
              <span class="font-mono text-ink-soft">{{ drop.quantity_min === drop.quantity_max ? drop.quantity_min : `${drop.quantity_min}–${drop.quantity_max}` }}</span>
              <span class="text-faint"> ud.</span>
            </span>
          </li>
        </ul>
      </section>

      <!-- SPAWNS -->
      <section class="flex flex-col gap-3">
        <div class="flex items-center gap-2">
          <MapPin :size="14" class="text-ink-soft" />
          <h4 class="font-display text-sm font-semibold tracking-tight text-ink">¿Dónde se encuentra?</h4>
        </div>
        <div v-if="!detail.spawns.length" class="rounded-md border border-edge-soft bg-white/[0.02] px-3 py-2 font-sans text-sm text-muted">
          No tiene puntos de aparición (sólo evolución/intercambio).
        </div>
        <ul v-else class="flex flex-col gap-2">
          <li
            v-for="(spawn, i) in detail.spawns"
            :key="i"
            class="flex flex-col gap-2 rounded-md border bg-white/[0.02] px-3 py-2.5"
            :class="bucketBorder(spawn.bucket)"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-2 font-display text-sm font-semibold">
                <span :class="bucketText(spawn.bucket)">{{ bucketLabel(spawn.bucket) }}</span>
                <span class="font-sans text-xs text-faint">peso {{ spawn.weight }}</span>
              </div>
              <span class="font-mono text-xs text-ink-soft">nv. {{ spawn.level_min }}–{{ spawn.level_max }}</span>
            </div>
            <div v-if="spawn.biomes.length" class="flex flex-wrap gap-1">
              <span
                v-for="b in spawn.biomes"
                :key="b"
                class="rounded border border-edge-soft bg-white/[0.04] px-1.5 py-0.5 font-sans text-xs text-ink-soft"
              >{{ prettyBiome(b) }}</span>
            </div>
            <div v-if="spawn.labels.length" class="flex flex-wrap gap-1 font-sans text-xs text-faint">
              <span v-for="l in spawn.labels" :key="l" class="lowercase">{{ l }}</span>
            </div>
          </li>
        </ul>
      </section>


    </template>
  </div>
</template>
