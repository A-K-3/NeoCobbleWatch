<script setup lang="ts">
import PokeballMark from './PokeballMark.vue'

type Props = {
  label?: string
}

withDefaults(defineProps<Props>(), { label: 'Cargando...' })
</script>

<template>
  <div class="inline-flex items-center gap-2.5 text-muted">
    <PokeballMark :size="16" tone="red" accent="ink" spinning />
    <p class="font-sans text-sm">{{ label }}</p>
  </div>
</template>
