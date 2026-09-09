import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'
import './styles/luogu-theme.css'
import './styles/community-ui.css'
import './styles/community-ui-overrides.css'
import './styles/editor-ui.css'
import './styles/contest-ui.css'
import './styles/community-polish.css'
import './styles/wiki-ui.css'

;(globalThis as typeof globalThis & { global: typeof globalThis }).global = globalThis

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
