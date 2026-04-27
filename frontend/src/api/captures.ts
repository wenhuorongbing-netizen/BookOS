import api, { unwrap } from './http'
import type {
  RawCaptureConversionRecord,
  RawCaptureConvertPayload,
  ConceptReviewPayload,
  ConceptReviewRecord,
  RawCapturePayload,
  RawCaptureRecord,
  RawCaptureUpdatePayload,
} from '../types'

export function createCapture(payload: RawCapturePayload) {
  return unwrap<RawCaptureRecord>(api.post('/captures', payload))
}

export function getCaptureInbox(params?: { bookId?: number | string }) {
  return unwrap<RawCaptureRecord[]>(api.get('/captures/inbox', { params }))
}

export function getCapture(id: number | string) {
  return unwrap<RawCaptureRecord>(api.get(`/captures/${id}`))
}

export function updateCapture(id: number | string, payload: RawCaptureUpdatePayload) {
  return unwrap<RawCaptureRecord>(api.put(`/captures/${id}`, payload))
}

export function convertCapture(id: number | string, payload?: RawCaptureConvertPayload) {
  return unwrap<RawCaptureConversionRecord>(api.post(`/captures/${id}/convert`, payload ?? { targetType: 'NOTE' }))
}

export function reviewCaptureConcepts(id: number | string, payload: ConceptReviewPayload) {
  return unwrap<ConceptReviewRecord>(api.post(`/captures/${id}/review/concepts`, payload))
}

export function archiveCapture(id: number | string) {
  return unwrap<RawCaptureRecord>(api.put(`/captures/${id}/archive`))
}
