import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import UserAvatar from '../components/UserAvatar'

const NAV_ITEMS = [
  { to: '/', label: '首页', end: true },
  { to: '/contests', label: '赛事' },
  { to: '/subscriptions', label: '我的订阅' },
]

export default function UserLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const displayName = user?.nickname || user?.username || ''

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell user-shell">
      <header className="user-topbar">
        <div className="user-topbar-inner">
          <NavLink to="/" className="user-brand">
            <span className="logo-mark sm">&lt;/&gt;</span>
            <span>ACMer</span>
          </NavLink>

          <nav className="user-nav">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => `user-nav-link${isActive ? ' active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="user-topbar-actions">
            <button
              type="button"
              className="user-chip-btn"
              onClick={() => navigate('/profile')}
              title="个人资料"
            >
              {user && <UserAvatar user={user} />}
              <span className="user-chip-name">{displayName}</span>
            </button>
            <button type="button" className="btn-ghost" onClick={handleLogout}>
              退出
            </button>
          </div>
        </div>
      </header>

      <main className="app-main user-main">
        <Outlet />
      </main>
    </div>
  )
}
