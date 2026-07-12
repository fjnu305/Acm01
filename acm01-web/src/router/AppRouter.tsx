import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AdminLayout from '../layouts/AdminLayout'
import UserLayout from '../layouts/UserLayout'
import AdminCrawlPage from '../pages/admin/AdminCrawlPage'
import AdminDashboardPage from '../pages/admin/AdminDashboardPage'
import AdminPlaceholderPage from '../pages/admin/AdminPlaceholderPage'
import ContestListPage from '../pages/ContestListPage'
import LoginPage from '../pages/LoginPage'
import MySubscriptionsPage from '../pages/MySubscriptionsPage'
import ProfileSettingsPage from '../pages/ProfileSettingsPage'
import RegisterPage from '../pages/RegisterPage'
import UserDashboardPage from '../pages/user/UserDashboardPage'

function BootScreen() {
  return (
    <div className="boot-screen">
      <div className="boot-spinner" />
      <p>ACMer 加载中…</p>
    </div>
  )
}

function RequireAuth({ admin }: { admin?: boolean }) {
  const { user, booting, isAdminUser } = useAuth()

  if (booting) return <BootScreen />
  if (!user) return <Navigate to="/login" replace />
  if (admin && !isAdminUser) return <Navigate to="/" replace />
  if (!admin && isAdminUser) return <Navigate to="/admin" replace />

  return <Outlet />
}

function GuestOnly() {
  const { user, booting, isAdminUser } = useAuth()

  if (booting) return <BootScreen />
  if (user) {
    return <Navigate to={isAdminUser ? '/admin' : '/'} replace />
  }

  return <Outlet />
}

export default function AppRouter() {
  return (
    <Routes>
      <Route element={<GuestOnly />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<RequireAuth admin />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="crawl" element={<AdminCrawlPage />} />
          <Route path="contests" element={<ContestListPage mode="admin" />} />
          <Route
            path="logs"
            element={
              <AdminPlaceholderPage
                title="爬取日志"
                description="查看 contest_crawl_log 爬取记录与五计数详情。"
              />
            }
          />
          <Route
            path="users"
            element={
              <AdminPlaceholderPage
                title="用户管理"
                description="用户列表、禁用账号与角色分配。"
              />
            }
          />
          <Route path="profile" element={<ProfileSettingsPage />} />
        </Route>
      </Route>

      <Route element={<RequireAuth />}>
        <Route element={<UserLayout />}>
          <Route index element={<UserDashboardPage />} />
          <Route path="contests" element={<ContestListPage mode="user" />} />
          <Route path="subscriptions" element={<MySubscriptionsPage />} />
          <Route path="profile" element={<ProfileSettingsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
