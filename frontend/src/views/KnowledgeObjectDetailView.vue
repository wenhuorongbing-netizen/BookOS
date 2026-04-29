<template>
  <div class="page-shell knowledge-detail-page">
    <AppLoadingState v-if="loading" label="Loading knowledge object" />
    <AppErrorState v-else-if="errorMessage" title="Knowledge object could not load" :description="errorMessage" retry-label="Retry" @retry="loadObject" />

    <template v-else-if="object">
      <AppSectionHeader
        :title="object.title"
        eyebrow="Knowledge object"
        :description="typeLabel(object.type)"
        :level="1"
      >
        <template #actions>
          <RouterLink to="/knowledge" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Knowledge</AppButton>
          </RouterLink>
          <AppButton variant="ghost" :disabled="!object.sourceReference" @click="openObjectSource">Open Source</AppButton>
          <RouterLink :to="graphContextLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Graph Context</AppButton>
          </RouterLink>
          <RouterLink :to="forumThreadLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
          </RouterLink>
          <AppButton variant="accent" @click="applyProjectOpen = true">Apply to Project</AppButton>
        </template>
      </AppSectionHeader>

      <AppCard class="knowledge-main" as="article">
        <div class="knowledge-main__badges">
          <AppBadge variant="info">{{ object.type }}</AppBadge>
          <AppBadge variant="primary">{{ object.visibility }}</AppBadge>
          <AppBadge v-if="object.ontologyLayer" variant="accent">{{ object.ontologyLayer }}</AppBadge>
          <AppBadge v-if="object.createdBy === 'SYSTEM'" variant="info">Seeded</AppBadge>
          <AppBadge v-if="object.sourceConfidence" variant="warning">{{ object.sourceConfidence }} confidence</AppBadge>
        </div>
        <p>{{ object.description ?? 'No description yet.' }}</p>
        <dl class="knowledge-meta">
          <div>
            <dt>Book</dt>
            <dd>{{ object.bookTitle ?? 'Not linked' }}</dd>
          </div>
          <div>
            <dt>Concept</dt>
            <dd>{{ object.conceptName ?? 'Not linked' }}</dd>
          </div>
          <div>
            <dt>Note</dt>
            <dd>{{ object.noteTitle ?? 'Not linked' }}</dd>
          </div>
          <div>
            <dt>Updated</dt>
            <dd>{{ formatDate(object.updatedAt) }}</dd>
          </div>
        </dl>
        <div v-if="object.tags.length" class="knowledge-main__tags">
          <AppBadge v-for="tag in object.tags" :key="tag" variant="neutral" size="sm">#{{ tag }}</AppBadge>
        </div>
      </AppCard>

      <BacklinksSection
        entity-type="KNOWLEDGE_OBJECT"
        :entity-id="object.id"
        :source-references="object.sourceReference ? [object.sourceReference] : []"
        :book-title="object.bookTitle"
      />

      <ApplyToProjectDialog
        v-if="object"
        v-model="applyProjectOpen"
        source-type="KNOWLEDGE_OBJECT"
        :source-id="object.id"
        :source-reference="object.sourceReference"
        :source-label="object.title"
        :default-title="`Apply ${object.title}`"
        :default-description="object.description ?? `Apply this ${typeLabel(object.type).toLowerCase()} to the current project.`"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getKnowledgeObject } from '../api/knowledge'
import ApplyToProjectDialog from '../components/project/ApplyToProjectDialog.vue'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { KnowledgeObjectRecord, KnowledgeObjectType } from '../types'

const route = useRoute()
const { openSource } = useOpenSource()
const object = ref<KnowledgeObjectRecord | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const applyProjectOpen = ref(false)

const graphContextLink = computed(() => {
  if (object.value?.conceptId) return { name: 'graph-concept', params: { conceptId: object.value.conceptId } }
  if (object.value?.bookId) return { name: 'graph-book', params: { bookId: object.value.bookId } }
  return { name: 'graph' }
})
const forumThreadLink = computed(() => {
  if (!object.value) return { name: 'forum-new' }
  return {
    name: 'forum-new',
    query: {
      relatedEntityType: 'KNOWLEDGE_OBJECT',
      relatedEntityId: String(object.value.id),
      bookId: object.value.bookId ? String(object.value.bookId) : undefined,
      conceptId: object.value.conceptId ? String(object.value.conceptId) : undefined,
      sourceReferenceId: object.value.sourceReference ? String(object.value.sourceReference.id) : undefined,
      title: `Discuss ${object.value.title}`,
    },
  }
})

onMounted(loadObject)

async function loadObject() {
  loading.value = true
  errorMessage.value = ''
  try {
    object.value = await getKnowledgeObject(String(route.params.id))
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try opening this knowledge object again.'
  } finally {
    loading.value = false
  }
}

function openObjectSource() {
  if (!object.value?.sourceReference) return
  void openSource({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: object.value.sourceReference.id,
    bookId: object.value.sourceReference.bookId,
    bookTitle: object.value.bookTitle,
    pageStart: object.value.sourceReference.pageStart,
    sourceReference: object.value.sourceReference,
    sourceReferenceId: object.value.sourceReference.id,
  })
}

function typeLabel(type: KnowledgeObjectType) {
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
.knowledge-detail-page {
  display: grid;
  gap: var(--space-5);
}

.knowledge-main {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.knowledge-main__badges,
.knowledge-main__tags {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.knowledge-main p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
  white-space: pre-wrap;
}

.knowledge-meta {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.knowledge-meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.knowledge-meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
}

@media (max-width: 720px) {
  .knowledge-meta {
    grid-template-columns: 1fr;
  }
}
</style>
