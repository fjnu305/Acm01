import { ApiError, type Result, getToken } from './auth'

export interface Post {
  id: number
  userId: number
  authorNickname: string
  authorAvatar?: string
  content: string
  topicId?: number
  topicName?: string
  likeCount: number
  commentCount: number
  likedByMe: boolean
  createdTime: string
}

export interface Comment {
  id: number
  postId: number
  userId: number
  authorNickname: string
  authorAvatar?: string
  content: string
  parentId?: number
  createdTime: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
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

export async function fetchHotFeed(): Promise<Post[]> {
  const result = await request<Post[]>('/api/posts/feed/hot', { method: 'GET' })
  return result.data
}

export async function fetchPosts(pageNum = 1, pageSize = 20): Promise<PageResult<Post>> {
  const result = await request<PageResult<Post>>(
    `/api/posts?pageNum=${pageNum}&pageSize=${pageSize}`,
    { method: 'GET' },
  )
  return result.data
}

export async function createPost(content: string, topicId?: number): Promise<Post> {
  const result = await request<Post>('/api/posts', {
    method: 'POST',
    body: JSON.stringify({ content, topicId }),
  })
  return result.data
}

export async function likePost(postId: number): Promise<Post> {
  const result = await request<Post>(`/api/posts/${postId}/like`, { method: 'POST' })
  return result.data
}

export async function unlikePost(postId: number): Promise<Post> {
  const result = await request<Post>(`/api/posts/${postId}/like`, { method: 'DELETE' })
  return result.data
}

export async function fetchComments(postId: number): Promise<Comment[]> {
  const result = await request<Comment[]>(`/api/posts/${postId}/comments`, { method: 'GET' })
  return result.data
}

export async function createComment(
  postId: number,
  content: string,
  parentId?: number,
): Promise<Comment> {
  const result = await request<Comment>(`/api/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content, parentId }),
  })
  return result.data
}

export async function followUser(userId: number): Promise<void> {
  await request<void>(`/api/follow/${userId}`, { method: 'POST' })
}

export async function unfollowUser(userId: number): Promise<void> {
  await request<void>(`/api/follow/${userId}`, { method: 'DELETE' })
}
