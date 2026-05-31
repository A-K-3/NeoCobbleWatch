<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import { formatInt } from '@/utils/format'

type Tone = 'default' | 'red' | 'yellow' | 'green' | 'blue' | 'cyan' | 'magenta'

type Props = {
  label: string
  value: number | string | null | undefined
  hint?: string
  tone?: Tone
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), { tone: 'default', size: 'md' })

const display = computed(() =>
  typeof props.value === 'number' ? formatInt(props.value) : (props.value ?? '—'),
)

const TONE_VALUE: Record<Tone, string> = {
  default: 'text-ink',
  red:     'text-red',
  yellow:  'text-yellow',
  green:   'text-green',
  blue:    'text-blue',
  cyan:    'text-cyan',
  magenta: 'text-magenta',
}

const wrapperClass = computed(() =>
  cn(
    'flex flex-col gap-2 rounded-lg border border-edge bg-white/[0.04] backdrop-blur-md',
    props.size === 'sm' && 'p-4',
    props.size === 'md' && 'p-5',
    props.size === 'lg' && 'p-6',
  ),
)

const isZero = computed(() => props.value === 0 || props.value === '0')

const valueClass = computed(() =>
  cn(
    'font-display font-semibold leading-[0.95] tracking-tight',
    props.size === 'sm' && 'text-3xl',
    props.size === 'md' && 'text-5xl',
    props.size === 'lg' && 'text-7xl',
    isZero.value ? 'text-faint' : TONE_VALUE[props.tone],
  ),
)
</script>

<template>
  <div :class="wrapperClass">
    <div class="flex items-baseline justify-between gap-3">
      <span class="font-sans text-sm text-muted">{{ label }}</span>
      <span v-if="hint" class="font-sans text-xs text-faint">{{ hint }}</span>
    </div>
    <div :class="valueClass">{{ display }}</div>
  </div>
</template>
