<template>
  <main class="onboarding-page" aria-labelledby="onboarding-title">
    <AppCard class="onboarding-card">
      <div class="onboarding-card__header">
        <div>
          <p class="eyebrow">First-run setup</p>
          <h1 id="onboarding-title">Shape BookOS around your workflow</h1>
          <p>BookOS helps you turn reading into source-backed notes, knowledge, projects, and discussions.</p>
        </div>
        <AppBadge variant="accent" size="sm">Step {{ currentStep }} of 5</AppBadge>
      </div>

      <ol class="onboarding-steps" aria-label="Onboarding progress">
        <li v-for="step in steps" :key="step.number" :class="{ active: step.number === currentStep, complete: step.number < currentStep }">
          <span>{{ step.number }}</span>
          {{ step.label }}
        </li>
      </ol>

      <section v-if="currentStep === 1" class="onboarding-panel" aria-labelledby="welcome-step-title">
        <h2 id="welcome-step-title">Start with the loop, not the modules</h2>
        <p>
          The product works best when you pick one path first: track reading, capture thoughts, build concepts, apply
          insights to a project, or join a source-linked discussion.
        </p>
        <div class="mode-preview-grid" aria-label="BookOS core loop">
          <AppCard v-for="item in loopPreview" :key="item.title" variant="muted" class="mode-preview">
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </AppCard>
        </div>
      </section>

      <section v-else-if="currentStep === 2" class="onboarding-panel" aria-labelledby="use-case-step-title">
        <h2 id="use-case-step-title">What do you want to do first?</h2>
        <p>Choose the intent that should shape your first dashboard and next actions.</p>
        <div class="option-grid">
          <button
            v-for="option in useCaseOptions"
            :key="option.value"
            class="choice-card"
            type="button"
            :aria-pressed="primaryUseCase === option.value"
            @click="primaryUseCase = option.value"
          >
            <span>{{ option.label }}</span>
            <small>{{ option.description }}</small>
          </button>
        </div>
      </section>

      <section v-else-if="currentStep === 3" class="onboarding-panel" aria-labelledby="mode-step-title">
        <h2 id="mode-step-title">Choose your starting mode</h2>
        <p>This does not remove any features. It only gives BookOS a clearer first-run path.</p>
        <div class="option-grid option-grid--compact">
          <button
            v-for="option in modeOptions"
            :key="option.value"
            class="choice-card"
            type="button"
            :aria-pressed="startingMode === option.value"
            @click="startingMode = option.value"
          >
            <span>{{ option.label }}</span>
            <small>{{ option.description }}</small>
          </button>
        </div>
      </section>

      <section v-else-if="currentStep === 4" class="onboarding-panel" aria-labelledby="first-object-step-title">
        <h2 id="first-object-step-title">{{ selectedModePlan.createTitle }}</h2>
        <p>{{ selectedModePlan.createDescription }}</p>
        <AppCard variant="highlight" class="workflow-card">
          <div>
            <span class="workflow-card__label">Recommended route</span>
            <strong>{{ selectedModePlan.routeLabel }}</strong>
          </div>
          <AppBadge variant="primary" size="sm">{{ selectedModeLabel }}</AppBadge>
        </AppCard>
      </section>

      <section v-else class="onboarding-panel" aria-labelledby="next-actions-step-title">
        <h2 id="next-actions-step-title">Your next 3 actions</h2>
        <p>These are practical next steps based on {{ selectedModeLabel }}.</p>
        <ol class="next-actions-list">
          <li v-for="action in selectedModePlan.nextActions" :key="action">{{ action }}</li>
        </ol>
        <AppEmptyState
          class="onboarding-empty-state"
          :title="selectedModePlan.emptyTitle"
          :description="selectedModePlan.emptyDescription"
          compact
        />
      </section>

      <p v-if="validationMessage" class="validation-message" role="alert">{{ validationMessage }}</p>

      <div class="onboarding-actions">
        <AppButton variant="text" :loading="saving" @click="skipOnboarding">Skip for now</AppButton>
        <div class="onboarding-actions__right">
          <AppButton v-if="currentStep > 1" variant="ghost" :disabled="saving" @click="currentStep -= 1">Back</AppButton>
          <AppButton v-if="currentStep < 5" variant="primary" :disabled="saving" @click="goNext">Next</AppButton>
          <AppButton v-else variant="primary" :loading="saving" @click="completeOnboarding">Start workflow</AppButton>
        </div>
      </div>
    </AppCard>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import { useAuthStore } from '../stores/auth'
import type { OnboardingUseCase, StartingMode } from '../types'

interface UseCaseOption {
  value: OnboardingUseCase
  label: string
  description: string
}

interface ModeOption {
  value: StartingMode
  label: string
  description: string
}

interface ModePlan {
  createTitle: string
  createDescription: string
  routeName: string
  routeLabel: string
  nextActions: string[]
  emptyTitle: string
  emptyDescription: string
}

const router = useRouter()
const auth = useAuthStore()

const currentStep = ref(1)
const primaryUseCase = ref<OnboardingUseCase | null>(auth.user?.primaryUseCase ?? null)
const startingMode = ref<StartingMode | null>(auth.user?.startingMode ?? null)
const saving = ref(false)
const validationMessage = ref('')

const steps = [
  { number: 1, label: 'Welcome' },
  { number: 2, label: 'Use case' },
  { number: 3, label: 'Mode' },
  { number: 4, label: 'First object' },
  { number: 5, label: 'Next actions' },
]

const loopPreview = [
  { title: 'Read', description: 'Add a book and track where you are.' },
  { title: 'Capture', description: 'Save raw thoughts with pages, tags, and concepts.' },
  { title: 'Convert', description: 'Turn captures into notes, quotes, actions, and concepts.' },
  { title: 'Apply', description: 'Move source-backed knowledge into project work.' },
]

const useCaseOptions: UseCaseOption[] = [
  {
    value: 'TRACK_READING',
    label: 'I want to track my reading.',
    description: 'Start with a shelf, status, pages, and ratings.',
  },
  {
    value: 'BETTER_NOTES',
    label: 'I want to take better book notes.',
    description: 'Start with a book and a structured note.',
  },
  {
    value: 'QUICK_CAPTURE',
    label: 'I want to capture ideas while reading.',
    description: 'Start with fast capture and parser-backed metadata.',
  },
  {
    value: 'CONCEPTS_ACTIONS',
    label: 'I want to turn books into concepts and actions.',
    description: 'Start with concepts, action items, and source references.',
  },
  {
    value: 'GAME_PROJECT',
    label: 'I want to apply game design knowledge to a project.',
    description: 'Start by pairing a game design book with a project.',
  },
  {
    value: 'COMMUNITY_DISCUSSION',
    label: 'I want to discuss books and concepts with others.',
    description: 'Start with source-linked forum threads.',
  },
  {
    value: 'ADVANCED_TOOLS',
    label: 'I want to explore advanced graph/AI/import features.',
    description: 'Start from the full dashboard with advanced tools visible.',
  },
]

const modeOptions: ModeOption[] = [
  { value: 'READER', label: 'Reader Mode', description: 'Track a book and capture your first note.' },
  { value: 'NOTE_TAKER', label: 'Note-Taker Mode', description: 'Start from books, notes, and source-backed blocks.' },
  { value: 'GAME_DESIGNER', label: 'Game Designer Mode', description: 'Apply reading material to an active game project.' },
  { value: 'RESEARCHER', label: 'Researcher Mode', description: 'Build concepts and knowledge objects from books.' },
  { value: 'COMMUNITY', label: 'Community Mode', description: 'Discuss books, concepts, and sources in the forum.' },
  { value: 'ADVANCED', label: 'Advanced Mode', description: 'Use the dashboard, graph, import/export, and AI draft tools.' },
]

const modePlans: Record<StartingMode, ModePlan> = {
  READER: {
    createTitle: 'Create your first book',
    createDescription: 'BookOS will send you to the Add Book form so you can create a real shelf item.',
    routeName: 'add-book',
    routeLabel: 'Add Book',
    nextActions: ['Add a book.', 'Set reading status and current page.', 'Capture your first note.'],
    emptyTitle: 'Your Reader Mode shelf is empty',
    emptyDescription: 'Add a real book first; BookOS will not show sample books as your data.',
  },
  NOTE_TAKER: {
    createTitle: 'Create your first book, then your first note',
    createDescription: 'Start with a source book so every note has a stable source context.',
    routeName: 'add-book',
    routeLabel: 'Add Book',
    nextActions: ['Add a source book.', 'Open the book detail page.', 'Create a Markdown note or quick capture.'],
    emptyTitle: 'No note source yet',
    emptyDescription: 'Notes become useful when they point back to a real book, page, or capture.',
  },
  GAME_DESIGNER: {
    createTitle: 'Create a book and a game project',
    createDescription: 'Start with a game design book, then create a project to apply source-backed ideas.',
    routeName: 'add-book',
    routeLabel: 'Add Game Design Book',
    nextActions: ['Add a game design book.', 'Create a project.', 'Apply a quote or concept to the project.'],
    emptyTitle: 'No project evidence yet',
    emptyDescription: 'Project Mode becomes useful after you connect real reading material to a design problem.',
  },
  RESEARCHER: {
    createTitle: 'Create a book before building concepts',
    createDescription: 'Concepts should stay traceable, so start with the source that produced the idea.',
    routeName: 'add-book',
    routeLabel: 'Add Source Book',
    nextActions: ['Add a book.', 'Capture a [[Concept]] marker.', 'Review and save the parsed concept.'],
    emptyTitle: 'No source-backed concepts yet',
    emptyDescription: 'BookOS will keep unknown page numbers null instead of inventing source locations.',
  },
  COMMUNITY: {
    createTitle: 'Create or choose a source before discussion',
    createDescription: 'Source-linked discussions are easier to follow than generic forum posts.',
    routeName: 'add-book',
    routeLabel: 'Add Discussion Source',
    nextActions: ['Add or open a book.', 'Capture a quote or concept.', 'Start a source-linked forum thread.'],
    emptyTitle: 'No discussion context yet',
    emptyDescription: 'Forum threads can exist without sources, but BookOS is strongest when context is attached.',
  },
  ADVANCED: {
    createTitle: 'Open the advanced dashboard',
    createDescription: 'You will see the full product surface and can move into graph, import/export, or AI draft tools.',
    routeName: 'dashboard',
    routeLabel: 'Dashboard',
    nextActions: ['Open the dashboard.', 'Use Cmd/Ctrl+K search.', 'Explore graph, import/export, or mock AI drafts.'],
    emptyTitle: 'Advanced tools need real source data',
    emptyDescription: 'Graph, search, import/export, and mock AI panels are honest when no records exist.',
  },
}

const selectedMode = computed<StartingMode>(() => startingMode.value ?? 'READER')
const selectedModePlan = computed(() => modePlans[selectedMode.value])
const selectedModeLabel = computed(() => modeOptions.find((option) => option.value === selectedMode.value)?.label ?? 'Reader Mode')

function goNext() {
  validationMessage.value = ''
  if (currentStep.value === 2 && !primaryUseCase.value) {
    validationMessage.value = 'Choose a primary use case before continuing.'
    return
  }
  if (currentStep.value === 3 && !startingMode.value) {
    validationMessage.value = 'Choose a starting mode before continuing.'
    return
  }
  currentStep.value += 1
}

async function skipOnboarding() {
  await saveAndRoute({
    onboardingCompleted: true,
    primaryUseCase: null,
    startingMode: null,
    preferredDashboardMode: 'DEFAULT',
    routeName: 'dashboard',
    message: 'Onboarding skipped. You can restart it from the dashboard.',
  })
}

async function completeOnboarding() {
  if (!startingMode.value || !primaryUseCase.value) {
    validationMessage.value = 'Choose a use case and starting mode before starting.'
    currentStep.value = !primaryUseCase.value ? 2 : 3
    return
  }

  await saveAndRoute({
    onboardingCompleted: true,
    primaryUseCase: primaryUseCase.value,
    startingMode: startingMode.value,
    preferredDashboardMode: startingMode.value,
    routeName: selectedModePlan.value.routeName,
    message: 'Onboarding saved. Your first workflow is ready.',
  })
}

async function saveAndRoute(options: {
  onboardingCompleted: boolean
  primaryUseCase: OnboardingUseCase | null
  startingMode: StartingMode | null
  preferredDashboardMode: StartingMode | string | null
  routeName: string | null
  message: string
}) {
  saving.value = true
  validationMessage.value = ''
  try {
    await auth.updateOnboarding({
      onboardingCompleted: options.onboardingCompleted,
      primaryUseCase: options.primaryUseCase,
      startingMode: options.startingMode,
      preferredDashboardMode: options.preferredDashboardMode,
    })
    ElMessage.success(options.message)
    await router.push({ name: options.routeName ?? 'dashboard' })
  } catch {
    validationMessage.value = 'Onboarding preferences could not be saved. Check your connection and try again.'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.onboarding-page {
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
  padding: clamp(var(--space-4), 4vw, var(--space-8));
}

.onboarding-card {
  width: min(100%, 980px);
  padding: clamp(var(--space-5), 4vw, var(--space-8));
  display: grid;
  gap: var(--space-6);
}

.onboarding-card__header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: flex-start;
}

.onboarding-card h1,
.onboarding-card h2,
.onboarding-card p {
  margin: 0;
}

.onboarding-card h1 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(2rem, 5vw, 3.4rem);
  line-height: 1;
  letter-spacing: -0.03em;
}

.onboarding-card h2 {
  font-size: clamp(1.35rem, 3vw, 2rem);
}

.onboarding-card p {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
  max-width: 68ch;
}

.onboarding-steps {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: var(--space-2);
  padding: 0;
  margin: 0;
  list-style: none;
}

.onboarding-steps li {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--bookos-text-tertiary);
  font-size: var(--type-meta-size);
  font-weight: 800;
}

.onboarding-steps span {
  width: 1.8rem;
  height: 1.8rem;
  display: inline-grid;
  place-items: center;
  flex: 0 0 auto;
  border-radius: 999px;
  border: 1px solid var(--bookos-border);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
}

.onboarding-steps li.active {
  color: var(--bookos-primary);
}

.onboarding-steps li.active span,
.onboarding-steps li.complete span {
  border-color: var(--bookos-primary);
  background: var(--bookos-primary);
  color: white;
}

.onboarding-panel {
  display: grid;
  gap: var(--space-4);
}

.mode-preview-grid,
.option-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.option-grid--compact {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.mode-preview {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
}

.mode-preview span {
  color: var(--bookos-text-secondary);
}

.choice-card {
  min-height: 7.5rem;
  padding: var(--space-4);
  text-align: left;
  display: grid;
  align-content: start;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition:
    border-color var(--motion-base) var(--motion-ease),
    box-shadow var(--motion-base) var(--motion-ease),
    transform var(--motion-base) var(--motion-ease);
}

.choice-card:hover {
  border-color: var(--bookos-border-strong);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

.choice-card[aria-pressed="true"] {
  border-color: var(--bookos-primary);
  box-shadow: var(--focus-ring);
  background: linear-gradient(135deg, var(--bookos-primary-soft), var(--bookos-surface));
}

.choice-card span {
  font-weight: 900;
}

.choice-card small {
  color: var(--bookos-text-secondary);
  line-height: 1.45;
}

.workflow-card {
  padding: var(--space-5);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}

.workflow-card__label {
  display: block;
  color: var(--bookos-text-secondary);
  font-size: var(--type-meta-size);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.next-actions-list {
  margin: 0;
  padding-left: 1.2rem;
  display: grid;
  gap: var(--space-2);
  color: var(--bookos-text-primary);
  font-weight: 800;
}

.onboarding-empty-state {
  border: 1px dashed var(--bookos-border);
  border-radius: var(--radius-lg);
}

.validation-message {
  color: var(--bookos-danger);
  font-weight: 800;
}

.onboarding-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.onboarding-actions__right {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

@media (max-width: 860px) {
  .onboarding-steps,
  .mode-preview-grid,
  .option-grid,
  .option-grid--compact {
    grid-template-columns: 1fr;
  }

  .onboarding-card__header,
  .workflow-card {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
