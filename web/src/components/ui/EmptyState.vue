<script setup lang="ts">
type Props = {
  label?: string
  hint?: string
}

withDefaults(defineProps<Props>(), { label: 'Sin registros todavía.', hint: '' })
</script>

<template>
  <div class="rounded-lg border border-dashed border-edge-soft px-6 py-10 text-center">
    <p class="font-display text-xl font-medium text-ink-soft">{{ label }}</p>
    <p v-if="hint" class="mt-2 font-sans text-sm text-muted">{{ hint }}</p>
  </div>
</template>
