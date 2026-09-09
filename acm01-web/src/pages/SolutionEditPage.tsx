import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { CONTEST_SOURCES } from '../api/contest'
import {
  createSolution,
  fetchSolutionTemplates,
  type SolutionCreatePayload,
  type SolutionTemplate,
} from '../api/solution'
import MarkdownEditor from '../components/editor/MarkdownEditor'
import { SOLUTION_OUTLINE } from '../utils/markdownPreview'

const TAG_SUGGESTIONS = ['DP', '图论', '数据结构', '数学', '字符串', '贪心', '二分', 'DFS']

const PROBLEM_SOURCES = CONTEST_SOURCES.filter((s) =>
  ['codeforces', 'atcoder', 'nowcoder', 'luogu'].includes(s.code),
)

export default function SolutionEditPage() {
  const navigate = useNavigate()
  const [templates, setTemplates] = useState<SolutionTemplate[]>([])
  const [form, setForm] = useState<SolutionCreatePayload>({
    title: '',
    content: '',
    problemSource: '',
    problemId: '',
    tags: '',
    status: 1,
  })
  const [tagInput, setTagInput] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    void fetchSolutionTemplates()
      .then(setTemplates)
      .catch(() => setTemplates([]))
  }, [])

  const selectedTags = form.tags
    ? form.tags.split(',').map((t) => t.trim()).filter(Boolean)
    : []

  const toggleTag = (tag: string) => {
    const set = new Set(selectedTags)
    if (set.has(tag)) set.delete(tag)
    else set.add(tag)
    setForm({ ...form, tags: [...set].join(',') })
  }

  const addTagFromInput = () => {
    const t = tagInput.trim()
    if (!t) return
    toggleTag(t)
    setTagInput('')
  }

  const applyTemplate = (template: SolutionTemplate) => {
    setForm((prev) => ({
      ...prev,
      content: `${prev.content}\n\n## ${template.name}\n\n${template.content}`.trim(),
      tags: prev.tags ? `${prev.tags},${template.category}` : template.category,
    }))
  }

  const handleSubmit = async () => {
    if (!form.title.trim()) {
      setError('请填写标题')
      return
    }
    if (!form.content.trim()) {
      setError('请填写正文')
      return
    }

    setSubmitting(true)
    setError('')
    try {
      const created = await createSolution({
        ...form,
        title: form.title.trim(),
        content: form.content.trim(),
      })
      navigate(`/solutions/${created.id}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '发布失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="editor-page">
      <header className="editor-topbar">
        <div className="editor-topbar-left">
          <Link to="/solutions" className="editor-back">← 题解</Link>
          <span className="editor-topbar-title">写题解</span>
        </div>
        <div className="editor-topbar-right">
          <button
            type="button"
            className="editor-btn-ghost"
            onClick={() => navigate('/solutions')}
          >
            取消
          </button>
          <button
            type="button"
            className="editor-btn-publish"
            disabled={submitting}
            onClick={() => void handleSubmit()}
          >
            {submitting ? '发布中…' : '发布题解'}
          </button>
        </div>
      </header>

      <div className="editor-title-wrap">
        <input
          className="editor-title-input"
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          placeholder="标题：例如「CF1900A — 双指针做法」"
          maxLength={200}
        />
      </div>

      <div className="editor-layout">
        <main className="editor-main">
          <MarkdownEditor
            value={form.content}
            onChange={(content) => setForm({ ...form, content })}
            placeholder="用 Markdown 写题解。可参考右侧大纲模板。"
          />

          {error && <div className="editor-error">{error}</div>}
        </main>

        <aside className="editor-sidebar">
          <section className="editor-side-card">
            <h3>题目信息</h3>
            <label className="editor-field">
              <span>来源平台</span>
              <select
                value={form.problemSource ?? ''}
                onChange={(e) => setForm({ ...form, problemSource: e.target.value })}
              >
                <option value="">不关联题目</option>
                {PROBLEM_SOURCES.map((s) => (
                  <option key={s.code} value={s.code}>{s.label}</option>
                ))}
              </select>
            </label>
            <label className="editor-field">
              <span>题号</span>
              <input
                value={form.problemId ?? ''}
                onChange={(e) => setForm({ ...form, problemId: e.target.value })}
                placeholder="如 1900A、P1001"
              />
            </label>
          </section>

          <section className="editor-side-card">
            <h3>标签</h3>
            <div className="editor-tag-chips">
              {TAG_SUGGESTIONS.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  className={`editor-tag-chip${selectedTags.includes(tag) ? ' on' : ''}`}
                  onClick={() => toggleTag(tag)}
                >
                  {tag}
                </button>
              ))}
            </div>
            <div className="editor-tag-add">
              <input
                value={tagInput}
                onChange={(e) => setTagInput(e.target.value)}
                placeholder="自定义标签"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault()
                    addTagFromInput()
                  }
                }}
              />
              <button type="button" onClick={addTagFromInput}>添加</button>
            </div>
          </section>

          <section className="editor-side-card">
            <h3>写作大纲</h3>
            <p className="editor-side-hint">洛谷式结构：题意 → 思路 → 代码 → 总结</p>
            <button
              type="button"
              className="editor-outline-btn"
              onClick={() => setForm({ ...form, content: SOLUTION_OUTLINE })}
            >
              插入大纲模板
            </button>
          </section>

          {templates.length > 0 && (
            <section className="editor-side-card">
              <h3>算法模板</h3>
              <p className="editor-side-hint">点击插入常用代码骨架</p>
              <div className="editor-template-list">
                {templates.map((tpl) => (
                  <button
                    key={tpl.id}
                    type="button"
                    className="editor-template-btn"
                    onClick={() => applyTemplate(tpl)}
                  >
                    <strong>{tpl.name}</strong>
                    <span>{tpl.category}</span>
                  </button>
                ))}
              </div>
            </section>
          )}

          <section className="editor-side-card editor-side-tip">
            <h3>发布提示</h3>
            <ul>
              <li>支持 Markdown，代码块用三个反引号</li>
              <li>分栏模式可边写边预览</li>
              <li>内容经服务端消毒，请勿粘贴不可信 HTML</li>
            </ul>
          </section>
        </aside>
      </div>
    </div>
  )
}
