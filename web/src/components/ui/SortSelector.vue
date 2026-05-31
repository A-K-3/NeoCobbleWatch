<script setup lang="ts" generic="T extends string">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { cn } from '@/utils/cn'
import { ArrowDownUp, Check, ChevronDown } from '@lucide/vue'

type Option = { value: T; label: string }

type Props = {
  modelValue: T
  options: Option[]
  label?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{ 'update:modelValue': [v: T] }>()

const isOpen = ref(false)
const rootRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const dropdownStyle = ref({ top: '0px', left: '0px' })

function currentLabel(): string {
  return props.options.find((o) => o.value === props.modelValue)?.label ?? ''
}

function pick(value: T) {
  emit('update:modelValue', value)
  isOpen.value = false
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

watch(isOpen, (open) => {
  if (open) updatePosition()
})
</script>

<template>
  <div ref="rootRef" class="inline-flex items-center gap-2 font-sans text-xs">
    <ArrowDownUp :size="12" class="text-faint" />
    <span v-if="label" class="text-faint">{{ label }}</span>
    <div class="relative inline-block">
      <button
        type="button"
        :class="cn(
          'inline-flex items-center gap-1.5 rounded-md border border-edge-soft bg-white/[0.04] px-2.5 py-1 font-sans text-xs font-medium text-ink transition-colors hover:border-edge',
        )"
        @click="isOpen = !isOpen"
      >
        <span>{{ currentLabel() }}</span>
        <ChevronDown :size="12" :class="isOpen ? 'rotate-180' : ''" class="transition-transform text-faint" />
      </button>
    </div>

    <Teleport to="body">
      <ul
        v-if="isOpen"
        ref="dropdownRef"
        class="fixed z-[9999] flex w-max flex-col gap-0.5 rounded-md border border-edge bg-night-soft p-1 shadow-glass-lift"
        :style="dropdownStyle"
      >
        <li v-for="o in options" :key="o.value">
          <button
            type="button"
            :class="cn(
              'flex w-full items-center gap-2 rounded px-2 py-1 text-left font-sans text-xs transition-colors hover:bg-white/[0.06]',
              modelValue === o.value ? 'text-ink' : 'text-ink-soft',
            )"
            @click="pick(o.value)"
          >
            <span
              :class="cn(
                'flex h-3.5 w-3.5 shrink-0 items-center justify-center rounded-full border',
                modelValue === o.value ? 'border-yellow bg-yellow/20' : 'border-edge-soft',
              )"
            >
              <Check v-if="modelValue === o.value" :size="10" class="text-yellow" />
            </span>
            {{ o.label }}
          </button>
        </li>
      </ul>
    </Teleport>
  </div>
</template>
