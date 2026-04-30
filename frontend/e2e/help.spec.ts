import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiPost, apiPut, setSession, type AuthPayload, uniqueRunId } from './support/api'

async function createHelpSession(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()
  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Help Reader ${runId}`,
    email: `help-${runId}@bookos.local`,
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

test('help glossary and contextual drawer explain advanced BookOS terms', async ({ page, request }) => {
  const { token, runId } = await createHelpSession(page, request)

  await page.goto('/help')
  await expect(page.getByRole('heading', { name: 'Learn BookOS terms' })).toBeVisible()
  await expect(page.getByRole('link', { name: /Source Link/ })).toBeVisible()
  await expect(page.getByRole('link', { name: /Draft Assistant/ })).toBeVisible()

  await page.getByRole('link', { name: /Source Link/ }).click()
  await expect(page).toHaveURL(/\/help\/source-reference/)
  await expect(page.getByRole('heading', { name: 'Source Link' })).toBeVisible()
  await expect(page.getByText('If a page is unknown, BookOS should say page unknown')).toBeVisible()

  const book = await apiPost<{ id: number }>(request, '/books', token, {
    title: `Help Dashboard Book ${runId}`,
    subtitle: null,
    description: 'Original E2E metadata for help tooltip visibility.',
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: 'E2E',
    visibility: 'PRIVATE',
    authors: ['QA Automation'],
    tags: ['help'],
  })
  await apiPost(request, `/books/${book.id}/add-to-library`, token, {
    readingStatus: 'CURRENTLY_READING',
  })

  await page.goto('/dashboard')
  const quickCaptureHelp = page.getByRole('button', { name: 'Help: Quick Capture' })
  await quickCaptureHelp.focus()
  await expect(quickCaptureHelp).toBeFocused()
  await quickCaptureHelp.click()
  await expect(page.getByRole('heading', { name: 'Quick Capture' }).last()).toBeVisible()
  await expect(page.getByText('The fastest way to save a raw reading thought')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open full help' })).toBeVisible()
})
