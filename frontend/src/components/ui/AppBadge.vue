<template>
  <span
    class="app-badge"
    :class="[
      `app-badge--${variant}`,
      `app-badge--${size}`,
      { 'app-badge--selected': selected, 'app-badge--checked': checked },
    ]"
    :aria-label="label || undefined"
  >
    <span v-if="checked" class="app-badge__check" aria-hidden="true">On</span>
    <slot />
  </span>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'neutral' | 'primary' | 'accent' | 'success' | 'warning' | 'danger' | 'info'
    size?: 'sm' | 'md'
    selected?: boolean
    checked?: boolean
    label?: string
  }>(),
  {
    variant: 'neutral',
    size: 'md',
    selected: false,
    checked: false,
    label: '',
  },
)
</script>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  border: 1px solid transparent;
  border-radius: 999px;
  font-weight: 800;
  line-height: 1.2;
  white-space: nowrap;
  gap: var(--space-1);
}

.app-badge--sm {
  padding: 0.22rem 0.55rem;
  font-size: 0.72rem;
}

.app-badge--md {
  padding: 0.32rem 0.74rem;
  font-size: 0.78rem;
}

.app-badge--neutral {
  background: color-mix(in srgb, var(--bookos-text-secondary) 10%, transparent);
  color: var(--bookos-text-secondary);
  border-color: color-mix(in srgb, var(--bookos-text-secondary) 18%, transparent);
}

.app-badge--primary {
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
  border-color: color-mix(in srgb, var(--bookos-primary) 20%, transparent);
}

.app-badge--accent {
  background: var(--bookos-accent-soft);
  color: var(--bookos-accent-hover);
  border-color: color-mix(in srgb, var(--bookos-accent) 20%, transparent);
}

.app-badge--success {
  background: var(--bookos-success-soft);
  color: #315f3b;
  border-color: color-mix(in srgb, var(--bookos-success) 20%, transparent);
}

.app-badge--warning {
  background: var(--bookos-warning-soft);
  color: #7f520f;
  border-color: color-mix(in srgb, var(--bookos-warning) 24%, transparent);
}

.app-badge--danger {
  background: var(--bookos-danger-soft);
  color: #843223;
  border-color: color-mix(in srgb, var(--bookos-danger) 22%, transparent);
}

.app-badge--info {
  background: var(--bookos-info-soft);
  color: #285f70;
  border-color: color-mix(in srgb, var(--bookos-info) 22%, transparent);
}

.app-badge--selected {
  box-shadow: inset 0 0 0 1px currentColor;
}

.app-badge--checked {
  border-style: double;
}

.app-badge__check {
  padding: 0.02rem 0.28rem;
  border: 1px solid currentColor;
  border-radius: 999px;
  font-size: 0.78em;
  font-weight: 900;
  line-height: 1;
}
</style>
