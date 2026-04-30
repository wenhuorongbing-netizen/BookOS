import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { archiveCapture, convertCapture, createCapture, getCaptureInbox, getCaptures } from '../api/captures'
import type { BookRecord, CaptureStatus, NoteBlockType, RawCaptureRecord, SourceReferenceRecord } from '../types'

export type CaptureMarker = 'text' | 'quote' | 'link' | 'emoji' | 'inspiration' | 'favorite' | 'important' | 'question' | 'idea'

export interface RecentNoteBlock {
  id: string
  captureId: number
  bookId: number
  bookTitle: string
  type: CaptureMarker
  parsedType: NoteBlockType
  title: string
  content: string
  tags: string[]
  concepts: string[]
  page: string | null
  pageStart: number | null
  pageEnd: number | null
  sourceReferences: SourceReferenceRecord[]
  parserWarnings: string[]
  createdAt: string
  bookmarked: boolean
  status: RawCaptureRecord['status']
}

export interface CaptureConvertibleBlock {
  captureId: number
  parsedType: NoteBlockType
  title?: string | null
}

export const useCaptureStore = defineStore('capture', () => {
  const captures = ref<RawCaptureRecord[]>([])
  const loading = ref(false)
  const error = ref('')
  const bookmarkVersion = ref(0)

  const latestBlocks = computed(() => {
    bookmarkVersion.value
    return captures.value.map(toRecentBlock)
  })

  async function loadInbox(bookId?: number | string) {
    await loadCaptures({ bookId, status: 'INBOX' })
  }

  async function loadCaptures(params?: { bookId?: number | string | null; status?: CaptureStatus | null }) {
    loading.value = true
    error.value = ''
    try {
      const bookId = params?.bookId ?? undefined
      const status = params?.status ?? undefined
      captures.value = status === 'INBOX'
        ? await getCaptureInbox(bookId ? { bookId } : undefined)
        : await getCaptures({ bookId, status })
    } catch (loadError) {
      error.value = 'Captures could not be loaded. Check backend availability and permissions.'
      throw loadError
    } finally {
      loading.value = false
    }
  }

  async function loadBookCaptures(bookId: number | string) {
    await loadInbox(bookId)
  }

  async function addCapture(book: BookRecord, content: string, marker: CaptureMarker) {
    const rawText = applyMarker(content, marker)
    const capture = await createCapture({ bookId: book.id, rawText })
    upsertCapture(capture)
    return toRecentBlock(capture)
  }

  async function convertToNote(captureId: number, title?: string) {
    const conversion = await convertCapture(captureId, { targetType: 'NOTE', title })
    removeCapture(captureId)
    return conversion
  }

  async function convertToQuote(captureId: number) {
    const conversion = await convertCapture(captureId, { targetType: 'QUOTE' })
    removeCapture(captureId)
    return conversion
  }

  async function convertToActionItem(captureId: number, title?: string) {
    const normalizedTitle = title?.trim() ? title.trim() : null
    const conversion = await convertCapture(captureId, { targetType: 'ACTION_ITEM', title: normalizedTitle })
    removeCapture(captureId)
    return conversion
  }

  async function convertByParsedType(block: CaptureConvertibleBlock) {
    if (block.parsedType === 'QUOTE') {
      return convertToQuote(block.captureId)
    }

    if (block.parsedType === 'ACTION_ITEM') {
      return convertToActionItem(block.captureId, block.title ?? undefined)
    }

    return convertToNote(block.captureId, block.title ?? undefined)
  }

  async function archive(captureId: number) {
    await archiveCapture(captureId)
    removeCapture(captureId)
  }

  function toggleBookmark(id: string) {
    const captureId = Number(id)
    const capture = captures.value.find((item) => item.id === captureId)
    if (!capture) return
    window.localStorage.setItem(bookmarkKey(captureId), isBookmarked(captureId) ? 'false' : 'true')
    bookmarkVersion.value += 1
  }

  function forBook(bookId: number) {
    return latestBlocks.value.filter((block) => block.bookId === bookId)
  }

  function upsertCapture(capture: RawCaptureRecord) {
    captures.value = [capture, ...captures.value.filter((item) => item.id !== capture.id)]
  }

  function removeCapture(captureId: number) {
    captures.value = captures.value.filter((item) => item.id !== captureId)
  }

  function resetPrivateState() {
    captures.value = []
    loading.value = false
    error.value = ''
    bookmarkVersion.value += 1
    clearCaptureBookmarks()
  }

  return {
    captures,
    loading,
    error,
    latestBlocks,
    loadInbox,
    loadCaptures,
    loadBookCaptures,
    addCapture,
    convertToNote,
    convertToQuote,
    convertToActionItem,
    convertByParsedType,
    archive,
    toggleBookmark,
    forBook,
    resetPrivateState,
  }
})

function toRecentBlock(capture: RawCaptureRecord): RecentNoteBlock {
  return {
    id: String(capture.id),
    captureId: capture.id,
    bookId: capture.bookId,
    bookTitle: capture.bookTitle,
    type: toMarker(capture.parsedType),
    parsedType: capture.parsedType,
    title: makeTitle(capture),
    content: capture.cleanText || capture.rawText,
    tags: [...capture.tags, ...capture.concepts].slice(0, 6),
    concepts: capture.concepts,
    page: pageLabel(capture.pageStart, capture.pageEnd),
    pageStart: capture.pageStart,
    pageEnd: capture.pageEnd,
    sourceReferences: capture.sourceReferences,
    parserWarnings: capture.parserWarnings,
    createdAt: capture.createdAt,
    bookmarked: isBookmarked(capture.id),
    status: capture.status,
  }
}

function applyMarker(content: string, marker: CaptureMarker) {
  const trimmed = content.trim()
  if (marker === 'text' || marker === 'emoji' || startsWithKnownMarker(trimmed)) {
    return trimmed
  }

  const markerPrefix: Record<Exclude<CaptureMarker, 'text' | 'emoji'>, string> = {
    quote: '\uD83D\uDCAC',
    link: '\uD83D\uDD17',
    inspiration: '\uD83D\uDCA1',
    favorite: '\uD83D\uDCCC',
    important: '\uD83D\uDCCC',
    question: '\u2753',
    idea: '\uD83D\uDCA1',
  }

  return `${markerPrefix[marker]} ${trimmed}`
}

function startsWithKnownMarker(value: string) {
  return /^[\u2600-\u27BF]|\uD83C|\uD83D|\uD83E/.test(value)
}

function toMarker(type: NoteBlockType): CaptureMarker {
  if (type === 'QUOTE') return 'quote'
  if (type === 'LINK') return 'link'
  if (type === 'INSPIRATION') return 'inspiration'
  if (type === 'IMPORTANT') return 'important'
  if (type === 'QUESTION') return 'question'
  if (type === 'ACTION_ITEM') return 'favorite'
  if (type === 'IDEA' || type === 'RELATED_CONCEPT') return 'idea'
  return 'text'
}

function makeTitle(capture: RawCaptureRecord) {
  const value = (capture.cleanText || capture.rawText || 'Untitled capture').split(/\r?\n/).find(Boolean) ?? 'Untitled capture'
  return value.length > 72 ? `${value.slice(0, 69)}...` : value
}

function pageLabel(pageStart: number | null, pageEnd: number | null) {
  if (pageStart && pageEnd) return `${pageStart}-${pageEnd}`
  if (pageStart) return String(pageStart)
  return null
}

function bookmarkKey(captureId: number) {
  return `bookos.capture.bookmark.${captureId}`
}

function isBookmarked(captureId: number) {
  return window.localStorage.getItem(bookmarkKey(captureId)) === 'true'
}

function clearCaptureBookmarks() {
  const keys: string[] = []
  for (let index = 0; index < window.localStorage.length; index += 1) {
    const key = window.localStorage.key(index)
    if (key?.startsWith('bookos.capture.bookmark.')) {
      keys.push(key)
    }
  }
  keys.forEach((key) => window.localStorage.removeItem(key))
}
