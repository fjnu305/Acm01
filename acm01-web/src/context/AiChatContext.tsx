import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

type AiChatContextValue = {
  open: boolean
  openChat: () => void
  closeChat: () => void
  toggleChat: () => void
}

const AiChatContext = createContext<AiChatContextValue | null>(null)

export function AiChatProvider({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false)

  const openChat = useCallback(() => setOpen(true), [])
  const closeChat = useCallback(() => setOpen(false), [])
  const toggleChat = useCallback(() => setOpen((v) => !v), [])

  const value = useMemo(
    () => ({ open, openChat, closeChat, toggleChat }),
    [open, openChat, closeChat, toggleChat],
  )

  return <AiChatContext.Provider value={value}>{children}</AiChatContext.Provider>
}

export function useAiChat() {
  const ctx = useContext(AiChatContext)
  if (!ctx) {
    throw new Error('useAiChat must be used within AiChatProvider')
  }
  return ctx
}
