import api, { unwrap } from './http'
import type {
  DailyHistoryRecord,
  DailyPrototypeTaskPayload,
  DailyReflectionPayload,
  DailyReflectionRecord,
  DailyTarget,
  DailyTodayRecord,
  KnowledgeObjectRecord,
} from '../types'

export function getDailyToday() {
  return unwrap<DailyTodayRecord>(api.get('/daily/today'))
}

export function regenerateDaily(target: DailyTarget) {
  return unwrap<DailyTodayRecord>(api.post('/daily/regenerate', { target }))
}

export function skipDaily(target: DailyTarget) {
  return unwrap<DailyTodayRecord>(api.post('/daily/skip', { target }))
}

export function saveDailyReflection(payload: DailyReflectionPayload) {
  return unwrap<DailyReflectionRecord>(api.post('/daily/reflections', payload))
}

export function getDailyHistory() {
  return unwrap<DailyHistoryRecord[]>(api.get('/daily/history'))
}

export function createPrototypeTaskFromDaily(payload: DailyPrototypeTaskPayload) {
  return unwrap<KnowledgeObjectRecord>(api.post('/daily/create-prototype-task', payload))
}
