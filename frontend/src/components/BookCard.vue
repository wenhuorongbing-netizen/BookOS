<template>
  <AppCard as="article" class="book-card">
    <div class="book-card__cover">
      <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
      <div v-else class="cover-placeholder">{{ book.title }}</div>
    </div>
    <div class="book-card__body">
      <div class="book-card__topline">
        <BookStatusBadge :status="book.readingStatus" />
        <AppBadge v-if="book.category" variant="neutral" size="sm">{{ book.category }}</AppBadge>
      </div>
      <h3>{{ book.title }}</h3>
      <p class="book-card__authors">{{ book.authors.join(', ') }}</p>
      <p v-if="book.subtitle" class="muted">{{ book.subtitle }}</p>
      <BookProgressBar v-if="book.progressPercent !== null" :progress="book.progressPercent" />
      <BookRating :rating="book.rating" />
      <div class="book-card__tags">
        <AppBadge v-for="tag in book.tags" :key="tag" variant="primary" size="sm">{{ tag }}</AppBadge>
      </div>
      <div class="book-card__actions">
        <AppButton variant="secondary" @click="$emit('primary', book)">Open</AppButton>
        <AppButton v-if="showSecondary" variant="ghost" @click="$emit('secondary', book)">
          {{ secondaryLabel }}
        </AppButton>
      </div>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import type { BookRecord, UserBookRecord } from '../types'
import BookProgressBar from './BookProgressBar.vue'
import BookRating from './BookRating.vue'
import BookStatusBadge from './BookStatusBadge.vue'
import AppBadge from './ui/AppBadge.vue'
import AppButton from './ui/AppButton.vue'
import AppCard from './ui/AppCard.vue'

defineProps<{
  book: BookRecord | UserBookRecord
  showSecondary?: boolean
  secondaryLabel?: string
}>()

defineEmits<{
  primary: [book: BookRecord | UserBookRecord]
  secondary: [book: BookRecord | UserBookRecord]
}>()
</script>

<style scoped>
.book-card {
  display: grid;
  grid-template-columns: 132px 1fr;
  gap: var(--space-4);
  padding: var(--space-4);
}

.book-card__cover img,
.book-card__cover .cover-placeholder {
  width: 100%;
  min-height: 200px;
  object-fit: cover;
  border-radius: var(--radius-lg);
}

.book-card__body {
  display: grid;
  gap: var(--space-3);
}

.book-card__topline {
  display: flex;
  justify-content: space-between;
  gap: var(--space-2);
  align-items: center;
}

.book-card h3 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.28rem;
  line-height: 1.12;
}

.book-card__authors {
  margin: calc(var(--space-2) * -1) 0 0;
  color: var(--bookos-text-secondary);
  font-weight: 600;
}

.book-card__tags,
.book-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 700px) {
  .book-card {
    grid-template-columns: 1fr;
  }
}
</style>
