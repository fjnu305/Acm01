interface CommStatProps {
  type: 'favorite' | 'view'
  value: number
}

export default function CommStat({ type, value }: CommStatProps) {
  return (
    <span className="comm-stat">
      {type === 'favorite' ? (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
          <path d="M12 17.3l-6.18 3.25 1.18-6.88L2 9.02l6.91-1L12 1.5l3.09 6.52 6.91 1-5 4.65 1.18 6.88z" />
        </svg>
      ) : (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
          <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z" />
          <circle cx="12" cy="12" r="2.5" fill="currentColor" stroke="none" />
        </svg>
      )}
      {value}
    </span>
  )
}
