import api, { unwrap } from './http'
import type { AuthPayload, AuthUser, LoginPayload, RegisterPayload } from '../types'

export function register(payload: RegisterPayload) {
  return unwrap<AuthPayload>(api.post('/auth/register', payload))
}

export function login(payload: LoginPayload) {
  return unwrap<AuthPayload>(api.post('/auth/login', payload))
}

export function getCurrentUser() {
  return unwrap<AuthUser>(api.get('/auth/me'))
}
