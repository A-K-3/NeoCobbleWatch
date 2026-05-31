<script setup lang="ts">
import { computed, watch } from 'vue'
import PokemonSprite from './PokemonSprite.vue'
import IVRadar from './IVRadar.vue'
import TypePill from '@/components/ui/TypePill.vue'
import LabelBadge from '@/components/ui/LabelBadge.vue'
import { prettify, prettySpecies, stripType } from '@/utils/format'
import { primaryBadge } from '@/utils/badges'
import { speciesTypes } from '@/utils/pokemon'
import { renderRequirement } from '@/utils/requirements'
import { evoReadiness, EVO_COLORS, EVO_LABELS } from '@/utils/evolution'
import { useSpeciesStore } from '@/stores/species'
import type { PokemonDto } from '@/types'
import Collapsible from '@/components/ui/Collapsible.vue'
import { ArrowRight, Sparkles } from '@lucide/vue'

const speciesStore = useSpeciesStore()

type Props = {
  pokemon: PokemonDto
  slotLabel?: string
}

const props = defineProps<Props>()


const ivTotal = computed(() => {
  const { hp, atk, def, spa, spd, spe } = props.pokemon.ivs
  return hp + atk + def + spa + spd + spe
})

const GENDER_GLYPH = { MALE: '♂', FEMALE: '♀' } as const
const GENDER_COLOR = { MALE: 'text-blue', FEMALE: 'text-magenta' } as const

const genderGlyph = computed(() => GENDER_GLYPH[props.pokemon.gender as 'MALE' | 'FEMALE'] ?? '⬡')
const genderColor = computed(() => GENDER_COLOR[props.pokemon.gender as 'MALE' | 'FEMALE'] ?? 'text-faint')

const teraSlug = computed(() => (props.pokemon.tera_type ? stripType(props.pokemon.tera_type) : null))

const displayName = computed(() => props.pokemon.nickname ?? prettySpecies(props.pokemon.species_id))

const hasAltForm = computed(() =>
  !!props.pokemon.form_name && props.pokemon.form_name.toLowerCase() !== 'normal',
)

const friendshipPct = computed(() => Math.min(100, (props.pokemon.friendship / 255) * 100))

const speciesInfo = computed(() => speciesStore.info(props.pokemon.species_id))

const rarityBadge = computed(() => primaryBadge(props.pokemon.species_id, speciesInfo.value))

const types = computed(() => speciesTypes(speciesInfo.value))

const showTera = computed(() => {
  if (!teraSlug.value) return false
  const slug = teraSlug.value.toLowerCase()
  return !types.value.some(t => t.toLowerCase() === slug)
})

watch(
  () => props.pokemon.species_id,
  (id) => { void speciesStore.loadDetail(id) },
  { immediate: true },
)

const evolutions = computed(() => {
  const all = speciesStore.detail(props.pokemon.species_id)?.evolutions ?? []
  const seen = new Set<string>()
  return all.filter(evo => {
    const key = `${evo.result_id}:${evo.result_name ?? ''}`
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
})
const readiness = computed(() => evoReadiness(props.pokemon, evolutions.value))

const EVO_PING = EVO_COLORS
const EVO_LABEL = EVO_LABELS
</script>

<template>
  <article class="flex flex-col gap-3 rounded-lg border border-edge-soft bg-white/[0.04] p-5 backdrop-blur-md transition-colors hover:border-edge">
    <header class="flex items-baseline justify-between gap-3 text-sm">
      <span v-if="slotLabel" class="text-muted">{{ slotLabel }}</span>
      <span v-else />
      <div class="flex items-baseline gap-3 text-muted">
        <span>nv. <span class="font-semibold text-ink">{{ pokemon.level }}</span></span>
        <span :class="['text-base leading-none', genderColor]">{{ genderGlyph }}</span>
        <Sparkles v-if="pokemon.shiny" :size="14" class="text-yellow" aria-label="shiny" />
      </div>
    </header>

    <!-- HERO: sprite + nombre + tags -->
    <div class="flex items-start gap-5">
      <div class="grid h-32 w-32 shrink-0 place-items-center rounded-lg border border-edge-soft bg-white/[0.02]">
        <PokemonSprite :species-id="pokemon.species_id" :shiny="pokemon.shiny" size="lg" />
      </div>
      <div class="flex min-w-0 flex-1 flex-col gap-2">
        <h3 class="truncate font-display text-3xl font-semibold leading-tight tracking-tight text-ink">
          {{ displayName }}
        </h3>
        <p v-if="pokemon.nickname" class="font-sans text-sm text-muted">
          {{ prettySpecies(pokemon.species_id) }}
        </p>
        <div class="flex flex-wrap items-baseline gap-2">
          <span v-if="speciesInfo" class="font-mono text-xs text-faint">#{{ String(speciesInfo.national_dex).padStart(4, '0') }}</span>
          <span v-if="speciesInfo" class="font-sans text-xs text-faint">Gen {{ speciesInfo.generation }}</span>
        </div>
        <div v-if="types.length" class="flex flex-wrap items-center gap-1.5">
          <TypePill v-for="t in types" :key="t" :type="t" size="sm" />
        </div>
        <div v-if="hasAltForm || showTera || rarityBadge" class="mt-1 flex flex-wrap items-center gap-2">
          <LabelBadge v-if="rarityBadge" :kind="rarityBadge" size="sm" />
          <span
            v-if="hasAltForm"
            class="inline-flex items-center rounded-md border border-edge-soft bg-white/[0.05] px-2 py-0.5 font-sans text-xs text-ink-soft"
          >{{ prettify(pokemon.form_name) }}</span>
          <span v-if="showTera" class="inline-flex items-center gap-1.5">
            <span class="font-sans text-xs text-faint">tera</span>
            <TypePill :type="teraSlug!" size="sm" />
          </span>
        </div>
      </div>
    </div>

    <!-- ATTRS colapsable -->
    <Collapsible label="Naturaleza, Ball &amp; Objeto" :hint="`${prettify(pokemon.nature)} · ${prettify(pokemon.ball)}`">
      <dl class="grid grid-cols-1 gap-x-6 gap-y-2.5 sm:grid-cols-2">
        <div class="flex flex-col gap-0.5 border-b border-edge-soft pb-1.5">
          <dt class="font-sans text-xs text-faint">Naturaleza</dt>
          <dd class="font-display text-base font-semibold text-ink">{{ prettify(pokemon.nature) }}</dd>
        </div>
        <div class="flex flex-col gap-0.5 border-b border-edge-soft pb-1.5">
          <dt class="font-sans text-xs text-faint">Poké Ball</dt>
          <dd class="font-display text-base font-semibold text-ink">{{ prettify(pokemon.ball) }}</dd>
        </div>
        <div v-if="pokemon.held_item" class="flex flex-col gap-0.5 border-b border-edge-soft pb-1.5">
          <dt class="font-sans text-xs text-faint">Objeto</dt>
          <dd class="font-display text-base font-semibold text-ink">{{ prettify(pokemon.held_item) }}</dd>
        </div>
      </dl>
    </Collapsible>

    <!-- AMISTAD + IVs colapsable -->
    <Collapsible label="Amistad &amp; IVs" :hint="`${ivTotal}/186 IVs`">
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-3 text-sm">
          <span class="text-faint">Amistad</span>
          <span class="relative h-1.5 flex-1 overflow-hidden rounded-full bg-white/[0.05]">
            <span class="absolute inset-y-0 left-0 rounded-full bg-red" :style="{ width: `${friendshipPct}%` }" />
          </span>
          <span class="font-mono text-xs text-ink">{{ pokemon.friendship }}<span class="text-faint">/255</span></span>
        </div>
        <div class="flex items-center justify-between gap-4 rounded-md border border-edge-soft bg-white/[0.02] p-3">
          <div class="flex flex-col gap-0.5">
            <span class="font-display text-base font-semibold text-ink">IVs</span>
            <span class="text-sm text-muted">
              Total <span :class="ivTotal === 186 ? 'font-bold text-yellow' : 'text-ink'">{{ ivTotal }}</span><span class="text-faint"> / 186</span>
            </span>
          </div>
          <IVRadar :ivs="pokemon.ivs" :size="120" />
        </div>
      </div>
    </Collapsible>


<!-- EVOLUTIONS -->
    <div v-if="evolutions.length" class="flex flex-col gap-2 border-t border-edge-soft pt-3">
      <div class="flex items-center gap-2">
        <span class="font-sans text-xs uppercase tracking-wider text-faint">Evoluciona a</span>
        <span v-if="readiness" class="flex items-center gap-1.5">
          <span class="relative flex h-2 w-2">
            <span :class="['absolute inline-flex h-full w-full animate-ping rounded-full opacity-75', EVO_PING[readiness]]" />
            <span :class="['relative inline-flex h-2 w-2 rounded-full', EVO_PING[readiness]]" />
          </span>
          <span class="font-sans text-xs" :class="readiness === 'ready' ? 'text-green' : 'text-yellow'">{{ EVO_LABEL[readiness] }}</span>
        </span>
      </div>
      <ul class="flex flex-col gap-1.5">
        <li
          v-for="(evo, i) in evolutions"
          :key="i"
          class="flex flex-col gap-1 rounded-md bg-white/[0.02] px-2.5 py-1.5"
        >
          <div class="flex items-center gap-2">
            <ArrowRight :size="14" class="text-faint" />
            <PokemonSprite v-if="evo.result_id" :species-id="evo.result_id" :aspects="evo.result_aspects" size="sm" />
            <span class="font-display text-base font-semibold text-ink">{{ evo.result_name ?? '???' }}</span>
          </div>
          <ul v-if="evo.requirements.length" class="ml-6 flex flex-wrap gap-x-3 gap-y-0.5 font-sans text-xs text-ink-soft">
            <li v-for="(req, j) in evo.requirements" :key="j" class="before:mr-1 before:text-faint before:content-['•']">
              {{ renderRequirement(req, pokemon) }}
            </li>
          </ul>
        </li>
      </ul>
    </div>
  </article>
</template>
