/** Normalize backend / user input to a known platform code */
export function normalizeContestSource(source?: string): string {
  if (!source) return 'all'
  const s = source.toLowerCase().trim()
  if (s === 'cf' || s.includes('codeforces')) return 'codeforces'
  if (s.includes('atcoder')) return 'atcoder'
  if (s.includes('nowcoder') || s.includes('牛客')) return 'nowcoder'
  if (s.includes('luogu') || s.includes('洛谷')) return 'luogu'
  if (s.includes('ccpc')) return 'ccpc'
  if (s.includes('icpc')) return 'icpc'
  if (s.includes('lanqiao') || s.includes('蓝桥')) return 'lanqiao'
  return s
}

interface ContestPlatformLogoProps {
  source?: string
  size?: number
  className?: string
  /** mark = icon only; badge = rounded tile with icon */
  variant?: 'mark' | 'badge'
  title?: string
}

function PlatformMark({ code, size }: { code: string; size: number }) {
  switch (code) {
    case 'codeforces':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#E74C3C" />
          <path
            d="M8 22 L16 8 L24 22 Z"
            stroke="#fff"
            strokeWidth="2.2"
            strokeLinejoin="round"
            fill="none"
          />
          <path d="M11 18 H21" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'atcoder':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#222" />
          <path
            d="M9 23 V11 H14 C17.2 11 19 12.6 19 15.2 C19 17.4 17.6 18.8 15.8 19.2 L20 23 H16.5 L12.8 19.4 H12.2 V23 H9 Z M12.2 16.8 H14 C15.4 16.8 16.2 16.1 16.2 15 C16.2 13.9 15.4 13.2 14 13.2 H12.2 V16.8 Z"
            fill="#fff"
          />
        </svg>
      )
    case 'nowcoder':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#2D8CF0" />
          <circle cx="16" cy="16" r="8" fill="#fff" />
          <path
            d="M12 16 C12 13.8 13.8 12 16 12 C18.2 12 20 13.8 20 16 C20 18.2 18.2 20 16 20"
            stroke="#2D8CF0"
            strokeWidth="2.2"
            strokeLinecap="round"
          />
          <circle cx="16" cy="16" r="2.2" fill="#2D8CF0" />
        </svg>
      )
    case 'luogu':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#52C41A" />
          <path
            d="M16 7 C11 7 8 10.5 8 14.5 C8 19.5 12 22 16 25 C20 22 24 19.5 24 14.5 C24 10.5 21 7 16 7 Z"
            fill="#fff"
          />
          <path
            d="M16 11 C18.5 11 20 12.8 20 14.8 C20 17.2 18 18.5 16 20 C14 18.5 12 17.2 12 14.8 C12 12.8 13.5 11 16 11 Z"
            fill="#52C41A"
          />
        </svg>
      )
    case 'ccpc':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#D4A017" />
          <text
            x="16"
            y="20"
            textAnchor="middle"
            fill="#1E293B"
            fontSize="9"
            fontWeight="800"
            fontFamily="Segoe UI, system-ui, sans-serif"
          >
            CCPC
          </text>
        </svg>
      )
    case 'icpc':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#4695EB" />
          <circle cx="16" cy="16" r="8" stroke="#fff" strokeWidth="1.8" fill="none" />
          <ellipse cx="16" cy="16" rx="8" ry="3.2" stroke="#fff" strokeWidth="1.4" fill="none" />
          <path d="M16 8 V24" stroke="#fff" strokeWidth="1.4" />
        </svg>
      )
    case 'lanqiao':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#1677FF" />
          <path
            d="M6 20 H26"
            stroke="#fff"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path
            d="M8 20 V14 C8 14 11 11 16 11 C21 11 24 14 24 14 V20"
            stroke="#fff"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
          />
        </svg>
      )
    case 'all':
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#EFF6FF" stroke="#BFDBFE" strokeWidth="1" />
          <circle cx="11" cy="17" r="4" fill="#E63946" />
          <circle cx="16" cy="15" r="4.2" fill="#fff" stroke="#CBD5E1" />
          <circle cx="21" cy="17" r="4" fill="#1D4ED8" />
        </svg>
      )
    default:
      return (
        <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden>
          <rect width="32" height="32" rx="6" fill="#F1F5F9" stroke="#E2E8F0" />
          <text
            x="16"
            y="20"
            textAnchor="middle"
            fill="#64748B"
            fontSize="10"
            fontWeight="700"
            fontFamily="Segoe UI, system-ui, sans-serif"
          >
            {code.slice(0, 2).toUpperCase()}
          </text>
        </svg>
      )
  }
}

export default function ContestPlatformLogo({
  source,
  size = 28,
  className = '',
  variant = 'mark',
  title,
}: ContestPlatformLogoProps) {
  const code = normalizeContestSource(source)
  const label = title ?? code

  if (variant === 'badge') {
    return (
      <span
        className={`platform-logo-badge platform-logo-${code}${className ? ` ${className}` : ''}`}
        title={label}
      >
        <PlatformMark code={code} size={size} />
      </span>
    )
  }

  return (
    <span className={`platform-logo${className ? ` ${className}` : ''}`} title={label}>
      <PlatformMark code={code} size={size} />
    </span>
  )
}
