<template>
  <div class="page-shell quote-detail-page">
    <AppLoadingState v-if="loading" label="Loading quote" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Quote could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadQuote"
    />

    <template v-else-if="quote">
      <AppSectionHeader
        title="Quote Detail"
        eyebrow="Source-backed quote"
        :description="`Attached to ${quote.bookTitle}.`"
        :level="1"
      >
        <template #actions>
          <RouterLink to="/quotes" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Quotes</AppButton>
          </RouterLink>
          <AppButton variant="ghost" @click="openSource">Open Source</AppButton>
          <RouterLink :to="{ name: 'graph-book', params: { bookId: quote.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Graph Context</AppButton>
          </RouterLink>
          <RouterLink :to="forumThreadLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
          </RouterLink>
          <AppButton variant="primary" @click="quoteDialogOpen = true">Edit</AppButton>
          <AppButton variant="text" @click="archiveCurrentQuote">Archive</AppButton>
        </template>
      </AppSectionHeader>

      <section class="quote-detail-grid" aria-label="Quote detail and source">
        <AppCard class="quote-main" as="article">
          <div class="quote-main__badges">
            <AppBadge variant="accent">{{ quote.visibility }}</AppBadge>
            <AppBadge v-if="pageLabel(quote)" variant="primary">{{ pageLabel(quote) }}</AppBadge>
          </div>

          <blockquote>
            <p>{{ quote.text }}</p>
          </blockquote>

          <dl class="quote-meta">
            <div>
              <dt>Book</dt>
              <dd>{{ quote.bookTitle }}</dd>
            </div>
            <div>
              <dt>Attribution</dt>
              <dd>{{ quote.attribution ?? 'Not set' }}</dd>
            </div>
            <div>
              <dt>Created</dt>
              <dd>{{ formatDateTime(quote.createdAt) }}</dd>
            </div>
            <div>
              <dt>Updated</dt>
              <dd>{{ formatDateTime(quote.updatedAt) }}</dd>
            </div>
          </dl>

          <div v-if="quote.tags.length || quote.concepts.length" class="quote-main__chips" aria-label="Quote tags and concepts">
            <AppBadge v-for="tag in quote.tags" :key="`tag-${tag}`" variant="neutral" size="sm">#{{ tag }}</AppBadge>
            <AppBadge v-for="concept in quote.concepts" :key="`concept-${concept}`" variant="info" size="sm">[[{{ concept }}]]</AppBadge>
          </div>
        </AppCard>

        <AppCard class="quote-source" as="aside">
          <AppSectionHeader title="Source Reference" eyebrow="Traceability" :level="2" compact />

          <dl v-if="quote.sourceReference" class="quote-meta">
            <div>
              <dt>Location</dt>
              <dd>{{ quote.sourceReference.locationLabel ?? quote.sourceReference.sourceType }}</dd>
            </div>
            <div>
              <dt>Page</dt>
              <dd>{{ pageLabel(quote) || 'Unknown' }}</dd>
            </div>
            <div>
              <dt>Confidence</dt>
              <dd>{{ quote.sourceReference.sourceConfidence }}</dd>
            </div>
            <div>
              <dt>Source text</dt>
              <dd>{{ quote.sourceReference.sourceText ?? 'No source text stored.' }}</dd>
            </div>
          </dl>

          <AppEmptyState
            v-else
            title="No source reference"
            description="This quote was created manually or without a note/capture source reference."
            compact
          />
        </AppCard>
      </section>

      <BacklinksSection
        entity-type="QUOTE"
        :entity-id="quote.id"
        :source-references="quote.sourceReference ? [quote.sourceReference] : []"
        :book-title="quote.bookTitle"
      />

      <QuoteFormDialog
        v-model="quoteDialogOpen"
        :books="books"
        :quote="quote"
        :saving="savingQuote"
        @submit="saveQuote"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getBooks } from '../api/books'
import { archiveQuote, getQuote, updateQuote } from '../api/quotes'
import QuoteFormDialog from '../components/quotes/QuoteFormDialog.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useRightRailStore } from '../stores/rightRail'
import type { BookRecord, QuotePayload, QuoteRecord } from '../types'

const route = useRoute()
const router = useRouter()
const rightRail = useRightRailStore()
const { openSource: openSourceDrawer } = useOpenSource()

const books = ref<BookRecord[]>([])
const quote = ref<QuoteRecord | null>(null)
const loading = ref(false)
const savingQuote = ref(false)
const errorMessage = ref('')
const quoteDialogOpen = ref(false)

const forumThreadLink = computed(() => {
  if (!quote.value) return { name: 'forum-new' }
  return {
    name: 'forum-new',
    query: {
      relatedEntityType: 'QUOTE',
      relatedEntityId: String(quote.value.id),
      bookId: String(quote.value.bookId),
      sourceReferenceId: quote.value.sourceReference ? String(quote.value.sourceReference.id) : undefined,
      title: `Discuss quote from ${quote.value.bookTitle}`,
    },
  }
})

onMounted(loadQuote)

async function loadQuote() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [quoteResult, booksResult] = await Promise.all([getQuote(String(route.params.id)), getBooks()])
    quote.value = quoteResult
    books.value = booksResult
    rightRail.setSourceFromQuote(quoteResult, booksResult.find((book) => book.id === quoteResult.bookId))
  } catch {
    quote.value = null
    errorMessage.value = 'Check your connection or permissions and try opening the quote again.'
  } finally {
    loading.value = false
  }
}

async function saveQuote(payload: QuotePayload) {
  if (!quote.value) return
  savingQuote.value = true
  try {
    quote.value = await updateQuote(quote.value.id, payload)
    quoteDialogOpen.value = false
    rightRail.setSourceFromQuote(quote.value, books.value.find((book) => book.id === quote.value?.bookId))
    ElMessage.success('Quote updated.')
  } catch {
    ElMessage.error('Quote update failed.')
  } finally {
    savingQuote.value = false
  }
}

async function archiveCurrentQuote() {
  if (!quote.value) return
  try {
    await ElMessageBox.confirm('Archive this quote? It will be removed from active quote views.', 'Archive Quote', {
      confirmButtonText: 'Archive',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await archiveQuote(quote.value.id)
    ElMessage.success('Quote archived.')
    await router.push({ name: 'quotes' })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('Quote archive failed.')
    }
  }
}

function openSource() {
  if (!quote.value) return
  rightRail.setSourceFromQuote(quote.value, books.value.find((book) => book.id === quote.value?.bookId))
  void openSourceDrawer({
    sourceType: 'QUOTE',
    sourceId: quote.value.id,
    bookId: quote.value.bookId,
    bookTitle: quote.value.bookTitle,
    pageStart: quote.value.pageStart,
    noteId: quote.value.noteId ?? undefined,
    rawCaptureId: quote.value.rawCaptureId ?? undefined,
    noteBlockId: quote.value.noteBlockId ?? undefined,
    sourceReference: quote.value.sourceReference,
    sourceReferenceId: quote.value.sourceReference?.id ?? null,
  })
}

function pageLabel(value: QuoteRecord) {
  if (value.pageStart && value.pageEnd) return `p.${value.pageStart}-${value.pageEnd}`
  if (value.pageStart) return `p.${value.pageStart}`
  return ''
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
</script>

<style scoped>
.quote-detail-page {
  display: grid;
  gap: var(--space-5);
}

.quote-detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 0.42fr);
  gap: var(--space-4);
}

.quote-main,
.quote-source {
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.quote-main__badges,
.quote-main__chips {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quote-main blockquote {
  margin: 0;
  padding-left: var(--space-5);
  border-left: 5px solid var(--bookos-accent);
}

.quote-main blockquote p {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.45rem, 3vw, 2.4rem);
  line-height: 1.26;
}

.quote-meta {
  margin: 0;
  display: grid;
  gap: var(--space-3);
}

.quote-meta div {
  display: grid;
  gap: var(--space-1);
}

.quote-meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.quote-meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
  line-height: var(--type-body-line);
  overflow-wrap: anywhere;
}

@media (max-width: 980px) {
  .quote-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
