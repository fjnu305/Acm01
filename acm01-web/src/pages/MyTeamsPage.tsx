import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { formatDateTime } from '../api/solution'
import { fetchMyTeams, teamStatusLabel, type TeamItem } from '../api/team'
import TeamMemberSlots from '../components/community/TeamMemberSlots'
import TagChips from '../components/community/TagChips'
import PageHeader from '../components/PageHeader'

const PAGE_SIZE = 20

export default function MyTeamsPage() {
  const [teams, setTeams] = useState<TeamItem[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await fetchMyTeams({ pageNum, pageSize: PAGE_SIZE })
      setTeams(data.list)
      setTotal(data.total)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '加载失败')
      setTeams([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }, [pageNum])

  useEffect(() => {
    void loadData()
  }, [loadData])

  return (
    <div className="page-content">
      <PageHeader title="我的小队" description="你发起或加入的组队帖" />

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="comm-loading">
          <div className="boot-spinner" />
          <span>加载中…</span>
        </div>
      ) : teams.length === 0 ? (
        <div className="empty-panel">
          <p>还没有参与任何组队</p>
          <Link to="/teams" className="text-link-btn">去组队广场看看</Link>
        </div>
      ) : (
        <>
          <div className="my-teams-list">
            {teams.map((team) => (
              <Link key={team.id} to={`/teams/${team.id}`} className="my-team-card">
                <div className="my-team-card-head">
                  <h3>{team.title}</h3>
                  <span className={`my-team-status status-${team.status}`}>{teamStatusLabel(team.status)}</span>
                </div>
                {team.description && <p className="my-team-desc">{team.description}</p>}
                <div className="my-team-meta">
                  <span>Rating {team.ratingMin}–{team.ratingMax}</span>
                  {team.region && <span>{team.region}</span>}
                  <span>{formatDateTime(team.createdTime)}</span>
                </div>
                {team.tags && <TagChips tags={team.tags} />}
                <TeamMemberSlots memberLimit={team.memberLimit} members={team.memberPreview ?? []} />
              </Link>
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
