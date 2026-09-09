import { ApiError, getToken, type Result } from './auth'

export { ApiError }

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface SolutionItem {
  id: number
  userId: number
  authorName: string
  title: string
  problemSource?: string
  problemId?: string
  tags?: string
  favoriteCount: number
  viewCount: number
  createdTime: string
  favorited?: boolean
}

export interface SolutionDetail extends SolutionItem {
  content: string
  status: number
}

export interface SolutionTemplate {
  id: number
  name: string
  category: string
  content: string
}

export interface SolutionCreatePayload {
  title: string
  content: string
  problemSource?: string
  problemId?: string
  tags?: string
  status?: number
}

async function publicRequest<T>(path: string): Promise<T> {
  const response = await fetch(path)
  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    throw new ApiError(response.status, `非 JSON 响应 (${response.status})`)
  }
  const result = (await response.json()) as Result<T>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }
  return result.data
}

async function authRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(path, { ...options, headers })
  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    throw new ApiError(response.status, `非 JSON 响应 (${response.status})`)
  }
  const result = (await response.json()) as Result<T>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }
  return result.data
}

export interface SolutionTagStat {
  name: string
  count: number
}

export async function fetchHotSolutionTags(limit = 8): Promise<SolutionTagStat[]> {
  return publicRequest<SolutionTagStat[]>(`/api/solutions/tags/hot?limit=${limit}`)
}

export async function fetchSolutionCategories(): Promise<SolutionTagStat[]> {
  return publicRequest<SolutionTagStat[]>(`/api/solutions/tags/categories`)
}

export async function fetchFavoriteSolutions(params: {
  pageNum?: number
  pageSize?: number
} = {}): Promise<PageResult<SolutionItem>> {
  const query = new URLSearchParams()
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return authRequest<PageResult<SolutionItem>>(`/api/solutions/favorites?${query.toString()}`)
}

export async function fetchSolutionList(params: {
  keyword?: string
  tag?: string
  pageNum?: number
  pageSize?: number
} = {}): Promise<PageResult<SolutionItem>> {
  const query = new URLSearchParams()
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.tag) query.set('tag', params.tag)
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return publicRequest<PageResult<SolutionItem>>(`/api/solutions?${query.toString()}`)
}

export async function fetchSolutionDetail(id: number): Promise<SolutionDetail> {
  const token = getToken()
  if (token) {
    return authRequest<SolutionDetail>(`/api/solutions/${id}`)
  }
  return publicRequest<SolutionDetail>(`/api/solutions/${id}`)
}

export async function createSolution(payload: SolutionCreatePayload): Promise<SolutionDetail> {
  return authRequest<SolutionDetail>('/api/solutions', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function updateSolution(id: number, payload: SolutionCreatePayload): Promise<SolutionDetail> {
  return authRequest<SolutionDetail>(`/api/solutions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteSolution(id: number): Promise<void> {
  await authRequest<void>(`/api/solutions/${id}`, { method: 'DELETE' })
}

export async function favoriteSolution(id: number): Promise<void> {
  await authRequest<void>(`/api/solutions/${id}/favorite`, { method: 'POST' })
}

export async function unfavoriteSolution(id: number): Promise<void> {
  await authRequest<void>(`/api/solutions/${id}/favorite`, { method: 'DELETE' })
}

export async function fetchSolutionTemplates(category?: string): Promise<SolutionTemplate[]> {
  const query = category ? `?category=${encodeURIComponent(category)}` : ''
  return publicRequest<SolutionTemplate[]>(`/api/solutions/templates${query}`)
}

export function formatDateTime(value?: string): string {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
