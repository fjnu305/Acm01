import { Link } from 'react-router-dom'
import type { UserInfo } from '../api/auth'
import UserAvatar from './UserAvatar'

interface ClickableUserAvatarProps {
  user: Pick<UserInfo, 'userId' | 'username' | 'nickname' | 'avatar' | 'roles'>
  className?: string
  stopPropagation?: boolean
}

export default function ClickableUserAvatar({
  user,
  className = '',
  stopPropagation = false,
}: ClickableUserAvatarProps) {
  const content = (
    <UserAvatar
      user={{
        userId: user.userId,
        username: user.username,
        nickname: user.nickname ?? user.username,
        avatar: user.avatar ?? '',
        roles: user.roles ?? [],
      }}
      className={className}
    />
  )

  return (
    <Link
      to={`/users/${user.userId}`}
      className={`clickable-avatar-link${className ? ` ${className}-link` : ''}`}
      title="查看资料"
      onClick={stopPropagation ? (e) => e.stopPropagation() : undefined}
    >
      {content}
    </Link>
  )
}
