<template>
  <div class="first-loop-page">
    <section class="first-loop-hero" aria-labelledby="first-loop-title">
      <div>
        <p class="eyebrow">First Valuable Loop</p>
        <h1 id="first-loop-title">Create one source-backed record</h1>
        <p>
          Follow one focused path: add or choose a book, mark it as reading, capture one original thought,
          process it into a durable record, then open its source.
        </p>
      </div>
      <div class="first-loop-hero__actions">
        <AppBadge variant="primary" size="md">{{ completedCount }}/{{ steps.length }} steps</AppBadge>
        <AppButton variant="secondary" @click="exitToDashboard">Exit and resume later</AppButton>
      </div>
    </section>

    <ol class="first-loop-progress" aria-label="First valuable loop progress">
      <li
        v-for="(step, index) in steps"
        :key="step.key"
        :class="{ active: activeStep === step.key, complete: completedStepKeys.has(step.key) }"
      >
        <button type="button" :aria-current="activeStep === step.key ? 'step' : undefined" @click="activeStep = step.key">
          <span>{{ index + 1 }}</span>
          {{ step.label }}
        </button>
      </li>
    </ol>

    <AppErrorState
      v-if="loadError"
      title="Guided loop could not load"
      :description="loadError"
      retry-label="Retry"
      @retry="loadInitialState"
    />
    <AppLoadingState v-else-if="loading" label="Loading your first loop state" />

    <template v-else>
      <AppCard v-if="activeStep === 'book'" class="loop-panel" as="section" aria-labelledby="loop-book-title">
        <div class="loop-panel__header">
          <div>
            <p class="eyebrow">Step 1</p>
            <h2 id="loop-book-title">Add or choose a source book</h2>
            <p>This must be a real book context. Demo data and fake sample books are not created here.</p>
          </div>
          <AppBadge :variant="selectedBook ? 'success' : 'warning'" size="sm">
            {{ selectedBook ? 'Book selected' : 'Needs book' }}
          </AppBadge>
        </div>

        <div v-if="bookOptions.length" class="existing-book-picker">
          <label for="first-loop-book">Choose an existing book</label>
          <select id="first-loop-book" v-model.number="selectedBookId" @change="chooseExistingBook">
            <option :value="0">Select a book</option>
            <option v-for="book in bookOptions" :key="book.id" :value="book.id">
              {{ book.title }}
            </option>
          </select>
          <p v-if="selectedBook" class="helper-text">
            Selected: {{ selectedBook.title }}. If it is not in your library yet, the next step will attach it safely.
          </p>
        </div>

        <div class="book-create-grid" aria-label="Create a first-loop book">
          <label>
            <span>Book title</span>
            <input v-model="bookForm.title" type="text" placeholder="Example: My Current Reading Book" />
          </label>
          <label>
            <span>Author</span>
            <input v-model="bookForm.author" type="text" placeholder="Optional author" />
          </label>
          <label>
            <span>Category</span>
            <input v-model="bookForm.category" type="text" placeholder="Optional category" />
          </label>
          <label>
            <span>Tags</span>
            <input v-model="bookForm.tags" type="text" placeholder="reading, design" />
          </label>
        </div>

        <div class="loop-actions">
          <AppButton variant="primary" :loading="savingBook" :disabled="!canCreateBook" @click="createFirstBook">
            Create and use this book
          </AppButton>
          <AppButton variant="secondary" :disabled="!selectedBook" @click="goToStep('reading')">Continue</AppButton>
        </div>
      </AppCard>

      <AppCard v-else-if="activeStep === 'reading'" class="loop-panel" as="section" aria-labelledby="loop-reading-title">
        <div class="loop-panel__header">
          <div>
            <p class="eyebrow">Step 2</p>
            <h2 id="loop-reading-title">Set this book as currently reading</h2>
            <p>
              BookOS will update real library state. Current page is optional and is used only as capture context unless
              you include it in the capture text.
            </p>
          </div>
          <AppBadge :variant="readingComplete ? 'success' : 'warning'" size="sm">
            {{ readingComplete ? 'Reading state saved' : 'Not set yet' }}
          </AppBadge>
        </div>

        <AppEmptyState
          v-if="!selectedBook"
          title="Choose a book first"
          description="The reading state needs a selected book context."
          compact
        >
          <template #actions>
            <AppButton variant="primary" @click="goToStep('book')">Choose Book</AppButton>
          </template>
        </AppEmptyState>

        <template v-else>
          <div class="selected-context-card">
            <strong>{{ selectedBook.title }}</strong>
            <span>{{ selectedUserBook ? 'Already in your library' : 'Will be added to your library' }}</span>
          </div>

          <div class="reading-form">
            <label>
              <span>Reading progress percent</span>
              <input v-model.number="readingProgressPercent" type="number" min="0" max="100" />
            </label>
            <label>
              <span>Current page if known</span>
              <input v-model.number="knownPage" type="number" min="1" placeholder="Optional" />
            </label>
          </div>

          <p class="helper-text">
            If page is unknown, leave it blank. BookOS will keep page values null instead of inventing them.
          </p>

          <div class="loop-actions">
            <AppButton variant="primary" :loading="savingReading" @click="saveReadingState">
              Set to Currently Reading
            </AppButton>
            <AppButton variant="secondary" :disabled="!readingComplete" @click="goToStep('capture')">Continue</AppButton>
          </div>
        </template>
      </AppCard>

      <AppCard v-else-if="activeStep === 'capture'" class="loop-panel" as="section" aria-labelledby="loop-capture-title">
        <div class="loop-panel__header">
          <div>
            <p class="eyebrow">Step 3</p>
            <h2 id="loop-capture-title">Capture one original thought</h2>
            <p>Write in your own words. Page is optional. Tags and [[Concept]] markers are supported.</p>
          </div>
          <AppBadge :variant="activeCapture ? 'success' : 'warning'" size="sm">
            {{ activeCapture ? 'Capture saved' : 'Needs capture' }}
          </AppBadge>
        </div>

        <AppEmptyState
          v-if="!readingComplete"
          title="Set reading state first"
          description="Captures require a book in your library so source links can be preserved."
          compact
        >
          <template #actions>
            <AppButton variant="primary" @click="goToStep('reading')">Set Reading</AppButton>
          </template>
        </AppEmptyState>

        <template v-else>
          <div class="capture-examples" aria-label="Safe capture examples">
            <AppButton variant="subtle" @click="insertCaptureExample('quote')">Insert quote-style example</AppButton>
            <AppButton variant="subtle" @click="insertCaptureExample('action')">Insert action example</AppButton>
            <AppButton variant="subtle" @click="insertCaptureExample('concept')">Insert concept example</AppButton>
            <AppButton variant="ghost" :disabled="!knownPage" @click="prependKnownPage">Use current page marker</AppButton>
          </div>

          <label class="capture-field" for="first-loop-capture">
            <span>Capture text</span>
            <textarea
              id="first-loop-capture"
              v-model="captureText"
              rows="5"
              placeholder="A source-backed thought from this book #note [[Idea]]"
            />
          </label>

          <div class="parser-preview" aria-live="polite">
            <AppLoadingState v-if="previewLoading" label="Parsing capture" compact />
            <template v-else-if="parsedPreview">
              <div class="inline-meta">
                <AppBadge variant="primary" size="sm">{{ formatType(parsedPreview.type) }}</AppBadge>
                <AppBadge variant="accent" size="sm">{{ pageLabel(parsedPreview.pageStart, parsedPreview.pageEnd) }}</AppBadge>
                <AppBadge v-for="tag in parsedPreview.tags" :key="tag" variant="neutral" size="sm">#{{ tag }}</AppBadge>
                <AppBadge v-for="concept in parsedPreview.concepts" :key="concept" variant="info" size="sm">
                  [[{{ concept }}]]
                </AppBadge>
              </div>
              <p>{{ parsedPreview.cleanText || 'No clean text yet.' }}</p>
              <p class="helper-text">
                Source preview: {{ selectedBook?.title ?? 'No book' }} /
                {{ parsedPreview.pageStart ? pageLabel(parsedPreview.pageStart, parsedPreview.pageEnd) : 'page unknown, stored as null' }}
              </p>
            </template>
            <p v-else class="helper-text">Type a capture to preview parsed type, page, tags, and concepts.</p>
          </div>

          <div v-if="activeCapture" class="created-record-card">
            <strong>Saved capture</strong>
            <span>{{ activeCapture.cleanText || activeCapture.rawText }}</span>
            <AppBadge variant="accent" size="sm">{{ pageLabel(activeCapture.pageStart, activeCapture.pageEnd) }}</AppBadge>
          </div>

          <div class="loop-actions">
            <AppButton variant="primary" :loading="savingCapture" :disabled="!canSaveCapture" @click="saveCapture">
              Save capture
            </AppButton>
            <AppButton variant="secondary" :disabled="!activeCapture" @click="goToStep('process')">Continue</AppButton>
          </div>
        </template>
      </AppCard>

      <AppCard v-else-if="activeStep === 'process'" class="loop-panel" as="section" aria-labelledby="loop-process-title">
        <div class="loop-panel__header">
          <div>
            <p class="eyebrow">Step 4</p>
            <h2 id="loop-process-title">Process the capture</h2>
            <p>Choose what the capture should become. The backend preserves the source link from the capture.</p>
          </div>
          <AppBadge :variant="createdRecord ? 'success' : 'warning'" size="sm">
            {{ createdRecord ? 'Record created' : 'Needs processing' }}
          </AppBadge>
        </div>

        <AppEmptyState
          v-if="!activeCapture"
          title="Save a capture first"
          description="Processing starts from a real capture, not a fake placeholder."
          compact
        >
          <template #actions>
            <AppButton variant="primary" @click="goToStep('capture')">Capture Thought</AppButton>
          </template>
        </AppEmptyState>

        <template v-else>
          <div class="process-card">
            <div>
              <p class="eyebrow">Capture to process</p>
              <h3>{{ activeCapture.cleanText || activeCapture.rawText }}</h3>
              <p>{{ activeCapture.bookTitle }} / {{ pageLabel(activeCapture.pageStart, activeCapture.pageEnd) }}</p>
            </div>
            <AppBadge variant="primary" size="sm">{{ formatType(activeCapture.parsedType) }}</AppBadge>
          </div>

          <div class="conversion-options" role="group" aria-label="Conversion target">
            <AppButton
              v-for="option in conversionOptions"
              :key="option.value"
              variant="secondary"
              :selected="conversionTarget === option.value"
              @click="conversionTarget = option.value"
            >
              {{ option.label }}
            </AppButton>
          </div>

          <label v-if="conversionTarget !== 'QUOTE' && conversionTarget !== 'CONCEPT_REVIEW'" class="capture-field" for="conversion-title">
            <span>{{ conversionTarget === 'ACTION_ITEM' ? 'Action title' : 'Note title' }}</span>
            <input id="conversion-title" v-model="conversionTitle" type="text" placeholder="Optional title" />
          </label>

          <p v-if="conversionTarget === 'CONCEPT_REVIEW'" class="helper-text">
            Concept review creates reviewed concepts and design knowledge from existing [[Concept]] markers. It does not mark the capture fully converted.
          </p>

          <div v-if="createdRecord" class="created-record-card">
            <strong>{{ createdRecord.typeLabel }} created</strong>
            <span>{{ createdRecord.title }}</span>
            <AppBadge :variant="createdRecord.sourceReference ? 'success' : 'warning'" size="sm">
              {{ createdRecord.sourceReference ? 'Source preserved' : 'Source lookup needed' }}
            </AppBadge>
          </div>

          <div class="loop-actions">
            <AppButton variant="primary" :loading="processingCapture" :disabled="!canProcessCapture" @click="processCapture">
              {{ processButtonLabel }}
            </AppButton>
            <AppButton variant="secondary" :disabled="!createdRecord" @click="goToStep('source')">Continue</AppButton>
          </div>
        </template>
      </AppCard>

      <AppCard v-else-if="activeStep === 'source'" class="loop-panel" as="section" aria-labelledby="loop-source-title">
        <div class="loop-panel__header">
          <div>
            <p class="eyebrow">Step 5</p>
            <h2 id="loop-source-title">Open the source</h2>
            <p>Verify where the created record came from. If no page was supplied, BookOS should say page unknown.</p>
          </div>
          <AppBadge :variant="sourceOpened ? 'success' : 'warning'" size="sm">
            {{ sourceOpened ? 'Source opened' : 'Needs source check' }}
          </AppBadge>
        </div>

        <AppEmptyState
          v-if="!createdRecord"
          title="Create a processed record first"
          description="The source step needs a note, quote, action, or reviewed concept."
          compact
        >
          <template #actions>
            <AppButton variant="primary" @click="goToStep('process')">Process Capture</AppButton>
          </template>
        </AppEmptyState>

        <template v-else>
          <div class="source-check-card">
            <div>
              <p class="eyebrow">{{ createdRecord.typeLabel }}</p>
              <h3>{{ createdRecord.title }}</h3>
              <p>
                Page status:
                <strong>{{ createdRecord.sourceReference?.pageStart ? pageLabel(createdRecord.sourceReference.pageStart, createdRecord.sourceReference.pageEnd) : 'page unknown' }}</strong>
              </p>
            </div>
            <AppBadge :variant="createdRecord.sourceReference?.sourceConfidence === 'HIGH' ? 'success' : 'neutral'" size="sm">
              {{ createdRecord.sourceReference?.sourceConfidence ?? 'Source lookup' }}
            </AppBadge>
          </div>

          <div class="loop-actions">
            <AppButton variant="primary" :loading="openingSource" @click="openCreatedSource">Open source drawer</AppButton>
            <AppButton variant="secondary" :disabled="!sourceOpened" @click="goToStep('next')">Continue</AppButton>
          </div>
        </template>
      </AppCard>

      <AppCard v-else class="loop-panel" as="section" aria-labelledby="loop-next-title">
        <div class="loop-panel__header">
          <div>
            <p class="eyebrow">Step 6</p>
            <h2 id="loop-next-title">Choose the next path</h2>
            <p>You now have a real source-backed record. Continue with the workflow that fits your goal.</p>
          </div>
          <AppBadge variant="success" size="sm">Loop complete</AppBadge>
        </div>

        <div class="next-path-grid">
          <RouterLink to="/dashboard" class="next-path-card">
            <strong>Continue reading</strong>
            <span>Return to the task-first dashboard and capture another thought.</span>
          </RouterLink>
          <RouterLink to="/review" class="next-path-card">
            <strong>Start review</strong>
            <span>Turn source-backed records into active recall and learning progress.</span>
          </RouterLink>
          <RouterLink to="/projects" class="next-path-card">
            <strong>Apply to project</strong>
            <span>Move the source-backed idea into a game design project.</span>
          </RouterLink>
          <RouterLink to="/use-cases" class="next-path-card">
            <strong>Explore use cases</strong>
            <span>Choose a longer workflow after completing the first loop.</span>
          </RouterLink>
        </div>
      </AppCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRouter } from 'vue-router'
import { addBookToLibrary, createBook, getBooks } from '../api/books'
import { convertCapture, createCapture, getCapture, reviewCaptureConcepts } from '../api/captures'
import { getActionItem } from '../api/actionItems'
import { getConcept } from '../api/knowledge'
import { getNote } from '../api/notes'
import { previewParsedNote } from '../api/parser'
import { getQuote } from '../api/quotes'
import { completeUseCaseStep, startUseCase } from '../api/useCaseProgress'
import { getUserBooks, updateUserBookProgress, updateUserBookStatus } from '../api/userBooks'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useAuthStore } from '../stores/auth'
import type {
  BookPayload,
  BookRecord,
  CaptureConversionTarget,
  NoteBlockType,
  ParsedNoteResult,
  RawCaptureRecord,
  SourceReferenceRecord,
  UserBookRecord,
} from '../types'

type StepKey = 'book' | 'reading' | 'capture' | 'process' | 'source' | 'next'
type GuidedConversionTarget = Exclude<CaptureConversionTarget, 'CONCEPT'> | 'CONCEPT_REVIEW'

interface BookOption {
  id: number
  title: string
  subtitle: string | null
  authors: string[]
  inLibrary: boolean
}

interface CreatedRecord {
  type: 'NOTE' | 'QUOTE' | 'ACTION_ITEM' | 'CONCEPT'
  typeLabel: string
  id: number
  title: string
  bookId: number
  bookTitle: string
  pageStart: number | null
  pageEnd: number | null
  sourceReference: SourceReferenceRecord | null
}

interface PersistedFirstLoopState {
  selectedBookId: number | null
  captureId: number | null
  createdType: CreatedRecord['type'] | null
  createdId: number | null
  knownPage: number | null
  readingSaved: boolean
  sourceOpened: boolean
  activeStep: StepKey
}

const FIRST_USE_CASE_SLUG = 'first-15-minutes'
const router = useRouter()
const auth = useAuthStore()
const { openSource } = useOpenSource()

const steps: Array<{ key: StepKey; label: string }> = [
  { key: 'book', label: 'Add Book' },
  { key: 'reading', label: 'Set Reading' },
  { key: 'capture', label: 'Capture Thought' },
  { key: 'process', label: 'Process Capture' },
  { key: 'source', label: 'Open Source' },
  { key: 'next', label: 'Next Path' },
]

const loading = ref(true)
const loadError = ref('')
const activeStep = ref<StepKey>('book')
const books = ref<BookRecord[]>([])
const library = ref<UserBookRecord[]>([])
const selectedBookId = ref(0)
const selectedUserBookId = ref<number | null>(null)
const readingSaved = ref(false)
const knownPage = ref<number | null>(null)
const readingProgressPercent = ref(0)
const activeCapture = ref<RawCaptureRecord | null>(null)
const createdRecord = ref<CreatedRecord | null>(null)
const sourceOpened = ref(false)

const savingBook = ref(false)
const savingReading = ref(false)
const savingCapture = ref(false)
const processingCapture = ref(false)
const openingSource = ref(false)
const previewLoading = ref(false)

const captureText = ref('')
const parsedPreview = ref<ParsedNoteResult | null>(null)
const conversionTarget = ref<GuidedConversionTarget>('NOTE')
const conversionTitle = ref('')
let previewTimer: number | null = null

const bookForm = reactive({
  title: '',
  author: '',
  category: '',
  tags: '',
})

const bookOptions = computed<BookOption[]>(() => {
  const options = new Map<number, BookOption>()
  for (const book of books.value) {
    options.set(book.id, {
      id: book.id,
      title: book.title,
      subtitle: book.subtitle,
      authors: book.authors,
      inLibrary: Boolean(book.inLibrary),
    })
  }
  for (const item of library.value) {
    options.set(item.bookId, {
      id: item.bookId,
      title: item.title,
      subtitle: item.subtitle,
      authors: item.authors,
      inLibrary: true,
    })
  }
  return Array.from(options.values())
})
const selectedBook = computed(() => bookOptions.value.find((book) => book.id === selectedBookId.value) ?? null)
const selectedUserBook = computed(() => library.value.find((item) => item.bookId === selectedBookId.value) ?? null)
const readingComplete = computed(() => readingSaved.value || selectedUserBook.value?.readingStatus === 'CURRENTLY_READING')
const canCreateBook = computed(() => Boolean(bookForm.title.trim()))
const canSaveCapture = computed(() => Boolean(selectedBook.value && readingComplete.value && captureText.value.trim()))
const canProcessCapture = computed(() => Boolean(activeCapture.value && !createdRecord.value))
const conversionOptions = computed(() => {
  const options: Array<{ value: GuidedConversionTarget; label: string }> = [
    { value: 'NOTE', label: 'Convert to Note' },
    { value: 'QUOTE', label: 'Convert to Quote' },
    { value: 'ACTION_ITEM', label: 'Convert to Action' },
  ]
  if (activeCapture.value?.concepts.length || parsedPreview.value?.concepts.length) {
    options.push({ value: 'CONCEPT_REVIEW', label: 'Review Concept' })
  }
  return options
})
const processButtonLabel = computed(() => {
  if (conversionTarget.value === 'QUOTE') return 'Convert to Quote'
  if (conversionTarget.value === 'ACTION_ITEM') return 'Convert to Action'
  if (conversionTarget.value === 'CONCEPT_REVIEW') return 'Review Concept'
  return 'Convert to Note'
})
const completedStepKeys = computed(() => {
  const keys = new Set<StepKey>()
  if (selectedBook.value) keys.add('book')
  if (readingComplete.value) keys.add('reading')
  if (activeCapture.value) keys.add('capture')
  if (createdRecord.value) keys.add('process')
  if (sourceOpened.value) keys.add('source')
  if (sourceOpened.value && activeStep.value === 'next') keys.add('next')
  return keys
})
const completedCount = computed(() => completedStepKeys.value.size)

onMounted(loadInitialState)

watch(captureText, () => scheduleParserPreview())
watch(
  [selectedBookId, selectedUserBookId, activeCapture, createdRecord, knownPage, readingSaved, sourceOpened, activeStep],
  () => persistState(),
)
watch(parsedPreview, (preview) => {
  if (!preview || activeCapture.value || createdRecord.value) return
  if (preview.type === 'QUOTE') conversionTarget.value = 'QUOTE'
  else if (preview.type === 'ACTION_ITEM') conversionTarget.value = 'ACTION_ITEM'
  else if (preview.concepts.length) conversionTarget.value = 'CONCEPT_REVIEW'
  else conversionTarget.value = 'NOTE'
})

async function loadInitialState() {
  loading.value = true
  loadError.value = ''
  try {
    await startUseCase(FIRST_USE_CASE_SLUG).catch(() => undefined)
    const [catalog, shelf] = await Promise.all([getBooks(), getUserBooks()])
    books.value = catalog
    library.value = shelf
    await restoreState()
    if (!selectedBookId.value && bookOptions.value.length === 1) {
      selectedBookId.value = bookOptions.value[0].id
    }
    selectedUserBookId.value = selectedUserBook.value?.id ?? null
    readingProgressPercent.value = selectedUserBook.value?.progressPercent ?? readingProgressPercent.value
    goToFirstIncomplete()
  } catch {
    loadError.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function refreshBooks() {
  const [catalog, shelf] = await Promise.all([getBooks(), getUserBooks()])
  books.value = catalog
  library.value = shelf
  selectedUserBookId.value = selectedUserBook.value?.id ?? selectedUserBookId.value
}

async function restoreState() {
  const raw = window.localStorage.getItem(storageKey())
  if (!raw) return

  try {
    const value = JSON.parse(raw) as PersistedFirstLoopState
    selectedBookId.value = value.selectedBookId ?? 0
    knownPage.value = value.knownPage ?? null
    readingSaved.value = Boolean(value.readingSaved)
    sourceOpened.value = Boolean(value.sourceOpened)
    activeStep.value = value.activeStep ?? 'book'
    if (value.captureId) {
      activeCapture.value = await getCapture(value.captureId).catch(() => null)
    }
    if (value.createdType && value.createdId) {
      createdRecord.value = await hydrateCreatedRecord(value.createdType, value.createdId).catch(() => null)
    } else if (activeCapture.value?.convertedEntityType && activeCapture.value.convertedEntityId) {
      createdRecord.value = await hydrateCreatedRecord(
        activeCapture.value.convertedEntityType as CreatedRecord['type'],
        activeCapture.value.convertedEntityId,
      ).catch(() => null)
    }
  } catch {
    window.localStorage.removeItem(storageKey())
  }
}

function persistState() {
  const value: PersistedFirstLoopState = {
    selectedBookId: selectedBookId.value || null,
    captureId: activeCapture.value?.id ?? null,
    createdType: createdRecord.value?.type ?? null,
    createdId: createdRecord.value?.id ?? null,
    knownPage: knownPage.value ?? null,
    readingSaved: readingSaved.value,
    sourceOpened: sourceOpened.value,
    activeStep: activeStep.value,
  }
  window.localStorage.setItem(storageKey(), JSON.stringify(value))
}

function storageKey() {
  return `bookos.guidedFirstLoop.${auth.user?.id ?? 'anonymous'}`
}

async function createFirstBook() {
  if (!canCreateBook.value) return
  savingBook.value = true
  try {
    const payload: BookPayload = {
      title: bookForm.title.trim(),
      subtitle: null,
      description: null,
      isbn: null,
      publisher: null,
      publicationYear: null,
      coverUrl: null,
      category: clean(bookForm.category),
      visibility: 'PRIVATE',
      authors: splitList(bookForm.author),
      tags: splitList(bookForm.tags),
    }
    const book = await createBook(payload)
    const userBook = await addBookToLibrary(book.id, { readingStatus: 'CURRENTLY_READING' })
    selectedBookId.value = book.id
    selectedUserBookId.value = userBook.id
    readingSaved.value = true
    readingProgressPercent.value = userBook.progressPercent
    await refreshBooks()
    await completeUseCaseStep(FIRST_USE_CASE_SLUG, 'add-book').catch(() => undefined)
    ElMessage.success('Book created and added to your library.')
    goToStep('reading')
  } catch {
    ElMessage.error('Book could not be created. Check the title and try again.')
  } finally {
    savingBook.value = false
  }
}

async function chooseExistingBook() {
  activeCapture.value = null
  createdRecord.value = null
  sourceOpened.value = false
  selectedUserBookId.value = selectedUserBook.value?.id ?? null
  readingSaved.value = selectedUserBook.value?.readingStatus === 'CURRENTLY_READING'
  readingProgressPercent.value = selectedUserBook.value?.progressPercent ?? 0
  await completeUseCaseStep(FIRST_USE_CASE_SLUG, 'add-book').catch(() => undefined)
}

async function saveReadingState() {
  if (!selectedBook.value) {
    goToStep('book')
    return
  }

  savingReading.value = true
  try {
    let userBook = selectedUserBook.value
    if (!userBook) {
      userBook = await addBookToLibrary(selectedBook.value.id, { readingStatus: 'CURRENTLY_READING' })
      await refreshBooks()
    }
    userBook = await updateUserBookStatus(userBook.id, 'CURRENTLY_READING')
    if (Number.isFinite(readingProgressPercent.value)) {
      userBook = await updateUserBookProgress(userBook.id, clamp(readingProgressPercent.value, 0, 100))
    }
    selectedUserBookId.value = userBook.id
    readingSaved.value = true
    await refreshBooks()
    ElMessage.success('Reading state saved.')
    goToStep('capture')
  } catch {
    ElMessage.error('Reading state could not be saved.')
  } finally {
    savingReading.value = false
  }
}

function scheduleParserPreview() {
  if (previewTimer) window.clearTimeout(previewTimer)
  if (!captureText.value.trim()) {
    parsedPreview.value = null
    previewLoading.value = false
    return
  }
  previewTimer = window.setTimeout(() => {
    void loadParserPreview()
  }, 250)
}

async function loadParserPreview() {
  const rawText = captureText.value.trim()
  if (!rawText) return
  previewLoading.value = true
  try {
    parsedPreview.value = await previewParsedNote({ rawText })
  } catch {
    parsedPreview.value = null
  } finally {
    previewLoading.value = false
  }
}

async function saveCapture() {
  if (!canSaveCapture.value || !selectedBook.value) return
  savingCapture.value = true
  try {
    const capture = await createCapture({ bookId: selectedBook.value.id, rawText: captureText.value.trim() })
    activeCapture.value = capture
    createdRecord.value = null
    sourceOpened.value = false
    parsedPreview.value = toParsedPreview(capture)
    conversionTarget.value = suggestedConversion(capture)
    conversionTitle.value = capture.cleanText || capture.rawText
    await completeUseCaseStep(FIRST_USE_CASE_SLUG, 'capture').catch(() => undefined)
    ElMessage.success('Capture saved and parsed.')
    goToStep('process')
  } catch {
    ElMessage.error('Capture could not be saved. Confirm the book is in your library.')
  } finally {
    savingCapture.value = false
  }
}

async function processCapture() {
  if (!activeCapture.value || createdRecord.value) return
  processingCapture.value = true
  try {
    if (conversionTarget.value === 'CONCEPT_REVIEW') {
      createdRecord.value = await reviewConceptCapture(activeCapture.value)
    } else {
      const conversion = await convertCapture(activeCapture.value.id, {
        targetType: conversionTarget.value,
        title: clean(conversionTitle.value),
      })
      activeCapture.value = conversion.capture
      createdRecord.value = await hydrateCreatedRecord(conversion.targetType as CreatedRecord['type'], conversion.targetId)
    }
    await completeUseCaseStep(FIRST_USE_CASE_SLUG, 'process').catch(() => undefined)
    ElMessage.success('Source-backed record created.')
    goToStep('source')
  } catch {
    ElMessage.error('Capture could not be processed. It may already be converted or unavailable.')
  } finally {
    processingCapture.value = false
  }
}

async function reviewConceptCapture(capture: RawCaptureRecord) {
  const concepts = capture.concepts.length ? capture.concepts : parsedPreview.value?.concepts ?? []
  if (!concepts.length) {
    throw new Error('No concepts to review.')
  }
  const review = await reviewCaptureConcepts(capture.id, {
    concepts: concepts.map((concept) => ({
      rawName: concept,
      finalName: concept,
      action: 'CREATE',
      existingConceptId: null,
      tags: capture.tags,
    })),
  })
  const first = review.concepts.find((item) => item.concept) ?? review.concepts[0]
  if (!first?.concept) {
    throw new Error('Concept review did not create a concept.')
  }
  const concept = await getConcept(first.concept.id)
  return {
    type: 'CONCEPT',
    typeLabel: 'Concept',
    id: concept.id,
    title: concept.name,
    bookId: concept.bookId ?? capture.bookId,
    bookTitle: concept.bookTitle ?? capture.bookTitle,
    pageStart: first.sourceReference?.pageStart ?? null,
    pageEnd: first.sourceReference?.pageEnd ?? null,
    sourceReference: first.sourceReference ?? concept.firstSourceReference,
  } satisfies CreatedRecord
}

async function hydrateCreatedRecord(type: CreatedRecord['type'], id: number): Promise<CreatedRecord> {
  if (type === 'QUOTE') {
    const quote = await getQuote(id)
    return {
      type,
      typeLabel: 'Quote',
      id: quote.id,
      title: quote.text,
      bookId: quote.bookId,
      bookTitle: quote.bookTitle,
      pageStart: quote.pageStart,
      pageEnd: quote.pageEnd,
      sourceReference: quote.sourceReference,
    }
  }

  if (type === 'ACTION_ITEM') {
    const action = await getActionItem(id)
    return {
      type,
      typeLabel: 'Action',
      id: action.id,
      title: action.title,
      bookId: action.bookId,
      bookTitle: action.bookTitle,
      pageStart: action.pageStart,
      pageEnd: action.pageEnd,
      sourceReference: action.sourceReference,
    }
  }

  if (type === 'CONCEPT') {
    const concept = await getConcept(id)
    return {
      type,
      typeLabel: 'Concept',
      id: concept.id,
      title: concept.name,
      bookId: concept.bookId ?? selectedBook.value?.id ?? 0,
      bookTitle: concept.bookTitle ?? selectedBook.value?.title ?? 'Selected book',
      pageStart: concept.firstSourceReference?.pageStart ?? null,
      pageEnd: concept.firstSourceReference?.pageEnd ?? null,
      sourceReference: concept.firstSourceReference,
    }
  }

  const note = await getNote(id)
  const firstBlock = note.blocks[0]
  const sourceReference = firstBlock?.sourceReferences[0] ?? null
  return {
    type: 'NOTE',
    typeLabel: 'Note',
    id: note.id,
    title: note.title,
    bookId: note.bookId,
    bookTitle: note.bookTitle,
    pageStart: firstBlock?.pageStart ?? sourceReference?.pageStart ?? null,
    pageEnd: firstBlock?.pageEnd ?? sourceReference?.pageEnd ?? null,
    sourceReference,
  }
}

async function openCreatedSource() {
  if (!createdRecord.value) return
  openingSource.value = true
  try {
    await openSource({
      sourceType: createdRecord.value.type,
      sourceId: createdRecord.value.id,
      bookId: createdRecord.value.bookId,
      bookTitle: createdRecord.value.bookTitle,
      pageStart: createdRecord.value.pageStart,
      sourceReferenceId: createdRecord.value.sourceReference?.id ?? null,
      sourceReference: createdRecord.value.sourceReference,
      navigate: false,
    })
    sourceOpened.value = true
    await completeUseCaseStep(FIRST_USE_CASE_SLUG, 'open-source').catch(() => undefined)
    ElMessage.success('Source drawer opened. Page unknown stays unknown when no page was supplied.')
    goToStep('next')
  } catch {
    ElMessage.error('Source could not be opened.')
  } finally {
    openingSource.value = false
  }
}

function goToStep(step: StepKey) {
  activeStep.value = step
}

function goToFirstIncomplete() {
  const first = steps.find((step) => !completedStepKeys.value.has(step.key))
  activeStep.value = first?.key ?? 'next'
}

function exitToDashboard() {
  router.push({ name: 'dashboard' })
}

function insertCaptureExample(kind: 'quote' | 'action' | 'concept') {
  const page = knownPage.value ? `p.${knownPage.value} ` : ''
  if (kind === 'quote') {
    captureText.value = `\uD83D\uDCAC ${page}"Readable feedback changes how a player trusts an interaction." #quote [[Game Feel]]`
  } else if (kind === 'action') {
    captureText.value = `\u2705 ${page}Test whether the next prototype makes the feedback loop clearer. #todo [[Feedback Loop]]`
  } else {
    captureText.value = `\uD83E\uDDE9 ${page}Connect this observation to [[Core Loop]] #concept`
  }
}

function prependKnownPage() {
  if (!knownPage.value) return
  const value = captureText.value.trim()
  if (!value) {
    captureText.value = `p.${knownPage.value} `
    return
  }
  if (/^(p\.?\s*\d+|page\s+\d+)/i.test(value)) return
  captureText.value = `p.${knownPage.value} ${value}`
}

function suggestedConversion(capture: RawCaptureRecord): GuidedConversionTarget {
  if (capture.parsedType === 'QUOTE') return 'QUOTE'
  if (capture.parsedType === 'ACTION_ITEM') return 'ACTION_ITEM'
  if (capture.concepts.length) return 'CONCEPT_REVIEW'
  return 'NOTE'
}

function toParsedPreview(capture: RawCaptureRecord): ParsedNoteResult {
  return {
    type: capture.parsedType,
    pageStart: capture.pageStart,
    pageEnd: capture.pageEnd,
    tags: capture.tags,
    concepts: capture.concepts,
    cleanText: capture.cleanText,
    rawText: capture.rawText,
    warnings: capture.parserWarnings,
  }
}

function splitList(value: string) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function clean(value: string | null | undefined) {
  return value && value.trim() ? value.trim() : null
}

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(max, value))
}

function formatType(type: NoteBlockType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function pageLabel(pageStart: number | null, pageEnd: number | null) {
  if (pageStart === null) return 'page unknown'
  if (pageEnd !== null && pageEnd !== pageStart) return `p.${pageStart}-${pageEnd}`
  return `p.${pageStart}`
}
</script>

<style scoped>
.first-loop-page {
  display: grid;
  gap: var(--space-5);
}

.first-loop-hero,
.loop-panel {
  padding: var(--space-6);
}

.first-loop-hero {
  display: flex;
  justify-content: space-between;
  gap: var(--space-5);
  align-items: flex-start;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at 14% 0%, color-mix(in srgb, var(--bookos-accent-soft) 72%, transparent), transparent 34%),
    linear-gradient(135deg, var(--bookos-surface), color-mix(in srgb, var(--bookos-primary-soft) 42%, var(--bookos-surface)));
}

.first-loop-hero h1,
.first-loop-hero p,
.loop-panel h2,
.loop-panel h3,
.loop-panel p {
  margin: 0;
}

.first-loop-hero h1 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(2.1rem, 5vw, 4rem);
  letter-spacing: -0.04em;
}

.first-loop-hero p:not(.eyebrow),
.loop-panel p,
.helper-text {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.first-loop-hero__actions,
.loop-actions,
.capture-examples,
.inline-meta,
.conversion-options {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.first-loop-hero__actions {
  justify-content: flex-end;
}

.first-loop-progress {
  margin: 0;
  padding: var(--space-3);
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  list-style: none;
}

.first-loop-progress button {
  width: 100%;
  min-height: var(--touch-target);
  padding: var(--space-2);
  display: grid;
  gap: var(--space-1);
  place-items: center;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
  font-weight: 800;
  cursor: pointer;
}

.first-loop-progress span {
  width: 1.7rem;
  height: 1.7rem;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: var(--bookos-surface);
  color: var(--bookos-text-tertiary);
}

.first-loop-progress .active button {
  border-color: var(--bookos-primary);
  color: var(--bookos-primary);
  box-shadow: var(--focus-ring);
}

.first-loop-progress .complete button {
  border-color: color-mix(in srgb, var(--bookos-success) 30%, var(--bookos-border));
  background: var(--bookos-success-soft);
}

.loop-panel {
  display: grid;
  gap: var(--space-5);
}

.loop-panel__header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: flex-start;
}

.loop-panel h2 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(1.6rem, 3vw, 2.5rem);
}

.book-create-grid,
.reading-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.book-create-grid label,
.reading-form label,
.existing-book-picker,
.capture-field {
  display: grid;
  gap: var(--space-2);
}

.book-create-grid span,
.reading-form span,
.existing-book-picker label,
.capture-field span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 900;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.book-create-grid input,
.reading-form input,
.existing-book-picker select,
.capture-field input,
.capture-field textarea {
  width: 100%;
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-primary);
  font: inherit;
}

.capture-field textarea {
  padding-block: var(--space-3);
  resize: vertical;
}

.selected-context-card,
.created-record-card,
.process-card,
.source-check-card,
.parser-preview {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.process-card,
.source-check-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
}

.next-path-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.next-path-card {
  min-height: 150px;
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
  color: inherit;
  text-decoration: none;
}

.next-path-card:hover,
.next-path-card:focus-visible {
  border-color: var(--bookos-primary);
  box-shadow: var(--shadow-card-hover);
}

.next-path-card span {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

@media (max-width: 980px) {
  .first-loop-hero,
  .loop-panel__header,
  .process-card,
  .source-check-card {
    flex-direction: column;
  }

  .first-loop-progress,
  .book-create-grid,
  .reading-form,
  .next-path-grid {
    grid-template-columns: 1fr;
  }
}
</style>
