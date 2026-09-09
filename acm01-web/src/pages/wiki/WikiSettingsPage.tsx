import { useEffect, useMemo, useState, type FormEvent } from 'react'
import PageHeader from '../../components/PageHeader'
import { useAiChat } from '../../context/AiChatContext'
import {
  deleteWikiCredentials,
  fetchWikiCredentials,
  saveWikiCredentials,
  testWikiCredentials,
  type WikiCredentials,
  WikiApiError,
} from '../../api/wiki'

type ProviderId = 'deepseek' | 'openai' | 'ollama' | 'custom'

type ProviderDef = {
  id: ProviderId
  name: string
  keyUrl: string
  baseUrl: string
  model: string
  color: string
  mark: string
}

const PROVIDERS: ProviderDef[] = [
  {
    id: 'deepseek',
    name: 'DeepSeek',
    keyUrl: 'https://platform.deepseek.com/api_keys',
    baseUrl: 'https://api.deepseek.com/v1',
    model: 'deepseek-chat',
    color: '#4d6bfe',
    mark: 'D',
  },
  {
    id: 'openai',
    name: 'OpenAI',
    keyUrl: 'https://platform.openai.com/api-keys',
    baseUrl: 'https://api.openai.com/v1',
    model: 'gpt-4o-mini',
    color: '#10a37f',
    mark: 'O',
  },
  {
    id: 'ollama',
    name: 'Ollama',
    keyUrl: 'https://ollama.com/download',
    baseUrl: 'http://127.0.0.1:11434/v1',
    model: 'llama3.2',
    color: '#1a1a1a',
    mark: '◆',
  },
  {
    id: 'custom',
    name: '自定义',
    keyUrl: '',
    baseUrl: '',
    model: '',
    color: '#64748b',
    mark: '⋯',
  },
]

function detectProvider(baseUrl: string): ProviderId {
  const u = baseUrl.toLowerCase()
  if (u.includes('deepseek')) return 'deepseek'
  if (u.includes('openai.com')) return 'openai'
  if (u.includes('11434') || u.includes('ollama')) return 'ollama'
  return 'custom'
}

function providerStatus(
  providerId: ProviderId,
  creds: WikiCredentials | null,
): 'configured' | 'idle' {
  if (!creds?.configured) return 'idle'
  return detectProvider(creds.baseUrl) === providerId ? 'configured' : 'idle'
}

export default function WikiSettingsPage() {
  const [creds, setCreds] = useState<WikiCredentials | null>(null)
  const [provider, setProvider] = useState<ProviderId>('deepseek')
  const [baseUrl, setBaseUrl] = useState(PROVIDERS[0].baseUrl)
  const [model, setModel] = useState(PROVIDERS[0].model)
  const [apiKey, setApiKey] = useState('')
  const [showKey, setShowKey] = useState(false)
  const [showAdvanced, setShowAdvanced] = useState(false)
  const [saving, setSaving] = useState(false)
  const [testing, setTesting] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const activeProvider = useMemo(
    () => PROVIDERS.find((p) => p.id === provider) ?? PROVIDERS[0],
    [provider],
  )

  const applyProvider = (id: ProviderId, fromCreds?: WikiCredentials | null) => {
    const preset = PROVIDERS.find((p) => p.id === id)
    if (!preset) return
    setProvider(id)
    if (fromCreds?.configured && detectProvider(fromCreds.baseUrl) === id) {
      setBaseUrl(fromCreds.baseUrl)
      setModel(fromCreds.model)
    } else {
      setBaseUrl(preset.baseUrl)
      setModel(preset.model)
    }
    setShowAdvanced(id === 'custom' || id === 'ollama')
    setApiKey('')
    setError(null)
    setMessage(null)
  }

  const load = () => {
    fetchWikiCredentials()
      .then((c) => {
        setCreds(c)
        if (c.configured) {
          applyProvider(detectProvider(c.baseUrl), c)
        }
      })
      .catch((e: unknown) => {
        setError(e instanceof WikiApiError ? e.message : '加载失败')
      })
  }

  useEffect(() => {
    load()
  }, [])

  const canSave = useMemo(() => {
    if (!baseUrl.trim() || !model.trim()) return false
    if (!creds?.configured && !apiKey.trim() && provider !== 'ollama') return false
    return true
  }, [baseUrl, model, apiKey, creds, provider])

  const onSave = async (e: FormEvent) => {
    e.preventDefault()
    if (!canSave) return
    setSaving(true)
    setError(null)
    setMessage(null)
    try {
      const next = await saveWikiCredentials({
        provider: 'openai_compatible',
        baseUrl: baseUrl.trim(),
        model: model.trim(),
        apiKey: apiKey.trim() || (provider === 'ollama' ? 'ollama' : undefined),
      })
      setCreds(next)
      setApiKey('')
      setMessage('已保存')
    } catch (err) {
      setError(err instanceof WikiApiError ? err.message : '保存失败')
    } finally {
      setSaving(false)
    }
  }

  const onTest = async () => {
    setTesting(true)
    setError(null)
    setMessage(null)
    try {
      const result = await testWikiCredentials({
        baseUrl: baseUrl.trim() || undefined,
        model: model.trim() || undefined,
        apiKey: apiKey.trim() || (provider === 'ollama' ? 'ollama' : undefined),
      })
      setMessage(result.ok ? '连通成功' : '测试未通过')
    } catch (err) {
      setError(err instanceof WikiApiError ? err.message : '测试失败')
    } finally {
      setTesting(false)
    }
  }

  const onDelete = async () => {
    if (!window.confirm('确定清除已保存的 API Key？')) return
    try {
      await deleteWikiCredentials()
      setCreds(null)
      setApiKey('')
      setMessage('已清除本地凭证')
      applyProvider(provider)
    } catch (err) {
      setError(err instanceof WikiApiError ? err.message : '删除失败')
    }
  }

  const isCurrentConfigured = providerStatus(provider, creds) === 'configured'
  const { openChat } = useAiChat()

  return (
    <div className="page-content llm-settings-page">
      <PageHeader
        title="模型 API"
        actions={
          <button type="button" className="text-link-btn" onClick={openChat}>
            去对话 →
          </button>
        }
      />

      <div className="llm-shell">
        <nav className="llm-sidebar" aria-label="模型提供商">
          {PROVIDERS.map((p) => {
            const status = providerStatus(p.id, creds)
            return (
              <button
                key={p.id}
                type="button"
                className={`llm-nav-item${provider === p.id ? ' active' : ''}`}
                onClick={() => applyProvider(p.id, creds)}
              >
                <span className="llm-nav-icon" style={{ background: p.color }}>
                  {p.mark}
                </span>
                <span className="llm-nav-copy">
                  <span className="llm-nav-name">{p.name}</span>
                  <span className={`llm-nav-status${status === 'configured' ? ' ok' : ''}`}>
                    {status === 'configured' ? '已配置' : '未配置'}
                  </span>
                </span>
              </button>
            )
          })}
        </nav>

        <section className="llm-detail">
          <header className="llm-detail-head">
            <div className="llm-detail-brand">
              <span className="llm-detail-icon" style={{ background: activeProvider.color }}>
                {activeProvider.mark}
              </span>
              <h2>{activeProvider.name}</h2>
            </div>
          </header>

          <form className="llm-detail-form" onSubmit={onSave}>
            {error && <div className="alert alert-error">{error}</div>}
            {message && <div className="alert alert-success">{message}</div>}

            <div className="llm-field-block">
              <div className="llm-field-head">
                <label htmlFor="llm-api-key">API Key</label>
                {activeProvider.keyUrl && (
                  <a
                    href={activeProvider.keyUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="llm-field-link"
                  >
                    获取 Key
                    <span aria-hidden>↗</span>
                  </a>
                )}
              </div>
              <div className="llm-key-row">
                <input
                  id="llm-api-key"
                  type={showKey ? 'text' : 'password'}
                  autoComplete="off"
                  spellCheck={false}
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder="sk-..."
                  disabled={saving || testing}
                />
                <button
                  type="button"
                  className="llm-key-toggle"
                  onClick={() => setShowKey((v) => !v)}
                  aria-label={showKey ? '隐藏 Key' : '显示 Key'}
                >
                  {showKey ? '隐藏' : '显示'}
                </button>
              </div>
            </div>

            <div className="llm-field-block">
              <label className="llm-field-head solo" htmlFor="llm-model">
                模型 ID
              </label>
              <input
                id="llm-model"
                type="text"
                value={model}
                onChange={(e) => setModel(e.target.value)}
                placeholder={activeProvider.model || 'model-name'}
                disabled={saving || testing}
              />
            </div>

            {(provider === 'custom' || provider === 'ollama' || showAdvanced) && (
              <div className="llm-field-block">
                <div className="llm-field-head">
                  <label htmlFor="llm-base-url">Base URL</label>
                  {provider !== 'custom' && provider !== 'ollama' && (
                    <button
                      type="button"
                      className="llm-field-link-btn"
                      onClick={() => setShowAdvanced(false)}
                    >
                      收起
                    </button>
                  )}
                </div>
                <input
                  id="llm-base-url"
                  type="url"
                  value={baseUrl}
                  onChange={(e) => {
                    setBaseUrl(e.target.value)
                    setProvider(detectProvider(e.target.value))
                  }}
                  placeholder="https://api.example.com/v1"
                  disabled={saving || testing}
                />
              </div>
            )}

            {provider !== 'custom' && provider !== 'ollama' && !showAdvanced && (
              <button
                type="button"
                className="llm-advanced-toggle"
                onClick={() => setShowAdvanced(true)}
              >
                Base URL
              </button>
            )}

            <div className="llm-detail-foot">
              <button type="submit" className="btn-primary" disabled={saving || !canSave}>
                {saving ? '保存中…' : '保存'}
              </button>
              <button
                type="button"
                className="llm-secondary-btn"
                onClick={onTest}
                disabled={
                  testing ||
                  (!apiKey.trim() && !isCurrentConfigured && provider !== 'ollama')
                }
              >
                {testing ? '测试中…' : '测试连通'}
              </button>
              {isCurrentConfigured && (
                <button
                  type="button"
                  className="llm-danger-link"
                  onClick={onDelete}
                  disabled={saving || testing}
                >
                  清除凭证
                </button>
              )}
            </div>
          </form>
        </section>
      </div>
    </div>
  )
}
