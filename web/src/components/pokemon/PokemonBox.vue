<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import PokemonGridCell from './PokemonGridCell.vue'
import { prettySpecies } from '@/utils/format'
import type { PcSlotDto } from '@/types'
import { ChevronLeft, ChevronRight } from '@lucide/vue'

type Props = {
  boxIndex: number
  totalBoxes: number
  slots: PcSlotDto[]
  selectedSlot?: number | null
  canPrev?: boolean
  canNext?: boolean
}

const props = withDefaults(defineProps<Props>(), { selectedSlot: null, canPrev: true, canNext: true })
const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
  (e: 'select', slotIndex: number): void
}>()

const COLS = 6
const ROWS = 5
const TOTAL = COLS * ROWS

const slotMap = computed(() => {
  const m = new Map<number, PcSlotDto>()
  for (const s of props.slots) m.set(s.slot_index, s)
  return m
})

const occupied = computed(() => props.slots.length)

function navBtnClass(enabled: boolean): string {
  return cn(
    'inline-flex h-8 w-8 items-center justify-center rounded-md border border-edge-soft transition-colors',
    enabled
      ? 'text-ink-soft hover:bg-white/[0.06] hover:text-ink'
      : 'text-quiet cursor-not-allowed',
  )
}
</script>

<template>
  <section class="overflow-hidden rounded-lg border border-edge-soft bg-white/[0.03] backdrop-blur-md">
    <header class="flex items-center justify-between gap-4 border-b border-edge-soft px-5 py-3">
      <button type="button" :class="navBtnClass(canPrev)" :disabled="!canPrev" aria-label="caja anterior" @click="emit('prev')">
        <ChevronLeft :size="16" />
      </button>

      <div class="flex flex-col items-center gap-0.5 text-center">
        <span class="font-display text-lg font-semibold text-ink">
          Caja {{ boxIndex + 1 }}
          <span class="font-display text-sm font-medium text-faint">/ {{ totalBoxes }}</span>
        </span>
        <span class="font-sans text-sm text-muted">
          <span class="text-green">{{ occupied }}</span> / {{ TOTAL }} ocupados
        </span>
      </div>

      <button type="button" :class="navBtnClass(canNext)" :disabled="!canNext" aria-label="caja siguiente" @click="emit('next')">
        <ChevronRight :size="16" />
      </button>
    </header>

    <div class="grid grid-cols-6 gap-2 p-5">
      <template v-for="i in TOTAL" :key="i - 1">
        <PokemonGridCell
          v-if="slotMap.get(i - 1)"
          :species-id="slotMap.get(i - 1)!.pokemon.species_id"
          :shiny="slotMap.get(i - 1)!.pokemon.shiny"
          :display-name="slotMap.get(i - 1)!.pokemon.nickname ?? prettySpecies(slotMap.get(i - 1)!.pokemon.species_id)"
          :sub-line="`nv. ${slotMap.get(i - 1)!.pokemon.level}`"
          :selected="selectedSlot === i - 1"
          @click="emit('select', i - 1)"
        />
        <button
          v-else
          type="button"
          :class="cn(
            'group relative flex aspect-square items-center justify-center rounded-md border border-dashed border-edge-soft bg-transparent transition-colors hover:border-edge',
            selectedSlot === i - 1 && 'ring-1 ring-yellow border-yellow',
          )"
          :aria-label="`slot vacío ${i}`"
          @click="emit('select', i - 1)"
        >
          <span class="font-mono text-xs text-quiet">{{ String(i).padStart(2, '0') }}</span>
        </button>
      </template>
    </div>
  </section>
</template>
