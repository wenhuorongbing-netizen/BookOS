<template>
  <div class="page-shell forum-thread-page">
    <AppLoadingState v-if="loading" label="Loading forum thread" />
    <AppErrorState v-else-if="errorMessage" title="Forum thread could not load" :description="errorMessage" retry-label="Retry" @retry="loadThread" />

    <template v-else-if="thread">
      <AppSectionHeader
        :title="thread.title"
        :eyebrow="thread.categoryName"
        :description="`Started by ${thread.authorDisplayName}. ${thread.commentCount} comments.`"
        :level="1"
      >
        <template #actions>
          <RouterLink to="/forum" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Forum</AppButton>
          </RouterLink>
          <AppButton variant="ghost" :disabled="!thread.sourceReference" @click="openThreadSource">Open Source</AppButton>
          <RouterLink :to="graphContextLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Knowledge Graph</AppButton>
          </RouterLink>
          <AppButton variant="secondary" :loading="busy" @click="toggleLike">
            {{ thread.likedByCurrentUser ? 'Unlike' : 'Like' }} ({{ thread.likeCount }})
          </AppButton>
          <AppButton variant="secondary" :loading="busy" @click="toggleBookmark">
            {{ thread.bookmarkedByCurrentUser ? 'Remove Bookmark' : 'Bookmark' }}
          </AppButton>
          <AppButton v-if="thread.canEdit" variant="primary" @click="editingThread = !editingThread">
            {{ editingThread ? 'Cancel Edit' : 'Edit' }}
          </AppButton>
          <AppButton v-if="thread.canModerate && thread.status !== 'LOCKED'" variant="secondary" :loading="busy" @click="setThreadStatus('LOCKED')">
            Lock
          </AppButton>
          <AppButton v-if="thread.canModerate && thread.status !== 'OPEN'" variant="secondary" :loading="busy" @click="setThreadStatus('OPEN')">
            Reopen
          </AppButton>
          <AppButton v-if="thread.canModerate && thread.status !== 'HIDDEN'" variant="danger" :loading="busy" @click="setThreadStatus('HIDDEN')">
            Hide
          </AppButton>
          <AppButton v-if="thread.canEdit" variant="text" @click="archiveThread">Delete</AppButton>
          <AppButton variant="text" @click="reportOpen = true">Report</AppButton>
        </template>
      </AppSectionHeader>

      <section class="thread-layout">
        <main class="thread-main">
          <AppCard class="thread-card" as="article">
            <div class="thread-meta">
              <AppBadge variant="info">{{ thread.categoryName }}</AppBadge>
              <AppBadge :variant="statusVariant(thread.status)">{{ statusLabel(thread.status) }}</AppBadge>
              <AppBadge :variant="thread.visibility === 'PRIVATE' ? 'warning' : 'success'">{{ thread.visibility }}</AppBadge>
              <AppBadge v-if="thread.relatedEntityType" variant="accent">{{ thread.relatedEntityType }}</AppBadge>
              <AppBadge v-if="thread.relatedBookTitle" variant="primary">{{ thread.relatedBookTitle }}</AppBadge>
              <AppBadge v-if="thread.relatedConceptName" variant="primary">[[{{ thread.relatedConceptName }}]]</AppBadge>
              <AppBadge v-if="thread.relatedProjectTitle" variant="success">{{ thread.relatedProjectTitle }}</AppBadge>
              <AppBadge v-if="thread.sourceContextUnavailable" variant="warning">Private source context hidden</AppBadge>
              <AppBadge v-if="thread.canModerate && thread.reportCount" variant="danger">{{ thread.reportCount }} open reports</AppBadge>
            </div>

            <div v-if="editingThread" class="thread-editor">
              <label class="form-field">
                <span>Title</span>
                <el-input v-model="editForm.title" maxlength="220" show-word-limit />
              </label>
              <label class="form-field">
                <span>Body Markdown</span>
                <el-input v-model="editForm.bodyMarkdown" type="textarea" :rows="10" maxlength="20000" show-word-limit />
              </label>
              <div class="thread-actions">
                <AppButton variant="primary" :loading="busy" @click="saveThread">Save Thread</AppButton>
                <AppButton variant="ghost" @click="editingThread = false">Cancel</AppButton>
              </div>
            </div>

            <div v-else class="markdown-body" v-html="renderSafeMarkdown(thread.bodyMarkdown)" />
          </AppCard>

          <AppCard class="comments-card" as="section">
            <AppSectionHeader title="Comments" eyebrow="Discussion" :level="2" compact />
            <div class="comment-form">
              <AppEmptyState
                v-if="thread.status === 'LOCKED' || thread.status === 'HIDDEN'"
                title="Thread is not accepting replies"
                :description="thread.status === 'HIDDEN' ? 'This thread is hidden by moderation.' : 'A moderator locked this thread.'"
                compact
              />
              <label class="form-field">
                <span>Add comment</span>
                <el-input
                  v-model="newComment"
                  type="textarea"
                  :rows="4"
                  :disabled="thread.status === 'LOCKED' || thread.status === 'HIDDEN'"
                  placeholder="Add a source-aware response..."
                />
              </label>
              <AppButton
                variant="primary"
                :loading="busy"
                :disabled="thread.status === 'LOCKED' || thread.status === 'HIDDEN'"
                @click="submitComment"
              >
                Post Comment
              </AppButton>
            </div>

            <AppEmptyState v-if="!comments.length" title="No comments yet" description="Be the first to add a structured reply." compact />
            <article v-for="comment in comments" :key="comment.id" class="comment-card">
              <div class="comment-card__meta">
                <strong>{{ comment.authorDisplayName }}</strong>
                <span>{{ formatDate(comment.createdAt) }}</span>
              </div>
              <div v-if="editingCommentId === comment.id" class="comment-editor">
                <el-input v-model="editingCommentBody" type="textarea" :rows="4" />
                <div class="thread-actions">
                  <AppButton variant="primary" :loading="busy" @click="saveComment(comment.id)">Save</AppButton>
                  <AppButton variant="ghost" @click="editingCommentId = null">Cancel</AppButton>
                </div>
              </div>
              <template v-else>
                <div class="markdown-body markdown-body--comment" v-html="renderSafeMarkdown(comment.bodyMarkdown)" />
                <div v-if="comment.canEdit" class="thread-actions">
                  <AppButton variant="text" @click="startEditComment(comment)">Edit</AppButton>
                  <AppButton variant="text" @click="removeComment(comment.id)">Delete</AppButton>
                </div>
              </template>
            </article>
          </AppCard>
        </main>

        <aside class="thread-aside">
          <AppCard class="thread-card" as="section">
            <AppSectionHeader title="Related Context" eyebrow="Traceability" :level="2" compact />
            <dl class="context-list">
              <div>
                <dt>Entity</dt>
                <dd>{{ thread.relatedEntityType ? `${thread.relatedEntityType} #${thread.relatedEntityId ?? 'unknown'}` : 'Not attached' }}</dd>
              </div>
              <div>
                <dt>Book</dt>
                <dd>{{ thread.relatedBookTitle ?? 'Not visible or not attached' }}</dd>
              </div>
              <div>
                <dt>Concept</dt>
                <dd>{{ thread.relatedConceptName ?? 'Not visible or not attached' }}</dd>
              </div>
              <div>
                <dt>Project</dt>
                <dd>{{ thread.relatedProjectTitle ?? 'Not visible or not attached' }}</dd>
              </div>
              <div>
                <dt>Source</dt>
                <dd>{{ thread.sourceReference?.locationLabel ?? (thread.sourceReferenceId ? `Source #${thread.sourceReferenceId}` : 'Not attached') }}</dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>{{ statusLabel(thread.status) }}</dd>
              </div>
              <div v-if="thread.canModerate">
                <dt>Open reports</dt>
                <dd>{{ thread.reportCount }}</dd>
              </div>
            </dl>
          </AppCard>
        </aside>
      </section>

      <BacklinksSection
        entity-type="FORUM_THREAD"
        :entity-id="thread.id"
        :source-references="thread.sourceReference ? [thread.sourceReference] : []"
        :book-title="thread.relatedBookTitle"
      />

      <el-dialog v-model="reportOpen" title="Report Thread" width="min(520px, 96vw)">
        <label class="form-field">
          <span>Reason</span>
          <el-input v-model="reportReason" maxlength="120" />
        </label>
        <label class="form-field">
          <span>Details</span>
          <el-input v-model="reportDetails" type="textarea" :rows="4" />
        </label>
        <template #footer>
          <AppButton variant="ghost" @click="reportOpen = false">Cancel</AppButton>
          <AppButton variant="primary" :loading="busy" @click="submitReport">Submit Report</AppButton>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import {
  bookmarkForumThread,
  createForumComment,
  deleteForumComment,
  deleteForumThread,
  getForumComments,
  getForumThread,
  likeForumThread,
  moderateForumThread,
  removeForumBookmark,
  removeForumLike,
  reportForumThread,
  updateForumComment,
  updateForumThread,
} from '../api/forum'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { ForumCommentRecord, ForumThreadPayload, ForumThreadRecord } from '../types'
import { renderSafeMarkdown } from '../utils/markdown'

const route = useRoute()
const router = useRouter()
const { openSource } = useOpenSource()
const thread = ref<ForumThreadRecord | null>(null)
const comments = ref<ForumCommentRecord[]>([])
const loading = ref(false)
const busy = ref(false)
const errorMessage = ref('')
const editingThread = ref(false)
const newComment = ref('')
const editingCommentId = ref<number | null>(null)
const editingCommentBody = ref('')
const reportOpen = ref(false)
const reportReason = ref('')
const reportDetails = ref('')
const editForm = reactive<ForumThreadPayload>({
  categoryId: 0,
  title: '',
  bodyMarkdown: '',
  relatedEntityType: null,
  relatedEntityId: null,
  relatedBookId: null,
  relatedConceptId: null,
  relatedProjectId: null,
  sourceReferenceId: null,
  visibility: 'SHARED',
})

const graphContextLink = computed(() => {
  if (thread.value?.relatedBookId) return { name: 'graph-book', params: { bookId: thread.value.relatedBookId } }
  if (thread.value?.relatedConceptId) return { name: 'graph-concept', params: { conceptId: thread.value.relatedConceptId } }
  if (thread.value?.relatedProjectId) return { name: 'graph-project', params: { projectId: thread.value.relatedProjectId } }
  return { name: 'graph' }
})

onMounted(loadThread)

async function loadThread() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [threadResult, commentResult] = await Promise.all([
      getForumThread(String(route.params.id)),
      getForumComments(String(route.params.id)),
    ])
    thread.value = threadResult
    comments.value = commentResult
    syncEditForm(threadResult)
  } catch {
    errorMessage.value = 'Check backend availability or permissions, then try opening this thread again.'
  } finally {
    loading.value = false
  }
}

function syncEditForm(value: ForumThreadRecord) {
  editForm.categoryId = value.categoryId
  editForm.title = value.title
  editForm.bodyMarkdown = value.bodyMarkdown
  editForm.relatedEntityType = value.relatedEntityType
  editForm.relatedEntityId = value.relatedEntityId
  editForm.relatedBookId = value.relatedBookId
  editForm.relatedConceptId = value.relatedConceptId
  editForm.relatedProjectId = value.relatedProjectId
  editForm.sourceReferenceId = value.sourceReferenceId
  editForm.visibility = value.visibility
}

async function saveThread() {
  if (!thread.value) return
  busy.value = true
  try {
    thread.value = await updateForumThread(thread.value.id, editForm)
    editingThread.value = false
    ElMessage.success('Thread updated.')
  } catch {
    ElMessage.error('Thread update failed.')
  } finally {
    busy.value = false
  }
}

async function archiveThread() {
  if (!thread.value) return
  try {
    await ElMessageBox.confirm('Delete this forum thread? It will be archived from active forum views.', 'Delete Thread', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await deleteForumThread(thread.value.id)
    ElMessage.success('Thread deleted.')
    await router.push({ name: 'forum' })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error('Thread delete failed.')
  }
}

async function toggleLike() {
  if (!thread.value) return
  busy.value = true
  try {
    thread.value = thread.value.likedByCurrentUser ? await removeForumLike(thread.value.id) : await likeForumThread(thread.value.id)
  } finally {
    busy.value = false
  }
}

async function toggleBookmark() {
  if (!thread.value) return
  busy.value = true
  try {
    thread.value = thread.value.bookmarkedByCurrentUser
      ? await removeForumBookmark(thread.value.id)
      : await bookmarkForumThread(thread.value.id)
  } finally {
    busy.value = false
  }
}

async function submitComment() {
  if (!thread.value || !newComment.value.trim()) return
  busy.value = true
  try {
    await createForumComment(thread.value.id, { bodyMarkdown: newComment.value.trim() })
    newComment.value = ''
    comments.value = await getForumComments(thread.value.id)
    thread.value = await getForumThread(thread.value.id)
    ElMessage.success('Comment posted.')
  } finally {
    busy.value = false
  }
}

async function setThreadStatus(status: 'OPEN' | 'LOCKED' | 'HIDDEN') {
  if (!thread.value) return
  busy.value = true
  try {
    thread.value = await moderateForumThread(thread.value.id, { status })
    ElMessage.success(`Thread marked ${status.toLowerCase()}.`)
  } catch {
    ElMessage.error('Moderation update failed.')
  } finally {
    busy.value = false
  }
}

function startEditComment(comment: ForumCommentRecord) {
  editingCommentId.value = comment.id
  editingCommentBody.value = comment.bodyMarkdown
}

async function saveComment(id: number) {
  if (!editingCommentBody.value.trim()) return
  busy.value = true
  try {
    await updateForumComment(id, { bodyMarkdown: editingCommentBody.value.trim() })
    editingCommentId.value = null
    comments.value = await getForumComments(String(route.params.id))
    ElMessage.success('Comment updated.')
  } finally {
    busy.value = false
  }
}

async function removeComment(id: number) {
  busy.value = true
  try {
    await deleteForumComment(id)
    comments.value = await getForumComments(String(route.params.id))
    if (thread.value) thread.value = await getForumThread(thread.value.id)
    ElMessage.success('Comment deleted.')
  } finally {
    busy.value = false
  }
}

async function submitReport() {
  if (!thread.value || !reportReason.value.trim()) {
    ElMessage.warning('Report reason is required.')
    return
  }
  busy.value = true
  try {
    await reportForumThread(thread.value.id, { reason: reportReason.value.trim(), details: reportDetails.value.trim() || null })
    reportOpen.value = false
    reportReason.value = ''
    reportDetails.value = ''
    ElMessage.success('Report submitted.')
  } finally {
    busy.value = false
  }
}

function openThreadSource() {
  if (!thread.value?.sourceReference) return
  const source = thread.value.sourceReference
  void openSource({
    sourceType: thread.value.relatedEntityType ?? 'FORUM_THREAD',
    sourceId: thread.value.relatedEntityId ?? thread.value.id,
    bookId: source.bookId,
    bookTitle: thread.value.relatedBookTitle,
    pageStart: source.pageStart,
    noteId: source.noteId ?? undefined,
    rawCaptureId: source.rawCaptureId ?? undefined,
    noteBlockId: source.noteBlockId ?? undefined,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function statusLabel(status: ForumThreadRecord['status']) {
  if (status === 'ACTIVE') return 'OPEN'
  if (status === 'CLOSED') return 'LOCKED'
  return status
}

function statusVariant(status: ForumThreadRecord['status']) {
  const canonical = statusLabel(status)
  if (canonical === 'LOCKED') return 'warning'
  if (canonical === 'HIDDEN') return 'danger'
  return 'success'
}
</script>

<style scoped>
.forum-thread-page,
.thread-main,
.comments-card {
  display: grid;
  gap: var(--space-5);
}

.thread-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 0.34fr);
  gap: var(--space-4);
}

.thread-card,
.comments-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.thread-aside {
  align-self: start;
  position: sticky;
  top: calc(var(--space-4) + 72px);
}

.thread-meta,
.thread-actions,
.comment-card__meta {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.thread-editor,
.comment-form,
.comment-editor {
  display: grid;
  gap: var(--space-3);
}

.form-field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.comment-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.comment-card__meta {
  justify-content: space-between;
  color: var(--bookos-text-secondary);
}

.context-list {
  margin: 0;
  display: grid;
  gap: var(--space-3);
}

.context-list dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.context-list dd {
  margin: 0;
  color: var(--bookos-text-primary);
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(p),
.markdown-body :deep(blockquote),
.markdown-body :deep(ul) {
  margin: 0 0 var(--space-3);
}

.markdown-body :deep(blockquote) {
  padding-left: var(--space-4);
  border-left: 4px solid var(--bookos-accent);
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
}

.markdown-body :deep(code) {
  padding: 0.1rem 0.32rem;
  border-radius: var(--radius-sm);
  background: var(--bookos-surface-muted);
}

.markdown-body--comment :deep(p:last-child) {
  margin-bottom: 0;
}

@media (max-width: 980px) {
  .thread-layout {
    grid-template-columns: 1fr;
  }

  .thread-aside {
    position: static;
  }
}
</style>
