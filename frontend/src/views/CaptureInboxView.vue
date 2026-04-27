<template>
  <div class="page-shell capture-inbox-page">
    <AppSectionHeader
      title="Capture Inbox"
      eyebrow="Quick Capture"
      :description="bookId ? 'Inbox captures filtered to the selected book.' : 'All unconverted quick captures that need processing.'"
      :level="1"
    >
      <template #actions>
        <RouterLink to="/my-library" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Open Library</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppLoadingState v-if="capture.loading" label="Loading capture inbox" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Capture inbox could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadInbox"
    />

    <AppEmptyState
      v-else-if="!capture.latestBlocks.length"
      title="No inbox captures"
      description="Use Quick Capture on a book detail page to save source-backed reading thoughts."
      eyebrow="Inbox clear"
    />

    <section v-else class="capture-grid" aria-label="Capture inbox items">
      <AppCard v-for="block in capture.latestBlocks" :key="block.captureId" class="capture-card" as="article">
        <div class="capture-card__header">
          <div>
            <div class="eyebrow">{{ block.bookTitle }}</div>
            <h2>{{ block.title }}</h2>
          </div>
          <AppBadge variant="primary">{{ formatType(block.parsedType) }}</AppBadge>
        </div>

        <p class="capture-card__content">{{ block.content }}</p>

        <div class="capture-card__meta">
          <AppBadge v-if="block.page" variant="accent" size="sm">p.{{ block.page }}</AppBadge>
          <AppBadge v-for="tag in block.tags" :key="tag" variant="neutral" size="sm">{{ tag }}</AppBadge>
          <span>{{ formatDate(block.createdAt) }}</span>
        </div>

        <div class="capture-card__actions">
          <AppButton variant="primary" :loading="convertingCaptureId === block.captureId" @click="convertBlock(block)">
            Convert to Note
          </AppButton>
          <AppButton variant="ghost" :disabled="archivingCaptureId === block.captureId" @click="archiveBlock(block)">
            Archive
          </AppButton>
          <RouterLink :to="{ name: 'book-detail', params: { id: block.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open Book</AppButton>
          </RouterLink>
        </div>
      </AppCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useCaptureStore, type RecentNoteBlock } from '../stores/capture'
import type { NoteBlockType } from '../types'

const route = useRoute()
const router = useRouter()
const capture = useCaptureStore()
const errorMessage = ref('')
const convertingCaptureId = ref<number | null>(null)
const archivingCaptureId = ref<number | null>(null)

const bookId = computed(() => {
  const value = route.query.bookId
  return typeof value === 'string' && value ? value : null
})

onMounted(loadInbox)

watch(
  () => route.query.bookId,
  () => {
    void loadInbox()
  },
)

async function loadInbox() {
  errorMessage.value = ''
  try {
    await capture.loadInbox(bookId.value ?? undefined)
  } catch {
    errorMessage.value = 'Check your connection and permissions, then try loading the capture inbox again.'
  }
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

function formatType(type: NoteBlockType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
</script>

<style scoped>
.capture-inbox-page {
  display: grid;
  gap: var(--space-5);
}

.capture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 320px), 1fr));
  gap: var(--space-4);
}

.capture-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.capture-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.capture-card h2 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
  line-height: var(--type-title-line);
}

.capture-card__content {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.capture-card__meta,
.capture-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.capture-card__meta {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 700;
}

@media (max-width: 640px) {
  .capture-card {
    padding: var(--space-4);
  }

  .capture-card__header {
    flex-direction: column;
  }
}
</style>
