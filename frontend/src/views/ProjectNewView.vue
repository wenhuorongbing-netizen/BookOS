<template>
  <div class="page-shell project-new-page">
    <AppSectionHeader
      title="Create Project"
      eyebrow="Game Project Mode"
      description="Start a game project that can receive source-backed applications from your reading system."
      :level="1"
    />

    <AppCard class="project-new-card">
      <ProjectFormPanel :saving="saving" submit-label="Create Project" @submit="submitProject">
        <template #secondary>
          <RouterLink :to="{ name: 'projects' }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Cancel</AppButton>
          </RouterLink>
        </template>
      </ProjectFormPanel>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRouter } from 'vue-router'
import { createProject } from '../api/projects'
import ProjectFormPanel from '../components/project/ProjectFormPanel.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type { GameProjectPayload } from '../types'

const router = useRouter()
const saving = ref(false)

async function submitProject(payload: GameProjectPayload) {
  saving.value = true
  try {
    const project = await createProject(payload)
    ElMessage.success('Project created.')
    await router.push({ name: 'project-detail', params: { id: project.id } })
  } catch {
    ElMessage.error('Project creation failed.')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.project-new-page {
  display: grid;
  gap: var(--space-5);
}

.project-new-card {
  max-width: 820px;
  padding: var(--space-5);
}
</style>
