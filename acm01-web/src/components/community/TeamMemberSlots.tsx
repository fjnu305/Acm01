import ClickableUserAvatar from '../ClickableUserAvatar'

export interface TeamSlotMember {
  userId: number
  username: string
  nickname?: string
  avatar?: string
}

interface TeamMemberSlotsProps {
  memberLimit: number
  members: TeamSlotMember[]
  showBar?: boolean
}

export default function TeamMemberSlots({
  memberLimit,
  members,
  showBar = true,
}: TeamMemberSlotsProps) {
  const fillPct = memberLimit > 0 ? Math.round((members.length / memberLimit) * 100) : 0

  return (
    <div className="team-slots">
      {Array.from({ length: memberLimit }).map((_, i) => {
        const member = members[i]
        const filled = Boolean(member)

        return (
          <div key={i} className={`team-slot${filled ? ' filled' : ''}`} title={filled ? member.nickname || member.username : undefined}>
            {filled ? (
              <ClickableUserAvatar
                user={{
                  userId: member.userId,
                  username: member.username,
                  nickname: member.nickname ?? member.username,
                  avatar: member.avatar ?? '',
                  roles: [],
                }}
                className="team-slot-avatar"
                stopPropagation
              />
            ) : (
              <span className="team-slot-vacant" aria-hidden />
            )}
          </div>
        )
      })}
      {showBar && (
        <>
          <div className="team-slot-bar">
            <div className="team-slot-bar-fill" style={{ width: `${fillPct}%` }} />
          </div>
          <span className="team-slot-label">
            {members.length}/{memberLimit} 人
          </span>
        </>
      )}
    </div>
  )
}

function toSlotMember(member: {
  userId: number
  username: string
  nickname?: string
  avatar?: string
}): TeamSlotMember {
  return {
    userId: member.userId,
    username: member.username,
    nickname: member.nickname,
    avatar: member.avatar,
  }
}

export function acceptedMembersToSlots<
  T extends { userId: number; username: string; nickname?: string; avatar?: string; status: number },
>(members: T[]): TeamSlotMember[] {
  return members.filter((m) => m.status === 1).map(toSlotMember)
}
