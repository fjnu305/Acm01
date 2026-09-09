const IMAGE_MD_RE = /!\[[^\]]*\]\((https?:\/\/[^)\s]+)\)/gi
const IMAGE_URL_RE = /https?:\/\/[^\s<>"']+\.(?:png|jpe?g|gif|webp)(?:\?[^\s<>"']*)?/gi

export interface ParsedPostContent {
  title: string
  excerpt: string
  images: string[]
}

export function parsePostContent(content: string): ParsedPostContent {
  const images: string[] = []
  const seen = new Set<string>()

  const collect = (url: string) => {
    const normalized = url.trim()
    if (normalized && !seen.has(normalized)) {
      seen.add(normalized)
      images.push(normalized)
    }
  }

  for (const match of content.matchAll(IMAGE_MD_RE)) {
    if (match[1]) collect(match[1])
  }
  for (const match of content.matchAll(IMAGE_URL_RE)) {
    collect(match[0])
  }

  let text = content
    .replace(IMAGE_MD_RE, '')
    .replace(IMAGE_URL_RE, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()

  const lines = text.split('\n').map((l) => l.trim()).filter(Boolean)

  if (lines.length === 0) {
    return { title: '（无内容）', excerpt: '', images: images.slice(0, 3) }
  }

  if (lines.length === 1) {
    const line = lines[0]
    if (line.length <= 48) {
      return { title: line, excerpt: '', images: images.slice(0, 3) }
    }
    const cut = line.slice(0, 48)
    const breakAt = Math.max(cut.lastIndexOf(' '), cut.lastIndexOf('，'), cut.lastIndexOf('。'))
    const titleEnd = breakAt > 20 ? breakAt : 48
    return {
      title: `${line.slice(0, titleEnd)}…`,
      excerpt: line.slice(titleEnd).trim(),
      images: images.slice(0, 3),
    }
  }

  return {
    title: lines[0],
    excerpt: lines.slice(1).join(' '),
    images: images.slice(0, 3),
  }
}
