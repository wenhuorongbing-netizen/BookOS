<template>
  <div class="page-shell project-wizard-page">
    <AppLoadingState v-if="loading" label="Loading guided project workflow" />
    <AppErrorState
      v-else-if="errorMessage"
      title="Project wizard could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadData"
    />

    <template v-else-if="project">
      <AppSectionHeader
        :title="`Apply reading knowledge to ${project.title}`"
        eyebrow="Project Mode Wizard"
        description="A guided path from source-backed reading material to a practical design decision, playtest plan, and iteration note."
        :level="1"
      >
        <template #actions>
          <HelpTooltip topic="project-apply-wizard" placement="bottom" />
          <RouterLink :to="{ name: 'project-detail', params: { id: project.id } }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Open Project Cockpit</AppButton>
          </RouterLink>
          <AppButton variant="ghost" @click="saveDraft">Save Draft</AppButton>
          <AppButton variant="text" @click="cancelWizard">Cancel</AppButton>
        </template>
      </AppSectionHeader>

      <ProjectWorkspaceNav :project-id="project.id" />

      <AppCard class="wizard-intro" variant="highlight">
        <div>
          <p class="eyebrow">Why this matters</p>
          <h2>Turn a quote or concept into an actual design move</h2>
          <p>
            This workflow waits until confirmation before creating records. Source links are carried into each
            source-backed record, and unknown pages stay unknown.
          </p>
        </div>
        <div class="wizard-intro__badges">
          <AppBadge variant="primary">Draft-safe</AppBadge>
          <AppBadge variant="success">Source-preserving</AppBadge>
          <AppBadge variant="warning">No fake data</AppBadge>
        </div>
      </AppCard>

      <AppCard class="wizard-panel">
        <el-steps :active="activeStep" finish-status="success" align-center>
          <el-step v-for="step in steps" :key="step.key" :title="step.shortTitle" />
        </el-steps>

        <section class="wizard-step" :aria-labelledby="`wizard-step-${activeStep}`">
          <div class="wizard-step__header">
            <div>
              <p class="eyebrow">Step {{ activeStep + 1 }} of {{ steps.length }}</p>
              <h2 :id="`wizard-step-${activeStep}`">{{ steps[activeStep].title }}</h2>
              <p>{{ steps[activeStep].description }}</p>
            </div>
            <AppBadge v-if="stepSkipped(steps[activeStep].key)" variant="warning">Skipped</AppBadge>
          </div>

          <template v-if="activeStep === 0">
            <div class="source-toolbar">
              <el-input v-model="sourceQuery" clearable placeholder="Search quotes, concepts, knowledge, sources, or daily prompt" aria-label="Search wizard sources" />
              <AppBadge variant="neutral">{{ filteredSourceOptions.length }} real sources</AppBadge>
            </div>

            <AppEmptyState
              v-if="!sourceOptions.length"
              title="No source-backed material yet"
              description="Create a quote, concept, design knowledge record, source link, or daily prompt before using the guided project flow."
              compact
            />

            <div v-else class="source-grid" role="listbox" aria-label="Choose project wizard source">
              <AppCard
                v-for="option in filteredSourceOptions"
                :key="option.key"
                variant="interactive"
                :selected="selectedSourceKey === option.key"
                :aria-label="`Choose ${option.label}`"
                role="option"
                @click="selectedSourceKey = option.key"
              >
                <div class="source-card">
                  <div class="source-card__topline">
                    <AppBadge :variant="sourceVariant(option.type)" size="sm">{{ sourceTypeLabel(option.type) }}</AppBadge>
                    <AppBadge v-if="option.sourceReference" variant="success" size="sm">
                      Source {{ option.sourceReference.sourceConfidence }}
                    </AppBadge>
                    <AppBadge v-else-if="option.type === 'DAILY_DESIGN_PROMPT'" variant="warning" size="sm">
                      {{ option.dailyTemplate ? 'Template prompt' : 'Source optional' }}
                    </AppBadge>
                  </div>
                  <h3>{{ option.label }}</h3>
                  <p>{{ option.detail }}</p>
                  <p class="source-card__meta">
                    {{ option.sourceReference ? sourceLocation(option.sourceReference) : 'No source link attached.' }}
                  </p>
                </div>
              </AppCard>
            </div>
          </template>

          <template v-else-if="activeStep === 1">
            <SkipStepControl
              :skipped="wizard.skipProblem"
              label="Create Project Problem"
              @toggle="wizard.skipProblem = !wizard.skipProblem"
            />
            <el-form v-if="!wizard.skipProblem" label-position="top" class="wizard-form">
              <el-form-item label="What issue are you trying to solve?" required>
                <el-input v-model="wizard.problem.title" placeholder="Example: Players do not notice feedback timing" />
              </el-form-item>
              <el-form-item label="Why this matters">
                <el-input v-model="wizard.problem.description" type="textarea" :rows="4" placeholder="Describe the design tension in your own words." />
              </el-form-item>
              <div class="wizard-form__grid">
                <el-form-item label="Priority">
                  <el-select v-model="wizard.problem.priority">
                    <el-option label="Low" value="LOW" />
                    <el-option label="Medium" value="MEDIUM" />
                    <el-option label="High" value="HIGH" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Status">
                  <el-select v-model="wizard.problem.status">
                    <el-option label="Open" value="OPEN" />
                    <el-option label="Investigating" value="INVESTIGATING" />
                    <el-option label="Resolved" value="RESOLVED" />
                  </el-select>
                </el-form-item>
              </div>
            </el-form>
          </template>

          <template v-else-if="activeStep === 2">
            <SkipStepControl
              :skipped="wizard.skipApplication"
              label="Create Project Application"
              @toggle="wizard.skipApplication = !wizard.skipApplication"
            />
            <el-form v-if="!wizard.skipApplication" label-position="top" class="wizard-form">
              <el-form-item label="How does this source help?" required>
                <el-input v-model="wizard.application.title" placeholder="Example: Apply quote to feedback loop" />
              </el-form-item>
              <el-form-item label="Design note">
                <el-input v-model="wizard.application.description" type="textarea" :rows="5" placeholder="Explain how this reading source changes the prototype." />
              </el-form-item>
              <el-form-item label="Application type">
                <el-select v-model="wizard.application.applicationType">
                  <el-option label="Mechanic Test" value="MECHANIC_TEST" />
                  <el-option label="Design Lens Review" value="DESIGN_LENS_REVIEW" />
                  <el-option label="Prototype Task" value="PROTOTYPE_TASK" />
                  <el-option label="Design Reference" value="DESIGN_REFERENCE" />
                  <el-option label="Project Application" value="PROJECT_APPLICATION" />
                </el-select>
              </el-form-item>
            </el-form>
          </template>

          <template v-else-if="activeStep === 3">
            <SkipStepControl
              :skipped="wizard.skipDecision"
              label="Create Design Decision"
              @toggle="wizard.skipDecision = !wizard.skipDecision"
            />
            <el-form v-if="!wizard.skipDecision" label-position="top" class="wizard-form">
              <el-form-item label="Decision title" required>
                <el-input v-model="wizard.decision.title" />
              </el-form-item>
              <el-form-item label="Decision" required>
                <el-input v-model="wizard.decision.decision" type="textarea" :rows="3" placeholder="What will you change or test?" />
              </el-form-item>
              <el-form-item label="Rationale">
                <el-input v-model="wizard.decision.rationale" type="textarea" :rows="3" placeholder="Why does the source support this decision?" />
              </el-form-item>
              <el-form-item label="Tradeoffs">
                <el-input v-model="wizard.decision.tradeoffs" type="textarea" :rows="3" placeholder="What might this decision make worse?" />
              </el-form-item>
            </el-form>
          </template>

          <template v-else-if="activeStep === 4">
            <SkipStepControl
              :skipped="wizard.skipPlan"
              label="Create Playtest Plan"
              @toggle="wizard.skipPlan = !wizard.skipPlan"
            />
            <el-form v-if="!wizard.skipPlan" label-position="top" class="wizard-form">
              <el-form-item label="Playtest plan title" required>
                <el-input v-model="wizard.plan.title" />
              </el-form-item>
              <el-form-item label="Hypothesis">
                <el-input v-model="wizard.plan.hypothesis" type="textarea" :rows="3" placeholder="What do you expect players to understand or do?" />
              </el-form-item>
              <el-form-item label="Task">
                <el-input v-model="wizard.plan.tasks" type="textarea" :rows="3" placeholder="What should the player try during the test?" />
              </el-form-item>
              <el-form-item label="Success criteria">
                <el-input v-model="wizard.plan.successCriteria" type="textarea" :rows="3" placeholder="What evidence would show this worked?" />
              </el-form-item>
            </el-form>
          </template>

          <template v-else-if="activeStep === 5">
            <SkipStepControl
              :skipped="wizard.skipFinding"
              label="Record Playtest Finding"
              @toggle="wizard.skipFinding = !wizard.skipFinding"
            />
            <el-form v-if="!wizard.skipFinding" label-position="top" class="wizard-form">
              <el-form-item label="Finding title" required>
                <el-input v-model="wizard.finding.title" />
              </el-form-item>
              <el-form-item label="Observation">
                <el-input v-model="wizard.finding.observation" type="textarea" :rows="3" placeholder="What did you observe or expect to observe?" />
              </el-form-item>
              <el-form-item label="Recommendation">
                <el-input v-model="wizard.finding.recommendation" type="textarea" :rows="3" placeholder="What should change in the next iteration?" />
              </el-form-item>
            </el-form>

            <SkipStepControl
              :skipped="wizard.skipIterationNote"
              label="Create Iteration Note"
              @toggle="wizard.skipIterationNote = !wizard.skipIterationNote"
            />
            <el-form v-if="!wizard.skipIterationNote" label-position="top" class="wizard-form">
              <el-form-item label="Iteration note">
                <el-input v-model="wizard.iterationNote" type="textarea" :rows="4" placeholder="Summarize the next change and evidence source." />
              </el-form-item>
              <p class="helper-text">
                Stored as a project knowledge link so it remains traceable to the selected quote, concept, design knowledge record, or source link.
              </p>
            </el-form>
          </template>

          <template v-else>
            <AppErrorState
              v-if="submitError"
              title="Project records were not created"
              :description="submitError"
              compact
            />

            <div class="finish-grid">
              <AppCard variant="muted">
                <p class="eyebrow">Before confirm</p>
                <h3>Review before creating</h3>
                <p>The wizard now uses one transactional request. If validation or network submission fails, BookOS does not create a confusing partial set.</p>
              </AppCard>
              <AppCard variant="muted">
                <p class="eyebrow">Source preservation</p>
                <h3>{{ selectedSourceReference ? 'Source link ready' : 'No source link available' }}</h3>
                <p>{{ selectedSourceReference ? sourceLocation(selectedSourceReference) : 'Template or source-free records will clearly remain source-free.' }}</p>
              </AppCard>
            </div>

            <AppCard class="review-summary" variant="muted">
              <AppSectionHeader title="Records that will be created" eyebrow="Final review" :level="2" compact />
              <AppEmptyState
                v-if="!recordsToCreate.length"
                title="No records ready yet"
                description="Go back and add text to at least one unskipped step before confirming."
                compact
              />
              <ul v-else>
                <li v-for="record in recordsToCreate" :key="`${record.type}-${record.title}`">
                  <strong>{{ record.type }}</strong>: {{ record.title }}
                  <AppBadge v-if="record.sourcePreserved" variant="success" size="sm">Source will be preserved</AppBadge>
                  <AppBadge v-else variant="neutral" size="sm">No source link</AppBadge>
                </li>
              </ul>
              <p class="helper-text">
                Idempotency key: {{ idempotencyKey }}. Re-submitting the same final step returns the previous result instead of duplicating records.
              </p>
            </AppCard>

            <AppCard v-if="createdRecords.length" class="created-summary" variant="highlight">
              <AppSectionHeader title="Guided project records created" eyebrow="Finish" :level="2" compact />
              <p v-if="wizardResult?.duplicate" class="helper-text">This was an idempotent replay. BookOS returned the previous result and did not create duplicates.</p>
              <ul>
                <li v-for="record in createdRecords" :key="record.key">
                  <strong>{{ record.type }}</strong>: {{ record.title }}
                  <AppBadge v-if="record.sourcePreserved" variant="success" size="sm">Source preserved</AppBadge>
                </li>
              </ul>
            </AppCard>
          </template>
        </section>

        <div v-if="selectedSourceReference" class="source-preservation" role="note">
          <strong>Source link preserved:</strong>
          {{ sourceLocation(selectedSourceReference) }}
          <span>Unknown pages stay null; BookOS does not invent page numbers.</span>
        </div>
        <div v-else class="source-preservation source-preservation--muted" role="note">
          <strong>No source link selected.</strong>
          Template prompts and source-free records stay clearly labeled as source-free.
        </div>

        <div class="wizard-actions">
          <AppButton variant="secondary" :disabled="activeStep === 0" @click="previousStep">Back</AppButton>
          <AppButton v-if="activeStep < steps.length - 1" variant="primary" @click="nextStep">Next</AppButton>
          <AppButton v-else variant="primary" :loading="confirming" :disabled="Boolean(createdRecords.length) || !recordsToCreate.length" @click="confirmWizard">
            Confirm Transaction
          </AppButton>
          <RouterLink v-if="createdRecords.length" :to="{ name: 'project-detail', params: { id: project.id } }" custom v-slot="{ navigate }">
            <AppButton variant="accent" @click="navigate">Open Project Cockpit</AppButton>
          </RouterLink>
          <RouterLink
            v-if="createdRecords.length"
            :to="{ name: 'forum-new', query: { relatedEntityType: 'GAME_PROJECT', relatedEntityId: String(project.id), projectId: String(project.id), title: `Discuss ${project.title}` } }"
            custom
            v-slot="{ navigate }"
          >
            <AppButton variant="secondary" @click="navigate">Start Forum Discussion</AppButton>
          </RouterLink>
        </div>
      </AppCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getDailyToday } from '../api/daily'
import { getConcepts, getKnowledgeObjects } from '../api/knowledge'
import {
  applyKnowledgeWizard,
  getProject,
} from '../api/projects'
import { getQuotes } from '../api/quotes'
import { getSourceReferences } from '../api/sourceReferences'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import type {
  ConceptRecord,
  DailyDesignPromptRecord,
  GameProjectRecord,
  KnowledgeObjectRecord,
  ProjectWizardApplyKnowledgePayload,
  ProjectWizardApplyKnowledgeRecord,
  QuoteRecord,
  SourceReferenceRecord,
} from '../types'

type WizardStepKey = 'source' | 'problem' | 'application' | 'decision' | 'plan' | 'finding' | 'finish'
type WizardSourceType = 'QUOTE' | 'CONCEPT' | 'KNOWLEDGE_OBJECT' | 'SOURCE_REFERENCE' | 'DAILY_DESIGN_PROMPT'

interface WizardSourceOption {
  key: string
  type: WizardSourceType
  id: number
  label: string
  detail: string
  sourceReference: SourceReferenceRecord | null
  dailyTemplate?: boolean
}

interface CreatedRecord {
  key: string
  type: string
  id: number | null
  title: string
  sourcePreserved: boolean
}

const SkipStepControl = defineComponent({
  props: {
    skipped: { type: Boolean, required: true },
    label: { type: String, required: true },
  },
  emits: ['toggle'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'skip-control' }, [
        h('div', [h('strong', props.label), h('span', props.skipped ? ' This step will not create a record.' : ' This step will create a record only after final confirmation.')]),
        h(
          AppButton,
          {
            variant: props.skipped ? 'secondary' : 'ghost',
            onClick: () => emit('toggle'),
          },
          () => (props.skipped ? 'Use this step' : 'Skip this step'),
        ),
      ])
  },
})

const route = useRoute()
const router = useRouter()
const project = ref<GameProjectRecord | null>(null)
const quotes = ref<QuoteRecord[]>([])
const concepts = ref<ConceptRecord[]>([])
const knowledgeObjects = ref<KnowledgeObjectRecord[]>([])
const sourceReferences = ref<SourceReferenceRecord[]>([])
const dailyPrompt = ref<DailyDesignPromptRecord | null>(null)
const loading = ref(false)
const confirming = ref(false)
const errorMessage = ref('')
const activeStep = ref(0)
const sourceQuery = ref('')
const selectedSourceKey = ref('')
const createdRecords = ref<CreatedRecord[]>([])
const wizardResult = ref<ProjectWizardApplyKnowledgeRecord | null>(null)
const submitError = ref('')
const idempotencyKey = ref(createIdempotencyKey())

const steps: Array<{ key: WizardStepKey; shortTitle: string; title: string; description: string }> = [
  {
    key: 'source',
    shortTitle: 'Source',
    title: 'Choose the reading source',
    description: 'Start from a real quote, concept, design knowledge record, source link, or daily prompt. This is the evidence the project work points back to.',
  },
  {
    key: 'problem',
    shortTitle: 'Problem',
    title: 'Define the project problem',
    description: 'Name the specific design issue you are trying to solve so the source-backed idea has a concrete target.',
  },
  {
    key: 'application',
    shortTitle: 'Apply',
    title: 'Apply the knowledge',
    description: 'Explain how the source changes the project. This creates the bridge from reading to design action.',
  },
  {
    key: 'decision',
    shortTitle: 'Decision',
    title: 'Make a design decision',
    description: 'Record the decision, rationale, and tradeoffs so the project has memory instead of scattered inspiration.',
  },
  {
    key: 'plan',
    shortTitle: 'Playtest',
    title: 'Plan the playtest',
    description: 'Turn the decision into a testable hypothesis and a small player task.',
  },
  {
    key: 'finding',
    shortTitle: 'Finding',
    title: 'Record a finding and iteration note',
    description: 'Capture the expected or observed evidence and summarize what should change next.',
  },
  {
    key: 'finish',
    shortTitle: 'Finish',
    title: 'Confirm created records',
    description: 'Review what will be created. Nothing is saved until you press the confirmation button.',
  },
]

const wizard = reactive({
  skipProblem: false,
  skipApplication: false,
  skipDecision: false,
  skipPlan: false,
  skipFinding: false,
  skipIterationNote: false,
  problem: {
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'OPEN',
  },
  application: {
    title: '',
    description: '',
    applicationType: 'PROJECT_APPLICATION',
  },
  decision: {
    title: '',
    decision: '',
    rationale: '',
    tradeoffs: '',
    status: 'PROPOSED',
  },
  plan: {
    title: '',
    hypothesis: '',
    targetPlayers: '',
    tasks: '',
    successCriteria: '',
    status: 'DRAFT',
  },
  finding: {
    title: '',
    observation: '',
    recommendation: '',
    severity: 'MEDIUM',
    status: 'OPEN',
  },
  iterationNote: '',
})

const draftKey = computed(() => `bookos.projectWizard.${route.params.id}`)
const sourceOptions = computed<WizardSourceOption[]>(() => {
  const quoteOptions = quotes.value.map((quote) => ({
    key: `QUOTE:${quote.id}`,
    type: 'QUOTE' as const,
    id: quote.id,
    label: quote.text,
    detail: quote.bookTitle,
    sourceReference: quote.sourceReference,
  }))

  const conceptOptions = concepts.value.map((concept) => ({
    key: `CONCEPT:${concept.id}`,
    type: 'CONCEPT' as const,
    id: concept.id,
    label: concept.name,
    detail: concept.description ?? concept.bookTitle ?? 'Reviewed concept',
    sourceReference: concept.firstSourceReference ?? concept.sourceReferences[0] ?? null,
  }))

  const knowledgeOptions = knowledgeObjects.value.map((object) => ({
    key: `KNOWLEDGE_OBJECT:${object.id}`,
    type: 'KNOWLEDGE_OBJECT' as const,
    id: object.id,
    label: object.title,
    detail: object.description ?? object.type,
    sourceReference: object.sourceReference,
  }))

  const referenceOptions = sourceReferences.value.map((source) => ({
    key: `SOURCE_REFERENCE:${source.id}`,
    type: 'SOURCE_REFERENCE' as const,
    id: source.id,
    label: source.locationLabel ?? `Source link #${source.id}`,
    detail: source.sourceText ?? source.sourceType,
    sourceReference: source,
  }))

  const dailyOptions: WizardSourceOption[] = dailyPrompt.value
    ? [
        {
          key: `DAILY_DESIGN_PROMPT:${dailyPrompt.value.id}`,
          type: 'DAILY_DESIGN_PROMPT',
          id: dailyPrompt.value.id,
          label: dailyPrompt.value.question,
          detail: dailyPrompt.value.sourceTitle ?? dailyPrompt.value.bookTitle ?? 'Daily design prompt',
          sourceReference: dailyPrompt.value.sourceReference,
          dailyTemplate: dailyPrompt.value.templatePrompt,
        },
      ]
    : []

  const unique = new Map<string, WizardSourceOption>()
  ;[...quoteOptions, ...conceptOptions, ...knowledgeOptions, ...referenceOptions, ...dailyOptions].forEach((option) => unique.set(option.key, option))
  return [...unique.values()]
})

const filteredSourceOptions = computed(() => {
  const query = sourceQuery.value.trim().toLowerCase()
  if (!query) return sourceOptions.value
  return sourceOptions.value.filter((option) => `${option.label} ${option.detail} ${option.type}`.toLowerCase().includes(query))
})
const selectedSource = computed(() => sourceOptions.value.find((option) => option.key === selectedSourceKey.value) ?? null)
const selectedSourceReference = computed(() => selectedSource.value?.sourceReference ?? null)
const recordsToCreate = computed(() => {
  const records: Array<{ type: string; title: string; sourcePreserved: boolean }> = []
  const sourcePreserved = Boolean(selectedSourceReference.value)
  if (!wizard.skipProblem && wizard.problem.title.trim()) {
    records.push({ type: 'Project Problem', title: wizard.problem.title.trim(), sourcePreserved })
  }
  if (!wizard.skipApplication && wizard.application.title.trim()) {
    records.push({ type: 'Project Application', title: wizard.application.title.trim(), sourcePreserved })
  }
  if (!wizard.skipDecision && wizard.decision.title.trim() && wizard.decision.decision.trim()) {
    records.push({ type: 'Design Decision', title: wizard.decision.title.trim(), sourcePreserved })
  }
  if (!wizard.skipPlan && wizard.plan.title.trim()) {
    records.push({ type: 'Playtest Plan', title: wizard.plan.title.trim(), sourcePreserved: false })
  }
  if (!wizard.skipFinding && wizard.finding.title.trim()) {
    records.push({ type: 'Playtest Finding', title: wizard.finding.title.trim(), sourcePreserved })
  }
  if (!wizard.skipIterationNote && wizard.iterationNote.trim() && selectedSource.value && iterationTarget(selectedSource.value)) {
    records.push({ type: 'Iteration Link', title: truncate(wizard.iterationNote.trim(), 90), sourcePreserved })
  }
  return records
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const projectId = String(route.params.id)
    const [projectResult, quoteResult, conceptResult, knowledgeResult, sourceResult, dailyResult] = await Promise.all([
      getProject(projectId),
      getQuotes(),
      getConcepts(),
      getKnowledgeObjects(),
      getSourceReferences(),
      getDailyToday().catch(() => null),
    ])
    project.value = projectResult
    quotes.value = quoteResult
    concepts.value = conceptResult
    knowledgeObjects.value = knowledgeResult
    sourceReferences.value = sourceResult
    dailyPrompt.value = dailyResult?.prompt ?? null
    restoreDraft()
    preselectSourceFromQuery()
    hydrateDefaults()
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try opening the guided project workflow again.'
  } finally {
    loading.value = false
  }
}

function preselectSourceFromQuery() {
  if (selectedSourceKey.value) return
  const sourceType = typeof route.query.sourceType === 'string' ? route.query.sourceType.toUpperCase() : ''
  const sourceId = typeof route.query.sourceId === 'string' ? Number(route.query.sourceId) : NaN
  if (sourceType && Number.isFinite(sourceId)) {
    const key = `${sourceType}:${sourceId}`
    if (sourceOptions.value.some((option) => option.key === key)) {
      selectedSourceKey.value = key
      return
    }
  }

  const sourceReferenceId = typeof route.query.sourceReferenceId === 'string' ? Number(route.query.sourceReferenceId) : NaN
  if (Number.isFinite(sourceReferenceId)) {
    const key = `SOURCE_REFERENCE:${sourceReferenceId}`
    if (sourceOptions.value.some((option) => option.key === key)) {
      selectedSourceKey.value = key
      return
    }
  }

  selectedSourceKey.value = sourceOptions.value[0]?.key ?? ''
}

function hydrateDefaults() {
  const source = selectedSource.value
  if (!source) return
  if (!wizard.problem.title) wizard.problem.title = `Solve project issue using ${sourceTypeLabel(source.type).toLowerCase()}`
  if (!wizard.application.title) wizard.application.title = `Apply ${sourceTypeLabel(source.type).toLowerCase()}: ${truncate(source.label, 80)}`
  if (!wizard.application.description) wizard.application.description = source.detail
  if (!wizard.decision.title) wizard.decision.title = `Decision from ${sourceTypeLabel(source.type).toLowerCase()}`
  if (!wizard.decision.rationale) wizard.decision.rationale = `This decision is based on ${source.label}.`
  if (!wizard.plan.title) wizard.plan.title = `Playtest ${project.value?.title ?? 'project'} change`
  if (!wizard.finding.title) wizard.finding.title = `Finding from ${project.value?.title ?? 'project'} playtest`
  if (!wizard.iterationNote) wizard.iterationNote = `Next iteration should respond to ${source.label}.`
}

function nextStep() {
  if (activeStep.value === 0 && !selectedSource.value) {
    ElMessage.warning('Choose a real source first, or create one before using the wizard.')
    return
  }
  hydrateDefaults()
  activeStep.value = Math.min(activeStep.value + 1, steps.length - 1)
}

function previousStep() {
  activeStep.value = Math.max(activeStep.value - 1, 0)
}

async function cancelWizard() {
  try {
    await ElMessageBox.confirm('Cancel the wizard? No project records have been created unless you already confirmed the final step.', 'Cancel Guided Flow', {
      confirmButtonText: 'Cancel Wizard',
      cancelButtonText: 'Keep Working',
      type: 'warning',
    })
    await router.push({ name: 'project-detail', params: { id: route.params.id } })
  } catch {
    // User kept working.
  }
}

function saveDraft() {
  window.localStorage.setItem(
    draftKey.value,
    JSON.stringify({
      selectedSourceKey: selectedSourceKey.value,
      activeStep: activeStep.value,
      idempotencyKey: idempotencyKey.value,
      wizard,
    }),
  )
  ElMessage.success('Wizard draft saved on this device. No project records were created.')
}

function restoreDraft() {
  const raw = window.localStorage.getItem(draftKey.value)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw) as { selectedSourceKey?: string; activeStep?: number; idempotencyKey?: string; wizard?: Partial<typeof wizard> }
    selectedSourceKey.value = parsed.selectedSourceKey ?? ''
    activeStep.value = Number.isInteger(parsed.activeStep) ? Math.min(Math.max(Number(parsed.activeStep), 0), steps.length - 1) : 0
    idempotencyKey.value = parsed.idempotencyKey || idempotencyKey.value
    if (parsed.wizard) {
      Object.assign(wizard, parsed.wizard)
    }
  } catch {
    window.localStorage.removeItem(draftKey.value)
  }
}

async function confirmWizard() {
  if (!project.value || !selectedSource.value) {
    ElMessage.warning('Choose a real source before confirming.')
    return
  }
  if (!recordsToCreate.value.length) {
    ElMessage.warning('Add text to at least one unskipped step before confirming.')
    return
  }

  confirming.value = true
  createdRecords.value = []
  wizardResult.value = null
  submitError.value = ''

  try {
    const result = await applyKnowledgeWizard(project.value.id, buildWizardPayload())
    wizardResult.value = result
    createdRecords.value = result.createdRecords.map((record) => ({
      key: `${record.type}-${record.id}`,
      type: record.type,
      id: record.id,
      title: record.title,
      sourcePreserved: record.sourcePreserved,
    }))
    window.localStorage.removeItem(draftKey.value)
    ElMessage.success(result.duplicate ? 'Previous wizard result returned. No duplicate records were created.' : 'Guided project records created transactionally.')
  } catch {
    submitError.value = 'The transactional wizard request failed. No partial project records were created by this submission.'
    ElMessage.error(submitError.value)
  } finally {
    confirming.value = false
  }
}

function buildWizardPayload(): ProjectWizardApplyKnowledgePayload {
  const source = selectedSource.value
  const sourceReferenceId = selectedSourceReference.value?.id ?? null
  const payload: ProjectWizardApplyKnowledgePayload = {
    sourceType: source?.type ?? null,
    sourceId: source?.id ?? null,
    sourceReferenceId,
    clientStepIntent: steps[activeStep.value]?.key ?? 'finish',
    idempotencyKey: idempotencyKey.value,
  }

  if (!wizard.skipProblem && wizard.problem.title.trim()) {
    payload.projectProblem = {
      title: wizard.problem.title.trim(),
      description: wizard.problem.description.trim() || null,
      priority: wizard.problem.priority,
      status: wizard.problem.status,
      relatedSourceReferenceId: sourceReferenceId,
    }
  }

  if (!wizard.skipApplication && wizard.application.title.trim()) {
    payload.projectApplication = {
      sourceEntityType: source?.type ?? null,
      sourceEntityId: source?.id ?? null,
      sourceReferenceId,
      applicationType: wizard.application.applicationType,
      title: wizard.application.title.trim(),
      description: wizard.application.description.trim() || null,
      status: 'OPEN',
    }
  }

  if (!wizard.skipDecision && wizard.decision.title.trim() && wizard.decision.decision.trim()) {
    payload.designDecision = {
      title: wizard.decision.title.trim(),
      decision: wizard.decision.decision.trim(),
      rationale: wizard.decision.rationale.trim() || null,
      tradeoffs: wizard.decision.tradeoffs.trim() || null,
      status: wizard.decision.status,
      sourceReferenceId,
    }
  }

  if (!wizard.skipPlan && wizard.plan.title.trim()) {
    payload.playtestPlan = {
      title: wizard.plan.title.trim(),
      hypothesis: wizard.plan.hypothesis.trim() || null,
      targetPlayers: wizard.plan.targetPlayers.trim() || null,
      tasks: wizard.plan.tasks.trim() || null,
      successCriteria: wizard.plan.successCriteria.trim() || null,
      status: wizard.plan.status,
    }
  }

  if (!wizard.skipFinding && wizard.finding.title.trim()) {
    payload.playtestFinding = {
      title: wizard.finding.title.trim(),
      observation: wizard.finding.observation.trim() || null,
      recommendation: wizard.finding.recommendation.trim() || null,
      severity: wizard.finding.severity,
      status: wizard.finding.status,
      sourceReferenceId,
    }
  }

  if (!wizard.skipIterationNote && wizard.iterationNote.trim() && source) {
    const target = iterationTarget(source)
    if (target) {
      payload.projectKnowledgeLink = {
        targetType: target.type,
        targetId: target.id,
        relationshipType: 'ITERATION_NOTE',
        note: wizard.iterationNote.trim(),
        sourceReferenceId,
      }
    }
  }

  return payload
}

function iterationTarget(source: WizardSourceOption) {
  if (source.type === 'DAILY_DESIGN_PROMPT') {
    return selectedSourceReference.value ? { type: 'SOURCE_REFERENCE', id: selectedSourceReference.value.id } : null
  }
  return { type: source.type, id: source.id }
}

function stepSkipped(key: WizardStepKey) {
  if (key === 'problem') return wizard.skipProblem
  if (key === 'application') return wizard.skipApplication
  if (key === 'decision') return wizard.skipDecision
  if (key === 'plan') return wizard.skipPlan
  if (key === 'finding') return wizard.skipFinding && wizard.skipIterationNote
  return false
}

function sourceVariant(type: WizardSourceType) {
  if (type === 'QUOTE') return 'accent'
  if (type === 'CONCEPT') return 'info'
  if (type === 'KNOWLEDGE_OBJECT') return 'primary'
  if (type === 'SOURCE_REFERENCE') return 'success'
  return 'warning'
}

function sourceTypeLabel(type: WizardSourceType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function sourceLocation(source: SourceReferenceRecord) {
  const page = source.pageStart && source.pageEnd ? `p.${source.pageStart}-${source.pageEnd}` : source.pageStart ? `p.${source.pageStart}` : 'page unknown'
  return `${source.locationLabel ?? source.sourceType} (${page})`
}

function truncate(value: string, max: number) {
  return value.length > max ? `${value.slice(0, max - 1)}...` : value
}

function createIdempotencyKey() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID()
  }
  return `wizard-${Date.now()}-${Math.random().toString(36).slice(2)}`
}
</script>

<style scoped>
.project-wizard-page,
.wizard-panel,
.wizard-step,
.wizard-form {
  display: grid;
  gap: var(--space-5);
}

.wizard-intro,
.wizard-panel {
  padding: var(--space-5);
}

.wizard-intro {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: flex-start;
}

.wizard-intro h2,
.wizard-intro p,
.source-card h3,
.source-card p,
.review-summary ul,
.created-summary ul {
  margin: 0;
}

.wizard-intro p,
.source-card p,
.source-preservation,
.helper-text {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.wizard-intro__badges,
.source-card__topline,
.wizard-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.wizard-step__header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: flex-start;
}

.wizard-step__header h2,
.wizard-step__header p {
  margin: var(--space-1) 0 0;
}

.source-toolbar,
.wizard-form__grid,
.finish-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--space-3);
  align-items: center;
}

.source-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.source-card {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
}

.source-card__meta {
  font-size: var(--type-metadata);
}

.skip-control {
  padding: var(--space-3);
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: center;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.skip-control span {
  display: block;
  margin-top: var(--space-1);
  color: var(--bookos-text-secondary);
}

.source-preservation {
  padding: var(--space-3);
  display: grid;
  gap: var(--space-1);
  border: 1px solid color-mix(in srgb, var(--bookos-success) 32%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-success) 10%, var(--bookos-surface));
}

.source-preservation--muted {
  border-color: var(--bookos-border);
  background: var(--bookos-surface-muted);
}

.review-summary,
.created-summary {
  padding: var(--space-4);
}

.review-summary ul,
.created-summary ul {
  padding-left: 1.1rem;
  display: grid;
  gap: var(--space-2);
}

@media (max-width: 900px) {
  .wizard-intro,
  .wizard-step__header,
  .skip-control {
    flex-direction: column;
  }

  .source-grid,
  .source-toolbar,
  .wizard-form__grid,
  .finish-grid {
    grid-template-columns: 1fr;
  }
}
</style>
