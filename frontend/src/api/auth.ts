import api, { unwrap } from './http'
import type { AuthPayload, AuthUser, LoginPayload, OnboardingPreferencePayload, RegisterPayload } from '../types'

export function register(payload: RegisterPayload) {
  return unwrap<AuthPayload>(api.post('/auth/register', payload))
}

export function login(payload: LoginPayload) {
  return unwrap<AuthPayload>(api.post('/auth/login', payload))
}

export function getCurrentUser() {
  return unwrap<AuthUser>(api.get('/auth/me'))
}

export function updateOnboardingPreferences(payload: OnboardingPreferencePayload) {
  return unwrap<AuthUser>(api.put('/users/me/onboarding', payload))
}
