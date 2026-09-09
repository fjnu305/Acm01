import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { InboxUnreadProvider } from './context/InboxUnreadContext'
import { NotificationProvider } from './context/NotificationContext'
import AppRouter from './router/AppRouter'
import './index.css'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <InboxUnreadProvider>
          <NotificationProvider>
            <AppRouter />
          </NotificationProvider>
        </InboxUnreadProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
