<template>
  <div class="dashboard-page" :class="`dashboard-page--${dashboardMode}`">
    <AppErrorState
      v-if="error"
      title="Dashboard data could not be loaded"
      :description="error"
      retry-label="Retry"
      @retry="loadDashboard"
    />

    <template v-else>
      <section class="dashboard-hero" aria-labelledby="dashboard-title">
        <div>
          <p class="eyebrow">{{ modeGuide.eyebrow }}</p>
          <h1 id="dashboard-title">What should I do today?</h1>
          <p>{{ modeGuide.description }}</p>
        </div>
        <div class="dashboard-hero__actions">
          <RouterLink :to="nextActionPlan.primaryTo" custom v-slot="{ navigate }">
            <AppButton variant="primary" @click="navigate">{{ nextActionPlan.primaryLabel }}</AppButton>
          </RouterLink>
          <RouterLink to="/demo" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">Try Demo Workspace</AppButton>
          </RouterLink>
          <RouterLink to="/onboarding?restart=1" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Restart onboarding</AppButton>
          </RouterLink>
        </div>
      </section>

      <AppCard class="next-step-card" as="section" aria-live="polite">
        <div class="next-step-card__copy">
          <p class="eyebrow">Simplest next step</p>
          <h2>{{ nextActionPlan.title }}</h2>
          <p>{{ nextActionPlan.description }}</p>
        </div>
        <div class="next-step-card__actions">
          <RouterLink :to="nextActionPlan.primaryTo" custom v-slot="{ navigate }">
            <AppButton variant="primary" @click="navigate">{{ nextActionPlan.primaryLabel }}</AppButton>
          </RouterLink>
          <RouterLink v-if="nextActionPlan.secondaryTo" :to="nextActionPlan.secondaryTo" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">{{ nextActionPlan.secondaryLabel }}</AppButton>
          </RouterLink>
        </div>
      </AppCard>

      <section class="dashboard-section dashboard-section--today" aria-labelledby="today-title">
        <AppSectionHeader
          eyebrow="Today"
          title="Today"
          description="One quote, one design prompt, and one action. No fake resurfacing when source data is missing."
          compact
        >
          <template #actions>
            <HelpTooltip topic="daily-prompt" placement="left" />
          </template>
        </AppSectionHeader>
        <div class="today-grid">
          <AppCard class="task-card task-card--quote" as="article">
            <div class="task-card__header">
              <div>
                <p class="eyebrow">Daily quote</p>
                <h3 id="today-title">What should I remember?</h3>
              </div>
              <AppBadge v-if="daily?.sentence?.sourceBacked" variant="success" size="sm">Source-backed</AppBadge>
            </div>
            <template v-if="daily?.sentence">
              <blockquote>{{ daily.sentence.text }}</blockquote>
              <p class="task-card__meta">
                {{ daily.sentence.bookTitle ?? daily.sentence.attribution ?? 'Source-backed sentence' }}
                <span v-if="daily.sentence.pageStart"> · p.{{ daily.sentence.pageStart }}</span>
                <span v-else> · page unknown</span>
              </p>
              <div class="task-card__actions">
                <AppButton variant="primary" :disabled="!daily.sentence.sourceReference" @click="openDailySource('SENTENCE')">
                  Reflect
                </AppButton>
                <RouterLink to="/daily" custom v-slot="{ navigate }">
                  <AppButton variant="secondary" @click="navigate">Open Daily</AppButton>
                </RouterLink>
              </div>
            </template>
            <AppEmptyState
              v-else
              title="No source-backed quote yet"
              description="Convert a capture to a quote before BookOS can resurface real reading material here."
              compact
            >
              <template #actions>
                <RouterLink to="/captures/inbox" custom v-slot="{ navigate }">
                  <AppButton variant="secondary" @click="navigate">Open capture inbox</AppButton>
                </RouterLink>
              </template>
            </AppEmptyState>
          </AppCard>

          <AppCard class="task-card" as="article">
            <div class="task-card__header">
              <div>
                <p class="eyebrow">Daily prompt</p>
                <h3>What should I think about?</h3>
              </div>
              <AppBadge v-if="daily?.prompt?.templatePrompt" variant="warning" size="sm">Template</AppBadge>
              <AppBadge v-else-if="daily?.prompt" variant="success" size="sm">Source-backed</AppBadge>
            </div>
            <template v-if="daily?.prompt">
              <p class="task-card__body">{{ daily.prompt.question }}</p>
              <p class="task-card__meta">
                {{ daily.prompt.sourceTitle ?? daily.prompt.bookTitle ?? 'Template prompt' }}
                <span v-if="daily.prompt.sourceReference?.pageStart"> · p.{{ daily.prompt.sourceReference.pageStart }}</span>
              </p>
              <div class="task-card__actions">
                <RouterLink :to="projects.length ? '/projects' : '/projects/new'" custom v-slot="{ navigate }">
                  <AppButton variant="primary" @click="navigate">
                    {{ projects.length ? 'Apply to Project' : 'Create project' }}
                  </AppButton>
                </RouterLink>
                <RouterLink to="/review" custom v-slot="{ navigate }">
                  <AppButton variant="secondary" @click="navigate">Start Review</AppButton>
                </RouterLink>
              </div>
            </template>
            <AppErrorState
              v-else-if="dailyError"
              title="Daily prompt unavailable"
              :description="dailyError"
              retry-label="Retry daily"
              @retry="loadDaily"
            />
            <AppLoadingState v-else label="Loading daily prompt" />
          </AppCard>
        </div>
      </section>

      <section class="dashboard-section dashboard-section--reading" aria-labelledby="continue-title">
        <AppSectionHeader
          eyebrow="Continue Reading"
          title="Continue Reading"
          description="Resume the book context that can anchor notes, captures, quotes, and source references."
          compact
        />
        <div v-if="continueBooks.length" class="reading-list">
          <AppCard v-for="book in continueBooks" :key="book.bookId" class="reading-card" as="article">
            <div class="reading-card__cover" aria-hidden="true">
              <img v-if="book.coverUrl" :src="book.coverUrl" alt="" />
              <span v-else>{{ initials(book.title) }}</span>
            </div>
            <div class="reading-card__body">
              <h3 id="continue-title">{{ book.title }}</h3>
              <p>{{ book.authors.length ? book.authors.join(', ') : 'Author unknown' }}</p>
              <AppProgressBar
                :value="book.progressPercent"
                :label="`${book.progressPercent}% read`"
                size="sm"
              />
              <p class="task-card__meta">Status: {{ readingStatusLabel(book.readingStatus) }}</p>
            </div>
            <div class="reading-card__actions">
              <RouterLink :to="{ name: 'book-detail', params: { id: book.bookId } }" custom v-slot="{ navigate }">
                <AppButton variant="primary" @click="navigate">Open book</AppButton>
              </RouterLink>
              <AppButton variant="secondary" @click="prepareCaptureForBook(book.bookId)">Capture note</AppButton>
            </div>
          </AppCard>
        </div>
        <AppEmptyState
          v-else
          title="No current reading book"
          description="Add a book to your library and mark it currently reading to make this dashboard practical."
          compact
        >
          <template #actions>
            <RouterLink to="/books/new" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Add a book</AppButton>
            </RouterLink>
            <RouterLink to="/my-library" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Open library</AppButton>
            </RouterLink>
          </template>
        </AppEmptyState>
      </section>

      <section class="dashboard-section dashboard-section--capture" aria-labelledby="capture-title">
        <AppCard class="quick-capture-card" as="section">
          <div class="quick-capture-card__intro">
            <p class="eyebrow">Quick Capture</p>
            <div class="help-heading-row">
              <h2 id="capture-title">Capture one reading thought</h2>
              <HelpTooltip topic="quick-capture" />
            </div>
            <p>Use emoji, page markers, tags, and [[Concept]] links. BookOS parses the saved capture and preserves source context.</p>
          </div>

          <form class="capture-form" @submit.prevent="saveQuickCapture">
            <label class="form-label" for="dashboard-capture-book">Current book</label>
            <select
              id="dashboard-capture-book"
              v-model.number="captureBookId"
              class="book-select"
              :disabled="!captureBookOptions.length"
            >
              <option :value="0">Select a book</option>
              <option v-for="book in captureBookOptions" :key="book.id" :value="book.id">
                {{ book.title }}
              </option>
            </select>
            <p v-if="!captureBookId" class="capture-warning" role="status">
              Choose a book before saving so BookOS can preserve the source reference. Unknown pages will stay null.
            </p>

            <label class="form-label" for="dashboard-capture-text">Capture text</label>
            <textarea
              id="dashboard-capture-text"
              v-model="captureText"
              class="capture-textarea"
              rows="4"
              placeholder="&#x1F4A1; p.12 This could become a core loop [[Core Loop]] #prototype"
            />
            <p class="capture-hint">
              Examples: &#x1F4AC; p.42 quote text #quote [[Concept]] · &#x2705; page 80 action item #todo
            </p>

            <div class="capture-form__actions">
              <AppButton variant="primary" native-type="submit" :loading="captureSaving" :disabled="!canSaveCapture">
                Save capture
              </AppButton>
              <RouterLink to="/captures/inbox" custom v-slot="{ navigate }">
                <AppButton variant="secondary" @click="navigate">Open inbox</AppButton>
              </RouterLink>
            </div>
          </form>

          <AppErrorState
            v-if="captureError"
            class="capture-feedback"
            title="Capture was not saved"
            :description="captureError"
            compact
          />

          <AppCard v-if="lastCapture" class="parsed-preview" variant="muted" as="article">
            <p class="eyebrow">Parsed preview</p>
            <h3>{{ noteTypeLabel(lastCapture.parsedType) }}</h3>
            <p>{{ lastCapture.cleanText || lastCapture.rawText }}</p>
            <div class="inline-meta">
              <AppBadge variant="primary" size="sm">{{ lastCapture.pageStart ? `p.${lastCapture.pageStart}` : 'page unknown' }}</AppBadge>
              <AppBadge v-for="tag in lastCapture.tags" :key="tag" variant="neutral" size="sm">#{{ tag }}</AppBadge>
              <AppBadge v-for="concept in lastCapture.concepts" :key="concept" variant="accent" size="sm">[[{{ concept }}]]</AppBadge>
            </div>
          </AppCard>
        </AppCard>
      </section>

      <section class="dashboard-section dashboard-section--inbox" aria-labelledby="inbox-title">
        <AppSectionHeader
          eyebrow="Inbox"
          title="Inbox"
          description="Process unconverted captures before they become another pile of raw notes."
          compact
        >
          <template #actions>
            <HelpTooltip topic="capture-inbox" placement="left" />
          </template>
        </AppSectionHeader>
        <div v-if="workflowLoading" class="dashboard-loading">
          <AppLoadingState label="Loading inbox" />
        </div>
        <div v-else-if="inboxCaptures.length" class="inbox-list">
          <AppCard v-for="capture in inboxCaptures.slice(0, 4)" :key="capture.id" class="inbox-card" as="article">
            <div>
              <div class="inline-meta">
                <AppBadge variant="primary" size="sm">{{ noteTypeLabel(capture.parsedType) }}</AppBadge>
                <AppBadge variant="accent" size="sm">{{ pageLabel(capture.pageStart, capture.pageEnd) }}</AppBadge>
              </div>
              <h3>{{ capture.cleanText || capture.rawText }}</h3>
              <p>{{ capture.bookTitle }}</p>
              <div class="inline-meta">
                <AppBadge v-for="tag in capture.tags" :key="tag" variant="neutral" size="sm">#{{ tag }}</AppBadge>
                <AppBadge v-for="concept in capture.concepts" :key="concept" variant="accent" size="sm">[[{{ concept }}]]</AppBadge>
              </div>
            </div>
            <div class="inbox-card__actions">
              <RouterLink :to="{ name: 'capture-inbox', query: { bookId: capture.bookId } }" custom v-slot="{ navigate }">
                <AppButton variant="primary" @click="navigate">{{ captureSuggestion(capture) }}</AppButton>
              </RouterLink>
              <RouterLink :to="{ name: 'book-detail', params: { id: capture.bookId } }" custom v-slot="{ navigate }">
                <AppButton variant="ghost" @click="navigate">Open source</AppButton>
              </RouterLink>
            </div>
          </AppCard>
        </div>
        <AppEmptyState
          v-else
          title="Capture inbox is clear"
          description="When you save raw thoughts from Quick Capture, BookOS will show suggested conversions here."
          compact
        />
      </section>

      <section class="dashboard-section dashboard-section--apply" aria-labelledby="apply-title">
        <AppSectionHeader
          eyebrow="Apply Knowledge"
          title="Apply Knowledge"
          description="Move one source-backed idea from reading into a project, decision, review, or action."
          compact
        >
          <template #actions>
            <HelpTooltip topic="project-application" placement="left" />
          </template>
        </AppSectionHeader>
        <AppCard v-if="recentKnowledgeTarget" class="knowledge-card" as="article">
          <div>
            <p class="eyebrow">{{ recentKnowledgeTarget.typeLabel }}</p>
            <h3 id="apply-title">{{ recentKnowledgeTarget.title }}</h3>
            <p>{{ recentKnowledgeTarget.excerpt }}</p>
            <p class="task-card__meta">{{ recentKnowledgeTarget.sourceLabel }}</p>
          </div>
          <div class="knowledge-card__actions">
            <RouterLink :to="projects.length ? recentKnowledgeTarget.route : '/projects/new'" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">
                {{ projects.length ? 'Apply to Project' : 'Create project' }}
              </AppButton>
            </RouterLink>
            <RouterLink :to="recentKnowledgeTarget.route" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Open item</AppButton>
            </RouterLink>
          </div>
        </AppCard>
        <AppEmptyState
          v-else
          title="No source-backed knowledge yet"
          description="Convert captures into quotes, actions, or reviewed concepts before applying reading knowledge to projects."
          compact
        >
          <template #actions>
            <RouterLink to="/captures/inbox" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Open capture inbox</AppButton>
            </RouterLink>
          </template>
        </AppEmptyState>
      </section>

      <section class="dashboard-section dashboard-section--project" aria-labelledby="project-title">
        <AppSectionHeader
          eyebrow="Project Focus"
          title="Project Focus"
          description="Keep source-backed reading connected to active game design work."
          compact
        />
        <AppCard v-if="selectedProject" class="project-focus-card" as="article">
          <div>
            <p class="eyebrow">{{ selectedProject.stage }}</p>
            <h3 id="project-title">{{ selectedProject.title }}</h3>
            <p>{{ selectedProject.description || 'No project description yet.' }}</p>
            <AppProgressBar
              :value="selectedProject.progressPercent"
              :label="`${selectedProject.progressPercent}% project progress`"
              size="sm"
            />
          </div>
          <dl class="project-focus-card__metrics">
            <div>
              <dt>Open problems</dt>
              <dd>{{ openProjectProblems.length }}</dd>
            </div>
            <div>
              <dt>Applications</dt>
              <dd>{{ selectedProjectApplications.length }}</dd>
            </div>
            <div>
              <dt>Recent source links</dt>
              <dd>{{ selectedProjectApplications.filter((item) => item.sourceReference).length }}</dd>
            </div>
          </dl>
          <div class="project-focus-card__actions">
            <RouterLink :to="{ name: 'project-detail', params: { id: selectedProject.id } }" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Open Project Cockpit</AppButton>
            </RouterLink>
            <RouterLink :to="{ name: 'project-problems', params: { id: selectedProject.id } }" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Review problems</AppButton>
            </RouterLink>
          </div>
        </AppCard>
        <AppEmptyState
          v-else
          title="No active project yet"
          description="Create a project when you are ready to turn reading into design decisions and playtest work."
          compact
        >
          <template #actions>
            <RouterLink to="/projects/new" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Create project</AppButton>
            </RouterLink>
          </template>
        </AppEmptyState>
      </section>

      <section class="dashboard-section dashboard-section--review" aria-labelledby="review-title">
        <AppSectionHeader
          eyebrow="Learning Loop"
          title="Learning Loop"
          description="Review source-backed items and keep mastery honest instead of inventing scores."
          compact
        >
          <template #actions>
            <HelpTooltip topic="review-session" placement="left" />
          </template>
        </AppSectionHeader>
        <AppCard class="review-card" as="article">
          <div>
            <p class="eyebrow">Review</p>
            <h3 id="review-title">{{ learningLoopTitle }}</h3>
            <p>{{ learningLoopDescription }}</p>
          </div>
          <div class="review-card__metrics">
            <AppBadge variant="primary" size="sm">{{ reviewSessions.length }} review sessions</AppBadge>
            <AppBadge variant="accent" size="sm">{{ mastery.length }} mastery targets</AppBadge>
          </div>
          <div class="review-card__actions">
            <RouterLink to="/review" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Start Review</AppButton>
            </RouterLink>
            <RouterLink v-if="weakMasteryRoute" :to="weakMasteryRoute" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Open weak item</AppButton>
            </RouterLink>
          </div>
        </AppCard>
      </section>

      <UseCaseSuggestionPanel
        title="Learn this dashboard through a workflow"
        description="Use these scenario guides when you need a concrete path instead of another module list."
        :slugs="dashboardUseCaseSlugs"
      />

      <section v-if="showAdvancedModules" class="dashboard-section dashboard-section--advanced" aria-labelledby="advanced-title">
        <AppSectionHeader
          eyebrow="Advanced Mode"
          title="Advanced tools"
          description="Graph, import/export, and AI are available, but they stay secondary unless you chose Advanced Mode."
          compact
        />
        <div class="advanced-grid">
          <RouterLink to="/graph" class="advanced-link">Open Graph</RouterLink>
          <RouterLink to="/import-export" class="advanced-link">Import / Export</RouterLink>
          <RouterLink to="/use-cases/mock-ai-draft-helper" class="advanced-link">Mock AI safety workflow</RouterLink>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getActionItems } from '../api/actionItems'
import { getBooks } from '../api/books'
import { createCapture, getCaptureInbox } from '../api/captures'
import { getDailyToday } from '../api/daily'
import { getConcepts, getKnowledgeObjects } from '../api/knowledge'
import { getMastery, getReviewSessions } from '../api/learning'
import { getProjectApplications, getProjectProblems, getProjects } from '../api/projects'
import { getQuotes } from '../api/quotes'
import { getCurrentlyReading, getUserBooks } from '../api/userBooks'
import type {
  ActionItemRecord,
  BookRecord,
  ConceptRecord,
  DailyTarget,
  DailyTodayRecord,
  GameProjectRecord,
  KnowledgeMasteryRecord,
  KnowledgeObjectRecord,
  NoteBlockType,
  ProjectApplicationRecord,
  ProjectProblemRecord,
  QuoteRecord,
  RawCaptureRecord,
  ReviewSessionRecord,
  StartingMode,
  UserBookRecord,
} from '../types'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppProgressBar from '../components/ui/AppProgressBar.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import { useOpenSource } from '../composables/useOpenSource'
import { useAuthStore } from '../stores/auth'

type DashboardRouteTarget = string | { name: string; params?: Record<string, number | string>; query?: Record<string, number | string> }

interface NextActionPlan {
  title: string
  description: string
  primaryLabel: string
  primaryTo: DashboardRouteTarget
  secondaryLabel?: string
  secondaryTo?: DashboardRouteTarget
}

interface ModeGuide {
  eyebrow: string
  description: string
}

interface CaptureBookOption {
  id: number
  title: string
}

interface KnowledgeTarget {
  typeLabel: string
  title: string
  excerpt: string
  sourceLabel: string
  route: DashboardRouteTarget
}

const auth = useAuthStore()
const { openSource } = useOpenSource()

const books = ref<BookRecord[]>([])
const library = ref<UserBookRecord[]>([])
const currentlyReading = ref<UserBookRecord[]>([])
const daily = ref<DailyTodayRecord | null>(null)
const inboxCaptures = ref<RawCaptureRecord[]>([])
const quotes = ref<QuoteRecord[]>([])
const actionItems = ref<ActionItemRecord[]>([])
const concepts = ref<ConceptRecord[]>([])
const knowledgeObjects = ref<KnowledgeObjectRecord[]>([])
const projects = ref<GameProjectRecord[]>([])
const projectApplications = ref<ProjectApplicationRecord[]>([])
const projectProblems = ref<ProjectProblemRecord[]>([])
const reviewSessions = ref<ReviewSessionRecord[]>([])
const mastery = ref<KnowledgeMasteryRecord[]>([])

const loading = ref(true)
const workflowLoading = ref(false)
const captureSaving = ref(false)
const error = ref('')
const dailyError = ref('')
const captureError = ref('')
const captureText = ref('')
const captureBookId = ref(0)
const lastCapture = ref<RawCaptureRecord | null>(null)

const dashboardUseCaseSlugs = computed(() => {
  if (dashboardMode.value === 'game_designer') {
    return ['apply-quote-to-game-project', 'run-design-lens-review', 'create-playtest-finding']
  }
  if (dashboardMode.value === 'researcher') {
    return ['review-concept-marker', 'search-rediscover-knowledge', 'inspect-knowledge-graph']
  }
  if (dashboardMode.value === 'advanced') {
    return ['inspect-knowledge-graph', 'export-reading-knowledge', 'mock-ai-draft-helper']
  }
  return ['track-book-start-to-finish', 'capture-idea-while-reading', 'capture-to-quote']
})

const dashboardMode = computed(() => {
  const mode = auth.user?.preferredDashboardMode ?? auth.user?.startingMode ?? 'READER'
  return String(mode).toLowerCase()
})

const modeGuide = computed<ModeGuide>(() => {
  const mode = (auth.user?.startingMode ?? 'READER') as StartingMode
  const guides: Record<StartingMode, ModeGuide> = {
    READER: {
      eyebrow: 'Reader Mode',
      description: 'Today is organized around continuing a book, capturing one note, and processing the inbox.',
    },
    NOTE_TAKER: {
      eyebrow: 'Note-Taker Mode',
      description: 'Today is organized around fast capture, note conversion, and source-backed review.',
    },
    GAME_DESIGNER: {
      eyebrow: 'Game Designer Mode',
      description: 'Today is organized around applying reading knowledge to a project and its next design problem.',
    },
    RESEARCHER: {
      eyebrow: 'Researcher Mode',
      description: 'Today is organized around concepts, review, and source-backed knowledge objects.',
    },
    COMMUNITY: {
      eyebrow: 'Community Mode',
      description: 'Today is organized around source-backed discussion after you capture or review an idea.',
    },
    ADVANCED: {
      eyebrow: 'Advanced Mode',
      description: 'Today still starts with a task, with graph, import/export, and AI kept available but secondary.',
    },
  }
  return guides[mode] ?? guides.READER
})

const continueBooks = computed(() => {
  if (currentlyReading.value.length) return currentlyReading.value.slice(0, 3)
  return library.value.slice(0, 3)
})

const captureBookOptions = computed<CaptureBookOption[]>(() => {
  const options = new Map<number, string>()
  for (const book of currentlyReading.value) options.set(book.bookId, book.title)
  for (const book of library.value) options.set(book.bookId, book.title)
  for (const book of books.value) options.set(book.id, book.title)
  return Array.from(options.entries()).map(([id, title]) => ({ id, title }))
})

const canSaveCapture = computed(() => Boolean(captureBookId.value && captureText.value.trim()))
const sourceBackedQuotes = computed(() => quotes.value.filter((quote) => quote.sourceReference))
const sourceBackedConcepts = computed(() => concepts.value.filter((concept) => concept.firstSourceReference || concept.sourceReferences.length))
const selectedProject = computed(() => projects.value.find((project) => !project.archivedAt) ?? projects.value[0] ?? null)
const selectedProjectApplications = computed(() =>
  selectedProject.value ? projectApplications.value.filter((application) => application.projectId === selectedProject.value?.id) : [],
)
const openProjectProblems = computed(() =>
  projectProblems.value.filter((problem) => !['DONE', 'RESOLVED', 'CLOSED'].includes(problem.status.toUpperCase())),
)
const showAdvancedModules = computed(() => dashboardMode.value === 'advanced')

const recentKnowledgeTarget = computed<KnowledgeTarget | null>(() => {
  const quote = sourceBackedQuotes.value[0] ?? quotes.value[0]
  if (quote) {
    return {
      typeLabel: 'Recent quote',
      title: quote.text,
      excerpt: quote.bookTitle,
      sourceLabel: quote.pageStart ? `p.${quote.pageStart}` : 'page unknown',
      route: { name: 'quote-detail', params: { id: quote.id } },
    }
  }

  const concept = sourceBackedConcepts.value[0] ?? concepts.value[0]
  if (concept) {
    return {
      typeLabel: 'Recent concept',
      title: concept.name,
      excerpt: concept.description || 'No concept description yet.',
      sourceLabel: concept.bookTitle ?? 'Source context unavailable',
      route: { name: 'concept-detail', params: { id: concept.id } },
    }
  }

  const knowledge = knowledgeObjects.value[0]
  if (knowledge) {
    return {
      typeLabel: knowledge.type.replaceAll('_', ' '),
      title: knowledge.title,
      excerpt: knowledge.description || 'No knowledge-object description yet.',
      sourceLabel: knowledge.bookTitle ?? knowledge.sourceReference?.locationLabel ?? 'Source context unavailable',
      route: { name: 'knowledge-detail', params: { id: knowledge.id } },
    }
  }

  return null
})

const weakMastery = computed(() => {
  return [...mastery.value].sort((a, b) => a.familiarityScore + a.usefulnessScore - (b.familiarityScore + b.usefulnessScore))[0] ?? null
})

const weakMasteryRoute = computed<DashboardRouteTarget | null>(() => {
  if (!weakMastery.value) return null
  if (weakMastery.value.targetType === 'CONCEPT') return { name: 'concept-detail', params: { id: weakMastery.value.targetId } }
  if (weakMastery.value.targetType === 'KNOWLEDGE_OBJECT') return { name: 'knowledge-detail', params: { id: weakMastery.value.targetId } }
  if (weakMastery.value.targetType === 'QUOTE') return { name: 'quote-detail', params: { id: weakMastery.value.targetId } }
  if (weakMastery.value.targetType === 'ACTION_ITEM') return { name: 'action-item-detail', params: { id: weakMastery.value.targetId } }
  return '/review'
})

const learningLoopTitle = computed(() => {
  if (weakMastery.value) return `${weakMastery.value.targetType.replaceAll('_', ' ')} needs review`
  if (reviewSessions.value.length) return 'Continue your review habit'
  return 'Create your first review session'
})

const learningLoopDescription = computed(() => {
  if (weakMastery.value) {
    return `Lowest current score: familiarity ${weakMastery.value.familiarityScore}, usefulness ${weakMastery.value.usefulnessScore}.`
  }
  if (reviewSessions.value.length) return 'You have review sessions. Keep the loop active by answering source-backed items.'
  return 'Once you have notes, quotes, concepts, or projects, start a review session to turn reading into retained knowledge.'
})

const nextActionPlan = computed<NextActionPlan>(() => {
  if (!library.value.length) {
    return {
      title: 'Add one real book',
      description: 'BookOS becomes useful only after there is a source book to anchor captures, quotes, concepts, and projects.',
      primaryLabel: 'Add a book',
      primaryTo: '/books/new',
      secondaryLabel: 'Browse use cases',
      secondaryTo: '/use-cases',
    }
  }

  if (inboxCaptures.value.length) {
    return {
      title: `Process ${inboxCaptures.value.length} inbox capture${inboxCaptures.value.length === 1 ? '' : 's'}`,
      description: 'Convert raw captures into notes, quotes, actions, or reviewed concepts before adding more knowledge.',
      primaryLabel: 'Open capture inbox',
      primaryTo: '/captures/inbox',
      secondaryLabel: 'Capture another thought',
      secondaryTo: selectedBookRoute(),
    }
  }

  if (!quotes.value.length && !concepts.value.length && !actionItems.value.length) {
    return {
      title: 'Capture one source-backed thought',
      description: 'You have reading context, but no extracted knowledge yet. Open a book or use Quick Capture below.',
      primaryLabel: 'Open current book',
      primaryTo: selectedBookRoute(),
      secondaryLabel: 'Open library',
      secondaryTo: '/my-library',
    }
  }

  if (projects.value.length && !projectApplications.value.length && recentKnowledgeTarget.value) {
    return {
      title: 'Apply one reading insight to a project',
      description: 'You have source-backed knowledge and a project, but no project applications yet.',
      primaryLabel: 'Apply to Project',
      primaryTo: recentKnowledgeTarget.value.route,
      secondaryLabel: 'Open project',
      secondaryTo: selectedProject.value ? { name: 'project-detail', params: { id: selectedProject.value.id } } : '/projects',
    }
  }

  if (!projects.value.length && (quotes.value.length || concepts.value.length || knowledgeObjects.value.length)) {
    return {
      title: 'Create a project for your reading knowledge',
      description: 'You have ideas to apply. Create a project so BookOS can turn reading into design action.',
      primaryLabel: 'Create project',
      primaryTo: '/projects/new',
      secondaryLabel: 'Open quotes',
      secondaryTo: '/quotes',
    }
  }

  return {
    title: 'Start a source-backed review',
    description: 'You have enough real material. Review or apply it before opening advanced modules.',
    primaryLabel: 'Start Review',
    primaryTo: '/review',
    secondaryLabel: selectedProject.value ? 'Open Project Cockpit' : 'Open concepts',
    secondaryTo: selectedProject.value ? { name: 'project-detail', params: { id: selectedProject.value.id } } : '/concepts',
  }
})

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const [catalog, userLibrary, current] = await Promise.all([getBooks(), getUserBooks(), getCurrentlyReading()])
    books.value = catalog
    library.value = userLibrary
    currentlyReading.value = current
    setDefaultCaptureBook()
    await Promise.all([loadDaily(), loadWorkflowSignals()])
  } catch {
    error.value = 'Check the API connection and try again.'
  } finally {
    loading.value = false
  }
}

async function loadDaily() {
  dailyError.value = ''
  try {
    daily.value = await getDailyToday()
  } catch {
    daily.value = null
    dailyError.value = 'Daily quote and prompt data could not be loaded. The dashboard will not invent daily content.'
  }
}

async function loadWorkflowSignals() {
  workflowLoading.value = true
  try {
    const [inbox, quoteList, actionList, conceptList, knowledgeList, projectList, reviewList, masteryList] = await Promise.all([
      getCaptureInbox(),
      getQuotes(),
      getActionItems(),
      getConcepts(),
      getKnowledgeObjects(),
      getProjects(),
      getReviewSessions(),
      getMastery(),
    ])

    inboxCaptures.value = inbox
    quotes.value = quoteList
    actionItems.value = actionList
    concepts.value = conceptList
    knowledgeObjects.value = knowledgeList
    projects.value = projectList
    reviewSessions.value = reviewList
    mastery.value = masteryList

    const activeProject = projectList.find((project) => !project.archivedAt) ?? projectList[0]
    if (activeProject) {
      const [applications, problems] = await Promise.all([getProjectApplications(activeProject.id), getProjectProblems(activeProject.id)])
      projectApplications.value = applications
      projectProblems.value = problems
    } else {
      projectApplications.value = []
      projectProblems.value = []
    }
  } catch {
    ElMessage.warning('Some workflow signals could not be loaded. Dashboard sections will show honest empty states.')
    inboxCaptures.value = []
    quotes.value = []
    actionItems.value = []
    concepts.value = []
    knowledgeObjects.value = []
    projects.value = []
    projectApplications.value = []
    projectProblems.value = []
    reviewSessions.value = []
    mastery.value = []
  } finally {
    workflowLoading.value = false
  }
}

async function saveQuickCapture() {
  if (!canSaveCapture.value) return
  captureSaving.value = true
  captureError.value = ''
  try {
    const capture = await createCapture({
      bookId: captureBookId.value,
      rawText: captureText.value.trim(),
    })
    lastCapture.value = capture
    inboxCaptures.value = [capture, ...inboxCaptures.value.filter((item) => item.id !== capture.id)]
    captureText.value = ''
    ElMessage.success('Capture saved and parsed.')
  } catch {
    captureError.value = 'BookOS could not save this capture. Check the selected book and backend connection.'
  } finally {
    captureSaving.value = false
  }
}

function prepareCaptureForBook(bookId: number) {
  captureBookId.value = bookId
  document.getElementById('dashboard-capture-text')?.focus()
}

function setDefaultCaptureBook() {
  if (captureBookId.value) return
  captureBookId.value = currentlyReading.value[0]?.bookId ?? library.value[0]?.bookId ?? books.value[0]?.id ?? 0
}

function selectedBookRoute(): DashboardRouteTarget {
  const id = captureBookId.value || currentlyReading.value[0]?.bookId || library.value[0]?.bookId || books.value[0]?.id
  return id ? { name: 'book-detail', params: { id } } : '/my-library'
}

function openDailySource(target: DailyTarget) {
  const source = target === 'SENTENCE' ? daily.value?.sentence?.sourceReference : daily.value?.prompt.sourceReference
  if (!source) {
    ElMessage.info('This daily item does not have a source reference.')
    return
  }

  void openSource({
    sourceType: target === 'SENTENCE' ? 'DAILY_SENTENCE' : 'DAILY_PROMPT',
    sourceId: target === 'SENTENCE' ? daily.value?.sentence?.id ?? source.id : daily.value?.prompt.id ?? source.id,
    bookId: source.bookId,
    bookTitle: target === 'SENTENCE' ? daily.value?.sentence?.bookTitle ?? undefined : daily.value?.prompt.bookTitle ?? undefined,
    pageStart: source.pageStart,
    noteId: source.noteId ?? undefined,
    rawCaptureId: source.rawCaptureId ?? undefined,
    noteBlockId: source.noteBlockId ?? undefined,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}

function captureSuggestion(capture: RawCaptureRecord) {
  if (capture.parsedType === 'QUOTE') return 'Convert to Quote'
  if (capture.parsedType === 'ACTION_ITEM' || capture.parsedType === 'ACTION') return 'Convert to Action'
  if (capture.concepts.length) return 'Review Concepts'
  return 'Convert to Note'
}

function noteTypeLabel(type: NoteBlockType) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function pageLabel(pageStart: number | null, pageEnd: number | null) {
  if (pageStart === null) return 'page unknown'
  if (pageEnd !== null && pageEnd !== pageStart) return `p.${pageStart}-${pageEnd}`
  return `p.${pageStart}`
}

function readingStatusLabel(status: string) {
  return status
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function initials(title: string) {
  return title
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: var(--space-5);
}

.dashboard-hero {
  display: flex;
  justify-content: space-between;
  gap: var(--space-5);
  align-items: flex-start;
  padding: var(--space-6);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at 18% 0%, color-mix(in srgb, var(--bookos-accent-soft) 70%, transparent), transparent 34%),
    linear-gradient(135deg, var(--bookos-surface), color-mix(in srgb, var(--bookos-primary-soft) 42%, var(--bookos-surface)));
  box-shadow: var(--shadow-card);
}

.dashboard-hero h1,
.dashboard-hero p,
.next-step-card h2,
.next-step-card p,
.task-card h3,
.task-card p,
.reading-card h3,
.reading-card p,
.knowledge-card h3,
.knowledge-card p,
.project-focus-card h3,
.project-focus-card p,
.review-card h3,
.review-card p {
  margin: 0;
}

.dashboard-hero h1 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(2.1rem, 5vw, 4rem);
  letter-spacing: -0.04em;
}

.dashboard-hero p:not(.eyebrow) {
  margin-top: var(--space-2);
  color: var(--bookos-text-secondary);
  max-width: 58rem;
  line-height: var(--type-body-line);
}

.dashboard-hero__actions,
.next-step-card__actions,
.task-card__actions,
.reading-card__actions,
.capture-form__actions,
.inbox-card__actions,
.knowledge-card__actions,
.project-focus-card__actions,
.review-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.next-step-card {
  padding: var(--space-5);
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: center;
  border-color: color-mix(in srgb, var(--bookos-primary) 22%, var(--bookos-border));
}

.next-step-card h2 {
  font-family: var(--font-book-title);
  font-size: clamp(1.55rem, 3vw, 2.35rem);
}

.next-step-card p {
  margin-top: var(--space-2);
  color: var(--bookos-text-secondary);
  max-width: 58rem;
}

.dashboard-section {
  display: grid;
  gap: var(--space-3);
}

.today-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.task-card,
.quick-capture-card,
.knowledge-card,
.project-focus-card,
.review-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.task-card__header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
}

.task-card h3,
.reading-card h3,
.knowledge-card h3,
.project-focus-card h3,
.review-card h3,
.parsed-preview h3,
.quick-capture-card h2 {
  font-family: var(--font-book-title);
  font-size: 1.35rem;
  letter-spacing: -0.02em;
}

.task-card blockquote {
  margin: 0;
  padding-left: var(--space-4);
  border-left: 4px solid var(--bookos-accent);
  color: var(--bookos-text-primary);
  font-family: var(--font-book-title);
  font-size: 1.15rem;
  line-height: 1.45;
}

.task-card__body,
.task-card__meta,
.capture-hint,
.capture-warning,
.reading-card p,
.knowledge-card p,
.project-focus-card p,
.review-card p,
.parsed-preview p,
.inbox-card p {
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.capture-warning {
  margin: 0;
  color: var(--bookos-warning);
  font-weight: 800;
}

.reading-list,
.inbox-list {
  display: grid;
  gap: var(--space-3);
}

.reading-card {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--space-4);
  align-items: center;
}

.reading-card__cover {
  width: 4.5rem;
  height: 6.2rem;
  border-radius: var(--radius-md);
  background: linear-gradient(145deg, var(--bookos-primary), var(--bookos-accent));
  color: white;
  display: grid;
  place-items: center;
  font-family: var(--font-book-title);
  font-size: 1.35rem;
  font-weight: 900;
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.reading-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reading-card__body {
  display: grid;
  gap: var(--space-2);
}

.quick-capture-card {
  grid-template-columns: minmax(14rem, 0.8fr) minmax(18rem, 1.2fr);
  align-items: start;
}

.quick-capture-card__intro {
  display: grid;
  gap: var(--space-2);
}

.help-heading-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.help-heading-row h2 {
  margin: 0;
}

.capture-form {
  display: grid;
  gap: var(--space-2);
}

.form-label {
  color: var(--bookos-text-primary);
  font-size: var(--type-meta-size);
  font-weight: 900;
}

.book-select,
.capture-textarea {
  width: 100%;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  font: inherit;
}

.book-select {
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
}

.capture-textarea {
  min-height: 8.5rem;
  padding: var(--space-3);
  resize: vertical;
  line-height: var(--type-body-line);
}

.capture-feedback {
  grid-column: 1 / -1;
}

.parsed-preview {
  grid-column: 1 / -1;
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
}

.inline-meta {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: center;
}

.inbox-card {
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: flex-start;
}

.inbox-card h3 {
  margin: var(--space-2) 0 var(--space-1);
  font-size: 1rem;
}

.project-focus-card__metrics {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.project-focus-card__metrics div {
  padding: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
}

.project-focus-card__metrics dt {
  color: var(--bookos-text-secondary);
  font-size: var(--type-meta-size);
  font-weight: 900;
}

.project-focus-card__metrics dd {
  margin: var(--space-1) 0 0;
  color: var(--bookos-primary);
  font-size: 1.55rem;
  font-weight: 900;
}

.review-card__metrics {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.advanced-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.advanced-link {
  padding: var(--space-4);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  color: var(--bookos-primary);
  font-weight: 900;
  text-decoration: none;
}

.advanced-link:hover {
  border-color: var(--bookos-primary);
  background: var(--bookos-primary-soft);
}

@media (max-width: 980px) {
  .today-grid,
  .quick-capture-card,
  .advanced-grid {
    grid-template-columns: 1fr;
  }

  .reading-card {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .reading-card__actions {
    grid-column: 1 / -1;
  }
}

@media (max-width: 720px) {
  .dashboard-hero,
  .next-step-card,
  .inbox-card {
    flex-direction: column;
    align-items: stretch;
  }

  .reading-card {
    grid-template-columns: 1fr;
  }

  .reading-card__cover {
    width: 100%;
    height: 8rem;
  }

  .project-focus-card__metrics {
    grid-template-columns: 1fr;
  }
}
</style>
