import api, { unwrap } from './http'
import type { EntityLinkPayload, EntityLinkRecord } from '../types'

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

export function updateEntityLink(id: number | string, payload: EntityLinkPayload) {
  return unwrap<EntityLinkRecord>(api.put(`/entity-links/${id}`, payload))
}

export function deleteEntityLink(id: number | string) {
  return unwrap<void>(api.delete(`/entity-links/${id}`))
}
