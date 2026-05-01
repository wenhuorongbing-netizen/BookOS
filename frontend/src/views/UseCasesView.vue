<template>
  <div class="page-shell">
    <AppCard class="use-cases-hero" as="section">
      <div>
        <p class="eyebrow">Hands-on scenarios</p>
        <h1>Use BookOS by workflow, not by module</h1>
        <p>
          These templates show practical paths from reading to notes, source-backed knowledge, project action,
          discussion, search, Knowledge Graph exploration, export, and safe draft assistance.
        </p>
      </div>
      <div class="use-cases-hero__note">
        <strong>No fake completion state</strong>
        <span>Use cases link to real BookOS routes. They do not claim a workflow is complete until you do the work in the app.</span>
        <RouterLink to="/guided/first-loop" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Start First Valuable Loop</AppButton>
        </RouterLink>
      </div>
    </AppCard>

    <AppSectionHeader
      eyebrow="Scenario Library"
      title="Choose the job you are trying to do"
      description="Start with a concrete outcome. Each guide explains prerequisites, steps, outputs, source-reference behavior, and what to do next."
    />

    <div class="use-cases-toolbar" role="search">
      <label for="use-case-filter">Search use cases</label>
      <input
        id="use-case-filter"
        v-model="query"
        type="search"
        placeholder="Search by workflow, route, or goal"
      />
    </div>

    <section class="use-cases-section" aria-label="Use case templates">
      <div v-for="group in groupedUseCases" :key="group.category" class="use-cases-group">
        <h2>{{ group.category }}</h2>
        <div class="use-cases-grid">
          <UseCaseCard
            v-for="useCase in group.items"
            :key="useCase.slug"
            :use-case="useCase"
            :progress="progressBySlug.get(useCase.slug) ?? null"
          />
        </div>
      </div>
    </section>

    <AppEmptyState
      v-if="!groupedUseCases.length"
      title="No use cases match that search"
      description="Try a broader term such as capture, project, source, Knowledge Graph, export, or Draft Assistant."
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getUseCaseProgress } from '../api/useCaseProgress'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import UseCaseCard from '../components/use-case/UseCaseCard.vue'
import { useCases } from '../data/useCases'
import type { UserUseCaseProgressRecord } from '../types'

const query = ref('')
const progressRecords = ref<UserUseCaseProgressRecord[]>([])

onMounted(loadProgress)

const progressBySlug = computed(() => new Map(progressRecords.value.map((record) => [record.useCaseSlug, record])))

const filteredUseCases = computed(() => {
  const normalizedQuery = query.value.trim().toLowerCase()
  if (!normalizedQuery) return useCases

  return useCases.filter((useCase) => {
    const searchable = [
      useCase.title,
      useCase.summary,
      useCase.audience,
      useCase.goal,
      useCase.category,
      ...useCase.prerequisites,
      ...useCase.creates,
      ...useCase.sourceReferences,
      ...useCase.relatedFeatures.map((item) => item.label),
    ]
      .join(' ')
      .toLowerCase()

    return searchable.includes(normalizedQuery)
  })
})

const groupedUseCases = computed(() => {
  const groups = new Map<string, typeof useCases>()

  for (const useCase of filteredUseCases.value) {
    const items = groups.get(useCase.category) ?? []
    items.push(useCase)
    groups.set(useCase.category, items)
  }

  return Array.from(groups.entries()).map(([category, items]) => ({ category, items }))
})

async function loadProgress() {
  try {
    progressRecords.value = await getUseCaseProgress()
  } catch {
    progressRecords.value = []
  }
}
</script>

<style scoped>
.use-cases-hero {
  padding: var(--space-6);
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(260px, 0.7fr);
  gap: var(--space-6);
  align-items: end;
  background:
    radial-gradient(circle at 12% 18%, color-mix(in srgb, var(--bookos-accent-soft) 78%, transparent), transparent 32%),
    linear-gradient(135deg, var(--bookos-surface), var(--bookos-primary-soft));
}

.use-cases-hero h1 {
  margin: 0 0 var(--space-3);
  color: var(--bookos-text-primary);
  font-family: var(--font-display);
  font-size: clamp(2.1rem, 4vw, 4rem);
  line-height: 1;
}

.use-cases-hero p {
  max-width: 76ch;
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.65;
}

.use-cases-hero__note {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4);
  border: 1px solid color-mix(in srgb, var(--bookos-primary) 24%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-surface) 86%, transparent);
}

.use-cases-hero__note strong {
  color: var(--bookos-primary);
}

.use-cases-hero__note span {
  color: var(--bookos-text-secondary);
  line-height: 1.5;
}

.use-cases-toolbar {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
}

.use-cases-toolbar label {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.use-cases-toolbar input {
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-primary);
  font: inherit;
}

.use-cases-section,
.use-cases-group {
  display: grid;
  gap: var(--space-5);
}

.use-cases-group h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-section-title);
}

.use-cases-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

@media (max-width: 1120px) {
  .use-cases-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .use-cases-hero,
  .use-cases-grid {
    grid-template-columns: 1fr;
  }
}
</style>
