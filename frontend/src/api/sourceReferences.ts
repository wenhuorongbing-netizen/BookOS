import api, { unwrap } from './http'
import type { SourceReferenceRecord } from '../types'

export function getSourceReference(id: number | string) {
  return unwrap<SourceReferenceRecord>(api.get(`/source-references/${id}`))
}

export function getBookSourceReferences(bookId: number | string) {
  return unwrap<SourceReferenceRecord[]>(api.get(`/books/${bookId}/source-references`))
}

export function getNoteSourceReferences(noteId: number | string) {
  return unwrap<SourceReferenceRecord[]>(api.get(`/notes/${noteId}/source-references`))
}

export function getCaptureSourceReferences(captureId: number | string) {
  return unwrap<SourceReferenceRecord[]>(api.get(`/captures/${captureId}/source-references`))
}
