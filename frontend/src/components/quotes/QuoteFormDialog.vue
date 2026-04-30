<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="min(720px, calc(100vw - 32px))"
    align-center
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="resetForm"
  >
    <form class="quote-form" @submit.prevent="submitForm">
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
        <span>Quote text <b aria-hidden="true">*</b></span>
        <el-input
          v-model="form.text"
          type="textarea"
          :rows="6"
          maxlength="10000"
          show-word-limit
          placeholder="Paste or write the quote exactly as you want it stored."
          :disabled="saving"
        />
      </label>

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

      <div class="quote-form__grid">
        <label class="field">
          <span>Attribution / author</span>
          <el-input v-model="form.attribution" maxlength="220" show-word-limit placeholder="Optional" :disabled="saving" />
        </label>

        <label class="field">
          <span>Visibility</span>
          <el-select v-model="form.visibility" :disabled="saving">
            <el-option v-for="item in visibilityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </label>
      </div>

      <div class="quote-form__grid">
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

      <label class="field">
        <span>Tags</span>
        <el-input v-model="tagsInput" placeholder="quote, game-feel, prototype" :disabled="saving" />
      </label>

      <label class="field">
        <span>Concepts</span>
        <el-input v-model="conceptsInput" placeholder="Game Feel, Feedback Loop, Meaningful Choice" :disabled="saving" />
      </label>

      <p class="helper-text">
        Unknown pages are stored as empty values. Do not enter a page unless the source actually provides one.
      </p>

      <div class="quote-form__actions">
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
import type { BookRecord, QuotePayload, QuoteRecord, SourceReferenceRecord, Visibility } from '../../types'
import { visibilityOptions } from '../../types'
import AppButton from '../ui/AppButton.vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    books: BookRecord[]
    quote?: QuoteRecord | null
    saving?: boolean
    title?: string
  }>(),
  {
    quote: null,
    saving: false,
    title: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: QuotePayload]
}>()

const form = reactive({
  bookId: null as number | null,
  text: '',
  attribution: '',
  sourceReferenceId: null as number | null,
  pageStart: null as number | null,
  pageEnd: null as number | null,
  visibility: 'PRIVATE' as Visibility,
})

const tagsInput = ref('')
const conceptsInput = ref('')
const sourceReferences = ref<SourceReferenceRecord[]>([])

const dialogTitle = computed(() => props.title || (props.quote ? 'Edit Quote' : 'Create Quote'))
const submitLabel = computed(() => (props.quote ? 'Save Quote' : 'Create Quote'))
const libraryBooks = computed(() => props.books.filter((book) => book.inLibrary || book.id === props.quote?.bookId))

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
  const quote = props.quote
  form.bookId = quote?.bookId ?? libraryBooks.value[0]?.id ?? null
  form.text = quote?.text ?? ''
  form.attribution = quote?.attribution ?? ''
  form.sourceReferenceId = quote?.sourceReference?.id ?? null
  form.pageStart = quote?.sourceReference ? null : quote?.pageStart ?? null
  form.pageEnd = quote?.sourceReference ? null : quote?.pageEnd ?? null
  form.visibility = quote?.visibility ?? 'PRIVATE'
  tagsInput.value = quote?.tags?.join(', ') ?? ''
  conceptsInput.value = quote?.concepts?.join(', ') ?? ''
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
    ElMessage.warning('Choose a book before saving the quote.')
    return
  }

  if (!form.text.trim()) {
    ElMessage.warning('Quote text is required.')
    return
  }

  if (form.pageStart && form.pageEnd && form.pageEnd < form.pageStart) {
    ElMessage.warning('Page end must be greater than or equal to page start.')
    return
  }

  emit('submit', {
    bookId: form.bookId,
    text: form.text.trim(),
    attribution: form.attribution.trim() || null,
    sourceReferenceId: form.sourceReferenceId,
    pageStart: form.sourceReferenceId ? null : form.pageStart,
    pageEnd: form.sourceReferenceId ? null : form.pageEnd,
    tags: parseList(tagsInput.value),
    concepts: parseList(conceptsInput.value),
    visibility: form.visibility,
  })
}

function sourceLabel(source: SourceReferenceRecord) {
  const page = source.pageStart ? `p.${source.pageStart}${source.pageEnd ? `-${source.pageEnd}` : ''}` : 'No page'
  return `${source.locationLabel ?? source.sourceType} / ${page}`
}

function parseList(value: string) {
  return value
    .split(',')
    .map((item) => item.trim().replace(/^#/, ''))
    .filter(Boolean)
    .filter((item, index, all) => all.indexOf(item) === index)
}
</script>

<style scoped>
.quote-form {
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

.quote-form__grid {
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

.quote-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 640px) {
  .quote-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
