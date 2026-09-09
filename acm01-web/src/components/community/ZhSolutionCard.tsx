import { Link } from 'react-router-dom'
import type { SolutionItem } from '../../api/solution'
import ClickableUserAvatar from '../ClickableUserAvatar'

function formatTime(value: string) {
  const date = new Date(value)
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function buildExcerpt(item: SolutionItem): string {
  if (!item.tags?.trim()) return ''
  return item.tags
    .split(/[,，\s]+/)
    .filter(Boolean)
    .slice(0, 4)
    .join(' · ')
}

function IconStar({ active }: { active?: boolean }) {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill={active ? 'currentColor' : 'none'}
      stroke="currentColor"
      strokeWidth="2"
      aria-hidden
    >
      <path d="M12 3 L15 9 L22 10 L17 15 L18 22 L12 18 L6 22 L7 15 L2 10 L9 9 Z" strokeLinejoin="round" />
    </svg>
  )
}

function IconView() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

interface ZhSolutionCardProps {
  item: SolutionItem
  onToggleFavorite?: () => void
  onDismiss?: () => void
  favoriteLoading?: boolean
}

export default function ZhSolutionCard({
  item,
  onToggleFavorite,
  onDismiss,
  favoriteLoading,
}: ZhSolutionCardProps) {
  const excerpt = buildExcerpt(item)
  const problemLabel =
    item.problemSource && item.problemId ? `${item.problemSource} ${item.problemId}` : null

  return (
    <article className="feed-card zh-feed-card zh-solution-card">
      <Link to={`/solutions/${item.id}`} className="zh-feed-title-link">
        <h3 className="zh-feed-title">{item.title}</h3>
      </Link>

      <div className="zh-feed-author">
        <ClickableUserAvatar
          user={{
            userId: item.userId,
            username: item.authorName,
            nickname: item.authorName,
            avatar: '',
            roles: [],
          }}
          className="zh-feed-avatar"
        />
        <div className="zh-feed-author-meta">
          <span className="zh-feed-author-name">{item.authorName}</span>
          {problemLabel && <span className="zh-feed-badge">{problemLabel}</span>}
          <time className="zh-feed-time">{formatTime(item.createdTime)}</time>
        </div>
      </div>

      {excerpt && <p className="zh-feed-excerpt">{excerpt}</p>}

      <footer className="zh-feed-footer">
        <button
          type="button"
          className={`zh-feed-action${item.favorited ? ' active' : ''}`}
          disabled={favoriteLoading || !onToggleFavorite}
          onClick={(e) => {
            e.preventDefault()
            onToggleFavorite?.()
          }}
        >
          <IconStar active={item.favorited} />
          <span>{item.favorited ? '已收藏' : '收藏'}</span>
          <em>{item.favoriteCount}</em>
        </button>
        <Link to={`/solutions/${item.id}`} className="zh-feed-action zh-feed-action-link">
          <IconView />
          <span>{item.viewCount > 0 ? item.viewCount : '浏览'}</span>
        </Link>
        {onDismiss && (
          <button
            type="button"
            className="zh-feed-dismiss"
            onClick={onDismiss}
            aria-label="不感兴趣"
          >
            ×
          </button>
        )}
      </footer>
    </article>
  )
}
