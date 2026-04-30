import { expect, test } from '@playwright/test'
import { apiGet, apiPost, apiPut, tokenFromPage, uniqueRunId } from './support/api'

test('MVP reading loop works through browser routes and real APIs', async ({ page, request }) => {
  const runId = uniqueRunId()
  const email = `e2e-${runId}@bookos.local`
  const password = 'Password123!'
  const displayName = `E2E Reader ${runId}`
  const bookTitle = `E2E Game Feel Workbook ${runId}`
  const quoteText = `Readable feedback loop ${runId}`
  const actionText = `Test input buffering ${runId}`
  const noteText = `Prototype rhythm idea ${runId}`
  const conceptName = `E2E Core Loop ${runId}`
  const forumTitle = `E2E source discussion ${runId}`
  const projectTitle = `E2E Project ${runId}`
  const projectApplicationTitle = `Apply quote to prototype ${runId}`
  const projectProblemTitle = `Clarify feedback timing ${runId}`
  const decisionTitle = `Use readable input feedback ${runId}`
  const findingTitle = `Players miss timing cue ${runId}`

  await test.step('register a fresh user, log out, and log back in through the browser', async () => {
    await page.goto('/register')
    await page.getByLabel('Display Name').fill(displayName)
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password').fill(password)
    await page.getByRole('button', { name: 'Register' }).click()
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.getByText('Turn reading into source-backed design work')).toBeVisible()

    await page.getByRole('button', { name: new RegExp(`Open profile menu for ${displayName}`) }).click()
    await page.getByRole('menuitem', { name: 'Logout' }).click()
    await expect(page).toHaveURL(/\/login/)

    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password').fill(password)
    await page.getByRole('button', { name: 'Login' }).click()
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.getByText('Turn reading into source-backed design work')).toBeVisible()
  })

  const token = await tokenFromPage(page)

  let bookId = 0
  let userBookId = 0

  await test.step('create a book from the browser and attach it to the personal library', async () => {
    await page.goto('/books/new')
    await page.getByRole('textbox', { name: /^\*?Title$/ }).fill(bookTitle)
    await page.getByLabel('Authors').fill('QA Automation')
    await page.getByLabel('Category').fill('E2E')
    await page.getByLabel('Tags').fill('e2e, smoke')
    await page.getByLabel('Description').fill('Original E2E smoke-test metadata, not production seed content.')
    await page.getByRole('button', { name: 'Create Book' }).click()
    await expect(page).toHaveURL(/\/books\/\d+$/)
    bookId = Number(new URL(page.url()).pathname.split('/').pop())
    expect(bookId).toBeGreaterThan(0)

    await page.getByRole('button', { name: 'Add to Library' }).first().click()
    await expect(page.getByText('Book added to your library.')).toBeVisible()

    const book = await apiGet<{ userBookId: number }>(request, `/books/${bookId}`, token)
    userBookId = book.userBookId
    expect(userBookId).toBeGreaterThan(0)
  })

  await test.step('update reading status, progress, and rating', async () => {
    await apiPut(request, `/user-books/${userBookId}/status`, token, { status: 'CURRENTLY_READING' })
    await apiPut(request, `/user-books/${userBookId}/progress`, token, { progressPercent: 42 })
    await apiPut(request, `/user-books/${userBookId}/rating`, token, { rating: 5 })
    await page.goto(`/books/${bookId}`)
    await expect(page.getByText('CURRENTLY_READING').first()).toBeVisible()
    await expect(page.getByText('42%').first()).toBeVisible()
  })

  let noteId = 0

  await test.step('create a Markdown note and parsed note block', async () => {
    const note = await apiPost<{ id: number }>(request, `/books/${bookId}/notes`, token, {
      title: `E2E Note ${runId}`,
      markdown: `# Reading note\n\nThis note is part of the E2E loop for [[${conceptName}]].`,
      visibility: 'PRIVATE',
      threeSentenceSummary: 'E2E note summary. It is deterministic. It is source-backed.',
    })
    noteId = note.id
    await apiPost(request, `/notes/${noteId}/blocks`, token, {
      rawText: `\uD83D\uDCA1 p.12 Capture this design observation #note [[${conceptName}]]`,
      sortOrder: 1,
    })
    await page.goto(`/notes/${noteId}`)
    await expect(page.getByRole('heading', { name: `E2E Note ${runId}` })).toBeVisible()
    await expect(page.getByText('Parsed Note Blocks')).toBeVisible()
    await expect(page.getByText(`[[${conceptName}]]`).first()).toBeVisible()
  })

  await test.step('quick capture parses emoji, page, tags, and concept markers', async () => {
    await page.goto(`/books/${bookId}`)
    const captureInput = page.getByLabel('Quick capture text')
    await captureInput.fill(`\uD83D\uDCAC p.42 "${quoteText}" #quote [[${conceptName}]]`)
    await expect(page.getByText('Quote').first()).toBeVisible()
    await expect(page.getByText('p.42').first()).toBeVisible()
    await expect(page.getByText('#quote').first()).toBeVisible()
    await expect(page.getByText(`[[${conceptName}]]`).first()).toBeVisible()
    await page.getByRole('button', { name: 'Submit quick capture' }).click()
    await expect(page.getByText(new RegExp(`Captured .*${bookTitle.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`)).first()).toBeVisible()
  })

  await test.step('convert captures to quote, action item, and note', async () => {
    const quoteCard = page.locator('article').filter({ hasText: quoteText }).first()
    await quoteCard.getByRole('button', { name: 'Convert to Quote' }).first().click()
    await expect(page.getByText('Capture converted to a source-backed quote.').first()).toBeVisible()

    await apiPost(request, '/captures', token, {
      bookId,
      rawText: `\u2705 p.43 ${actionText} tomorrow #todo [[${conceptName}]]`,
    })
    await page.goto(`/captures/inbox?bookId=${bookId}`)
    const actionCard = page.locator('article').filter({ hasText: actionText }).first()
    await actionCard.getByRole('button', { name: 'Convert to Action' }).first().click()
    const actionDialog = page.getByRole('dialog', { name: 'Convert to Action Item' })
    await expect(actionDialog).toBeVisible()
    await actionDialog.getByRole('button', { name: 'Convert' }).click()
    await expect(page).toHaveURL(/\/action-items/)
    await expect(page.getByText(actionText).first()).toBeVisible()

    await apiPost(request, '/captures', token, {
      bookId,
      rawText: `\uD83D\uDCA1 p.44 ${noteText} #prototype [[${conceptName}]]`,
    })
    await page.goto(`/captures/inbox?bookId=${bookId}`)
    const noteCard = page.locator('article').filter({ hasText: noteText }).first()
    await noteCard.getByRole('button', { name: 'Convert to Note' }).first().click()
    await expect(page).toHaveURL(/\/notes\/\d+/)
  })

  let quoteId = 0
  let actionItemId = 0
  let sourceReferenceId = 0

  await test.step('verify quotes, action items, and source opening', async () => {
    const quotes = await apiGet<Array<{ id: number; sourceReference: { id: number } | null }>>(request, `/quotes?bookId=${bookId}`, token)
    expect(quotes.length).toBeGreaterThan(0)
    quoteId = quotes[0].id
    sourceReferenceId = quotes[0].sourceReference?.id ?? 0

    await page.goto('/quotes')
    await expect(page.getByText(quoteText).first()).toBeVisible()
    await page.getByText(quoteText).first().click()
    await expect(page).toHaveURL(new RegExp(`/quotes/${quoteId}`))
    await expect(page.getByRole('heading', { name: 'Quote Detail' })).toBeVisible()
    await page.getByRole('button', { name: 'Open Source' }).first().click()
    await expect(page).toHaveURL(new RegExp(`/books/${bookId}`))
    await expect(page).toHaveURL(/sourceType=QUOTE/)

    const actionItems = await apiGet<Array<{ id: number }>>(request, `/action-items?bookId=${bookId}`, token)
    expect(actionItems.length).toBeGreaterThan(0)
    actionItemId = actionItems[0].id
    await page.goto('/action-items')
    await expect(page.getByText(actionText).first()).toBeVisible()
    const actionCard = page.locator('article').filter({ hasText: actionText }).first()
    await actionCard.getByRole('button', { name: 'Complete' }).first().click()
    await expect(page.getByText('Action item completed.').first()).toBeVisible()
    await actionCard.getByRole('button', { name: 'Reopen' }).first().click()
    await expect(page.getByText('Action item reopened.').first()).toBeVisible()
  })

  let conceptId = 0
  let projectId = 0

  await test.step('review parsed concept and open concept detail', async () => {
    const conceptCapture = await apiPost<{ id: number }>(request, '/captures', token, {
      bookId,
      rawText: `\uD83E\uDDE9 p.45 Connect this to [[${conceptName}]] #concept`,
    })
    await page.goto(`/captures/inbox?bookId=${bookId}`)
    const conceptCard = page.locator('article').filter({ hasText: conceptName }).first()
    await conceptCard.getByRole('button', { name: 'Review Concepts' }).click()
    await expect(page.getByRole('dialog', { name: 'Review Parsed Concepts' })).toBeVisible()
    await page.getByRole('button', { name: 'Save Reviewed Concepts' }).click()
    await expect(page.getByText('Parsed concepts reviewed and source references preserved.').first()).toBeVisible()

    const concepts = await apiGet<Array<{ id: number; name: string }>>(request, `/concepts?q=${encodeURIComponent(conceptName)}`, token)
    conceptId = concepts.find((item) => item.name === conceptName)?.id ?? 0
    expect(conceptId).toBeGreaterThan(0)

    await page.goto(`/concepts/${conceptId}`)
    await expect(page.getByRole('heading', { name: conceptName })).toBeVisible()
  })

  await test.step('create a project in the browser', async () => {
    await page.goto('/projects/new')
    await page.getByRole('textbox', { name: 'Title' }).fill(projectTitle)
    await page.getByLabel('Description').fill('E2E project for source-backed reading application.')
    await page.getByLabel('Genre').fill('Puzzle')
    await page.getByLabel('Platform').fill('Web')
    await page.getByRole('button', { name: 'Create Project' }).click()
    await expect(page).toHaveURL(/\/projects\/\d+$/)
    projectId = Number(new URL(page.url()).pathname.split('/').pop())
    expect(projectId).toBeGreaterThan(0)
    await expect(page.getByRole('heading', { name: projectTitle })).toBeVisible()
  })

  await test.step('apply a quote to the project through the browser workflow', async () => {
    await page.goto(`/quotes/${quoteId}`)
    await page.getByRole('button', { name: 'Apply to Project' }).click()
    const dialog = page.getByRole('dialog', { name: 'Apply to Project' })
    await expect(dialog).toBeVisible()
    await dialog.getByLabel('Application title').fill(projectApplicationTitle)
    await dialog.getByLabel('Design note').fill('Use the quote as source-backed evidence for the prototype feedback loop.')
    await dialog.getByRole('button', { name: 'Create Application' }).click()
    await expect(page.getByText('Project application created.').first()).toBeVisible()

    await page.goto(`/projects/${projectId}`)
    await expect(page.getByText(projectApplicationTitle).first()).toBeVisible()
  })

  await test.step('create project problem, design decision, and playtest finding', async () => {
    await page.goto(`/projects/${projectId}/problems`)
    await page.getByRole('textbox', { name: 'Title' }).fill(projectProblemTitle)
    await page.getByLabel('Description').fill('The prototype needs a clearer feedback window.')
    await page.getByRole('button', { name: 'Create Problem' }).click()
    await expect(page.getByText('Problem created.').first()).toBeVisible()
    await expect(page.getByText(projectProblemTitle).first()).toBeVisible()

    await page.goto(`/projects/${projectId}/decisions`)
    await page.getByRole('textbox', { name: 'Title' }).fill(decisionTitle)
    await page.getByLabel('Decision').fill('Make input acknowledgement visible within one frame.')
    await page.getByLabel('Rationale').fill('The quote and action item point to faster feedback as a design need.')
    await page.getByLabel('Tradeoffs').fill('More UI feedback may add visual noise.')
    await page.getByRole('button', { name: 'Create Decision' }).click()
    await expect(page.getByText('Decision created.').first()).toBeVisible()
    await expect(page.getByText(decisionTitle).first()).toBeVisible()

    await page.goto(`/projects/${projectId}/playtests`)
    await page.getByRole('textbox', { name: 'Title' }).first().fill(`E2E Playtest Plan ${runId}`)
    await page.getByLabel('Hypothesis').fill('Players will understand timing better with immediate feedback.')
    await page.getByRole('button', { name: 'Create Plan' }).click()
    await expect(page.getByText('Playtest plan created.').first()).toBeVisible()

    await page.getByRole('textbox', { name: 'Title' }).nth(1).fill(findingTitle)
    await page.getByLabel('Observation').fill('Players missed the first timing cue during the smoke scenario.')
    await page.getByLabel('Recommendation').fill('Add an earlier visual pulse tied to input acknowledgement.')
    await page.getByRole('button', { name: 'Create Finding' }).click()
    await expect(page.getByText('Playtest finding created.').first()).toBeVisible()
    await expect(page.getByText(findingTitle).first()).toBeVisible()
  })

  await test.step('search and graph routes use real data or honest empty state', async () => {
    await page.keyboard.press(process.platform === 'darwin' ? 'Meta+K' : 'Control+K')
    await page.getByLabel('Global search query').fill(bookTitle)
    await expect(page.getByRole('option').filter({ hasText: bookTitle }).first()).toBeVisible()
    await page.keyboard.press('Enter')
    await expect(page).toHaveURL(new RegExp(`/books/${bookId}`))

    await page.goto(`/graph/book/${bookId}`)
    await expect(page.getByRole('heading', { name: 'Knowledge Graph' })).toBeVisible()
    await expect(page.getByText(/Real data only|No graph links yet/)).toBeVisible()

    await page.goto(`/graph/project/${projectId}`)
    await expect(page.getByRole('heading', { name: 'Knowledge Graph' })).toBeVisible()
    await expect(page.getByText(/PROJECT|Real data only|No graph links yet/).first()).toBeVisible()
  })

  await test.step('MockAIProvider draft workflow remains draft-only', async () => {
    await page.goto(`/books/${bookId}`)
    const rightRail = page.locator('aside[aria-label="Contextual sidebar"]')
    await rightRail.getByRole('button', { name: 'Generate draft' }).click()
    await expect(page.getByText('MockAIProvider draft created.').first()).toBeVisible()
    await expect(page.getByText('Draft').first()).toBeVisible()

    await rightRail.getByRole('button', { name: 'Edit' }).first().click()
    await page.getByLabel('Edit draft suggestion').fill(`Edited draft ${runId}`)
    await page.getByRole('button', { name: 'Save edit' }).click()
    await expect(page.getByText('Draft suggestion updated.').first()).toBeVisible()

    await rightRail.getByRole('button', { name: 'Accept' }).first().click()
    await expect(page.getByRole('dialog', { name: 'Accept AI draft?' })).toBeVisible()
    await page.getByRole('button', { name: 'Accept draft' }).click()
    await expect(page.getByText('Draft suggestion accepted. No user content was overwritten.').first()).toBeVisible()

    await rightRail.getByRole('button', { name: 'Generate draft' }).click()
    await expect(page.getByText('MockAIProvider draft created.').first()).toBeVisible()
    await rightRail.getByRole('button', { name: 'Reject' }).first().click()
    await expect(page.getByText('Draft suggestion rejected.').first()).toBeVisible()
  })

  await test.step('create a source-linked forum thread and comment', async () => {
    await page.goto(`/forum/new?relatedEntityType=BOOK&relatedEntityId=${bookId}&bookId=${bookId}&sourceReferenceId=${sourceReferenceId}&title=${encodeURIComponent(forumTitle)}`)
    await page.getByLabel('Body Markdown').fill(`## Source question\n\nHow should this source-backed idea change the prototype?`)
    await page.getByRole('button', { name: 'Create Thread' }).click()
    await expect(page).toHaveURL(/\/forum\/threads\/\d+/)
    await expect(page.getByRole('heading', { name: forumTitle })).toBeVisible()
    await page.getByLabel('Add comment').fill(`Comment from E2E ${runId}`)
    await page.getByRole('button', { name: 'Post Comment' }).click()
    await expect(page.getByText(`Comment from E2E ${runId}`)).toBeVisible()
  })

  await test.step('open export page and export user JSON', async () => {
    await page.goto('/import-export')
    await expect(page.getByRole('heading', { name: 'Data Portability' })).toBeVisible()
    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Export All JSON' }).click()
    await expect(await download.then((file) => file.suggestedFilename())).toBe('bookos-export.json')
  })

  await test.step('logout through the profile menu', async () => {
    await page.getByRole('button', { name: new RegExp(`Open profile menu for ${displayName}`) }).click()
    await page.getByRole('menuitem', { name: 'Logout' }).click()
    await expect(page).toHaveURL(/\/login/)
  })

  expect(actionItemId).toBeGreaterThan(0)
})
