<template>
  <div class="page-shell learning-page">
    <AppSectionHeader
      eyebrow="Reading Analytics"
      title="Knowledge Activity"
      description="Computed from real BookOS records: books, notes, captures, quotes, actions, projects, reviews, and mastery."
      :level="1"
    />

    <AppLoadingState v-if="loading" label="Loading analytics" />
    <AppErrorState v-else-if="errorMessage" title="Analytics could not load" :description="errorMessage" retry-label="Retry" @retry="loadAnalytics" />

    <template v-else-if="reading && knowledge">
      <AppEmptyState
        v-if="!hasAnyAnalytics"
        title="No learning activity yet"
        description="Analytics will appear after you add books, reading sessions, notes, captures, quotes, concepts, projects, or review items."
      />

      <section v-else class="stat-grid" aria-label="Reading analytics summary">
        <AppStat label="Library books" :value="reading.libraryBooks" tone="primary" />
        <AppStat label="Currently reading" :value="reading.currentlyReadingBooks" tone="accent" />
        <AppStat label="Notes" :value="reading.notesCount" tone="info" />
        <AppStat label="Captures" :value="reading.capturesCount" tone="neutral" />
        <AppStat label="Quotes" :value="reading.quotesCount" tone="success" />
        <AppStat label="Open actions" :value="reading.openActionItems" tone="warning" />
        <AppStat label="Concepts" :value="knowledge.conceptsCount" tone="primary" />
        <AppStat label="Review completion" :value="`${knowledge.completedReviewItems}/${knowledge.reviewItems}`" tone="success" />
      </section>

      <section v-if="hasAnyAnalytics" class="analytics-grid">
        <AppCard class="analytics-card" as="section">
          <AppSectionHeader title="Reading Progress" eyebrow="Real activity" compact />
          <dl class="metric-list">
            <div><dt>Completed books</dt><dd>{{ reading.completedBooks }}</dd></div>
            <div><dt>Total minutes read</dt><dd>{{ reading.totalMinutesRead }}</dd></div>
            <div><dt>Daily reflections</dt><dd>{{ reading.dailyReflectionsCount }}</dd></div>
            <div><dt>Project applications</dt><dd>{{ reading.projectApplicationsCount }}</dd></div>
          </dl>
        </AppCard>

        <AppCard class="analytics-card" as="section">
          <AppSectionHeader title="Knowledge Mastery" eyebrow="Review signal" compact />
          <dl class="metric-list">
            <div><dt>Knowledge objects</dt><dd>{{ knowledge.knowledgeObjectsCount }}</dd></div>
            <div><dt>Mastery targets</dt><dd>{{ knowledge.masteryTargets }}</dd></div>
            <div><dt>Due for review</dt><dd>{{ knowledge.dueForReview }}</dd></div>
            <div><dt>Review sessions</dt><dd>{{ reading.completedReviewSessions }}/{{ reading.reviewSessionsCount }}</dd></div>
          </dl>
        </AppCard>

        <AppCard class="analytics-card" as="section">
          <AppSectionHeader title="Most Active Books" eyebrow="Source activity" compact />
          <AppEmptyState v-if="!reading.mostActiveBooks.length" title="No activity yet" description="Create notes, captures, quotes, or reading sessions to populate this list." compact />
          <ol v-else class="rank-list">
            <li v-for="item in reading.mostActiveBooks" :key="item.label">
              <span>{{ item.label }}</span>
              <AppBadge variant="primary" size="sm">{{ item.count }}</AppBadge>
            </li>
          </ol>
        </AppCard>

        <AppCard class="analytics-card" as="section">
          <AppSectionHeader title="Most Linked Concepts" eyebrow="Knowledge graph input" compact />
          <AppEmptyState v-if="!knowledge.mostLinkedConcepts.length" title="No concept activity yet" description="Review parsed concepts from captures to grow this list." compact />
          <ol v-else class="rank-list">
            <li v-for="item in knowledge.mostLinkedConcepts" :key="item.label">
              <span>{{ item.label }}</span>
              <AppBadge variant="success" size="sm">{{ item.count }}</AppBadge>
            </li>
          </ol>
        </AppCard>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getKnowledgeAnalytics, getReadingAnalytics } from '../api/learning'
import AppBadge from '../components/ui/AppBadge.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import AppStat from '../components/ui/AppStat.vue'
import type { KnowledgeAnalyticsRecord, ReadingAnalyticsRecord } from '../types'

const reading = ref<ReadingAnalyticsRecord | null>(null)
const knowledge = ref<KnowledgeAnalyticsRecord | null>(null)
const loading = ref(false)
const errorMessage = ref('')

const hasAnyAnalytics = computed(() => {
  if (!reading.value || !knowledge.value) return false
  return [
    reading.value.libraryBooks,
    reading.value.notesCount,
    reading.value.capturesCount,
    reading.value.quotesCount,
    reading.value.openActionItems,
    reading.value.completedActionItems,
    reading.value.conceptsCount,
    reading.value.dailyReflectionsCount,
    reading.value.projectApplicationsCount,
    reading.value.reviewSessionsCount,
    reading.value.totalMinutesRead,
    knowledge.value.knowledgeObjectsCount,
    knowledge.value.masteryTargets,
    knowledge.value.reviewItems,
  ].some((value) => value > 0)
})

onMounted(loadAnalytics)

async function loadAnalytics() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [readingResult, knowledgeResult] = await Promise.all([getReadingAnalytics(), getKnowledgeAnalytics()])
    reading.value = readingResult
    knowledge.value = knowledgeResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.learning-page,
.analytics-grid,
.analytics-card {
  display: grid;
  gap: var(--space-5);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.analytics-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.analytics-card {
  padding: var(--space-5);
}

.metric-list {
  margin: 0;
  display: grid;
  gap: var(--space-3);
}

.metric-list div,
.rank-list li {
  min-height: 44px;
  padding: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.metric-list dt {
  color: var(--bookos-text-secondary);
  font-weight: 800;
}

.metric-list dd {
  margin: 0;
  color: var(--bookos-text-primary);
  font-weight: 900;
}

.rank-list {
  margin: 0;
  padding-left: var(--space-5);
  display: grid;
  gap: var(--space-2);
}

@media (max-width: 1040px) {
  .stat-grid,
  .analytics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .stat-grid,
  .analytics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
