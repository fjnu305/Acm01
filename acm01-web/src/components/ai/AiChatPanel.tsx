import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  askWiki,
  fetchWikiCredentials,
  fetchWikiHealth,
  type AskResult,
  type WikiCredentials,
  type WikiHealth,
  WikiApiError,
} from '../../api/wiki'

const MODES = [
  { id: 'quick' as const, label: '快速' },
  { id: 'standard' as const, label: '标准' },
  { id: 'deep' as const, label: '深入' },
]

type Turn = {
  id: string
  question: string
  result: AskResult
}

type Props = {
  compact?: boolean
}

export default function AiChatPanel({ compact = false }: Props) {
  const [health, setHealth] = useState<WikiHealth | null>(null)
  const [creds, setCreds] = useState<WikiCredentials | null>(null)
  const [question, setQuestion] = useState('')
  const [mode, setMode] = useState<'quick' | 'standard' | 'deep'>('standard')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [turns, setTurns] = useState<Turn[]>([])
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    Promise.all([fetchWikiHealth(), fetchWikiCredentials()])
      .then(([h, c]) => {
        setHealth(h)
        setCreds(c)
      })
      .catch((e: unknown) => {
        setError(e instanceof WikiApiError ? e.message : '加载失败')
      })
  }, [])

  useEffect(() => {
    const el = scrollRef.current
    if (el) el.scrollTop = el.scrollHeight
  }, [turns, loading])

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const q = question.trim()
    if (!q || loading) return
    setLoading(true)
    setError(null)
    try {
      const data = await askWiki(q, mode)
      setTurns((prev) => [
        ...prev,
        { id: `${Date.now()}`, question: q, result: data },
      ])
      setQuestion('')
    } catch (err) {
      setError(err instanceof WikiApiError ? err.message : '提问失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`ai-chat-panel${compact ? ' compact' : ''}`}>
      <div className="ai-chat-meta">
        <span className={`wiki-chip${health?.ok ? ' ok' : ''}`}>
          {health?.ok ? `${health.pageCount ?? 0} 篇笔记` : 'Vault 未就绪'}
        </span>
        <span className={`wiki-chip${creds?.configured ? ' ok' : ''}`}>
          {creds?.configured ? creds.model : '未配置模型'}
        </span>
        {!creds?.configured && (
          <Link to="/profile/llm" className="ai-chat-settings-link">
            配置模型
          </Link>
        )}
      </div>

      <div className="ai-chat-thread" ref={scrollRef}>
        {turns.length === 0 && !loading && (
          <div className="ai-chat-empty">
            <p>基于你的学习笔记回答问题</p>
          </div>
        )}

        {turns.map((turn) => (
          <div key={turn.id} className="ai-chat-turn">
            <div className="ai-chat-bubble user">{turn.question}</div>
            <div className="ai-chat-bubble assistant">
              <div className="ai-chat-answer">{turn.result.answer}</div>
              {turn.result.citations.length > 0 && (
                <ul className="ai-chat-cites">
                  {turn.result.citations.map((c) => (
                    <li key={c.path}>
                      <Link to={`/wiki/note?path=${encodeURIComponent(c.path)}`}>
                        [{c.index}] {c.title}
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        ))}

        {loading && (
          <div className="ai-chat-turn">
            <div className="ai-chat-bubble user">{question.trim() || '…'}</div>
            <div className="ai-chat-bubble assistant loading">思考中…</div>
          </div>
        )}
      </div>

      {error && <div className="alert alert-error ai-chat-error">{error}</div>}

      <form className="ai-chat-compose" onSubmit={onSubmit}>
        <textarea
          rows={2}
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="向笔记提问…"
          disabled={loading}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault()
              void onSubmit(e)
            }
          }}
        />
        <div className="ai-chat-compose-bar">
          <div className="ai-chat-modes" role="group" aria-label="问答深度">
            {MODES.map((m) => (
              <button
                key={m.id}
                type="button"
                className={`ai-chat-mode${mode === m.id ? ' active' : ''}`}
                onClick={() => setMode(m.id)}
                disabled={loading}
              >
                {m.label}
              </button>
            ))}
          </div>
          <button type="submit" className="btn-primary ai-chat-send" disabled={loading || !question.trim()}>
            发送
          </button>
        </div>
      </form>
    </div>
  )
}
