<template>
  <el-dialog
    v-model="open"
    class="global-search-dialog"
    width="min(760px, calc(100vw - 24px))"
    :show-close="false"
    align-center
    @opened="focusSearch"
  >
    <template #header>
      <div class="global-search__header">
        <div>
          <div class="eyebrow">Global Search</div>
          <h2>Search BookOS</h2>
        </div>
        <kbd>Esc</kbd>
      </div>
    </template>

    <div class="global-search">
      <label class="sr-only" for="command-search-input">Search books, notes, quotes, concepts, and forum threads</label>
      <div class="global-search__controls">
        <el-input
          id="command-search-input"
          ref="searchInput"
          v-model="query"
          clearable
          size="large"
          placeholder="Search books, notes, quotes, action items, concepts..."
          aria-label="Global search query"
          @keydown.enter.prevent="openFirstResult"
        />
        <el-select v-model="typeFilter" aria-label="Search result type filter" size="large">
          <el-option label="All types" value="" />
          <el-option v-for="option in typeOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>

      <AppLoadingState v-if="loading" label="Searching BookOS" compact />
      <AppErrorState v-else-if="errorMessage" title="Search failed" :description="errorMessage" retry-label="Retry" @retry="runSearch" />

      <div v-else-if="results.length" class="global-search__results" role="listbox" aria-label="Search results">
        <article
          v-for="result in results"
          :key="`${result.type}-${result.id}`"
          class="global-search__result"
          role="option"
          tabindex="0"
          @click="openResult(result)"
          @keydown.enter.prevent="openResult(result)"
          @keydown.space.prevent="openResult(result)"
        >
          <div class="global-search__result-main">
            <AppBadge :variant="badgeVariant(result.type)" size="sm">{{ typeLabel(result.type) }}</AppBadge>
            <div>
              <h3>{{ result.title }}</h3>
              <p>{{ result.excerpt ?? result.bookTitle ?? 'No excerpt available.' }}</p>
              <small v-if="result.bookTitle">Source book: {{ result.bookTitle }}</small>
            </div>
          </div>
          <div class="global-search__result-actions">
            <AppButton variant="text" @click.stop="openResult(result)">Open</AppButton>
            <AppButton
              v-if="result.sourceReferenceId || result.bookId"
              variant="text"
              @click.stop="openResultSource(result)"
            >
              Open source
            </AppButton>
          </div>
        </article>
      </div>

      <AppEmptyState
        v-else
        title="No results yet"
        :description="query.trim().length < 2 ? 'Type at least two characters to search your BookOS workspace.' : 'No matching source-backed records were found.'"
        compact
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage, type InputInstance } from 'element-plus'
import { useRouter } from 'vue-router'
import { searchBookOS } from '../../api/search'
import { useOpenSource } from '../../composables/useOpenSource'
import type { SearchResultRecord, SearchResultType } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import AppLoadingState from '../ui/AppLoadingState.vue'

const router = useRouter()
const { openSource } = useOpenSource()
const open = ref(false)
const query = ref('')
const typeFilter = ref<SearchResultType | ''>('')
const results = ref<SearchResultRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')
const searchInput = ref<InputInstance | null>(null)
let searchTimer: number | undefined

const typeOptions: Array<{ label: string; value: SearchResultType }> = [
  { label: 'Books', value: 'BOOK' },
  { label: 'Notes', value: 'NOTE' },
  { label: 'Captures', value: 'CAPTURE' },
  { label: 'Quotes', value: 'QUOTE' },
  { label: 'Action Items', value: 'ACTION_ITEM' },
  { label: 'Concepts', value: 'CONCEPT' },
  { label: 'Knowledge Objects', value: 'KNOWLEDGE_OBJECT' },
  { label: 'Forum Threads', value: 'FORUM_THREAD' },
]

const trimmedQuery = computed(() => query.value.trim())

onMounted(() => {
  window.addEventListener('keydown', handleShortcut)
  window.addEventListener('bookos:open-search', handleOpenSearchEvent)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleShortcut)
  window.removeEventListener('bookos:open-search', handleOpenSearchEvent)
  if (searchTimer) window.clearTimeout(searchTimer)
})

watch([trimmedQuery, typeFilter], () => {
  if (searchTimer) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(runSearch, 220)
})

function handleShortcut(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    openSearch()
  }
}

function openSearch() {
  open.value = true
  void focusSearch()
}

function handleOpenSearchEvent() {
  openSearch()
}

async function focusSearch() {
  await nextTick()
  searchInput.value?.focus()
}

async function runSearch() {
  if (trimmedQuery.value.length < 2) {
    results.value = []
    errorMessage.value = ''
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    results.value = await searchBookOS({ q: trimmedQuery.value, type: typeFilter.value })
  } catch (error) {
    errorMessage.value = searchErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function openFirstResult() {
  const first = results.value[0]
  if (first) openResult(first)
}

function openResult(result: SearchResultRecord) {
  open.value = false
  const target = routeTarget(result)
  if (!target) {
    ElMessage.info('This result does not have a dedicated route yet.')
    return
  }
  router.push(target)
}

function openResultSource(result: SearchResultRecord) {
  open.value = false
  void openSource({
    sourceType: result.sourceReferenceId ? 'SOURCE_REFERENCE' : result.type,
    sourceId: result.sourceReferenceId ?? result.id,
    bookId: result.bookId,
    bookTitle: result.bookTitle,
    sourceReferenceId: result.sourceReferenceId,
    sourceText: result.excerpt,
  })
}

function routeTarget(result: SearchResultRecord) {
  if (result.type === 'BOOK') return { name: 'book-detail', params: { id: result.id } }
  if (result.type === 'NOTE') return { name: 'note-detail', params: { id: result.id } }
  if (result.type === 'CAPTURE') return { name: 'capture-inbox', query: { captureId: String(result.id) } }
  if (result.type === 'QUOTE') return { name: 'quote-detail', params: { id: result.id } }
  if (result.type === 'ACTION_ITEM') return { name: 'action-item-detail', params: { id: result.id } }
  if (result.type === 'CONCEPT') return { name: 'concept-detail', params: { id: result.id } }
  if (result.type === 'KNOWLEDGE_OBJECT') return { name: 'knowledge-detail', params: { id: result.id } }
  if (result.type === 'FORUM_THREAD') return { name: 'forum-thread', params: { id: result.id } }
  return null
}

function typeLabel(type: SearchResultType) {
  return type.replaceAll('_', ' ').toLowerCase().replace(/^\w|\s\w/g, (match) => match.toUpperCase())
}

function badgeVariant(type: SearchResultType) {
  if (type === 'QUOTE') return 'accent'
  if (type === 'ACTION_ITEM') return 'warning'
  if (type === 'FORUM_THREAD') return 'info'
  if (type === 'CONCEPT' || type === 'KNOWLEDGE_OBJECT') return 'primary'
  return 'neutral'
}

function searchErrorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { status?: number; data?: { message?: string } } }).response
    if (response?.status === 403) return 'Permission denied. Search can only return records you are allowed to access.'
    return response?.data?.message ?? 'Search failed. Check that the backend is available.'
  }
  return 'Search failed. Check that the backend is available.'
}
</script>

<style scoped>
.global-search__header,
.global-search__controls,
.global-search__result,
.global-search__result-main,
.global-search__result-actions {
  display: flex;
  gap: var(--space-3);
}

.global-search__header {
  align-items: center;
  justify-content: space-between;
}

.global-search__header h2 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
}

.global-search__header kbd {
  padding: 0.2rem 0.5rem;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-sm);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
}

.global-search {
  display: grid;
  gap: var(--space-4);
}

.global-search__controls {
  align-items: center;
}

.global-search__controls :deep(.el-input) {
  flex: 1 1 auto;
}

.global-search__controls :deep(.el-select) {
  width: 210px;
}

.global-search__results {
  max-height: min(58vh, 560px);
  overflow: auto;
  display: grid;
  gap: var(--space-2);
  padding-right: var(--space-1);
}

.global-search__result {
  min-height: 72px;
  padding: var(--space-3);
  justify-content: space-between;
  align-items: center;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  cursor: pointer;
}

.global-search__result:hover,
.global-search__result:focus-visible {
  border-color: color-mix(in srgb, var(--bookos-primary) 34%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-primary-soft) 24%, var(--bookos-surface));
}

.global-search__result-main {
  min-width: 0;
  align-items: start;
}

.global-search__result-main h3,
.global-search__result-main p {
  margin: 0;
}

.global-search__result-main h3 {
  color: var(--bookos-text-primary);
  font-size: 1rem;
}

.global-search__result-main p,
.global-search__result-main small {
  color: var(--bookos-text-secondary);
  line-height: 1.4;
}

.global-search__result-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .global-search__controls,
  .global-search__result,
  .global-search__result-main {
    display: grid;
  }

  .global-search__controls :deep(.el-select) {
    width: 100%;
  }

  .global-search__result-actions {
    justify-content: flex-start;
  }
}
</style>
