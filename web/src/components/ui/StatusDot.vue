<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'

type Props = {
  online: boolean
  size?: 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), { size: 'md' })

const classes = computed(() =>
  cn(
    'inline-block rounded-full',
    props.size === 'sm' ? 'h-1.5 w-1.5' : 'h-2 w-2',
    props.online ? 'bg-green' : 'bg-quiet',
  ),
)
</script>

<template>
  <span :class="classes" :aria-label="online ? 'en línea' : 'desconectado'" role="status" />
</template>
