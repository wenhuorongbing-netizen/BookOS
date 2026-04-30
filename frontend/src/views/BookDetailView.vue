<template>
  <div v-if="loading" class="page-shell">
    <AppCard class="book-detail-state" as="section">
      <AppLoadingState label="Loading book knowledge dashboard" />
    </AppCard>
  </div>

  <div v-else-if="errorMessage" class="page-shell">
    <AppErrorState
      title="Book dashboard could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadBook"
    />
  </div>

  <div v-else-if="book" class="page-shell">
    <BookContextHeader>
      <template #actions>
        <HelpTooltip topic="source-reference" placement="left" />
        <RouterLink :to="{ name: 'book-notes', params: { bookId: book.id } }" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Open Notes</AppButton>
        </RouterLink>
        <RouterLink
          :to="{ name: 'forum-new', query: { relatedEntityType: 'BOOK', relatedEntityId: String(book.id), bookId: String(book.id), title: `Discuss ${book.title}` } }"
          custom
          v-slot="{ navigate }"
        >
          <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
        </RouterLink>
        <RouterLink :to="`/books/${book.id}/edit`" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Edit Book</AppButton>
        </RouterLink>
        <AppButton v-if="!book.inLibrary" variant="primary" @click="handleAddToLibrary">Add to Library</AppButton>
      </template>
    </BookContextHeader>

    <BookHero
      :book="book"
      :status="status"
      :progress="progress"
      :rating="rating"
      :favorite-loading="favoriteLoading"
      @toggle-favorite="handleFavoriteToggle"
    />
    <DetailNextStepCard
      :title="bookNextStep.title"
      :description="bookNextStep.description"
      :primary-label="bookNextStep.primaryLabel"
      :primary-to="bookNextStep.primaryTo"
      :primary-loading="bookNextStep.primaryLoading"
      :secondary-label="bookNextStep.secondaryLabel"
      :secondary-to="bookNextStep.secondaryTo"
      :loop="bookWorkflowLoop"
      @primary="handleBookNextStep"
      @secondary="createBookReview"
    />
    <UseCaseSuggestionPanel
      title="Start a book workflow"
      description="Use this book as the source context for capture, conversion, concepts, or project application."
      :slugs="bookUseCaseSlugs"
    />
    <AppCard class="reading-session-panel" as="section">
      <div class="detail-panel__heading">
        <div class="eyebrow">Reading Session</div>
        <h2>Track this reading pass</h2>
      </div>
      <div class="reading-session-panel__grid">
        <label class="detail-field">
          <span>Start page</span>
          <el-input-number v-model="readingStartPage" :min="0" controls-position="right" aria-label="Reading session start page" />
        </label>
        <label class="detail-field">
          <span>End page</span>
          <el-input-number v-model="readingEndPage" :min="0" controls-position="right" aria-label="Reading session end page" />
        </label>
        <label class="detail-field">
          <span>Minutes read</span>
          <el-input-number v-model="readingMinutes" :min="0" controls-position="right" aria-label="Minutes read" />
        </label>
      </div>
      <label class="detail-field">
        <span>Reflection</span>
        <el-input v-model="readingReflection" type="textarea" :rows="3" placeholder="What changed in your understanding during this session?" />
      </label>
      <div class="reading-session-panel__actions">
        <AppButton v-if="!activeReadingSession" variant="primary" :loading="readingBusy" @click="startSession">Start Session</AppButton>
        <AppButton v-else variant="primary" :loading="readingBusy" @click="finishSession">Finish Session</AppButton>
        <AppButton variant="secondary" :loading="readingBusy" @click="createBookReview">Create Review from Book</AppButton>
        <span class="reading-session-panel__meta">
          {{ readingSessions.length }} sessions logged
          <template v-if="activeReadingSession">/ active since {{ formatDate(activeReadingSession.startedAt) }}</template>
        </span>
      </div>
    </AppCard>
    <AppCard v-if="cockpitWarning" class="cockpit-warning" as="section" role="status">
      <strong>Some cockpit data could not load</strong>
      <p>{{ cockpitWarning }}</p>
    </AppCard>

    <BookInsightCards
      :book="book"
      :daily="daily"
      :daily-action-loading="dailyActionLoading"
      @open-source="handleOpenSource"
      @open-graph="handleOpenGraph"
      @regenerate-daily="handleRegenerateDaily"
      @skip-daily="handleSkipDaily"
      @save-reflection="handleSaveDailyReflection"
      @create-prototype-task="handleCreatePrototypeTask"
      @apply-prompt-to-project="applyDailyPromptOpen = true"
    />
    <BookKnowledgeSection
      :book="book"
      @open-graph="handleOpenGraph"
      @open-concept="handleOpenConcept"
      @open-lens="handleOpenLens"
    />
    <div id="book-quick-capture">
      <BookCaptureSection :book="book" />
    </div>

    <section class="detail-grid" aria-label="Book metadata and reading controls">
      <AppCard as="article" class="detail-panel">
        <div class="detail-panel__heading">
          <div class="eyebrow">Source Metadata</div>
          <h2>Book record</h2>
        </div>
        <dl class="detail-meta">
          <div>
            <dt>Category</dt>
            <dd>{{ book.category ?? 'Uncategorized' }}</dd>
          </div>
          <div>
            <dt>Publisher</dt>
            <dd>{{ book.publisher ?? 'Unknown' }}</dd>
          </div>
          <div>
            <dt>Year</dt>
            <dd>{{ book.publicationYear ?? 'Unknown' }}</dd>
          </div>
          <div>
            <dt>Visibility</dt>
            <dd>{{ book.visibility }}</dd>
          </div>
          <div>
            <dt>ISBN</dt>
            <dd>{{ book.isbn ?? 'Not set' }}</dd>
          </div>
        </dl>
        <p class="detail-description">{{ book.description ?? 'No description yet.' }}</p>
        <div v-if="book.tags.length" class="detail-tags" aria-label="Book tags">
          <AppBadge v-for="tag in book.tags" :key="tag" variant="primary" size="sm">{{ tag }}</AppBadge>
        </div>
      </AppCard>

      <AppCard id="library-state" as="section" class="detail-library">
        <div class="detail-panel__heading">
          <div class="eyebrow">Reading Controls</div>
          <h2>Your library state</h2>
        </div>

        <template v-if="book.inLibrary && book.userBookId">
          <BookStatusBadge :status="status" />

          <label class="detail-field">
            <span>Reading status</span>
            <el-select v-model="status" @change="handleStatusUpdate" aria-label="Reading status">
              <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>

          <div class="detail-progress-control">
            <BookProgressBar :progress="progress" />
            <label class="detail-field">
              <span>Progress percentage</span>
              <el-input-number v-model="progress" :min="0" :max="100" @change="handleProgressUpdate" aria-label="Progress percentage" />
            </label>
          </div>

          <div class="detail-rating-control">
            <BookRating :rating="rating" />
            <label class="detail-field">
              <span>Rating</span>
              <el-rate v-model="rating" aria-label="Rating" @change="handleRatingUpdate" />
            </label>
          </div>
        </template>

        <div v-else class="detail-library__empty">
          <p>Add this book to your library to track status, progress, rating, and five-star favorite state.</p>
          <AppButton variant="primary" @click="handleAddToLibrary">Add to Library</AppButton>
        </div>
      </AppCard>
    </section>

    <BacklinksSection
      entity-type="BOOK"
      :entity-id="book.id"
      :source-references="sourceReferenceRecords"
      :book-title="book.title"
    />

    <ApplyToProjectDialog
      v-if="daily?.prompt"
      v-model="applyDailyPromptOpen"
      source-type="DAILY_DESIGN_PROMPT"
      :source-id="daily.prompt.id"
      :source-reference="daily.prompt.sourceReference"
      :source-label="daily.prompt.sourceTitle ?? daily.prompt.bookTitle ?? 'Daily design prompt'"
      :default-title="daily.prompt.question"
      :default-description="daily.prompt.templatePrompt ? 'Template prompt application. No source page is implied.' : daily.prompt.question"
    />
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { addBookToLibrary, getBook } from '../api/books'
import { getActionItems } from '../api/actionItems'
import { getCaptureInbox } from '../api/captures'
import { createPrototypeTaskFromDaily, getDailyToday, regenerateDaily, saveDailyReflection, skipDaily } from '../api/daily'
import { getBookGraph } from '../api/graph'
import { getBookConcepts } from '../api/knowledge'
import { finishReadingSession, generateReviewFromBook, getBookReadingSessions, startReadingSession } from '../api/learning'
import { getBookNotes } from '../api/notes'
import { getQuotes } from '../api/quotes'
import { getBookSourceReferences } from '../api/sourceReferences'
import { updateUserBookProgress, updateUserBookRating, updateUserBookStatus } from '../api/userBooks'
import BookContextHeader from '../components/book-detail/BookContextHeader.vue'
import BookCaptureSection from '../components/book-detail/BookCaptureSection.vue'
import BookHero from '../components/book-detail/BookHero.vue'
import BookInsightCards from '../components/book-detail/BookInsightCards.vue'
import BookKnowledgeSection from '../components/book-detail/BookKnowledgeSection.vue'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import ApplyToProjectDialog from '../components/project/ApplyToProjectDialog.vue'
import BookProgressBar from '../components/BookProgressBar.vue'
import BookRating from '../components/BookRating.vue'
import BookStatusBadge from '../components/BookStatusBadge.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useRightRailStore } from '../stores/rightRail'
import type {
  ActionItemRecord,
  BookConceptPreview,
  BookNoteRecord,
  BookRecord,
  ConceptRecord,
  DailyTarget,
  DailyTodayRecord,
  GraphRecord,
  QuoteRecord,
  RawCaptureRecord,
  ReadingSessionRecord,
  ReadingStatus,
  SourceReferenceRecord,
} from '../types'
import { readingStatusOptions } from '../types'

const route = useRoute()
const router = useRouter()
const rightRail = useRightRailStore()
const { openSource } = useOpenSource()
const bookUseCaseSlugs = [
  'capture-idea-while-reading',
  'capture-to-quote',
  'capture-to-action-item',
  'review-concept-marker',
]
const bookWorkflowLoop = ['Book source', 'Quick capture', 'Parsed knowledge', 'Project action']
const book = ref<BookRecord | null>(null)
const status = ref<ReadingStatus>('BACKLOG')
const progress = ref(0)
const rating = ref(0)
const favoriteLoading = ref(false)
const loading = ref(true)
const errorMessage = ref('')
const cockpitWarning = ref('')
const statusOptions = readingStatusOptions
const latestQuote = ref<QuoteRecord | null>(null)
const latestSourceReference = ref<SourceReferenceRecord | null>(null)
const quoteRecords = ref<QuoteRecord[]>([])
const actionItemRecords = ref<ActionItemRecord[]>([])
const sourceReferenceRecords = ref<SourceReferenceRecord[]>([])
const daily = ref<DailyTodayRecord | null>(null)
const dailyActionLoading = ref(false)
const applyDailyPromptOpen = ref(false)
const readingSessions = ref<ReadingSessionRecord[]>([])
const activeReadingSession = ref<ReadingSessionRecord | null>(null)
const readingStartPage = ref<number | null>(null)
const readingEndPage = ref<number | null>(null)
const readingMinutes = ref<number | null>(null)
const readingReflection = ref('')
const readingBusy = ref(false)
const bookNextStep = computed(() => {
  const currentBook = book.value
  if (!currentBook) {
    return {
      title: 'Open a book to start a workflow',
      description: 'Book detail workflows appear after the book record loads.',
      primaryLabel: 'Open Library',
      primaryTo: { name: 'my-library' },
      primaryLoading: false,
      secondaryLabel: null,
      secondaryTo: null,
    }
  }

  if (!currentBook.inLibrary) {
    return {
      title: 'Add this book to start tracking it',
      description: 'Library state unlocks reading progress, rating, sessions, captures, and source-backed review for this book.',
      primaryLabel: 'Add to Library',
      primaryTo: null,
      primaryLoading: false,
      secondaryLabel: 'Open Notes',
      secondaryTo: { name: 'book-notes', params: { bookId: currentBook.id } },
    }
  }

  if (!currentBook.capturesCount && !currentBook.notesCount) {
    return {
      title: 'Capture the first source-backed idea',
      description: 'Start with one raw thought, quote, or page note. BookOS will keep the book context and parsed metadata visible.',
      primaryLabel: 'Capture a Thought',
      primaryTo: { name: 'book-detail', params: { id: currentBook.id }, hash: '#book-quick-capture' },
      primaryLoading: false,
      secondaryLabel: 'Open Notes',
      secondaryTo: { name: 'book-notes', params: { bookId: currentBook.id } },
    }
  }

  if ((currentBook.actionItemsCount ?? 0) > 0) {
    return {
      title: 'Act on the knowledge extracted from this book',
      description: 'Review the source-backed action items from this book and turn one into completed design work.',
      primaryLabel: 'Open Action Items',
      primaryTo: { name: 'action-items', query: { bookId: String(currentBook.id) } },
      primaryLoading: false,
      secondaryLabel: 'Create Review',
      secondaryTo: null,
    }
  }

  return {
    title: 'Review this book before adding more modules',
    description: 'Create a source-backed review session from the existing notes, quotes, and captures before moving into advanced graph work.',
    primaryLabel: 'Create Review',
    primaryTo: null,
    primaryLoading: readingBusy.value,
    secondaryLabel: 'Capture a Thought',
    secondaryTo: { name: 'book-detail', params: { id: currentBook.id }, hash: '#book-quick-capture' },
  }
})

onMounted(loadBook)
onUnmounted(() => {
  if (book.value) rightRail.clearSourceForBook(book.value.id)
  rightRail.setActionItemsFromRecords([])
})

async function loadBook() {
  loading.value = true
  errorMessage.value = ''
  cockpitWarning.value = ''

  try {
    const result = await getBook(Number(route.params.id))
    const hydrated = await hydrateBookKnowledge(result)
    book.value = hydrated
    setRightRailSourceFromRouteOrLatest(hydrated)
    status.value = hydrated.readingStatus ?? 'BACKLOG'
    progress.value = hydrated.progressPercent ?? 0
    rating.value = hydrated.rating ?? 0
  } catch {
    book.value = null
    errorMessage.value = 'Check your connection and try opening this book again.'
  } finally {
    loading.value = false
  }
}

async function hydrateBookKnowledge(base: BookRecord): Promise<BookRecord> {
  const [notesResult, capturesResult, quotesResult, actionItemsResult, sourceReferencesResult, conceptsResult, dailyResult, graphResult, readingSessionsResult] =
    (await Promise.allSettled([
      getBookNotes(base.id),
      getCaptureInbox({ bookId: base.id }),
      getQuotes({ bookId: base.id }),
      getActionItems({ bookId: base.id }),
      getBookSourceReferences(base.id),
      getBookConcepts(base.id),
      getDailyToday(),
      getBookGraph(base.id),
      getBookReadingSessions(base.id),
    ])) as [
      PromiseSettledResult<BookNoteRecord[]>,
      PromiseSettledResult<RawCaptureRecord[]>,
      PromiseSettledResult<QuoteRecord[]>,
      PromiseSettledResult<ActionItemRecord[]>,
      PromiseSettledResult<SourceReferenceRecord[]>,
      PromiseSettledResult<ConceptRecord[]>,
      PromiseSettledResult<DailyTodayRecord>,
      PromiseSettledResult<GraphRecord>,
      PromiseSettledResult<ReadingSessionRecord[]>,
    ]

  const notes = settledValue(notesResult, [])
  const captures = settledValue(capturesResult, [])
  const quotes = settledValue(quotesResult, [])
  const actionItems = settledValue(actionItemsResult, [])
  const sourceReferences = settledValue(sourceReferencesResult, [])
  const concepts = settledValue(conceptsResult, [])
  const dailyData = settledValue<DailyTodayRecord | null>(dailyResult, null)
  const graph = settledValue<GraphRecord | null>(graphResult, null)
  const sessions = settledValue(readingSessionsResult, [])

  daily.value = dailyData
  readingSessions.value = sessions
  activeReadingSession.value = sessions.find((session) => !session.endedAt) ?? null
  quoteRecords.value = quotes
  actionItemRecords.value = actionItems
  sourceReferenceRecords.value = sourceReferences
  latestQuote.value = quotes[0] ?? null
  latestSourceReference.value = sourceReferences[0] ?? null
  rightRail.setActionItemsFromRecords(actionItems.slice(0, 6))
  cockpitWarning.value = buildCockpitWarning([
    ['notes', notesResult],
    ['raw captures', capturesResult],
    ['quotes', quotesResult],
    ['action items', actionItemsResult],
    ['source references', sourceReferencesResult],
    ['concepts', conceptsResult],
    ['daily resurfacing', dailyResult],
    ['knowledge graph', graphResult],
    ['reading sessions', readingSessionsResult],
  ])

  const conceptPreviews = concepts.map(toConceptPreview)
  const quotePreview = dailyData?.sentence ? dailySentencePreview(dailyData) : null

  return {
    ...base,
    latestQuote: latestQuote.value
      ? {
          id: latestQuote.value.id,
          text: latestQuote.value.text,
          author: latestQuote.value.attribution,
          page: latestQuote.value.pageStart,
          sourceLabel: latestQuote.value.sourceReference?.locationLabel,
        }
      : null,
    dailyQuote: quotePreview,
    dailyDesignPrompt: dailyData?.prompt ? dailyPromptPreview(dailyData) : null,
    notesCount: notes.length,
    quotesCount: quotes.length,
    actionItemsCount: actionItems.length,
    capturesCount: captures.length,
    sourceReferencesCount: sourceReferences.length,
    conceptsCount: conceptPreviews.length,
    concepts: conceptPreviews,
    ontologyConceptCount: conceptPreviews.length,
    knowledgeGraph: {
      ...(base.knowledgeGraph ?? {}),
      concepts: conceptPreviews,
      nodes: graph?.nodes ?? [],
      edges: graph?.edges ?? [],
    },
  }
}

function dailySentencePreview(dailyData: DailyTodayRecord) {
  if (!dailyData.sentence) return null
  return {
    id: dailyData.sentence.id,
    text: dailyData.sentence.text,
    author: dailyData.sentence.attribution ?? dailyData.sentence.bookTitle,
    page: dailyData.sentence.pageStart,
    sourceLabel: dailyData.sentence.sourceReference?.locationLabel ?? dailyData.sentence.bookTitle,
  }
}

function dailyPromptPreview(dailyData: DailyTodayRecord) {
  return {
    id: dailyData.prompt.id,
    question: dailyData.prompt.question,
    linkedConcept: dailyData.prompt.sourceTitle,
    sourceTitle: dailyData.prompt.sourceTitle,
    templatePrompt: dailyData.prompt.templatePrompt,
  }
}

function applyDailyState(dailyData: DailyTodayRecord) {
  daily.value = dailyData
  if (!book.value) return
  book.value = {
    ...book.value,
    dailyQuote: dailySentencePreview(dailyData),
    dailyDesignPrompt: dailyData.prompt ? dailyPromptPreview(dailyData) : null,
  }
}

function settledValue<T>(result: PromiseSettledResult<T>, fallback: T) {
  return result.status === 'fulfilled' ? result.value : fallback
}

function toConceptPreview(concept: ConceptRecord): BookConceptPreview {
  const mentionCount = concept.mentionCount ?? concept.sourceReferences.length
  return {
    id: concept.id,
    name: concept.name,
    type: 'Concept',
    relevance: Math.min(100, mentionCount * 20),
    mentions: mentionCount,
    edgeStrength: mentionCount >= 3 ? 'strong' : mentionCount >= 2 ? 'medium' : 'weak',
  }
}

function setRightRailSourceFromRouteOrLatest(currentBook: BookRecord) {
  const sourceType = typeof route.query.sourceType === 'string' ? route.query.sourceType.toUpperCase() : null
  const sourceId = typeof route.query.sourceId === 'string' ? Number(route.query.sourceId) : null

  if (sourceType === 'QUOTE' && sourceId) {
    const quote = quoteRecords.value.find((item) => item.id === sourceId)
    if (quote) {
      rightRail.setSourceFromQuote(quote, currentBook)
      return
    }
  }

  if (sourceType === 'ACTION_ITEM' && sourceId) {
    const actionItem = actionItemRecords.value.find((item) => item.id === sourceId)
    if (actionItem) {
      rightRail.setSourceFromActionItem(actionItem, currentBook)
      return
    }
  }

  if ((sourceType === 'SOURCE_REFERENCE' || sourceType === 'NOTE') && sourceId) {
    const sourceReference = sourceReferenceRecords.value.find((item) => item.id === sourceId)
    if (sourceReference) {
      rightRail.setSourceFromReference(sourceReference, currentBook.title, currentBook)
      return
    }
  }

  if (latestQuote.value) {
    rightRail.setSourceFromQuote(latestQuote.value, currentBook)
    return
  }

  if (latestSourceReference.value) {
    rightRail.setSourceFromReference(latestSourceReference.value, currentBook.title, currentBook)
    return
  }

  rightRail.setCurrentBookSource(currentBook)
}

function buildCockpitWarning(results: Array<[string, PromiseSettledResult<unknown>]>) {
  const failed = results
    .filter(([, result]) => result.status === 'rejected')
    .map(([label, result]) => `${label}: ${loadFailureReason(result)}`)

  return failed.length ? failed.join(' ') : ''
}

function loadFailureReason(result: PromiseSettledResult<unknown>) {
  if (result.status !== 'rejected') return ''

  if (!axios.isAxiosError(result.reason)) {
    return 'unavailable.'
  }

  if (!result.reason.response) {
    return 'backend unavailable.'
  }

  if (result.reason.response.status === 403) {
    return 'permission denied.'
  }

  const data = result.reason.response.data as { message?: string } | undefined
  return data?.message ? `${data.message}.` : 'could not be loaded.'
}

async function handleAddToLibrary() {
  if (!book.value) return
  const userBook = await addBookToLibrary(book.value.id)
  book.value = {
    ...book.value,
    inLibrary: true,
    userBookId: userBook.id,
    readingStatus: userBook.readingStatus,
    readingFormat: userBook.readingFormat,
    ownershipStatus: userBook.ownershipStatus,
    progressPercent: userBook.progressPercent,
    rating: userBook.rating,
  }
  status.value = userBook.readingStatus
  progress.value = userBook.progressPercent
  rating.value = userBook.rating ?? 0
  ElMessage.success('Book added to your library.')
}

function handleBookNextStep() {
  if (!book.value?.inLibrary) {
    void handleAddToLibrary()
    return
  }

  void createBookReview()
}

async function handleStatusUpdate(value: ReadingStatus) {
  if (!book.value?.userBookId) return
  const userBook = await updateUserBookStatus(book.value.userBookId, value)
  book.value.readingStatus = userBook.readingStatus
  status.value = userBook.readingStatus
  ElMessage.success('Status updated.')
}

async function handleProgressUpdate(value: number | undefined) {
  if (!book.value?.userBookId) return
  const userBook = await updateUserBookProgress(book.value.userBookId, Number(value ?? 0))
  book.value.progressPercent = userBook.progressPercent
  progress.value = userBook.progressPercent
  ElMessage.success('Progress updated.')
}

async function handleRatingUpdate(value: number) {
  if (!book.value?.userBookId) return
  const userBook = await updateUserBookRating(book.value.userBookId, Number(value))
  book.value.rating = userBook.rating
  rating.value = userBook.rating ?? 0
  ElMessage.success('Rating updated.')
}

async function handleFavoriteToggle() {
  if (!book.value?.userBookId) {
    ElMessage.warning('Add this book to your library before marking it as a favorite.')
    return
  }

  favoriteLoading.value = true
  try {
    const nextRating = rating.value >= 5 ? 0 : 5
    const userBook = await updateUserBookRating(book.value.userBookId, nextRating)
    book.value.rating = userBook.rating
    rating.value = userBook.rating ?? 0
    ElMessage.success(nextRating === 5 ? 'Book marked as a five-star favorite.' : 'Book removed from five-star favorites.')
  } finally {
    favoriteLoading.value = false
  }
}

async function startSession() {
  if (!book.value) return
  readingBusy.value = true
  try {
    const session = await startReadingSession({
      bookId: book.value.id,
      startPage: readingStartPage.value,
      reflection: readingReflection.value.trim() || null,
    })
    activeReadingSession.value = session
    readingSessions.value = [session, ...readingSessions.value]
    ElMessage.success('Reading session started.')
  } finally {
    readingBusy.value = false
  }
}

async function finishSession() {
  if (!activeReadingSession.value) return
  readingBusy.value = true
  try {
    const session = await finishReadingSession(activeReadingSession.value.id, {
      endPage: readingEndPage.value,
      minutesRead: readingMinutes.value,
      notesCount: book.value?.notesCount ?? 0,
      capturesCount: book.value?.capturesCount ?? 0,
      reflection: readingReflection.value.trim() || null,
    })
    readingSessions.value = readingSessions.value.map((item) => (item.id === session.id ? session : item))
    activeReadingSession.value = null
    readingReflection.value = ''
    ElMessage.success('Reading session finished.')
  } finally {
    readingBusy.value = false
  }
}

async function createBookReview() {
  if (!book.value) return
  readingBusy.value = true
  try {
    const session = await generateReviewFromBook({ id: book.value.id, title: `Review ${book.value.title}`, mode: 'SOURCE_REVIEW', limit: 8 })
    await router.push({ name: 'review-detail', params: { id: session.id } })
  } finally {
    readingBusy.value = false
  }
}

function handleOpenSource(target?: DailyTarget) {
  if (!book.value) return
  if (target === 'SENTENCE' && daily.value?.sentence?.sourceReference) {
    const source = daily.value.sentence.sourceReference
    rightRail.setSourceFromReference(source, daily.value.sentence.bookTitle ?? book.value.title, book.value)
    void openSource({
      sourceType: 'DAILY_SENTENCE',
      sourceId: daily.value.sentence.id,
      bookId: source.bookId,
      bookTitle: daily.value.sentence.bookTitle ?? book.value.title,
      pageStart: source.pageStart,
      noteId: source.noteId ?? undefined,
      rawCaptureId: source.rawCaptureId ?? undefined,
      noteBlockId: source.noteBlockId ?? undefined,
      sourceReference: source,
      sourceReferenceId: source.id,
    })
    return
  }

  if (target === 'PROMPT' && daily.value?.prompt?.sourceReference) {
    const source = daily.value.prompt.sourceReference
    rightRail.setSourceFromReference(source, daily.value.prompt.bookTitle ?? book.value.title, book.value)
    void openSource({
      sourceType: 'DAILY_PROMPT',
      sourceId: daily.value.prompt.id,
      bookId: source.bookId,
      bookTitle: daily.value.prompt.bookTitle ?? book.value.title,
      pageStart: source.pageStart,
      noteId: source.noteId ?? undefined,
      rawCaptureId: source.rawCaptureId ?? undefined,
      noteBlockId: source.noteBlockId ?? undefined,
      sourceReference: source,
      sourceReferenceId: source.id,
    })
    return
  }

  if (latestQuote.value) {
    rightRail.setSourceFromQuote(latestQuote.value, book.value)
    void openSource({
      sourceType: 'QUOTE',
      sourceId: latestQuote.value.id,
      bookId: book.value.id,
      bookTitle: book.value.title,
      pageStart: latestQuote.value.pageStart,
      noteId: latestQuote.value.noteId ?? undefined,
      rawCaptureId: latestQuote.value.rawCaptureId ?? undefined,
      noteBlockId: latestQuote.value.noteBlockId ?? undefined,
      sourceReference: latestQuote.value.sourceReference,
      sourceReferenceId: latestQuote.value.sourceReference?.id ?? null,
    })
    return
  }

  if (latestSourceReference.value) {
    rightRail.setSourceFromReference(latestSourceReference.value, book.value.title, book.value)
    void openSource({
      sourceType: 'SOURCE_REFERENCE',
      sourceId: latestSourceReference.value.id,
      bookId: book.value.id,
      bookTitle: book.value.title,
      pageStart: latestSourceReference.value.pageStart,
      noteId: latestSourceReference.value.noteId ?? undefined,
      rawCaptureId: latestSourceReference.value.rawCaptureId ?? undefined,
      noteBlockId: latestSourceReference.value.noteBlockId ?? undefined,
      sourceReference: latestSourceReference.value,
      sourceReferenceId: latestSourceReference.value.id,
    })
    return
  } else {
    rightRail.setCurrentBookSource(book.value)
  }
  router.push({ name: 'book-detail', params: { id: book.value.id }, hash: '#library-state' })
}

async function handleRegenerateDaily(target: DailyTarget) {
  dailyActionLoading.value = true
  try {
    applyDailyState(await regenerateDaily(target))
    ElMessage.success('Daily item regenerated.')
  } finally {
    dailyActionLoading.value = false
  }
}

async function handleSkipDaily(target: DailyTarget) {
  dailyActionLoading.value = true
  try {
    applyDailyState(await skipDaily(target))
    ElMessage.success('Daily item skipped.')
  } finally {
    dailyActionLoading.value = false
  }
}

async function handleSaveDailyReflection(payload: { target: DailyTarget; text: string }) {
  dailyActionLoading.value = true
  try {
    const currentDaily = daily.value
    await saveDailyReflection({
      target: payload.target,
      dailySentenceId: payload.target === 'SENTENCE' ? currentDaily?.sentence?.id ?? null : null,
      dailyDesignPromptId: payload.target === 'PROMPT' ? currentDaily?.prompt.id ?? null : null,
      reflectionText: payload.text,
    })
    applyDailyState(await getDailyToday())
    ElMessage.success('Daily reflection saved.')
  } finally {
    dailyActionLoading.value = false
  }
}

async function handleCreatePrototypeTask(promptId: number | string | null) {
  dailyActionLoading.value = true
  try {
    const task = await createPrototypeTaskFromDaily({ dailyDesignPromptId: promptId === null ? null : Number(promptId) })
    ElMessage.success('Prototype task created.')
    router.push({ name: 'knowledge-detail', params: { id: task.id } })
  } finally {
    dailyActionLoading.value = false
  }
}

function handleOpenGraph() {
  if (!book.value) return
  router.push({ name: 'graph-book', params: { bookId: String(book.value.id) } })
}

function handleOpenConcept(name: string) {
  if (!book.value) return
  router.push({ name: 'concepts', query: { q: name === 'all' ? undefined : name, bookId: String(book.value.id) } })
}

function handleOpenLens(name: string) {
  if (!book.value) return
  router.push({ name: 'dashboard', query: { focus: 'lens', lens: name, bookId: String(book.value.id) } })
}

function formatDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}
</script>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 0.72fr);
  gap: var(--space-4);
}

.book-detail-state {
  padding: var(--space-6);
}

.cockpit-warning {
  padding: var(--space-4);
  border-color: color-mix(in srgb, var(--bookos-warning) 28%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-warning-soft) 52%, var(--bookos-surface));
}

.cockpit-warning strong {
  color: var(--bookos-text-primary);
}

.cockpit-warning p {
  margin: var(--space-2) 0 0;
  color: var(--bookos-text-secondary);
}

.detail-panel,
.detail-library,
.reading-session-panel {
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.reading-session-panel__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.reading-session-panel__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.reading-session-panel__meta {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.detail-panel__heading {
  display: grid;
  gap: var(--space-1);
}

.detail-panel__heading h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-section-title);
  line-height: var(--type-title-line);
}

.detail-meta {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.detail-meta div {
  min-width: 0;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.detail-meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.detail-meta dd {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-weight: 800;
  overflow-wrap: anywhere;
}

.detail-description {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.detail-tags,
.detail-rating-control {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.detail-field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.detail-progress-control,
.detail-rating-control {
  display: grid;
  gap: var(--space-3);
}

.detail-library__empty {
  display: grid;
  gap: var(--space-3);
}

.detail-library__empty p {
  margin: 0;
  color: var(--bookos-text-secondary);
}

@media (max-width: 1180px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .detail-meta,
  .reading-session-panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>
