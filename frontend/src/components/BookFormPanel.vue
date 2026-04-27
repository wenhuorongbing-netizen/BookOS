<template>
  <section class="surface-card form-panel">
    <el-form label-position="top" @submit.prevent>
      <div class="form-grid">
        <el-form-item label="Title" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="Subtitle">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="Authors" class="form-span-2">
          <el-input v-model="authorsText" placeholder="Comma-separated authors" />
        </el-form-item>
        <el-form-item label="Category">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="Visibility">
          <el-select v-model="form.visibility">
            <el-option v-for="item in visibilityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="Publisher">
          <el-input v-model="form.publisher" />
        </el-form-item>
        <el-form-item label="Publication Year">
          <el-input-number v-model="form.publicationYear" :min="1950" :max="2100" />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="form.isbn" />
        </el-form-item>
        <el-form-item label="Cover URL">
          <el-input v-model="form.coverUrl" />
        </el-form-item>
        <el-form-item label="Tags" class="form-span-2">
          <el-input v-model="tagsText" placeholder="Comma-separated tags" />
        </el-form-item>
        <el-form-item label="Description" class="form-span-2">
          <el-input v-model="form.description" type="textarea" :rows="6" />
        </el-form-item>
      </div>
      <div class="form-actions">
        <el-button type="primary" :loading="loading" @click="submitForm">{{ submitLabel }}</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { visibilityOptions, type BookPayload } from '../types'

const props = defineProps<{
  initialValue: BookPayload
  submitLabel: string
  loading?: boolean
}>()

const emit = defineEmits<{
  submit: [payload: BookPayload]
}>()

const form = reactive<BookPayload>({ ...props.initialValue })
const authorsText = ref(props.initialValue.authors.join(', '))
const tagsText = ref(props.initialValue.tags.join(', '))

watch(
  () => props.initialValue,
  (value) => {
    Object.assign(form, value)
    authorsText.value = value.authors.join(', ')
    tagsText.value = value.tags.join(', ')
  },
  { deep: true },
)

function submitForm() {
  emit('submit', {
    ...form,
    title: form.title.trim(),
    subtitle: clean(form.subtitle),
    description: clean(form.description),
    isbn: clean(form.isbn),
    publisher: clean(form.publisher),
    coverUrl: clean(form.coverUrl),
    category: clean(form.category),
    authors: splitList(authorsText.value),
    tags: splitList(tagsText.value),
  })
}

function splitList(value: string) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function clean(value: string | null) {
  return value && value.trim().length ? value.trim() : null
}
</script>

<style scoped>
.form-panel {
  padding: 1.25rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.form-span-2 {
  grid-column: span 2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-span-2 {
    grid-column: span 1;
  }
}
</style>
