import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const PublicLayout = () => import('../layouts/PublicLayout.vue')
const AppLayout = () => import('../layouts/AppLayout.vue')
const DashboardLayout = () => import('../layouts/DashboardLayout.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const OnboardingView = () => import('../views/OnboardingView.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const UseCasesView = () => import('../views/UseCasesView.vue')
const UseCaseDetailView = () => import('../views/UseCaseDetailView.vue')
const HelpView = () => import('../views/HelpView.vue')
const DemoWorkspaceView = () => import('../views/DemoWorkspaceView.vue')
const MyLibraryView = () => import('../views/MyLibraryView.vue')
const AddBookView = () => import('../views/AddBookView.vue')
const EditBookView = () => import('../views/EditBookView.vue')
const BookDetailView = () => import('../views/BookDetailView.vue')
const FiveStarBooksView = () => import('../views/FiveStarBooksView.vue')
const CurrentlyReadingView = () => import('../views/CurrentlyReadingView.vue')
const AntiLibraryView = () => import('../views/AntiLibraryView.vue')
const NotesView = () => import('../views/NotesView.vue')
const NoteDetailView = () => import('../views/NoteDetailView.vue')
const CaptureInboxView = () => import('../views/CaptureInboxView.vue')
const QuotesView = () => import('../views/QuotesView.vue')
const QuoteDetailView = () => import('../views/QuoteDetailView.vue')
const ActionItemsView = () => import('../views/ActionItemsView.vue')
const ActionItemDetailView = () => import('../views/ActionItemDetailView.vue')
const ConceptsView = () => import('../views/ConceptsView.vue')
const ConceptDetailView = () => import('../views/ConceptDetailView.vue')
const KnowledgeObjectsView = () => import('../views/KnowledgeObjectsView.vue')
const KnowledgeObjectDetailView = () => import('../views/KnowledgeObjectDetailView.vue')
const DailyView = () => import('../views/DailyView.vue')
const AnalyticsView = () => import('../views/AnalyticsView.vue')
const ReviewView = () => import('../views/ReviewView.vue')
const ReviewSessionView = () => import('../views/ReviewSessionView.vue')
const MasteryView = () => import('../views/MasteryView.vue')
const ImportExportView = () => import('../views/ImportExportView.vue')
const ProjectsView = () => import('../views/ProjectsView.vue')
const ProjectNewView = () => import('../views/ProjectNewView.vue')
const ProjectDetailView = () => import('../views/ProjectDetailView.vue')
const ProjectProblemsView = () => import('../views/ProjectProblemsView.vue')
const ProjectApplicationsView = () => import('../views/ProjectApplicationsView.vue')
const ProjectDecisionsView = () => import('../views/ProjectDecisionsView.vue')
const ProjectApplyKnowledgeWizardView = () => import('../views/ProjectApplyKnowledgeWizardView.vue')
const ProjectPlaytestsView = () => import('../views/ProjectPlaytestsView.vue')
const ProjectLensReviewsView = () => import('../views/ProjectLensReviewsView.vue')
const GraphView = () => import('../views/GraphView.vue')
const AdminOntologyView = () => import('../views/AdminOntologyView.vue')
const ForumView = () => import('../views/ForumView.vue')
const ForumNewThreadView = () => import('../views/ForumNewThreadView.vue')
const ForumThreadView = () => import('../views/ForumThreadView.vue')
const NotFoundView = () => import('../views/NotFoundView.vue')

const APP_TITLE = 'BookOS'

function resolveRedirectTarget(value: unknown) {
  if (typeof value !== 'string') {
    return null
  }

  if (!value.startsWith('/') || value.startsWith('//')) {
    return null
  }

  return value
}

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, top: 80 }
    }

    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      component: PublicLayout,
      children: [
        { path: '', redirect: { name: 'login' } },
        { path: 'login', name: 'login', component: LoginView, meta: { public: true, title: 'Login' } },
        { path: 'register', name: 'register', component: RegisterView, meta: { public: true, title: 'Register' } },
      ],
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'onboarding',
          name: 'onboarding',
          component: OnboardingView,
          meta: { requiresAuth: true, title: 'Onboarding' },
        },
        {
          path: 'dashboard',
          component: DashboardLayout,
          meta: { requiresAuth: true, title: 'Dashboard' },
          children: [{ path: '', name: 'dashboard', component: DashboardView, meta: { title: 'Dashboard' } }],
        },
        {
          path: 'use-cases',
          name: 'use-cases',
          component: UseCasesView,
          meta: { requiresAuth: true, title: 'Use Cases' },
        },
        {
          path: 'use-cases/:slug',
          name: 'use-case-detail',
          component: UseCaseDetailView,
          meta: { requiresAuth: true, title: 'Use Case' },
        },
        {
          path: 'help',
          name: 'help',
          component: HelpView,
          meta: { requiresAuth: true, title: 'Help' },
        },
        {
          path: 'help/:topic',
          name: 'help-topic',
          component: HelpView,
          meta: { requiresAuth: true, title: 'Help Topic' },
        },
        {
          path: 'demo',
          name: 'demo-workspace',
          component: DemoWorkspaceView,
          meta: { requiresAuth: true, title: 'Demo Workspace' },
        },
        {
          path: 'my-library',
          name: 'my-library',
          component: MyLibraryView,
          meta: { requiresAuth: true, title: 'My Library' },
        },
        {
          path: 'books/new',
          name: 'add-book',
          component: AddBookView,
          meta: { requiresAuth: true, title: 'Add Book' },
        },
        {
          path: 'books/:id/edit',
          name: 'edit-book',
          component: EditBookView,
          meta: { requiresAuth: true, title: 'Edit Book' },
        },
        {
          path: 'books/:id',
          name: 'book-detail',
          component: BookDetailView,
          meta: { requiresAuth: true, title: 'Book Detail' },
        },
        {
          path: 'notes',
          name: 'notes',
          component: NotesView,
          meta: { requiresAuth: true, title: 'Notes' },
        },
        {
          path: 'books/:bookId/notes',
          name: 'book-notes',
          component: NotesView,
          meta: { requiresAuth: true, title: 'Book Notes' },
        },
        {
          path: 'notes/:id',
          name: 'note-detail',
          component: NoteDetailView,
          meta: { requiresAuth: true, title: 'Note Detail' },
        },
        {
          path: 'captures/inbox',
          name: 'capture-inbox',
          component: CaptureInboxView,
          meta: { requiresAuth: true, title: 'Capture Inbox' },
        },
        {
          path: 'quotes',
          name: 'quotes',
          component: QuotesView,
          meta: { requiresAuth: true, title: 'Quotes' },
        },
        {
          path: 'quotes/:id',
          name: 'quote-detail',
          component: QuoteDetailView,
          meta: { requiresAuth: true, title: 'Quote Detail' },
        },
        {
          path: 'action-items',
          name: 'action-items',
          component: ActionItemsView,
          meta: { requiresAuth: true, title: 'Action Items' },
        },
        {
          path: 'action-items/:id',
          name: 'action-item-detail',
          component: ActionItemDetailView,
          meta: { requiresAuth: true, title: 'Action Item Detail' },
        },
        {
          path: 'concepts',
          name: 'concepts',
          component: ConceptsView,
          meta: { requiresAuth: true, title: 'Concepts' },
        },
        {
          path: 'concepts/:id',
          name: 'concept-detail',
          component: ConceptDetailView,
          meta: { requiresAuth: true, title: 'Concept Detail' },
        },
        {
          path: 'knowledge',
          name: 'knowledge',
          component: KnowledgeObjectsView,
          meta: { requiresAuth: true, title: 'Knowledge Objects' },
        },
        {
          path: 'knowledge/:id',
          name: 'knowledge-detail',
          component: KnowledgeObjectDetailView,
          meta: { requiresAuth: true, title: 'Knowledge Object Detail' },
        },
        {
          path: 'daily',
          name: 'daily',
          component: DailyView,
          meta: { requiresAuth: true, title: 'Daily' },
        },
        {
          path: 'analytics',
          name: 'analytics',
          component: AnalyticsView,
          meta: { requiresAuth: true, title: 'Analytics' },
        },
        {
          path: 'review',
          name: 'review',
          component: ReviewView,
          meta: { requiresAuth: true, title: 'Review' },
        },
        {
          path: 'review/:id',
          name: 'review-detail',
          component: ReviewSessionView,
          meta: { requiresAuth: true, title: 'Review Session' },
        },
        {
          path: 'mastery',
          name: 'mastery',
          component: MasteryView,
          meta: { requiresAuth: true, title: 'Mastery' },
        },
        {
          path: 'import-export',
          name: 'import-export',
          component: ImportExportView,
          meta: { requiresAuth: true, title: 'Import / Export' },
        },
        {
          path: 'import-export/import',
          name: 'import-data',
          component: ImportExportView,
          meta: { requiresAuth: true, title: 'Import Data' },
        },
        {
          path: 'import-export/export',
          name: 'export-data',
          component: ImportExportView,
          meta: { requiresAuth: true, title: 'Export Data' },
        },
        {
          path: 'projects',
          name: 'projects',
          component: ProjectsView,
          meta: { requiresAuth: true, title: 'Projects' },
        },
        {
          path: 'projects/new',
          name: 'project-new',
          component: ProjectNewView,
          meta: { requiresAuth: true, title: 'New Project' },
        },
        {
          path: 'projects/:id',
          name: 'project-detail',
          component: ProjectDetailView,
          meta: { requiresAuth: true, title: 'Project Cockpit' },
        },
        {
          path: 'projects/:id/problems',
          name: 'project-problems',
          component: ProjectProblemsView,
          meta: { requiresAuth: true, title: 'Project Problems' },
        },
        {
          path: 'projects/:id/applications',
          name: 'project-applications',
          component: ProjectApplicationsView,
          meta: { requiresAuth: true, title: 'Project Applications' },
        },
        {
          path: 'projects/:id/decisions',
          name: 'project-decisions',
          component: ProjectDecisionsView,
          meta: { requiresAuth: true, title: 'Project Decisions' },
        },
        {
          path: 'projects/:id/wizard/apply-knowledge',
          name: 'project-apply-knowledge-wizard',
          component: ProjectApplyKnowledgeWizardView,
          meta: { requiresAuth: true, title: 'Apply Knowledge Wizard' },
        },
        {
          path: 'projects/:id/playtests',
          name: 'project-playtests',
          component: ProjectPlaytestsView,
          meta: { requiresAuth: true, title: 'Project Playtests' },
        },
        {
          path: 'projects/:id/lens-reviews',
          name: 'project-lens-reviews',
          component: ProjectLensReviewsView,
          meta: { requiresAuth: true, title: 'Project Lens Reviews' },
        },
        {
          path: 'graph',
          name: 'graph',
          component: GraphView,
          meta: { requiresAuth: true, title: 'Knowledge Graph' },
        },
        {
          path: 'graph/book/:bookId',
          name: 'graph-book',
          component: GraphView,
          meta: { requiresAuth: true, title: 'Book Graph' },
        },
        {
          path: 'graph/concept/:conceptId',
          name: 'graph-concept',
          component: GraphView,
          meta: { requiresAuth: true, title: 'Concept Graph' },
        },
        {
          path: 'graph/project/:projectId',
          name: 'graph-project',
          component: GraphView,
          meta: { requiresAuth: true, title: 'Project Graph' },
        },
        {
          path: 'admin/ontology',
          name: 'admin-ontology',
          component: AdminOntologyView,
          meta: { requiresAuth: true, title: 'Ontology Admin', role: 'ADMIN' },
        },
        {
          path: 'forum',
          name: 'forum',
          component: ForumView,
          meta: { requiresAuth: true, title: 'Forum' },
        },
        {
          path: 'forum/categories/:slug',
          name: 'forum-category',
          component: ForumView,
          meta: { requiresAuth: true, title: 'Forum Category' },
        },
        {
          path: 'forum/new',
          name: 'forum-new',
          component: ForumNewThreadView,
          meta: { requiresAuth: true, title: 'New Forum Thread' },
        },
        {
          path: 'forum/threads/:id',
          name: 'forum-thread',
          component: ForumThreadView,
          meta: { requiresAuth: true, title: 'Forum Thread' },
        },
        {
          path: 'five-star',
          name: 'five-star',
          component: FiveStarBooksView,
          meta: { requiresAuth: true, title: 'Five-Star Books' },
        },
        {
          path: 'currently-reading',
          name: 'currently-reading',
          component: CurrentlyReadingView,
          meta: { requiresAuth: true, title: 'Currently Reading' },
        },
        {
          path: 'anti-library',
          name: 'anti-library',
          component: AntiLibraryView,
          meta: { requiresAuth: true, title: 'Anti-Library' },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      component: PublicLayout,
      children: [{ path: '', name: 'not-found', component: NotFoundView, meta: { title: 'Not Found' } }],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (auth.token && !auth.user) {
    await auth.hydrate()
  }

  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const isPublic = to.matched.some((record) => record.meta.public)

  if (requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (typeof to.meta.role === 'string' && auth.user?.role !== to.meta.role) {
    return { name: 'dashboard' }
  }

  if (
    requiresAuth &&
    auth.user &&
    auth.user.onboardingCompleted === false &&
    to.name !== 'onboarding'
  ) {
    return { name: 'onboarding', query: { redirect: to.fullPath } }
  }

  if (isPublic && auth.isAuthenticated) {
    return resolveRedirectTarget(to.query.redirect) ?? { name: 'dashboard' }
  }

  return true
})

router.afterEach((to) => {
  const matchedTitle = [...to.matched]
    .reverse()
    .find((record) => typeof record.meta.title === 'string')
    ?.meta.title

  document.title = matchedTitle ? `${matchedTitle} | ${APP_TITLE}` : APP_TITLE
})

export default router
