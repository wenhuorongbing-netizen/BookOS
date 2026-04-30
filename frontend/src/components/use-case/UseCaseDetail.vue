<template>
  <div class="use-case-detail">
    <AppCard class="use-case-detail__hero" as="section">
      <div class="use-case-detail__hero-copy">
        <p class="eyebrow">Hands-on use case</p>
        <h1>{{ useCase.title }}</h1>
        <p>{{ useCase.summary }}</p>
      </div>
      <UseCaseProgress :step-count="useCase.steps.length" :time-required="useCase.timeRequired" />
    </AppCard>

    <div class="use-case-detail__grid">
      <AppCard class="use-case-detail__panel" as="section">
        <h2>Who this is for</h2>
        <p>{{ useCase.audience }}</p>
      </AppCard>

      <AppCard class="use-case-detail__panel" as="section">
        <h2>Goal</h2>
        <p>{{ useCase.goal }}</p>
      </AppCard>
    </div>

    <AppCard class="use-case-detail__panel" as="section">
      <h2>Prerequisites</h2>
      <ul>
        <li v-for="item in useCase.prerequisites" :key="item">{{ item }}</li>
      </ul>
      <UseCaseActionButton :to="useCase.primaryRoute" :label="useCase.primaryLabel" variant="primary" />
    </AppCard>

    <AppCard class="use-case-detail__panel" as="section">
      <h2>Steps</h2>
      <UseCaseStepList :steps="useCase.steps" />
    </AppCard>

    <div class="use-case-detail__grid">
      <AppCard class="use-case-detail__panel" as="section">
        <h2>What you will create</h2>
        <ul>
          <li v-for="item in useCase.creates" :key="item">{{ item }}</li>
        </ul>
      </AppCard>

      <AppCard class="use-case-detail__panel" as="section">
        <h2>What source references will be preserved</h2>
        <ul>
          <li v-for="item in useCase.sourceReferences" :key="item">{{ item }}</li>
        </ul>
      </AppCard>
    </div>

    <AppCard v-if="useCase.safetyNote" class="use-case-detail__safety" as="section">
      <h2>Safety note</h2>
      <p>{{ useCase.safetyNote }}</p>
    </AppCard>

    <UseCaseRelatedObjects :next-routes="useCase.nextRoutes" :related-features="useCase.relatedFeatures" />
  </div>
</template>

<script setup lang="ts">
import type { UseCaseTemplate } from '../../data/useCases'
import AppCard from '../ui/AppCard.vue'
import UseCaseActionButton from './UseCaseActionButton.vue'
import UseCaseProgress from './UseCaseProgress.vue'
import UseCaseRelatedObjects from './UseCaseRelatedObjects.vue'
import UseCaseStepList from './UseCaseStepList.vue'

defineProps<{
  useCase: UseCaseTemplate
}>()
</script>

<style scoped>
.use-case-detail {
  display: grid;
  gap: var(--space-5);
}

.use-case-detail__hero {
  padding: var(--space-6);
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.75fr);
  gap: var(--space-6);
  align-items: center;
  background:
    radial-gradient(circle at 10% 20%, color-mix(in srgb, var(--bookos-accent-soft) 72%, transparent), transparent 34%),
    linear-gradient(135deg, var(--bookos-surface), var(--bookos-primary-soft));
}

.use-case-detail__hero h1 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3.7rem);
  line-height: 1.02;
}

.use-case-detail__hero p,
.use-case-detail__panel p,
.use-case-detail__safety p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.6;
}

.use-case-detail__hero-copy {
  display: grid;
  gap: var(--space-3);
}

.use-case-detail__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.use-case-detail__panel,
.use-case-detail__safety {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.use-case-detail__panel h2,
.use-case-detail__safety h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-section-title);
}

.use-case-detail__panel ul {
  margin: 0;
  padding-left: 1.1rem;
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  line-height: 1.55;
}

.use-case-detail__panel :deep(.app-button) {
  justify-self: start;
}

.use-case-detail__safety {
  border-color: color-mix(in srgb, var(--bookos-warning) 36%, var(--bookos-border));
  background: var(--bookos-accent-soft);
}

@media (max-width: 860px) {
  .use-case-detail__hero,
  .use-case-detail__grid {
    grid-template-columns: 1fr;
  }
}
</style>
