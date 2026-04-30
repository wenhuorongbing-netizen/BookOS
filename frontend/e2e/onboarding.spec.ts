import { expect, test, type Page } from '@playwright/test'
import { apiGet, tokenFromPage, uniqueRunId } from './support/api'

async function registerFreshUser(page: Page, runId: string, displayPrefix: string) {
  await page.goto('/register')
  await page.getByLabel('Display Name').fill(`${displayPrefix} ${runId}`)
  await page.getByLabel('Email').fill(`onboarding-${displayPrefix.toLowerCase().replace(/\s+/g, '-')}-${runId}@bookos.local`)
  await page.getByLabel('Password').fill('Password123!')
  await page.getByRole('button', { name: 'Register' }).click()
  await expect(page).toHaveURL(/\/onboarding/)
  await expect(page.getByRole('heading', { name: 'Shape BookOS around your workflow' })).toBeVisible()
}

test('new user can skip onboarding and return to dashboard', async ({ page }) => {
  const runId = uniqueRunId()
  await registerFreshUser(page, runId, 'Skip User')

  await page.getByRole('button', { name: 'Skip for now' }).click()
  await expect(page).toHaveURL(/\/dashboard/)
  await expect(page.getByRole('heading', { name: 'What should I do today?' })).toBeVisible()

  await page.goto('/dashboard')
  await expect(page).toHaveURL(/\/dashboard/)
  await expect(page.getByRole('heading', { name: 'Shape BookOS around your workflow' })).toHaveCount(0)
})

test('new user can complete Reader Mode onboarding and preferences persist', async ({ page, request }) => {
  const runId = uniqueRunId()
  await registerFreshUser(page, runId, 'Reader User')

  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: /track my reading/i }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: /Reader Mode/i }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await expect(page.getByText('Create your first book')).toBeVisible()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Your next 3 actions' })).toBeVisible()
  await page.getByRole('button', { name: 'Start workflow' }).click()
  await expect(page).toHaveURL(/\/books\/new/)

  const token = await tokenFromPage(page)
  const currentUser = await apiGet<{
    onboardingCompleted: boolean
    primaryUseCase: string | null
    startingMode: string | null
    preferredDashboardMode: string | null
  }>(request, '/auth/me', token)

  expect(currentUser.onboardingCompleted).toBe(true)
  expect(currentUser.primaryUseCase).toBe('TRACK_READING')
  expect(currentUser.startingMode).toBe('READER')
  expect(currentUser.preferredDashboardMode).toBe('READER')
})

test('new user can complete Game Designer Mode onboarding', async ({ page, request }) => {
  const runId = uniqueRunId()
  await registerFreshUser(page, runId, 'Designer User')

  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: /apply game design knowledge/i }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: /Game Designer Mode/i }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await expect(page.getByText('Create a book and a game project')).toBeVisible()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await expect(page.getByText('Apply a quote or concept to the project.')).toBeVisible()
  await page.getByRole('button', { name: 'Start workflow' }).click()
  await expect(page).toHaveURL(/\/books\/new/)

  const token = await tokenFromPage(page)
  const currentUser = await apiGet<{
    onboardingCompleted: boolean
    primaryUseCase: string | null
    startingMode: string | null
  }>(request, '/auth/me', token)

  expect(currentUser.onboardingCompleted).toBe(true)
  expect(currentUser.primaryUseCase).toBe('GAME_PROJECT')
  expect(currentUser.startingMode).toBe('GAME_DESIGNER')
})
