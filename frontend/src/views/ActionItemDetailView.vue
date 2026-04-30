<template>
  <div class="page-shell action-detail-page">
    <AppLoadingState v-if="loading" label="Loading action" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Action could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadActionItem"
    />

    <template v-else-if="item">
      <AppSectionHeader
        title="Action Detail"
        eyebrow="Source-backed task"
        :description="`Attached to ${item.bookTitle}.`"
        :level="1"
      >
        <template #actions>
          <RouterLink to="/action-items" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Actions</AppButton>
          </RouterLink>
          <AppButton variant="ghost" @click="openSource">Open Source</AppButton>
          <RouterLink :to="{ name: 'graph-book', params: { bookId: item.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Knowledge Graph</AppButton>
          </RouterLink>
          <RouterLink :to="forumThreadLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
          </RouterLink>
          <AppButton variant="secondary" :loading="toggling" @click="toggleCompletion">
            {{ item.completed ? 'Reopen' : 'Complete' }}
          </AppButton>
          <AppButton variant="primary" @click="actionDialogOpen = true">Edit</AppButton>
          <AppButton variant="text" @click="archiveCurrentItem">Archive</AppButton>
        </template>
      </AppSectionHeader>

      <DetailNextStepCard
        :title="item.completed ? 'Keep this task connected to its source' : 'Complete one source-backed action'"
        :description="item.completed ? 'This action is done. Reopen it if the source-backed task needs another pass, or reopen the source to verify the reading context.' : 'Close the reading-to-action loop by completing this task, then reopen the source if you need the original page or capture context.'"
        :primary-label="item.completed ? 'Reopen Action' : 'Complete Action'"
        secondary-label="Open Source"
        :primary-loading="toggling"
        :loop="['Source', 'Action', 'Done work', 'Review source']"
        @primary="toggleCompletion"
        @secondary="openSource"
      />

      <section class="action-detail-grid" aria-label="Action detail and source">
        <AppCard class="action-main" as="article">
          <div class="action-main__badges">
            <AppBadge :variant="item.completed ? 'success' : 'primary'">{{ item.completed ? 'Completed' : 'Open' }}</AppBadge>
            <AppBadge :variant="priorityVariant(item.priority)">{{ priorityLabel(item.priority) }}</AppBadge>
            <AppBadge v-if="pageLabel(item)" variant="accent">{{ pageLabel(item) }}</AppBadge>
            <AppBadge variant="neutral">{{ item.visibility }}</AppBadge>
          </div>

          <h2>{{ item.title }}</h2>
          <p v-if="item.description" class="action-main__description">{{ item.description }}</p>
          <AppEmptyState
            v-else
            title="No description"
            description="This action only has a title. Add context when you edit it."
            compact
          />

          <dl class="action-meta">
            <div>
              <dt>Book</dt>
              <dd>{{ item.bookTitle }}</dd>
            </div>
            <div>
              <dt>Created</dt>
              <dd>{{ formatDateTime(item.createdAt) }}</dd>
            </div>
            <div>
              <dt>Updated</dt>
              <dd>{{ formatDateTime(item.updatedAt) }}</dd>
            </div>
            <div>
              <dt>Completed</dt>
              <dd>{{ item.completedAt ? formatDateTime(item.completedAt) : 'Not completed' }}</dd>
            </div>
          </dl>
        </AppCard>

        <AppCard class="action-source" as="aside">
          <AppSectionHeader title="Source Link" eyebrow="Traceability" :level="2" compact />

          <dl v-if="item.sourceReference" class="action-meta">
            <div>
              <dt>Location</dt>
              <dd>{{ item.sourceReference.locationLabel ?? item.sourceReference.sourceType }}</dd>
            </div>
            <div>
              <dt>Page</dt>
              <dd>{{ pageLabel(item) || 'Unknown' }}</dd>
            </div>
            <div>
              <dt>Confidence</dt>
              <dd>{{ item.sourceReference.sourceConfidence }}</dd>
            </div>
            <div>
              <dt>Source text</dt>
              <dd>{{ item.sourceReference.sourceText ?? 'No source text stored.' }}</dd>
            </div>
          </dl>

          <AppEmptyState
            v-else
            title="No source link"
            description="This action was created manually or without a note/capture source link."
            compact
          />
        </AppCard>
      </section>

      <BacklinksSection
        entity-type="ACTION_ITEM"
        :entity-id="item.id"
        :source-references="item.sourceReference ? [item.sourceReference] : []"
        :book-title="item.bookTitle"
      />

      <ActionItemFormDialog
        v-model="actionDialogOpen"
        :books="books"
        :action-item="item"
        :saving="savingItem"
        @submit="saveActionItem"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getBooks } from '../api/books'
import { archiveActionItem, completeActionItem, getActionItem, reopenActionItem, updateActionItem } from '../api/actionItems'
import ActionItemFormDialog from '../components/action-items/ActionItemFormDialog.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useRightRailStore } from '../stores/rightRail'
import type { ActionItemPayload, ActionItemRecord, ActionPriority, BookRecord } from '../types'

const route = useRoute()
const router = useRouter()
const rightRail = useRightRailStore()
const { openSource: openSourceDrawer } = useOpenSource()

const books = ref<BookRecord[]>([])
const item = ref<ActionItemRecord | null>(null)
const loading = ref(false)
const savingItem = ref(false)
const toggling = ref(false)
const errorMessage = ref('')
const actionDialogOpen = ref(false)

const forumThreadLink = computed(() => {
  if (!item.value) return { name: 'forum-new' }
  return {
    name: 'forum-new',
    query: {
      relatedEntityType: 'ACTION_ITEM',
      relatedEntityId: String(item.value.id),
      bookId: String(item.value.bookId),
      sourceReferenceId: item.value.sourceReference ? String(item.value.sourceReference.id) : undefined,
      title: `Discuss action: ${item.value.title}`,
    },
  }
})

onMounted(loadActionItem)

async function loadActionItem() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [itemResult, booksResult] = await Promise.all([getActionItem(String(route.params.id)), getBooks()])
    item.value = itemResult
    books.value = booksResult
    rightRail.setSourceFromActionItem(itemResult, booksResult.find((book) => book.id === itemResult.bookId))
  } catch {
    item.value = null
    errorMessage.value = 'Check your connection or permissions and try opening the action again.'
  } finally {
    loading.value = false
  }
}

async function saveActionItem(payload: ActionItemPayload) {
  if (!item.value) return
  savingItem.value = true
  try {
    item.value = await updateActionItem(item.value.id, payload)
    actionDialogOpen.value = false
    rightRail.setSourceFromActionItem(item.value, books.value.find((book) => book.id === item.value?.bookId))
    ElMessage.success('Action updated.')
  } catch {
    ElMessage.error('Action update failed.')
  } finally {
    savingItem.value = false
  }
}

async function toggleCompletion() {
  if (!item.value) return
  toggling.value = true
  try {
    item.value = item.value.completed ? await reopenActionItem(item.value.id) : await completeActionItem(item.value.id)
    rightRail.setSourceFromActionItem(item.value, books.value.find((book) => book.id === item.value?.bookId))
    ElMessage.success(item.value.completed ? 'Action completed.' : 'Action reopened.')
  } catch {
    ElMessage.error('Action status update failed.')
  } finally {
    toggling.value = false
  }
}

async function archiveCurrentItem() {
  if (!item.value) return
  try {
    await ElMessageBox.confirm('Archive this action? It will be removed from active action views.', 'Archive Action', {
      confirmButtonText: 'Archive',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await archiveActionItem(item.value.id)
    ElMessage.success('Action archived.')
    await router.push({ name: 'action-items' })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('Action archive failed.')
    }
  }
}

function openSource() {
  if (!item.value) return
  rightRail.setSourceFromActionItem(item.value, books.value.find((book) => book.id === item.value?.bookId))
  void openSourceDrawer({
    sourceType: 'ACTION_ITEM',
    sourceId: item.value.id,
    bookId: item.value.bookId,
    bookTitle: item.value.bookTitle,
    pageStart: item.value.pageStart,
    noteId: item.value.noteId ?? undefined,
    rawCaptureId: item.value.rawCaptureId ?? undefined,
    noteBlockId: item.value.noteBlockId ?? undefined,
    sourceReference: item.value.sourceReference,
    sourceReferenceId: item.value.sourceReference?.id ?? null,
  })
}

function pageLabel(value: ActionItemRecord) {
  if (value.pageStart && value.pageEnd) return `p.${value.pageStart}-${value.pageEnd}`
  if (value.pageStart) return `p.${value.pageStart}`
  return ''
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

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
</script>

<style scoped>
.action-detail-page {
  display: grid;
  gap: var(--space-5);
}

.action-detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 0.42fr);
  gap: var(--space-4);
}

.action-main,
.action-source {
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.action-main__badges {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.action-main h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.7rem, 4vw, 3rem);
  line-height: 1.12;
}

.action-main__description {
  margin: 0;
  color: var(--bookos-text-secondary);
  font-size: 1.05rem;
  line-height: var(--type-body-line);
  white-space: pre-wrap;
}

.action-meta {
  margin: 0;
  display: grid;
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
  line-height: var(--type-body-line);
  overflow-wrap: anywhere;
}

@media (max-width: 980px) {
  .action-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
