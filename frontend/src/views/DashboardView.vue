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

    <section v-if="daily" class="daily-grid" aria-label="Daily resurfacing">
      <AppCard class="daily-card" as="article">
        <div class="eyebrow">Today's Resurfaced Quote</div>
        <h2>Remember</h2>
        <template v-if="daily.sentence">
          <blockquote>{{ daily.sentence.text }}</blockquote>
          <p class="daily-card__meta">
            {{ daily.sentence.bookTitle ?? daily.sentence.attribution ?? 'Source-backed sentence' }}
            <AppBadge v-if="daily.sentence.pageStart" variant="accent" size="sm">p.{{ daily.sentence.pageStart }}</AppBadge>
          </p>
          <div class="daily-card__actions">
            <AppButton variant="secondary" :disabled="!daily.sentence.sourceReference" @click="openDailySource('SENTENCE')">Open source</AppButton>
            <AppButton variant="ghost" :loading="dailyBusy" @click="updateDaily('SENTENCE', 'regenerate')">Regenerate</AppButton>
            <AppButton variant="text" :loading="dailyBusy" @click="updateDaily('SENTENCE', 'skip')">Skip</AppButton>
          </div>
        </template>
        <AppEmptyState
          v-else
          title="No source-backed sentence yet"
          description="Capture or convert a quote before BookOS can resurface daily source material."
          compact
        />
      </AppCard>

      <AppCard class="daily-card" as="article">
        <div class="eyebrow">Daily Design Prompt</div>
        <h2>Think <AppBadge v-if="daily.prompt.templatePrompt" variant="warning" size="sm">Template Prompt</AppBadge></h2>
        <p>{{ daily.prompt.question }}</p>
        <p class="daily-card__meta">{{ daily.prompt.sourceTitle ?? daily.prompt.bookTitle ?? 'Daily prompt' }}</p>
        <div class="daily-card__actions">
          <AppButton variant="secondary" :disabled="!daily.prompt.sourceReference" @click="openDailySource('PROMPT')">Open source</AppButton>
          <AppButton variant="ghost" :loading="dailyBusy" @click="updateDaily('PROMPT', 'regenerate')">Regenerate</AppButton>
          <AppButton variant="text" :loading="dailyBusy" @click="updateDaily('PROMPT', 'skip')">Skip</AppButton>
        </div>
      </AppCard>
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
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getBooks } from '../api/books'
import { getDailyToday, regenerateDaily, skipDaily } from '../api/daily'
import { getAntiLibraryBooks, getCurrentlyReading, getFiveStarBooks, getUserBooks } from '../api/userBooks'
import type { BookRecord, DailyTarget, DailyTodayRecord, UserBookRecord } from '../types'
import BookCard from '../components/BookCard.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import AppStat from '../components/ui/AppStat.vue'
import { useOpenSource } from '../composables/useOpenSource'

const router = useRouter()
const { openSource } = useOpenSource()
const books = ref<BookRecord[]>([])
const library = ref<UserBookRecord[]>([])
const currentlyReading = ref<UserBookRecord[]>([])
const fiveStar = ref<UserBookRecord[]>([])
const antiLibrary = ref<UserBookRecord[]>([])
const daily = ref<DailyTodayRecord | null>(null)
const loading = ref(true)
const dailyBusy = ref(false)
const error = ref('')

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const [catalog, userLibrary, current, stars, anti, today] = await Promise.all([
      getBooks(),
      getUserBooks(),
      getCurrentlyReading(),
      getFiveStarBooks(),
      getAntiLibraryBooks(),
      getDailyToday(),
    ])

    books.value = catalog
    library.value = userLibrary
    currentlyReading.value = current
    fiveStar.value = stars
    antiLibrary.value = anti
    daily.value = today
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

async function updateDaily(target: DailyTarget, action: 'regenerate' | 'skip') {
  dailyBusy.value = true
  try {
    daily.value = action === 'regenerate' ? await regenerateDaily(target) : await skipDaily(target)
    ElMessage.success(action === 'regenerate' ? 'Daily item regenerated.' : 'Daily item skipped.')
  } finally {
    dailyBusy.value = false
  }
}

function openDailySource(target: DailyTarget) {
  const source = target === 'SENTENCE' ? daily.value?.sentence?.sourceReference : daily.value?.prompt.sourceReference
  if (!source) {
    ElMessage.info('This daily item does not have a source reference.')
    return
  }

  void openSource({
    sourceType: target === 'SENTENCE' ? 'DAILY_SENTENCE' : 'DAILY_PROMPT',
    sourceId: target === 'SENTENCE' ? daily.value?.sentence?.id ?? source.id : daily.value?.prompt.id ?? source.id,
    bookId: source.bookId,
    bookTitle: target === 'SENTENCE' ? daily.value?.sentence?.bookTitle ?? undefined : daily.value?.prompt.bookTitle ?? undefined,
    pageStart: source.pageStart,
    noteId: source.noteId ?? undefined,
    rawCaptureId: source.rawCaptureId ?? undefined,
    noteBlockId: source.noteBlockId ?? undefined,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 1rem;
}

.daily-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.daily-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-3);
}

.daily-card h2,
.daily-card p,
.daily-card blockquote {
  margin: 0;
}

.daily-card blockquote {
  padding-left: var(--space-4);
  border-left: 4px solid var(--bookos-accent);
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.16rem;
  line-height: 1.4;
}

.daily-card__meta,
.daily-card p {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.daily-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
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

  .daily-grid {
    grid-template-columns: 1fr;
  }
}
</style>
