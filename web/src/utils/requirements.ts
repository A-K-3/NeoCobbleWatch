import type { EvolutionRequirementDto } from '@/types'
import { prettify, stripNamespace } from './format'
import { typeLabel } from './pokemon'

const DEFAULT_FRIENDSHIP_TARGET = 220

type Progress = { level: number; friendship: number }

/**
 * Render an evolution requirement as a human-readable Spanish string.
 * Pass `progress` to surface the player's live numbers for level / friendship
 * (e.g. `Nivel 16 (12/16)`).
 */
export function renderRequirement(req: EvolutionRequirementDto, progress?: Progress): string {
  switch (req.type) {
    case 'level':
      if (req.min_level && progress) return `Nivel ${req.min_level} (${progress.level}/${req.min_level})`
      if (req.min_level && req.max_level) return `Nivel ${req.min_level}–${req.max_level}`
      if (req.min_level) return `Nivel ${req.min_level}+`
      if (req.max_level) return `Nivel ≤ ${req.max_level}`
      return 'Nivel'

    case 'friendship': {
      const target = req.amount ?? DEFAULT_FRIENDSHIP_TARGET
      if (progress) return `Amistad ${target} (${progress.friendship}/${target})`
      return req.amount ? `Amistad ≥ ${target}` : 'Amistad alta'
    }

    case 'item_trigger':
      if (req.item) return prettify(stripNamespace(req.item))
      return 'Usar objeto'

    case 'held_item':
      if (req.item) return `Equipar ${prettify(stripNamespace(req.item))}`
      if (req.item_tag) return `Equipar objeto de ${prettyTag(req.item_tag)}`
      return 'Equipar objeto'

    case 'owner_holds_item':
      if (req.item) return `Entrenador con ${prettify(req.item)}`
      if (req.item_tag) return `Entrenador con objeto de ${prettyTag(req.item_tag)}`
      return 'Entrenador con objeto'

    case 'biome': {
      const raw = (req.tag ?? req.identifier ?? '').toLowerCase()
      if (raw.includes('alola')) return 'Bioma de Alola'
      if (raw.includes('galar')) return 'Bioma de Galar'
      if (raw.includes('hisui')) return 'Bioma de Hisui'
      if (raw.includes('paldea')) return 'Bioma de Paldea'
      if (raw.includes('regional')) return 'Bioma regional'
      if (req.identifier) return `Bioma: ${prettify(stripNamespace(req.identifier))}`
      if (req.tag) return `Biomas de ${prettyTag(req.tag)}`
      return 'Bioma específico'
    }

    case 'structure':
      if (req.identifier) return `Cerca de ${prettify(stripNamespace(req.identifier))}`
      if (req.tag) return `Cerca de estructuras de ${prettyTag(req.tag)}`
      return 'Cerca de estructura'

    case 'time_range':
      return 'Hora específica'

    case 'weather':
      if (req.thundering === true) return 'Tormenta'
      if (req.raining === true) return 'Lloviendo'
      if (req.raining === false && req.thundering === false) return 'Sin lluvia'
      return 'Clima específico'

    case 'moon_phase':
      return req.moon_phase ? `Fase lunar: ${prettify(req.moon_phase)}` : 'Fase lunar específica'

    case 'world':
      return req.identifier
        ? `Dimensión: ${prettify(stripNamespace(req.identifier))}`
        : 'Dimensión específica'

    case 'move_set':
      return req.move ? `Conocer ${prettify(req.move)}` : 'Conocer un movimiento'

    case 'move_type':
      return req.move_type ? `Conocer movimiento de ${typeLabel(req.move_type)}` : 'Conocer movimiento'

    case 'use_move':
      if (req.move && req.amount) return `Usar ${prettify(req.move)} ${req.amount} veces`
      if (req.move) return `Usar ${prettify(req.move)}`
      return 'Usar movimiento'

    case 'attack_defence_ratio':
      switch (req.ratio) {
        case 'ATTACK_HIGHER': return 'Ataque > Defensa'
        case 'EQUAL': return 'Ataque = Defensa'
        case 'DEFENCE_HIGHER': return 'Defensa > Ataque'
        default: return 'Atk/Def específico'
      }

    case 'chance':
      return req.chance ? `Aleatorio (${req.chance})` : 'Aleatorio'

    case 'any':
      if (req.children?.length) {
        return req.children.map((c) => renderRequirement(c, progress)).join(' o ')
      }
      return 'Cualquier requisito'

    case 'pokemonproperties':
      return 'Naturaleza específica'

    default:
      return `Requisito: ${prettify(req.type)}`
  }
}

function prettyTag(tag: string): string {
  return prettify(stripNamespace(tag.replace(/^#/, '')))
}
