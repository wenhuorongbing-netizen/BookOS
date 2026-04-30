<template>
  <div class="page-shell project-section-page">
    <AppLoadingState v-if="loading" label="Loading project applications" />
    <AppErrorState v-else-if="errorMessage" title="Project applications could not load" :description="errorMessage" retry-label="Retry" @retry="loadData" />

    <template v-else-if="project">
      <AppSectionHeader :title="`${project.title}: Applications`" eyebrow="Source-backed Applications" description="Turn reading material into concrete design actions." :level="1" />
      <ProjectWorkspaceNav :project-id="project.id" />

      <section class="section-grid">
        <AppCard class="form-card">
          <AppSectionHeader title="Manual Application" eyebrow="Optional source" :level="2" compact />
          <el-form label-position="top" @submit.prevent="createApplication">
            <el-form-item label="Title" required>
              <el-input v-model="form.title" placeholder="Apply source-backed idea..." />
            </el-form-item>
            <el-form-item label="Design note">
              <el-input v-model="form.description" type="textarea" :rows="4" />
            </el-form-item>
            <div class="form-grid">
              <el-form-item label="Application type">
                <el-select v-model="form.applicationType">
                  <el-option label="Mechanic Test" value="MECHANIC_TEST" />
                  <el-option label="Design Reference" value="DESIGN_REFERENCE" />
                  <el-option label="Prototype Task" value="PROTOTYPE_TASK" />
                  <el-option label="Project Application" value="PROJECT_APPLICATION" />
                </el-select>
              </el-form-item>
              <el-form-item label="Status">
                <el-select v-model="form.status">
                  <el-option label="Open" value="OPEN" />
                  <el-option label="In Progress" value="IN_PROGRESS" />
                  <el-option label="Done" value="DONE" />
                </el-select>
              </el-form-item>
            </div>
            <AppButton variant="primary" :loading="saving" native-type="submit">Create Application</AppButton>
          </el-form>
        </AppCard>

        <AppCard class="list-card">
          <AppSectionHeader title="Applications" eyebrow="Preserved sources" :level="2" compact />
          <AppEmptyState v-if="!applications.length" title="No applications yet" description="Use Apply to Project from quotes, concepts, design knowledge, or source links." compact />
          <article v-for="application in applications" v-else :key="application.id" class="record-card">
            <div class="record-card__topline">
              <AppBadge variant="primary">{{ application.applicationType }}</AppBadge>
              <AppBadge variant="neutral">{{ application.status }}</AppBadge>
              <AppBadge v-if="application.sourceEntityType" variant="info">{{ application.sourceEntityType }}</AppBadge>
            </div>
            <h3>{{ application.title }}</h3>
            <p>{{ application.description ?? 'No design note.' }}</p>
            <div class="record-card__actions">
              <AppButton v-if="application.sourceReference" variant="secondary" @click="openSource(application.sourceReference)">Open Source</AppButton>
              <RouterLink
                :to="{
                  name: 'forum-new',
                  query: {
                    relatedEntityType: 'PROJECT_APPLICATION',
                    relatedEntityId: String(application.id),
                    projectId: String(project.id),
                    sourceReferenceId: application.sourceReference?.id ? String(application.sourceReference.id) : undefined,
                    title: `Discuss project application: ${application.title}`,
                  },
                }"
                custom
                v-slot="{ navigate }"
              >
                <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
              </RouterLink>
              <AppButton variant="text" @click="removeApplication(application.id)">Delete</AppButton>
            </div>
          </article>
        </AppCard>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { createProjectApplication, deleteProjectApplication, getProject, getProjectApplications } from '../api/projects'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { GameProjectRecord, ProjectApplicationRecord, SourceReferenceRecord } from '../types'

const route = useRoute()
const { openSource: openSourceDrawer } = useOpenSource()
const project = ref<GameProjectRecord | null>(null)
const applications = ref<ProjectApplicationRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const form = reactive({ title: '', description: '', applicationType: 'PROJECT_APPLICATION', status: 'OPEN' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  const projectId = String(route.params.id)
  try {
    const [projectResult, applicationResult] = await Promise.all([getProject(projectId), getProjectApplications(projectId)])
    project.value = projectResult
    applications.value = applicationResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function createApplication() {
  if (!project.value || !form.title.trim()) return
  saving.value = true
  try {
    await createProjectApplication(project.value.id, {
      title: form.title.trim(),
      description: form.description.trim() || null,
      applicationType: form.applicationType,
      status: form.status,
    })
    form.title = ''
    form.description = ''
    await loadData()
    ElMessage.success('Application created.')
  } catch {
    ElMessage.error('Could not create project application.')
  } finally {
    saving.value = false
  }
}

async function removeApplication(id: number) {
  try {
    await deleteProjectApplication(id)
    applications.value = applications.value.filter((application) => application.id !== id)
    ElMessage.success('Application deleted.')
  } catch {
    ElMessage.error('Could not delete application.')
  }
}

function openSource(source: SourceReferenceRecord) {
  void openSourceDrawer({ sourceType: 'SOURCE_REFERENCE', sourceId: source.id, bookId: source.bookId, pageStart: source.pageStart, sourceReference: source, sourceReferenceId: source.id })
}
</script>

<style scoped>
@import './project-section.css';
</style>
