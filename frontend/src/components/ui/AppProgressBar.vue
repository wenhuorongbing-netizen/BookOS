<template>
  <div class="app-progress" :class="[`app-progress--${size}`, `app-progress--${tone}`]">
    <div
      class="app-progress__track"
      role="progressbar"
      :aria-label="label"
      :aria-valuemin="0"
      :aria-valuemax="safeMax"
      :aria-valuenow="normalizedValue"
      :aria-valuetext="`${percent}% complete`"
    >
      <div class="app-progress__bar" :style="barStyle" />
    </div>
    <span v-if="showValue" class="app-progress__value">{{ percent }}%</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CSSProperties } from 'vue'

const props = withDefaults(
  defineProps<{
    value: number | null | undefined
    max?: number
    label?: string
    showValue?: boolean
    size?: 'sm' | 'md'
    tone?: 'primary' | 'accent' | 'success' | 'warning'
  }>(),
  {
    max: 100,
    label: 'Progress',
    showValue: true,
    size: 'md',
    tone: 'primary',
  },
)

const safeMax = computed(() => Math.max(1, props.max))
const normalizedValue = computed(() => Math.min(Math.max(props.value ?? 0, 0), safeMax.value))
const percent = computed(() => Math.round((normalizedValue.value / safeMax.value) * 100))
const barStyle = computed<CSSProperties>(() => ({ width: `${percent.value}%` }))
</script>

<style scoped>
.app-progress {
  display: grid;
  grid-template-columns: minmax(80px, 1fr) auto;
  align-items: center;
  gap: var(--space-2);
  min-width: 120px;
}

.app-progress__track {
  overflow: hidden;
  width: 100%;
  background: color-mix(in srgb, var(--bookos-border) 58%, transparent);
  border-radius: 999px;
}

.app-progress--sm .app-progress__track {
  height: 8px;
}

.app-progress--md .app-progress__track {
  height: 10px;
}

.app-progress__bar {
  height: 100%;
  border-radius: inherit;
  transition: width var(--motion-base) var(--motion-ease);
}

.app-progress--primary .app-progress__bar {
  background: var(--bookos-primary);
}

.app-progress--accent .app-progress__bar {
  background: var(--bookos-accent);
}

.app-progress--success .app-progress__bar {
  background: var(--bookos-success);
}

.app-progress--warning .app-progress__bar {
  background: var(--bookos-warning);
}

.app-progress__value {
  color: var(--bookos-text-secondary);
  font-size: 0.78rem;
  font-weight: 700;
  min-width: 2.4rem;
  text-align: right;
}
</style>
