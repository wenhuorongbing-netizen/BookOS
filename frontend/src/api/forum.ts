import api, { unwrap } from './http'
import type {
  ForumCategoryPayload,
  ForumCategoryRecord,
  ForumCommentPayload,
  ForumCommentRecord,
  ForumReportPayload,
  ForumThreadPayload,
  ForumThreadRecord,
  StructuredPostTemplateRecord,
} from '../types'

export function getForumCategories() {
  return unwrap<ForumCategoryRecord[]>(api.get('/forum/categories'))
}

export function createForumCategory(payload: ForumCategoryPayload) {
  return unwrap<ForumCategoryRecord>(api.post('/forum/categories', payload))
}

export function getForumTemplates() {
  return unwrap<StructuredPostTemplateRecord[]>(api.get('/forum/templates'))
}

export function getForumThreads(params?: { categorySlug?: string; q?: string }) {
  return unwrap<ForumThreadRecord[]>(api.get('/forum/threads', { params }))
}

export function createForumThread(payload: ForumThreadPayload) {
  return unwrap<ForumThreadRecord>(api.post('/forum/threads', payload))
}

export function getForumThread(id: number | string) {
  return unwrap<ForumThreadRecord>(api.get(`/forum/threads/${id}`))
}

export function updateForumThread(id: number | string, payload: ForumThreadPayload) {
  return unwrap<ForumThreadRecord>(api.put(`/forum/threads/${id}`, payload))
}

export function deleteForumThread(id: number | string) {
  return unwrap<void>(api.delete(`/forum/threads/${id}`))
}

export function getForumComments(threadId: number | string) {
  return unwrap<ForumCommentRecord[]>(api.get(`/forum/threads/${threadId}/comments`))
}

export function createForumComment(threadId: number | string, payload: ForumCommentPayload) {
  return unwrap<ForumCommentRecord>(api.post(`/forum/threads/${threadId}/comments`, payload))
}

export function updateForumComment(id: number | string, payload: ForumCommentPayload) {
  return unwrap<ForumCommentRecord>(api.put(`/forum/comments/${id}`, payload))
}

export function deleteForumComment(id: number | string) {
  return unwrap<void>(api.delete(`/forum/comments/${id}`))
}

export function bookmarkForumThread(id: number | string) {
  return unwrap<ForumThreadRecord>(api.post(`/forum/threads/${id}/bookmark`))
}

export function removeForumBookmark(id: number | string) {
  return unwrap<ForumThreadRecord>(api.delete(`/forum/threads/${id}/bookmark`))
}

export function likeForumThread(id: number | string) {
  return unwrap<ForumThreadRecord>(api.post(`/forum/threads/${id}/like`))
}

export function removeForumLike(id: number | string) {
  return unwrap<ForumThreadRecord>(api.delete(`/forum/threads/${id}/like`))
}

export function reportForumThread(id: number | string, payload: ForumReportPayload) {
  return unwrap<void>(api.post(`/forum/threads/${id}/report`, payload))
}
