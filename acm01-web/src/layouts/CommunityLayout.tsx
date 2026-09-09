import { Outlet } from 'react-router-dom'

export default function CommunityLayout() {
  return (
    <div className="comm-page">
      <Outlet />
    </div>
  )
}
