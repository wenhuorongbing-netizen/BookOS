<template>
  <section class="surface-card filter-bar">
    <label class="filter-bar__field" for="library-filter-search">
      <span class="sr-only">Search library</span>
      <el-input
        id="library-filter-search"
        v-model="local.q"
        placeholder="Search title, author, tag, or category"
        clearable
        aria-label="Search title, author, tag, or category"
      />
    </label>
    <label class="filter-bar__field">
      <span class="sr-only">Filter by category</span>
      <el-select v-model="local.category" clearable placeholder="Category" aria-label="Filter by category">
      <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
    </label>
    <label class="filter-bar__field">
      <span class="sr-only">Filter by tag</span>
      <el-select v-model="local.tag" clearable placeholder="Tag" aria-label="Filter by tag">
      <el-option v-for="item in tags" :key="item" :label="item" :value="item" />
      </el-select>
    </label>
    <div class="filter-bar__actions">
      <el-button type="primary" @click="applyFilters">Apply</el-button>
      <el-button @click="resetFilters">Reset</el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { BookFilterState } from '../types'

const props = defineProps<{
  modelValue: BookFilterState
  categories: string[]
  tags: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: BookFilterState]
  apply: []
}>()

const local = reactive<BookFilterState>({ ...props.modelValue })

watch(
  () => props.modelValue,
  (value) => Object.assign(local, value),
  { deep: true },
)

function applyFilters() {
  emit('update:modelValue', { ...local })
  emit('apply')
}

function resetFilters() {
  Object.assign(local, { q: '', category: '', tag: '' })
  emit('update:modelValue', { ...local })
  emit('apply')
}
</script>

<style scoped>
.filter-bar {
  padding: 1rem;
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr auto;
  gap: 0.75rem;
  align-items: start;
}

.filter-bar__field {
  min-width: 0;
  display: grid;
}

.filter-bar__actions {
  display: flex;
  gap: 0.5rem;
}

@media (max-width: 900px) {
  .filter-bar {
    grid-template-columns: 1fr;
  }

  .filter-bar__actions {
    flex-wrap: wrap;
  }

  .filter-bar__actions :deep(.el-button) {
    flex: 1 1 140px;
  }
}
</style>
