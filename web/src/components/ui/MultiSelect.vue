<script setup lang="ts" generic="T">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { cn } from '@/utils/cn'
import { Check, ChevronDown, X } from '@lucide/vue'

type Props = {
  modelValue: T[]
  options: readonly T[]
  /** Returns the string used by the search filter and the default label rendering. */
  labelFn: (v: T) => string
  /** Label shown on the trigger button when nothing is selected. */
  triggerLabel: string
  /** Placeholder for the search input. */
  searchPlaceholder?: string
  /** Whether to hide the search input (useful for short lists). */
  noSearch?: boolean
  /** Optional count shown next to each option. */
  countFn?: (v: T) => number
}

const props = withDefaults(defineProps<Props>(), {
  searchPlaceholder: 'Buscar...',
  noSearch: false,
})

const emit = defineEmits<{ 'update:modelValue': [v: T[]] }>()

const isOpen = ref(false)
const query = ref('')
const rootRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const searchRef = ref<HTMLInputElement | null>(null)
const dropdownStyle = ref({ top: '0px', left: '0px' })

const filteredOptions = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return props.options
  return props.options.filter((o) => props.labelFn(o).toLowerCase().includes(q))
})

function isSelected(o: T): boolean {
  return props.modelValue.includes(o)
}

function toggle(o: T) {
  if (isSelected(o)) emit('update:modelValue', props.modelValue.filter((x) => x !== o))
  else emit('update:modelValue', [...props.modelValue, o])
}

function clear() {
  emit('update:modelValue', [])
}

function updatePosition() {
  if (!rootRef.value) return
  const rect = rootRef.value.getBoundingClientRect()
  dropdownStyle.value = {
    top: `${rect.bottom + 4}px`,
    left: `${rect.left}px`,
  }
}

function onDocClick(e: MouseEvent) {
  const target = e.target as Node
  if (rootRef.value?.contains(target) || dropdownRef.value?.contains(target)) return
  isOpen.value = false
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape' && isOpen.value) isOpen.value = false
}

onMounted(() => {
  document.addEventListener('mousedown', onDocClick)
  document.addEventListener('keydown', onKey)
})
onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onDocClick)
  document.removeEventListener('keydown', onKey)
})

watch(isOpen, async (open) => {
  if (open) {
    updatePosition()
    if (!props.noSearch) {
      await nextTick()
      searchRef.value?.focus()
    }
  } else {
    query.value = ''
  }
})
</script>

<template>
  <div ref="rootRef" class="relative inline-block">
    <button
      type="button"
      :class="cn(
        'inline-flex items-center gap-1.5 rounded-md border px-2.5 py-1 font-sans text-xs font-medium transition-colors',
        modelValue.length
          ? 'border-yellow/40 bg-yellow/10 text-yellow'
          : 'border-edge-soft bg-white/[0.04] text-muted hover:border-edge hover:text-ink',
      )"
      @click="isOpen = !isOpen"
    >
      <span>{{ triggerLabel }}</span>
      <span v-if="modelValue.length" class="rounded-full bg-yellow/30 px-1.5 leading-tight">{{ modelValue.length }}</span>
      <ChevronDown :size="12" :class="isOpen ? 'rotate-180' : ''" class="transition-transform" />
    </button>

    <Teleport to="body">
      <div
        v-if="isOpen"
        ref="dropdownRef"
        class="fixed z-[9999] flex w-72 flex-col gap-2 rounded-md border border-edge bg-night-soft p-2 shadow-glass-lift"
        :style="dropdownStyle"
      >
        <div v-if="!noSearch" class="relative">
          <input
            ref="searchRef"
            v-model="query"
            type="search"
            :placeholder="searchPlaceholder"
            class="w-full rounded-md border border-edge-soft bg-white/[0.04] px-2 py-1 font-sans text-xs text-ink placeholder:text-faint focus:border-edge focus:outline-none"
          />
          <button
            v-if="query"
            type="button"
            class="absolute right-1.5 top-1/2 -translate-y-1/2 text-faint hover:text-ink"
            @click="query = ''"
          >
            <X :size="12" />
          </button>
        </div>

        <ul class="flex max-h-64 flex-col gap-0.5 overflow-y-auto">
          <li
            v-for="option in filteredOptions"
            :key="String(option)"
          >
            <button
              type="button"
              :class="cn(
                'flex w-full items-center gap-2 rounded px-1.5 py-1 text-left font-sans text-xs transition-colors hover:bg-white/[0.06]',
                isSelected(option) ? 'text-ink' : 'text-ink-soft',
              )"
              @click="toggle(option)"
            >
              <span
                :class="cn(
                  'flex h-3.5 w-3.5 shrink-0 items-center justify-center rounded border',
                  isSelected(option) ? 'border-yellow bg-yellow/20' : 'border-edge-soft',
                )"
              >
                <Check v-if="isSelected(option)" :size="10" class="text-yellow" />
              </span>
              <slot name="option" :option="option" :selected="isSelected(option)">
                <span class="flex-1">{{ labelFn(option) }}</span>
              </slot>
              <span v-if="countFn" class="ml-auto shrink-0 font-mono text-[10px] text-faint">{{ countFn(option) }}</span>
            </button>
          </li>
          <li v-if="!filteredOptions.length" class="px-1.5 py-1 font-sans text-xs text-faint">
            Sin resultados
          </li>
        </ul>

        <div v-if="modelValue.length" class="flex items-center justify-between border-t border-edge-soft pt-1.5">
          <span class="font-sans text-xs text-faint">{{ modelValue.length }} seleccionados</span>
          <button
            type="button"
            class="font-sans text-xs text-faint hover:text-ink"
            @click="clear"
          >
            Limpiar
          </button>
        </div>
      </div>
    </Teleport>
  </div>
</template>
