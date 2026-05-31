<script setup lang="ts">
import { computed } from 'vue'
import type { StatBlock } from '@/types'

type Props = {
  ivs: StatBlock
  size?: number
}

const props = withDefaults(defineProps<Props>(), { size: 120 })

const MAX = 31
const STATS = ['hp', 'atk', 'def', 'spe', 'spd', 'spa'] as const
const LABELS: Record<typeof STATS[number], string> = {
  hp: 'HP', atk: 'AT', def: 'DF', spe: 'VEL', spd: 'DE', spa: 'AE',
}

const radius = computed(() => props.size / 2 - 14)
const center = computed(() => props.size / 2)

function polar(index: number, magnitude: number) {
  const angle = (Math.PI * 2 * index) / STATS.length - Math.PI / 2
  const r = radius.value * magnitude
  return {
    x: center.value + Math.cos(angle) * r,
    y: center.value + Math.sin(angle) * r,
  }
}

const ringPaths = computed(() =>
  [0.25, 0.5, 0.75, 1].map((m) =>
    STATS.map((_, i) => {
      const p = polar(i, m)
      return `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(2)} ${p.y.toFixed(2)}`
    }).join(' ') + ' Z',
  ),
)

const axisLines = computed(() =>
  STATS.map((_, i) => polar(i, 1)),
)

const fillPolygon = computed(() =>
  STATS.map((stat, i) => {
    const ratio = Math.min(1, props.ivs[stat] / MAX)
    const p = polar(i, ratio)
    return `${p.x.toFixed(2)},${p.y.toFixed(2)}`
  }).join(' '),
)

const labels = computed(() =>
  STATS.map((stat, i) => {
    const p = polar(i, 1.18)
    return {
      stat,
      label: LABELS[stat],
      value: props.ivs[stat],
      x: p.x,
      y: p.y,
      perfect: props.ivs[stat] === 31,
    }
  }),
)

const total = computed(() => Object.values(props.ivs).reduce((a, b) => a + b, 0))
const isPerfect = computed(() => total.value === MAX * 6)
</script>

<template>
  <div class="relative inline-flex flex-col items-center gap-1">
    <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`" class="overflow-visible">
      <!-- background rings -->
      <path
        v-for="(d, i) in ringPaths"
        :key="i"
        :d="d"
        fill="none"
        stroke="rgba(255,255,255,0.08)"
        :stroke-width="i === ringPaths.length - 1 ? 1 : 0.6"
        :opacity="i === ringPaths.length - 1 ? 1 : 0.7"
      />
      <!-- axes -->
      <line
        v-for="(p, i) in axisLines"
        :key="i"
        :x1="center"
        :y1="center"
        :x2="p.x"
        :y2="p.y"
        stroke="rgba(255,255,255,0.08)"
        stroke-width="0.6"
      />
      <!-- ivs polygon -->
      <polygon
        :points="fillPolygon"
        :fill="isPerfect ? 'rgba(255,214,10,0.20)' : 'rgba(58,255,157,0.18)'"
        :stroke="isPerfect ? 'var(--color-yellow)' : 'var(--color-green)'"
        stroke-width="1.4"
        stroke-linejoin="round"
      />
      <!-- iv dots -->
      <circle
        v-for="(l, i) in labels"
        :key="`d-${i}`"
        :cx="polar(i, Math.min(1, l.value / MAX)).x"
        :cy="polar(i, Math.min(1, l.value / MAX)).y"
        r="2.2"
        :fill="l.perfect ? 'var(--color-yellow)' : 'var(--color-green)'"
      />
      <!-- labels -->
      <text
        v-for="(l, i) in labels"
        :key="`l-${i}`"
        :x="l.x"
        :y="l.y"
        text-anchor="middle"
        dominant-baseline="middle"
        class="font-mono"
        :class="l.perfect ? 'fill-yellow' : 'fill-muted'"
        font-size="8"
      >{{ l.label }} <tspan :class="l.perfect ? 'fill-yellow' : 'fill-ink'" font-weight="700">{{ l.value }}</tspan></text>
    </svg>
  </div>
</template>
