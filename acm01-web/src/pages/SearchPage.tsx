import { useState } from 'react'
import { searchContent, type SearchHit, type SearchType } from '../api/search'
import { ApiError } from '../api/auth'
import CommunitySearchResults from '../components/community/CommunitySearchResults'
import PageHeader from '../components/PageHeader'

const TYPE_OPTIONS: { label: string; value: SearchType }[] = [
  { label: '全部', value: 'all' },
  { label: '题解', value: 'solution' },
  { label: '组队', value: 'team' },
]

export default function SearchPage() {
  const [query, setQuery] = useState('')
  const [type, setType] = useState<SearchType>('all')
  const [hits, setHits] = useState<SearchHit[]>([])
  const [total, setTotal] = useState(0)
  const [searched, setSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim()) return
    setLoading(true)
    setError('')
    try {
      const result = await searchContent({ q: query.trim(), type })
      setHits(result.hits)
      setTotal(result.total)
      setSearched(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '搜索失败')
      setHits([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-container">
      <PageHeader title="全文搜索" description="检索题解与组队帖" />

      <form className="filter-bar" onSubmit={(e) => void handleSearch(e)}>
        <input
          type="search"
          placeholder="输入关键词"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select value={type} onChange={(e) => setType(e.target.value as SearchType)}>
          {TYPE_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? '搜索中…' : '搜索'}
        </button>
      </form>

      {error && <p className="error-text">{error}</p>}

      {searched && (
        <CommunitySearchResults
          hits={hits}
          total={total}
          loading={loading}
          query={query}
          scope={type === 'solution' ? 'solution' : type === 'team' ? 'team' : 'all'}
        />
      )}
    </div>
  )
}
