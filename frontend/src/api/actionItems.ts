import api, { unwrap } from './http'
import type { ActionItemPayload, ActionItemRecord, RawCaptureConversionRecord } from '../types'

export function getActionItems(params?: { bookId?: number | string; completed?: boolean; q?: string }) {
  return unwrap<ActionItemRecord[]>(api.get('/action-items', { params }))
}

export function createActionItem(payload: ActionItemPayload) {
  return unwrap<ActionItemRecord>(api.post('/action-items', payload))
}

export function getActionItem(id: number | string) {
  return unwrap<ActionItemRecord>(api.get(`/action-items/${id}`))
}

export function updateActionItem(id: number | string, payload: ActionItemPayload) {
  return unwrap<ActionItemRecord>(api.put(`/action-items/${id}`, payload))
}

export function completeActionItem(id: number | string) {
  return unwrap<ActionItemRecord>(api.put(`/action-items/${id}/complete`))
}

export function reopenActionItem(id: number | string) {
  return unwrap<ActionItemRecord>(api.put(`/action-items/${id}/reopen`))
}

export function archiveActionItem(id: number | string) {
  return unwrap<void>(api.delete(`/action-items/${id}`))
}

export function convertCaptureToActionItem(captureId: number | string, payload?: { title?: string | null }) {
  return unwrap<RawCaptureConversionRecord>(api.post(`/captures/${captureId}/convert/action-item`, payload ?? {}))
}
