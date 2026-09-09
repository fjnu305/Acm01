import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import ContestPlatformLogo from '../components/contest/ContestPlatformLogo'
import {
  fetchContestDetail,
  formatContestStatus,
  formatDateTime,
  sourceLabel,
  statusClass,
  type ContestDetail,
} from '../api/contest'
import PageHeader from '../components/PageHeader'

export default function ContestDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [contest, setContest] = useState<ContestDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    const load = async () => {
      setLoading(true)
      setError('')
      try {
        const data = await fetchContestDetail(Number(id))
        setContest(data)
      } catch (err) {
        setError(err instanceof ApiError ? err.message : '加载失败')
      } finally {
        setLoading(false)
      }
    }
    void load()
  }, [id])

  if (loading) {
    return (
      <div className="page-content">
        <div className="loading-panel">
          <div className="boot-spinner" />
          <p>加载赛事详情…</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="page-content">
        <div className="alert alert-error">{error}</div>
        <Link to="/contests" className="contest-detail-back">
          返回赛事列表
        </Link>
      </div>
    )
  }

  if (!contest) {
    return (
      <div className="page-content">
        <p>赛事不存在</p>
        <Link to="/contests" className="contest-detail-back">
          返回赛事列表
        </Link>
      </div>
    )
  }

  return (
    <div className="page-content page-content-wide">
      <PageHeader title="赛事详情" description={sourceLabel(contest.source)} />

      <header className="contest-detail-hero">
        <ContestPlatformLogo source={contest.source} size={56} variant="badge" title={sourceLabel(contest.source)} />
        <div>
          <h1>{contest.title}</h1>
          <p className="contest-detail-sub">
            {sourceLabel(contest.source)} · #{contest.externalId}
          </p>
          <span className={`status-pill ${statusClass(contest.status)}`} style={{ marginTop: 10, display: 'inline-block' }}>
            {formatContestStatus(contest.status)}
          </span>
        </div>
      </header>

      <div className="contest-detail-grid">
        <div className="contest-detail-item">
          <span>开始时间</span>
          <strong>{formatDateTime(contest.startTime)}</strong>
        </div>
        {contest.endTime && (
          <div className="contest-detail-item">
            <span>结束时间</span>
            <strong>{formatDateTime(contest.endTime)}</strong>
          </div>
        )}
        {contest.location && (
          <div className="contest-detail-item">
            <span>地点</span>
            <strong>{contest.location}</strong>
          </div>
        )}
        {contest.difficulty && (
          <div className="contest-detail-item">
            <span>难度</span>
            <strong>{contest.difficulty}</strong>
          </div>
        )}
        {contest.contestType && (
          <div className="contest-detail-item">
            <span>类型</span>
            <strong>{contest.contestType}</strong>
          </div>
        )}
        {contest.lastCrawledAt && (
          <div className="contest-detail-item">
            <span>最近同步</span>
            <strong>{formatDateTime(contest.lastCrawledAt)}</strong>
          </div>
        )}
      </div>

      {contest.description && (
        <div className="contest-detail-desc">{contest.description}</div>
      )}

      <footer className="contest-detail-footer">
        {contest.url && (
          <a href={contest.url} target="_blank" rel="noreferrer" className="contest-detail-link">
            前往官方页面
          </a>
        )}
        <Link to="/contests" className="contest-detail-back">
          ← 返回赛事列表
        </Link>
      </footer>
    </div>
  )
}
