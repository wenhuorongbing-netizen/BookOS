<template>
  <div class="page-shell capture-inbox-page">
    <AppSectionHeader
      title="Capture Inbox"
      eyebrow="Quick Capture"
      :description="bookId ? 'Inbox captures filtered to the selected book.' : 'All unconverted quick captures that need processing.'"
      :level="1"
    >
      <template #actions>
        <RouterLink to="/my-library" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Open Library</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppLoadingState v-if="capture.loading" label="Loading capture inbox" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Capture inbox could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadInbox"
    />

    <AppEmptyState
      v-else-if="!capture.latestBlocks.length"
      title="No inbox captures"
      description="Use Quick Capture on a book detail page to save source-backed reading thoughts."
      eyebrow="Inbox clear"
    />

    <section v-else class="capture-grid" aria-label="Capture inbox items">
      <AppCard v-for="block in capture.latestBlocks" :key="block.captureId" class="capture-card" as="article">
        <div class="capture-card__header">
          <div>
            <div class="capture-card__book-label">Source book</div>
            <div class="eyebrow">{{ block.bookTitle }}</div>
            <h2>{{ block.title }}</h2>
          </div>
          <AppBadge variant="primary">{{ formatType(block.parsedType) }}</AppBadge>
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

        <div class="capture-card__actions">
          <AppButton
            variant="primary"
            :loading="isConverting(block.captureId)"
            :disabled="archivingCaptureId === block.captureId"
            @click="convertDefaultBlock(block)"
          >
            {{ primaryConversionLabel(block) }}
          </AppButton>

          <el-dropdown trigger="click" :disabled="isConverting(block.captureId)" @command="handleSecondaryConversion(block, $event)">
            <AppButton variant="secondary" :disabled="isConverting(block.captureId)" aria-label="Open conversion options">
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

          <AppButton variant="ghost" :disabled="isConverting(block.captureId) || archivingCaptureId === block.captureId" @click="archiveBlock(block)">
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
import type { ConceptRecord, ConceptReviewPayload, NoteBlockType, RawCaptureConversionRecord } from '../types'

type ConversionKind = 'NOTE' | 'QUOTE' | 'ACTION_ITEM'

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

const bookId = computed(() => {
  const value = route.query.bookId
  return typeof value === 'string' && value ? value : null
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
  try {
    await Promise.all([capture.loadInbox(bookId.value ?? undefined), loadConceptOptions()])
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

@media (max-width: 640px) {
  .capture-card {
    padding: var(--space-4);
  }

  .capture-card__header {
    flex-direction: column;
  }
}
</style>
