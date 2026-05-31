<script setup lang="ts">
type Props = {
  label: string
  hint?: string
}

defineProps<Props>()
</script>

<template>
  <div class="flex flex-wrap items-baseline justify-between gap-3">
    <h2 class="font-display text-xl font-semibold tracking-tight text-ink">{{ label }}</h2>
    <span v-if="hint || $slots.hint" class="font-sans text-sm text-muted">
      <slot name="hint">{{ hint }}</slot>
    </span>
  </div>
</template>
