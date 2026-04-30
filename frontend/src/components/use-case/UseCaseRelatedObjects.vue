<template>
  <div class="use-case-related">
    <section>
      <h3>Where to go next</h3>
      <div class="use-case-related__links">
        <UseCaseActionButton v-for="link in nextRoutes" :key="link.route + link.label" :to="link.route" :label="link.label" variant="secondary" />
      </div>
    </section>
    <section>
      <h3>Related features</h3>
      <div class="use-case-related__links">
        <UseCaseActionButton
          v-for="link in relatedFeatures"
          :key="link.route + link.label"
          :to="link.route"
          :label="link.label"
          variant="subtle"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { UseCaseRelatedLink } from '../../data/useCases'
import UseCaseActionButton from './UseCaseActionButton.vue'

defineProps<{
  nextRoutes: UseCaseRelatedLink[]
  relatedFeatures: UseCaseRelatedLink[]
}>()
</script>

<style scoped>
.use-case-related {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.use-case-related section {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.use-case-related h3 {
  margin: 0;
  font-size: var(--type-card-title);
}

.use-case-related__links {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

@media (max-width: 760px) {
  .use-case-related {
    grid-template-columns: 1fr;
  }
}
</style>
