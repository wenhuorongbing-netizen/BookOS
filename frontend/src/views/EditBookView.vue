<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="eyebrow">Edit</div>
        <h1>Edit book metadata</h1>
        <p>Adjust shared catalog metadata without touching personal library state.</p>
      </div>
    </div>

    <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" />
    <BookFormPanel v-if="formValue" :initial-value="formValue" submit-label="Save Changes" :loading="loading" @submit="handleSubmit" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getBook, updateBook } from '../api/books'
import BookFormPanel from '../components/BookFormPanel.vue'
import type { BookPayload } from '../types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const formValue = ref<BookPayload | null>(null)

onMounted(loadBook)

async function loadBook() {
  loading.value = true
  try {
    const book = await getBook(Number(route.params.id))
    formValue.value = {
      title: book.title,
      subtitle: book.subtitle,
      description: book.description,
      isbn: book.isbn,
      publisher: book.publisher,
      publicationYear: book.publicationYear,
      coverUrl: book.coverUrl,
      category: book.category,
      visibility: book.visibility,
      authors: book.authors,
      tags: book.tags,
    }
  } catch {
    error.value = 'Failed to load the book.'
  } finally {
    loading.value = false
  }
}

async function handleSubmit(payload: BookPayload) {
  error.value = ''
  loading.value = true
  try {
    await updateBook(Number(route.params.id), payload)
    ElMessage.success('Book updated.')
    router.push({ name: 'book-detail', params: { id: route.params.id } })
  } catch {
    error.value = 'Failed to update the book.'
  } finally {
    loading.value = false
  }
}
</script>
