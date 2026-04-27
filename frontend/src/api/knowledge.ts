import api, { unwrap } from './http'
import type {
  ConceptPayload,
  ConceptRecord,
  EntityLinkPayload,
  EntityLinkRecord,
  KnowledgeObjectPayload,
  KnowledgeObjectRecord,
  KnowledgeObjectType,
} from '../types'

export function getConcepts(params?: { bookId?: number | string; q?: string }) {
  return unwrap<ConceptRecord[]>(api.get('/concepts', { params }))
}

export function getBookConcepts(bookId: number | string, params?: { q?: string }) {
  return unwrap<ConceptRecord[]>(api.get(`/books/${bookId}/concepts`, { params }))
}

export function createConcept(payload: ConceptPayload) {
  return unwrap<ConceptRecord>(api.post('/concepts', payload))
}

export function getConcept(id: number | string) {
  return unwrap<ConceptRecord>(api.get(`/concepts/${id}`))
}

export function updateConcept(id: number | string, payload: ConceptPayload) {
  return unwrap<ConceptRecord>(api.put(`/concepts/${id}`, payload))
}

export function archiveConcept(id: number | string) {
  return unwrap<void>(api.delete(`/concepts/${id}`))
}

export function getKnowledgeObjects(params?: {
  type?: KnowledgeObjectType
  bookId?: number | string
  conceptId?: number | string
  q?: string
}) {
  return unwrap<KnowledgeObjectRecord[]>(api.get('/knowledge-objects', { params }))
}

export function createKnowledgeObject(payload: KnowledgeObjectPayload) {
  return unwrap<KnowledgeObjectRecord>(api.post('/knowledge-objects', payload))
}

export function getKnowledgeObject(id: number | string) {
  return unwrap<KnowledgeObjectRecord>(api.get(`/knowledge-objects/${id}`))
}

export function updateKnowledgeObject(id: number | string, payload: KnowledgeObjectPayload) {
  return unwrap<KnowledgeObjectRecord>(api.put(`/knowledge-objects/${id}`, payload))
}

export function archiveKnowledgeObject(id: number | string) {
  return unwrap<void>(api.delete(`/knowledge-objects/${id}`))
}

export function getEntityLinks(params?: {
  sourceType?: string
  sourceId?: number | string
  targetType?: string
  targetId?: number | string
}) {
  return unwrap<EntityLinkRecord[]>(api.get('/entity-links', { params }))
}

export function createEntityLink(payload: EntityLinkPayload) {
  return unwrap<EntityLinkRecord>(api.post('/entity-links', payload))
}

export function deleteEntityLink(id: number | string) {
  return unwrap<void>(api.delete(`/entity-links/${id}`))
}
