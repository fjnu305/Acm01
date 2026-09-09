import { useCallback, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { searchContent, type SearchHit } from '../api/search'
import {
  type SolutionItem,
  type SolutionTagStat,
  favoriteSolution,
  fetchHotSolutionTags,
  fetchSolutionCategories,
  fetchSolutionList,
  unfavoriteSolution,
} from '../api/solution'
import CommunitySearchResults from '../components/community/CommunitySearchResults'
import SolutionSidebar from '../components/community/SolutionSidebar'
import ZhSolutionCard from '../components/community/ZhSolutionCard'

const PAGE_SIZE = 20

export default function SolutionListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const globalQ = searchParams.get('q') ?? ''
  const activeTag = searchParams.get('tag') ?? ''

  const [solutions, setSolutions] = useState<SolutionItem[]>([])
  const [searchHits, setSearchHits] = useState<SearchHit[]>([])
  const [searchTotal, setSearchTotal] = useState(0)
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [hiddenIds, setHiddenIds] = useState<Set<number>>(() => new Set())
  const [favoriteLoadingId, setFavoriteLoadingId] = useState<number | null>(null)
  const [hotTags, setHotTags] = useState<SolutionTagStat[]>([])
  const [categories, setCategories] = useState<SolutionTagStat[]>([])
  const [sidebarLoading, setSidebarLoading] = useState(true)

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))
  const isSearchMode = Boolean(globalQ.trim())

  const setTagFilter = (tag: string) => {
    const next = new URLSearchParams(searchParams)
    if (!tag || activeTag === tag) {
      next.delete('tag')
    } else {
      next.set('tag', tag)
    }
    setSearchParams(next, { replace: true })
    setPageNum(1)
  }

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      if (isSearchMode) {
        const result = await searchContent({ q: globalQ.trim(), type: 'solution' })
        setSearchHits(result.hits)
        setSearchTotal(result.total)
        setSolutions([])
        setTotal(0)
      } else {
        const data = await fetchSolutionList({
          tag: activeTag || undefined,
          pageNum,
          pageSize: PAGE_SIZE,
        })
        setSolutions(data.list)
        setTotal(data.total)
        setSearchHits([])
        setSearchTotal(0)
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '加载失败')
      setSolutions([])
      setSearchHits([])
      setTotal(0)
      setSearchTotal(0)
    } finally {
      setLoading(false)
    }
  }, [globalQ, isSearchMode, activeTag, pageNum])

  useEffect(() => {
    void loadData()
  }, [loadData])

  useEffect(() => {
    let cancelled = false
    const loadSidebar = async () => {
      setSidebarLoading(true)
      try {
        const [hot, cats] = await Promise.all([
          fetchHotSolutionTags(8),
          fetchSolutionCategories(),
        ])
        if (!cancelled) {
          setHotTags(hot)
          setCategories(cats)
        }
      } catch {
        if (!cancelled) {
          setHotTags([])
          setCategories([])
        }
      } finally {
        if (!cancelled) {
          setSidebarLoading(false)
        }
      }
    }
    void loadSidebar()
    return () => {
      cancelled = true
    }
  }, [])

  const visibleSolutions = solutions.filter((item) => !hiddenIds.has(item.id))

  const handleToggleFavorite = async (item: SolutionItem) => {
    setFavoriteLoadingId(item.id)
    try {
      if (item.favorited) {
        await unfavoriteSolution(item.id)
        setSolutions((prev) =>
          prev.map((s) =>
            s.id === item.id
              ? { ...s, favorited: false, favoriteCount: Math.max(0, s.favoriteCount - 1) }
              : s,
          ),
        )
      } else {
        await favoriteSolution(item.id)
        setSolutions((prev) =>
          prev.map((s) =>
            s.id === item.id ? { ...s, favorited: true, favoriteCount: s.favoriteCount + 1 } : s,
          ),
        )
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '收藏操作失败')
    } finally {
      setFavoriteLoadingId(null)
    }
  }

  const handleDismiss = (id: number) => {
    setHiddenIds((prev) => new Set(prev).add(id))
  }

  return (
    <div className="comm-body comm-board-solutions">
      <div className="comm-layout">
        <div className="comm-main">
          <div className="comm-section-head">
            <div>
              <h2>题解库</h2>
              <p>
                {activeTag ? `正在浏览「${activeTag}」相关题解` : '推荐流展示最新题解，右侧可按分类筛选'}
              </p>
            </div>
          </div>

          {isSearchMode && (
            <p className="comm-filter-hint comm-filter-hint-block">
              当前为全文搜索；清除顶部关键词后可恢复标签筛选。
            </p>
          )}

          {error && <div className="alert alert-error">{error}</div>}

          {isSearchMode ? (
            <CommunitySearchResults
              hits={searchHits}
              total={searchTotal}
              loading={loading}
              query={globalQ}
              scope="solution"
              hideHint
            />
          ) : (
            <div className="sol-list-panel">
              {loading ? (
                <div className="comm-loading">
                  <div className="boot-spinner" />
                  <span>加载题解中…</span>
                </div>
              ) : visibleSolutions.length === 0 ? (
                <div className="comm-empty">
                  <h3>{activeTag ? `没有「${activeTag}」标签的题解` : '还没有题解'}</h3>
                  <p>
                    <Link to="/solutions/new">发布第一篇题解</Link>
                  </p>
                </div>
              ) : (
                <div className="feed-stream zh-feed-stream">
                  {visibleSolutions.map((item) => (
                    <ZhSolutionCard
                      key={item.id}
                      item={item}
                      favoriteLoading={favoriteLoadingId === item.id}
                      onToggleFavorite={() => void handleToggleFavorite(item)}
                      onDismiss={() => handleDismiss(item.id)}
                    />
                  ))}
                </div>
              )}
            </div>
          )}

          {!isSearchMode && totalPages > 1 && (
            <div className="comm-pagination">
              <button type="button" disabled={pageNum <= 1} onClick={() => setPageNum((p) => p - 1)}>
                上一页
              </button>
              <span>{pageNum} / {totalPages}</span>
              <button
                type="button"
                disabled={pageNum >= totalPages}
                onClick={() => setPageNum((p) => p + 1)}
              >
                下一页
              </button>
            </div>
          )}
        </div>

        <aside className="comm-sidebar">
          {!isSearchMode && (
            <SolutionSidebar
              hotTags={hotTags}
              categories={categories}
              activeTag={activeTag}
              loading={sidebarLoading}
              onSelectTag={setTagFilter}
            />
          )}
          {isSearchMode && (
            <div className="side-widget side-widget-compact">
              <p className="side-tip">搜索模式下暂不可用侧边筛选，请清除顶部关键词后使用分类。</p>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}
