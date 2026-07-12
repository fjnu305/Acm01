import { ApiError, getToken, type Result } from './auth'

export { ApiError }

export interface SubscriptionItem {
  id: number
  contestId: number
  contestTitle: string
  source: string
  contestStartTime: string
  remindBeforeMinutes: number
  channel: string
  status: number
  createdTime: string
}

export interface SubscribePayload {
  contestId: number
  remindBeforeMinutes: number[]
}

export const REMIND_OPTIONS = [
  { minutes: 1440, label: '赛前 24 小时' },
  { minutes: 60, label: '赛前 1 小时' },
] as const

async function authRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
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

export async function subscribeContest(payload: SubscribePayload): Promise<SubscriptionItem[]> {
  return authRequest<SubscriptionItem[]>('/api/subscriptions', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function cancelSubscription(id: number): Promise<void> {
  await authRequest<void>(`/api/subscriptions/${id}`, { method: 'DELETE' })
}

export async function fetchMySubscriptions(): Promise<SubscriptionItem[]> {
  return authRequest<SubscriptionItem[]>('/api/subscriptions/my')
}

export function remindLabel(minutes: number): string {
  if (minutes >= 1440) return '赛前 24 小时'
  if (minutes >= 60) return '赛前 1 小时'
  return `赛前 ${minutes} 分钟`
}

export function channelLabel(): string {
  return '邮件'
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
