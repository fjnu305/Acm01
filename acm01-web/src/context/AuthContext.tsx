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

const BOOT_TIMEOUT_MS = 10000

function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return new Promise((resolve, reject) => {
    const timer = window.setTimeout(() => reject(new Error('请求超时')), ms)
    promise
      .then((value) => {
        window.clearTimeout(timer)
        resolve(value)
      })
      .catch((err) => {
        window.clearTimeout(timer)
        reject(err)
      })
  })
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [booting, setBooting] = useState(true)

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setBooting(false)
      return
    }
    withTimeout(fetchCurrentUser(), BOOT_TIMEOUT_MS)
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
