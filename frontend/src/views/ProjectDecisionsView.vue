<template>
  <div class="page-shell project-section-page">
    <AppLoadingState v-if="loading" label="Loading design decisions" />
    <AppErrorState v-else-if="errorMessage" title="Design decisions could not load" :description="errorMessage" retry-label="Retry" @retry="loadData" />

    <template v-else-if="project">
      <AppSectionHeader :title="`${project.title}: Decisions`" eyebrow="Design Decisions" description="Record choices, rationale, tradeoffs, and source context." :level="1" />
      <ProjectWorkspaceNav :project-id="project.id" />

      <section class="section-grid">
        <AppCard class="form-card">
          <AppSectionHeader title="Create Decision" eyebrow="Rationale" :level="2" compact />
          <el-form label-position="top" @submit.prevent="createDecision">
            <el-form-item label="Title" required>
              <el-input v-model="form.title" placeholder="Decision title" />
            </el-form-item>
            <el-form-item label="Decision" required>
              <el-input v-model="form.decision" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="Rationale">
              <el-input v-model="form.rationale" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="Tradeoffs">
              <el-input v-model="form.tradeoffs" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="Status">
              <el-select v-model="form.status">
                <el-option label="Proposed" value="PROPOSED" />
                <el-option label="Accepted" value="ACCEPTED" />
                <el-option label="Reversed" value="REVERSED" />
              </el-select>
            </el-form-item>
            <AppButton variant="primary" :loading="saving" native-type="submit">Create Decision</AppButton>
          </el-form>
        </AppCard>

        <AppCard class="list-card">
          <AppSectionHeader title="Recent Decisions" eyebrow="Project memory" :level="2" compact />
          <AppEmptyState v-if="!decisions.length" title="No decisions yet" description="Record the first project decision." compact />
          <article v-for="decision in decisions" v-else :key="decision.id" class="record-card">
            <div class="record-card__topline">
              <AppBadge variant="primary">{{ decision.status }}</AppBadge>
            </div>
            <h3>{{ decision.title }}</h3>
            <p>{{ decision.decision }}</p>
            <p v-if="decision.rationale"><strong>Rationale:</strong> {{ decision.rationale }}</p>
            <p v-if="decision.tradeoffs"><strong>Tradeoffs:</strong> {{ decision.tradeoffs }}</p>
            <div class="record-card__actions">
              <AppButton v-if="decision.sourceReference" variant="secondary" @click="openSource(decision.sourceReference)">Open Source</AppButton>
              <RouterLink
                :to="{
                  name: 'forum-new',
                  query: {
                    relatedEntityType: 'DESIGN_DECISION',
                    relatedEntityId: String(decision.id),
                    projectId: String(project.id),
                    sourceReferenceId: decision.sourceReference?.id ? String(decision.sourceReference.id) : undefined,
                    title: `Discuss design decision: ${decision.title}`,
                  },
                }"
                custom
                v-slot="{ navigate }"
              >
                <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
              </RouterLink>
              <AppButton variant="text" @click="removeDecision(decision.id)">Delete</AppButton>
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
import { createDesignDecision, deleteDesignDecision, getProject, getProjectDecisions } from '../api/projects'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { DesignDecisionRecord, GameProjectRecord, SourceReferenceRecord } from '../types'

const route = useRoute()
const { openSource: openSourceDrawer } = useOpenSource()
const project = ref<GameProjectRecord | null>(null)
const decisions = ref<DesignDecisionRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const form = reactive({ title: '', decision: '', rationale: '', tradeoffs: '', status: 'PROPOSED' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  const projectId = String(route.params.id)
  try {
    const [projectResult, decisionResult] = await Promise.all([getProject(projectId), getProjectDecisions(projectId)])
    project.value = projectResult
    decisions.value = decisionResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function createDecision() {
  if (!project.value || !form.title.trim() || !form.decision.trim()) return
  saving.value = true
  try {
    await createDesignDecision(project.value.id, {
      title: form.title.trim(),
      decision: form.decision.trim(),
      rationale: form.rationale.trim() || null,
      tradeoffs: form.tradeoffs.trim() || null,
      status: form.status,
    })
    form.title = ''
    form.decision = ''
    form.rationale = ''
    form.tradeoffs = ''
    await loadData()
    ElMessage.success('Decision created.')
  } catch {
    ElMessage.error('Could not create design decision.')
  } finally {
    saving.value = false
  }
}

async function removeDecision(id: number) {
  try {
    await deleteDesignDecision(id)
    decisions.value = decisions.value.filter((decision) => decision.id !== id)
    ElMessage.success('Decision deleted.')
  } catch {
    ElMessage.error('Could not delete decision.')
  }
}

function openSource(source: SourceReferenceRecord) {
  void openSourceDrawer({ sourceType: 'SOURCE_REFERENCE', sourceId: source.id, bookId: source.bookId, pageStart: source.pageStart, sourceReference: source, sourceReferenceId: source.id })
}
</script>

<style scoped>
@import './project-section.css';
</style>
