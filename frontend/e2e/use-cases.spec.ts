import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { useCases } from '../src/data/useCases'
import { apiPost, apiPut, setSession, uniqueRunId, type AuthPayload } from './support/api'

async function createCompletedUser(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()

  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Use Case Reader ${runId}`,
    email: `use-cases-${runId}@bookos.local`,
    password: 'Password123!',
  })
  const user = await apiPut<AuthPayload['user']>(request, '/users/me/onboarding', auth.token, {
    onboardingCompleted: true,
    primaryUseCase: 'TRACK_READING',
    startingMode: 'READER',
    preferredDashboardMode: 'READER',
  })
  await setSession(page, { ...auth, user })
  return { ...auth, user }
}

test('use-case library routes open and link to real workflows', async ({ page, request }) => {
  const auth = await createCompletedUser(page, request)

  await page.goto('/dashboard')
  await page.getByRole('button', { name: /Search all routes/i }).click()
  const searchEvent = page.waitForResponse((response) => response.url().includes('/api/use-cases/progress/events') && response.request().method() === 'POST')
  await page.getByLabel('Global search query').fill('use cases')
  await expect(page.getByRole('option', { name: /Use Cases/i })).toBeVisible()
  await searchEvent
  await page.keyboard.press('Escape')
  await expect(page.getByRole('heading', { name: 'Learn this dashboard through a workflow' })).toBeVisible()
  await page.getByRole('link', { name: 'Start this use case' }).first().click()
  await expect(page).toHaveURL(/\/use-cases\//)

  await page.goto('/use-cases')
  await expect(page.getByRole('heading', { name: 'Use BookOS by workflow, not by module' })).toBeVisible()
  await expect(page.getByText('No fake completion state')).toBeVisible()
  await expect(page.getByRole('link', { name: 'Open use case' })).toHaveCount(useCases.length)

  await page.goto('/use-cases/first-15-minutes')
  await expect(page.getByRole('heading', { name: 'First 15 minutes: Add Book, Capture, Process, Open Source' })).toBeVisible()
  await expect(page.getByText('0/4')).toBeVisible()
  await page.getByRole('button', { name: 'Start checklist' }).click()
  await expect(page.getByText('In progress')).toBeVisible()
  await page.getByRole('button', { name: 'Mark manually complete' }).click()
  await expect(page.getByText('1/4')).toBeVisible()
  await page.reload()
  await expect(page.getByText('1/4')).toBeVisible()
  await page.getByRole('button', { name: 'Reset progress' }).click()
  await expect(page.getByText('Not started')).toBeVisible()
  await apiPost(request, '/use-cases/progress/events', auth.token, {
    eventType: 'SOURCE_OPENED',
    contextType: 'QUOTE',
    contextId: 'e2e-source-open',
  })
  await page.reload()
  await expect(page.getByText('1/4')).toBeVisible()

  for (const useCase of useCases) {
    await page.goto(`/use-cases/${useCase.slug}`)
    await expect(page.getByRole('heading', { name: useCase.title })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Who this is for' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'What source links will be preserved' })).toBeVisible()
    await expect(page.getByRole('link', { name: useCase.primaryLabel }).first()).toHaveAttribute('href', useCase.primaryRoute)
  }

  const projectUseCase = useCases.find((useCase) => useCase.slug === 'apply-quote-to-game-project')
  expect(projectUseCase).toBeTruthy()
  await page.goto(`/use-cases/${projectUseCase!.slug}`)
  await page.getByRole('link', { name: projectUseCase!.primaryLabel }).first().click()
  await expect(page).toHaveURL(/\/quotes$/)

  const graphEvent = page.waitForResponse((response) => response.url().includes('/api/use-cases/progress/events') && response.request().method() === 'POST')
  await page.goto('/graph')
  await expect(page.getByRole('heading', { name: 'Connect two accessible records' })).toBeVisible()
  await expect(page.getByText('Workflow guides')).toBeVisible()
  await page.getByText('Workflow guides').click()
  await expect(page.getByRole('heading', { name: 'Make Knowledge Graph exploration practical' })).toBeVisible()
  await graphEvent

  await page.goto('/import-export/export')
  await expect(page.getByRole('heading', { name: 'Export a safe backup first' })).toBeVisible()
  await expect(page.getByText('Workflow guides')).toBeVisible()
  await page.getByText('Workflow guides').click()
  await expect(page.getByRole('heading', { name: 'Export or import without losing source rules' })).toBeVisible()
  const download = page.waitForEvent('download')
  const exportEvent = page.waitForResponse((response) => response.url().includes('/api/use-cases/progress/events') && response.request().method() === 'POST')
  await page.getByRole('button', { name: 'Export All JSON' }).click()
  await download
  await exportEvent

  await page.goto('/use-cases/advanced-mode-search-graph-export-ai')
  await expect(page.getByText('3/4')).toBeVisible()
})
