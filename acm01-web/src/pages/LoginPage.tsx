import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError, isAdmin, login } from '../api/auth'
import { useAuth } from '../context/AuthContext'
import AcmBalloonLogo from '../components/AcmBalloonLogo'

export default function LoginPage() {
  const { setUser } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!username.trim()) {
      setError('请输入用户名')
      return
    }
    if (!password.trim()) {
      setError('请输入密码')
      return
    }

    setLoading(true)
    try {
      const auth = await login(username.trim(), password, remember)
      setUser(auth.user)
      navigate(isAdmin(auth.user) ? '/admin' : '/', { replace: true })
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '登录失败，请稍后重试'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <AcmBalloonLogo size={56} />
          <h1>ACMer</h1>
          <p>算法竞赛聚合平台</p>
        </div>

        <h2>登录</h2>
        <p className="auth-subtitle">登录账号，订阅赛事提醒</p>

        <form className="login-form" onSubmit={handleSubmit}>
          {error && <div className="login-error">{error}</div>}

          <label className="form-item">
            <span>用户名</span>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="请输入用户名"
              autoComplete="username"
              disabled={loading}
            />
          </label>

          <label className="form-item">
            <span>密码</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入密码"
              autoComplete="current-password"
              disabled={loading}
            />
          </label>

          <div className="form-row">
            <label className="checkbox">
              <input
                type="checkbox"
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
                disabled={loading}
              />
              <span>记住我</span>
            </label>
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? '登录中…' : '登 录'}
          </button>
        </form>

        <p className="login-footer">
          还没有账号？
          <Link to="/register" className="text-link-btn">
            立即注册
          </Link>
        </p>
      </div>
    </div>
  )
}
