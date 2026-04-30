export function generateIdenticon(username) {
  let hash = 0
  for (let i = 0; i < username.length; i++) {
    hash = ((hash << 5) - hash + username.charCodeAt(i)) | 0
  }

  const hue = Math.abs(hash) % 360
  const color = `hsl(${hue}, 65%, 55%)`
  const bg = `hsl(${hue}, 25%, 95%)`

  let cells = ''
  for (let row = 0; row < 5; row++) {
    for (let col = 0; col < 3; col++) {
      const bit = (Math.abs(hash * (row * 3 + col + 1)) >> 4) & 1
      if (bit) {
        cells += `<rect x="${col + 1}" y="${row}" width="1" height="1" fill="${color}"/>`
        if (col < 2) {
          cells += `<rect x="${5 - col}" y="${row}" width="1" height="1" fill="${color}"/>`
        }
      }
    }
  }

  return `data:image/svg+xml,${encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 7 5"><rect width="7" height="5" fill="${bg}"/>${cells}</svg>`
  )}`
}
