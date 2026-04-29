<template>
  <div class="page-shell knowledge-page">
    <AppSectionHeader
      title="Knowledge Objects"
      eyebrow="Structured design knowledge"
      description="Create and browse source-backed concepts, principles, lenses, exercises, methods, and prototype tasks."
      :level="1"
    >
      <template #actions>
        <AppButton variant="primary" @click="dialogOpen = true">Create Knowledge Object</AppButton>
      </template>
    </AppSectionHeader>

    <AppCard class="knowledge-filter" as="section">
      <label class="field">
        <span>Search</span>
        <el-input v-model="searchText" clearable placeholder="Search title, description, tags..." />
      </label>
      <label class="field">
        <span>Type</span>
        <el-select v-model="typeFilter" clearable placeholder="All types" @change="loadObjects">
          <el-option v-for="type in supportedTypes" :key="type" :label="typeLabel(type)" :value="type" />
        </el-select>
      </label>
      <label class="field">
        <span>Layer</span>
        <el-select v-model="layerFilter" clearable placeholder="All layers" @change="loadObjects">
          <el-option v-for="layer in ontologyLayers" :key="layer" :label="layer" :value="layer" />
        </el-select>
      </label>
    </AppCard>

    <AppErrorState v-if="errorMessage" title="Knowledge objects could not load" :description="errorMessage" retry-label="Retry" @retry="loadPage" />
    <AppLoadingState v-else-if="loading" label="Loading knowledge objects" />
    <AppEmptyState v-else-if="!filteredObjects.length" title="No knowledge objects found" description="Create one manually or review parsed concepts from reading sources." compact />

    <section v-else class="knowledge-grid" aria-label="Knowledge object results">
      <AppCard v-for="item in filteredObjects" :key="item.id" class="knowledge-card" as="article">
        <div class="knowledge-card__topline">
          <AppBadge variant="info" size="sm">{{ item.type }}</AppBadge>
          <AppBadge variant="primary" size="sm">{{ item.visibility }}</AppBadge>
          <AppBadge v-if="item.ontologyLayer" variant="accent" size="sm">{{ item.ontologyLayer }}</AppBadge>
          <AppBadge v-if="item.createdBy === 'SYSTEM'" variant="info" size="sm">Seeded</AppBadge>
          <AppBadge v-if="item.sourceConfidence" :variant="confidenceVariant(item.sourceConfidence)" size="sm">
            {{ item.sourceConfidence }} confidence
          </AppBadge>
        </div>
        <RouterLink :to="{ name: 'knowledge-detail', params: { id: item.id } }" class="knowledge-card__link">
          <h2>{{ item.title }}</h2>
        </RouterLink>
        <p>{{ item.description ?? 'No description yet.' }}</p>
        <p v-if="item.sourceConfidence === 'LOW'" class="knowledge-card__warning">
          Low-confidence seed: no page-level source is implied.
        </p>
        <div v-if="item.tags.length" class="knowledge-card__tags">
          <AppBadge v-for="tag in item.tags" :key="`${item.id}-${tag}`" variant="neutral" size="sm">#{{ tag }}</AppBadge>
        </div>
        <div class="knowledge-card__actions">
          <RouterLink :to="{ name: 'knowledge-detail', params: { id: item.id } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Details</AppButton>
          </RouterLink>
          <AppButton variant="ghost" :disabled="!item.sourceReference" @click="openObjectSource(item)">Open Source</AppButton>
        </div>
      </AppCard>
    </section>

    <el-dialog v-model="dialogOpen" title="Create Knowledge Object" width="min(720px, calc(100vw - 32px))" align-center>
      <form class="knowledge-form" @submit.prevent="saveObject">
        <div class="knowledge-form__grid">
          <label class="field">
            <span>Type</span>
            <el-select v-model="form.type">
              <el-option v-for="type in supportedTypes" :key="type" :label="typeLabel(type)" :value="type" />
            </el-select>
          </label>
          <label class="field">
            <span>Visibility</span>
            <el-select v-model="form.visibility">
              <el-option label="PRIVATE" value="PRIVATE" />
              <el-option label="SHARED" value="SHARED" />
              <el-option label="PUBLIC" value="PUBLIC" />
            </el-select>
          </label>
        </div>

        <label class="field">
          <span>Title</span>
          <el-input v-model="form.title" maxlength="220" show-word-limit placeholder="Knowledge object title" />
        </label>
        <label class="field">
          <span>Description</span>
          <el-input v-model="form.description" type="textarea" :rows="5" maxlength="10000" />
        </label>

        <div class="knowledge-form__grid">
          <label class="field">
            <span>Book</span>
            <el-select v-model="form.bookId" clearable filterable placeholder="Optional book">
              <el-option v-for="book in books" :key="book.id" :label="book.title" :value="book.id" />
            </el-select>
          </label>
          <label class="field">
            <span>Concept</span>
            <el-select v-model="form.conceptId" clearable filterable placeholder="Optional concept">
              <el-option v-for="concept in concepts" :key="concept.id" :label="concept.name" :value="concept.id" />
            </el-select>
          </label>
        </div>

        <label class="field">
          <span>Ontology layer</span>
          <el-select v-model="form.ontologyLayer" clearable placeholder="Optional layer">
            <el-option v-for="layer in ontologyLayers" :key="layer" :label="layer" :value="layer" />
          </el-select>
        </label>

        <label class="field">
          <span>Tags</span>
          <el-input v-model="form.tagsInput" placeholder="lens, prototype, systems" />
        </label>

        <div class="knowledge-form__actions">
          <AppButton variant="ghost" @click="dialogOpen = false">Cancel</AppButton>
          <AppButton variant="primary" native-type="submit" :loading="saving">Create</AppButton>
        </div>
      </form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink } from 'vue-router'
import { getBooks } from '../api/books'
import { createKnowledgeObject, getConcepts, getKnowledgeObjects } from '../api/knowledge'
import { useOpenSource } from '../composables/useOpenSource'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type { BookRecord, ConceptRecord, KnowledgeObjectRecord, KnowledgeObjectType, SourceConfidence, Visibility } from '../types'

const supportedTypes: KnowledgeObjectType[] = [
  'CONCEPT',
  'PRINCIPLE',
  'DESIGN_LENS',
  'DIAGNOSTIC_QUESTION',
  'EXERCISE',
  'PROTOTYPE_TASK',
  'PATTERN',
  'ANTI_PATTERN',
  'METHOD',
  'EXAMPLE_CASE',
]

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

const { openSource } = useOpenSource()
const books = ref<BookRecord[]>([])
const concepts = ref<ConceptRecord[]>([])
const objects = ref<KnowledgeObjectRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const searchText = ref('')
const typeFilter = ref<KnowledgeObjectType | ''>('')
const layerFilter = ref('')
const dialogOpen = ref(false)
const form = reactive({
  type: 'CONCEPT' as KnowledgeObjectType,
  title: '',
  description: '',
  visibility: 'PRIVATE' as Visibility,
  bookId: null as number | null,
  conceptId: null as number | null,
  ontologyLayer: '',
  tagsInput: '',
})

const filteredObjects = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  if (!query) return objects.value
  return objects.value.filter((item) => [item.title, item.description ?? '', item.type, ...item.tags].some((value) => value.toLowerCase().includes(query)))
})

onMounted(loadPage)

async function loadPage() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [bookResult, conceptResult] = await Promise.all([getBooks(), getConcepts()])
    books.value = bookResult
    concepts.value = conceptResult
    await loadObjects()
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try loading knowledge objects again.'
  } finally {
    loading.value = false
  }
}

async function loadObjects() {
  objects.value = await getKnowledgeObjects({
    type: typeFilter.value || undefined,
    layer: layerFilter.value || undefined,
  })
}

async function saveObject() {
  if (!form.title.trim()) {
    ElMessage.warning('Title is required.')
    return
  }
  saving.value = true
  try {
    const saved = await createKnowledgeObject({
      type: form.type,
      title: form.title.trim(),
      description: form.description.trim() || null,
      visibility: form.visibility,
      bookId: form.bookId,
      conceptId: form.conceptId,
      ontologyLayer: form.ontologyLayer || null,
      tags: parseList(form.tagsInput),
    })
    objects.value = [saved, ...objects.value]
    dialogOpen.value = false
    form.title = ''
    form.description = ''
    form.ontologyLayer = ''
    form.tagsInput = ''
    ElMessage.success('Knowledge object created.')
  } catch {
    ElMessage.error('Knowledge object could not be created.')
  } finally {
    saving.value = false
  }
}

function openObjectSource(item: KnowledgeObjectRecord) {
  if (!item.sourceReference) return
  void openSource({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: item.sourceReference.id,
    bookId: item.sourceReference.bookId,
    bookTitle: item.bookTitle,
    pageStart: item.sourceReference.pageStart,
    sourceReference: item.sourceReference,
    sourceReferenceId: item.sourceReference.id,
  })
}

function typeLabel(type: KnowledgeObjectType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function parseList(value: string) {
  return value
    .split(',')
    .map((item) => item.trim().replace(/^#/, '').toLowerCase())
    .filter(Boolean)
}

function confidenceVariant(confidence: SourceConfidence) {
  if (confidence === 'HIGH') return 'success'
  if (confidence === 'MEDIUM') return 'warning'
  return 'danger'
}
</script>

<style scoped>
.knowledge-page {
  display: grid;
  gap: var(--space-5);
}

.knowledge-filter,
.knowledge-form__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 0.32fr) minmax(220px, 0.32fr);
  gap: var(--space-3);
}

.knowledge-filter,
.knowledge-card {
  padding: var(--space-4);
}

.field,
.knowledge-form {
  display: grid;
  gap: var(--space-2);
}

.field {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 320px), 1fr));
  gap: var(--space-4);
}

.knowledge-card {
  display: grid;
  gap: var(--space-3);
}

.knowledge-card__topline,
.knowledge-card__tags,
.knowledge-card__actions,
.knowledge-form__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.knowledge-card__link {
  color: inherit;
  text-decoration: none;
}

.knowledge-card h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
}

.knowledge-card p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.knowledge-card__warning {
  padding: var(--space-2) var(--space-3);
  border: 1px solid color-mix(in srgb, var(--bookos-warning) 32%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-warning-soft) 52%, var(--bookos-surface));
  font-size: var(--type-metadata);
}

.knowledge-form__actions {
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .knowledge-filter,
  .knowledge-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
