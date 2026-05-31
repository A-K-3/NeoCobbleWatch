import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/api/client'
import type { HealthDto, OnlinePlayerDto, ServerStatsDto } from '@/types'

export const useServerStore = defineStore('server', () => {
  const stats = ref<ServerStatsDto | null>(null)
  const online = ref<OnlinePlayerDto[]>([])
  const health = ref<HealthDto | null>(null)
  const lastRefreshedAt = ref<number | null>(null)
  const isRefreshing = ref(false)
  const error = ref<Error | null>(null)

  async function refresh(): Promise<void> {
    if (isRefreshing.value) return
    isRefreshing.value = true
    error.value = null
    try {
      const [s, o, h] = await Promise.all([api.serverStats(), api.online(), api.health()])
      stats.value = s
      online.value = o
      health.value = h
      lastRefreshedAt.value = Date.now()
    } catch (e) {
      error.value = e as Error
    } finally {
      isRefreshing.value = false
    }
  }

  return { stats, online, health, lastRefreshedAt, isRefreshing, error, refresh }
})
