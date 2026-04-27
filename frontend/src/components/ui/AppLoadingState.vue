<template>
  <div class="app-loading" :class="{ 'app-loading--compact': compact }" role="status" aria-live="polite" aria-busy="true" :aria-label="label">
    <span class="app-loading__spinner" aria-hidden="true" />
    <span>{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string
    compact?: boolean
  }>(),
  {
    label: 'Loading',
    compact: false,
  },
)
</script>

<style scoped>
.app-loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: 96px;
  color: var(--bookos-text-secondary);
  font-weight: 700;
}

.app-loading--compact {
  min-height: 44px;
}

.app-loading__spinner {
  width: 18px;
  height: 18px;
  border: 2px solid color-mix(in srgb, var(--bookos-primary) 18%, transparent);
  border-top-color: var(--bookos-primary);
  border-radius: 999px;
  animation: app-loading-spin 760ms linear infinite;
}

@keyframes app-loading-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
