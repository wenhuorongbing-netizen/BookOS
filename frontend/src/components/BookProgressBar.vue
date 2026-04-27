<template>
  <AppProgressBar class="progress-wrap" :value="progress" label="Reading progress" />
</template>

<script setup lang="ts">
import AppProgressBar from './ui/AppProgressBar.vue'

defineProps<{
  progress: number | null | undefined
}>()
</script>

<style scoped>
.progress-wrap {
  min-width: 120px;
}
</style>
