<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { X } from '@lucide/vue'

defineProps<{ title?: string }>()
const emit = defineEmits<{ close: [] }>()

function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape') emit('close')
}

onMounted(() => {
  document.addEventListener('keydown', onKey)
  document.body.style.overflow = 'hidden'
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 z-[9998] flex items-start justify-center overflow-y-auto bg-black/60 px-4 py-12 backdrop-blur-sm"
      @mousedown.self="emit('close')"
    >
      <div class="relative w-full max-w-2xl rounded-xl border border-edge bg-night-soft shadow-glass-lift">
        <header v-if="title || $slots.header" class="flex items-center justify-between gap-4 border-b border-edge-soft px-6 py-4">
          <slot name="header">
            <span class="font-display text-lg font-semibold text-ink">{{ title }}</span>
          </slot>
          <button
            type="button"
            class="ml-auto flex h-7 w-7 items-center justify-center rounded-md text-faint transition-colors hover:bg-white/[0.06] hover:text-ink"
            aria-label="Cerrar"
            @click="emit('close')"
          >
            <X :size="16" />
          </button>
        </header>
        <div class="p-6">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>
