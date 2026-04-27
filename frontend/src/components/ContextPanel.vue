<template>
  <AppCard as="section" class="context-panel" variant="rail">
    <button
      class="context-panel__header"
      type="button"
      :id="headingId"
      :aria-controls="panelId"
      :aria-expanded="isOpen ? 'true' : 'false'"
      @click="isOpen = !isOpen"
    >
      <span>
        <span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span>
        <strong>{{ title }}</strong>
      </span>
      <span class="context-panel__chevron" aria-hidden="true">{{ isOpen ? 'Less' : 'More' }}</span>
    </button>

    <div v-show="isOpen" :id="panelId" class="context-panel__body" role="region" :aria-labelledby="headingId">
      <slot />
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { ref, useId } from 'vue'
import AppCard from './ui/AppCard.vue'

const props = withDefaults(
  defineProps<{
    title: string
    eyebrow?: string
    defaultOpen?: boolean
  }>(),
  {
    eyebrow: '',
    defaultOpen: true,
  },
)

const baseId = useId()
const headingId = `${baseId}-heading`
const panelId = `${baseId}-panel`
const isOpen = ref(props.defaultOpen)
</script>

<style scoped>
.context-panel {
  padding: 0;
  overflow: hidden;
}

.context-panel__header {
  width: 100%;
  min-height: var(--touch-target);
  padding: var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  border: 0;
  background: transparent;
  color: var(--bookos-text-primary);
  text-align: left;
  cursor: pointer;
}

.context-panel__header strong {
  display: block;
  margin-top: var(--space-1);
  font-size: var(--type-card-title);
}

.context-panel__chevron {
  flex: 0 0 auto;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.context-panel__body {
  padding: 0 var(--space-4) var(--space-4);
  display: grid;
  gap: var(--space-3);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  line-height: 1.5;
}
</style>
