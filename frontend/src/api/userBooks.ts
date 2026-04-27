import api, { unwrap } from './http'
import type { ReadingStatus, UserBookRecord } from '../types'

export function getUserBooks() {
  return unwrap<UserBookRecord[]>(api.get('/user-books'))
}

export function updateUserBookStatus(id: number, status: ReadingStatus) {
  return unwrap<UserBookRecord>(api.put(`/user-books/${id}/status`, { status }))
}

export function updateUserBookProgress(id: number, progressPercent: number) {
  return unwrap<UserBookRecord>(api.put(`/user-books/${id}/progress`, { progressPercent }))
}

export function updateUserBookRating(id: number, rating: number) {
  return unwrap<UserBookRecord>(api.put(`/user-books/${id}/rating`, { rating }))
}

export function getCurrentlyReading() {
  return unwrap<UserBookRecord[]>(api.get('/user-books/currently-reading'))
}

export function getFiveStarBooks() {
  return unwrap<UserBookRecord[]>(api.get('/user-books/five-star'))
}

export function getAntiLibraryBooks() {
  return unwrap<UserBookRecord[]>(api.get('/user-books/anti-library'))
}
