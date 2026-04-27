<template>
  <div class="auth-view">
    <div>
      <div class="eyebrow">Sign In</div>
      <h2>Return to your reading system</h2>
      <p class="muted">Use the seeded account or your own registered account.</p>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" />
    <el-form label-position="top">
      <el-form-item label="Email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="Password">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <div class="auth-view__actions">
        <el-button type="primary" :loading="loading" @click="handleLogin">Login</el-button>
        <RouterLink to="/register" class="muted">Create an account</RouterLink>
      </div>
    </el-form>
    <div class="surface-card surface-card--muted auth-view__seed">
      <strong>Seed account</strong>
      <div class="muted">designer@bookos.local / Password123!</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = reactive({
  email: 'designer@bookos.local',
  password: 'Password123!',
})

function resolveRedirectTarget() {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string') {
    return null
  }

  if (!redirect.startsWith('/') || redirect.startsWith('//')) {
    return null
  }

  return redirect
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(form)
    await router.push(resolveRedirectTarget() ?? { name: 'dashboard' })
  } catch {
    error.value = 'Login failed. Check your email and password.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-view {
  display: grid;
  gap: 1rem;
}

.auth-view h2 {
  margin: 0.35rem 0 0;
  font-size: 2rem;
}

.auth-view__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.auth-view__seed {
  padding: 1rem;
}
</style>
