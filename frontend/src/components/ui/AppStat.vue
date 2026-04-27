<template>
  <AppCard as="article" class="app-stat" :class="`app-stat--${tone}`" variant="default">
    <div class="app-stat__header">
      <div>
        <div v-if="eyebrow" class="eyebrow">{{ eyebrow }}</div>
        <h3>{{ label }}</h3>
      </div>
      <div v-if="$slots.icon" class="app-stat__icon" aria-hidden="true">
        <slot name="icon" />
      </div>
    </div>
    <strong class="app-stat__value">{{ value }}</strong>
    <p v-if="description">{{ description }}</p>
    <div v-if="$slots.meta" class="app-stat__meta">
      <slot name="meta" />
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import AppCard from './AppCard.vue'

withDefaults(
  defineProps<{
    label: string
    value: string | number
    description?: string
    eyebrow?: string
    tone?: 'neutral' | 'primary' | 'accent' | 'success' | 'warning' | 'danger' | 'info'
  }>(),
  {
    description: '',
    eyebrow: '',
    tone: 'neutral',
  },
)
</script>

<style scoped>
.app-stat {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-3);
}

.app-stat__header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
}

.app-stat h3 {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-secondary);
  font-size: 0.86rem;
  font-weight: 800;
}

.app-stat__value {
  color: var(--bookos-text-primary);
  font-size: clamp(1.75rem, 4vw, 2.35rem);
  line-height: 1;
  font-family: var(--font-book-title);
}

.app-stat p {
  margin: 0;
  color: var(--bookos-text-secondary);
  font-size: 0.88rem;
}

.app-stat__icon {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-primary);
}

.app-stat__meta {
  color: var(--bookos-text-tertiary);
  font-size: 0.78rem;
}

.app-stat--primary {
  border-color: color-mix(in srgb, var(--bookos-primary) 22%, var(--bookos-border));
}

.app-stat--accent {
  border-color: color-mix(in srgb, var(--bookos-accent) 22%, var(--bookos-border));
}

.app-stat--success {
  border-color: color-mix(in srgb, var(--bookos-success) 22%, var(--bookos-border));
}

.app-stat--warning {
  border-color: color-mix(in srgb, var(--bookos-warning) 22%, var(--bookos-border));
}

.app-stat--danger {
  border-color: color-mix(in srgb, var(--bookos-danger) 22%, var(--bookos-border));
}

.app-stat--info {
  border-color: color-mix(in srgb, var(--bookos-info) 22%, var(--bookos-border));
}
</style>
