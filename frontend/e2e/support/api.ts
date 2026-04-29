import { expect, type APIRequestContext, type Page } from '@playwright/test'

export const apiBaseUrl = process.env.E2E_API_BASE_URL ?? 'http://127.0.0.1:18080/api'

export interface AuthPayload {
  token: string
  user: {
    id: number
    email: string
    displayName: string
    role: 'ADMIN' | 'MODERATOR' | 'USER'
  }
}

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export async function apiGet<T>(request: APIRequestContext, path: string, token: string) {
  return apiFetch<T>(request, 'GET', path, token)
}

export async function apiPost<T>(request: APIRequestContext, path: string, token: string | null, data?: unknown) {
  return apiFetch<T>(request, 'POST', path, token, data)
}

export async function apiPut<T>(request: APIRequestContext, path: string, token: string, data?: unknown) {
  return apiFetch<T>(request, 'PUT', path, token, data)
}

async function apiFetch<T>(
  request: APIRequestContext,
  method: 'GET' | 'POST' | 'PUT',
  path: string,
  token: string | null,
  data?: unknown,
) {
  const response = await request.fetch(`${apiBaseUrl}${path}`, {
    method,
    data,
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  })
  const body = await response.text()
  expect(response.ok(), `${method} ${path} failed with ${response.status()}: ${body}`).toBe(true)
  if (!body) return undefined as T
  return (JSON.parse(body) as ApiResponse<T>).data
}

export async function login(request: APIRequestContext, email: string, password: string) {
  return apiPost<AuthPayload>(request, '/auth/login', null, { email, password })
}

export async function setSession(page: Page, auth: AuthPayload) {
  await page.addInitScript((payload) => {
    window.localStorage.setItem('bookos.token', payload.token)
    window.localStorage.setItem('bookos.user', JSON.stringify(payload.user))
  }, auth)
}

export async function tokenFromPage(page: Page) {
  const token = await page.evaluate(() => window.localStorage.getItem('bookos.token'))
  expect(token, 'Expected the browser session to contain a JWT token.').toBeTruthy()
  return token as string
}

export function uniqueRunId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}
