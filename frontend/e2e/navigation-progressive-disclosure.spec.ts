import { expect, test, type Page, type APIRequestContext } from '@playwright/test'
import { apiPost, apiPut, setSession, uniqueRunId, type AuthPayload } from './support/api'

type NavigationMode = 'READER' | 'NOTE_TAKER' | 'GAME_DESIGNER' | 'RESEARCHER' | 'COMMUNITY' | 'ADVANCED'

const modeCases: Array<{
  mode: NavigationMode
  label: string
  primary: string[]
  secondary: string[]
}> = [
  {
    mode: 'READER',
    label: 'Reader Mode',
    primary: ['Dashboard', 'Library', 'Process Captures', 'Notes', 'Review'],
    secondary: ['Quotes', 'Actions', 'Concepts'],
  },
  {
    mode: 'NOTE_TAKER',
    label: 'Note-Taker Mode',
    primary: ['Dashboard', 'Library', 'Process Captures', 'Notes', 'Review'],
    secondary: ['Quotes', 'Actions', 'Concepts', 'Design Knowledge'],
  },
  {
    mode: 'GAME_DESIGNER',
    label: 'Game Designer Mode',
    primary: ['Dashboard', 'Library', 'Process Captures', 'Projects', 'Review'],
    secondary: ['Quotes', 'Concepts', 'Design Knowledge', 'Daily', 'Forum'],
  },
  {
    mode: 'RESEARCHER',
    label: 'Researcher Mode',
    primary: ['Dashboard', 'Library', 'Notes', 'Concepts', 'Review'],
    secondary: ['Design Knowledge', 'Quotes'],
  },
  {
    mode: 'COMMUNITY',
    label: 'Community Mode',
    primary: ['Dashboard', 'Library', 'Process Captures', 'Notes', 'Forum', 'Review'],
    secondary: ['Quotes', 'Concepts', 'Design Knowledge', 'Daily'],
  },
  {
    mode: 'ADVANCED',
    label: 'Advanced Mode',
    primary: ['Dashboard', 'Library', 'Process Captures', 'Notes', 'Projects', 'Concepts', 'Review'],
    secondary: ['Quotes', 'Actions', 'Design Knowledge', 'Daily', 'Forum', 'Use Cases'],
  },
]

async function createModeUser(page: Page, request: APIRequestContext, mode: NavigationMode) {
  const runId = uniqueRunId()
  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Nav ${mode} ${runId}`,
    email: `nav-${mode.toLowerCase()}-${runId}@bookos.local`,
    password: 'Password123!',
  })
  const user = await apiPut<AuthPayload['user']>(request, '/users/me/onboarding', auth.token, {
    onboardingCompleted: true,
    primaryUseCase: mode === 'GAME_DESIGNER' ? 'GAME_PROJECT' : mode === 'COMMUNITY' ? 'COMMUNITY_DISCUSSION' : 'TRACK_READING',
    startingMode: mode,
    preferredDashboardMode: mode,
  })
  await setSession(page, { ...auth, user })
}

function sidebarLink(page: Page, name: string) {
  return page.getByRole('navigation', { name: 'Main navigation' }).getByRole('link', { name, exact: true })
}

for (const modeCase of modeCases) {
  test(`sidebar prioritizes ${modeCase.label}`, async ({ page, request }) => {
    await createModeUser(page, request, modeCase.mode)
    await page.goto('/dashboard')

    await expect(page.getByText(modeCase.label).first()).toBeVisible()
    await expect(page.getByRole('button', { name: /Advanced More|Advanced Less/i })).toBeVisible()

    for (const item of modeCase.primary) {
      await expect(sidebarLink(page, item)).toBeVisible()
    }

    for (const item of modeCase.secondary) {
      await expect(sidebarLink(page, item)).toBeVisible()
    }

    await expect(sidebarLink(page, 'Ontology Import')).toHaveCount(0)
    if (modeCase.mode !== 'ADVANCED') {
      await expect(sidebarLink(page, 'Knowledge Graph')).toHaveCount(0)
    }
  })
}

test('advanced routes stay reachable through More and command palette', async ({ page, request }) => {
  await createModeUser(page, request, 'READER')
  await page.goto('/dashboard')

  await expect(sidebarLink(page, 'Knowledge Graph')).toHaveCount(0)
  await page.getByRole('button', { name: /Advanced More/i }).click()
  await expect(sidebarLink(page, 'Knowledge Graph')).toBeVisible()
  await expect(sidebarLink(page, 'Import / Export')).toBeVisible()

  await page.getByRole('button', { name: /Search all routes/i }).click()
  await page.getByLabel('Global search query').fill('graph')
  await expect(page.getByRole('option', { name: /Knowledge Graph/i })).toBeVisible()
  await page.getByRole('button', { name: 'Open route' }).first().click()
  await expect(page).toHaveURL(/\/graph$/)
  await expect(page.getByRole('navigation', { name: 'Breadcrumb' }).getByText('Knowledge Graph')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Back to main workflow' })).toBeVisible()
})

test('user menu can change navigation mode without removing deep links', async ({ page, request }) => {
  await createModeUser(page, request, 'READER')
  await page.goto('/graph')

  await expect(page).toHaveURL(/\/graph$/)
  await expect(page.getByRole('heading', { name: 'Return to the reading loop' })).toBeVisible()
  await page.getByRole('button', { name: /Open profile menu/i }).click()
  await page.getByRole('menuitemradio', { name: 'Game Designer Mode' }).click()

  await expect(page.getByText('Game Designer Mode').first()).toBeVisible()
  await expect(sidebarLink(page, 'Projects')).toBeVisible()
  await expect(page).toHaveURL(/\/graph$/)
  await expect(page.getByRole('heading', { name: 'Return to project work' })).toBeVisible()
})

test('mobile sidebar keeps primary navigation usable', async ({ page, request }) => {
  await createModeUser(page, request, 'GAME_DESIGNER')
  await page.setViewportSize({ width: 390, height: 860 })
  await page.goto('/dashboard')

  await page.getByRole('button', { name: 'Toggle BookOS navigation menu' }).click()
  await expect(sidebarLink(page, 'Dashboard')).toBeVisible()
  await expect(sidebarLink(page, 'Projects')).toBeVisible()
  await expect(page.getByRole('button', { name: /Search all routes/i })).toBeVisible()
})
