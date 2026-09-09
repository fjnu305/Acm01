import { Link, NavLink, useLocation, useSearchParams } from 'react-router-dom'
import { useEffect, useState } from 'react'

const COMMUNITY_TABS = [
  { to: '/social', label: '讨论' },
  { to: '/solutions', label: '题解' },
  { to: '/teams', label: '组队' },
]

const PUBLISH_LINK: Record<string, { to: string; label: string } | null> = {
  '/social': null,
  '/solutions': { to: '/solutions/new', label: '发布题解' },
  '/teams': { to: '/teams/new', label: '发布招募' },
}

const COMPOSE_LABEL = '发布动态'

export default function CommunitySubHeader() {
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const query = searchParams.get('q') ?? ''
  const [inputVal, setInputVal] = useState(query)

  useEffect(() => {
    setInputVal(query)
  }, [query])

  const basePath = COMMUNITY_TABS.find((t) => location.pathname.startsWith(t.to))?.to ?? '/social'
  const publish = PUBLISH_LINK[basePath]

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const value = inputVal.trim()
    const next = new URLSearchParams(searchParams)
    if (value) {
      next.set('q', value)
    } else {
      next.delete('q')
    }
    setSearchParams(next, { replace: true })
  }

  const clearSearch = () => {
    setInputVal('')
    const next = new URLSearchParams(searchParams)
    next.delete('q')
    setSearchParams(next, { replace: true })
  }

  const openCompose = () => {
    const next = new URLSearchParams(searchParams)
    next.set('compose', '1')
    setSearchParams(next, { replace: true })
  }

  return (
    <div className="lg-comm-bar">
      <div className="lg-comm-inner">
        <nav className="comm-tab-nav" aria-label="社区分区">
          {COMMUNITY_TABS.map((tab) => (
            <NavLink
              key={tab.to}
              to={{ pathname: tab.to, search: searchParams.toString() }}
              className={({ isActive }) => `comm-tab${isActive ? ' active' : ''}`}
            >
              {tab.label}
            </NavLink>
          ))}
        </nav>

        <div className="comm-shell-toolbar">
          <form className="comm-shell-search" onSubmit={handleSearch}>
            <svg
              className="comm-search-icon"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              aria-hidden="true"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-3.5-3.5" />
            </svg>
            <input
              name="q"
              type="search"
              value={inputVal}
              onChange={(e) => setInputVal(e.target.value)}
              placeholder="搜索…"
              aria-label="搜索题解与组队"
            />
            {query && (
              <button type="button" className="comm-search-clear" onClick={clearSearch}>
                清除
              </button>
            )}
          </form>

          {basePath === '/social' && (
            <button
              type="button"
              className="comm-publish-btn comm-publish-primary"
              onClick={openCompose}
            >
              {COMPOSE_LABEL}
            </button>
          )}

          {publish && (
            <Link to={publish.to} className="comm-publish-btn">
              {publish.label}
            </Link>
          )}
        </div>
      </div>
    </div>
  )
}
