<template>
  <section class="book-insights" aria-label="Daily reading insights">
    <AppCard as="article" class="insight-card insight-card--quote" variant="default">
      <div class="insight-card__heading">
        <div>
          <div class="eyebrow">Today's Resurfaced Quote</div>
          <h2>Remember</h2>
        </div>
        <span class="insight-card__icon" aria-hidden="true">QT</span>
      </div>

      <template v-if="quote">
        <blockquote class="insight-card__quote">
          <p>{{ quote.text }}</p>
        </blockquote>
        <div class="insight-card__source">
          <span>{{ quote.author || quote.sourceLabel || authorLine }}</span>
          <AppBadge v-if="quote.page" variant="accent" size="sm">p.{{ quote.page }}</AppBadge>
          <AppBadge v-if="quote.sourceBacked" variant="success" size="sm">Source-backed</AppBadge>
        </div>
        <div class="insight-card__actions">
          <AppButton variant="secondary" :disabled="!quote.hasSource" @click="$emit('open-source', 'SENTENCE')">Open source</AppButton>
          <AppButton variant="ghost" :loading="dailyActionLoading" @click="$emit('regenerate-daily', 'SENTENCE')">Regenerate</AppButton>
          <AppButton variant="text" :loading="dailyActionLoading" @click="$emit('skip-daily', 'SENTENCE')">Skip</AppButton>
        </div>
      </template>

      <AppEmptyState
        v-else
        title="No quote captured yet"
        description="Capture a quote from this book to resurface it in your daily reading workflow."
        compact
      >
        <template #actions>
          <AppButton variant="secondary" @click="$emit('open-source')">Open book context</AppButton>
        </template>
      </AppEmptyState>
    </AppCard>

    <AppCard as="article" class="insight-card insight-card--prompt" variant="default">
      <div class="insight-card__heading">
        <div>
          <div class="eyebrow">Daily Design Prompt</div>
          <h2>
            Think
            <AppBadge v-if="prompt.templatePrompt" variant="warning" size="sm">Template Prompt</AppBadge>
            <AppBadge v-else variant="success" size="sm">Source-backed</AppBadge>
          </h2>
        </div>
        <span class="insight-card__icon" aria-hidden="true">DP</span>
      </div>

      <p class="insight-card__prompt">{{ prompt.question }}</p>
      <div class="insight-card__source">
        <AppBadge variant="primary" size="sm">{{ prompt.sourceTitle ?? prompt.linkedConcept ?? prompt.linkedLens ?? 'Design prompt' }}</AppBadge>
      </div>
      <div class="insight-card__actions">
        <AppButton variant="primary" @click="promptOpen = true">Save Reflection</AppButton>
        <AppButton variant="accent" :loading="dailyActionLoading" @click="$emit('create-prototype-task', prompt.id ?? null)">
          Create Prototype Task
        </AppButton>
        <AppButton variant="ghost" :loading="dailyActionLoading" @click="$emit('regenerate-daily', 'PROMPT')">Regenerate</AppButton>
        <AppButton variant="text" :loading="dailyActionLoading" @click="$emit('skip-daily', 'PROMPT')">Skip</AppButton>
      </div>
    </AppCard>

    <AppCard as="article" class="insight-card insight-card--ontology" variant="default">
      <div class="insight-card__heading">
        <div>
          <div class="eyebrow">Ontology Preview</div>
          <h2>Connect</h2>
        </div>
        <span class="insight-card__icon" aria-hidden="true">OG</span>
      </div>

      <p class="insight-card__prompt">
        This book connects to {{ conceptCount }} {{ conceptCount === 1 ? 'concept' : 'concepts' }} captured from your notes and source references.
      </p>
      <div class="mini-graph" role="img" :aria-label="graphDescription">
        <span class="mini-graph__node mini-graph__node--book">{{ book.title }}</span>
        <span
          v-for="(concept, index) in visibleConcepts"
          :key="concept.name"
          class="mini-graph__node"
          :class="`mini-graph__node--${index + 1}`"
        >
          {{ concept.name }}
        </span>
      </div>
      <AppButton variant="secondary" @click="$emit('open-graph')">Open Graph</AppButton>
    </AppCard>

    <div
      v-if="promptOpen"
      class="prompt-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="prompt-modal-title"
      @keydown.escape="promptOpen = false"
    >
      <div ref="promptPanel" class="prompt-modal__panel" tabindex="-1">
        <div class="prompt-modal__header">
          <div>
            <div class="eyebrow">Prompt Reflection</div>
            <h2 id="prompt-modal-title">Start Prompt</h2>
          </div>
          <AppIconButton label="Close prompt reflection" variant="ghost" @click="promptOpen = false">X</AppIconButton>
        </div>
        <p>{{ prompt.question }}</p>
        <label class="prompt-modal__field">
          <span>Your reflection draft</span>
          <el-input
            v-model="reflectionDraft"
            type="textarea"
            :rows="5"
            placeholder="Write a quick design reflection. It will be saved to your daily reflection history."
          />
        </label>
        <div class="prompt-modal__actions">
          <AppButton variant="primary" :loading="dailyActionLoading" @click="saveDraft">Save reflection</AppButton>
          <AppButton variant="ghost" @click="promptOpen = false">Close</AppButton>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { BookConceptPreview, BookDesignPromptPreview, BookQuotePreview, BookRecord, DailyTarget, DailyTodayRecord } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'
import AppIconButton from '../ui/AppIconButton.vue'

const props = defineProps<{
  book: BookRecord
  daily?: DailyTodayRecord | null
  dailyActionLoading?: boolean
}>()

const emit = defineEmits<{
  'open-source': [target?: DailyTarget]
  'open-graph': []
  'regenerate-daily': [target: DailyTarget]
  'skip-daily': [target: DailyTarget]
  'save-reflection': [payload: { target: DailyTarget; text: string }]
  'create-prototype-task': [promptId: number | string | null]
}>()

const promptOpen = ref(false)
const promptPanel = ref<HTMLElement | null>(null)
const reflectionDraft = ref('')

const authorLine = computed(() => (props.book.authors.length ? props.book.authors.join(', ') : 'Unknown author'))
const quote = computed<(BookQuotePreview & { sourceBacked?: boolean; hasSource?: boolean }) | null>(() => {
  if (props.daily?.sentence) {
    return {
      id: props.daily.sentence.id,
      text: props.daily.sentence.text,
      author: props.daily.sentence.attribution,
      page: props.daily.sentence.pageStart,
      sourceLabel: props.daily.sentence.bookTitle,
      sourceBacked: props.daily.sentence.sourceBacked,
      hasSource: Boolean(props.daily.sentence.sourceReference || props.daily.sentence.bookId),
    }
  }

  const fallback = props.book.dailyQuote ?? props.book.latestQuote ?? null
  return fallback ? { ...fallback, sourceBacked: Boolean(fallback.sourceLabel), hasSource: Boolean(fallback.id) } : null
})

const directConcepts = computed<BookConceptPreview[]>(() => {
  const fromConcepts = props.book.concepts
    ?.map((concept) => (typeof concept === 'string' ? { name: concept, type: 'Concept' } : concept))
    .filter((concept) => concept.name.trim().length)

  return fromConcepts ?? []
})

const visibleConcepts = computed(() => {
  const values = directConcepts.value.slice(0, 3)
  if (values.length) return values
  return [{ name: 'No concepts yet', type: 'Empty state' }]
})

const conceptCount = computed(() => props.book.ontologyConceptCount ?? directConcepts.value.length)
const prompt = computed<BookDesignPromptPreview>(() => {
  if (props.daily?.prompt) {
    return {
      id: props.daily.prompt.id,
      question: props.daily.prompt.question,
      linkedConcept: props.daily.prompt.templatePrompt ? null : props.daily.prompt.sourceTitle,
      sourceTitle: props.daily.prompt.sourceTitle,
      templatePrompt: props.daily.prompt.templatePrompt,
    }
  }

  if (props.book.dailyDesignPrompt) return props.book.dailyDesignPrompt

  const anchor = directConcepts.value[0]?.name ?? props.book.category ?? 'player experience'
  return {
    question: `What one design decision in ${props.book.title} could you test with a smaller prototype today?`,
    linkedConcept: anchor,
    sourceTitle: 'Template Prompt',
    templatePrompt: true,
  }
})

const graphDescription = computed(() => {
  const names = visibleConcepts.value.map((concept) => concept.name).join(', ')
  return `Mini knowledge graph preview connecting ${props.book.title} to ${names}.`
})

watch(promptOpen, async (open) => {
  if (!open) return
  await nextTick()
  promptPanel.value?.focus()
})

function saveDraft() {
  if (!reflectionDraft.value.trim()) {
    ElMessage.warning('Write a short reflection before saving.')
    return
  }

  emit('save-reflection', { target: 'PROMPT', text: reflectionDraft.value.trim() })
  reflectionDraft.value = ''
  promptOpen.value = false
}
</script>

<style scoped>
.book-insights {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
  min-width: 0;
}

.insight-card {
  min-height: 320px;
  padding: var(--space-5);
  display: grid;
  grid-template-rows: auto 1fr auto auto;
  gap: var(--space-4);
  position: relative;
  overflow: hidden;
}

.insight-card::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.72;
}

.insight-card--quote::before {
  background: radial-gradient(circle at 100% 0%, rgba(197, 107, 44, 0.14), transparent 34%);
}

.insight-card--prompt::before {
  background: radial-gradient(circle at 100% 0%, rgba(31, 95, 87, 0.12), transparent 34%);
}

.insight-card--ontology::before {
  background: radial-gradient(circle at 100% 0%, rgba(47, 111, 132, 0.12), transparent 34%);
}

.insight-card > * {
  position: relative;
}

.insight-card__heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.insight-card h2 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: 1.2rem;
  line-height: var(--type-title-line);
}

.insight-card__icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-primary-hover);
  font-size: 0.72rem;
  font-weight: 900;
}

.insight-card__quote {
  margin: 0;
  padding-left: var(--space-4);
  border-left: 4px solid var(--bookos-accent);
}

.insight-card__quote p,
.insight-card__prompt {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: clamp(1.08rem, 2vw, 1.32rem);
  line-height: 1.35;
}

.insight-card__source {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.insight-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.mini-graph {
  min-height: 118px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  grid-template-rows: repeat(2, minmax(44px, auto));
  gap: var(--space-3);
  align-items: center;
  position: relative;
}

.mini-graph::before {
  content: "";
  position: absolute;
  inset: 24px 14%;
  border-top: 1px solid color-mix(in srgb, var(--bookos-primary) 28%, transparent);
  border-bottom: 1px solid color-mix(in srgb, var(--bookos-accent) 22%, transparent);
  transform: skewY(-8deg);
}

.mini-graph__node {
  position: relative;
  min-height: 44px;
  padding: var(--space-2);
  display: grid;
  place-items: center;
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  font-size: 0.76rem;
  font-weight: 800;
  text-align: center;
  line-height: 1.15;
}

.mini-graph__node--book {
  grid-column: 1 / 3;
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
}

.mini-graph__node--1 {
  grid-column: 3;
}

.mini-graph__node--2 {
  grid-column: 1;
}

.mini-graph__node--3 {
  grid-column: 2 / 4;
}

.prompt-modal {
  position: fixed;
  z-index: 100;
  inset: 0;
  padding: var(--space-4);
  display: grid;
  place-items: center;
  background: rgba(27, 33, 30, 0.38);
}

.prompt-modal__panel {
  width: min(640px, 100%);
  max-height: calc(100vh - var(--space-8));
  overflow: auto;
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-xl);
  background: var(--bookos-surface);
  box-shadow: var(--shadow-card-hover);
}

.prompt-modal__header,
.prompt-modal__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.prompt-modal h2,
.prompt-modal p {
  margin: 0;
}

.prompt-modal__field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

@media (max-width: 1180px) {
  .book-insights {
    grid-template-columns: 1fr;
  }
}
</style>
