export function renderSafeMarkdown(markdown: string) {
  const escaped = escapeHtml(markdown || '')
  const lines = escaped.split(/\r?\n/)
  const html: string[] = []
  let inList = false

  for (const line of lines) {
    if (line.startsWith('### ')) {
      closeList()
      html.push(`<h3>${line.slice(4)}</h3>`)
      continue
    }
    if (line.startsWith('## ')) {
      closeList()
      html.push(`<h2>${line.slice(3)}</h2>`)
      continue
    }
    if (line.startsWith('# ')) {
      closeList()
      html.push(`<h1>${line.slice(2)}</h1>`)
      continue
    }
    if (line.startsWith('&gt; ')) {
      closeList()
      html.push(`<blockquote>${inline(line.slice(5))}</blockquote>`)
      continue
    }
    if (/^- /.test(line)) {
      if (!inList) {
        html.push('<ul>')
        inList = true
      }
      html.push(`<li>${inline(line.slice(2))}</li>`)
      continue
    }
    if (!line.trim()) {
      closeList()
      continue
    }
    closeList()
    html.push(`<p>${inline(line)}</p>`)
  }

  closeList()
  return html.join('')

  function closeList() {
    if (inList) {
      html.push('</ul>')
      inList = false
    }
  }
}

function inline(value: string) {
  return value
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}
