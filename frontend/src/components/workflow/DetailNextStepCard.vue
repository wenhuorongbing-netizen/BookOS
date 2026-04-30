<template>
  <AppCard class="detail-next-step" as="section" variant="highlight" :aria-labelledby="headingId">
    <div class="detail-next-step__content">
      <div>
        <p class="detail-next-step__eyebrow">{{ eyebrow }}</p>
        <h2 :id="headingId">{{ title }}</h2>
        <p>{{ description }}</p>
      </div>

      <ol v-if="loop.length" class="detail-next-step__loop" aria-label="Core workflow loop">
        <li v-for="step in loop" :key="step">
          <span>{{ step }}</span>
        </li>
      </ol>
    </div>

    <div class="detail-next-step__actions">
      <RouterLink v-if="primaryTo && !primaryDisabled" :to="primaryTo" custom v-slot="{ navigate }">
        <AppButton variant="primary" :loading="primaryLoading" @click="navigate">{{ primaryLabel }}</AppButton>
      </RouterLink>
      <AppButton
        v-else
        variant="primary"
        :loading="primaryLoading"
        :disabled="primaryDisabled"
        @click="$emit('primary')"
      >
        {{ primaryLabel }}
      </AppButton>

      <RouterLink v-if="secondaryLabel && secondaryTo && !secondaryDisabled" :to="secondaryTo" custom v-slot="{ navigate }">
        <AppButton variant="secondary" :loading="secondaryLoading" @click="navigate">{{ secondaryLabel }}</AppButton>
      </RouterLink>
      <AppButton
        v-else-if="secondaryLabel"
        variant="secondary"
        :loading="secondaryLoading"
        :disabled="secondaryDisabled"
        @click="$emit('secondary')"
      >
        {{ secondaryLabel }}
      </AppButton>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RouteLocationRaw } from 'vue-router'
import { RouterLink } from 'vue-router'
import AppButton from '../ui/AppButton.vue'
import AppCard from '../ui/AppCard.vue'

const props = withDefaults(
  defineProps<{
    eyebrow?: string
    title: string
    description: string
    primaryLabel: string
    primaryTo?: RouteLocationRaw | null
    primaryLoading?: boolean
    primaryDisabled?: boolean
    secondaryLabel?: string | null
    secondaryTo?: RouteLocationRaw | null
    secondaryLoading?: boolean
    secondaryDisabled?: boolean
    loop?: string[]
  }>(),
  {
    eyebrow: 'Next best action',
    primaryTo: null,
    primaryLoading: false,
    primaryDisabled: false,
    secondaryLabel: null,
    secondaryTo: null,
    secondaryLoading: false,
    secondaryDisabled: false,
    loop: () => [],
  },
)

defineEmits<{
  primary: []
  secondary: []
}>()

const headingId = computed(() => `detail-next-step-${props.title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`)
</script>

<style scoped>
.detail-next-step {
  padding: var(--space-5);
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--space-4);
  align-items: center;
}

.detail-next-step__content {
  display: grid;
  gap: var(--space-3);
}

.detail-next-step__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--bookos-primary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-next-step h2,
.detail-next-step p {
  margin: 0;
}

.detail-next-step h2 {
  color: var(--bookos-text-primary);
  font-family: var(--font-display);
  font-size: clamp(1.2rem, 1.8vw, 1.55rem);
  line-height: 1.12;
}

.detail-next-step p {
  max-width: 68ch;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.detail-next-step__loop {
  margin: 0;
  padding: 0;
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  list-style: none;
}

.detail-next-step__loop li {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-meta);
  font-weight: 800;
}

.detail-next-step__loop li:not(:last-child)::after {
  content: "->";
  color: var(--bookos-accent);
}

.detail-next-step__loop span {
  padding: 0.25rem 0.55rem;
  border: 1px solid color-mix(in srgb, var(--bookos-primary) 18%, var(--bookos-border));
  border-radius: 999px;
  background: color-mix(in srgb, var(--bookos-surface) 82%, var(--bookos-primary-soft));
}

.detail-next-step__actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
}

@media (max-width: 760px) {
  .detail-next-step {
    grid-template-columns: 1fr;
  }

  .detail-next-step__actions {
    justify-content: flex-start;
  }
}
</style>
