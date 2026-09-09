import { NavLink } from 'react-router-dom'

const COMMUNITY_TABS = [
  { to: '/social', label: '讨论', emoji: '💬', hint: '刷题日常与竞赛复盘' },
  { to: '/solutions', label: '题解', emoji: '📝', hint: '思路分享与收藏' },
  { to: '/teams', label: '组队', emoji: '🚀', hint: '招募队友一起冲' },
]

export default function CommunitySubNav() {
  return (
    <nav className="comm-subnav" aria-label="社区导航">
      {COMMUNITY_TABS.map((tab) => (
        <NavLink
          key={tab.to}
          to={tab.to}
          className={({ isActive }) => `comm-subnav-item${isActive ? ' active' : ''}`}
        >
          <span className="comm-subnav-emoji">{tab.emoji}</span>
          <span className="comm-subnav-label">{tab.label}</span>
          <span className="comm-subnav-hint">{tab.hint}</span>
        </NavLink>
      ))}
    </nav>
  )
}
