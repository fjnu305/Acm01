import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAiChat } from '../../context/AiChatContext'
import { fetchWikiPage, type WikiPage, WikiApiError } from '../../api/wiki'

export default function WikiNotePage() {
  const [params] = useSearchParams()
  const path = params.get('path') ?? ''
  const [page, setPage] = useState<WikiPage | null>(null)
  const [error, setError] = useState<string | null>(null)
  const { openChat } = useAiChat()

  useEffect(() => {
    if (!path) {
      setError('缺少 path 参数')
      return
    }
    setError(null)
    fetchWikiPage(path)
      .then(setPage)
      .catch((e: unknown) => {
        setError(e instanceof WikiApiError ? e.message : '加载失败')
        setPage(null)
      })
  }, [path])

  return (
    <div className="wiki-note">
      <div className="wiki-note-nav">
        <Link to="/wiki">← 关联图</Link>
        <button type="button" className="wiki-note-ai-btn" onClick={openChat}>
          AI 对话
        </button>
      </div>
      {error && <div className="alert alert-error wiki-banner">{error}</div>}
      {page && (
        <article className="wiki-note-card">
          <header>
            <h2>{page.title}</h2>
            <code>{page.path}</code>
          </header>
          <pre className="wiki-note-body">{page.content}</pre>
        </article>
      )}
    </div>
  )
}
