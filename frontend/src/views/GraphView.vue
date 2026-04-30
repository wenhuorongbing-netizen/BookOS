<template>
  <div class="page-shell graph-page">
    <AppSectionHeader
      title="Knowledge Graph"
      eyebrow="Source-backed network"
      description="Explore and curate real relationships between BookOS records. Empty areas mean no source-backed links exist yet."
      :level="1"
    >
      <template #actions>
        <HelpTooltip topic="graph" placement="left" />
      </template>
    </AppSectionHeader>

    <DetailNextStepCard
      :title="graphNextStep.title"
      :description="graphNextStep.description"
      :primary-label="graphNextStep.primaryLabel"
      :primary-to="graphNextStep.primaryTo"
      :secondary-label="graphNextStep.secondaryLabel"
      :secondary-to="graphNextStep.secondaryTo"
      :loop="graphWorkflowLoop"
      @primary="handleGraphPrimaryAction"
      @secondary="handleGraphSecondaryAction"
    />

    <details class="graph-disclosure">
      <summary>
        <span>Workflow guides</span>
        <small>Use when the Knowledge Graph feels abstract</small>
      </summary>
      <UseCaseSuggestionPanel
        title="Make Knowledge Graph exploration practical"
        description="Use the Knowledge Graph after you have source-backed captures, concepts, project applications, or manual relationships."
        :slugs="graphUseCaseSlugs"
      />
    </details>

    <details class="graph-disclosure" :open="filtersActive">
      <summary>
        <span>Filters</span>
        <small>{{ filtersActive ? 'Custom filters active' : 'Optional advanced narrowing' }}</small>
      </summary>
      <AppCard class="graph-filters" as="section" aria-label="Knowledge Graph filters">
        <label class="graph-field">
          <span>Book ID</span>
          <el-input-number
            v-model="bookIdFilter"
            :min="1"
            controls-position="right"
            placeholder="Any book"
            :disabled="isBookRoute || isConceptRoute || isProjectRoute"
            aria-label="Filter graph by book id"
          />
        </label>
        <label class="graph-field">
          <span>Concept ID</span>
          <el-input-number
            v-model="conceptIdFilter"
            :min="1"
            controls-position="right"
            placeholder="Any concept"
            :disabled="isBookRoute || isConceptRoute || isProjectRoute"
            aria-label="Filter graph by concept id"
          />
        </label>
        <label class="graph-field">
          <span>Project ID</span>
          <el-input-number
            v-model="projectIdFilter"
            :min="1"
            controls-position="right"
            placeholder="Any project"
            :disabled="isBookRoute || isConceptRoute || isProjectRoute"
            aria-label="Filter graph by project id"
          />
        </label>
        <label class="graph-field">
          <span>Entity type</span>
          <el-select v-model="entityTypeFilter" clearable placeholder="All entities" aria-label="Filter graph by entity type">
            <el-option v-for="option in entityTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
          </el-select>
        </label>
        <label class="graph-field">
          <span>Relationship</span>
          <el-select v-model="relationshipTypeFilter" clearable filterable placeholder="All relationships" aria-label="Filter graph by relationship type">
            <el-option v-for="option in relationshipTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
          </el-select>
        </label>
        <label class="graph-field">
          <span>Confidence</span>
          <el-select v-model="sourceConfidenceFilter" clearable placeholder="Any confidence" aria-label="Filter graph by source confidence">
            <el-option v-for="option in sourceConfidenceOptions" :key="option" :label="option" :value="option" />
          </el-select>
        </label>
        <label class="graph-field graph-field--wide">
          <span>Created range</span>
          <el-date-picker
            v-model="createdRange"
            type="datetimerange"
            start-placeholder="From"
            end-placeholder="To"
            value-format="YYYY-MM-DDTHH:mm:ss.SSS[Z]"
            aria-label="Filter graph by created date range"
          />
        </label>
        <label class="graph-field">
          <span>Depth</span>
          <el-input-number v-model="depthFilter" :min="1" :max="8" controls-position="right" aria-label="Graph traversal depth" />
        </label>
        <label class="graph-field">
          <span>Limit</span>
          <el-input-number v-model="limitFilter" :min="10" :max="300" controls-position="right" aria-label="Maximum graph nodes" />
        </label>
        <div class="graph-filter-actions">
          <AppButton variant="secondary" @click="resetFilters">Reset Filters</AppButton>
          <AppButton variant="primary" :loading="loading" @click="loadGraph">Apply Filters</AppButton>
        </div>
      </AppCard>
    </details>

    <AppLoadingState v-if="loading" label="Loading source-backed Knowledge Graph" />
    <AppErrorState v-else-if="errorMessage" title="Knowledge Graph could not load" :description="errorMessage" retry-label="Retry" @retry="loadGraph" />

    <AppCard v-else class="graph-workspace" as="section">
      <div class="graph-workspace__summary">
        <div>
          <div class="eyebrow">Knowledge Graph Scope</div>
          <h2>{{ graphTitle }}</h2>
          <p>{{ graph.nodes.length }} nodes and {{ graph.edges.length }} edges from persisted records.</p>
        </div>
        <div class="graph-workspace__badges">
          <AppBadge v-if="!graph.nodes.length" variant="warning">Empty</AppBadge>
          <AppBadge v-else variant="success">Real data only</AppBadge>
          <AppBadge variant="info">Manual links editable</AppBadge>
        </div>
      </div>

      <AppEmptyState
        v-if="!graph.nodes.length"
        title="No Knowledge Graph links match these filters"
        description="Create relationships through captures, concepts, source links, project applications, or manual relationships to grow this Knowledge Graph. BookOS will not invent graph nodes."
        compact
      >
        <template #actions>
          <UseCaseActionButton to="/use-cases/inspect-knowledge-graph" label="Learn Knowledge Graph workflow" variant="secondary" />
          <UseCaseActionButton to="/use-cases/review-concept-marker" label="Create source-backed concept" variant="ghost" />
        </template>
      </AppEmptyState>

      <div v-else class="graph-workspace__grid">
        <section class="graph-canvas-card" aria-labelledby="graph-canvas-title">
          <div class="graph-panel-heading">
            <div>
              <h3 id="graph-canvas-title">Visual Network</h3>
              <p>Click a node or edge to inspect source, confidence, and navigation actions.</p>
            </div>
          </div>
          <svg class="graph-canvas" viewBox="0 0 920 540" role="img" :aria-label="graphTextSummary">
            <defs>
              <marker id="graph-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
                <path d="M 0 0 L 10 5 L 0 10 z" fill="currentColor" />
              </marker>
            </defs>
            <g class="graph-edges" aria-hidden="true">
              <g v-for="edge in graph.edges" :key="edgeKey(edge)">
                <line
                  class="graph-edge"
                  :class="{ 'graph-edge--manual': !edge.systemCreated, 'graph-edge--system': edge.systemCreated }"
                  :x1="positionFor(edge.source).x"
                  :y1="positionFor(edge.source).y"
                  :x2="positionFor(edge.target).x"
                  :y2="positionFor(edge.target).y"
                  marker-end="url(#graph-arrow)"
                />
                <line
                  class="graph-edge-hit"
                  :x1="positionFor(edge.source).x"
                  :y1="positionFor(edge.source).y"
                  :x2="positionFor(edge.target).x"
                  :y2="positionFor(edge.target).y"
                  @click="selectEdge(edge)"
                />
              </g>
            </g>
            <g class="graph-nodes">
              <g
                v-for="item in positionedNodes"
                :key="item.node.id"
                class="graph-node"
                role="button"
                tabindex="0"
                :aria-label="`Inspect ${typeLabel(item.node.type)} ${item.node.label}`"
                :transform="`translate(${item.x} ${item.y})`"
                @click="selectNode(item.node)"
                @keydown.enter.prevent="selectNode(item.node)"
                @keydown.space.prevent="selectNode(item.node)"
              >
                <circle :r="nodeRadius(item.node.type)" :fill="nodeColor(item.node.type)" />
                <text y="4" text-anchor="middle">{{ shortNodeLabel(item.node) }}</text>
              </g>
            </g>
          </svg>
        </section>

        <section class="graph-side-panels" aria-label="Graph list fallback">
          <div class="graph-panel">
            <div class="graph-panel-heading">
              <h3>Nodes</h3>
              <span>{{ graph.nodes.length }}</span>
            </div>
            <div class="node-list">
              <button
                v-for="node in graph.nodes"
                :key="node.id"
                class="node-card"
                type="button"
                :aria-label="`Inspect ${typeLabel(node.type)} ${node.label}`"
                @click="selectNode(node)"
              >
                <AppBadge :variant="nodeVariant(node.type)" size="sm">{{ typeLabel(node.type) }}</AppBadge>
                <strong>{{ node.label }}</strong>
                <span>ID {{ node.entityId }}</span>
                <small v-if="node.sourceConfidence">Confidence: {{ node.sourceConfidence }}</small>
              </button>
            </div>
          </div>

          <div class="graph-panel">
            <div class="graph-panel-heading">
              <h3>Edges</h3>
              <span>{{ graph.edges.length }}</span>
            </div>
            <div class="edge-list">
              <article v-for="edge in graph.edges" :key="edgeKey(edge)" class="edge-card">
                <button class="edge-card__main" type="button" :aria-label="`Inspect ${typeLabel(edge.type)} relationship`" @click="selectEdge(edge)">
                  <AppBadge variant="info" size="sm">{{ typeLabel(edge.type) }}</AppBadge>
                  <span>{{ nodeLabel(edge.source) }}</span>
                  <b aria-hidden="true">→</b>
                  <span>{{ nodeLabel(edge.target) }}</span>
                </button>
                <div class="edge-card__meta">
                  <AppBadge :variant="edge.systemCreated ? 'neutral' : 'accent'" size="sm">
                    {{ edge.systemCreated ? 'System' : 'Manual' }}
                  </AppBadge>
                  <AppBadge v-if="edge.sourceConfidence" variant="success" size="sm">{{ edge.sourceConfidence }}</AppBadge>
                </div>
              </article>
            </div>
          </div>
        </section>
      </div>
    </AppCard>

    <details class="graph-disclosure relationship-disclosure" :open="relationshipEditorOpen" @toggle="handleRelationshipToggle">
      <summary>
        <span>Manual relationship editor</span>
        <small>{{ editingLinkId ? 'Editing a user-created link' : 'Advanced curation' }}</small>
      </summary>
      <AppCard class="relationship-editor" as="section" aria-label="Manual relationship editor">
        <div class="relationship-editor__header">
          <div>
            <div class="eyebrow">Relationship Editor</div>
            <h2>{{ editingLinkId ? 'Edit Manual Relationship' : 'Create Manual Relationship' }}</h2>
            <p>Only entities you can access can be linked. System-created relationships are protected from silent deletion.</p>
          </div>
          <AppButton v-if="editingLinkId || relationshipEditorOpen" variant="ghost" @click="clearRelationshipForm">Cancel Edit</AppButton>
        </div>
        <div class="relationship-form">
          <label class="graph-field">
            <span>Source type</span>
            <el-select v-model="relationshipForm.sourceType" filterable allow-create aria-label="Relationship source type">
              <el-option v-for="option in entityTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
            </el-select>
          </label>
          <label class="graph-field">
            <span>Source ID</span>
            <el-input-number v-model="relationshipForm.sourceId" :min="1" controls-position="right" aria-label="Relationship source id" />
          </label>
          <label class="graph-field">
            <span>Target type</span>
            <el-select v-model="relationshipForm.targetType" filterable allow-create aria-label="Relationship target type">
              <el-option v-for="option in entityTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
            </el-select>
          </label>
          <label class="graph-field">
            <span>Target ID</span>
            <el-input-number v-model="relationshipForm.targetId" :min="1" controls-position="right" aria-label="Relationship target id" />
          </label>
          <label class="graph-field">
            <span>Relationship</span>
            <el-select v-model="relationshipForm.relationType" filterable allow-create aria-label="Relationship type">
              <el-option v-for="option in relationshipTypeOptions" :key="option" :label="typeLabel(option)" :value="option" />
            </el-select>
          </label>
          <label class="graph-field">
            <span>Source link ID</span>
            <el-input-number
              v-model="relationshipForm.sourceReferenceId"
              :min="1"
              controls-position="right"
              placeholder="Optional"
              aria-label="Optional relationship source link id"
            />
          </label>
          <label class="graph-field graph-field--note">
            <span>Curator note</span>
            <el-input
              v-model="relationshipForm.note"
              type="textarea"
              :rows="3"
              maxlength="2000"
              show-word-limit
              placeholder="Why are these records related?"
              aria-label="Relationship curator note"
            />
          </label>
          <div class="relationship-form__actions">
            <AppButton variant="primary" :loading="savingRelationship" @click="saveRelationship">
              {{ editingLinkId ? 'Save Relationship' : 'Create Relationship' }}
            </AppButton>
          </div>
        </div>
      </AppCard>
    </details>

    <el-drawer v-model="nodeDrawerOpen" size="420px" title="Node Details" append-to-body>
      <div v-if="selectedNode" class="detail-drawer">
        <AppBadge :variant="nodeVariant(selectedNode.type)">{{ typeLabel(selectedNode.type) }}</AppBadge>
        <h2>{{ selectedNode.label }}</h2>
        <dl>
          <div><dt>Entity ID</dt><dd>{{ selectedNode.entityId }}</dd></div>
          <div><dt>Graph ID</dt><dd>{{ selectedNode.id }}</dd></div>
          <div v-if="selectedNode.sourceConfidence"><dt>Source confidence</dt><dd>{{ selectedNode.sourceConfidence }}</dd></div>
          <div v-if="selectedNode.sourceReferenceId"><dt>Source link</dt><dd>#{{ selectedNode.sourceReferenceId }}</dd></div>
          <div v-if="selectedNode.createdAt"><dt>Created</dt><dd>{{ formatDate(selectedNode.createdAt) }}</dd></div>
        </dl>
        <div class="drawer-actions">
          <AppButton variant="primary" @click="openSelectedNode">Open Entity</AppButton>
          <AppButton variant="secondary" :disabled="!canOpenNodeSource(selectedNode)" @click="openSelectedNodeSource">Open Source</AppButton>
          <AppButton variant="ghost" @click="useNodeAsRelationshipSource(selectedNode)">Use As Source</AppButton>
          <AppButton variant="ghost" @click="useNodeAsRelationshipTarget(selectedNode)">Use As Target</AppButton>
        </div>
      </div>
    </el-drawer>

    <el-drawer v-model="edgeDrawerOpen" size="460px" title="Relationship Details" append-to-body>
      <div v-if="selectedEdge" class="detail-drawer">
        <div class="drawer-badges">
          <AppBadge variant="info">{{ typeLabel(selectedEdge.type) }}</AppBadge>
          <AppBadge :variant="selectedEdge.systemCreated ? 'neutral' : 'accent'">
            {{ selectedEdge.systemCreated ? 'System-created' : 'Manual' }}
          </AppBadge>
        </div>
        <h2>{{ nodeLabel(selectedEdge.source) }} → {{ nodeLabel(selectedEdge.target) }}</h2>
        <dl>
          <div><dt>Source node</dt><dd>{{ selectedEdge.source }}</dd></div>
          <div><dt>Target node</dt><dd>{{ selectedEdge.target }}</dd></div>
          <div v-if="selectedEdge.entityLinkId"><dt>Entity link</dt><dd>#{{ selectedEdge.entityLinkId }}</dd></div>
          <div v-if="selectedEdge.sourceReferenceId"><dt>Source link</dt><dd>#{{ selectedEdge.sourceReferenceId }}</dd></div>
          <div v-if="selectedEdge.sourceConfidence"><dt>Source confidence</dt><dd>{{ selectedEdge.sourceConfidence }}</dd></div>
          <div v-if="selectedEdge.createdBy"><dt>Created by</dt><dd>{{ selectedEdge.createdBy }}</dd></div>
          <div v-if="selectedEdge.createdAt"><dt>Created</dt><dd>{{ formatDate(selectedEdge.createdAt) }}</dd></div>
        </dl>
        <p v-if="selectedEdge.note" class="relationship-note">{{ selectedEdge.note }}</p>
        <div class="drawer-actions">
          <AppButton variant="primary" @click="openEdgeNode(selectedEdge.source)">Open Source Node</AppButton>
          <AppButton variant="secondary" @click="openEdgeNode(selectedEdge.target)">Open Target Node</AppButton>
          <AppButton variant="secondary" :disabled="!selectedEdge.sourceReferenceId" @click="openSelectedEdgeSource">Open Source</AppButton>
          <AppButton v-if="canEditEdge(selectedEdge)" variant="ghost" @click="editSelectedEdge">Edit Manual Link</AppButton>
          <AppButton v-if="canEditEdge(selectedEdge)" variant="danger" @click="deleteSelectedEdge">Delete Manual Link</AppButton>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookGraph, getConceptGraph, getProjectGraph, getWorkspaceGraph, type WorkspaceGraphQuery } from '../api/graph'
import { createEntityLink, deleteEntityLink, updateEntityLink } from '../api/entityLinks'
import { recordUseCaseEvent } from '../api/useCaseProgress'
import { useOpenSource } from '../composables/useOpenSource'
import type { EntityLinkPayload, GraphEdgeRecord, GraphNodeRecord, GraphRecord, SourceConfidence } from '../types'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import UseCaseActionButton from '../components/use-case/UseCaseActionButton.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'

interface PositionedNode {
  node: GraphNodeRecord
  x: number
  y: number
}

interface RelationshipForm {
  sourceType: string
  sourceId?: number
  targetType: string
  targetId?: number
  relationType: string
  sourceReferenceId?: number
  note: string
}

const route = useRoute()
const router = useRouter()
const { openSource } = useOpenSource()

const graph = ref<GraphRecord>({ nodes: [], edges: [] })
const loading = ref(false)
const savingRelationship = ref(false)
const errorMessage = ref('')
const bookIdFilter = ref<number | undefined>(initialId('bookId'))
const conceptIdFilter = ref<number | undefined>(initialId('conceptId'))
const projectIdFilter = ref<number | undefined>(initialId('projectId'))
const entityTypeFilter = ref(queryString('entityType'))
const relationshipTypeFilter = ref(queryString('relationshipType'))
const sourceConfidenceFilter = ref<SourceConfidence | ''>(queryString('sourceConfidence') as SourceConfidence | '')
const createdRange = ref<[string, string] | null>(initialDateRange())
const depthFilter = ref<number | undefined>(initialNumberQuery('depth') ?? 3)
const limitFilter = ref<number | undefined>(initialNumberQuery('limit') ?? 120)
const selectedNode = ref<GraphNodeRecord | null>(null)
const selectedEdge = ref<GraphEdgeRecord | null>(null)
const nodeDrawerOpen = ref(false)
const edgeDrawerOpen = ref(false)
const editingLinkId = ref<number | null>(null)
const relationshipEditorOpen = ref(false)
const graphEventRecorded = ref(false)
const graphUseCaseSlugs = [
  'inspect-knowledge-graph',
  'review-concept-marker',
  'search-rediscover-knowledge',
]
const graphWorkflowLoop = ['Source-backed record', 'Relationship', 'Knowledge Graph inspection', 'Open source']

const relationshipForm = reactive<RelationshipForm>({
  sourceType: 'BOOK',
  sourceId: undefined,
  targetType: 'CONCEPT',
  targetId: undefined,
  relationType: 'RELATED_TO',
  sourceReferenceId: undefined,
  note: '',
})

const entityTypeOptions = [
  'BOOK',
  'NOTE',
  'NOTE_BLOCK',
  'RAW_CAPTURE',
  'QUOTE',
  'ACTION_ITEM',
  'CONCEPT',
  'KNOWLEDGE_OBJECT',
  'DAILY_PROMPT',
  'FORUM_THREAD',
  'SOURCE_REFERENCE',
  'PROJECT',
  'GAME_PROJECT',
  'PROJECT_PROBLEM',
  'PROJECT_APPLICATION',
  'DESIGN_DECISION',
  'PLAYTEST_FINDING',
  'PROJECT_LENS_REVIEW',
]
const relationshipTypeOptions = [
  'MENTIONS',
  'MENTIONS_CONCEPT',
  'SOURCE_OF',
  'DERIVED_FROM',
  'RELATED_TO',
  'DISCUSSES',
  'APPLIES_TO',
  'EVIDENCE_FOR',
  'DECISION_FOR',
  'FINDING_FOR',
  'REVIEWS_WITH_LENS',
]
const sourceConfidenceOptions: SourceConfidence[] = ['LOW', 'MEDIUM', 'HIGH']

const isBookRoute = computed(() => route.name === 'graph-book')
const isConceptRoute = computed(() => route.name === 'graph-concept')
const isProjectRoute = computed(() => route.name === 'graph-project')

const graphTitle = computed(() => {
  if (isBookRoute.value) return `Book Knowledge Graph #${route.params.bookId}`
  if (isConceptRoute.value) return `Concept Knowledge Graph #${route.params.conceptId}`
  if (isProjectRoute.value) return `Project Knowledge Graph #${route.params.projectId}`
  if (projectIdFilter.value) return `Project Knowledge Graph #${projectIdFilter.value}`
  if (conceptIdFilter.value) return `Concept Knowledge Graph #${conceptIdFilter.value}`
  if (bookIdFilter.value) return `Book Knowledge Graph #${bookIdFilter.value}`
  return 'Workspace Knowledge Graph'
})

const graphTextSummary = computed(() => {
  const nodeTypes = [...new Set(graph.value.nodes.map((node) => typeLabel(node.type)))].join(', ') || 'none'
  return `Knowledge graph with ${graph.value.nodes.length} nodes, ${graph.value.edges.length} relationships, node types: ${nodeTypes}.`
})

const filtersActive = computed(() =>
  Boolean(
    bookIdFilter.value ||
      conceptIdFilter.value ||
      projectIdFilter.value ||
      entityTypeFilter.value ||
      relationshipTypeFilter.value ||
      sourceConfidenceFilter.value ||
      createdRange.value ||
      depthFilter.value !== 3 ||
      limitFilter.value !== 120,
  ),
)

const graphNextStep = computed(() => {
  if (!graph.value.nodes.length) {
    return {
      title: 'Create one source-backed record first',
      description: 'The Knowledge Graph stays honest when it is built from real notes, captures, quotes, concepts, projects, or source links.',
      primaryLabel: 'Start First Loop',
      primaryTo: { name: 'guided-first-loop' },
      secondaryLabel: 'Learn Knowledge Graph Workflow',
      secondaryTo: { name: 'use-case-detail', params: { slug: 'inspect-knowledge-graph' } },
    }
  }

  if (!graph.value.edges.length) {
    return {
      title: 'Connect two accessible records',
      description: 'This Knowledge Graph has nodes but no relationships. Add a manual link only when you can explain why the records belong together.',
      primaryLabel: 'Create Relationship',
      primaryTo: null,
      secondaryLabel: null,
      secondaryTo: null,
    }
  }

  return {
    title: 'Inspect a real relationship',
    description: 'Start by opening a node or edge, then use source links before editing or creating manual links.',
    primaryLabel: 'Create Relationship',
    primaryTo: null,
    secondaryLabel: filtersActive.value ? 'Reset Filters' : null,
    secondaryTo: null,
  }
})

const positionedNodes = computed<PositionedNode[]>(() => {
  const nodes = graph.value.nodes
  if (!nodes.length) return []
  if (nodes.length === 1) return [{ node: nodes[0], x: 460, y: 270 }]

  const centerX = 460
  const centerY = 270
  const radius = Math.min(215, Math.max(130, 54 + nodes.length * 7))
  return nodes.map((node, index) => {
    const angle = (Math.PI * 2 * index) / nodes.length - Math.PI / 2
    const isPrimary = index === 0 && (isBookRoute.value || isConceptRoute.value || isProjectRoute.value)
    return {
      node,
      x: isPrimary ? centerX : centerX + Math.cos(angle) * radius,
      y: isPrimary ? centerY : centerY + Math.sin(angle) * radius,
    }
  })
})

const positionMap = computed(() => new Map(positionedNodes.value.map((item) => [item.node.id, { x: item.x, y: item.y }])))

onMounted(loadGraph)

watch(
  () => [route.name, route.params.bookId, route.params.conceptId, route.params.projectId, route.query],
  () => {
    syncFiltersFromRoute()
    void loadGraph()
  },
)

async function loadGraph() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = graphParams()
    if (isBookRoute.value) {
      graph.value = await getBookGraph(String(route.params.bookId), scopedParams(params))
      recordGraphOpened()
      return
    }
    if (isConceptRoute.value) {
      graph.value = await getConceptGraph(String(route.params.conceptId), scopedParams(params))
      recordGraphOpened()
      return
    }
    if (isProjectRoute.value) {
      graph.value = await getProjectGraph(String(route.params.projectId), scopedParams(params))
      recordGraphOpened()
      return
    }
    graph.value = await getWorkspaceGraph(params)
    recordGraphOpened()
  } catch {
    graph.value = { nodes: [], edges: [] }
    errorMessage.value = 'Check backend availability, graph filters, and permissions, then retry.'
  } finally {
    loading.value = false
  }
}

function recordGraphOpened() {
  if (graphEventRecorded.value) return
  graphEventRecorded.value = true
  void recordUseCaseEvent({
    eventType: 'GRAPH_OPENED',
    contextType: String(route.name ?? 'graph').toUpperCase(),
    contextId: String(route.params.bookId ?? route.params.conceptId ?? route.params.projectId ?? 'workspace'),
  }).catch(() => undefined)
}

function resetFilters() {
  entityTypeFilter.value = ''
  relationshipTypeFilter.value = ''
  sourceConfidenceFilter.value = ''
  createdRange.value = null
  depthFilter.value = 3
  limitFilter.value = 120
  if (!isBookRoute.value && !isConceptRoute.value && !isProjectRoute.value) {
    bookIdFilter.value = undefined
    conceptIdFilter.value = undefined
    projectIdFilter.value = undefined
  }
  void loadGraph()
}

function handleGraphPrimaryAction() {
  if (!graph.value.nodes.length) {
    void router.push({ name: 'guided-first-loop' })
    return
  }
  openRelationshipEditor()
}

function handleGraphSecondaryAction() {
  resetFilters()
}

function graphParams(): WorkspaceGraphQuery {
  return pruneParams({
    bookId: isBookRoute.value ? null : (bookIdFilter.value ?? null),
    conceptId: isConceptRoute.value ? null : (conceptIdFilter.value ?? null),
    projectId: isProjectRoute.value ? null : (projectIdFilter.value ?? null),
    entityType: entityTypeFilter.value || null,
    relationshipType: relationshipTypeFilter.value || null,
    sourceConfidence: sourceConfidenceFilter.value || null,
    createdFrom: createdRange.value?.[0] ?? null,
    createdTo: createdRange.value?.[1] ?? null,
    depth: depthFilter.value ?? null,
    limit: limitFilter.value ?? null,
  })
}

function scopedParams(params: WorkspaceGraphQuery): Omit<WorkspaceGraphQuery, 'bookId' | 'conceptId' | 'projectId'> {
  const { bookId: _bookId, conceptId: _conceptId, projectId: _projectId, ...filters } = params
  return filters
}

function pruneParams(params: WorkspaceGraphQuery): WorkspaceGraphQuery {
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== null && value !== undefined && value !== '')) as WorkspaceGraphQuery
}

function syncFiltersFromRoute() {
  bookIdFilter.value = initialId('bookId')
  conceptIdFilter.value = initialId('conceptId')
  projectIdFilter.value = initialId('projectId')
  entityTypeFilter.value = queryString('entityType')
  relationshipTypeFilter.value = queryString('relationshipType')
  sourceConfidenceFilter.value = queryString('sourceConfidence') as SourceConfidence | ''
  createdRange.value = initialDateRange()
  depthFilter.value = initialNumberQuery('depth') ?? 3
  limitFilter.value = initialNumberQuery('limit') ?? 120
}

function selectNode(node: GraphNodeRecord) {
  selectedNode.value = node
  nodeDrawerOpen.value = true
}

function selectEdge(edge: GraphEdgeRecord) {
  selectedEdge.value = edge
  edgeDrawerOpen.value = true
}

function openSelectedNode() {
  if (!selectedNode.value) return
  void openGraphNode(selectedNode.value)
}

function openSelectedNodeSource() {
  if (!selectedNode.value) return
  void openNodeSource(selectedNode.value)
}

function openSelectedEdgeSource() {
  if (!selectedEdge.value?.sourceReferenceId) return
  void openSource({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: selectedEdge.value.sourceReferenceId,
    sourceReferenceId: selectedEdge.value.sourceReferenceId,
  })
}

function openEdgeNode(nodeId: string) {
  const node = graph.value.nodes.find((item) => item.id === nodeId)
  if (!node) {
    ElMessage.warning('This graph node is not available in the current filtered result.')
    return
  }
  void openGraphNode(node)
}

async function openGraphNode(node: GraphNodeRecord) {
  if (node.type === 'SOURCE_REFERENCE') {
    await openNodeSource(node)
    return
  }

  const target = routeForNode(node)
  if (target) {
    await router.push(target)
    return
  }

  if (canOpenNodeSource(node)) {
    await openNodeSource(node)
    return
  }

  ElMessage.info('No detail route exists for this graph node yet.')
}

async function openNodeSource(node: GraphNodeRecord) {
  const sourceReferenceId = node.type === 'SOURCE_REFERENCE' ? node.entityId : node.sourceReferenceId
  if (!sourceReferenceId) {
    ElMessage.info('This node has no source link to open.')
    return
  }
  await openSource({
    sourceType: node.type,
    sourceId: node.entityId,
    sourceReferenceId,
  })
}

function canOpenNodeSource(node: GraphNodeRecord) {
  return node.type === 'SOURCE_REFERENCE' || Boolean(node.sourceReferenceId)
}

function useNodeAsRelationshipSource(node: GraphNodeRecord) {
  relationshipForm.sourceType = normalizeType(node.type)
  relationshipForm.sourceId = node.entityId
  relationshipForm.sourceReferenceId = node.sourceReferenceId ?? relationshipForm.sourceReferenceId
  relationshipEditorOpen.value = true
  nodeDrawerOpen.value = false
}

function useNodeAsRelationshipTarget(node: GraphNodeRecord) {
  relationshipForm.targetType = normalizeType(node.type)
  relationshipForm.targetId = node.entityId
  relationshipForm.sourceReferenceId = node.sourceReferenceId ?? relationshipForm.sourceReferenceId
  relationshipEditorOpen.value = true
  nodeDrawerOpen.value = false
}

function openRelationshipEditor() {
  clearRelationshipForm(false)
  relationshipEditorOpen.value = true
  if (selectedNode.value) {
    useNodeAsRelationshipSource(selectedNode.value)
  }
}

async function saveRelationship() {
  const payload = relationshipPayload()
  if (!payload) return

  savingRelationship.value = true
  try {
    if (editingLinkId.value) {
      await updateEntityLink(editingLinkId.value, payload)
      ElMessage.success('Relationship updated.')
    } else {
      await createEntityLink(payload)
      ElMessage.success('Relationship created.')
    }
    clearRelationshipForm()
    await loadGraph()
  } catch {
    ElMessage.error('Relationship could not be saved. Check ownership, entity IDs, and source link permissions.')
  } finally {
    savingRelationship.value = false
  }
}

function relationshipPayload(): EntityLinkPayload | null {
  if (!relationshipForm.sourceType || !relationshipForm.sourceId || !relationshipForm.targetType || !relationshipForm.targetId || !relationshipForm.relationType) {
    ElMessage.warning('Source, target, and relationship type are required.')
    return null
  }

  return {
    sourceType: normalizeType(relationshipForm.sourceType),
    sourceId: relationshipForm.sourceId,
    targetType: normalizeType(relationshipForm.targetType),
    targetId: relationshipForm.targetId,
    relationType: normalizeType(relationshipForm.relationType),
    sourceReferenceId: relationshipForm.sourceReferenceId ?? null,
    note: relationshipForm.note?.trim() || null,
  }
}

function editSelectedEdge() {
  if (!selectedEdge.value || !canEditEdge(selectedEdge.value)) return
  const source = parseGraphNodeId(selectedEdge.value.source)
  const target = parseGraphNodeId(selectedEdge.value.target)
  if (!source || !target) {
    ElMessage.warning('This relationship cannot be edited because its graph IDs are invalid.')
    return
  }

  editingLinkId.value = selectedEdge.value.entityLinkId ?? null
  relationshipEditorOpen.value = true
  relationshipForm.sourceType = source.type
  relationshipForm.sourceId = source.id
  relationshipForm.targetType = target.type
  relationshipForm.targetId = target.id
  relationshipForm.relationType = selectedEdge.value.type
  relationshipForm.sourceReferenceId = selectedEdge.value.sourceReferenceId ?? undefined
  relationshipForm.note = selectedEdge.value.note ?? ''
  edgeDrawerOpen.value = false
}

async function deleteSelectedEdge() {
  if (!selectedEdge.value || !canEditEdge(selectedEdge.value) || !selectedEdge.value.entityLinkId) return

  try {
    await ElMessageBox.confirm('Delete this user-created relationship? System-created links remain protected.', 'Delete relationship', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await deleteEntityLink(selectedEdge.value.entityLinkId)
    ElMessage.success('Relationship deleted.')
    edgeDrawerOpen.value = false
    selectedEdge.value = null
    await loadGraph()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Relationship could not be deleted.')
    }
  }
}

function canEditEdge(edge: GraphEdgeRecord) {
  return Boolean(edge.entityLinkId && !edge.systemCreated)
}

function clearRelationshipForm(closeEditor = true) {
  editingLinkId.value = null
  relationshipForm.sourceType = 'BOOK'
  relationshipForm.sourceId = undefined
  relationshipForm.targetType = 'CONCEPT'
  relationshipForm.targetId = undefined
  relationshipForm.relationType = 'RELATED_TO'
  relationshipForm.sourceReferenceId = undefined
  relationshipForm.note = ''
  if (closeEditor) relationshipEditorOpen.value = false
}

function handleRelationshipToggle(event: Event) {
  relationshipEditorOpen.value = (event.target as HTMLDetailsElement).open
}

function routeForNode(node: GraphNodeRecord) {
  if (node.type === 'BOOK') return { name: 'book-detail', params: { id: node.entityId } }
  if (node.type === 'NOTE') return { name: 'note-detail', params: { id: node.entityId } }
  if (node.type === 'QUOTE') return { name: 'quote-detail', params: { id: node.entityId } }
  if (node.type === 'ACTION_ITEM') return { name: 'action-item-detail', params: { id: node.entityId } }
  if (node.type === 'CONCEPT') return { name: 'concept-detail', params: { id: node.entityId } }
  if (node.type === 'KNOWLEDGE_OBJECT') return { name: 'knowledge-detail', params: { id: node.entityId } }
  if (node.type === 'FORUM_THREAD') return { name: 'forum-thread', params: { id: node.entityId } }
  if (node.type === 'PROJECT' || node.type === 'GAME_PROJECT') return { name: 'project-detail', params: { id: node.entityId } }
  if (node.type === 'PROJECT_PROBLEM') return { name: 'project-problems', params: { id: projectIdFromNode(node) } }
  if (node.type === 'PROJECT_APPLICATION') return { name: 'project-applications', params: { id: projectIdFromNode(node) } }
  if (node.type === 'DESIGN_DECISION') return { name: 'project-decisions', params: { id: projectIdFromNode(node) } }
  if (node.type === 'PLAYTEST_FINDING') return { name: 'project-playtests', params: { id: projectIdFromNode(node) } }
  if (node.type === 'PROJECT_LENS_REVIEW') return { name: 'project-lens-reviews', params: { id: projectIdFromNode(node) } }
  if (node.type === 'DAILY_PROMPT') return { name: 'daily' }
  return null
}

function projectIdFromNode(node: GraphNodeRecord) {
  const projectEdge = graph.value.edges.find(
    (edge) =>
      (edge.source === node.id && edge.target.startsWith('project:')) ||
      (edge.target === node.id && edge.source.startsWith('project:')),
  )
  if (!projectEdge) return String(projectIdFilter.value ?? route.params.projectId ?? '')
  const projectNodeId = projectEdge.source.startsWith('project:') ? projectEdge.source : projectEdge.target
  return projectNodeId.replace('project:', '')
}

function positionFor(nodeId: string) {
  return positionMap.value.get(nodeId) ?? { x: 460, y: 270 }
}

function nodeLabel(nodeId: string) {
  return graph.value.nodes.find((node) => node.id === nodeId)?.label ?? nodeId
}

function shortNodeLabel(node: GraphNodeRecord) {
  const label = node.label || node.id
  return label.length > 18 ? `${label.slice(0, 15)}...` : label
}

function edgeKey(edge: GraphEdgeRecord) {
  return `${edge.source}-${edge.target}-${edge.type}-${edge.entityLinkId ?? edge.sourceReferenceId ?? edge.createdAt ?? 'system'}`
}

function parseGraphNodeId(nodeId: string) {
  const [type, id] = nodeId.split(':')
  const parsedId = Number(id)
  if (!type || !Number.isFinite(parsedId) || parsedId <= 0) return null
  return { type: normalizeType(type), id: parsedId }
}

function initialId(key: 'bookId' | 'conceptId' | 'projectId') {
  const raw = route.params[key] ?? route.query[key]
  return parsePositiveNumber(Array.isArray(raw) ? raw[0] : raw)
}

function initialNumberQuery(key: string) {
  const raw = route.query[key]
  return parsePositiveNumber(Array.isArray(raw) ? raw[0] : raw)
}

function parsePositiveNumber(raw: unknown) {
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
}

function queryString(key: string) {
  const raw = route.query[key]
  return typeof raw === 'string' ? raw.toUpperCase() : ''
}

function initialDateRange(): [string, string] | null {
  const from = route.query.createdFrom
  const to = route.query.createdTo
  if (typeof from === 'string' && typeof to === 'string') return [from, to]
  return null
}

function typeLabel(value: string) {
  const normalized = normalizeType(value)
  const friendlyLabels: Record<string, string> = {
    ACTION_ITEM: 'Action',
    KNOWLEDGE_OBJECT: 'Design Knowledge',
    SOURCE_REFERENCE: 'Source Link',
    ENTITY_LINK: 'Relationship',
    GAME_PROJECT: 'Project',
  }
  if (friendlyLabels[normalized]) return friendlyLabels[normalized]
  return normalized
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/^\w|\s\w/g, (match) => match.toUpperCase())
}

function normalizeType(value: string) {
  return value.trim().replaceAll(' ', '_').toUpperCase()
}

function nodeVariant(type: string) {
  if (type === 'BOOK') return 'primary'
  if (type === 'QUOTE') return 'accent'
  if (type === 'ACTION_ITEM') return 'warning'
  if (type === 'SOURCE_REFERENCE') return 'info'
  if (type === 'CONCEPT' || type === 'KNOWLEDGE_OBJECT') return 'success'
  if (type === 'PROJECT' || type === 'GAME_PROJECT' || type.startsWith('PROJECT_') || type === 'DESIGN_DECISION' || type === 'PLAYTEST_FINDING') {
    return 'primary'
  }
  return 'neutral'
}

function nodeRadius(type: string) {
  if (type === 'BOOK' || type === 'PROJECT' || type === 'GAME_PROJECT') return 42
  if (type === 'SOURCE_REFERENCE') return 30
  return 34
}

function nodeColor(type: string) {
  if (type === 'BOOK') return 'var(--bookos-primary)'
  if (type === 'PROJECT' || type === 'GAME_PROJECT') return 'var(--bookos-accent)'
  if (type === 'CONCEPT' || type === 'KNOWLEDGE_OBJECT') return 'var(--bookos-success)'
  if (type === 'QUOTE') return 'var(--bookos-info)'
  if (type === 'ACTION_ITEM') return 'var(--bookos-warning)'
  if (type === 'SOURCE_REFERENCE') return 'var(--bookos-text-secondary)'
  return 'var(--bookos-surface-muted)'
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
</script>

<style scoped>
.graph-page,
.graph-workspace,
.graph-workspace__grid,
.graph-side-panels,
.graph-panel,
.node-list,
.edge-list,
.graph-disclosure,
.relationship-editor,
.relationship-form,
.detail-drawer,
.drawer-actions {
  display: grid;
  gap: var(--space-4);
}

.graph-disclosure summary {
  min-height: 44px;
  padding: var(--space-3) var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: color-mix(in srgb, var(--bookos-surface) 86%, var(--bookos-primary-soft));
  color: var(--bookos-text-primary);
  cursor: pointer;
  font-weight: 900;
  list-style: none;
}

.graph-disclosure summary::-webkit-details-marker {
  display: none;
}

.graph-disclosure summary::after {
  content: "+";
  color: var(--bookos-primary);
}

.graph-disclosure[open] summary::after {
  content: "-";
}

.graph-disclosure summary small {
  margin-left: auto;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.graph-filters {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--space-3);
}

.graph-field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.graph-field--wide,
.graph-field--note {
  grid-column: span 2;
}

.graph-field :deep(.el-input-number),
.graph-field :deep(.el-select),
.graph-field :deep(.el-date-editor),
.graph-field :deep(.el-textarea) {
  width: 100%;
}

.graph-filter-actions,
.relationship-form__actions {
  display: flex;
  gap: var(--space-2);
  align-items: end;
  justify-content: flex-end;
}

.graph-workspace,
.relationship-editor {
  padding: var(--space-5);
}

.graph-workspace__summary,
.relationship-editor__header,
.graph-panel-heading {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: start;
  flex-wrap: wrap;
}

.graph-workspace__badges,
.drawer-badges,
.edge-card__meta {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.graph-workspace__summary h2,
.relationship-editor__header h2,
.graph-panel-heading h3,
.detail-drawer h2 {
  margin: 0;
  color: var(--bookos-text-primary);
}

.graph-workspace__summary p,
.relationship-editor__header p,
.graph-panel-heading p {
  margin: var(--space-2) 0 0;
  color: var(--bookos-text-secondary);
}

.graph-workspace__grid {
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 0.65fr);
  align-items: start;
}

.graph-canvas-card,
.graph-panel {
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
  padding: var(--space-4);
}

.graph-canvas {
  width: 100%;
  min-height: 460px;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background:
    radial-gradient(circle at 20% 15%, color-mix(in srgb, var(--bookos-accent-soft) 52%, transparent), transparent 32%),
    radial-gradient(circle at 80% 25%, color-mix(in srgb, var(--bookos-primary-soft) 46%, transparent), transparent 34%),
    var(--bookos-surface);
  color: var(--bookos-primary);
}

.graph-edge {
  stroke: color-mix(in srgb, var(--bookos-primary) 42%, var(--bookos-border));
  stroke-width: 2;
}

.graph-edge--manual {
  stroke: var(--bookos-accent);
  stroke-dasharray: 8 5;
}

.graph-edge-hit {
  stroke: transparent;
  stroke-width: 18;
  cursor: pointer;
}

.graph-node {
  cursor: pointer;
  outline: none;
}

.graph-node circle {
  stroke: var(--bookos-surface);
  stroke-width: 5;
  filter: drop-shadow(0 9px 16px rgba(33, 45, 43, 0.18));
}

.graph-node text {
  fill: var(--bookos-text-primary);
  font-size: 0.72rem;
  font-weight: 900;
  pointer-events: none;
}

.graph-node:focus-visible circle,
.graph-node:hover circle {
  stroke: var(--bookos-focus);
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
  background: var(--bookos-surface);
}

.node-card,
.edge-card__main {
  color: var(--bookos-text-primary);
  text-align: left;
  cursor: pointer;
}

.node-card {
  display: grid;
  gap: var(--space-2);
}

.node-card:hover,
.node-card:focus-visible,
.edge-card__main:hover,
.edge-card__main:focus-visible {
  border-color: color-mix(in srgb, var(--bookos-primary) 36%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-primary-soft) 24%, var(--bookos-surface));
}

.node-card strong,
.edge-card span {
  overflow-wrap: anywhere;
}

.node-card span,
.node-card small {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
}

.edge-card {
  display: grid;
  gap: var(--space-2);
}

.edge-card__main {
  width: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  border: 0;
  background: transparent;
  padding: 0;
  font: inherit;
}

.edge-card__main span {
  color: var(--bookos-text-primary);
  font-weight: 800;
}

.relationship-form {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.detail-drawer dl {
  display: grid;
  gap: var(--space-3);
  margin: 0;
}

.detail-drawer dl div {
  display: grid;
  grid-template-columns: 130px minmax(0, 1fr);
  gap: var(--space-3);
  padding-block-end: var(--space-2);
  border-bottom: 1px solid var(--bookos-border);
}

.detail-drawer dt {
  color: var(--bookos-text-tertiary);
  font-weight: 800;
}

.detail-drawer dd {
  margin: 0;
  color: var(--bookos-text-primary);
  overflow-wrap: anywhere;
}

.relationship-note {
  margin: 0;
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
}

.drawer-actions {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

@media (max-width: 1180px) {
  .graph-filters,
  .relationship-form {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .graph-field--wide,
  .graph-field--note {
    grid-column: span 3;
  }

  .graph-workspace__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .graph-filters,
  .relationship-form {
    grid-template-columns: 1fr;
  }

  .graph-field--wide,
  .graph-field--note {
    grid-column: auto;
  }

  .graph-canvas {
    min-height: 360px;
  }

  .detail-drawer dl div {
    grid-template-columns: 1fr;
    gap: var(--space-1);
  }
}
</style>
