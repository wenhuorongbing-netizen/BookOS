<template>
  <section class="surface-card table-wrap" v-loading="loading" aria-label="Tracked books table and cards">
    <div class="book-table__desktop">
      <el-table :data="rows" empty-text="No books found">
      <el-table-column label="Book" min-width="280">
        <template #default="{ row }">
          <div class="book-cell">
            <div class="book-cell__cover">
              <img v-if="row.coverUrl" :src="row.coverUrl" :alt="`Cover of ${row.title}`" />
              <div v-else class="cover-placeholder">{{ row.title }}</div>
            </div>
            <div>
              <strong>{{ row.title }}</strong>
              <div class="muted">{{ row.authors.join(', ') }}</div>
              <div class="book-cell__tags">
                <span v-for="tag in row.tags" :key="tag" class="tag-chip">{{ tag }}</span>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Status" min-width="220">
        <template #default="{ row }">
          <div class="stack">
            <BookStatusBadge :status="row.readingStatus" />
            <el-select
              :model-value="row.readingStatus"
              :aria-label="`Reading status for ${row.title}`"
              @change="handleStatusChange(row.id, $event)"
            >
              <el-option v-for="status in statusOptions" :key="status" :label="status" :value="status" />
            </el-select>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Progress" min-width="220">
        <template #default="{ row }">
          <div class="stack">
            <BookProgressBar :progress="row.progressPercent" />
            <el-input-number
              :model-value="row.progressPercent"
              :min="0"
              :max="100"
              :aria-label="`Progress percentage for ${row.title}`"
              @change="handleProgressChange(row.id, $event)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Rating" min-width="200">
        <template #default="{ row }">
          <div class="stack">
            <BookRating :rating="row.rating" />
            <el-rate :model-value="row.rating ?? 0" :aria-label="`Rating for ${row.title}`" @change="handleRatingChange(row.id, $event)" />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="120" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" :aria-label="`Open details for ${row.title}`" @click="$emit('open', row)">Detail</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <div class="book-table__cards" aria-label="Tracked books cards">
      <article v-for="row in rows" :key="row.id" class="book-table-card">
        <button class="book-table-card__open" type="button" :aria-label="`Open details for ${row.title}`" @click="$emit('open', row)">
          <span class="book-table-card__cover">
            <img v-if="row.coverUrl" :src="row.coverUrl" :alt="`Cover of ${row.title}`" />
            <span v-else class="cover-placeholder">{{ row.title }}</span>
          </span>
          <span class="book-table-card__identity">
            <strong>{{ row.title }}</strong>
            <span>{{ row.authors.join(', ') || 'Unknown author' }}</span>
          </span>
        </button>

        <div class="book-table-card__tags" v-if="row.tags.length" aria-label="Book tags">
          <span v-for="tag in row.tags" :key="tag" class="tag-chip">{{ tag }}</span>
        </div>

        <div class="book-table-card__controls">
          <label>
            <span>Status</span>
            <el-select
              :model-value="row.readingStatus"
              :aria-label="`Reading status for ${row.title}`"
              @change="handleStatusChange(row.id, $event)"
            >
              <el-option v-for="status in statusOptions" :key="status" :label="status" :value="status" />
            </el-select>
          </label>
          <label>
            <span>Progress</span>
            <BookProgressBar :progress="row.progressPercent" />
            <el-input-number
              :model-value="row.progressPercent"
              :min="0"
              :max="100"
              :aria-label="`Progress percentage for ${row.title}`"
              @change="handleProgressChange(row.id, $event)"
            />
          </label>
          <label>
            <span>Rating</span>
            <BookRating :rating="row.rating" />
            <el-rate :model-value="row.rating ?? 0" :aria-label="`Rating for ${row.title}`" @change="handleRatingChange(row.id, $event)" />
          </label>
        </div>
      </article>

      <div v-if="!rows.length && !loading" class="empty-state">No books found. Adjust filters or add a book to your library.</div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ReadingStatus, UserBookRecord } from '../types'
import { readingStatusOptions } from '../types'
import BookProgressBar from './BookProgressBar.vue'
import BookRating from './BookRating.vue'
import BookStatusBadge from './BookStatusBadge.vue'

defineProps<{
  rows: UserBookRecord[]
  loading?: boolean
}>()

const emit = defineEmits<{
  open: [row: UserBookRecord]
  'status-change': [id: number, status: ReadingStatus]
  'progress-change': [id: number, progress: number]
  'rating-change': [id: number, rating: number]
}>()

const statusOptions = readingStatusOptions

function handleStatusChange(id: number, value: string | number | boolean) {
  if (statusOptions.includes(value as ReadingStatus)) {
    emit('status-change', id, value as ReadingStatus)
  }
}

function handleProgressChange(id: number, value: number | undefined) {
  emit('progress-change', id, Number(value ?? 0))
}

function handleRatingChange(id: number, value: number | undefined) {
  emit('rating-change', id, Number(value ?? 0))
}
</script>

<style scoped>
.table-wrap {
  padding: 1rem;
  overflow: hidden;
}

.book-table__desktop {
  min-width: 0;
  overflow-x: auto;
}

.book-cell {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 0.75rem;
  align-items: start;
}

.book-cell__cover img,
.book-cell__cover .cover-placeholder {
  width: 72px;
  min-height: 100px;
  border-radius: 12px;
  object-fit: cover;
}

.book-cell__tags {
  margin-top: 0.4rem;
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.stack {
  display: grid;
  gap: 0.55rem;
}

.book-table__cards {
  display: none;
  gap: var(--space-3);
}

.book-table-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
}

.book-table-card__open {
  min-width: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: var(--space-3);
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.book-table-card__cover img,
.book-table-card__cover .cover-placeholder {
  width: 72px;
  min-height: 100px;
  border-radius: var(--radius-md);
  object-fit: cover;
}

.book-table-card__identity {
  min-width: 0;
  display: grid;
  align-content: start;
  gap: var(--space-1);
}

.book-table-card__identity strong {
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.1rem;
  line-height: 1.15;
}

.book-table-card__identity span,
.book-table-card__controls label > span {
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.book-table-card__tags {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.book-table-card__controls {
  display: grid;
  gap: var(--space-3);
}

.book-table-card__controls label {
  display: grid;
  gap: var(--space-2);
}

@media (max-width: 760px) {
  .book-table__desktop {
    display: none;
  }

  .book-table__cards {
    display: grid;
  }
}

@media (max-width: 420px) {
  .book-table-card__open {
    grid-template-columns: 1fr;
  }

  .book-table-card__cover {
    max-width: 96px;
  }
}
</style>
