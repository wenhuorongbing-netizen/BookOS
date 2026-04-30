import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { completeActionItem, reopenActionItem } from '../api/actionItems'
import type { ActionItemRecord, BookRecord, QuoteRecord, SourceReferenceRecord } from '../types'

export type RailSourceType = 'book' | 'quote' | 'note' | 'daily-sentence' | 'action-item'
export type AiDraftStatus = 'DRAFT' | 'ACCEPTED' | 'DISCARDED'
export type ActionItemPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface RailSourceReference {
  id: string
  type: RailSourceType
  entityId?: number | string | null
  sourceReferenceId?: number | string | null
  bookId: number
  bookTitle: string
  subtitle?: string | null
  author?: string | null
  coverUrl?: string | null
  chapter?: string | null
  pageRange?: string | null
  location?: string | null
  addedAt?: string | null
  excerpt?: string | null
}

export interface AiDraftSuggestion {
  id: string
  text: string
  status: AiDraftStatus
  generatedAt?: string | null
}

export interface ExtractedActionItem {
  id: string
  bookId: number
  bookTitle: string
  text: string
  priority: ActionItemPriority
  completed: boolean
  sourceId?: string | null
  pageRange?: string | null
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
      entityId: book.id,
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

  function setSourceFromQuote(quote: QuoteRecord, book?: BookRecord) {
    setSourceReference({
      id: `quote-${quote.id}`,
      type: 'quote',
      entityId: quote.id,
      sourceReferenceId: quote.sourceReference?.id ?? null,
      bookId: quote.bookId,
      bookTitle: quote.bookTitle,
      subtitle: book?.subtitle,
      author: book?.authors.join(', ') || quote.attribution,
      coverUrl: book?.coverImageUrl ?? book?.coverUrl,
      pageRange: buildPageRangeFromValues(quote.pageStart, quote.pageEnd),
      location: quote.sourceReference?.locationLabel ?? 'Quote',
      addedAt: quote.createdAt,
      excerpt: quote.text,
    })
  }

  function setSourceFromActionItem(item: ActionItemRecord, book?: BookRecord) {
    setSourceReference({
      id: `action-${item.id}`,
      type: 'action-item',
      entityId: item.id,
      sourceReferenceId: item.sourceReference?.id ?? null,
      bookId: item.bookId,
      bookTitle: item.bookTitle,
      subtitle: book?.subtitle,
      author: book?.authors.join(', ') || null,
      coverUrl: book?.coverImageUrl ?? book?.coverUrl,
      pageRange: buildPageRangeFromValues(item.pageStart, item.pageEnd),
      location: item.sourceReference?.locationLabel ?? 'Action item',
      addedAt: item.createdAt,
      excerpt: item.description || item.title,
    })
  }

  function setSourceFromReference(source: SourceReferenceRecord, bookTitle: string, book?: BookRecord) {
    setSourceReference({
      id: `source-${source.id}`,
      type: source.rawCaptureId || source.noteId || source.noteBlockId ? 'note' : 'book',
      entityId: source.id,
      sourceReferenceId: source.id,
      bookId: source.bookId,
      bookTitle,
      subtitle: book?.subtitle,
      author: book?.authors.join(', ') || null,
      coverUrl: book?.coverImageUrl ?? book?.coverUrl,
      pageRange: buildPageRangeFromValues(source.pageStart, source.pageEnd),
      location: source.locationLabel ?? source.sourceType,
      excerpt: source.sourceText,
    })
  }

  function clearSourceForBook(bookId: number) {
    if (sourceReference.value?.bookId === bookId) {
      sourceReference.value = null
    }
  }

  function setActionItemsFromRecords(records: ActionItemRecord[]) {
    actionItems.value = records.map(toRailActionItem)
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

  async function toggleActionItem(id: string) {
    const item = actionItems.value.find((entry) => entry.id === id)
    if (item) item.completed = !item.completed
    const numericId = Number(id)
    if (!item || Number.isNaN(numericId)) return

    try {
      const updated = item.completed ? await completeActionItem(numericId) : await reopenActionItem(numericId)
      const index = actionItems.value.findIndex((entry) => entry.id === id)
      if (index >= 0) {
        actionItems.value[index] = toRailActionItem(updated)
      }
    } catch (error) {
      item.completed = !item.completed
      throw error
    }
  }

  function resetPrivateState() {
    sourceReference.value = null
    aiDrafts.value = []
    actionItems.value = []
  }

  return {
    sourceReference,
    aiDrafts,
    visibleAiDrafts,
    actionItems,
    setSourceReference,
    setSourceFromQuote,
    setSourceFromActionItem,
    setSourceFromReference,
    setCurrentBookSource,
    clearSourceForBook,
    setActionItemsFromRecords,
    updateAiDraftText,
    acceptAiDraft,
    discardAiDraft,
    toggleActionItem,
    resetPrivateState,
  }
})

function buildPageRange(book: BookRecord) {
  if (typeof book.currentPage === 'number' && typeof book.totalPages === 'number') {
    return `p.${book.currentPage} of ${book.totalPages}`
  }

  return book.pageRange ?? null
}

function buildPageRangeFromValues(pageStart: number | null, pageEnd: number | null) {
  if (pageStart && pageEnd) return `p.${pageStart}-${pageEnd}`
  if (pageStart) return `p.${pageStart}`
  return null
}

function toRailActionItem(item: ActionItemRecord): ExtractedActionItem {
  return {
    id: String(item.id),
    bookId: item.bookId,
    bookTitle: item.bookTitle,
    text: item.title,
    priority: item.priority,
    completed: item.completed,
    sourceId: item.sourceReference ? String(item.sourceReference.id) : null,
    pageRange: buildPageRangeFromValues(item.pageStart, item.pageEnd),
  }
}
