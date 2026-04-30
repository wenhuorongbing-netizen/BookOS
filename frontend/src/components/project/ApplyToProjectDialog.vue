<template>
  <el-dialog
    :model-value="modelValue"
    title="Apply to Project"
    width="min(620px, 94vw)"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="loadProjects"
  >
    <div class="apply-project">
      <AppErrorState
        v-if="errorMessage"
        title="Projects unavailable"
        :description="errorMessage"
        retry-label="Retry"
        @retry="loadProjects"
      />

      <template v-else>
        <p class="apply-project__source">
          Applying <strong>{{ sourceLabel }}</strong>
          <span v-if="sourceReference">with source link #{{ sourceReference.id }}</span>
        </p>

        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="Project" required>
            <el-select v-model="selectedProjectId" filterable placeholder="Choose a project" :loading="loadingProjects">
              <el-option v-for="project in projects" :key="project.id" :label="project.title" :value="project.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="Application type">
            <el-select v-model="form.applicationType">
              <el-option label="Mechanic Test" value="MECHANIC_TEST" />
              <el-option label="Design Lens Review" value="DESIGN_LENS_REVIEW" />
              <el-option label="Prototype Task" value="PROTOTYPE_TASK" />
              <el-option label="Design Reference" value="DESIGN_REFERENCE" />
              <el-option label="Project Application" value="PROJECT_APPLICATION" />
            </el-select>
          </el-form-item>

          <el-form-item label="Application title" required>
            <el-input v-model="form.title" placeholder="What will this source change in the project?" />
          </el-form-item>

          <el-form-item label="Design note">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="Describe how this source-backed idea should affect the prototype."
            />
          </el-form-item>

          <div class="apply-project__actions">
            <AppButton variant="primary" :loading="saving" native-type="submit">Create Application</AppButton>
            <AppButton variant="secondary" @click="$emit('update:modelValue', false)">Cancel</AppButton>
          </div>
        </el-form>
      </template>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  applyConceptToProject,
  applyKnowledgeObjectToProject,
  applyQuoteToProject,
  applySourceReferenceToProject,
  createProjectApplication,
  createPrototypeTaskFromDaily,
  getProjects,
} from '../../api/projects'
import AppButton from '../ui/AppButton.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import type { GameProjectRecord, ProjectApplicationRecord, SourceReferenceRecord } from '../../types'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    sourceType: 'QUOTE' | 'CONCEPT' | 'KNOWLEDGE_OBJECT' | 'SOURCE_REFERENCE' | 'DAILY_DESIGN_PROMPT' | string
    sourceId: number
    sourceLabel: string
    sourceReference?: SourceReferenceRecord | null
    defaultTitle?: string
    defaultDescription?: string | null
  }>(),
  {
    sourceReference: null,
    defaultTitle: '',
    defaultDescription: null,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  applied: [application: ProjectApplicationRecord]
}>()

const projects = ref<GameProjectRecord[]>([])
const selectedProjectId = ref<number | null>(null)
const loadingProjects = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const form = reactive({
  applicationType: 'PROJECT_APPLICATION',
  title: '',
  description: '',
})

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    form.title = props.defaultTitle || `Apply ${props.sourceLabel}`
    form.description = props.defaultDescription ?? ''
    form.applicationType = props.sourceType === 'DAILY_DESIGN_PROMPT' ? 'PROTOTYPE_TASK' : 'PROJECT_APPLICATION'
  },
)

async function loadProjects() {
  loadingProjects.value = true
  errorMessage.value = ''
  try {
    projects.value = await getProjects()
    selectedProjectId.value = selectedProjectId.value ?? projects.value[0]?.id ?? null
  } catch {
    errorMessage.value = 'Check backend availability and your permissions, then try again.'
  } finally {
    loadingProjects.value = false
  }
}

async function submit() {
  if (!selectedProjectId.value) {
    ElMessage.warning('Create or choose a project first.')
    return
  }

  saving.value = true
  try {
    const payload = {
      sourceId: props.sourceId,
      title: form.title.trim(),
      description: form.description.trim() || null,
      applicationType: form.applicationType,
    }
    let application: ProjectApplicationRecord
    if (props.sourceType === 'QUOTE') {
      application = await applyQuoteToProject(selectedProjectId.value, payload)
    } else if (props.sourceType === 'CONCEPT') {
      application = await applyConceptToProject(selectedProjectId.value, payload)
    } else if (props.sourceType === 'KNOWLEDGE_OBJECT') {
      application = await applyKnowledgeObjectToProject(selectedProjectId.value, payload)
    } else if (props.sourceType === 'SOURCE_REFERENCE') {
      application = await applySourceReferenceToProject(selectedProjectId.value, payload)
    } else if (props.sourceType === 'DAILY_DESIGN_PROMPT') {
      application = await createPrototypeTaskFromDaily(selectedProjectId.value, {
        dailyDesignPromptId: props.sourceId,
        title: form.title.trim(),
        description: form.description.trim() || null,
      })
    } else {
      application = await createProjectApplication(selectedProjectId.value, {
        sourceEntityType: props.sourceType,
        sourceEntityId: props.sourceId,
        sourceReferenceId: props.sourceReference?.id ?? null,
        applicationType: form.applicationType,
        title: form.title.trim(),
        description: form.description.trim() || null,
        status: 'OPEN',
      })
    }
    ElMessage.success('Project application created.')
    emit('applied', application)
    emit('update:modelValue', false)
  } catch {
    ElMessage.error('Could not apply this source to the project.')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.apply-project {
  display: grid;
  gap: var(--space-4);
}

.apply-project__source {
  margin: 0;
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
}

.apply-project__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}
</style>
