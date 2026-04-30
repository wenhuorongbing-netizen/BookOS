import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { apiGet, apiPost, tokenFromPage, uniqueRunId } from './support/api'

type ModeName = 'Reader Mode' | 'Note-Taker Mode' | 'Game Designer Mode' | 'Researcher Mode' | 'Community Mode' | 'Advanced Mode'

interface RegisteredModeUser {
  token: string
  runId: string
  displayName: string
}

interface BookRecord {
  id: number
  userBookId?: number
}

interface QuoteRecord {
  id: number
  text: string
  sourceReference: { id: number; pageStart: number | null } | null
}

async function registerAndChooseMode(
  page: Page,
  request: APIRequestContext,
  mode: ModeName,
  useCaseName: RegExp,
): Promise<RegisteredModeUser> {
  const runId = uniqueRunId()
  const displayName = `Usability ${mode.replace(/\s+/g, '')} ${runId}`
  const email = `usability-${mode.toLowerCase().replace(/[^a-z0-9]+/g, '-')}-${runId}@bookos.local`

  await page.goto('/register')
  await page.getByLabel('Display Name').fill(displayName)
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill('Password123!')
  await page.getByRole('button', { name: 'Register' }).click()
  await expect(page).toHaveURL(/\/onboarding/)
  await expect(page.getByRole('heading', { name: 'Shape BookOS around your workflow' })).toBeVisible()

  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: useCaseName }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: new RegExp(mode, 'i') }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: 'Next', exact: true }).click()
  await page.getByRole('button', { name: 'Start workflow' }).click()
  await expect(page).toHaveURL(/\/guided\/first-loop/)

  const token = await tokenFromPage(page)
  const user = await apiGet<{ preferredDashboardMode: string | null }>(request, '/auth/me', token)
  expect(user.preferredDashboardMode).toBe(modeToPreference(mode))
  return { token, runId, displayName }
}

function modeToPreference(mode: ModeName) {
  return mode.toUpperCase().replace(/ MODE$/, '').replace('-', '_').replace(/\s+/g, '_')
}

async function createBookThroughUi(page: Page, title: string, author = 'BookOS Usability E2E') {
  await page.goto('/books/new')
  await page.getByRole('textbox', { name: /^\*?Title$/ }).fill(title)
  await page.getByLabel('Authors').fill(author)
  await page.getByLabel('Category').fill('Usability E2E')
  await page.getByLabel('Description').fill('Original browser usability test metadata. Not seed or demo content.')
  await page.getByRole('button', { name: 'Create Book' }).click()
  await expect(page).toHaveURL(/\/books\/\d+$/)
  const bookId = Number(new URL(page.url()).pathname.split('/').pop())
  expect(bookId).toBeGreaterThan(0)

  const addToLibrary = page.getByRole('button', { name: 'Add to Library' }).first()
  if (await addToLibrary.isVisible()) {
    await addToLibrary.click()
    await expect(page.getByText('Book added to your library.')).toBeVisible()
  }

  return bookId
}

async function createBookThroughApi(request: APIRequestContext, token: string, runId: string, titlePrefix: string) {
  const book = await apiPost<BookRecord>(request, '/books', token, {
    title: `${titlePrefix} ${runId}`,
    subtitle: null,
    description: 'Original browser usability test metadata. Not seed or demo content.',
    isbn: null,
    publisher: null,
    publicationYear: null,
    coverUrl: null,
    category: 'Usability E2E',
    visibility: 'PRIVATE',
    authors: ['BookOS Usability E2E'],
    tags: ['usability-e2e'],
  })
  await apiPost(request, `/books/${book.id}/add-to-library`, token, { readingStatus: 'CURRENTLY_READING' })
  return book
}

async function createSourceBackedQuote(request: APIRequestContext, token: string, bookId: number, rawText: string) {
  const capture = await apiPost<{ id: number }>(request, '/captures', token, { bookId, rawText })
  const conversion = await apiPost<{ targetId: number }>(request, `/captures/${capture.id}/convert/quote`, token)
  const quote = await apiGet<QuoteRecord>(request, `/quotes/${conversion.targetId}`, token)
  expect(quote.sourceReference?.id).toBeTruthy()
  return quote
}

function annotatePath(startedAt: number, stepCount: number) {
  test.info().annotations.push({
    type: 'usability-path',
    description: `${stepCount} user-visible steps in ${Math.round((Date.now() - startedAt) / 1000)}s`,
  })
}

test.describe('first 15 minutes usability paths by mode', () => {
  test('Reader Mode: add book, set reading, capture, convert to quote, and open source', async ({ page, request }) => {
    const startedAt = Date.now()
    const session = await registerAndChooseMode(page, request, 'Reader Mode', /track my reading/i)
    const bookTitle = `Reader First 15 Book ${session.runId}`
    const quoteText = `Reader source trust thought ${session.runId}`

    await expect(page.getByRole('heading', { name: 'Create one source-backed record' })).toBeVisible()
    await page.getByLabel('Book title').fill(bookTitle)
    await page.getByLabel('Author').fill('BookOS Usability E2E')
    await page.getByRole('button', { name: 'Create and use this book' }).click()

    await expect(page.getByRole('heading', { name: 'Set this book as currently reading' })).toBeVisible()
    await page.getByLabel('Reading progress percent').fill('12')
    await page.getByLabel('Current page if known').fill('15')
    await page.getByRole('button', { name: 'Set to Currently Reading' }).click()
    await page.getByRole('button', { name: '3 Capture Thought' }).click()

    await expect(page.getByRole('heading', { name: 'Capture one original thought' })).toBeVisible()
    await page.getByLabel('Capture text').fill(`\uD83D\uDCAC p.15 "${quoteText}" #quote [[Source Trust]]`)
    await expect(page.getByText('p.15').first()).toBeVisible()
    await page.getByRole('button', { name: 'Save capture' }).click()

    await expect(page.getByRole('heading', { name: 'Process the capture' })).toBeVisible()
    await page.getByRole('group', { name: 'Conversion target' }).getByRole('button', { name: 'Convert to Quote' }).click()
    await page.getByRole('button', { name: 'Convert to Quote' }).last().click()

    await expect(page.getByRole('heading', { name: 'Open the source' })).toBeVisible()
    await page.getByRole('button', { name: 'Open source drawer' }).click()
    await expect(page.getByRole('heading', { name: 'Source Link' })).toBeVisible()
    await expect(page.getByText('p.15').first()).toBeVisible()

    const quotes = await apiGet<QuoteRecord[]>(request, '/quotes', session.token)
    const createdQuote = quotes.find((quote) => quote.text.includes(quoteText))
    expect(createdQuote?.sourceReference?.pageStart).toBe(15)
    annotatePath(startedAt, 7)
  })

  test('Note-Taker Mode: create note, learn capture syntax, convert capture to action, and process inbox', async ({ page, request }) => {
    const startedAt = Date.now()
    const session = await registerAndChooseMode(page, request, 'Note-Taker Mode', /take better book notes/i)
    const bookTitle = `Note Taker First 15 Book ${session.runId}`
    const noteTitle = `Note-taker source note ${session.runId}`
    const actionText = `Process the note-taking queue ${session.runId}`

    const bookId = await createBookThroughUi(page, bookTitle)

    await page.goto(`/books/${bookId}/notes`)
    await expect(page.getByRole('heading', { name: 'Write a source-backed note' })).toBeVisible()
    await page.getByLabel('Note title').fill(noteTitle)
    await page.getByLabel('Markdown content').fill(`# ${noteTitle}\n\n\u2705 p.20 ${actionText} #todo [[Capture Queue]]`)
    await page.getByLabel('Three-sentence summary').fill('The note captures one action. It preserves source context. It stays original.')
    await page.getByRole('button', { name: 'Preview Parser' }).click()
    await expect(page.getByText('p.20').first()).toBeVisible()
    await page.getByRole('button', { name: 'Save Note' }).click()
    await expect(page.getByText('Note saved with parsed source link.').first()).toBeVisible()

    await page.goto(`/books/${bookId}`)
    await expect(page.getByRole('heading', { name: 'Learn the parser by trying safe examples' })).toBeVisible()
    await page.getByRole('button', { name: 'Insert action example' }).click()
    await expect(page.getByText('Example inserted. Edit it if needed, then press Capture to save it.')).toBeVisible()
    await page.getByLabel('Quick capture text').fill(`\u2705 p.21 ${actionText} #todo [[Capture Queue]]`)
    await page.getByRole('button', { name: 'Submit quick capture' }).click()
    await expect(page.getByRole('status').filter({ hasText: /Captured/ }).first()).toBeVisible()

    await page.goto(`/captures/inbox?bookId=${bookId}`)
    await expect(page.getByRole('heading', { name: 'Process Captures' })).toBeVisible()
    const actionCard = page.locator('article').filter({ hasText: actionText }).first()
    await actionCard.getByRole('button', { name: 'Convert to Action' }).first().click()
    const dialog = page.getByRole('dialog', { name: 'Convert to Action' })
    await expect(dialog).toBeVisible()
    await dialog.getByRole('button', { name: 'Convert' }).click()
    await expect(page).toHaveURL(/\/action-items/)
    await expect(page.getByText(actionText).first()).toBeVisible()
    annotatePath(startedAt, 7)
  })

  test('Game Designer Mode: create project, apply quote, create decision, and open project cockpit', async ({ page, request }) => {
    const startedAt = Date.now()
    const session = await registerAndChooseMode(page, request, 'Game Designer Mode', /apply game design knowledge/i)
    const bookId = await createBookThroughUi(page, `Game Designer First 15 Book ${session.runId}`)
    const quoteText = `Feedback should explain the next move ${session.runId}`
    const projectTitle = `First 15 Prototype ${session.runId}`
    const applicationTitle = `Apply feedback quote ${session.runId}`
    const decisionTitle = `Make feedback immediate ${session.runId}`
    const quote = await createSourceBackedQuote(request, session.token, bookId, `\uD83D\uDCAC p.18 "${quoteText}" #quote [[Feedback Loop]]`)

    await page.goto('/projects/new')
    await page.getByRole('textbox', { name: 'Title' }).fill(projectTitle)
    await page.getByLabel('Description').fill('Original game project for browser usability testing.')
    await page.getByLabel('Genre').fill('Puzzle')
    await page.getByLabel('Platform').fill('Web')
    await page.getByRole('button', { name: 'Create Project' }).click()
    await expect(page).toHaveURL(/\/projects\/\d+$/)
    const projectId = Number(new URL(page.url()).pathname.split('/').pop())
    expect(projectId).toBeGreaterThan(0)

    await page.goto(`/quotes/${quote.id}`)
    await page
      .locator('section')
      .filter({ has: page.getByRole('heading', { name: 'Apply this quote to a real project' }) })
      .getByRole('button', { name: 'Apply to Project' })
      .click()
    const applyDialog = page.getByRole('dialog', { name: 'Apply to Project' })
    await expect(applyDialog).toBeVisible()
    await applyDialog.getByLabel('Application title').fill(applicationTitle)
    await applyDialog.getByLabel('Design note').fill('Use this source-backed quote to shape the prototype feedback loop.')
    await applyDialog.getByRole('button', { name: 'Create Application' }).click()
    await expect(page.getByText('Project application created.').first()).toBeVisible()

    await page.goto(`/projects/${projectId}/decisions`)
    await page.getByRole('textbox', { name: 'Title' }).fill(decisionTitle)
    await page.getByLabel('Decision').fill('Show input feedback within the first interaction frame.')
    await page.getByLabel('Rationale').fill('The quote gives source-backed reason to prioritize player feedback.')
    await page.getByLabel('Tradeoffs').fill('More feedback may increase visual noise.')
    await page.getByRole('button', { name: 'Create Decision' }).click()
    await expect(page.getByText('Decision created.').first()).toBeVisible()

    await page.goto(`/projects/${projectId}`)
    await expect(page.getByRole('heading', { name: projectTitle })).toBeVisible()
    await expect(page.getByText(applicationTitle).first()).toBeVisible()
    await expect(page.getByText(decisionTitle).first()).toBeVisible()
    annotatePath(startedAt, 7)
  })

  test('Researcher Mode: review concept marker, open concept graph, and start review session', async ({ page, request }) => {
    const startedAt = Date.now()
    const session = await registerAndChooseMode(page, request, 'Researcher Mode', /turn books into concepts and actions/i)
    const book = await createBookThroughApi(request, session.token, session.runId, 'Researcher First 15 Book')
    const conceptName = `Research Concept ${session.runId}`
    const reviewTitle = `Research review ${session.runId}`

    await page.goto(`/books/${book.id}`)
    await page.getByLabel('Quick capture text').fill(`\uD83E\uDDE9 p.33 Compare this with [[${conceptName}]] #concept`)
    await page.getByRole('button', { name: 'Submit quick capture' }).click()
    await expect(page.getByRole('status').filter({ hasText: /Captured/ }).first()).toBeVisible()

    await page.goto(`/captures/inbox?bookId=${book.id}`)
    const conceptCard = page.locator('article').filter({ hasText: conceptName }).first()
    await conceptCard.getByRole('button', { name: 'Review Concepts' }).click()
    await expect(page.getByRole('dialog', { name: 'Review Parsed Concepts' })).toBeVisible()
    await page.getByRole('button', { name: 'Save Reviewed Concepts' }).click()
    await expect(page.getByText('Parsed concepts reviewed and source links preserved.').first()).toBeVisible()

    const concepts = await apiGet<Array<{ id: number; name: string }>>(request, `/concepts?q=${encodeURIComponent(conceptName)}`, session.token)
    const conceptId = concepts.find((concept) => concept.name === conceptName)?.id ?? 0
    expect(conceptId).toBeGreaterThan(0)

    await page.goto(`/concepts/${conceptId}`)
    await expect(page.getByRole('heading', { name: conceptName })).toBeVisible()
    await page.goto(`/graph/concept/${conceptId}`)
    await expect(page.getByRole('heading', { name: 'Knowledge Graph', exact: true })).toBeVisible()
    await expect(page.getByText(/Real data only|No graph links yet|No Knowledge Graph links/).first()).toBeVisible()

    await page.goto('/review')
    await page.getByLabel('Title').first().fill(reviewTitle)
    await page.getByRole('button', { name: 'Create Empty Session' }).click()
    await expect(page).toHaveURL(/\/review\/\d+/)
    await expect(page.getByRole('heading', { name: reviewTitle })).toBeVisible()
    annotatePath(startedAt, 6)
  })

  test('Community Mode: create source-linked thread, add comment, and open source context', async ({ page, request }) => {
    const startedAt = Date.now()
    const session = await registerAndChooseMode(page, request, 'Community Mode', /discuss books and concepts/i)
    const book = await createBookThroughApi(request, session.token, session.runId, 'Community First 15 Book')
    const quote = await createSourceBackedQuote(
      request,
      session.token,
      book.id,
      `\uD83D\uDCAC p.24 "Discussion should stay attached to source context ${session.runId}" #quote [[Discussion Context]]`,
    )
    const threadTitle = `Source-linked discussion ${session.runId}`
    const comment = `Source-aware reply ${session.runId}`

    await page.goto(
      `/forum/new?relatedEntityType=QUOTE&relatedEntityId=${quote.id}&bookId=${book.id}&sourceReferenceId=${quote.sourceReference!.id}&title=${encodeURIComponent(threadTitle)}`,
    )
    await expect(page.getByRole('heading', { name: 'New Structured Thread' })).toBeVisible()
    await expect(page.getByLabel('Prefilled source context')).toBeVisible()
    await page.getByLabel('Body Markdown').fill('## Source-linked question\n\nHow should this source-backed quote shape the next reading discussion?')
    await page.getByRole('button', { name: 'Create Thread' }).click()
    await expect(page).toHaveURL(/\/forum\/threads\/\d+/)
    await expect(page.getByRole('heading', { name: threadTitle })).toBeVisible()

    await page.getByLabel('Add comment').fill(comment)
    await page.getByRole('button', { name: 'Post Comment' }).click()
    await expect(page.getByText(comment)).toBeVisible()

    await page.getByLabel('Source links').getByRole('button', { name: 'Open Source' }).click()
    await expect(page).toHaveURL(new RegExp(`/books/${book.id}`))
    await expect(page).toHaveURL(/sourceType=SOURCE_REFERENCE/)
    annotatePath(startedAt, 5)
  })

  test('Advanced Mode: open graph, export data, and use MockAIProvider draft safely', async ({ page, request }) => {
    const startedAt = Date.now()
    const session = await registerAndChooseMode(page, request, 'Advanced Mode', /advanced graph\/AI\/import/i)
    const book = await createBookThroughApi(request, session.token, session.runId, 'Advanced First 15 Book')
    await apiPost(request, `/books/${book.id}/notes`, session.token, {
      title: `Advanced draft source note ${session.runId}`,
      markdown: `# Advanced source\n\nThis original note gives MockAIProvider local content for ${session.runId}.`,
      visibility: 'PRIVATE',
      threeSentenceSummary: 'Original note for draft testing. It is local. It is not overwritten.',
    })

    await page.goto('/graph')
    await expect(page.getByRole('heading', { name: 'Knowledge Graph', exact: true })).toBeVisible()
    await expect(page.getByText(/Real data only|No graph links yet|No Knowledge Graph links/).first()).toBeVisible()

    await page.goto('/import-export')
    await expect(page.getByRole('heading', { name: 'Data Portability' })).toBeVisible()
    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Export All JSON' }).click()
    await expect(await download.then((file) => file.suggestedFilename())).toBe('bookos-export.json')

    await page.goto(`/books/${book.id}`)
    const rightRail = page.locator('aside[aria-label="Contextual sidebar"]')
    await expect(rightRail.getByText('MockAIProvider').first()).toBeVisible()
    await rightRail.getByRole('button', { name: 'Generate draft' }).click()
    await expect(page.getByText('MockAIProvider draft created.').first()).toBeVisible()
    await rightRail.getByRole('button', { name: 'Accept' }).first().click()
    await expect(page.getByRole('dialog', { name: 'Accept assistant draft?' })).toBeVisible()
    await page.getByRole('button', { name: 'Accept draft' }).click()
    await expect(page.getByText('Draft suggestion accepted. No user content was overwritten.').first()).toBeVisible()

    await rightRail.getByRole('button', { name: 'Generate draft' }).click()
    await expect(page.getByText('MockAIProvider draft created.').first()).toBeVisible()
    await rightRail.getByRole('button', { name: 'Reject' }).first().click()
    await expect(page.getByText('Draft suggestion rejected.').first()).toBeVisible()
    await expect(page.getByRole('heading', { name: `Advanced First 15 Book ${session.runId}` })).toBeVisible()
    annotatePath(startedAt, 5)
  })
})
