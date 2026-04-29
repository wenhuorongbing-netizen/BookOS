import api, { unwrap } from './http'
import type { BacklinkRecord } from '../types'

export interface BacklinkQuery {
  entityType: string
  entityId: number | string
}

export function getBacklinks(params: BacklinkQuery) {
  return unwrap<BacklinkRecord[]>(api.get('/backlinks', { params }))
}
