<template>
  <AppIconButton
    :label="label"
    :tooltip="label"
    :selected="active"
    :disabled="disabled"
    :loading="loading"
    variant="accent"
    @click="$emit('toggle')"
  >
    <span aria-hidden="true">{{ active ? '5*' : 'ST' }}</span>
  </AppIconButton>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppIconButton from '../ui/AppIconButton.vue'

const props = withDefaults(
  defineProps<{
    active: boolean
    bookTitle: string
    disabled?: boolean
    loading?: boolean
  }>(),
  {
    disabled: false,
    loading: false,
  },
)

defineEmits<{
  toggle: []
}>()

const label = computed(() => {
  if (props.disabled) return `Add ${props.bookTitle} to your library before marking it as a favorite`
  return props.active ? `Remove ${props.bookTitle} from five-star favorites` : `Mark ${props.bookTitle} as a five-star favorite`
})
</script>
