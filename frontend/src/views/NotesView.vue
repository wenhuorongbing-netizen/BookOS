<template>
  <div class="page-shell notes-page">
    <AppSectionHeader
      title="Book Notes"
      eyebrow="Phase 3"
      :description="book ? `Structured notes for ${book.title}.` : 'Open a book to create source-backed notes.'"
      :level="1"
    >
      <template #actions>
        <RouterLink to="/my-library" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Open Library</AppButton>
        </RouterLink>
        <RouterLink v-if="book" :to="{ name: 'book-detail', params: { id: book.id } }" custom v-slot="{ navigate }">
          <AppButton variant="ghost" @click="navigate">Back to Book</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppErrorState
      v-if="errorMessage"
      title="Notes could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadPage"
    />

    <AppLoadingState v-else-if="loading" label="Loading notes workspace" />

    <AppEmptyState
      v-else-if="!book"
      title="Choose a book first"
      description="Notes are attached to a specific book so every block can preserve source references."
      eyebrow="Source required"
    >
      <template #actions>
        <RouterLink to="/my-library" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Open Library</AppButton>
        </RouterLink>
      </template>
    </AppEmptyState>

    <template v-else>
      <AppCard class="note-composer" as="section">
        <AppSectionHeader
          title="Write a source-backed note"
          eyebrow="Markdown note"
          description="Emoji markers, page references, tags, and [[Concept]] links are parsed without AI."
          :level="2"
          compact
        />

        <form class="note-composer__form" @submit.prevent="handleCreateNote">
          <label class="field">
            <span>Note title</span>
            <el-input v-model="noteForm.title" maxlength="220" show-word-limit placeholder="e.g. Meaningful choice and feedback loops" />
          </label>

          <label class="field">
            <span>Markdown content</span>
            <el-input
              v-model="noteForm.markdown"
              type="textarea"
              :rows="7"
              maxlength="50000"
              placeholder="💬 p.42 Capture a quote or idea... #quote [[Meaningful Choice]]"
              aria-describedby="notes-parser-help"
            />
          </label>

          <p id="notes-parser-help" class="helper-text">
            Supported markers include quote, action item, question, inspiration, concept, warning, experiment, and reflection.
          </p>

          <div class="note-composer__actions">
            <AppButton variant="secondary" :loading="previewLoading" :disabled="!noteForm.markdown.trim()" @click="handlePreview">
              Preview Parser
            </AppButton>
            <AppButton variant="primary" native-type="submit" :loading="savingNote" :disabled="!canCreateNote">
              Save Note
            </AppButton>
          </div>
        </form>
      </AppCard>

      <section class="notes-grid" aria-label="Notes workspace">
        <AppCard class="parser-panel" as="section">
          <AppSectionHeader title="Parser Preview" eyebrow="Deterministic" :level="2" compact />
          <AppEmptyState
            v-if="!parsedPreview"
            title="No parser preview yet"
            description="Run Preview Parser to inspect type, page, tags, concepts, and source text before saving."
            compact
          />
          <div v-else class="parser-result">
            <div class="parser-result__row">
              <span>Type</span>
              <AppBadge variant="primary">{{ formatType(parsedPreview.type) }}</AppBadge>
            </div>
            <div class="parser-result__row">
              <span>Page</span>
              <strong>{{ pageLabel(parsedPreview.pageStart, parsedPreview.pageEnd) }}</strong>
            </div>
            <div class="parser-result__chips" aria-label="Parsed tags">
              <AppBadge v-for="tag in parsedPreview.tags" :key="tag" variant="accent" size="sm">#{{ tag }}</AppBadge>
              <span v-if="!parsedPreview.tags.length" class="muted">No tags</span>
            </div>
            <div class="parser-result__chips" aria-label="Parsed concepts">
              <AppBadge v-for="concept in parsedPreview.concepts" :key="concept" variant="info" size="sm">[[{{ concept }}]]</AppBadge>
              <span v-if="!parsedPreview.concepts.length" class="muted">No concepts</span>
            </div>
            <p class="parser-result__clean">{{ parsedPreview.cleanText || 'No clean text extracted.' }}</p>
            <ul v-if="parsedPreview.warnings.length" class="parser-result__warnings" aria-label="Parser warnings">
              <li v-for="warning in parsedPreview.warnings" :key="warning">{{ warning }}</li>
            </ul>
          </div>
        </AppCard>

        <AppCard class="notes-list" as="section">
          <AppSectionHeader title="Recent Notes" eyebrow="Source-backed" :level="2" compact />
          <AppEmptyState
            v-if="!notes.length"
            title="No notes yet"
            description="Create your first markdown note to generate parsed note blocks and source references."
            compact
          />
          <template v-else>
            <article v-for="note in notes" :key="note.id" class="note-card">
              <RouterLink :to="{ name: 'note-detail', params: { id: note.id } }" class="note-card__link">
                <div>
                  <h3>{{ note.title }}</h3>
                  <p>{{ note.threeSentenceSummary || note.markdown }}</p>
                </div>
                <div class="note-card__meta">
                  <AppBadge variant="neutral" size="sm">{{ note.blocks.length }} blocks</AppBadge>
                  <AppBadge variant="primary" size="sm">{{ note.visibility }}</AppBadge>
                </div>
              </RouterLink>
              <div class="note-card__blocks" aria-label="Parsed note blocks">
                <button
                  v-for="block in note.blocks.slice(0, 3)"
                  :key="block.id"
                  class="block-chip"
                  type="button"
                  :aria-label="`Open source for ${formatType(block.blockType)} ${pageLabel(block.pageStart, block.pageEnd)}`"
                  @click="openBlockSource(note, block.id)"
                >
                  <span>{{ formatType(block.blockType) }}</span>
                  <strong>{{ pageLabel(block.pageStart, block.pageEnd) }}</strong>
                </button>
              </div>
            </article>
          </template>
        </AppCard>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getBook } from '../api/books'
import { createBookNote, getBookNotes } from '../api/notes'
import { previewParsedNote } from '../api/parser'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useRightRailStore } from '../stores/rightRail'
import type { BookNoteRecord, BookRecord, NoteBlockType, ParsedNoteResult, Visibility } from '../types'

const route = useRoute()
const router = useRouter()
const rightRail = useRightRailStore()

const book = ref<BookRecord | null>(null)
const notes = ref<BookNoteRecord[]>([])
const parsedPreview = ref<ParsedNoteResult | null>(null)
const loading = ref(false)
const savingNote = ref(false)
const previewLoading = ref(false)
const errorMessage = ref('')

const noteForm = reactive({
  title: '',
  markdown: '',
  visibility: 'PRIVATE' as Visibility,
})

const bookId = computed(() => {
  const value = route.params.bookId
  return value ? Number(value) : null
})
const canCreateNote = computed(() => Boolean(noteForm.title.trim() && noteForm.markdown.trim()))

onMounted(loadPage)
onUnmounted(() => {
  if (book.value) rightRail.clearSourceForBook(book.value.id)
})

watch(
  () => route.params.bookId,
  () => {
    void loadPage()
  },
)

async function loadPage() {
  errorMessage.value = ''
  parsedPreview.value = null

  if (!bookId.value || Number.isNaN(bookId.value)) {
    book.value = null
    notes.value = []
    return
  }

  loading.value = true
  try {
    const [bookResult, noteResults] = await Promise.all([getBook(bookId.value), getBookNotes(bookId.value)])
    book.value = bookResult
    notes.value = noteResults
    rightRail.setCurrentBookSource(bookResult)
  } catch {
    book.value = null
    notes.value = []
    errorMessage.value = 'Could not load this book note workspace. Confirm the book is in your library and try again.'
  } finally {
    loading.value = false
  }
}

async function handlePreview() {
  if (!noteForm.markdown.trim()) return
  previewLoading.value = true
  try {
    parsedPreview.value = await previewParsedNote({ rawText: noteForm.markdown })
  } catch {
    ElMessage.error('Parser preview failed.')
  } finally {
    previewLoading.value = false
  }
}

async function handleCreateNote() {
  if (!book.value || !canCreateNote.value) return
  savingNote.value = true
  try {
    const created = await createBookNote(book.value.id, {
      title: noteForm.title.trim(),
      markdown: noteForm.markdown.trim(),
      visibility: noteForm.visibility,
    })
    notes.value = [created, ...notes.value]
    noteForm.title = ''
    noteForm.markdown = ''
    parsedPreview.value = null
    ElMessage.success('Note saved with parsed source reference.')
  } catch {
    ElMessage.error('Note could not be saved. Add this book to your library first if needed.')
  } finally {
    savingNote.value = false
  }
}

function openBlockSource(note: BookNoteRecord, blockId: number) {
  router.push({ name: 'note-detail', params: { id: note.id }, hash: `#block-${blockId}` })
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
.notes-page {
  display: grid;
  gap: var(--space-5);
}

.note-composer,
.parser-panel,
.notes-list {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.note-composer__form,
.field {
  display: grid;
  gap: var(--space-2);
}

.field span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.helper-text,
.muted {
  margin: 0;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
}

.note-composer__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.notes-grid {
  display: grid;
  grid-template-columns: minmax(280px, 0.62fr) minmax(0, 1fr);
  gap: var(--space-4);
  align-items: start;
}

.parser-result {
  display: grid;
  gap: var(--space-3);
}

.parser-result__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.parser-result__row > span {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.parser-result__chips,
.note-card__meta,
.note-card__blocks {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.parser-result__clean {
  margin: 0;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
}

.parser-result__warnings {
  margin: 0;
  padding-left: var(--space-5);
  color: var(--bookos-warning);
}

.note-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.note-card__link {
  display: grid;
  gap: var(--space-3);
  color: inherit;
}

.note-card h3 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
}

.note-card p {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.block-chip {
  min-height: 44px;
  padding: var(--space-2) var(--space-3);
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface);
  color: var(--bookos-text-secondary);
  cursor: pointer;
}

.block-chip strong {
  color: var(--bookos-primary);
}

@media (max-width: 980px) {
  .notes-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .note-composer,
  .parser-panel,
  .notes-list {
    padding: var(--space-4);
  }

  .note-composer__actions {
    justify-content: stretch;
  }
}
</style>
