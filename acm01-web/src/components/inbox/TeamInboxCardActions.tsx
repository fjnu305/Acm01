import { Link } from 'react-router-dom'
import {
  INBOX_REF_TEAM_APPLY,
  INBOX_REF_TEAM_APPLY_ACCEPTED,
  INBOX_REF_TEAM_APPLY_REJECTED,
  INBOX_REF_TEAM_INVITE,
  type ChatMessage,
} from '../../api/inbox'

interface Props {
  msg: ChatMessage
  handled: boolean
  onAcceptInvite: (memberId: number) => void
  onDeclineInvite: (memberId: number) => void
  onApproveApply: (memberId: number) => void
  onRejectApply: (memberId: number) => void
}

export default function TeamInboxCardActions({
  msg,
  handled,
  onAcceptInvite,
  onDeclineInvite,
  onApproveApply,
  onRejectApply,
}: Props) {
  if (msg.mine || msg.refId == null || !msg.refType) return null

  if (msg.refType === INBOX_REF_TEAM_INVITE) {
    if (handled) return <p className="wx-chat-card-done">已处理</p>
    return (
      <div className="wx-chat-card-actions">
        <button type="button" onClick={() => onAcceptInvite(msg.refId!)}>同意</button>
        <button type="button" className="muted" onClick={() => onDeclineInvite(msg.refId!)}>拒绝</button>
      </div>
    )
  }

  if (msg.refType === INBOX_REF_TEAM_APPLY) {
    if (handled) return <p className="wx-chat-card-done">已处理</p>
    return (
      <div className="wx-chat-card-actions">
        <button type="button" onClick={() => onApproveApply(msg.refId!)}>同意加入</button>
        <button type="button" className="muted" onClick={() => onRejectApply(msg.refId!)}>拒绝</button>
      </div>
    )
  }

  if (msg.refType === INBOX_REF_TEAM_APPLY_ACCEPTED || msg.refType === INBOX_REF_TEAM_APPLY_REJECTED) {
    return (
      <div className="wx-chat-card-actions">
        <Link to={`/teams/${msg.refId}`} className="wx-chat-card-link">查看队伍</Link>
      </div>
    )
  }

  return null
}

export function isTeamActionCard(msg: ChatMessage): boolean {
  if (msg.mine || !msg.refType || msg.refId == null) return false
  return (
    msg.refType === INBOX_REF_TEAM_INVITE
    || msg.refType === INBOX_REF_TEAM_APPLY
    || msg.refType === INBOX_REF_TEAM_APPLY_ACCEPTED
    || msg.refType === INBOX_REF_TEAM_APPLY_REJECTED
  )
}
