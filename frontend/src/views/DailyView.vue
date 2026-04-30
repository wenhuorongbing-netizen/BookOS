<template>
  <div class="page-shell daily-page">
    <AppSectionHeader
      eyebrow="Daily Resurfacing"
      title="Today in BookOS"
      description="Review source-backed quotes and design prompts selected by the daily API. Template prompts are labeled explicitly."
    >
      <template #actions>
        <HelpTooltip topic="daily-prompt" placement="left" />
      </template>
    </AppSectionHeader>

    <AppLoadingState v-if="loading" label="Loading daily cockpit" />

    <AppErrorState
      v-else-if="errorMessage"
      title="Daily cockpit could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadDailyPage"
    />

    <template v-else-if="daily">
      <DetailNextStepCard
        title="Use one daily item today"
        description="Start by writing a short reflection. Apply to a project only when the prompt changes a real design decision."
        primary-label="Write Reflection"
        secondary-label="Apply to Project"
        :loop="dailyWorkflowLoop"
        @primary="focusReflection('PROMPT')"
        @secondary="applyProjectOpen = true"
      />

      <section class="daily-grid" aria-label="Today's daily cards">
        <AppCard class="daily-card daily-card--quote" as="article">
          <div class="daily-card__heading">
            <div>
              <div class="eyebrow">Today's Resurfaced Quote</div>
              <h2>Remember</h2>
            </div>
            <AppBadge v-if="daily.sentence?.sourceBacked" variant="success" size="sm">Source-backed</AppBadge>
          </div>

          <template v-if="daily.sentence">
            <blockquote>
              <p>{{ daily.sentence.text }}</p>
            </blockquote>
            <p class="daily-card__meta">
              {{ daily.sentence.bookTitle ?? daily.sentence.attribution ?? 'Source-backed sentence' }}
              <AppBadge v-if="daily.sentence.pageStart" variant="accent" size="sm">p.{{ daily.sentence.pageStart }}</AppBadge>
            </p>
            <div class="daily-card__actions">
              <AppButton variant="secondary" :disabled="!daily.sentence.sourceReference" @click="openDailySource('SENTENCE')">Open source</AppButton>
            </div>
            <details class="daily-more-actions">
              <summary>More quote actions</summary>
              <div class="daily-card__actions">
                <AppButton variant="ghost" :loading="dailyBusy" @click="updateDaily('SENTENCE', 'regenerate')">Regenerate</AppButton>
                <AppButton variant="text" :loading="dailyBusy" @click="updateDaily('SENTENCE', 'skip')">Skip</AppButton>
              </div>
            </details>
          </template>

          <AppEmptyState
            v-else
            title="No daily quote yet"
            description="Create or convert a source-backed quote before BookOS can resurface one here."
            compact
          />
        </AppCard>

        <AppCard class="daily-card daily-card--prompt" as="article">
          <div class="daily-card__heading">
            <div>
              <div class="eyebrow">Daily Design Prompt</div>
              <h2>Think</h2>
            </div>
            <AppBadge :variant="daily.prompt.templatePrompt ? 'warning' : 'success'" size="sm">
              {{ daily.prompt.templatePrompt ? 'Template Prompt' : 'Source-backed' }}
            </AppBadge>
          </div>

          <p class="daily-card__prompt">{{ daily.prompt.question }}</p>
          <p class="daily-card__meta">{{ daily.prompt.sourceTitle ?? daily.prompt.bookTitle ?? 'Daily prompt' }}</p>
          <div class="daily-card__actions">
            <AppButton variant="secondary" :disabled="!daily.prompt.sourceReference" @click="openDailySource('PROMPT')">Open source</AppButton>
            <AppButton variant="primary" @click="focusReflection('PROMPT')">Write reflection</AppButton>
            <AppButton variant="secondary" @click="applyProjectOpen = true">Apply to Project</AppButton>
          </div>
          <details class="daily-more-actions">
            <summary>More prompt actions</summary>
            <div class="daily-card__actions">
              <AppButton variant="accent" :loading="dailyBusy" @click="createPrototypeTask">Create prototype task</AppButton>
              <AppButton variant="secondary" @click="openProjectAction('PROBLEM')">Create project problem</AppButton>
              <AppButton variant="secondary" @click="openProjectAction('LENS_REVIEW')">Create lens review</AppButton>
              <AppButton variant="ghost" :loading="dailyBusy" @click="updateDaily('PROMPT', 'regenerate')">Regenerate</AppButton>
              <AppButton variant="text" :loading="dailyBusy" @click="updateDaily('PROMPT', 'skip')">Skip</AppButton>
            </div>
          </details>
        </AppCard>
      </section>

      <AppCard id="daily-reflection" class="reflection-card" as="section">
        <div class="reflection-card__heading">
          <div>
            <div class="eyebrow">Daily Reflection</div>
            <h2>Save a source-aware thought</h2>
          </div>
          <el-segmented v-model="reflectionTarget" :options="reflectionOptions" aria-label="Reflection target" />
        </div>
        <label class="reflection-card__field">
          <span>Reflection</span>
          <el-input
            v-model="reflectionText"
            type="textarea"
            :rows="5"
            maxlength="10000"
            show-word-limit
            placeholder="What does this daily item change about your current design thinking?"
          />
        </label>
        <div class="daily-card__actions">
          <AppButton variant="primary" :loading="dailyBusy" :disabled="!reflectionText.trim()" @click="saveReflection">Save reflection</AppButton>
          <AppButton variant="ghost" :disabled="!reflectionText.trim()" @click="reflectionText = ''">Clear</AppButton>
        </div>
      </AppCard>

      <AppCard class="history-card" as="section">
        <AppSectionHeader eyebrow="History" title="Recent daily activity" compact />
        <AppErrorState v-if="historyError" title="History unavailable" :description="historyError" retry-label="Retry history" @retry="loadHistory" />
        <div v-else-if="history.length" class="history-list">
          <article v-for="item in history" :key="item.id" class="history-item">
            <AppBadge variant="neutral" size="sm">{{ item.target }}</AppBadge>
            <span>{{ item.action }}</span>
            <time :datetime="item.createdAt">{{ formatDate(item.createdAt) }}</time>
          </article>
        </div>
        <AppEmptyState
          v-else
          title="No daily history yet"
          description="Regenerate, skip, reflect, or create a prototype task to build daily history."
          compact
        />
      </AppCard>

      <ApplyToProjectDialog
        v-if="daily.prompt"
        v-model="applyProjectOpen"
        source-type="DAILY_DESIGN_PROMPT"
        :source-id="daily.prompt.id"
        :source-reference="daily.prompt.sourceReference"
        :source-label="daily.prompt.sourceTitle ?? daily.prompt.bookTitle ?? 'Daily design prompt'"
        :default-title="daily.prompt.question"
        :default-description="daily.prompt.templatePrompt ? 'Template prompt application. No source page is implied.' : daily.prompt.question"
      />

      <DailyProjectActionDialog
        v-if="daily.prompt"
        v-model="projectActionOpen"
        :mode="projectActionMode"
        :prompt="daily.prompt"
        @created="loadHistory"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createPrototypeTaskFromDaily, getDailyHistory, getDailyToday, regenerateDaily, saveDailyReflection, skipDaily } from '../api/daily'
import ApplyToProjectDialog from '../components/project/ApplyToProjectDialog.vue'
import DailyProjectActionDialog from '../components/project/DailyProjectActionDialog.vue'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { DailyHistoryRecord, DailyTarget, DailyTodayRecord } from '../types'

const router = useRouter()
const { openSource } = useOpenSource()
const daily = ref<DailyTodayRecord | null>(null)
const history = ref<DailyHistoryRecord[]>([])
const loading = ref(true)
const dailyBusy = ref(false)
const errorMessage = ref('')
const historyError = ref('')
const reflectionTarget = ref<DailyTarget>('PROMPT')
const reflectionText = ref('')
const applyProjectOpen = ref(false)
const projectActionOpen = ref(false)
const projectActionMode = ref<'PROBLEM' | 'LENS_REVIEW'>('PROBLEM')
const dailyWorkflowLoop = ['Read prompt', 'Reflect', 'Apply only if useful']
const reflectionOptions = computed(() => [
  { label: 'Prompt', value: 'PROMPT' },
  { label: 'Sentence', value: 'SENTENCE', disabled: !daily.value?.sentence },
])

onMounted(loadDailyPage)

async function loadDailyPage() {
  loading.value = true
  errorMessage.value = ''
  try {
    daily.value = await getDailyToday()
    await loadHistory()
  } catch {
    daily.value = null
    errorMessage.value = 'Check backend availability and permissions, then try loading daily resurfacing again.'
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  historyError.value = ''
  try {
    history.value = await getDailyHistory()
  } catch {
    history.value = []
    historyError.value = 'Daily history could not be loaded.'
  }
}

function focusReflection(target: DailyTarget) {
  reflectionTarget.value = target
  requestAnimationFrame(() => {
    document.getElementById('daily-reflection')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
}

async function updateDaily(target: DailyTarget, action: 'regenerate' | 'skip') {
  dailyBusy.value = true
  try {
    daily.value = action === 'regenerate' ? await regenerateDaily(target) : await skipDaily(target)
    await loadHistory()
    ElMessage.success(action === 'regenerate' ? 'Daily item regenerated.' : 'Daily item skipped.')
  } finally {
    dailyBusy.value = false
  }
}

async function saveReflection() {
  if (!daily.value || !reflectionText.value.trim()) return
  if (reflectionTarget.value === 'SENTENCE' && !daily.value.sentence) {
    ElMessage.warning('There is no daily sentence to reflect on yet.')
    return
  }
  dailyBusy.value = true
  try {
    await saveDailyReflection({
      target: reflectionTarget.value,
      dailySentenceId: reflectionTarget.value === 'SENTENCE' ? daily.value.sentence?.id ?? null : null,
      dailyDesignPromptId: reflectionTarget.value === 'PROMPT' ? daily.value.prompt.id : null,
      reflectionText: reflectionText.value.trim(),
    })
    reflectionText.value = ''
    daily.value = await getDailyToday()
    await loadHistory()
    ElMessage.success('Reflection saved.')
  } finally {
    dailyBusy.value = false
  }
}

async function createPrototypeTask() {
  if (!daily.value) return
  dailyBusy.value = true
  try {
    const task = await createPrototypeTaskFromDaily({ dailyDesignPromptId: daily.value.prompt.id })
    await loadHistory()
    ElMessage.success('Prototype task created.')
    router.push({ name: 'knowledge-detail', params: { id: task.id } })
  } finally {
    dailyBusy.value = false
  }
}

function openProjectAction(mode: 'PROBLEM' | 'LENS_REVIEW') {
  projectActionMode.value = mode
  projectActionOpen.value = true
}

function openDailySource(target: DailyTarget) {
  if (!daily.value) return
  const item = target === 'SENTENCE' ? daily.value.sentence : daily.value.prompt
  const source = item?.sourceReference
  if (!source) {
    ElMessage.info('This daily item does not have a source link.')
    return
  }

  void openSource({
    sourceType: target === 'SENTENCE' ? 'DAILY_SENTENCE' : 'DAILY_PROMPT',
    sourceId: item.id,
    bookId: source.bookId,
    bookTitle: item.bookTitle ?? undefined,
    pageStart: source.pageStart,
    noteId: source.noteId ?? undefined,
    rawCaptureId: source.rawCaptureId ?? undefined,
    noteBlockId: source.noteBlockId ?? undefined,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}

function formatDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}
</script>

<style scoped>
.daily-page {
  min-width: 0;
}

.daily-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.daily-card,
.reflection-card,
.history-card {
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.daily-card__heading,
.reflection-card__heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.daily-card h2,
.reflection-card h2,
.daily-card p,
.daily-card blockquote {
  margin: 0;
}

.daily-card blockquote {
  padding-left: var(--space-4);
  border-left: 4px solid var(--bookos-accent);
}

.daily-card blockquote p,
.daily-card__prompt {
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.12rem, 2vw, 1.36rem);
  line-height: 1.38;
}

.daily-card__meta {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.daily-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.daily-more-actions {
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.daily-more-actions summary {
  min-height: 42px;
  padding: 0 var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--bookos-text-secondary);
  cursor: pointer;
  font-size: var(--type-metadata);
  font-weight: 900;
}

.daily-more-actions summary::-webkit-details-marker {
  display: none;
}

.daily-more-actions summary::after {
  content: "+";
  color: var(--bookos-accent);
}

.daily-more-actions[open] summary::after {
  content: "-";
}

.daily-more-actions .daily-card__actions {
  padding: 0 var(--space-3) var(--space-3);
}

.reflection-card__field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.history-list {
  display: grid;
  gap: var(--space-2);
}

.history-item {
  min-height: 48px;
  padding: var(--space-3);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  color: var(--bookos-text-secondary);
}

.history-item span {
  color: var(--bookos-text-primary);
  font-weight: 800;
}

.history-item time {
  margin-left: auto;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
}

@media (max-width: 900px) {
  .daily-grid {
    grid-template-columns: 1fr;
  }

  .history-item time {
    margin-left: 0;
  }
}
</style>
