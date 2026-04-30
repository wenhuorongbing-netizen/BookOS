<template>
  <el-drawer
    :model-value="sourceDrawerOpen"
    title="Source Reference"
    size="min(420px, 100vw)"
    direction="rtl"
    @update:model-value="handleVisibilityChange"
  >
    <div class="source-drawer">
      <AppLoadingState v-if="sourceDrawerLoading" label="Loading source reference" />

      <AppErrorState
        v-else-if="sourceDrawerError && !sourceDrawerSource"
        title="Source unavailable"
        :description="sourceDrawerError"
      />

      <template v-else>
        <div class="source-drawer__topline">
          <AppBadge variant="primary">{{ sourceDrawerSource?.sourceType ?? relatedEntityType ?? 'SOURCE' }}</AppBadge>
          <AppBadge v-if="confidence" :variant="confidenceVariant(confidence)">
            {{ confidence }}
          </AppBadge>
        </div>

        <dl class="source-drawer__meta" aria-label="Source reference metadata">
          <div>
            <dt>Book</dt>
            <dd>{{ bookLabel }}</dd>
          </div>
          <div>
            <dt>Chapter</dt>
            <dd>{{ sourceDrawerSource?.chapterId ? `Chapter #${sourceDrawerSource.chapterId}` : 'Not tracked' }}</dd>
          </div>
          <div>
            <dt>Page start</dt>
            <dd>{{ sourceDrawerSource?.pageStart ?? sourceDrawerTarget?.pageStart ?? 'Page unknown' }}</dd>
          </div>
          <div>
            <dt>Page end</dt>
            <dd>{{ sourceDrawerSource?.pageEnd ?? sourceDrawerSource?.pageStart ?? sourceDrawerTarget?.pageStart ?? 'Page unknown' }}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>{{ sourceDrawerSource?.locationLabel ?? sourceDrawerTarget?.locationLabel ?? 'Not tracked' }}</dd>
          </div>
          <div>
            <dt>Related entity</dt>
            <dd>{{ relatedEntityLabel }}</dd>
          </div>
          <div>
            <dt>Created</dt>
            <dd>{{ formatDate(sourceDrawerSource?.createdAt) }}</dd>
          </div>
        </dl>

        <section class="source-drawer__excerpt" aria-labelledby="source-drawer-excerpt-title">
          <h3 id="source-drawer-excerpt-title">Source text</h3>
          <p>{{ sourceText }}</p>
        </section>

        <AppButton variant="primary" :disabled="!sourceDrawerSource?.bookId && !sourceDrawerTarget?.bookId" @click="reopenSource">
          Open Source
        </AppButton>
        <AppButton variant="secondary" :disabled="!sourceDrawerSource?.id" @click="createForumThread">
          Discuss Source
        </AppButton>
        <AppButton variant="accent" :disabled="!sourceDrawerSource?.id" @click="applyProjectOpen = true">
          Apply to Project
        </AppButton>

        <ApplyToProjectDialog
          v-if="sourceDrawerSource"
          v-model="applyProjectOpen"
          source-type="SOURCE_REFERENCE"
          :source-id="sourceDrawerSource.id"
          :source-reference="sourceDrawerSource"
          :source-label="sourceProjectLabel"
          :default-title="sourceProjectTitle"
          :default-description="sourceDrawerSource.sourceText"
        />
      </template>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import ApplyToProjectDialog from '../project/ApplyToProjectDialog.vue'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppErrorState from '../ui/AppErrorState.vue'
import AppLoadingState from '../ui/AppLoadingState.vue'
import { useOpenSource } from '../../composables/useOpenSource'
import type { SourceConfidence } from '../../types'

const {
  sourceDrawerOpen,
  sourceDrawerLoading,
  sourceDrawerError,
  sourceDrawerSource,
  sourceDrawerTarget,
  relatedEntityType,
  relatedEntityId,
  bookLabel,
  openSource,
  closeSourceDrawer,
} = useOpenSource()
const router = useRouter()
const applyProjectOpen = ref(false)

const relatedEntityLabel = computed(() => {
  if (!relatedEntityType.value || !relatedEntityId.value) return 'Not linked'
  return `${relatedEntityType.value} #${relatedEntityId.value}`
})
const confidence = computed(() => sourceDrawerSource.value?.sourceConfidence ?? sourceDrawerTarget.value?.sourceConfidence ?? null)
const sourceText = computed(() => sourceDrawerSource.value?.sourceText ?? sourceDrawerTarget.value?.sourceText ?? 'No source text stored for this reference.')
const sourceProjectLabel = computed(() => {
  if (!sourceDrawerSource.value) return 'Source reference'
  return sourceDrawerSource.value.locationLabel ?? `Source #${sourceDrawerSource.value.id}`
})
const sourceProjectTitle = computed(() => `Apply ${sourceProjectLabel.value}`)

function handleVisibilityChange(value: boolean) {
  if (!value) closeSourceDrawer()
}

async function reopenSource() {
  if (!sourceDrawerTarget.value) return
  await openSource(sourceDrawerTarget.value)
}

async function createForumThread() {
  if (!sourceDrawerSource.value) return
  const source = sourceDrawerSource.value
  await router.push({
    name: 'forum-new',
    query: {
      relatedEntityType: 'SOURCE_REFERENCE',
      relatedEntityId: String(source.id),
      bookId: source.bookId ? String(source.bookId) : undefined,
      sourceReferenceId: String(source.id),
      title: `Discuss source ${source.locationLabel ?? `#${source.id}`}`,
    },
  })
  closeSourceDrawer()
}

function confidenceVariant(confidence: SourceConfidence) {
  if (confidence === 'HIGH') return 'success'
  if (confidence === 'MEDIUM') return 'warning'
  return 'neutral'
}

function formatDate(value: string | null | undefined) {
  if (!value) return 'Not tracked'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}
</script>

<style scoped>
.source-drawer {
  display: grid;
  gap: var(--space-4);
}

.source-drawer__topline {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.source-drawer__meta {
  margin: 0;
  display: grid;
  gap: var(--space-3);
}

.source-drawer__meta div {
  display: grid;
  gap: var(--space-1);
}

.source-drawer__meta dt,
.source-drawer__excerpt h3 {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.source-drawer__meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
  overflow-wrap: anywhere;
}

.source-drawer__excerpt {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.source-drawer__excerpt h3,
.source-drawer__excerpt p {
  margin: 0;
}

.source-drawer__excerpt p {
  color: var(--bookos-text-primary);
  line-height: var(--type-body-line);
  white-space: pre-wrap;
}
</style>
