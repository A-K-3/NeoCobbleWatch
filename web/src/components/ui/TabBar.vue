<script setup lang="ts" generic="T extends string">
import { cn } from '@/utils/cn'

type Tab = {
  id: T
  label: string
  count?: number | string | null
}

type Props = {
  modelValue: T
  tabs: ReadonlyArray<Tab>
}

defineProps<Props>()
defineEmits<{ (e: 'update:modelValue', value: T): void }>()
</script>

<template>
  <div class="flex flex-wrap items-end gap-6 border-b border-edge-soft">
    <button
      v-for="tab in tabs"
      :key="tab.id"
      type="button"
      :class="cn(
        'group relative -mb-px inline-flex items-baseline gap-2 border-b-2 px-1 pb-3 pt-1 font-display text-base transition-colors',
        modelValue === tab.id
          ? 'border-yellow text-ink'
          : 'border-transparent text-muted hover:text-ink',
      )"
      @click="$emit('update:modelValue', tab.id)"
    >
      <span>{{ tab.label }}</span>
      <span
        v-if="tab.count != null"
        :class="cn(
          'font-mono text-xs',
          modelValue === tab.id ? 'text-yellow' : 'text-faint',
        )"
      >{{ tab.count }}</span>
    </button>
  </div>
</template>
