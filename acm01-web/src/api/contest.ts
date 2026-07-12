import { ApiError, getToken, type Result } from './auth'

export { ApiError }

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ContestItem {
  id: number
  source: string
  externalId: string
  title: string
  url?: string
  startTime: string
  endTime?: string
  status: number
  difficulty?: string
  contestType?: string
  location?: string
}

export type ContestStatusFilter = '' | '1' | '2' | '3'

export interface ContestListParams {
  source?: string
  status?: ContestStatusFilter
  pageNum?: number
  pageSize?: number
}

export interface ContestSourceOption {
  code: string
  label: string
  short: string
  implemented: boolean
}

export const CONTEST_SOURCES: ContestSourceOption[] = [
  { code: 'codeforces', label: 'Codeforces', short: 'CF', implemented: true },
  { code: 'atcoder', label: 'AtCoder', short: 'AT', implemented: true },
  { code: 'nowcoder', label: '牛客网', short: 'NC', implemented: true },
  { code: 'luogu', label: '洛谷', short: 'LG', implemented: false },
  { code: 'ccpc', label: 'CCPC', short: 'CC', implemented: false },
  { code: 'icpc', label: 'ICPC', short: 'IC', implemented: false },
  { code: 'lanqiao', label: '蓝桥杯', short: 'LQ', implemented: false },
]

async function publicRequest<T>(path: string): Promise<Result<T>> {
  const response = await fetch(path)
  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    throw new ApiError(response.status, `非 JSON 响应 (${response.status})`)
  }
  const result = (await response.json()) as Result<T>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }
  return result
}

export async function fetchContestList(params: ContestListParams = {}): Promise<PageResult<ContestItem>> {
  const query = new URLSearchParams()
  if (params.source) query.set('source', params.source)
  if (params.status) query.set('status', params.status)
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))

  const result = await publicRequest<PageResult<ContestItem>>(`/api/contests?${query.toString()}`)
  return result.data
}

async function authRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
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

/** 管理端手动触发爬虫，返回 contest_crawl_log.id；平台未启用时可能为 null */
export async function triggerContestCrawl(sourceCode: string): Promise<number | null> {
  return authRequest<number | null>(`/api/admin/contests/crawl/${sourceCode}`, {
    method: 'POST',
  })
}

export function getSourceOption(code: string): ContestSourceOption | undefined {
  return CONTEST_SOURCES.find((item) => item.code === code)
}

export function sourceLabel(code: string): string {
  return getSourceOption(code)?.label ?? code
}

export function formatContestStatus(status: number): string {
  switch (status) {
    case 1:
      return '即将开始'
    case 2:
      return '进行中'
    case 3:
      return '已结束'
    default:
      return '未知'
  }
}

export function statusClass(status: number): string {
  switch (status) {
    case 1:
      return 'status-upcoming'
    case 2:
      return 'status-running'
    case 3:
      return 'status-finished'
    default:
      return ''
  }
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
