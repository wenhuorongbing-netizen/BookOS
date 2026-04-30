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
      <DetailNextStepCard
        :title="forumNextStep.title"
        :description="forumNextStep.description"
        :primary-label="forumNextStep.primaryLabel"
        :primary-to="forumNextStep.primaryTo"
        :secondary-label="forumNextStep.secondaryLabel"
        :secondary-to="forumNextStep.secondaryTo"
        :loop="forumWorkflowLoop"
      />

      <section v-if="!activeSlug" class="forum-overview" aria-label="Forum overview">
        <AppCard class="overview-card" as="section">
          <AppSectionHeader title="Latest Threads" eyebrow="Recent activity" :level="2" compact />
          <MiniThreadList :threads="latestThreads" empty-label="No recent threads yet." />
        </AppCard>
        <AppCard class="overview-card" as="section">
          <AppSectionHeader title="Popular Threads" eyebrow="Likes and replies" :level="2" compact />
          <MiniThreadList :threads="popularThreads" empty-label="No popular threads yet." />
        </AppCard>
        <AppCard class="overview-card" as="section">
          <AppSectionHeader title="Your Bookmarks" eyebrow="Saved discussions" :level="2" compact />
          <MiniThreadList :threads="bookmarkedThreads" empty-label="No bookmarked discussions yet." />
        </AppCard>
        <AppCard class="overview-card" as="section">
          <AppSectionHeader title="Source-Linked" eyebrow="Traceable discussions" :level="2" compact />
          <MiniThreadList :threads="sourceLinkedThreads" empty-label="No source-linked discussions yet." />
        </AppCard>
      </section>

      <AppCard v-if="isModerator" class="moderation-panel" as="section">
        <AppSectionHeader title="Open Reports" eyebrow="Moderator queue" :level="2" compact />
        <AppEmptyState v-if="!openReports.length" title="No open reports" description="Reported discussions will appear here for review." compact />
        <div v-else class="report-list">
          <article v-for="report in openReports" :key="report.id" class="report-row">
            <div>
              <RouterLink :to="{ name: 'forum-thread', params: { id: report.threadId } }">
                <strong>{{ report.threadTitle }}</strong>
              </RouterLink>
              <p>{{ report.reason }} · {{ report.reporterDisplayName }}</p>
            </div>
            <AppButton variant="secondary" :loading="resolvingReportId === report.id" @click="resolveOpenReport(report.id)">
              Resolve
            </AppButton>
          </article>
        </div>
      </AppCard>

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
              <label class="forum-search">
                <span>Sort</span>
                <el-select v-model="sortMode" aria-label="Sort forum threads" @change="loadForum">
                  <el-option label="Latest" value="latest" />
                  <el-option label="Popular" value="popular" />
                  <el-option label="Unanswered" value="unanswered" />
                </el-select>
              </label>
              <label class="forum-search">
                <span>Filter</span>
                <el-select v-model="filterMode" clearable aria-label="Filter forum threads" @change="loadForum">
                  <el-option label="Bookmarked" value="bookmarked" />
                  <el-option label="Source-linked" value="source-linked" />
                  <el-option label="Unanswered" value="unanswered" />
                  <el-option label="Reported" value="reported" />
                  <el-option label="Hidden" value="hidden" />
                </el-select>
              </label>
              <AppButton variant="secondary" @click="loadForum">Search</AppButton>
            </div>

            <AppLoadingState v-if="loading && !threads.length" label="Loading forum threads" />
            <AppEmptyState
              v-else-if="!threads.length"
              title="No threads yet"
              description="Start a structured discussion from a book, quote, concept, source link, or this forum page."
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
                  <AppBadge :variant="statusVariant(thread.status)" size="sm">{{ statusLabel(thread.status) }}</AppBadge>
                  <AppBadge :variant="thread.visibility === 'PRIVATE' ? 'warning' : 'success'" size="sm">{{ thread.visibility }}</AppBadge>
                  <AppBadge v-if="thread.relatedEntityType" variant="accent" size="sm">{{ thread.relatedEntityType }}</AppBadge>
                  <AppBadge v-if="thread.sourceReferenceId" variant="primary" size="sm">Source-linked</AppBadge>
                  <AppBadge v-if="thread.sourceContextUnavailable" variant="warning" size="sm">Private context</AppBadge>
                </div>
                <h2>{{ thread.title }}</h2>
                <p>{{ preview(thread.bodyMarkdown) }}</p>
                <div class="thread-card__meta">
                  <span>By {{ thread.authorDisplayName }}</span>
                  <span>{{ thread.commentCount }} comments</span>
                  <span>{{ thread.likeCount }} likes</span>
                  <span v-if="thread.bookmarkedByCurrentUser">Bookmarked</span>
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
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute } from 'vue-router'
import { getForumCategories, getForumReports, getForumThreads, resolveForumReport } from '../api/forum'
import MiniThreadList from '../components/forum/MiniThreadList.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'
import { useAuthStore } from '../stores/auth'
import type { ForumCategoryRecord, ForumReportRecord, ForumThreadRecord } from '../types'

const route = useRoute()
const auth = useAuthStore()
const categories = ref<ForumCategoryRecord[]>([])
const threads = ref<ForumThreadRecord[]>([])
const openReports = ref<ForumReportRecord[]>([])
const loading = ref(false)
const resolvingReportId = ref<number | null>(null)
const errorMessage = ref('')
const searchText = ref('')
const sortMode = ref<'latest' | 'popular' | 'unanswered'>('latest')
const filterMode = ref<'bookmarked' | 'source-linked' | 'unanswered' | 'hidden' | 'reported' | ''>('')

const activeSlug = computed(() => (typeof route.params.slug === 'string' ? route.params.slug : ''))
const activeCategory = computed(() => categories.value.find((category) => category.slug === activeSlug.value))
const categoryTitle = computed(() => activeCategory.value?.name ?? 'Forum')
const totalThreads = computed(() => categories.value.reduce((sum, category) => sum + category.threadCount, 0))
const newThreadQuery = computed(() => (activeCategory.value ? { categoryId: String(activeCategory.value.id) } : {}))
const latestThreads = computed(() => threads.value.slice(0, 4))
const popularThreads = computed(() => [...threads.value]
  .sort((a, b) => b.likeCount + b.commentCount + b.bookmarkCount - (a.likeCount + a.commentCount + a.bookmarkCount))
  .slice(0, 4))
const bookmarkedThreads = computed(() => threads.value.filter((thread) => thread.bookmarkedByCurrentUser).slice(0, 4))
const sourceLinkedThreads = computed(() => threads.value.filter((thread) => thread.sourceReferenceId || thread.relatedEntityType).slice(0, 4))
const isModerator = computed(() => auth.user?.role === 'ADMIN' || auth.user?.role === 'MODERATOR')
const forumWorkflowLoop = ['Choose context', 'Ask focused question', 'Link source when possible']
const forumNextStep = computed(() => {
  const thread = threads.value[0]
  if (thread) {
    return {
      title: 'Read one active discussion',
      description: 'Start with the latest thread before browsing categories. Structured discussion works best around one book, concept, project, or source.',
      primaryLabel: 'Open Latest Thread',
      primaryTo: { name: 'forum-thread', params: { id: thread.id } },
      secondaryLabel: 'Start Thread',
      secondaryTo: { name: 'forum-new', query: newThreadQuery.value },
    }
  }

  return {
    title: 'Start one structured discussion',
    description: 'Create a thread with a clear template and attach source context when you have it.',
    primaryLabel: 'Start Thread',
    primaryTo: { name: 'forum-new', query: newThreadQuery.value },
    secondaryLabel: 'Browse Use Cases',
      secondaryTo: '/use-cases/source-linked-forum-discussion',
  }
})

onMounted(loadForum)
watch(() => route.params.slug, loadForum)

async function loadForum() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [categoryResult, threadResult, reportResult] = await Promise.all([
      getForumCategories(),
      getForumThreads({
        categorySlug: activeSlug.value || undefined,
        q: searchText.value || undefined,
        sort: sortMode.value,
        filter: filterMode.value || undefined,
      }),
      isModerator.value ? getForumReports({ status: 'OPEN' }) : Promise.resolve([]),
    ])
    categories.value = categoryResult
    threads.value = threadResult
    openReports.value = reportResult
  } catch {
    errorMessage.value = 'Check backend availability and try loading the structured forum again.'
  } finally {
    loading.value = false
  }
}

async function resolveOpenReport(id: number) {
  resolvingReportId.value = id
  try {
    await resolveForumReport(id)
    openReports.value = openReports.value.filter((report) => report.id !== id)
    ElMessage.success('Report resolved.')
  } catch {
    ElMessage.error('Report resolution failed.')
  } finally {
    resolvingReportId.value = null
  }
}

function preview(markdown: string) {
  return markdown.replace(/[#>*`_-]/g, '').replace(/\s+/g, ' ').trim().slice(0, 220)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}

function statusLabel(status: ForumThreadRecord['status']) {
  if (status === 'ACTIVE') return 'OPEN'
  if (status === 'CLOSED') return 'LOCKED'
  return status
}

function statusVariant(status: ForumThreadRecord['status']) {
  const canonical = statusLabel(status)
  if (canonical === 'LOCKED') return 'warning'
  if (canonical === 'HIDDEN') return 'danger'
  return 'success'
}
</script>

<style scoped>
.forum-page,
.forum-overview,
.forum-grid,
.thread-list {
  display: grid;
  gap: var(--space-5);
}

.forum-overview {
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
  grid-template-columns: minmax(0, 1fr) minmax(160px, 0.24fr) minmax(170px, 0.24fr) auto;
  align-items: end;
  gap: var(--space-3);
}

.overview-card {
  padding: var(--space-4);
  display: grid;
  align-content: start;
  gap: var(--space-3);
}

.moderation-panel {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.report-list {
  display: grid;
  gap: var(--space-3);
}

.report-row {
  padding: var(--space-3);
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: center;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.report-row p {
  margin: var(--space-1) 0 0;
  color: var(--bookos-text-secondary);
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
  .forum-overview,
  .forum-grid,
  .forum-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .forum-sidebar {
    position: static;
  }
}

@media (max-width: 680px) {
  .forum-overview,
  .forum-grid,
  .forum-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
