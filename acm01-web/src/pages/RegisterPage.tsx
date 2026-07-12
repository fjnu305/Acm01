import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError, register } from '../api/auth'
import { useAuth } from '../context/AuthContext'

export default function RegisterPage() {
  const { setUser } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!username.trim() || username.trim().length < 3) {
      setError('用户名至少 3 个字符')
      return
    }
    if (password.length < 6) {
      setError('密码至少 6 位')
      return
    }
    if (password !== confirmPassword) {
      setError('两次输入的密码不一致')
      return
    }
    if (!email.trim()) {
      setError('请填写邮箱，订阅提醒需要收件地址')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError('邮箱格式不正确')
      return
    }

    setLoading(true)
    try {
      const auth = await register({
        username: username.trim(),
        password,
        nickname: nickname.trim() || undefined,
        email: email.trim(),
      })
      setUser(auth.user)
      navigate('/', { replace: true })
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '注册失败，请稍后重试'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <section className="login-brand">
        <div className="brand-inner">
          <div className="logo-mark">&lt;/&gt;</div>
          <h1>加入 ACMer</h1>
          <p className="brand-tagline">注册账号，开启你的竞赛之路</p>
          <ul className="brand-features">
            <li>订阅全网赛事，不再错过比赛</li>
            <li>记录刷题与 Rating 成长轨迹</li>
            <li>结识同好，组队训练</li>
          </ul>
        </div>
        <div className="brand-glow" aria-hidden />
      </section>

      <section className="login-panel">
        <div className="login-card">
          <header className="login-header">
            <h2>创建账号</h2>
            <p>填写真实邮箱以接收赛前邮件提醒</p>
          </header>

          <form className="login-form" onSubmit={handleSubmit}>
            {error && <div className="login-error">{error}</div>}

            <label className="form-item">
              <span>用户名 *</span>
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="3~50 个字符"
                disabled={loading}
              />
            </label>

            <label className="form-item">
              <span>昵称</span>
              <input
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="可选，默认同用户名"
                disabled={loading}
              />
            </label>

            <label className="form-item">
              <span>邮箱 *</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="订阅提醒的收件邮箱"
                disabled={loading}
                required
              />
            </label>

            <label className="form-item">
              <span>密码 *</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="至少 6 位"
                disabled={loading}
              />
            </label>

            <label className="form-item">
              <span>确认密码 *</span>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="再次输入密码"
                disabled={loading}
              />
            </label>

            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? '注册中…' : '注 册'}
            </button>
          </form>

          <p className="login-footer">
            已有账号？
            <Link to="/login" className="text-link-btn">
              去登录
            </Link>
          </p>
        </div>
      </section>
    </div>
  )
}
