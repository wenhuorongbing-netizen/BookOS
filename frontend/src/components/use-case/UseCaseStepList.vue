<template>
  <ol class="use-case-steps">
    <li
      v-for="(step, index) in steps"
      :key="stepKey(step, index)"
      class="use-case-steps__item"
      :class="{
        'use-case-steps__item--done': isDone(step, index),
        'use-case-steps__item--blocked': isBlocked(step, index),
      }"
    >
      <div class="use-case-steps__number" aria-hidden="true">{{ isDone(step, index) ? 'OK' : index + 1 }}</div>
      <div class="use-case-steps__content">
        <div class="use-case-steps__heading">
          <h3>{{ step.title }}</h3>
          <AppBadge v-if="verificationLabel(step, index)" :variant="verificationVariant(step, index)" size="sm">
            {{ verificationLabel(step, index) }}
          </AppBadge>
        </div>
        <p>{{ step.description }}</p>
        <p v-if="step.autoCheckLabel" class="use-case-steps__auto">{{ step.autoCheckLabel }}</p>
        <p v-if="verificationMessage(step, index)" class="use-case-steps__verification">
          {{ verificationMessage(step, index) }}
        </p>
        <div class="use-case-steps__actions">
          <UseCaseActionButton :to="step.route" :label="step.actionLabel" variant="ghost" />
          <AppButton v-if="canAutoVerify(step, index)" variant="secondary" @click="$emit('refresh')">
            Verify again
          </AppButton>
          <AppButton
            v-if="canCompleteManually(step, index)"
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
import type { UseCaseStepVerificationRecord, UserUseCaseProgressRecord } from '../../types'
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
  return getVerification(step, index)?.complete ?? props.progress?.effectiveCompletedStepKeys.includes(stepKey(step, index)) ?? false
}

function isAutomaticDone(step: UseCaseStep, index: number) {
  return getVerification(step, index)?.automatic ?? props.progress?.automaticCompletedStepKeys.includes(stepKey(step, index)) ?? false
}

function isManualDone(step: UseCaseStep, index: number) {
  return getVerification(step, index)?.manual ?? props.progress?.completedStepKeys.includes(stepKey(step, index)) ?? false
}

function getVerification(step: UseCaseStep, index: number): UseCaseStepVerificationRecord | null {
  return props.progress?.stepVerification?.[stepKey(step, index)] ?? null
}

function isBlocked(step: UseCaseStep, index: number) {
  const state = getVerification(step, index)?.state
  return state === 'blocked' || state === 'missingPrerequisite'
}

function verificationLabel(step: UseCaseStep, index: number) {
  const verification = getVerification(step, index)
  if (verification?.state === 'auto' || isAutomaticDone(step, index)) return 'Auto verified'
  if (verification?.state === 'manual' || isManualDone(step, index)) return 'Manually marked'
  if (verification?.state === 'missingPrerequisite') return 'Missing prerequisite'
  if (verification?.state === 'blocked') return 'Blocked'
  if (verification?.state === 'unavailable') return 'Manual only'
  return ''
}

function verificationVariant(step: UseCaseStep, index: number) {
  const state = getVerification(step, index)?.state
  if (state === 'auto' || isAutomaticDone(step, index)) return 'success'
  if (state === 'manual' || isManualDone(step, index)) return 'primary'
  if (state === 'missingPrerequisite' || state === 'blocked') return 'warning'
  return 'neutral'
}

function verificationMessage(step: UseCaseStep, index: number) {
  return getVerification(step, index)?.message ?? ''
}

function canAutoVerify(step: UseCaseStep, index: number) {
  const verification = getVerification(step, index)
  return !isDone(step, index) && (Boolean(step.autoCheckLabel) || verification?.state === 'blocked' || verification?.state === 'missingPrerequisite')
}

function canCompleteManually(step: UseCaseStep, index: number) {
  const verification = getVerification(step, index)
  return !isDone(step, index) && !step.autoCheckLabel && (!verification || verification.state === 'unavailable')
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

.use-case-steps__item--blocked {
  border-color: color-mix(in srgb, var(--bookos-warning) 34%, var(--bookos-border));
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

.use-case-steps__auto,
.use-case-steps__verification {
  font-size: var(--type-metadata);
}

.use-case-steps__verification {
  color: var(--bookos-text-muted);
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
