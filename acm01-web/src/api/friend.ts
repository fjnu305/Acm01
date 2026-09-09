import { ApiError, getToken, type Result } from './auth'

export interface FriendItem {
  userId: number
  username: string
  nickname?: string
  avatar?: string
  school?: string
  cfRating?: number
  friendsSince: string
  official?: boolean
}

export interface FriendRequestItem {
  id: number
  requesterId: number
  requesterName: string
  requesterAvatar?: string
  addresseeId: number
  addresseeName: string
  addresseeAvatar?: string
  message?: string
  status: number
  createdTime: string
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(path, { ...options, headers })
  const result = (await response.json()) as Result<T>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }
  return result.data
}

export async function fetchFriends(): Promise<FriendItem[]> {
  return request<FriendItem[]>('/api/friends')
}

export async function fetchIncomingFriendRequests(): Promise<FriendRequestItem[]> {
  return request<FriendRequestItem[]>('/api/friends/requests/incoming')
}

export async function fetchOutgoingFriendRequests(): Promise<FriendRequestItem[]> {
  return request<FriendRequestItem[]>('/api/friends/requests/outgoing')
}

export async function sendFriendRequest(targetUserId: number, message?: string): Promise<FriendRequestItem> {
  return request<FriendRequestItem>('/api/friends/requests', {
    method: 'POST',
    body: JSON.stringify({ targetUserId, message }),
  })
}

export async function acceptFriendRequest(requestId: number): Promise<void> {
  await request<void>(`/api/friends/requests/${requestId}/accept`, { method: 'POST' })
}

export async function rejectFriendRequest(requestId: number): Promise<void> {
  await request<void>(`/api/friends/requests/${requestId}/reject`, { method: 'POST' })
}

export async function cancelFriendRequest(requestId: number): Promise<void> {
  await request<void>(`/api/friends/requests/${requestId}`, { method: 'DELETE' })
}

export async function removeFriend(userId: number): Promise<void> {
  await request<void>(`/api/friends/${userId}`, { method: 'DELETE' })
}
