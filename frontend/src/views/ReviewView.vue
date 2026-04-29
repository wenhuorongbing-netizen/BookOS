<template>
  <div class="page-shell review-page">
    <AppSectionHeader
      eyebrow="Review Sessions"
      title="Source-backed review"
      description="Create lightweight review sessions from books, concepts, or projects. No fake review items are generated."
      :level="1"
    />

    <section class="review-grid">
      <AppCard class="review-form" as="section">
        <AppSectionHeader title="Create Session" eyebrow="Manual or generated" compact />
        <el-form label-position="top" @submit.prevent="createManualSession">
          <el-form-item label="Title" required>
            <el-input v-model="form.title" placeholder="Weekly source review" />
          </el-form-item>
          <div class="form-grid">
            <el-form-item label="Scope type">
              <el-select v-model="form.scopeType">
                <el-option label="General" value="GENERAL" />
                <el-option label="Book" value="BOOK" />
                <el-option label="Concept" value="CONCEPT" />
                <el-option label="Project" value="PROJECT" />
              </el-select>
            </el-form-item>
            <el-form-item label="Scope ID">
              <el-input-number v-model="form.scopeId" :min="1" controls-position="right" />
            </el-form-item>
          </div>
          <AppButton variant="primary" :loading="saving" native-type="submit">Create Empty Session</AppButton>
        </el-form>

        <AppDivider />

        <el-form label-position="top" @submit.prevent="generateSession">
          <div class="form-grid">
            <el-form-item label="Generate from">
              <el-select v-model="generateForm.scopeType">
                <el-option label="Book" value="BOOK" />
                <el-option label="Concept" value="CONCEPT" />
                <el-option label="Project" value="PROJECT" />
              </el-select>
            </el-form-item>
            <el-form-item label="ID" required>
              <el-input-number v-model="generateForm.id" :min="1" controls-position="right" />
            </el-form-item>
          </div>
          <el-form-item label="Title">
            <el-input v-model="generateForm.title" placeholder="Optional generated session title" />
          </el-form-item>
          <AppButton variant="accent" :loading="saving" native-type="submit">Generate from Real Records</AppButton>
        </el-form>
      </AppCard>

      <AppCard class="review-list" as="section">
        <AppSectionHeader title="Sessions" eyebrow="Latest first" compact />
        <AppLoadingState v-if="loading" label="Loading review sessions" compact />
        <AppErrorState v-else-if="errorMessage" title="Reviews unavailable" :description="errorMessage" retry-label="Retry" @retry="loadSessions" />
        <AppEmptyState v-else-if="!sessions.length" title="No review sessions yet" description="Create a session or generate one from source-backed book/concept/project records." compact />
        <div v-else class="session-list">
          <RouterLink v-for="session in sessions" :key="session.id" :to="{ name: 'review-detail', params: { id: session.id } }" class="session-card">
            <div>
              <AppBadge :variant="session.completedAt ? 'success' : 'warning'" size="sm">{{ session.completedAt ? 'Completed' : 'Open' }}</AppBadge>
              <h3>{{ session.title }}</h3>
              <p>{{ session.scopeType }} {{ session.scopeId ? `#${session.scopeId}` : '' }}</p>
            </div>
            <strong>{{ session.completedItemCount }}/{{ session.itemCount }}</strong>
          </RouterLink>
        </div>
      </AppCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRouter } from 'vue-router'
import { createReviewSession, generateReviewFromBook, generateReviewFromConcept, generateReviewFromProject, getReviewSessions } from '../api/learning'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppDivider from '../components/ui/AppDivider.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type { ReviewSessionRecord } from '../types'

const router = useRouter()
const sessions = ref<ReviewSessionRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const form = reactive({ title: '', scopeType: 'GENERAL', scopeId: null as number | null })
const generateForm = reactive({ scopeType: 'BOOK', id: null as number | null, title: '' })

onMounted(loadSessions)

async function loadSessions() {
  loading.value = true
  errorMessage.value = ''
  try {
    sessions.value = await getReviewSessions()
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function createManualSession() {
  if (!form.title.trim()) {
    ElMessage.warning('Session title is required.')
    return
  }
  saving.value = true
  try {
    const session = await createReviewSession({
      title: form.title.trim(),
      scopeType: form.scopeType,
      scopeId: form.scopeId,
      mode: 'SOURCE_REVIEW',
    })
    await router.push({ name: 'review-detail', params: { id: session.id } })
  } finally {
    saving.value = false
  }
}

async function generateSession() {
  if (!generateForm.id) {
    ElMessage.warning('A source ID is required.')
    return
  }
  saving.value = true
  try {
    const payload = { id: generateForm.id, title: generateForm.title.trim() || null, mode: 'SOURCE_REVIEW', limit: 8 }
    const session =
      generateForm.scopeType === 'CONCEPT'
        ? await generateReviewFromConcept(payload)
        : generateForm.scopeType === 'PROJECT'
          ? await generateReviewFromProject(payload)
          : await generateReviewFromBook(payload)
    await router.push({ name: 'review-detail', params: { id: session.id } })
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.review-page,
.review-form,
.review-list,
.session-list {
  display: grid;
  gap: var(--space-5);
}

.review-grid {
  display: grid;
  grid-template-columns: minmax(300px, 0.42fr) minmax(0, 1fr);
  gap: var(--space-4);
  align-items: start;
}

.review-form,
.review-list {
  padding: var(--space-5);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.session-card {
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
  color: inherit;
  text-decoration: none;
}

.session-card h3,
.session-card p {
  margin: var(--space-2) 0 0;
}

.session-card p {
  color: var(--bookos-text-secondary);
}

@media (max-width: 960px) {
  .review-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
