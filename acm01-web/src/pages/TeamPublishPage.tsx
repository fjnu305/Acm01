import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { publishTeam, type TeamPublishPayload } from '../api/team'

const TAG_SUGGESTIONS = ['DP', '图论', '数据结构', '数学', '字符串', '贪心', '二分', 'DFS', '构造']
const MEMBER_LIMIT_OPTIONS = [2, 3, 4, 5]

export default function TeamPublishPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState<TeamPublishPayload>({
    title: '',
    description: '',
    ratingMin: 1200,
    ratingMax: 2000,
    region: '',
    tags: '',
    memberLimit: 3,
  })
  const [tagInput, setTagInput] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const selectedTags = useMemo(
    () => (form.tags ? form.tags.split(',').map((t) => t.trim()).filter(Boolean) : []),
    [form.tags],
  )

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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.title.trim()) {
      setError('请填写标题')
      return
    }
    if ((form.ratingMin ?? 0) > (form.ratingMax ?? 0)) {
      setError('Rating 下限不能高于上限')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      const created = await publishTeam({
        ...form,
        title: form.title.trim(),
        description: form.description?.trim() || '',
        region: form.region?.trim() || '',
        tags: selectedTags.join(','),
      })
      navigate(`/teams/${created.id}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '发布失败，请稍后重试')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="team-publish-page">
      <header className="team-publish-topbar">
        <div>
          <h1>发布组队</h1>
          <p>写清目标比赛、Rating 区间和人数，方便队友找到你</p>
        </div>
        <div className="team-publish-topbar-actions">
          <Link to="/teams" className="btn-outline btn-sm">
            返回列表
          </Link>
        </div>
      </header>

      <form className="team-publish-layout" onSubmit={(e) => void handleSubmit(e)}>
        <div className="team-publish-main">
          <section className="team-publish-section">
            <h2>基本信息</h2>
            <label className="team-publish-field">
              <span>标题</span>
              <input
                required
                maxLength={80}
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="例如：招 ICPC 区域赛队友 · 偏 DP / 图论"
              />
            </label>
            <label className="team-publish-field">
              <span>描述</span>
              <textarea
                rows={6}
                maxLength={2000}
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="介绍一下目标比赛、训练节奏、希望队友的方向…"
              />
            </label>
          </section>

          <section className="team-publish-section">
            <h2>招募条件</h2>
            <div className="team-publish-rating">
              <label className="team-publish-field">
                <span>Rating 下限</span>
                <input
                  type="number"
                  min={0}
                  max={4000}
                  step={100}
                  value={form.ratingMin}
                  onChange={(e) => setForm({ ...form, ratingMin: Number(e.target.value) })}
                />
              </label>
              <span className="team-publish-rating-sep" aria-hidden>
                –
              </span>
              <label className="team-publish-field">
                <span>Rating 上限</span>
                <input
                  type="number"
                  min={0}
                  max={4000}
                  step={100}
                  value={form.ratingMax}
                  onChange={(e) => setForm({ ...form, ratingMax: Number(e.target.value) })}
                />
              </label>
            </div>
            <label className="team-publish-field">
              <span>地区 / 学校</span>
              <input
                maxLength={80}
                value={form.region}
                onChange={(e) => setForm({ ...form, region: e.target.value })}
                placeholder="如：福州大学 / 线上"
              />
            </label>
            <div className="team-publish-field">
              <span>算法标签</span>
              <div className="team-publish-tag-chips">
                {TAG_SUGGESTIONS.map((tag) => (
                  <button
                    key={tag}
                    type="button"
                    className={`team-publish-tag${selectedTags.includes(tag) ? ' on' : ''}`}
                    onClick={() => toggleTag(tag)}
                  >
                    {tag}
                  </button>
                ))}
              </div>
              <div className="team-publish-tag-add">
                <input
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  placeholder="自定义标签，回车添加"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      addTagFromInput()
                    }
                  }}
                />
                <button type="button" className="btn-outline btn-sm" onClick={addTagFromInput}>
                  添加
                </button>
              </div>
            </div>
          </section>
        </div>

        <aside className="team-publish-aside">
          <section className="team-publish-section">
            <h2>队伍规模</h2>
            <p className="team-publish-hint">含队长在内，发布后不可随意改人数上限</p>
            <div className="team-publish-size-options" role="group" aria-label="队伍总人数">
              {MEMBER_LIMIT_OPTIONS.map((n) => (
                <button
                  key={n}
                  type="button"
                  className={`team-publish-size${form.memberLimit === n ? ' on' : ''}`}
                  onClick={() => setForm({ ...form, memberLimit: n })}
                >
                  {n} 人
                </button>
              ))}
            </div>
            <label className="team-publish-field team-publish-size-custom">
              <span>自定义人数（2–10）</span>
              <input
                type="number"
                min={2}
                max={10}
                value={form.memberLimit}
                onChange={(e) => {
                  const n = Number(e.target.value)
                  setForm({ ...form, memberLimit: Number.isFinite(n) ? n : 3 })
                }}
              />
            </label>
          </section>

          <section className="team-publish-preview">
            <h2>预览</h2>
            <div className="team-publish-preview-card">
              <strong>{form.title.trim() || '未命名招募'}</strong>
              <div className="team-publish-preview-meta">
                Rating {form.ratingMin ?? 0} – {form.ratingMax ?? 0}
                {form.region?.trim() ? ` · ${form.region.trim()}` : ''}
                {` · ${form.memberLimit ?? 3} 人`}
              </div>
              {selectedTags.length > 0 && (
                <div className="team-publish-preview-tags">
                  {selectedTags.map((tag) => (
                    <span key={tag}>{tag}</span>
                  ))}
                </div>
              )}
              {form.description?.trim() ? (
                <p>{form.description.trim()}</p>
              ) : (
                <p className="muted">描述会显示在队伍详情页</p>
              )}
            </div>
          </section>

          {error && <p className="team-publish-error">{error}</p>}

          <div className="team-publish-actions">
            <button type="button" className="btn-outline" onClick={() => navigate('/teams')}>
              取消
            </button>
            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? '发布中…' : '发布组队帖'}
            </button>
          </div>
        </aside>
      </form>
    </div>
  )
}
