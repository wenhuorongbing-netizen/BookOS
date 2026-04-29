<template>
  <div class="page-shell project-section-page">
    <AppLoadingState v-if="loading" label="Loading playtest workspace" />
    <AppErrorState v-else-if="errorMessage" title="Playtest workspace could not load" :description="errorMessage" retry-label="Retry" @retry="loadData" />

    <template v-else-if="project">
      <AppSectionHeader :title="`${project.title}: Playtests`" eyebrow="Playtest MVP" description="Plan simple tests and capture findings without analytics overbuild." :level="1" />
      <ProjectWorkspaceNav :project-id="project.id" />

      <section class="section-grid">
        <AppCard class="form-card">
          <AppSectionHeader title="Create Plan" eyebrow="Hypothesis" :level="2" compact />
          <el-form label-position="top" @submit.prevent="createPlan">
            <el-form-item label="Title" required>
              <el-input v-model="planForm.title" />
            </el-form-item>
            <el-form-item label="Hypothesis">
              <el-input v-model="planForm.hypothesis" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="Target players">
              <el-input v-model="planForm.targetPlayers" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="Tasks">
              <el-input v-model="planForm.tasks" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="Success criteria">
              <el-input v-model="planForm.successCriteria" type="textarea" :rows="3" />
            </el-form-item>
            <AppButton variant="primary" :loading="savingPlan" native-type="submit">Create Plan</AppButton>
          </el-form>
        </AppCard>

        <div class="stack">
          <AppCard class="list-card">
            <AppSectionHeader title="Plans" eyebrow="Test setup" :level="2" compact />
            <AppEmptyState v-if="!plans.length" title="No playtest plans" description="Create a minimal test plan first." compact />
            <article v-for="plan in plans" v-else :key="plan.id" class="record-card">
              <div class="record-card__topline">
                <AppBadge variant="primary">{{ plan.status }}</AppBadge>
              </div>
              <h3>{{ plan.title }}</h3>
              <p>{{ plan.hypothesis ?? 'No hypothesis.' }}</p>
            </article>
          </AppCard>

          <AppCard class="list-card">
            <AppSectionHeader title="Create Finding" eyebrow="Observation" :level="2" compact />
            <el-form label-position="top" @submit.prevent="createFinding">
              <el-form-item label="Title" required>
                <el-input v-model="findingForm.title" />
              </el-form-item>
              <el-form-item label="Observation">
                <el-input v-model="findingForm.observation" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="Recommendation">
                <el-input v-model="findingForm.recommendation" type="textarea" :rows="3" />
              </el-form-item>
              <div class="form-grid">
                <el-form-item label="Severity">
                  <el-select v-model="findingForm.severity">
                    <el-option label="Low" value="LOW" />
                    <el-option label="Medium" value="MEDIUM" />
                    <el-option label="High" value="HIGH" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Status">
                  <el-select v-model="findingForm.status">
                    <el-option label="Open" value="OPEN" />
                    <el-option label="Resolved" value="RESOLVED" />
                  </el-select>
                </el-form-item>
              </div>
              <AppButton variant="primary" :loading="savingFinding" native-type="submit">Create Finding</AppButton>
            </el-form>
          </AppCard>

          <AppCard class="list-card">
            <AppSectionHeader title="Findings" eyebrow="Evidence" :level="2" compact />
            <AppEmptyState v-if="!findings.length" title="No findings yet" description="Capture findings after a playtest." compact />
            <article v-for="finding in findings" v-else :key="finding.id" class="record-card">
              <div class="record-card__topline">
                <AppBadge variant="warning">{{ finding.severity }}</AppBadge>
                <AppBadge variant="primary">{{ finding.status }}</AppBadge>
              </div>
              <h3>{{ finding.title }}</h3>
              <p>{{ finding.observation ?? 'No observation.' }}</p>
              <p v-if="finding.recommendation"><strong>Recommendation:</strong> {{ finding.recommendation }}</p>
              <div class="record-card__actions">
                <AppButton v-if="finding.sourceReference" variant="secondary" @click="openSource(finding.sourceReference)">Open Source</AppButton>
                <RouterLink
                  :to="{
                    name: 'forum-new',
                    query: {
                      relatedEntityType: 'PLAYTEST_FINDING',
                      relatedEntityId: String(finding.id),
                      projectId: String(project.id),
                      sourceReferenceId: finding.sourceReference?.id ? String(finding.sourceReference.id) : undefined,
                      title: `Discuss playtest finding: ${finding.title}`,
                    },
                  }"
                  custom
                  v-slot="{ navigate }"
                >
                  <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
                </RouterLink>
                <AppButton variant="text" @click="removeFinding(finding.id)">Delete</AppButton>
              </div>
            </article>
          </AppCard>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { createPlaytestFinding, createPlaytestPlan, deletePlaytestFinding, getPlaytestFindings, getPlaytestPlans, getProject } from '../api/projects'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { GameProjectRecord, PlaytestFindingRecord, PlaytestPlanRecord, SourceReferenceRecord } from '../types'

const route = useRoute()
const { openSource: openSourceDrawer } = useOpenSource()
const project = ref<GameProjectRecord | null>(null)
const plans = ref<PlaytestPlanRecord[]>([])
const findings = ref<PlaytestFindingRecord[]>([])
const loading = ref(false)
const savingPlan = ref(false)
const savingFinding = ref(false)
const errorMessage = ref('')
const planForm = reactive({ title: '', hypothesis: '', targetPlayers: '', tasks: '', successCriteria: '', status: 'DRAFT' })
const findingForm = reactive({ title: '', observation: '', recommendation: '', severity: 'MEDIUM', status: 'OPEN' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  const projectId = String(route.params.id)
  try {
    const [projectResult, planResult, findingResult] = await Promise.all([getProject(projectId), getPlaytestPlans(projectId), getPlaytestFindings(projectId)])
    project.value = projectResult
    plans.value = planResult
    findings.value = findingResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function createPlan() {
  if (!project.value || !planForm.title.trim()) return
  savingPlan.value = true
  try {
    await createPlaytestPlan(project.value.id, {
      title: planForm.title.trim(),
      hypothesis: planForm.hypothesis.trim() || null,
      targetPlayers: planForm.targetPlayers.trim() || null,
      tasks: planForm.tasks.trim() || null,
      successCriteria: planForm.successCriteria.trim() || null,
      status: planForm.status,
    })
    planForm.title = ''
    planForm.hypothesis = ''
    planForm.targetPlayers = ''
    planForm.tasks = ''
    planForm.successCriteria = ''
    await loadData()
    ElMessage.success('Playtest plan created.')
  } catch {
    ElMessage.error('Could not create playtest plan.')
  } finally {
    savingPlan.value = false
  }
}

async function createFinding() {
  if (!project.value || !findingForm.title.trim()) return
  savingFinding.value = true
  try {
    await createPlaytestFinding(project.value.id, {
      title: findingForm.title.trim(),
      observation: findingForm.observation.trim() || null,
      recommendation: findingForm.recommendation.trim() || null,
      severity: findingForm.severity,
      status: findingForm.status,
    })
    findingForm.title = ''
    findingForm.observation = ''
    findingForm.recommendation = ''
    await loadData()
    ElMessage.success('Playtest finding created.')
  } catch {
    ElMessage.error('Could not create playtest finding.')
  } finally {
    savingFinding.value = false
  }
}

async function removeFinding(id: number) {
  try {
    await deletePlaytestFinding(id)
    findings.value = findings.value.filter((finding) => finding.id !== id)
    ElMessage.success('Finding deleted.')
  } catch {
    ElMessage.error('Could not delete finding.')
  }
}

function openSource(source: SourceReferenceRecord) {
  void openSourceDrawer({ sourceType: 'SOURCE_REFERENCE', sourceId: source.id, bookId: source.bookId, pageStart: source.pageStart, sourceReference: source, sourceReferenceId: source.id })
}
</script>

<style scoped>
@import './project-section.css';

.stack {
  display: grid;
  gap: var(--space-4);
}
</style>
