<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import { BADGES, type BadgeKind } from '@/utils/badges'

type Props = {
  kind: BadgeKind
  size?: 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), { size: 'sm' })

const def = computed(() => BADGES[props.kind])

const iconSize = computed(() => (props.size === 'md' ? 14 : 12))

const classes = computed(() =>
  cn(
    'inline-flex items-center gap-1.5 rounded-md border bg-white/[0.04] font-sans font-medium backdrop-blur-md',
    def.value.textCls,
    def.value.borderCls,
    props.size === 'sm' && 'px-2 py-0.5 text-xs',
    props.size === 'md' && 'px-2.5 py-1 text-sm',
  ),
)
</script>

<template>
  <span :class="classes">
    <component :is="def.icon" :size="iconSize" />
    {{ def.name }}
  </span>
</template>
