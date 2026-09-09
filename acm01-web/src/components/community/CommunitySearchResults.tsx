import { Link } from 'react-router-dom'
import type { SearchHit } from '../../api/search'
import type { SolutionItem } from '../../api/solution'
import { formatDateTime } from '../../api/solution'
import { teamStatusLabel } from '../../api/team'
import TagChips from './TagChips'
import ZhSolutionCard from './ZhSolutionCard'

function stripHighlight(html: string): string {
  return html.replace(/<\/?em>/gi, '')
}

function hitToSolution(hit: SearchHit): SolutionItem {
  return {
    id: hit.refId,
    userId: 0,
    authorName: hit.authorName || '未知作者',
    title: stripHighlight(hit.highlights?.[0] ?? hit.title),
    tags: hit.tags,
    favoriteCount: 0,
    viewCount: 0,
    createdTime: hit.createdTime || new Date().toISOString(),
    favorited: false,
  }
}

interface CommunitySearchResultsProps {
  hits: SearchHit[]
  total: number
  loading: boolean
  query: string
  /** 当前板块：用对应列表卡片样式渲染 */
  scope?: 'solution' | 'team' | 'all'
  /** 外层已有提示时隐藏内部条数提示 */
  hideHint?: boolean
  onToggleFavorite?: (item: SolutionItem) => void
  favoriteLoadingId?: number | null
}

export default function CommunitySearchResults({
  hits,
  total,
  loading,
  query,
  scope = 'all',
  hideHint = false,
  onToggleFavorite,
  favoriteLoadingId,
}: CommunitySearchResultsProps) {
  if (!query.trim()) return null

  if (loading) {
    return (
      <div className="comm-loading">
        <div className="boot-spinner" />
        <span>正在搜索「{query}」…</span>
      </div>
    )
  }

  if (hits.length === 0) {
    return (
      <div className="comm-empty">
        <h3>未找到相关结果</h3>
        <p>没有与「{query}」匹配的内容，换个关键词试试</p>
      </div>
    )
  }

  const solutionHits = hits.filter((h) => h.type === 'solution')
  const teamHits = hits.filter((h) => h.type === 'team')
  const showSolutions = scope === 'solution' || scope === 'all'
  const showTeams = scope === 'team' || scope === 'all'

  return (
    <div className="comm-search-unified">
      {!hideHint && (
        <p className="comm-filter-hint">
          搜索「{query}」共 {total} 条
          {scope === 'solution' ? '题解' : scope === 'team' ? '组队' : '结果'}
        </p>
      )}

      {showSolutions && solutionHits.length > 0 && (
        <div className="feed-stream zh-feed-stream">
          {solutionHits.map((hit) => {
            const item = hitToSolution(hit)
            return (
              <ZhSolutionCard
                key={`solution-${hit.refId}`}
                item={item}
                favoriteLoading={favoriteLoadingId === item.id}
                onToggleFavorite={
                  onToggleFavorite ? () => onToggleFavorite(item) : undefined
                }
              />
            )
          })}
        </div>
      )}

      {showTeams && teamHits.length > 0 && (
        <div className="team-grid">
          {teamHits.map((hit) => (
            <Link key={`team-${hit.refId}`} to={`/teams/${hit.refId}`} className="team-card">
              <div className="team-card-head">
                <h3>{stripHighlight(hit.highlights?.[0] ?? hit.title)}</h3>
                <span className="team-status-badge status-1">{teamStatusLabel(1)}</span>
              </div>
              {hit.snippet && hit.snippet !== hit.title && (
                <p className="team-card-snippet">{hit.snippet}</p>
              )}
              {hit.tags && <TagChips tags={hit.tags} />}
              <div className="team-card-footer">
                <span>{hit.authorName || '未知作者'}</span>
                <span>{hit.createdTime ? formatDateTime(hit.createdTime) : ''}</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
