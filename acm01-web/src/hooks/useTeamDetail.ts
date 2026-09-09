import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiError } from '../api/auth'
import {
  fetchTeamDetail,
  fetchTeamRecommend,
  type TeamDetail,
  type TeamMember,
  type TeamRecommend,
} from '../api/team'
import { useAuth } from '../context/AuthContext'

export interface TeamDetailDerived {
  isLeader: boolean
  isRecruiting: boolean
  isMember: boolean
  isFull: boolean
  myMembership?: TeamMember
  myPendingInvite: TeamMember | null
  myPendingApply: TeamMember | null
  pendingApplications: TeamMember[]
  acceptedMembers: TeamMember[]
}

export function useTeamDetail(teamId: number) {
  const { user } = useAuth()
  const [team, setTeam] = useState<TeamDetail | null>(null)
  const [recommendations, setRecommendations] = useState<TeamRecommend[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    const data = await fetchTeamDetail(teamId)
    setTeam(data)
    if (user && data.userId === user.userId && data.status === 1) {
      const rec = await fetchTeamRecommend(teamId)
      setRecommendations(rec)
    } else {
      setRecommendations([])
    }
    return data
  }, [teamId, user])

  useEffect(() => {
    if (!Number.isFinite(teamId)) {
      setError('无效的队伍 ID')
      setLoading(false)
      return
    }
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError('')
      try {
        await reload()
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : '加载失败')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [teamId, reload])

  const derived: TeamDetailDerived | null = useMemo(() => {
    if (!team) return null
    const myMembership = team.members.find((m) => m.userId === user?.userId)
    const acceptedMembers = team.members.filter((m) => m.status === 1)
    return {
      isLeader: user?.userId === team.userId,
      isRecruiting: team.status === 1,
      isMember: myMembership?.status === 1,
      isFull: team.status === 2 || acceptedMembers.length >= team.memberLimit,
      myMembership,
      myPendingInvite:
        myMembership?.status === 0 && myMembership.pendingKind !== 'APPLY' ? myMembership : null,
      myPendingApply:
        myMembership?.status === 0 && myMembership.pendingKind === 'APPLY' ? myMembership : null,
      pendingApplications: team.members.filter(
        (m) => m.status === 0 && m.pendingKind === 'APPLY',
      ),
      acceptedMembers,
    }
  }, [team, user?.userId])

  return {
    team,
    recommendations,
    loading,
    error,
    reload,
    setError,
    derived,
    user,
  }
}
