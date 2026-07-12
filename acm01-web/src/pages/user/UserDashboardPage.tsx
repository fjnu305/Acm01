import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import PageHeader from '../../components/PageHeader'

export default function UserDashboardPage() {
  const { user } = useAuth()
  const displayName = user?.nickname || user?.username || ''
  const hasEmail = Boolean(user?.email?.trim())

  return (
    <div className="page-content">
      <PageHeader
        title={`你好，${displayName}`}
        description="订阅即将开始的比赛，赛前通过邮件收到提醒。"
      />

      {!hasEmail && (
        <div className="alert alert-warn">
          你尚未填写邮箱，无法接收赛前提醒。
          <Link to="/profile" className="text-link-btn">
            去个人资料补充
          </Link>
        </div>
      )}

      <section className="stat-grid user-stat-grid">
        <article className="stat-card">
          <span className="stat-label">邮箱</span>
          <strong className="stat-value stat-value-sm">{user?.email || '未填写'}</strong>
        </article>
        <article className="stat-card">
          <span className="stat-label">CF Rating</span>
          <strong className="stat-value">{user?.cfRating ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span className="stat-label">刷题数</span>
          <strong className="stat-value">{user?.solvedCount ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span className="stat-label">参赛场次</span>
          <strong className="stat-value">{user?.contestCount ?? 0}</strong>
        </article>
      </section>

      <section className="user-hero-grid">
        <Link to="/contests" className="hero-card hero-card-primary">
          <span className="hero-eyebrow">赛事聚合</span>
          <h2>浏览比赛</h2>
          <p>查看各平台即将开始、进行中和已结束的赛事，一键订阅邮件提醒。</p>
          <span className="hero-cta">进入赛事列表 →</span>
        </Link>

        <Link to="/subscriptions" className="hero-card">
          <span className="hero-eyebrow">订阅管理</span>
          <h2>我的订阅</h2>
          <p>管理已订阅的赛前提醒，支持 24 小时与 1 小时两档提醒。</p>
          <span className="hero-cta">查看订阅 →</span>
        </Link>

        <article className="hero-card hero-card-muted">
          <span className="hero-eyebrow">社区</span>
          <h2>动态广场</h2>
          <p>刷题日常、竞赛心得与选手互动。</p>
          <span className="badge-soon">即将上线</span>
        </article>
      </section>

      <section className="info-panel">
        <h3>如何订阅提醒</h3>
        <ol className="steps-list">
          <li>在「赛事」页筛选「即将开始」</li>
          <li>点击比赛行的「订阅」，选择 24 小时 / 1 小时提醒</li>
          <li>在「我的订阅」查看与管理已添加的提醒</li>
        </ol>
      </section>
    </div>
  )
}
