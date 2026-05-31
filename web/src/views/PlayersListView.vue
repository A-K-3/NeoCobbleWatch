<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { api } from '@/api/client'
import { useAsyncResource } from '@/composables/useAsyncResource'
import PageHeader from '@/components/ui/PageHeader.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PlayerRow from '@/components/player/PlayerRow.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import { formatInt } from '@/utils/format'
import { Search } from '@lucide/vue'

const limit = ref(50)
const offset = ref(0)
const search = ref('')

const { data, error, isLoading, reload } = useAsyncResource(() => api.players(limit.value, offset.value))

watch(offset, () => {
  void reload()
})

const filtered = computed(() => {
  if (!data.value) return []
  const q = search.value.trim().toLowerCase()
  if (!q) return data.value.data
  return data.value.data.filter((p) => p.name.toLowerCase().includes(q))
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <PageHeader
      eyebrow="Entrenadores"
      title="Todos los entrenadores"
      subtitle="Cada cuenta registrada en el servidor. Última conexión, estado actual y enlace a su ficha."
    >
      <template #actions>
        <div class="relative">
          <Search :size="16" class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-faint" />
          <input
            v-model="search"
            type="search"
            placeholder="Buscar por nombre..."
            class="w-64 rounded-md border border-edge-soft bg-white/[0.04] py-2 pl-9 pr-3 font-sans text-sm text-ink placeholder:text-faint focus:border-edge focus:bg-white/[0.06] focus:outline-none"
          />
        </div>
      </template>
    </PageHeader>

    <LoadingState v-if="isLoading && !data" label="Cargando entrenadores..." />
    <ErrorState v-else-if="error && !data" :error="error" :on-retry="reload" />

    <template v-else-if="data">
      <p class="text-sm text-muted">
        Mostrando <span class="font-semibold text-ink">{{ formatInt(filtered.length) }}</span>
        de <span class="font-semibold text-ink">{{ formatInt(data.page.total) }}</span> entrenadores
      </p>

      <EmptyState
        v-if="!filtered.length"
        :label="search ? `Ningún entrenador coincide con “${search}” en esta página.` : 'No hay entrenadores registrados aún.'"
        :hint="search ? 'Prueba a paginar, o limpia la búsqueda.' : 'Aparecerán aquí tras su primer snapshot.'"
      />

      <div v-else class="grid grid-cols-1 gap-3 lg:grid-cols-2">
        <PlayerRow
          v-for="p in filtered"
          :key="p.uuid"
          :uuid="p.uuid"
          :name="p.name"
          :online="p.online"
          :last-seen="p.last_seen"
        />
      </div>

      <PaginationBar
        v-if="data.page.total > limit"
        :limit="data.page.limit"
        :offset="data.page.offset"
        :total="data.page.total"
        @update:offset="(v) => (offset = v)"
      />
    </template>
  </div>
</template>
