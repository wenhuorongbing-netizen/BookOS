<template>
  <header class="top-nav" aria-label="Workspace controls" @keydown.escape="closeMenus">
    <div class="top-nav__title">
      <div class="eyebrow">BookOS Workspace</div>
      <div class="top-nav__route-title">{{ title }}</div>
    </div>

    <div class="top-nav__search">
      <label class="sr-only" for="global-search">Global search</label>
      <div class="top-nav__search-shell">
        <el-input
          id="global-search"
          v-model="searchQuery"
          class="top-nav__search-input"
          placeholder="Search books, notes, quotes, concepts"
          aria-label="Global search"
          aria-describedby="global-search-shortcut"
        />
        <kbd id="global-search-shortcut" class="top-nav__shortcut">Ctrl / Cmd K</kbd>
      </div>
    </div>

    <div class="top-nav__book-context" aria-label="Current book context">
      <span class="top-nav__context-label">Current book</span>
      <div class="top-nav__book-menu">
        <button
          class="top-nav__book-button"
          type="button"
          aria-haspopup="dialog"
          :aria-expanded="bookMenuOpen ? 'true' : 'false'"
          aria-controls="current-book-menu"
          :aria-label="`Current book selector: ${currentBookTitle}`"
          @click="bookMenuOpen = !bookMenuOpen"
        >
          <span class="book-title">{{ currentBookTitle }}</span>
          <AppBadge variant="accent" size="sm">Select</AppBadge>
        </button>
        <div v-if="bookMenuOpen" id="current-book-menu" class="top-nav__popover" role="dialog" aria-label="Current book selector">
          <p v-if="currentBookId">Pinned source context is currently set to {{ currentBookTitle }}.</p>
          <p v-else>No active book is selected.</p>
          <RouterLink v-if="currentBookId" :to="`/books/${currentBookId}`" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigateTo(navigate)">Open current book</AppButton>
          </RouterLink>
          <RouterLink to="/currently-reading" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigateTo(navigate)">Open reading shelf</AppButton>
          </RouterLink>
        </div>
      </div>
    </div>

    <div class="top-nav__actions">
      <RouterLink to="/books/new" custom v-slot="{ navigate }">
        <AppButton variant="primary" @click="navigate">Add Book</AppButton>
      </RouterLink>
      <AppIconButton label="Notifications" tooltip="Notifications" variant="ghost">N</AppIconButton>
      <div class="top-nav__profile">
        <button
          class="top-nav__avatar"
          type="button"
          aria-haspopup="menu"
          :aria-expanded="profileMenuOpen ? 'true' : 'false'"
          aria-controls="profile-menu"
          @click="profileMenuOpen = !profileMenuOpen"
        >
          <span aria-hidden="true">{{ initials }}</span>
          <span class="sr-only">Open profile menu for {{ userName }}</span>
        </button>
        <div v-if="profileMenuOpen" id="profile-menu" class="top-nav__profile-menu" role="menu">
          <div class="top-nav__profile-name" role="presentation">{{ userName }}</div>
          <button type="button" role="menuitem" @click="handleLogout">Logout</button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AppBadge from './ui/AppBadge.vue'
import AppButton from './ui/AppButton.vue'
import AppIconButton from './ui/AppIconButton.vue'

const props = defineProps<{
  title: string
  userName: string
  currentBookTitle: string
  currentBookId?: number | null
}>()

const emit = defineEmits<{
  logout: []
}>()

const searchQuery = ref('')
const bookMenuOpen = ref(false)
const profileMenuOpen = ref(false)
const initials = computed(() => {
  const value = props.userName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('')

  return value || 'BU'
})

onMounted(() => {
  window.addEventListener('keydown', handleGlobalShortcut)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalShortcut)
})

function handleGlobalShortcut(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    document.getElementById('global-search')?.focus()
  }
}

function closeMenus() {
  bookMenuOpen.value = false
  profileMenuOpen.value = false
}

function navigateTo(navigate: () => void) {
  closeMenus()
  navigate()
}

function handleLogout() {
  closeMenus()
  emit('logout')
}
</script>

<style scoped>
.top-nav {
  position: sticky;
  top: var(--space-4);
  z-index: 20;
  padding: var(--space-3) var(--space-4);
  display: grid;
  grid-template-columns: minmax(150px, 0.55fr) minmax(280px, 1fr) minmax(240px, 0.75fr) auto;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
  min-height: 72px;
  background: color-mix(in srgb, var(--bookos-surface) 94%, transparent);
  backdrop-filter: blur(14px);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.top-nav__title {
  min-width: 0;
}

.top-nav__route-title {
  margin: 0.15rem 0 0;
  font-size: 1.25rem;
  font-weight: 800;
  line-height: 1.1;
}

.top-nav__search-shell,
.top-nav__book-menu,
.top-nav__profile {
  position: relative;
}

.top-nav__search-shell {
  display: flex;
  align-items: center;
  min-width: 0;
}

.top-nav__search-input {
  width: 100%;
}

.top-nav__search-input :deep(.el-input__wrapper) {
  padding-right: 5.8rem;
}

.top-nav__shortcut {
  position: absolute;
  right: var(--space-2);
  padding: 0.14rem 0.42rem;
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-sm);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-family: var(--font-ui);
  pointer-events: none;
}

.top-nav__book-context {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
}

.top-nav__context-label {
  color: var(--bookos-text-tertiary);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.top-nav__book-button {
  min-height: 44px;
  width: 100%;
  padding: var(--space-2) var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-primary);
  cursor: pointer;
}

.top-nav__popover,
.top-nav__profile-menu {
  position: absolute;
  top: calc(100% + var(--space-2));
  right: 0;
  z-index: 30;
  min-width: 240px;
  padding: var(--space-3);
  display: grid;
  gap: var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-md);
  background: var(--bookos-surface);
  box-shadow: var(--shadow-card-hover);
}

.top-nav__popover p,
.top-nav__profile-name {
  margin: 0;
  color: var(--bookos-text-secondary);
  font-size: var(--type-metadata);
}

.top-nav__book-button .book-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 700;
}

.top-nav__actions {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.top-nav__avatar {
  width: 44px;
  height: 44px;
  border: 1px solid color-mix(in srgb, var(--bookos-primary) 28%, var(--bookos-border));
  border-radius: 999px;
  background: var(--bookos-primary-soft);
  color: var(--bookos-primary-hover);
  font-weight: 900;
  cursor: pointer;
}

.top-nav__profile-menu button {
  min-height: var(--touch-target);
  padding: 0 var(--space-3);
  border: 1px solid var(--bookos-border);
  border-radius: var(--radius-sm);
  background: var(--bookos-surface-muted);
  color: var(--bookos-text-primary);
  text-align: left;
  cursor: pointer;
}

@media (max-width: 1240px) {
  .top-nav {
    grid-template-columns: 1fr 1fr;
  }

  .top-nav__actions {
    justify-content: flex-start;
  }
}

@media (max-width: 720px) {
  .top-nav {
    top: var(--space-3);
    grid-template-columns: 1fr;
    padding: var(--space-3);
  }

  .top-nav__popover,
  .top-nav__profile-menu {
    left: 0;
    right: auto;
    width: min(100%, 320px);
  }

  .top-nav__actions {
    justify-content: stretch;
  }

  .top-nav__actions > :deep(.el-button),
  .top-nav__actions > a {
    flex: 1 1 auto;
  }
}

@media (max-width: 480px) {
  .top-nav__shortcut {
    display: none;
  }

  .top-nav__search-input :deep(.el-input__wrapper) {
    padding-right: var(--space-3);
  }
}
</style>
