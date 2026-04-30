<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="min(720px, calc(100vw - 32px))"
    align-center
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="resetForm"
  >
    <form class="action-form" @submit.prevent="submitForm">
      <label class="field">
        <span>Book <b aria-hidden="true">*</b></span>
        <el-select
          v-model="form.bookId"
          filterable
          placeholder="Choose a book from your library"
          :disabled="saving"
          @change="loadSourceReferences"
        >
          <el-option v-for="book in libraryBooks" :key="book.id" :label="book.title" :value="book.id" />
        </el-select>
      </label>

      <label class="field">
        <span>Title <b aria-hidden="true">*</b></span>
        <el-input
          v-model="form.title"
          maxlength="220"
          show-word-limit
          placeholder="What should be done?"
          :disabled="saving"
        />
      </label>

      <label class="field">
        <span>Description</span>
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="10000"
          show-word-limit
          placeholder="Optional context, next step, or acceptance criteria."
          :disabled="saving"
        />
      </label>

      <div class="action-form__grid">
        <label class="field">
          <span>Priority</span>
          <el-select v-model="form.priority" :disabled="saving">
            <el-option v-for="item in priorityOptions" :key="item" :label="priorityLabel(item)" :value="item" />
          </el-select>
        </label>

        <label class="field">
          <span>Visibility</span>
          <el-select v-model="form.visibility" :disabled="saving">
            <el-option v-for="item in visibilityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </label>
      </div>

      <label class="field">
        <span>Source link</span>
        <el-select
          v-model="form.sourceReferenceId"
          clearable
          filterable
          placeholder="Optional existing note/capture source"
          :disabled="saving || !form.bookId"
        >
          <el-option
            v-for="source in sourceReferences"
            :key="source.id"
            :label="sourceLabel(source)"
            :value="source.id"
          />
        </el-select>
      </label>

      <div class="action-form__grid">
        <label class="field">
          <span>Page start</span>
          <el-input-number v-model="form.pageStart" :min="1" :disabled="saving || Boolean(form.sourceReferenceId)" />
        </label>

        <label class="field">
          <span>Page end</span>
          <el-input-number v-model="form.pageEnd" :min="1" :disabled="saving || Boolean(form.sourceReferenceId)" />
        </label>
      </div>

      <p v-if="form.sourceReferenceId" class="helper-text">
        Page values are inherited from the selected source link. Manual page fields are disabled to avoid conflicting source data.
      </p>

      <p class="helper-text">
        Unknown pages are stored as empty values. Do not enter a page unless the source actually provides one.
      </p>

      <div class="action-form__actions">
        <AppButton variant="ghost" :disabled="saving" @click="$emit('update:modelValue', false)">Cancel</AppButton>
        <AppButton variant="primary" native-type="submit" :loading="saving">{{ submitLabel }}</AppButton>
      </div>
    </form>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getBookSourceReferences } from '../../api/sourceReferences'
import type { ActionItemPayload, ActionItemRecord, ActionPriority, BookRecord, SourceReferenceRecord, Visibility } from '../../types'
import { visibilityOptions } from '../../types'
import AppButton from '../ui/AppButton.vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    books: BookRecord[]
    actionItem?: ActionItemRecord | null
    saving?: boolean
    title?: string
  }>(),
  {
    actionItem: null,
    saving: false,
    title: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: ActionItemPayload]
}>()

const priorityOptions: ActionPriority[] = ['LOW', 'MEDIUM', 'HIGH']

const form = reactive({
  bookId: null as number | null,
  title: '',
  description: '',
  priority: 'MEDIUM' as ActionPriority,
  sourceReferenceId: null as number | null,
  pageStart: null as number | null,
  pageEnd: null as number | null,
  visibility: 'PRIVATE' as Visibility,
})

const sourceReferences = ref<SourceReferenceRecord[]>([])

const dialogTitle = computed(() => props.title || (props.actionItem ? 'Edit Action' : 'Create Action'))
const submitLabel = computed(() => (props.actionItem ? 'Save Action' : 'Create Action'))
const libraryBooks = computed(() => props.books.filter((book) => book.inLibrary || book.id === props.actionItem?.bookId))

watch(
  () => props.modelValue,
  (open) => {
    if (open) resetForm()
  },
)

watch(
  () => form.bookId,
  (bookId) => {
    if (bookId) void loadSourceReferences(bookId)
  },
)

function resetForm() {
  const item = props.actionItem
  form.bookId = item?.bookId ?? libraryBooks.value[0]?.id ?? null
  form.title = item?.title ?? ''
  form.description = item?.description ?? ''
  form.priority = item?.priority ?? 'MEDIUM'
  form.sourceReferenceId = item?.sourceReference?.id ?? null
  form.pageStart = item?.sourceReference ? null : item?.pageStart ?? null
  form.pageEnd = item?.sourceReference ? null : item?.pageEnd ?? null
  form.visibility = item?.visibility ?? 'PRIVATE'
  if (form.bookId) void loadSourceReferences(form.bookId)
}

async function loadSourceReferences(bookId: number | string) {
  sourceReferences.value = []
  try {
    sourceReferences.value = await getBookSourceReferences(bookId)
  } catch {
    ElMessage.warning('Source links for this book could not be loaded.')
  }
}

function submitForm() {
  if (!form.bookId) {
    ElMessage.warning('Choose a book before saving the action.')
    return
  }

  if (!form.title.trim()) {
    ElMessage.warning('Action item title is required.')
    return
  }

  if (form.pageStart && form.pageEnd && form.pageEnd < form.pageStart) {
    ElMessage.warning('Page end must be greater than or equal to page start.')
    return
  }

  emit('submit', {
    bookId: form.bookId,
    title: form.title.trim(),
    description: form.description.trim() || null,
    priority: form.priority,
    sourceReferenceId: form.sourceReferenceId,
    pageStart: form.sourceReferenceId ? null : form.pageStart,
    pageEnd: form.sourceReferenceId ? null : form.pageEnd,
    visibility: form.visibility,
  })
}

function sourceLabel(source: SourceReferenceRecord) {
  const page = source.pageStart ? `p.${source.pageStart}${source.pageEnd ? `-${source.pageEnd}` : ''}` : 'No page'
  return `${source.locationLabel ?? source.sourceType} / ${page}`
}

function priorityLabel(priority: ActionPriority) {
  if (priority === 'HIGH') return 'High'
  if (priority === 'LOW') return 'Low'
  return 'Medium'
}
</script>

<style scoped>
.action-form {
  display: grid;
  gap: var(--space-4);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.field b {
  color: var(--bookos-danger);
}

.action-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.helper-text {
  margin: 0;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  line-height: 1.45;
}

.action-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 640px) {
  .action-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
