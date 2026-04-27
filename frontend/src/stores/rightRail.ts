import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { BookRecord } from '../types'

export type RailSourceType = 'book' | 'quote' | 'note' | 'daily-sentence' | 'action-item'
export type AiDraftStatus = 'DRAFT' | 'ACCEPTED' | 'DISCARDED'
export type ActionItemPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface RailSourceReference {
  id: string
  type: RailSourceType
  bookId: number
  bookTitle: string
  subtitle?: string | null
  author?: string | null
  coverUrl?: string | null
  chapter?: string | null
  pageRange?: string | null
  location?: string | null
  addedAt?: string | null
}

export interface AiDraftSuggestion {
  id: string
  text: string
  status: AiDraftStatus
  generatedAt?: string | null
}

export interface ExtractedActionItem {
  id: string
  text: string
  priority: ActionItemPriority
  completed: boolean
  sourceId?: string | null
}

export const useRightRailStore = defineStore('rightRail', () => {
  const sourceReference = ref<RailSourceReference | null>(null)
  const aiDrafts = ref<AiDraftSuggestion[]>([])
  const actionItems = ref<ExtractedActionItem[]>([])

  const visibleAiDrafts = computed(() => aiDrafts.value.filter((draft) => draft.status === 'DRAFT'))

  function setCurrentBookSource(book: BookRecord) {
    setSourceReference({
      id: `book-${book.id}`,
      type: 'book',
      bookId: book.id,
      bookTitle: book.title,
      subtitle: book.subtitle,
      author: book.authors.join(', ') || 'Unknown author',
      coverUrl: book.coverImageUrl ?? book.coverUrl,
      chapter: book.currentChapter ?? null,
      pageRange: buildPageRange(book),
      location: book.sourceLocation ?? null,
      addedAt: book.sourceAddedAt ?? null,
    })
  }

  function setSourceReference(source: RailSourceReference) {
    sourceReference.value = source
  }

  function clearSourceForBook(bookId: number) {
    if (sourceReference.value?.bookId === bookId) {
      sourceReference.value = null
    }
  }

  function updateAiDraftText(id: string, text: string) {
    const draft = aiDrafts.value.find((item) => item.id === id)
    if (draft) draft.text = text
  }

  function acceptAiDraft(id: string) {
    const draft = aiDrafts.value.find((item) => item.id === id)
    if (draft) draft.status = 'ACCEPTED'
  }

  function discardAiDraft(id: string) {
    const draft = aiDrafts.value.find((item) => item.id === id)
    if (draft) draft.status = 'DISCARDED'
  }

  function toggleActionItem(id: string) {
    const item = actionItems.value.find((entry) => entry.id === id)
    if (item) item.completed = !item.completed
  }

  return {
    sourceReference,
    aiDrafts,
    visibleAiDrafts,
    actionItems,
    setSourceReference,
    setCurrentBookSource,
    clearSourceForBook,
    updateAiDraftText,
    acceptAiDraft,
    discardAiDraft,
    toggleActionItem,
  }
})

function buildPageRange(book: BookRecord) {
  if (typeof book.currentPage === 'number' && typeof book.totalPages === 'number') {
    return `p.${book.currentPage} of ${book.totalPages}`
  }

  return book.pageRange ?? null
}
