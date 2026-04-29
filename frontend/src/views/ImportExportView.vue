<template>
  <div class="page-shell transfer-page">
    <AppSectionHeader
      eyebrow="Import / Export"
      title="Data Portability"
      description="Export your source-backed BookOS knowledge, or preview structured imports before anything is written."
      :level="1"
    />

    <section class="transfer-grid">
      <AppCard class="transfer-card" as="section">
        <AppSectionHeader title="Export" eyebrow="Your data only" compact>
          <template #actions>
            <AppBadge variant="success" size="sm">JSON / Markdown / CSV</AppBadge>
          </template>
        </AppSectionHeader>

        <p class="transfer-copy">
          Exports are scoped to the current authenticated user. Book exports include notes, captures, quotes, action
          items, concepts, source references, and backlink summary where available.
        </p>

        <div class="export-actions">
          <AppButton :loading="exporting" variant="primary" @click="downloadAllJson">Export All JSON</AppButton>
          <AppButton :loading="exporting" variant="secondary" @click="downloadCsv('/export/quotes/csv', 'bookos-quotes.csv')">
            Quotes CSV
          </AppButton>
          <AppButton :loading="exporting" variant="secondary" @click="downloadCsv('/export/action-items/csv', 'bookos-action-items.csv')">
            Action Items CSV
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
      </AppCard>

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
            <el-option label="Action Items CSV" value="ACTION_ITEMS_CSV" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBooks } from '../api/books'
import { commitImport, downloadExport, exportAllJson, exportBookJson, previewImport } from '../api/importExport'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppDivider from '../components/ui/AppDivider.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import AppStat from '../components/ui/AppStat.vue'
import type { BookRecord, ImportCommitRecord, ImportPreviewRecord, ImportRequestPayload, ImportType } from '../types'

const books = ref<BookRecord[]>([])
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
</script>

<style scoped>
.transfer-page,
.transfer-card,
.preview-card {
  display: grid;
  gap: var(--space-5);
}

.transfer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);
}

.transfer-card,
.preview-card {
  padding: var(--space-5);
}

.transfer-copy {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.65;
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
