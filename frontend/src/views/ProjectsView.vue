<template>
  <div class="page-shell projects-page">
    <AppSectionHeader
      title="Projects"
      eyebrow="Game Project Mode"
      description="Turn reading knowledge into design problems, decisions, applications, lens reviews, and playtest work."
      :level="1"
    >
      <template #actions>
        <RouterLink :to="{ name: 'project-new' }" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Create Project</AppButton>
        </RouterLink>
      </template>
    </AppSectionHeader>

    <AppCard class="task-first-panel" variant="highlight" as="section">
      <div>
        <div class="eyebrow">Next project action</div>
        <h2>{{ projectTask.title }}</h2>
        <p>{{ projectTask.description }}</p>
        <div class="task-first-panel__metrics" aria-label="Project summary">
          <AppBadge variant="primary">{{ projectCards.length }} projects</AppBadge>
          <AppBadge variant="accent">{{ totalOpenProblems }} open problems</AppBadge>
          <AppBadge variant="neutral">{{ totalActiveApplications }} active applications</AppBadge>
        </div>
      </div>
      <div class="task-first-panel__actions">
        <RouterLink :to="{ name: projectTask.routeName, params: projectTask.routeParams }" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">{{ projectTask.primaryLabel }}</AppButton>
        </RouterLink>
        <RouterLink to="/use-cases/apply-quote-to-game-project" custom v-slot="{ navigate }">
          <AppButton variant="secondary" @click="navigate">See project workflow</AppButton>
        </RouterLink>
      </div>
    </AppCard>

    <UseCaseSuggestionPanel
      :slugs="['apply-quote-to-game-project', 'run-design-lens-review', 'create-playtest-finding']"
      eyebrow="Project playbook"
      title="Use projects to prove knowledge is useful"
      description="A project turns source-backed notes, quotes, and concepts into design decisions and playtest evidence."
    />

    <AppLoadingState v-if="loading" label="Loading projects" />
    <AppErrorState
      v-else-if="errorMessage"
      title="Projects could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadProjects"
    />
    <AppEmptyState
      v-else-if="!projectCards.length"
      title="Create one project to apply knowledge"
      description="Project Mode becomes useful after you create a real prototype or design problem, then attach quotes, concepts, or source references to it."
      eyebrow="First project"
    >
      <template #actions>
        <RouterLink :to="{ name: 'project-new' }" custom v-slot="{ navigate }">
          <AppButton variant="primary" @click="navigate">Create Project</AppButton>
        </RouterLink>
      </template>
    </AppEmptyState>

    <section v-else class="project-grid" aria-label="Game projects">
      <RouterLink
        v-for="item in projectCards"
        :key="item.project.id"
        :to="{ name: 'project-detail', params: { id: item.project.id } }"
        class="project-card-link"
      >
        <AppCard class="project-card" as="article">
          <div class="project-card__topline">
            <AppBadge variant="primary">{{ item.project.stage }}</AppBadge>
            <AppBadge variant="neutral">{{ item.project.visibility }}</AppBadge>
          </div>
          <h2>{{ item.project.title }}</h2>
          <p>{{ item.project.description ?? 'No project brief yet.' }}</p>
          <dl class="project-card__meta">
            <div>
              <dt>Genre</dt>
              <dd>{{ item.project.genre ?? 'Not set' }}</dd>
            </div>
            <div>
              <dt>Platform</dt>
              <dd>{{ item.project.platform ?? 'Not set' }}</dd>
            </div>
            <div>
              <dt>Open problems</dt>
              <dd>{{ item.openProblems }}</dd>
            </div>
            <div>
              <dt>Applications</dt>
              <dd>{{ item.activeApplications }}</dd>
            </div>
          </dl>
          <AppProgressBar :value="item.project.progressPercent" label="Project progress" tone="accent" />
          <span class="project-card__date">Updated {{ formatDate(item.project.updatedAt) }}</span>
        </AppCard>
      </RouterLink>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getProjectApplications, getProjectProblems, getProjects } from '../api/projects'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppProgressBar from '../components/ui/AppProgressBar.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import type { GameProjectRecord } from '../types'

interface ProjectCardModel {
  project: GameProjectRecord
  openProblems: number
  activeApplications: number
}

const projectCards = ref<ProjectCardModel[]>([])
const loading = ref(false)
const errorMessage = ref('')
const totalOpenProblems = computed(() => projectCards.value.reduce((total, item) => total + item.openProblems, 0))
const totalActiveApplications = computed(() =>
  projectCards.value.reduce((total, item) => total + item.activeApplications, 0),
)
const projectTask = computed(() => {
  const needsAttention = projectCards.value.find((item) => item.openProblems > 0 || item.activeApplications > 0)
  if (needsAttention) {
    return {
      title: `Open ${needsAttention.project.title}`,
      description: 'This project has active problems or applications. Use the cockpit to decide the next design action.',
      primaryLabel: 'Open Project Cockpit',
      routeName: 'project-detail',
      routeParams: { id: needsAttention.project.id },
    }
  }

  const firstProject = projectCards.value[0]
  if (firstProject) {
    return {
      title: 'Apply one source-backed idea',
      description: 'Open a project, then attach a quote, concept, or source reference from your reading loop.',
      primaryLabel: 'Open Project Cockpit',
      routeName: 'project-detail',
      routeParams: { id: firstProject.project.id },
    }
  }

  return {
    title: 'Create a project for one real design problem',
    description: 'BookOS becomes practical when reading creates action. Start with a game prototype, mechanic, or design question.',
    primaryLabel: 'Create Project',
    routeName: 'project-new',
    routeParams: {},
  }
})

onMounted(loadProjects)

async function loadProjects() {
  loading.value = true
  errorMessage.value = ''
  try {
    const projects = await getProjects()
    projectCards.value = await Promise.all(
      projects.map(async (project) => {
        const [problems, applications] = await Promise.all([
          getProjectProblems(project.id).catch(() => []),
          getProjectApplications(project.id).catch(() => []),
        ])
        return {
          project,
          openProblems: problems.filter((problem) => problem.status !== 'RESOLVED' && problem.status !== 'CLOSED').length,
          activeApplications: applications.filter((application) => application.status !== 'DONE' && application.status !== 'ARCHIVED').length,
        }
      }),
    )
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try loading projects again.'
  } finally {
    loading.value = false
  }
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
</script>

<style scoped>
.projects-page {
  display: grid;
  gap: var(--space-5);
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-4);
}

.project-card-link {
  color: inherit;
  text-decoration: none;
}

.project-card {
  height: 100%;
  padding: var(--space-5);
  display: grid;
  align-content: start;
  gap: var(--space-4);
}

.project-card__topline {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.project-card h2,
.project-card p {
  margin: 0;
}

.project-card h2 {
  font-family: var(--font-book-title);
  font-size: clamp(1.35rem, 2.4vw, 2rem);
}

.project-card p,
.project-card__date {
  color: var(--bookos-text-secondary);
}

.project-card__meta {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.project-card__meta dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.project-card__meta dd {
  margin: 0;
  color: var(--bookos-text-primary);
  font-weight: 800;
}
</style>
