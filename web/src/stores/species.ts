import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/api/client'
import type { SpeciesDetailDto, SpeciesInfoDto } from '@/types'
import { type BadgeKind, speciesBadges } from '@/utils/badges'

/**
 * Cobblemon species labels (legendary/mythical/etc., generation, fully-evolved).
 * Fetched once at app mount and cached for the session.
 *
 * Keys are species ids in `namespace:path` form (e.g. `cobblemon:bulbasaur`).
 */
export const useSpeciesStore = defineStore('species', () => {
  const byId = ref<Record<string, SpeciesInfoDto>>({})
  const isLoading = ref(false)
  const error = ref<Error | null>(null)
  const loadedAt = ref<number | null>(null)

  async function load(force = false): Promise<void> {
    if (!force && (loadedAt.value || isLoading.value)) return
    isLoading.value = true
    error.value = null
    try {
      const res = await api.speciesLabels()
      byId.value = res.data
      loadedAt.value = Date.now()
    } catch (e) {
      error.value = e as Error
    } finally {
      isLoading.value = false
    }
  }

  function info(speciesId: string): SpeciesInfoDto | null {
    return byId.value[speciesId] ?? null
  }

  function hasLabel(speciesId: string, label: string): boolean {
    return info(speciesId)?.labels.includes(label) ?? false
  }

  const totalByBadge = computed<Partial<Record<BadgeKind, number>>>(() => {
    const acc: Partial<Record<BadgeKind, number>> = {}
    for (const [id, info] of Object.entries(byId.value)) {
      for (const b of speciesBadges(id, info)) {
        acc[b] = (acc[b] ?? 0) + 1
      }
    }
    return acc
  })

  const totalByGeneration = computed<Record<number, number>>(() => {
    const acc: Record<number, number> = {}
    for (const sp of Object.values(byId.value)) {
      acc[sp.generation] = (acc[sp.generation] ?? 0) + 1
    }
    return acc
  })

  const totalByType = computed<Record<string, number>>(() => {
    const acc: Record<string, number> = {}
    for (const sp of Object.values(byId.value)) {
      acc[sp.primary_type] = (acc[sp.primary_type] ?? 0) + 1
      if (sp.secondary_type) acc[sp.secondary_type] = (acc[sp.secondary_type] ?? 0) + 1
    }
    return acc
  })

  const detailById = ref<Record<string, SpeciesDetailDto>>({})
  const detailLoading = ref<Record<string, true>>({})
  const inFlight = new Map<string, Promise<SpeciesDetailDto | null>>()

  function loadDetail(speciesId: string): Promise<SpeciesDetailDto | null> {
    const cached = detailById.value[speciesId]
    if (cached) return Promise.resolve(cached)
    const pending = inFlight.get(speciesId)
    if (pending) return pending
    detailLoading.value[speciesId] = true
    const promise = api.speciesDetail(speciesId)
      .then((data) => {
        detailById.value[speciesId] = data
        return data
      })
      .catch(() => null)
      .finally(() => {
        delete detailLoading.value[speciesId]
        inFlight.delete(speciesId)
      })
    inFlight.set(speciesId, promise)
    return promise
  }

  function detail(speciesId: string): SpeciesDetailDto | null {
    return detailById.value[speciesId] ?? null
  }

  function isDetailLoading(speciesId: string): boolean {
    return detailLoading.value[speciesId] === true
  }

  return {
    byId,
    isLoading,
    error,
    loadedAt,
    load,
    info,
    hasLabel,
    totalByBadge,
    totalByGeneration,
    totalByType,
    detailById,
    loadDetail,
    detail,
    isDetailLoading,
  }
})
