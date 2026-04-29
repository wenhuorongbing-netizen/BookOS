<template>
  <div class="page-shell project-section-page">
    <AppLoadingState v-if="loading" label="Loading project problems" />
    <AppErrorState v-else-if="errorMessage" title="Project problems could not load" :description="errorMessage" retry-label="Retry" @retry="loadData" />

    <template v-else-if="project">
      <AppSectionHeader :title="`${project.title}: Problems`" eyebrow="Project Problems" description="Track the design problems this project needs to solve." :level="1" />
      <ProjectWorkspaceNav :project-id="project.id" />

      <section class="section-grid">
        <AppCard class="form-card">
          <AppSectionHeader title="Create Problem" eyebrow="Design question" :level="2" compact />
          <el-form label-position="top" @submit.prevent="createProblem">
            <el-form-item label="Title" required>
              <el-input v-model="form.title" placeholder="What is blocking the design?" />
            </el-form-item>
            <el-form-item label="Description">
              <el-input v-model="form.description" type="textarea" :rows="4" />
            </el-form-item>
            <div class="form-grid">
              <el-form-item label="Status">
                <el-select v-model="form.status">
                  <el-option label="Open" value="OPEN" />
                  <el-option label="Investigating" value="INVESTIGATING" />
                  <el-option label="Resolved" value="RESOLVED" />
                </el-select>
              </el-form-item>
              <el-form-item label="Priority">
                <el-select v-model="form.priority">
                  <el-option label="Low" value="LOW" />
                  <el-option label="Medium" value="MEDIUM" />
                  <el-option label="High" value="HIGH" />
                </el-select>
              </el-form-item>
            </div>
            <AppButton variant="primary" :loading="saving" native-type="submit">Create Problem</AppButton>
          </el-form>
        </AppCard>

        <AppCard class="list-card">
          <AppSectionHeader title="Current Problems" eyebrow="Real project data" :level="2" compact />
          <AppEmptyState v-if="!problems.length" title="No problems yet" description="Create the first design problem for this project." compact />
          <article v-for="problem in problems" v-else :key="problem.id" class="record-card">
            <div class="record-card__topline">
              <AppBadge variant="primary">{{ problem.status }}</AppBadge>
              <AppBadge variant="warning">{{ problem.priority }}</AppBadge>
            </div>
            <h3>{{ problem.title }}</h3>
            <p>{{ problem.description ?? 'No description.' }}</p>
            <div class="record-card__actions">
              <AppButton v-if="problem.relatedSourceReference" variant="secondary" @click="openSource(problem.relatedSourceReference)">Open Source</AppButton>
              <RouterLink
                :to="{
                  name: 'forum-new',
                  query: {
                    relatedEntityType: 'PROJECT_PROBLEM',
                    relatedEntityId: String(problem.id),
                    projectId: String(project.id),
                    sourceReferenceId: problem.relatedSourceReference?.id ? String(problem.relatedSourceReference.id) : undefined,
                    title: `Discuss project problem: ${problem.title}`,
                  },
                }"
                custom
                v-slot="{ navigate }"
              >
                <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
              </RouterLink>
              <AppButton variant="text" @click="removeProblem(problem.id)">Delete</AppButton>
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
import { createProjectProblem, deleteProjectProblem, getProject, getProjectProblems } from '../api/projects'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { GameProjectRecord, ProjectProblemRecord, SourceReferenceRecord } from '../types'

const route = useRoute()
const { openSource: openSourceDrawer } = useOpenSource()
const project = ref<GameProjectRecord | null>(null)
const problems = ref<ProjectProblemRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const form = reactive({ title: '', description: '', status: 'OPEN', priority: 'MEDIUM' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  const projectId = String(route.params.id)
  try {
    const [projectResult, problemsResult] = await Promise.all([getProject(projectId), getProjectProblems(projectId)])
    project.value = projectResult
    problems.value = problemsResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function createProblem() {
  if (!project.value || !form.title.trim()) return
  saving.value = true
  try {
    await createProjectProblem(project.value.id, {
      title: form.title.trim(),
      description: form.description.trim() || null,
      status: form.status,
      priority: form.priority,
    })
    form.title = ''
    form.description = ''
    await loadData()
    ElMessage.success('Problem created.')
  } catch {
    ElMessage.error('Could not create project problem.')
  } finally {
    saving.value = false
  }
}

async function removeProblem(id: number) {
  try {
    await deleteProjectProblem(id)
    problems.value = problems.value.filter((problem) => problem.id !== id)
    ElMessage.success('Problem deleted.')
  } catch {
    ElMessage.error('Could not delete problem.')
  }
}

function openSource(source: SourceReferenceRecord) {
  void openSourceDrawer({ sourceType: 'SOURCE_REFERENCE', sourceId: source.id, bookId: source.bookId, pageStart: source.pageStart, sourceReference: source, sourceReferenceId: source.id })
}
</script>

<style scoped>
.project-section-page,
.section-grid,
.form-card,
.list-card,
.record-card {
  display: grid;
  gap: var(--space-5);
}

.section-grid {
  grid-template-columns: minmax(280px, 0.42fr) minmax(0, 1fr);
  align-items: start;
}

.form-card,
.list-card {
  padding: var(--space-5);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.record-card {
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.record-card__topline,
.record-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.record-card h3,
.record-card p {
  margin: 0;
}

.record-card p {
  color: var(--bookos-text-secondary);
}

@media (max-width: 980px) {
  .section-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
