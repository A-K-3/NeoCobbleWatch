<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import { avatarUrl, bodyUrl } from '@/utils/crafatar'

type Props = {
  name: string
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  variant?: 'head' | 'body'
}

const props = withDefaults(defineProps<Props>(), { size: 'md', variant: 'head' })

const px = computed(() => {
  switch (props.size) {
    case 'xs': return 24
    case 'sm': return 40
    case 'md': return 64
    case 'lg': return 96
    case 'xl': return 144
  }
})

const url = computed(() =>
  props.variant === 'body' ? bodyUrl(props.name, 10) : avatarUrl(props.name, px.value * 2),
)

const wrapperClass = computed(() =>
  cn(
    'relative inline-block overflow-hidden border border-edge bg-white/[0.04] backdrop-blur-md',
    props.variant === 'head' ? 'rounded-md' : 'rounded-lg',
  ),
)
</script>

<template>
  <span
    :class="wrapperClass"
    :style="{ width: `${px}px`, height: variant === 'body' ? `${px * 2}px` : `${px}px` }"
  >
    <img
      :src="url"
      :alt="name"
      :width="px"
      :height="variant === 'body' ? px * 2 : px"
      class="h-full w-full object-contain"
      style="image-rendering: pixelated;"
      loading="lazy"
    />
  </span>
</template>
