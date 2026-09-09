import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { Client, type IMessage } from '@stomp/stompjs'
import { getToken } from '../api/auth'
import { INBOX_PUSH_EVENT, type InboxPushDetail } from '../api/inbox'
import { useAuth } from './AuthContext'
import { useInboxUnread } from './InboxUnreadContext'

export interface PushMessage {
  type: string
  title: string
  body: string
  contestId?: number
  contestTitle?: string
  contestUrl?: string
  inboxId?: number
  inboxCategory?: string
  inboxSenderId?: number
  inboxRefType?: string
}

interface ToastItem {
  id: number
  message: PushMessage
}

interface NotificationContextValue {
  toasts: ToastItem[]
  dismissToast: (id: number) => void
}

const NotificationContext = createContext<NotificationContextValue | null>(null)

const TOAST_DURATION_MS = 8000

export function NotificationProvider({ children }: { children: ReactNode }) {
  const { user, booting } = useAuth()
  const { refreshUnread } = useInboxUnread()
  const [toasts, setToasts] = useState<ToastItem[]>([])
  const clientRef = useRef<Client | null>(null)
  const toastIdRef = useRef(0)

  const dismissToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const showToast = useCallback((message: PushMessage) => {
    const id = ++toastIdRef.current
    setToasts((prev) => [...prev, { id, message }])
    window.setTimeout(() => dismissToast(id), TOAST_DURATION_MS)
  }, [dismissToast])

  useEffect(() => {
    if (booting) {
      return
    }

    const token = getToken()
    if (!user || !token) {
      clientRef.current?.deactivate()
      clientRef.current = null
      return
    }

    let cancelled = false

    import('sockjs-client').then(({ default: SockJS }) => {
      if (cancelled) return

      const client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 5000,
        onConnect: () => {
          client.subscribe('/user/queue/notifications', (frame: IMessage) => {
            try {
              const payload = JSON.parse(frame.body) as PushMessage
              showToast(payload)
              if (payload.type === 'INBOX') {
                void refreshUnread()
                window.dispatchEvent(
                  new CustomEvent<InboxPushDetail>(INBOX_PUSH_EVENT, {
                    detail: {
                      inboxId: payload.inboxId,
                      inboxSenderId: payload.inboxSenderId,
                      title: payload.title,
                      body: payload.body,
                      inboxRefType: payload.inboxRefType,
                    },
                  }),
                )
              }
            } catch {
              showToast({
                type: 'NOTIFY_REMINDER',
                title: '新消息',
                body: frame.body,
              })
            }
          })
        },
        onStompError: (frame) => {
          console.error('STOMP error', frame.headers['message'], frame.body)
        },
      })

      client.activate()
      clientRef.current = client
    })

    return () => {
      cancelled = true
      clientRef.current?.deactivate()
      clientRef.current = null
    }
  }, [user, booting, showToast, refreshUnread])

  const value = useMemo(
    () => ({ toasts, dismissToast }),
    [toasts, dismissToast],
  )

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <div className="toast-container" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className="toast toast-info" role="status">
            <div className="toast-header">
              <strong>{toast.message.title}</strong>
              <button
                type="button"
                className="toast-close"
                aria-label="关闭"
                onClick={() => dismissToast(toast.id)}
              >
                ×
              </button>
            </div>
            <p className="toast-body">{toast.message.body}</p>
            {toast.message.contestUrl && (
              <a
                className="toast-link"
                href={toast.message.contestUrl}
                target="_blank"
                rel="noreferrer"
              >
                查看赛事
              </a>
            )}
          </div>
        ))}
      </div>
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const ctx = useContext(NotificationContext)
  if (!ctx) {
    throw new Error('useNotifications must be used within NotificationProvider')
  }
  return ctx
}
