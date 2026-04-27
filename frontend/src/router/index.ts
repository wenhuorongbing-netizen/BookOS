import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const PublicLayout = () => import('../layouts/PublicLayout.vue')
const AppLayout = () => import('../layouts/AppLayout.vue')
const DashboardLayout = () => import('../layouts/DashboardLayout.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const DashboardView = () => import('../views/DashboardView.vue')
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
          path: 'dashboard',
          component: DashboardLayout,
          meta: { requiresAuth: true, title: 'Dashboard' },
          children: [{ path: '', name: 'dashboard', component: DashboardView, meta: { title: 'Dashboard' } }],
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
