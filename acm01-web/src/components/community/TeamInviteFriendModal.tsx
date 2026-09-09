import { useEffect, useState } from 'react'
import { ApiError } from '../../api/auth'
import { fetchInvitableFriends, inviteTeamMember, type InvitableFriend } from '../../api/team'
import ClickableUserAvatar from '../ClickableUserAvatar'

interface TeamInviteFriendModalProps {
  open: boolean
  teamId: number
  onClose: () => void
  onInvited: () => void
}

export default function TeamInviteFriendModal({
  open,
  teamId,
  onClose,
  onInvited,
}: TeamInviteFriendModalProps) {
  const [friends, setFriends] = useState<InvitableFriend[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [invitingId, setInvitingId] = useState<number | null>(null)

  useEffect(() => {
    if (!open) return
    setLoading(true)
    setError('')
    void fetchInvitableFriends(teamId)
      .then(setFriends)
      .catch((err: unknown) => {
        setFriends([])
        setError(err instanceof ApiError ? err.message : '加载好友失败')
      })
      .finally(() => setLoading(false))
  }, [open, teamId])

  if (!open) return null

  const handleInvite = async (userId: number) => {
    setInvitingId(userId)
    setError('')
    try {
      await inviteTeamMember(teamId, userId)
      setFriends((prev) => prev.filter((f) => f.userId !== userId))
      onInvited()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '邀请失败')
    } finally {
      setInvitingId(null)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="team-invite-modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="team-invite-modal-head">
          <h3>邀请好友入队</h3>
          <button type="button" className="team-invite-modal-close" onClick={onClose} aria-label="关闭">
            ×
          </button>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        {loading ? (
          <div className="comm-loading">
            <div className="boot-spinner" />
            <span>加载好友…</span>
          </div>
        ) : friends.length === 0 ? (
          <p className="team-invite-empty">没有可邀请的好友，或好友已在队伍中</p>
        ) : (
          <ul className="team-invite-list">
            {friends.map((friend) => (
              <li key={friend.userId} className="team-invite-row">
                <ClickableUserAvatar
                  user={{
                    userId: friend.userId,
                    username: friend.username,
                    nickname: friend.nickname ?? friend.username,
                    avatar: friend.avatar ?? '',
                    roles: [],
                  }}
                  className="member-avatar"
                />
                <div className="team-invite-meta">
                  <strong>{friend.nickname || friend.username}</strong>
                  <span>
                    Rating {friend.cfRating ?? 0}
                    {friend.school ? ` · ${friend.school}` : ''}
                  </span>
                </div>
                <button
                  type="button"
                  className="btn-invite btn-sm"
                  disabled={invitingId === friend.userId}
                  onClick={() => void handleInvite(friend.userId)}
                >
                  {invitingId === friend.userId ? '邀请中…' : '邀请'}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
