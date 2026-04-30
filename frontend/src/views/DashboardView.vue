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
          <div class="dashboard-hero__support" aria-label="Secondary dashboard help">
            <button type="button" @click="focusModeSwitcher">Switch mode</button>
            <RouterLink to="/guided/first-loop">First loop</RouterLink>
            <RouterLink to="/demo">Practice in Demo</RouterLink>
            <RouterLink to="/onboarding?restart=1">Restart onboarding</RouterLink>
          </div>
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

      <AppCard id="dashboard-mode-switcher" class="mode-landing-card" as="section" aria-labelledby="mode-landing-title">
        <AppSectionHeader
          eyebrow="Focused landing"
          :title="`${currentModeLabel} landing`"
          :description="modeLanding.description"
          compact
        />
        <div class="mode-landing-card__why">
          <strong>Why am I seeing this?</strong>
          <span>{{ modeLanding.why }}</span>
        </div>
        <div class="mode-landing-grid">
          <AppCard v-for="item in modeLanding.items" :key="item.title" class="mode-task-card" variant="muted" as="article">
            <div>
              <p class="eyebrow">{{ item.eyebrow }}</p>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
              <p class="task-card__meta">{{ item.meta }}</p>
            </div>
            <RouterLink :to="item.to" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">{{ item.cta }}</AppButton>
            </RouterLink>
          </AppCard>
        </div>
        <div class="mode-switcher" role="group" aria-label="Switch dashboard mode">
          <button
            v-for="mode in navigationModes"
            :key="mode.value"
            type="button"
            :aria-pressed="mode.value === currentDashboardMode"
            :disabled="modeSaving || mode.value === currentDashboardMode"
            @click="setDashboardMode(mode.value)"
          >
            {{ mode.label }}
          </button>
        </div>
      </AppCard>

      <AppCard v-if="isEmptyUser" class="readiness-card" as="section" aria-labelledby="readiness-title">
        <div>
          <p class="eyebrow">First-day flow</p>
          <h2 id="readiness-title">Start with one book, one capture, and one source check.</h2>
          <p>
            BookOS will keep advanced tools out of the way until you have real reading material. You can still open them
            from More or Advanced Mode when you need them.
          </p>
        </div>
        <div class="readiness-card__actions">
          <RouterLink to="/books/new" custom v-slot="{ navigate }">
            <AppButton variant="primary" @click="navigate">Add Book</AppButton>
          </RouterLink>
          <RouterLink to="/guided/first-loop" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">First Valuable Loop</AppButton>
          </RouterLink>
          <RouterLink to="/demo" custom v-slot="{ navigate }">
            <AppButton variant="ghost" @click="navigate">Practice in Demo</AppButton>
          </RouterLink>
        </div>
      </AppCard>

      <section v-if="showTodaySection" class="dashboard-section dashboard-section--today" aria-labelledby="today-title">
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
                  <AppButton variant="secondary" @click="navigate">Process Captures</AppButton>
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

      <section v-if="showContinueReadingSection" class="dashboard-section dashboard-section--reading" aria-labelledby="continue-title">
        <AppSectionHeader
          eyebrow="Continue Reading"
          title="Continue Reading"
          description="Resume the book context that can anchor notes, captures, quotes, and source links."
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
            <RouterLink to="/guided/first-loop" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Start first loop</AppButton>
            </RouterLink>
            <RouterLink to="/my-library" custom v-slot="{ navigate }">
              <AppButton variant="ghost" @click="navigate">Open library</AppButton>
            </RouterLink>
          </template>
        </AppEmptyState>
      </section>

      <section v-if="showQuickCaptureSection" class="dashboard-section dashboard-section--capture" aria-labelledby="capture-title">
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
              Choose a book before saving so BookOS can preserve the source link. Unknown pages will stay null.
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
              Examples: &#x1F4AC; p.42 quote text #quote [[Concept]] · &#x2705; page 80 action #todo
            </p>

            <div v-if="showParserExamples" class="parser-examples" aria-label="Parser examples">
              <strong>Parser examples</strong>
              <span>&#x1F4AC; p.42 original quote text #quote [[Core Loop]]</span>
              <span>&#x2705; page 80 test this tomorrow #todo [[Feedback Loop]]</span>
              <span>&#x1F9E9; 第12页 connect this to [[Meaningful Choice]] #concept</span>
            </div>

            <div class="capture-form__actions">
              <AppButton variant="primary" native-type="submit" :loading="captureSaving" :disabled="!canSaveCapture">
                Save capture
              </AppButton>
              <RouterLink to="/captures/inbox" custom v-slot="{ navigate }">
                <AppButton variant="secondary" @click="navigate">Process Captures</AppButton>
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

      <section v-if="showInboxSection" class="dashboard-section dashboard-section--inbox" aria-labelledby="inbox-title">
        <AppSectionHeader
          eyebrow="Process Captures"
          title="Process Captures"
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
          title="No captures to process"
          description="When you save raw thoughts from Quick Capture, BookOS will show suggested conversions here."
          compact
        />
      </section>

      <section v-if="showApplySection" class="dashboard-section dashboard-section--apply" aria-labelledby="apply-title">
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
            <RouterLink v-if="showSourceBackedTools" to="/review" custom v-slot="{ navigate }">
              <AppButton variant="ghost" @click="navigate">Review Source</AppButton>
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
              <AppButton variant="primary" @click="navigate">Process Captures</AppButton>
            </RouterLink>
          </template>
        </AppEmptyState>
      </section>

      <section v-if="showProjectSection" class="dashboard-section dashboard-section--project" aria-labelledby="project-title">
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
            <RouterLink
              v-if="showProjectGraphLink"
              :to="{ name: 'graph-project', params: { projectId: selectedProject.id } }"
              custom
              v-slot="{ navigate }"
            >
              <AppButton variant="ghost" @click="navigate">Open Project Graph</AppButton>
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

      <section v-if="showLearningSection" class="dashboard-section dashboard-section--review" aria-labelledby="review-title">
        <AppSectionHeader
          eyebrow="Learning Loop"
          title="Learning Loop"
          description="Review source-backed items and keep learning progress honest instead of inventing scores."
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
            <AppBadge variant="accent" size="sm">{{ mastery.length }} progress targets</AppBadge>
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
        v-if="showUseCasePanel"
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
          <RouterLink to="/graph" class="advanced-link">Open Knowledge Graph</RouterLink>
          <RouterLink to="/import-export" class="advanced-link">Import / Export</RouterLink>
          <RouterLink to="/use-cases/mock-ai-draft-helper" class="advanced-link">Mock AI safety workflow</RouterLink>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
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
  SourceReferenceRecord,
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
import { navigationModeLabel, navigationModes, normalizeNavigationMode, type NavigationMode } from '../utils/navigationMode'

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

interface ModeLandingItem {
  eyebrow: string
  title: string
  description: string
  meta: string
  cta: string
  to: DashboardRouteTarget
}

interface ModeLanding {
  description: string
  why: string
  items: ModeLandingItem[]
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
const modeSaving = ref(false)

const currentDashboardMode = computed(() =>
  normalizeNavigationMode(auth.user?.preferredDashboardMode, auth.user?.startingMode),
)
const currentModeLabel = computed(() => navigationModeLabel(currentDashboardMode.value))

const dashboardUseCaseSlugs = computed(() => {
  if (currentDashboardMode.value === 'GAME_DESIGNER') {
    return ['apply-quote-to-game-project', 'run-design-lens-review', 'create-playtest-finding']
  }
  if (currentDashboardMode.value === 'RESEARCHER') {
    return ['review-concept-marker', 'search-rediscover-knowledge', 'inspect-knowledge-graph']
  }
  if (currentDashboardMode.value === 'ADVANCED') {
    return ['inspect-knowledge-graph', 'export-reading-knowledge', 'mock-ai-draft-helper']
  }
  if (currentDashboardMode.value === 'COMMUNITY') {
    return ['start-source-linked-discussion', 'open-source-from-quote-or-action', 'capture-idea-while-reading']
  }
  if (currentDashboardMode.value === 'NOTE_TAKER') {
    return ['note-taker-capture-convert', 'capture-to-action-item', 'open-source-from-quote-or-action']
  }
  return ['track-book-start-to-finish', 'capture-idea-while-reading', 'capture-to-quote']
})

const dashboardMode = computed(() => {
  return currentDashboardMode.value.toLowerCase()
})

const modeGuide = computed<ModeGuide>(() => {
  const guides: Record<NavigationMode, ModeGuide> = {
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
      description: 'Today is organized around concepts, review, and source-backed design knowledge.',
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
  return guides[currentDashboardMode.value] ?? guides.READER
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
const sourceBackedConcepts = computed(() =>
  concepts.value.filter(
    (concept) =>
      (concept.firstSourceReference && isUserSourceReference(concept.firstSourceReference)) ||
      concept.sourceReferences.some((source) => isUserSourceReference(source)),
  ),
)
const userKnowledgeObjects = computed(() =>
  knowledgeObjects.value.filter((knowledge) => knowledge.sourceReference && isUserSourceReference(knowledge.sourceReference)),
)
const selectedProject = computed(() => projects.value.find((project) => !project.archivedAt) ?? projects.value[0] ?? null)
const selectedProjectApplications = computed(() =>
  selectedProject.value ? projectApplications.value.filter((application) => application.projectId === selectedProject.value?.id) : [],
)
const openProjectProblems = computed(() =>
  projectProblems.value.filter((problem) => !['DONE', 'RESOLVED', 'CLOSED'].includes(problem.status.toUpperCase())),
)
const isEmptyUser = computed(() => library.value.length === 0)
const hasLibraryBooks = computed(() => library.value.length > 0)
const hasCaptures = computed(() => inboxCaptures.value.length > 0 || Boolean(lastCapture.value))
const convertedRecordCount = computed(() => quotes.value.length + actionItems.value.length + sourceBackedConcepts.value.length + userKnowledgeObjects.value.length)
const sourceBackedRecordCount = computed(
  () =>
    sourceBackedQuotes.value.length +
    sourceBackedConcepts.value.length +
    actionItems.value.filter((item) => item.sourceReference).length +
    userKnowledgeObjects.value.filter((item) => item.sourceReference).length,
)
const hasSourceBackedRecords = computed(() => sourceBackedRecordCount.value > 0)
const showTodaySection = computed(() => hasSourceBackedRecords.value || reviewSessions.value.length > 0 || dashboardMode.value === 'advanced')
const showContinueReadingSection = computed(() => hasLibraryBooks.value)
const showQuickCaptureSection = computed(() => hasLibraryBooks.value)
const showParserExamples = computed(() => hasLibraryBooks.value && !hasCaptures.value && convertedRecordCount.value === 0)
const showInboxSection = computed(() => hasCaptures.value)
const showApplySection = computed(() => convertedRecordCount.value > 0)
const showProjectSection = computed(() => Boolean(selectedProject.value))
const showProjectGraphLink = computed(() => selectedProjectApplications.value.some((application) => application.sourceReference))
const showLearningSection = computed(
  () => hasSourceBackedRecords.value || convertedRecordCount.value > 0 || reviewSessions.value.length > 0 || mastery.value.length > 0,
)
const showSourceBackedTools = computed(() => hasSourceBackedRecords.value)
const showUseCasePanel = computed(() => true)
const showAdvancedModules = computed(() => dashboardMode.value === 'advanced')

const modeLanding = computed<ModeLanding>(() => {
  const firstBookRoute = selectedBookRoute()
  const activeProjectRoute = selectedProject.value ? { name: 'project-detail', params: { id: selectedProject.value.id } } : '/projects/new'
  const sourceLinksMeta = `${sourceBackedRecordCount.value} source-backed record${sourceBackedRecordCount.value === 1 ? '' : 's'}`

  const landings: Record<NavigationMode, ModeLanding> = {
    READER: {
      description: 'A quiet reader-first path: resume a book, capture one thought, then review what matters.',
      why: 'You chose Reader Mode or have not selected another mode. BookOS keeps projects, graph, import, and assistant tools secondary until you ask for them.',
      items: [
        {
          eyebrow: 'Continue Reading',
          title: 'Continue Reading',
          description: continueBooks.value.length ? `Open ${continueBooks.value[0].title} before adding more notes.` : 'Add or resume a book before adding more notes.',
          meta: `${continueBooks.value.length} reading candidate${continueBooks.value.length === 1 ? '' : 's'}`,
          cta: continueBooks.value.length ? 'Open book' : 'Add Book',
          to: continueBooks.value.length ? firstBookRoute : '/books/new',
        },
        {
          eyebrow: 'Capture',
          title: 'Capture',
          description: 'Use Quick Capture to preserve book context, tags, concepts, and page markers.',
          meta: hasCaptures.value ? `${inboxCaptures.value.length} capture${inboxCaptures.value.length === 1 ? '' : 's'} waiting` : 'No pending captures',
          cta: hasLibraryBooks.value ? 'Capture now' : 'Start first loop',
          to: hasLibraryBooks.value ? firstBookRoute : '/guided/first-loop',
        },
        {
          eyebrow: 'Review',
          title: 'Review',
          description: 'Review only real source-backed records; no fake mastery prompts.',
          meta: `${reviewSessions.value.length} review session${reviewSessions.value.length === 1 ? '' : 's'}`,
          cta: 'Start Review',
          to: '/review',
        },
      ],
    },
    NOTE_TAKER: {
      description: 'A note-taking path: write, process, and reconnect notes to their source links.',
      why: 'Note-Taker Mode emphasizes the workbench where raw captures become notes, quotes, actions, and source-backed blocks.',
      items: [
        {
          eyebrow: 'Current Notes',
          title: 'Current Notes',
          description: 'Review existing notes or create a note from a book context.',
          meta: 'Notes stay useful when tied to books and blocks',
          cta: 'Open Notes',
          to: '/notes',
        },
        {
          eyebrow: 'Process Captures',
          title: 'Process Captures',
          description: 'Convert raw thoughts into notes, quotes, actions, or concept reviews.',
          meta: hasCaptures.value ? `${inboxCaptures.value.length} capture${inboxCaptures.value.length === 1 ? '' : 's'} waiting` : 'Capture first, then process',
          cta: 'Process Captures',
          to: '/captures/inbox',
        },
        {
          eyebrow: 'Source Links',
          title: 'Source Links',
          description: 'Open source-backed records and confirm page unknown stays unknown when no page was supplied.',
          meta: sourceLinksMeta,
          cta: sourceBackedRecordCount.value ? 'Open Quotes' : 'Create source-backed record',
          to: sourceBackedRecordCount.value ? '/quotes' : '/guided/first-loop',
        },
      ],
    },
    GAME_DESIGNER: {
      description: 'A game-design path: apply reading to a project, make decisions, and plan playtests.',
      why: 'Game Designer Mode promotes project application over browsing modules so reading turns into design action.',
      items: [
        {
          eyebrow: 'Active Project',
          title: 'Active Project',
          description: selectedProject.value ? `Keep ${selectedProject.value.title} visible so reading can become design work.` : 'Create a project cockpit so reading can become design work.',
          meta: selectedProject.value ? `${selectedProject.value.progressPercent}% progress` : 'No project yet',
          cta: selectedProject.value ? 'Open Project Cockpit' : 'Create Project',
          to: activeProjectRoute,
        },
        {
          eyebrow: 'Apply Knowledge',
          title: 'Apply Knowledge',
          description: recentKnowledgeTarget.value ? `Use ${recentKnowledgeTarget.value.title} as evidence for project applications.` : 'Use source-backed quotes, concepts, or design knowledge as evidence for project applications.',
          meta: `${projectApplications.value.length} project application${projectApplications.value.length === 1 ? '' : 's'}`,
          cta: recentKnowledgeTarget.value ? 'Apply Source' : 'Find source',
          to: recentKnowledgeTarget.value?.route ?? '/quotes',
        },
        {
          eyebrow: 'Playtest',
          title: 'Playtest',
          description: 'Plan or record findings after reading informs a project problem.',
          meta: openProjectProblems.value.length ? `${openProjectProblems.value.length} open problem${openProjectProblems.value.length === 1 ? '' : 's'}` : 'Create a problem first',
          cta: selectedProject.value ? 'Open Playtests' : 'Create Project',
          to: selectedProject.value ? { name: 'project-playtests', params: { id: selectedProject.value.id } } : '/projects/new',
        },
      ],
    },
    RESEARCHER: {
      description: 'A research path: build concepts, review them, and inspect relationships only when context exists.',
      why: 'Researcher Mode promotes concepts and review, with the Knowledge Graph available from context instead of dominating the empty dashboard.',
      items: [
        {
          eyebrow: 'Concepts',
          title: 'Concepts',
          description: 'Review [[Concept]] markers before saving them as durable knowledge.',
          meta: `${sourceBackedConcepts.value.length} source-backed concept${sourceBackedConcepts.value.length === 1 ? '' : 's'}`,
          cta: concepts.value.length ? 'Open Concepts' : 'Review Concept',
          to: concepts.value.length ? '/concepts' : '/captures/inbox',
        },
        {
          eyebrow: 'Review',
          title: 'Review',
          description: 'Use review sessions to make concepts and source-backed notes active again.',
          meta: `${mastery.value.length} progress target${mastery.value.length === 1 ? '' : 's'}`,
          cta: 'Start Review',
          to: '/review',
        },
        {
          eyebrow: 'Graph From Context',
          title: 'Graph From Context',
          description: 'Open the graph only when it can show real books, concepts, sources, or project links.',
          meta: sourceBackedRecordCount.value ? 'Real source context available' : 'No graph links yet',
          cta: 'Open Knowledge Graph',
          to: concepts.value[0] ? { name: 'graph-concept', params: { conceptId: concepts.value[0].id } } : '/graph',
        },
      ],
    },
    COMMUNITY: {
      description: 'A community path: create source-linked discussions instead of generic posts.',
      why: 'Community Mode keeps forum templates near books, quotes, concepts, and source context while private data remains protected by backend permissions.',
      items: [
        {
          eyebrow: 'Source-Linked Threads',
          title: 'Source-Linked Threads',
          description: 'Start from a quote, concept, book, or source link before opening a thread.',
          meta: sourceLinksMeta,
          cta: sourceBackedRecordCount.value ? 'Open Forum' : 'Create source first',
          to: sourceBackedRecordCount.value ? '/forum' : '/guided/first-loop',
        },
        {
          eyebrow: 'Forum Templates',
          title: 'Forum Templates',
          description: 'Use Book Discussion, Quote Discussion, Concept Discussion, or Project Critique templates.',
          meta: 'Templates prevent generic comment dumps',
          cta: 'New Thread',
          to: '/forum/new',
        },
        {
          eyebrow: 'Recent Context',
          title: 'Recent Context',
          description: 'Attach a book, quote, concept, project, or source link when possible.',
          meta: recentKnowledgeTarget.value?.sourceLabel ?? 'No recent source-backed context',
          cta: recentKnowledgeTarget.value ? 'Open source item' : 'Open Use Cases',
          to: recentKnowledgeTarget.value?.route ?? '/use-cases/start-source-linked-discussion',
        },
      ],
    },
    ADVANCED: {
      description: 'An advanced path: graph, import/export, Draft Assistant, and analytics are visible while still using real data only.',
      why: 'Advanced Mode expands advanced navigation and surfaces technical workflows without making them look complete when there is no data.',
      items: [
        {
          eyebrow: 'Knowledge Graph',
          title: 'Knowledge Graph',
          description: 'Inspect graph nodes and relationships generated from source references and manual links.',
          meta: sourceBackedRecordCount.value ? `${sourceBackedRecordCount.value} source-backed inputs` : 'Honest empty state if no links exist',
          cta: 'Open Graph',
          to: '/graph',
        },
        {
          eyebrow: 'Import / Export',
          title: 'Import / Export',
          description: 'Export user-owned data or preview imports before committing records.',
          meta: 'Preview before write; no fake pages',
          cta: 'Open Import / Export',
          to: '/import-export',
        },
        {
          eyebrow: 'Draft Assistant + Analytics',
          title: 'Draft Assistant + Analytics',
          description: 'MockAIProvider drafts remain editable suggestions; analytics uses real counts only.',
          meta: 'Draft-only assistant behavior',
          cta: 'Open AI workflow',
          to: '/use-cases/mock-ai-draft-helper',
        },
      ],
    },
  }

  return landings[currentDashboardMode.value] ?? landings.READER
})

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

  const concept = sourceBackedConcepts.value[0]
  if (concept) {
    return {
      typeLabel: 'Recent concept',
      title: concept.name,
      excerpt: concept.description || 'No concept description yet.',
      sourceLabel: concept.bookTitle ?? 'Source context unavailable',
      route: { name: 'concept-detail', params: { id: concept.id } },
    }
  }

  const knowledge = userKnowledgeObjects.value[0]
  if (knowledge) {
    return {
      typeLabel: knowledge.type.replaceAll('_', ' '),
      title: knowledge.title,
      excerpt: knowledge.description || 'No design knowledge description yet.',
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
      title: 'Complete the first valuable loop',
      description: 'Start with one real book, one capture, one processed record, and one source check before exploring modules.',
      primaryLabel: 'Start first loop',
      primaryTo: '/guided/first-loop',
      secondaryLabel: 'Add a book directly',
      secondaryTo: '/books/new',
    }
  }

  if (inboxCaptures.value.length) {
    return {
      title: `Process ${inboxCaptures.value.length} capture${inboxCaptures.value.length === 1 ? '' : 's'}`,
      description: 'Convert raw captures into notes, quotes, actions, or reviewed concepts before adding more knowledge.',
      primaryLabel: 'Process Captures',
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

async function setDashboardMode(mode: NavigationMode) {
  if (modeSaving.value || mode === currentDashboardMode.value) return
  modeSaving.value = true
  try {
    await auth.updateOnboarding({
      preferredDashboardMode: mode,
      onboardingCompleted: auth.user?.onboardingCompleted ?? true,
      primaryUseCase: auth.user?.primaryUseCase ?? null,
      startingMode: auth.user?.startingMode ?? mode,
    })
    ElMessage.success(`Dashboard changed to ${navigationModeLabel(mode)}.`)
  } catch {
    ElMessage.error('Dashboard mode could not be saved.')
  } finally {
    modeSaving.value = false
  }
}

async function focusModeSwitcher() {
  await nextTick()
  const target = document.getElementById('dashboard-mode-switcher')
  target?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  target?.querySelector<HTMLButtonElement>('.mode-switcher button:not([disabled])')?.focus()
}

function selectedBookRoute(): DashboardRouteTarget {
  const id = captureBookId.value || currentlyReading.value[0]?.bookId || library.value[0]?.bookId || books.value[0]?.id
  return id ? { name: 'book-detail', params: { id } } : '/my-library'
}

function isUserSourceReference(source: SourceReferenceRecord) {
  return Boolean(source.rawCaptureId || source.noteId || source.noteBlockId)
}

function openDailySource(target: DailyTarget) {
  const source = target === 'SENTENCE' ? daily.value?.sentence?.sourceReference : daily.value?.prompt.sourceReference
  if (!source) {
    ElMessage.info('This daily item does not have a source link.')
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

.dashboard-hero__actions {
  flex-direction: column;
  align-items: flex-end;
  min-width: 12rem;
}

.dashboard-hero__support {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: flex-end;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.dashboard-hero__support a {
  color: inherit;
  text-decoration: underline;
  text-decoration-style: dotted;
  text-underline-offset: 4px;
}

.dashboard-hero__support button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 800;
  text-decoration: underline;
  text-decoration-style: dotted;
  text-underline-offset: 4px;
  cursor: pointer;
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

.mode-landing-card {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
  border-color: color-mix(in srgb, var(--bookos-primary) 18%, var(--bookos-border));
}

.mode-landing-card:target,
.mode-landing-card:focus-within {
  box-shadow: var(--focus-ring);
}

.mode-landing-card__why {
  padding: var(--space-3);
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  align-items: baseline;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.mode-landing-card__why strong {
  color: var(--bookos-text-primary);
}

.mode-landing-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.mode-task-card {
  padding: var(--space-4);
  display: grid;
  align-content: space-between;
  gap: var(--space-4);
}

.mode-task-card h3,
.mode-task-card p {
  margin: 0;
}

.mode-task-card h3 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: 1.25rem;
  letter-spacing: -0.02em;
}

.mode-task-card p:not(.eyebrow) {
  margin-top: var(--space-2);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.mode-switcher {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.mode-switcher button {
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-sm);
  background: var(--bookos-surface);
  color: var(--bookos-text-primary);
  font-weight: 900;
  cursor: pointer;
}

.mode-switcher button[aria-pressed='true'] {
  border-color: color-mix(in srgb, var(--bookos-primary) 48%, var(--bookos-border));
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
}

.mode-switcher button:disabled {
  cursor: default;
  opacity: 0.82;
}

.readiness-card {
  padding: var(--space-5);
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: center;
  border-color: color-mix(in srgb, var(--bookos-accent) 28%, var(--bookos-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--bookos-accent-soft) 42%, var(--bookos-surface)), var(--bookos-surface));
}

.readiness-card h2,
.readiness-card p {
  margin: 0;
}

.readiness-card h2 {
  margin-top: var(--space-1);
  font-family: var(--font-book-title);
  font-size: clamp(1.45rem, 3vw, 2.1rem);
}

.readiness-card p:not(.eyebrow) {
  margin-top: var(--space-2);
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
  max-width: 60rem;
}

.readiness-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: flex-end;
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

.parser-examples {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-3);
  border: 1px dashed color-mix(in srgb, var(--bookos-primary) 34%, var(--bookos-border));
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--bookos-primary-soft) 48%, var(--bookos-surface));
  color: var(--bookos-text-secondary);
  font-size: var(--type-meta-size);
}

.parser-examples strong {
  color: var(--bookos-text-primary);
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
  .mode-landing-grid,
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
  .readiness-card,
  .inbox-card {
    flex-direction: column;
    align-items: stretch;
  }

  .dashboard-hero__actions,
  .dashboard-hero__support,
  .readiness-card__actions {
    align-items: stretch;
    justify-content: flex-start;
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
