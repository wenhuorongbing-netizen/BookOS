export interface UseCaseStep {
  title: string
  description: string
  route: string
  actionLabel: string
}

export interface UseCaseRelatedLink {
  label: string
  route: string
}

export interface UseCaseTemplate {
  slug: string
  title: string
  summary: string
  audience: string
  goal: string
  timeRequired: string
  category: 'Reading' | 'Capture' | 'Source' | 'Projects' | 'Community' | 'Discovery' | 'Safety'
  primaryRoute: string
  primaryLabel: string
  prerequisites: string[]
  steps: UseCaseStep[]
  creates: string[]
  sourceReferences: string[]
  nextRoutes: UseCaseRelatedLink[]
  relatedFeatures: UseCaseRelatedLink[]
  safetyNote?: string
}

export const useCases: UseCaseTemplate[] = [
  {
    slug: 'track-book-start-to-finish',
    title: 'Track a book from start to finish',
    summary: 'Add a book, attach it to your library, then keep status, progress, and rating current.',
    audience: 'Readers who want BookOS to become a reliable reading record before using advanced knowledge tools.',
    goal: 'Make the current book context visible and measurable.',
    timeRequired: '5-8 minutes',
    category: 'Reading',
    primaryRoute: '/books/new',
    primaryLabel: 'Add a book',
    prerequisites: ['A book title and author you want to track.', 'Optional total page count if you know it.'],
    steps: [
      {
        title: 'Create the book record',
        description: 'Add safe metadata such as title, author, category, tags, and description.',
        route: '/books/new',
        actionLabel: 'Open Add Book',
      },
      {
        title: 'Add it to your personal library',
        description: 'Open the book detail page and attach it to your own library state.',
        route: '/my-library',
        actionLabel: 'Open Library',
      },
      {
        title: 'Set reading state',
        description: 'Mark the book as reading, paused, finished, reference, or another real status.',
        route: '/currently-reading',
        actionLabel: 'Open Current Reading',
      },
      {
        title: 'Update progress and rating',
        description: 'Update current progress and rating from the book detail page as you read.',
        route: '/my-library',
        actionLabel: 'Choose Book',
      },
    ],
    creates: ['Book record', 'Personal library entry', 'Reading status', 'Progress and rating data'],
    sourceReferences: ['No derived source reference is created until you add notes, captures, quotes, or actions.'],
    nextRoutes: [
      { label: 'Capture while reading', route: '/use-cases/capture-idea-while-reading' },
      { label: 'Create a note', route: '/notes' },
    ],
    relatedFeatures: [
      { label: 'Library', route: '/my-library' },
      { label: 'Currently Reading', route: '/currently-reading' },
      { label: 'Book Detail', route: '/my-library' },
    ],
  },
  {
    slug: 'capture-idea-while-reading',
    title: 'Capture an idea while reading',
    summary: 'Use quick capture with an emoji marker, page marker, tag, and concept marker.',
    audience: 'Readers who need the fastest path from reading thought to source-backed inbox item.',
    goal: 'Save the raw thought without losing source context.',
    timeRequired: '2-4 minutes',
    category: 'Capture',
    primaryRoute: '/my-library',
    primaryLabel: 'Open a book',
    prerequisites: ['At least one book in your library.', 'A short original thought you want to capture.'],
    steps: [
      {
        title: 'Open the current book',
        description: 'Use Library or Current Reading to open the book detail cockpit.',
        route: '/my-library',
        actionLabel: 'Open Library',
      },
      {
        title: 'Write a source-aware capture',
        description: 'Use a pattern such as: idea marker, known page if any, original thought, #tag, and [[Concept]].',
        route: '/captures/inbox',
        actionLabel: 'Open Capture Inbox',
      },
      {
        title: 'Check parsed metadata',
        description: 'Confirm BookOS detected type, page when supplied, tags, and concept markers.',
        route: '/captures/inbox',
        actionLabel: 'Review Captures',
      },
    ],
    creates: ['Raw capture', 'Parsed type', 'Tags', 'Concept markers', 'Book source context'],
    sourceReferences: ['Book ID is preserved.', 'Page stays null when unknown.', 'Known page marker is preserved without inventing extra pages.'],
    nextRoutes: [
      { label: 'Convert to quote', route: '/use-cases/capture-to-quote' },
      { label: 'Convert to action item', route: '/use-cases/capture-to-action-item' },
      { label: 'Review concept marker', route: '/use-cases/review-concept-marker' },
    ],
    relatedFeatures: [
      { label: 'Capture Inbox', route: '/captures/inbox' },
      { label: 'Book Detail', route: '/my-library' },
      { label: 'Concepts', route: '/concepts' },
    ],
  },
  {
    slug: 'capture-to-quote',
    title: 'Turn a capture into a quote',
    summary: 'Convert a parsed quote capture into a source-backed Quote record.',
    audience: 'Readers who want memorable passages or short observations to be searchable and source-linked.',
    goal: 'Create a quote without losing the original capture source.',
    timeRequired: '3-5 minutes',
    category: 'Capture',
    primaryRoute: '/captures/inbox',
    primaryLabel: 'Open Capture Inbox',
    prerequisites: ['A capture marked as quote or a capture you want to preserve as a quote.', 'Known page only if you actually captured it.'],
    steps: [
      {
        title: 'Open Capture Inbox',
        description: 'Filter by current book or quote type if needed.',
        route: '/captures/inbox',
        actionLabel: 'Open Inbox',
      },
      {
        title: 'Choose Convert to Quote',
        description: 'Use the primary conversion button on quote captures or the secondary conversion menu.',
        route: '/captures/inbox',
        actionLabel: 'Convert Capture',
      },
      {
        title: 'Open Quotes',
        description: 'Confirm the quote appears with source book and page if available.',
        route: '/quotes',
        actionLabel: 'Open Quotes',
      },
    ],
    creates: ['Quote', 'Quote source reference', 'Searchable quote record'],
    sourceReferences: ['Raw capture link is preserved.', 'Book context is preserved.', 'Page is preserved only if supplied.'],
    nextRoutes: [
      { label: 'Open source from quote', route: '/use-cases/open-source-from-quote-or-action' },
      { label: 'Apply quote to project', route: '/use-cases/apply-quote-to-game-project' },
    ],
    relatedFeatures: [
      { label: 'Capture Inbox', route: '/captures/inbox' },
      { label: 'Quotes', route: '/quotes' },
      { label: 'Search', route: '/dashboard' },
    ],
  },
  {
    slug: 'capture-to-action-item',
    title: 'Turn a capture into an action item',
    summary: 'Convert a reading insight into a concrete task you can complete or reopen.',
    audience: 'Readers and designers who want notes to become visible next actions.',
    goal: 'Create an action item that stays linked to the book or capture that inspired it.',
    timeRequired: '3-5 minutes',
    category: 'Capture',
    primaryRoute: '/captures/inbox',
    primaryLabel: 'Open Capture Inbox',
    prerequisites: ['A capture with an action marker or a capture that should become a task.'],
    steps: [
      {
        title: 'Open the action capture',
        description: 'Use Capture Inbox filters to find unconverted action-like captures.',
        route: '/captures/inbox',
        actionLabel: 'Open Inbox',
      },
      {
        title: 'Confirm the action title',
        description: 'Edit the generated title if needed before conversion.',
        route: '/captures/inbox',
        actionLabel: 'Convert to Action',
      },
      {
        title: 'Manage the task',
        description: 'Open Actions to complete, reopen, edit, archive, or open source.',
        route: '/action-items',
        actionLabel: 'Open Actions',
      },
    ],
    creates: ['Action item', 'Completion state', 'Action source reference'],
    sourceReferences: ['Book and raw capture links are preserved.', 'Unknown page remains unknown.'],
    nextRoutes: [
      { label: 'Open source from action', route: '/use-cases/open-source-from-quote-or-action' },
      { label: 'Apply to project', route: '/projects' },
    ],
    relatedFeatures: [
      { label: 'Capture Inbox', route: '/captures/inbox' },
      { label: 'Actions', route: '/action-items' },
      { label: 'Projects', route: '/projects' },
    ],
  },
  {
    slug: 'review-concept-marker',
    title: 'Turn a concept marker into a reviewed concept',
    summary: 'Review parsed [[Concept]] markers before creating or linking concepts.',
    audience: 'Researchers and game designers building a controlled vocabulary over time.',
    goal: 'Avoid auto-creating messy concepts while preserving the source that mentioned them.',
    timeRequired: '4-7 minutes',
    category: 'Capture',
    primaryRoute: '/captures/inbox',
    primaryLabel: 'Review Captures',
    prerequisites: ['A capture or note block containing a [[Concept Name]] marker.'],
    steps: [
      {
        title: 'Open a parsed concept capture',
        description: 'Find a capture that shows one or more concept markers.',
        route: '/captures/inbox',
        actionLabel: 'Open Inbox',
      },
      {
        title: 'Open concept review',
        description: 'Accept an existing concept, rename before saving, create a new concept, or skip it.',
        route: '/captures/inbox',
        actionLabel: 'Review Concepts',
      },
      {
        title: 'Open Concepts',
        description: 'Confirm the reviewed concept appears with source references and backlinks.',
        route: '/concepts',
        actionLabel: 'Open Concepts',
      },
    ],
    creates: ['Reviewed concept', 'Concept tags', 'Concept source link'],
    sourceReferences: ['Capture source is preserved.', 'Page data comes only from the original capture.'],
    nextRoutes: [
      { label: 'Inspect graph connections', route: '/use-cases/inspect-knowledge-graph' },
      { label: 'Apply concept to project', route: '/projects' },
    ],
    relatedFeatures: [
      { label: 'Concepts', route: '/concepts' },
      { label: 'Knowledge', route: '/knowledge' },
      { label: 'Graph', route: '/graph' },
    ],
  },
  {
    slug: 'open-source-from-quote-or-action',
    title: 'Open source from a quote or action',
    summary: 'Jump from a derived object back to the book, page, capture, or note context.',
    audience: 'Anyone who needs trustable source traceability instead of disconnected notes.',
    goal: 'Verify where an idea came from before using it.',
    timeRequired: '2-3 minutes',
    category: 'Source',
    primaryRoute: '/quotes',
    primaryLabel: 'Open Quotes',
    prerequisites: ['At least one quote or action item with a source reference.'],
    steps: [
      {
        title: 'Open Quotes or Actions',
        description: 'Choose a derived record that shows source information.',
        route: '/quotes',
        actionLabel: 'Open Quotes',
      },
      {
        title: 'Click Open Source',
        description: 'BookOS navigates to the book detail with source query parameters or opens source context where available.',
        route: '/action-items',
        actionLabel: 'Open Actions',
      },
      {
        title: 'Check source details',
        description: 'Verify book, page if known, source text, and confidence.',
        route: '/my-library',
        actionLabel: 'Open Library',
      },
    ],
    creates: ['No new object; this is a verification workflow.'],
    sourceReferences: ['Existing source reference is read and opened.', 'Unknown page is displayed as unknown instead of guessed.'],
    nextRoutes: [
      { label: 'Apply quote to project', route: '/use-cases/apply-quote-to-game-project' },
      { label: 'Start forum discussion', route: '/use-cases/source-linked-forum-discussion' },
    ],
    relatedFeatures: [
      { label: 'Quotes', route: '/quotes' },
      { label: 'Actions', route: '/action-items' },
      { label: 'Source References', route: '/my-library' },
    ],
  },
  {
    slug: 'apply-quote-to-game-project',
    title: 'Apply a quote to a game project',
    summary: 'Turn a source-backed quote into a project application inside a design cockpit.',
    audience: 'Game designers who want reading to influence project decisions.',
    goal: 'Connect a quote to a practical design note for a project.',
    timeRequired: '5-8 minutes',
    category: 'Projects',
    primaryRoute: '/quotes',
    primaryLabel: 'Open Quotes',
    prerequisites: ['At least one quote.', 'At least one project.', 'A design reason for applying the quote.'],
    steps: [
      {
        title: 'Open Quotes',
        description: 'Pick a source-backed quote you want to apply.',
        route: '/quotes',
        actionLabel: 'Open Quotes',
      },
      {
        title: 'Click Apply to Project',
        description: 'Choose a project, application type, title, and design note.',
        route: '/projects',
        actionLabel: 'Open Projects',
      },
      {
        title: 'Open Project Cockpit',
        description: 'Check the application and confirm its source reference is visible.',
        route: '/projects',
        actionLabel: 'Open Project Cockpit',
      },
    ],
    creates: ['Project application', 'Project knowledge link where supported', 'Source-backed design note'],
    sourceReferences: ['Quote source reference is preserved.', 'Book and page remain traceable when available.'],
    nextRoutes: [
      { label: 'Create design decision', route: '/projects' },
      { label: 'Run lens review', route: '/use-cases/run-design-lens-review' },
    ],
    relatedFeatures: [
      { label: 'Quotes', route: '/quotes' },
      { label: 'Projects', route: '/projects' },
      { label: 'Graph', route: '/graph' },
    ],
  },
  {
    slug: 'run-design-lens-review',
    title: 'Run a design lens review on a project',
    summary: 'Use a lens or diagnostic question to review a project problem.',
    audience: 'Game designers evaluating whether a reading-derived lens changes a design choice.',
    goal: 'Create a source-aware review answer that can influence the project.',
    timeRequired: '8-12 minutes',
    category: 'Projects',
    primaryRoute: '/projects',
    primaryLabel: 'Open Projects',
    prerequisites: ['At least one project.', 'A design lens or diagnostic knowledge object.'],
    steps: [
      {
        title: 'Open a project',
        description: 'Choose the project you want to evaluate.',
        route: '/projects',
        actionLabel: 'Open Projects',
      },
      {
        title: 'Open Lens Reviews',
        description: 'Select a real lens or diagnostic question and write your review answer.',
        route: '/projects',
        actionLabel: 'Open Project',
      },
      {
        title: 'Link evidence',
        description: 'Attach source context when the review was inspired by a quote, concept, or knowledge object.',
        route: '/knowledge',
        actionLabel: 'Open Knowledge',
      },
    ],
    creates: ['Project lens review', 'Optional score', 'Source-backed review answer'],
    sourceReferences: ['Lens source is preserved when attached.', 'Project review remains user-owned.'],
    nextRoutes: [
      { label: 'Create design decision', route: '/projects' },
      { label: 'Create playtest finding', route: '/use-cases/create-playtest-finding' },
    ],
    relatedFeatures: [
      { label: 'Projects', route: '/projects' },
      { label: 'Knowledge', route: '/knowledge' },
      { label: 'Review', route: '/review' },
    ],
  },
  {
    slug: 'create-playtest-finding',
    title: 'Create a playtest finding from project work',
    summary: 'Record a finding after playtesting and connect it to a project or source-backed idea.',
    audience: 'Designers validating whether source-backed reading ideas work in practice.',
    goal: 'Turn playtest observations into trackable evidence.',
    timeRequired: '5-10 minutes',
    category: 'Projects',
    primaryRoute: '/projects',
    primaryLabel: 'Open Projects',
    prerequisites: ['At least one project.', 'A real observation from project work or playtesting.'],
    steps: [
      {
        title: 'Open Project Playtests',
        description: 'Navigate to the playtest area for the relevant project.',
        route: '/projects',
        actionLabel: 'Open Projects',
      },
      {
        title: 'Create finding',
        description: 'Write the observation, severity, recommendation, and status.',
        route: '/projects',
        actionLabel: 'Open Project',
      },
      {
        title: 'Link evidence if available',
        description: 'Attach a source reference only when a reading source directly informed the finding.',
        route: '/quotes',
        actionLabel: 'Open Quotes',
      },
    ],
    creates: ['Playtest finding', 'Recommendation', 'Optional source-backed evidence link'],
    sourceReferences: ['Source reference is optional.', 'No page is invented when the finding comes from project work only.'],
    nextRoutes: [
      { label: 'Inspect project graph', route: '/graph' },
      { label: 'Start forum critique', route: '/use-cases/source-linked-forum-discussion' },
    ],
    relatedFeatures: [
      { label: 'Projects', route: '/projects' },
      { label: 'Graph', route: '/graph' },
      { label: 'Forum', route: '/forum' },
    ],
  },
  {
    slug: 'daily-prompt-project-task',
    title: 'Use Daily Prompt to create a project task',
    summary: 'Use the daily prompt as a practical design action, not just a reflection.',
    audience: 'Readers who want daily resurfacing to produce project momentum.',
    goal: 'Convert a prompt into a project application, problem, or task where supported.',
    timeRequired: '4-8 minutes',
    category: 'Projects',
    primaryRoute: '/daily',
    primaryLabel: 'Open Daily',
    prerequisites: ['A daily prompt.', 'A project if you want to apply the prompt to design work.'],
    steps: [
      {
        title: 'Open Daily',
        description: 'Review the daily sentence and design prompt.',
        route: '/daily',
        actionLabel: 'Open Daily',
      },
      {
        title: 'Open source if available',
        description: 'If the prompt is source-backed, verify the original source before applying it.',
        route: '/daily',
        actionLabel: 'Check Daily Source',
      },
      {
        title: 'Apply to a project',
        description: 'Create a project application or task only when the UI exposes a real action.',
        route: '/projects',
        actionLabel: 'Open Projects',
      },
    ],
    creates: ['Daily reflection', 'Optional project application or task when supported'],
    sourceReferences: ['Template prompts are labeled as templates.', 'Source-backed prompts preserve their source reference.'],
    nextRoutes: [
      { label: 'Open projects', route: '/projects' },
      { label: 'Review mastery', route: '/review' },
    ],
    relatedFeatures: [
      { label: 'Daily', route: '/daily' },
      { label: 'Projects', route: '/projects' },
      { label: 'Review', route: '/review' },
    ],
  },
  {
    slug: 'source-linked-forum-discussion',
    title: 'Start a forum discussion from a quote or concept',
    summary: 'Create a structured discussion that carries book, concept, or source context.',
    audience: 'Readers and designers who want discussion to stay tied to evidence.',
    goal: 'Start a thread without turning the forum into a disconnected comment dump.',
    timeRequired: '4-7 minutes',
    category: 'Community',
    primaryRoute: '/forum/new',
    primaryLabel: 'Start thread',
    prerequisites: ['A quote, concept, book, project, or source reference worth discussing.'],
    steps: [
      {
        title: 'Choose source context',
        description: 'Open the quote, concept, book, or project that should anchor the discussion.',
        route: '/quotes',
        actionLabel: 'Open Quotes',
      },
      {
        title: 'Start a structured thread',
        description: 'Use a template such as Quote Discussion, Concept Discussion, or Project Critique.',
        route: '/forum/new',
        actionLabel: 'New Thread',
      },
      {
        title: 'Add a focused comment',
        description: 'Ask a concrete question or propose a project application.',
        route: '/forum',
        actionLabel: 'Open Forum',
      },
    ],
    creates: ['Forum thread', 'Forum comment', 'Optional related source context'],
    sourceReferences: ['Private source context must not be exposed to users who cannot access it.', 'Thread context should link to real entities only.'],
    nextRoutes: [
      { label: 'Search discussions', route: '/forum' },
      { label: 'Apply discussion to project', route: '/projects' },
    ],
    relatedFeatures: [
      { label: 'Forum', route: '/forum' },
      { label: 'Quotes', route: '/quotes' },
      { label: 'Concepts', route: '/concepts' },
    ],
  },
  {
    slug: 'search-rediscover-knowledge',
    title: 'Search and rediscover old knowledge',
    summary: 'Use global search to find books, notes, captures, quotes, actions, concepts, projects, and forum threads.',
    audience: 'Users with enough material that browsing by module is slower than search.',
    goal: 'Find the right object and reopen its source or detail page.',
    timeRequired: '1-3 minutes',
    category: 'Discovery',
    primaryRoute: '/dashboard',
    primaryLabel: 'Open Dashboard',
    prerequisites: ['At least one created record, such as a book, quote, concept, or project.'],
    steps: [
      {
        title: 'Open command search',
        description: 'Use the sidebar Search action or Cmd/Ctrl+K.',
        route: '/dashboard',
        actionLabel: 'Open Dashboard',
      },
      {
        title: 'Search a term',
        description: 'Search by title, text, concept, project, or source-backed phrase you created.',
        route: '/dashboard',
        actionLabel: 'Use Search',
      },
      {
        title: 'Open result or source',
        description: 'Open the object detail page, or open source where the result exposes source context.',
        route: '/my-library',
        actionLabel: 'Open Library',
      },
    ],
    creates: ['No new object; this is a rediscovery workflow.'],
    sourceReferences: ['Search results expose source reference IDs only when the user can access them.'],
    nextRoutes: [
      { label: 'Inspect graph', route: '/use-cases/inspect-knowledge-graph' },
      { label: 'Export knowledge', route: '/use-cases/export-reading-knowledge' },
    ],
    relatedFeatures: [
      { label: 'Dashboard', route: '/dashboard' },
      { label: 'Graph', route: '/graph' },
      { label: 'Import / Export', route: '/import-export' },
    ],
  },
  {
    slug: 'inspect-knowledge-graph',
    title: 'Use graph to inspect knowledge connections',
    summary: 'Open the graph workspace to inspect real links across books, concepts, projects, and sources.',
    audience: 'Users who want to understand how reading knowledge connects across the system.',
    goal: 'Explore real relationships without relying on fake nodes.',
    timeRequired: '3-6 minutes',
    category: 'Discovery',
    primaryRoute: '/graph',
    primaryLabel: 'Open Graph',
    prerequisites: ['Real links from concepts, source references, project applications, or entity links.'],
    steps: [
      {
        title: 'Open Graph',
        description: 'Start from the global graph or a book, concept, or project graph route.',
        route: '/graph',
        actionLabel: 'Open Graph',
      },
      {
        title: 'Filter real relationships',
        description: 'Filter by entity type, relationship type, book, concept, project, or source confidence.',
        route: '/graph',
        actionLabel: 'Filter Graph',
      },
      {
        title: 'Open a node',
        description: 'Use node detail actions to open entity detail pages or source references.',
        route: '/graph',
        actionLabel: 'Inspect Node',
      },
    ],
    creates: ['Optional manual relationship when supported by the graph workspace.'],
    sourceReferences: ['Graph edges must come from real source references or explicit entity links.', 'Unknown pages remain unknown.'],
    nextRoutes: [
      { label: 'Review concepts', route: '/concepts' },
      { label: 'Open projects', route: '/projects' },
    ],
    relatedFeatures: [
      { label: 'Graph', route: '/graph' },
      { label: 'Concepts', route: '/concepts' },
      { label: 'Projects', route: '/projects' },
    ],
  },
  {
    slug: 'export-reading-knowledge',
    title: 'Export your reading knowledge',
    summary: 'Export your own BookOS data as JSON, Markdown, or CSV where supported.',
    audience: 'Users who need backup, portability, or offline review.',
    goal: 'Get user-owned data out of BookOS without leaking other users data.',
    timeRequired: '3-5 minutes',
    category: 'Safety',
    primaryRoute: '/import-export/export',
    primaryLabel: 'Open Export',
    prerequisites: ['User-owned records to export.', 'A clear export scope such as all data, one book, quotes, actions, or concepts.'],
    steps: [
      {
        title: 'Open Import / Export',
        description: 'Choose export mode, scope, and format.',
        route: '/import-export/export',
        actionLabel: 'Open Export',
      },
      {
        title: 'Review what is included',
        description: 'Confirm whether the export includes notes, captures, quotes, actions, concepts, projects, and source references.',
        route: '/import-export/export',
        actionLabel: 'Review Export',
      },
      {
        title: 'Download safely',
        description: 'Store the export in a safe place because it may contain private notes and source text.',
        route: '/import-export/export',
        actionLabel: 'Download Export',
      },
    ],
    creates: ['JSON backup', 'Markdown export', 'CSV export where useful'],
    sourceReferences: ['Export includes only source references the current user can access.', 'Unknown pages remain null or unknown in export output.'],
    nextRoutes: [
      { label: 'Import data preview', route: '/import-export/import' },
      { label: 'Search exported knowledge first', route: '/use-cases/search-rediscover-knowledge' },
    ],
    relatedFeatures: [
      { label: 'Import / Export', route: '/import-export' },
      { label: 'Library', route: '/my-library' },
      { label: 'Quotes', route: '/quotes' },
    ],
    safetyNote: 'Exports may include private user-generated content. Treat downloaded files as sensitive.',
  },
  {
    slug: 'mock-ai-draft-helper',
    title: 'Use Mock AI safely as a draft helper',
    summary: 'Generate draft suggestions from existing content while keeping user review mandatory.',
    audience: 'Users who want optional assistance without automatic content changes.',
    goal: 'Create, inspect, edit, accept, or reject a draft suggestion safely.',
    timeRequired: '4-6 minutes',
    category: 'Safety',
    primaryRoute: '/my-library',
    primaryLabel: 'Open a source',
    prerequisites: ['A note, source-backed object, or current book context.', 'MockAIProvider enabled in local/dev.'],
    steps: [
      {
        title: 'Open a source-backed context',
        description: 'Start from a book, note, quote, project, or right rail source context.',
        route: '/my-library',
        actionLabel: 'Open Library',
      },
      {
        title: 'Generate a draft',
        description: 'Use the AI panel only where the UI clearly labels the provider as MockAIProvider or configured provider.',
        route: '/dashboard',
        actionLabel: 'Open Dashboard',
      },
      {
        title: 'Review before accepting',
        description: 'Edit, accept, or reject the suggestion. Accepting must not overwrite original source content.',
        route: '/dashboard',
        actionLabel: 'Review Draft',
      },
    ],
    creates: ['AI suggestion draft', 'AI interaction record', 'Optional accepted draft state'],
    sourceReferences: ['Source inputs used for the suggestion should be recorded.', 'Drafts do not become authoritative content automatically.'],
    nextRoutes: [
      { label: 'Open source', route: '/my-library' },
      { label: 'Review AI safety docs', route: '/use-cases/mock-ai-draft-helper' },
    ],
    relatedFeatures: [
      { label: 'Dashboard', route: '/dashboard' },
      { label: 'Notes', route: '/notes' },
      { label: 'Projects', route: '/projects' },
    ],
    safetyNote: 'AI suggestions are drafts only. BookOS must work without any external AI key and must never overwrite source content automatically.',
  },
  {
    slug: 'practice-with-demo-workspace',
    title: 'Practice with the Demo Workspace',
    summary: 'Start a safe sample workspace, open demo records, then reset or delete the demo data.',
    audience: 'New users who want hands-on practice before adding real books, notes, or projects.',
    goal: 'Learn the reading-to-knowledge-to-project loop without polluting your real knowledge base.',
    timeRequired: '5-10 minutes',
    category: 'Safety',
    primaryRoute: '/demo',
    primaryLabel: 'Open Demo Workspace',
    prerequisites: ['A BookOS account.', 'No real book or project data required.'],
    steps: [
      {
        title: 'Open Demo Workspace',
        description: 'Read the safety note before creating any sample records.',
        route: '/demo',
        actionLabel: 'Open Demo',
      },
      {
        title: 'Start demo mode',
        description: 'Create clearly labeled original sample records scoped to your account.',
        route: '/demo',
        actionLabel: 'Start Demo Workspace',
      },
      {
        title: 'Open a demo source',
        description: 'Use the demo book, quote, concept, project, graph, and discussion links to practice real routes.',
        route: '/demo',
        actionLabel: 'Open Demo Records',
      },
      {
        title: 'Reset or delete demo data',
        description: 'Reset to replay the workflow, or delete demo data when you are ready to work with real sources.',
        route: '/demo',
        actionLabel: 'Reset or Delete',
      },
    ],
    creates: ['Demo book', 'Demo captures', 'Demo quote', 'Demo action item', 'Demo concepts', 'Demo project', 'Demo forum thread'],
    sourceReferences: [
      'Demo source references are original BookOS sample content.',
      'Demo pages are unknown and stored as null.',
      'Demo records are tracked by demo scope so they can be deleted separately.',
    ],
    nextRoutes: [
      { label: 'Track a real book', route: '/use-cases/track-book-start-to-finish' },
      { label: 'Capture a real idea', route: '/use-cases/capture-idea-while-reading' },
    ],
    relatedFeatures: [
      { label: 'Demo Workspace', route: '/demo' },
      { label: 'Use Cases', route: '/use-cases' },
      { label: 'Dashboard', route: '/dashboard' },
    ],
    safetyNote: 'Demo data is clearly labeled and excluded from normal analytics by default. It is not real personal knowledge.',
  },
]

export function findUseCase(slug: string) {
  return useCases.find((useCase) => useCase.slug === slug) ?? null
}
