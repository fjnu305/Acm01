import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/auth'
import {
  favoriteSolution,
  fetchFavoriteSolutions,
  unfavoriteSolution,
  type SolutionItem,
} from '../api/solution'
import ZhSolutionCard from '../components/community/ZhSolutionCard'
import PageHeader from '../components/PageHeader'

const PAGE_SIZE = 20

export default function MyFavoritesPage() {
  const [items, setItems] = useState<SolutionItem[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await fetchFavoriteSolutions({ pageNum, pageSize: PAGE_SIZE })
      setItems(data.list)
      setTotal(data.total)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '加载失败')
      setItems([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }, [pageNum])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const handleToggleFavorite = async (item: SolutionItem) => {
    try {
      if (item.favorited) {
        await unfavoriteSolution(item.id)
        setItems((prev) => prev.filter((row) => row.id !== item.id))
        setTotal((prev) => Math.max(0, prev - 1))
      } else {
        await favoriteSolution(item.id)
        setItems((prev) => prev.map((row) => (row.id === item.id ? { ...row, favorited: true } : row)))
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  return (
    <div className="page-content">
      <PageHeader title="我的收藏" description="你收藏的题解会显示在这里" />

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="comm-loading">
          <div className="boot-spinner" />
          <span>加载中…</span>
        </div>
      ) : items.length === 0 ? (
        <div className="empty-panel">
          <p>还没有收藏任何题解</p>
          <Link to="/solutions" className="text-link-btn">去逛逛题解</Link>
        </div>
      ) : (
        <>
          <div className="feed-stream zh-feed-stream">
            {items.map((item) => (
              <ZhSolutionCard
                key={item.id}
                item={item}
                onToggleFavorite={() => void handleToggleFavorite(item)}
              />
            ))}
          </div>
          {totalPages > 1 && (
            <div className="comm-pager">
              <button type="button" disabled={pageNum <= 1} onClick={() => setPageNum((p) => p - 1)}>
                上一页
              </button>
              <span>{pageNum} / {totalPages}</span>
              <button type="button" disabled={pageNum >= totalPages} onClick={() => setPageNum((p) => p + 1)}>
                下一页
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
