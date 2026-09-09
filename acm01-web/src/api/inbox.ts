import { ApiError, getToken, type Result } from './auth'
import type { PageResult } from './solution'

export interface InboxMessage {
  id: number
  senderId: number
  senderName?: string
  senderAvatar?: string
  category: 'OFFICIAL' | 'PERSONAL' | 'FRIEND' | string
  title: string
  body: string
  refType?: string
  refId?: number
  read?: boolean
  readAt?: string
  createdTime: string
}

export interface ChatMessage {
  id: number
  senderId: number
  recipientId: number
  senderName?: string
  senderAvatar?: string
  title?: string
  body: string
  refType?: string
  refId?: number
  mine?: boolean
  read?: boolean
  createdTime: string
}

export interface ConversationSummary {
  peerId: number
  peerName: string
  peerAvatar?: string
  official?: boolean
  lastMessageBody?: string
  lastMessageTitle?: string
  lastMessageTime: string
  unreadCount?: number
}

export const INBOX_PUSH_EVENT = 'acm-inbox-push'
export const INBOX_REF_TEAM_INVITE = 'team_invite'
export const INBOX_REF_TEAM_APPLY = 'team_apply'
export const INBOX_REF_TEAM_APPLY_ACCEPTED = 'team_apply_accepted'
export const INBOX_REF_TEAM_APPLY_REJECTED = 'team_apply_rejected'

const TEAM_INBOX_REF_TYPES = new Set([
  INBOX_REF_TEAM_INVITE,
  INBOX_REF_TEAM_APPLY,
  INBOX_REF_TEAM_APPLY_ACCEPTED,
  INBOX_REF_TEAM_APPLY_REJECTED,
])

export function isTeamInboxRef(refType?: string): boolean {
  return !!refType && TEAM_INBOX_REF_TYPES.has(refType)
}

export interface InboxPushDetail {
  inboxId?: number
  inboxSenderId?: number
  title?: string
  body?: string
  inboxRefType?: string
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

export async function fetchInbox(params: {
  category?: string
  pageNum?: number
  pageSize?: number
} = {}): Promise<PageResult<InboxMessage>> {
  const query = new URLSearchParams()
  if (params.category) query.set('category', params.category)
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return request<PageResult<InboxMessage>>(`/api/inbox?${query.toString()}`)
}

export async function fetchConversations(): Promise<ConversationSummary[]> {
  return request<ConversationSummary[]>('/api/inbox/conversations')
}

export async function fetchChatThread(peerId: number, limit = 100): Promise<ChatMessage[]> {
  return request<ChatMessage[]>(`/api/inbox/thread/${peerId}?limit=${limit}`)
}

export async function markChatThreadRead(peerId: number): Promise<void> {
  await request<void>(`/api/inbox/thread/${peerId}/read`, { method: 'POST' })
}

export async function fetchInboxUnreadCount(): Promise<number> {
  return request<number>('/api/inbox/unread-count')
}

export async function fetchInboxDetail(id: number): Promise<InboxMessage> {
  return request<InboxMessage>(`/api/inbox/${id}`)
}

export async function markInboxRead(id: number): Promise<InboxMessage> {
  return request<InboxMessage>(`/api/inbox/${id}/read`, { method: 'POST' })
}

export async function markAllInboxRead(): Promise<void> {
  await request<void>('/api/inbox/read-all', { method: 'POST' })
}

export async function sendPersonalMessage(
  recipientId: number,
  body: string,
): Promise<ChatMessage> {
  return request<ChatMessage>('/api/inbox/messages', {
    method: 'POST',
    body: JSON.stringify({ recipientId, body }),
  })
}

export function inboxCategoryLabel(category: string): string {
  switch (category) {
    case 'OFFICIAL':
    case 'SYSTEM':
      return '官方'
    case 'FRIEND':
      return '好友'
    case 'PERSONAL':
      return '私信'
    default:
      return category
  }
}
