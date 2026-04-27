<template>
  <el-rate :model-value="rating ?? 0" disabled text-color="#8a5a14" />
</template>

<script setup lang="ts">
const props = defineProps<{
  rating: number | null | undefined
}>()
</script>
