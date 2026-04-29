<template>
  <aside class="sidebar" aria-label="BookOS navigation" @keydown.escape="closeNav">
    <RouterLink class="sidebar__brand" to="/dashboard" :aria-current="route.path === '/dashboard' ? 'page' : undefined">
      <div class="sidebar__mark">B</div>
      <div>
        <strong>BookOS</strong>
        <div class="sidebar__subtitle">Game Design Knowledge Operating System</div>
      </div>
    </RouterLink>

    <button
      class="sidebar__menu-toggle"
      type="button"
      aria-label="Toggle BookOS navigation menu"
      aria-controls="bookos-sidebar-drawer"
      :aria-expanded="navOpen ? 'true' : 'false'"
      @click="navOpen = !navOpen"
    >
      Menu
    </button>

    <div id="bookos-sidebar-drawer" class="sidebar__drawer" :class="{ 'sidebar__drawer--open': navOpen }">
      <nav class="sidebar__nav" aria-label="Main navigation">
        <div class="sidebar__section-label">Main</div>
        <RouterLink v-for="item in visibleRouteItems" :key="item.to" :to="item.to" custom v-slot="{ href, navigate }">
          <a
            class="sidebar__nav-link"
            :class="{ 'sidebar__nav-link--active': isNavActive(item) }"
            :href="href"
            :aria-current="isNavActive(item) ? 'page' : undefined"
            @click="handleNavigate(navigate)"
          >
            <span class="sidebar__item-kicker" aria-hidden="true">{{ item.short }}</span>
            <span>{{ item.label }}</span>
          </a>
        </RouterLink>

        <button
          v-for="item in plannedItems"
          :key="item.label"
          class="sidebar__nav-link sidebar__nav-link--disabled"
          type="button"
          disabled
          aria-disabled="true"
        >
          <span class="sidebar__item-kicker" aria-hidden="true">{{ item.short }}</span>
          <span>{{ item.label }}</span>
        </button>

        <button class="sidebar__nav-link" type="button" @click="openSearch">
          <span class="sidebar__item-kicker" aria-hidden="true">SE</span>
          <span>Search</span>
        </button>
      </nav>

      <AppCard class="sidebar__project" variant="rail" as="section">
        <div class="eyebrow">Current Project</div>
        <strong>Project mode is not implemented yet</strong>
        <p>Game project application workflows will appear here after the project module is built.</p>
      </AppCard>

      <AppCard class="sidebar__concepts" variant="rail" as="section">
        <div class="eyebrow">Recently Viewed</div>
        <p>Recent concept history is not implemented yet. No sample concepts are shown as real activity.</p>
      </AppCard>

      <button class="sidebar__settings" type="button" disabled aria-disabled="true">
        <span class="sidebar__item-kicker" aria-hidden="true">ST</span>
        <span>Settings</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppCard from './ui/AppCard.vue'

const route = useRoute()
const auth = useAuthStore()
const navOpen = ref(false)

const routeItems = [
  {
    label: 'Library',
    to: '/my-library',
    short: 'LI',
    activePaths: ['/my-library', '/books', '/currently-reading', '/five-star', '/anti-library'],
  },
  {
    label: 'Notes',
    to: '/notes',
    short: 'NO',
    activePaths: ['/notes'],
  },
  {
    label: 'Captures',
    to: '/captures/inbox',
    short: 'CA',
    activePaths: ['/captures'],
  },
  {
    label: 'Quotes',
    to: '/quotes',
    short: 'QU',
    activePaths: ['/quotes'],
  },
  {
    label: 'Action Items',
    to: '/action-items',
    short: 'AC',
    activePaths: ['/action-items'],
  },
  {
    label: 'Concepts',
    to: '/concepts',
    short: 'CO',
    activePaths: ['/concepts'],
  },
  {
    label: 'Knowledge',
    to: '/knowledge',
    short: 'KO',
    activePaths: ['/knowledge'],
  },
  {
    label: 'Daily',
    to: '/daily',
    short: 'DA',
    activePaths: ['/daily'],
  },
  {
    label: 'Graph',
    to: '/graph',
    short: 'GR',
    activePaths: ['/graph'],
  },
  {
    label: 'Forum',
    to: '/forum',
    short: 'FO',
    activePaths: ['/forum'],
  },
]

const plannedItems = [
  { label: 'Lenses', short: 'LE' },
  { label: 'Diagnostics', short: 'DI' },
  { label: 'Exercises', short: 'EX' },
  { label: 'Projects', short: 'PR' },
]

const visibleRouteItems = computed(() => {
  if (auth.user?.role !== 'ADMIN') {
    return routeItems
  }
  return [
    ...routeItems,
    {
      label: 'Admin',
      to: '/admin/ontology',
      short: 'AD',
      activePaths: ['/admin'],
    },
  ]
})

onMounted(() => {
  window.addEventListener('keydown', handleGlobalEscape)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalEscape)
})

watch(
  () => route.fullPath,
  () => {
    navOpen.value = false
  },
)

function isNavActive(item: (typeof routeItems)[number]) {
  return item.activePaths.some((path) => route.path === path || route.path.startsWith(`${path}/`))
}

function closeNav() {
  navOpen.value = false
}

function handleGlobalEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') closeNav()
}

function handleNavigate(navigate: () => void) {
  closeNav()
  navigate()
}

function openSearch() {
  closeNav()
  window.dispatchEvent(new CustomEvent('bookos:open-search'))
}
</script>

<style scoped>
.sidebar {
  position: sticky;
  top: 0;
  min-height: 100vh;
  max-height: 100vh;
  overflow: auto;
  overscroll-behavior: contain;
  padding: var(--space-4);
  display: grid;
  grid-template-rows: auto auto 1fr;
  align-content: start;
  gap: var(--space-4);
  background: linear-gradient(180deg, #123c38, #1d4f49);
  color: rgba(255, 253, 248, 0.96);
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  border-radius: var(--radius-lg);
  outline-offset: 5px;
}

.sidebar__mark {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: grid;
  place-items: center;
  background: var(--bookos-accent);
  color: #fffdf8;
  font-weight: 800;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
}

.sidebar__subtitle {
  color: rgba(255, 253, 248, 0.72);
  font-size: 0.76rem;
  line-height: 1.35;
}

.sidebar__menu-toggle {
  display: none;
  min-height: var(--touch-target);
  border: 1px solid rgba(255, 253, 248, 0.18);
  border-radius: var(--radius-md);
  background: rgba(255, 253, 248, 0.08);
  color: #fffdf8;
  font-weight: 800;
  cursor: pointer;
}

.sidebar__drawer {
  min-height: 0;
  min-width: 0;
  display: grid;
  grid-template-rows: auto auto auto 1fr auto;
  gap: var(--space-4);
}

.sidebar__nav {
  display: grid;
  gap: var(--space-2);
}

.sidebar__section-label {
  margin-top: var(--space-2);
  color: rgba(255, 253, 248, 0.58);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar__nav-link,
.sidebar__settings {
  min-height: 44px;
  width: 100%;
  padding: 0 var(--space-3);
  border: 1px solid transparent;
  color: rgba(255, 253, 248, 0.88);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: transparent;
  text-align: left;
  font-weight: 700;
}

.sidebar__nav-link:hover,
.sidebar__nav-link:focus-visible,
.sidebar__settings:focus-visible {
  background: rgba(255, 253, 248, 0.1);
}

.sidebar__nav-link--active {
  background: rgba(255, 253, 248, 0.16);
  border-color: rgba(255, 253, 248, 0.2);
  color: #fffdf8;
}

.sidebar__nav-link--disabled,
.sidebar__settings {
  color: rgba(255, 253, 248, 0.5);
  cursor: not-allowed;
}

.sidebar__item-kicker {
  width: 30px;
  color: rgba(255, 253, 248, 0.62);
  font-size: 0.72rem;
  font-weight: 800;
}

.sidebar__project,
.sidebar__concepts {
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  background: rgba(255, 253, 248, 0.08);
  border-color: rgba(255, 253, 248, 0.12);
  color: rgba(255, 253, 248, 0.94);
}

.sidebar__project p,
.sidebar__concepts p {
  margin: 0;
  color: rgba(255, 253, 248, 0.76);
  font-size: var(--type-metadata);
  line-height: 1.45;
}

.sidebar :deep(.app-progress__value) {
  color: rgba(255, 253, 248, 0.78);
}

.sidebar :deep(.app-progress__track) {
  background: rgba(255, 253, 248, 0.16);
}

.sidebar__settings {
  align-self: end;
}

@media (max-width: 980px) {
  .sidebar {
    position: relative;
    min-height: auto;
    max-height: none;
    grid-template-columns: auto 1fr;
    grid-template-rows: auto auto;
    align-items: center;
    z-index: 25;
  }

  .sidebar__menu-toggle {
    display: inline-flex;
    justify-content: center;
    justify-self: end;
    padding: 0 var(--space-4);
  }

  .sidebar__drawer {
    display: none;
    grid-column: span 2;
  }

  .sidebar__drawer--open {
    display: grid;
    padding-top: var(--space-2);
  }
}

@media (max-width: 720px) {
  .sidebar {
    grid-template-columns: 1fr;
    padding: var(--space-4);
  }

  .sidebar__menu-toggle,
  .sidebar__drawer {
    grid-column: auto;
  }

  .sidebar__brand {
    min-width: 0;
  }

  .sidebar__subtitle {
    max-width: 26ch;
  }
}
</style>
