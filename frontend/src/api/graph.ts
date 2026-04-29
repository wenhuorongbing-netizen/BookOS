import api, { unwrap } from './http'
import type { GraphRecord } from '../types'

export interface WorkspaceGraphQuery {
  bookId?: number | string | null
  entityType?: string | null
  relationshipType?: string | null
}

export function getWorkspaceGraph(params: WorkspaceGraphQuery = {}) {
  return unwrap<GraphRecord>(api.get('/graph', { params }))
}

export function getBookGraph(bookId: number | string) {
  return unwrap<GraphRecord>(api.get(`/graph/book/${bookId}`))
}

export function getConceptGraph(conceptId: number | string) {
  return unwrap<GraphRecord>(api.get(`/graph/concept/${conceptId}`))
}
