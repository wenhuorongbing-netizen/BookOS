<template>
  <nav class="project-nav" aria-label="Project workspace">
    <RouterLink
      v-for="item in items"
      :key="item.name"
      :to="{ name: item.name, params: { id: projectId } }"
      class="project-nav__link"
      :class="{ 'project-nav__link--active': route.name === item.name }"
      :aria-current="route.name === item.name ? 'page' : undefined"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>

<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

defineProps<{
  projectId: number | string
}>()

const route = useRoute()
const items = [
  { name: 'project-detail', label: 'Cockpit' },
  { name: 'project-problems', label: 'Problems' },
  { name: 'project-applications', label: 'Applications' },
  { name: 'project-decisions', label: 'Decisions' },
  { name: 'project-playtests', label: 'Playtests' },
  { name: 'project-lens-reviews', label: 'Lens Reviews' },
]
</script>

<style scoped>
.project-nav {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.project-nav__link {
  min-height: 40px;
  padding: 0 var(--space-3);
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--bookos-border);
  border-radius: 999px;
  background: var(--bookos-surface);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
  text-decoration: none;
}

.project-nav__link:hover,
.project-nav__link--active {
  border-color: var(--bookos-primary);
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary);
}
</style>
