<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="eyebrow">Anti-Library</div>
        <h1>Unread books worth keeping in sight</h1>
        <p>This shelf tracks the books you own or collected but have not started, so backlog stays intentional instead of invisible.</p>
      </div>
    </div>
    <div v-if="books.length" class="responsive-grid">
      <BookCard v-for="book in books" :key="book.id" :book="book" @primary="openBook" />
    </div>
    <div v-else class="surface-card empty-state">No anti-library books yet.</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAntiLibraryBooks } from '../api/userBooks'
import type { BookRecord, UserBookRecord } from '../types'
import BookCard from '../components/BookCard.vue'

const router = useRouter()
const books = ref<UserBookRecord[]>([])

onMounted(async () => {
  books.value = await getAntiLibraryBooks()
})

function openBook(book: BookRecord | UserBookRecord) {
  const id = 'bookId' in book ? book.bookId : book.id
  router.push({ name: 'book-detail', params: { id } })
}
</script>
