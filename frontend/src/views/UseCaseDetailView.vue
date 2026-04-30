<template>
  <div class="page-shell">
    <RouterLink to="/use-cases" custom v-slot="{ navigate }">
      <AppButton class="use-case-detail-view__back" variant="text" @click="navigate">Back to use cases</AppButton>
    </RouterLink>

    <UseCaseDetail
      v-if="useCase"
      :use-case="useCase"
      :progress="progress"
      :loading="loadingProgress"
      :mutating="mutatingProgress"
      :error-message="progressError"
      @start="startChecklist"
      @reset="resetChecklist"
      @refresh="loadProgress"
      @complete-step="completeStep"
    />

    <AppErrorState
      v-else
      title="Use case not found"
      description="This scenario does not exist. Open the use-case library to choose a supported workflow."
    >
      <template #actions>
        <RouterLink to="/use-cases" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Open use-case library</AppButton>
        </RouterLink>
      </template>
    </AppErrorState>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute } from 'vue-router'
import { completeUseCaseStep, getUseCaseProgressBySlug, resetUseCaseProgress, startUseCase } from '../api/useCaseProgress'
import AppButton from '../components/ui/AppButton.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import UseCaseDetail from '../components/use-case/UseCaseDetail.vue'
import { findUseCase } from '../data/useCases'
import type { UserUseCaseProgressRecord } from '../types'

const route = useRoute()
const progress = ref<UserUseCaseProgressRecord | null>(null)
const loadingProgress = ref(false)
const mutatingProgress = ref(false)
const progressError = ref('')

const useCase = computed(() => findUseCase(String(route.params.slug ?? '')))
const slug = computed(() => useCase.value?.slug ?? '')

watch(slug, () => {
  void loadProgress()
}, { immediate: true })

async function loadProgress() {
  if (!slug.value) return
  loadingProgress.value = true
  progressError.value = ''
  try {
    progress.value = await getUseCaseProgressBySlug(slug.value)
  } catch {
    progressError.value = 'Progress could not be loaded. The guide still works, but checklist state may not persist.'
  } finally {
    loadingProgress.value = false
  }
}

async function startChecklist() {
  if (!slug.value) return
  mutatingProgress.value = true
  progressError.value = ''
  try {
    progress.value = await startUseCase(slug.value)
    ElMessage.success('Use case checklist started.')
  } catch {
    progressError.value = 'Checklist could not be started.'
  } finally {
    mutatingProgress.value = false
  }
}

async function completeStep(stepKey: string) {
  if (!slug.value) return
  mutatingProgress.value = true
  progressError.value = ''
  try {
    progress.value = await completeUseCaseStep(slug.value, stepKey)
    ElMessage.success('Step marked complete.')
  } catch {
    progressError.value = 'Step could not be marked complete.'
  } finally {
    mutatingProgress.value = false
  }
}

async function resetChecklist() {
  if (!slug.value) return
  mutatingProgress.value = true
  progressError.value = ''
  try {
    progress.value = await resetUseCaseProgress(slug.value)
    ElMessage.success('Use case progress reset.')
  } catch {
    progressError.value = 'Checklist could not be reset.'
  } finally {
    mutatingProgress.value = false
  }
}
</script>

<style scoped>
.use-case-detail-view__back {
  justify-self: start;
}
</style>
