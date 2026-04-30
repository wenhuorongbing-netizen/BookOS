<template>
  <div class="use-case-progress" aria-label="Use case progress">
    <div>
      <span class="use-case-progress__value">{{ completedCount }}/{{ stepCount }}</span>
      <span class="use-case-progress__label">steps complete</span>
    </div>
    <div>
      <span class="use-case-progress__value">{{ timeRequired }}</span>
      <span class="use-case-progress__label">estimated time</span>
    </div>
    <div>
      <span class="use-case-progress__value">{{ statusLabel }}</span>
      <span class="use-case-progress__label">status</span>
    </div>
    <div class="use-case-progress__bar" role="progressbar" :aria-valuenow="percent" aria-valuemin="0" aria-valuemax="100">
      <span :style="{ width: `${percent}%` }" />
    </div>
    <p>{{ helperText }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserUseCaseProgressRecord } from '../../types'

const props = defineProps<{
  stepCount: number
  timeRequired: string
  progress: UserUseCaseProgressRecord | null
}>()

const completedCount = computed(() => Math.min(props.progress?.effectiveCompletedStepKeys.length ?? 0, props.stepCount))
const percent = computed(() => (props.stepCount ? Math.round((completedCount.value / props.stepCount) * 100) : 0))
const statusLabel = computed(() => {
  if (!props.progress || props.progress.status === 'NOT_STARTED') return 'Not started'
  if (props.progress.status === 'COMPLETED' || completedCount.value === props.stepCount) return 'Complete'
  return 'In progress'
})
const helperText = computed(() => {
  if (!props.progress || props.progress.status === 'NOT_STARTED') return 'Click Start to track this checklist. Automatic checks use real records only.'
  if (statusLabel.value === 'Complete') return 'Checklist complete from real actions and manual confirmations.'
  return 'Continue through the remaining steps. Manual completion is available only where automatic detection is not available.'
})
</script>

<style scoped>
.use-case-progress {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.use-case-progress > div:not(.use-case-progress__bar) {
  display: grid;
  gap: var(--space-1);
}

.use-case-progress__value {
  color: var(--bookos-primary);
  font-size: 1.28rem;
  font-weight: 900;
}

.use-case-progress__label {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.use-case-progress p {
  grid-column: 1 / -1;
  margin: 0;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  line-height: 1.45;
}

.use-case-progress__bar {
  grid-column: 1 / -1;
  height: 0.55rem;
  overflow: hidden;
  border-radius: 999px;
  background: var(--bookos-border);
}

.use-case-progress__bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--bookos-primary), var(--bookos-accent));
}

@media (max-width: 640px) {
  .use-case-progress {
    grid-template-columns: 1fr;
  }
}
</style>
