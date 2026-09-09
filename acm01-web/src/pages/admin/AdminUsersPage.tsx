import { useEffect, useState } from 'react'
import { ApiError } from '../../api/auth'
import {
  fetchAdminUsers,
  updateUserRoles,
  updateUserStatus,
  type AdminUserItem,
} from '../../api/admin'
import PageHeader from '../../components/PageHeader'

const PAGE_SIZE = 20

export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUserItem[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionMsg, setActionMsg] = useState('')

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))

  const loadUsers = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await fetchAdminUsers({ keyword: keyword || undefined, pageNum, pageSize: PAGE_SIZE })
      setUsers(data.list)
      setTotal(data.total)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadUsers()
  }, [pageNum])

  const handleSearch = () => {
    setPageNum(1)
    void loadUsers()
  }

  const toggleStatus = async (user: AdminUserItem) => {
    setActionMsg('')
    try {
      const newStatus = user.status === 1 ? 0 : 1
      await updateUserStatus(user.id, newStatus)
      setActionMsg(`已更新 ${user.username} 状态`)
      void loadUsers()
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const toggleAdmin = async (user: AdminUserItem) => {
    setActionMsg('')
    const isAdmin = user.roles.includes('ADMIN')
    const newRoles = isAdmin ? ['USER'] : ['USER', 'ADMIN']
    try {
      await updateUserRoles(user.id, newRoles)
      setActionMsg(`已更新 ${user.username} 角色`)
      void loadUsers()
    } catch (err) {
      setActionMsg(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  return (
    <div className="page-content">
      <PageHeader title="用户管理" description="查看用户、禁用账号与角色分配。" />

      <div className="filter-row">
        <input
          type="search"
          placeholder="搜索用户名 / 昵称 / 邮箱"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button type="button" onClick={handleSearch}>搜索</button>
      </div>

      {error && <p className="error-text">{error}</p>}
      {actionMsg && <p className="success-text">{actionMsg}</p>}

      {loading ? (
        <p>加载中…</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>昵称</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.username}</td>
                <td>{user.nickname ?? '—'}</td>
                <td>{user.email ?? '—'}</td>
                <td>{user.roles.join(', ')}</td>
                <td>{user.status === 1 ? '正常' : '禁用'}</td>
                <td>{user.createdTime ? new Date(user.createdTime).toLocaleString('zh-CN') : '—'}</td>
                <td>
                  <button type="button" onClick={() => toggleStatus(user)}>
                    {user.status === 1 ? '禁用' : '启用'}
                  </button>
                  <button type="button" onClick={() => toggleAdmin(user)}>
                    {user.roles.includes('ADMIN') ? '取消管理员' : '设为管理员'}
                  </button>
                </td>
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
    </div>
  )
}
