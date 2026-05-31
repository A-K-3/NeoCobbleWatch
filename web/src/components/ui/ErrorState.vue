<script setup lang="ts">
import { ApiError } from '@/api/client'
import { AlertCircle, RefreshCcw } from '@lucide/vue'

type Props = {
  error: Error | ApiError | null
  onRetry?: () => void
}

defineProps<Props>()

function describe(e: Error | ApiError | null): string {
  if (e instanceof ApiError) return `${e.code ?? e.status} — ${e.message}`
  return e?.message ?? 'error desconocido'
}
</script>

<template>
  <div class="flex flex-col items-start gap-3 rounded-lg border border-red/40 bg-red-soft px-5 py-4 backdrop-blur-md">
    <div class="flex items-center gap-2 text-red">
      <AlertCircle :size="18" />
      <span class="font-display text-lg font-semibold">No se pudo cargar.</span>
    </div>
    <pre class="w-full whitespace-pre-wrap rounded-md border border-edge-soft bg-night-soft/60 px-3 py-2 font-mono text-xs text-ink-soft">{{ describe(error) }}</pre>
    <button
      v-if="onRetry"
      type="button"
      class="inline-flex items-center gap-2 rounded-md border border-edge bg-white/[0.06] px-3 py-1.5 font-sans text-sm text-ink transition-colors hover:bg-white/15"
      @click="onRetry?.()"
    >
      <RefreshCcw :size="14" />
      Reintentar
    </button>
  </div>
</template>
