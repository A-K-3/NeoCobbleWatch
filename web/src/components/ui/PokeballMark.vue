<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'

type Tone = 'ink' | 'muted' | 'faint' | 'red' | 'yellow' | 'green' | 'blue' | 'cyan' | 'magenta'
type Accent = 'paper' | 'ink' | 'yellow'

type Props = {
  size?: number
  spinning?: boolean
  tone?: Tone
  accent?: Accent
}

const props = withDefaults(defineProps<Props>(), { size: 18, tone: 'red', accent: 'ink' })

const TONE_CLASS: Record<Tone, string> = {
  ink:     'text-ink',
  muted:   'text-muted',
  faint:   'text-faint',
  red:     'text-red',
  yellow:  'text-yellow',
  green:   'text-green',
  blue:    'text-blue',
  cyan:    'text-cyan',
  magenta: 'text-magenta',
}

const ACCENT_VAR: Record<Accent, string> = {
  paper:  '#ffffff',
  ink:    'var(--color-night)',
  yellow: 'var(--color-yellow)',
}

const toneClass = computed(() => TONE_CLASS[props.tone])
const accentVar = computed(() => ACCENT_VAR[props.accent])
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    :class="cn('inline-block shrink-0', toneClass, spinning && 'animate-[spin_2.4s_linear_infinite]')"
    aria-hidden="true"
  >
    <!-- outer ring -->
    <circle cx="12" cy="12" r="10.5" fill="currentColor" />
    <!-- equator -->
    <path d="M1.5 12 H 8.5 M 15.5 12 H 22.5" stroke="rgba(0,0,0,0.55)" stroke-width="1.6" stroke-linecap="round" />
    <!-- bottom half overlay (subtle gradient feel) -->
    <path d="M 1.5 12 A 10.5 10.5 0 0 0 22.5 12 Z" fill="rgba(0,0,0,0.2)" />
    <!-- center button -->
    <circle cx="12" cy="12" r="3.4" fill="rgba(0,0,0,0.85)" />
    <circle cx="12" cy="12" r="1.7" :fill="accentVar" />
    <!-- specular highlight -->
    <ellipse cx="9" cy="8" rx="3" ry="1.6" fill="rgba(255,255,255,0.35)" />
  </svg>
</template>
