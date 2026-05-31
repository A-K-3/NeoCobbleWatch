import type {
  ActivityDto,
  AdvancementsDto,
  EconomyDto,
  HealthDto,
  OnlinePlayerDto,
  PaginatedResponse,
  PartyDto,
  PcSlotDto,
  PlayerProfileDto,
  PlayerSummaryDto,
  PokedexDto,
  ServerStatsDto,
  SingleResponse,
  SpeciesDetailDto,
  SpeciesLabelsResponse,
  TopMetric,
  TopResponse,
} from '@/types'

const BASE = (import.meta.env.VITE_API_BASE ?? 'http://127.0.0.1:8080/api/v1').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly code?: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

type QueryParams = Record<string, string | number | boolean | undefined>

async function request<T>(path: string, params?: QueryParams): Promise<T> {
  const url = new URL(`${BASE}${path}`)
  if (params) {
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined) url.searchParams.set(k, String(v))
    }
  }
  let res: Response
  try {
    res = await fetch(url.toString(), { headers: { Accept: 'application/json' } })
  } catch (e) {
    throw new ApiError(0, `Network error: ${(e as Error).message}`, 'NETWORK')
  }
  const body = (await res.json().catch(() => null)) as unknown
  if (!res.ok) {
    const err = (body as { error?: { code?: string; message?: string } } | null)?.error
    throw new ApiError(res.status, err?.message ?? res.statusText, err?.code)
  }
  return body as T
}

export const api = {
  health: () => request<SingleResponse<HealthDto>>('/health').then((r) => r.data),

  online: () => request<SingleResponse<OnlinePlayerDto[]>>('/server/online').then((r) => r.data),

  serverStats: () => request<SingleResponse<ServerStatsDto>>('/server/stats').then((r) => r.data),

  players: (limit = 50, offset = 0) =>
    request<PaginatedResponse<PlayerSummaryDto>>('/players', { limit, offset }),

  player: (uuid: string) =>
    request<SingleResponse<PlayerProfileDto>>(`/players/${uuid}`).then((r) => r.data),

  pokedex: (uuid: string) =>
    request<SingleResponse<PokedexDto>>(`/players/${uuid}/pokedex`).then((r) => r.data),

  party: (uuid: string) =>
    request<SingleResponse<PartyDto>>(`/players/${uuid}/party`).then((r) => r.data),

  pcByBox: (uuid: string, box: number) =>
    request<PaginatedResponse<PcSlotDto>>(`/players/${uuid}/pc`, { box }),

  pcPaginated: (uuid: string, limit = 50, offset = 0) =>
    request<PaginatedResponse<PcSlotDto>>(`/players/${uuid}/pc`, { limit, offset }),

  tops: (metric: TopMetric, limit = 50, offset = 0) =>
    request<TopResponse>(`/tops/${metric}`, { limit, offset }),

  activity: (uuid: string) =>
    request<SingleResponse<ActivityDto>>(`/players/${uuid}/activity`).then((r) => r.data),

  advancements: (uuid: string) =>
    request<SingleResponse<AdvancementsDto>>(`/players/${uuid}/advancements`).then((r) => r.data),

  economy: (uuid: string) =>
    request<SingleResponse<EconomyDto>>(`/players/${uuid}/economy`).then((r) => r.data),

  speciesLabels: () => request<SpeciesLabelsResponse>('/species/labels'),

  speciesDetail: (id: string) => {
    const [ns, path] = id.includes(':') ? id.split(':', 2) : ['cobblemon', id]
    return request<SingleResponse<SpeciesDetailDto>>(`/species/${ns}/${path}`).then((r) => r.data)
  },
}
