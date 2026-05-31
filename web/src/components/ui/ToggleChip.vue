<script setup lang="ts">
import { computed, type Component } from 'vue'
import { cn } from '@/utils/cn'

type Tone = 'yellow' | 'cyan' | 'magenta' | 'blue' | 'green' | 'red' | 'orange'

type Props = {
  selected: boolean
  tone?: Tone
  icon?: Component
}

const props = withDefaults(defineProps<Props>(), { tone: 'yellow' })

defineEmits<{ toggle: [] }>()

const TONES: Record<Tone, string> = {
  yellow:  'border-yellow/40 bg-yellow/10 text-yellow',
  cyan:    'border-cyan/40 bg-cyan/10 text-cyan',
  magenta: 'border-magenta/40 bg-magenta/10 text-magenta',
  blue:    'border-blue/40 bg-blue/10 text-blue',
  green:   'border-green/40 bg-green/10 text-green',
  red:     'border-red/40 bg-red/10 text-red',
  orange:  'border-orange/40 bg-orange/10 text-orange',
}

const classes = computed(() => cn(
  'inline-flex items-center gap-1.5 rounded-md border px-2.5 py-1 font-sans text-xs font-medium transition-colors',
  props.selected ? TONES[props.tone] : 'border-edge-soft text-muted hover:text-ink',
))
</script>

<template>
  <button type="button" :class="classes" @click="$emit('toggle')">
    <component :is="icon" v-if="icon && selected" :size="12" />
    <slot />
  </button>
</template>
