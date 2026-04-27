<template>
  <dl class="book-meta-stats" aria-label="Book knowledge metadata">
    <div v-for="item in items" :key="item.label" class="book-meta-stats__item">
      <dt>
        <span class="book-meta-stats__icon" aria-hidden="true">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </dt>
      <dd>{{ item.value }}</dd>
    </div>
  </dl>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  currentPage?: number | null
  totalPages?: number | null
  lastReadAt?: string | null
  notesCount?: number | null
  quotesCount?: number | null
  actionItemsCount?: number | null
  conceptsCount?: number | null
}>()

const missing = 'Not tracked yet'

const pageValue = computed(() => {
  if (typeof props.currentPage === 'number' && typeof props.totalPages === 'number' && props.totalPages > 0) {
    return `Page ${props.currentPage} of ${props.totalPages}`
  }

  return missing
})

const lastReadValue = computed(() => {
  if (!props.lastReadAt) return missing
  const date = new Date(props.lastReadAt)
  if (Number.isNaN(date.getTime())) return props.lastReadAt
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(date)
})

function countValue(value: number | null | undefined) {
  return typeof value === 'number' ? String(value) : missing
}

const items = computed(() => [
  { icon: 'PG', label: 'Page', value: pageValue.value },
  { icon: 'LR', label: 'Last read', value: lastReadValue.value },
  { icon: 'NO', label: 'Notes', value: countValue(props.notesCount) },
  { icon: 'QT', label: 'Quotes', value: countValue(props.quotesCount) },
  { icon: 'AC', label: 'Actions', value: countValue(props.actionItemsCount) },
  { icon: 'CN', label: 'Concepts', value: countValue(props.conceptsCount) },
])
</script>

<style scoped>
.book-meta-stats {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--space-3);
}

.book-meta-stats__item {
  min-width: 0;
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-surface-muted) 68%, var(--bookos-surface));
}

.book-meta-stats dt {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.book-meta-stats dd {
  margin: var(--space-2) 0 0;
  color: var(--bookos-text-primary);
  font-size: var(--type-metadata);
  font-weight: 800;
  line-height: 1.25;
}

.book-meta-stats__icon {
  width: 28px;
  height: 28px;
  display: inline-grid;
  place-items: center;
  border-radius: 999px;
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
  font-size: 0.66rem;
  font-weight: 900;
}

@media (max-width: 1100px) {
  .book-meta-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .book-meta-stats {
    grid-template-columns: 1fr;
  }
}
</style>
