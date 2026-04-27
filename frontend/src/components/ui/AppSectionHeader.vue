<template>
  <div class="app-section-header" :class="{ 'app-section-header--compact': compact }">
    <div class="app-section-header__copy">
      <div v-if="eyebrow" class="eyebrow">{{ eyebrow }}</div>
      <component :is="headingTag" class="app-section-header__title">{{ title }}</component>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="app-section-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title: string
    eyebrow?: string
    description?: string
    level?: 1 | 2 | 3 | 4
    compact?: boolean
  }>(),
  {
    eyebrow: '',
    description: '',
    level: 2,
    compact: false,
  },
)

const headingTag = computed(() => `h${props.level}`)
</script>

<style scoped>
.app-section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.app-section-header--compact {
  gap: var(--space-2);
}

.app-section-header__copy {
  min-width: 0;
}

.app-section-header__title {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-primary);
  font-size: clamp(1.15rem, 2vw, 1.45rem);
  line-height: 1.18;
}

.app-section-header p {
  margin: var(--space-2) 0 0;
  color: var(--bookos-text-secondary);
  max-width: 44rem;
}

.app-section-header__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}
</style>
