<template>
  <el-dialog
    :model-value="modelValue"
    title="Review Parsed Concepts"
    width="min(920px, calc(100vw - 32px))"
    align-center
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="resetRows"
  >
    <form class="concept-review" @submit.prevent="submitReview">
      <p class="concept-review__intro">
        Review each parsed <code>[[Concept]]</code> before it becomes a real concept. Skipped concepts are not saved.
      </p>

      <AppEmptyState
        v-if="!rows.length"
        title="No parsed concepts"
        description="This source does not contain any parsed [[Concept]] markers."
        compact
      />

      <div v-else class="concept-review__rows">
        <article v-for="row in rows" :key="row.key" class="concept-row">
          <div class="concept-row__header">
            <div>
              <div class="eyebrow">Parsed concept</div>
              <strong>[[{{ row.rawName }}]]</strong>
            </div>
            <el-select v-model="row.action" aria-label="Concept review action">
              <el-option label="Accept existing" value="ACCEPT" />
              <el-option label="Create new" value="CREATE" />
              <el-option label="Skip" value="SKIP" />
            </el-select>
          </div>

          <div v-if="row.action !== 'SKIP'" class="concept-row__grid">
            <label class="field">
              <span>Existing concept</span>
              <el-select
                v-model="row.existingConceptId"
                clearable
                filterable
                placeholder="Choose existing concept"
                :disabled="row.action === 'CREATE'"
              >
                <el-option v-for="concept in concepts" :key="concept.id" :label="concept.name" :value="concept.id" />
              </el-select>
            </label>

            <label class="field">
              <span>Final name</span>
              <el-input
                v-model="row.finalName"
                maxlength="180"
                show-word-limit
                placeholder="Rename before saving"
                :disabled="row.action === 'ACCEPT' && Boolean(row.existingConceptId)"
              />
            </label>
          </div>

          <label v-if="row.action !== 'SKIP'" class="field">
            <span>Tags</span>
            <el-input v-model="row.tagsInput" placeholder="systems, loop, prototype" />
          </label>

          <div class="concept-row__source">
            <AppBadge v-if="sourceReference" :variant="sourceReference.sourceConfidence === 'HIGH' ? 'success' : 'neutral'" size="sm">
              Source {{ sourceReference.sourceConfidence }}
            </AppBadge>
            <span>{{ sourceLabel }}</span>
          </div>
        </article>
      </div>

      <div class="concept-review__actions">
        <AppButton variant="ghost" :disabled="saving" @click="$emit('update:modelValue', false)">Cancel</AppButton>
        <AppButton variant="primary" native-type="submit" :loading="saving" :disabled="!rows.length">Save Reviewed Concepts</AppButton>
      </div>
    </form>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { ConceptRecord, ConceptReviewAction, ConceptReviewPayload, SourceReferenceRecord } from '../../types'
import AppBadge from '../ui/AppBadge.vue'
import AppButton from '../ui/AppButton.vue'
import AppEmptyState from '../ui/AppEmptyState.vue'

interface ConceptReviewRow {
  key: string
  rawName: string
  finalName: string
  action: ConceptReviewAction
  existingConceptId: number | null
  tagsInput: string
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    parsedConcepts: string[]
    concepts: ConceptRecord[]
    sourceReference?: SourceReferenceRecord | null
    defaultTags?: string[]
    saving?: boolean
  }>(),
  {
    sourceReference: null,
    defaultTags: () => [],
    saving: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: ConceptReviewPayload]
}>()

const rows = reactive<ConceptReviewRow[]>([])

const sourceLabel = computed(() => {
  const source = props.sourceReference
  if (!source) return 'No source reference selected'
  const page = source.pageStart ? `p.${source.pageStart}${source.pageEnd ? `-${source.pageEnd}` : ''}` : 'No page'
  return `${source.locationLabel ?? source.sourceType} / ${page}`
})

watch(
  () => props.modelValue,
  (open) => {
    if (open) resetRows()
  },
)

watch(
  () => props.parsedConcepts,
  () => {
    if (props.modelValue) resetRows()
  },
)

function resetRows() {
  rows.splice(0, rows.length, ...dedupe(props.parsedConcepts).map(toRow))
}

function toRow(name: string, index: number): ConceptReviewRow {
  const existing = findExistingConcept(name)
  return {
    key: `${name}-${index}`,
    rawName: name,
    finalName: name,
    action: existing ? 'ACCEPT' : 'CREATE',
    existingConceptId: existing?.id ?? null,
    tagsInput: props.defaultTags.join(', '),
  }
}

function submitReview() {
  const concepts = rows.map((row) => {
    if (row.action === 'SKIP') {
      return {
        rawName: row.rawName,
        finalName: row.finalName.trim() || row.rawName,
        action: row.action,
        existingConceptId: null,
        tags: [],
      }
    }

    const finalName = row.existingConceptId ? existingConceptName(row.existingConceptId) ?? row.finalName : row.finalName
    if (!finalName.trim()) {
      ElMessage.warning('Every accepted or created concept needs a final name.')
      return null
    }

    return {
      rawName: row.rawName,
      finalName: finalName.trim(),
      action: row.action,
      existingConceptId: row.action === 'ACCEPT' ? row.existingConceptId : null,
      tags: parseList(row.tagsInput),
    }
  })

  if (concepts.some((item) => item === null)) return
  emit('submit', { concepts: concepts.filter((item) => item !== null) })
}

function findExistingConcept(name: string) {
  const normalized = normalize(name)
  return props.concepts.find((concept) => normalize(concept.name) === normalized) ?? null
}

function existingConceptName(id: number) {
  return props.concepts.find((concept) => concept.id === id)?.name ?? null
}

function dedupe(values: string[]) {
  const seen = new Set<string>()
  return values
    .map((value) => value.trim())
    .filter(Boolean)
    .filter((value) => {
      const key = normalize(value)
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
}

function normalize(value: string) {
  return value.trim().toLowerCase()
}

function parseList(value: string) {
  return value
    .split(',')
    .map((item) => item.trim().replace(/^#/, '').toLowerCase())
    .filter(Boolean)
    .filter((item, index, all) => all.indexOf(item) === index)
}
</script>

<style scoped>
.concept-review,
.concept-review__rows,
.concept-row {
  display: grid;
  gap: var(--space-4);
}

.concept-review__intro {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.concept-review__intro code {
  color: var(--bookos-primary-hover);
  font-weight: 800;
}

.concept-row {
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface-muted);
}

.concept-row__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.concept-row__header strong {
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.22rem;
}

.concept-row__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.concept-row__source {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.concept-review__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 720px) {
  .concept-row__header,
  .concept-row__grid {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}
</style>
