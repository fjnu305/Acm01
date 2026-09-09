import { useCallback, useState } from 'react'
import { ApiError } from '../api/auth'
import {
  acceptTeamInvite,
  applyToTeam,
  approveTeamApplication,
  closeTeamRecruitment,
  declineTeamInvite,
  dissolveTeam,
  inviteTeamMember,
  rejectTeamApplication,
} from '../api/team'

type ActionFn = () => Promise<void>

export function useTeamActions(teamId: number, reload: () => Promise<unknown>) {
  const [actionMsg, setActionMsg] = useState('')
  const [busy, setBusy] = useState(false)

  const runAction = useCallback(
    async (action: ActionFn, successMsg: string, after?: () => void) => {
      setActionMsg('')
      setBusy(true)
      try {
        await action()
        setActionMsg(successMsg)
        await reload()
        after?.()
      } catch (err) {
        setActionMsg(err instanceof ApiError ? err.message : '操作失败')
      } finally {
        setBusy(false)
      }
    },
    [reload],
  )

  return {
    actionMsg,
    setActionMsg,
    busy,
    invite: (userId: number) => void runAction(() => inviteTeamMember(teamId, userId), '邀请已发送'),
    acceptInvite: (memberId: number) =>
      void runAction(() => acceptTeamInvite(memberId), '已加入队伍'),
    declineInvite: (memberId: number) =>
      void runAction(() => declineTeamInvite(memberId), '已拒绝邀请'),
    apply: () => void runAction(() => applyToTeam(teamId), '申请已提交，等待队长审核'),
    approve: (memberId: number) =>
      void runAction(() => approveTeamApplication(teamId, memberId), '已同意加入'),
    reject: (memberId: number) =>
      void runAction(() => rejectTeamApplication(teamId, memberId), '已拒绝申请'),
    close: () => {
      if (!window.confirm('确定结束招募？结束后将不再接受新成员。')) return
      void runAction(() => closeTeamRecruitment(teamId), '已结束招募')
    },
    dissolve: (after?: () => void) => {
      if (!window.confirm('确定解散队伍？解散后该组队帖将不再显示。')) return
      void runAction(() => dissolveTeam(teamId), '队伍已解散', after)
    },
  }
}
