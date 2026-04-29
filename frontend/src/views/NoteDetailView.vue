<template>
  <div class="page-shell note-detail-page">
    <AppLoadingState v-if="loading" label="Loading note" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Note could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadNote"
    />

    <template v-else-if="note">
      <AppSectionHeader
        :title="note.title"
        eyebrow="Source-backed note"
        :description="`Attached to ${note.bookTitle}.`"
        :level="1"
      >
        <template #actions>
          <RouterLink :to="{ name: 'book-notes', params: { bookId: note.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Notes</AppButton>
          </RouterLink>
          <RouterLink :to="{ name: 'book-detail', params: { id: note.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Open Book</AppButton>
          </RouterLink>
          <RouterLink :to="{ name: 'graph-book', params: { bookId: note.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Graph Context</AppButton>
          </RouterLink>
          <RouterLink :to="forumThreadLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
          </RouterLink>
        </template>
      </AppSectionHeader>

      <section class="note-detail-grid" aria-label="Note editor and parsed blocks">
        <AppCard class="note-editor" as="section">
          <AppSectionHeader
            title="Edit Markdown Note"
            eyebrow="User content"
            description="Saving updates the note summary, but existing block edits remain explicit."
            :level="2"
            compact
          />

          <form class="note-editor__form" @submit.prevent="handleSaveNote">
            <label class="field">
              <span>Title</span>
              <el-input v-model="editForm.title" maxlength="220" show-word-limit />
            </label>

            <label class="field">
              <span>Markdown</span>
              <div class="markdown-split">
                <el-input v-model="editForm.markdown" type="textarea" :rows="12" maxlength="50000" />
                <div class="markdown-preview" aria-label="Sanitized markdown preview">
                  <div class="markdown-preview__label">Preview</div>
                  <div v-if="editForm.markdown.trim()" v-html="renderedMarkdown" />
                  <p v-else class="muted">Markdown preview appears as you type.</p>
                </div>
              </div>
            </label>

            <label class="field">
              <span>Three-sentence summary</span>
              <el-input
                v-model="editForm.threeSentenceSummary"
                type="textarea"
                :rows="3"
                maxlength="1000"
                show-word-limit
                placeholder="Optional. Leave blank to derive from markdown."
              />
            </label>

            <div class="note-editor__actions">
              <AppButton variant="danger" @click="handleArchive">Archive Note</AppButton>
              <AppButton variant="primary" native-type="submit" :loading="savingNote" :disabled="!canSaveNote">Save Note</AppButton>
            </div>
          </form>
        </AppCard>

        <AppCard class="block-composer" as="section">
          <AppSectionHeader
            title="Add Parsed Block"
            eyebrow="Atomic block"
            description="Blocks preserve their own parser result and source reference."
            :level="2"
            compact
          />

          <form class="block-composer__form" @submit.prevent="handleAddBlock">
            <label class="field">
              <span>Raw block</span>
              <el-input
                v-model="blockForm.rawText"
                type="textarea"
                :rows="5"
                maxlength="5000"
                placeholder="&#x2705; &#x7B2C;80&#x9875; Test this design method tomorrow. #todo [[Feedback Loop]]"
              />
            </label>
            <AppButton variant="primary" native-type="submit" :loading="addingBlock" :disabled="!blockForm.rawText.trim()">
              Add Block
            </AppButton>
          </form>
        </AppCard>
      </section>

      <AppCard class="blocks-panel" as="section">
        <AppSectionHeader
          title="Parsed Note Blocks"
          eyebrow="Source references"
          description="Each block keeps raw text, parsed type, page data, concepts, tags, and source linkage."
          :level="2"
          compact
        />

        <AppEmptyState
          v-if="!note.blocks.length"
          title="No parsed blocks"
          description="Add a block to create an atomic, source-backed note fragment."
          compact
        />

        <template v-else>
          <article
            v-for="block in sortedBlocks"
            :id="`block-${block.id}`"
            :key="block.id"
            class="block-card"
            tabindex="0"
            :aria-label="`${formatType(block.blockType)} block ${pageLabel(block.pageStart, block.pageEnd)}`"
            @focus="setRailSource(block)"
            @click="setRailSource(block)"
          >
            <div class="block-card__header">
              <div class="block-card__badges">
                <AppBadge variant="primary">{{ formatType(block.blockType) }}</AppBadge>
                <AppBadge variant="accent">{{ pageLabel(block.pageStart, block.pageEnd) }}</AppBadge>
                <AppBadge v-if="block.sourceReferences[0]" variant="success" size="sm">
                  Source {{ block.sourceReferences[0].sourceConfidence }}
                </AppBadge>
              </div>
              <AppButton variant="danger" :disabled="deletingBlockId === block.id" @click.stop="handleDeleteBlock(block.id)">
                Delete
              </AppButton>
              <AppButton variant="secondary" @click.stop="openBlockSource(block)">Open Source</AppButton>
            </div>

            <p class="block-card__text">{{ block.plainText || block.rawText }}</p>
            <div v-if="parsedConcepts(block).length" class="block-card__concepts" aria-label="Parsed concepts">
              <AppBadge v-for="concept in parsedConcepts(block)" :key="`${block.id}-${concept}`" variant="info" size="sm">
                [[{{ concept }}]]
              </AppBadge>
              <AppButton variant="accent" @click.stop="openConceptReview(block)">Review Concepts</AppButton>
            </div>
            <details class="block-card__details">
              <summary>Raw and source data</summary>
              <pre>{{ block.rawText }}</pre>
              <dl>
                <div v-for="source in block.sourceReferences" :key="source.id">
                  <dt>{{ source.locationLabel ?? 'Source reference' }}</dt>
                  <dd>{{ source.sourceText ?? 'No source text stored.' }}</dd>
                </div>
              </dl>
            </details>

            <form class="block-card__edit" @submit.prevent="handleUpdateBlock(block)">
              <label class="field">
                <span>Edit block raw text</span>
                <el-input v-model="blockEdits[block.id]" type="textarea" :rows="3" maxlength="5000" />
              </label>
              <AppButton variant="secondary" native-type="submit" :loading="updatingBlockId === block.id">Update Block</AppButton>
            </form>
          </article>
        </template>
      </AppCard>

      <BacklinksSection
        entity-type="NOTE"
        :entity-id="note.id"
        :source-references="noteSourceReferences"
        :book-title="note.bookTitle"
      />

      <ConceptReviewDialog
        v-model="conceptDialogOpen"
        :parsed-concepts="selectedConceptBlock ? parsedConcepts(selectedConceptBlock) : []"
        :concepts="conceptOptions"
        :source-reference="selectedConceptBlock?.sourceReferences[0] ?? null"
        :saving="savingConceptReview"
        @submit="saveNoteConceptReview"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { createKnowledgeObject, createConcept, getConcepts, updateConcept } from '../api/knowledge'
import { addNoteBlock, archiveNote, deleteNoteBlock, getNote, updateNote, updateNoteBlock } from '../api/notes'
import ConceptReviewDialog from '../components/concept/ConceptReviewDialog.vue'
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
import { renderSafeMarkdown } from '../utils/markdown'
import type { BookNoteRecord, ConceptRecord, ConceptReviewPayload, NoteBlockRecord, NoteBlockType, Visibility } from '../types'

const route = useRoute()
const router = useRouter()
const rightRail = useRightRailStore()
const { openSource } = useOpenSource()

const note = ref<BookNoteRecord | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const savingNote = ref(false)
const addingBlock = ref(false)
const deletingBlockId = ref<number | null>(null)
const updatingBlockId = ref<number | null>(null)
const conceptDialogOpen = ref(false)
const selectedConceptBlock = ref<NoteBlockRecord | null>(null)
const conceptOptions = ref<ConceptRecord[]>([])
const savingConceptReview = ref(false)
const blockEdits = reactive<Record<number, string>>({})

const editForm = reactive({
  title: '',
  markdown: '',
  threeSentenceSummary: '',
  visibility: 'PRIVATE' as Visibility,
})

const blockForm = reactive({
  rawText: '',
})

const sortedBlocks = computed(() => {
  return [...(note.value?.blocks ?? [])].sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
})
const noteSourceReferences = computed(() => note.value?.blocks.flatMap((block) => block.sourceReferences) ?? [])
const canSaveNote = computed(() => Boolean(editForm.title.trim() && editForm.markdown.trim()))
const renderedMarkdown = computed(() => renderSafeMarkdown(editForm.markdown))
const forumThreadLink = computed(() => {
  if (!note.value) return { name: 'forum-new' }
  const firstSource = noteSourceReferences.value[0] ?? null
  return {
    name: 'forum-new',
    query: {
      relatedEntityType: 'NOTE',
      relatedEntityId: String(note.value.id),
      bookId: String(note.value.bookId),
      sourceReferenceId: firstSource ? String(firstSource.id) : undefined,
      title: `Discuss note: ${note.value.title}`,
    },
  }
})

onMounted(loadNote)

async function loadNote() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getNote(String(route.params.id))
    note.value = result
    editForm.title = result.title
    editForm.markdown = result.markdown
    editForm.threeSentenceSummary = result.threeSentenceSummary ?? ''
    editForm.visibility = result.visibility
    result.blocks.forEach((block) => {
      blockEdits[block.id] = block.rawText
    })
    await loadConceptOptions()
    if (result.blocks[0]) setRailSource(result.blocks[0])
  } catch {
    note.value = null
    errorMessage.value = 'Check your connection or permissions and try again.'
  } finally {
    loading.value = false
  }
}

async function handleSaveNote() {
  if (!note.value || !canSaveNote.value) return
  savingNote.value = true
  try {
    note.value = await updateNote(note.value.id, {
      title: editForm.title.trim(),
      markdown: editForm.markdown.trim(),
      visibility: editForm.visibility,
      threeSentenceSummary: editForm.threeSentenceSummary.trim() || null,
    })
    ElMessage.success('Note updated.')
  } catch {
    ElMessage.error('Note update failed.')
  } finally {
    savingNote.value = false
  }
}

async function handleArchive() {
  if (!note.value) return
  try {
    await ElMessageBox.confirm('Archive this note? It will be removed from active book notes.', 'Archive note', {
      confirmButtonText: 'Archive',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
  } catch {
    return
  }
  await archiveNote(note.value.id)
  ElMessage.success('Note archived.')
  router.push({ name: 'book-notes', params: { bookId: note.value.bookId } })
}

async function handleAddBlock() {
  if (!note.value || !blockForm.rawText.trim()) return
  addingBlock.value = true
  try {
    const block = await addNoteBlock(note.value.id, {
      rawText: blockForm.rawText.trim(),
      sortOrder: note.value.blocks.length + 1,
    })
    note.value.blocks = [...note.value.blocks, block]
    blockEdits[block.id] = block.rawText
    blockForm.rawText = ''
    setRailSource(block)
    ElMessage.success('Parsed block added.')
  } catch {
    ElMessage.error('Block could not be added.')
  } finally {
    addingBlock.value = false
  }
}

async function handleUpdateBlock(block: NoteBlockRecord) {
  const rawText = blockEdits[block.id]?.trim()
  if (!rawText || !note.value) return
  updatingBlockId.value = block.id
  try {
    const updated = await updateNoteBlock(block.id, { rawText, sortOrder: block.sortOrder })
    note.value.blocks = note.value.blocks.map((entry) => (entry.id === updated.id ? updated : entry))
    blockEdits[updated.id] = updated.rawText
    setRailSource(updated)
    ElMessage.success('Block updated.')
  } catch {
    ElMessage.error('Block update failed.')
  } finally {
    updatingBlockId.value = null
  }
}

async function handleDeleteBlock(blockId: number) {
  if (!note.value) return
  deletingBlockId.value = blockId
  try {
    await deleteNoteBlock(blockId)
    note.value.blocks = note.value.blocks.filter((block) => block.id !== blockId)
    delete blockEdits[blockId]
    ElMessage.success('Block deleted.')
  } catch {
    ElMessage.error('Block delete failed.')
  } finally {
    deletingBlockId.value = null
  }
}

function setRailSource(block: NoteBlockRecord) {
  if (!note.value) return
  rightRail.setSourceReference({
    id: `note-block-${block.id}`,
    type: 'note',
    entityId: block.id,
    bookId: note.value.bookId,
    bookTitle: note.value.bookTitle,
    pageRange: pageLabel(block.pageStart, block.pageEnd),
    location: block.sourceReferences[0]?.locationLabel ?? null,
    addedAt: block.createdAt,
    excerpt: block.plainText || block.rawText,
  })
}

function openBlockSource(block: NoteBlockRecord) {
  if (!note.value) return
  setRailSource(block)
  const sourceReference = block.sourceReferences[0] ?? null
  void openSource({
    sourceType: 'NOTE_BLOCK',
    sourceId: block.id,
    bookId: note.value.bookId,
    bookTitle: note.value.bookTitle,
    pageStart: block.pageStart,
    noteId: note.value.id,
    noteBlockId: block.id,
    sourceReference,
    sourceReferenceId: sourceReference?.id ?? null,
  })
}

function openConceptReview(block: NoteBlockRecord) {
  selectedConceptBlock.value = block
  conceptDialogOpen.value = true
}

async function saveNoteConceptReview(payload: ConceptReviewPayload) {
  if (!note.value || !selectedConceptBlock.value) return
  const sourceReference = selectedConceptBlock.value.sourceReferences[0] ?? null
  if (!sourceReference) {
    ElMessage.warning('This note block has no source reference to attach concepts to.')
    return
  }

  savingConceptReview.value = true
  try {
    for (const item of payload.concepts) {
      if (item.action === 'SKIP') continue
      const concept = item.existingConceptId
        ? await updateConcept(item.existingConceptId, {
            name: conceptOptions.value.find((entry) => entry.id === item.existingConceptId)?.name ?? item.finalName ?? item.rawName,
            description: conceptOptions.value.find((entry) => entry.id === item.existingConceptId)?.description ?? null,
            visibility: conceptOptions.value.find((entry) => entry.id === item.existingConceptId)?.visibility ?? 'PRIVATE',
            bookId: note.value.bookId,
            sourceReferenceId: sourceReference.id,
          })
        : await createConcept({
            name: item.finalName?.trim() || item.rawName,
            visibility: 'PRIVATE',
            bookId: note.value.bookId,
            sourceReferenceId: sourceReference.id,
          })

      try {
        await createKnowledgeObject({
          type: 'CONCEPT',
          title: concept.name,
          visibility: 'PRIVATE',
          bookId: note.value.bookId,
          noteId: note.value.id,
          conceptId: concept.id,
          sourceReferenceId: sourceReference.id,
          tags: item.tags ?? [],
        })
      } catch {
        // Existing concept knowledge objects are safe to leave unchanged.
      }
    }
    await loadConceptOptions()
    conceptDialogOpen.value = false
    ElMessage.success('Note concepts reviewed and linked to source.')
  } catch {
    ElMessage.error('Concept review failed.')
  } finally {
    savingConceptReview.value = false
  }
}

async function loadConceptOptions() {
  conceptOptions.value = await getConcepts()
}

function parsedConcepts(block: NoteBlockRecord) {
  return [...block.rawText.matchAll(/\[\[([^\]]+)\]\]/g)]
    .map((match) => match[1]?.trim())
    .filter(Boolean)
    .filter((value, index, all) => all.findIndex((entry) => entry.toLowerCase() === value.toLowerCase()) === index)
}

function formatType(type: NoteBlockType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function pageLabel(pageStart: number | null, pageEnd: number | null) {
  if (pageStart && pageEnd) return `p.${pageStart}-${pageEnd}`
  if (pageStart) return `p.${pageStart}`
  return 'No page'
}
</script>

<style scoped>
.note-detail-page {
  display: grid;
  gap: var(--space-5);
}

.note-detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 0.62fr);
  gap: var(--space-4);
  align-items: start;
}

.note-editor,
.block-composer,
.blocks-panel {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.note-editor__form,
.block-composer__form,
.field,
.block-card__edit {
  display: grid;
  gap: var(--space-2);
}

.field span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.muted {
  margin: 0;
  color: var(--bookos-text-tertiary);
}

.markdown-split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 0.86fr);
  gap: var(--space-3);
  align-items: stretch;
}

.markdown-preview {
  min-height: 280px;
  padding: var(--space-4);
  overflow: auto;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.markdown-preview__label {
  margin-bottom: var(--space-2);
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
  margin: var(--space-3) 0 var(--space-2);
  color: var(--bookos-text-primary);
}

.markdown-preview :deep(p) {
  margin: 0 0 var(--space-2);
}

.note-editor__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.block-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.block-card:focus-visible {
  outline: 2px solid var(--bookos-focus);
  outline-offset: 3px;
}

.block-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.block-card__badges {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.block-card__concepts {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.block-card__text {
  margin: 0;
  color: var(--bookos-text-primary);
  line-height: var(--type-body-line);
}

.block-card__details {
  color: var(--bookos-text-secondary);
}

.block-card__details summary {
  min-height: 44px;
  cursor: pointer;
  font-weight: 800;
}

.block-card__details pre {
  margin: var(--space-2) 0;
  padding: var(--space-3);
  overflow-x: auto;
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  white-space: pre-wrap;
}

.block-card__details dl {
  margin: 0;
  display: grid;
  gap: var(--space-2);
}

.block-card__details dt {
  font-weight: 900;
}

.block-card__details dd {
  margin: 0;
}

@media (max-width: 1080px) {
  .note-detail-grid {
    grid-template-columns: 1fr;
  }

  .markdown-split {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .note-editor,
  .block-composer,
  .blocks-panel {
    padding: var(--space-4);
  }

  .note-editor__actions {
    justify-content: stretch;
  }
}
</style>
