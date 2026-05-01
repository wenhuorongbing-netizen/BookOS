import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiGet, apiPut, apiPost, setSession, type AuthPayload, uniqueRunId } from './support/api'

interface DemoStatus {
  active: boolean
  lastResetAt: string | null
  bookId: number | null
  projectId: number | null
  quoteId: number | null
  actionItemId: number | null
  forumThreadId: number | null
  conceptIds: number[]
  recordCounts: Record<string, number>
  includedRecordTypes: string[]
  excludedFromAnalyticsByDefault: boolean
}

interface ReadingAnalytics {
  libraryBooks: number
  quotesCount: number
  projectApplicationsCount: number
}

interface SourceReferenceRecord {
  pageStart: number | null
  pageEnd: number | null
  sourceConfidence: string
  sourceText: string | null
}

interface GraphResponse {
  nodes: Array<{ id: string; label: string }>
  edges: Array<{ source: string; target: string; type: string }>
}

async function createDemoUser(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()
  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Demo Learner ${runId}`,
    email: `demo-${runId}@bookos.local`,
    password: 'Password123!',
  })
  const user = await apiPut<AuthPayload['user']>(request, '/users/me/onboarding', auth.token, {
    onboardingCompleted: true,
    primaryUseCase: 'ADVANCED_TOOLS',
    startingMode: 'ADVANCED',
    preferredDashboardMode: 'ADVANCED',
  })
  await setSession(page, { ...auth, user })
  return auth.token
}

test('demo workspace creates labeled practice data that can be reset and deleted', async ({ page, request }) => {
  const token = await createDemoUser(page, request)

  await page.goto('/demo')
  await expect(page.getByRole('heading', { name: 'Practice BookOS without polluting your real knowledge base' })).toBeVisible()
  await expect(page.getByText('Demo data is labeled')).toBeVisible()
  await expect(page.getByText('Inactive')).toBeVisible()
  await expect(page.getByRole('link', { name: /Demo Capture and Convert/i })).toBeVisible()
  await expect(page.getByRole('link', { name: /Demo Export Demo Data/i })).toBeVisible()

  await page.getByRole('button', { name: 'Start Demo Workspace' }).first().click()
  await expect(page.getByRole('heading', { name: 'Demo workspace is ready' })).toBeVisible()
  const statusRegion = page.getByRole('region', { name: 'Demo workspace status' })
  await expect(statusRegion.getByText('Excluded by default')).toBeVisible()
  await expect(statusRegion.getByText('Included record types')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Graduate to Real Book' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Delete Demo and Start Real Workflow' })).toBeVisible()
  await expect(page.getByText('No copyrighted passages are included.')).toBeVisible()
  await expect(page.getByText('Demo records are not your real reading.')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Reset Demo' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open demo book' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open demo project' })).toBeVisible()

  const status = await apiGet<DemoStatus>(request, '/demo/status', token)
  expect(status.active).toBe(true)
  expect(status.lastResetAt).toBeTruthy()
  expect(status.bookId).toBeTruthy()
  expect(status.projectId).toBeTruthy()
  expect(status.quoteId).toBeTruthy()
  expect(status.actionItemId).toBeTruthy()
  expect(status.forumThreadId).toBeTruthy()
  expect(status.conceptIds).toHaveLength(4)
  expect(status.recordCounts.BOOK).toBe(1)
  expect(status.recordCounts.RAW_CAPTURE).toBeGreaterThanOrEqual(4)
  expect(status.includedRecordTypes).toContain('BOOK')
  expect(status.includedRecordTypes).toContain('GAME_PROJECT')
  expect(status.excludedFromAnalyticsByDefault).toBe(true)

  const sources = await apiGet<SourceReferenceRecord[]>(request, `/source-references?bookId=${status.bookId}`, token)
  expect(sources.length).toBeGreaterThanOrEqual(5)
  expect(sources.every((source) => source.pageStart === null && source.pageEnd === null)).toBe(true)
  expect(sources.every((source) => source.sourceConfidence === 'LOW')).toBe(true)
  expect(JSON.stringify(sources)).not.toContain('The impediment to action')
  expect(JSON.stringify(sources)).not.toContain('Discipline equals freedom')

  const searchResults = await apiGet<unknown[]>(request, '/search?q=Demo%20Game%20Design%20Notebook', token)
  expect(searchResults).toHaveLength(0)

  const graph = await apiGet<GraphResponse>(request, `/graph/book/${status.bookId}`, token)
  expect(graph.nodes.length).toBeGreaterThan(0)
  expect(graph.nodes.some((node) => node.label.startsWith('[Demo]'))).toBe(true)

  const regularAnalytics = await apiGet<ReadingAnalytics>(request, '/analytics/reading', token)
  expect(regularAnalytics.libraryBooks).toBe(0)
  expect(regularAnalytics.quotesCount).toBe(0)
  expect(regularAnalytics.projectApplicationsCount).toBe(0)

  const demoAnalytics = await apiGet<ReadingAnalytics>(request, '/analytics/reading?includeDemo=true', token)
  expect(demoAnalytics.libraryBooks).toBe(1)
  expect(demoAnalytics.quotesCount).toBe(1)
  expect(demoAnalytics.projectApplicationsCount).toBe(1)

  await page.getByRole('button', { name: 'Open demo book' }).click()
  await expect(page).toHaveURL(new RegExp(`/books/${status.bookId}`))
  await expect(page.getByRole('heading', { name: /Demo Game Design Notebook/ })).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Open demo quote' }).click()
  await expect(page).toHaveURL(new RegExp(`/quotes/${status.quoteId}`))
  await expect(page.getByText('BookOS Demo original sample')).toBeVisible()
  await expect(page.getByText('Unknown', { exact: true })).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Open demo action' }).click()
  await expect(page).toHaveURL(new RegExp(`/action-items/${status.actionItemId}`))
  await expect(page.getByRole('heading', { name: '[Demo] Test glowing-door readability' })).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Open demo concept' }).click()
  await expect(page).toHaveURL(new RegExp(`/concepts/${status.conceptIds[0]}`))
  await expect(page.getByText('Core Loop')).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Open demo project' }).click()
  await expect(page).toHaveURL(new RegExp(`/projects/${status.projectId}`))
  await expect(page.getByRole('heading', { name: /Demo Puzzle Adventure/ })).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Open project Knowledge Graph' }).click()
  await expect(page).toHaveURL(new RegExp(`/graph/project/${status.projectId}`))
  await expect(page.getByText('[Demo]', { exact: false }).first()).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Open demo discussion' }).click()
  await expect(page).toHaveURL(new RegExp(`/forum/threads/${status.forumThreadId}`))
  await expect(page.getByText('[Demo] Discussion: how should the first puzzle teach feedback?')).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('link', { name: /Demo Capture and Convert/i }).click()
  await expect(page).toHaveURL(/\/use-cases\/note-taker-capture-convert$/)
  await expect(page.getByRole('heading', { name: 'Note-Taker Mode: Capture and convert to note, action, and quote' })).toBeVisible()
  await page.goto('/demo')

  await page.getByRole('button', { name: 'Reset Demo' }).click()
  await expect(page.getByRole('heading', { name: 'Demo workspace is ready' })).toBeVisible()

  await page.getByRole('button', { name: 'Delete Demo Data' }).click()
  await expect(page.getByRole('heading', { name: 'Start a safe practice workspace' })).toBeVisible()

  const deletedStatus = await apiGet<DemoStatus>(request, '/demo/status', token)
  expect(deletedStatus.active).toBe(false)
})
