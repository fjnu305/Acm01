import { useEffect } from 'react'
import { useAiChat } from '../../context/AiChatContext'
import AiChatPanel from './AiChatPanel'

function AiIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M12 3c4.97 0 9 3.58 9 8 0 2.8-1.55 5.27-4 6.74V21l-3.5-1.92C8.9 19.36 3 15.64 3 11c0-4.42 4.03-8 9-8Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
      <circle cx="9" cy="11" r="1" fill="currentColor" />
      <circle cx="12" cy="11" r="1" fill="currentColor" />
      <circle cx="15" cy="11" r="1" fill="currentColor" />
    </svg>
  )
}

export default function AiChatLauncher() {
  const { open, toggleChat, closeChat } = useAiChat()

  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') closeChat()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, closeChat])

  return (
    <>
      <button
        type="button"
        className={`ai-chat-fab${open ? ' open' : ''}`}
        onClick={toggleChat}
        aria-label={open ? '关闭 AI 对话' : '打开 AI 对话'}
        aria-expanded={open}
      >
        {open ? (
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        ) : (
          <AiIcon />
        )}
      </button>

      {open && (
        <>
          <button type="button" className="ai-chat-backdrop" onClick={closeChat} aria-label="关闭 AI 对话" />
          <aside className="ai-chat-drawer" role="dialog" aria-label="AI 对话">
            <header className="ai-chat-drawer-head">
              <h2>AI 助手</h2>
              <button type="button" className="ai-chat-drawer-close" onClick={closeChat} aria-label="关闭">
                ×
              </button>
            </header>
            <AiChatPanel compact />
          </aside>
        </>
      )}
    </>
  )
}
