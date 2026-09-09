import { useCallback, useRef, useState } from 'react'
import { markdownToPreviewHtml } from '../../utils/markdownPreview'

type ViewMode = 'edit' | 'preview' | 'split'

interface MarkdownEditorProps {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  minRows?: number
}

const TOOLBAR_ITEMS: { label: string; title: string; insert: string }[] = [
  { label: 'H2', title: '二级标题', insert: '## 标题\n' },
  { label: 'B', title: '粗体', insert: '**粗体**' },
  { label: '代码', title: '行内代码', insert: '`code`' },
  { label: '块', title: '代码块', insert: '```cpp\n\n```' },
  { label: '列表', title: '无序列表', insert: '- 条目\n' },
]

export default function MarkdownEditor({
  value,
  onChange,
  placeholder = '支持 Markdown：标题、列表、代码块…',
  minRows = 18,
}: MarkdownEditorProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const [mode, setMode] = useState<ViewMode>('split')

  const insertSnippet = useCallback(
    (snippet: string) => {
      const el = textareaRef.current
      if (!el) {
        onChange(value + snippet)
        return
      }
      const start = el.selectionStart
      const end = el.selectionEnd
      const next = value.slice(0, start) + snippet + value.slice(end)
      onChange(next)
      requestAnimationFrame(() => {
        el.focus()
        const pos = start + snippet.length
        el.setSelectionRange(pos, pos)
      })
    },
    [value, onChange],
  )

  const previewHtml = markdownToPreviewHtml(value)

  return (
    <div className="md-editor">
      <div className="md-editor-toolbar">
        <div className="md-editor-modes">
          {(['edit', 'split', 'preview'] as ViewMode[]).map((m) => (
            <button
              key={m}
              type="button"
              className={`md-mode-btn${mode === m ? ' active' : ''}`}
              onClick={() => setMode(m)}
            >
              {m === 'edit' ? '编辑' : m === 'preview' ? '预览' : '分栏'}
            </button>
          ))}
        </div>
        <div className="md-editor-tools">
          {TOOLBAR_ITEMS.map((item) => (
            <button
              key={item.label}
              type="button"
              className="md-tool-btn"
              title={item.title}
              onClick={() => insertSnippet(item.insert)}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      <div className={`md-editor-body mode-${mode}`}>
        {(mode === 'edit' || mode === 'split') && (
          <div className="md-pane md-pane-edit">
            <textarea
              ref={textareaRef}
              className="md-textarea"
              value={value}
              onChange={(e) => onChange(e.target.value)}
              placeholder={placeholder}
              rows={minRows}
              spellCheck={false}
            />
          </div>
        )}
        {(mode === 'preview' || mode === 'split') && (
          <div className="md-pane md-pane-preview">
            <div className="md-preview-label">预览</div>
            <div
              className="md-preview-content"
              dangerouslySetInnerHTML={{ __html: previewHtml }}
            />
          </div>
        )}
      </div>
    </div>
  )
}
