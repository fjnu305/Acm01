import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  teamStatusLabel,
} from '../api/team'
import TagChips from '../components/community/TagChips'
import TeamInviteFriendModal from '../components/community/TeamInviteFriendModal'
import TeamMemberSlots, { acceptedMembersToSlots } from '../components/community/TeamMemberSlots'
import ClickableUserAvatar from '../components/ClickableUserAvatar'
import { formatDateTime } from '../api/solution'
import { useTeamActions } from '../hooks/useTeamActions'
import { useTeamDetail } from '../hooks/useTeamDetail'
import { useState } from 'react'

export default function TeamDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const teamId = Number(id)
  const { team, recommendations, loading, error, reload, derived, user } = useTeamDetail(teamId)
  const actions = useTeamActions(teamId, reload)
  const [inviteModalOpen, setInviteModalOpen] = useState(false)

  if (loading) {
    return (
      <div className="comm-loading">
        <div className="boot-spinner" />
        <span>加载中…</span>
      </div>
    )
  }

  if (error && !team) {
    return (
      <div className="comm-body">
        <div className="alert alert-error">{error}</div>
      </div>
    )
  }

  if (!team || !derived) return null

  const {
    isLeader,
    isRecruiting,
    isMember,
    isFull,
    myMembership,
    myPendingInvite,
    myPendingApply,
    pendingApplications,
    acceptedMembers,
  } = derived
  const slotMembers = acceptedMembersToSlots(team.members)

  return (
    <div className="comm-detail-wrap">
      <nav className="comm-detail-nav">
        <Link to="/teams">← 返回组队</Link>
        <Link to="/social">讨论</Link>
        <Link to="/solutions">题解</Link>
      </nav>

      <div className="team-detail-card">
        <div className="team-detail-hero">
          <div className="team-detail-hero-head">
            <h1>{team.title}</h1>
            {isLeader && (
              <div className="team-leader-actions">
                {isRecruiting ? (
                  <button type="button" className="btn-outline btn-sm" onClick={actions.close}>
                    结束招募
                  </button>
                ) : (
                  <button
                    type="button"
                    className="btn-outline btn-sm btn-danger-outline"
                    onClick={() => actions.dissolve(() => navigate('/teams/mine', { replace: true }))}
                  >
                    解散队伍
                  </button>
                )}
              </div>
            )}
          </div>
          <div className="team-detail-badges">
            <span className={`team-status-badge status-${team.status}`}>{teamStatusLabel(team.status)}</span>
            <span className="team-rating-badge">
              Rating {team.ratingMin} – {team.ratingMax}
            </span>
            {team.region && <span className="tag-chip tag-sky">{team.region}</span>}
          </div>
          {team.tags && <TagChips tags={team.tags} />}
          <TeamMemberSlots
            memberLimit={team.memberLimit}
            members={slotMembers}
            showBar
          />
          <p className="team-detail-meta">
            {team.authorName} · {formatDateTime(team.createdTime)}
          </p>

          {isLeader && isRecruiting && slotMembers.length < team.memberLimit && (
            <button type="button" className="btn-invite team-apply-btn" onClick={() => setInviteModalOpen(true)}>
              邀请好友
            </button>
          )}
          {user && !isMember && isRecruiting && !myMembership && (
            <button type="button" className="btn-invite team-apply-btn" onClick={actions.apply}>
              申请加入
            </button>
          )}
          {myPendingApply && (
            <p className="team-pending-hint">已申请加入，等待队长审核</p>
          )}
        </div>

        <div className="team-detail-body">
          {team.description && <p className="team-detail-desc">{team.description}</p>}

          {myPendingInvite && (
            <div className="alert alert-warn team-invite-banner">
              你收到了队长的加入邀请
              <div className="team-member-actions" style={{ marginTop: 10 }}>
                <button type="button" className="btn-invite" onClick={() => actions.acceptInvite(myPendingInvite.id)}>
                  接受邀请
                </button>
                <button type="button" className="btn-outline btn-sm" onClick={() => actions.declineInvite(myPendingInvite.id)}>
                  拒绝
                </button>
              </div>
            </div>
          )}

          <h3 className="comm-section-title">队员</h3>
          <div className="member-grid">
            {acceptedMembers.map((member) => (
              <div key={member.id} className="member-card">
                <ClickableUserAvatar
                  user={{
                    userId: member.userId,
                    username: member.username,
                    nickname: member.nickname ?? member.username,
                    avatar: member.avatar ?? '',
                    roles: [],
                  }}
                  className="member-avatar"
                />
                <div className="member-info">
                  <strong>{member.nickname || member.username}</strong>
                  <span>
                    {member.role === 'LEADER' ? '队长' : '队员'}
                    {' · Rating '}
                    {member.cfRating ?? 0}
                    {member.school ? ` · ${member.school}` : ''}
                  </span>
                </div>
              </div>
            ))}
          </div>

          {isLeader && pendingApplications.length > 0 && !isFull && (
            <div style={{ marginTop: 28 }}>
              <h3 className="comm-section-title">待审核申请</h3>
              {pendingApplications.map((member) => (
                <div key={member.id} className="rec-card">
                  <div className="member-card member-card-inline">
                    <ClickableUserAvatar
                      user={{
                        userId: member.userId,
                        username: member.username,
                        nickname: member.nickname ?? member.username,
                        avatar: member.avatar ?? '',
                        roles: [],
                      }}
                      className="member-avatar"
                    />
                    <div className="member-info">
                      <strong>{member.nickname || member.username}</strong>
                      <span>
                        Rating {member.cfRating ?? 0}
                        {member.school ? ` · ${member.school}` : ''}
                      </span>
                    </div>
                  </div>
                  <div className="team-member-actions">
                    <button type="button" className="btn-invite" onClick={() => actions.approve(member.id)}>
                      同意
                    </button>
                    <button type="button" className="btn-outline btn-sm" onClick={() => actions.reject(member.id)}>
                      拒绝
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {isLeader && recommendations.length > 0 && (
            <div style={{ marginTop: 28 }}>
              <h3 className="comm-section-title">智能推荐</h3>
              {recommendations.map((rec) => (
                <div key={rec.userId} className="rec-card">
                  <div className="member-card member-card-inline">
                    <ClickableUserAvatar
                      user={{
                        userId: rec.userId,
                        username: rec.username,
                        nickname: rec.nickname ?? rec.username,
                        avatar: rec.avatar ?? '',
                        roles: [],
                      }}
                      className="member-avatar"
                    />
                    <div className="member-info">
                      <strong>{rec.nickname || rec.username}</strong>
                      <span>
                        Rating {rec.cfRating ?? 0}
                        {rec.school ? ` · ${rec.school}` : ''}
                      </span>
                    </div>
                  </div>
                  <span className="rec-score">匹配 {(rec.matchScore * 100).toFixed(0)}%</span>
                  <button type="button" className="btn-invite" onClick={() => actions.invite(rec.userId)}>
                    邀请
                  </button>
                </div>
              ))}
            </div>
          )}

          {actions.actionMsg && <p className="comm-action-msg">{actions.actionMsg}</p>}

          <button type="button" className="btn-outline" style={{ marginTop: 24 }} onClick={() => navigate(-1)}>
            返回
          </button>
        </div>
      </div>

      <TeamInviteFriendModal
        open={inviteModalOpen}
        teamId={teamId}
        onClose={() => setInviteModalOpen(false)}
        onInvited={() => {
          actions.setActionMsg('邀请已发送，好友将在消息中心收到通知')
          void reload()
        }}
      />
    </div>
  )
}
