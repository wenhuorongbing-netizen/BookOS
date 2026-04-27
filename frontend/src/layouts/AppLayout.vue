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
      <div class="app-layout__body">
        <main id="main-content" class="app-layout__content" tabindex="-1">
          <RouterView />
        </main>
        <RightRail />
      </div>
    </div>
    <SourceReferenceDrawer />
    <GlobalSearchDialog />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import RightRail from '../components/RightRail.vue'
import Sidebar from '../components/Sidebar.vue'
import GlobalSearchDialog from '../components/search/GlobalSearchDialog.vue'
import SourceReferenceDrawer from '../components/source/SourceReferenceDrawer.vue'
import TopNav from '../components/TopNav.vue'
import { useAuthStore } from '../stores/auth'
import { useRightRailStore } from '../stores/rightRail'

const auth = useAuthStore()
const rightRail = useRightRailStore()
const route = useRoute()
const router = useRouter()

const title = computed(() => String(route.meta.title ?? 'BookOS'))
const currentBookTitle = computed(() => rightRail.sourceReference?.bookTitle ?? 'No active book selected')
const currentBookId = computed(() => rightRail.sourceReference?.bookId ?? null)

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
