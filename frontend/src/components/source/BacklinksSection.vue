<template>
  <AppCard class="backlinks-section" as="section">
    <AppSectionHeader
      :title="title"
      eyebrow="Traceability"
      :description="description"
      :level="2"
      compact
    />

    <AppLoadingState v-if="loading" label="Loading backlinks" />

    <AppErrorState v-else-if="errorMessage" title="Backlinks unavailable" :description="errorMessage" />

    <div v-else class="backlinks-section__content">
      <section aria-labelledby="direct-sources-title" class="backlinks-section__group">
        <h3 id="direct-sources-title">Source references</h3>
        <AppEmptyState
          v-if="!sourceReferences.length"
          title="No source references"
          description="This object does not have a direct source reference yet."
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
        <h3 id="entity-links-title">Backlinks</h3>
        <AppEmptyState
          v-if="!entityLinks.length"
          title="No backlinks"
          description="Related concepts, notes, captures, quotes, and tasks will appear here when linked."
          compact
        />
        <template v-else>
          <article v-for="link in entityLinks" :key="link.id" class="backlink-card">
            <div class="backlink-card__main">
              <AppBadge variant="info" size="sm">{{ link.relationType }}</AppBadge>
              <strong>{{ linkLabel(link) }}</strong>
              <span>{{ formatDate(link.createdAt) }}</span>
            </div>
            <AppButton
              v-if="link.sourceReferenceId"
              variant="secondary"
              @click="openReferenceById(link.sourceReferenceId, link)"
            >
              Open Source
            </AppButton>
          </article>
        </template>
      </section>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { getEntityLinks } from '../../api/entityLinks'
import { getSourceReference } from '../../api/sourceReferences'
import { useOpenSource } from '../../composables/useOpenSource'
import type { EntityLinkRecord, SourceConfidence, SourceReferenceRecord } from '../../types'
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
    title: 'Sources and Backlinks',
    description: 'Open the original source and inspect source-backed relationships.',
    bookTitle: null,
  },
)

const { openSource } = useOpenSource()
const outgoingLinks = ref<EntityLinkRecord[]>([])
const incomingLinks = ref<EntityLinkRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')

const entityLinks = computed(() => {
  const seen = new Set<number>()
  return [...outgoingLinks.value, ...incomingLinks.value].filter((link) => {
    if (seen.has(link.id)) return false
    seen.add(link.id)
    return true
  })
})

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
    const [outgoing, incoming] = await Promise.all([
      getEntityLinks({ sourceType: props.entityType, sourceId: props.entityId }),
      getEntityLinks({ targetType: props.entityType, targetId: props.entityId }),
    ])
    outgoingLinks.value = outgoing
    incomingLinks.value = incoming
  } catch {
    outgoingLinks.value = []
    incomingLinks.value = []
    errorMessage.value = 'Backlinks could not be loaded. Check backend availability and permissions.'
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

async function openReferenceById(sourceReferenceId: number, link: EntityLinkRecord) {
  try {
    const source = await getSourceReference(sourceReferenceId)
    await openSource({
      sourceType: 'SOURCE_REFERENCE',
      sourceId: source.id,
      bookId: source.bookId,
      bookTitle: props.bookTitle,
      pageStart: source.pageStart,
      sourceReference: source,
    })
  } catch {
    await openSource({
      sourceType: link.sourceType,
      sourceId: link.sourceId,
    })
  }
}

function linkLabel(link: EntityLinkRecord) {
  return `${link.sourceType} #${link.sourceId} -> ${link.targetType} #${link.targetId}`
}

function pageLabel(source: SourceReferenceRecord) {
  if (source.pageStart && source.pageEnd) return `p.${source.pageStart}-${source.pageEnd}`
  if (source.pageStart) return `p.${source.pageStart}`
  return 'No page'
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
