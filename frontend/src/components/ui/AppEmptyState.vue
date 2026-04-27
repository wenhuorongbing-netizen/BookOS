<template>
  <AppCard class="app-empty" :class="{ 'app-empty--compact': compact }" variant="muted">
    <div class="app-empty__content">
      <div v-if="eyebrow" class="eyebrow">{{ eyebrow }}</div>
      <h2>{{ title }}</h2>
      <p v-if="description">{{ description }}</p>
      <slot />
    </div>
    <div v-if="$slots.actions" class="app-empty__actions">
      <slot name="actions" />
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import AppCard from './AppCard.vue'

withDefaults(
  defineProps<{
    title: string
    description?: string
    eyebrow?: string
    compact?: boolean
  }>(),
  {
    description: '',
    eyebrow: '',
    compact: false,
  },
)
</script>

<style scoped>
.app-empty {
  padding: var(--space-6);
  display: grid;
  gap: var(--space-4);
  text-align: center;
}

.app-empty--compact {
  padding: var(--space-4);
}

.app-empty__content {
  display: grid;
  justify-items: center;
  gap: var(--space-2);
}

.app-empty h2 {
  margin: 0;
  font-size: 1.1rem;
}

.app-empty p {
  margin: 0;
  color: var(--bookos-text-secondary);
  max-width: 36rem;
}

.app-empty__actions {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}
</style>
