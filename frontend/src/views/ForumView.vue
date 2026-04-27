<template>
  <div class="page-shell forum-page">
    <AppSectionHeader
      :title="categoryTitle"
      eyebrow="Structured Forum"
      description="Discuss books, source-backed notes, concepts, lenses, prototype tasks, and project applications."
      :level="1"
    >
      <template #actions>
        <RouterLink :to="{ name: 'forum-new', query: newThreadQuery }" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">New Structured Thread</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppErrorState v-if="errorMessage" title="Forum could not load" :description="errorMessage" retry-label="Retry" @retry="loadForum" />

    <template v-else>
      <section class="forum-grid" aria-label="Forum categories and threads">
        <aside class="forum-sidebar">
          <AppCard class="forum-panel" as="section">
            <AppSectionHeader title="Categories" eyebrow="Discussion spaces" :level="2" compact />
            <AppLoadingState v-if="loading && !categories.length" label="Loading categories" />
            <nav v-else class="category-list" aria-label="Forum categories">
              <RouterLink
                class="category-link"
                :class="{ 'category-link--active': !activeSlug }"
                :to="{ name: 'forum' }"
              >
                <strong>All Discussions</strong>
                <AppBadge variant="neutral" size="sm">{{ totalThreads }}</AppBadge>
              </RouterLink>
              <RouterLink
                v-for="category in categories"
                :key="category.id"
                class="category-link"
                :class="{ 'category-link--active': activeSlug === category.slug }"
                :to="{ name: 'forum-category', params: { slug: category.slug } }"
              >
                <span>
                  <strong>{{ category.name }}</strong>
                  <small>{{ category.description }}</small>
                </span>
                <AppBadge variant="primary" size="sm">{{ category.threadCount }}</AppBadge>
              </RouterLink>
            </nav>
          </AppCard>
        </aside>

        <main class="forum-main">
          <AppCard class="forum-panel" as="section">
            <div class="forum-toolbar">
              <label class="forum-search">
                <span>Search discussions</span>
                <el-input v-model="searchText" clearable placeholder="Search thread title or body..." @keyup.enter="loadForum" />
              </label>
              <AppButton variant="secondary" @click="loadForum">Search</AppButton>
            </div>

            <AppLoadingState v-if="loading && !threads.length" label="Loading forum threads" />
            <AppEmptyState
              v-else-if="!threads.length"
              title="No threads yet"
              description="Start a structured discussion from a book, quote, concept, source reference, or this forum page."
              compact
            />
            <div v-else class="thread-list">
              <RouterLink
                v-for="thread in threads"
                :key="thread.id"
                class="thread-card"
                :to="{ name: 'forum-thread', params: { id: thread.id } }"
              >
                <div class="thread-card__topline">
                  <AppBadge variant="info" size="sm">{{ thread.categoryName }}</AppBadge>
                  <AppBadge :variant="thread.visibility === 'PRIVATE' ? 'warning' : 'success'" size="sm">{{ thread.visibility }}</AppBadge>
                  <AppBadge v-if="thread.relatedEntityType" variant="accent" size="sm">{{ thread.relatedEntityType }}</AppBadge>
                </div>
                <h2>{{ thread.title }}</h2>
                <p>{{ preview(thread.bodyMarkdown) }}</p>
                <div class="thread-card__meta">
                  <span>By {{ thread.authorDisplayName }}</span>
                  <span>{{ thread.commentCount }} comments</span>
                  <span>{{ thread.likeCount }} likes</span>
                  <span>{{ formatDate(thread.updatedAt) }}</span>
                </div>
              </RouterLink>
            </div>
          </AppCard>
        </main>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getForumCategories, getForumThreads } from '../api/forum'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type { ForumCategoryRecord, ForumThreadRecord } from '../types'

const route = useRoute()
const categories = ref<ForumCategoryRecord[]>([])
const threads = ref<ForumThreadRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')
const searchText = ref('')

const activeSlug = computed(() => (typeof route.params.slug === 'string' ? route.params.slug : ''))
const activeCategory = computed(() => categories.value.find((category) => category.slug === activeSlug.value))
const categoryTitle = computed(() => activeCategory.value?.name ?? 'Forum')
const totalThreads = computed(() => categories.value.reduce((sum, category) => sum + category.threadCount, 0))
const newThreadQuery = computed(() => (activeCategory.value ? { categoryId: String(activeCategory.value.id) } : {}))

onMounted(loadForum)
watch(() => route.params.slug, loadForum)

async function loadForum() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [categoryResult, threadResult] = await Promise.all([
      getForumCategories(),
      getForumThreads({ categorySlug: activeSlug.value || undefined, q: searchText.value || undefined }),
    ])
    categories.value = categoryResult
    threads.value = threadResult
  } catch {
    errorMessage.value = 'Check backend availability and try loading the structured forum again.'
  } finally {
    loading.value = false
  }
}

function preview(markdown: string) {
  return markdown.replace(/[#>*`_-]/g, '').replace(/\s+/g, ' ').trim().slice(0, 220)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
</script>

<style scoped>
.forum-page,
.forum-grid,
.thread-list {
  display: grid;
  gap: var(--space-5);
}

.forum-grid {
  grid-template-columns: minmax(260px, 0.34fr) minmax(0, 1fr);
}

.forum-panel {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.forum-sidebar {
  align-self: start;
  position: sticky;
  top: calc(var(--space-4) + 72px);
}

.category-list {
  display: grid;
  gap: var(--space-2);
}

.category-link,
.thread-card {
  color: inherit;
  text-decoration: none;
}

.category-link {
  min-height: 48px;
  padding: var(--space-3);
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.category-link--active {
  border-color: color-mix(in srgb, var(--bookos-primary) 32%, var(--bookos-border));
  background: var(--bookos-primary-soft);
}

.category-link small {
  display: block;
  margin-top: var(--space-1);
  color: var(--bookos-text-secondary);
  line-height: 1.35;
}

.forum-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: var(--space-3);
}

.forum-search {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.thread-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  box-shadow: var(--shadow-card);
}

.thread-card:hover {
  border-color: color-mix(in srgb, var(--bookos-primary) 24%, var(--bookos-border));
}

.thread-card__topline,
.thread-card__meta {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.thread-card h2,
.thread-card p {
  margin: 0;
}

.thread-card h2 {
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.35rem;
}

.thread-card p,
.thread-card__meta {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

@media (max-width: 980px) {
  .forum-grid,
  .forum-toolbar {
    grid-template-columns: 1fr;
  }

  .forum-sidebar {
    position: static;
  }
}
</style>
