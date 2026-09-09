export interface WikiHealth {
  ok: boolean
  vaultRoot?: string
  wikiExists?: boolean
  hotExists?: boolean
  indexExists?: boolean
  retrieveReady?: boolean
  pageCount?: number
  error?: string
}

export interface WikiPage {
  path: string
  title: string
  frontmatter: Record<string, string>
  content: string
  raw: string
}

export interface GraphNode {
  id: string
  label: string
  group: string
}

export interface GraphEdge {
  source: string
  target: string
  type: string
}

export interface WikiGraph {
  nodes: GraphNode[]
  edges: GraphEdge[]
}

export interface WikiCredentials {
  configured: boolean
  provider: string | null
  baseUrl: string
  model: string
  apiKeyMasked: string | null
}

export interface AskCitation {
  index: number
  path: string
  title: string
}

export interface AskResult {
  answer: string
  mode: 'quick' | 'standard' | 'deep'
  citations: AskCitation[]
}

export class WikiApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function wikiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  let response: Response
  try {
    response = await fetch(`/wiki-api${path}`, { ...options, headers })
  } catch {
    throw new WikiApiError(0, '无法连接 wiki-ai（请确认已启动 :8787）')
  }

  if (!response.ok) {
    let detail = `请求失败 (${response.status})`
    try {
      const body = await response.json()
      if (typeof body?.detail === 'string') detail = body.detail
      else if (Array.isArray(body?.detail)) detail = body.detail.map((d: { msg?: string }) => d.msg).join('; ')
    } catch {
      /* ignore */
    }
    throw new WikiApiError(response.status, detail)
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export function fetchWikiHealth() {
  return wikiRequest<WikiHealth>('/health')
}

export function fetchVaultStatus() {
  return wikiRequest<WikiHealth>('/vault/status')
}

export function fetchWikiPage(path: string) {
  return wikiRequest<WikiPage>(`/vault/page?path=${encodeURIComponent(path)}`)
}

export function fetchWikiGraph() {
  return wikiRequest<WikiGraph>('/graph')
}

export function fetchWikiCredentials() {
  return wikiRequest<WikiCredentials>('/credentials')
}

export function saveWikiCredentials(payload: {
  provider?: string
  baseUrl?: string
  model?: string
  apiKey?: string
}) {
  return wikiRequest<WikiCredentials>('/credentials', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteWikiCredentials() {
  return wikiRequest<{ ok: boolean }>('/credentials', { method: 'DELETE' })
}

export function testWikiCredentials(payload?: {
  baseUrl?: string
  model?: string
  apiKey?: string
}) {
  return wikiRequest<{ ok: boolean; reply: string }>('/credentials/test', {
    method: 'POST',
    body: JSON.stringify(payload ?? {}),
  })
}

export function askWiki(question: string, mode: 'quick' | 'standard' | 'deep' = 'standard') {
  return wikiRequest<AskResult>('/ask', {
    method: 'POST',
    body: JSON.stringify({ question, mode }),
  })
}
