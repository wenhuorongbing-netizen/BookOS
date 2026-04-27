<template>
  <div class="page-shell">
    <AppErrorState
      v-if="error"
      title="Dashboard data could not be loaded"
      :description="error"
      retry-label="Retry"
      @retry="loadDashboard"
    />

    <section v-else class="stats-grid" aria-label="Library dashboard stats">
      <AppStat label="Catalog" :value="books.length" description="Books in the shared shelf" eyebrow="Catalog" />
      <AppStat label="My Library" :value="library.length" description="Books attached to your account" eyebrow="Library" tone="primary" />
      <AppStat
        label="Current Reading"
        :value="currentlyReading.length"
        description="Books actively in progress"
        eyebrow="Reading"
        tone="accent"
      />
      <AppStat label="Five-Star Shelf" :value="fiveStar.length" description="Books you rated 5/5" eyebrow="Quality" tone="success" />
      <AppStat label="Anti-Library" :value="antiLibrary.length" description="Books parked for later curiosity" eyebrow="Backlog" tone="warning" />
    </section>

    <AppSectionHeader
      eyebrow="Next Actions"
      title="Build the shelf before the knowledge graph"
      description="Milestone 1 is focused on getting book ingestion and personal library workflows stable."
    >
      <template #actions>
        <RouterLink to="/books/new" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Add a new book</AppButton>
        </RouterLink>
        <RouterLink to="/my-library" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Open my library</AppButton>
        </RouterLink>
        <RouterLink to="/anti-library" custom v-slot="{ navigate }">
          <AppButton variant="ghost" @click="navigate">Review anti-library</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <section class="page-shell">
      <AppSectionHeader eyebrow="Currently Reading" title="Active books" compact />
      <AppLoadingState v-if="loading && !currentlyReading.length" label="Loading active books" />
      <div v-if="currentlyReading.length" class="responsive-grid">
        <BookCard v-for="book in currentlyReading" :key="book.id" :book="book" @primary="openBook" />
      </div>
      <AppEmptyState
        v-else-if="!loading"
        title="No active books yet"
        description="Mark a book as currently reading to keep it visible in your daily BookOS workflow."
        compact
      />
    </section>

    <section class="page-shell">
      <AppSectionHeader eyebrow="Catalog Snapshot" title="Recent books" compact />
      <AppLoadingState v-if="loading && !books.length" label="Loading books" />
      <div v-if="books.length" class="responsive-grid">
        <BookCard v-for="book in books.slice(0, 6)" :key="book.id" :book="book" @primary="openBook" />
      </div>
      <AppEmptyState v-else-if="!loading" title="No books found" description="Add a book to start building the BookOS catalog." compact />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getBooks } from '../api/books'
import { getAntiLibraryBooks, getCurrentlyReading, getFiveStarBooks, getUserBooks } from '../api/userBooks'
import type { BookRecord, UserBookRecord } from '../types'
import BookCard from '../components/BookCard.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import AppStat from '../components/ui/AppStat.vue'

const router = useRouter()
const books = ref<BookRecord[]>([])
const library = ref<UserBookRecord[]>([])
const currentlyReading = ref<UserBookRecord[]>([])
const fiveStar = ref<UserBookRecord[]>([])
const antiLibrary = ref<UserBookRecord[]>([])
const loading = ref(true)
const error = ref('')

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const [catalog, userLibrary, current, stars, anti] = await Promise.all([
      getBooks(),
      getUserBooks(),
      getCurrentlyReading(),
      getFiveStarBooks(),
      getAntiLibraryBooks(),
    ])

    books.value = catalog
    library.value = userLibrary
    currentlyReading.value = current
    fiveStar.value = stars
    antiLibrary.value = anti
  } catch {
    error.value = 'Check the API connection and try again.'
  } finally {
    loading.value = false
  }
}

function openBook(book: BookRecord | UserBookRecord) {
  const id = 'bookId' in book ? book.bookId : book.id
  router.push({ name: 'book-detail', params: { id } })
}
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 1rem;
}

@media (max-width: 1180px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
