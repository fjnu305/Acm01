export default function TagChips({ tags, split = ',' }: { tags: string; split?: string }) {
  const items = tags
    .split(split)
    .map((t) => t.trim())
    .filter(Boolean)

  if (items.length === 0) return null

  return (
    <div className="tag-chips">
      {items.map((tag) => (
        <span key={tag} className="tag-chip">
          {tag}
        </span>
      ))}
    </div>
  )
}
