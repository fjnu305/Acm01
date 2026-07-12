import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  clearToken,
  fetchCurrentUser,
  getToken,
  isAdmin,
  type UserInfo,
} from '../api/auth'

interface AuthContextValue {
  user: UserInfo | null
  booting: boolean
  setUser: (user: UserInfo | null) => void
  logout: () => void
  isAdminUser: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [booting, setBooting] = useState(true)

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setBooting(false)
      return
    }
    fetchCurrentUser()
      .then(setUser)
      .catch(() => clearToken())
      .finally(() => setBooting(false))
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      booting,
      setUser,
      logout,
      isAdminUser: user ? isAdmin(user) : false,
    }),
    [user, booting, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
