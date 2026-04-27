<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="eyebrow">Currently Reading</div>
        <h1>Books in active progress</h1>
        <p>Use this view to focus on the books that are actually moving through your design workflow.</p>
      </div>
    </div>
    <div v-if="books.length" class="responsive-grid">
      <BookCard v-for="book in books" :key="book.id" :book="book" @primary="openBook" />
    </div>
    <div v-else class="surface-card empty-state">Nothing is currently marked as in progress.</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentlyReading } from '../api/userBooks'
import type { BookRecord, UserBookRecord } from '../types'
import BookCard from '../components/BookCard.vue'

const router = useRouter()
const books = ref<UserBookRecord[]>([])

onMounted(async () => {
  books.value = await getCurrentlyReading()
})

function openBook(book: BookRecord | UserBookRecord) {
  const id = 'bookId' in book ? book.bookId : book.id
  router.push({ name: 'book-detail', params: { id } })
}
</script>
