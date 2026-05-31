<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import { formatInt } from '@/utils/format'
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from '@lucide/vue'

type Props = {
  limit: number
  offset: number
  total: number
}

const props = defineProps<Props>()
const emit = defineEmits<{ (e: 'update:offset', value: number): void }>()

const currentPage = computed(() => Math.floor(props.offset / props.limit) + 1)
const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.limit)))

const canPrev = computed(() => props.offset > 0)
const canNext = computed(() => props.offset + props.limit < props.total)

const rangeStart = computed(() => (props.total === 0 ? 0 : props.offset + 1))
const rangeEnd = computed(() => Math.min(props.offset + props.limit, props.total))

function btnClass(disabled: boolean): string {
  return cn(
    'inline-flex h-8 w-8 items-center justify-center rounded-md border border-edge-soft transition-colors',
    disabled
      ? 'text-quiet cursor-not-allowed'
      : 'text-ink-soft hover:bg-white/[0.06] hover:text-ink',
  )
}

function goPrev() { if (canPrev.value) emit('update:offset', Math.max(0, props.offset - props.limit)) }
function goNext() { if (canNext.value) emit('update:offset', props.offset + props.limit) }
function goFirst() { if (canPrev.value) emit('update:offset', 0) }
function goLast() { if (canNext.value) emit('update:offset', (totalPages.value - 1) * props.limit) }
</script>

<template>
  <div class="flex flex-wrap items-center justify-between gap-4 border-t border-edge-soft pt-4">
    <p class="font-sans text-sm text-muted">
      {{ formatInt(rangeStart) }}–{{ formatInt(rangeEnd) }}
      <span class="text-faint">de</span>
      {{ formatInt(total) }}
    </p>
    <div class="flex items-center gap-1.5">
      <button type="button" :class="btnClass(!canPrev)" :disabled="!canPrev" @click="goFirst" aria-label="primera página">
        <ChevronsLeft :size="16" />
      </button>
      <button type="button" :class="btnClass(!canPrev)" :disabled="!canPrev" @click="goPrev" aria-label="anterior">
        <ChevronLeft :size="16" />
      </button>
      <span class="px-2 font-sans text-sm text-muted">
        <span class="text-ink">{{ currentPage }}</span> / {{ totalPages }}
      </span>
      <button type="button" :class="btnClass(!canNext)" :disabled="!canNext" @click="goNext" aria-label="siguiente">
        <ChevronRight :size="16" />
      </button>
      <button type="button" :class="btnClass(!canNext)" :disabled="!canNext" @click="goLast" aria-label="última página">
        <ChevronsRight :size="16" />
      </button>
    </div>
  </div>
</template>
