<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { api } from '@/api/client'
import { useAsyncResource } from '@/composables/useAsyncResource'
import PageHeader from '@/components/ui/PageHeader.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import PlayerAvatar from '@/components/player/PlayerAvatar.vue'
import { cn } from '@/utils/cn'
import { formatInt } from '@/utils/format'
import { TOP_METRICS, TOP_METRICS_MINECRAFT, TOP_METRICS_POKEMON, type TopMetric } from '@/types'

type Props = { metric?: string }
const props = defineProps<Props>()

const route = useRoute()
const router = useRouter()

// ── Metadata ───────────────────────────────────────────────────────────────

type Category = 'pokemon' | 'minecraft'

const METRIC_CATEGORY: Record<TopMetric, Category> = {
  captures: 'pokemon', shinies: 'pokemon', pokedex_caught: 'pokemon',
  pvp_wins: 'pokemon', eggs_hatched: 'pokemon', trades: 'pokemon',
  level_ups: 'pokemon', evolutions: 'pokemon',
  play_time: 'minecraft', distance: 'minecraft', mob_kills: 'minecraft',
  damage_dealt: 'minecraft', fish_caught: 'minecraft',
  raids_completed: 'minecraft', badges: 'minecraft', deaths: 'minecraft', money: 'minecraft',
}

const METRIC_LABEL: Record<TopMetric, string> = {
  captures: 'Capturas',
  shinies: 'Shinies',
  pokedex_caught: 'Pokédex',
  pvp_wins: 'PvP',
  eggs_hatched: 'Crianza',
  trades: 'Intercambios',
  level_ups: 'Subidas nivel',
  evolutions: 'Evoluciones',
  play_time: 'Tiempo jugado',
  distance: 'Distancia',
  mob_kills: 'Mobs matados',
  damage_dealt: 'Daño infligido',
  fish_caught: 'Pesca',
  raids_completed: 'Raids',
  badges: 'Medallas',
  deaths: 'Muertes',
  money: 'Dinero',
}

const METRIC_TITLE: Record<TopMetric, string> = {
  captures: 'Más capturas',
  shinies: 'Cazadores shiny',
  pokedex_caught: 'Pokédex completa',
  pvp_wins: 'Campeones PvP',
  eggs_hatched: 'Criadores',
  trades: 'Intercambios',
  level_ups: 'Más subidas de nivel',
  evolutions: 'Más evoluciones',
  play_time: 'Más tiempo jugado',
  distance: 'Más distancia recorrida',
  mob_kills: 'Exterminadores',
  damage_dealt: 'Más daño infligido',
  fish_caught: 'Mejores pescadores',
  raids_completed: 'Héroes de raid',
  badges: 'Líderes de liga',
  deaths: 'El ranking que nadie quiere ganar',
  money: 'Más ricos',
}

const METRIC_SUBTITLE: Record<TopMetric, string> = {
  captures: 'Los que no pasan junto a hierba alta sin tirar una bola.',
  shinies: 'Paciencia y suerte — cazadores de variocolores.',
  pokedex_caught: 'Completistas, ordenados por especies únicas capturadas.',
  pvp_wins: 'Combaten. Ganan. Vuelven a combatir.',
  eggs_hatched: 'Reyes y reinas del paseo con la bici.',
  trades: 'Sin ellos esto no sería una liga.',
  level_ups: 'Los que más han entrenado a sus Pokémon acumulando niveles.',
  evolutions: 'Del huevo al último estadio — los más evolucionados.',
  play_time: 'Los que más horas han invertido en el servidor.',
  distance: 'Caminantes, voladores y nadadores. La suma de todos los cm recorridos.',
  mob_kills: 'Combatientes, agricultores y exterminadores de silverfish.',
  damage_dealt: 'Los que más daño han repartido en Minecraft (PvE + PvP).',
  fish_caught: 'Los que se sientan a pescar mientras el mundo arde.',
  raids_completed: 'Los que se han enfrentado a los bosses de raid y han salido vivos.',
  badges: 'Entrenadores ordenados por medallas de gimnasio conseguidas.',
  deaths: 'Cada muerte es una historia. Este ranking las cuenta todas.',
  money: 'Los que más CobbleDólares acumulan. El mercado manda.',
}

const METRIC_UNIT: Record<TopMetric, string> = {
  captures: 'capturas', shinies: 'shinies', pokedex_caught: 'especies',
  pvp_wins: 'victorias', eggs_hatched: 'eclosiones', trades: 'intercambios',
  level_ups: 'subidas', evolutions: 'evoluciones',
  play_time: '', distance: '', mob_kills: 'mobs',
  damage_dealt: 'daño', fish_caught: 'peces',
  raids_completed: 'raids', badges: 'medallas', deaths: 'muertes', money: '',
}

function formatValue(m: TopMetric, value: number): string {
  if (m === 'play_time') {
    const h = Math.floor(value / 72000)
    const min = Math.floor((value % 72000) / 1200)
    return min > 0 ? `${h}h ${min}m` : `${h}h`
  }
  if (m === 'distance') {
    const km = value / 100000
    return km >= 1 ? `${km.toFixed(1)} km` : `${Math.round(value / 100)} m`
  }
  if (m === 'damage_dealt') return formatInt(Math.round(value / 10))
  if (m === 'money') return `$ ${formatInt(value)}`
  return formatInt(value)
}

// ── Routing ────────────────────────────────────────────────────────────────

function parseMetric(raw: string | undefined): TopMetric {
  if (raw && (TOP_METRICS as readonly string[]).includes(raw)) return raw as TopMetric
  return 'captures'
}

const metric = computed<TopMetric>(() =>
  parseMetric(props.metric ?? (route.params.metric as string | undefined)),
)

const category = computed<Category>(() => METRIC_CATEGORY[metric.value])

const metricsInCategory = computed(() =>
  category.value === 'pokemon' ? TOP_METRICS_POKEMON : TOP_METRICS_MINECRAFT,
)

function selectMetric(m: TopMetric) {
  if (m === metric.value) return
  offset.value = 0
  void router.push({ name: 'tops', params: { metric: m } })
}

function selectCategory(cat: Category) {
  if (cat === category.value) return
  offset.value = 0
  const first = cat === 'pokemon' ? TOP_METRICS_POKEMON[0] : TOP_METRICS_MINECRAFT[0]
  void router.push({ name: 'tops', params: { metric: first } })
}

// ── Data ───────────────────────────────────────────────────────────────────

const limit = ref(50)
const offset = ref(0)

const { data, error, isLoading, reload } = useAsyncResource(
  () => api.tops(metric.value, limit.value, offset.value),
  { immediate: false },
)

watch([metric, offset], () => void reload(), { immediate: true })

// ── Ranking display ────────────────────────────────────────────────────────

const RANK_TONE: Record<number, string> = { 1: 'text-yellow', 2: 'text-cyan', 3: 'text-orange' }
const RANK_BORDER: Record<number, string> = { 1: 'border-yellow/40', 2: 'border-cyan/40', 3: 'border-orange/40' }
const RANK_MEDAL: Record<number, string> = { 1: 'oro', 2: 'plata', 3: 'bronce' }
const rankTone = (rank: number) => RANK_TONE[rank] ?? 'text-faint'

const podium = computed(() => data.value && data.value.data.length >= 3 ? data.value.data.slice(0, 3) : [])
const rest = computed(() => {
  if (!data.value) return []
  return data.value.data.length >= 3 ? data.value.data.slice(3) : data.value.data
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <PageHeader
      eyebrow="Rankings"
      :title="METRIC_TITLE[metric]"
      :subtitle="METRIC_SUBTITLE[metric]"
    />

    <!-- Category selector -->
    <div class="flex gap-2">
      <button
        v-for="(label, cat) in ({ pokemon: 'Pokémon', minecraft: 'Minecraft' } as Record<Category, string>)"
        :key="cat"
        type="button"
        :class="cn(
          'rounded-full border px-4 py-1.5 font-display text-sm font-semibold transition-colors',
          category === cat
            ? 'border-yellow bg-yellow/10 text-yellow'
            : 'border-edge-soft text-muted hover:border-edge hover:text-ink',
        )"
        @click="selectCategory(cat)"
      >{{ label }}</button>
    </div>

    <!-- Metric tabs for current category -->
    <div class="flex flex-wrap items-end gap-x-5 gap-y-1 border-b border-edge-soft">
      <button
        v-for="m in metricsInCategory"
        :key="m"
        type="button"
        :class="cn(
          '-mb-px border-b-2 pb-3 pt-1 font-display text-sm transition-colors',
          metric === m
            ? 'border-yellow text-ink'
            : 'border-transparent text-muted hover:text-ink',
        )"
        @click="selectMetric(m)"
      >{{ METRIC_LABEL[m] }}</button>
    </div>

    <LoadingState v-if="isLoading && !data" label="Cargando ranking..." />
    <ErrorState v-else-if="error && !data" :error="error" :on-retry="reload" />

    <template v-else-if="data">
      <EmptyState
        v-if="!data.data.length"
        label="No hay clasificación todavía."
        hint="En cuanto lleguen snapshots, los rankings aparecerán aquí."
      />

      <!-- Podium -->
      <div v-if="podium.length" class="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <RouterLink
          v-for="entry in podium"
          :key="entry.uuid"
          :to="{ name: 'player', params: { uuid: entry.uuid } }"
          :class="cn(
            'group flex flex-col items-center gap-3 rounded-lg border bg-white/[0.04] px-5 py-6 backdrop-blur-md transition-colors hover:bg-white/[0.07]',
            RANK_BORDER[entry.rank] ?? 'border-edge',
            entry.rank === 1 && 'sm:order-2 sm:-translate-y-2',
            entry.rank === 2 && 'sm:order-1',
            entry.rank === 3 && 'sm:order-3',
          )"
        >
          <div class="flex items-baseline gap-2">
            <span :class="cn('font-display text-5xl font-semibold leading-none tracking-tight', rankTone(entry.rank))">#{{ entry.rank }}</span>
            <span :class="cn('font-sans text-xs', rankTone(entry.rank))">{{ RANK_MEDAL[entry.rank] }}</span>
          </div>
          <PlayerAvatar :name="entry.name" :size="entry.rank === 1 ? 'lg' : 'md'" />
          <span class="truncate font-display text-xl font-semibold text-ink">{{ entry.name }}</span>
          <div class="flex items-baseline gap-1.5">
            <span class="font-display text-3xl font-semibold leading-none tracking-tight text-ink">
              {{ formatValue(metric, entry.value) }}
            </span>
            <span v-if="METRIC_UNIT[metric]" class="text-sm text-muted">{{ METRIC_UNIT[metric] }}</span>
          </div>
        </RouterLink>
      </div>

      <!-- List -->
      <ol v-if="rest.length" class="flex flex-col gap-2">
        <li v-for="entry in rest" :key="entry.uuid">
          <RouterLink
            :to="{ name: 'player', params: { uuid: entry.uuid } }"
            class="grid grid-cols-[3.5rem_2.5rem_1fr_auto] items-center gap-4 rounded-lg border border-edge-soft bg-white/[0.03] px-4 py-3 backdrop-blur-md transition-colors hover:border-edge hover:bg-white/[0.06]"
          >
            <span :class="cn('font-display text-2xl font-semibold leading-none tracking-tight', rankTone(entry.rank))">
              {{ String(entry.rank).padStart(2, '0') }}
            </span>
            <PlayerAvatar :name="entry.name" size="sm" />
            <span class="truncate font-display text-base font-semibold text-ink">{{ entry.name }}</span>
            <div class="flex items-baseline gap-2 text-right">
              <span class="font-display text-2xl font-semibold leading-none tracking-tight text-ink">
                {{ formatValue(metric, entry.value) }}
              </span>
              <span v-if="METRIC_UNIT[metric]" class="font-sans text-sm text-muted">{{ METRIC_UNIT[metric] }}</span>
            </div>
          </RouterLink>
        </li>
      </ol>

      <PaginationBar
        v-if="data.page.total > limit"
        :limit="data.page.limit"
        :offset="data.page.offset"
        :total="data.page.total"
        @update:offset="(v) => (offset = v)"
      />
    </template>
  </div>
</template>
