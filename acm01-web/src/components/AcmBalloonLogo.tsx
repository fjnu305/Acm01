import { useId } from 'react'

interface AcmBalloonLogoProps {
  size?: number
  className?: string
}

function BalloonBody({
  cx,
  cy,
  rx,
  ry,
  fill,
  stroke,
  highlight,
}: {
  cx: number
  cy: number
  rx: number
  ry: number
  fill: string
  stroke?: string
  highlight: string
}) {
  const knotY = cy + ry
  return (
    <g>
      <ellipse cx={cx} cy={cy} rx={rx} ry={ry} fill={fill} stroke={stroke} strokeWidth={stroke ? 1 : 0} />
      <path
        d={`M ${cx - 2.2} ${knotY - 1} L ${cx} ${knotY + 3.2} L ${cx + 2.2} ${knotY - 1} Z`}
        fill={fill}
        stroke={stroke}
        strokeWidth={stroke ? 0.8 : 0}
        strokeLinejoin="round"
      />
      <ellipse cx={cx - rx * 0.28} cy={cy - ry * 0.22} rx={rx * 0.28} ry={ry * 0.2} fill={highlight} />
    </g>
  )
}

/** ACM 经典三色气球 Logo（红 / 白 / 蓝） */
export default function AcmBalloonLogo({ size = 32, className = '' }: AcmBalloonLogoProps) {
  const uid = useId().replace(/:/g, '')

  return (
    <svg
      className={className}
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      aria-hidden
    >
      <defs>
        <linearGradient id={`${uid}-red`} x1="12" y1="14" x2="12" y2="32" gradientUnits="userSpaceOnUse">
          <stop stopColor="#FF6B76" />
          <stop offset="1" stopColor="#C81E2D" />
        </linearGradient>
        <linearGradient id={`${uid}-white`} x1="24" y1="12" x2="24" y2="30" gradientUnits="userSpaceOnUse">
          <stop stopColor="#FFFFFF" />
          <stop offset="1" stopColor="#E8EDF4" />
        </linearGradient>
        <linearGradient id={`${uid}-blue`} x1="36" y1="14" x2="36" y2="32" gradientUnits="userSpaceOnUse">
          <stop stopColor="#3B82F6" />
          <stop offset="1" stopColor="#1E40AF" />
        </linearGradient>
        <filter id={`${uid}-shadow`} x="-20%" y="-10%" width="140%" height="130%">
          <feDropShadow dx="0" dy="1.5" stdDeviation="1.2" floodColor="#0F172A" floodOpacity="0.12" />
        </filter>
      </defs>

      <g filter={`url(#${uid}-shadow)`}>
        <BalloonBody
          cx={14}
          cy={27}
          rx={7.2}
          ry={8.8}
          fill={`url(#${uid}-red)`}
          highlight="rgba(255,255,255,0.42)"
        />
        <BalloonBody
          cx={34}
          cy={27}
          rx={7.2}
          ry={8.8}
          fill={`url(#${uid}-blue)`}
          highlight="rgba(255,255,255,0.35)"
        />
        <BalloonBody
          cx={24}
          cy={24}
          rx={7.8}
          ry={9.4}
          fill={`url(#${uid}-white)`}
          stroke="#D1D9E6"
          highlight="rgba(255,255,255,0.9)"
        />
      </g>
    </svg>
  )
}
