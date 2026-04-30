import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiGet, apiPost, apiPut, setSession, type AuthPayload, uniqueRunId } from './support/api'

async function createSessionWithOnboardingComplete(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()
  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Capture Guide ${runId}`,
    email: `capture-guide-${runId}@bookos.local`,
    password: 'Password123!',
  })

  const user = await apiPut<AuthPayload['user']>(request, '/users/me/onboarding', auth.token, {
    onboardingCompleted: true,
    primaryUseCase: 'QUICK_CAPTURE',
    startingMode: 'NOTE_TAKER',
    preferredDashboardMode: 'NOTE_TAKER',
  })

  await setSession(page, { token: auth.token, user })
  return { token: auth.token, runId }
}

test('quick capture guide teaches syntax without saving examples automatically', async ({ page, request }) => {
  const { token, runId } = await createSessionWithOnboardingComplete(page, request)

  const book = await apiPost<{ id: number }>(request, '/books', token, {
    title: `Capture Guide Book ${runId}`,
    subtitle: null,
    description: 'Original E2E metadata for guided capture.',
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: 'E2E',
    visibility: 'PRIVATE',
    authors: ['QA Automation'],
    tags: ['capture-guide'],
  })
  await apiPost(request, `/books/${book.id}/add-to-library`, token, {
    readingStatus: 'CURRENTLY_READING',
  })

  await page.goto('/dashboard')
  const captureHeading = page.getByRole('heading', { name: 'Capture one reading thought' })
  await captureHeading.scrollIntoViewIfNeeded()
  await expect(captureHeading).toBeVisible()
  await expect(page.getByLabel('Parser examples')).toBeVisible()
  await page.getByRole('combobox', { name: 'Current book' }).selectOption('0')
  await expect(page.getByText('Choose a book before saving so BookOS can preserve the source link.')).toBeVisible()

  await page.goto(`/books/${book.id}`)
  await expect(page.getByRole('heading', { name: 'Learn the parser by trying safe examples' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Insert quote example' })).toBeVisible()

  await page.getByRole('button', { name: 'Insert quote example' }).click()
  await expect(page.getByLabel('Quick capture text')).toHaveValue(/Readable feedback matters in a prototype\./)
  await expect(page.getByText('Example inserted. Edit it if needed, then press Capture to save it.')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'What BookOS will preserve' })).toBeVisible()
  await expect(page.getByText('Source link preview')).toBeVisible()
  await expect(page.getByText('Game Feel').first()).toBeVisible()

  const capturesAfterExample = await apiGet<unknown[]>(request, `/captures/inbox?bookId=${book.id}`, token)
  expect(capturesAfterExample).toHaveLength(0)

  await page.getByLabel('Quick capture text').fill('\uD83D\uDCAC p.xx Malformed page example #quote [[Parser Warning]]')
  await expect(page.getByText('Page marker looks malformed.')).toBeVisible()

  await page.getByRole('button', { name: 'Beginner mode' }).click()
  await page.locator('.structured-capture').getByText('Idea / inspiration').click()
  await page.getByRole('option', { name: 'Action item' }).click()
  await page.getByLabel('Beginner page marker').fill('\u7B2C80\u9875')
  await page.getByLabel('Beginner capture content').fill(`Playtest the capture guide flow ${runId}`)
  await page.getByLabel('Beginner tags').fill('todo, parser')
  await page.getByLabel('Beginner concepts').fill('Feedback Loop, Core Loop')

  await expect(page.getByText('\u7B2C80\u9875')).toBeVisible()
  await expect(page.getByText('[[Feedback Loop]]').last()).toBeVisible()
  await expect(page.getByText('p.80').first()).toBeVisible()

  await page.getByRole('button', { name: 'Submit quick capture' }).click()
  await expect(page.getByRole('status').filter({ hasText: /Captured .* for Capture Guide Book/ }).first()).toBeVisible()
  const nextSteps = page.getByRole('region', { name: 'Post-save quick capture actions' })
  await expect(nextSteps.getByRole('heading', { name: 'Convert or review this capture now' })).toBeVisible()
  await expect(nextSteps.getByRole('button', { name: 'Convert to Note' })).toBeVisible()
  await expect(nextSteps.getByRole('button', { name: 'Convert to Quote' })).toBeVisible()
  await expect(nextSteps.getByRole('button', { name: 'Convert to Action' })).toBeVisible()
  await expect(nextSteps.getByRole('button', { name: 'Review Concept' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Convert to Action' })).toHaveCount(1)

  const savedCaptures = await apiGet<unknown[]>(request, `/captures/inbox?bookId=${book.id}`, token)
  expect(savedCaptures).toHaveLength(1)
})
