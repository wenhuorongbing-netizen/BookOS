<template>
  <div class="book-context-header">
    <div>
      <div v-if="eyebrow" class="eyebrow">{{ eyebrow }}</div>
      <p>{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="book-context-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow?: string
    description?: string
  }>(),
  {
    eyebrow: 'Book Detail',
    description: 'Reading source, library state, and knowledge extraction context.',
  },
)
</script>

<style scoped>
.book-context-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.book-context-header p {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-secondary);
  font-size: var(--type-body);
}

.book-context-header__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}
</style>
