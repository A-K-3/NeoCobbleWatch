<script setup lang="ts">
import { RouterLink } from 'vue-router'
import PlayerAvatar from './PlayerAvatar.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import { formatRelative, formatAbsolute } from '@/utils/format'
import { ChevronRight } from '@lucide/vue'

type Props = {
  uuid: string
  name: string
  online: boolean
  lastSeen: number
}

defineProps<Props>()
</script>

<template>
  <RouterLink
    :to="{ name: 'player', params: { uuid } }"
    class="group flex items-center gap-4 rounded-lg border border-edge-soft bg-white/[0.03] px-4 py-3 backdrop-blur-md transition-colors hover:border-edge hover:bg-white/[0.06]"
  >
    <PlayerAvatar :name="name" size="sm" />
    <div class="flex min-w-0 flex-1 flex-col gap-0.5">
      <div class="flex items-center gap-2">
        <span class="truncate font-display text-base font-semibold text-ink">{{ name }}</span>
        <StatusDot :online="online" size="sm" />
      </div>
      <span class="font-sans text-sm text-muted" :title="formatAbsolute(lastSeen)">
        <template v-if="online">en línea · </template>
        <template v-else>visto </template>
        <span class="text-ink-soft">{{ formatRelative(lastSeen) }}</span>
      </span>
    </div>
    <ChevronRight :size="18" class="text-faint transition-colors group-hover:text-ink" />
  </RouterLink>
</template>
