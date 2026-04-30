<template>
  <ol class="use-case-steps">
    <li v-for="(step, index) in steps" :key="stepKey(step, index)" class="use-case-steps__item" :class="{ 'use-case-steps__item--done': isDone(step, index) }">
      <div class="use-case-steps__number" aria-hidden="true">{{ isDone(step, index) ? 'OK' : index + 1 }}</div>
      <div class="use-case-steps__content">
        <div class="use-case-steps__heading">
          <h3>{{ step.title }}</h3>
          <AppBadge v-if="isDone(step, index)" :variant="isAutomaticDone(step, index) ? 'success' : 'primary'" size="sm">
            {{ isAutomaticDone(step, index) ? 'Auto verified' : 'Complete' }}
          </AppBadge>
        </div>
        <p>{{ step.description }}</p>
        <p v-if="step.autoCheckLabel" class="use-case-steps__auto">{{ step.autoCheckLabel }}</p>
        <div class="use-case-steps__actions">
          <UseCaseActionButton :to="step.route" :label="step.actionLabel" variant="ghost" />
          <AppButton v-if="step.autoCheckLabel && !isDone(step, index)" variant="secondary" @click="$emit('refresh')">
            Verify automatically
          </AppButton>
          <AppButton
            v-else-if="!step.autoCheckLabel && !isDone(step, index)"
            variant="secondary"
            :disabled="!started"
            @click="$emit('complete', stepKey(step, index))"
          >
            Mark manually complete
          </AppButton>
        </div>
      </div>
    </li>
  </ol>
</template>

<script setup lang="ts">
import type { UseCaseStep } from '../../data/useCases'
import type { UserUseCaseProgressRecord } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import UseCaseActionButton from './UseCaseActionButton.vue'

const props = defineProps<{
  useCaseSlug: string
  steps: UseCaseStep[]
  progress: UserUseCaseProgressRecord | null
  started: boolean
}>()

defineEmits<{
  complete: [stepKey: string]
  refresh: []
}>()

function stepKey(step: UseCaseStep, index: number) {
  return step.key ?? `${props.useCaseSlug}-step-${index + 1}`
}

function isDone(step: UseCaseStep, index: number) {
  return props.progress?.effectiveCompletedStepKeys.includes(stepKey(step, index)) ?? false
}

function isAutomaticDone(step: UseCaseStep, index: number) {
  return props.progress?.automaticCompletedStepKeys.includes(stepKey(step, index)) ?? false
}
</script>

<style scoped>
.use-case-steps {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: var(--space-3);
}

.use-case-steps__item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
}

.use-case-steps__item--done {
  border-color: color-mix(in srgb, var(--bookos-success) 36%, var(--bookos-border));
  background: color-mix(in srgb, var(--bookos-success) 7%, var(--bookos-surface));
}

.use-case-steps__number {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary);
  font-weight: 900;
}

.use-case-steps__item--done .use-case-steps__number {
  background: color-mix(in srgb, var(--bookos-success) 16%, var(--bookos-surface));
  color: var(--bookos-success);
}

.use-case-steps__content {
  display: grid;
  gap: var(--space-2);
}

.use-case-steps__heading,
.use-case-steps__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.use-case-steps h3 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-card-title);
}

.use-case-steps p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: 1.55;
}

.use-case-steps__auto {
  font-size: var(--type-metadata);
}

.use-case-steps :deep(.app-button) {
  justify-self: start;
}

@media (max-width: 640px) {
  .use-case-steps__item {
    grid-template-columns: 1fr;
  }
}
</style>
