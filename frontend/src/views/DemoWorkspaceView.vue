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
        <div class="demo-status__actions">
          <AppButton v-if="!status?.active" variant="primary" :loading="working" @click="startDemo">Start Demo Workspace</AppButton>
          <AppButton v-else variant="primary" :loading="working" @click="resetDemo">Reset Demo</AppButton>
          <AppButton v-if="status?.active" variant="secondary" :loading="working" @click="deleteDemo">Delete Demo Data</AppButton>
          <RouterLink to="/dashboard" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Exit Demo</AppButton>
          </RouterLink>
        </div>
      </AppCard>

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
          <li>No copyrighted passages are included.</li>
          <li>No page numbers are invented; demo source references use page unknown.</li>
          <li>Demo records are user-owned and scoped in `demo_records` for reset/delete.</li>
          <li>Normal analytics excludes demo records unless the backend request explicitly asks to include them.</li>
        </ul>
      </AppCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
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
const status = ref<DemoWorkspaceStatus | null>(null)
const loading = ref(true)
const working = ref(false)
const error = ref('')

const firstConceptId = computed(() => status.value?.conceptIds[0] ?? null)
const recordCount = computed(() => {
  if (!status.value?.recordCounts) return 0
  return Object.values(status.value.recordCounts).reduce((sum, count) => sum + count, 0)
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
    title: 'Concepts and knowledge objects',
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
    description: 'A private demo forum thread attached to the project and source reference.',
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

function count(type: string) {
  return status.value?.recordCounts?.[type] ?? 0
}

function countLabel(type: string, noun: string) {
  const value = count(type)
  return `${value} ${noun}${value === 1 ? '' : 's'}`
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

.demo-section {
  display: grid;
  gap: var(--space-3);
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

  .demo-hero,
  .demo-status,
  .demo-safety {
    padding: var(--space-4);
  }
}
</style>
