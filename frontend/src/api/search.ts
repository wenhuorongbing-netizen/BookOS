import api, { unwrap } from './http'
import type { SearchResultRecord, SearchResultType } from '../types'

export function searchBookOS(params: { q?: string; type?: SearchResultType | ''; bookId?: number | string | null }) {
  return unwrap<SearchResultRecord[]>(api.get('/search', { params }))
}
