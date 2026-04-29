<template>
  <div class="mini-list">
    <RouterLink
      v-for="thread in threads"
      :key="thread.id"
      class="mini-thread"
      :to="{ name: 'forum-thread', params: { id: thread.id } }"
    >
      <strong>{{ thread.title }}</strong>
      <small>{{ thread.commentCount }} comments · {{ thread.likeCount }} likes</small>
    </RouterLink>
    <p v-if="!threads.length" class="mini-empty">{{ emptyLabel }}</p>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { ForumThreadRecord } from '../../types'

defineProps<{
  threads: ForumThreadRecord[]
  emptyLabel: string
}>()
</script>

<style scoped>
.mini-list {
  display: grid;
  gap: var(--space-2);
}

.mini-thread {
  color: inherit;
  text-decoration: none;
  display: grid;
  gap: var(--space-1);
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.mini-thread strong {
  color: var(--bookos-text-primary);
}

.mini-thread small,
.mini-empty {
  color: var(--bookos-text-secondary);
}

.mini-empty {
  margin: 0;
}
</style>
