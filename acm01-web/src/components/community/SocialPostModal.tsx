import { useEffect, useState } from 'react'
import { ApiError } from '../../api/auth'
import { createPost, type Post } from '../../api/social'
import UserAvatar from '../UserAvatar'
import { useAuth } from '../../context/AuthContext'

type Props = {
  open: boolean
  onClose: () => void
  onPosted: (post: Post) => void
}

export default function SocialPostModal({ open, onClose, onPosted }: Props) {
  const { user } = useAuth()
  const [content, setContent] = useState('')
  const [posting, setPosting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (open) {
      setContent('')
      setError('')
    }
  }, [open])

  if (!open) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const text = content.trim()
    if (!text) return

    setPosting(true)
    setError('')
    try {
      const created = await createPost(text)
      onPosted(created)
      onClose()
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '发布失败'
      setError(msg)
    } finally {
      setPosting(false)
    }
  }

  return (
    <div className="modal-overlay social-compose-overlay" onClick={onClose}>
      <div
        className="modal-card social-compose-modal"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="social-compose-title"
      >
        <header className="social-compose-header">
          {user && <UserAvatar user={user} className="social-post-avatar" />}
          <div>
            <h2 id="social-compose-title">发布动态</h2>
            <p className="social-compose-sub">分享刷题心得、竞赛复盘、学习记录</p>
          </div>
          <button type="button" className="social-compose-close" onClick={onClose} aria-label="关闭">
            ×
          </button>
        </header>

        <form onSubmit={handleSubmit}>
          <textarea
            className="social-compose-input"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="写下你的想法…"
            rows={8}
            maxLength={5000}
            disabled={posting}
            autoFocus
          />

          {error && <div className="login-error">{error}</div>}

          <div className="modal-actions">
            <button type="button" className="btn-outline" onClick={onClose} disabled={posting}>
              取消
            </button>
            <button type="submit" className="btn-primary" disabled={posting || !content.trim()}>
              {posting ? '发布中…' : '发布'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
