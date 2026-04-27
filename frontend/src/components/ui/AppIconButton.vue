<template>
  <AppTooltip :content="tooltip ?? label" :disabled="!showTooltip">
    <el-button
      class="app-icon-button"
      :class="[
        `app-icon-button--${variant}`,
        {
          'app-icon-button--selected': selected,
          'app-icon-button--checked': checked,
          'app-icon-button--loading': loading,
        },
      ]"
      :aria-label="label"
      :aria-pressed="selected || checked ? 'true' : undefined"
      :aria-expanded="expanded === null ? undefined : String(expanded)"
      :aria-busy="loading ? 'true' : undefined"
      :disabled="disabled"
      :loading="loading"
      native-type="button"
      circle
      @click="$emit('click', $event)"
    >
      <slot />
    </el-button>
  </AppTooltip>
</template>

<script setup lang="ts">
import AppTooltip from './AppTooltip.vue'

withDefaults(
  defineProps<{
    label: string
    tooltip?: string
    variant?: 'default' | 'primary' | 'accent' | 'danger' | 'ghost'
    selected?: boolean
    checked?: boolean
    expanded?: boolean | null
    disabled?: boolean
    loading?: boolean
    showTooltip?: boolean
  }>(),
  {
    tooltip: undefined,
    variant: 'default',
    selected: false,
    checked: false,
    expanded: null,
    disabled: false,
    loading: false,
    showTooltip: true,
  },
)

defineEmits<{
  click: [event: MouseEvent]
}>()
</script>

<style scoped>
.app-icon-button {
  position: relative;
  width: 44px;
  min-width: 44px;
  height: 44px;
  border-color: var(--bookos-border);
  color: var(--bookos-text-secondary);
  background: var(--bookos-surface);
  transition:
    background-color var(--motion-fast) var(--motion-ease),
    border-color var(--motion-fast) var(--motion-ease),
    color var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease),
    box-shadow var(--motion-fast) var(--motion-ease);
}

.app-icon-button:hover {
  border-color: var(--bookos-border-strong);
  color: var(--bookos-text-primary);
  background: var(--bookos-surface-muted);
}

.app-icon-button--primary,
.app-icon-button--selected {
  border-color: color-mix(in srgb, var(--bookos-primary) 28%, var(--bookos-border));
  color: var(--bookos-primary-hover);
  background: var(--bookos-primary-soft);
}

.app-icon-button--selected,
.app-icon-button--checked {
  box-shadow: inset 0 0 0 2px color-mix(in srgb, var(--bookos-focus) 35%, transparent);
}

.app-icon-button--checked::after {
  content: "";
  position: absolute;
  right: 7px;
  top: 7px;
  width: 8px;
  height: 8px;
  border: 2px solid var(--bookos-surface);
  border-radius: 999px;
  background: currentColor;
}

.app-icon-button--accent {
  border-color: color-mix(in srgb, var(--bookos-accent) 28%, var(--bookos-border));
  color: var(--bookos-accent-hover);
  background: var(--bookos-accent-soft);
}

.app-icon-button--danger {
  border-color: color-mix(in srgb, var(--bookos-danger) 28%, var(--bookos-border));
  color: var(--bookos-danger);
  background: var(--bookos-danger-soft);
}

.app-icon-button--ghost {
  background: transparent;
}

.app-icon-button:not(.is-disabled):not(.is-loading):active {
  transform: translateY(1px);
}

.app-icon-button.is-disabled {
  opacity: 0.62;
}
</style>
