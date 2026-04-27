<template>
  <component
    :is="as"
    class="app-card"
    :class="classes"
    :tabindex="tabIndex"
    :role="computedRole"
    :aria-selected="computedRole && selected ? 'true' : undefined"
    :aria-disabled="disabled ? 'true' : undefined"
    @click="handleClick"
    @keydown.enter="handleKeyboardActivate"
    @keydown.space.prevent="handleKeyboardActivate"
  >
    <slot />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    as?: 'section' | 'article' | 'aside' | 'div'
    variant?: 'default' | 'muted' | 'highlight' | 'interactive' | 'rail'
    selected?: boolean
    focusable?: boolean
    disabled?: boolean
    role?: string
  }>(),
  {
    as: 'section',
    variant: 'default',
    selected: false,
    focusable: false,
    disabled: false,
    role: undefined,
  },
)

const emit = defineEmits<{
  click: [event: MouseEvent | KeyboardEvent]
}>()

const isFocusable = computed(() => props.focusable || props.variant === 'interactive')
const tabIndex = computed(() => (props.disabled || !isFocusable.value ? undefined : 0))
const computedRole = computed(() => props.role ?? (isFocusable.value ? 'button' : undefined))
const classes = computed(() => [
  `app-card--${props.variant}`,
  {
    'app-card--selected': props.selected,
    'app-card--focusable': isFocusable.value,
    'app-card--disabled': props.disabled,
  },
])

function handleClick(event: MouseEvent) {
  if (props.disabled) {
    event.preventDefault()
    event.stopPropagation()
    return
  }

  emit('click', event)
}

function handleKeyboardActivate(event: KeyboardEvent) {
  if (props.disabled || !isFocusable.value || event.target !== event.currentTarget) {
    return
  }

  emit('click', event)
}
</script>

<style scoped>
.app-card {
  background: var(--bookos-surface);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  color: var(--bookos-text-primary);
}

.app-card--muted {
  background: var(--bookos-surface-muted);
  box-shadow: none;
}

.app-card--highlight {
  background: linear-gradient(135deg, var(--bookos-primary-soft), var(--bookos-accent-soft));
  border-color: color-mix(in srgb, var(--bookos-primary) 24%, var(--bookos-border));
}

.app-card--interactive {
  cursor: pointer;
  transition:
    border-color var(--motion-base) var(--motion-ease),
    box-shadow var(--motion-base) var(--motion-ease),
    transform var(--motion-base) var(--motion-ease);
}

.app-card--interactive:hover {
  border-color: var(--bookos-border-strong);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

.app-card--interactive:active {
  transform: translateY(0);
}

.app-card--rail {
  background: var(--bookos-surface);
  border-radius: var(--radius-md);
  box-shadow: 0 8px 22px rgba(54, 42, 24, 0.06);
}

.app-card--selected {
  border-color: var(--bookos-primary);
  box-shadow: var(--focus-ring);
}

.app-card--focusable:focus-visible {
  outline: 2px solid var(--bookos-focus);
  outline-offset: 3px;
  box-shadow: var(--focus-ring);
}

.app-card--disabled {
  opacity: 0.62;
  cursor: not-allowed;
}
</style>
