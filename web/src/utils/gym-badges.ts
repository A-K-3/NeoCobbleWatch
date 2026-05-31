import type { PokemonType } from './pokemon'

export type GymBadgeInfo = {
  id: string
  name: string
  leader: string
  city: string
  type: PokemonType
  sprite?: number
}

export type GymRegionInfo = {
  key: string
  label: string
  badges: GymBadgeInfo[]
  trophies?: TrophyInfo[]
}

export type TrophyInfo = {
  id: string
  name: string
}

export const GYM_REGIONS: GymRegionInfo[] = [
  {
    key: 'kanto',
    label: 'Kanto',
    badges: [
      { id: 'cobbleversebadges:kanto_boulder_badge',  name: 'Roca',      leader: 'Brock',      city: 'Ciudad Plateada', type: 'rock',     sprite: 1 },
      { id: 'cobbleversebadges:kanto_cascade_badge',  name: 'Cascada',   leader: 'Misty',      city: 'Ciudad Celeste',  type: 'water',    sprite: 2 },
      { id: 'cobbleversebadges:kanto_thunder_badge',  name: 'Trueno',    leader: 'Tte. Surge', city: 'Ciudad Carmín',   type: 'electric', sprite: 3 },
      { id: 'cobbleversebadges:kanto_rainbow_badge',  name: 'Arco Iris', leader: 'Erika',      city: 'Ciudad Azulona',  type: 'grass',    sprite: 4 },
      { id: 'cobbleversebadges:kanto_soul_badge',     name: 'Alma',      leader: 'Koga',       city: 'Ciudad Fucsia',   type: 'poison',   sprite: 5 },
      { id: 'cobbleversebadges:kanto_marsh_badge',    name: 'Pantano',   leader: 'Sabrina',    city: 'Ciudad Joyería',  type: 'psychic',  sprite: 6 },
      { id: 'cobbleversebadges:kanto_volcano_badge',  name: 'Volcán',    leader: 'Blaine',     city: 'Isla Canela',     type: 'fire',     sprite: 7 },
      { id: 'cobbleversebadges:kanto_earth_badge',    name: 'Tierra',    leader: 'Giovanni',   city: 'Ciudad Verdinal', type: 'ground',   sprite: 8 },
    ],
  },
  {
    key: 'johto',
    label: 'Johto',
    badges: [
      { id: 'cobbleversebadges:johto_zephyr_badge',   name: 'Brisa',     leader: 'Falkner', city: 'Ciudad Nogales', type: 'flying',   sprite: 9 },
      { id: 'cobbleversebadges:johto_hive_badge',     name: 'Colmena',   leader: 'Bugsy',   city: 'Ciudad Malva',   type: 'bug',      sprite: 10 },
      { id: 'cobbleversebadges:johto_plain_badge',    name: 'Llanura',   leader: 'Whitney', city: 'Ciudad Trigal',  type: 'normal',   sprite: 11 },
      { id: 'cobbleversebadges:johto_fog_badge',      name: 'Niebla',    leader: 'Morty',   city: 'Ciudad Caoba',   type: 'ghost',    sprite: 12 },
      { id: 'cobbleversebadges:johto_storm_badge',    name: 'Tormenta',  leader: 'Chuck',   city: 'Ciudad Olivia',  type: 'fighting', sprite: 13 },
      { id: 'cobbleversebadges:johto_mineral_badge',  name: 'Mineral',   leader: 'Jasmine', city: 'Ciudad Acero',   type: 'steel',    sprite: 14 },
      { id: 'cobbleversebadges:johto_glacier_badge',  name: 'Glaciar',   leader: 'Pryce',   city: 'Ciudad Álamo',   type: 'ice',      sprite: 15 },
      { id: 'cobbleversebadges:johto_rising_badge',   name: 'Ascenso',   leader: 'Clair',   city: 'Ciudad Franja',  type: 'dragon',   sprite: 16 },
    ],
    trophies: [
      { id: 'cobbleversebadges:johto_league_trophy', name: 'Trofeo Liga Johto' },
    ],
  },
  {
    key: 'hoenn',
    label: 'Hoenn',
    badges: [
      { id: 'cobbleversebadges:hoenn_stone_badge',    name: 'Roca',       leader: 'Roxanne', city: 'Ciudad Periflor', type: 'rock',     sprite: 17 },
      { id: 'cobbleversebadges:hoenn_knuckle_badge',  name: 'Nudillo',    leader: 'Brawly',  city: 'Isla Férrica',    type: 'fighting', sprite: 18 },
      { id: 'cobbleversebadges:hoenn_dynamo_badge',   name: 'Dínamo',     leader: 'Wattson', city: 'Ciudad Maubri',   type: 'electric', sprite: 19 },
      { id: 'cobbleversebadges:hoenn_heat_badge',     name: 'Calor',      leader: 'Flannery', city: 'Pueblo Lavacalda', type: 'fire',   sprite: 20 },
      { id: 'cobbleversebadges:hoenn_balance_badge',  name: 'Equilibrio', leader: 'Norman',  city: 'Ciudad Petalia',  type: 'normal',   sprite: 21 },
      { id: 'cobbleversebadges:hoenn_feather_badge',  name: 'Pluma',      leader: 'Winona',  city: 'Ciudad Aérea',    type: 'flying',   sprite: 22 },
      { id: 'cobbleversebadges:hoenn_mind_badge',     name: 'Mente',      leader: 'Tate y Liza', city: 'Islacosecha', type: 'psychic', sprite: 23 },
      { id: 'cobbleversebadges:hoenn_rain_badge',     name: 'Lluvia',     leader: 'Wallace', city: 'Ciudad Sotópolis', type: 'water',   sprite: 24 },
    ],
  },
  {
    key: 'sinnoh',
    label: 'Sinnoh',
    badges: [
      { id: 'cobbleversebadges:sinnoh_coal_badge',    name: 'Carbón',     leader: 'Roark',   city: 'Ciudad Mina',     type: 'rock',     sprite: 25 },
      { id: 'cobbleversebadges:sinnoh_forest_badge',  name: 'Bosque',     leader: 'Gardenia', city: 'Ciudad Vetusta', type: 'grass',    sprite: 26 },
      { id: 'cobbleversebadges:sinnoh_cobble_badge',  name: 'Guijarro',   leader: 'Maylene', city: 'Ciudad Velo',     type: 'fighting', sprite: 27 },
      { id: 'cobbleversebadges:sinnoh_fen_badge',     name: 'Ciénaga',    leader: 'Crasher Wake', city: 'Ciudad Cobija', type: 'water', sprite: 28 },
      { id: 'cobbleversebadges:sinnoh_relic_badge',   name: 'Reliquia',   leader: 'Fantina', city: 'Ciudad Corazón',  type: 'ghost',    sprite: 29 },
      { id: 'cobbleversebadges:sinnoh_mine_badge',    name: 'Mina',       leader: 'Byron',   city: 'Ciudad Canalave', type: 'steel',    sprite: 30 },
      { id: 'cobbleversebadges:sinnoh_icicle_badge',  name: 'Carámbano',  leader: 'Candice', city: 'Ciudad Nevada',   type: 'ice',      sprite: 31 },
      { id: 'cobbleversebadges:sinnoh_beacon_badge',  name: 'Faro',       leader: 'Volkner', city: 'Ciudad Soleana',  type: 'electric', sprite: 32 },
    ],
  },
  {
    key: 'unova',
    label: 'Unova',
    badges: [
      { id: 'cobbleversebadges:unova_trio_badge',     name: 'Trío',       leader: 'Cilan',   city: 'Ciudad Acanto',   type: 'grass',    sprite: 33 },
      { id: 'cobbleversebadges:unova_basic_badge',    name: 'Básica',     leader: 'Lenora',  city: 'Ciudad Nacarena', type: 'normal',   sprite: 34 },
      { id: 'cobbleversebadges:unova_insect_badge',   name: 'Insecto',    leader: 'Burgh',   city: 'Ciudad Castelia', type: 'bug',      sprite: 35 },
      { id: 'cobbleversebadges:unova_bolt_badge',     name: 'Rayo',       leader: 'Elesa',   city: 'Ciudad Nimbasa',  type: 'electric', sprite: 36 },
      { id: 'cobbleversebadges:unova_quake_badge',    name: 'Seísmo',     leader: 'Clay',    city: 'Ciudad Tornelia', type: 'ground',   sprite: 37 },
      { id: 'cobbleversebadges:unova_jet_badge',      name: 'Jet',        leader: 'Skyla',   city: 'Ciudad Mistral',  type: 'flying',   sprite: 38 },
      { id: 'cobbleversebadges:unova_freeze_badge',   name: 'Hielo',      leader: 'Brycen',  city: 'Ciudad Icirrus',  type: 'ice',      sprite: 39 },
      { id: 'cobbleversebadges:unova_legend_badge',   name: 'Leyenda',    leader: 'Drayden', city: 'Ciudad Opelucid', type: 'dragon',   sprite: 40 },
    ],
  },
  {
    key: 'kalos',
    label: 'Kalos',
    badges: [
      { id: 'cobbleversebadges:kalos_bug_badge',      name: 'Bicho',      leader: 'Viola',   city: 'Ciudad Santalune', type: 'bug',      sprite: 43 },
      { id: 'cobbleversebadges:kalos_cliff_badge',    name: 'Risco',      leader: 'Grant',   city: 'Ciudad Cyllage',  type: 'rock',     sprite: 44 },
      { id: 'cobbleversebadges:kalos_rumble_badge',   name: 'Combate',    leader: 'Korrina', city: 'Ciudad Shalour',  type: 'fighting', sprite: 45 },
      { id: 'cobbleversebadges:kalos_plant_badge',    name: 'Planta',     leader: 'Ramos',   city: 'Ciudad Coumarine', type: 'grass',   sprite: 46 },
      { id: 'cobbleversebadges:kalos_voltage_badge',  name: 'Voltaje',    leader: 'Clemont', city: 'Ciudad Lumiose',  type: 'electric', sprite: 47 },
      { id: 'cobbleversebadges:kalos_fairy_badge',    name: 'Hada',       leader: 'Valerie', city: 'Ciudad Laverre',  type: 'fairy',    sprite: 48 },
      { id: 'cobbleversebadges:kalos_psychic_badge',  name: 'Psíquica',   leader: 'Olympia', city: 'Ciudad Anistar',  type: 'psychic',  sprite: 49 },
      { id: 'cobbleversebadges:kalos_iceberg_badge',  name: 'Iceberg',    leader: 'Wulfric', city: 'Ciudad Snowbelle', type: 'ice',     sprite: 50 },
    ],
  },
  {
    key: 'alola',
    label: 'Alola',
    badges: [
      { id: 'cobbleversebadges:alola_melemele_badge', name: 'Melemele', leader: 'Hala',   city: 'Melemele',  type: 'fighting' },
      { id: 'cobbleversebadges:alola_akala_badge',    name: 'Akala',    leader: 'Olivia', city: 'Akala',     type: 'rock' },
      { id: 'cobbleversebadges:alola_ula_ula_badge',  name: 'Ula\'ula', leader: 'Nanu',   city: 'Ula\'ula',  type: 'dark' },
      { id: 'cobbleversebadges:alola_poni_badge',     name: 'Poni',     leader: 'Hapu',   city: 'Poni',      type: 'ground' },
    ],
  },
  {
    key: 'galar',
    label: 'Galar',
    badges: [
      { id: 'cobbleversebadges:galar_grass_badge',    name: 'Planta',    leader: 'Milo',   city: 'Turffield',  type: 'grass',    sprite: 51 },
      { id: 'cobbleversebadges:galar_water_badge',    name: 'Agua',      leader: 'Nessa',  city: 'Hulbury',    type: 'water',    sprite: 52 },
      { id: 'cobbleversebadges:galar_fire_badge',     name: 'Fuego',     leader: 'Kabu',   city: 'Motostoke',  type: 'fire',     sprite: 53 },
      { id: 'cobbleversebadges:galar_fighting_badge', name: 'Lucha',     leader: 'Bea',    city: 'Stow-on-Side', type: 'fighting', sprite: 54 },
      { id: 'cobbleversebadges:galar_fairy_badge',    name: 'Hada',      leader: 'Opal',   city: 'Ballonlea',  type: 'fairy',    sprite: 56 },
      { id: 'cobbleversebadges:galar_rock_badge',     name: 'Roca',      leader: 'Gordie', city: 'Circhester', type: 'rock',     sprite: 57 },
      { id: 'cobbleversebadges:galar_dark_badge',     name: 'Siniestro', leader: 'Piers',  city: 'Spikemuth',  type: 'dark',     sprite: 59 },
      { id: 'cobbleversebadges:galar_dragon_badge',   name: 'Dragón',    leader: 'Raihan', city: 'Hammerlocke', type: 'dragon',  sprite: 60 },
    ],
  },
  {
    key: 'paldea',
    label: 'Paldea',
    badges: [
      { id: 'cobbleversebadges:paldea_bug_badge',      name: 'Bicho',    leader: 'Katy',    city: 'Cortondo',    type: 'bug',      sprite: 70 },
      { id: 'cobbleversebadges:paldea_grass_badge',    name: 'Planta',   leader: 'Brassius', city: 'Artazon',    type: 'grass',    sprite: 71 },
      { id: 'cobbleversebadges:paldea_electric_badge', name: 'Eléctrica', leader: 'Iono',   city: 'Levincia',   type: 'electric', sprite: 72 },
      { id: 'cobbleversebadges:paldea_water_badge',    name: 'Agua',     leader: 'Kofu',    city: 'Cascarrafa',  type: 'water',    sprite: 73 },
      { id: 'cobbleversebadges:paldea_normal_badge',   name: 'Normal',   leader: 'Larry',   city: 'Medali',      type: 'normal',   sprite: 74 },
      { id: 'cobbleversebadges:paldea_ghost_badge',    name: 'Fantasma', leader: 'Ryme',    city: 'Montenevera', type: 'ghost',    sprite: 75 },
      { id: 'cobbleversebadges:paldea_psychic_badge',  name: 'Psíquica', leader: 'Tulip',   city: 'Alfornada',   type: 'psychic',  sprite: 76 },
      { id: 'cobbleversebadges:paldea_ice_badge',      name: 'Hielo',    leader: 'Grusha',  city: 'Glasado',     type: 'ice',      sprite: 77 },
    ],
  },
]

/** All known badge IDs across all regions, keyed by id for fast lookup. */
export const BADGE_BY_ID: Record<string, GymBadgeInfo & { region: string }> = {}
for (const region of GYM_REGIONS) {
  for (const badge of region.badges) {
    BADGE_BY_ID[badge.id] = { ...badge, region: region.key }
  }
}

/** All known trophy IDs. */
export const TROPHY_IDS: Set<string> = new Set(
  GYM_REGIONS.flatMap((r) => r.trophies?.map((t) => t.id) ?? []),
)
