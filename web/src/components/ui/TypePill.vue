<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import { stripType } from '@/utils/format'
import { typeBgColor, typeLabel } from '@/utils/pokemon'

type Props = {
  type: string
  size?: 'xs' | 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), { size: 'sm' })

const slug = computed(() => stripType(props.type))

const classes = computed(() =>
  cn(
    'inline-flex items-center justify-center rounded border font-sans font-medium',
    props.size === 'xs' && 'px-1 py-px text-[10px] leading-tight',
    props.size === 'sm' && 'px-2 py-0.5 text-xs',
    props.size === 'md' && 'px-2.5 py-1 text-sm',
  ),
)

const pillStyle = computed(() => {
  const c = typeBgColor(slug.value)
  if (c.startsWith('#')) {
    return { backgroundColor: c + '28', borderColor: c + '80', color: c }
  }
  return { backgroundColor: 'rgba(255,255,255,0.05)', borderColor: 'rgba(255,255,255,0.2)', color: 'rgba(255,255,255,0.6)' }
})
</script>

<template>
  <span :class="classes" :style="pillStyle">{{ typeLabel(slug) }}</span>
</template>
