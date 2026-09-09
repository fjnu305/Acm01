import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { searchContent, type SearchHit } from '../api/search'
import {
  createComment,
  fetchComments,
  fetchHotFeed,
  likePost,
  type Comment,
  type Post,
  unlikePost,
} from '../api/social'
import CommunitySearchResults from '../components/community/CommunitySearchResults'
import SocialPostModal from '../components/community/SocialPostModal'
import ZhFeedCard from '../components/community/ZhFeedCard'

export default function SocialFeedPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const globalQ = searchParams.get('q') ?? ''
  const isSearchMode = Boolean(globalQ.trim())
  const composeOpen = searchParams.get('compose') === '1'

  const [posts, setPosts] = useState<Post[]>([])
  const [searchHits, setSearchHits] = useState<SearchHit[]>([])
  const [searchTotal, setSearchTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [hiddenIds, setHiddenIds] = useState<Set<number>>(() => new Set())
  const [expandedPostId, setExpandedPostId] = useState<number | null>(null)
  const [comments, setComments] = useState<Record<number, Comment[]>>({})
  const [commentDrafts, setCommentDrafts] = useState<Record<number, string>>({})
  const [commentLoading, setCommentLoading] = useState<number | null>(null)

  const closeCompose = () => {
    const next = new URLSearchParams(searchParams)
    next.delete('compose')
    setSearchParams(next, { replace: true })
  }

  const loadFeed = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      if (isSearchMode) {
        const result = await searchContent({ q: globalQ.trim(), type: 'all' })
        setSearchHits(result.hits)
        setSearchTotal(result.total)
        setPosts([])
      } else {
        const feed = await fetchHotFeed()
        setPosts(feed)
        setSearchHits([])
        setSearchTotal(0)
      }
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '加载失败'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }, [globalQ, isSearchMode])

  useEffect(() => {
    void loadFeed()
  }, [loadFeed])

  const visiblePosts = posts.filter((p) => !hiddenIds.has(p.id))

  const handlePosted = (created: Post) => {
    setPosts((prev) => [created, ...prev.filter((p) => p.id !== created.id)])
  }

  const handleToggleLike = async (post: Post) => {
    try {
      const updated = post.likedByMe ? await unlikePost(post.id) : await likePost(post.id)
      setPosts((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '操作失败'
      setError(msg)
    }
  }

  const handleExpandComments = async (postId: number) => {
    if (expandedPostId === postId) {
      setExpandedPostId(null)
      return
    }
    setExpandedPostId(postId)
    if (!comments[postId]) {
      try {
        const list = await fetchComments(postId)
        setComments((prev) => ({ ...prev, [postId]: list }))
      } catch {
        setComments((prev) => ({ ...prev, [postId]: [] }))
      }
    }
  }

  const handleSubmitComment = async (postId: number) => {
    const content = (commentDrafts[postId] ?? '').trim()
    if (!content) return

    setCommentLoading(postId)
    try {
      const created = await createComment(postId, content)
      setComments((prev) => ({
        ...prev,
        [postId]: [...(prev[postId] ?? []), created],
      }))
      setCommentDrafts((prev) => ({ ...prev, [postId]: '' }))
      setPosts((prev) =>
        prev.map((p) =>
          p.id === postId ? { ...p, commentCount: p.commentCount + 1 } : p,
        ),
      )
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '评论失败'
      setError(msg)
    } finally {
      setCommentLoading(null)
    }
  }

  const handleDismiss = (postId: number) => {
    setHiddenIds((prev) => new Set(prev).add(postId))
    if (expandedPostId === postId) {
      setExpandedPostId(null)
    }
  }

  return (
    <div className="comm-body comm-board-social">
      <SocialPostModal open={composeOpen} onClose={closeCompose} onPosted={handlePosted} />

      <div className="comm-layout">
        <div className="comm-main">
          <div className="comm-section-head">
            <div>
              <h2>讨论广场</h2>
              <p>热榜动态按互动热度排序</p>
            </div>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          {!isSearchMode && (
            <div className="feed-channel-bar" aria-label="讨论分区">
              <span className="feed-channel active">推荐</span>
              <span className="feed-channel muted">按热度排序</span>
            </div>
          )}

          {isSearchMode ? (
            <CommunitySearchResults
              hits={searchHits}
              total={searchTotal}
              loading={loading}
              query={globalQ}
              scope="all"
            />
          ) : loading ? (
            <div className="comm-loading">
              <div className="boot-spinner" />
              <span>加载动态…</span>
            </div>
          ) : visiblePosts.length === 0 ? (
            <div className="comm-empty">
              <h3>广场还很安静</h3>
              <p>使用右上角「发布动态」写下第一条内容</p>
            </div>
          ) : (
            <div className="feed-stream zh-feed-stream">
              {visiblePosts.map((post) => (
                <ZhFeedCard
                  key={post.id}
                  post={post}
                  expanded={expandedPostId === post.id}
                  comments={comments[post.id] ?? []}
                  commentDraft={commentDrafts[post.id] ?? ''}
                  commentLoading={commentLoading === post.id}
                  onToggleLike={() => void handleToggleLike(post)}
                  onToggleComments={() => void handleExpandComments(post.id)}
                  onDismiss={() => handleDismiss(post.id)}
                  onCommentDraftChange={(value) =>
                    setCommentDrafts((prev) => ({ ...prev, [post.id]: value }))
                  }
                  onSubmitComment={() => void handleSubmitComment(post.id)}
                />
              ))}
            </div>
          )}
        </div>

        <aside className="comm-sidebar">
          <div className="side-widget side-widget-accent side-widget-social">
            <h4>讨论广场</h4>
            <p className="side-tip">
              卡片布局参考知乎推荐流：标题、作者、摘要与配图。正文中的图片链接会自动展示。
            </p>
          </div>
        </aside>
      </div>
    </div>
  )
}
