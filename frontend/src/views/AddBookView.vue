<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="eyebrow">Create</div>
        <h1>Add a book to BookOS</h1>
        <p>Start with a clean book record. Library attachment can happen immediately after creation.</p>
      </div>
    </div>

    <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" />
    <BookFormPanel :initial-value="initialValue" submit-label="Create Book" :loading="loading" @submit="handleSubmit" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createBook } from '../api/books'
import BookFormPanel from '../components/BookFormPanel.vue'
import type { BookPayload } from '../types'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const initialValue: BookPayload = {
  title: '',
  subtitle: null,
  description: null,
  isbn: null,
  publisher: null,
  publicationYear: 2026,
  coverUrl: null,
  category: null,
  visibility: 'PRIVATE',
  authors: [],
  tags: [],
}

async function handleSubmit(payload: BookPayload) {
  error.value = ''
  loading.value = true
  try {
    const book = await createBook(payload)
    ElMessage.success('Book created.')
    router.push({ name: 'book-detail', params: { id: book.id } })
  } catch {
    error.value = 'Book creation failed. Check the required fields and try again.'
  } finally {
    loading.value = false
  }
}
</script>
