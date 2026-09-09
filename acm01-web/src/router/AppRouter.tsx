import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AdminLayout from '../layouts/AdminLayout'
import CommunityLayout from '../layouts/CommunityLayout'
import UserLayout from '../layouts/UserLayout'
import AdminCrawlLogsPage from '../pages/admin/AdminCrawlLogsPage'
import AdminCrawlPage from '../pages/admin/AdminCrawlPage'
import AdminDashboardPage from '../pages/admin/AdminDashboardPage'
import AdminUsersPage from '../pages/admin/AdminUsersPage'
import ContestDetailPage from '../pages/ContestDetailPage'
import ContestListPage from '../pages/ContestListPage'
import LoginPage from '../pages/LoginPage'
import MySubscriptionsPage from '../pages/MySubscriptionsPage'
import MyFavoritesPage from '../pages/MyFavoritesPage'
import MyTeamsPage from '../pages/MyTeamsPage'
import ProfileSettingsPage from '../pages/ProfileSettingsPage'
import InboxPage from '../pages/InboxPage'
import UserProfilePage from '../pages/UserProfilePage'
import SocialFeedPage from '../pages/SocialFeedPage'
import SolutionDetailPage from '../pages/SolutionDetailPage'
import SolutionEditPage from '../pages/SolutionEditPage'
import SolutionListPage from '../pages/SolutionListPage'
import TeamDetailPage from '../pages/TeamDetailPage'
import TeamListPage from '../pages/TeamListPage'
import TeamPublishPage from '../pages/TeamPublishPage'
import RegisterPage from '../pages/RegisterPage'
import UserDashboardPage from '../pages/user/UserDashboardPage'
import WikiLayout from '../pages/wiki/WikiLayout'
import WikiGraphPage from '../pages/wiki/WikiGraphPage'
import WikiSettingsPage from '../pages/wiki/WikiSettingsPage'
import WikiNotePage from '../pages/wiki/WikiNotePage'

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
          <Route path="logs" element={<AdminCrawlLogsPage />} />
          <Route path="users" element={<AdminUsersPage />} />
          <Route path="profile" element={<ProfileSettingsPage />} />
        </Route>
      </Route>

      <Route element={<RequireAuth />}>
        <Route element={<UserLayout />}>
          <Route index element={<UserDashboardPage />} />
          <Route path="contests" element={<ContestListPage mode="user" />} />
          <Route path="contests/:id" element={<ContestDetailPage />} />
          <Route path="subscriptions" element={<MySubscriptionsPage />} />

          <Route element={<CommunityLayout />}>
            <Route path="social" element={<SocialFeedPage />} />
            <Route path="solutions" element={<SolutionListPage />} />
            <Route path="teams" element={<TeamListPage />} />
          </Route>

          <Route path="solutions/new" element={<SolutionEditPage />} />
          <Route path="solutions/favorites" element={<MyFavoritesPage />} />
          <Route path="solutions/:id" element={<SolutionDetailPage />} />
          <Route path="teams/new" element={<TeamPublishPage />} />
          <Route path="teams/mine" element={<MyTeamsPage />} />
          <Route path="teams/:id" element={<TeamDetailPage />} />

          <Route path="search" element={<Navigate to="/solutions" replace />} />
          <Route path="community" element={<Navigate to="/social" replace />} />
          <Route path="favorites" element={<Navigate to="/solutions/favorites" replace />} />
          <Route path="my-teams" element={<Navigate to="/teams/mine" replace />} />
          <Route path="inbox" element={<InboxPage />} />
          <Route path="users/:id" element={<UserProfilePage />} />
          <Route path="profile" element={<ProfileSettingsPage />} />
          <Route path="profile/llm" element={<WikiSettingsPage />} />

          <Route path="wiki" element={<WikiLayout />}>
            <Route index element={<WikiGraphPage />} />
            <Route path="graph" element={<Navigate to="/wiki" replace />} />
            <Route path="chat" element={<Navigate to="/" replace />} />
            <Route path="note" element={<WikiNotePage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
