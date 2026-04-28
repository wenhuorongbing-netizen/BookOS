<template>
  <div class="app-loading" :class="{ 'app-loading--compact': compact }" role="status" aria-live="polite" aria-busy="true" :aria-label="label">
    <div class="app-loading__status">
      <span class="app-loading__spinner" aria-hidden="true" />
      <span>{{ label }}</span>
    </div>
    <div v-if="skeleton && !compact" class="app-loading__skeleton" aria-hidden="true">
      <span />
      <span />
      <span />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string
    compact?: boolean
    skeleton?: boolean
  }>(),
  {
    label: 'Loading',
    compact: false,
    skeleton: true,
  },
)
</script>

<style scoped>
.app-loading {
  width: 100%;
  display: grid;
  justify-content: center;
  gap: var(--space-2);
  min-height: 96px;
  color: var(--bookos-text-secondary);
  font-weight: 700;
}

.app-loading--compact {
  min-height: 44px;
}

.app-loading__status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.app-loading__spinner {
  width: 18px;
  height: 18px;
  border: 2px solid color-mix(in srgb, var(--bookos-primary) 18%, transparent);
  border-top-color: var(--bookos-primary);
  border-radius: 999px;
  animation: app-loading-spin 760ms linear infinite;
}

.app-loading__skeleton {
  width: min(420px, 100%);
  display: grid;
  gap: var(--space-2);
}

.app-loading__skeleton span {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    color-mix(in srgb, var(--bookos-surface-muted) 86%, transparent),
    color-mix(in srgb, var(--bookos-primary-soft) 46%, transparent),
    color-mix(in srgb, var(--bookos-surface-muted) 86%, transparent)
  );
  background-size: 220% 100%;
  animation: app-loading-skeleton 1.2s var(--motion-ease) infinite;
}

.app-loading__skeleton span:nth-child(2) {
  width: 78%;
}

.app-loading__skeleton span:nth-child(3) {
  width: 56%;
}

@keyframes app-loading-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes app-loading-skeleton {
  to {
    background-position: -220% 0;
  }
}
</style>
