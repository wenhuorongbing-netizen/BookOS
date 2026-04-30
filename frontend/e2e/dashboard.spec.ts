import { expect, test, type Page } from '@playwright/test'
import { apiPost, apiPut, tokenFromPage, uniqueRunId } from './support/api'

async function registerAndSkipOnboarding(page: Page, runId: string) {
  await page.goto('/register')
  await page.getByLabel('Display Name').fill(`Dashboard Reader ${runId}`)
  await page.getByLabel('Email').fill(`dashboard-${runId}@bookos.local`)
  await page.getByLabel('Password').fill('Password123!')
  await page.getByRole('button', { name: 'Register' }).click()
  await expect(page).toHaveURL(/\/onboarding/)
  await page.getByRole('button', { name: 'Skip for now' }).click()
  await expect(page).toHaveURL(/\/dashboard/)
}

test('dashboard guides a user from empty state to book, capture, and project focus', async ({ page, request }) => {
  const runId = uniqueRunId()
  const bookTitle = `Dashboard Source Book ${runId}`
  const projectTitle = `Dashboard Project ${runId}`
  const captureText = `\uD83D\uDCAC p.8 Dashboard quote ${runId} #quote [[Dashboard Concept ${runId}]]`

  await registerAndSkipOnboarding(page, runId)
  await expect(page.getByRole('heading', { name: 'What should I do today?' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Add one real book' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'No current reading book' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Capture one reading thought' })).toBeVisible()

  const token = await tokenFromPage(page)
  const book = await apiPost<{ id: number }>(request, '/books', token, {
    title: bookTitle,
    subtitle: null,
    description: 'Original dashboard E2E metadata.',
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: 'E2E',
    visibility: 'PRIVATE',
    authors: ['QA Automation'],
    tags: ['dashboard'],
  })
  const userBook = await apiPost<{ id: number }>(request, `/books/${book.id}/add-to-library`, token, {
    readingStatus: 'CURRENTLY_READING',
  })
  await apiPut(request, `/user-books/${userBook.id}/progress`, token, { progressPercent: 25 })

  await page.goto('/dashboard')
  await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible()
  await expect(page.getByRole('progressbar', { name: '25% read' })).toBeVisible()

  await page.getByLabel('Capture text').fill(captureText)
  await page.getByRole('button', { name: 'Save capture' }).click()
  await expect(page.getByText('Capture saved and parsed.')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Quote', exact: true })).toBeVisible()
  await expect(page.getByText('p.8').first()).toBeVisible()
  await expect(page.getByRole('button', { name: 'Convert to Quote' })).toBeVisible()

  await apiPost(request, '/projects', token, {
    title: projectTitle,
    description: 'Original project for dashboard smoke testing.',
    genre: 'Puzzle',
    platform: 'Web',
    stage: 'PROTOTYPE',
    visibility: 'PRIVATE',
    progressPercent: 10,
  })

  await page.goto('/dashboard')
  await expect(page.getByRole('heading', { name: projectTitle })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open Project Cockpit' })).toBeVisible()
})
