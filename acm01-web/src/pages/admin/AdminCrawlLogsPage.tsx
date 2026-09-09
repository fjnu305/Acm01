import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../../api/auth'
import {
  CONTEST_SOURCES,
  fetchCrawlLogs,
  type CrawlLogItem,
  formatDateTime,
} from '../../api/contest'
import PageHeader from '../../components/PageHeader'

const PAGE_SIZE = 20

export default function AdminCrawlLogsPage() {
  const [logs, setLogs] = useState<CrawlLogItem[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [sourceFilter, setSourceFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      setError('')
      try {
        const data = await fetchCrawlLogs({
          source: sourceFilter || undefined,
          status: statusFilter || undefined,
          pageNum,
          pageSize: PAGE_SIZE,
        })
        setLogs(data.list)
        setTotal(data.total)
      } catch (err) {
        setError(err instanceof ApiError ? err.message : '加载失败')
        setLogs([])
        setTotal(0)
      } finally {
        setLoading(false)
      }
    }
    void load()
  }, [pageNum, sourceFilter, statusFilter])

  return (
    <div className="page-content">
      <PageHeader
        title="爬取日志"
        description="查看各平台爬虫执行记录与入库统计。"
      />

      <div className="filter-row">
        <select value={sourceFilter} onChange={(e) => { setSourceFilter(e.target.value); setPageNum(1) }}>
          <option value="">全部平台</option>
          {CONTEST_SOURCES.map((s) => (
            <option key={s.code} value={s.code}>{s.label}</option>
          ))}
        </select>
        <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPageNum(1) }}>
          <option value="">全部状态</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="FAILED">FAILED</option>
        </select>
      </div>

      {error && <p className="error-text">{error}</p>}
      {loading ? (
        <p>加载中…</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>平台</th>
              <th>状态</th>
              <th>触发</th>
              <th>抓取</th>
              <th>新增</th>
              <th>更新</th>
              <th>跳过</th>
              <th>耗时</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td>{log.id}</td>
                <td>{log.source}</td>
                <td className={log.status === 'SUCCESS' ? 'status-upcoming' : 'status-finished'}>{log.status}</td>
                <td>{log.triggerType}</td>
                <td>{log.fetchedCount}</td>
                <td>{log.insertedCount}</td>
                <td>{log.updatedCount}</td>
                <td>{log.skippedCount}</td>
                <td>{log.elapsedMs ?? '—'}ms</td>
                <td>{formatDateTime(log.createdTime)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className="pagination">
        <button disabled={pageNum <= 1} onClick={() => setPageNum((p) => p - 1)}>上一页</button>
        <span>{pageNum} / {totalPages}</span>
        <button disabled={pageNum >= totalPages} onClick={() => setPageNum((p) => p + 1)}>下一页</button>
      </div>

      <p><Link to="/admin/crawl">返回爬虫管理</Link></p>
    </div>
  )
}
