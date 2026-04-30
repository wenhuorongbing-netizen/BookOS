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
}

test('use-case library routes open and link to real workflows', async ({ page, request }) => {
  await createCompletedUser(page, request)

  await page.goto('/dashboard')
  await page.getByRole('button', { name: /Search all routes/i }).click()
  await page.getByLabel('Global search query').fill('use cases')
  await expect(page.getByRole('option', { name: /Use Cases/i })).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(page.getByRole('heading', { name: 'Learn this dashboard through a workflow' })).toBeVisible()
  await page.getByRole('link', { name: 'Start this use case' }).first().click()
  await expect(page).toHaveURL(/\/use-cases\//)

  await page.goto('/use-cases')
  await expect(page.getByRole('heading', { name: 'Use BookOS by workflow, not by module' })).toBeVisible()
  await expect(page.getByText('No fake completion state')).toBeVisible()
  await expect(page.getByRole('link', { name: 'Open use case' })).toHaveCount(useCases.length)

  for (const useCase of useCases) {
    await page.goto(`/use-cases/${useCase.slug}`)
    await expect(page.getByRole('heading', { name: useCase.title })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Who this is for' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'What source references will be preserved' })).toBeVisible()
    await expect(page.getByRole('link', { name: useCase.primaryLabel }).first()).toHaveAttribute('href', useCase.primaryRoute)
  }

  const projectUseCase = useCases.find((useCase) => useCase.slug === 'apply-quote-to-game-project')
  expect(projectUseCase).toBeTruthy()
  await page.goto(`/use-cases/${projectUseCase!.slug}`)
  await page.getByRole('link', { name: projectUseCase!.primaryLabel }).first().click()
  await expect(page).toHaveURL(/\/quotes$/)

  await page.goto('/graph')
  await expect(page.getByRole('heading', { name: 'Make graph exploration practical' })).toBeVisible()

  await page.goto('/import-export/export')
  await expect(page.getByRole('heading', { name: 'Export or import without losing source rules' })).toBeVisible()
})
