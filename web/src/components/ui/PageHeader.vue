<script setup lang="ts">
type Props = {
  eyebrow?: string
  title: string
  subtitle?: string
}

defineProps<Props>()
</script>

<template>
  <header class="flex flex-col gap-3 pb-4">
    <p
      v-if="eyebrow || $slots.eyebrow"
      class="font-sans text-sm text-muted"
    >
      <slot name="eyebrow">{{ eyebrow }}</slot>
    </p>
    <div class="flex flex-wrap items-end justify-between gap-5">
      <h1 class="font-display text-4xl font-semibold leading-[1.05] tracking-tight text-ink md:text-5xl">{{ title }}</h1>
      <div v-if="$slots.actions" class="flex items-center gap-2">
        <slot name="actions" />
      </div>
    </div>
    <p v-if="subtitle || $slots.subtitle" class="max-w-2xl font-sans text-base text-muted">
      <slot name="subtitle">{{ subtitle }}</slot>
    </p>
  </header>
</template>
