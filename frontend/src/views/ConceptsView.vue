<template>
  <div class="page-shell concepts-page">
    <AppSectionHeader
      title="Concepts"
      eyebrow="Reviewed ontology"
      description="Browse concepts that were explicitly reviewed or created from source-backed notes and captures."
      :level="1"
    />

    <AppCard class="concepts-filter" as="section">
      <label class="field">
        <span>Search concepts</span>
        <el-input v-model="searchText" clearable placeholder="Search concepts, books, source text..." />
      </label>
      <label class="field">
        <span>Book</span>
        <el-select v-model="bookFilter" clearable filterable placeholder="All books" @change="loadConcepts">
          <el-option v-for="book in books" :key="book.id" :label="book.title" :value="book.id" />
        </el-select>
      </label>
      <label class="field">
        <span>Layer</span>
        <el-select v-model="layerFilter" clearable placeholder="All layers" @change="loadConcepts">
          <el-option v-for="layer in ontologyLayers" :key="layer" :label="layer" :value="layer" />
        </el-select>
      </label>
    </AppCard>

    <AppErrorState v-if="errorMessage" title="Concepts could not load" :description="errorMessage" retry-label="Retry" @retry="loadPage" />
    <AppLoadingState v-else-if="loading" label="Loading concepts" />
    <AppEmptyState
      v-else-if="!filteredConcepts.length"
      title="No concepts found"
      description="Review parsed [[Concept]] items from captures or note blocks to create source-backed concepts."
      eyebrow="Concepts"
    />

    <section v-else class="concept-grid" aria-label="Concept results">
      <AppCard v-for="concept in filteredConcepts" :key="concept.id" class="concept-card" as="article">
        <div class="concept-card__topline">
          <AppBadge variant="primary" size="sm">{{ concept.visibility }}</AppBadge>
          <AppBadge v-if="concept.ontologyLayer" variant="accent" size="sm">{{ concept.ontologyLayer }}</AppBadge>
          <AppBadge v-if="concept.createdBy === 'SYSTEM'" variant="info" size="sm">Seeded</AppBadge>
          <AppBadge v-if="concept.sourceConfidence" :variant="confidenceVariant(concept.sourceConfidence)" size="sm">
            {{ concept.sourceConfidence }} confidence
          </AppBadge>
          <AppBadge variant="info" size="sm">{{ concept.sourceReferences.length }} sources</AppBadge>
        </div>
        <RouterLink :to="{ name: 'concept-detail', params: { id: concept.id } }" class="concept-card__link">
          <h2>{{ concept.name }}</h2>
        </RouterLink>
        <p>{{ concept.description ?? 'No description yet.' }}</p>
        <div v-if="concept.tags.length" class="concept-card__tags">
          <AppBadge v-for="tag in concept.tags" :key="`${concept.id}-${tag}`" variant="neutral" size="sm">#{{ tag }}</AppBadge>
        </div>
        <p v-if="concept.sourceConfidence === 'LOW'" class="concept-card__warning">
          Low-confidence seed: source exists only at metadata or original-summary level. No page number is implied.
        </p>
        <dl class="concept-meta">
          <div>
            <dt>Related book</dt>
            <dd>{{ concept.bookTitle ?? 'Cross-book' }}</dd>
          </div>
          <div>
            <dt>Updated</dt>
            <dd>{{ formatDate(concept.updatedAt) }}</dd>
          </div>
        </dl>
        <div class="concept-card__actions">
          <AppButton variant="secondary" @click="openConceptSource(concept)">Open Source</AppButton>
          <AppButton variant="secondary" :loading="creatingPrototypeId === concept.id" @click="createPrototypeFromConcept(concept)">
            Create Prototype Task
          </AppButton>
          <RouterLink :to="{ name: 'concept-detail', params: { id: concept.id } }" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Details</AppButton>
          </RouterLink>
        </div>
      </AppCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink } from 'vue-router'
import { getBooks } from '../api/books'
import { createKnowledgeObject, getConcepts } from '../api/knowledge'
import { useOpenSource } from '../composables/useOpenSource'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type { BookRecord, ConceptRecord, SourceConfidence } from '../types'

const { openSource } = useOpenSource()
const ontologyLayers = [
  'Play & Fun',
  'Player Experience',
  'Design Lenses',
  'Systems & Loops',
  'Game Feel',
  'Practice & Exercises',
  'Topics & Reader',
  'Projects & Application',
]
const books = ref<BookRecord[]>([])
const concepts = ref<ConceptRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')
const searchText = ref('')
const bookFilter = ref<number | null>(null)
const layerFilter = ref('')
const creatingPrototypeId = ref<number | null>(null)

const filteredConcepts = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  if (!query) return concepts.value
  return concepts.value.filter((concept) => {
    const values = [
      concept.name,
      concept.description ?? '',
      concept.bookTitle ?? '',
      ...concept.sourceReferences.map((source) => source.sourceText ?? ''),
    ]
    return values.some((value) => value.toLowerCase().includes(query))
  })
})

onMounted(loadPage)

async function loadPage() {
  loading.value = true
  errorMessage.value = ''
  try {
    books.value = await getBooks()
    await loadConcepts()
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try loading concepts again.'
  } finally {
    loading.value = false
  }
}

async function loadConcepts() {
  concepts.value = await getConcepts({
    bookId: bookFilter.value || undefined,
    layer: layerFilter.value || undefined,
  })
}

function openConceptSource(concept: ConceptRecord) {
  const source = concept.firstSourceReference ?? concept.sourceReferences[0] ?? null
  if (!source) return
  void openSource({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: source.id,
    bookId: source.bookId,
    bookTitle: concept.bookTitle,
    pageStart: source.pageStart,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}

async function createPrototypeFromConcept(concept: ConceptRecord) {
  creatingPrototypeId.value = concept.id
  try {
    await createKnowledgeObject({
      type: 'PROTOTYPE_TASK',
      title: `Prototype: ${concept.name}`,
      description: `Create a small playable prototype that tests the concept "${concept.name}" in one focused design question.`,
      visibility: 'PRIVATE',
      conceptId: concept.id,
      ontologyLayer: concept.ontologyLayer ?? 'Projects & Application',
      tags: ['prototype', 'concept'],
    })
    ElMessage.success('Prototype task created from concept.')
  } catch {
    ElMessage.error('Prototype task could not be created. It may already exist.')
  } finally {
    creatingPrototypeId.value = null
  }
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}

function confidenceVariant(confidence: SourceConfidence) {
  if (confidence === 'HIGH') return 'success'
  if (confidence === 'MEDIUM') return 'warning'
  return 'danger'
}
</script>

<style scoped>
.concepts-page {
  display: grid;
  gap: var(--space-5);
}

.concepts-filter {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 0.32fr) minmax(220px, 0.32fr);
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.concept-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 320px), 1fr));
  gap: var(--space-4);
}

.concept-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.concept-card__topline,
.concept-card__tags,
.concept-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.concept-card__link {
  color: inherit;
  text-decoration: none;
}

.concept-card h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.35rem, 2vw, 1.9rem);
}

.concept-card p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.concept-card__warning {
  padding: var(--space-2) var(--space-3);
  border: 1px solid color-mix(in srgb, var(--bookos-warning) 32%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-warning-soft) 52%, var(--bookos-surface));
  font-size: var(--type-metadata);
}

.concept-meta {
  margin: 0;
  display: grid;
  gap: var(--space-2);
}

.concept-meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.concept-meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
}

@media (max-width: 720px) {
  .concepts-filter {
    grid-template-columns: 1fr;
  }
}
</style>
