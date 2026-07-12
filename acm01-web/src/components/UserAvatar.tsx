import type { UserInfo } from '../api/auth'

interface UserAvatarProps {
  user: UserInfo
  className?: string
}

export default function UserAvatar({ user, className = '' }: UserAvatarProps) {
  const displayName = user.nickname || user.username
  const classes = `avatar${className ? ` ${className}` : ''}`

  if (user.avatar?.trim()) {
    return <img src={user.avatar} alt="" className={`avatar-img${className ? ` ${className}` : ''}`} />
  }

  return <span className={classes}>{displayName.charAt(0).toUpperCase()}</span>
}
