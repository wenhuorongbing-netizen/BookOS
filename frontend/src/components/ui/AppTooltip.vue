<template>
  <el-tooltip
    :content="content"
    :placement="placement"
    :disabled="disabled || !content"
    :show-after="showAfter"
    :hide-after="hideAfter"
  >
    <slot />
  </el-tooltip>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    content?: string
    placement?: 'top' | 'top-start' | 'top-end' | 'bottom' | 'bottom-start' | 'bottom-end' | 'left' | 'right'
    disabled?: boolean
    showAfter?: number
    hideAfter?: number
  }>(),
  {
    content: '',
    placement: 'top',
    disabled: false,
    showAfter: 250,
    hideAfter: 80,
  },
)
</script>
