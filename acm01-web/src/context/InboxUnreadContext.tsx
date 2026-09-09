import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { fetchInboxUnreadCount } from '../api/inbox'
import { useAuth } from './AuthContext'

interface InboxUnreadContextValue {
  unreadCount: number
  refreshUnread: () => Promise<void>
}

const InboxUnreadContext = createContext<InboxUnreadContextValue | null>(null)

export function InboxUnreadProvider({ children }: { children: ReactNode }) {
  const { user, booting } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)

  const refreshUnread = useCallback(async () => {
    if (!user) {
      setUnreadCount(0)
      return
    }
    try {
      const count = await fetchInboxUnreadCount()
      setUnreadCount(count)
    } catch {
      setUnreadCount(0)
    }
  }, [user])

  useEffect(() => {
    if (booting) return
    void refreshUnread()
  }, [booting, refreshUnread])

  const value = useMemo(
    () => ({ unreadCount, refreshUnread }),
    [unreadCount, refreshUnread],
  )

  return <InboxUnreadContext.Provider value={value}>{children}</InboxUnreadContext.Provider>
}

export function useInboxUnread() {
  const ctx = useContext(InboxUnreadContext)
  if (!ctx) {
    throw new Error('useInboxUnread must be used within InboxUnreadProvider')
  }
  return ctx
}
