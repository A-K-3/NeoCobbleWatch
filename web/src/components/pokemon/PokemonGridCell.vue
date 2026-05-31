<script setup lang="ts">
import { computed } from 'vue'
import PokemonSprite from './PokemonSprite.vue'
import { cn } from '@/utils/cn'
import { useSpeciesStore } from '@/stores/species'
import { speciesTypes, typeBgColor, typeLabel } from '@/utils/pokemon'
import type { PokedexKnowledge } from '@/types'
import { Box, Check, Eye, Lock, Sparkles } from '@lucide/vue'

import { EVO_COLORS, type EvoReadiness } from '@/utils/evolution'

type Props = {
  speciesId: string
  displayName: string
  shiny?: boolean
  knowledge?: PokedexKnowledge
  subLine?: string
  selected?: boolean
  inParty?: boolean
  evoReadiness?: EvoReadiness | null
  pcCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  shiny: false,
  knowledge: undefined,
  subLine: undefined,
  selected: false,
  inParty: false,
  evoReadiness: null,
  pcCount: undefined,
})


defineEmits<{ click: [] }>()

const speciesStore = useSpeciesStore()

const types = computed(() => speciesTypes(speciesStore.info(props.speciesId)))

const KNOWLEDGE_BORDER: Record<PokedexKnowledge, string> = {
  CAUGHT: 'border-yellow/40 hover:border-yellow/70',
  ENCOUNTERED: 'border-blue/30 hover:border-blue/60',
  NONE: 'border-edge-soft',
}
const KNOWLEDGE_TONE: Record<PokedexKnowledge, string> = {
  CAUGHT: 'text-yellow',
  ENCOUNTERED: 'text-blue',
  NONE: 'text-faint',
}

const borderClass = computed(() =>
  props.knowledge ? KNOWLEDGE_BORDER[props.knowledge] : 'border-edge-soft hover:border-edge',
)
const isLocked = computed(() => props.knowledge === 'NONE')
</script>

<template>
  <button
    type="button"
    :class="cn(
      'group relative flex flex-col items-center gap-1.5 rounded-md border bg-white/[0.04] p-3 text-center backdrop-blur-md transition-all hover:bg-white/[0.07]',
      borderClass,
      isLocked && 'opacity-50 hover:opacity-100',
      selected && 'ring-2 ring-yellow/60',
    )"
    @click="$emit('click')"
  >
    <PokemonSprite
      :species-id="speciesId"
      :shiny="shiny"
      size="md"
      :class="isLocked ? 'grayscale transition-all duration-200 group-hover:filter-none' : ''"
    />
    <span class="w-full truncate font-sans text-xs text-ink-soft">{{ displayName }}</span>
    <div v-if="types.length" class="flex gap-1">
      <span
        v-for="t in types"
        :key="t"
        class="h-2 w-5 rounded-sm"
        :style="{ backgroundColor: typeBgColor(t) }"
        :title="typeLabel(t)"
      />
    </div>
    <span v-if="subLine" class="font-mono text-[10px] text-faint">{{ subLine }}</span>
    <span
      v-if="inParty"
      class="absolute left-1.5 top-1.5 rounded bg-green/20 px-1 font-mono text-[9px] font-bold leading-tight text-green"
    >E</span>
    <span v-if="knowledge" :class="cn('absolute right-1.5 top-1.5', KNOWLEDGE_TONE[knowledge])">
      <Check v-if="knowledge === 'CAUGHT'" :size="12" />
      <Eye v-else-if="knowledge === 'ENCOUNTERED'" :size="12" />
      <Lock v-else :size="12" />
    </span>
    <Sparkles v-else-if="shiny" :size="12" class="absolute right-1.5 top-1.5 text-yellow" />
    <span v-if="pcCount" class="absolute bottom-1.5 left-1.5 flex items-center gap-0.5 rounded bg-white/[0.10] px-1.5 py-0.5 font-mono text-[10px] font-bold leading-tight text-ink-soft">
      <Box :size="10" />
      <span v-if="pcCount > 1">×{{ pcCount }}</span>
    </span>
    <span v-if="evoReadiness && evoReadiness !== 'not_yet'" class="absolute bottom-1.5 right-1.5 flex h-2.5 w-2.5">
      <span :class="['absolute inline-flex h-full w-full animate-ping rounded-full opacity-75', EVO_COLORS[evoReadiness]]" />
      <span :class="['relative inline-flex h-2.5 w-2.5 rounded-full', EVO_COLORS[evoReadiness]]" />
    </span>
  </button>
</template>
