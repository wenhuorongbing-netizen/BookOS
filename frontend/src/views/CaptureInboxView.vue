<template>
  <div class="page-shell capture-inbox-page">
    <AppSectionHeader
      title="Capture Inbox"
      eyebrow="Quick Capture"
      :description="bookId ? 'Captures filtered to the selected book.' : 'Review, convert, or archive source-backed quick captures.'"
      :level="1"
    >
      <template #actions>
        <AppButton
          v-if="selectedCaptureIds.length"
          variant="ghost"
          :loading="bulkArchiving"
          @click="archiveSelected"
        >
          Archive selected
        </AppButton>
        <RouterLink to="/my-library" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Open Library</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppCard class="capture-filters" as="section" aria-label="Capture inbox filters">
      <label>
        <span>Status</span>
        <el-select v-model="statusFilter" aria-label="Filter captures by status" @change="loadInbox">
          <el-option label="Unconverted" value="INBOX" />
          <el-option label="Converted" value="CONVERTED" />
          <el-option label="Archived" value="ARCHIVED" />
          <el-option label="All captures" value="ALL" />
        </el-select>
      </label>
      <label>
        <span>Parsed type</span>
        <el-select v-model="typeFilter" aria-label="Filter captures by parsed type">
          <el-option label="All types" value="ALL" />
          <el-option label="Quote" value="QUOTE" />
          <el-option label="Action item" value="ACTION_ITEM" />
          <el-option label="Question" value="QUESTION" />
          <el-option label="Inspiration" value="INSPIRATION" />
          <el-option label="Related concept" value="RELATED_CONCEPT" />
          <el-option label="Note" value="NOTE" />
        </el-select>
      </label>
      <AppButton variant="secondary" :loading="capture.loading" @click="loadInbox">Refresh</AppButton>
    </AppCard>

    <AppLoadingState v-if="capture.loading" label="Loading capture inbox" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Capture inbox could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadInbox"
    />

    <AppEmptyState
      v-else-if="!displayBlocks.length"
      :title="capture.latestBlocks.length ? 'No captures match this filter' : 'No captures found'"
      :description="capture.latestBlocks.length ? 'Adjust the status or parsed type filters to review other captures.' : 'Use Quick Capture on a book detail page to save source-backed reading thoughts.'"
      eyebrow="Capture queue"
    />

    <section v-else class="capture-grid" aria-label="Capture inbox items">
      <AppCard v-for="block in displayBlocks" :key="block.captureId" class="capture-card" as="article">
        <div class="capture-card__header">
          <label class="capture-card__select">
            <input
              type="checkbox"
              :checked="selectedCaptureIds.includes(block.captureId)"
              :disabled="!canArchive(block)"
              :aria-label="`Select ${block.title} for bulk archive`"
              @change="toggleSelected(block.captureId)"
            />
          </label>
          <div>
            <div class="capture-card__book-label">Source book</div>
            <div class="eyebrow">{{ block.bookTitle }}</div>
            <h2>{{ block.title }}</h2>
          </div>
          <div class="capture-card__badges">
            <AppBadge variant="primary">{{ formatType(block.parsedType) }}</AppBadge>
            <AppBadge variant="neutral" size="sm">{{ block.status }}</AppBadge>
          </div>
        </div>

        <p class="capture-card__content">{{ block.content }}</p>

        <div class="capture-card__meta">
          <AppBadge v-if="block.page" variant="accent" size="sm">p.{{ block.page }}</AppBadge>
          <span>{{ formatDate(block.createdAt) }}</span>
        </div>
        <div v-if="displayTags(block).length || block.concepts.length" class="capture-card__taxonomy" aria-label="Tags and concepts">
          <AppBadge v-for="tag in displayTags(block)" :key="`tag-${tag}`" variant="neutral" size="sm">#{{ tag }}</AppBadge>
          <AppBadge v-for="concept in block.concepts" :key="`concept-${concept}`" variant="info" size="sm">[[{{ concept }}]]</AppBadge>
        </div>
        <div class="capture-card__source">
          <strong>Source reference</strong>
          <span>{{ sourceSummary(block) }}</span>
        </div>
        <ul v-if="block.parserWarnings.length" class="capture-card__warnings" aria-label="Parser warnings">
          <li v-for="warning in block.parserWarnings" :key="warning">{{ warning }}</li>
        </ul>

        <div class="capture-card__actions">
          <AppButton
            variant="primary"
            :loading="isConverting(block.captureId)"
            :disabled="archivingCaptureId === block.captureId || block.status !== 'INBOX'"
            @click="convertDefaultBlock(block)"
          >
            {{ primaryConversionLabel(block) }}
          </AppButton>

          <el-dropdown trigger="click" :disabled="isConverting(block.captureId) || block.status !== 'INBOX'" @command="handleSecondaryConversion(block, $event)">
            <AppButton variant="secondary" :disabled="isConverting(block.captureId) || block.status !== 'INBOX'" aria-label="Open conversion options">
              Convert as
            </AppButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="NOTE">Convert to Note</el-dropdown-item>
                <el-dropdown-item command="QUOTE">Convert to Quote</el-dropdown-item>
                <el-dropdown-item command="ACTION_ITEM">Convert to Action Item</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <AppButton variant="ghost" :disabled="!canArchive(block) || isConverting(block.captureId) || archivingCaptureId === block.captureId" @click="archiveBlock(block)">
            Archive
          </AppButton>
          <RouterLink :to="{ name: 'book-detail', params: { id: block.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open Book</AppButton>
          </RouterLink>
          <AppButton variant="ghost" @click="openCaptureSource(block)">Open Source</AppButton>
          <AppButton v-if="block.concepts.length" variant="accent" @click="openConceptReview(block)">Review Concepts</AppButton>
        </div>
      </AppCard>
    </section>

    <ConceptReviewDialog
      v-model="conceptDialogOpen"
      :parsed-concepts="selectedConceptBlock?.concepts ?? []"
      :concepts="conceptOptions"
      :source-reference="selectedConceptBlock?.sourceReferences[0] ?? null"
      :default-tags="selectedConceptBlock ? displayTags(selectedConceptBlock) : []"
      :saving="savingConceptReview"
      @submit="saveCaptureConceptReview"
    />
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getConcepts } from '../api/knowledge'
import { reviewCaptureConcepts } from '../api/captures'
import ConceptReviewDialog from '../components/concept/ConceptReviewDialog.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useCaptureStore, type RecentNoteBlock } from '../stores/capture'
import type { CaptureStatus, ConceptRecord, ConceptReviewPayload, NoteBlockType, RawCaptureConversionRecord } from '../types'

type ConversionKind = 'NOTE' | 'QUOTE' | 'ACTION_ITEM'
type StatusFilter = CaptureStatus | 'ALL'
type TypeFilter = NoteBlockType | 'ALL'

const route = useRoute()
const router = useRouter()
const capture = useCaptureStore()
const { openSource } = useOpenSource()
const errorMessage = ref('')
const convertingCaptureId = ref<number | null>(null)
const convertingQuoteCaptureId = ref<number | null>(null)
const convertingActionCaptureId = ref<number | null>(null)
const archivingCaptureId = ref<number | null>(null)
const conceptDialogOpen = ref(false)
const selectedConceptBlock = ref<RecentNoteBlock | null>(null)
const conceptOptions = ref<ConceptRecord[]>([])
const savingConceptReview = ref(false)
const statusFilter = ref<StatusFilter>('INBOX')
const typeFilter = ref<TypeFilter>('ALL')
const selectedCaptureIds = ref<number[]>([])
const bulkArchiving = ref(false)

const bookId = computed(() => {
  const value = route.query.bookId
  return typeof value === 'string' && value ? value : null
})
const displayBlocks = computed(() => {
  return capture.latestBlocks.filter((block) => typeFilter.value === 'ALL' || block.parsedType === typeFilter.value)
})

onMounted(loadInbox)

watch(
  () => route.query.bookId,
  () => {
    void loadInbox()
  },
)

async function loadInbox() {
  errorMessage.value = ''
  selectedCaptureIds.value = []
  try {
    await Promise.all([
      capture.loadCaptures({
        bookId: bookId.value ?? undefined,
        status: statusFilter.value === 'ALL' ? null : statusFilter.value,
      }),
      loadConceptOptions(),
    ])
  } catch {
    errorMessage.value = 'Check your connection and permissions, then try loading the capture inbox again.'
  }
}

async function loadConceptOptions() {
  conceptOptions.value = await getConcepts()
}

async function convertDefaultBlock(block: RecentNoteBlock) {
  const kind = defaultConversionKind(block)

  if (kind === 'ACTION_ITEM') {
    const title = await promptActionItemTitle(block)
    if (title === null) return
    await runConversion(block, kind, () => capture.convertByParsedType({ captureId: block.captureId, parsedType: block.parsedType, title }))
    return
  }

  await runConversion(block, kind, () => capture.convertByParsedType(block))
}

async function handleSecondaryConversion(block: RecentNoteBlock, command: unknown) {
  if (!isConversionKind(command)) return
  await convertBlockAs(block, command)
}

async function convertBlockAs(block: RecentNoteBlock, kind: ConversionKind) {
  if (kind === 'ACTION_ITEM') {
    const title = await promptActionItemTitle(block)
    if (title === null) return
    await runConversion(block, kind, () => capture.convertToActionItem(block.captureId, title))
    return
  }

  if (kind === 'QUOTE') {
    await runConversion(block, kind, () => capture.convertToQuote(block.captureId))
    return
  }

  await runConversion(block, kind, () => capture.convertToNote(block.captureId, block.title))
}

async function runConversion(
  block: RecentNoteBlock,
  kind: ConversionKind,
  convert: () => Promise<RawCaptureConversionRecord>,
) {
  setConverting(block.captureId, kind)
  try {
    const conversion = await convert()
    await handleConversionSuccess(kind, conversion)
  } catch (error) {
    ElMessage.error(conversionErrorMessage(error, kind))
  } finally {
    clearConverting(kind)
  }
}

async function archiveBlock(block: RecentNoteBlock) {
  archivingCaptureId.value = block.captureId
  try {
    await capture.archive(block.captureId)
    ElMessage.success('Capture archived.')
  } catch {
    ElMessage.error('Capture archive failed.')
  } finally {
    archivingCaptureId.value = null
  }
}

async function archiveSelected() {
  const ids = [...selectedCaptureIds.value]
  if (!ids.length) return

  bulkArchiving.value = true
  try {
    for (const captureId of ids) {
      await capture.archive(captureId)
    }
    selectedCaptureIds.value = []
    ElMessage.success('Selected captures archived.')
  } catch {
    ElMessage.error('Bulk archive stopped because one capture could not be archived.')
  } finally {
    bulkArchiving.value = false
  }
}

function openCaptureSource(block: RecentNoteBlock) {
  const sourceReference = block.sourceReferences[0] ?? null
  void openSource({
    sourceType: 'RAW_CAPTURE',
    sourceId: block.captureId,
    bookId: block.bookId,
    bookTitle: block.bookTitle,
    pageStart: block.pageStart,
    rawCaptureId: block.captureId,
    sourceReference,
    sourceReferenceId: sourceReference?.id ?? null,
  })
}

function openConceptReview(block: RecentNoteBlock) {
  selectedConceptBlock.value = block
  conceptDialogOpen.value = true
}

async function saveCaptureConceptReview(payload: ConceptReviewPayload) {
  if (!selectedConceptBlock.value) return
  savingConceptReview.value = true
  try {
    await reviewCaptureConcepts(selectedConceptBlock.value.captureId, payload)
    await loadConceptOptions()
    conceptDialogOpen.value = false
    ElMessage.success('Parsed concepts reviewed and source references preserved.')
  } catch {
    ElMessage.error('Concept review failed. Check required fields and permissions.')
  } finally {
    savingConceptReview.value = false
  }
}

function formatType(type: NoteBlockType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function displayTags(block: RecentNoteBlock) {
  return block.tags.filter((tag) => !block.concepts.includes(tag))
}

function canArchive(block: RecentNoteBlock) {
  return block.status !== 'CONVERTED' && block.status !== 'ARCHIVED'
}

function toggleSelected(captureId: number) {
  selectedCaptureIds.value = selectedCaptureIds.value.includes(captureId)
    ? selectedCaptureIds.value.filter((id) => id !== captureId)
    : [...selectedCaptureIds.value, captureId]
}

function sourceSummary(block: RecentNoteBlock) {
  const source = block.sourceReferences[0] ?? null
  if (!source) {
    return block.page ? `Current book, p.${block.page}` : 'Current book; page unknown.'
  }
  const page = pageLabel(source.pageStart, source.pageEnd)
  return [source.locationLabel, page, source.sourceConfidence ? `confidence ${source.sourceConfidence}` : null]
    .filter(Boolean)
    .join(' | ') || 'Source reference saved.'
}

function pageLabel(pageStart: number | null, pageEnd: number | null) {
  if (pageStart && pageEnd) return `p.${pageStart}-${pageEnd}`
  if (pageStart) return `p.${pageStart}`
  return null
}

function defaultConversionKind(block: RecentNoteBlock): ConversionKind {
  if (block.parsedType === 'QUOTE') return 'QUOTE'
  if (block.parsedType === 'ACTION_ITEM') return 'ACTION_ITEM'
  return 'NOTE'
}

function primaryConversionLabel(block: RecentNoteBlock) {
  const kind = defaultConversionKind(block)
  if (kind === 'QUOTE') return 'Convert to Quote'
  if (kind === 'ACTION_ITEM') return 'Convert to Action'
  return 'Convert to Note'
}

function isConversionKind(value: unknown): value is ConversionKind {
  return value === 'NOTE' || value === 'QUOTE' || value === 'ACTION_ITEM'
}

function isConverting(captureId: number) {
  return (
    convertingCaptureId.value === captureId ||
    convertingQuoteCaptureId.value === captureId ||
    convertingActionCaptureId.value === captureId
  )
}

function setConverting(captureId: number, kind: ConversionKind) {
  if (kind === 'NOTE') convertingCaptureId.value = captureId
  if (kind === 'QUOTE') convertingQuoteCaptureId.value = captureId
  if (kind === 'ACTION_ITEM') convertingActionCaptureId.value = captureId
}

function clearConverting(kind: ConversionKind) {
  if (kind === 'NOTE') convertingCaptureId.value = null
  if (kind === 'QUOTE') convertingQuoteCaptureId.value = null
  if (kind === 'ACTION_ITEM') convertingActionCaptureId.value = null
}

async function promptActionItemTitle(block: RecentNoteBlock) {
  try {
    const result = await ElMessageBox.prompt('Review the action item title before conversion. Leave it blank to use the backend default.', 'Convert to Action Item', {
      inputValue: defaultActionTitle(block),
      inputPlaceholder: 'Action item title',
      confirmButtonText: 'Convert',
      cancelButtonText: 'Cancel',
      distinguishCancelAndClose: true,
    })

    return result.value
  } catch (error) {
    if (error === 'cancel' || error === 'close') return null
    throw error
  }
}

function defaultActionTitle(block: RecentNoteBlock) {
  return block.content
    .split(/\r?\n/)
    .map((line) => line.trim())
    .find(Boolean) ?? block.title
}

async function handleConversionSuccess(kind: ConversionKind, conversion: RawCaptureConversionRecord) {
  if (kind === 'NOTE') {
    ElMessage.success('Capture converted to a formal note.')
    await router.push({ name: 'note-detail', params: { id: conversion.targetId } })
    return
  }

  if (kind === 'QUOTE') {
    ElMessage.success('Capture converted to a source-backed quote.')
    if (router.hasRoute('quote-detail')) {
      await router.push({ name: 'quote-detail', params: { id: conversion.targetId } })
    }
    return
  }

  ElMessage.success('Capture converted to a source-backed action item.')
  if (router.hasRoute('action-items')) {
    await router.push({ name: 'action-items' })
  }
}

function conversionErrorMessage(error: unknown, kind: ConversionKind) {
  const target = kind === 'ACTION_ITEM' ? 'action item' : kind.toLowerCase()

  if (!axios.isAxiosError(error)) {
    return `Capture conversion to ${target} failed.`
  }

  if (!error.response) {
    return 'Backend unavailable. Check that the API server is running and try again.'
  }

  const status = error.response.status
  const data = error.response.data as { message?: string } | undefined
  const serverMessage = data?.message

  if (status === 403) return 'Permission denied. You do not have access to convert this capture.'
  if (status === 409 || serverMessage?.toLowerCase().includes('already')) {
    return serverMessage ?? 'This capture was already converted or archived.'
  }
  if (status === 400) return serverMessage ?? 'Validation error. Review the capture content and try again.'

  return serverMessage ?? `Capture conversion to ${target} failed.`
}
</script>

<style scoped>
.capture-inbox-page {
  display: grid;
  gap: var(--space-5);
}

.capture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 320px), 1fr));
  gap: var(--space-4);
}

.capture-filters {
  padding: var(--space-4);
  display: flex;
  align-items: end;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.capture-filters label {
  min-width: min(220px, 100%);
  display: grid;
  gap: var(--space-2);
}

.capture-filters span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.capture-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.capture-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.capture-card__select {
  min-height: var(--touch-target);
  display: inline-flex;
  align-items: center;
}

.capture-card__select input {
  width: 18px;
  height: 18px;
  accent-color: var(--bookos-primary);
}

.capture-card__badges {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: flex-end;
}

.capture-card h2 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
  line-height: var(--type-title-line);
}

.capture-card__book-label {
  margin-bottom: var(--space-1);
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.capture-card__content {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.capture-card__meta,
.capture-card__taxonomy,
.capture-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.capture-card__meta {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 700;
}

.capture-card__taxonomy {
  align-items: flex-start;
}

.capture-card__source {
  padding: var(--space-3);
  display: grid;
  gap: var(--space-1);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.capture-card__source strong {
  color: var(--bookos-text-primary);
}

.capture-card__warnings {
  margin: 0;
  padding-left: var(--space-5);
  color: var(--bookos-warning);
  font-size: var(--type-metadata);
  line-height: var(--type-body-line);
}

@media (max-width: 640px) {
  .capture-filters {
    align-items: stretch;
  }

  .capture-filters label,
  .capture-filters :deep(.el-button) {
    width: 100%;
  }

  .capture-card {
    padding: var(--space-4);
  }

  .capture-card__header {
    flex-direction: column;
  }
}
</style>
