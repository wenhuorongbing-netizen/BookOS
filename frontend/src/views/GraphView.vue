<template>
  <div class="page-shell graph-page">
    <AppSectionHeader
      title="Knowledge Graph"
      eyebrow="Source-backed network"
      description="Explore real links between books, notes, quotes, action items, concepts, knowledge objects, forum threads, daily prompts, and source references."
      :level="1"
    >
      <template #actions>
        <AppButton variant="secondary" @click="resetFilters">Reset Filters</AppButton>
      </template>
    </AppSectionHeader>

    <AppCard class="graph-filters" as="section">
      <label class="graph-field">
        <span>Book filter</span>
        <el-input-number
          v-model="bookIdFilter"
          :min="1"
          controls-position="right"
          placeholder="Any book"
          :disabled="isConceptRoute"
          aria-label="Filter graph by book id"
          @change="loadGraph"
        />
      </label>
      <label class="graph-field">
        <span>Entity type</span>
        <el-select v-model="entityTypeFilter" clearable placeholder="All entity types" aria-label="Filter graph by entity type" @change="loadGraph">
          <el-option v-for="option in entityTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
        </el-select>
      </label>
      <label class="graph-field">
        <span>Relationship</span>
        <el-select
          v-model="relationshipTypeFilter"
          clearable
          placeholder="All relationships"
          aria-label="Filter graph by relationship type"
          @change="loadGraph"
        >
          <el-option v-for="option in relationshipTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
        </el-select>
      </label>
    </AppCard>

    <AppLoadingState v-if="loading" label="Loading source-backed graph" />
    <AppErrorState v-else-if="errorMessage" title="Graph could not load" :description="errorMessage" retry-label="Retry" @retry="loadGraph" />

    <AppCard v-else class="graph-workspace" as="section">
      <div class="graph-workspace__summary">
        <div>
          <div class="eyebrow">Graph Scope</div>
          <h2>{{ graphTitle }}</h2>
          <p>{{ filteredGraph.nodes.length }} nodes and {{ filteredGraph.edges.length }} edges from persisted BookOS records.</p>
        </div>
        <AppBadge v-if="!filteredGraph.nodes.length" variant="warning">Empty</AppBadge>
        <AppBadge v-else variant="success">Real data only</AppBadge>
      </div>

      <AppEmptyState
        v-if="!filteredGraph.nodes.length"
        title="No graph links yet"
        description="Create source-backed notes, captures, quotes, action items, concepts, knowledge objects, or forum threads to grow this graph."
        compact
      />

      <div v-else class="graph-workspace__grid">
        <section class="graph-panel" aria-labelledby="graph-nodes-title">
          <h3 id="graph-nodes-title">Nodes</h3>
          <div class="node-list">
            <button
              v-for="node in filteredGraph.nodes"
              :key="node.id"
              class="node-card"
              type="button"
              :aria-label="`Open ${typeLabel(node.type)} ${node.label}`"
              @click="openNode(node)"
            >
              <AppBadge :variant="nodeVariant(node.type)" size="sm">{{ typeLabel(node.type) }}</AppBadge>
              <strong>{{ node.label }}</strong>
              <span>#{{ node.entityId }}</span>
            </button>
          </div>
        </section>

        <section class="graph-panel" aria-labelledby="graph-edges-title">
          <h3 id="graph-edges-title">Edges</h3>
          <div class="edge-list">
            <article v-for="edge in filteredGraph.edges" :key="`${edge.source}-${edge.target}-${edge.type}`" class="edge-card">
              <AppBadge variant="info" size="sm">{{ typeLabel(edge.type) }}</AppBadge>
              <span>{{ nodeLabel(edge.source) }}</span>
              <b aria-hidden="true">→</b>
              <span>{{ nodeLabel(edge.target) }}</span>
            </article>
          </div>
        </section>
      </div>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConceptGraph, getWorkspaceGraph } from '../api/graph'
import { useOpenSource } from '../composables/useOpenSource'
import type { GraphNodeRecord, GraphRecord } from '../types'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'

const route = useRoute()
const router = useRouter()
const { openSource } = useOpenSource()

const graph = ref<GraphRecord>({ nodes: [], edges: [] })
const loading = ref(false)
const errorMessage = ref('')
const bookIdFilter = ref<number | undefined>(initialBookId())
const entityTypeFilter = ref(initialQueryValue('entityType'))
const relationshipTypeFilter = ref(initialQueryValue('relationshipType'))

const entityTypeOptions = [
  'BOOK',
  'NOTE',
  'QUOTE',
  'ACTION_ITEM',
  'CONCEPT',
  'KNOWLEDGE_OBJECT',
  'FORUM_THREAD',
  'DAILY_PROMPT',
  'SOURCE_REFERENCE',
]
const relationshipTypeOptions = ['MENTIONS', 'SOURCE_OF', 'DERIVED_FROM', 'RELATED_TO', 'DISCUSSES', 'APPLIES_TO']

const isConceptRoute = computed(() => route.name === 'graph-concept')

const graphTitle = computed(() => {
  if (route.name === 'graph-concept') return `Concept graph #${route.params.conceptId}`
  if (bookIdFilter.value) return `Book graph #${bookIdFilter.value}`
  return 'Workspace graph'
})

const filteredGraph = computed(() => {
  if (!isConceptRoute.value || (!entityTypeFilter.value && !relationshipTypeFilter.value)) {
    return graph.value
  }

  const wantedEntityType = entityTypeFilter.value
  const wantedRelationshipType = relationshipTypeFilter.value
  const scopedEdges = graph.value.edges.filter((edge) => !wantedRelationshipType || edge.type === wantedRelationshipType)
  const includedNodeIds = new Set<string>()

  if (wantedEntityType) {
    graph.value.nodes.filter((node) => node.type === wantedEntityType).forEach((node) => includedNodeIds.add(node.id))
    scopedEdges
      .filter((edge) => includedNodeIds.has(edge.source) || includedNodeIds.has(edge.target))
      .forEach((edge) => {
        includedNodeIds.add(edge.source)
        includedNodeIds.add(edge.target)
      })
  } else {
    scopedEdges.forEach((edge) => {
      includedNodeIds.add(edge.source)
      includedNodeIds.add(edge.target)
    })
  }

  return {
    nodes: graph.value.nodes.filter((node) => includedNodeIds.has(node.id)),
    edges: scopedEdges.filter((edge) => includedNodeIds.has(edge.source) && includedNodeIds.has(edge.target)),
  }
})

onMounted(loadGraph)

watch(
  () => [route.name, route.params.bookId, route.params.conceptId, route.query.bookId, route.query.entityType, route.query.relationshipType],
  () => {
    bookIdFilter.value = initialBookId()
    entityTypeFilter.value = initialQueryValue('entityType')
    relationshipTypeFilter.value = initialQueryValue('relationshipType')
    void loadGraph()
  },
)

async function loadGraph() {
  loading.value = true
  errorMessage.value = ''
  try {
    if (isConceptRoute.value) {
      graph.value = await getConceptGraph(String(route.params.conceptId))
      return
    }

    graph.value = await getWorkspaceGraph({
      bookId: bookIdFilter.value ?? null,
      entityType: entityTypeFilter.value || null,
      relationshipType: relationshipTypeFilter.value || null,
    })
  } catch {
    graph.value = { nodes: [], edges: [] }
    errorMessage.value = 'Check backend availability and graph permissions, then retry.'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  entityTypeFilter.value = ''
  relationshipTypeFilter.value = ''
  if (route.name === 'graph') {
    bookIdFilter.value = undefined
  }
  void loadGraph()
}

function openNode(node: GraphNodeRecord) {
  if (node.type === 'SOURCE_REFERENCE') {
    void openSource({
      sourceType: 'SOURCE_REFERENCE',
      sourceId: node.entityId,
      sourceReferenceId: node.entityId,
    })
    return
  }

  const target = routeForNode(node)
  if (target) {
    void router.push(target)
  }
}

function routeForNode(node: GraphNodeRecord) {
  if (node.type === 'BOOK') return { name: 'book-detail', params: { id: node.entityId } }
  if (node.type === 'NOTE') return { name: 'note-detail', params: { id: node.entityId } }
  if (node.type === 'QUOTE') return { name: 'quote-detail', params: { id: node.entityId } }
  if (node.type === 'ACTION_ITEM') return { name: 'action-item-detail', params: { id: node.entityId } }
  if (node.type === 'CONCEPT') return { name: 'concept-detail', params: { id: node.entityId } }
  if (node.type === 'KNOWLEDGE_OBJECT') return { name: 'knowledge-detail', params: { id: node.entityId } }
  if (node.type === 'FORUM_THREAD') return { name: 'forum-thread', params: { id: node.entityId } }
  if (node.type === 'DAILY_PROMPT') return { name: 'daily' }
  return null
}

function nodeLabel(nodeId: string) {
  return graph.value.nodes.find((node) => node.id === nodeId)?.label ?? nodeId
}

function initialBookId() {
  const raw = route.params.bookId ?? route.query.bookId
  const value = Array.isArray(raw) ? raw[0] : raw
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
}

function initialQueryValue(key: 'entityType' | 'relationshipType') {
  const raw = route.query[key]
  return typeof raw === 'string' ? raw.toUpperCase() : ''
}

function typeLabel(value: string) {
  return value.replaceAll('_', ' ').toLowerCase().replace(/^\w|\s\w/g, (match) => match.toUpperCase())
}

function nodeVariant(type: string) {
  if (type === 'BOOK') return 'primary'
  if (type === 'QUOTE') return 'accent'
  if (type === 'ACTION_ITEM') return 'warning'
  if (type === 'SOURCE_REFERENCE') return 'info'
  if (type === 'CONCEPT' || type === 'KNOWLEDGE_OBJECT') return 'success'
  return 'neutral'
}
</script>

<style scoped>
.graph-page,
.graph-workspace,
.graph-workspace__grid,
.graph-panel,
.node-list,
.edge-list {
  display: grid;
  gap: var(--space-4);
}

.graph-filters {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.graph-field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.graph-field :deep(.el-input-number),
.graph-field :deep(.el-select) {
  width: 100%;
}

.graph-workspace {
  padding: var(--space-5);
}

.graph-workspace__summary {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: start;
  flex-wrap: wrap;
}

.graph-workspace__summary h2,
.graph-panel h3 {
  margin: 0;
  color: var(--bookos-text-primary);
}

.graph-workspace__summary p {
  margin: var(--space-2) 0 0;
  color: var(--bookos-text-secondary);
}

.graph-workspace__grid {
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  align-items: start;
}

.node-list {
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 210px), 1fr));
}

.node-card,
.edge-card {
  min-height: 72px;
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.node-card {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-primary);
  text-align: left;
  cursor: pointer;
}

.node-card:hover,
.node-card:focus-visible {
  border-color: color-mix(in srgb, var(--bookos-primary) 36%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-primary-soft) 24%, var(--bookos-surface));
}

.node-card strong {
  overflow-wrap: anywhere;
}

.node-card span {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
}

.edge-card {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  color: var(--bookos-text-secondary);
}

.edge-card span {
  color: var(--bookos-text-primary);
  font-weight: 800;
}

@media (max-width: 980px) {
  .graph-filters,
  .graph-workspace__grid {
    grid-template-columns: 1fr;
  }
}
</style>
