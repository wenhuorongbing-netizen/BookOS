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

    <BookInsightCards :book="book" @open-source="handleOpenSource" @open-graph="handleOpenGraph" />
    <BookKnowledgeSection
      :book="book"
      @open-graph="handleOpenGraph"
      @open-concept="handleOpenConcept"
      @open-lens="handleOpenLens"
    />
    <BookCaptureSection :book="book" />

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
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { addBookToLibrary, getBook } from '../api/books'
import { updateUserBookProgress, updateUserBookRating, updateUserBookStatus } from '../api/userBooks'
import BookContextHeader from '../components/book-detail/BookContextHeader.vue'
import BookCaptureSection from '../components/book-detail/BookCaptureSection.vue'
import BookHero from '../components/book-detail/BookHero.vue'
import BookInsightCards from '../components/book-detail/BookInsightCards.vue'
import BookKnowledgeSection from '../components/book-detail/BookKnowledgeSection.vue'
import BookProgressBar from '../components/BookProgressBar.vue'
import BookRating from '../components/BookRating.vue'
import BookStatusBadge from '../components/BookStatusBadge.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import { useRightRailStore } from '../stores/rightRail'
import type { BookRecord, ReadingStatus } from '../types'
import { readingStatusOptions } from '../types'

const route = useRoute()
const router = useRouter()
const rightRail = useRightRailStore()
const book = ref<BookRecord | null>(null)
const status = ref<ReadingStatus>('BACKLOG')
const progress = ref(0)
const rating = ref(0)
const favoriteLoading = ref(false)
const loading = ref(true)
const errorMessage = ref('')
const statusOptions = readingStatusOptions

onMounted(loadBook)
onUnmounted(() => {
  if (book.value) rightRail.clearSourceForBook(book.value.id)
})

async function loadBook() {
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await getBook(Number(route.params.id))
    book.value = result
    rightRail.setCurrentBookSource(result)
    status.value = result.readingStatus ?? 'BACKLOG'
    progress.value = result.progressPercent ?? 0
    rating.value = result.rating ?? 0
  } catch {
    book.value = null
    errorMessage.value = 'Check your connection and try opening this book again.'
  } finally {
    loading.value = false
  }
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

function handleOpenSource() {
  if (!book.value) return
  router.push({ name: 'book-detail', params: { id: book.value.id }, hash: '#library-state' })
}

function handleOpenGraph() {
  if (!book.value) return
  router.push({ name: 'dashboard', query: { focus: 'knowledge-graph', bookId: String(book.value.id) } })
}

function handleOpenConcept(name: string) {
  if (!book.value) return
  router.push({ name: 'dashboard', query: { focus: 'concept', concept: name, bookId: String(book.value.id) } })
}

function handleOpenLens(name: string) {
  if (!book.value) return
  router.push({ name: 'dashboard', query: { focus: 'lens', lens: name, bookId: String(book.value.id) } })
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

.detail-panel,
.detail-library {
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
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
  .detail-meta {
    grid-template-columns: 1fr;
  }
}
</style>
