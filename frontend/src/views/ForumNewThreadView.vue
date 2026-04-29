<template>
  <div class="page-shell forum-new-page">
    <AppSectionHeader
      title="New Structured Thread"
      eyebrow="Forum"
      description="Start with a template, attach source context, and keep the discussion tied to reading and design work."
      :level="1"
    >
      <template #actions>
        <RouterLink to="/forum" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">Back to Forum</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppErrorState v-if="errorMessage" title="Thread form could not load" :description="errorMessage" retry-label="Retry" @retry="loadFormData" />

    <AppCard v-else class="thread-form-card" as="section">
      <form class="thread-form" @submit.prevent="submitThread">
        <section v-if="sourceContextSummary.length" class="context-card" aria-label="Prefilled source context">
          <div>
            <strong>Source context attached</strong>
            <p>This thread will carry the selected entity/book/source IDs. The backend will hide private context from unauthorized users.</p>
          </div>
          <AppBadge v-for="item in sourceContextSummary" :key="item" variant="primary" size="sm">{{ item }}</AppBadge>
        </section>

        <div class="form-grid">
        <label class="form-field">
          <span>Template</span>
          <el-select v-model="selectedTemplateSlug" clearable placeholder="Choose a structured template" @change="applyTemplate">
            <el-option v-for="template in templates" :key="template.slug" :label="template.name" :value="template.slug" />
          </el-select>
        </label>

        <label class="form-field">
          <span>Category</span>
          <el-select v-model="form.categoryId" placeholder="Choose category">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </label>

        <label class="form-field form-field--wide">
          <span>Title</span>
          <el-input v-model="form.title" maxlength="220" show-word-limit placeholder="What should the forum discuss?" />
        </label>

        <label class="form-field">
          <span>Related entity type</span>
          <el-select v-model="form.relatedEntityType" clearable filterable allow-create placeholder="BOOK, QUOTE, CONCEPT...">
            <el-option v-for="type in relatedEntityTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </label>

        <label class="form-field">
          <span>Related entity ID</span>
          <el-input-number v-model="form.relatedEntityId" :min="1" controls-position="right" placeholder="Optional" />
        </label>

        <label class="form-field">
          <span>Related book ID</span>
          <el-input-number v-model="form.relatedBookId" :min="1" controls-position="right" placeholder="Optional" />
        </label>

        <label class="form-field">
          <span>Related concept ID</span>
          <el-input-number v-model="form.relatedConceptId" :min="1" controls-position="right" placeholder="Optional" />
        </label>

        <label class="form-field">
          <span>Source reference ID</span>
          <el-input-number v-model="form.sourceReferenceId" :min="1" controls-position="right" placeholder="Optional" />
        </label>

        <label class="form-field">
          <span>Visibility</span>
          <el-select v-model="form.visibility">
            <el-option label="Shared" value="SHARED" />
            <el-option label="Private" value="PRIVATE" />
            <el-option label="Public" value="PUBLIC" />
          </el-select>
        </label>

        <label class="form-field form-field--wide">
          <span>Body Markdown</span>
          <el-input
            v-model="form.bodyMarkdown"
            type="textarea"
            :rows="14"
            maxlength="20000"
            show-word-limit
            placeholder="Use the structured template to frame the discussion."
          />
        </label>
        </div>

        <div class="form-actions">
          <AppButton variant="primary" native-type="submit" :loading="saving">Create Thread</AppButton>
          <AppButton variant="ghost" @click="resetFromQuery">Reset Context</AppButton>
        </div>
      </form>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { createForumThread, getForumCategories, getForumTemplates } from '../api/forum'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type { ForumCategoryRecord, ForumThreadPayload, StructuredPostTemplateRecord, Visibility } from '../types'

const route = useRoute()
const router = useRouter()
const categories = ref<ForumCategoryRecord[]>([])
const templates = ref<StructuredPostTemplateRecord[]>([])
const selectedTemplateSlug = ref('')
const saving = ref(false)
const errorMessage = ref('')

const relatedEntityTypes = ['BOOK', 'NOTE', 'QUOTE', 'CONCEPT', 'KNOWLEDGE_OBJECT', 'SOURCE_REFERENCE', 'DESIGN_LENS', 'EXERCISE', 'PROTOTYPE_TASK', 'ACTION_ITEM', 'GAME_PROJECT', 'GENERAL']
const form = reactive<ForumThreadPayload>({
  categoryId: 0,
  title: '',
  bodyMarkdown: '',
  relatedEntityType: null,
  relatedEntityId: null,
  relatedBookId: null,
  relatedConceptId: null,
  sourceReferenceId: null,
  visibility: 'SHARED',
})
const sourceContextSummary = computed(() => {
  const items: string[] = []
  if (form.relatedEntityType && form.relatedEntityId) items.push(`${form.relatedEntityType} #${form.relatedEntityId}`)
  if (form.relatedBookId) items.push(`Book #${form.relatedBookId}`)
  if (form.relatedConceptId) items.push(`Concept #${form.relatedConceptId}`)
  if (form.sourceReferenceId) items.push(`Source #${form.sourceReferenceId}`)
  return items
})

onMounted(loadFormData)

async function loadFormData() {
  errorMessage.value = ''
  try {
    const [categoryResult, templateResult] = await Promise.all([getForumCategories(), getForumTemplates()])
    categories.value = categoryResult
    templates.value = templateResult
    resetFromQuery()
  } catch {
    errorMessage.value = 'Check backend availability and try opening the thread form again.'
  }
}

function resetFromQuery() {
  form.categoryId = numberQuery('categoryId') ?? chooseCategoryFromType(stringQuery('relatedEntityType'))?.id ?? categories.value[0]?.id ?? 0
  form.title = stringQuery('title') ?? ''
  form.bodyMarkdown = ''
  form.relatedEntityType = stringQuery('relatedEntityType')
  form.relatedEntityId = numberQuery('relatedEntityId')
  form.relatedBookId = numberQuery('bookId')
  form.relatedConceptId = numberQuery('conceptId')
  form.sourceReferenceId = numberQuery('sourceReferenceId')
  form.visibility = 'SHARED'
  selectedTemplateSlug.value = ''
  const template = chooseTemplateFromType(form.relatedEntityType)
  if (template) {
    selectedTemplateSlug.value = template.slug
    form.bodyMarkdown = template.bodyMarkdownTemplate
  }
}

function applyTemplate() {
  const template = templates.value.find((item) => item.slug === selectedTemplateSlug.value)
  if (!template) return
  if (!form.relatedEntityType && template.defaultRelatedEntityType) {
    form.relatedEntityType = template.defaultRelatedEntityType
  }
  form.bodyMarkdown = template.bodyMarkdownTemplate
  const category = chooseCategoryFromType(template.defaultRelatedEntityType)
  if (category) form.categoryId = category.id
}

async function submitThread() {
  if (!form.categoryId || !form.title.trim() || !form.bodyMarkdown.trim()) {
    ElMessage.warning('Category, title, and body are required.')
    return
  }

  saving.value = true
  try {
    const created = await createForumThread({
      ...form,
      title: form.title.trim(),
      bodyMarkdown: form.bodyMarkdown.trim(),
      visibility: (form.visibility ?? 'SHARED') as Visibility,
    })
    ElMessage.success('Forum thread created.')
    await router.push({ name: 'forum-thread', params: { id: created.id } })
  } catch {
    ElMessage.error('Thread creation failed. Check related IDs and source ownership.')
  } finally {
    saving.value = false
  }
}

function chooseTemplateFromType(type: string | null | undefined) {
  const normalized = type?.toUpperCase()
  if (normalized === 'QUOTE') return templates.value.find((item) => item.slug === 'quote-discussion')
  if (normalized === 'CONCEPT') return templates.value.find((item) => item.slug === 'concept-discussion')
  if (normalized === 'DESIGN_LENS') return templates.value.find((item) => item.slug === 'design-lens-review')
  if (normalized === 'PROTOTYPE_TASK' || normalized === 'ACTION_ITEM') return templates.value.find((item) => item.slug === 'prototype-challenge')
  if (normalized === 'GAME_PROJECT') return templates.value.find((item) => item.slug === 'project-critique')
  if (normalized === 'BOOK') return templates.value.find((item) => item.slug === 'book-discussion')
  return templates.value.find((item) => item.slug === 'general') ?? templates.value.find((item) => item.slug === 'book-discussion')
}

function chooseCategoryFromType(type: string | null | undefined) {
  const normalized = type?.toUpperCase()
  if (normalized === 'CONCEPT') return findCategory('concept-discussions')
  if (normalized === 'DESIGN_LENS') return findCategory('design-lens-reviews')
  if (normalized === 'PROTOTYPE_TASK' || normalized === 'ACTION_ITEM') return findCategory('prototype-challenges')
  if (normalized === 'GAME_PROJECT') return findCategory('project-critiques')
  if (normalized === 'BOOK' || normalized === 'QUOTE' || normalized === 'NOTE') return findCategory('book-discussions')
  return findCategory('general')
}

function findCategory(slug: string) {
  return categories.value.find((category) => category.slug === slug)
}

function stringQuery(key: string) {
  const value = route.query[key]
  return typeof value === 'string' && value.trim() ? value.trim() : null
}

function numberQuery(key: string) {
  const value = stringQuery(key)
  if (!value) return null
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : null
}
</script>

<style scoped>
.forum-new-page {
  display: grid;
  gap: var(--space-5);
}

.thread-form-card,
.thread-form {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-5);
}

.thread-form-card {
  padding: 0;
}

.context-card {
  padding: var(--space-4);
  display: flex;
  gap: var(--space-2);
  align-items: center;
  flex-wrap: wrap;
  border: 1px solid color-mix(in srgb, var(--bookos-primary) 24%, var(--bookos-border));
  border-radius: var(--radius-lg);
  background: var(--bookos-primary-soft);
}

.context-card p {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.form-field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.form-field--wide {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

@media (max-width: 780px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
