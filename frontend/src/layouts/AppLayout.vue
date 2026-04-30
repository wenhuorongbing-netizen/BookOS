<template>
  <div class="app-layout">
    <a class="skip-link" href="#main-content">Skip to main content</a>
    <Sidebar />
    <div class="app-layout__workspace">
      <TopNav
        :title="title"
        :user-name="auth.user?.displayName ?? 'BookOS User'"
        :current-book-title="currentBookTitle"
        :current-book-id="currentBookId"
        @logout="handleLogout"
      />
      <nav v-if="breadcrumbItems.length" class="app-layout__breadcrumbs" aria-label="Breadcrumb">
        <ol>
          <li>
            <RouterLink :to="{ name: 'dashboard' }">Dashboard</RouterLink>
          </li>
          <li v-for="item in breadcrumbItems" :key="item.label" :aria-current="item.current ? 'page' : undefined">
            <RouterLink v-if="!item.current && item.to" :to="item.to">{{ item.label }}</RouterLink>
            <span v-else>{{ item.label }}</span>
          </li>
        </ol>
      </nav>
      <div class="app-layout__body">
        <main id="main-content" class="app-layout__content" tabindex="-1">
          <AppCard v-if="isAdvancedRoute" class="app-layout__workflow-return" variant="highlight" as="section">
            <div>
              <div class="eyebrow">Advanced workspace</div>
              <h2>{{ advancedReturn.title }}</h2>
              <p>{{ advancedReturn.description }}</p>
            </div>
            <RouterLink :to="{ name: advancedReturn.routeName }" custom v-slot="{ navigate }">
              <AppButton variant="primary" @click="navigate">Back to main workflow</AppButton>
            </RouterLink>
          </AppCard>
          <RouterView />
        </main>
        <RightRail />
      </div>
    </div>
    <SourceReferenceDrawer />
    <HelpDrawer />
    <GlobalSearchDialog />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter, type RouteLocationRaw } from 'vue-router'
import RightRail from '../components/RightRail.vue'
import Sidebar from '../components/Sidebar.vue'
import HelpDrawer from '../components/help/HelpDrawer.vue'
import GlobalSearchDialog from '../components/search/GlobalSearchDialog.vue'
import SourceReferenceDrawer from '../components/source/SourceReferenceDrawer.vue'
import TopNav from '../components/TopNav.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import { useAuthStore } from '../stores/auth'
import { useRightRailStore } from '../stores/rightRail'
import { normalizeNavigationMode } from '../utils/navigationMode'

const auth = useAuthStore()
const rightRail = useRightRailStore()
const route = useRoute()
const router = useRouter()

const title = computed(() => String(route.meta.title ?? 'BookOS'))
const currentBookTitle = computed(() => rightRail.sourceReference?.bookTitle ?? 'No active book selected')
const currentBookId = computed(() => rightRail.sourceReference?.bookId ?? null)
const currentMode = computed(() =>
  normalizeNavigationMode(auth.user?.preferredDashboardMode, auth.user?.startingMode),
)
const breadcrumbItems = computed<Array<{ label: string; to?: RouteLocationRaw; current: boolean }>>(() => {
  if (route.name === 'dashboard') return []

  return [
    {
      label: title.value,
      current: true,
    },
  ]
})
const isAdvancedRoute = computed(() =>
  ['/graph', '/analytics', '/import-export', '/admin/ontology'].some((path) => route.path.startsWith(path)),
)
const advancedReturn = computed(() => {
  if (currentMode.value === 'GAME_DESIGNER') {
    return {
      title: 'Return to project work',
      description: 'Advanced tools are secondary. Go back to the project cockpit when you are ready to apply what you found.',
      routeName: 'projects',
    }
  }
  if (currentMode.value === 'RESEARCHER') {
    return {
      title: 'Return to review',
      description: 'Use advanced exploration to support the review loop, then return to source-backed review work.',
      routeName: 'review',
    }
  }
  return {
    title: 'Return to the reading loop',
    description: 'Advanced tools are available, but the main workflow is still read, capture, convert, review, and apply.',
    routeName: 'dashboard',
  }
})

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  overflow-x: clip;
  background:
    radial-gradient(circle at top right, rgba(197, 107, 44, 0.1), transparent 30%),
    var(--bookos-bg);
}

.app-layout__workspace {
  min-width: 0;
  width: 100%;
  padding: var(--space-4);
  display: grid;
  grid-template-rows: auto 1fr;
  gap: var(--space-4);
}

.app-layout__body {
  min-width: 0;
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 360px);
  align-items: start;
  gap: var(--space-4);
}

.app-layout__content {
  min-width: 0;
  width: 100%;
  outline: none;
  display: grid;
  gap: var(--space-4);
}

.app-layout__breadcrumbs ol {
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  list-style: none;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

.app-layout__breadcrumbs li {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

.app-layout__breadcrumbs li + li::before {
  content: '/';
  color: var(--bookos-text-tertiary);
}

.app-layout__breadcrumbs a {
  color: var(--bookos-primary);
}

.app-layout__workflow-return {
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.app-layout__workflow-return h2,
.app-layout__workflow-return p {
  margin: 0;
}

.app-layout__workflow-return h2 {
  margin-top: var(--space-1);
  font-size: var(--type-card-title);
}

.app-layout__workflow-return p {
  margin-top: var(--space-1);
  color: var(--bookos-text-secondary);
}

@media (min-width: 1281px) {
  .app-layout__body > :deep(.right-rail) {
    position: sticky;
    top: calc(var(--space-4) + 88px);
  }
}

@media (max-width: 1280px) {
  .app-layout__body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .app-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .app-layout__workspace {
    padding: var(--space-3);
    gap: var(--space-3);
  }
}
</style>
