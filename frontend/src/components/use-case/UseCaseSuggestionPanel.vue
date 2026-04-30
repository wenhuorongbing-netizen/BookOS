<template>
  <AppCard class="use-case-suggestion" as="section">
    <div class="use-case-suggestion__header">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <UseCaseActionButton to="/use-cases" label="Browse all use cases" variant="text" />
    </div>

    <div class="use-case-suggestion__grid">
      <article v-for="useCase in visibleUseCases" :key="useCase.slug" class="use-case-suggestion__item">
        <div class="use-case-suggestion__meta">
          <AppBadge variant="primary" size="sm">{{ useCase.category }}</AppBadge>
          <span>{{ useCase.timeRequired }}</span>
        </div>
        <h3>{{ useCase.title }}</h3>
        <p>{{ useCase.summary }}</p>
        <UseCaseActionButton :to="`/use-cases/${useCase.slug}`" label="Start this use case" variant="secondary" />
      </article>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useCases } from '../../data/useCases'
import AppBadge from '../ui/AppBadge.vue'
import AppCard from '../ui/AppCard.vue'
import UseCaseActionButton from './UseCaseActionButton.vue'

const props = withDefaults(
  defineProps<{
    slugs: string[]
    eyebrow?: string
    title?: string
    description?: string
  }>(),
  {
    eyebrow: 'Hands-on guide',
    title: 'Not sure what to do next?',
    description: 'Start with a concrete BookOS workflow. These guides link to real routes and do not claim work is complete until you do it.',
  },
)

const visibleUseCases = computed(() => {
  const requested = new Set(props.slugs)
  return useCases.filter((useCase) => requested.has(useCase.slug))
})
</script>

<style scoped>
.use-case-suggestion {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--bookos-primary-soft) 72%, transparent), transparent 34%),
    var(--bookos-surface);
}

.use-case-suggestion__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
}

.use-case-suggestion h2,
.use-case-suggestion h3,
.use-case-suggestion p {
  margin: 0;
}

.use-case-suggestion h2 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(1.35rem, 2.8vw, 2rem);
}

.use-case-suggestion__header p:not(.eyebrow),
.use-case-suggestion__item p {
  margin-top: var(--space-2);
  color: var(--bookos-text-secondary);
  line-height: 1.55;
}

.use-case-suggestion__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.use-case-suggestion__item {
  display: grid;
  gap: var(--space-3);
  align-content: start;
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-surface) 88%, transparent);
}

.use-case-suggestion__item h3 {
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
  line-height: 1.25;
}

.use-case-suggestion__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.use-case-suggestion__item :deep(.use-case-action-button) {
  justify-self: start;
}

@media (max-width: 1100px) {
  .use-case-suggestion__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .use-case-suggestion__header {
    flex-direction: column;
  }

  .use-case-suggestion__grid {
    grid-template-columns: 1fr;
  }
}
</style>
