import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi, register as registerApi } from '../api/auth'
import { useCaptureStore } from './capture'
import { useRightRailStore } from './rightRail'
import type { AuthPayload, AuthUser, LoginPayload, RegisterPayload } from '../types'

const TOKEN_KEY = 'bookos.token'
const USER_KEY = 'bookos.user'

function persistSession(payload: AuthPayload) {
  window.localStorage.setItem(TOKEN_KEY, payload.token)
  window.localStorage.setItem(USER_KEY, JSON.stringify(payload.user))
}

function clearPersistedSession() {
  window.localStorage.removeItem(TOKEN_KEY)
  window.localStorage.removeItem(USER_KEY)
}

function clearPrivateSessionState() {
  useCaptureStore().resetPrivateState()
  useRightRailStore().resetPrivateState()
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(window.localStorage.getItem(TOKEN_KEY))
  const user = ref<AuthUser | null>(
    window.localStorage.getItem(USER_KEY)
      ? (JSON.parse(window.localStorage.getItem(USER_KEY) as string) as AuthUser)
      : null,
  )
  const hydrating = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value && user.value))

  function setSession(payload: AuthPayload) {
    token.value = payload.token
    user.value = payload.user
    persistSession(payload)
  }

  async function login(payload: LoginPayload) {
    const result = await loginApi(payload)
    setSession(result)
    return result
  }

  async function register(payload: RegisterPayload) {
    const result = await registerApi(payload)
    setSession(result)
    return result
  }

  async function hydrate() {
    if (!token.value || hydrating.value) return
    hydrating.value = true
    try {
      user.value = await getCurrentUser()
      window.localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    } catch {
      logout()
    } finally {
      hydrating.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    clearPersistedSession()
    clearPrivateSessionState()
  }

  return {
    token,
    user,
    hydrating,
    isAuthenticated,
    login,
    register,
    hydrate,
    logout,
  }
})
