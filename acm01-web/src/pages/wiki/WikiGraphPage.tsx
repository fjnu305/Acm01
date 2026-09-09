import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { forceCollide } from 'd3-force'
import ForceGraph2D, { type ForceGraphMethods, type LinkObject, type NodeObject } from 'react-force-graph-2d'
import { useAiChat } from '../../context/AiChatContext'
import { fetchWikiGraph, type GraphEdge, type GraphNode, WikiApiError } from '../../api/wiki'

type FgNode = NodeObject & {
  id: string
  label: string
  group: string
  val: number
}

type FgLink = LinkObject & {
  source: string
  target: string
}

const GROUPS: { id: string; label: string; color: string }[] = [
  { id: 'concepts', label: '概念', color: '#4c6ef5' },
  { id: 'entities', label: '实体', color: '#e64980' },
  { id: 'sources', label: '来源', color: '#12b886' },
  { id: 'questions', label: '问题', color: '#f76707' },
  { id: 'comparisons', label: '对比', color: '#7950f2' },
  { id: 'references', label: '参考', color: '#868e96' },
  { id: 'meta', label: '元信息', color: '#adb5bd' },
  { id: 'root', label: '根节点', color: '#495057' },
]

const GROUP_COLORS = Object.fromEntries(GROUPS.map((g) => [g.id, g.color]))

function colorFor(group: string) {
  return GROUP_COLORS[group] ?? '#868e96'
}

function groupLabel(group: string) {
  return GROUPS.find((g) => g.id === group)?.label ?? group
}

function neighborSet(id: string, edges: GraphEdge[]) {
  const set = new Set<string>([id])
  edges.forEach((e) => {
    if (e.source === id) set.add(e.target)
    if (e.target === id) set.add(e.source)
  })
  return set
}

function linkEndpoints(link: LinkObject) {
  const source = typeof link.source === 'object' ? (link.source as FgNode).id : String(link.source)
  const target = typeof link.target === 'object' ? (link.target as FgNode).id : String(link.target)
  return { source, target }
}

export default function WikiGraphPage() {
  const [nodes, setNodes] = useState<GraphNode[]>([])
  const [edges, setEdges] = useState<GraphEdge[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState('')
  const [selected, setSelected] = useState<string | null>(null)
  const [focusId, setFocusId] = useState<string | null>(null)
  const [size, setSize] = useState({ w: 800, h: 600 })
  const wrapRef = useRef<HTMLDivElement>(null)
  const fgRef = useRef<ForceGraphMethods<FgNode, FgLink> | undefined>(undefined)
  const selectedRef = useRef<string | null>(null)
  const fittedKeyRef = useRef('')
  const { openChat } = useAiChat()

  useEffect(() => {
    selectedRef.current = selected
  }, [selected])

  useEffect(() => {
    setLoading(true)
    fetchWikiGraph()
      .then((g) => {
        setNodes(g.nodes)
        setEdges(g.edges)
        setError(null)
      })
      .catch((e: unknown) => {
        setError(e instanceof WikiApiError ? e.message : '加载图谱失败')
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    const el = wrapRef.current
    if (!el) return
    const ro = new ResizeObserver(([entry]) => {
      const { width, height } = entry.contentRect
      if (width > 0 && height > 0) setSize({ w: width, h: height })
    })
    ro.observe(el)
    return () => ro.disconnect()
  }, [])

  const filtered = useMemo(() => {
    const q = filter.trim().toLowerCase()
    if (!q) return { nodes, edges }
    const keep = new Set(
      nodes.filter((n) => n.label.toLowerCase().includes(q) || n.id.toLowerCase().includes(q)).map((n) => n.id),
    )
    edges.forEach((e) => {
      if (keep.has(e.source) || keep.has(e.target)) {
        keep.add(e.source)
        keep.add(e.target)
      }
    })
    return {
      nodes: nodes.filter((n) => keep.has(n.id)),
      edges: edges.filter((e) => keep.has(e.source) && keep.has(e.target)),
    }
  }, [nodes, edges, filter])

  const focusNeighbors = useMemo(() => {
    if (!focusId) return null
    return neighborSet(focusId, filtered.edges)
  }, [focusId, filtered.edges])

  const degreeMap = useMemo(() => {
    const map = new Map<string, number>()
    filtered.edges.forEach((e) => {
      map.set(e.source, (map.get(e.source) ?? 0) + 1)
      map.set(e.target, (map.get(e.target) ?? 0) + 1)
    })
    return map
  }, [filtered.edges])

  const graphKey = useMemo(
    () => `${filter.trim()}|${filtered.nodes.map((n) => n.id).join(',')}`,
    [filter, filtered.nodes],
  )

  const graphData = useMemo(() => {
    const fgNodes: FgNode[] = filtered.nodes.map((n) => ({
      id: n.id,
      label: n.label,
      group: n.group,
      val: 1 + Math.sqrt(degreeMap.get(n.id) ?? 0) * 0.6,
    }))
    const fgLinks: FgLink[] = filtered.edges.map((e) => ({
      source: e.source,
      target: e.target,
    }))
    return { nodes: fgNodes, links: fgLinks }
  }, [filtered.nodes, filtered.edges, degreeMap])

  useEffect(() => {
    fittedKeyRef.current = ''
    setSelected(null)
    setFocusId(null)
  }, [graphKey])

  useEffect(() => {
    const fg = fgRef.current
    if (!fg || graphData.nodes.length === 0) return

    const count = graphData.nodes.length
    const linkDist = Math.min(200, 90 + Math.sqrt(count) * 14)
    const charge = -Math.min(600, 160 + count * 12)

    fg.d3Force('link')?.distance(linkDist)
    fg.d3Force('charge')?.strength(charge)
    fg.d3Force(
      'collision',
      forceCollide<FgNode>().radius((node: FgNode) => 10 + Math.sqrt(node.val ?? 1) * 2),
    )
    fg.d3ReheatSimulation()
  }, [graphData])

  const selectedNode = useMemo(
    () => filtered.nodes.find((n) => n.id === selected) ?? null,
    [filtered.nodes, selected],
  )

  const neighbors = useMemo(() => {
    if (!selected) return []
    const ids = neighborSet(selected, filtered.edges)
    ids.delete(selected)
    return filtered.nodes.filter((n) => ids.has(n.id))
  }, [filtered.edges, filtered.nodes, selected])

  const activeGroups = useMemo(() => {
    const set = new Set(filtered.nodes.map((n) => n.group))
    return GROUPS.filter((g) => set.has(g.id))
  }, [filtered.nodes])

  const paintNode = useCallback((node: NodeObject, ctx: CanvasRenderingContext2D, globalScale: number) => {
    const n = node as FgNode
    const x = n.x ?? 0
    const y = n.y ?? 0
    const isFocus = focusId === n.id
    const isNeighbor = focusNeighbors?.has(n.id) ?? false
    const dim = focusNeighbors ? !isFocus && !isNeighbor : false
    const r = (5 + Math.sqrt(n.val) * 0.9) / globalScale
    const color = colorFor(n.group)

    if (isFocus) {
      ctx.beginPath()
      ctx.arc(x, y, (r * globalScale + 6) / globalScale, 0, 2 * Math.PI)
      ctx.fillStyle = 'rgba(76, 110, 245, 0.14)'
      ctx.fill()
    }

    ctx.beginPath()
    ctx.arc(x, y, r, 0, 2 * Math.PI)
    ctx.fillStyle = dim ? 'rgba(148, 163, 184, 0.35)' : color
    ctx.fill()

    if (isFocus || isNeighbor) {
      const fontSize = (isFocus ? 8 : 7) / globalScale
      const maxLen = isFocus ? 20 : 14
      const text = n.label.length > maxLen ? `${n.label.slice(0, maxLen - 1)}…` : n.label
      ctx.font = `${isFocus ? 600 : 500} ${fontSize}px "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'top'
      ctx.fillStyle = dim ? 'rgba(100, 116, 139, 0.55)' : '#475569'
      ctx.fillText(text, x, y + r + 2 / globalScale)
    }
  }, [focusId, focusNeighbors])

  const paintLink = useCallback((link: LinkObject, ctx: CanvasRenderingContext2D) => {
    const { source, target } = linkEndpoints(link)
    const highlight = focusNeighbors ? focusNeighbors.has(source) && focusNeighbors.has(target) : false
    const dim = focusNeighbors ? !highlight : false

    const a = link.source as FgNode
    const b = link.target as FgNode
    if (a.x == null || a.y == null || b.x == null || b.y == null) return

    ctx.beginPath()
    ctx.moveTo(a.x, a.y)
    ctx.lineTo(b.x, b.y)
    ctx.strokeStyle = highlight
      ? 'rgba(76, 110, 245, 0.5)'
      : dim
        ? 'rgba(148, 163, 184, 0.06)'
        : 'rgba(100, 116, 139, 0.16)'
    ctx.lineWidth = highlight ? 1.2 : 0.6
    ctx.stroke()
  }, [focusNeighbors])

  const handleEngineStop = useCallback(() => {
    if (fittedKeyRef.current === graphKey) return
    fgRef.current?.zoomToFit(0, 90)
    fittedKeyRef.current = graphKey
    fgRef.current?.pauseAnimation()
  }, [graphKey])

  const handleFit = useCallback(() => {
    fgRef.current?.zoomToFit(0, 90)
  }, [])

  return (
    <div className="wiki-graph-view">
      <header className="wiki-graph-topbar">
        <div>
          <h1>知识图谱</h1>
          <p>{filtered.nodes.length} 篇笔记 · {filtered.edges.length} 条关联</p>
        </div>
        <div className="wiki-graph-topbar-actions">
          <button type="button" className="wiki-graph-top-btn" onClick={handleFit}>
            适应画布
          </button>
          <button type="button" className="wiki-graph-top-btn accent" onClick={openChat}>
            AI 对话
          </button>
        </div>
      </header>

      {error && <div className="alert alert-error wiki-banner">{error}</div>}

      <div className="wiki-graph-stage">
        <div className="wiki-graph-canvas-wrap" ref={wrapRef}>
          <div className="wiki-graph-floatbar">
            <input
              className="wiki-graph-search-input"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              placeholder="搜索笔记…"
              aria-label="搜索笔记"
            />
          </div>

          {loading ? (
            <div className="wiki-graph-placeholder">加载中…</div>
          ) : filtered.nodes.length === 0 ? (
            <div className="wiki-graph-placeholder">暂无笔记节点</div>
          ) : (
            <ForceGraph2D
              ref={fgRef}
              width={size.w}
              height={size.h}
              graphData={graphData}
              backgroundColor="#eceff3"
              nodeRelSize={1}
              nodeVal="val"
              linkWidth={0}
              linkColor={() => 'transparent'}
              linkCanvasObjectMode={() => 'replace'}
              linkCanvasObject={(link, ctx) => paintLink(link, ctx)}
              nodeCanvasObjectMode={() => 'replace'}
              nodeCanvasObject={(node, ctx, globalScale) => paintNode(node, ctx, globalScale)}
              nodePointerAreaPaint={(node, color, ctx, globalScale) => {
                const n = node as FgNode
                const r = (5 + Math.sqrt(n.val) * 0.9) / globalScale + 4 / globalScale
                ctx.fillStyle = color
                ctx.beginPath()
                ctx.arc(node.x ?? 0, node.y ?? 0, r, 0, 2 * Math.PI)
                ctx.fill()
              }}
              onNodeClick={(node) => {
                const id = (node as FgNode).id
                setSelected(id)
                setFocusId(id)
              }}
              onNodeHover={(node) => setFocusId(node ? (node as FgNode).id : selectedRef.current)}
              onBackgroundClick={() => {
                setSelected(null)
                setFocusId(null)
              }}
              onEngineStop={handleEngineStop}
              cooldownTicks={100}
              warmupTicks={60}
              d3AlphaDecay={0.04}
              d3VelocityDecay={0.65}
              enableNodeDrag
              enableZoomInteraction
              enablePanInteraction
            />
          )}

          {activeGroups.length > 0 && !loading && filtered.nodes.length > 0 && (
            <div className="wiki-graph-legend">
              {activeGroups.map((g) => (
                <span key={g.id} className="wiki-graph-legend-item">
                  <i style={{ background: g.color }} />
                  {g.label}
                </span>
              ))}
            </div>
          )}

          <aside className={`wiki-graph-inspector${selectedNode ? ' open' : ''}`}>
            {selectedNode && (
              <>
                <button
                  type="button"
                  className="wiki-graph-inspector-close"
                  onClick={() => {
                    setSelected(null)
                    setFocusId(null)
                  }}
                  aria-label="关闭"
                >
                  ×
                </button>
                <div className="wiki-graph-side-head">
                  <span className="wiki-graph-side-group" style={{ color: colorFor(selectedNode.group) }}>
                    {groupLabel(selectedNode.group)}
                  </span>
                  <h3>{selectedNode.label}</h3>
                </div>

                {neighbors.length > 0 && (
                  <div className="wiki-graph-side-section">
                    <h4>关联 · {neighbors.length}</h4>
                    <ul>
                      {neighbors.slice(0, 20).map((n) => (
                        <li key={n.id}>
                          <button
                            type="button"
                            onClick={() => {
                              setSelected(n.id)
                              setFocusId(n.id)
                            }}
                          >
                            <i style={{ background: colorFor(n.group) }} />
                            {n.label}
                          </button>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                <Link
                  to={`/wiki/note?path=${encodeURIComponent(selectedNode.id)}`}
                  className="btn-primary wiki-graph-open-btn"
                >
                  打开笔记
                </Link>
              </>
            )}
          </aside>
        </div>
      </div>
    </div>
  )
}
