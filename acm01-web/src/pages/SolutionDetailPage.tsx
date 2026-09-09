import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import {
  favoriteSolution,
  fetchSolutionDetail,
  formatDateTime,
  unfavoriteSolution,
  type SolutionDetail,
} from '../api/solution'
import TagChips from '../components/community/TagChips'

export default function SolutionDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const solutionId = Number(id)
  const [solution, setSolution] = useState<SolutionDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [favLoading, setFavLoading] = useState(false)

  useEffect(() => {
    if (!Number.isFinite(solutionId)) {
      setError('无效的题解 ID')
      setLoading(false)
      return
    }
    void (async () => {
      setLoading(true)
      setError('')
      try {
        const data = await fetchSolutionDetail(solutionId)
        setSolution(data)
      } catch (err) {
        setError(err instanceof ApiError ? err.message : '加载题解失败')
      } finally {
        setLoading(false)
      }
    })()
  }, [solutionId])

  const toggleFavorite = async () => {
    if (!solution) return
    setFavLoading(true)
    try {
      if (solution.favorited) {
        await unfavoriteSolution(solution.id)
        setSolution({ ...solution, favorited: false, favoriteCount: solution.favoriteCount - 1 })
      } else {
        await favoriteSolution(solution.id)
        setSolution({ ...solution, favorited: true, favoriteCount: solution.favoriteCount + 1 })
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    } finally {
      setFavLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="comm-loading">
        <div className="boot-spinner" />
        <span>加载中…</span>
      </div>
    )
  }

  if (error && !solution) {
    return (
      <div className="comm-body">
        <div className="alert alert-error">{error}</div>
      </div>
    )
  }

  if (!solution) return null

  return (
    <div className="comm-detail-wrap">
      <nav className="comm-detail-nav">
        <Link to="/solutions">← 返回题解</Link>
        <Link to="/social">讨论</Link>
        <Link to="/teams">组队</Link>
      </nav>

      <article className="article-card">
        <header className="article-header">
          <h1 className="article-title">{solution.title}</h1>
          <div className="article-meta-row">
            <span className="article-author">{solution.authorName}</span>
            <span>{formatDateTime(solution.createdTime)}</span>
            {solution.problemSource && solution.problemId && (
              <span className="sol-card-problem">
                {solution.problemSource} {solution.problemId}
              </span>
            )}
            <span>👁 {solution.viewCount}</span>
          </div>
          {solution.tags && <TagChips tags={solution.tags} />}
        </header>

        <div className="article-body">
          <div className="article-content">{solution.content}</div>
        </div>

        <footer className="article-actions">
          <button
            type="button"
            className={`btn-fav${solution.favorited ? ' favorited' : ''}`}
            disabled={favLoading}
            onClick={() => void toggleFavorite()}
          >
            {solution.favorited ? '⭐ 已收藏' : '⭐ 收藏'} ({solution.favoriteCount})
          </button>
          <button type="button" className="btn-outline" onClick={() => navigate(-1)}>
            返回
          </button>
        </footer>
      </article>
    </div>
  )
}
