<template>
  <section class="capture-section" aria-labelledby="quick-capture-title">
    <AppCard as="section" class="quick-capture" variant="highlight">
      <div class="quick-capture__header">
        <div>
          <div class="eyebrow">Quick Capture</div>
          <h2 id="quick-capture-title">Capture while the source is still warm</h2>
        </div>
        <div class="quick-capture__header-actions">
          <AppBadge variant="primary" size="sm">{{ book.title }}</AppBadge>
          <RouterLink :to="{ name: 'capture-inbox', query: { bookId: String(book.id) } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open Inbox</AppButton>
          </RouterLink>
        </div>
      </div>

      <label class="quick-capture__field" for="quick-capture-input">
        <span class="sr-only">Quick capture text</span>
        <el-input
          id="quick-capture-input"
          v-model="draft"
          type="textarea"
          :rows="5"
          placeholder="Capture a thought, quote, or idea..."
          @keydown.ctrl.enter.prevent="submitCapture"
          @keydown.meta.enter.prevent="submitCapture"
        />
      </label>

      <div class="quick-capture__bar">
        <div class="quick-capture__tools" role="toolbar" aria-label="Capture markers">
          <AppIconButton
            v-for="marker in markers"
            :key="marker.value"
            :label="marker.label"
            :tooltip="marker.label"
            :selected="selectedMarker === marker.value"
            variant="ghost"
            @click="selectedMarker = marker.value"
          >
            {{ marker.icon }}
          </AppIconButton>
        </div>

        <div class="quick-capture__submit">
          <span class="quick-capture__shortcut">Ctrl/Cmd + Enter to capture</span>
          <AppButton
            variant="primary"
            aria-label="Submit quick capture"
            :loading="submitting"
            :disabled="!draft.trim()"
            @click="submitCapture"
          >
            Capture
          </AppButton>
        </div>
      </div>

      <p class="quick-capture__feedback" role="status" aria-live="polite">{{ feedback }}</p>
    </AppCard>

    <AppCard as="section" class="recent-notes" variant="default" aria-labelledby="recent-note-blocks-title">
      <div class="recent-notes__header">
        <div>
          <div class="eyebrow">Recent Note Blocks</div>
          <h2 id="recent-note-blocks-title">Captured from this book</h2>
        </div>
        <label class="recent-notes__sort">
          <span>Sort</span>
          <el-select v-model="sortOrder" aria-label="Sort recent note blocks">
            <el-option label="Latest first" value="latest" />
            <el-option label="Oldest first" value="oldest" />
          </el-select>
        </label>
      </div>

      <AppLoadingState v-if="capture.loading" label="Loading capture inbox" />

      <div v-else-if="sortedBlocks.length" class="note-block-list">
        <article v-for="block in sortedBlocks" :key="block.id" class="note-block-card">
          <button class="note-block-card__main" type="button" :aria-label="`Open note block ${block.title}`" @click="openSource(block)">
            <span class="note-block-card__icon" aria-hidden="true">{{ markerIcon(block.type) }}</span>
            <span class="note-block-card__copy">
              <strong>{{ block.title }}</strong>
              <span>{{ block.content }}</span>
            </span>
          </button>

          <div class="note-block-card__meta">
            <button v-if="block.page" class="note-block-card__page" type="button" @click="openSource(block)">p.{{ block.page }}</button>
            <button v-for="tag in block.tags" :key="tag" class="note-block-card__tag" type="button" @click="openTag(tag)">#{{ tag }}</button>
            <span>{{ formatDate(block.createdAt) }}</span>
          </div>

          <div class="note-block-card__actions">
            <AppButton
              variant="secondary"
              :loading="convertingCaptureId === block.captureId"
              @click="convertBlock(block)"
            >
              Convert to Note
            </AppButton>
            <AppButton
              variant="ghost"
              :disabled="archivingCaptureId === block.captureId"
              @click="archiveBlock(block)"
            >
              Archive
            </AppButton>
            <AppIconButton
              :label="block.bookmarked ? `Remove bookmark from ${block.title}` : `Bookmark ${block.title}`"
              :selected="block.bookmarked"
              variant="ghost"
              @click="capture.toggleBookmark(block.id)"
            >
              {{ block.bookmarked ? 'BK' : 'BM' }}
            </AppIconButton>
            <AppIconButton label="More note block actions" variant="ghost" @click="openSource(block)">MO</AppIconButton>
          </div>
        </article>
      </div>

      <AppEmptyState
        v-else
        title="No note blocks yet"
        description="Use Quick Capture above to create a persisted inbox capture for this book."
        compact
      />
    </AppCard>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { onBeforeRouteLeave, RouterLink, useRouter } from 'vue-router'
import type { BookRecord } from '../../types'
import { useCaptureStore, type CaptureMarker, type RecentNoteBlock } from '../../stores/capture'
import { useRightRailStore } from '../../stores/rightRail'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppIconButton from '../ui/AppIconButton.vue'
import AppLoadingState from '../ui/AppLoadingState.vue'

const props = defineProps<{
  book: BookRecord
}>()

const router = useRouter()
const capture = useCaptureStore()
const rightRail = useRightRailStore()
const draft = ref('')
const feedback = ref('')
const selectedMarker = ref<CaptureMarker>('text')
const sortOrder = ref<'latest' | 'oldest'>('latest')
const submitting = ref(false)
const convertingCaptureId = ref<number | null>(null)
const archivingCaptureId = ref<number | null>(null)

const markers: Array<{ value: CaptureMarker; label: string; icon: string }> = [
  { value: 'text', label: 'Text note marker', icon: 'TX' },
  { value: 'quote', label: 'Quote marker', icon: 'QT' },
  { value: 'link', label: 'Link marker', icon: 'LN' },
  { value: 'emoji', label: 'Emoji marker', icon: ':)' },
  { value: 'inspiration', label: 'Inspiration marker', icon: 'IN' },
  { value: 'favorite', label: 'Favorite marker', icon: 'ST' },
  { value: 'important', label: 'Important marker', icon: 'FI' },
  { value: 'question', label: 'Question marker', icon: '?' },
  { value: 'idea', label: 'Lightbulb idea marker', icon: 'LB' },
]

const sortedBlocks = computed(() => {
  const blocks = capture.forBook(props.book.id)
  if (sortOrder.value === 'oldest') {
    return [...blocks].reverse()
  }
  return blocks
})

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  void capture.loadBookCaptures(props.book.id)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(() => {
  if (!draft.value.trim()) return true
  return window.confirm('You have an unsaved quick capture draft. Leave this page and discard it?')
})

watch(
  () => props.book.id,
  (bookId) => {
    void capture.loadBookCaptures(bookId)
  },
)

async function submitCapture() {
  const value = draft.value.trim()
  if (!value) {
    feedback.value = 'Write a thought before capturing.'
    ElMessage.warning(feedback.value)
    return
  }

  submitting.value = true
  try {
    const block = await capture.addCapture(props.book, value, selectedMarker.value)
    draft.value = ''
    feedback.value = `Captured ${markerLabel(block.type)} for ${props.book.title}.`
    ElMessage.success(feedback.value)
    openSource(block, false)
  } catch {
    feedback.value = 'Capture could not be saved. Add this book to your library first if needed.'
    ElMessage.error(feedback.value)
  } finally {
    submitting.value = false
  }
}

function openSource(block: RecentNoteBlock, navigate = true) {
  rightRail.setSourceReference({
    id: block.id,
    type: 'note',
    bookId: block.bookId,
    bookTitle: block.bookTitle,
    subtitle: props.book.subtitle,
    author: props.book.authors.join(', ') || 'Unknown author',
    coverUrl: props.book.coverImageUrl ?? props.book.coverUrl,
    chapter: props.book.currentChapter ?? null,
    pageRange: block.page ? `p.${block.page}` : props.book.pageRange ?? null,
    location: `${markerLabel(block.type)} note block`,
    addedAt: block.createdAt,
  })

  if (navigate) {
    router.push({ name: 'book-detail', params: { id: block.bookId }, hash: '#recent-note-blocks-title' })
  }
}

function openTag(tag: string) {
  router.push({ name: 'dashboard', query: { focus: 'tag', tag, bookId: String(props.book.id) } })
}

async function convertBlock(block: RecentNoteBlock) {
  convertingCaptureId.value = block.captureId
  try {
    const conversion = await capture.convertToNote(block.captureId, block.title)
    ElMessage.success('Capture converted to a formal note.')
    router.push({ name: 'note-detail', params: { id: conversion.targetId } })
  } catch {
    ElMessage.error('Capture conversion failed.')
  } finally {
    convertingCaptureId.value = null
  }
}

async function archiveBlock(block: RecentNoteBlock) {
  archivingCaptureId.value = block.captureId
  try {
    await capture.archive(block.captureId)
    ElMessage.success('Capture archived.')
  } catch {
    ElMessage.error('Capture archive failed.')
  } finally {
    archivingCaptureId.value = null
  }
}

function markerIcon(marker: CaptureMarker) {
  return markers.find((item) => item.value === marker)?.icon ?? 'TX'
}

function markerLabel(marker: CaptureMarker) {
  return markers.find((item) => item.value === marker)?.label.replace(' marker', '') ?? 'Text note'
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (!draft.value.trim()) return
  event.preventDefault()
  event.returnValue = ''
}
</script>

<style scoped>
.capture-section {
  display: grid;
  gap: var(--space-4);
}

.quick-capture,
.recent-notes {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.quick-capture__header,
.recent-notes__header,
.quick-capture__bar,
.note-block-card {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
}

.quick-capture__header,
.recent-notes__header {
  align-items: flex-start;
}

.quick-capture__header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-capture h2,
.recent-notes h2 {
  scroll-margin-top: 112px;
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-section-title);
  line-height: var(--type-title-line);
}

.quick-capture__field {
  display: grid;
}

.quick-capture__bar {
  align-items: center;
  flex-wrap: wrap;
}

.quick-capture__tools {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-capture__submit {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.quick-capture__shortcut,
.quick-capture__feedback,
.recent-notes__sort span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.quick-capture__feedback {
  min-height: 1.4em;
  margin: 0;
}

.recent-notes__sort {
  min-width: 180px;
  display: grid;
  gap: var(--space-2);
}

.note-block-list {
  display: grid;
  gap: var(--space-3);
}

.note-block-card {
  align-items: flex-start;
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  box-shadow: 0 10px 24px rgba(54, 42, 24, 0.05);
}

.note-block-card__main {
  min-width: 0;
  flex: 1;
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: var(--space-3);
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.note-block-card__icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
  font-size: 0.72rem;
  font-weight: 900;
}

.note-block-card__copy {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
}

.note-block-card__copy strong {
  color: var(--bookos-text-primary);
}

.note-block-card__copy span {
  color: var(--bookos-text-secondary);
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.note-block-card__meta,
.note-block-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.note-block-card__meta {
  max-width: 280px;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 700;
}

.note-block-card__page,
.note-block-card__tag {
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface-muted);
  color: var(--bookos-primary-hover);
  font-size: var(--type-metadata);
  font-weight: 800;
  cursor: pointer;
}

.note-block-card__tag {
  color: var(--bookos-accent-hover);
}

@media (max-width: 900px) {
  .quick-capture__header,
  .recent-notes__header,
  .quick-capture__bar,
  .note-block-card {
    flex-direction: column;
  }

  .recent-notes__sort {
    width: 100%;
  }

  .note-block-card__meta {
    max-width: none;
  }

  .quick-capture__submit,
  .quick-capture__submit :deep(.el-button) {
    width: 100%;
  }
}
</style>
