<template>
  <div class="page-shell concept-detail-page">
    <AppLoadingState v-if="loading" label="Loading concept" />
    <AppErrorState v-else-if="errorMessage" title="Concept could not load" :description="errorMessage" retry-label="Retry" @retry="loadConcept" />

    <template v-else-if="concept">
      <AppSectionHeader
        :title="concept.name"
        eyebrow="Reviewed concept"
        :description="concept.bookTitle ? `First seen in ${concept.bookTitle}.` : 'Cross-book concept.'"
        :level="1"
      >
        <template #actions>
          <RouterLink to="/concepts" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Concepts</AppButton>
          </RouterLink>
          <AppButton variant="ghost" :disabled="!primarySource" @click="openPrimarySource">Open Source</AppButton>
          <RouterLink :to="forumThreadLink" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Discuss</AppButton>
          </RouterLink>
        </template>
      </AppSectionHeader>

      <section class="concept-detail-grid">
        <AppCard class="concept-main" as="article">
          <div class="concept-main__badges">
            <AppBadge variant="primary">{{ concept.visibility }}</AppBadge>
            <AppBadge variant="info">{{ concept.mentionCount }} mentions</AppBadge>
            <AppBadge variant="accent">{{ concept.sourceReferences.length }} sources</AppBadge>
          </div>
          <p>{{ concept.description ?? 'No description yet. Add one later when the concept stabilizes.' }}</p>
        </AppCard>

        <AppCard class="concept-related" as="aside">
          <AppSectionHeader title="Related Objects" eyebrow="Knowledge OS" :level="2" compact />
          <dl class="related-meta">
            <div>
              <dt>Knowledge objects</dt>
              <dd>{{ knowledgeObjects.length }}</dd>
            </div>
            <div>
              <dt>Related quotes</dt>
              <dd>{{ relatedQuotes.length }}</dd>
            </div>
            <div>
              <dt>Related notes</dt>
              <dd>{{ relatedNoteIds.length }}</dd>
            </div>
            <div>
              <dt>Related action items</dt>
              <dd>{{ relatedActionItems.length }}</dd>
            </div>
          </dl>
        </AppCard>
      </section>

      <AppCard class="related-list" as="section">
        <AppSectionHeader title="Knowledge Objects" eyebrow="Connected records" :level="2" compact />
        <AppEmptyState v-if="!knowledgeObjects.length" title="No knowledge objects yet" description="Reviewed concepts can be connected to principles, lenses, exercises, and prototype tasks." compact />
        <div v-else class="related-items">
          <RouterLink v-for="item in knowledgeObjects" :key="item.id" :to="{ name: 'knowledge-detail', params: { id: item.id } }" class="related-item">
            <AppBadge variant="info" size="sm">{{ item.type }}</AppBadge>
            <strong>{{ item.title }}</strong>
            <span>{{ item.tags.join(', ') || 'No tags' }}</span>
          </RouterLink>
        </div>
      </AppCard>

      <BacklinksSection
        entity-type="CONCEPT"
        :entity-id="concept.id"
        :source-references="concept.sourceReferences"
        :book-title="concept.bookTitle"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getActionItems } from '../api/actionItems'
import { getConcept, getKnowledgeObjects } from '../api/knowledge'
import { getQuotes } from '../api/quotes'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { ActionItemRecord, ConceptRecord, KnowledgeObjectRecord, QuoteRecord } from '../types'

const route = useRoute()
const { openSource } = useOpenSource()
const concept = ref<ConceptRecord | null>(null)
const knowledgeObjects = ref<KnowledgeObjectRecord[]>([])
const quotes = ref<QuoteRecord[]>([])
const actionItems = ref<ActionItemRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')

const primarySource = computed(() => concept.value?.firstSourceReference ?? concept.value?.sourceReferences[0] ?? null)
const forumThreadLink = computed(() => {
  if (!concept.value) return { name: 'forum-new' }
  return {
    name: 'forum-new',
    query: {
      relatedEntityType: 'CONCEPT',
      relatedEntityId: String(concept.value.id),
      conceptId: String(concept.value.id),
      bookId: concept.value.bookId ? String(concept.value.bookId) : undefined,
      sourceReferenceId: primarySource.value ? String(primarySource.value.id) : undefined,
      title: `Discuss [[${concept.value.name}]]`,
    },
  }
})
const relatedQuotes = computed(() => {
  if (!concept.value) return []
  return quotes.value.filter((quote) => quote.concepts.some((item) => item.toLowerCase() === concept.value?.name.toLowerCase()))
})
const relatedActionItems = computed(() => {
  if (!concept.value) return []
  const query = concept.value.name.toLowerCase()
  return actionItems.value.filter((item) => `${item.title} ${item.description ?? ''}`.toLowerCase().includes(query))
})
const relatedNoteIds = computed(() => {
  const ids = new Set<number>()
  concept.value?.sourceReferences.forEach((source) => {
    if (source.noteId) ids.add(source.noteId)
  })
  return [...ids]
})

onMounted(loadConcept)

async function loadConcept() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getConcept(String(route.params.id))
    concept.value = result
    const [objects, quoteResult, actionResult] = await Promise.all([
      getKnowledgeObjects({ conceptId: result.id }),
      getQuotes(),
      getActionItems(),
    ])
    knowledgeObjects.value = objects
    quotes.value = quoteResult
    actionItems.value = actionResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try opening this concept again.'
  } finally {
    loading.value = false
  }
}

function openPrimarySource() {
  if (!concept.value || !primarySource.value) return
  void openSource({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: primarySource.value.id,
    bookId: primarySource.value.bookId,
    bookTitle: concept.value.bookTitle,
    pageStart: primarySource.value.pageStart,
    sourceReference: primarySource.value,
    sourceReferenceId: primarySource.value.id,
  })
}
</script>

<style scoped>
.concept-detail-page,
.related-items {
  display: grid;
  gap: var(--space-5);
}

.concept-detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 0.42fr);
  gap: var(--space-4);
}

.concept-main,
.concept-related,
.related-list {
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.concept-main__badges {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.concept-main p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.related-meta {
  margin: 0;
  display: grid;
  gap: var(--space-3);
}

.related-meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.related-meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
  font-weight: 900;
}

.related-item {
  padding: var(--space-3);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: inherit;
  text-decoration: none;
}

.related-item span {
  color: var(--bookos-text-secondary);
}

@media (max-width: 980px) {
  .concept-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
