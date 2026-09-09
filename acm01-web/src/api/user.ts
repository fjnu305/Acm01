import { ApiError, getToken, type CfRatingChange, type Result } from './auth'

export interface PublicProfile {
  userId: number
  username: string
  nickname?: string
  avatar?: string
  school?: string
  bio?: string
  cfHandle?: string
  cfRating?: number
  solvedCount?: number
  isSelf?: boolean
  friendStatus?: 'NONE' | 'PENDING_SENT' | 'PENDING_RECEIVED' | 'FRIENDS' | null
  official?: boolean
  following?: boolean
  cfRatingHistory?: CfRatingChange[]
}

async function publicRequest<T>(path: string): Promise<T> {
  const headers = new Headers()
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(path, { headers })
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

export async function fetchPublicProfile(userId: number): Promise<PublicProfile> {
  return publicRequest<PublicProfile>(`/api/users/${userId}/profile`)
}

export interface UserSearchItem {
  userId: number
  username: string
  nickname?: string
  avatar?: string
  school?: string
  friendStatus: 'NONE' | 'PENDING_SENT' | 'PENDING_RECEIVED' | 'FRIENDS' | string
  official?: boolean
}

async function authRequest<T>(path: string): Promise<T> {
  const headers = new Headers()
  const token = getToken()
  if (!token) {
    throw new ApiError(401, 'Unauthorized')
  }
  headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(path, { headers })
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

export async function searchUsers(keyword: string, limit = 20): Promise<UserSearchItem[]> {
  const query = new URLSearchParams({ q: keyword, limit: String(limit) })
  return authRequest<UserSearchItem[]>(`/api/users/search?${query.toString()}`)
}

export function friendRelationLabel(
  status: string | undefined,
  official?: boolean,
): string {
  if (official) return '官方'
  switch (status) {
    case 'FRIENDS':
      return '好友'
    case 'PENDING_SENT':
      return '已申请'
    case 'PENDING_RECEIVED':
      return '待通过'
    default:
      return '非好友'
  }
}
