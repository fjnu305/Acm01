import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useInboxUnread } from '../context/InboxUnreadContext'
import { AiChatProvider } from '../context/AiChatContext'
import AcmBalloonLogo from '../components/AcmBalloonLogo'
import AiChatLauncher from '../components/ai/AiChatLauncher'
import CommunitySubHeader from '../components/community/CommunitySubHeader'
import UserMenuDropdown from '../components/UserMenuDropdown'

const NAV_ITEMS = [
  { to: '/', label: '首页', end: true },
  { to: '/contests', label: '比赛' },
  { to: '/social', label: '社区', match: ['/social', '/solutions', '/teams'] },
  { to: '/wiki', label: '学习笔记', match: ['/wiki'] },
  { to: '/subscriptions', label: '我的订阅' },
]

const COMMUNITY_FEED_PATHS = ['/social', '/solutions', '/teams']

function isNavActive(pathname: string, item: { to: string; end?: boolean; match?: string[] }) {
  if (item.match) {
    return item.match.some((p) => pathname === p || pathname.startsWith(`${p}/`))
  }
  if (item.end) return pathname === item.to
  return pathname === item.to || pathname.startsWith(`${item.to}/`)
}

export default function UserLayout() {
  const { logout } = useAuth()
  const { unreadCount } = useInboxUnread()
  const navigate = useNavigate()
  const location = useLocation()
  const showCommunityBar = COMMUNITY_FEED_PATHS.some(
    (p) => location.pathname === p || location.pathname.startsWith(`${p}/`),
  )

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <AiChatProvider>
      <div className="app-shell user-shell">
        <header className="lg-header">
          <div className="lg-header-inner">
            <NavLink to="/" className="lg-logo">
              <AcmBalloonLogo size={34} />
              <span>ACMer</span>
            </NavLink>

            <nav className="lg-nav">
              {NAV_ITEMS.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  className={({ isActive }) =>
                    `lg-nav-link${isNavActive(location.pathname, item) || isActive ? ' active' : ''}`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>

            <div className="lg-header-right">
              <NavLink to="/inbox" className="lg-inbox-btn" title="消息中心">
                <span className="lg-inbox-icon" aria-hidden>
                  ✉
                </span>
                {unreadCount > 0 && (
                  <span className="lg-inbox-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                )}
              </NavLink>
              <UserMenuDropdown />
              <button type="button" className="lg-logout-btn" onClick={handleLogout}>
                退出
              </button>
            </div>
          </div>
          {showCommunityBar && <CommunitySubHeader />}
        </header>

        <main className="app-main lg-main">
          <Outlet />
        </main>
        <AiChatLauncher />
      </div>
    </AiChatProvider>
  )
}
