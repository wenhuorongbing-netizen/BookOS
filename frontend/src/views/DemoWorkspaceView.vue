<template>
  <div class="demo-page">
    <section class="demo-hero" aria-labelledby="demo-title">
      <div>
        <p class="eyebrow">Demo Workspace</p>
        <h1 id="demo-title">Practice BookOS without polluting your real knowledge base</h1>
        <p>
          Demo mode creates clearly labeled, original sample records you can open, convert, search, graph, reset, or delete.
          Unknown pages stay unknown and demo analytics are excluded by default.
        </p>
      </div>
      <AppBadge variant="warning" size="md">Demo data is labeled</AppBadge>
    </section>

    <AppErrorState
      v-if="error"
      title="Demo workspace could not be loaded"
      :description="error"
      retry-label="Retry"
      @retry="loadStatus"
    />
    <AppLoadingState v-else-if="loading" label="Loading demo workspace" />

    <template v-else>
      <AppCard class="demo-status" as="section">
        <div>
          <p class="eyebrow">{{ status?.active ? 'Active demo' : 'Not started' }}</p>
          <h2>{{ status?.active ? 'Demo workspace is ready' : 'Start a safe practice workspace' }}</h2>
          <p>{{ status?.safetyNote ?? safetyFallback }}</p>
        </div>
        <div class="demo-status__summary" role="region" aria-label="Demo workspace status">
          <span>
            <strong>Status</strong>
            {{ status?.active ? 'Active' : 'Inactive' }}
          </span>
          <span>
            <strong>Scoped records</strong>
            {{ recordCount }}
          </span>
          <span>
            <strong>Last reset</strong>
            {{ lastResetLabel }}
          </span>
          <span>
            <strong>Analytics</strong>
            {{ status?.excludedFromAnalyticsByDefault ? 'Excluded by default' : 'Not confirmed' }}
          </span>
          <span class="demo-status__wide">
            <strong>Included record types</strong>
            {{ includedRecordTypesLabel }}
          </span>
        </div>
        <div class="demo-status__actions">
          <AppButton v-if="!status?.active" variant="primary" :loading="working" @click="startDemo">Start Demo Workspace</AppButton>
          <AppButton v-else variant="primary" :loading="working" @click="resetDemo">Reset Demo</AppButton>
          <AppButton v-if="status?.active" variant="secondary" :loading="working" @click="deleteDemo">Delete Demo Data</AppButton>
          <RouterLink to="/books/new" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Graduate to Real Book</AppButton>
          </RouterLink>
          <AppButton v-if="status?.active" variant="accent" :loading="working" @click="deleteAndStartRealWorkflow">
            Delete Demo and Start Real Workflow
          </AppButton>
          <RouterLink to="/guided/first-loop" custom v-slot="{ navigate }">
            <AppButton variant="accent" @click="navigate">Start real first loop</AppButton>
          </RouterLink>
          <RouterLink to="/dashboard" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Exit Demo</AppButton>
          </RouterLink>
        </div>
      </AppCard>

      <section class="demo-section" aria-label="Demo tutorials">
        <AppSectionHeader
          eyebrow="Training ground"
          title="Pick a tutorial and practice with safe demo records"
          description="Each tutorial opens an executable use-case checklist. Starting the tutorial never creates fake completion; it only guides you to real demo routes."
          compact
        />
        <div class="demo-tutorial-grid">
          <RouterLink
            v-for="tutorial in tutorialCards"
            :key="tutorial.title"
            class="demo-tutorial"
            :to="tutorial.route"
          >
            <span class="demo-tutorial__kicker">{{ tutorial.kicker }}</span>
            <h3>{{ tutorial.title }}</h3>
            <p>{{ tutorial.description }}</p>
            <span class="demo-tutorial__meta">{{ tutorial.time }} - Use case checklist</span>
          </RouterLink>
        </div>
      </section>

      <section class="demo-section" aria-label="Safe original sample records">
        <AppSectionHeader
          eyebrow="What gets created"
          title="Safe original sample records"
          description="The backend stores a demo scope record for each created object so the workspace can be reset or deleted without touching real work."
          compact
        />
        <div class="demo-grid">
          <AppCard v-for="item in demoItems" :key="item.title" class="demo-item" as="article">
            <div class="demo-item__icon" aria-hidden="true">{{ item.short }}</div>
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
              <AppBadge variant="neutral" size="sm">{{ item.countLabel }}</AppBadge>
            </div>
          </AppCard>
        </div>
      </section>

      <section class="demo-section" aria-label="Guided practice links">
        <AppSectionHeader
          eyebrow="Guided practice"
          title="Open the demo records"
          description="Use these links to practice the real BookOS routes. Every sample is labeled as demo and can be removed from this page."
          compact
        />
        <AppEmptyState
          v-if="!status?.active"
          title="Start demo mode first"
          description="No sample records are created until you explicitly start the Demo Workspace."
          compact
        >
          <template #actions>
            <AppButton variant="primary" :loading="working" @click="startDemo">Start Demo Workspace</AppButton>
          </template>
        </AppEmptyState>
        <div v-else class="demo-actions">
          <RouterLink v-if="status.bookId" :to="{ name: 'book-detail', params: { id: status.bookId } }" custom v-slot="{ navigate }">
            <AppButton variant="primary" @click="navigate">Open demo book</AppButton>
          </RouterLink>
          <RouterLink v-if="status.quoteId" :to="{ name: 'quote-detail', params: { id: status.quoteId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open demo quote</AppButton>
          </RouterLink>
          <RouterLink v-if="status.actionItemId" :to="{ name: 'action-item-detail', params: { id: status.actionItemId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open demo action</AppButton>
          </RouterLink>
          <RouterLink v-if="firstConceptId" :to="{ name: 'concept-detail', params: { id: firstConceptId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open demo concept</AppButton>
          </RouterLink>
          <RouterLink v-if="status.projectId" :to="{ name: 'project-detail', params: { id: status.projectId } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open demo project</AppButton>
          </RouterLink>
          <RouterLink v-if="status.projectId" :to="{ name: 'graph-project', params: { projectId: status.projectId } }" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Open project graph</AppButton>
          </RouterLink>
          <RouterLink v-if="status.forumThreadId" :to="{ name: 'forum-thread', params: { id: status.forumThreadId } }" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Open demo discussion</AppButton>
          </RouterLink>
        </div>
      </section>

      <AppCard class="demo-safety" as="section">
        <p class="eyebrow">Safety rules</p>
        <ul>
          <li>Demo records are not your real reading.</li>
          <li>Demo pages are unknown unless explicitly labeled as fictional demo.</li>
          <li>Demo content is original BookOS training material.</li>
          <li>No copyrighted passages are included.</li>
          <li>No page numbers are invented; demo source links use page unknown.</li>
          <li>Demo records are user-owned and scoped in `demo_records` for reset/delete.</li>
          <li>Normal analytics excludes demo records unless the backend request explicitly asks to include them.</li>
        </ul>
      </AppCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deleteDemoWorkspace, getDemoStatus, resetDemoWorkspace, startDemoWorkspace } from '../api/demo'
import type { DemoWorkspaceStatus } from '../types'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'

const safetyFallback = 'Demo mode uses original sample content and keeps unknown pages null.'
const router = useRouter()
const status = ref<DemoWorkspaceStatus | null>(null)
const loading = ref(true)
const working = ref(false)
const error = ref('')
const tutorialCards = [
  {
    kicker: 'Demo',
    title: 'Capture and Convert',
    description: 'Practice turning demo captures into notes, quotes, and actions without touching real reading records.',
    route: '/use-cases/note-taker-capture-convert',
    time: '8-12 min',
  },
  {
    kicker: 'Demo',
    title: 'Open Source',
    description: 'Open a demo quote or action and verify its source link, confidence, and unknown-page behavior.',
    route: '/use-cases/open-source-from-quote-or-action',
    time: '2-3 min',
  },
  {
    kicker: 'Demo',
    title: 'Apply Quote to Project',
    description: 'Use the demo quote as source-backed project evidence inside the demo game project workflow.',
    route: '/use-cases/apply-quote-to-game-project',
    time: '5-8 min',
  },
  {
    kicker: 'Demo',
    title: 'Review Concept',
    description: 'Practice concept review from parsed [[Concept]] markers before creating real vocabulary.',
    route: '/use-cases/review-concept-marker',
    time: '4-7 min',
  },
  {
    kicker: 'Demo',
    title: 'Explore Graph',
    description: 'Inspect only real demo relationships between the demo book, concepts, project, and discussion.',
    route: '/use-cases/inspect-knowledge-graph',
    time: '3-6 min',
  },
  {
    kicker: 'Demo',
    title: 'Forum Discussion',
    description: 'Open a source-linked demo discussion and see how context follows the thread.',
    route: '/use-cases/source-linked-forum-discussion',
    time: '4-7 min',
  },
  {
    kicker: 'Demo',
    title: 'Export Demo Data',
    description: 'Review export behavior using demo records while keeping private real data separate.',
    route: '/use-cases/export-reading-knowledge',
    time: '3-5 min',
  },
]

const firstConceptId = computed(() => status.value?.conceptIds[0] ?? null)
const recordCount = computed(() => {
  if (!status.value?.recordCounts) return 0
  return Object.values(status.value.recordCounts).reduce((sum, count) => sum + count, 0)
})
const includedRecordTypes = computed(() => {
  if (status.value?.includedRecordTypes?.length) return status.value.includedRecordTypes
  if (status.value?.recordCounts) return Object.keys(status.value.recordCounts).sort()
  return []
})
const includedRecordTypesLabel = computed(() => {
  if (!includedRecordTypes.value.length) return 'None yet'
  return includedRecordTypes.value.map(formatRecordType).join(', ')
})
const lastResetLabel = computed(() => {
  if (!status.value?.lastResetAt) return 'Never'
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(status.value.lastResetAt))
})
const demoItems = computed(() => [
  {
    short: 'BK',
    title: 'Demo Game Design Notebook',
    description: 'A private demo book by BookOS Demo with original learning notes and no real passages.',
    countLabel: countLabel('BOOK', 'book'),
  },
  {
    short: 'CA',
    title: 'Captures and parser examples',
    description: 'Idea, quote-like, action, and concept captures with tags and [[Concept]] markers.',
    countLabel: countLabel('RAW_CAPTURE', 'capture'),
  },
  {
    short: 'KO',
    title: 'Concepts and design knowledge',
    description: 'Core Loop, Feedback Loop, Game Feel, and Meaningful Choice with source confidence LOW.',
    countLabel: `${count('CONCEPT')} concepts`,
  },
  {
    short: 'PR',
    title: 'Demo Puzzle Adventure',
    description: 'A demo project with a problem, application, decision, and playtest finding.',
    countLabel: countLabel('GAME_PROJECT', 'project'),
  },
  {
    short: 'FO',
    title: 'Source-linked discussion',
    description: 'A private demo forum thread attached to the project and source link.',
    countLabel: countLabel('FORUM_THREAD', 'thread'),
  },
  {
    short: 'Σ',
    title: 'Scoped reset and delete',
    description: 'Demo records can be reset or deleted without touching real user-created records.',
    countLabel: `${recordCount.value} scoped records`,
  },
])

onMounted(loadStatus)

async function loadStatus() {
  loading.value = true
  error.value = ''
  try {
    status.value = await getDemoStatus()
  } catch {
    error.value = 'Check the backend connection and try again.'
  } finally {
    loading.value = false
  }
}

async function startDemo() {
  working.value = true
  try {
    status.value = await startDemoWorkspace()
    ElMessage.success('Demo workspace started.')
  } catch {
    ElMessage.error('Demo workspace could not be started.')
  } finally {
    working.value = false
  }
}

async function resetDemo() {
  working.value = true
  try {
    status.value = await resetDemoWorkspace()
    ElMessage.success('Demo workspace reset.')
  } catch {
    ElMessage.error('Demo workspace could not be reset.')
  } finally {
    working.value = false
  }
}

async function deleteDemo() {
  working.value = true
  try {
    await deleteDemoWorkspace()
    status.value = await getDemoStatus()
    ElMessage.success('Demo data deleted.')
  } catch {
    ElMessage.error('Demo data could not be deleted.')
  } finally {
    working.value = false
  }
}

async function deleteAndStartRealWorkflow() {
  working.value = true
  try {
    await deleteDemoWorkspace()
    status.value = await getDemoStatus()
    ElMessage.success('Demo data deleted. Start with a real source next.')
    await router.push({ name: 'guided-first-loop' })
  } catch {
    ElMessage.error('Demo data could not be deleted.')
  } finally {
    working.value = false
  }
}

function count(type: string) {
  return status.value?.recordCounts?.[type] ?? 0
}

function countLabel(type: string, noun: string) {
  const value = count(type)
  return `${value} ${noun}${value === 1 ? '' : 's'}`
}

function formatRecordType(type: string) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}
</script>

<style scoped>
.demo-page {
  display: grid;
  gap: var(--space-5);
}

.demo-hero,
.demo-status,
.demo-safety {
  padding: var(--space-6);
  display: flex;
  justify-content: space-between;
  gap: var(--space-5);
  align-items: flex-start;
}

.demo-hero {
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at 20% 0%, color-mix(in srgb, var(--bookos-accent-soft) 72%, transparent), transparent 34%),
    linear-gradient(135deg, var(--bookos-surface), color-mix(in srgb, var(--bookos-primary-soft) 40%, var(--bookos-surface)));
  box-shadow: var(--shadow-card);
}

.demo-hero h1,
.demo-hero p,
.demo-status h2,
.demo-status p,
.demo-item h3,
.demo-item p,
.demo-safety p {
  margin: 0;
}

.demo-hero h1 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(2rem, 4vw, 3.6rem);
  letter-spacing: -0.04em;
  max-width: 14ch;
}

.demo-hero p:not(.eyebrow),
.demo-status p,
.demo-item p {
  margin-top: var(--space-2);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.demo-status__actions,
.demo-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: flex-end;
}

.demo-status__summary {
  min-width: min(320px, 100%);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-2);
}

.demo-status__summary span {
  padding: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-surface-muted) 68%, transparent);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.demo-status__summary strong {
  display: block;
  margin-bottom: 2px;
  color: var(--bookos-text-primary);
  font-size: var(--type-micro);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.demo-status__wide {
  grid-column: 1 / -1;
}

.demo-section {
  display: grid;
  gap: var(--space-3);
}

.demo-tutorial-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(220px, 1fr));
  gap: var(--space-3);
  overflow-x: auto;
  padding-bottom: var(--space-1);
}

.demo-tutorial {
  min-height: 190px;
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--bookos-accent-soft) 20%, transparent), transparent 48%),
    var(--bookos-surface);
  box-shadow: var(--shadow-card);
  color: inherit;
  text-decoration: none;
  display: grid;
  align-content: start;
  gap: var(--space-2);
  transition:
    transform 140ms ease,
    box-shadow 140ms ease,
    border-color 140ms ease;
}

.demo-tutorial:hover,
.demo-tutorial:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--bookos-primary) 36%, var(--bookos-border));
  box-shadow: var(--shadow-card);
  outline: none;
}

.demo-tutorial h3,
.demo-tutorial p {
  margin: 0;
}

.demo-tutorial p {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.demo-tutorial__kicker,
.demo-tutorial__meta {
  color: var(--bookos-primary);
  font-size: var(--type-micro);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.demo-tutorial__meta {
  align-self: end;
  color: var(--bookos-text-tertiary);
}

.demo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.demo-item {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--space-3);
}

.demo-item__icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: grid;
  place-items: center;
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary);
  font-weight: 900;
}

.demo-safety {
  display: grid;
}

.demo-safety ul {
  margin: var(--space-2) 0 0;
  padding-left: var(--space-5);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

@media (max-width: 980px) {
  .demo-tutorial-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    overflow-x: visible;
  }

  .demo-grid {
    grid-template-columns: 1fr 1fr;
  }

  .demo-hero,
  .demo-status {
    flex-direction: column;
  }

  .demo-status__actions,
  .demo-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 720px) {
  .demo-grid {
    grid-template-columns: 1fr;
  }

  .demo-tutorial-grid,
  .demo-status__summary {
    grid-template-columns: 1fr;
  }

  .demo-status__wide {
    grid-column: auto;
  }

  .demo-hero,
  .demo-status,
  .demo-safety {
    padding: var(--space-4);
  }
}
</style>
