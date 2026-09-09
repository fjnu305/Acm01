import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import CfRatingChart from '../../components/CfRatingChart'

export default function UserDashboardPage() {
  const { user } = useAuth()
  const displayName = user?.nickname || user?.username || ''
  const hasEmail = Boolean(user?.email?.trim())

  return (
    <div className="page-content">
      <section className="dash-banner">
        <div className="dash-banner-left">
          <h2>你好，{displayName}</h2>
          <p>订阅即将开始的比赛，赛前通过邮件收到提醒</p>
        </div>
        <div className="dash-stats-inline">
          <div className="dash-stat-item">
            <span>CF Rating</span>
            <strong>{user?.cfRating ?? 0}</strong>
          </div>
          <div className="dash-stat-item">
            <span>刷题数</span>
            <strong>{user?.solvedCount ?? 0}</strong>
          </div>
          <div className="dash-stat-item">
            <span>参赛场次</span>
            <strong>{user?.contestCount ?? 0}</strong>
          </div>
        </div>
      </section>

      {!hasEmail && (
        <div className="alert alert-warn">
          你尚未填写邮箱，无法接收赛前提醒。
          <Link to="/profile" className="text-link-btn">
            去个人资料补充
          </Link>
        </div>
      )}

      {(user?.cfHandle || (user?.cfRatingHistory?.length ?? 0) > 0) && (
        <CfRatingChart
          compact
          handle={user?.cfHandle}
          currentRating={user?.cfRating}
          history={user?.cfRatingHistory ?? []}
        />
      )}

      <section className="user-hero-grid">
        <Link to="/contests" className="hero-card hero-card-primary">
          <h2>浏览比赛</h2>
          <p>Codeforces、AtCoder、洛谷等 7 个平台的赛事聚合，支持赛前邮件提醒订阅。</p>
          <span className="hero-cta">进入比赛列表 →</span>
        </Link>

        <Link to="/subscriptions" className="hero-card">
          <h2>我的订阅</h2>
          <p>管理已订阅的赛前提醒，支持赛前 24 小时与 1 小时两档通知。</p>
          <span className="hero-cta">查看订阅 →</span>
        </Link>

        <Link to="/social" className="hero-card hero-card-primary">
          <h2>讨论 · 题解 · 组队</h2>
          <p>发布动态、分享题解、招募队友，顶栏搜索可检索题解与组队帖。</p>
          <span className="hero-cta">进入社区 →</span>
        </Link>
      </section>

      <section className="info-panel">
        <h3>如何订阅提醒</h3>
        <ol className="steps-list">
          <li>在「比赛」页筛选「即将开始」</li>
          <li>点击比赛行的「订阅」，选择 24 小时 / 1 小时提醒</li>
          <li>在「订阅」页查看与管理已添加的提醒</li>
        </ol>
      </section>
    </div>
  )
}
