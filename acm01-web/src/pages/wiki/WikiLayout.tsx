import { Outlet, useLocation } from 'react-router-dom'
import PageHeader from '../../components/PageHeader'

export default function WikiLayout() {
  const { pathname } = useLocation()
  const isGraph = pathname === '/wiki' || pathname === '/wiki/graph'

  if (isGraph) {
    return (
      <div className="page-content page-content-wide wiki-page wiki-graph-route">
        <Outlet />
      </div>
    )
  }

  return (
    <div className="page-content wiki-page">
      <PageHeader title="学习笔记" description="浏览本地笔记，查看知识关联图谱。" />
      <div className="wiki-board">
        <Outlet />
      </div>
    </div>
  )
}
