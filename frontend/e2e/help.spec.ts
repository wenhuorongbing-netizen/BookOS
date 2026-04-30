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
}

test('help glossary and contextual drawer explain advanced BookOS terms', async ({ page, request }) => {
  await createHelpSession(page, request)

  await page.goto('/help')
  await expect(page.getByRole('heading', { name: 'Learn BookOS terms' })).toBeVisible()
  await expect(page.getByRole('link', { name: /Source Reference/ })).toBeVisible()
  await expect(page.getByRole('link', { name: /AI Draft/ })).toBeVisible()

  await page.getByRole('link', { name: /Source Reference/ }).click()
  await expect(page).toHaveURL(/\/help\/source-reference/)
  await expect(page.getByRole('heading', { name: 'Source Reference' })).toBeVisible()
  await expect(page.getByText('If a page is unknown, BookOS should say page unknown')).toBeVisible()

  await page.goto('/dashboard')
  const quickCaptureHelp = page.getByRole('button', { name: 'Help: Quick Capture' })
  await quickCaptureHelp.focus()
  await expect(quickCaptureHelp).toBeFocused()
  await quickCaptureHelp.click()
  await expect(page.getByRole('heading', { name: 'Quick Capture' }).last()).toBeVisible()
  await expect(page.getByText('The fastest way to save a raw reading thought')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Open full help' })).toBeVisible()
})
