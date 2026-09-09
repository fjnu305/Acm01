import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import UserAvatar from './UserAvatar'

const MENU_ITEMS = [
  { label: '个性设置', path: '/profile' },
  { label: '模型 API', path: '/profile/llm' },
  { label: '我的小队', path: '/teams/mine' },
  { label: '我的收藏', path: '/solutions/favorites' },
] as const

export default function UserMenuDropdown() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)

  const displayName = user?.nickname || user?.username || ''

  useEffect(() => {
    const onPointerDown = (event: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', onPointerDown)
    return () => document.removeEventListener('mousedown', onPointerDown)
  }, [])

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [])

  const go = (path: string) => {
    setOpen(false)
    navigate(path)
  }

  if (!user) return null

  return (
    <div className="lg-user-menu" ref={rootRef}>
      <button
        type="button"
        className={`lg-user-btn${open ? ' open' : ''}`}
        onClick={() => setOpen((prev) => !prev)}
        aria-expanded={open}
        aria-haspopup="menu"
      >
        <UserAvatar user={user} />
        <span className="lg-user-name">{displayName}</span>
      </button>
      {open && (
        <div className="lg-user-menu-panel" role="menu">
          <div className="lg-user-menu-head">
            <UserAvatar user={user} className="lg-user-menu-avatar" />
            <div>
              <strong>{displayName}</strong>
              <span>@{user.username}</span>
            </div>
          </div>
          {MENU_ITEMS.map((item) => (
            <button
              key={item.path}
              type="button"
              className="lg-user-menu-item"
              role="menuitem"
              onClick={() => go(item.path)}
            >
              {item.label}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
