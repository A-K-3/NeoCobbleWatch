<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useServerStore } from '@/stores/server'
import StatCard from '@/components/ui/StatCard.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PlayerAvatar from '@/components/player/PlayerAvatar.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import { formatInt, formatRelative } from '@/utils/format'
import { ArrowLeftRight, Egg, RefreshCcw, Swords } from '@lucide/vue'

const server = useServerStore()

const captureRate = computed(() => {
  const s = server.stats
  if (!s || s.total_capture_count === 0) return null
  return ((s.total_shiny_capture_count / s.total_capture_count) * 100).toFixed(2)
})
</script>

<template>
  <div class="flex flex-col gap-12">
    <PageHeader
      eyebrow="Resumen del servidor"
      title="La liga Cobbleverse"
      subtitle="Todo lo que pasa en el servidor en un vistazo. Capturas, batallas, criadero, entrenadores activos."
    >
      <template #actions>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-md border border-edge-soft bg-white/[0.04] px-3 py-1.5 font-sans text-sm text-ink-soft backdrop-blur-md transition-colors hover:bg-white/[0.08] hover:text-ink disabled:cursor-not-allowed disabled:text-faint"
          :disabled="server.isRefreshing"
          @click="server.refresh()"
        >
          <RefreshCcw :size="14" :class="server.isRefreshing ? 'animate-spin' : ''" />
          {{ server.isRefreshing ? 'Sincronizando...' : 'Sincronizar' }}
        </button>
      </template>
    </PageHeader>

    <LoadingState v-if="server.isRefreshing && !server.stats" label="Cargando..." />
    <ErrorState v-else-if="server.error && !server.stats" :error="server.error" :on-retry="() => server.refresh()" />

    <template v-else-if="server.stats">
      <!-- HERO + side stats -->
      <section class="grid grid-cols-1 gap-5 lg:grid-cols-3">
        <article class="flex flex-col justify-between gap-5 rounded-lg border border-edge bg-white/[0.04] p-7 backdrop-blur-md lg:col-span-2">
          <header class="flex items-baseline justify-between gap-4 text-sm text-muted">
            <span>Total acumulado</span>
            <span class="text-faint">todos los entrenadores</span>
          </header>

          <div class="flex items-baseline gap-4">
            <span class="font-display text-7xl font-semibold leading-[0.9] tracking-tight text-ink md:text-8xl">
              {{ server.stats.total_capture_count.toLocaleString('es-ES') }}
            </span>
            <span class="font-display text-xl text-muted">pokémon capturados</span>
          </div>

          <footer class="flex flex-wrap gap-x-6 gap-y-2 text-sm text-muted">
            <span><span class="font-semibold text-yellow">{{ server.stats.total_shiny_capture_count }}</span> shinies</span>
            <span v-if="captureRate">ratio <span class="font-semibold text-ink">{{ captureRate }}%</span></span>
            <span><span class="font-semibold text-ink">{{ server.stats.total_eggs_hatched }}</span> huevos eclosionados</span>
          </footer>
        </article>

        <div class="flex flex-col gap-5">
          <StatCard
            label="Entrenadores"
            :value="server.stats.total_players"
            :hint="server.stats.online_players === 1 ? '1 en línea' : `${server.stats.online_players} en línea`"
            size="md"
          />
          <StatCard
            label="Victorias PvP"
            :value="server.stats.total_pvp_battle_victory_count"
            tone="blue"
            size="md"
          />
        </div>
      </section>

      <!-- LEDGER (compact strip) -->
      <section class="flex flex-col gap-5">
        <SectionTitle label="Combate y cría" />
        <div class="grid grid-cols-2 divide-x divide-edge-soft overflow-hidden rounded-lg border border-edge-soft bg-white/[0.04] backdrop-blur-md sm:grid-cols-3 md:grid-cols-5">
          <div class="flex flex-col gap-1 px-5 py-4">
            <span class="text-sm text-muted">Victorias</span>
            <span class="font-display text-3xl font-semibold leading-none tracking-tight text-ink">{{ formatInt(server.stats.total_battle_victory_count) }}</span>
          </div>
          <div class="flex flex-col gap-1 px-5 py-4">
            <span class="flex items-center gap-1.5 text-sm text-muted">
              <Swords :size="12" /> PvP
            </span>
            <span class="font-display text-3xl font-semibold leading-none tracking-tight text-blue">{{ formatInt(server.stats.total_pvp_battle_victory_count) }}</span>
          </div>
          <div class="flex flex-col gap-1 px-5 py-4">
            <span class="text-sm text-muted">Evoluciones</span>
            <span class="font-display text-3xl font-semibold leading-none tracking-tight text-cyan">{{ formatInt(server.stats.total_evolved_count) }}</span>
          </div>
          <div class="flex flex-col gap-1 px-5 py-4">
            <span class="flex items-center gap-1.5 text-sm text-muted">
              <Egg :size="12" /> Huevos
            </span>
            <span class="font-display text-3xl font-semibold leading-none tracking-tight text-ink">{{ formatInt(server.stats.total_eggs_hatched) }}</span>
          </div>
          <div class="flex flex-col gap-1 px-5 py-4">
            <span class="flex items-center gap-1.5 text-sm text-muted">
              <ArrowLeftRight :size="12" /> Intercambios
            </span>
            <span class="font-display text-3xl font-semibold leading-none tracking-tight text-ink">{{ formatInt(server.stats.total_traded_count) }}</span>
          </div>
        </div>
      </section>

      <!-- ROSTER -->
      <section class="flex flex-col gap-5">
        <SectionTitle label="En el campo ahora">
          <template #hint>
            <span class="text-green">{{ server.online.length }}</span>
            {{ server.online.length === 1 ? 'entrenador activo' : 'entrenadores activos' }}
          </template>
        </SectionTitle>

        <EmptyState
          v-if="!server.online.length"
          label="No hay nadie en línea."
          hint="Vuelve tras el próximo ciclo."
        />

        <div v-else class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
          <RouterLink
            v-for="p in server.online"
            :key="p.uuid"
            :to="{ name: 'player', params: { uuid: p.uuid } }"
            class="group flex flex-col items-center gap-2.5 rounded-lg border border-edge-soft bg-white/[0.03] p-3 backdrop-blur-md transition-colors hover:border-edge hover:bg-white/[0.06]"
          >
            <PlayerAvatar :name="p.name" size="md" />
            <div class="flex w-full items-center justify-center gap-1.5 text-center">
              <StatusDot :online="true" size="sm" />
              <span class="truncate font-display text-sm font-semibold text-ink">{{ p.name }}</span>
            </div>
          </RouterLink>
        </div>
      </section>

      <!-- footnote -->
      <div class="flex items-center justify-between gap-3 border-t border-edge-soft pt-5 text-sm text-muted">
        <span>último snapshot · <span class="text-ink-soft">{{ formatRelative(server.lastRefreshedAt) }}</span></span>
        <span v-if="server.health?.version" class="font-mono text-xs text-faint">build {{ server.health.version }}</span>
      </div>
    </template>
  </div>
</template>
