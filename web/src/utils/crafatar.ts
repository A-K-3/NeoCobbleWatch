/**
 * Minecraft skin URL helpers via mc-heads.net.
 * https://mc-heads.net
 */

const BASE = 'https://mc-heads.net'

export function avatarUrl(name: string, size = 128): string {
  return `${BASE}/avatar/${name}/${size}`
}

export function bodyUrl(name: string, scale = 8): string {
  // mc-heads `body` endpoint takes a height in px; map Crafatar's pixel-scale
  // (each block ≈ 16 px) onto a comparable rendered height.
  const height = Math.max(64, scale * 32)
  return `${BASE}/body/${name}/${height}`
}

export function headUrl(name: string, scale = 6): string {
  // 3D-isometric head render. mc-heads `head` returns square PNG sized in px.
  const size = Math.max(32, scale * 24)
  return `${BASE}/head/${name}/${size}`
}
