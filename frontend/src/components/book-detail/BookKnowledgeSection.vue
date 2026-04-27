<template>
  <section class="knowledge-section" aria-labelledby="knowledge-section-title">
    <div class="knowledge-section__heading">
      <div>
        <div class="eyebrow">Middle Knowledge Layer</div>
        <h2 id="knowledge-section-title">Graph, concepts, and active lenses</h2>
      </div>
      <AppBadge v-if="!directConcepts.length && !directLenses.length" variant="warning" size="sm">No extracted data yet</AppBadge>
    </div>

    <div class="knowledge-section__grid">
      <AppCard as="article" class="graph-card" variant="default">
        <div class="card-heading">
          <div>
            <div class="eyebrow">Knowledge Graph Preview</div>
            <h3>Current book network</h3>
          </div>
          <AppIconButton label="About this knowledge graph preview" tooltip="Derived from source references, concepts, notes, quotes, actions, and entity links" variant="ghost">
            i
          </AppIconButton>
        </div>

        <div v-if="graphNodesAround.length" class="graph-preview" role="img" :aria-label="graphSummary">
          <button
            class="graph-node graph-node--book"
            type="button"
            :aria-label="`Current book node: ${book.title}`"
            @click="$emit('open-graph')"
          >
            {{ book.title }}
          </button>
          <button
            v-for="(node, index) in graphNodesAround"
            :key="`${node.type}-${node.id ?? node.name}`"
            class="graph-node graph-node--concept"
            :class="[`graph-node--${index + 1}`, `graph-node--${node.edgeStrength ?? 'weak'}`]"
            type="button"
            :aria-label="`${node.type ?? 'Graph'} node ${node.name}, ${node.edgeStrength ?? 'weak'} relationship`"
            @click="openGraphNode(node)"
          >
            {{ node.name }}
          </button>
          <span
            v-for="(node, index) in graphNodesAround"
            :key="`${node.type}-${node.id ?? node.name}-edge`"
            class="graph-edge"
            :class="[`graph-edge--${index + 1}`, `graph-edge--${node.edgeStrength ?? 'weak'}`]"
            aria-hidden="true"
          />
        </div>

        <AppEmptyState
          v-else
          title="No graph nodes yet"
          description="Add source-backed notes, concepts, quotes, or action items to build a graph preview."
          compact
        />

        <div class="graph-legend" aria-label="Graph edge strength legend">
          <span><i class="graph-legend__line graph-legend__line--strong" />Strong</span>
          <span><i class="graph-legend__line graph-legend__line--medium" />Medium</span>
          <span><i class="graph-legend__line graph-legend__line--weak" />Weak</span>
        </div>

        <AppButton variant="secondary" @click="$emit('open-graph')">View graph</AppButton>
      </AppCard>

      <AppCard as="article" class="concept-card" variant="default">
        <div class="card-heading">
          <div>
            <div class="eyebrow">Extracted Concepts</div>
            <h3>Central ideas</h3>
          </div>
          <AppButton variant="text" @click="$emit('open-concept', 'all')">View all concepts</AppButton>
        </div>

        <div v-if="conceptRows.length" class="concept-table" role="table" aria-label="Extracted concepts by relevance and mentions">
          <div class="concept-table__head" role="row">
            <span role="columnheader">Concept</span>
            <span role="columnheader">Relevance</span>
            <span role="columnheader">Mentions</span>
          </div>
          <button
            v-for="concept in conceptRows"
            :key="concept.name"
            class="concept-table__row"
            type="button"
            role="row"
            :aria-label="`Open concept ${concept.name}, relevance ${concept.relevance} out of 100, ${concept.mentions} mentions`"
            @click="$emit('open-concept', concept.name)"
          >
            <span role="cell">{{ concept.name }}</span>
            <span class="relevance-meter" role="cell" :aria-label="`${concept.relevance} percent relevance`">
              <i
                v-for="tick in 5"
                :key="tick"
                :class="{ 'relevance-meter__tick--on': tick <= relevanceTicks(concept.relevance) }"
                aria-hidden="true"
              />
              <b>{{ concept.relevance }}%</b>
            </span>
            <span role="cell">{{ concept.mentions }}</span>
          </button>
        </div>

        <AppEmptyState v-else title="No extracted concepts yet" description="Concepts will appear here after extraction or tagging." compact />
      </AppCard>

      <AppCard as="article" class="lenses-card" variant="default">
        <div class="card-heading">
          <div>
            <div class="eyebrow">Design Lenses (Active)</div>
            <h3>Ways to read this book</h3>
          </div>
          <AppButton variant="text" @click="$emit('open-lens', 'all')">Manage lenses</AppButton>
        </div>

        <div v-if="activeLenses.length" class="lens-list">
          <button
            v-for="lens in activeLenses"
            :key="lens.name"
            class="lens-item"
            type="button"
            :aria-label="`Open lens ${lens.name}`"
            @click="$emit('open-lens', lens.name)"
          >
            <span class="lens-item__icon" aria-hidden="true">{{ lens.icon ?? lens.name.slice(0, 2).toUpperCase() }}</span>
            <span class="lens-item__copy">
              <strong>{{ lens.name }}</strong>
              <small>{{ lens.description || 'Use this lens to inspect the design decisions behind the reading.' }}</small>
            </span>
            <AppBadge variant="primary" size="sm">{{ lens.count ?? 0 }}</AppBadge>
          </button>
        </div>

        <AppEmptyState v-else title="No active lenses yet" description="Activate lenses to turn this book into design questions." compact />
      </AppCard>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { BookConceptPreview, BookLensPreview, BookRecord, KnowledgeEdgeStrength } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppIconButton from '../ui/AppIconButton.vue'

const props = defineProps<{
  book: BookRecord
}>()

const emit = defineEmits<{
  'open-graph': []
  'open-concept': [name: string]
  'open-lens': [name: string]
}>()

const directConcepts = computed(() => normalizeConcepts(props.book.knowledgeGraph?.concepts ?? props.book.concepts))
const directLenses = computed(() => normalizeLenses(props.book.knowledgeGraph?.lenses ?? props.book.linkedLenses))

const conceptRows = computed(() => {
  return directConcepts.value
    .map((concept, index) => ({
      ...concept,
      relevance: clampScore(concept.relevance ?? (concept.mentions ?? 0) * 20),
      mentions: Math.max(0, Math.round(concept.mentions ?? 0)),
      edgeStrength: concept.edgeStrength ?? edgeStrengthForIndex(index),
    }))
    .sort((a, b) => (b.mentions ?? 0) - (a.mentions ?? 0) || (b.relevance ?? 0) - (a.relevance ?? 0))
    .slice(0, 8)
})

const graphNodesAround = computed(() => {
  const graphNodes = props.book.knowledgeGraph?.nodes ?? []
  if (graphNodes.length) {
    const bookNodeId = graphNodes.find((node) => node.type === 'BOOK')?.id
    const relatedNodes = graphNodes.filter((node) => node.id !== bookNodeId && node.type !== 'SOURCE_REFERENCE')
    return relatedNodes.slice(0, 6).map((node) => {
      const relatedEdge = props.book.knowledgeGraph?.edges?.find((edge) => edge.source === node.id || edge.target === node.id)
      const matchingConcept = directConcepts.value.find((concept) => String(concept.id ?? '') === String(node.entityId) || concept.name === node.label)
      return {
        ...matchingConcept,
        id: node.entityId,
        name: node.label,
        type: node.type,
        edgeStrength: edgeStrengthForEdge(relatedEdge?.type),
        relevance: matchingConcept?.relevance ?? 0,
        mentions: matchingConcept?.mentions ?? 0,
      }
    })
  }
  return conceptRows.value.slice(0, 6)
})

const activeLenses = computed(() => {
  return directLenses.value.slice(0, 5)
})

const graphSummary = computed(() => {
  const nodes = graphNodesAround.value.map((node) => `${node.name} (${node.type ?? 'node'}, ${node.edgeStrength ?? 'weak'})`).join(', ')
  return `${props.book.title} is shown as the central node connected to ${nodes}. Edge strengths are derived from real graph relationships.`
})

function openGraphNode(node: BookConceptPreview) {
  if (node.type === 'CONCEPT') {
    emit('open-concept', node.name)
    return
  }
  emit('open-graph')
}

function normalizeConcepts(values: BookConceptPreview[] | string[] | null | undefined) {
  if (!values?.length) return []

  return values
    .map((value) => (typeof value === 'string' ? { name: value } : value))
    .filter((value) => value.name.trim().length)
}

function normalizeLenses(values: BookLensPreview[] | string[] | null | undefined) {
  if (!values?.length) return []

  return values
    .map((value) => (typeof value === 'string' ? { name: value } : value))
    .filter((value) => value.name.trim().length)
}

function edgeStrengthForIndex(index: number): KnowledgeEdgeStrength {
  if (index < 2) return 'strong'
  if (index < 5) return 'medium'
  return 'weak'
}

function edgeStrengthForEdge(type: string | null | undefined): KnowledgeEdgeStrength {
  if (!type) return 'weak'
  if (type === 'MENTIONS' || type === 'HAS_KNOWLEDGE') return 'strong'
  if (type === 'HAS_QUOTE' || type === 'HAS_ACTION' || type === 'HAS_NOTE') return 'medium'
  return 'weak'
}

function clampScore(value: number) {
  return Math.min(Math.max(Math.round(value), 0), 100)
}

function relevanceTicks(value: number) {
  return Math.max(1, Math.ceil(clampScore(value) / 20))
}
</script>

<style scoped>
.knowledge-section {
  display: grid;
  gap: var(--space-4);
}

.knowledge-section__heading {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.knowledge-section__heading h2,
.card-heading h3 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  line-height: var(--type-title-line);
}

.knowledge-section__heading h2 {
  font-size: var(--type-section-title);
}

.card-heading h3 {
  font-size: 1.12rem;
}

.knowledge-section__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(260px, 0.85fr);
  gap: var(--space-4);
  align-items: stretch;
}

.graph-card,
.concept-card,
.lenses-card {
  min-height: 430px;
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.lenses-card {
  grid-column: 1 / -1;
  min-height: 0;
}

.card-heading {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-3);
}

.graph-preview {
  position: relative;
  min-height: 250px;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at 50% 50%, rgba(31, 95, 87, 0.12), transparent 24%),
    linear-gradient(135deg, rgba(251, 247, 238, 0.88), rgba(240, 232, 218, 0.72));
  overflow: hidden;
}

.graph-node {
  position: absolute;
  min-height: 44px;
  max-width: 150px;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  box-shadow: 0 10px 26px rgba(54, 42, 24, 0.08);
  font-size: 0.78rem;
  font-weight: 900;
  line-height: 1.15;
  text-align: center;
  cursor: pointer;
  transform: translate(-50%, -50%);
  transition:
    border-color var(--motion-fast) var(--motion-ease),
    box-shadow var(--motion-fast) var(--motion-ease),
    background-color var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease);
}

.graph-node:hover {
  border-color: var(--bookos-primary);
  box-shadow: var(--shadow-card-hover);
  transform: translate(-50%, -51%);
}

.graph-node--book {
  left: 50%;
  top: 50%;
  z-index: 3;
  min-width: 170px;
  background: var(--bookos-primary);
  color: #fffdf8;
}

.graph-node--1 {
  left: 50%;
  top: 16%;
}

.graph-node--2 {
  left: 82%;
  top: 36%;
}

.graph-node--3 {
  left: 75%;
  top: 76%;
}

.graph-node--4 {
  left: 28%;
  top: 78%;
}

.graph-node--5 {
  left: 18%;
  top: 37%;
}

.graph-node--6 {
  left: 50%;
  top: 88%;
}

.graph-node--strong {
  border-color: color-mix(in srgb, var(--bookos-primary) 50%, var(--bookos-border));
}

.graph-node--medium {
  border-color: color-mix(in srgb, var(--bookos-accent) 44%, var(--bookos-border));
}

.graph-node--weak {
  border-style: dashed;
}

.graph-edge {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 34%;
  height: 2px;
  background: var(--bookos-border-strong);
  transform-origin: left center;
}

.graph-edge--strong {
  height: 3px;
  background: var(--bookos-primary);
}

.graph-edge--medium {
  background: var(--bookos-accent);
}

.graph-edge--weak {
  height: 1px;
  border-top: 1px dashed var(--bookos-text-tertiary);
  background: transparent;
}

.graph-edge--1 {
  transform: rotate(-90deg);
}

.graph-edge--2 {
  transform: rotate(-24deg);
}

.graph-edge--3 {
  transform: rotate(38deg);
}

.graph-edge--4 {
  transform: rotate(142deg);
}

.graph-edge--5 {
  transform: rotate(204deg);
}

.graph-edge--6 {
  transform: rotate(90deg);
}

.graph-legend {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.graph-legend span {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

.graph-legend__line {
  width: 28px;
  display: inline-block;
  border-top: 2px solid var(--bookos-border-strong);
}

.graph-legend__line--strong {
  border-top: 3px solid var(--bookos-primary);
}

.graph-legend__line--medium {
  border-top-color: var(--bookos-accent);
}

.graph-legend__line--weak {
  border-top-style: dashed;
}

.concept-table {
  display: grid;
  gap: var(--space-2);
}

.concept-table__head,
.concept-table__row {
  display: grid;
  grid-template-columns: minmax(120px, 1fr) minmax(120px, 0.8fr) 70px;
  gap: var(--space-3);
  align-items: center;
}

.concept-table__head {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.concept-table__row {
  min-height: 48px;
  padding: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  text-align: left;
  cursor: pointer;
  transition:
    border-color var(--motion-fast) var(--motion-ease),
    background-color var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease);
}

.concept-table__row:hover {
  border-color: var(--bookos-border-strong);
  background: var(--bookos-surface-muted);
  transform: translateY(-1px);
}

.relevance-meter {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.relevance-meter i {
  width: 10px;
  height: 18px;
  border: 1px solid var(--bookos-border-strong);
  border-radius: 999px;
  background: transparent;
}

.relevance-meter__tick--on {
  background: var(--bookos-primary) !important;
}

.relevance-meter b {
  margin-left: var(--space-1);
  color: var(--bookos-text-secondary);
  font-size: var(--type-micro);
}

.lens-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 220px), 1fr));
  gap: var(--space-3);
}

.lens-item {
  min-height: 72px;
  padding: var(--space-3);
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  text-align: left;
  cursor: pointer;
  transition:
    border-color var(--motion-fast) var(--motion-ease),
    background-color var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease);
}

.lens-item:hover {
  border-color: color-mix(in srgb, var(--bookos-primary) 30%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-primary-soft) 36%, var(--bookos-surface));
  transform: translateY(-1px);
}

.lens-item__icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
  background: var(--bookos-accent-soft);
  color: var(--bookos-accent-hover);
  font-size: 0.72rem;
  font-weight: 900;
}

.lens-item__copy {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
}

.lens-item__copy strong {
  color: var(--bookos-text-primary);
}

.lens-item__copy small {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  line-height: 1.35;
}

@media (min-width: 1680px) {
  .knowledge-section__grid {
    grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.9fr) minmax(280px, 0.82fr);
  }

  .lenses-card {
    grid-column: auto;
    min-height: 430px;
  }
}

@media (max-width: 900px) {
  .knowledge-section__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .graph-preview {
    min-height: 340px;
  }

  .graph-node--1,
  .graph-node--2,
  .graph-node--3,
  .graph-node--4,
  .graph-node--5,
  .graph-node--6 {
    left: 50%;
  }

  .graph-node--1 {
    top: 14%;
  }

  .graph-node--2 {
    top: 28%;
  }

  .graph-node--3 {
    top: 42%;
  }

  .graph-node--4 {
    top: 70%;
  }

  .graph-node--5 {
    top: 84%;
  }

  .graph-node--6 {
    top: 96%;
  }

  .graph-edge {
    display: none;
  }

  .concept-table__head {
    display: none;
  }

  .concept-table__row {
    grid-template-columns: 1fr;
  }

  .lens-item {
    grid-template-columns: 44px minmax(0, 1fr);
  }
}
</style>
