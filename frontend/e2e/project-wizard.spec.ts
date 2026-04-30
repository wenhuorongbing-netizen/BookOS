import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiGet, apiPost, apiPut, setSession, type AuthPayload, uniqueRunId } from './support/api'

async function createSessionWithOnboardingComplete(page: Page, request: APIRequestContext) {
  const runId = uniqueRunId()
  const auth = await apiPost<AuthPayload>(request, '/auth/register', null, {
    displayName: `Project Wizard ${runId}`,
    email: `project-wizard-${runId}@bookos.local`,
    password: 'Password123!',
  })

  const user = await apiPut<AuthPayload['user']>(request, '/users/me/onboarding', auth.token, {
    onboardingCompleted: true,
    primaryUseCase: 'PROJECT_APPLICATION',
    startingMode: 'GAME_DESIGNER',
    preferredDashboardMode: 'GAME_DESIGNER',
  })

  await setSession(page, { token: auth.token, user })
  return { token: auth.token, runId }
}

test('project wizard turns a source-backed quote into project action only after confirmation', async ({ page, request }) => {
  const { token, runId } = await createSessionWithOnboardingComplete(page, request)
  const bookTitle = `Wizard Source Book ${runId}`
  const quoteText = `Feedback should teach the player ${runId}`
  const projectTitle = `Wizard Project ${runId}`
  const problemTitle = `Wizard problem ${runId}`
  const applicationTitle = `Wizard application ${runId}`
  const decisionTitle = `Wizard decision ${runId}`
  const planTitle = `Wizard playtest plan ${runId}`
  const findingTitle = `Wizard finding ${runId}`
  const iterationNote = `Wizard iteration note ${runId}`

  const book = await apiPost<{ id: number }>(request, '/books', token, {
    title: bookTitle,
    subtitle: null,
    description: 'Original project wizard E2E metadata.',
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: 'E2E',
    visibility: 'PRIVATE',
    authors: ['QA Automation'],
    tags: ['project-wizard'],
  })
  await apiPost(request, `/books/${book.id}/add-to-library`, token, { readingStatus: 'CURRENTLY_READING' })

  const capture = await apiPost<{ id: number }>(request, '/captures', token, {
    bookId: book.id,
    rawText: `\uD83D\uDCAC p.21 "${quoteText}" #quote [[Feedback Loop]]`,
  })
  const conversion = await apiPost<{ targetId: number }>(request, `/captures/${capture.id}/convert/quote`, token)
  const quote = await apiGet<{ id: number; sourceReference: { id: number } | null }>(request, `/quotes/${conversion.targetId}`, token)
  expect(quote.sourceReference?.id).toBeTruthy()
  const sourceReferenceId = quote.sourceReference?.id ?? 0

  const project = await apiPost<{ id: number }>(request, '/projects', token, {
    title: projectTitle,
    description: 'Project wizard E2E project.',
    genre: 'Puzzle',
    platform: 'Web',
    stage: 'PROTOTYPE',
    visibility: 'PRIVATE',
    progressPercent: 10,
  })

  await test.step('canceling the wizard before final confirmation creates no project records', async () => {
    await page.goto(`/projects/${project.id}/wizard/apply-knowledge?sourceType=QUOTE&sourceId=${quote.id}&sourceReferenceId=${sourceReferenceId}`)
    await expect(page.getByRole('heading', { name: `Apply reading knowledge to ${projectTitle}` })).toBeVisible()
    await expect(page.getByText(quoteText).first()).toBeVisible()

    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await page.getByLabel('What issue are you trying to solve?').fill(problemTitle)
    await page.getByRole('button', { name: 'Save Draft' }).click()
    await expect(page.getByText('Wizard draft saved on this device. No project records were created.')).toBeVisible()
    await page.getByRole('button', { name: 'Cancel' }).click()
    await page.getByRole('button', { name: 'Cancel Wizard' }).click()
    await expect(page).toHaveURL(new RegExp(`/projects/${project.id}`))

    const problems = await apiGet<unknown[]>(request, `/projects/${project.id}/problems`, token)
    const applications = await apiGet<unknown[]>(request, `/projects/${project.id}/applications`, token)
    expect(problems).toHaveLength(0)
    expect(applications).toHaveLength(0)
  })

  await test.step('confirming the wizard creates source-backed project records', async () => {
    await page.goto(`/projects/${project.id}/wizard/apply-knowledge?sourceType=QUOTE&sourceId=${quote.id}&sourceReferenceId=${sourceReferenceId}`)
    await expect(page.getByLabel('What issue are you trying to solve?')).toHaveValue(problemTitle)

    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await page.getByLabel('How does this source help?').fill(applicationTitle)
    await page.getByLabel('Design note').fill('Use the quote to drive a feedback-loop prototype change.')

    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await page.getByLabel('Decision title').fill(decisionTitle)
    await page.getByLabel('Decision', { exact: true }).fill('Make input feedback immediate and readable.')
    await page.getByLabel('Rationale').fill('The selected quote points to feedback as teaching.')
    await page.getByLabel('Tradeoffs').fill('More feedback may add visual noise.')

    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await page.getByLabel('Playtest plan title').fill(planTitle)
    await page.getByLabel('Hypothesis').fill('Players will understand the action faster with immediate feedback.')
    await page.getByRole('textbox', { name: 'Task', exact: true }).fill('Ask the player to trigger the action three times.')
    await page.getByLabel('Success criteria').fill('Player identifies the feedback within one attempt.')

    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await page.getByLabel('Finding title').fill(findingTitle)
    await page.getByLabel('Observation').fill('Players need a stronger first response cue.')
    await page.getByLabel('Recommendation').fill('Add a brighter pulse on action acknowledgement.')
    await page.getByRole('textbox', { name: 'Iteration note', exact: true }).fill(iterationNote)

    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await expect(page.getByText('Nothing has been created yet')).toBeVisible()
    await page.getByRole('button', { name: 'Confirm and Create Records' }).click()
    await expect(page.getByRole('heading', { name: 'Guided project records created' })).toBeVisible()
    await expect(page.getByText('Source preserved').first()).toBeVisible()

    const problems = await apiGet<Array<{ title: string; relatedSourceReference: { id: number } | null }>>(request, `/projects/${project.id}/problems`, token)
    const applications = await apiGet<Array<{ title: string; sourceReference: { id: number } | null }>>(request, `/projects/${project.id}/applications`, token)
    const decisions = await apiGet<Array<{ title: string; sourceReference: { id: number } | null }>>(request, `/projects/${project.id}/decisions`, token)
    const plans = await apiGet<Array<{ title: string }>>(request, `/projects/${project.id}/playtest-plans`, token)
    const findings = await apiGet<Array<{ title: string; sourceReference: { id: number } | null }>>(request, `/projects/${project.id}/playtest-findings`, token)
    const links = await apiGet<Array<{ relationshipType: string; note: string | null; sourceReference: { id: number } | null }>>(request, `/projects/${project.id}/knowledge-links`, token)

    expect(problems.find((item) => item.title === problemTitle)?.relatedSourceReference?.id).toBe(sourceReferenceId)
    expect(applications.find((item) => item.title === applicationTitle)?.sourceReference?.id).toBe(sourceReferenceId)
    expect(decisions.find((item) => item.title === decisionTitle)?.sourceReference?.id).toBe(sourceReferenceId)
    expect(plans.some((item) => item.title === planTitle)).toBe(true)
    expect(findings.find((item) => item.title === findingTitle)?.sourceReference?.id).toBe(sourceReferenceId)
    expect(links.find((item) => item.relationshipType === 'ITERATION_NOTE' && item.note === iterationNote)?.sourceReference?.id).toBe(sourceReferenceId)

    await page.getByRole('button', { name: 'Open Project Cockpit' }).first().click()
    await expect(page).toHaveURL(new RegExp(`/projects/${project.id}`))
    await expect(page.getByText(applicationTitle).first()).toBeVisible()
    await expect(page.getByText(decisionTitle).first()).toBeVisible()
  })
})
