<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="min(620px, 94vw)"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="loadProjects"
  >
    <div class="daily-project-action">
      <AppErrorState
        v-if="errorMessage"
        title="Projects unavailable"
        :description="errorMessage"
        retry-label="Retry"
        @retry="loadProjects"
      />

      <template v-else>
        <p class="daily-project-action__source">
          Using daily prompt <strong>{{ prompt.question }}</strong>
          <span v-if="prompt.sourceReference">with source reference #{{ prompt.sourceReference.id }}</span>
          <span v-else>as a template prompt. No source page is implied.</span>
        </p>

        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="Project" required>
            <el-select v-model="selectedProjectId" filterable placeholder="Choose a project" :loading="loadingProjects">
              <el-option v-for="project in projects" :key="project.id" :label="project.title" :value="project.id" />
            </el-select>
          </el-form-item>

          <template v-if="mode === 'PROBLEM'">
            <el-form-item label="Problem title" required>
              <el-input v-model="problemForm.title" placeholder="What design problem should this prompt create?" />
            </el-form-item>

            <el-form-item label="Description">
              <el-input v-model="problemForm.description" type="textarea" :rows="5" />
            </el-form-item>

            <div class="daily-project-action__grid">
              <el-form-item label="Priority">
                <el-select v-model="problemForm.priority">
                  <el-option label="Low" value="LOW" />
                  <el-option label="Medium" value="MEDIUM" />
                  <el-option label="High" value="HIGH" />
                </el-select>
              </el-form-item>
              <el-form-item label="Status">
                <el-select v-model="problemForm.status">
                  <el-option label="Open" value="OPEN" />
                  <el-option label="Investigating" value="INVESTIGATING" />
                  <el-option label="Resolved" value="RESOLVED" />
                </el-select>
              </el-form-item>
            </div>
          </template>

          <template v-else>
            <el-form-item label="Review question" required>
              <el-input v-model="reviewForm.question" placeholder="What should this prompt make the project answer?" />
            </el-form-item>

            <el-form-item label="Answer">
              <el-input v-model="reviewForm.answer" type="textarea" :rows="5" />
            </el-form-item>

            <div class="daily-project-action__grid">
              <el-form-item label="Score">
                <el-input-number v-model="reviewForm.score" :min="0" :max="10" :step="1" />
              </el-form-item>
              <el-form-item label="Status">
                <el-select v-model="reviewForm.status">
                  <el-option label="Open" value="OPEN" />
                  <el-option label="Reviewed" value="REVIEWED" />
                  <el-option label="Action Needed" value="ACTION_NEEDED" />
                </el-select>
              </el-form-item>
            </div>
          </template>

          <div class="daily-project-action__actions">
            <AppButton variant="primary" :loading="saving" native-type="submit">{{ submitLabel }}</AppButton>
            <AppButton variant="secondary" @click="$emit('update:modelValue', false)">Cancel</AppButton>
          </div>
        </el-form>
      </template>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createProjectLensReview, createProjectProblem, getProjects } from '../../api/projects'
import AppButton from '../ui/AppButton.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import type { DailyDesignPromptRecord, GameProjectRecord, ProjectLensReviewRecord, ProjectProblemRecord } from '../../types'

const props = defineProps<{
  modelValue: boolean
  mode: 'PROBLEM' | 'LENS_REVIEW'
  prompt: DailyDesignPromptRecord
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  created: [value: ProjectProblemRecord | ProjectLensReviewRecord]
}>()

const projects = ref<GameProjectRecord[]>([])
const selectedProjectId = ref<number | null>(null)
const loadingProjects = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const problemForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'OPEN',
})
const reviewForm = reactive({
  question: '',
  answer: '',
  score: null as number | null,
  status: 'OPEN',
})

const dialogTitle = computed(() => (props.mode === 'PROBLEM' ? 'Create Project Problem' : 'Create Project Lens Review'))
const submitLabel = computed(() => (props.mode === 'PROBLEM' ? 'Create Problem' : 'Create Lens Review'))

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    problemForm.title = props.prompt.question
    problemForm.description = props.prompt.templatePrompt
      ? `Template prompt: ${props.prompt.question}`
      : `Daily design prompt: ${props.prompt.question}`
    problemForm.priority = 'MEDIUM'
    problemForm.status = 'OPEN'
    reviewForm.question = props.prompt.question
    reviewForm.answer = ''
    reviewForm.score = null
    reviewForm.status = 'OPEN'
  },
)

async function loadProjects() {
  loadingProjects.value = true
  errorMessage.value = ''
  try {
    projects.value = await getProjects()
    selectedProjectId.value = selectedProjectId.value ?? projects.value[0]?.id ?? null
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loadingProjects.value = false
  }
}

async function submit() {
  if (!selectedProjectId.value) {
    ElMessage.warning('Create or choose a project first.')
    return
  }
  if (props.mode === 'PROBLEM' && !problemForm.title.trim()) {
    ElMessage.warning('Problem title is required.')
    return
  }
  if (props.mode === 'LENS_REVIEW' && !reviewForm.question.trim()) {
    ElMessage.warning('Review question is required.')
    return
  }

  saving.value = true
  try {
    const sourceReferenceId = props.prompt.sourceReference?.id ?? null
    const created =
      props.mode === 'PROBLEM'
        ? await createProjectProblem(selectedProjectId.value, {
            title: problemForm.title.trim(),
            description: problemForm.description.trim() || null,
            priority: problemForm.priority,
            status: problemForm.status,
            relatedSourceReferenceId: sourceReferenceId,
          })
        : await createProjectLensReview(selectedProjectId.value, {
            knowledgeObjectId: props.prompt.knowledgeObjectId,
            question: reviewForm.question.trim(),
            answer: reviewForm.answer.trim() || null,
            score: reviewForm.score,
            status: reviewForm.status,
            sourceReferenceId,
          })

    ElMessage.success(props.mode === 'PROBLEM' ? 'Project problem created.' : 'Project lens review created.')
    emit('created', created)
    emit('update:modelValue', false)
  } catch {
    ElMessage.error(props.mode === 'PROBLEM' ? 'Could not create project problem.' : 'Could not create lens review.')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.daily-project-action {
  display: grid;
  gap: var(--space-4);
}

.daily-project-action__source {
  margin: 0;
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
}

.daily-project-action__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.daily-project-action__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 680px) {
  .daily-project-action__grid {
    grid-template-columns: 1fr;
  }
}
</style>
