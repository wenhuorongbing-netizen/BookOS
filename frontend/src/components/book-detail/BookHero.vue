<template>
  <AppCard as="section" class="book-hero" variant="default" aria-labelledby="book-detail-title">
    <div class="book-hero__cover">
      <BookCover :title="book.title" :cover-url="coverUrl" />
    </div>

    <div class="book-hero__content">
      <div class="book-hero__topline">
        <BookStatusBadge :status="status" />
        <FavoriteBookButton
          :active="isFavorite"
          :book-title="book.title"
          :disabled="!book.inLibrary"
          :loading="favoriteLoading"
          @toggle="$emit('toggle-favorite')"
        />
      </div>

      <div class="book-hero__identity">
        <h1 id="book-detail-title" class="book-title">{{ book.title }}</h1>
        <p v-if="book.subtitle" class="book-hero__subtitle">{{ book.subtitle }}</p>
        <p class="book-hero__authors">{{ authorLine }}</p>
        <div class="book-hero__meta-line" aria-label="Book metadata">
          <span v-if="book.category">{{ book.category }}</span>
          <span v-if="book.publisher">{{ book.publisher }}</span>
          <span v-if="book.publicationYear">{{ book.publicationYear }}</span>
          <span v-if="book.readingFormat">{{ formatReadingFormat(book.readingFormat) }}</span>
        </div>
      </div>

      <ReadingProgressBar
        :title="book.title"
        :progress="progress"
        :current-page="book.currentPage"
        :total-pages="book.totalPages"
      />

      <BookMetaStats
        :current-page="book.currentPage"
        :total-pages="book.totalPages"
        :last-read-at="book.lastReadAt"
        :notes-count="book.notesCount"
        :quotes-count="book.quotesCount"
        :action-items-count="book.actionItemsCount"
        :concepts-count="book.conceptsCount ?? book.ontologyConceptCount"
      />
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { BookRecord, ReadingFormat, ReadingStatus } from '../../types'
import AppCard from '../ui/AppCard.vue'
import BookStatusBadge from '../BookStatusBadge.vue'
import BookCover from './BookCover.vue'
import BookMetaStats from './BookMetaStats.vue'
import FavoriteBookButton from './FavoriteBookButton.vue'
import ReadingProgressBar from './ReadingProgressBar.vue'

const props = defineProps<{
  book: BookRecord
  status: ReadingStatus | null | undefined
  progress: number | null | undefined
  rating: number | null | undefined
  favoriteLoading?: boolean
}>()

defineEmits<{
  'toggle-favorite': []
}>()

const authorLine = computed(() => (props.book.authors.length ? props.book.authors.join(', ') : 'Unknown author'))
const coverUrl = computed(() => props.book.coverImageUrl ?? props.book.coverUrl)
const isFavorite = computed(() => (props.rating ?? 0) >= 5)

const readingFormatLabels: Record<ReadingFormat, string> = {
  PHYSICAL: 'Physical',
  EBOOK: 'Ebook',
  AUDIOBOOK: 'Audiobook',
  PDF: 'PDF',
  WEB: 'Web',
  OTHER: 'Other format',
}

function formatReadingFormat(format: ReadingFormat) {
  return readingFormatLabels[format]
}
</script>

<style scoped>
.book-hero {
  position: relative;
  overflow: hidden;
  padding: var(--space-6);
  display: grid;
  grid-template-columns: minmax(170px, 220px) minmax(0, 1fr);
  gap: var(--space-8);
}

.book-hero::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 78% 18%, rgba(31, 95, 87, 0.12), transparent 30%),
    radial-gradient(circle at 20% 110%, rgba(197, 107, 44, 0.12), transparent 28%);
  pointer-events: none;
}

.book-hero__cover,
.book-hero__content {
  position: relative;
}

.book-hero__content {
  display: grid;
  gap: var(--space-5);
  align-content: start;
}

.book-hero__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.book-hero__identity {
  display: grid;
  gap: var(--space-2);
}

.book-hero h1 {
  margin: 0;
  max-width: 15ch;
  color: var(--bookos-text-primary);
  font-size: clamp(2.35rem, 5vw, 4.1rem);
  line-height: 1.02;
}

.book-hero__subtitle,
.book-hero__authors,
.book-hero__meta-line {
  margin: 0;
}

.book-hero__subtitle {
  max-width: 60ch;
  color: var(--bookos-text-secondary);
  font-size: 1.05rem;
}

.book-hero__authors {
  color: var(--bookos-primary-hover);
  font-size: 1rem;
  font-weight: 800;
}

.book-hero__meta-line {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 700;
}

.book-hero__meta-line span + span::before {
  content: "/";
  margin-right: var(--space-2);
  color: var(--bookos-border-strong);
}

@media (max-width: 980px) {
  .book-hero {
    grid-template-columns: minmax(140px, 180px) minmax(0, 1fr);
    gap: var(--space-5);
  }
}

@media (max-width: 700px) {
  .book-hero {
    grid-template-columns: 1fr;
    padding: var(--space-5);
  }

  .book-hero__cover {
    max-width: 220px;
  }
}
</style>
