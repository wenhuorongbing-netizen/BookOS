<template>
  <aside class="right-rail" aria-label="Contextual sidebar">
    <ContextPanel eyebrow="Source Reference" title="Current source">
      <div v-if="source" class="source-card">
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
        <p>Open a book, quote, note, or action item to pin its source here.</p>
      </div>
    </ContextPanel>

    <ContextPanel eyebrow="AI Draft Suggestions" title="Draft assistance">
      <div v-if="visibleDrafts.length" class="draft-list">
        <article v-for="draft in visibleDrafts" :key="draft.id" class="draft-card">
          <div class="draft-card__topline">
            <AppBadge variant="warning" size="sm">Draft</AppBadge>
            <span>{{ formatDate(draft.generatedAt) }}</span>
          </div>

          <label v-if="editingDraftId === draft.id" class="draft-card__editor">
            <span>Edit draft suggestion</span>
            <el-input v-model="draftEditText" type="textarea" :rows="4" />
          </label>
          <p v-else>{{ draft.text }}</p>

          <div class="right-rail__actions" :aria-label="`Actions for draft suggestion ${draft.id}`">
            <template v-if="editingDraftId === draft.id">
              <AppButton variant="primary" @click="saveDraftEdit(draft.id)">Save edit</AppButton>
              <AppButton variant="ghost" @click="cancelDraftEdit">Cancel</AppButton>
            </template>
            <template v-else>
              <AppButton variant="secondary" @click="pendingAcceptDraft = draft">Accept</AppButton>
              <AppButton variant="ghost" @click="startDraftEdit(draft)">Edit</AppButton>
              <AppButton variant="text" @click="discardDraft(draft.id)">Discard</AppButton>
            </template>
          </div>
        </article>
      </div>

      <div v-else class="rail-empty">
        <strong>No AI drafts available</strong>
        <p>Suggestions will appear here as drafts only. They will not overwrite your notes unless accepted.</p>
      </div>
    </ContextPanel>

    <ContextPanel eyebrow="Action Items Extracted" title="Next actions">
      <div v-if="actionItems.length" class="action-list">
        <label v-for="item in actionItems" :key="item.id" class="action-item" :class="{ 'action-item--complete': item.completed }">
          <input type="checkbox" :checked="item.completed" @change="toggleActionItem(item.id)" />
          <span class="action-item__text">{{ item.text }}</span>
          <AppBadge :variant="priorityVariant(item.priority)" size="sm">{{ priorityLabel(item.priority) }}</AppBadge>
        </label>
      </div>

      <div v-else class="rail-empty">
        <strong>No extracted action items yet</strong>
        <p>Action items from quotes, notes, and AI-reviewed drafts will be listed here.</p>
      </div>

      <AppButton variant="secondary" @click="openActionItems">View all action items</AppButton>
    </ContextPanel>

    <div
      v-if="pendingAcceptDraft"
      class="rail-confirm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="accept-draft-title"
      @keydown.escape="pendingAcceptDraft = null"
    >
      <div ref="confirmPanel" class="rail-confirm__panel" tabindex="-1">
        <h2 id="accept-draft-title">Accept AI draft?</h2>
        <p>This will only mark the suggestion as accepted in the local draft state. It will not overwrite an existing note.</p>
        <div class="right-rail__actions">
          <AppButton variant="primary" @click="confirmAcceptDraft">Accept draft</AppButton>
          <AppButton variant="ghost" @click="pendingAcceptDraft = null">Cancel</AppButton>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import ContextPanel from './ContextPanel.vue'
import AppBadge from './ui/AppBadge.vue'
import AppButton from './ui/AppButton.vue'
import { useRightRailStore, type ActionItemPriority, type AiDraftSuggestion } from '../stores/rightRail'

const router = useRouter()
const rail = useRightRailStore()

const editingDraftId = ref<string | null>(null)
const draftEditText = ref('')
const pendingAcceptDraft = ref<AiDraftSuggestion | null>(null)
const confirmPanel = ref<HTMLElement | null>(null)

const source = computed(() => rail.sourceReference)
const visibleDrafts = computed(() => rail.visibleAiDrafts)
const actionItems = computed(() => rail.actionItems)

watch(pendingAcceptDraft, async (draft) => {
  if (!draft) return
  await nextTick()
  confirmPanel.value?.focus()
})

function openSource() {
  if (!source.value) return
  const hash = source.value.type === 'note' ? '#recent-note-blocks-title' : '#library-state'
  router.push({ name: 'book-detail', params: { id: source.value.bookId }, hash })
}

function openActionItems() {
  router.push({ name: 'dashboard', query: { focus: 'action-items', bookId: source.value?.bookId ? String(source.value.bookId) : undefined } })
}

function startDraftEdit(draft: AiDraftSuggestion) {
  editingDraftId.value = draft.id
  draftEditText.value = draft.text
}

function saveDraftEdit(id: string) {
  rail.updateAiDraftText(id, draftEditText.value)
  cancelDraftEdit()
  ElMessage.success('Draft suggestion updated locally.')
}

function cancelDraftEdit() {
  editingDraftId.value = null
  draftEditText.value = ''
}

function confirmAcceptDraft() {
  if (!pendingAcceptDraft.value) return
  rail.acceptAiDraft(pendingAcceptDraft.value.id)
  pendingAcceptDraft.value = null
  ElMessage.success('Draft suggestion accepted locally.')
}

function discardDraft(id: string) {
  rail.discardAiDraft(id)
  ElMessage.success('Draft suggestion discarded locally.')
}

function toggleActionItem(id: string) {
  rail.toggleActionItem(id)
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
.draft-list,
.draft-card,
.action-list,
.rail-empty {
  display: grid;
  gap: var(--space-3);
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
.rail-empty p {
  margin: 0;
  color: var(--bookos-text-secondary);
}

.draft-card__editor {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-weight: 800;
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
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--space-2);
  align-items: center;
  cursor: pointer;
}

.action-item input {
  width: 18px;
  height: 18px;
  accent-color: var(--bookos-primary);
}

.action-item__text {
  color: var(--bookos-text-primary);
  font-weight: 700;
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
}
</style>
