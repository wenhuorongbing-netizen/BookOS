import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiGet, apiPost, apiPut, setSession, uniqueRunId, type AuthPayload } from './support/api'

async function createOnboardedUser(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()
  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `First Loop ${runId}`,
    email: `first-loop-${runId}@bookos.local`,
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

test('fresh user completes first valuable loop without inventing page numbers', async ({ page, request }) => {
  const auth = await createOnboardedUser(page, request)
  const runId = uniqueRunId()
  const captureText = `A first loop quote-like thought ${runId} about source trust. #quote`

  await page.goto('/guided/first-loop')
  await expect(page.getByRole('heading', { name: 'Create one source-backed record' })).toBeVisible()

  await page.getByLabel('Book title').fill(`First Loop Book ${runId}`)
  await page.getByLabel('Author').fill('BookOS E2E')
  await page.getByRole('button', { name: 'Create and use this book' }).click()
  await expect(page.getByRole('heading', { name: 'Set this book as currently reading' })).toBeVisible()
  await expect(page.getByText('Reading state saved')).toBeVisible()
  await page.getByRole('button', { name: 'Continue' }).click()

  await expect(page.getByRole('heading', { name: 'Capture one original thought' })).toBeVisible()
  await page.getByLabel('Capture text').fill(captureText)
  await expect(page.getByText('page unknown, stored as null')).toBeVisible()
  await page.getByRole('button', { name: 'Save capture' }).click()

  await expect(page.getByRole('heading', { name: 'Process the capture' })).toBeVisible()
  await page.getByRole('group', { name: 'Conversion target' }).getByRole('button', { name: 'Convert to Quote' }).click()
  await page.getByRole('button', { name: 'Convert to Quote' }).last().click()
  await expect(page.getByRole('heading', { name: 'Open the source' })).toBeVisible()

  const quotes = await apiGet<Array<{ text: string; pageStart: number | null; sourceReference: { pageStart: number | null } | null }>>(
    request,
    '/quotes',
    auth.token,
  )
  const createdQuote = quotes.find((quote) => quote.text.includes(runId))
  expect(createdQuote, 'Expected guided loop to create a quote.').toBeTruthy()
  expect(createdQuote!.pageStart).toBeNull()
  expect(createdQuote!.sourceReference?.pageStart ?? null).toBeNull()

  await page.getByRole('button', { name: 'Open source drawer' }).click()
  await expect(page.getByRole('heading', { name: 'Choose the next path' })).toBeVisible()
  await expect(page.getByText('Loop complete')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Source Link' })).toBeVisible()
  await expect(page.getByText('Page unknown').first()).toBeVisible()
})

test('existing user can start first loop with an existing book', async ({ page, request }) => {
  const auth = await createOnboardedUser(page, request)
  const runId = uniqueRunId()
  const book = await apiPost<{ id: number; title: string }>(request, '/books', auth.token, {
    title: `Existing First Loop Book ${runId}`,
    subtitle: null,
    description: null,
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: null,
    visibility: 'PRIVATE',
    authors: ['BookOS E2E'],
    tags: [],
  })
  await apiPost(request, `/books/${book.id}/add-to-library`, auth.token, { readingStatus: 'BACKLOG' })

  await page.goto('/guided/first-loop')
  await expect(page.getByRole('heading', { name: 'Add or choose a source book' })).toBeVisible()
  await page.getByLabel('Choose an existing book').selectOption(String(book.id))
  await expect(page.getByLabel('Choose an existing book')).toHaveValue(String(book.id))
  await page.getByRole('button', { name: 'Continue' }).click()
  await expect(page.getByRole('heading', { name: 'Set this book as currently reading' })).toBeVisible()
  await page.getByRole('button', { name: 'Set to Currently Reading' }).click()
  await expect(page.getByRole('heading', { name: 'Capture one original thought' })).toBeVisible()
})
