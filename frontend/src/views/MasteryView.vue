<template>
  <div class="page-shell mastery-page">
    <AppSectionHeader
      eyebrow="Knowledge Mastery"
      title="What Needs Review"
      description="Mastery scores are user-owned and updated from review sessions or source-backed daily reflections."
      :level="1"
    >
      <template #actions>
        <HelpTooltip topic="mastery" placement="left" />
      </template>
    </AppSectionHeader>

    <AppLoadingState v-if="loading" label="Loading mastery targets" />
    <AppErrorState v-else-if="errorMessage" title="Mastery could not load" :description="errorMessage" retry-label="Retry" @retry="loadMastery" />
    <AppEmptyState v-else-if="!items.length" title="No mastery targets yet" description="Complete review items or save source-backed daily reflections to create mastery records. Mastery is not invented from empty activity." >
      <template #actions>
        <RouterLink to="/review" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Start Review</AppButton>
        </RouterLink>
        <RouterLink to="/help/mastery" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Learn mastery</AppButton>
        </RouterLink>
      </template>
    </AppEmptyState>

    <section v-else class="mastery-list" aria-label="Mastery target list">
      <AppCard v-for="item in items" :key="item.id" class="mastery-card" as="article">
        <div class="mastery-card__topline">
          <AppBadge variant="primary">{{ item.targetType }}</AppBadge>
          <AppBadge v-if="item.sourceReference" variant="info">Source-backed</AppBadge>
        </div>
        <h2>{{ item.targetType }} #{{ item.targetId }}</h2>
        <dl class="mastery-score">
          <div><dt>Familiarity</dt><dd>{{ item.familiarityScore }}/5</dd></div>
          <div><dt>Usefulness</dt><dd>{{ item.usefulnessScore }}/5</dd></div>
          <div><dt>Last reviewed</dt><dd>{{ formatDate(item.lastReviewedAt) }}</dd></div>
          <div><dt>Next review</dt><dd>{{ formatDate(item.nextReviewAt) }}</dd></div>
        </dl>
        <AppButton v-if="item.sourceReference" variant="secondary" @click="openMasterySource(item)">Open Source</AppButton>
      </AppCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getMastery } from '../api/learning'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { KnowledgeMasteryRecord } from '../types'

const { openSource } = useOpenSource()
const items = ref<KnowledgeMasteryRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')

onMounted(loadMastery)

async function loadMastery() {
  loading.value = true
  errorMessage.value = ''
  try {
    items.value = await getMastery()
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

function openMasterySource(item: KnowledgeMasteryRecord) {
  if (!item.sourceReference) return
  void openSource({
    sourceType: item.targetType,
    sourceId: item.targetId,
    bookId: item.sourceReference.bookId,
    pageStart: item.sourceReference.pageStart,
    sourceReference: item.sourceReference,
    sourceReferenceId: item.sourceReference.id,
  })
}

function formatDate(value: string | null) {
  if (!value) return 'Not scheduled'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(date)
}
</script>

<style scoped>
.mastery-page,
.mastery-list,
.mastery-card {
  display: grid;
  gap: var(--space-5);
}

.mastery-list {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.mastery-card {
  padding: var(--space-5);
}

.mastery-card__topline {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.mastery-card h2 {
  margin: 0;
  color: var(--bookos-text-primary);
}

.mastery-score {
  margin: 0;
  display: grid;
  gap: var(--space-2);
}

.mastery-score div {
  padding: var(--space-3);
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.mastery-score dt {
  color: var(--bookos-text-secondary);
  font-weight: 800;
}

.mastery-score dd {
  margin: 0;
  color: var(--bookos-text-primary);
  font-weight: 900;
}

@media (max-width: 860px) {
  .mastery-list {
    grid-template-columns: 1fr;
  }
}
</style>
