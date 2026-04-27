<template>
  <div class="page-shell action-items-page">
    <AppSectionHeader
      title="Action Items"
      eyebrow="Reading to execution"
      description="Manage source-backed tasks converted from captures, notes, and manual reading decisions."
      :level="1"
    >
      <template #actions>
        <AppButton variant="primary" @click="openCreateDialog">Create Action Item</AppButton>
      </template>
    </AppSectionHeader>

    <AppCard class="action-filter" as="section">
      <label class="field action-filter__search">
        <span>Search action items, source text, or book</span>
        <el-input v-model="searchText" clearable placeholder="Search tasks, source notes, book title..." />
      </label>

      <label class="field">
        <span>Book</span>
        <el-select v-model="bookFilter" clearable filterable placeholder="All books" @change="loadItems">
          <el-option v-for="book in books" :key="book.id" :label="book.title" :value="book.id" />
        </el-select>
      </label>

      <label class="field">
        <span>Status</span>
        <el-select v-model="statusFilter" @change="loadItems">
          <el-option label="All" value="ALL" />
          <el-option label="Open" value="OPEN" />
          <el-option label="Completed" value="COMPLETED" />
        </el-select>
      </label>

      <label class="field">
        <span>Priority</span>
        <el-select v-model="priorityFilter">
          <el-option label="All priorities" value="ALL" />
          <el-option label="High" value="HIGH" />
          <el-option label="Medium" value="MEDIUM" />
          <el-option label="Low" value="LOW" />
        </el-select>
      </label>
    </AppCard>

    <AppErrorState
      v-if="errorMessage"
      title="Action items could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadPage"
    />

    <AppLoadingState v-else-if="loading" label="Loading action items workspace" />

    <AppEmptyState
      v-else-if="!filteredItems.length"
      title="No action items found"
      description="Create an action item manually or convert an action capture from the Capture Inbox."
      eyebrow="Action Items"
    >
      <template #actions>
        <AppButton variant="primary" @click="openCreateDialog">Create Action Item</AppButton>
      </template>
    </AppEmptyState>

    <section v-else class="action-list" aria-label="Action item results">
      <AppCard
        v-for="item in filteredItems"
        :key="item.id"
        class="action-card"
        :class="{ 'action-card--complete': item.completed }"
        as="article"
      >
        <div class="action-card__topline">
          <AppBadge :variant="item.completed ? 'success' : 'primary'" size="sm">
            {{ item.completed ? 'Completed' : 'Open' }}
          </AppBadge>
          <AppBadge :variant="priorityVariant(item.priority)" size="sm">{{ priorityLabel(item.priority) }}</AppBadge>
          <AppBadge v-if="pageLabel(item)" variant="accent" size="sm">{{ pageLabel(item) }}</AppBadge>
        </div>

        <RouterLink :to="{ name: 'action-item-detail', params: { id: item.id } }" class="action-card__link">
          <h2>{{ item.title }}</h2>
        </RouterLink>

        <p v-if="item.description" class="action-card__description">{{ item.description }}</p>

        <dl class="action-meta" aria-label="Action item metadata">
          <div>
            <dt>Book</dt>
            <dd>{{ item.bookTitle }}</dd>
          </div>
          <div>
            <dt>Source</dt>
            <dd>{{ sourceLabel(item) }}</dd>
          </div>
          <div>
            <dt>Created</dt>
            <dd>{{ formatDate(item.createdAt) }}</dd>
          </div>
        </dl>

        <div class="action-card__actions">
          <AppButton
            variant="secondary"
            :loading="togglingId === item.id"
            @click="toggleCompletion(item)"
          >
            {{ item.completed ? 'Reopen' : 'Complete' }}
          </AppButton>
          <AppButton variant="ghost" @click="openSource(item)">Open Source</AppButton>
          <AppButton variant="ghost" @click="openEditDialog(item)">Edit</AppButton>
          <AppButton variant="text" @click="archiveActionItemRecord(item)">Archive</AppButton>
        </div>
      </AppCard>
    </section>

    <ActionItemFormDialog
      v-model="actionDialogOpen"
      :books="books"
      :action-item="editingItem"
      :saving="savingItem"
      @submit="saveActionItem"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute } from 'vue-router'
import { getBooks } from '../api/books'
import {
  archiveActionItem,
  completeActionItem,
  createActionItem,
  getActionItems,
  reopenActionItem,
  updateActionItem,
} from '../api/actionItems'
import ActionItemFormDialog from '../components/action-items/ActionItemFormDialog.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useRightRailStore } from '../stores/rightRail'
import type { ActionItemPayload, ActionItemRecord, ActionPriority, BookRecord } from '../types'

type StatusFilter = 'ALL' | 'OPEN' | 'COMPLETED'
type PriorityFilter = 'ALL' | ActionPriority

const route = useRoute()
const rightRail = useRightRailStore()
const { openSource: openSourceDrawer } = useOpenSource()

const books = ref<BookRecord[]>([])
const items = ref<ActionItemRecord[]>([])
const loading = ref(false)
const savingItem = ref(false)
const errorMessage = ref('')
const searchText = ref('')
const bookFilter = ref<number | null>(null)
const statusFilter = ref<StatusFilter>('ALL')
const priorityFilter = ref<PriorityFilter>('ALL')
const actionDialogOpen = ref(false)
const editingItem = ref<ActionItemRecord | null>(null)
const togglingId = ref<number | null>(null)

const filteredItems = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  return items.value.filter((item) => {
    if (priorityFilter.value !== 'ALL' && item.priority !== priorityFilter.value) {
      return false
    }

    if (!query) return true

    const values = [
      item.title,
      item.description ?? '',
      item.bookTitle,
      item.priority,
      item.sourceReference?.locationLabel ?? '',
      item.sourceReference?.sourceText ?? '',
    ]
    return values.some((value) => value.toLowerCase().includes(query))
  })
})

onMounted(async () => {
  hydrateBookFilterFromRoute()
  await loadPage()
})

watch(
  () => route.query.bookId,
  async () => {
    hydrateBookFilterFromRoute()
    await loadItems()
  },
)

async function loadPage() {
  loading.value = true
  errorMessage.value = ''
  try {
    books.value = await getBooks()
    await loadItems()
  } catch {
    errorMessage.value = 'Check your connection and permissions, then try loading action items again.'
  } finally {
    loading.value = false
  }
}

async function loadItems() {
  const params: { bookId?: number; completed?: boolean } = {}
  if (bookFilter.value) params.bookId = bookFilter.value
  if (statusFilter.value === 'OPEN') params.completed = false
  if (statusFilter.value === 'COMPLETED') params.completed = true
  errorMessage.value = ''
  try {
    items.value = await getActionItems(params)
  } catch {
    items.value = []
    errorMessage.value = 'Action items could not be loaded. Check backend availability and permissions.'
  }
}

function hydrateBookFilterFromRoute() {
  const value = route.query.bookId
  if (typeof value !== 'string') return
  const numeric = Number(value)
  bookFilter.value = Number.isFinite(numeric) ? numeric : null
}

function openCreateDialog() {
  editingItem.value = null
  actionDialogOpen.value = true
}

function openEditDialog(item: ActionItemRecord) {
  editingItem.value = item
  actionDialogOpen.value = true
}

async function saveActionItem(payload: ActionItemPayload) {
  savingItem.value = true
  try {
    const saved = editingItem.value
      ? await updateActionItem(editingItem.value.id, payload)
      : await createActionItem(payload)
    actionDialogOpen.value = false
    editingItem.value = null
    await loadItems()
    rightRail.setSourceFromActionItem(saved, books.value.find((book) => book.id === saved.bookId))
    ElMessage.success('Action item saved.')
  } catch {
    ElMessage.error('Action item could not be saved. Check required fields and permissions.')
  } finally {
    savingItem.value = false
  }
}

async function toggleCompletion(item: ActionItemRecord) {
  togglingId.value = item.id
  try {
    const updated = item.completed ? await reopenActionItem(item.id) : await completeActionItem(item.id)
    replaceItem(updated)
    rightRail.setSourceFromActionItem(updated, books.value.find((book) => book.id === updated.bookId))
    ElMessage.success(updated.completed ? 'Action item completed.' : 'Action item reopened.')
  } catch {
    ElMessage.error('Action item status update failed.')
  } finally {
    togglingId.value = null
  }
}

async function archiveActionItemRecord(item: ActionItemRecord) {
  try {
    await ElMessageBox.confirm('Archive this action item? It will be removed from active action item views.', 'Archive Action Item', {
      confirmButtonText: 'Archive',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await archiveActionItem(item.id)
    items.value = items.value.filter((entry) => entry.id !== item.id)
    ElMessage.success('Action item archived.')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('Action item archive failed.')
    }
  }
}

function openSource(item: ActionItemRecord) {
  rightRail.setSourceFromActionItem(item, books.value.find((book) => book.id === item.bookId))
  void openSourceDrawer({
    sourceType: 'ACTION_ITEM',
    sourceId: item.id,
    bookId: item.bookId,
    bookTitle: item.bookTitle,
    pageStart: item.pageStart,
    noteId: item.noteId ?? undefined,
    rawCaptureId: item.rawCaptureId ?? undefined,
    noteBlockId: item.noteBlockId ?? undefined,
    sourceReference: item.sourceReference,
    sourceReferenceId: item.sourceReference?.id ?? null,
  })
}

function replaceItem(item: ActionItemRecord) {
  items.value = items.value.map((entry) => (entry.id === item.id ? item : entry))
}

function pageLabel(item: ActionItemRecord) {
  if (item.pageStart && item.pageEnd) return `p.${item.pageStart}-${item.pageEnd}`
  if (item.pageStart) return `p.${item.pageStart}`
  return ''
}

function sourceLabel(item: ActionItemRecord) {
  if (item.sourceReference?.locationLabel) return item.sourceReference.locationLabel
  if (item.sourceReference?.sourceType) return item.sourceReference.sourceType
  return pageLabel(item) || 'Manual action item'
}

function priorityLabel(priority: ActionPriority) {
  if (priority === 'HIGH') return 'High'
  if (priority === 'LOW') return 'Low'
  return 'Medium'
}

function priorityVariant(priority: ActionPriority) {
  if (priority === 'HIGH') return 'danger'
  if (priority === 'LOW') return 'info'
  return 'warning'
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
</script>

<style scoped>
.action-items-page {
  display: grid;
  gap: var(--space-5);
}

.action-filter {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: minmax(280px, 1fr) repeat(3, minmax(160px, 0.24fr));
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.action-list {
  display: grid;
  gap: var(--space-4);
}

.action-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.action-card__topline,
.action-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.action-card__link {
  color: inherit;
  text-decoration: none;
}

.action-card h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.25rem, 2vw, 1.75rem);
  line-height: 1.2;
}

.action-card__description {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.action-card--complete h2,
.action-card--complete .action-card__description {
  color: var(--bookos-text-tertiary);
}

.action-card--complete h2 {
  text-decoration: line-through;
}

.action-meta {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.action-meta div {
  display: grid;
  gap: var(--space-1);
}

.action-meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.action-meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
  overflow-wrap: anywhere;
}

@media (max-width: 1080px) {
  .action-filter {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .action-filter__search {
    grid-column: span 2;
  }
}

@media (max-width: 720px) {
  .action-filter,
  .action-meta {
    grid-template-columns: 1fr;
  }

  .action-filter__search {
    grid-column: auto;
  }
}
</style>
