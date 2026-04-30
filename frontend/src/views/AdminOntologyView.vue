<template>
  <div class="page-shell admin-ontology-page">
    <AppSectionHeader
      title="Ontology Seed Import"
      eyebrow="Admin"
      description="Review and import original game design ontology seed data. Imports are disabled unless APP_SEED_ENABLED=true."
      :level="1"
    />

    <AppCard class="admin-panel" as="section">
      <div class="admin-panel__intro">
        <div>
          <h2>AI Provider Status</h2>
          <p>Provider secrets are never displayed. External providers remain optional and all suggestions stay draft-only.</p>
        </div>
        <AppBadge :variant="aiStatus?.available ? 'success' : 'warning'">
          {{ aiStatus?.activeProvider ?? 'Unknown provider' }}
        </AppBadge>
      </div>
      <dl v-if="aiStatus" class="result-grid">
        <div><dt>Enabled</dt><dd>{{ aiStatus.enabled ? 'Yes' : 'No' }}</dd></div>
        <div><dt>Available</dt><dd>{{ aiStatus.available ? 'Yes' : 'No' }}</dd></div>
        <div><dt>Configured</dt><dd>{{ aiStatus.configuredProvider }}</dd></div>
        <div><dt>External configured</dt><dd>{{ aiStatus.externalProviderConfigured ? 'Yes' : 'No' }}</dd></div>
        <div><dt>Max input</dt><dd>{{ aiStatus.maxInputChars }} characters</dd></div>
      </dl>
      <p class="status-note">{{ aiStatus?.message ?? aiStatusError }}</p>
    </AppCard>

    <AppCard class="admin-panel" as="section">
      <div class="admin-panel__intro">
        <div>
          <h2>Game Design Ontology</h2>
          <p>
            Seed books as metadata only, then import original concepts, lenses, diagnostic questions, exercises, and prototype task
            templates. Page numbers are omitted unless provided by a verified source.
          </p>
        </div>
        <AppBadge variant="warning">Admin only</AppBadge>
      </div>

      <div class="admin-actions">
        <AppButton variant="secondary" :loading="loadingDefault" @click="loadDefaultSeed">Load Default JSON</AppButton>
        <AppButton variant="ghost" :loading="submitting" @click="submitImport(true)">Dry Run</AppButton>
        <AppButton variant="primary" :loading="submitting" @click="submitImport(false)">Import Seed</AppButton>
      </div>

      <label class="field">
        <span>Import JSON</span>
        <el-input v-model="jsonText" type="textarea" :rows="18" spellcheck="false" />
      </label>
    </AppCard>

    <AppErrorState v-if="errorMessage" title="Ontology import unavailable" :description="errorMessage" />

    <AppCard v-if="result" class="result-panel" as="section">
      <AppSectionHeader
        :title="result.dryRun ? 'Dry Run Result' : 'Import Result'"
        eyebrow="Seed status"
        :description="result.dryRun ? 'No records were written.' : 'Records were imported idempotently.'"
        :level="2"
        compact
      />
      <dl class="result-grid">
        <div><dt>Books</dt><dd>{{ result.booksCreated }} created, {{ result.booksExisting }} existing</dd></div>
        <div><dt>Concepts</dt><dd>{{ result.conceptsCreated }} created, {{ result.conceptsUpdated }} updated, {{ result.conceptsExisting }} existing</dd></div>
        <div><dt>Design Knowledge</dt><dd>{{ result.knowledgeObjectsCreated }} created, {{ result.knowledgeObjectsUpdated }} updated, {{ result.knowledgeObjectsExisting }} existing</dd></div>
        <div><dt>Source Links</dt><dd>{{ result.sourceReferencesCreated }} created</dd></div>
      </dl>
      <div v-if="result.warnings.length" class="warnings">
        <strong>Warnings</strong>
        <ul>
          <li v-for="warning in result.warnings" :key="warning">{{ warning }}</li>
        </ul>
      </div>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAIProviderStatus } from '../api/ai'
import { getDefaultOntologySeed, importOntologySeed } from '../api/admin'
import type { AIProviderStatusRecord, OntologyImportPayload, OntologyImportResult } from '../types'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'

const jsonText = ref('')
const result = ref<OntologyImportResult | null>(null)
const errorMessage = ref('')
const loadingDefault = ref(false)
const submitting = ref(false)
const aiStatus = ref<AIProviderStatusRecord | null>(null)
const aiStatusError = ref('AI provider status could not be loaded.')

onMounted(loadAIProviderStatus)

async function loadAIProviderStatus() {
  try {
    aiStatus.value = await getAIProviderStatus()
  } catch (error) {
    aiStatusError.value = apiError(error, 'AI provider status could not be loaded.')
  }
}

async function loadDefaultSeed() {
  loadingDefault.value = true
  errorMessage.value = ''
  try {
    const seed = await getDefaultOntologySeed()
    jsonText.value = JSON.stringify(seed, null, 2)
    result.value = null
  } catch (error) {
    errorMessage.value = apiError(error, 'Default seed could not be loaded. Confirm you are an admin.')
  } finally {
    loadingDefault.value = false
  }
}

async function submitImport(dryRun: boolean) {
  let payload: OntologyImportPayload
  try {
    payload = JSON.parse(jsonText.value || '{}') as OntologyImportPayload
  } catch {
    ElMessage.error('Import JSON is not valid.')
    return
  }

  submitting.value = true
  errorMessage.value = ''
  try {
    result.value = await importOntologySeed(payload, dryRun)
    ElMessage.success(dryRun ? 'Ontology seed validated.' : 'Ontology seed imported.')
  } catch (error) {
    errorMessage.value = apiError(error, 'Ontology import failed. Check admin role and APP_SEED_ENABLED.')
  } finally {
    submitting.value = false
  }
}

function apiError(error: unknown, fallback: string) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { status?: number; data?: { message?: string } } }).response
    if (response?.status === 403) return 'Permission denied or ontology imports are disabled for this environment.'
    return response?.data?.message ?? fallback
  }
  return fallback
}
</script>

<style scoped>
.admin-ontology-page,
.admin-panel,
.result-panel {
  display: grid;
  gap: var(--space-5);
}

.admin-panel,
.result-panel {
  padding: var(--space-5);
}

.admin-panel__intro,
.admin-actions {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.admin-panel__intro h2,
.admin-panel__intro p {
  margin: 0;
}

.admin-panel__intro p {
  margin-top: var(--space-2);
  max-width: 78ch;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.result-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 220px), 1fr));
  gap: var(--space-3);
}

.result-grid div {
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.result-grid dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  text-transform: uppercase;
}

.result-grid dd {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-weight: 800;
}

.warnings {
  color: var(--bookos-text-secondary);
}

.status-note {
  margin: 0;
  color: var(--bookos-text-secondary);
}
</style>
