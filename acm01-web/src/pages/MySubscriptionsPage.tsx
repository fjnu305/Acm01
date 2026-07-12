import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { sourceLabel } from '../api/contest'
import PageHeader from '../components/PageHeader'
import {
  cancelSubscription,
  channelLabel,
  fetchMySubscriptions,
  formatDateTime,
  remindLabel,
  type SubscriptionItem,
} from '../api/subscription'

export default function MySubscriptionsPage() {
  const [items, setItems] = useState<SubscriptionItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [cancellingId, setCancellingId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setItems(await fetchMySubscriptions())
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '加载订阅失败'
      setError(msg)
      setItems([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const handleCancel = async (id: number) => {
    if (!window.confirm('确定取消该订阅？关联的待发送提醒将一并作废。')) {
      return
    }
    setCancellingId(id)
    setError('')
    try {
      await cancelSubscription(id)
      setItems((prev) => prev.filter((item) => item.id !== id))
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '取消订阅失败'
      setError(msg)
    } finally {
      setCancellingId(null)
    }
  }

  return (
    <div className="page-content page-content-wide">
      <PageHeader
        title="我的订阅"
        description={`管理赛前邮件提醒 · 当前 ${items.length} 条生效订阅`}
        actions={
          <button type="button" className="btn-outline" onClick={() => void load()} disabled={loading}>
            {loading ? '刷新中…' : '刷新'}
          </button>
        }
      />

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-panel">
          <div className="boot-spinner" />
          <p>加载订阅列表…</p>
        </div>
      ) : items.length === 0 ? (
        <div className="empty-panel">
          <h2>暂无订阅</h2>
          <p>在赛事列表中筛选「即将开始」，为比赛添加赛前邮件提醒。</p>
          <Link to="/contests" className="btn-primary">
            去浏览赛事
          </Link>
        </div>
      ) : (
        <div className="data-table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>赛事</th>
                <th>平台</th>
                <th>开始时间</th>
                <th>提醒档位</th>
                <th>渠道</th>
                <th>订阅时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td className="col-title">{item.contestTitle}</td>
                  <td>
                    <span className={`source-pill source-${item.source}`}>
                      {sourceLabel(item.source)}
                    </span>
                  </td>
                  <td>{formatDateTime(item.contestStartTime)}</td>
                  <td>{remindLabel(item.remindBeforeMinutes)}</td>
                  <td>{channelLabel()}</td>
                  <td>{formatDateTime(item.createdTime)}</td>
                  <td>
                    <button
                      type="button"
                      className="btn-outline btn-sm"
                      disabled={cancellingId === item.id}
                      onClick={() => void handleCancel(item.id)}
                    >
                      {cancellingId === item.id ? '取消中…' : '取消'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
