<template>
  <el-form class="project-form" label-position="top" @submit.prevent="submit">
    <el-form-item label="Title" required>
      <el-input v-model="form.title" placeholder="Prototype name or working title" />
    </el-form-item>

    <el-form-item label="Description">
      <el-input
        v-model="form.description"
        type="textarea"
        :rows="4"
        placeholder="What is this game trying to become?"
      />
    </el-form-item>

    <div class="project-form__grid">
      <el-form-item label="Genre">
        <el-input v-model="form.genre" placeholder="Puzzle, roguelike, cozy sim..." />
      </el-form-item>
      <el-form-item label="Platform">
        <el-input v-model="form.platform" placeholder="Web, PC, mobile, tabletop..." />
      </el-form-item>
      <el-form-item label="Stage">
        <el-select v-model="form.stage" placeholder="Select stage">
          <el-option v-for="stage in stageOptions" :key="stage" :label="stageLabel(stage)" :value="stage" />
        </el-select>
      </el-form-item>
      <el-form-item label="Visibility">
        <el-select v-model="form.visibility">
          <el-option v-for="visibility in visibilityOptions" :key="visibility" :label="visibility" :value="visibility" />
        </el-select>
      </el-form-item>
    </div>

    <el-form-item label="Progress">
      <el-slider v-model="form.progressPercent" :min="0" :max="100" :step="5" show-input />
    </el-form-item>

    <div class="project-form__actions">
      <AppButton variant="primary" :loading="saving" native-type="submit">
        {{ submitLabel }}
      </AppButton>
      <slot name="secondary" />
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import AppButton from '../ui/AppButton.vue'
import type { GameProjectPayload, GameProjectRecord, ProjectStage, Visibility } from '../../types'
import { visibilityOptions } from '../../types'

const props = withDefaults(
  defineProps<{
    project?: GameProjectRecord | null
    saving?: boolean
    submitLabel?: string
  }>(),
  {
    project: null,
    saving: false,
    submitLabel: 'Save Project',
  },
)

const emit = defineEmits<{
  submit: [payload: GameProjectPayload]
}>()

const stageOptions: ProjectStage[] = ['IDEATION', 'PROTOTYPE', 'PLAYTESTING', 'ITERATING', 'SHIPPED']
interface ProjectFormState {
  title: string
  description: string
  genre: string
  platform: string
  stage: ProjectStage
  visibility: Visibility
  progressPercent: number
}

const form = reactive<ProjectFormState>({
  title: '',
  description: '',
  genre: '',
  platform: '',
  stage: 'IDEATION',
  visibility: 'PRIVATE',
  progressPercent: 0,
})

watch(
  () => props.project,
  (project) => {
    form.title = project?.title ?? ''
    form.description = project?.description ?? ''
    form.genre = project?.genre ?? ''
    form.platform = project?.platform ?? ''
    form.stage = project?.stage ?? 'IDEATION'
    form.visibility = project?.visibility ?? 'PRIVATE'
    form.progressPercent = project?.progressPercent ?? 0
  },
  { immediate: true },
)

function submit() {
  emit('submit', {
    title: form.title.trim(),
    description: form.description.trim() || null,
    genre: form.genre.trim() || null,
    platform: form.platform.trim() || null,
    stage: form.stage,
    visibility: form.visibility as Visibility,
    progressPercent: form.progressPercent,
  })
}

function stageLabel(stage: ProjectStage) {
  return stage
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}
</script>

<style scoped>
.project-form {
  display: grid;
  gap: var(--space-3);
}

.project-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.project-form__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 720px) {
  .project-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
