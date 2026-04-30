<template>
  <div class="use-case-progress" aria-label="Use case guide scope">
    <div>
      <span class="use-case-progress__value">{{ stepCount }}</span>
      <span class="use-case-progress__label">steps</span>
    </div>
    <div>
      <span class="use-case-progress__value">{{ timeRequired }}</span>
      <span class="use-case-progress__label">estimated time</span>
    </div>
    <p>This guide does not mark work complete. Progress happens only when you use the linked BookOS workflows.</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  stepCount: number
  timeRequired: string
}>()
</script>

<style scoped>
.use-case-progress {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.use-case-progress > div {
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

@media (max-width: 640px) {
  .use-case-progress {
    grid-template-columns: 1fr;
  }
}
</style>
