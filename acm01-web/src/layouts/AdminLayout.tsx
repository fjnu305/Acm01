import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import UserAvatar from '../components/UserAvatar'

const NAV_ITEMS = [
  { to: '/admin', label: '控制台', icon: '◉', end: true },
  { to: '/admin/crawl', label: '爬虫管理', icon: '↻' },
  { to: '/admin/contests', label: '赛事数据', icon: '☰' },
  { to: '/admin/logs', label: '爬取日志', icon: '▤', soon: true },
  { to: '/admin/users', label: '用户管理', icon: '◎', soon: true },
]

export default function AdminLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell admin-shell">
      <aside className="app-sidebar admin-sidebar">
        <div className="sidebar-brand">
          <span className="logo-mark sm">&lt;/&gt;</span>
          <div>
            <strong>ACMer</strong>
            <span>管理后台</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `sidebar-link${isActive ? ' active' : ''}${item.soon ? ' soon' : ''}`
              }
            >
              <span className="sidebar-icon">{item.icon}</span>
              <span>{item.label}</span>
              {item.soon && <span className="sidebar-soon">即将上线</span>}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button
            type="button"
            className="sidebar-user-btn"
            onClick={() => navigate('/admin/profile')}
            title="个人资料"
          >
            {user && <UserAvatar user={user} className="admin-avatar" />}
            <div>
              <strong>{user?.nickname || user?.username}</strong>
              <span>管理员 · 个人资料</span>
            </div>
          </button>
          <button type="button" className="btn-ghost btn-block" onClick={handleLogout}>
            退出登录
          </button>
        </div>
      </aside>

      <div className="app-main admin-main">
        <Outlet />
      </div>
    </div>
  )
}
