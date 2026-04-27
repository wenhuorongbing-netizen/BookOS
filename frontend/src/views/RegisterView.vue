<template>
  <div class="auth-view">
    <div>
      <div class="eyebrow">Register</div>
      <h2>Create your BookOS account</h2>
      <p class="muted">This milestone stores auth locally in the app backend with JWT.</p>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" />
    <el-form label-position="top">
      <el-form-item label="Display Name">
        <el-input v-model="form.displayName" />
      </el-form-item>
      <el-form-item label="Email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="Password">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <div class="auth-view__actions">
        <el-button type="primary" :loading="loading" @click="handleRegister">Register</el-button>
        <RouterLink to="/login" class="muted">Already have an account?</RouterLink>
      </div>
    </el-form>
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
  displayName: '',
  email: '',
  password: '',
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

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(form)
    await router.push(resolveRedirectTarget() ?? { name: 'dashboard' })
  } catch {
    error.value = 'Registration failed. Make sure the email is new and the password is at least 8 characters.'
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
</style>
