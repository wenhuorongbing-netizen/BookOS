<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="eyebrow">Five-Star Shelf</div>
        <h1>Books you rated 5/5</h1>
        <p>These are the books that have already proven useful enough to keep resurfacing.</p>
      </div>
    </div>
    <div v-if="books.length" class="responsive-grid">
      <BookCard v-for="book in books" :key="book.id" :book="book" @primary="openBook" />
    </div>
    <div v-else class="surface-card empty-state">No five-star books yet.</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getFiveStarBooks } from '../api/userBooks'
import type { BookRecord, UserBookRecord } from '../types'
import BookCard from '../components/BookCard.vue'

const router = useRouter()
const books = ref<UserBookRecord[]>([])

onMounted(async () => {
  books.value = await getFiveStarBooks()
})

function openBook(book: BookRecord | UserBookRecord) {
  const id = 'bookId' in book ? book.bookId : book.id
  router.push({ name: 'book-detail', params: { id } })
}
</script>
