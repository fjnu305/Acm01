import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import {
  acceptFriendRequest,
  cancelFriendRequest,
  fetchIncomingFriendRequests,
  fetchOutgoingFriendRequests,
  removeFriend,
  sendFriendRequest,
} from '../api/friend'
import { followUser, unfollowUser } from '../api/social'
import { fetchPublicProfile, type PublicProfile } from '../api/user'
import ClickableUserAvatar from '../components/ClickableUserAvatar'
import CfRatingChart from '../components/CfRatingChart'
import { useAuth } from '../context/AuthContext'

export default function UserProfilePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const userId = Number(id)
  const [profile, setProfile] = useState<PublicProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionMsg, setActionMsg] = useState('')
  const [requestMessage, setRequestMessage] = useState('')
  const [friendDialogOpen, setFriendDialogOpen] = useState(false)
  const [pendingIncomingId, setPendingIncomingId] = useState<number | null>(null)
  const [pendingOutgoingId, setPendingOutgoingId] = useState<number | null>(null)
  const [followLoading, setFollowLoading] = useState(false)

  const loadProfile = async () => {
    const data = await fetchPublicProfile(userId)
    setProfile(data)
    if (data.friendStatus === 'PENDING_RECEIVED') {
      const incoming = await fetchIncomingFriendRequests()
      setPendingIncomingId(incoming.find((item) => item.requesterId === userId)?.id ?? null)
    } else {
      setPendingIncomingId(null)
    }
    if (data.friendStatus === 'PENDING_SENT') {
      const outgoing = await fetchOutgoingFriendRequests()
      setPendingOutgoingId(outgoing.find((item) => item.addresseeId === userId)?.id ?? null)
    } else {
      setPendingOutgoingId(null)
    }
  }

  useEffect(() => {
    if (!Number.isFinite(userId)) {
      setError('无效的用户 ID')
      setLoading(false)
      return
    }
    void (async () => {
      setLoading(true)
      setError('')
      try {
        await loadProfile()
      } catch (err) {
        setError(err instanceof ApiError ? err.message : '加载资料失败')
      } finally {
        setLoading(false)
      }
    })()
  }, [userId])

  const handleSendRequest = async () => {
    setActionMsg('')
    try {
      await sendFriendRequest(userId, requestMessage.trim() || undefined)
      setFriendDialogOpen(false)
      setRequestMessage('')
      await loadProfile()
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '发送失败')
    }
  }

  const handleAccept = async () => {
    if (!pendingIncomingId) return
    setActionMsg('')
    try {
      await acceptFriendRequest(pendingIncomingId)
      await loadProfile()
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleCancelOutgoing = async () => {
    if (!pendingOutgoingId) return
    setActionMsg('')
    try {
      await cancelFriendRequest(pendingOutgoingId)
      await loadProfile()
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleRemoveFriend = async () => {
    setActionMsg('')
    try {
      await removeFriend(userId)
      await loadProfile()
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleToggleFollow = async () => {
    if (!profile) return
    setFollowLoading(true)
    setActionMsg('')
    try {
      if (profile.following) {
        await unfollowUser(userId)
        setProfile({ ...profile, following: false })
      } else {
        await followUser(userId)
        setProfile({ ...profile, following: true })
      }
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '操作失败')
    } finally {
      setFollowLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="comm-loading">
        <div className="boot-spinner" />
        <span>加载资料中…</span>
      </div>
    )
  }

  if (error || !profile) {
    return (
      <div className="comm-body">
        <div className="alert alert-error">{error || '用户不存在'}</div>
      </div>
    )
  }

  const displayName = profile.nickname || profile.username
  const showFriendActions = !profile.isSelf && user && !profile.official

  const renderFriendButton = () => {
    if (!showFriendActions) return null
    switch (profile.friendStatus) {
      case 'NONE':
        return (
          <button type="button" className="profile-toolbar-btn outline" onClick={() => setFriendDialogOpen(true)}>
            加好友
          </button>
        )
      case 'PENDING_SENT':
        return (
          <button type="button" className="profile-toolbar-btn outline" onClick={() => void handleCancelOutgoing()}>
            撤回申请
          </button>
        )
      case 'PENDING_RECEIVED':
        return (
          <button type="button" className="profile-toolbar-btn primary" onClick={() => void handleAccept()}>
            同意好友
          </button>
        )
      case 'FRIENDS':
        return (
          <button type="button" className="profile-toolbar-btn danger" onClick={() => void handleRemoveFriend()}>
            删除好友
          </button>
        )
      default:
        return null
    }
  }

  return (
    <div className="comm-body profile-public-page">
      <nav className="comm-detail-nav">
        <button type="button" className="text-link-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
        <Link to="/inbox">消息中心</Link>
      </nav>

      <div className="profile-public-card">
        <div className="profile-public-header">
          <div className="profile-public-hero">
            <ClickableUserAvatar
              user={{
                userId: profile.userId,
                username: profile.username,
                nickname: profile.nickname ?? profile.username,
                avatar: profile.avatar,
                roles: [],
              }}
              className="profile-avatar-lg"
            />
            <div className="profile-public-meta">
              <div className="profile-public-name-row">
                <h1>{displayName}</h1>
                {profile.official && <span className="profile-official-pill">官方</span>}
              </div>
              <p className="profile-username">@{profile.username}</p>
              {profile.school && <p className="profile-school">{profile.school}</p>}
              <div className="profile-public-stats inline">
                {profile.cfHandle && <span>Codeforces · {profile.cfHandle}</span>}
                {profile.cfRating != null && <span>Rating {profile.cfRating}</span>}
                {profile.solvedCount != null && <span>解题 {profile.solvedCount}</span>}
              </div>
            </div>
          </div>

          {!profile.isSelf && user && (
            <div className="profile-public-toolbar">
              <button
                type="button"
                className={`profile-toolbar-btn${profile.following ? ' following' : ' primary'}`}
                disabled={followLoading}
                onClick={() => void handleToggleFollow()}
              >
                {followLoading ? '…' : profile.following ? '已关注' : '+ 关注'}
              </button>
              {renderFriendButton()}
            </div>
          )}
        </div>

        <div className="profile-public-bio-block">
          {profile.bio ? (
            <p className="profile-bio">{profile.bio}</p>
          ) : (
            <p className="profile-bio muted">这位选手还没有写简介</p>
          )}
        </div>

        {profile.friendStatus === 'FRIENDS' && !profile.isSelf && !profile.official && (
          <div className="profile-public-links">
            <Link to={`/inbox?user=${profile.userId}`} className="profile-link-action">
              发消息
            </Link>
          </div>
        )}

        {profile.isSelf && (
          <p className="side-tip">
            这是你的公开资料页，邮箱等敏感信息不会展示给他人。
            <Link to="/profile"> 编辑资料</Link>
          </p>
        )}

        {(profile.isSelf || profile.cfHandle || (profile.cfRatingHistory?.length ?? 0) > 0) && (
          <CfRatingChart
            handle={profile.cfHandle}
            currentRating={profile.cfRating}
            history={profile.cfRatingHistory ?? []}
          />
        )}

        {actionMsg && <p className="comm-action-msg">{actionMsg}</p>}
      </div>

      {friendDialogOpen && (
        <div className="modal-overlay social-compose-overlay" onClick={() => setFriendDialogOpen(false)}>
          <div
            className="modal-card social-compose-modal profile-friend-modal"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="friend-request-title"
          >
            <div className="social-compose-header">
              <div>
                <h2 id="friend-request-title">添加好友</h2>
                <p className="social-compose-sub">向 {displayName} 发送好友申请</p>
              </div>
              <button type="button" className="social-compose-close" onClick={() => setFriendDialogOpen(false)}>
                ×
              </button>
            </div>
            <div className="profile-friend-modal-body">
              <textarea
                placeholder="附言（可选）"
                value={requestMessage}
                onChange={(e) => setRequestMessage(e.target.value)}
                maxLength={200}
                rows={3}
              />
              <button type="button" className="btn-invite" onClick={() => void handleSendRequest()}>
                发送申请
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
