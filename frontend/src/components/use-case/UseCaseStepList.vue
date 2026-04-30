<template>
  <ol class="use-case-steps">
    <li v-for="(step, index) in steps" :key="step.title" class="use-case-steps__item">
      <div class="use-case-steps__number" aria-hidden="true">{{ index + 1 }}</div>
      <div class="use-case-steps__content">
        <h3>{{ step.title }}</h3>
        <p>{{ step.description }}</p>
        <UseCaseActionButton :to="step.route" :label="step.actionLabel" variant="ghost" />
      </div>
    </li>
  </ol>
</template>

<script setup lang="ts">
import type { UseCaseStep } from '../../data/useCases'
import UseCaseActionButton from './UseCaseActionButton.vue'

defineProps<{
  steps: UseCaseStep[]
}>()
</script>

<style scoped>
.use-case-steps {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: var(--space-3);
}

.use-case-steps__item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
}

.use-case-steps__number {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary);
  font-weight: 900;
}

.use-case-steps__content {
  display: grid;
  gap: var(--space-2);
}

.use-case-steps h3 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
}

.use-case-steps p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.55;
}

.use-case-steps :deep(.app-button) {
  justify-self: start;
}

@media (max-width: 640px) {
  .use-case-steps__item {
    grid-template-columns: 1fr;
  }
}
</style>
