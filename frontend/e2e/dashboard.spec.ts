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
  const primaryActions = page.getByRole('region', { name: 'Primary dashboard actions' })
  await expect(primaryActions.getByRole('article')).toHaveCount(3)
  await expect(primaryActions.getByRole('heading', { name: 'Add Book' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'First Valuable Loop' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Demo Workspace' })).toBeVisible()
  await expect(primaryActions.getByRole('button', { name: 'Add Book' })).toBeVisible()
  await expect(primaryActions.getByRole('button', { name: 'Practice in Demo' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'No current reading book' })).toHaveCount(0)
  await expect(page.getByRole('heading', { name: 'Capture one reading thought' })).toHaveCount(0)
  await expect(page.getByRole('heading', { name: 'Learning Loop' })).toHaveCount(0)

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
  await expect(primaryActions.getByRole('article')).toHaveCount(3)
  await expect(primaryActions.getByRole('heading', { name: 'Continue Reading' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Quick Capture' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Process Captures' })).toBeVisible()
  const currentBookSelector = page.getByRole('button', { name: `Current book selector: ${bookTitle}` })
  const currentBookContext = page.getByLabel('Current book context')
  await expect(currentBookSelector).toBeVisible()
  await currentBookSelector.click()
  await expect(page.getByText(`Defaulting to your currently-reading book: ${bookTitle}.`)).toBeVisible()
  await expect(currentBookContext.getByRole('button', { name: 'Open current book' })).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible()
  await expect(page.getByRole('progressbar', { name: '25% read' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Capture one reading thought' })).toBeVisible()
  await expect(page.getByLabel('Parser examples')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Today', exact: true })).toHaveCount(0)

  await page.getByLabel('Capture text').fill(captureText)
  await page.getByRole('button', { name: 'Save capture' }).click()
  await expect(page.getByText('Capture saved and parsed.')).toBeVisible()
  await expect(primaryActions.getByRole('article')).toHaveCount(3)
  await expect(primaryActions.getByRole('heading', { name: 'Process Captures' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Quote', exact: true })).toBeVisible()
  await expect(page.getByText('p.8').first()).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Process Captures' })).toBeVisible()
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

test('dashboard mode switcher changes landing focus and persists after reload', async ({ page, request }) => {
  const runId = uniqueRunId()
  const bookTitle = `Mode Dashboard Book ${runId}`
  const projectTitle = `Mode Dashboard Project ${runId}`

  await registerAndSkipOnboarding(page, runId)
  await expect(page.getByRole('heading', { name: 'Reader Mode landing' })).toBeVisible()
  await expect(page.getByText('Why am I seeing this?')).toBeVisible()
  const primaryActions = page.getByRole('region', { name: 'Primary dashboard actions' })
  await expect(primaryActions.getByRole('article')).toHaveCount(3)

  const token = await tokenFromPage(page)
  const book = await apiPost<{ id: number }>(request, '/books', token, {
    title: bookTitle,
    subtitle: null,
    description: 'Original dashboard mode-switch metadata.',
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: 'E2E',
    visibility: 'PRIVATE',
    authors: ['QA Automation'],
    tags: ['dashboard'],
  })
  await apiPost(request, `/books/${book.id}/add-to-library`, token, {
    readingStatus: 'CURRENTLY_READING',
  })
  await apiPost(request, '/projects', token, {
    title: projectTitle,
    description: 'Original mode-switch project.',
    genre: 'Puzzle',
    platform: 'Web',
    stage: 'PROTOTYPE',
    visibility: 'PRIVATE',
    progressPercent: 10,
  })
  await page.reload()

  const switcher = page.getByRole('group', { name: 'Switch dashboard mode' })
  await switcher.getByRole('button', { name: 'Game Designer Mode' }).click()

  await expect(page.getByText('Dashboard changed to Game Designer Mode.')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Game Designer Mode landing' })).toBeVisible()
  await expect(primaryActions.getByRole('article')).toHaveCount(3)
  await expect(primaryActions.getByRole('heading', { name: 'Project Focus', exact: true })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Quick Capture', exact: true })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Process Captures', exact: true })).toBeVisible()
  await expect(page.getByRole('navigation', { name: 'Main navigation' }).getByRole('link', { name: 'Projects', exact: true })).toBeVisible()

  await page.reload()
  await expect(page.getByRole('heading', { name: 'Game Designer Mode landing' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Project Focus', exact: true })).toBeVisible()

  await switcher.getByRole('button', { name: 'Advanced Mode' }).click()
  await expect(page.getByRole('heading', { name: 'Advanced Mode landing' })).toBeVisible()
  await expect(primaryActions.getByRole('article')).toHaveCount(3)
  await expect(primaryActions.getByRole('heading', { name: 'Advanced tools need source-backed data' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Quick Capture' })).toBeVisible()
  await expect(primaryActions.getByRole('heading', { name: 'Draft Assistant' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Advanced tools', exact: true })).toBeVisible()
  await expect(page.getByRole('navigation', { name: 'Main navigation' }).getByRole('link', { name: 'Knowledge Graph', exact: true })).toBeVisible()
})
