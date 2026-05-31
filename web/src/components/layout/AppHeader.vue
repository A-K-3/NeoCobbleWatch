<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { cn } from '@/utils/cn'
import { useServerStore } from '@/stores/server'
import { formatDuration } from '@/utils/format'
import PokeballMark from '@/components/ui/PokeballMark.vue'

const server = useServerStore()

onMounted(() => {
  void server.refresh()
})

const navItems = [
  { to: '/', label: 'Inicio' },
  { to: '/players', label: 'Entrenadores' },
  { to: '/tops', label: 'Liga' },
] as const

function linkClass(isActive: boolean) {
  return cn(
    'inline-flex items-center px-3 py-1.5 font-display text-sm transition-colors',
    isActive ? 'text-ink' : 'text-muted hover:text-ink',
  )
}
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-edge-soft bg-night-soft/60 backdrop-blur-xl">
    <div class="mx-auto flex max-w-6xl items-center gap-8 px-5 py-3.5 sm:px-8">
      <RouterLink to="/" class="group flex items-center gap-2.5">
        <PokeballMark :size="22" tone="red" accent="ink" class="transition-transform group-hover:rotate-180 group-hover:duration-700" />
        <span class="font-display text-lg font-semibold leading-none text-ink">Cobbleverse</span>
      </RouterLink>

      <nav class="hidden flex-1 items-center gap-2 sm:flex">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          custom
          v-slot="{ isActive, navigate, href }"
        >
          <a :href="href" :class="linkClass(isActive)" @click="navigate">
            {{ item.label }}
            <span
              v-if="isActive"
              class="ml-2 inline-block h-1 w-1 rounded-full bg-yellow align-middle"
            />
          </a>
        </RouterLink>
      </nav>

      <div class="ml-auto flex items-center gap-4 text-sm">
        <span class="inline-flex items-center gap-2 text-muted">
          <span
            :class="cn(
              'inline-block h-1.5 w-1.5 rounded-full',
              server.error ? 'bg-red' : 'bg-green',
            )"
          />
          <span :class="server.error ? 'text-red' : 'text-ink'">
            {{ server.error ? 'sin conexión' : 'en vivo' }}
          </span>
        </span>
        <span class="hidden text-muted md:inline">
          {{ server.online.length }} en línea
        </span>
        <span
          v-if="server.health?.last_full_cycle_age_seconds != null"
          class="hidden text-faint lg:inline"
        >
          ciclo · hace {{ formatDuration(server.health.last_full_cycle_age_seconds) }}
        </span>
      </div>
    </div>
  </header>
</template>
