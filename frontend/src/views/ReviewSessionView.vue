<template>
  <div class="page-shell review-detail-page">
    <AppLoadingState v-if="loading" label="Loading review session" />
    <AppErrorState v-else-if="errorMessage" title="Review session could not load" :description="errorMessage" retry-label="Retry" @retry="loadSession" />

    <template v-else-if="session">
      <AppSectionHeader
        :title="session.title"
        eyebrow="Review session"
        :description="`${session.scopeType}${session.scopeId ? ` #${session.scopeId}` : ''} / ${session.completedItemCount} of ${session.itemCount} items complete`"
        :level="1"
      >
        <template #actions>
          <RouterLink :to="{ name: 'review' }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Reviews</AppButton>
          </RouterLink>
        </template>
      </AppSectionHeader>

      <section class="review-items">
        <AppEmptyState v-if="!session.items.length" title="No review items" description="Add review items from a source, or generate from a book, concept, or project." compact />
        <AppCard v-for="item in session.items" v-else :key="item.id" class="review-item" as="article">
          <div class="review-item__topline">
            <AppBadge variant="primary">{{ item.targetType }}</AppBadge>
            <AppBadge :variant="item.status === 'COMPLETED' ? 'success' : 'warning'">{{ item.status }}</AppBadge>
            <AppBadge v-if="item.sourceReference" variant="info">Source-backed</AppBadge>
          </div>

          <h2>{{ item.prompt }}</h2>
          <label class="review-field">
            <span>Response</span>
            <el-input v-model="responses[item.id]" type="textarea" :rows="4" placeholder="Write what you remember or how you will apply it." />
          </label>

          <div class="review-item__controls">
            <label class="review-field review-field--inline">
              <span>Confidence</span>
              <el-rate v-model="confidence[item.id]" :max="5" aria-label="Confidence score" />
            </label>
            <AppButton v-if="item.sourceReference" variant="secondary" @click="openItemSource(item)">Open Source</AppButton>
            <AppButton variant="primary" :loading="savingId === item.id" @click="completeItem(item)">Save Review</AppButton>
          </div>
        </AppCard>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute } from 'vue-router'
import { getReviewSession, updateReviewItem } from '../api/learning'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type { ReviewItemRecord, ReviewSessionRecord } from '../types'

const route = useRoute()
const { openSource } = useOpenSource()
const session = ref<ReviewSessionRecord | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const savingId = ref<number | null>(null)
const responses = reactive<Record<number, string>>({})
const confidence = reactive<Record<number, number>>({})

onMounted(loadSession)

async function loadSession() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getReviewSession(String(route.params.id))
    session.value = result
    result.items.forEach((item) => {
      responses[item.id] = item.userResponse ?? responses[item.id] ?? ''
      confidence[item.id] = item.confidenceScore ?? confidence[item.id] ?? 0
    })
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try again.'
  } finally {
    loading.value = false
  }
}

async function completeItem(item: ReviewItemRecord) {
  savingId.value = item.id
  try {
    await updateReviewItem(item.id, {
      userResponse: responses[item.id] || null,
      confidenceScore: confidence[item.id] || null,
      status: 'COMPLETED',
    })
    await loadSession()
    ElMessage.success('Review saved.')
  } catch {
    ElMessage.error('Review item could not be saved. Check permissions and backend availability.')
  } finally {
    savingId.value = null
  }
}

function openItemSource(item: ReviewItemRecord) {
  if (!item.sourceReference) return
  void openSource({
    sourceType: item.targetType,
    sourceId: item.targetId,
    bookId: item.sourceReference.bookId,
    pageStart: item.sourceReference.pageStart,
    sourceReference: item.sourceReference,
    sourceReferenceId: item.sourceReference.id,
  })
}
</script>

<style scoped>
.review-detail-page,
.review-items,
.review-item {
  display: grid;
  gap: var(--space-5);
}

.review-item {
  padding: var(--space-5);
}

.review-item__topline,
.review-item__controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.review-item h2 {
  margin: 0;
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
}

.review-field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.review-field--inline {
  display: flex;
  align-items: center;
}
</style>
