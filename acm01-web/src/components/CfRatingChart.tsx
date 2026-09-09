import { useId } from 'react'
import type { CfRatingChange } from '../api/auth'

function cfColor(rating: number): string {
  if (rating >= 2400) return '#ff0000'
  if (rating >= 2100) return '#ff8c00'
  if (rating >= 1900) return '#aa00aa'
  if (rating >= 1600) return '#0000ff'
  if (rating >= 1400) return '#03a89e'
  if (rating >= 1200) return '#008000'
  return '#808080'
}

function formatDelta(oldRating: number, newRating: number): string {
  const delta = newRating - oldRating
  if (delta > 0) return `+${delta}`
  return String(delta)
}

function formatRatedAt(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

type Props = {
  handle?: string
  currentRating?: number | null
  history: CfRatingChange[]
  compact?: boolean
}

export default function CfRatingChart({ handle, currentRating, history, compact }: Props) {
  const uid = useId().replace(/:/g, '')
  if (!history.length) {
    return (
      <section className={`cf-rating-panel${compact ? ' compact' : ''}`} aria-labelledby={`cf-rating-heading-${uid}`}>
        <h2 id={`cf-rating-heading-${uid}`}>Codeforces Rating</h2>
        <p className="cf-rating-empty">
          {handle
            ? '已绑定账号，但还没有 Rated 比赛记录。打完场后重新同步即可看到曲线。'
            : '绑定 Codeforces 账号后，这里会画出每场比赛后的分数变化。'}
        </p>
      </section>
    )
  }

  const ratings = history.map((p) => p.newRating)
  const minR = Math.min(...ratings, ...history.map((p) => p.oldRating))
  const maxR = Math.max(...ratings, ...history.map((p) => p.oldRating))
  const pad = Math.max(40, Math.round((maxR - minR) * 0.08) || 40)
  const yMin = Math.max(0, minR - pad)
  const yMax = maxR + pad
  const latest = history[history.length - 1]
  const peak = Math.max(...ratings)
  const first = history[0].oldRating
  const net = latest.newRating - first
  const width = 640
  const height = compact ? 168 : 220
  const left = 44
  const right = 16
  const top = 16
  const bottom = 28
  const plotW = width - left - right
  const plotH = height - top - bottom

  const xAt = (i: number) => {
    if (history.length === 1) return left + plotW / 2
    return left + (i / (history.length - 1)) * plotW
  }
  const yAt = (rating: number) => top + ((yMax - rating) / (yMax - yMin)) * plotH

  const line = history.map((p, i) => `${i === 0 ? 'M' : 'L'} ${xAt(i).toFixed(1)} ${yAt(p.newRating).toFixed(1)}`).join(' ')
  const area = `${line} L ${xAt(history.length - 1).toFixed(1)} ${top + plotH} L ${xAt(0).toFixed(1)} ${top + plotH} Z`
  const recent = [...history].slice(-8).reverse()
  const shownRating = currentRating ?? latest.newRating

  return (
    <section className={`cf-rating-panel${compact ? ' compact' : ''}`} aria-labelledby={`cf-rating-heading-${uid}`}>
      <div className="cf-rating-head">
        <div>
          <h2 id={`cf-rating-heading-${uid}`}>Codeforces Rating</h2>
          {handle && (
            <p className="cf-rating-handle">
              <a href={`https://codeforces.com/profile/${encodeURIComponent(handle)}`} target="_blank" rel="noreferrer">
                {handle}
              </a>
            </p>
          )}
        </div>
        <p className="cf-rating-now" style={{ color: cfColor(shownRating) }}>
          {shownRating}
        </p>
      </div>

      <dl className="cf-rating-kpis">
        <div>
          <dt>峰值</dt>
          <dd>{peak}</dd>
        </div>
        <div>
          <dt>场次</dt>
          <dd>{history.length}</dd>
        </div>
        <div>
          <dt>相对首场</dt>
          <dd className={net >= 0 ? 'up' : 'down'}>{net >= 0 ? `+${net}` : net}</dd>
        </div>
      </dl>

      <div className="cf-rating-chart-wrap">
        <svg
          className="cf-rating-svg"
          viewBox={`0 0 ${width} ${height}`}
          role="img"
          aria-label="Codeforces 每场比赛后的 Rating 折线"
        >
          <defs>
            <linearGradient id={`cf-fill-${uid}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={cfColor(shownRating)} stopOpacity="0.22" />
              <stop offset="100%" stopColor={cfColor(shownRating)} stopOpacity="0.02" />
            </linearGradient>
          </defs>
          {[0, 0.5, 1].map((t) => {
            const y = top + t * plotH
            const label = Math.round(yMax - t * (yMax - yMin))
            return (
              <g key={t}>
                <line x1={left} x2={width - right} y1={y} y2={y} className="cf-grid" />
                <text x={left - 8} y={y + 4} className="cf-axis" textAnchor="end">
                  {label}
                </text>
              </g>
            )
          })}
          <path d={area} fill={`url(#cf-fill-${uid})`} />
          <path d={line} className="cf-line" stroke={cfColor(shownRating)} />
          {history.map((p, i) => (
            <circle
              key={`${p.contestId}-${i}`}
              cx={xAt(i)}
              cy={yAt(p.newRating)}
              r={history.length > 40 ? 2.2 : 3.4}
              fill={cfColor(p.newRating)}
              tabIndex={0}
            >
              <title>
                {p.contestName} · {p.newRating} ({formatDelta(p.oldRating, p.newRating)}) · {formatRatedAt(p.ratedAt)}
              </title>
            </circle>
          ))}
        </svg>
      </div>

      <ol className="cf-rating-log">
        {recent.map((p) => {
          const delta = p.newRating - p.oldRating
          return (
            <li key={p.contestId}>
              <span className="cf-log-name">{p.contestName}</span>
              <span className="cf-log-meta">
                {formatRatedAt(p.ratedAt)}
                {p.rank != null && ` · 第 ${p.rank} 名`}
              </span>
              <span className={`cf-log-delta${delta >= 0 ? ' up' : ' down'}`}>{formatDelta(p.oldRating, p.newRating)}</span>
              <strong style={{ color: cfColor(p.newRating) }}>{p.newRating}</strong>
            </li>
          )
        })}
      </ol>
    </section>
  )
}
