import api, { unwrap } from './http'
import type { GraphRecord } from '../types'

export function getBookGraph(bookId: number | string) {
  return unwrap<GraphRecord>(api.get(`/graph/book/${bookId}`))
}

export function getConceptGraph(conceptId: number | string) {
  return unwrap<GraphRecord>(api.get(`/graph/concept/${conceptId}`))
}
