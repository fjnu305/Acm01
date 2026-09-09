import { ApiError, type Result, getToken } from './auth'

export interface RatingSnapshot {
  platform: string
  rating: number
  maxRating?: number
  rank?: string
  snapshotDate: string
}

export interface OjAccount {
  id: number
  platform: string
  handle: string
  currentRating?: number
  maxRating?: number
  rank?: string
  lastSyncAt?: string
  hasCredential: boolean
}

async function request<T>(path: string, options: RequestInit = {}): Promise<Result<T>> {
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(path, { ...options, headers })
  const result = (await response.json()) as Result<T>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }
  return result
}

export async function bindCfHandle(handle: string, credential?: string): Promise<OjAccount> {
  const result = await request<OjAccount>('/api/oj/accounts/cf', {
    method: 'POST',
    body: JSON.stringify({ handle, credential }),
  })
  return result.data
}

export async function listOjAccounts(): Promise<OjAccount[]> {
  const result = await request<OjAccount[]>('/api/oj/accounts', { method: 'GET' })
  return result.data
}
