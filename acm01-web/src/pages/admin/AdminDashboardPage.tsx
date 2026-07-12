import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, fetchAdminDashboard, type AdminDashboard } from '../../api/auth'
import PageHeader from '../../components/PageHeader'

export default function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState<AdminDashboard | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchAdminDashboard()
      .then(setDashboard)
      .catch((err) => {
        const msg = err instanceof ApiError ? err.message : '加载控制台数据失败'
        setError(msg)
      })
  }, [])

  return (
    <div className="page-content">
      <PageHeader
        title="控制台"
        description="平台运营概览与快捷入口"
      />

      {error && <div className="alert alert-error">{error}</div>}

      {dashboard && (
        <section className="stat-grid">
          <article className="stat-card">
            <span className="stat-label">平台用户</span>
            <strong className="stat-value">{dashboard.totalUsers}</strong>
          </article>
          <article className="stat-card">
            <span className="stat-label">待审核内容</span>
            <strong className="stat-value">{dashboard.pendingReviews}</strong>
          </article>
          <article className="stat-card">
            <span className="stat-label">今日注册</span>
            <strong className="stat-value">{dashboard.todayRegistrations}</strong>
          </article>
        </section>
      )}

      <section className="quick-grid">
        <Link to="/admin/crawl" className="quick-card">
          <h3>爬虫管理</h3>
          <p>手动触发各平台赛事同步，写入 contest 表</p>
        </Link>
        <Link to="/admin/contests" className="quick-card">
          <h3>赛事数据</h3>
          <p>浏览全平台赛事列表，按状态与来源筛选</p>
        </Link>
        <article className="quick-card disabled">
          <h3>爬取日志</h3>
          <p>按批次查看爬取结果与错误详情</p>
          <span className="badge-soon">即将上线</span>
        </article>
        <article className="quick-card disabled">
          <h3>用户管理</h3>
          <p>账号状态、角色与权限配置</p>
          <span className="badge-soon">即将上线</span>
        </article>
      </section>
    </div>
  )
}
