import api, { unwrap } from './http'
import type { GraphRecord } from '../types'

export interface WorkspaceGraphQuery {
  bookId?: number | string | null
  conceptId?: number | string | null
  projectId?: number | string | null
  entityType?: string | null
  relationshipType?: string | null
  sourceConfidence?: string | null
  createdFrom?: string | null
  createdTo?: string | null
  depth?: number | string | null
  limit?: number | string | null
}

export function getWorkspaceGraph(params: WorkspaceGraphQuery = {}) {
  return unwrap<GraphRecord>(api.get('/graph', { params }))
}

export function getBookGraph(bookId: number | string, params: Omit<WorkspaceGraphQuery, 'bookId' | 'conceptId' | 'projectId'> = {}) {
  return unwrap<GraphRecord>(api.get(`/graph/book/${bookId}`, { params }))
}

export function getConceptGraph(conceptId: number | string, params: Omit<WorkspaceGraphQuery, 'bookId' | 'conceptId' | 'projectId'> = {}) {
  return unwrap<GraphRecord>(api.get(`/graph/concept/${conceptId}`, { params }))
}

export function getProjectGraph(projectId: number | string, params: Omit<WorkspaceGraphQuery, 'bookId' | 'conceptId' | 'projectId'> = {}) {
  return unwrap<GraphRecord>(api.get(`/graph/project/${projectId}`, { params }))
}
