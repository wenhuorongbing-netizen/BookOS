import api, { unwrap } from './http'
import type { AddToLibraryPayload, BookPayload, BookRecord, UserBookRecord } from '../types'

export function getBooks(params?: { q?: string; category?: string; tag?: string }) {
  return unwrap<BookRecord[]>(api.get('/books', { params }))
}

export function createBook(payload: BookPayload) {
  return unwrap<BookRecord>(api.post('/books', payload))
}

export function getBook(id: number | string) {
  return unwrap<BookRecord>(api.get(`/books/${id}`))
}

export function updateBook(id: number | string, payload: BookPayload) {
  return unwrap<BookRecord>(api.put(`/books/${id}`, payload))
}

export function deleteBook(id: number | string) {
  return unwrap<void>(api.delete(`/books/${id}`))
}

export function addBookToLibrary(id: number | string, payload?: AddToLibraryPayload) {
  return unwrap<UserBookRecord>(api.post(`/books/${id}/add-to-library`, payload ?? {}))
}
