import api, { unwrap } from './http'
import type { BookNotePayload, BookNoteRecord, NoteBlockPayload, NoteBlockRecord } from '../types'

export function getBookNotes(bookId: number | string) {
  return unwrap<BookNoteRecord[]>(api.get(`/books/${bookId}/notes`))
}

export function createBookNote(bookId: number | string, payload: BookNotePayload) {
  return unwrap<BookNoteRecord>(api.post(`/books/${bookId}/notes`, payload))
}

export function getNote(id: number | string) {
  return unwrap<BookNoteRecord>(api.get(`/notes/${id}`))
}

export function updateNote(id: number | string, payload: BookNotePayload) {
  return unwrap<BookNoteRecord>(api.put(`/notes/${id}`, payload))
}

export function archiveNote(id: number | string) {
  return unwrap<void>(api.delete(`/notes/${id}`))
}

export function addNoteBlock(noteId: number | string, payload: NoteBlockPayload) {
  return unwrap<NoteBlockRecord>(api.post(`/notes/${noteId}/blocks`, payload))
}

export function updateNoteBlock(id: number | string, payload: NoteBlockPayload) {
  return unwrap<NoteBlockRecord>(api.put(`/note-blocks/${id}`, payload))
}

export function deleteNoteBlock(id: number | string) {
  return unwrap<void>(api.delete(`/note-blocks/${id}`))
}
