/** 轻量 Markdown 预览（仅用于本地预览，提交仍走服务端消毒） */
export function markdownToPreviewHtml(markdown: string): string {
  if (!markdown.trim()) {
    return '<p class="md-empty">预览区域：在左侧编写 Markdown 内容</p>'
  }

  let text = markdown
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // fenced code blocks
  text = text.replace(
    /```(\w*)\n([\s\S]*?)```/g,
    (_, _lang, code) => `<pre class="md-pre"><code>${code.trimEnd()}</code></pre>`,
  )

  const lines = text.split('\n')
  const out: string[] = []
  let inList = false

  for (const line of lines) {
    const trimmed = line.trimEnd()

    if (/^### (.+)/.test(trimmed)) {
      if (inList) {
        out.push('</ul>')
        inList = false
      }
      out.push(`<h3>${trimmed.slice(4)}</h3>`)
      continue
    }
    if (/^## (.+)/.test(trimmed)) {
      if (inList) {
        out.push('</ul>')
        inList = false
      }
      out.push(`<h2>${trimmed.slice(3)}</h2>`)
      continue
    }
    if (/^# (.+)/.test(trimmed)) {
      if (inList) {
        out.push('</ul>')
        inList = false
      }
      out.push(`<h1>${trimmed.slice(2)}</h1>`)
      continue
    }
    if (/^[-*] (.+)/.test(trimmed)) {
      if (!inList) {
        out.push('<ul>')
        inList = true
      }
      out.push(`<li>${inlineFormat(trimmed.slice(2))}</li>`)
      continue
    }
    if (trimmed === '') {
      if (inList) {
        out.push('</ul>')
        inList = false
      }
      out.push('<br/>')
      continue
    }
    if (inList) {
      out.push('</ul>')
      inList = false
    }
    if (trimmed.startsWith('<pre')) {
      out.push(trimmed)
    } else {
      out.push(`<p>${inlineFormat(trimmed)}</p>`)
    }
  }

  if (inList) out.push('</ul>')

  return out.join('')
}

function inlineFormat(line: string): string {
  return line
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')
}

export const SOLUTION_OUTLINE = `## 题意

简要描述题目要求。

## 思路

- 关键观察：
- 算法选型：
- 复杂度：

## 代码

\`\`\`cpp
// 核心实现
\`\`\`

## 总结

可复用的技巧或易错点。
`
