<template>
  <aside class="right-rail" aria-label="Contextual sidebar">
    <template v-if="showDashboardStarterRail">
      <ContextPanel eyebrow="Context" title="No source pinned yet">
        <div class="rail-starter">
          <strong>Pin context when you open a source</strong>
          <p>
            Open a book, quote, note, action, or source-backed project application to use source tools here. Draft
            Assistant and extracted actions stay quiet until there is useful context.
          </p>
          <div class="rail-starter__actions">
            <RouterLink to="/guided/first-loop" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Start first loop</AppButton>
            </RouterLink>
            <RouterLink to="/books/new" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Add Book</AppButton>
            </RouterLink>
            <RouterLink to="/captures/inbox" custom v-slot="{ navigate }">
              <AppButton variant="ghost" @click="navigate">Process Captures</AppButton>
            </RouterLink>
          </div>
        </div>
      </ContextPanel>
    </template>

    <template v-else>
      <ContextPanel eyebrow="Source Link" title="Current source">
      <div v-if="source" class="source-card">
        <div class="source-card__topline">
          <AppBadge :variant="source.type === 'quote' ? 'accent' : source.type === 'action-item' ? 'warning' : 'primary'" size="sm">
            {{ sourceTypeLabel(source.type) }}
          </AppBadge>
        </div>
        <div class="source-card__book">
          <img v-if="source.coverUrl" :src="source.coverUrl" :alt="`Cover of ${source.bookTitle}`" />
          <div v-else class="source-card__cover-fallback" role="img" :aria-label="`Cover placeholder for ${source.bookTitle}`">
            {{ source.bookTitle.slice(0, 2).toUpperCase() }}
          </div>
          <div class="source-card__identity">
            <strong class="book-title">{{ source.bookTitle }}</strong>
            <span v-if="source.subtitle">{{ source.subtitle }}</span>
            <span>{{ source.author ?? 'Unknown author' }}</span>
          </div>
        </div>

        <blockquote v-if="source.excerpt" class="source-card__excerpt">
          <p>{{ source.excerpt }}</p>
        </blockquote>

        <dl class="source-card__meta" aria-label="Source metadata">
          <div>
            <dt>Chapter</dt>
            <dd>{{ source.chapter ?? 'Not tracked yet' }}</dd>
          </div>
          <div>
            <dt>Page range</dt>
            <dd>{{ source.pageRange ?? 'Not tracked yet' }}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>{{ source.location ?? 'Current book context' }}</dd>
          </div>
          <div>
            <dt>Added date</dt>
            <dd>{{ formatDate(source.addedAt) }}</dd>
          </div>
        </dl>

        <AppButton variant="secondary" @click="openSource">
          <span aria-hidden="true">EX</span>
          Open Source
        </AppButton>
      </div>

      <div v-else class="rail-empty">
        <strong>No source selected</strong>
        <p>Open a book, quote, note, or action to pin its source here.</p>
      </div>
    </ContextPanel>

    <ContextPanel eyebrow="Draft Assistant" title="Draft assistance">
      <div class="draft-disabled">
        <div class="draft-disabled__status">
          <AppBadge :variant="aiStatus?.available ? 'info' : 'warning'" size="sm">
            {{ aiStatus?.activeProvider ?? 'AI provider' }}
          </AppBadge>
          <AppBadge v-if="aiStatus?.configuredProvider === 'openai-compatible'" variant="accent" size="sm">External configured</AppBadge>
          <AppBadge v-else variant="neutral" size="sm">Local mock</AppBadge>
          <HelpTooltip topic="ai-draft" placement="left" />
        </div>
        <p>{{ aiStatus?.message ?? 'Draft-only suggestions are generated from existing BookOS content.' }}</p>
        <p>Accepting a draft records your decision only; it never overwrites notes, quotes, concepts, projects, forum posts, or actions.</p>
      </div>
      <div class="ai-task-row" aria-label="Generate assistant draft suggestions">
        <label class="ai-task-row__field">
          <span>Generation task</span>
          <el-select v-model="selectedAITask" aria-label="AI generation task">
            <el-option v-for="task in aiTaskOptions" :key="task.value" :label="task.label" :value="task.value" />
          </el-select>
        </label>
        <AppButton
          variant="secondary"
          :disabled="aiStatus ? !aiStatus.available : false"
          :loading="aiGenerating === selectedAITask"
          @click="generateSuggestion(selectedAITask)"
        >
          Generate draft
        </AppButton>
      </div>

      <AppLoadingState v-if="aiLoading" label="Loading assistant drafts" compact />
      <AppErrorState v-else-if="aiError" title="Draft Assistant unavailable" :description="aiError" retry-label="Retry" @retry="loadAISuggestions" />

      <div v-else-if="visibleDrafts.length" class="draft-list">
        <article v-for="draft in visibleDrafts" :key="draft.id" class="draft-card">
          <div class="draft-card__topline">
            <AppBadge variant="warning" size="sm">Draft</AppBadge>
            <span>{{ draft.providerName || 'MockAIProvider' }} - {{ formatSuggestionType(draft.suggestionType) }} - {{ formatDate(draft.updatedAt) }}</span>
          </div>
          <div v-if="jsonValidationWarnings(draft).length" class="draft-card__warnings">
            <strong>JSON validation warnings</strong>
            <span v-for="warning in jsonValidationWarnings(draft)" :key="warning">{{ warning }}</span>
          </div>

          <label v-if="editingDraftId === draft.id" class="draft-card__editor">
            <span>Edit draft suggestion</span>
            <el-input v-model="draftEditText" type="textarea" :rows="4" />
          </label>
          <p v-else>{{ draft.draftText }}</p>

          <div class="right-rail__actions" :aria-label="`Actions for assistant draft ${draft.id}`">
            <template v-if="editingDraftId === draft.id">
              <AppButton variant="primary" :loading="aiMutatingId === draft.id" @click="saveDraftEdit(draft)">Save edit</AppButton>
              <AppButton variant="ghost" @click="cancelDraftEdit">Cancel</AppButton>
            </template>
            <template v-else>
              <AppButton variant="secondary" @click="pendingAcceptDraft = draft">Accept</AppButton>
              <AppButton variant="ghost" @click="startDraftEdit(draft)">Edit</AppButton>
              <AppButton variant="text" :loading="aiMutatingId === draft.id" @click="discardDraft(draft.id)">Reject</AppButton>
              <AppButton v-if="draft.sourceReference" variant="text" @click="openAISource(draft)">View source</AppButton>
            </template>
          </div>
        </article>
      </div>

      <div v-else class="rail-empty">
        <strong>No assistant drafts available</strong>
        <p>Generate a MockAIProvider draft from the current source or recent user content.</p>
      </div>
    </ContextPanel>

    <ContextPanel eyebrow="Extracted Actions" title="Next actions">
      <div v-if="actionItems.length" class="action-list">
        <article v-for="item in actionItems" :key="item.id" class="action-item" :class="{ 'action-item--complete': item.completed }">
          <div class="action-item__body">
            <span class="action-item__text">{{ item.text }}</span>
            <span v-if="item.pageRange" class="action-item__source">{{ item.pageRange }}</span>
          </div>
          <div class="action-item__actions">
            <AppBadge :variant="priorityVariant(item.priority)" size="sm">{{ priorityLabel(item.priority) }}</AppBadge>
            <AppButton variant="text" :loading="togglingActionItemId === item.id" @click="toggleActionItem(item.id)">
              {{ item.completed ? 'Reopen' : 'Complete' }}
            </AppButton>
            <AppButton variant="text" @click="openActionItemSource(item)">Open source</AppButton>
          </div>
        </article>
      </div>

      <div v-else class="rail-empty">
        <strong>No extracted actions yet</strong>
        <p>Actions converted from captures, notes, or manual entries will be listed here.</p>
      </div>

      <AppButton variant="secondary" @click="openActionItems">View all actions</AppButton>
    </ContextPanel>
    </template>

    <div
      v-if="pendingAcceptDraft"
      class="rail-confirm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="accept-draft-title"
      @keydown.escape="pendingAcceptDraft = null"
    >
      <div ref="confirmPanel" class="rail-confirm__panel" tabindex="-1">
        <h2 id="accept-draft-title">Accept assistant draft?</h2>
        <p>This marks the suggestion as accepted. It still will not overwrite existing user content automatically.</p>
        <div class="right-rail__actions">
          <AppButton variant="primary" :loading="aiMutatingId === pendingAcceptDraft.id" @click="confirmAcceptDraft">Accept draft</AppButton>
          <AppButton variant="ghost" @click="pendingAcceptDraft = null">Cancel</AppButton>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  acceptAISuggestion,
  createDesignLensSuggestion,
  createExtractActionsSuggestion,
  createExtractConceptsSuggestion,
  createForumThreadSuggestion,
  createNoteSummarySuggestion,
  createProjectApplicationSuggestion,
  editAISuggestion,
  getAIProviderStatus,
  getAISuggestions,
  rejectAISuggestion,
} from '../api/ai'
import ContextPanel from './ContextPanel.vue'
import HelpTooltip from './help/HelpTooltip.vue'
import AppBadge from './ui/AppBadge.vue'
import AppButton from './ui/AppButton.vue'
import AppErrorState from './ui/AppErrorState.vue'
import AppLoadingState from './ui/AppLoadingState.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useRightRailStore, type ActionItemPriority, type ExtractedActionItem, type RailSourceType } from '../stores/rightRail'
import type { AIProviderStatusRecord, AISuggestionPayload, AISuggestionRecord, AISuggestionType } from '../types'

const router = useRouter()
const route = useRoute()
const rail = useRightRailStore()
const { openSource: openSourceDrawer } = useOpenSource()

const aiSuggestions = ref<AISuggestionRecord[]>([])
const aiStatus = ref<AIProviderStatusRecord | null>(null)
const aiLoading = ref(false)
const aiError = ref('')
const aiGenerating = ref<AISuggestionType | null>(null)
const selectedAITask = ref<AISuggestionType>('NOTE_SUMMARY')
const aiMutatingId = ref<number | null>(null)
const editingDraftId = ref<number | null>(null)
const draftEditText = ref('')
const pendingAcceptDraft = ref<AISuggestionRecord | null>(null)
const confirmPanel = ref<HTMLElement | null>(null)
const togglingActionItemId = ref<string | null>(null)

const source = computed(() => rail.sourceReference)
const visibleDrafts = computed(() => aiSuggestions.value.filter((draft) => draft.status === 'DRAFT'))
const actionItems = computed(() => rail.actionItems)
const showDashboardStarterRail = computed(
  () => route.name === 'dashboard' && !source.value && !visibleDrafts.value.length && !actionItems.value.length && !aiError.value,
)
const aiTaskOptions: { label: string; value: AISuggestionType }[] = [
  { label: 'Summarize source', value: 'NOTE_SUMMARY' },
  { label: 'Extract actions', value: 'EXTRACT_ACTIONS' },
  { label: 'Extract concepts', value: 'EXTRACT_CONCEPTS' },
  { label: 'Suggest design lenses', value: 'SUGGEST_DESIGN_LENSES' },
  { label: 'Suggest project applications', value: 'SUGGEST_PROJECT_APPLICATIONS' },
  { label: 'Draft forum thread', value: 'FORUM_THREAD_DRAFT' },
]

onMounted(loadAISuggestions)

watch(pendingAcceptDraft, async (draft) => {
  if (!draft) return
  await nextTick()
  confirmPanel.value?.focus()
})

function openSource() {
  if (!source.value) return
  const querySourceType = source.value.type === 'note' && source.value.id.startsWith('note-block-') ? 'NOTE_BLOCK' : sourceTypeQuery(source.value.type)
  const sourceId = source.value.entityId ?? source.value.id.split('-').at(-1)
  void openSourceDrawer({
    sourceType: querySourceType ?? source.value.type,
    sourceId: sourceId ?? source.value.id,
    bookId: source.value.bookId,
    bookTitle: source.value.bookTitle,
    sourceReferenceId: source.value.sourceReferenceId ?? null,
    locationLabel: source.value.location,
    sourceText: source.value.excerpt,
  })
}

function openActionItems() {
  router.push({
    name: 'action-items',
    query: source.value?.bookId ? { bookId: String(source.value.bookId) } : undefined,
  })
}

function openActionItemSource(item: ExtractedActionItem) {
  rail.setSourceReference({
    id: `action-${item.id}`,
    type: 'action-item',
    entityId: item.id,
    bookId: item.bookId,
    bookTitle: item.bookTitle,
    pageRange: item.pageRange,
    location: 'Action',
    excerpt: item.text,
  })
  void openSourceDrawer({
    sourceType: item.sourceId ? 'SOURCE_REFERENCE' : 'ACTION_ITEM',
    sourceId: item.sourceId ?? item.id,
    bookId: item.bookId,
    bookTitle: item.bookTitle,
    sourceReferenceId: item.sourceId ?? null,
    locationLabel: 'Action',
    sourceText: item.text,
  })
}

async function loadAISuggestions() {
  aiLoading.value = true
  aiError.value = ''
  try {
    const [status, suggestions] = await Promise.all([getAIProviderStatus(), getAISuggestions()])
    aiStatus.value = status
    aiSuggestions.value = suggestions
  } catch (error) {
    aiError.value = apiErrorMessage(error, 'Assistant drafts could not be loaded.')
  } finally {
    aiLoading.value = false
  }
}

async function generateSuggestion(type: AISuggestionType) {
  aiGenerating.value = type
  try {
    const payload = currentSuggestionPayload()
    const suggestion = await createSuggestionByType(type, payload)
    upsertSuggestion(suggestion)
    ElMessage.success(`${aiStatus.value?.activeProvider ?? 'AI provider'} draft created.`)
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, 'Assistant draft generation failed.'))
  } finally {
    aiGenerating.value = null
  }
}

function createSuggestionByType(type: AISuggestionType, payload: AISuggestionPayload) {
  if (type === 'NOTE_SUMMARY') return createNoteSummarySuggestion(payload)
  if (type === 'EXTRACT_ACTIONS') return createExtractActionsSuggestion(payload)
  if (type === 'EXTRACT_CONCEPTS') return createExtractConceptsSuggestion(payload)
  if (type === 'SUGGEST_DESIGN_LENSES') return createDesignLensSuggestion(payload)
  if (type === 'SUGGEST_PROJECT_APPLICATIONS') return createProjectApplicationSuggestion(payload)
  return createForumThreadSuggestion(payload)
}

function startDraftEdit(draft: AISuggestionRecord) {
  editingDraftId.value = draft.id
  draftEditText.value = draft.draftText
}

async function saveDraftEdit(draft: AISuggestionRecord) {
  aiMutatingId.value = draft.id
  try {
    const updated = await editAISuggestion(draft.id, { draftText: draftEditText.value, draftJson: draft.draftJson })
    upsertSuggestion(updated)
    cancelDraftEdit()
    ElMessage.success('Draft suggestion updated.')
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, 'Draft suggestion could not be edited.'))
  } finally {
    aiMutatingId.value = null
  }
}

function cancelDraftEdit() {
  editingDraftId.value = null
  draftEditText.value = ''
}

async function confirmAcceptDraft() {
  if (!pendingAcceptDraft.value) return
  aiMutatingId.value = pendingAcceptDraft.value.id
  try {
    const updated = await acceptAISuggestion(pendingAcceptDraft.value.id)
    upsertSuggestion(updated)
    pendingAcceptDraft.value = null
    ElMessage.success('Draft suggestion accepted. No user content was overwritten.')
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, 'Draft suggestion could not be accepted.'))
  } finally {
    aiMutatingId.value = null
  }
}

async function discardDraft(id: number) {
  aiMutatingId.value = id
  try {
    const updated = await rejectAISuggestion(id)
    upsertSuggestion(updated)
    ElMessage.success('Draft suggestion rejected.')
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, 'Draft suggestion could not be rejected.'))
  } finally {
    aiMutatingId.value = null
  }
}

function openAISource(draft: AISuggestionRecord) {
  if (!draft.sourceReference) return
  void openSourceDrawer({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: draft.sourceReference.id,
    bookId: draft.sourceReference.bookId,
    bookTitle: draft.bookTitle,
    sourceReferenceId: draft.sourceReference.id,
    sourceReference: draft.sourceReference,
  })
}

function currentSuggestionPayload(): AISuggestionPayload {
  const current = source.value
  if (!current) return {}
  const sourceReferenceId = current.sourceReferenceId ?? (current.id.startsWith('source-') ? current.entityId : null)
  const numericSourceReferenceId = sourceReferenceId === null || sourceReferenceId === undefined ? null : Number(sourceReferenceId)
  return {
    bookId: current.bookId,
    sourceReferenceId: numericSourceReferenceId && !Number.isNaN(numericSourceReferenceId) ? numericSourceReferenceId : null,
    text: current.excerpt ?? null,
  }
}

function upsertSuggestion(suggestion: AISuggestionRecord) {
  const index = aiSuggestions.value.findIndex((item) => item.id === suggestion.id)
  if (index >= 0) {
    aiSuggestions.value[index] = suggestion
    return
  }
  aiSuggestions.value.unshift(suggestion)
}

async function toggleActionItem(id: string) {
  togglingActionItemId.value = id
  try {
    await rail.toggleActionItem(id)
    ElMessage.success('Action updated.')
  } catch (error) {
    ElMessage.error(actionItemErrorMessage(error))
  } finally {
    togglingActionItemId.value = null
  }
}

function sourceTypeLabel(type: RailSourceType) {
  if (type === 'quote') return 'Quote'
  if (type === 'action-item') return 'Action'
  if (type === 'note') return 'Note source'
  if (type === 'daily-sentence') return 'Daily sentence'
  return 'Book context'
}

function sourceTypeQuery(type: RailSourceType) {
  if (type === 'quote') return 'QUOTE'
  if (type === 'action-item') return 'ACTION_ITEM'
  if (type === 'note') return 'NOTE'
  if (type === 'daily-sentence') return 'DAILY_SENTENCE'
  return null
}

function actionItemErrorMessage(error: unknown) {
  if (typeof error !== 'object' || error === null || !('response' in error)) {
    return 'Action update failed. Check that the backend is available and try again.'
  }

  const response = (error as { response?: { status?: number; data?: { message?: string } } }).response
  if (!response) return 'Backend unavailable. Check that the API server is running and try again.'
  if (response.status === 403) return 'Permission denied. You do not have access to update this action.'
  if (response.status === 400) return response.data?.message ?? 'Validation error. The action could not be updated.'
  return response.data?.message ?? 'Action update failed.'
}

function apiErrorMessage(error: unknown, fallback: string) {
  if (typeof error !== 'object' || error === null || !('response' in error)) {
    return fallback
  }

  const response = (error as { response?: { status?: number; data?: { message?: string } } }).response
  if (!response) return 'Backend unavailable. Check that the API server is running and try again.'
  if (response.status === 403) return 'Permission denied. You do not have access to this assistant draft.'
  if (response.status === 400) return response.data?.message ?? 'Validation error. The assistant draft request could not be completed.'
  return response.data?.message ?? fallback
}

function formatSuggestionType(type: AISuggestionType) {
  if (type === 'NOTE_SUMMARY') return 'Note summary'
  if (type === 'EXTRACT_ACTIONS') return 'Extract actions'
  if (type === 'EXTRACT_CONCEPTS') return 'Extract concepts'
  if (type === 'SUGGEST_DESIGN_LENSES') return 'Design lenses'
  if (type === 'SUGGEST_PROJECT_APPLICATIONS') return 'Project applications'
  return 'Forum thread draft'
}

function jsonValidationWarnings(draft: AISuggestionRecord) {
  try {
    const parsed = JSON.parse(draft.draftJson) as { validationWarnings?: string[]; warnings?: string[] }
    return parsed.validationWarnings ?? parsed.warnings ?? []
  } catch {
    return ['Draft JSON could not be parsed in the browser.']
  }
}

function priorityLabel(priority: ActionItemPriority) {
  if (priority === 'HIGH') return 'High'
  if (priority === 'MEDIUM') return 'Medium'
  return 'Low'
}

function priorityVariant(priority: ActionItemPriority) {
  if (priority === 'HIGH') return 'danger'
  if (priority === 'MEDIUM') return 'warning'
  return 'info'
}

function formatDate(value: string | null | undefined) {
  if (!value) return 'Not tracked yet'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(date)
}
</script>

<style scoped>
.right-rail {
  min-width: 0;
  width: 100%;
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.source-card,
.source-card__topline,
.draft-list,
.draft-card,
.draft-disabled,
.draft-disabled__status,
.draft-card__warnings,
.ai-task-row,
.action-list,
.rail-empty,
.rail-starter,
.rail-starter__actions {
  display: grid;
  gap: var(--space-3);
}

.rail-starter p {
  margin: 0;
  color: var(--bookos-text-secondary);
}

.rail-starter strong {
  color: var(--bookos-text-primary);
  font-size: 1rem;
}

.rail-starter__actions {
  grid-template-columns: 1fr;
}

.source-card__topline {
  justify-items: start;
}

.source-card__book {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: var(--space-3);
  align-items: center;
}

.source-card__book img,
.source-card__cover-fallback {
  width: 64px;
  aspect-ratio: 2 / 3;
  border-radius: var(--radius-md);
  box-shadow: 0 12px 24px rgba(54, 42, 24, 0.14);
}

.source-card__book img {
  object-fit: cover;
}

.source-card__cover-fallback {
  display: grid;
  place-items: center;
  background: linear-gradient(160deg, var(--bookos-primary), var(--bookos-accent));
  color: #fffdf8;
  font-weight: 900;
}

.source-card__identity {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
}

.source-card__identity strong {
  color: var(--bookos-text-primary);
  font-size: 1rem;
  line-height: 1.15;
}

.source-card__identity span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-card__excerpt {
  margin: 0;
  padding: var(--space-3);
  border-left: 4px solid var(--bookos-accent);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-accent-soft) 42%, var(--bookos-surface));
}

.source-card__excerpt p {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  line-height: 1.4;
}

.source-card__meta {
  margin: 0;
  display: grid;
  gap: var(--space-2);
}

.source-card__meta div {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: var(--space-2);
}

.source-card__meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.source-card__meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
  font-weight: 700;
  overflow-wrap: anywhere;
}

.draft-card,
.action-item {
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
}

.draft-card__topline {
  display: flex;
  justify-content: space-between;
  gap: var(--space-2);
  align-items: center;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 800;
}

.draft-card p,
.draft-disabled p,
.rail-empty p {
  margin: 0;
  color: var(--bookos-text-secondary);
}

.draft-disabled__status {
  grid-template-columns: repeat(auto-fit, minmax(130px, max-content));
  align-items: center;
}

.ai-task-row {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
}

.ai-task-row__field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.ai-task-row__field :deep(.el-select) {
  width: 100%;
}

.draft-card__editor {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-weight: 800;
}

.draft-card__warnings {
  padding: var(--space-2);
  border: 1px solid color-mix(in srgb, var(--bookos-warning) 28%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: var(--bookos-warning-soft);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.draft-card__warnings strong {
  color: var(--bookos-text-primary);
}

.right-rail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.rail-empty {
  padding: var(--space-3);
  border: 1px dashed var(--bookos-border-strong);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-surface-muted) 58%, var(--bookos-surface));
}

.rail-empty strong {
  color: var(--bookos-text-primary);
}

.action-item {
  min-height: 56px;
  display: grid;
  gap: var(--space-3);
}

.action-item__text {
  color: var(--bookos-text-primary);
  font-weight: 700;
}

.action-item__body {
  display: grid;
  gap: var(--space-1);
}

.action-item__source {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.action-item__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.action-item--complete .action-item__text {
  color: var(--bookos-text-tertiary);
  text-decoration: line-through;
}

.rail-confirm {
  position: fixed;
  z-index: 120;
  inset: 0;
  padding: var(--space-4);
  display: grid;
  place-items: center;
  background: rgba(27, 33, 30, 0.38);
}

.rail-confirm__panel {
  width: min(420px, 100%);
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-xl);
  background: var(--bookos-surface);
  box-shadow: var(--shadow-card-hover);
}

.rail-confirm h2,
.rail-confirm p {
  margin: 0;
}

@media (max-width: 1280px) {
  .right-rail {
    grid-template-columns: repeat(auto-fit, minmax(min(100%, 280px), 1fr));
    align-items: start;
  }
}

@media (max-width: 900px) {
  .right-rail {
    grid-template-columns: 1fr;
  }

  .ai-task-row {
    grid-template-columns: 1fr;
  }
}
</style>
