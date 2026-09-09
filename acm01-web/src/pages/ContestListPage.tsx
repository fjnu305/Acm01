import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/auth'
import ContestPlatformLogo from '../components/contest/ContestPlatformLogo'
import {
  CONTEST_SOURCES,
  type ContestItem,
  type ContestStatusFilter,
  fetchContestList,
  formatContestStatus,
  formatDateTime,
  getSourceOption,
  sourceLabel,
  statusClass,
} from '../api/contest'
import PageHeader from '../components/PageHeader'
import {
  REMIND_OPTIONS,
  subscribeContest,
} from '../api/subscription'

interface ContestListPageProps {
  mode?: 'user' | 'admin'
  initialSource?: string
}

const STATUS_TABS: { label: string; value: ContestStatusFilter }[] = [
  { label: '全部', value: '' },
  { label: '即将开始', value: '1' },
  { label: '进行中', value: '2' },
  { label: '已结束', value: '3' },
]

const PAGE_SIZE = 20

export default function ContestListPage({ mode = 'user', initialSource = '' }: ContestListPageProps) {
  const isAdmin = mode === 'admin'
  const [contests, setContests] = useState<ContestItem[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [sourceFilter, setSourceFilter] = useState(initialSource)
  const [statusFilter, setStatusFilter] = useState<ContestStatusFilter>(isAdmin ? '' : '1')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [subscribeTarget, setSubscribeTarget] = useState<ContestItem | null>(null)
  const [selectedReminds, setSelectedReminds] = useState<number[]>([1440, 60])
  const [subscribing, setSubscribing] = useState(false)
  const [subscribeMsg, setSubscribeMsg] = useState('')

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))
  const activeSource = getSourceOption(sourceFilter)

  const loadContests = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await fetchContestList({
        source: sourceFilter || undefined,
        status: statusFilter,
        pageNum,
        pageSize: PAGE_SIZE,
      })
      setContests(data.list)
      setTotal(data.total)
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '加载赛事列表失败'
      setError(msg)
      setContests([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }, [pageNum, sourceFilter, statusFilter])

  useEffect(() => {
    void loadContests()
  }, [loadContests])

  const handleSourceChange = (code: string) => {
    setSourceFilter(code)
    setPageNum(1)
  }

  const handleStatusChange = (value: ContestStatusFilter) => {
    setStatusFilter(value)
    setPageNum(1)
  }

  const openSubscribe = (contest: ContestItem) => {
    setSubscribeTarget(contest)
    setSelectedReminds([1440, 60])
    setSubscribeMsg('')
  }

  const closeSubscribe = () => {
    if (subscribing) return
    setSubscribeTarget(null)
    setSubscribeMsg('')
  }

  const toggleRemind = (minutes: number) => {
    setSelectedReminds((prev) =>
      prev.includes(minutes) ? prev.filter((m) => m !== minutes) : [...prev, minutes],
    )
  }

  const handleSubscribe = async () => {
    if (!subscribeTarget || selectedReminds.length === 0) return
    setSubscribing(true)
    setSubscribeMsg('')
    try {
      const created = await subscribeContest({
        contestId: subscribeTarget.id,
        remindBeforeMinutes: selectedReminds,
      })
      setSubscribeMsg(`已订阅 ${created.length} 个提醒档位`)
      setTimeout(() => closeSubscribe(), 1200)
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '订阅失败'
      setSubscribeMsg(msg)
    } finally {
      setSubscribing(false)
    }
  }

  return (
    <div className="page-content page-content-wide">
      <PageHeader
        title="赛事列表"
        description={
          isAdmin
            ? '全平台赛事数据，支持按来源与状态筛选'
            : '发现即将开始的比赛，订阅赛前邮件提醒'
        }
        actions={
          <ContestPlatformLogo
            source={activeSource?.code ?? 'all'}
            size={36}
            variant="badge"
            title={activeSource?.label ?? '全部平台'}
          />
        }
      />

      <nav className="contest-source-tabs" aria-label="赛事平台">
        <button
          type="button"
          className={`contest-source-tab ${sourceFilter === '' ? 'active' : ''}`}
          onClick={() => handleSourceChange('')}
        >
          <ContestPlatformLogo source="all" size={22} />
          全部平台
        </button>
        {CONTEST_SOURCES.map((source) => (
          <button
            key={source.code}
            type="button"
            className={`contest-source-tab ${sourceFilter === source.code ? 'active' : ''}`}
            onClick={() => handleSourceChange(source.code)}
          >
            <ContestPlatformLogo source={source.code} size={22} title={source.label} />
            {source.label}
          </button>
        ))}
      </nav>

      <section className="filter-bar filter-bar-split">
        <div className="status-tabs">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value || 'all'}
              type="button"
              className={`status-tab ${statusFilter === tab.value ? 'active' : ''}`}
              onClick={() => handleStatusChange(tab.value)}
            >
              {tab.label}
            </button>
          ))}
        </div>
        <div className="contest-meta">
          共 <strong>{total}</strong> 场
        </div>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-panel">
          <div className="boot-spinner" />
          <p>加载赛事列表…</p>
        </div>
      ) : contests.length === 0 ? (
        <div className="empty-panel">
          <h2>暂无赛事</h2>
          <p>
            {sourceFilter
              ? `${sourceLabel(sourceFilter)} 暂无数据，请稍后在管理端同步或切换其他平台。`
              : isAdmin
                ? '请先在爬虫管理页同步各平台赛事数据。'
                : '当前筛选条件下没有比赛，试试切换平台或状态。'}
          </p>
        </div>
      ) : (
        <div className="contest-list-panel">
          {contests.map((contest) => (
            <article key={contest.id} className="contest-list-card">
              <ContestPlatformLogo
                source={contest.source}
                size={40}
                variant="badge"
                title={sourceLabel(contest.source)}
              />
              <div className="contest-list-body">
                <h3 className="contest-list-title">
                  <span className="contest-id">#{contest.externalId}</span>
                  <Link to={`/contests/${contest.id}`}>{contest.title}</Link>
                </h3>
                <div className="contest-list-meta">
                  <span>{formatDateTime(contest.startTime)}</span>
                  {contest.endTime && <span>至 {formatDateTime(contest.endTime)}</span>}
                  {contest.difficulty && <span>{contest.difficulty}</span>}
                  {contest.contestType && <span>{contest.contestType}</span>}
                  <span className={`status-pill ${statusClass(contest.status)}`}>
                    {formatContestStatus(contest.status)}
                  </span>
                </div>
              </div>
              <div className="contest-list-actions">
                {contest.url && (
                  <a href={contest.url} target="_blank" rel="noreferrer" className="btn-outline btn-sm">
                    官网
                  </a>
                )}
                {!isAdmin && contest.status === 1 && (
                  <button
                    type="button"
                    className="btn-primary btn-sm"
                    onClick={() => openSubscribe(contest)}
                  >
                    订阅
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}

      {!loading && totalPages > 1 && (
        <footer className="pagination-bar">
          <button
            type="button"
            className="btn-outline"
            disabled={pageNum <= 1}
            onClick={() => setPageNum((p) => p - 1)}
          >
            上一页
          </button>
          <span>
            第 {pageNum} / {totalPages} 页
          </span>
          <button
            type="button"
            className="btn-outline"
            disabled={pageNum >= totalPages}
            onClick={() => setPageNum((p) => p + 1)}
          >
            下一页
          </button>
        </footer>
      )}

      {subscribeTarget && (
        <div className="modal-overlay" onClick={closeSubscribe}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <ContestPlatformLogo source={subscribeTarget.source} size={36} variant="badge" />
              <div>
                <h2 style={{ margin: 0 }}>订阅提醒</h2>
                <p className="modal-meta" style={{ margin: '4px 0 0' }}>
                  {sourceLabel(subscribeTarget.source)}
                </p>
              </div>
            </div>
            <p className="modal-subtitle">{subscribeTarget.title}</p>
            <p className="modal-meta">
              开始：{formatDateTime(subscribeTarget.startTime)} · 将通过邮件提醒
            </p>

            <div className="modal-section">
              <p className="modal-label">提醒时间</p>
              <div className="checkbox-group">
                {REMIND_OPTIONS.map((opt) => (
                  <label key={opt.minutes} className="checkbox-item">
                    <input
                      type="checkbox"
                      checked={selectedReminds.includes(opt.minutes)}
                      onChange={() => toggleRemind(opt.minutes)}
                    />
                    {opt.label}
                  </label>
                ))}
              </div>
            </div>

            {subscribeMsg && (
              <div className={subscribeMsg.startsWith('已订阅') ? 'modal-success' : 'login-error'}>
                {subscribeMsg}
              </div>
            )}

            <div className="modal-actions">
              <button type="button" className="btn-outline" onClick={closeSubscribe} disabled={subscribing}>
                取消
              </button>
              <button
                type="button"
                className="btn-primary"
                disabled={subscribing || selectedReminds.length === 0}
                onClick={() => void handleSubscribe()}
              >
                {subscribing ? '提交中…' : '确认订阅'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
