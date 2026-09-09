import { ApiError, type Result } from './auth'

export { ApiError }

export interface SearchHit {
  type: string
  refId: number
  title: string
  snippet: string
  tags?: string
  authorName?: string
  createdTime?: string
  highlights?: string[]
}

export interface SearchResult {
  query: string
  type: string
  hits: SearchHit[]
  total: number
  pageNum: number
  pageSize: number
}

export type SearchType = 'all' | 'solution' | 'team'

export async function searchContent(params: {
  q: string
  type?: SearchType
  pageNum?: number
  pageSize?: number
}): Promise<SearchResult> {
  const query = new URLSearchParams()
  query.set('q', params.q)
  query.set('type', params.type ?? 'all')
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))

  const response = await fetch(`/api/search?${query.toString()}`)
  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    throw new ApiError(response.status, `非 JSON 响应 (${response.status})`)
  }
  const result = (await response.json()) as Result<SearchResult>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }
  return result.data
}

export function searchTypeLabel(type: string): string {
  switch (type) {
    case 'solution':
      return '题解'
    case 'team':
      return '组队'
    default:
      return type
  }
}
