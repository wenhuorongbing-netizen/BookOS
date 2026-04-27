import api, { unwrap } from './http'
import type { AISuggestionEditPayload, AISuggestionPayload, AISuggestionRecord } from '../types'

export function getAISuggestions() {
  return unwrap<AISuggestionRecord[]>(api.get('/ai/suggestions'))
}

export function createNoteSummarySuggestion(payload?: AISuggestionPayload) {
  return unwrap<AISuggestionRecord>(api.post('/ai/suggestions/note-summary', payload ?? {}))
}

export function createExtractActionsSuggestion(payload?: AISuggestionPayload) {
  return unwrap<AISuggestionRecord>(api.post('/ai/suggestions/extract-actions', payload ?? {}))
}

export function createExtractConceptsSuggestion(payload?: AISuggestionPayload) {
  return unwrap<AISuggestionRecord>(api.post('/ai/suggestions/extract-concepts', payload ?? {}))
}

export function acceptAISuggestion(id: number | string) {
  return unwrap<AISuggestionRecord>(api.put(`/ai/suggestions/${id}/accept`))
}

export function rejectAISuggestion(id: number | string) {
  return unwrap<AISuggestionRecord>(api.put(`/ai/suggestions/${id}/reject`))
}

export function editAISuggestion(id: number | string, payload: AISuggestionEditPayload) {
  return unwrap<AISuggestionRecord>(api.put(`/ai/suggestions/${id}/edit`, payload))
}
