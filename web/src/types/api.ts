// API response DTO types. Field names match the backend snake_case JSON exactly.
// See F:/Coding/NeoCobbleWatch/API.md.

export type PageInfo = {
  limit: number
  offset: number
  total: number
}

export type PaginatedResponse<T> = {
  data: T[]
  page: PageInfo
}

export type SingleResponse<T> = {
  data: T
}

export type ApiErrorBody = {
  error: {
    code: string
    message: string
  }
}

// === Health ===

export type HealthDto = {
  status: string
  version: string
  last_full_cycle_at: number | null
  last_full_cycle_age_seconds: number | null
}

// === Server ===

export type OnlinePlayerDto = {
  uuid: string
  name: string
}

export type ServerStatsDto = {
  total_players: number
  online_players: number
  total_capture_count: number
  total_shiny_capture_count: number
  total_eggs_hatched: number
  total_evolved_count: number
  total_battle_victory_count: number
  total_pvp_battle_victory_count: number
  total_traded_count: number
}

// === Player ===

export type PlayerSummaryDto = {
  uuid: string
  name: string
  last_seen: number
  online: boolean
}

export type PlayerDto = {
  uuid: string
  name: string
  first_seen: number
  last_seen: number
  online: boolean
  starter_prompted: boolean
  starter_locked: boolean
  starter_selected: boolean
  starter_uuid: string | null
  key_items: string[]
  snapshot_at: number
}

export type StatsDto = {
  total_capture_count: number
  total_shiny_capture_count: number
  total_eggs_collected: number
  total_eggs_hatched: number
  total_evolved_count: number
  total_battle_victory_count: number
  total_pvp_battle_victory_count: number
  total_pvw_battle_victory_count: number
  total_pvn_battle_victory_count: number
  total_traded_count: number
  type_capture_counts: Record<string, number>
  defeated_counts: Record<string, number>
  aspects_collected: Record<string, string[]>
  snapshot_at: number
}

export type PlayerProfileDto = {
  player: PlayerDto
  stats: StatsDto
}

// === Pokedex ===

export type PokedexKnowledge = 'NONE' | 'ENCOUNTERED' | 'CAUGHT'

export type PokedexEntryDto = {
  species_id: string
  knowledge: PokedexKnowledge
  forms_encountered: string[]
  forms_caught: string[]
  shiny_states: string[]
  genders_seen: string[]
  aspects_seen: string[]
}

export type PokedexDto = {
  entries: PokedexEntryDto[]
  snapshot_at: number
}

// === Pokemon ===

export type MoveDto = {
  name: string
  pp: number
  pp_max: number
}

export type StatBlock = {
  hp: number
  atk: number
  def: number
  spa: number
  spd: number
  spe: number
}

export type PokemonDto = {
  pokemon_uuid: string
  species_id: string
  form_name: string
  nickname: string | null
  level: number
  shiny: boolean
  gender: 'MALE' | 'FEMALE' | 'GENDERLESS' | string
  nature: string
  ability: string
  ball: string
  tera_type: string | null
  held_item: string | null
  ot_name: string | null
  friendship: number
  ivs: StatBlock
  evs: StatBlock
  moves: MoveDto[]
  aspects: string[]
}

// === Party / PC ===

export type PartySlotDto = {
  slot_index: number
  pokemon: PokemonDto
}

export type PartyDto = {
  slots: PartySlotDto[]
  snapshot_at: number
}

export type PcSlotDto = {
  box_index: number
  slot_index: number
  pokemon: PokemonDto
}

// === Tops ===

export type TopMetric =
  // Pokémon
  | 'captures'
  | 'shinies'
  | 'pokedex_caught'
  | 'pvp_wins'
  | 'eggs_hatched'
  | 'trades'
  | 'level_ups'
  | 'evolutions'
  // Minecraft
  | 'play_time'
  | 'distance'
  | 'mob_kills'
  | 'damage_dealt'
  | 'fish_caught'
  | 'raids_completed'
  | 'badges'
  | 'deaths'
  | 'money'

export const TOP_METRICS_POKEMON: readonly TopMetric[] = [
  'captures', 'shinies', 'pokedex_caught', 'pvp_wins',
  'eggs_hatched', 'trades', 'level_ups', 'evolutions',
]

export const TOP_METRICS_MINECRAFT: readonly TopMetric[] = [
  'play_time', 'distance', 'mob_kills', 'damage_dealt',
  'fish_caught', 'raids_completed', 'badges', 'deaths', 'money',
]

export const TOP_METRICS: readonly TopMetric[] = [...TOP_METRICS_POKEMON, ...TOP_METRICS_MINECRAFT]

export type TopEntryDto = {
  rank: number
  uuid: string
  name: string
  value: number
}

export type TopResponse = {
  metric: string
  data: TopEntryDto[]
  page: PageInfo
}

// === Activity ===

export type ActivityDto = {
  play_time_ticks: number
  deaths: number
  mob_kills: number
  fish_caught: number
  damage_dealt: number
  damage_taken: number
  damage_blocked: number
  walk_cm: number
  sprint_cm: number
  fly_cm: number
  swim_cm: number
  jumps: number
  sleep_in_bed: number
  traded_with_villager: number
  blocks_mined: number
  items_used: number
  items_crafted: number
  items_broken: number
  kills_map: Record<string, number>
  killed_by_map: Record<string, number>
  level_ups: number
  evolutions: number
  times_ridden: number
  riding_land_cm: number
  riding_air_cm: number
  riding_liquid_cm: number
  eggs_collected: number
  eggs_hatched: number
  rod_casts: number
  reel_ins: number
  released: number
  raids_hosted: number
  raids_joined: number
  raids_completed: number
  raid_tier1: number
  raid_tier2: number
  raid_tier3: number
  raid_tier4: number
  raid_tier5: number
  badges: string[]
  snapshot_at: number
}

// === Species labels ===

export type SpeciesInfoDto = {
  national_dex: number
  generation: number
  labels: string[]
  fully_evolved: boolean
  primary_type: string
  secondary_type: string | null
}

export type SpeciesLabelsResponse = {
  data: Record<string, SpeciesInfoDto>
  snapshot_at: number
}

export type EvolutionRequirementDto = {
  type: string
  min_level?: number | null
  max_level?: number | null
  amount?: number | null
  item?: string | null
  item_tag?: string | null
  identifier?: string | null
  tag?: string | null
  raining?: boolean | null
  thundering?: boolean | null
  moon_phase?: string | null
  move?: string | null
  move_type?: string | null
  ratio?: string | null
  chance?: string | null
  children?: EvolutionRequirementDto[] | null
}

export type EvolutionDto = {
  result_id: string | null
  result_name: string | null
  result_aspects: string[]
  requirements: EvolutionRequirementDto[]
  optional: boolean
  consume_held_item: boolean
}

export type SpawnDto = {
  bucket: string
  weight: number
  level_min: number
  level_max: number
  biomes: string[]
  labels: string[]
}

export type PreEvolutionDto = {
  species_id: string
  species_name: string
}

export type DropEntryDto = {
  item: string
  percentage: number
  quantity_min: number
  quantity_max: number
}

export type SpecialFormDto = {
  name: string
  aspects: string[]
  labels: string[]
  primary_type: string
  secondary_type: string | null
  dynamax_blocked: boolean
}

// === Economy ===

export type EconomyDto = {
  cobble_dollars: number
  snapshot_at: number
}

// === Advancements ===

export type AdvancementsDto = {
  completed: string[]
  snapshot_at: number
}

export type SpeciesDetailDto = {
  id: string
  name: string
  national_dex: number
  generation: number
  primary_type: string
  secondary_type: string | null
  labels: string[]
  fully_evolved: boolean
  catch_rate: number
  pre_evolution: PreEvolutionDto | null
  evolutions: EvolutionDto[]
  drops: DropEntryDto[]
  spawns: SpawnDto[]
  forms: SpecialFormDto[]
}
