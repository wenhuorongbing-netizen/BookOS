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
      <label class="sr-only" for="command-search-input">Search books, notes, quotes, concepts, projects, and forum threads</label>
      <div class="global-search__controls">
        <el-input
          id="command-search-input"
          ref="searchInput"
          v-model="query"
          clearable
          size="large"
          placeholder="Search books, notes, quotes, actions, concepts, projects..."
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

      <div v-else-if="hasResults" class="global-search__results" role="listbox" aria-label="Search results">
        <section v-if="shortcutResults.length" class="global-search__section" aria-label="Routes and tools">
          <div class="global-search__section-title">Routes and tools</div>
          <article
            v-for="shortcut in shortcutResults"
            :key="shortcut.label"
            class="global-search__result global-search__result--shortcut"
            role="option"
            tabindex="0"
            @click="openShortcut(shortcut)"
            @keydown.enter.prevent="openShortcut(shortcut)"
            @keydown.space.prevent="openShortcut(shortcut)"
          >
            <div class="global-search__result-main">
              <AppBadge variant="info" size="sm">{{ shortcut.group }}</AppBadge>
              <div>
                <h3>{{ shortcut.label }}</h3>
                <p>{{ shortcut.description }}</p>
              </div>
            </div>
            <div class="global-search__result-actions">
              <AppButton variant="text" @click.stop="openShortcut(shortcut)">Open route</AppButton>
            </div>
          </article>
        </section>

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
              <p>{{ result.excerpt ?? result.bookTitle ?? result.projectTitle ?? 'No excerpt available.' }}</p>
              <small v-if="result.bookTitle">Source book: {{ result.bookTitle }}</small>
              <small v-if="result.projectTitle">Project: {{ result.projectTitle }}</small>
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
            <AppButton v-if="canOpenGraph(result)" variant="text" @click.stop="openResultGraph(result)">
              Open Knowledge Graph
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
import { useRouter, type RouteLocationRaw } from 'vue-router'
import { searchBookOS } from '../../api/search'
import { recordUseCaseEvent } from '../../api/useCaseProgress'
import { useOpenSource } from '../../composables/useOpenSource'
import { useAuthStore } from '../../stores/auth'
import type { SearchResultRecord, SearchResultType } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import AppLoadingState from '../ui/AppLoadingState.vue'

const router = useRouter()
const auth = useAuthStore()
const { openSource } = useOpenSource()
const open = ref(false)
const query = ref('')
const typeFilter = ref<SearchResultType | ''>('')
const results = ref<SearchResultRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')
const searchInput = ref<InputInstance | null>(null)
const searchEventRecorded = ref(false)
let searchTimer: number | undefined

interface RouteShortcut {
  label: string
  description: string
  group: 'Primary' | 'Secondary' | 'Advanced' | 'Admin'
  to: RouteLocationRaw
  keywords: string[]
  adminOnly?: boolean
}

const typeOptions: Array<{ label: string; value: SearchResultType }> = [
  { label: 'Books', value: 'BOOK' },
  { label: 'Notes', value: 'NOTE' },
  { label: 'Captures', value: 'CAPTURE' },
  { label: 'Quotes', value: 'QUOTE' },
  { label: 'Actions', value: 'ACTION_ITEM' },
  { label: 'Concepts', value: 'CONCEPT' },
  { label: 'Design Knowledge', value: 'KNOWLEDGE_OBJECT' },
  { label: 'Forum Threads', value: 'FORUM_THREAD' },
  { label: 'Projects', value: 'GAME_PROJECT' },
  { label: 'Project Problems', value: 'PROJECT_PROBLEM' },
  { label: 'Project Applications', value: 'PROJECT_APPLICATION' },
  { label: 'Design Decisions', value: 'DESIGN_DECISION' },
  { label: 'Playtest Findings', value: 'PLAYTEST_FINDING' },
  { label: 'Project Lens Reviews', value: 'PROJECT_LENS_REVIEW' },
]

const trimmedQuery = computed(() => query.value.trim())

const routeShortcuts: RouteShortcut[] = [
  {
    label: 'Dashboard',
    description: 'Return to the task-first command center.',
    group: 'Primary',
    to: { name: 'dashboard' },
    keywords: ['home', 'today', 'continue reading', 'capture'],
  },
  {
    label: 'Library',
    description: 'Manage books, reading status, progress, and ratings.',
    group: 'Primary',
    to: { name: 'my-library' },
    keywords: ['books', 'reading', 'add book'],
  },
  {
    label: 'Process Captures',
    description: 'Review quick captures and convert them into notes, quotes, actions, or concepts.',
    group: 'Primary',
    to: { name: 'capture-inbox' },
    keywords: ['capture', 'inbox', 'parser', 'quick capture'],
  },
  {
    label: 'Notes',
    description: 'Open source-backed book notes and note blocks.',
    group: 'Primary',
    to: { name: 'notes' },
    keywords: ['markdown', 'blocks', 'writing'],
  },
  {
    label: 'Projects',
    description: 'Apply reading knowledge to game design projects.',
    group: 'Primary',
    to: { name: 'projects' },
    keywords: ['game design', 'project cockpit', 'application'],
  },
  {
    label: 'Review',
    description: 'Start source-backed review sessions and update learning progress.',
    group: 'Primary',
    to: { name: 'review' },
    keywords: ['learning', 'mastery', 'review session'],
  },
  {
    label: 'Quotes',
    description: 'Browse and manage captured quotes.',
    group: 'Secondary',
    to: { name: 'quotes' },
    keywords: ['quote', 'source'],
  },
  {
    label: 'Actions',
    description: 'Manage source-backed actions.',
    group: 'Secondary',
    to: { name: 'action-items' },
    keywords: ['actions', 'action items', 'tasks', 'todo'],
  },
  {
    label: 'Concepts',
    description: 'Review and connect extracted concepts.',
    group: 'Secondary',
    to: { name: 'concepts' },
    keywords: ['concept', 'ontology'],
  },
  {
    label: 'Design Knowledge',
    description: 'Open design knowledge, lenses, principles, and exercises.',
    group: 'Secondary',
    to: { name: 'knowledge' },
    keywords: ['design knowledge', 'knowledge objects', 'lenses', 'principles'],
  },
  {
    label: 'Daily',
    description: 'Open daily quote, prompt, reflection, and resurfacing tools.',
    group: 'Secondary',
    to: { name: 'daily' },
    keywords: ['daily prompt', 'daily sentence', 'reflection'],
  },
  {
    label: 'Forum',
    description: 'Discuss books, concepts, projects, and source-backed ideas.',
    group: 'Secondary',
    to: { name: 'forum' },
    keywords: ['community', 'discussion', 'threads'],
  },
  {
    label: 'Use Cases',
    description: 'Learn BookOS through hands-on workflows.',
    group: 'Secondary',
    to: { name: 'use-cases' },
    keywords: ['workflow', 'tutorial', 'scenario'],
  },
  {
    label: 'Demo Workspace',
    description: 'Practice with safe, clearly labeled sample records that can be reset or deleted.',
    group: 'Secondary',
    to: { name: 'demo-workspace' },
    keywords: ['demo', 'sample', 'practice', 'hands-on'],
  },
  {
    label: 'Knowledge Graph',
    description: 'Explore source-backed relationships.',
    group: 'Advanced',
    to: { name: 'graph' },
    keywords: ['knowledge graph', 'graph', 'relationships', 'entity links', 'backlinks', 'related links'],
  },
  {
    label: 'Analytics',
    description: 'Inspect real reading, review, and knowledge activity.',
    group: 'Advanced',
    to: { name: 'analytics' },
    keywords: ['stats', 'metrics', 'learning loop'],
  },
  {
    label: 'Import / Export',
    description: 'Move user-owned BookOS data in and out safely.',
    group: 'Advanced',
    to: { name: 'import-export' },
    keywords: ['backup', 'restore', 'json', 'markdown', 'csv'],
  },
  {
    label: 'Draft Assistant',
    description: 'Open draft-only assistant suggestions; AI remains optional and draft-only.',
    group: 'Advanced',
    to: { name: 'dashboard', query: { focus: 'ai' } },
    keywords: ['mock ai', 'suggestions', 'drafts'],
  },
  {
    label: 'Ontology Import',
    description: 'Admin-only ontology seed and import tools.',
    group: 'Admin',
    to: { name: 'admin-ontology' },
    keywords: ['admin', 'ontology', 'seed import'],
    adminOnly: true,
  },
]

const shortcutResults = computed(() => {
  if (trimmedQuery.value.length < 2) return []
  const queryText = trimmedQuery.value.toLowerCase()
  return routeShortcuts
    .filter((shortcut) => !shortcut.adminOnly || auth.user?.role === 'ADMIN')
    .filter((shortcut) => {
      const haystack = [
        shortcut.label,
        shortcut.description,
        shortcut.group,
        ...shortcut.keywords,
      ].join(' ').toLowerCase()
      return haystack.includes(queryText)
    })
    .slice(0, 6)
})

const hasResults = computed(() => results.value.length > 0 || shortcutResults.value.length > 0)

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
  searchEventRecorded.value = false
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
    recordSearchUsed()
  } catch (error) {
    errorMessage.value = searchErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function recordSearchUsed() {
  if (searchEventRecorded.value) return
  searchEventRecorded.value = true
  void recordUseCaseEvent({
    eventType: 'SEARCH_USED',
    contextType: typeFilter.value || 'GLOBAL_SEARCH',
    contextId: trimmedQuery.value.slice(0, 120),
  }).catch(() => undefined)
}

function openFirstResult() {
  const firstShortcut = shortcutResults.value[0]
  if (firstShortcut) {
    openShortcut(firstShortcut)
    return
  }

  const first = results.value[0]
  if (first) openResult(first)
}

function openShortcut(shortcut: RouteShortcut) {
  open.value = false
  void router.push(shortcut.to)
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

function openResultGraph(result: SearchResultRecord) {
  open.value = false
  if (result.type === 'CONCEPT') {
    void router.push({ name: 'graph-concept', params: { conceptId: result.id } })
    return
  }
  if (result.type === 'BOOK') {
    void router.push({ name: 'graph-book', params: { bookId: result.id } })
    return
  }
  if (result.bookId) {
    void router.push({ name: 'graph-book', params: { bookId: result.bookId } })
    return
  }
  if (result.projectId) {
    void router.push({ name: 'graph-project', params: { projectId: result.projectId } })
    return
  }
  void router.push({ name: 'graph', query: { entityType: result.type, entityId: String(result.id) } })
}

function canOpenGraph(result: SearchResultRecord) {
  return result.type === 'BOOK' || result.type === 'CONCEPT' || Boolean(result.bookId) || Boolean(result.projectId)
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
  if (result.type === 'GAME_PROJECT' || result.type === 'PROJECT') return { name: 'project-detail', params: { id: result.id } }
  if (result.type === 'PROJECT_PROBLEM') return { name: 'project-problems', params: { id: result.projectId } }
  if (result.type === 'PROJECT_APPLICATION') return { name: 'project-applications', params: { id: result.projectId } }
  if (result.type === 'DESIGN_DECISION') return { name: 'project-decisions', params: { id: result.projectId } }
  if (result.type === 'PLAYTEST_FINDING') return { name: 'project-playtests', params: { id: result.projectId } }
  if (result.type === 'PROJECT_LENS_REVIEW') return { name: 'project-lens-reviews', params: { id: result.projectId } }
  return null
}

function typeLabel(type: SearchResultType) {
  if (type === 'ACTION_ITEM') return 'Action'
  if (type === 'KNOWLEDGE_OBJECT') return 'Design Knowledge'
  if (type === 'GAME_PROJECT' || type === 'PROJECT') return 'Project'
  return type.replaceAll('_', ' ').toLowerCase().replace(/^\w|\s\w/g, (match) => match.toUpperCase())
}

function badgeVariant(type: SearchResultType) {
  if (type === 'QUOTE') return 'accent'
  if (type === 'ACTION_ITEM') return 'warning'
  if (type === 'FORUM_THREAD') return 'info'
  if (type === 'CONCEPT' || type === 'KNOWLEDGE_OBJECT') return 'primary'
  if (type === 'GAME_PROJECT' || type === 'PROJECT' || type.startsWith('PROJECT_') || type === 'DESIGN_DECISION' || type === 'PLAYTEST_FINDING') return 'success'
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

.global-search__section {
  display: grid;
  gap: var(--space-2);
}

.global-search__section-title {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
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

.global-search__result--shortcut {
  background: color-mix(in srgb, var(--bookos-info-soft, var(--bookos-primary-soft)) 22%, var(--bookos-surface));
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
