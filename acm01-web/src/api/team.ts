import { ApiError, getToken, type Result } from './auth'
import type { PageResult } from './solution'

export { ApiError }

export interface TeamItem {
  id: number
  userId: number
  authorName: string
  title: string
  description?: string
  ratingMin: number
  ratingMax: number
  region?: string
  tags?: string
  memberLimit: number
  currentCount: number
  status: number
  createdTime: string
  memberPreview?: TeamMemberPreview[]
}

export interface TeamMemberPreview {
  userId: number
  username: string
  nickname?: string
  avatar?: string
}

export interface TeamMember {
  id: number
  userId: number
  username: string
  nickname?: string
  avatar?: string
  cfRating?: number
  school?: string
  role: string
  status: number
  pendingKind?: string | null
  joinedTime?: string
}

export interface TeamDetail extends TeamItem {
  members: TeamMember[]
}

export interface InvitableFriend {
  userId: number
  username: string
  nickname?: string
  avatar?: string
  school?: string
  cfRating?: number
}

export interface TeamRecommend {
  userId: number
  username: string
  nickname?: string
  avatar?: string
  cfRating?: number
  school?: string
  matchScore: number
}

export interface TeamPublishPayload {
  title: string
  description?: string
  ratingMin?: number
  ratingMax?: number
  region?: string
  tags?: string
  memberLimit?: number
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

export async function fetchMyTeams(params: {
  pageNum?: number
  pageSize?: number
} = {}): Promise<PageResult<TeamItem>> {
  const query = new URLSearchParams()
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return authRequest<PageResult<TeamItem>>(`/api/teams/mine?${query.toString()}`)
}

export async function fetchInvitableFriends(teamId: number): Promise<InvitableFriend[]> {
  return authRequest<InvitableFriend[]>(`/api/teams/${teamId}/invitable-friends`)
}

export async function fetchTeamList(params: {
  status?: number
  region?: string
  pageNum?: number
  pageSize?: number
} = {}): Promise<PageResult<TeamItem>> {
  const query = new URLSearchParams()
  if (params.status != null) query.set('status', String(params.status))
  if (params.region) query.set('region', params.region)
  query.set('pageNum', String(params.pageNum ?? 1))
  query.set('pageSize', String(params.pageSize ?? 20))
  return publicRequest<PageResult<TeamItem>>(`/api/teams?${query.toString()}`)
}

export async function fetchTeamDetail(id: number): Promise<TeamDetail> {
  return publicRequest<TeamDetail>(`/api/teams/${id}`)
}

export async function publishTeam(payload: TeamPublishPayload): Promise<TeamDetail> {
  return authRequest<TeamDetail>('/api/teams', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function fetchTeamRecommend(teamId: number): Promise<TeamRecommend[]> {
  return authRequest<TeamRecommend[]>(`/api/teams/${teamId}/recommend`)
}

export async function inviteTeamMember(teamId: number, userId: number): Promise<void> {
  await authRequest<void>(`/api/teams/${teamId}/invite/${userId}`, { method: 'POST' })
}

export async function acceptTeamInvite(memberId: number): Promise<void> {
  await authRequest<void>(`/api/teams/invites/${memberId}/accept`, { method: 'POST' })
}

export async function declineTeamInvite(memberId: number): Promise<void> {
  await authRequest<void>(`/api/teams/invites/${memberId}/decline`, { method: 'POST' })
}

export async function applyToTeam(teamId: number): Promise<void> {
  await authRequest<void>(`/api/teams/${teamId}/apply`, { method: 'POST' })
}

export async function approveTeamApplication(teamId: number, memberId: number): Promise<void> {
  await authRequest<void>(`/api/teams/${teamId}/applications/${memberId}/approve`, { method: 'POST' })
}

export async function rejectTeamApplication(teamId: number, memberId: number): Promise<void> {
  await authRequest<void>(`/api/teams/${teamId}/applications/${memberId}/reject`, { method: 'POST' })
}

/** Inbox 卡片场景：仅持有 memberId */
export async function approveTeamApplicationByMemberId(memberId: number): Promise<void> {
  await authRequest<void>(`/api/teams/applications/${memberId}/approve`, { method: 'POST' })
}

export async function rejectTeamApplicationByMemberId(memberId: number): Promise<void> {
  await authRequest<void>(`/api/teams/applications/${memberId}/reject`, { method: 'POST' })
}

export async function closeTeamRecruitment(teamId: number): Promise<void> {
  await authRequest<void>(`/api/teams/${teamId}/close`, { method: 'POST' })
}

export async function dissolveTeam(teamId: number): Promise<void> {
  await authRequest<void>(`/api/teams/${teamId}/dissolve`, { method: 'POST' })
}

export function teamStatusLabel(status: number): string {
  switch (status) {
    case 1:
      return '招募中'
    case 2:
      return '已满员'
    case 0:
      return '已结束招募'
    default:
      return '未知'
  }
}

export function memberPendingLabel(member: TeamMember): string {
  if (member.status !== 0) return ''
  return member.pendingKind === 'APPLY' ? '待审核' : '待接受邀请'
}
