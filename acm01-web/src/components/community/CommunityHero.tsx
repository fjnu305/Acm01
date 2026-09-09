import type { ReactNode } from 'react'
import CommunitySubNav from './CommunitySubNav'

type HeroVariant = 'social' | 'solution' | 'team'

const HERO_CONFIG: Record<
  HeroVariant,
  { title: string; subtitle: string; gradient: string }
> = {
  social: {
    title: '讨论广场',
    subtitle: '像知乎一样分享刷题心得、竞赛复盘，与同好实时互动',
    gradient: 'comm-hero-social',
  },
  solution: {
    title: '题解文库',
    subtitle: '高质量算法思路，按标签检索，收藏你喜欢的写法',
    gradient: 'comm-hero-solution',
  },
  team: {
    title: '组队大厅',
    subtitle: '发布招募、智能匹配队友，一起训练冲榜',
    gradient: 'comm-hero-team',
  },
}

interface CommunityHeroProps {
  variant: HeroVariant
  actions?: ReactNode
}

export default function CommunityHero({ variant, actions }: CommunityHeroProps) {
  const config = HERO_CONFIG[variant]

  return (
    <section className={`comm-hero ${config.gradient}`}>
      <div className="comm-hero-bg" aria-hidden />
      <div className="comm-hero-content">
        <div className="comm-hero-top">
          <div>
            <h1 className="comm-hero-title">{config.title}</h1>
            <p className="comm-hero-subtitle">{config.subtitle}</p>
          </div>
          {actions && <div className="comm-hero-actions">{actions}</div>}
        </div>
        <CommunitySubNav />
      </div>
    </section>
  )
}
