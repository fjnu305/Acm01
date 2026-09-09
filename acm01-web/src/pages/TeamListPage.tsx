import { useCallback, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { searchContent, type SearchHit } from '../api/search'
import { fetchTeamList, teamStatusLabel, type TeamItem } from '../api/team'
import CommunitySearchResults from '../components/community/CommunitySearchResults'
import TeamMemberSlots from '../components/community/TeamMemberSlots'
import TagChips from '../components/community/TagChips'
import { formatDateTime } from '../api/solution'

const PAGE_SIZE = 20

export default function TeamListPage() {
  const [searchParams] = useSearchParams()
  const globalQ = searchParams.get('q') ?? ''

  const [teams, setTeams] = useState<TeamItem[]>([])
  const [searchHits, setSearchHits] = useState<SearchHit[]>([])
  const [searchTotal, setSearchTotal] = useState(0)
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [region, setRegion] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))
  const isSearchMode = Boolean(globalQ.trim())

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      if (isSearchMode) {
        const result = await searchContent({ q: globalQ.trim(), type: 'team' })
        setSearchHits(result.hits)
        setSearchTotal(result.total)
        setTeams([])
        setTotal(0)
      } else {
        const data = await fetchTeamList({
          status: 1,
          region: region || undefined,
          pageNum,
          pageSize: PAGE_SIZE,
        })
        setTeams(data.list)
        setTotal(data.total)
        setSearchHits([])
        setSearchTotal(0)
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '加载失败')
      setTeams([])
      setSearchHits([])
      setTotal(0)
      setSearchTotal(0)
    } finally {
      setLoading(false)
    }
  }, [globalQ, isSearchMode, region, pageNum])

  useEffect(() => {
    void loadData()
  }, [loadData])

  return (
    <div className="comm-body comm-board-teams">
      <div className="comm-layout">
        <div className="comm-main">
          <div className="comm-section-head">
            <div>
              <h2>组队招募</h2>
              <p>按地区筛选，或发布新帖寻找队友</p>
            </div>
          </div>
          {!isSearchMode && (
            <div className="comm-filter-row">
              <input
                type="text"
                className="comm-region-input"
                placeholder="按地区 / 学校筛选"
                value={region}
                onChange={(e) => {
                  setRegion(e.target.value)
                  setPageNum(1)
                }}
              />
            </div>
          )}

          {error && <div className="alert alert-error">{error}</div>}

          {isSearchMode ? (
            <CommunitySearchResults
              hits={searchHits}
              total={searchTotal}
              loading={loading}
              query={globalQ}
              scope="team"
            />
          ) : loading ? (
            <div className="comm-loading">
              <div className="boot-spinner" />
              <span>加载组队帖…</span>
            </div>
          ) : teams.length === 0 ? (
            <div className="comm-empty">
              <h3>暂无招募</h3>
              <p>
                <Link to="/teams/new">发布招募帖</Link>
              </p>
            </div>
          ) : (
            <div className="team-grid">
              {teams.map((team) => (
                  <Link key={team.id} to={`/teams/${team.id}`} className="team-card">
                    <div className="team-card-head">
                      <h3>{team.title}</h3>
                      <span className={`team-status-badge status-${team.status}`}>{teamStatusLabel(team.status)}</span>
                    </div>
                    <div className="team-rating-badge">
                      Rating {team.ratingMin} – {team.ratingMax}
                      {team.region && ` · ${team.region}`}
                    </div>
                    {team.tags && <TagChips tags={team.tags} />}
                    <TeamMemberSlots
                      memberLimit={team.memberLimit}
                      members={team.memberPreview ?? []}
                    />
                    <div className="team-card-footer">
                      <span>{team.authorName}</span>
                      <span>{formatDateTime(team.createdTime)}</span>
                    </div>
                  </Link>
              ))}
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
          <div className="side-widget side-widget-accent side-widget-teams">
            <h4>智能匹配</h4>
            <p className="side-tip">队长可在详情页查看推荐队友，按 Rating、地区、标签综合打分。</p>
          </div>
        </aside>
      </div>
    </div>
  )
}
