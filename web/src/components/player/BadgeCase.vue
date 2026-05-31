<script setup lang="ts">
import { computed } from 'vue'
import { BADGE_BY_ID, GYM_REGIONS, TROPHY_IDS, type GymBadgeInfo } from '@/utils/gym-badges'
import { typeBgColor, typeOnBgTextClass } from '@/utils/pokemon'
import { Trophy } from '@lucide/vue'

type Props = { badges: string[] }
const props = defineProps<Props>()

const earnedSet = computed(() => new Set(props.badges))

const unknownBadges = computed(() =>
  props.badges.filter((b) => !BADGE_BY_ID[b] && !TROPHY_IDS.has(b)),
)

function badgeCount(badges: GymBadgeInfo[]): number {
  return badges.filter((b) => earnedSet.value.has(b.id)).length
}

function prettyId(id: string): string {
  return id.split(':')[1]?.replace(/_/g, ' ') ?? id
}
</script>

<template>
  <div class="flex flex-col gap-6">
    <div v-for="region in GYM_REGIONS" :key="region.key" class="flex flex-col gap-3">
      <!-- Region header -->
      <div class="flex items-center justify-between gap-3">
        <h3 class="font-display text-base font-semibold tracking-tight text-ink">{{ region.label }}</h3>
        <span class="font-mono text-xs">
          <span :class="badgeCount(region.badges) === region.badges.length ? 'font-bold text-yellow' : 'text-ink-soft'">
            {{ badgeCount(region.badges) }}
          </span>
          <span class="text-faint"> / {{ region.badges.length }}</span>
        </span>
      </div>

      <!-- Badge grid -->
      <div class="flex flex-wrap gap-3">
        <div
          v-for="badge in region.badges"
          :key="badge.id"
          class="flex flex-col items-center gap-1.5"
          :title="`${badge.name} — ${badge.leader} (${badge.city})`"
        >
          <div
            class="relative h-12 w-12 rounded-full border-2 transition-all duration-200"
            :style="earnedSet.has(badge.id)
              ? { backgroundColor: typeBgColor(badge.type), borderColor: typeBgColor(badge.type), boxShadow: `0 0 12px ${typeBgColor(badge.type)}60` }
              : { backgroundColor: 'rgba(255,255,255,0.04)', borderColor: 'rgba(255,255,255,0.10)' }"
            :class="earnedSet.has(badge.id) ? '' : 'opacity-25'"
          >
            <div
              class="absolute inset-1.5 rounded-full border"
              :style="{ borderColor: earnedSet.has(badge.id) ? 'rgba(255,255,255,0.45)' : 'rgba(255,255,255,0.12)' }"
            />
            <span
              class="absolute inset-0 flex items-center justify-center font-display text-[11px] font-bold"
              :class="earnedSet.has(badge.id) ? typeOnBgTextClass(badge.type) : 'text-faint'"
            >{{ badge.name.charAt(0) }}</span>
          </div>
          <span
            class="w-12 text-center font-sans text-[9px] leading-tight"
            :class="earnedSet.has(badge.id) ? 'text-ink-soft' : 'text-faint'"
          >{{ badge.name }}</span>
        </div>
      </div>

      <!-- Trophies -->
      <div v-if="region.trophies?.length" class="flex flex-wrap gap-2 pt-0.5">
        <div
          v-for="trophy in region.trophies"
          :key="trophy.id"
          class="flex items-center gap-2 rounded-md border px-3 py-1.5 transition-all"
          :class="earnedSet.has(trophy.id)
            ? 'border-yellow/50 bg-yellow/10'
            : 'border-edge-soft opacity-25'"
        >
          <Trophy :size="12" :class="earnedSet.has(trophy.id) ? 'text-yellow' : 'text-faint'" />
          <span class="font-sans text-xs" :class="earnedSet.has(trophy.id) ? 'text-yellow' : 'text-faint'">
            {{ trophy.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- Unknown badges (not in registry) -->
    <div v-if="unknownBadges.length" class="flex flex-col gap-2">
      <h3 class="font-display text-sm font-semibold text-muted">Otros</h3>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="b in unknownBadges"
          :key="b"
          class="rounded border border-yellow/30 bg-yellow/10 px-2 py-1 font-mono text-xs text-yellow"
        >{{ prettyId(b) }}</span>
      </div>
    </div>
  </div>
</template>
