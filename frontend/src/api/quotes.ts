import api, { unwrap } from './http'
import type { QuotePayload, QuoteRecord, RawCaptureConversionRecord } from '../types'

export function getQuotes(params?: { bookId?: number | string; q?: string }) {
  return unwrap<QuoteRecord[]>(api.get('/quotes', { params }))
}

export function createQuote(payload: QuotePayload) {
  return unwrap<QuoteRecord>(api.post('/quotes', payload))
}

export function getQuote(id: number | string) {
  return unwrap<QuoteRecord>(api.get(`/quotes/${id}`))
}

export function updateQuote(id: number | string, payload: QuotePayload) {
  return unwrap<QuoteRecord>(api.put(`/quotes/${id}`, payload))
}

export function archiveQuote(id: number | string) {
  return unwrap<void>(api.delete(`/quotes/${id}`))
}

export function convertCaptureToQuote(captureId: number | string) {
  return unwrap<RawCaptureConversionRecord>(api.post(`/captures/${captureId}/convert/quote`))
}
