import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiPost, apiPut, setSession, type AuthPayload, uniqueRunId } from './support/api'

async function createSessionWithOnboardingComplete(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()

  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Task First Reader ${runId}`,
    email: `task-first-${runId}@bookos.local`,
    password: 'Password123!',
  })

  const user = await apiPut<AuthPayload['user']>(request, '/users/me/onboarding', auth.token, {
    onboardingCompleted: true,
    primaryUseCase: null,
    startingMode: null,
    preferredDashboardMode: 'DEFAULT',
  })

  await setSession(page, { token: auth.token, user })
}

test('core workspaces present one obvious first action without fake data', async ({ page, request }) => {
  await createSessionWithOnboardingComplete(page, request)

  await page.goto('/my-library')
  await expect(page.getByRole('heading', { name: 'Choose what you are reading now' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Add one real book' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Add first book' }).first()).toBeVisible()

  await page.goto('/captures/inbox')
  await expect(page.getByRole('heading', { name: 'Capture Inbox' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Capture one reading thought first' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open Library' }).first()).toBeVisible()

  await page.goto('/notes')
  await expect(page.getByRole('heading', { name: 'Book Notes' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Choose a source book' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open Library' }).first()).toBeVisible()

  await page.goto('/projects')
  await expect(page.getByRole('heading', { name: 'Projects', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Create a project for one real design problem' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Create Project' }).first()).toBeVisible()

  await page.goto('/review')
  await expect(page.getByRole('heading', { name: 'Source-backed review', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Start with one small review session' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Create First Review' })).toBeVisible()
})
