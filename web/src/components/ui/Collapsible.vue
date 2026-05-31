<script setup lang="ts">
import { ref } from 'vue'
import { ChevronDown } from '@lucide/vue'

defineProps<{ label: string; hint?: string }>()

const open = ref(false)
</script>

<template>
  <div class="flex flex-col gap-0">
    <button
      type="button"
      class="flex w-full items-center justify-between gap-2 rounded-md px-1 py-1.5 font-sans text-xs text-faint transition-colors hover:bg-white/[0.04] hover:text-ink-soft"
      @click="open = !open"
    >
      <span>{{ label }}</span>
      <span class="flex items-center gap-2">
        <span v-if="!open && hint" class="font-sans text-[11px] text-faint">{{ hint }}</span>
        <ChevronDown :size="14" :class="open ? 'rotate-180' : ''" class="transition-transform" />
      </span>
    </button>
    <div v-if="open" class="pt-2">
      <slot />
    </div>
  </div>
</template>
