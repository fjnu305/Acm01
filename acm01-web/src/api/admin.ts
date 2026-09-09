import { ApiError, getToken, type Result } from './auth'
import type { PageResult } from './contest'

export interface AdminUserItem {
  id: number
  username: string
  nickname?: string
  email?: string
  status: number
  roles: string[]
  createdTime?: string
  lastLoginTime?: string
}

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

export async function fetchAdminUsers(params: {
  keyword?: string
  pageNum?: number
  pageSize?: number
} = {}): Promise<PageResult<AdminUserItem>> {
  const query = new URLSearchParams()
  if (params.keyword) query.set('keyword', params.keyword)
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return authRequest<PageResult<AdminUserItem>>(`/api/admin/users?${query.toString()}`)
}

export async function updateUserStatus(userId: number, status: number): Promise<void> {
  await authRequest<void>(`/api/admin/users/${userId}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status }),
  })
}

export async function updateUserRoles(userId: number, roles: string[]): Promise<void> {
  await authRequest<void>(`/api/admin/users/${userId}/roles`, {
    method: 'PUT',
    body: JSON.stringify({ roles }),
  })
}
