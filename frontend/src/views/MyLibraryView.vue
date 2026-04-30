<template>
  <div class="page-shell">
    <AppSectionHeader
      eyebrow="Library"
      title="Choose what you are reading now"
      description="Keep the library practical: add one real book, set its status, then open it to capture source-backed notes."
      :level="1"
    >
      <template #actions>
        <RouterLink to="/books/new" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">{{ library.length ? 'Add another book' : 'Add first book' }}</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppCard class="task-first-panel" variant="highlight" as="section">
      <div>
        <div class="eyebrow">Next library action</div>
        <h2>{{ libraryTask.title }}</h2>
        <p>{{ libraryTask.description }}</p>
        <div class="task-first-panel__metrics" aria-label="Library summary">
          <AppBadge variant="primary">{{ library.length }} tracked</AppBadge>
          <AppBadge variant="accent">{{ currentReadingBooks.length }} reading now</AppBadge>
          <AppBadge variant="neutral">{{ discoverBooks.length }} discoverable</AppBadge>
        </div>
      </div>
      <div class="task-first-panel__actions">
        <RouterLink v-if="libraryTask.routeName" :to="{ name: libraryTask.routeName, params: libraryTask.routeParams }" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">{{ libraryTask.primaryLabel }}</AppButton>
        </RouterLink>
        <AppButton v-else variant="primary" @click="openBook(libraryTask.book!)">{{ libraryTask.primaryLabel }}</AppButton>
        <RouterLink to="/use-cases/track-book-start-to-finish" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">See workflow</AppButton>
        </RouterLink>
      </div>
    </AppCard>

    <UseCaseSuggestionPanel
      :slugs="['track-book-start-to-finish', 'capture-idea-while-reading', 'open-source-from-quote-or-action']"
      eyebrow="Library playbook"
      title="Use the library as the start of the loop"
      description="A book only becomes useful after it has reading status, captures, notes, and source-backed follow-up."
    />

    <BookFilterBar v-model="filters" :categories="categories" :tags="tags" @apply="noop" />

    <section class="page-shell">
      <div class="page-header">
        <div>
          <div class="eyebrow">Personal Shelf</div>
          <h2>Tracked books</h2>
        </div>
      </div>
      <AppEmptyState
        v-if="!loading && !library.length"
        title="Add one book to start"
        description="The first useful BookOS action is adding a real book, then setting its reading status."
        eyebrow="First-day flow"
      >
        <template #actions>
          <RouterLink to="/books/new" custom v-slot="{ navigate }">
            <AppButton variant="primary" @click="navigate">Add first book</AppButton>
          </RouterLink>
        </template>
      </AppEmptyState>
      <BookTable
        v-else
        :rows="filteredLibrary"
        :loading="loading"
        @open="openBook"
        @status-change="handleStatusChange"
        @progress-change="handleProgressChange"
        @rating-change="handleRatingChange"
      />
    </section>

    <section class="page-shell">
      <div class="page-header">
        <div>
          <div class="eyebrow">Discover</div>
          <h2>Books not yet in your library</h2>
        </div>
      </div>
      <div v-if="discoverBooks.length" class="responsive-grid">
        <BookCard
          v-for="book in discoverBooks"
          :key="book.id"
          :book="book"
          :show-secondary="true"
          secondary-label="Add to library"
          @primary="openBook"
          @secondary="handleAddToLibrary"
        />
      </div>
      <AppEmptyState
        v-else
        title="Nothing new in Discover"
        description="Everything matching the current filter is already in your library. Clear filters or add a new book manually."
        eyebrow="Discover"
        compact
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { addBookToLibrary, getBooks } from '../api/books'
import {
  getUserBooks,
  updateUserBookProgress,
  updateUserBookRating,
  updateUserBookStatus,
} from '../api/userBooks'
import BookCard from '../components/BookCard.vue'
import BookFilterBar from '../components/BookFilterBar.vue'
import BookTable from '../components/BookTable.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import type { BookFilterState, BookRecord, ReadingStatus, UserBookRecord } from '../types'

const router = useRouter()
const loading = ref(false)
const books = ref<BookRecord[]>([])
const library = ref<UserBookRecord[]>([])
const filters = ref<BookFilterState>({ q: '', category: '', tag: '' })

const categories = computed(() =>
  Array.from(new Set(books.value.map((book) => book.category).filter(Boolean) as string[])).sort(),
)

const tags = computed(() =>
  Array.from(new Set(books.value.flatMap((book) => book.tags))).sort(),
)

const filteredLibrary = computed(() => library.value.filter(matchesFilters))
const discoverBooks = computed(() => books.value.filter((book) => !book.inLibrary && matchesFilters(book)))
const currentReadingBooks = computed(() => library.value.filter((book) => book.readingStatus === 'CURRENTLY_READING'))
const libraryTask = computed(() => {
  const current = currentReadingBooks.value[0]
  if (current) {
    return {
      title: `Continue ${current.title}`,
      description: 'Open the book cockpit, capture the next useful idea, or update reading progress before moving on.',
      primaryLabel: 'Open current book',
      book: current,
      routeName: '',
      routeParams: {},
    }
  }

  const firstBook = library.value[0]
  if (firstBook) {
    return {
      title: 'Pick one tracked book to read next',
      description: 'Set the book to Currently Reading when it becomes active, then use the book detail page for notes and captures.',
      primaryLabel: 'Open tracked book',
      book: firstBook,
      routeName: '',
      routeParams: {},
    }
  }

  return {
    title: 'Add one real book',
    description: 'Start small. Add a book you are actually reading so notes, captures, quotes, and actions have a real source.',
    primaryLabel: 'Add first book',
    book: null,
    routeName: 'add-book',
    routeParams: {},
  }
})

onMounted(refresh)

async function refresh() {
  loading.value = true
  try {
    const [catalog, libraryItems] = await Promise.all([getBooks(), getUserBooks()])
    books.value = catalog
    library.value = libraryItems
  } finally {
    loading.value = false
  }
}

async function handleAddToLibrary(book: BookRecord | UserBookRecord) {
  const id = 'bookId' in book ? book.bookId : book.id
  await addBookToLibrary(id)
  ElMessage.success('Book added to your personal library.')
  await refresh()
}

async function handleStatusChange(id: number, status: ReadingStatus) {
  await updateUserBookStatus(id, status)
  ElMessage.success('Reading status updated.')
  await refresh()
}

async function handleProgressChange(id: number, progress: number) {
  await updateUserBookProgress(id, progress)
  ElMessage.success('Progress updated.')
  await refresh()
}

async function handleRatingChange(id: number, rating: number) {
  await updateUserBookRating(id, rating)
  ElMessage.success('Rating updated.')
  await refresh()
}

function openBook(book: BookRecord | UserBookRecord) {
  const id = 'bookId' in book ? book.bookId : book.id
  router.push({ name: 'book-detail', params: { id } })
}

function matchesFilters(book: BookRecord | UserBookRecord) {
  const q = filters.value.q.trim().toLowerCase()
  const category = filters.value.category.trim().toLowerCase()
  const tag = filters.value.tag.trim().toLowerCase()

  const queryMatched =
    !q ||
    [book.title, book.subtitle ?? '', book.category ?? '', book.authors.join(', '), book.tags.join(', ')]
      .join(' ')
      .toLowerCase()
      .includes(q)

  const categoryMatched = !category || (book.category ?? '').toLowerCase() === category
  const tagMatched = !tag || book.tags.some((item) => item.toLowerCase() === tag)
  return queryMatched && categoryMatched && tagMatched
}

function noop() {}
</script>
