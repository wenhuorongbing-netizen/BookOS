<template>
  <div class="page-shell quotes-page">
    <AppSectionHeader
      title="Quotes"
      eyebrow="Source-backed library"
      description="Browse, search, create, and edit quotes preserved from captures, notes, and manual entries."
      :level="1"
    >
      <template #actions>
        <AppButton variant="primary" @click="openCreateDialog">Create Quote</AppButton>
      </template>
    </AppSectionHeader>

    <AppCard class="quote-filter" as="section">
      <label class="field">
        <span>Search quotes, authors, source text, tags, or concepts</span>
        <el-input v-model="searchText" clearable placeholder="Search quote text, author, note source, [[Concept]]..." />
      </label>
      <label class="field">
        <span>Filter by book</span>
        <el-select v-model="bookFilter" clearable filterable placeholder="All books" @change="loadQuotes">
          <el-option v-for="book in books" :key="book.id" :label="book.title" :value="book.id" />
        </el-select>
      </label>
    </AppCard>

    <AppErrorState
      v-if="errorMessage"
      title="Quotes could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadPage"
    />

    <AppLoadingState v-else-if="loading" label="Loading quotes workspace" />

    <AppEmptyState
      v-else-if="!filteredQuotes.length"
      title="No quotes found"
      description="Create a quote manually or convert a quote capture from the Capture Inbox."
      eyebrow="Quotes"
    >
      <template #actions>
        <AppButton variant="primary" @click="openCreateDialog">Create Quote</AppButton>
      </template>
    </AppEmptyState>

    <section v-else class="quote-grid" aria-label="Quote results">
      <AppCard v-for="quote in filteredQuotes" :key="quote.id" class="quote-card" as="article">
        <div class="quote-card__topline">
          <AppBadge variant="accent" size="sm">{{ quote.visibility }}</AppBadge>
          <AppBadge v-if="pageLabel(quote)" variant="primary" size="sm">{{ pageLabel(quote) }}</AppBadge>
        </div>

        <RouterLink :to="{ name: 'quote-detail', params: { id: quote.id } }" class="quote-card__link">
          <blockquote>
            <p>{{ quote.text }}</p>
          </blockquote>
        </RouterLink>

        <div class="quote-card__source">
          <strong>{{ quote.bookTitle }}</strong>
          <span>{{ quote.attribution || 'No attribution' }}</span>
        </div>

        <div v-if="quote.tags.length || quote.concepts.length" class="quote-card__chips" aria-label="Quote tags and concepts">
          <AppBadge v-for="tag in quote.tags" :key="`tag-${quote.id}-${tag}`" variant="neutral" size="sm">#{{ tag }}</AppBadge>
          <AppBadge v-for="concept in quote.concepts" :key="`concept-${quote.id}-${concept}`" variant="info" size="sm">[[{{ concept }}]]</AppBadge>
        </div>

        <div class="quote-card__actions">
          <AppButton variant="secondary" @click="openSource(quote)">Open Source</AppButton>
          <AppButton variant="ghost" @click="openEditDialog(quote)">Edit</AppButton>
          <AppButton variant="text" @click="archiveQuoteRecord(quote)">Archive</AppButton>
        </div>
      </AppCard>
    </section>

    <QuoteFormDialog
      v-model="quoteDialogOpen"
      :books="books"
      :quote="editingQuote"
      :saving="savingQuote"
      @submit="saveQuote"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink } from 'vue-router'
import { getBooks } from '../api/books'
import { archiveQuote, createQuote, getQuotes, updateQuote } from '../api/quotes'
import QuoteFormDialog from '../components/quotes/QuoteFormDialog.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useRightRailStore } from '../stores/rightRail'
import type { BookRecord, QuotePayload, QuoteRecord } from '../types'

const rightRail = useRightRailStore()
const { openSource: openSourceDrawer } = useOpenSource()

const books = ref<BookRecord[]>([])
const quotes = ref<QuoteRecord[]>([])
const loading = ref(false)
const savingQuote = ref(false)
const errorMessage = ref('')
const searchText = ref('')
const bookFilter = ref<number | null>(null)
const quoteDialogOpen = ref(false)
const editingQuote = ref<QuoteRecord | null>(null)

const filteredQuotes = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  if (!query) return quotes.value

  return quotes.value.filter((quote) => {
    const sourceText = quote.sourceReference?.sourceText ?? ''
    const values = [
      quote.text,
      quote.attribution ?? '',
      quote.bookTitle,
      sourceText,
      ...quote.tags,
      ...quote.concepts,
    ]
    return values.some((value) => value.toLowerCase().includes(query))
  })
})

onMounted(loadPage)

async function loadPage() {
  loading.value = true
  errorMessage.value = ''
  try {
    books.value = await getBooks()
    await loadQuotes()
  } catch {
    errorMessage.value = 'Check your connection and permissions, then try loading quotes again.'
  } finally {
    loading.value = false
  }
}

async function loadQuotes() {
  quotes.value = await getQuotes(bookFilter.value ? { bookId: bookFilter.value } : undefined)
}

function openCreateDialog() {
  editingQuote.value = null
  quoteDialogOpen.value = true
}

function openEditDialog(quote: QuoteRecord) {
  editingQuote.value = quote
  quoteDialogOpen.value = true
}

async function saveQuote(payload: QuotePayload) {
  savingQuote.value = true
  try {
    const saved = editingQuote.value
      ? await updateQuote(editingQuote.value.id, payload)
      : await createQuote(payload)
    upsertQuote(saved)
    quoteDialogOpen.value = false
    editingQuote.value = null
    ElMessage.success('Quote saved.')
  } catch {
    ElMessage.error('Quote could not be saved. Check required fields and permissions.')
  } finally {
    savingQuote.value = false
  }
}

async function archiveQuoteRecord(quote: QuoteRecord) {
  try {
    await ElMessageBox.confirm('Archive this quote? It will be removed from the active quotes workspace.', 'Archive Quote', {
      confirmButtonText: 'Archive',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await archiveQuote(quote.id)
    quotes.value = quotes.value.filter((item) => item.id !== quote.id)
    ElMessage.success('Quote archived.')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('Quote archive failed.')
    }
  }
}

function openSource(quote: QuoteRecord) {
  rightRail.setSourceFromQuote(quote, books.value.find((book) => book.id === quote.bookId))
  void openSourceDrawer({
    sourceType: 'QUOTE',
    sourceId: quote.id,
    bookId: quote.bookId,
    bookTitle: quote.bookTitle,
    pageStart: quote.pageStart,
    noteId: quote.noteId ?? undefined,
    rawCaptureId: quote.rawCaptureId ?? undefined,
    noteBlockId: quote.noteBlockId ?? undefined,
    sourceReference: quote.sourceReference,
    sourceReferenceId: quote.sourceReference?.id ?? null,
  })
}

function upsertQuote(quote: QuoteRecord) {
  quotes.value = [quote, ...quotes.value.filter((item) => item.id !== quote.id)]
}

function pageLabel(quote: QuoteRecord) {
  if (quote.pageStart && quote.pageEnd) return `p.${quote.pageStart}-${quote.pageEnd}`
  if (quote.pageStart) return `p.${quote.pageStart}`
  return ''
}
</script>

<style scoped>
.quotes-page {
  display: grid;
  gap: var(--space-5);
}

.quote-filter {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 0.34fr);
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.quote-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 340px), 1fr));
  gap: var(--space-4);
}

.quote-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.quote-card__topline,
.quote-card__chips,
.quote-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quote-card__link {
  color: inherit;
  text-decoration: none;
}

.quote-card blockquote {
  margin: 0;
  padding-left: var(--space-4);
  border-left: 4px solid var(--bookos-accent);
}

.quote-card blockquote p {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.12rem, 2vw, 1.42rem);
  line-height: 1.36;
}

.quote-card__source {
  display: grid;
  gap: var(--space-1);
}

.quote-card__source strong {
  color: var(--bookos-primary-hover);
}

.quote-card__source span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

@media (max-width: 760px) {
  .quote-filter {
    grid-template-columns: 1fr;
  }
}
</style>
