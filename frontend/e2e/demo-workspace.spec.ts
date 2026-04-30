import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiGet, apiPut, apiPost, setSession, type AuthPayload, uniqueRunId } from './support/api'

interface DemoStatus {
  active: boolean
  lastResetAt: string | null
  bookId: number | null
  projectId: number | null
  quoteId: number | null
  actionItemId: number | null
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
  expect(status.conceptIds).toHaveLength(4)
  expect(status.recordCounts.BOOK).toBe(1)
  expect(status.recordCounts.RAW_CAPTURE).toBeGreaterThanOrEqual(4)
  expect(status.includedRecordTypes).toContain('BOOK')
  expect(status.includedRecordTypes).toContain('GAME_PROJECT')
  expect(status.excludedFromAnalyticsByDefault).toBe(true)

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
