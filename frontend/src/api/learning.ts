import api, { unwrap } from './http'
import type {
  BookAnalyticsRecord,
  KnowledgeAnalyticsRecord,
  KnowledgeMasteryPayload,
  KnowledgeMasteryRecord,
  ReadingAnalyticsRecord,
  ReadingSessionFinishPayload,
  ReadingSessionRecord,
  ReadingSessionStartPayload,
  ReviewGeneratePayload,
  ReviewItemPayload,
  ReviewItemRecord,
  ReviewItemUpdatePayload,
  ReviewSessionPayload,
  ReviewSessionRecord,
} from '../types'

export function getReadingSessions() {
  return unwrap<ReadingSessionRecord[]>(api.get('/reading-sessions'))
}

export function startReadingSession(payload: ReadingSessionStartPayload) {
  return unwrap<ReadingSessionRecord>(api.post('/reading-sessions/start', payload))
}

export function finishReadingSession(id: number | string, payload: ReadingSessionFinishPayload) {
  return unwrap<ReadingSessionRecord>(api.put(`/reading-sessions/${id}/finish`, payload))
}

export function getBookReadingSessions(bookId: number | string) {
  return unwrap<ReadingSessionRecord[]>(api.get(`/books/${bookId}/reading-sessions`))
}

export function getReviewSessions() {
  return unwrap<ReviewSessionRecord[]>(api.get('/review/sessions'))
}

export function createReviewSession(payload: ReviewSessionPayload) {
  return unwrap<ReviewSessionRecord>(api.post('/review/sessions', payload))
}

export function getReviewSession(id: number | string) {
  return unwrap<ReviewSessionRecord>(api.get(`/review/sessions/${id}`))
}

export function addReviewItem(sessionId: number | string, payload: ReviewItemPayload) {
  return unwrap<ReviewItemRecord>(api.post(`/review/sessions/${sessionId}/items`, payload))
}

export function updateReviewItem(id: number | string, payload: ReviewItemUpdatePayload) {
  return unwrap<ReviewItemRecord>(api.put(`/review/items/${id}`, payload))
}

export function generateReviewFromBook(payload: ReviewGeneratePayload) {
  return unwrap<ReviewSessionRecord>(api.post('/review/generate-from-book', payload))
}

export function generateReviewFromConcept(payload: ReviewGeneratePayload) {
  return unwrap<ReviewSessionRecord>(api.post('/review/generate-from-concept', payload))
}

export function generateReviewFromProject(payload: ReviewGeneratePayload) {
  return unwrap<ReviewSessionRecord>(api.post('/review/generate-from-project', payload))
}

export function getMastery() {
  return unwrap<KnowledgeMasteryRecord[]>(api.get('/mastery'))
}

export function getMasteryTarget(targetType: string, targetId: number | string) {
  return unwrap<KnowledgeMasteryRecord>(api.get('/mastery/target', { params: { targetType, targetId } }))
}

export function updateMasteryTarget(payload: KnowledgeMasteryPayload) {
  return unwrap<KnowledgeMasteryRecord>(api.put('/mastery/target', payload))
}

export function getReadingAnalytics() {
  return unwrap<ReadingAnalyticsRecord>(api.get('/analytics/reading'))
}

export function getKnowledgeAnalytics() {
  return unwrap<KnowledgeAnalyticsRecord>(api.get('/analytics/knowledge'))
}

export function getBookAnalytics(bookId: number | string) {
  return unwrap<BookAnalyticsRecord>(api.get(`/analytics/books/${bookId}`))
}
