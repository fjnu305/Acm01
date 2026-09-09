export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface CfRatingChange {
  contestId: number
  contestName: string
  rank?: number
  oldRating: number
  newRating: number
  ratedAt: string
}

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  email?: string
  avatar?: string
  school?: string
  bio?: string
  cfHandle?: string
  cfRating?: number
  solvedCount?: number
  acCount?: number
  contestCount?: number
  roles: string[]
  ratingSnapshots?: RatingSnapshot[]
  cfRatingHistory?: CfRatingChange[]
}

export interface RatingSnapshot {
  platform: string
  rating: number
  maxRating?: number
  rank?: string
  snapshotDate: string
}

export interface AuthResponse {
  token: string
  user: UserInfo
}

export interface AdminDashboard {
  totalUsers: number
  pendingReviews: number
  todayRegistrations: number
}

export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.code = code
  }
}

const TOKEN_KEY = 'acm_token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string, remember = true): void {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
  }
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
}

export function isAdmin(user: UserInfo): boolean {
  return user.roles.includes('ADMIN')
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

export async function login(username: string, password: string, remember = true): Promise<AuthResponse> {
  const result = await request<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  setToken(result.data.token, remember)
  return result.data
}

export async function register(payload: {
  username: string
  password: string
  nickname?: string
  email?: string
}): Promise<AuthResponse> {
  const result = await request<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  setToken(result.data.token, true)
  return result.data
}

export async function fetchCurrentUser(): Promise<UserInfo> {
  const result = await request<UserInfo>('/api/user/me', { method: 'GET' })
  return result.data
}

export async function updateProfile(payload: {
  nickname?: string
  email?: string
  school?: string
  bio?: string
}): Promise<UserInfo> {
  const result = await request<UserInfo>('/api/user/profile', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  return result.data
}

const AVATAR_MAX_BYTES = 2 * 1024 * 1024
const AVATAR_ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']

export function validateAvatarFile(file: File): string | null {
  if (!AVATAR_ALLOWED_TYPES.includes(file.type)) {
    return '仅支持 JPG、PNG、WEBP、GIF 格式'
  }
  if (file.size > AVATAR_MAX_BYTES) {
    return '图片不能超过 2MB'
  }
  return null
}

export async function uploadAvatar(file: File): Promise<UserInfo> {
  const validationError = validateAvatarFile(file)
  if (validationError) {
    throw new ApiError(400, validationError)
  }

  const formData = new FormData()
  formData.append('file', file)

  const headers = new Headers()
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch('/api/user/avatar', {
    method: 'POST',
    headers,
    body: formData,
  })

  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    throw new ApiError(response.status, `上传失败 (${response.status})`)
  }

  const result = (await response.json()) as Result<UserInfo>
  if (result.code !== 200) {
    throw new ApiError(result.code, result.message)
  }

  return result.data
}

export async function fetchAdminDashboard(): Promise<AdminDashboard> {
  const result = await request<AdminDashboard>('/api/admin/dashboard', { method: 'GET' })
  return result.data
}
