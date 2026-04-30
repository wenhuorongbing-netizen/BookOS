<template>
  <div class="page-shell transfer-page">
    <AppSectionHeader
      eyebrow="Import / Export"
      title="Data Portability"
      description="Export your source-backed BookOS knowledge, or preview structured imports before anything is written."
      :level="1"
    >
      <template #actions>
        <HelpTooltip topic="import-preview" placement="left" />
      </template>
    </AppSectionHeader>

    <DetailNextStepCard
      :title="transferNextStep.title"
      :description="transferNextStep.description"
      :primary-label="transferNextStep.primaryLabel"
      :primary-loading="transferNextStep.primaryLoading"
      :secondary-label="transferNextStep.secondaryLabel"
      :loop="transferWorkflowLoop"
      @primary="handleTransferPrimaryAction"
      @secondary="clearImportState"
    />

    <details class="transfer-disclosure">
      <summary>
        <span>Workflow guides</span>
        <small>Use when import/export feels risky</small>
      </summary>
      <UseCaseSuggestionPanel
        title="Export or import without losing source rules"
        description="Start with the export workflow before using import preview. Unknown pages stay unknown and private exports should be handled carefully."
        :slugs="importExportUseCaseSlugs"
      />
    </details>

    <section class="transfer-grid transfer-grid--primary">
      <AppCard class="transfer-card" as="section">
        <AppSectionHeader title="Export" eyebrow="Your data only" compact>
          <template #actions>
            <AppBadge variant="success" size="sm">JSON / Markdown / CSV</AppBadge>
          </template>
        </AppSectionHeader>

        <p class="transfer-copy">
          Exports are scoped to the current authenticated user. Book exports include notes, captures, quotes, action
          items, concepts, source links, and related-link summary where available.
        </p>

        <p class="transfer-hint">
          Use the primary action above for the full JSON backup. Keep CSV and single-book exports collapsed until you
          need a narrower format.
        </p>

        <details class="transfer-disclosure transfer-disclosure--nested">
          <summary>
            <span>CSV and single-book exports</span>
            <small>Advanced formats</small>
          </summary>
          <div class="export-actions">
            <AppButton :loading="exporting" variant="secondary" @click="downloadCsv('/export/quotes/csv', 'bookos-quotes.csv')">
              Quotes CSV
            </AppButton>
            <AppButton :loading="exporting" variant="secondary" @click="downloadCsv('/export/action-items/csv', 'bookos-action-items.csv')">
              Actions CSV
            </AppButton>
            <AppButton :loading="exporting" variant="secondary" @click="downloadCsv('/export/concepts/csv', 'bookos-concepts.csv')">
              Concepts CSV
            </AppButton>
          </div>

          <AppDivider />

          <label class="field">
            <span>Single book export</span>
            <el-select v-model="selectedBookId" filterable clearable placeholder="Choose a book">
              <el-option v-for="book in books" :key="book.id" :label="book.title" :value="book.id" />
            </el-select>
          </label>
          <div class="export-actions">
            <AppButton :disabled="!selectedBookId" :loading="exporting" variant="secondary" @click="downloadBookJson">
              Book JSON
            </AppButton>
            <AppButton :disabled="!selectedBookId" :loading="exporting" variant="secondary" @click="downloadBookMarkdown">
              Book Markdown
            </AppButton>
          </div>
        </details>
      </AppCard>

      <details class="transfer-disclosure import-panel" :open="importPanelOpen">
        <summary>
          <span>Import data</span>
          <small>Preview before anything is written</small>
        </summary>
        <AppCard class="transfer-card" as="section">
          <AppSectionHeader title="Import" eyebrow="Preview before write" compact>
            <template #actions>
              <AppBadge variant="warning" size="sm">No overwrite</AppBadge>
            </template>
          </AppSectionHeader>

          <p class="transfer-copy">
            Import preview validates records, duplicate risk, unsupported fields, source-reference issues, and malformed
            page numbers. Unknown pages stay null.
          </p>

          <label class="field">
            <span>Import type</span>
            <el-select v-model="importType" placeholder="Choose import type">
              <el-option label="BookOS JSON backup" value="BOOKOS_JSON" />
              <el-option label="Markdown notes" value="MARKDOWN_NOTES" />
              <el-option label="Quotes CSV" value="QUOTES_CSV" />
              <el-option label="Actions CSV" value="ACTION_ITEMS_CSV" />
            </el-select>
          </label>

          <label v-if="importType === 'MARKDOWN_NOTES'" class="field">
            <span>Book title fallback</span>
            <el-input v-model="bookTitle" placeholder="Used if the Markdown has no # heading" />
          </label>

          <label class="field">
            <span>Upload file</span>
            <input class="file-input" type="file" accept=".json,.md,.markdown,.csv,.txt" @change="handleFile" />
          </label>

          <label class="field">
            <span>Import content</span>
            <el-input
              v-model="importContent"
              type="textarea"
              :rows="12"
              maxlength="1000000"
              show-word-limit
              placeholder="Paste BookOS JSON, Markdown notes, or CSV content..."
            />
          </label>

          <div class="export-actions">
            <AppButton :disabled="!canPreview" :loading="previewing" variant="primary" @click="runPreview">
              Preview Import
            </AppButton>
            <AppButton :disabled="!preview || !preview.recordsToCreate" :loading="committing" variant="secondary" @click="runCommit">
              Confirm Import
            </AppButton>
          </div>
        </AppCard>
      </details>
    </section>

    <AppErrorState
      v-if="errorMessage"
      title="Import/export action failed"
      :description="errorMessage"
      retry-label="Clear"
      @retry="errorMessage = ''"
    />

    <AppCard v-if="preview" class="preview-card" as="section">
      <AppSectionHeader title="Import Preview" eyebrow="No records written yet" compact>
        <template #actions>
          <AppBadge variant="primary" size="sm">{{ preview.recordsToCreate }} create</AppBadge>
          <AppBadge v-if="preview.potentialDuplicates" variant="warning" size="sm">
            {{ preview.potentialDuplicates }} duplicates
          </AppBadge>
        </template>
      </AppSectionHeader>

      <div class="preview-summary" aria-label="Import preview summary">
        <AppStat label="Total records" :value="preview.totalRecords" tone="neutral" />
        <AppStat label="Create" :value="preview.recordsToCreate" tone="success" />
        <AppStat label="Duplicates" :value="preview.potentialDuplicates" tone="warning" />
        <AppStat label="Warnings" :value="allPreviewIssues.length" tone="accent" />
      </div>

      <AppCard v-if="allPreviewIssues.length" class="issue-card" variant="muted" as="section">
        <strong>Warnings and safety checks</strong>
        <ul>
          <li v-for="issue in allPreviewIssues" :key="issue">{{ issue }}</li>
        </ul>
      </AppCard>

      <AppEmptyState
        v-if="!preview.records.length"
        title="No importable records"
        description="The file was parsed, but no supported BookOS records were found."
        compact
      />
      <div v-else class="preview-table-wrap">
        <table class="preview-table">
          <thead>
            <tr>
              <th>Type</th>
              <th>Title</th>
              <th>Action</th>
              <th>Duplicate</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in preview.records" :key="record.stableKey">
              <td>{{ record.type }}</td>
              <td>{{ record.title }}</td>
              <td>{{ record.action }}</td>
              <td>
                <AppBadge :variant="record.duplicate ? 'warning' : 'success'" size="sm">
                  {{ record.duplicate ? 'Possible duplicate' : 'New' }}
                </AppBadge>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </AppCard>

    <AppCard v-if="commitResult" class="preview-card" as="section">
      <AppSectionHeader title="Import Result" eyebrow="Committed" compact />
      <div class="preview-summary">
        <AppStat label="Books" :value="commitResult.booksCreated" tone="primary" />
        <AppStat label="Notes" :value="commitResult.notesCreated" tone="info" />
        <AppStat label="Quotes" :value="commitResult.quotesCreated" tone="success" />
        <AppStat label="Actions" :value="commitResult.actionItemsCreated" tone="warning" />
        <AppStat label="Concepts" :value="commitResult.conceptsCreated" tone="accent" />
        <AppStat label="Skipped" :value="commitResult.duplicatesSkipped" tone="neutral" />
      </div>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBooks } from '../api/books'
import { commitImport, downloadExport, exportAllJson, exportBookJson, previewImport } from '../api/importExport'
import { recordUseCaseEvent } from '../api/useCaseProgress'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppDivider from '../components/ui/AppDivider.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import AppStat from '../components/ui/AppStat.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'
import type { BookRecord, ImportCommitRecord, ImportPreviewRecord, ImportRequestPayload, ImportType } from '../types'

const route = useRoute()
const books = ref<BookRecord[]>([])
const importExportUseCaseSlugs = [
  'export-reading-knowledge',
  'search-rediscover-knowledge',
  'mock-ai-draft-helper',
]
const transferWorkflowLoop = ['Export backup', 'Preview import', 'Review warnings', 'Confirm intentionally']
const selectedBookId = ref<number | null>(null)
const exporting = ref(false)
const previewing = ref(false)
const committing = ref(false)
const importType = ref<ImportType>('BOOKOS_JSON')
const importContent = ref('')
const fileName = ref<string | null>(null)
const bookTitle = ref('')
const preview = ref<ImportPreviewRecord | null>(null)
const commitResult = ref<ImportCommitRecord | null>(null)
const errorMessage = ref('')

const canPreview = computed(() => importContent.value.trim().length > 0)
const importPanelOpen = computed(() => route.name === 'import-data' || Boolean(importContent.value || preview.value || commitResult.value))
const transferNextStep = computed(() => {
  if (preview.value) {
    return {
      title: 'Review import preview before writing',
      description: 'Check duplicates, source-reference warnings, and page-number issues. Preview has not written any records.',
      primaryLabel: 'Confirm Import',
      primaryLoading: committing.value,
      secondaryLabel: 'Clear Preview',
    }
  }

  if (commitResult.value) {
    return {
      title: 'Export a fresh backup after import',
      description: 'A new JSON export gives you a portable snapshot after the explicit import commit.',
      primaryLabel: 'Export All JSON',
      primaryLoading: exporting.value,
      secondaryLabel: 'Clear Result',
    }
  }

  return {
    title: 'Export a safe backup first',
    description: 'Start with a complete JSON export. Import stays collapsed until you intentionally open preview-before-write.',
    primaryLabel: 'Export All JSON',
    primaryLoading: exporting.value,
    secondaryLabel: null,
  }
})

const allPreviewIssues = computed(() => {
  if (!preview.value) return []
  return [
    ...preview.value.warnings,
    ...preview.value.unsupportedFields,
    ...preview.value.sourceReferenceIssues,
    ...preview.value.pageNumberIssues,
  ]
})

onMounted(async () => {
  try {
    books.value = await getBooks()
  } catch {
    errorMessage.value = 'Books could not load. Export all data and CSV still work if the backend is available.'
  }
})

async function downloadAllJson() {
  exporting.value = true
  errorMessage.value = ''
  try {
    const data = await exportAllJson()
    saveJson(data, 'bookos-export.json')
    recordExportStarted('BOOKOS_JSON', 'all')
  } catch {
    errorMessage.value = 'Full JSON export failed. Check backend availability and permissions.'
  } finally {
    exporting.value = false
  }
}

async function downloadBookJson() {
  if (!selectedBookId.value) return
  exporting.value = true
  errorMessage.value = ''
  try {
    const data = await exportBookJson(selectedBookId.value)
    saveJson(data, `bookos-book-${selectedBookId.value}.json`)
    recordExportStarted('BOOK_JSON', selectedBookId.value)
  } catch {
    errorMessage.value = 'Book JSON export failed. Confirm the book is in your library.'
  } finally {
    exporting.value = false
  }
}

async function downloadBookMarkdown() {
  if (!selectedBookId.value) return
  await downloadCsv(`/export/book/${selectedBookId.value}/markdown`, `bookos-book-${selectedBookId.value}.md`)
}

async function downloadCsv(path: string, filename: string) {
  exporting.value = true
  errorMessage.value = ''
  try {
    await downloadExport(path, filename)
    recordExportStarted(exportContextType(filename), filename)
  } catch {
    errorMessage.value = 'Download failed. Check backend availability and permissions.'
  } finally {
    exporting.value = false
  }
}

async function handleFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (file.size > 1_000_000) {
    ElMessage.error('Import files must be 1 MB or smaller.')
    input.value = ''
    return
  }
  fileName.value = file.name
  importContent.value = await file.text()
  preview.value = null
  commitResult.value = null
}

async function runPreview() {
  previewing.value = true
  errorMessage.value = ''
  commitResult.value = null
  try {
    preview.value = await previewImport(buildPayload())
    ElMessage.success('Import preview generated. No records were written.')
  } catch {
    errorMessage.value = 'Import preview failed. Check file type, payload size, and required fields.'
  } finally {
    previewing.value = false
  }
}

async function runCommit() {
  if (!preview.value) return
  try {
    await ElMessageBox.confirm(
      'Commit this import? Existing duplicates will be skipped and no records will be overwritten.',
      'Confirm Import',
      {
        confirmButtonText: 'Commit Import',
        cancelButtonText: 'Cancel',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  committing.value = true
  errorMessage.value = ''
  try {
    commitResult.value = await commitImport(buildPayload())
    preview.value = null
    ElMessage.success('Import committed.')
  } catch {
    errorMessage.value = 'Import commit failed. Nothing is overwritten automatically; review the file and try again.'
  } finally {
    committing.value = false
  }
}

function handleTransferPrimaryAction() {
  if (preview.value) {
    void runCommit()
    return
  }
  void downloadAllJson()
}

function clearImportState() {
  preview.value = null
  commitResult.value = null
  errorMessage.value = ''
}

function buildPayload(): ImportRequestPayload {
  return {
    importType: importType.value,
    content: importContent.value,
    bookTitle: bookTitle.value.trim() || null,
    fileName: fileName.value,
  }
}

function saveJson(data: Record<string, unknown>, filename: string) {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

function recordExportStarted(contextType: string, contextId: string | number) {
  void recordUseCaseEvent({
    eventType: 'EXPORT_STARTED',
    contextType,
    contextId,
  }).catch(() => undefined)
}

function exportContextType(filename: string) {
  if (filename.endsWith('.md')) return 'BOOK_MARKDOWN'
  if (filename.includes('quotes')) return 'QUOTES_CSV'
  if (filename.includes('action-items')) return 'ACTION_ITEMS_CSV'
  if (filename.includes('concepts')) return 'CONCEPTS_CSV'
  return 'EXPORT'
}
</script>

<style scoped>
.transfer-page,
.transfer-card,
.preview-card,
.transfer-disclosure {
  display: grid;
  gap: var(--space-5);
}

.transfer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);
}

.transfer-grid--primary {
  grid-template-columns: minmax(0, 1fr);
}

.transfer-card,
.preview-card {
  padding: var(--space-5);
}

.transfer-disclosure summary {
  min-height: 44px;
  padding: var(--space-3) var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: color-mix(in srgb, var(--bookos-surface) 86%, var(--bookos-primary-soft));
  color: var(--bookos-text-primary);
  cursor: pointer;
  font-weight: 900;
  list-style: none;
}

.transfer-disclosure summary::-webkit-details-marker {
  display: none;
}

.transfer-disclosure summary::after {
  content: "+";
  color: var(--bookos-primary);
}

.transfer-disclosure[open] summary::after {
  content: "-";
}

.transfer-disclosure summary small {
  margin-left: auto;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.transfer-disclosure--nested {
  gap: var(--space-4);
}

.transfer-disclosure--nested summary {
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.import-panel {
  margin-top: var(--space-2);
}

.transfer-copy {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.65;
}

.transfer-hint {
  margin: 0;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  line-height: 1.55;
}

.export-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.file-input {
  min-height: 44px;
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-primary);
}

.preview-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.issue-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
}

.issue-card ul {
  margin: 0;
  padding-left: var(--space-5);
  color: var(--bookos-text-secondary);
}

.preview-table-wrap {
  overflow-x: auto;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 720px;
}

.preview-table th,
.preview-table td {
  padding: var(--space-3);
  border-bottom: 1px solid var(--bookos-border);
  text-align: left;
  vertical-align: top;
}

.preview-table th {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

@media (max-width: 960px) {
  .transfer-grid,
  .preview-summary {
    grid-template-columns: 1fr;
  }
}
</style>
