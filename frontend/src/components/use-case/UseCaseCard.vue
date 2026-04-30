<template>
  <AppCard class="use-case-card" as="article">
    <div class="use-case-card__topline">
      <AppBadge variant="primary" size="sm">{{ useCase.category }}</AppBadge>
      <span>{{ useCase.timeRequired }}</span>
    </div>
    <div>
      <h2>{{ useCase.title }}</h2>
      <p>{{ useCase.summary }}</p>
    </div>
    <div class="use-case-card__meta">
      <span>{{ useCase.steps.length }} steps</span>
      <span>{{ useCase.creates.length }} outputs</span>
    </div>
    <RouterLink class="use-case-card__link" :to="`/use-cases/${useCase.slug}`" @click.stop>Open use case</RouterLink>
  </AppCard>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { UseCaseTemplate } from '../../data/useCases'
import AppBadge from '../ui/AppBadge.vue'
import AppCard from '../ui/AppCard.vue'

defineProps<{
  useCase: UseCaseTemplate
}>()
</script>

<style scoped>
.use-case-card {
  min-height: 100%;
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
  align-content: start;
}

.use-case-card__topline,
.use-case-card__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 700;
}

.use-case-card h2 {
  margin: 0 0 var(--space-2);
  color: var(--bookos-text-primary);
  font-size: 1.14rem;
  line-height: 1.25;
}

.use-case-card p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.55;
}

.use-case-card__link {
  min-height: var(--touch-target);
  justify-self: start;
  display: inline-flex;
  align-items: center;
  padding: 0 var(--space-4);
  border: 1px solid color-mix(in srgb, var(--bookos-primary) 28%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary);
  font-weight: 800;
  text-decoration: none;
}

.use-case-card__link:hover {
  border-color: var(--bookos-primary);
  background: color-mix(in srgb, var(--bookos-primary-soft) 76%, var(--bookos-surface));
}

.use-case-card__link:focus-visible {
  justify-self: start;
  outline: 2px solid var(--bookos-focus);
  outline-offset: 3px;
}
</style>
