import api, { unwrap } from './http'
import type { UseCaseEventPayload, UserUseCaseEventRecord, UserUseCaseProgressRecord } from '../types'

export function getUseCaseProgress() {
  return unwrap<UserUseCaseProgressRecord[]>(api.get('/use-cases/progress'))
}

export function getUseCaseProgressBySlug(slug: string) {
  return unwrap<UserUseCaseProgressRecord>(api.get(`/use-cases/progress/${encodeURIComponent(slug)}`))
}

export function startUseCase(slug: string) {
  return unwrap<UserUseCaseProgressRecord>(api.post(`/use-cases/progress/${encodeURIComponent(slug)}/start`))
}

export function completeUseCaseStep(slug: string, stepKey: string) {
  return unwrap<UserUseCaseProgressRecord>(
    api.put(`/use-cases/progress/${encodeURIComponent(slug)}/steps/${encodeURIComponent(stepKey)}/complete`),
  )
}

export function resetUseCaseProgress(slug: string) {
  return unwrap<UserUseCaseProgressRecord>(api.put(`/use-cases/progress/${encodeURIComponent(slug)}/reset`))
}

export function recordUseCaseEvent(payload: UseCaseEventPayload) {
  return unwrap<UserUseCaseEventRecord>(api.post('/use-cases/progress/events', payload))
}
