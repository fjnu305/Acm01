import { useMemo } from 'react'
import type { Comment, Post } from '../../api/social'
import ClickableUserAvatar from '../ClickableUserAvatar'
import { parsePostContent } from '../../utils/parsePostContent'

function formatTime(value: string) {
  const date = new Date(value)
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function IconAgree({ active }: { active?: boolean }) {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M12 4 L5 14 H19 Z" strokeLinejoin="round" />
    </svg>
  )
}

function IconComment() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M21 11.5a8.5 8.5 0 0 1-8.5 8.5H7l-4 3V11.5a8.5 8.5 0 1 1 18 0z" strokeLinejoin="round" />
    </svg>
  )
}

interface ZhFeedCardProps {
  post: Post
  expanded: boolean
  comments: Comment[]
  commentDraft: string
  commentLoading: boolean
  onToggleLike: () => void
  onToggleComments: () => void
  onDismiss: () => void
  onCommentDraftChange: (value: string) => void
  onSubmitComment: () => void
}

export default function ZhFeedCard({
  post,
  expanded,
  comments,
  commentDraft,
  commentLoading,
  onToggleLike,
  onToggleComments,
  onDismiss,
  onCommentDraftChange,
  onSubmitComment,
}: ZhFeedCardProps) {
  const parsed = useMemo(() => parsePostContent(post.content), [post.content])
  const galleryClass =
    parsed.images.length === 1
      ? 'zh-feed-gallery zh-feed-gallery-1'
      : parsed.images.length === 2
        ? 'zh-feed-gallery zh-feed-gallery-2'
        : 'zh-feed-gallery zh-feed-gallery-3'

  return (
    <article className="feed-card zh-feed-card">
      <h3 className="zh-feed-title">{parsed.title}</h3>

      <div className="zh-feed-author">
        <ClickableUserAvatar
          user={{
            userId: post.userId,
            username: post.authorNickname,
            nickname: post.authorNickname,
            avatar: post.authorAvatar,
            roles: [],
          }}
          className="zh-feed-avatar"
          stopPropagation
        />
        <div className="zh-feed-author-meta">
          <span className="zh-feed-author-name">{post.authorNickname}</span>
          {post.topicName && <span className="zh-feed-badge">{post.topicName}</span>}
          <time className="zh-feed-time">{formatTime(post.createdTime)}</time>
        </div>
      </div>

      {parsed.excerpt && <p className="zh-feed-excerpt">{parsed.excerpt}</p>}

      {parsed.images.length > 0 && (
        <div className={galleryClass}>
          {parsed.images.map((src: string) => (
            <a
              key={src}
              href={src}
              target="_blank"
              rel="noreferrer"
              className="zh-feed-thumb"
            >
              <img src={src} alt="" loading="lazy" />
            </a>
          ))}
        </div>
      )}

      <footer className="zh-feed-footer">
        <button
          type="button"
          className={`zh-feed-action${post.likedByMe ? ' active' : ''}`}
          onClick={onToggleLike}
        >
          <IconAgree active={post.likedByMe} />
          <span>{post.likedByMe ? '已赞同' : '赞同'}</span>
          <em>{post.likeCount}</em>
        </button>
        <button type="button" className="zh-feed-action" onClick={onToggleComments}>
          <IconComment />
          <span>{post.commentCount > 0 ? post.commentCount : '评论'}</span>
        </button>
        <button
          type="button"
          className="zh-feed-dismiss"
          onClick={onDismiss}
          aria-label="不感兴趣"
        >
          ×
        </button>
      </footer>

      {expanded && (
        <div className="feed-comments">
          {(comments ?? []).map((c) => (
            <div key={c.id} className={`feed-comment${c.parentId ? ' feed-comment-reply' : ''}`}>
              <ClickableUserAvatar
                user={{
                  userId: c.userId,
                  username: c.authorNickname,
                  nickname: c.authorNickname,
                  avatar: c.authorAvatar,
                  roles: [],
                }}
                className="feed-comment-avatar"
              />
              <div className="feed-comment-body">
                <strong>{c.authorNickname}</strong>
                <span>{c.content}</span>
                <time>{formatTime(c.createdTime)}</time>
              </div>
            </div>
          ))}
          <div className="feed-comment-input">
            <input
              type="text"
              value={commentDraft}
              onChange={(e) => onCommentDraftChange(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  onSubmitComment()
                }
              }}
              placeholder="写评论，按 Enter 发送…"
              maxLength={1000}
              disabled={commentLoading}
            />
            <button
              type="button"
              className="btn-feed-post"
              onClick={onSubmitComment}
              disabled={commentLoading}
            >
              发送
            </button>
          </div>
        </div>
      )}
    </article>
  )
}
