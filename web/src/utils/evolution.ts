import type { PokemonDto, EvolutionDto, EvolutionRequirementDto } from '@/types'

export type EvoReadiness = 'ready' | 'needs_item' | 'not_yet'

export const EVO_COLORS: Record<EvoReadiness, string> = {
  ready: 'bg-green',
  needs_item: 'bg-yellow',
  not_yet: 'bg-blue',
}

export const EVO_LABELS: Record<EvoReadiness, string> = {
  ready: 'Puede evolucionar',
  needs_item: 'Necesita objeto',
  not_yet: 'Podrá evolucionar',
}

function checkReq(pokemon: PokemonDto, req: EvolutionRequirementDto): 'ok' | 'needs_item' | 'no' {
  switch (req.type) {
    case 'level':
      if (req.min_level != null && pokemon.level < req.min_level) return 'no'
      if (req.max_level != null && pokemon.level > req.max_level) return 'no'
      return 'ok'
    case 'friendship': {
      const target = req.amount ?? 220
      return pokemon.friendship >= target ? 'ok' : 'no'
    }
    case 'item_trigger':
      return 'needs_item'
    case 'held_item':
      if (req.item && pokemon.held_item)
        return pokemon.held_item === req.item ? 'ok' : 'needs_item'
      return 'needs_item'
    case 'move_set':
      if (req.move) {
        const has = pokemon.moves.some(m => m.name.toLowerCase() === req.move!.toLowerCase())
        return has ? 'ok' : 'no'
      }
      return 'ok'
    case 'any': {
      if (!req.children?.length) return 'ok'
      for (const child of req.children) {
        const r = checkReq(pokemon, child)
        if (r !== 'no') return r
      }
      return 'no'
    }
    // biome, time_range, weather, world, use_move, moon_phase, structure → no podemos comprobar, asumimos OK
    default:
      return 'ok'
  }
}

function checkEvolution(pokemon: PokemonDto, evo: EvolutionDto): EvoReadiness | null {
  let itemNeeded = false
  for (const req of evo.requirements) {
    const r = checkReq(pokemon, req)
    if (r === 'no') return null
    if (r === 'needs_item') itemNeeded = true
  }
  return itemNeeded ? 'needs_item' : 'ready'
}

export function evoReadiness(pokemon: PokemonDto, evolutions: EvolutionDto[]): EvoReadiness | null {
  if (!evolutions.length) return null
  let best: EvoReadiness | null = null
  for (const evo of evolutions) {
    const r = checkEvolution(pokemon, evo)
    if (r === 'ready') return 'ready'
    if (r === 'needs_item') best = 'needs_item'
    else if (best === null) best = 'not_yet'
  }
  return best
}
