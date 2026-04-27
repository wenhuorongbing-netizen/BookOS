<template>
  <el-button
    v-bind="$attrs"
    class="app-button"
    :class="[
      `app-button--${variant}`,
      {
        'app-button--selected': selected,
        'app-button--checked': checked,
        'app-button--loading': loading,
      },
    ]"
    :type="elementType"
    :plain="isPlain"
    :text="variant === 'text'"
    :loading="loading"
    :disabled="disabled"
    :native-type="nativeType"
    :aria-busy="loading ? 'true' : undefined"
    :aria-pressed="selected || checked ? 'true' : undefined"
    :aria-expanded="expanded === null ? undefined : String(expanded)"
  >
    <slot />
  </el-button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  inheritAttrs: false,
})

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'accent' | 'ghost' | 'danger' | 'subtle' | 'text'
    loading?: boolean
    disabled?: boolean
    selected?: boolean
    checked?: boolean
    expanded?: boolean | null
    nativeType?: 'button' | 'submit' | 'reset'
  }>(),
  {
    variant: 'secondary',
    loading: false,
    disabled: false,
    selected: false,
    checked: false,
    expanded: null,
    nativeType: 'button',
  },
)

const elementType = computed(() => {
  if (props.variant === 'primary') return 'primary'
  if (props.variant === 'danger') return 'danger'
  if (props.variant === 'accent') return 'warning'
  return undefined
})

const isPlain = computed(
  () => props.variant === 'secondary' || props.variant === 'accent' || props.variant === 'ghost' || props.variant === 'subtle',
)
</script>

<style scoped>
.app-button {
  position: relative;
  min-height: var(--touch-target);
  font-weight: 700;
  transition:
    background-color var(--motion-fast) var(--motion-ease),
    border-color var(--motion-fast) var(--motion-ease),
    color var(--motion-fast) var(--motion-ease),
    transform var(--motion-fast) var(--motion-ease),
    box-shadow var(--motion-fast) var(--motion-ease);
}

.app-button:not(.is-disabled):not(.is-loading):active {
  transform: translateY(1px);
}

.app-button--selected,
.app-button--checked,
.app-button[aria-pressed="true"] {
  box-shadow: inset 0 0 0 2px color-mix(in srgb, var(--bookos-focus) 35%, transparent);
}

.app-button--checked::before {
  content: "On";
  margin-inline-end: var(--space-1);
  padding: 0.05rem 0.32rem;
  border: 1px solid currentColor;
  border-radius: 999px;
  font-size: 0.68rem;
  font-weight: 900;
  line-height: 1.2;
}

.app-button.is-disabled {
  opacity: 0.62;
}

.app-button--primary {
  --el-button-bg-color: var(--bookos-primary);
  --el-button-border-color: var(--bookos-primary);
  --el-button-hover-bg-color: var(--bookos-primary-hover);
  --el-button-hover-border-color: var(--bookos-primary-hover);
}

.app-button--secondary {
  --el-button-text-color: var(--bookos-primary);
  --el-button-border-color: color-mix(in srgb, var(--bookos-primary) 28%, var(--bookos-border));
  --el-button-hover-text-color: var(--bookos-primary-hover);
  --el-button-hover-bg-color: var(--bookos-primary-soft);
  --el-button-hover-border-color: var(--bookos-primary);
}

.app-button--accent {
  --el-button-text-color: var(--bookos-accent-hover);
  --el-button-border-color: color-mix(in srgb, var(--bookos-accent) 34%, var(--bookos-border));
  --el-button-hover-text-color: var(--bookos-accent-hover);
  --el-button-hover-bg-color: var(--bookos-accent-soft);
  --el-button-hover-border-color: var(--bookos-accent);
}

.app-button--ghost,
.app-button--subtle {
  --el-button-text-color: var(--bookos-text-primary);
  --el-button-border-color: var(--bookos-border);
  --el-button-hover-bg-color: var(--bookos-surface-muted);
  --el-button-hover-border-color: var(--bookos-border-strong);
}

.app-button--danger {
  --el-button-bg-color: var(--bookos-danger);
  --el-button-border-color: var(--bookos-danger);
  --el-button-hover-bg-color: #8d3626;
  --el-button-hover-border-color: #8d3626;
}

.app-button--text {
  --el-button-text-color: var(--bookos-primary);
}
</style>
