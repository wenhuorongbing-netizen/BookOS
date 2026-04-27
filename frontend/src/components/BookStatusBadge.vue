<template>
  <AppBadge :variant="variant">{{ label }}</AppBadge>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ReadingStatus } from '../types'
import AppBadge from './ui/AppBadge.vue'

const props = defineProps<{
  status: ReadingStatus | null | undefined
}>()

const labelMap: Record<ReadingStatus, string> = {
  BACKLOG: 'Backlog',
  CURRENTLY_READING: 'Currently Reading',
  COMPLETED: 'Completed',
  PAUSED: 'Paused',
  DROPPED: 'Dropped',
  REFERENCE: 'Reference',
  ANTI_LIBRARY: 'Anti-Library',
}

const label = computed(() => (props.status ? labelMap[props.status] : 'Not in Library'))

type BadgeVariant = 'neutral' | 'primary' | 'accent' | 'success' | 'warning' | 'danger' | 'info'

const variant = computed<BadgeVariant>(() => {
  if (!props.status) return 'neutral'
  if (props.status === 'CURRENTLY_READING') return 'primary'
  if (props.status === 'COMPLETED') return 'success'
  if (props.status === 'PAUSED') return 'warning'
  if (props.status === 'DROPPED' || props.status === 'ANTI_LIBRARY') return 'danger'
  if (props.status === 'REFERENCE') return 'info'
  return 'neutral'
})
</script>
