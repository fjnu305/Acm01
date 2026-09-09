import type { SolutionTagStat } from '../../api/solution'

interface SolutionSidebarProps {
  hotTags: SolutionTagStat[]
  categories: SolutionTagStat[]
  activeTag: string
  loading?: boolean
  onSelectTag: (tag: string) => void
}

export default function SolutionSidebar({
  hotTags,
  categories,
  activeTag,
  loading,
  onSelectTag,
}: SolutionSidebarProps) {
  const visibleHot = hotTags.filter((t) => t.count > 0)

  return (
    <>
      <div className="side-widget side-widget-hot">
        <h4>热门话题</h4>
        {loading ? (
          <p className="side-tip">加载中…</p>
        ) : visibleHot.length === 0 ? (
          <p className="side-tip">暂无热门标签，发布题解后会自动统计。</p>
        ) : (
          <ul className="side-hot-list">
            {visibleHot.map((tag, index) => (
              <li key={tag.name}>
                <button
                  type="button"
                  className={`side-hot-item${activeTag === tag.name ? ' active' : ''}`}
                  onClick={() => onSelectTag(tag.name)}
                >
                  <span className="side-hot-rank">{index + 1}</span>
                  <span className="side-hot-name">#{tag.name}</span>
                  <span className="side-hot-count">{tag.count}</span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="side-widget side-widget-accent side-widget-solutions">
        <div className="side-widget-head">
          <h4>算法分类</h4>
          {activeTag && (
            <button type="button" className="side-clear-filter" onClick={() => onSelectTag('')}>
              清除
            </button>
          )}
        </div>
        <div className="side-category-grid">
          {categories.map((tag) => (
            <button
              key={tag.name}
              type="button"
              className={`side-category-chip${activeTag === tag.name ? ' active' : ''}${tag.count === 0 ? ' empty' : ''}`}
              onClick={() => onSelectTag(tag.name)}
            >
              <span>{tag.name}</span>
              {tag.count > 0 && <em>{tag.count}</em>}
            </button>
          ))}
        </div>
      </div>

      <div className="side-widget side-widget-compact">
        <p className="side-tip">
          点击分类或热门话题筛选题解；与顶部关键词搜索互斥，请先清除搜索词。
        </p>
      </div>
    </>
  )
}
