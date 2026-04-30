import api, { unwrap } from './http'
import type {
  ApplySourcePayload,
  DesignDecisionPayload,
  DesignDecisionRecord,
  GameProjectPayload,
  GameProjectRecord,
  PlaytestFindingPayload,
  PlaytestFindingRecord,
  PlaytestPlanPayload,
  PlaytestPlanRecord,
  ProjectApplicationPayload,
  ProjectApplicationRecord,
  ProjectKnowledgeLinkPayload,
  ProjectKnowledgeLinkRecord,
  ProjectLensReviewPayload,
  ProjectLensReviewRecord,
  ProjectProblemPayload,
  ProjectProblemRecord,
  ProjectPrototypeTaskFromDailyPayload,
  ProjectWizardApplyKnowledgePayload,
  ProjectWizardApplyKnowledgeRecord,
} from '../types'

export function getProjects() {
  return unwrap<GameProjectRecord[]>(api.get('/projects'))
}

export function createProject(payload: GameProjectPayload) {
  return unwrap<GameProjectRecord>(api.post('/projects', payload))
}

export function getProject(id: number | string) {
  return unwrap<GameProjectRecord>(api.get(`/projects/${id}`))
}

export function updateProject(id: number | string, payload: GameProjectPayload) {
  return unwrap<GameProjectRecord>(api.put(`/projects/${id}`, payload))
}

export function archiveProject(id: number | string) {
  return unwrap<void>(api.put(`/projects/${id}/archive`))
}

export function getProjectProblems(projectId: number | string) {
  return unwrap<ProjectProblemRecord[]>(api.get(`/projects/${projectId}/problems`))
}

export function createProjectProblem(projectId: number | string, payload: ProjectProblemPayload) {
  return unwrap<ProjectProblemRecord>(api.post(`/projects/${projectId}/problems`, payload))
}

export function updateProjectProblem(id: number | string, payload: ProjectProblemPayload) {
  return unwrap<ProjectProblemRecord>(api.put(`/project-problems/${id}`, payload))
}

export function deleteProjectProblem(id: number | string) {
  return unwrap<void>(api.delete(`/project-problems/${id}`))
}

export function getProjectApplications(projectId: number | string) {
  return unwrap<ProjectApplicationRecord[]>(api.get(`/projects/${projectId}/applications`))
}

export function createProjectApplication(projectId: number | string, payload: ProjectApplicationPayload) {
  return unwrap<ProjectApplicationRecord>(api.post(`/projects/${projectId}/applications`, payload))
}

export function updateProjectApplication(id: number | string, payload: ProjectApplicationPayload) {
  return unwrap<ProjectApplicationRecord>(api.put(`/project-applications/${id}`, payload))
}

export function deleteProjectApplication(id: number | string) {
  return unwrap<void>(api.delete(`/project-applications/${id}`))
}

export function getProjectDecisions(projectId: number | string) {
  return unwrap<DesignDecisionRecord[]>(api.get(`/projects/${projectId}/decisions`))
}

export function createDesignDecision(projectId: number | string, payload: DesignDecisionPayload) {
  return unwrap<DesignDecisionRecord>(api.post(`/projects/${projectId}/decisions`, payload))
}

export function updateDesignDecision(id: number | string, payload: DesignDecisionPayload) {
  return unwrap<DesignDecisionRecord>(api.put(`/design-decisions/${id}`, payload))
}

export function deleteDesignDecision(id: number | string) {
  return unwrap<void>(api.delete(`/design-decisions/${id}`))
}

export function getPlaytestPlans(projectId: number | string) {
  return unwrap<PlaytestPlanRecord[]>(api.get(`/projects/${projectId}/playtest-plans`))
}

export function createPlaytestPlan(projectId: number | string, payload: PlaytestPlanPayload) {
  return unwrap<PlaytestPlanRecord>(api.post(`/projects/${projectId}/playtest-plans`, payload))
}

export function updatePlaytestPlan(id: number | string, payload: PlaytestPlanPayload) {
  return unwrap<PlaytestPlanRecord>(api.put(`/playtest-plans/${id}`, payload))
}

export function deletePlaytestPlan(id: number | string) {
  return unwrap<void>(api.delete(`/playtest-plans/${id}`))
}

export function getPlaytestFindings(projectId: number | string) {
  return unwrap<PlaytestFindingRecord[]>(api.get(`/projects/${projectId}/playtest-findings`))
}

export function createPlaytestFinding(projectId: number | string, payload: PlaytestFindingPayload) {
  return unwrap<PlaytestFindingRecord>(api.post(`/projects/${projectId}/playtest-findings`, payload))
}

export function updatePlaytestFinding(id: number | string, payload: PlaytestFindingPayload) {
  return unwrap<PlaytestFindingRecord>(api.put(`/playtest-findings/${id}`, payload))
}

export function deletePlaytestFinding(id: number | string) {
  return unwrap<void>(api.delete(`/playtest-findings/${id}`))
}

export function getProjectKnowledgeLinks(projectId: number | string) {
  return unwrap<ProjectKnowledgeLinkRecord[]>(api.get(`/projects/${projectId}/knowledge-links`))
}

export function createProjectKnowledgeLink(projectId: number | string, payload: ProjectKnowledgeLinkPayload) {
  return unwrap<ProjectKnowledgeLinkRecord>(api.post(`/projects/${projectId}/knowledge-links`, payload))
}

export function deleteProjectKnowledgeLink(id: number | string) {
  return unwrap<void>(api.delete(`/project-knowledge-links/${id}`))
}

export function getProjectLensReviews(projectId: number | string) {
  return unwrap<ProjectLensReviewRecord[]>(api.get(`/projects/${projectId}/lens-reviews`))
}

export function createProjectLensReview(projectId: number | string, payload: ProjectLensReviewPayload) {
  return unwrap<ProjectLensReviewRecord>(api.post(`/projects/${projectId}/lens-reviews`, payload))
}

export function updateProjectLensReview(id: number | string, payload: ProjectLensReviewPayload) {
  return unwrap<ProjectLensReviewRecord>(api.put(`/project-lens-reviews/${id}`, payload))
}

export function deleteProjectLensReview(id: number | string) {
  return unwrap<void>(api.delete(`/project-lens-reviews/${id}`))
}

export function applySourceReferenceToProject(projectId: number | string, payload: ApplySourcePayload) {
  return unwrap<ProjectApplicationRecord>(api.post(`/projects/${projectId}/apply/source-reference`, payload))
}

export function applyQuoteToProject(projectId: number | string, payload: ApplySourcePayload) {
  return unwrap<ProjectApplicationRecord>(api.post(`/projects/${projectId}/apply/quote`, payload))
}

export function applyConceptToProject(projectId: number | string, payload: ApplySourcePayload) {
  return unwrap<ProjectApplicationRecord>(api.post(`/projects/${projectId}/apply/concept`, payload))
}

export function applyKnowledgeObjectToProject(projectId: number | string, payload: ApplySourcePayload) {
  return unwrap<ProjectApplicationRecord>(api.post(`/projects/${projectId}/apply/knowledge-object`, payload))
}

export function createPrototypeTaskFromDaily(projectId: number | string, payload: ProjectPrototypeTaskFromDailyPayload) {
  return unwrap<ProjectApplicationRecord>(api.post(`/projects/${projectId}/create-prototype-task-from-daily`, payload))
}

export function applyKnowledgeWizard(projectId: number | string, payload: ProjectWizardApplyKnowledgePayload) {
  return unwrap<ProjectWizardApplyKnowledgeRecord>(api.post(`/projects/${projectId}/wizard/apply-knowledge`, payload))
}
