<template>
  <section class="capture-section" aria-labelledby="quick-capture-title">
    <AppCard as="section" class="quick-capture" variant="highlight">
      <div class="quick-capture__header">
        <div>
          <div class="eyebrow">Quick Capture</div>
          <h2 id="quick-capture-title">Capture while the source is still warm</h2>
          <p>Use raw syntax when you know it, or switch to beginner mode to generate the same parser-friendly text.</p>
        </div>
        <div class="quick-capture__header-actions">
          <HelpTooltip topic="quick-capture" placement="left" />
          <AppBadge variant="primary" size="sm">{{ book.title }}</AppBadge>
          <RouterLink :to="{ name: 'capture-inbox', query: { bookId: String(book.id) } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open Inbox</AppButton>
          </RouterLink>
        </div>
      </div>

      <div class="quick-capture__mode" role="group" aria-label="Capture mode">
        <AppButton variant="secondary" :selected="captureMode === 'raw'" @click="captureMode = 'raw'">Raw text</AppButton>
        <AppButton variant="secondary" :selected="captureMode === 'beginner'" @click="captureMode = 'beginner'">Beginner mode</AppButton>
      </div>

      <AppCard class="capture-guide" as="section" variant="muted" aria-labelledby="capture-guide-title">
        <div class="capture-guide__header">
          <div>
            <p class="eyebrow">Capture Guide</p>
            <h3 id="capture-guide-title">Learn the parser by trying safe examples</h3>
            <p>Examples only fill the input. Nothing is saved until you press Capture.</p>
          </div>
          <RouterLink to="/captures/inbox" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Review inbox rules</AppButton>
          </RouterLink>
        </div>
        <div class="capture-guide__examples" aria-label="One-click capture examples">
          <AppButton v-for="example in guideExamples" :key="example.label" variant="subtle" @click="insertExample(example)">
            {{ example.label }}
          </AppButton>
        </div>
        <ul class="capture-guide__syntax" aria-label="Supported capture syntax">
          <li><strong>Quote:</strong> <code>&#x1F4AC; p.42 "Original quote note" #quote [[Game Feel]]</code></li>
          <li><strong>Action:</strong> <code>&#x2705; page 80 Playtest this loop tomorrow #todo [[Feedback Loop]]</code></li>
          <li><strong>Idea:</strong> <code>&#x1F4A1; p.12 This could become a prototype task #prototype [[Core Loop]]</code></li>
          <li><strong>Concept:</strong> <code>&#x1F9E9; &#x7B2C;42&#x9875; Connect this to [[Meaningful Choice]] #concept</code></li>
          <li><strong>Question:</strong> <code>&#x2753; &#x9875;18 Why does this mechanic create tension? #question [[Risk vs Reward]]</code></li>
        </ul>
      </AppCard>

      <label v-if="captureMode === 'raw'" class="quick-capture__field" for="quick-capture-input">
        <span class="sr-only">Quick capture text</span>
        <el-input
          id="quick-capture-input"
          v-model="draft"
          type="textarea"
          :rows="5"
          placeholder="Capture a thought, quote, or idea..."
          @keydown.ctrl.enter.prevent="submitCapture"
          @keydown.meta.enter.prevent="submitCapture"
        />
      </label>

      <div v-else class="structured-capture" aria-labelledby="structured-capture-title">
        <div>
          <p class="eyebrow">Beginner Mode</p>
          <h3 id="structured-capture-title">Build a capture without memorizing syntax</h3>
        </div>
        <div class="structured-capture__grid">
          <label class="structured-capture__field">
            <span>Type</span>
            <el-select v-model="structuredForm.type" aria-label="Beginner capture type">
              <el-option v-for="option in structuredTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </label>
          <label class="structured-capture__field">
            <span>Page or location</span>
            <el-input v-model="structuredForm.page" aria-label="Beginner page marker" placeholder="42, p.42, page 42, &#x9875;42, or &#x7B2C;42&#x9875;" />
          </label>
          <label class="structured-capture__field structured-capture__field--wide">
            <span>Content</span>
            <el-input v-model="structuredForm.content" type="textarea" :rows="3" aria-label="Beginner capture content" placeholder="Write the thought in your own words." />
          </label>
          <label class="structured-capture__field">
            <span>Tags</span>
            <el-input v-model="structuredForm.tags" aria-label="Beginner tags" placeholder="prototype, todo" />
          </label>
          <label class="structured-capture__field">
            <span>Concepts</span>
            <el-input v-model="structuredForm.concepts" aria-label="Beginner concepts" placeholder="Core Loop, Feedback Loop" />
          </label>
        </div>
        <div class="structured-capture__generated" aria-live="polite">
          <span>Generated raw capture</span>
          <code>{{ generatedStructuredRaw || 'Add content to generate parser-friendly text.' }}</code>
          <AppButton variant="ghost" :disabled="!generatedStructuredRaw" @click="copyStructuredToRaw">Use in raw input</AppButton>
        </div>
      </div>

      <div class="quick-capture__bar">
        <div class="quick-capture__tools" role="toolbar" aria-label="Capture markers">
          <AppIconButton
            v-for="marker in markers"
            :key="marker.value"
            :label="marker.label"
            :tooltip="marker.label"
            :selected="selectedMarker === marker.value"
            variant="ghost"
            @click="selectedMarker = marker.value"
          >
            {{ marker.icon }}
          </AppIconButton>
        </div>

        <div class="quick-capture__submit">
          <span class="quick-capture__shortcut">Ctrl/Cmd + Enter to capture</span>
          <span class="quick-capture__shortcut">Ctrl/Cmd + Shift + C to focus</span>
          <AppButton
            variant="primary"
            aria-label="Submit quick capture"
            :loading="submitting"
            :disabled="!canSubmitCapture"
            @click="submitCapture"
          >
            Capture
          </AppButton>
        </div>
      </div>

      <div v-if="activePreviewText || parsedPreview || previewLoading" class="quick-capture__preview" aria-live="polite">
        <div class="quick-capture__preview-heading">
          <div>
            <p class="eyebrow">Live parser preview</p>
            <h3>What BookOS will preserve</h3>
          </div>
          <AppBadge variant="success" size="sm">Source: {{ book.title }}</AppBadge>
        </div>
        <AppLoadingState v-if="previewLoading" label="Parsing capture preview" compact />
        <template v-else-if="parsedPreview">
          <dl class="quick-capture__preview-grid">
            <div>
              <dt>Parsed type</dt>
              <dd><AppBadge variant="primary" size="sm">{{ formatType(parsedPreview.type) }}</AppBadge></dd>
            </div>
            <div>
              <dt>Page</dt>
              <dd>
                <AppBadge v-if="parsedPreview.pageStart" variant="accent" size="sm">
                  {{ pageLabel(parsedPreview.pageStart, parsedPreview.pageEnd) }}
                </AppBadge>
                <span v-else class="quick-capture__preview-muted">Page unknown; saved page stays null.</span>
              </dd>
            </div>
            <div>
              <dt>Tags</dt>
              <dd class="quick-capture__preview-row">
                <AppBadge v-for="tag in parsedPreview.tags" :key="tag" variant="neutral" size="sm">#{{ tag }}</AppBadge>
                <span v-if="!parsedPreview.tags.length" class="quick-capture__preview-muted">No tags</span>
              </dd>
            </div>
            <div>
              <dt>Concepts</dt>
              <dd class="quick-capture__preview-row">
                <AppBadge v-for="concept in parsedPreview.concepts" :key="concept" variant="info" size="sm">[[{{ concept }}]]</AppBadge>
                <span v-if="!parsedPreview.concepts.length" class="quick-capture__preview-muted">No concepts</span>
              </dd>
            </div>
          </dl>
          <p>{{ parsedPreview.cleanText || 'No clean text extracted yet.' }}</p>
          <div class="quick-capture__source-preview">
            <strong>Source reference preview</strong>
            <span>Book: {{ book.title }}</span>
            <span>{{ parsedPreview.pageStart ? `Page: ${pageLabel(parsedPreview.pageStart, parsedPreview.pageEnd)}` : 'Page: unknown, stored as null' }}</span>
            <span>Confidence: {{ parsedPreview.pageStart ? 'MEDIUM' : 'LOW' }}</span>
          </div>
          <ul v-if="allPreviewWarnings.length" class="quick-capture__warnings">
            <li v-for="warning in allPreviewWarnings" :key="warning">{{ warning }}</li>
          </ul>
        </template>
      </div>

      <div
        v-if="lastSavedBlock"
        class="quick-capture__next-steps"
        role="region"
        aria-label="Post-save quick capture actions"
        aria-live="polite"
      >
        <div>
          <p class="eyebrow">Post-save next step</p>
          <h3>Convert or review this capture now</h3>
          <p>{{ lastSavedBlock.title }}</p>
        </div>
        <div class="quick-capture__next-actions">
          <AppButton variant="secondary" :loading="convertingCaptureId === lastSavedBlock.captureId" @click="convertBlock(lastSavedBlock)">
            Convert to Note
          </AppButton>
          <AppButton variant="secondary" :loading="convertingQuoteCaptureId === lastSavedBlock.captureId" @click="convertQuoteBlock(lastSavedBlock)">
            Convert to Quote
          </AppButton>
          <AppButton variant="secondary" :loading="convertingActionCaptureId === lastSavedBlock.captureId" @click="convertActionBlock(lastSavedBlock)">
            Convert to Action
          </AppButton>
          <AppButton variant="accent" :disabled="!lastSavedBlock.concepts.length" @click="openConceptReview(lastSavedBlock)">
            Review Concept
          </AppButton>
          <RouterLink :to="{ name: 'capture-inbox', query: { bookId: String(book.id) } }" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Open Inbox</AppButton>
          </RouterLink>
        </div>
      </div>

      <p class="quick-capture__feedback" role="status" aria-live="polite">{{ feedback }}</p>
    </AppCard>

    <AppCard as="section" class="recent-notes" variant="default" aria-labelledby="recent-note-blocks-title">
      <div class="recent-notes__header">
        <div>
          <div class="eyebrow">Recent Note Blocks</div>
          <h2 id="recent-note-blocks-title">Captured from this book</h2>
        </div>
        <label class="recent-notes__sort">
          <span>Sort</span>
          <el-select v-model="sortOrder" aria-label="Sort recent note blocks">
            <el-option label="Latest first" value="latest" />
            <el-option label="Oldest first" value="oldest" />
          </el-select>
        </label>
      </div>

      <AppLoadingState v-if="capture.loading" label="Loading capture inbox" />
      <AppErrorState
        v-else-if="capture.error"
        title="Recent captures unavailable"
        :description="capture.error"
        retry-label="Retry captures"
        @retry="capture.loadBookCaptures(book.id)"
      />

      <div v-else-if="sortedBlocks.length" class="note-block-list">
        <article v-for="block in sortedBlocks" :key="block.id" class="note-block-card">
          <button class="note-block-card__main" type="button" :aria-label="`Open note block ${block.title}`" @click="openSource(block)">
            <span class="note-block-card__icon" aria-hidden="true">{{ markerIcon(block.type) }}</span>
            <span class="note-block-card__copy">
              <strong>{{ block.title }}</strong>
              <span>{{ block.content }}</span>
            </span>
          </button>

          <div class="note-block-card__meta">
            <button v-if="block.page" class="note-block-card__page" type="button" @click="openSource(block)">p.{{ block.page }}</button>
            <button v-for="tag in block.tags" :key="tag" class="note-block-card__tag" type="button" @click="openTag(tag)">#{{ tag }}</button>
            <AppBadge v-if="block.sourceReferences[0]" variant="success" size="sm">
              Source {{ block.sourceReferences[0].sourceConfidence }}
            </AppBadge>
            <span>{{ formatDate(block.createdAt) }}</span>
          </div>
          <ul v-if="block.parserWarnings.length" class="note-block-card__warnings" aria-label="Parser warnings">
            <li v-for="warning in block.parserWarnings" :key="warning">{{ warning }}</li>
          </ul>

          <div class="note-block-card__actions">
            <AppButton
              variant="secondary"
              :loading="convertingCaptureId === block.captureId"
              @click="convertDefaultBlock(block)"
            >
              {{ primaryConversionLabel(block) }}
            </AppButton>
            <AppButton
              v-if="block.parsedType === 'QUOTE'"
              variant="secondary"
              :loading="convertingQuoteCaptureId === block.captureId"
              @click="convertQuoteBlock(block)"
            >
              Convert to Quote
            </AppButton>
            <AppButton
              v-if="block.parsedType === 'ACTION_ITEM'"
              variant="secondary"
              :loading="convertingActionCaptureId === block.captureId"
              @click="convertActionBlock(block)"
            >
              Convert to Action
            </AppButton>
            <AppButton v-if="block.concepts.length" variant="accent" @click="openConceptReview(block)">
              Review Concepts
            </AppButton>
            <AppButton
              variant="ghost"
              :disabled="archivingCaptureId === block.captureId"
              @click="archiveBlock(block)"
            >
              Archive
            </AppButton>
            <AppIconButton
              :label="block.bookmarked ? `Remove bookmark from ${block.title}` : `Bookmark ${block.title}`"
              :selected="block.bookmarked"
              variant="ghost"
              @click="capture.toggleBookmark(block.id)"
            >
              {{ block.bookmarked ? 'BK' : 'BM' }}
            </AppIconButton>
            <AppIconButton label="More note block actions" variant="ghost" @click="openSource(block)">MO</AppIconButton>
          </div>
        </article>
      </div>

      <AppEmptyState
        v-else
        title="No note blocks yet"
        description="Use Quick Capture above to create a persisted inbox capture for this book."
        compact
      />
    </AppCard>

    <ConceptReviewDialog
      v-model="conceptDialogOpen"
      :parsed-concepts="selectedConceptBlock?.concepts ?? []"
      :concepts="conceptOptions"
      :source-reference="selectedConceptBlock?.sourceReferences[0] ?? null"
      :default-tags="selectedConceptBlock ? selectedConceptBlock.tags.filter((tag) => !selectedConceptBlock?.concepts.includes(tag)) : []"
      :saving="savingConceptReview"
      @submit="saveCaptureConceptReview"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { onBeforeRouteLeave, RouterLink, useRouter } from 'vue-router'
import { getConcepts } from '../../api/knowledge'
import { reviewCaptureConcepts } from '../../api/captures'
import { previewParsedNote } from '../../api/parser'
import type { BookRecord, ConceptRecord, ConceptReviewPayload, NoteBlockType, ParsedNoteResult } from '../../types'
import { useCaptureStore, type CaptureMarker, type RecentNoteBlock } from '../../stores/capture'
import { useRightRailStore } from '../../stores/rightRail'
import ConceptReviewDialog from '../concept/ConceptReviewDialog.vue'
import HelpTooltip from '../help/HelpTooltip.vue'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import AppIconButton from '../ui/AppIconButton.vue'
import AppLoadingState from '../ui/AppLoadingState.vue'
import { useOpenSource } from '../../composables/useOpenSource'

const props = defineProps<{
  book: BookRecord
}>()

const router = useRouter()
const capture = useCaptureStore()
const rightRail = useRightRailStore()
const { openSource: openSourceDrawer } = useOpenSource()
const draft = ref('')
const feedback = ref('')
const captureMode = ref<'raw' | 'beginner'>('raw')
const selectedMarker = ref<CaptureMarker>('text')
const sortOrder = ref<'latest' | 'oldest'>('latest')
const submitting = ref(false)
const previewLoading = ref(false)
const parsedPreview = ref<ParsedNoteResult | null>(null)
const convertingCaptureId = ref<number | null>(null)
const convertingQuoteCaptureId = ref<number | null>(null)
const convertingActionCaptureId = ref<number | null>(null)
const archivingCaptureId = ref<number | null>(null)
const conceptDialogOpen = ref(false)
const selectedConceptBlock = ref<RecentNoteBlock | null>(null)
const conceptOptions = ref<ConceptRecord[]>([])
const savingConceptReview = ref(false)
const preserveSavedPreview = ref(false)
const lastSavedBlock = ref<RecentNoteBlock | null>(null)
const structuredForm = reactive({
  type: 'IDEA' as StructuredCaptureType,
  page: '',
  content: '',
  tags: '',
  concepts: '',
})
let previewTimer: number | null = null

type StructuredCaptureType = 'QUOTE' | 'ACTION_ITEM' | 'IDEA' | 'RELATED_CONCEPT' | 'QUESTION'

interface CaptureExample {
  label: string
  rawText: string
  marker: CaptureMarker
}

const markers: Array<{ value: CaptureMarker; label: string; icon: string }> = [
  { value: 'text', label: 'Text note marker', icon: 'TX' },
  { value: 'quote', label: 'Quote marker', icon: 'QT' },
  { value: 'link', label: 'Link marker', icon: 'LN' },
  { value: 'emoji', label: 'Emoji marker', icon: ':)' },
  { value: 'inspiration', label: 'Inspiration marker', icon: 'IN' },
  { value: 'favorite', label: 'Favorite marker', icon: 'ST' },
  { value: 'important', label: 'Important marker', icon: 'FI' },
  { value: 'question', label: 'Question marker', icon: '?' },
  { value: 'idea', label: 'Lightbulb idea marker', icon: 'LB' },
]

const guideExamples: CaptureExample[] = [
  {
    label: 'Insert quote example',
    rawText: '\uD83D\uDCAC p.42 "Readable feedback matters in a prototype." #quote [[Game Feel]]',
    marker: 'quote',
  },
  {
    label: 'Insert action example',
    rawText: '\u2705 page 80 Playtest the feedback loop tomorrow. #todo [[Feedback Loop]]',
    marker: 'important',
  },
  {
    label: 'Insert concept example',
    rawText: '\uD83E\uDDE9 \u7B2C42\u9875 Connect this to [[Meaningful Choice]] #concept',
    marker: 'idea',
  },
  {
    label: 'Insert project idea example',
    rawText: '\uD83D\uDCA1 \u987512 This could become a prototype task. #prototype [[Core Loop]]',
    marker: 'idea',
  },
]

const structuredTypeOptions: Array<{ value: StructuredCaptureType; label: string; marker: string }> = [
  { value: 'QUOTE', label: 'Quote', marker: '\uD83D\uDCAC' },
  { value: 'ACTION_ITEM', label: 'Action item', marker: '\u2705' },
  { value: 'IDEA', label: 'Idea / inspiration', marker: '\uD83D\uDCA1' },
  { value: 'RELATED_CONCEPT', label: 'Concept link', marker: '\uD83E\uDDE9' },
  { value: 'QUESTION', label: 'Question', marker: '\u2753' },
]

const sortedBlocks = computed(() => {
  const hiddenCaptureId = lastSavedBlock.value?.captureId ?? null
  const blocks = capture.forBook(props.book.id).filter((block) => block.captureId !== hiddenCaptureId)
  if (sortOrder.value === 'oldest') {
    return [...blocks].reverse()
  }
  return blocks
})
const generatedStructuredRaw = computed(() => {
  if (!structuredForm.content.trim()) return ''
  const marker = structuredTypeOptions.find((option) => option.value === structuredForm.type)?.marker ?? '\uD83D\uDCA1'
  const page = normalizeStructuredPage(structuredForm.page)
  const tags = parseTokenList(structuredForm.tags)
    .map((tag) => `#${tag.replace(/^#/, '')}`)
    .join(' ')
  const concepts = parseTokenList(structuredForm.concepts)
    .map((concept) => `[[${concept.replace(/^\[\[|\]\]$/g, '')}]]`)
    .join(' ')

  return [marker, page, structuredForm.content.trim(), tags, concepts].filter(Boolean).join(' ')
})
const activePreviewText = computed(() => previewRawText())
const canSubmitCapture = computed(() => {
  if (captureMode.value === 'beginner') return Boolean(structuredForm.content.trim())
  return Boolean(draft.value.trim())
})
const localPreviewWarnings = computed(() => {
  const warnings: string[] = []
  if (activePreviewText.value && hasMalformedPageMarker(activePreviewText.value)) {
    warnings.push('Page marker looks malformed. Use p.42, page 42, \u987542, or \u7B2C42\u9875. Unknown pages are saved as null.')
  }
  return warnings
})
const allPreviewWarnings = computed(() => [...(parsedPreview.value?.warnings ?? []), ...localPreviewWarnings.value])

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  window.addEventListener('keydown', handleGlobalQuickCaptureShortcut)
  void capture.loadBookCaptures(props.book.id).catch(() => undefined)
  void loadConceptOptions()
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  window.removeEventListener('keydown', handleGlobalQuickCaptureShortcut)
  if (previewTimer) window.clearTimeout(previewTimer)
})

onBeforeRouteLeave(() => {
  if (!hasUnsavedDraft()) return true
  return window.confirm('You have an unsaved quick capture draft. Leave this page and discard it?')
})

watch(
  () => props.book.id,
  (bookId) => {
    void capture.loadBookCaptures(bookId).catch(() => undefined)
  },
)

watch(
  [
    draft,
    selectedMarker,
    captureMode,
    () => structuredForm.type,
    () => structuredForm.page,
    () => structuredForm.content,
    () => structuredForm.tags,
    () => structuredForm.concepts,
  ],
  () => {
    scheduleParserPreview()
  },
)

async function submitCapture() {
  const value = activePreviewText.value.trim()
  if (!value) {
    feedback.value = 'Write a thought before capturing.'
    ElMessage.warning(feedback.value)
    return
  }

  submitting.value = true
  try {
    const marker = captureMode.value === 'beginner' ? 'text' : selectedMarker.value
    const block = await capture.addCapture(props.book, value, marker)
    preserveSavedPreview.value = true
    clearCurrentInput()
    lastSavedBlock.value = block
    parsedPreview.value = toParsedPreview(block)
    feedback.value = `Captured ${markerLabel(block.type)} for ${props.book.title}.`
    ElMessage.success(feedback.value)
    openSource(block, false)
  } catch {
    feedback.value = 'Capture could not be saved. Add this book to your library first if needed.'
    ElMessage.error(feedback.value)
  } finally {
    submitting.value = false
  }
}

function insertExample(example: CaptureExample) {
  captureMode.value = 'raw'
  selectedMarker.value = example.marker
  draft.value = example.rawText
  lastSavedBlock.value = null
  feedback.value = 'Example inserted. Edit it if needed, then press Capture to save it.'
  window.setTimeout(() => document.getElementById('quick-capture-input')?.focus(), 0)
}

function copyStructuredToRaw() {
  if (!generatedStructuredRaw.value) return
  captureMode.value = 'raw'
  selectedMarker.value = 'text'
  draft.value = generatedStructuredRaw.value
  feedback.value = 'Generated capture copied to raw text. Nothing has been saved yet.'
  window.setTimeout(() => document.getElementById('quick-capture-input')?.focus(), 0)
}

function clearCurrentInput() {
  if (captureMode.value === 'beginner') {
    structuredForm.page = ''
    structuredForm.content = ''
    structuredForm.tags = ''
    structuredForm.concepts = ''
    return
  }
  draft.value = ''
}

function scheduleParserPreview() {
  if (previewTimer) window.clearTimeout(previewTimer)
  if (!activePreviewText.value.trim()) {
    if (preserveSavedPreview.value) {
      preserveSavedPreview.value = false
      return
    }
    parsedPreview.value = null
    previewLoading.value = false
    return
  }

  previewTimer = window.setTimeout(() => {
    void loadParserPreview()
  }, 300)
}

async function loadParserPreview() {
  const rawText = previewRawText()
  if (!rawText.trim()) return

  previewLoading.value = true
  try {
    parsedPreview.value = await previewParsedNote({ rawText })
  } catch {
    parsedPreview.value = null
  } finally {
    previewLoading.value = false
  }
}

function openSource(block: RecentNoteBlock, navigate = true) {
  const sourceReference = block.sourceReferences[0] ?? null
  rightRail.setSourceReference({
    id: block.id,
    type: 'note',
    bookId: block.bookId,
    bookTitle: block.bookTitle,
    subtitle: props.book.subtitle,
    author: props.book.authors.join(', ') || 'Unknown author',
    coverUrl: props.book.coverImageUrl ?? props.book.coverUrl,
    chapter: props.book.currentChapter ?? null,
    pageRange: block.page ? `p.${block.page}` : props.book.pageRange ?? null,
    location: `${markerLabel(block.type)} note block`,
    addedAt: block.createdAt,
    excerpt: block.content,
  })

  if (navigate) {
    void openSourceDrawer({
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
}

function openTag(tag: string) {
  router.push({ name: 'dashboard', query: { focus: 'tag', tag, bookId: String(props.book.id) } })
}

async function convertBlock(block: RecentNoteBlock) {
  convertingCaptureId.value = block.captureId
  try {
    const conversion = await capture.convertToNote(block.captureId, block.title)
    lastSavedBlock.value = null
    ElMessage.success('Capture converted to a formal note.')
    router.push({ name: 'note-detail', params: { id: conversion.targetId } })
  } catch {
    ElMessage.error('Capture conversion failed.')
  } finally {
    convertingCaptureId.value = null
  }
}

async function convertDefaultBlock(block: RecentNoteBlock) {
  if (block.parsedType === 'QUOTE') {
    await convertQuoteBlock(block)
    return
  }

  if (block.parsedType === 'ACTION_ITEM') {
    await convertActionBlock(block)
    return
  }

  await convertBlock(block)
}

async function convertQuoteBlock(block: RecentNoteBlock) {
  convertingQuoteCaptureId.value = block.captureId
  try {
    await capture.convertToQuote(block.captureId)
    lastSavedBlock.value = null
    ElMessage.success('Capture converted to a source-backed quote.')
  } catch {
    ElMessage.error('Quote conversion failed.')
  } finally {
    convertingQuoteCaptureId.value = null
  }
}

async function convertActionBlock(block: RecentNoteBlock) {
  convertingActionCaptureId.value = block.captureId
  try {
    await capture.convertToActionItem(block.captureId, block.title)
    lastSavedBlock.value = null
    ElMessage.success('Capture converted to a source-backed action item.')
  } catch {
    ElMessage.error('Action item conversion failed.')
  } finally {
    convertingActionCaptureId.value = null
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
    ElMessage.success('Concept review saved with source references.')
  } catch {
    ElMessage.error('Concept review failed.')
  } finally {
    savingConceptReview.value = false
  }
}

async function loadConceptOptions() {
  try {
    conceptOptions.value = await getConcepts()
  } catch {
    conceptOptions.value = []
  }
}

function markerIcon(marker: CaptureMarker) {
  return markers.find((item) => item.value === marker)?.icon ?? 'TX'
}

function markerLabel(marker: CaptureMarker) {
  return markers.find((item) => item.value === marker)?.label.replace(' marker', '') ?? 'Text note'
}

function primaryConversionLabel(block: RecentNoteBlock) {
  if (block.parsedType === 'QUOTE') return 'Convert to Quote'
  if (block.parsedType === 'ACTION_ITEM') return 'Convert to Action'
  return 'Convert to Note'
}

function formatType(type: NoteBlockType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function pageLabel(pageStart: number | null, pageEnd: number | null) {
  if (pageStart && pageEnd) return `p.${pageStart}-${pageEnd}`
  if (pageStart) return `p.${pageStart}`
  return 'No page'
}

function previewRawText() {
  if (captureMode.value === 'beginner') {
    return generatedStructuredRaw.value.trim()
  }

  const trimmed = draft.value.trim()
  if (!trimmed || selectedMarker.value === 'text' || selectedMarker.value === 'emoji' || startsWithKnownMarker(trimmed)) {
    return trimmed
  }
  return `${markerPrefix(selectedMarker.value)} ${trimmed}`.trim()
}

function normalizeStructuredPage(value: string) {
  const trimmed = value.trim()
  if (!trimmed) return ''
  if (/^\d+$/.test(trimmed)) return `p.${trimmed}`
  return trimmed
}

function parseTokenList(value: string) {
  return value
    .split(/[,;\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function hasMalformedPageMarker(value: string) {
  return /\bp\.?\s*(?:$|[^\d\s#\[])/i.test(value)
    || /\bpage\s*(?:$|[^\d\s#\[])/i.test(value)
    || /第\s*(?:页|[^\d\s])/.test(value)
    || /页\s*(?:$|[^\d\s#\[])/.test(value)
}

function markerPrefix(marker: CaptureMarker) {
  const prefixes: Partial<Record<CaptureMarker, string>> = {
    quote: '\uD83D\uDCAC',
    link: '\uD83D\uDD17',
    inspiration: '\uD83D\uDCA1',
    favorite: '\uD83D\uDCCC',
    important: '\uD83D\uDCCC',
    question: '\u2753',
    idea: '\uD83D\uDCA1',
  }
  return prefixes[marker] ?? ''
}

function startsWithKnownMarker(value: string) {
  return /^[\u2600-\u27BF]|\uD83C|\uD83D|\uD83E/.test(value)
}

function toParsedPreview(block: RecentNoteBlock): ParsedNoteResult {
  return {
    type: block.parsedType,
    pageStart: block.pageStart,
    pageEnd: block.pageEnd,
    tags: block.tags.filter((tag) => !block.concepts.includes(tag)),
    concepts: block.concepts,
    cleanText: block.content,
    rawText: block.content,
    warnings: block.parserWarnings,
  }
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (!hasUnsavedDraft()) return
  event.preventDefault()
  event.returnValue = ''
}

function handleGlobalQuickCaptureShortcut(event: KeyboardEvent) {
  if (!(event.ctrlKey || event.metaKey) || !event.shiftKey || event.key.toLowerCase() !== 'c') return
  event.preventDefault()
  document.getElementById('quick-capture-input')?.focus()
}

function hasUnsavedDraft() {
  return captureMode.value === 'beginner'
    ? Boolean(structuredForm.page.trim() || structuredForm.content.trim() || structuredForm.tags.trim() || structuredForm.concepts.trim())
    : Boolean(draft.value.trim())
}
</script>

<style scoped>
.capture-section {
  display: grid;
  gap: var(--space-4);
}

.quick-capture,
.recent-notes {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.quick-capture__header,
.recent-notes__header,
.capture-guide__header,
.quick-capture__bar,
.note-block-card {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
}

.quick-capture__header,
.recent-notes__header,
.capture-guide__header {
  align-items: flex-start;
}

.quick-capture__header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-capture h2,
.recent-notes h2 {
  scroll-margin-top: 112px;
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-section-title);
  line-height: var(--type-title-line);
}

.quick-capture h3,
.capture-guide h3,
.structured-capture h3,
.quick-capture__next-steps h3 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
}

.quick-capture__header p,
.capture-guide p,
.quick-capture__next-steps p {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.quick-capture__mode,
.capture-guide__examples,
.capture-guide__syntax,
.structured-capture,
.quick-capture__next-steps {
  display: grid;
  gap: var(--space-3);
}

.quick-capture__mode,
.capture-guide__examples,
.quick-capture__next-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.capture-guide,
.structured-capture,
.quick-capture__next-steps {
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: color-mix(in srgb, var(--bookos-surface) 74%, var(--bookos-surface-muted));
}

.capture-guide__syntax {
  margin: 0;
  padding-left: var(--space-5);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.capture-guide__syntax code,
.structured-capture__generated code {
  color: var(--bookos-primary-hover);
  font-weight: 800;
  white-space: normal;
}

.quick-capture__field {
  display: grid;
}

.structured-capture__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.structured-capture__field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 900;
}

.structured-capture__field--wide {
  grid-column: 1 / -1;
}

.structured-capture__generated {
  padding: var(--space-3);
  display: grid;
  gap: var(--space-2);
  border: 1px dashed var(--bookos-border-strong);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
}

.structured-capture__generated span {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.quick-capture__bar {
  align-items: center;
  flex-wrap: wrap;
}

.quick-capture__tools {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-capture__submit {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.quick-capture__shortcut,
.quick-capture__feedback,
.recent-notes__sort span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.quick-capture__feedback {
  min-height: 1.4em;
  margin: 0;
}

.quick-capture__preview {
  padding: var(--space-3);
  display: grid;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
}

.quick-capture__preview-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.quick-capture__preview-heading h3 {
  margin: 0;
}

.quick-capture__preview p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.quick-capture__preview-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
}

.quick-capture__preview-grid dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.quick-capture__preview-grid dd {
  margin: var(--space-1) 0 0;
}

.quick-capture__preview-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-capture__preview-muted {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.quick-capture__source-preview {
  padding: var(--space-3);
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.quick-capture__source-preview strong {
  color: var(--bookos-text-primary);
}

.quick-capture__warnings,
.note-block-card__warnings {
  margin: 0;
  padding-left: var(--space-5);
  color: var(--bookos-warning);
  font-size: var(--type-metadata);
  line-height: var(--type-body-line);
}

.recent-notes__sort {
  min-width: 180px;
  display: grid;
  gap: var(--space-2);
}

.note-block-list {
  display: grid;
  gap: var(--space-3);
}

.note-block-card {
  align-items: flex-start;
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  box-shadow: 0 10px 24px rgba(54, 42, 24, 0.05);
}

.note-block-card__main {
  min-width: 0;
  flex: 1;
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: var(--space-3);
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.note-block-card__icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
  font-size: 0.72rem;
  font-weight: 900;
}

.note-block-card__copy {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
}

.note-block-card__copy strong {
  color: var(--bookos-text-primary);
}

.note-block-card__copy span {
  color: var(--bookos-text-secondary);
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.note-block-card__meta,
.note-block-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.note-block-card__meta {
  max-width: 280px;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 700;
}

.note-block-card__page,
.note-block-card__tag {
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface-muted);
  color: var(--bookos-primary-hover);
  font-size: var(--type-metadata);
  font-weight: 800;
  cursor: pointer;
}

.note-block-card__tag {
  color: var(--bookos-accent-hover);
}

@media (max-width: 900px) {
  .quick-capture__header,
  .recent-notes__header,
  .capture-guide__header,
  .quick-capture__bar,
  .note-block-card {
    flex-direction: column;
  }

  .structured-capture__grid,
  .quick-capture__preview-grid {
    grid-template-columns: 1fr;
  }

  .recent-notes__sort {
    width: 100%;
  }

  .note-block-card__meta {
    max-width: none;
  }

  .quick-capture__submit,
  .quick-capture__submit :deep(.el-button) {
    width: 100%;
  }
}
</style>
