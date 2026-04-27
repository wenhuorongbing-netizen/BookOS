<template>
  <div class="reading-progress">
    <div class="reading-progress__header">
      <span class="reading-progress__label">Reading progress</span>
      <strong>{{ safeProgress }}%</strong>
    </div>
    <AppProgressBar
      :value="safeProgress"
      :label="`Reading progress for ${title}`"
      :show-value="false"
      tone="primary"
      size="md"
    />
    <p>{{ pageStatus }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppProgressBar from '../ui/AppProgressBar.vue'

const props = defineProps<{
  title: string
  progress: number | null | undefined
  currentPage?: number | null
  totalPages?: number | null
}>()

const safeProgress = computed(() => Math.min(Math.max(Math.round(props.progress ?? 0), 0), 100))
const pageStatus = computed(() => {
  if (typeof props.currentPage === 'number' && typeof props.totalPages === 'number' && props.totalPages > 0) {
    return `Page ${props.currentPage} of ${props.totalPages}`
  }

  return 'Page status not tracked yet'
})
</script>

<style scoped>
.reading-progress {
  display: grid;
  gap: var(--space-2);
}

.reading-progress__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
}

.reading-progress__label {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.reading-progress__header strong {
  color: var(--bookos-primary-hover);
  font-size: clamp(1.35rem, 3vw, 2.15rem);
  line-height: 1;
}

.reading-progress p {
  margin: 0;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}
</style>
