import { computed, nextTick, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSourceReference, getSourceReferences } from '../api/sourceReferences'
import type { SourceConfidence, SourceReferenceRecord } from '../types'

export interface OpenSourceTarget {
  sourceType: string
  sourceId: number | string
  bookId?: number | string | null
  bookTitle?: string | null
  pageStart?: number | null
  noteId?: number | null
  rawCaptureId?: number | null
  noteBlockId?: number | null
  sourceReferenceId?: number | string | null
  sourceReference?: SourceReferenceRecord | null
  locationLabel?: string | null
  sourceText?: string | null
  sourceConfidence?: SourceConfidence | null
}

const drawerOpen = ref(false)
const drawerLoading = ref(false)
const drawerError = ref('')
const drawerSource = ref<SourceReferenceRecord | null>(null)
const drawerTarget = ref<OpenSourceTarget | null>(null)

export function useOpenSource() {
  const router = useRouter()
  const route = useRoute()

  const relatedEntityType = computed(() => drawerTarget.value?.sourceType?.toUpperCase() ?? null)
  const relatedEntityId = computed(() => drawerTarget.value?.sourceId ?? null)
  const bookLabel = computed(() => {
    const bookTitle = drawerTarget.value?.bookTitle
    if (bookTitle) return bookTitle
    const bookId = drawerSource.value?.bookId ?? drawerTarget.value?.bookId
    return bookId ? `Book #${bookId}` : 'Unknown book'
  })

  async function openSource(target: OpenSourceTarget) {
    drawerTarget.value = target
    drawerOpen.value = true
    drawerError.value = ''
    drawerSource.value = target.sourceReference ?? null

    await loadDrawerSource(target)
    await navigateToSource(target)
  }

  function closeSourceDrawer() {
    drawerOpen.value = false
  }

  async function loadDrawerSource(target: OpenSourceTarget) {
    if (target.sourceReference) return

    const sourceReferenceId = target.sourceReferenceId ?? (target.sourceType.toUpperCase() === 'SOURCE_REFERENCE' ? target.sourceId : null)
    if (sourceReferenceId) {
      drawerLoading.value = true
      try {
        drawerSource.value = await getSourceReference(sourceReferenceId)
      } catch {
        drawerError.value = 'Source reference could not be loaded.'
      } finally {
        drawerLoading.value = false
      }
      return
    }

    const entityLookup = sourceReferenceLookup(target)
    if (!entityLookup) return

    drawerLoading.value = true
    try {
      const references = await getSourceReferences(entityLookup)
      drawerSource.value = references[0] ?? null
      if (!drawerSource.value) {
        drawerError.value = 'No source reference exists for this entity yet.'
      }
    } catch {
      drawerError.value = 'Source references could not be loaded.'
    } finally {
      drawerLoading.value = false
    }
  }

  async function navigateToSource(target: OpenSourceTarget) {
    const source = drawerSource.value
    const bookId = source?.bookId ?? target.bookId

    if (!bookId) {
      ElMessage.info('Source drawer opened, but no book route is available for this source.')
      return
    }

    const query: Record<string, string> = {}
    Object.entries(route.query).forEach(([key, value]) => {
      if (typeof value === 'string') query[key] = value
    })
    query.sourceType = target.sourceType.toUpperCase()
    query.sourceId = String(target.sourceId)
    query.sourceDrawer = '1'
    const pageStart = source?.pageStart ?? target.pageStart
    if (pageStart) query.pageStart = String(pageStart)

    await router.push({
      name: 'book-detail',
      params: { id: String(bookId) },
      query,
      hash: '#library-state',
    })
    await nextTick()
    highlightCurrentSource()
  }

  return {
    sourceDrawerOpen: drawerOpen,
    sourceDrawerLoading: drawerLoading,
    sourceDrawerError: drawerError,
    sourceDrawerSource: drawerSource,
    sourceDrawerTarget: drawerTarget,
    relatedEntityType,
    relatedEntityId,
    bookLabel,
    openSource,
    closeSourceDrawer,
  }
}

function sourceReferenceLookup(target: OpenSourceTarget) {
  if (target.noteBlockId) return { entityType: 'NOTE_BLOCK', entityId: target.noteBlockId }
  if (target.noteId) return { entityType: 'NOTE', entityId: target.noteId }
  if (target.rawCaptureId) return { entityType: 'RAW_CAPTURE', entityId: target.rawCaptureId }

  const sourceType = target.sourceType.toUpperCase()
  if (sourceType === 'NOTE' || sourceType === 'NOTE_BLOCK' || sourceType === 'RAW_CAPTURE' || sourceType === 'CAPTURE') {
    return { entityType: sourceType, entityId: target.sourceId }
  }

  return null
}

function highlightCurrentSource() {
  const target = document.querySelector<HTMLElement>('#library-state')
  if (!target) return
  target.classList.add('bookos-source-highlight')
  window.setTimeout(() => target.classList.remove('bookos-source-highlight'), 1600)
}
