<template>
  <AppCard class="app-error" variant="muted" role="alert" aria-live="assertive">
    <div>
      <div class="eyebrow">Error</div>
      <h2>{{ title }}</h2>
      <p v-if="description">{{ description }}</p>
      <pre v-if="details" class="app-error__details">{{ details }}</pre>
    </div>
    <div v-if="$slots.actions || retryLabel" class="app-error__actions">
      <slot name="actions">
        <AppButton v-if="retryLabel" variant="primary" @click="$emit('retry')">{{ retryLabel }}</AppButton>
      </slot>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import AppButton from './AppButton.vue'
import AppCard from './AppCard.vue'

withDefaults(
  defineProps<{
    title: string
    description?: string
    retryLabel?: string
    details?: string
  }>(),
  {
    description: '',
    retryLabel: '',
    details: '',
  },
)

defineEmits<{
  retry: []
}>()
</script>

<style scoped>
.app-error {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
  border-color: color-mix(in srgb, var(--bookos-danger) 30%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-danger-soft) 36%, var(--bookos-surface));
}

.app-error h2 {
  margin: var(--space-1) 0 0;
  font-size: 1.1rem;
}

.app-error p {
  margin: var(--space-2) 0 0;
  color: var(--bookos-text-secondary);
}

.app-error__details {
  overflow: auto;
  margin: var(--space-3) 0 0;
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--bookos-surface) 80%, var(--bookos-danger-soft));
  color: var(--bookos-danger);
  font-size: 0.82rem;
}

.app-error__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}
</style>
