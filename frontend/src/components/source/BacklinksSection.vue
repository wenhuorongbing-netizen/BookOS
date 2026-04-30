<template>
  <AppCard class="backlinks-section" as="section">
    <AppSectionHeader
      :title="title"
      eyebrow="Traceability"
      :description="description"
      :level="2"
      compact
    />

    <AppLoadingState v-if="loading" label="Loading related links" />

    <AppErrorState v-else-if="errorMessage" title="Related links unavailable" :description="errorMessage" />

    <div v-else class="backlinks-section__content">
      <section aria-labelledby="direct-sources-title" class="backlinks-section__group">
        <h3 id="direct-sources-title">Source links</h3>
        <AppEmptyState
          v-if="!sourceReferences.length"
          title="No source links"
          description="This object does not have a direct source link yet."
          compact
        />
        <template v-else>
          <article v-for="source in sourceReferences" :key="source.id" class="backlink-card">
            <div class="backlink-card__main">
              <AppBadge :variant="confidenceVariant(source.sourceConfidence)" size="sm">{{ source.sourceConfidence }}</AppBadge>
              <strong>{{ source.locationLabel ?? source.sourceType }}</strong>
              <span>{{ pageLabel(source) }}</span>
            </div>
            <p>{{ source.sourceText ?? 'No source text stored.' }}</p>
            <AppButton variant="secondary" @click="openReference(source)">Open Source</AppButton>
          </article>
        </template>
      </section>

      <section aria-labelledby="entity-links-title" class="backlinks-section__group">
        <h3 id="entity-links-title">Related Links</h3>
        <AppEmptyState
          v-if="!backlinks.length"
          title="No related links"
          description="Related concepts, notes, captures, quotes, and tasks will appear here when linked."
          compact
        />
        <template v-else>
          <article v-for="backlink in backlinks" :key="`${backlink.direction}-${backlink.linkId}`" class="backlink-card">
            <div class="backlink-card__main">
              <AppBadge variant="info" size="sm">{{ backlink.relationType }}</AppBadge>
              <AppBadge variant="neutral" size="sm">{{ backlink.direction }}</AppBadge>
              <strong>{{ backlink.title }}</strong>
              <span>{{ backlink.entityType }} #{{ backlink.entityId }}</span>
              <span>{{ formatDate(backlink.createdAt) }}</span>
            </div>
            <p v-if="backlink.excerpt">{{ backlink.excerpt }}</p>
            <div class="backlink-card__actions">
              <AppButton variant="secondary" @click="openBacklink(backlink)">Open</AppButton>
              <AppButton v-if="backlink.sourceReference" variant="ghost" @click="openReference(backlink.sourceReference)">
                Open Source
              </AppButton>
            </div>
          </article>
        </template>
      </section>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getBacklinks } from '../../api/backlinks'
import { useOpenSource } from '../../composables/useOpenSource'
import type { BacklinkRecord, SourceConfidence, SourceReferenceRecord } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import AppLoadingState from '../ui/AppLoadingState.vue'
import AppSectionHeader from '../ui/AppSectionHeader.vue'

const props = withDefaults(
  defineProps<{
    entityType: string
    entityId: number | string
    sourceReferences?: SourceReferenceRecord[]
    title?: string
    description?: string
    bookTitle?: string | null
  }>(),
  {
    sourceReferences: () => [],
    title: 'Sources and Related Links',
    description: 'Open the original source and inspect source-backed relationships.',
    bookTitle: null,
  },
)

const router = useRouter()
const { openSource } = useOpenSource()
const backlinks = ref<BacklinkRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')

onMounted(loadLinks)

watch(
  () => [props.entityType, props.entityId],
  () => {
    void loadLinks()
  },
)

async function loadLinks() {
  loading.value = true
  errorMessage.value = ''
  try {
    backlinks.value = await getBacklinks({ entityType: props.entityType, entityId: props.entityId })
  } catch {
    backlinks.value = []
    errorMessage.value = 'Related links could not be loaded. Check backend availability and permissions.'
  } finally {
    loading.value = false
  }
}

function openReference(source: SourceReferenceRecord) {
  void openSource({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: source.id,
    bookId: source.bookId,
    bookTitle: props.bookTitle,
    pageStart: source.pageStart,
    noteId: source.noteId,
    rawCaptureId: source.rawCaptureId,
    noteBlockId: source.noteBlockId,
    sourceReference: source,
  })
}

function openBacklink(backlink: BacklinkRecord) {
  const target = routeFor(backlink.entityType, backlink.entityId)
  if (target) {
    void router.push(target)
    return
  }
  if (backlink.sourceReference) {
    openReference(backlink.sourceReference)
    return
  }
  void openSource({
    sourceType: backlink.entityType,
    sourceId: backlink.entityId,
  })
}

function routeFor(entityType: string, entityId: number) {
  if (entityType === 'BOOK') return { name: 'book-detail', params: { id: entityId } }
  if (entityType === 'NOTE') return { name: 'note-detail', params: { id: entityId } }
  if (entityType === 'RAW_CAPTURE' || entityType === 'CAPTURE') return { name: 'capture-inbox', query: { captureId: String(entityId) } }
  if (entityType === 'QUOTE') return { name: 'quote-detail', params: { id: entityId } }
  if (entityType === 'ACTION_ITEM') return { name: 'action-item-detail', params: { id: entityId } }
  if (entityType === 'CONCEPT') return { name: 'concept-detail', params: { id: entityId } }
  if (entityType === 'KNOWLEDGE_OBJECT') return { name: 'knowledge-detail', params: { id: entityId } }
  if (entityType === 'FORUM_THREAD') return { name: 'forum-thread', params: { id: entityId } }
  if (entityType === 'PROJECT' || entityType === 'GAME_PROJECT') return { name: 'project-detail', params: { id: entityId } }
  if (entityType === 'DAILY_PROMPT' || entityType === 'DAILY_DESIGN_PROMPT' || entityType === 'DAILY_SENTENCE') {
    return { name: 'daily' }
  }
  return null
}

function pageLabel(source: SourceReferenceRecord) {
  if (source.pageStart != null && source.pageEnd != null) return `p.${source.pageStart}-${source.pageEnd}`
  if (source.pageStart != null) return `p.${source.pageStart}`
  return 'Page unknown'
}

function confidenceVariant(confidence: SourceConfidence) {
  if (confidence === 'HIGH') return 'success'
  if (confidence === 'MEDIUM') return 'warning'
  return 'neutral'
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
</script>

<style scoped>
.backlinks-section {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.backlinks-section__content,
.backlinks-section__group {
  display: grid;
  gap: var(--space-3);
}

.backlinks-section__group h3 {
  margin: 0;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.backlink-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.backlink-card__main {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.backlink-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.backlink-card__main strong {
  color: var(--bookos-text-primary);
}

.backlink-card__main span,
.backlink-card p {
  color: var(--bookos-text-secondary);
}

.backlink-card p {
  margin: 0;
  line-height: var(--type-body-line);
  white-space: pre-wrap;
}
</style>
