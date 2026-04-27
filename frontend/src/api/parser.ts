import api, { unwrap } from './http'
import type { ParsedNoteResult, ParserPreviewPayload } from '../types'

export function previewParsedNote(payload: ParserPreviewPayload) {
  return unwrap<ParsedNoteResult>(api.post('/parser/preview', payload))
}
