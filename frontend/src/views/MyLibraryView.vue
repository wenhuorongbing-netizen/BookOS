<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="eyebrow">My Library</div>
        <h1>Personal shelf and shared catalog</h1>
        <p>Update status, progress, and ratings from one place, then pull new books in from the shared shelf.</p>
      </div>
      <RouterLink to="/books/new">
        <el-button type="primary">Add another book</el-button>
      </RouterLink>
    </div>

    <BookFilterBar v-model="filters" :categories="categories" :tags="tags" @apply="noop" />

    <section class="page-shell">
      <div class="page-header">
        <div>
          <div class="eyebrow">Personal Shelf</div>
          <h2>Tracked books</h2>
        </div>
      </div>
      <BookTable
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
      <div v-else class="surface-card empty-state">Everything matching the current filter is already in your library.</div>
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
