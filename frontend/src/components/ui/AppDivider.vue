<template>
  <component
    :is="decorative ? 'div' : 'hr'"
    class="app-divider"
    :class="`app-divider--${orientation}`"
    :aria-hidden="decorative ? 'true' : undefined"
  />
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    orientation?: 'horizontal' | 'vertical'
    decorative?: boolean
  }>(),
  {
    orientation: 'horizontal',
    decorative: false,
  },
)
</script>

<style scoped>
.app-divider {
  margin: 0;
  border: 0;
  background: var(--bookos-border);
}

.app-divider--horizontal {
  width: 100%;
  height: 1px;
}

.app-divider--vertical {
  align-self: stretch;
  width: 1px;
  min-height: 24px;
}
</style>
