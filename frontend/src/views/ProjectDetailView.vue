<template>
  <div class="page-shell project-detail-page">
    <AppLoadingState v-if="loading" label="Loading project cockpit" />
    <AppErrorState
      v-else-if="errorMessage"
      title="Project could not load"
      :description="errorMessage"
      retry-label="Retry"
      @retry="loadProject"
    />

    <template v-else-if="project">
      <AppSectionHeader
        :title="project.title"
        eyebrow="Project Cockpit"
        :description="project.description ?? 'No project brief yet.'"
        :level="1"
      >
        <template #actions>
          <HelpTooltip topic="project-application" placement="left" />
          <RouterLink :to="{ name: 'projects' }" custom v-slot="{ navigate }">
            <AppButton variant="secondary" @click="navigate">All Projects</AppButton>
          </RouterLink>
          <RouterLink
            :to="{ name: 'forum-new', query: { relatedEntityType: 'GAME_PROJECT', relatedEntityId: String(project.id), projectId: String(project.id), title: `Critique ${project.title}` } }"
            custom
            v-slot="{ navigate }"
          >
            <AppButton variant="secondary" @click="navigate">Start Project Critique</AppButton>
          </RouterLink>
          <RouterLink :to="{ name: 'project-apply-knowledge-wizard', params: { id: project.id } }" custom v-slot="{ navigate }">
            <AppButton variant="accent" @click="navigate">Apply Knowledge Guided Flow</AppButton>
          </RouterLink>
          <AppButton variant="primary" @click="editOpen = true">Edit Project</AppButton>
          <AppButton variant="text" @click="archiveCurrentProject">Archive</AppButton>
        </template>
      </AppSectionHeader>

      <ProjectWorkspaceNav :project-id="project.id" />

      <DetailNextStepCard
        :title="projectNextStep.title"
        :description="projectNextStep.description"
        :primary-label="projectNextStep.primaryLabel"
        :primary-to="projectNextStep.primaryTo"
        :secondary-label="projectNextStep.secondaryLabel"
        :secondary-to="projectNextStep.secondaryTo"
        :loop="projectWorkflowLoop"
      />

      <UseCaseSuggestionPanel
        title="Turn reading into project action"
        description="Use source-backed applications, lens reviews, playtest findings, and forum critique to keep this project practical."
        :slugs="projectUseCaseSlugs"
      />

      <section class="project-cockpit">
        <main class="project-cockpit__main">
          <AppCard class="project-hero">
            <div class="project-hero__badges">
              <AppBadge variant="primary">{{ project.stage }}</AppBadge>
              <AppBadge variant="neutral">{{ project.visibility }}</AppBadge>
              <AppBadge variant="accent">{{ project.genre ?? 'Genre unset' }}</AppBadge>
              <AppBadge variant="info">{{ project.platform ?? 'Platform unset' }}</AppBadge>
            </div>
            <AppProgressBar :value="project.progressPercent" label="Project progress" tone="accent" />
            <dl class="project-stats">
              <div>
                <dt>Problems</dt>
                <dd>{{ openProblems.length }}</dd>
              </div>
              <div>
                <dt>Applications</dt>
                <dd>{{ activeApplications.length }}</dd>
              </div>
              <div>
                <dt>Decisions</dt>
                <dd>{{ decisions.length }}</dd>
              </div>
              <div>
                <dt>Findings</dt>
                <dd>{{ findings.length }}</dd>
              </div>
            </dl>
          </AppCard>

          <section class="project-card-grid" aria-label="Project cockpit sections">
            <AppCard class="project-section-card">
              <AppSectionHeader title="Current Design Problems" eyebrow="Problems" :level="2" compact>
                <template #actions>
                  <RouterLink :to="{ name: 'project-problems', params: { id: project.id } }" custom v-slot="{ navigate }">
                    <AppButton variant="text" @click="navigate">Manage</AppButton>
                  </RouterLink>
                </template>
              </AppSectionHeader>
              <ProjectMiniList :items="openProblems" empty-title="No open problems" />
            </AppCard>

            <AppCard class="project-section-card">
              <AppSectionHeader title="Source-backed Applications" eyebrow="Applications" :level="2" compact>
                <template #actions>
                  <RouterLink :to="{ name: 'project-applications', params: { id: project.id } }" custom v-slot="{ navigate }">
                    <AppButton variant="text" @click="navigate">Manage</AppButton>
                  </RouterLink>
                </template>
              </AppSectionHeader>
              <ProjectMiniList :items="activeApplications" empty-title="No applications yet" />
            </AppCard>

            <AppCard class="project-section-card">
              <AppSectionHeader title="Recent Design Decisions" eyebrow="Decisions" :level="2" compact>
                <template #actions>
                  <RouterLink :to="{ name: 'project-decisions', params: { id: project.id } }" custom v-slot="{ navigate }">
                    <AppButton variant="text" @click="navigate">Manage</AppButton>
                  </RouterLink>
                </template>
              </AppSectionHeader>
              <ProjectMiniList :items="decisions" empty-title="No decisions recorded" />
            </AppCard>

            <AppCard class="project-section-card">
              <AppSectionHeader title="Playtest Findings" eyebrow="Playtests" :level="2" compact>
                <template #actions>
                  <RouterLink :to="{ name: 'project-playtests', params: { id: project.id } }" custom v-slot="{ navigate }">
                    <AppButton variant="text" @click="navigate">Manage</AppButton>
                  </RouterLink>
                </template>
              </AppSectionHeader>
              <ProjectMiniList :items="findings" empty-title="No playtest findings yet" />
            </AppCard>

            <AppCard class="project-section-card">
              <AppSectionHeader title="Active Lens Reviews" eyebrow="Lenses" :level="2" compact>
                <template #actions>
                  <RouterLink :to="{ name: 'project-lens-reviews', params: { id: project.id } }" custom v-slot="{ navigate }">
                    <AppButton variant="text" @click="navigate">Manage</AppButton>
                  </RouterLink>
                </template>
              </AppSectionHeader>
              <ProjectMiniList :items="lensReviews" empty-title="No lens reviews yet" />
            </AppCard>

            <AppCard class="project-section-card">
              <AppSectionHeader title="Linked Knowledge Objects" eyebrow="Knowledge" :level="2" compact />
              <ProjectMiniList :items="knowledgeLinks" empty-title="No linked knowledge yet" />
            </AppCard>
          </section>
        </main>

        <aside class="project-cockpit__rail" aria-label="Project context">
          <AppCard class="rail-card">
            <AppSectionHeader title="Active Source" eyebrow="Traceability" :level="2" compact />
            <template v-if="latestSource">
              <p>{{ latestSource.sourceText ?? latestSource.locationLabel ?? 'Source reference attached.' }}</p>
              <AppBadge variant="warning">{{ latestSource.sourceConfidence }} confidence</AppBadge>
              <AppButton variant="secondary" @click="openSource(latestSource)">Open Source</AppButton>
            </template>
            <AppEmptyState
              v-else
              title="No selected source"
              description="Apply a quote, concept, knowledge object, or source reference to populate this rail."
              compact
            />
          </AppCard>

          <AppCard class="rail-card">
            <AppSectionHeader title="Project Action Items" eyebrow="Tasks" :level="2" compact />
            <AppEmptyState
              title="Project-specific actions are not implemented"
              description="Use source-backed applications and playtest findings as the MVP task surface for now."
              compact
            />
          </AppCard>

          <AppCard class="rail-card">
            <AppSectionHeader title="Suggested Next Tasks" eyebrow="Existing records only" :level="2" compact />
            <ul class="next-tasks">
              <li v-for="task in nextTasks" :key="task">{{ task }}</li>
            </ul>
          </AppCard>

          <AppCard class="rail-card">
            <AppSectionHeader title="Graph Context" eyebrow="Real project links" :level="2" compact />
            <p v-if="projectGraph.nodes.length">
              {{ projectGraph.nodes.length }} nodes and {{ projectGraph.edges.length }} edges connect this project to source-backed records.
            </p>
            <AppEmptyState
              v-else
              title="No project graph links yet"
              description="Create applications, decisions, findings, or lens reviews to grow the project graph."
              compact
            />
            <RouterLink :to="{ name: 'graph-project', params: { projectId: project.id } }" custom v-slot="{ navigate }">
              <AppButton variant="secondary" @click="navigate">Open Project Graph</AppButton>
            </RouterLink>
          </AppCard>
        </aside>
      </section>

      <BacklinksSection
        entity-type="GAME_PROJECT"
        :entity-id="project.id"
        :source-references="projectSourceReferences"
        :book-title="project.title"
      />

      <el-dialog v-model="editOpen" title="Edit Project" width="min(760px, 94vw)">
        <ProjectFormPanel :project="project" :saving="savingProject" submit-label="Save Project" @submit="saveProject" />
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getProjectGraph } from '../api/graph'
import {
  archiveProject,
  getPlaytestFindings,
  getProject,
  getProjectApplications,
  getProjectDecisions,
  getProjectKnowledgeLinks,
  getProjectLensReviews,
  getProjectProblems,
  updateProject,
} from '../api/projects'
import ProjectFormPanel from '../components/project/ProjectFormPanel.vue'
import ProjectWorkspaceNav from '../components/project/ProjectWorkspaceNav.vue'
import HelpTooltip from '../components/help/HelpTooltip.vue'
import BacklinksSection from '../components/source/BacklinksSection.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppButton from '../components/ui/AppButton.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppEmptyState from '../components/ui/AppEmptyState.vue'
import AppErrorState from '../components/ui/AppErrorState.vue'
import AppLoadingState from '../components/ui/AppLoadingState.vue'
import AppProgressBar from '../components/ui/AppProgressBar.vue'
import AppSectionHeader from '../components/ui/AppSectionHeader.vue'
import UseCaseSuggestionPanel from '../components/use-case/UseCaseSuggestionPanel.vue'
import DetailNextStepCard from '../components/workflow/DetailNextStepCard.vue'
import { useOpenSource } from '../composables/useOpenSource'
import type {
  DesignDecisionRecord,
  GameProjectPayload,
  GameProjectRecord,
  GraphRecord,
  PlaytestFindingRecord,
  ProjectApplicationRecord,
  ProjectKnowledgeLinkRecord,
  ProjectLensReviewRecord,
  ProjectProblemRecord,
  SourceReferenceRecord,
} from '../types'

const ProjectMiniList = defineComponent({
  props: {
    items: { type: Array, required: true },
    emptyTitle: { type: String, required: true },
  },
  setup(props) {
    return () =>
      props.items.length
        ? h(
            'ul',
            { class: 'mini-list' },
            props.items.slice(0, 4).map((item: any) =>
              h('li', { key: item.id }, [
                h('strong', item.title ?? item.question ?? `${item.targetType} #${item.targetId}`),
                h('span', item.status ?? item.relationshipType ?? item.applicationType ?? ''),
              ]),
            ),
          )
        : h(AppEmptyState, { title: props.emptyTitle, description: 'Create one from the related workspace.', compact: true })
  },
})

const route = useRoute()
const router = useRouter()
const { openSource: openSourceDrawer } = useOpenSource()
const project = ref<GameProjectRecord | null>(null)
const problems = ref<ProjectProblemRecord[]>([])
const applications = ref<ProjectApplicationRecord[]>([])
const decisions = ref<DesignDecisionRecord[]>([])
const findings = ref<PlaytestFindingRecord[]>([])
const lensReviews = ref<ProjectLensReviewRecord[]>([])
const knowledgeLinks = ref<ProjectKnowledgeLinkRecord[]>([])
const projectGraph = ref<GraphRecord>({ nodes: [], edges: [] })
const loading = ref(false)
const savingProject = ref(false)
const errorMessage = ref('')
const editOpen = ref(false)
const projectUseCaseSlugs = [
  'apply-quote-to-game-project',
  'run-design-lens-review',
  'create-playtest-finding',
]
const projectWorkflowLoop = ['Project problem', 'Source-backed application', 'Design decision', 'Playtest finding']

const openProblems = computed(() => problems.value.filter((problem) => problem.status !== 'RESOLVED' && problem.status !== 'CLOSED'))
const activeApplications = computed(() => applications.value.filter((application) => application.status !== 'DONE' && application.status !== 'ARCHIVED'))
const latestSource = computed<SourceReferenceRecord | null>(() => {
  return (
    applications.value.find((application) => application.sourceReference)?.sourceReference ??
    decisions.value.find((decision) => decision.sourceReference)?.sourceReference ??
    findings.value.find((finding) => finding.sourceReference)?.sourceReference ??
    lensReviews.value.find((review) => review.sourceReference)?.sourceReference ??
    knowledgeLinks.value.find((link) => link.sourceReference)?.sourceReference ??
    null
  )
})
const nextTasks = computed(() => {
  const tasks: string[] = []
  if (!openProblems.value.length) tasks.push('Add the main design problem blocking this prototype.')
  if (!activeApplications.value.length) tasks.push('Apply one quote, concept, or design lens to this project.')
  if (!decisions.value.length) tasks.push('Record the first design decision and rationale.')
  if (!findings.value.length) tasks.push('Create a playtest finding after the next prototype run.')
  return tasks.length ? tasks : ['Review open problems and close one source-backed application.']
})
const projectNextStep = computed(() => {
  const currentProject = project.value
  if (!currentProject) {
    return {
      title: 'Open a project to focus the workflow',
      description: 'Project next steps appear after a project cockpit loads.',
      primaryLabel: 'All Projects',
      primaryTo: { name: 'projects' },
      secondaryLabel: null,
      secondaryTo: null,
    }
  }

  if (!openProblems.value.length) {
    return {
      title: 'Define the blocking design problem',
      description: 'A project becomes actionable when the next design problem is explicit and can be linked back to source-backed evidence.',
      primaryLabel: 'Add Problem',
      primaryTo: { name: 'project-problems', params: { id: currentProject.id } },
      secondaryLabel: 'Apply Knowledge',
      secondaryTo: { name: 'project-applications', params: { id: currentProject.id } },
    }
  }

  if (!activeApplications.value.length) {
    return {
      title: 'Apply one piece of reading knowledge',
      description: 'Turn a quote, concept, or knowledge object into a project application so the project is connected to the reading system.',
      primaryLabel: 'Add Application',
      primaryTo: { name: 'project-applications', params: { id: currentProject.id } },
      secondaryLabel: 'Open Graph',
      secondaryTo: { name: 'graph-project', params: { projectId: currentProject.id } },
    }
  }

  if (!decisions.value.length) {
    return {
      title: 'Record the first design decision',
      description: 'Use the project problem and source-backed applications as rationale, then capture the tradeoff explicitly.',
      primaryLabel: 'Add Decision',
      primaryTo: { name: 'project-decisions', params: { id: currentProject.id } },
      secondaryLabel: 'Open Applications',
      secondaryTo: { name: 'project-applications', params: { id: currentProject.id } },
    }
  }

  if (!findings.value.length) {
    return {
      title: 'Test the decision with a playtest finding',
      description: 'Create a finding that turns the current decision into observable evidence for the next iteration.',
      primaryLabel: 'Add Finding',
      primaryTo: { name: 'project-playtests', params: { id: currentProject.id } },
      secondaryLabel: 'Open Graph',
      secondaryTo: { name: 'graph-project', params: { projectId: currentProject.id } },
    }
  }

  return {
    title: 'Inspect the project knowledge graph',
    description: 'This project now has enough records to review how problems, applications, decisions, and findings connect.',
    primaryLabel: 'Open Project Graph',
    primaryTo: { name: 'graph-project', params: { projectId: currentProject.id } },
    secondaryLabel: 'Start Forum Critique',
    secondaryTo: {
      name: 'forum-new',
      query: {
        relatedEntityType: 'GAME_PROJECT',
        relatedEntityId: String(currentProject.id),
        projectId: String(currentProject.id),
        title: `Critique ${currentProject.title}`,
      },
    },
  }
})
const projectSourceReferences = computed(() => {
  const references = [
    ...problems.value.map((item) => item.relatedSourceReference),
    ...applications.value.map((item) => item.sourceReference),
    ...decisions.value.map((item) => item.sourceReference),
    ...findings.value.map((item) => item.sourceReference),
    ...lensReviews.value.map((item) => item.sourceReference),
    ...knowledgeLinks.value.map((item) => item.sourceReference),
  ].filter((source): source is SourceReferenceRecord => Boolean(source))

  const unique = new Map<number, SourceReferenceRecord>()
  references.forEach((source) => unique.set(source.id, source))
  return [...unique.values()]
})

onMounted(loadProject)

async function loadProject() {
  loading.value = true
  errorMessage.value = ''
  const projectId = String(route.params.id)
  try {
    const [projectResult, problemResult, applicationResult, decisionResult, findingResult, lensResult, linkResult, graphResult] = await Promise.all([
      getProject(projectId),
      getProjectProblems(projectId),
      getProjectApplications(projectId),
      getProjectDecisions(projectId),
      getPlaytestFindings(projectId),
      getProjectLensReviews(projectId),
      getProjectKnowledgeLinks(projectId),
      getProjectGraph(projectId),
    ])
    project.value = projectResult
    problems.value = problemResult
    applications.value = applicationResult
    decisions.value = decisionResult
    findings.value = findingResult
    lensReviews.value = lensResult
    knowledgeLinks.value = linkResult
    projectGraph.value = graphResult
  } catch {
    errorMessage.value = 'Check backend availability and permissions, then try opening this project again.'
  } finally {
    loading.value = false
  }
}

async function saveProject(payload: GameProjectPayload) {
  if (!project.value) return
  savingProject.value = true
  try {
    project.value = await updateProject(project.value.id, payload)
    editOpen.value = false
    ElMessage.success('Project updated.')
  } catch {
    ElMessage.error('Project update failed.')
  } finally {
    savingProject.value = false
  }
}

async function archiveCurrentProject() {
  if (!project.value) return
  try {
    await ElMessageBox.confirm('Archive this project? It will disappear from the active project list.', 'Archive Project', {
      confirmButtonText: 'Archive',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await archiveProject(project.value.id)
    ElMessage.success('Project archived.')
    await router.push({ name: 'projects' })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('Project archive failed.')
    }
  }
}

function openSource(source: SourceReferenceRecord) {
  void openSourceDrawer({
    sourceType: 'SOURCE_REFERENCE',
    sourceId: source.id,
    bookId: source.bookId,
    pageStart: source.pageStart,
    sourceReference: source,
    sourceReferenceId: source.id,
  })
}
</script>

<style scoped>
.project-detail-page,
.project-cockpit__main,
.project-section-card,
.rail-card {
  display: grid;
  gap: var(--space-5);
}

.project-cockpit {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 340px);
  gap: var(--space-4);
  align-items: start;
}

.project-cockpit__rail {
  position: sticky;
  top: var(--space-4);
  display: grid;
  gap: var(--space-4);
}

.project-hero,
.project-section-card,
.rail-card {
  padding: var(--space-5);
}

.project-hero__badges {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.project-stats {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
}

.project-stats dt {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-micro);
  font-weight: 900;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.project-stats dd {
  margin: 0;
  color: var(--bookos-primary);
  font-size: 1.5rem;
  font-weight: 900;
}

.project-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.rail-card p {
  margin: 0;
  color: var(--bookos-text-secondary);
  line-height: var(--type-body-line);
}

.next-tasks,
:deep(.mini-list) {
  margin: 0;
  padding-left: 1.1rem;
  display: grid;
  gap: var(--space-2);
}

:deep(.mini-list li) {
  color: var(--bookos-text-secondary);
}

:deep(.mini-list strong) {
  display: block;
  color: var(--bookos-text-primary);
}

:deep(.mini-list span) {
  color: var(--bookos-text-tertiary);
  font-size: var(--type-metadata);
  font-weight: 800;
}

@media (max-width: 1100px) {
  .project-cockpit,
  .project-card-grid {
    grid-template-columns: 1fr;
  }

  .project-cockpit__rail {
    position: static;
  }
}

@media (max-width: 720px) {
  .project-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
