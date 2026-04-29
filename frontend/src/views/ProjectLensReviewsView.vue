<template>
  <div class="page-shell project-section-page">
    <AppLoadingState v-if="loading" label="Loading lens reviews" />
    <AppErrorState v-else-if="errorMessage" title="Lens reviews could not load" :description="errorMessage" retry-label="Retry" @retry="loadData" />

    <template v-else-if="project">
      <AppSectionHeader
        :title="`${project.title}: Lens Reviews`"
        eyebrow="Design Lens Application"
        description="Review the project through source-backed lenses and diagnostic questions."
        :level="1"
      />
      <ProjectWorkspaceNav :project-id="project.id" />

      <section class="section-grid">
        <AppCard class="form-card">
          <AppSectionHeader title="Create Review" eyebrow="Lens or diagnostic" :level="2" compact />
          <el-form label-position="top" @submit.prevent="createReview">
            <el-form-item label="Lens or diagnostic question">
              <el-select
                v-model="form.knowledgeObjectId"
                filterable
                clearable
                placeholder="Select source-backed lens if available"
              >
                <el-option
                  v-for="item in reviewableKnowledge"
                  :key="item.id"
                  :label="`${item.title} (${item.type})`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Review question" required>
              <el-input v-model="form.question" placeholder="What does this lens reveal about the current project?" />
            </el-form-item>
            <el-form-item label="Answer">
              <el-input v-model="form.answer" type="textarea" :rows="5" placeholder="Record the project-specific answer." />
            </el-form-item>
            <div class="form-grid">
              <el-form-item label="Score">
                <el-input-number v-model="form.score" :min="0" :max="10" :step="1" />
              </el-form-item>
              <el-form-item label="Status">
                <el-select v-model="form.status">
                  <el-option label="Open" value="OPEN" />
                  <el-option label="Reviewed" value="REVIEWED" />
                  <el-option label="Action Needed" value="ACTION_NEEDED" />
                </el-select>
              </el-form-item>
            </div>
            <AppButton variant="primary" :loading="saving" native-type="submit">Create Lens Review</AppButton>
          </el-form>
        </AppCard>

        <AppCard class="list-card">
          <AppSectionHeader title="Active Reviews" eyebrow="Project thinking" :level="2" compact />
          <AppEmptyState
            v-if="!reviews.length"
            title="No lens reviews yet"
            description="Seed or create design lenses, then apply them to this project."
            compact
          />
          <article v-for="review in reviews" v-else :key="review.id" class="record-card">
            <div class="record-card__topline">
              <AppBadge variant="primary">{{ review.status }}</AppBadge>
              <AppBadge v-if="review.score !== null" variant="accent">Score {{ review.score }}/10</AppBadge>
              <AppBadge v-if="review.knowledgeObjectTitle" variant="info">{{ review.knowledgeObjectTitle }}</AppBadge>
            </div>
            <h3>{{ review.question }}</h3>
            <p>{{ review.answer ?? 'No answer yet.' }}</p>
            <div class="record-card__actions">
              <AppButton v-if="review.sourceReference" variant="secondary" @click="openSource(review.sourceReference)">Open Source</AppButton>
              <RouterLink
                :to="{
                  name: 'forum-new',
                  query: {
                    relatedEntityType: 'PROJECT_LENS_REVIEW',
                    relatedEntityId: String(review.id),
                    projectId: String(project.id),
                    sourceReferenceId: review.sourceReference?.id ? String(review.sourceReference.id) : undefined,
                    title: `Discuss lens review: ${review.question}`,
                  },
                }"
                custom
                v-slot="{ navigate }"
              >
                <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
              </RouterLink>
              <AppButton variant="text" @click="removeReview(review.id)">Delete</AppButton>
            </div>
          </article>
        </AppCard>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getKnowledgeObjects } from '../api/knowledge'
import { createProjectLensReview, deleteProjectLensReview, getProject, getProjectLensReviews } from '../api/projects'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { GameProjectRecord, KnowledgeObjectRecord, ProjectLensReviewRecord, SourceReferenceRecord } from '../types'

const route = useRoute()
const { openSource: openSourceDrawer } = useOpenSource()
const project = ref<GameProjectRecord | null>(null)
const reviews = ref<ProjectLensReviewRecord[]>([])
const knowledgeObjects = ref<KnowledgeObjectRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const form = reactive({
  knowledgeObjectId: null as number | null,
  question: '',
  answer: '',
  score: null as number | null,
  status: 'OPEN',
})

const reviewableKnowledge = computed(() =>
  knowledgeObjects.value.filter((item) => item.type === 'DESIGN_LENS' || item.type === 'LENS' || item.type === 'DIAGNOSTIC_QUESTION' || item.type === 'QUESTION'),
)

onMounted(loadData)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  const projectId = String(route.params.id)
  try {
    const [projectResult, reviewResult, objectResult] = await Promise.all([
      getProject(projectId),
      getProjectLensReviews(projectId),
      getKnowledgeObjects(),
    ])
    project.value = projectResult
    reviews.value = reviewResult
    knowledgeObjects.value = objectResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function createReview() {
  if (!project.value || !form.question.trim()) return
  saving.value = true
  try {
    await createProjectLensReview(project.value.id, {
      knowledgeObjectId: form.knowledgeObjectId,
      question: form.question.trim(),
      answer: form.answer.trim() || null,
      score: form.score,
      status: form.status,
    })
    form.knowledgeObjectId = null
    form.question = ''
    form.answer = ''
    form.score = null
    form.status = 'OPEN'
    await loadData()
    ElMessage.success('Lens review created.')
  } catch {
    ElMessage.error('Could not create lens review.')
  } finally {
    saving.value = false
  }
}

async function removeReview(id: number) {
  try {
    await deleteProjectLensReview(id)
    reviews.value = reviews.value.filter((review) => review.id !== id)
    ElMessage.success('Lens review deleted.')
  } catch {
    ElMessage.error('Could not delete lens review.')
  }
}

function openSource(source: SourceReferenceRecord) {
  void openSourceDrawer({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: source.id,
    bookId: source.bookId,
    pageStart: source.pageStart,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}
</script>

<style scoped>
@import './project-section.css';
</style>
