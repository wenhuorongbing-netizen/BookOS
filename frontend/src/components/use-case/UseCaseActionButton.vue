<template>
  <RouterLink class="use-case-action-button" :class="`use-case-action-button--${variant}`" :to="to">
    {{ label }}
  </RouterLink>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

withDefaults(
  defineProps<{
    to: string
    label: string
    variant?: 'primary' | 'secondary' | 'accent' | 'ghost' | 'danger' | 'subtle' | 'text'
  }>(),
  {
    variant: 'secondary',
  },
)
</script>

<style scoped>
.use-case-action-button {
  min-height: var(--touch-target);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: 0 var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  color: var(--bookos-primary);
  background: var(--bookos-surface);
  font-weight: 800;
  line-height: 1;
  text-decoration: none;
  transition:
    background-color var(--motion-fast) var(--motion-ease),
    border-color var(--motion-fast) var(--motion-ease),
    color var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease);
}

.use-case-action-button:hover {
  border-color: var(--bookos-border-strong);
  background: var(--bookos-surface-muted);
}

.use-case-action-button:active {
  transform: translateY(1px);
}

.use-case-action-button--primary {
  border-color: var(--bookos-primary);
  background: var(--bookos-primary);
  color: #fffdf8;
}

.use-case-action-button--primary:hover {
  border-color: var(--bookos-primary-hover);
  background: var(--bookos-primary-hover);
}

.use-case-action-button--accent {
  border-color: var(--bookos-accent);
  background: var(--bookos-accent);
  color: #fffdf8;
}

.use-case-action-button--secondary {
  border-color: color-mix(in srgb, var(--bookos-primary) 28%, var(--bookos-border));
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary);
}

.use-case-action-button--ghost,
.use-case-action-button--subtle {
  background: transparent;
  color: var(--bookos-text-primary);
}

.use-case-action-button--text {
  min-height: auto;
  padding: 0;
  border-color: transparent;
  background: transparent;
  color: var(--bookos-primary);
}

.use-case-action-button--danger {
  border-color: var(--bookos-danger);
  background: var(--bookos-danger);
  color: #fffdf8;
}
</style>
