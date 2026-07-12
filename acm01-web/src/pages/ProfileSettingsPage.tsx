import { useEffect, useRef, useState } from 'react'
import { ApiError, updateProfile, uploadAvatar } from '../api/auth'
import { useAuth } from '../context/AuthContext'
import PageHeader from '../components/PageHeader'
import UserAvatar from '../components/UserAvatar'

export default function ProfileSettingsPage() {
  const { user, setUser } = useAuth()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [nickname, setNickname] = useState('')
  const [email, setEmail] = useState('')
  const [school, setSchool] = useState('')
  const [bio, setBio] = useState('')
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [uploadingAvatar, setUploadingAvatar] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!user) return
    setNickname(user.nickname || '')
    setEmail(user.email || '')
    setSchool(user.school || '')
    setBio(user.bio || '')
    setAvatarPreview(null)
  }, [user])

  useEffect(() => {
    return () => {
      if (avatarPreview) {
        URL.revokeObjectURL(avatarPreview)
      }
    }
  }, [avatarPreview])

  if (!user) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError('邮箱格式不正确')
      return
    }

    setLoading(true)
    try {
      const updated = await updateProfile({
        nickname: nickname.trim() || undefined,
        email: email.trim() || undefined,
        school: school.trim() || undefined,
        bio: bio.trim() || undefined,
      })
      setUser(updated)
      setSuccess('个人资料已保存')
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '保存失败，请稍后重试'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleAvatarPick = () => {
    if (!uploadingAvatar && !loading) {
      fileInputRef.current?.click()
    }
  }

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    setError('')
    setSuccess('')

    const previewUrl = URL.createObjectURL(file)
    if (avatarPreview) {
      URL.revokeObjectURL(avatarPreview)
    }
    setAvatarPreview(previewUrl)

    setUploadingAvatar(true)
    try {
      const updated = await uploadAvatar(file)
      setUser(updated)
      setAvatarPreview(null)
      setSuccess('头像已更新')
    } catch (err) {
      setAvatarPreview(null)
      const msg = err instanceof ApiError ? err.message : '头像上传失败，请稍后重试'
      setError(msg)
    } finally {
      setUploadingAvatar(false)
    }
  }

  const previewUser = {
    ...user,
    nickname: nickname || user.username,
    avatar: avatarPreview || user.avatar,
  }

  return (
    <div className="page-content">
      <PageHeader
        title="个人资料"
        description="管理昵称、邮箱等基本信息。邮箱用于接收赛前邮件提醒。"
      />

      <div className="profile-layout">
        <aside className="profile-preview-card">
          <button
            type="button"
            className="profile-avatar-upload"
            onClick={handleAvatarPick}
            disabled={uploadingAvatar || loading}
            title="点击上传头像"
          >
            <UserAvatar user={previewUser} className="profile-avatar-lg" />
            <span className="profile-avatar-overlay">
              {uploadingAvatar ? '上传中…' : '更换头像'}
            </span>
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            className="profile-avatar-input"
            onChange={handleAvatarChange}
          />
          <p className="profile-avatar-hint">支持 JPG / PNG / WEBP / GIF，最大 2MB</p>

          <h2>{nickname.trim() || user.username}</h2>
          <p className="profile-username">@{user.username}</p>
          {school.trim() && <p className="profile-school">{school.trim()}</p>}
          {bio.trim() ? (
            <p className="profile-bio">{bio.trim()}</p>
          ) : (
            <p className="profile-bio muted">写一句简介，让其他选手认识你</p>
          )}
          <div className="profile-stats">
            <div>
              <span>CF Rating</span>
              <strong>{user.cfRating ?? 0}</strong>
            </div>
            <div>
              <span>刷题数</span>
              <strong>{user.solvedCount ?? 0}</strong>
            </div>
            <div>
              <span>参赛场次</span>
              <strong>{user.contestCount ?? 0}</strong>
            </div>
          </div>
        </aside>

        <section className="profile-form-card">
          <form className="profile-form" onSubmit={handleSubmit}>
            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <label className="form-item">
              <span>用户名</span>
              <input type="text" value={user.username} disabled />
            </label>

            <label className="form-item">
              <span>昵称</span>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="展示给其他用户的名称"
                maxLength={50}
                disabled={loading || uploadingAvatar}
              />
            </label>

            <label className="form-item">
              <span>邮箱</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="用于接收赛前提醒邮件"
                maxLength={100}
                disabled={loading || uploadingAvatar}
              />
            </label>

            <label className="form-item">
              <span>学校 / 单位</span>
              <input
                type="text"
                value={school}
                onChange={(e) => setSchool(e.target.value)}
                placeholder="如：福州大学"
                maxLength={100}
                disabled={loading || uploadingAvatar}
              />
            </label>

            <label className="form-item">
              <span>个人简介</span>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="介绍一下你的竞赛经历或擅长方向"
                maxLength={500}
                rows={4}
                disabled={loading || uploadingAvatar}
              />
            </label>

            <div className="profile-form-actions">
              <button
                type="submit"
                className="btn-primary profile-save-btn"
                disabled={loading || uploadingAvatar}
              >
                {loading ? '保存中…' : '保存修改'}
              </button>
            </div>
          </form>
        </section>
      </div>
    </div>
  )
}
