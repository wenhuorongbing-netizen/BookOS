import api, { unwrap } from './http'
import type { ImportCommitRecord, ImportPreviewRecord, ImportRequestPayload } from '../types'

export function exportAllJson() {
  return unwrap<Record<string, unknown>>(api.get('/export/json'))
}

export function exportBookJson(bookId: number | string) {
  return unwrap<Record<string, unknown>>(api.get(`/export/book/${bookId}/json`))
}

export function previewImport(payload: ImportRequestPayload) {
  return unwrap<ImportPreviewRecord>(api.post('/import/preview', payload))
}

export function commitImport(payload: ImportRequestPayload) {
  return unwrap<ImportCommitRecord>(api.post('/import/commit', payload))
}

export async function downloadExport(path: string, filename: string) {
  const response = await api.get(path, { responseType: 'blob' })
  const contentType = response.headers['content-type']
  const blob = new Blob([response.data], {
    type: typeof contentType === 'string' ? contentType : 'application/octet-stream',
  })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
