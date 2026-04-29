export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export type RoleName = 'ADMIN' | 'MODERATOR' | 'USER'
export type Visibility = 'PRIVATE' | 'SHARED' | 'PUBLIC'
export type ReadingStatus =
  | 'BACKLOG'
  | 'CURRENTLY_READING'
  | 'COMPLETED'
  | 'PAUSED'
  | 'DROPPED'
  | 'REFERENCE'
  | 'ANTI_LIBRARY'
export type ReadingFormat = 'PHYSICAL' | 'EBOOK' | 'AUDIOBOOK' | 'PDF' | 'WEB' | 'OTHER'
export type OwnershipStatus = 'OWNED' | 'BORROWED' | 'WISHLIST' | 'SUBSCRIPTION' | 'SAMPLE'
export type NoteBlockType =
  | 'NOTE'
  | 'INSPIRATION'
  | 'KEY_ARGUMENT'
  | 'QUOTE'
  | 'DISCUSSION_POINT'
  | 'MIND_BLOWING_IDEA'
  | 'ACTION_ITEM'
  | 'QUESTION'
  | 'MENTAL_MODEL'
  | 'RELATED_CONCEPT'
  | 'WARNING'
  | 'IMPORTANT'
  | 'EXPERIMENT'
  | 'PERSONAL_REFLECTION'
  | 'DATA_STATISTIC'
  | 'LINK'
  | 'READING_DIRECTION'
  | 'IDEA'
  | 'ACTION'
  | 'SUMMARY'
  | 'REFERENCE'
export type SourceConfidence = 'LOW' | 'MEDIUM' | 'HIGH'
export type CaptureStatus = 'INBOX' | 'CONVERTED' | 'ARCHIVED' | 'DISCARDED'
export type CaptureConversionTarget = 'NOTE' | 'QUOTE' | 'ACTION_ITEM' | 'CONCEPT'
export type ConceptReviewAction = 'ACCEPT' | 'CREATE' | 'SKIP'
export type ActionPriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type KnowledgeObjectType =
  | 'CONCEPT'
  | 'PRINCIPLE'
  | 'DESIGN_LENS'
  | 'LENS'
  | 'DIAGNOSTIC_QUESTION'
  | 'QUESTION'
  | 'CHECKLIST'
  | 'METHOD'
  | 'PATTERN'
  | 'ANTI_PATTERN'
  | 'EXAMPLE_CASE'
  | 'EXERCISE'
  | 'PROTOTYPE_TASK'
  | 'CROSS_BOOK_SYNTHESIS'
  | 'MECHANIC'
  | 'METHOD_PATTERN'
export type ForumThreadStatus = 'OPEN' | 'LOCKED' | 'HIDDEN' | 'ACTIVE' | 'CLOSED' | 'ARCHIVED'
export type ForumReportStatus = 'OPEN' | 'RESOLVED'
export type SearchResultType =
  | 'BOOK'
  | 'NOTE'
  | 'CAPTURE'
  | 'QUOTE'
  | 'ACTION_ITEM'
  | 'CONCEPT'
  | 'KNOWLEDGE_OBJECT'
  | 'FORUM_THREAD'
export type GraphNodeType =
  | 'BOOK'
  | 'NOTE'
  | 'CAPTURE'
  | 'QUOTE'
  | 'ACTION_ITEM'
  | 'CONCEPT'
  | 'KNOWLEDGE_OBJECT'
  | 'SOURCE_REFERENCE'
  | string
export type AISuggestionType = 'NOTE_SUMMARY' | 'EXTRACT_ACTIONS' | 'EXTRACT_CONCEPTS'
export type AISuggestionStatus = 'DRAFT' | 'ACCEPTED' | 'REJECTED'

export interface AuthUser {
  id: number
  email: string
  displayName: string
  role: RoleName
}

export interface AuthPayload {
  token: string
  user: AuthUser
}

export interface RegisterPayload {
  email: string
  password: string
  displayName: string
}

export interface LoginPayload {
  email: string
  password: string
}

export interface BookRecord {
  id: number
  title: string
  subtitle: string | null
  description: string | null
  isbn: string | null
  publisher: string | null
  publicationYear: number | null
  coverUrl: string | null
  coverImageUrl?: string | null
  category: string | null
  visibility: Visibility
  authors: string[]
  tags: string[]
  inLibrary: boolean
  userBookId: number | null
  readingStatus: ReadingStatus | null
  readingFormat: ReadingFormat | null
  ownershipStatus: OwnershipStatus | null
  progressPercent: number | null
  rating: number | null
  currentPage?: number | null
  totalPages?: number | null
  lastReadAt?: string | null
  currentChapter?: string | null
  pageRange?: string | null
  sourceLocation?: string | null
  sourceAddedAt?: string | null
  notesCount?: number | null
  quotesCount?: number | null
  actionItemsCount?: number | null
  lensesCount?: number | null
  conceptsCount?: number | null
  capturesCount?: number | null
  sourceReferencesCount?: number | null
  dailyQuote?: BookQuotePreview | null
  latestQuote?: BookQuotePreview | null
  dailyDesignPrompt?: BookDesignPromptPreview | null
  concepts?: BookConceptPreview[] | string[] | null
  ontologyConceptCount?: number | null
  linkedLenses?: BookLensPreview[] | string[] | null
  knowledgeGraph?: BookGraphPreview | null
}

export interface BookQuotePreview {
  id?: number | string | null
  text: string
  author?: string | null
  page?: number | string | null
  sourceLabel?: string | null
}

export interface BookDesignPromptPreview {
  id?: number | string | null
  question: string
  linkedConcept?: string | null
  linkedLens?: string | null
  sourceTitle?: string | null
  templatePrompt?: boolean | null
}

export interface BookConceptPreview {
  id?: number | string | null
  name: string
  type?: string | null
  relevance?: number | null
  mentions?: number | null
  edgeStrength?: KnowledgeEdgeStrength | null
}

export type KnowledgeEdgeStrength = 'strong' | 'medium' | 'weak'

export interface BookLensPreview {
  id?: number | string | null
  name: string
  description?: string | null
  count?: number | null
  icon?: string | null
}

export interface BookGraphPreview {
  concepts?: BookConceptPreview[] | string[] | null
  lenses?: BookLensPreview[] | string[] | null
  nodes?: GraphNodeRecord[] | null
  edges?: GraphEdgeRecord[] | null
}

export interface SearchResultRecord {
  type: SearchResultType
  id: number
  title: string
  excerpt: string | null
  bookId: number | null
  bookTitle: string | null
  sourceReferenceId: number | null
  updatedAt: string | null
}

export interface GraphNodeRecord {
  id: string
  type: GraphNodeType
  label: string
  entityId: number
}

export interface GraphEdgeRecord {
  source: string
  target: string
  type: string
}

export interface GraphRecord {
  nodes: GraphNodeRecord[]
  edges: GraphEdgeRecord[]
}

export interface AISuggestionPayload {
  bookId?: number | null
  noteId?: number | null
  rawCaptureId?: number | null
  sourceReferenceId?: number | null
  text?: string | null
}

export interface AISuggestionEditPayload {
  draftText?: string | null
  draftJson?: string | null
}

export interface AISuggestionRecord {
  id: number
  suggestionType: AISuggestionType
  status: AISuggestionStatus
  providerName: string
  bookId: number | null
  bookTitle: string | null
  sourceReferenceId: number | null
  sourceReference: SourceReferenceRecord | null
  draftText: string
  draftJson: string
  createdAt: string
  updatedAt: string
}

export interface UserBookRecord {
  id: number
  bookId: number
  title: string
  subtitle: string | null
  coverUrl: string | null
  category: string | null
  authors: string[]
  tags: string[]
  readingStatus: ReadingStatus
  readingFormat: ReadingFormat
  ownershipStatus: OwnershipStatus
  progressPercent: number
  rating: number | null
}

export interface BookPayload {
  title: string
  subtitle: string | null
  description: string | null
  isbn: string | null
  publisher: string | null
  publicationYear: number | null
  coverUrl: string | null
  category: string | null
  visibility: Visibility
  authors: string[]
  tags: string[]
}

export interface AddToLibraryPayload {
  readingStatus?: ReadingStatus
  readingFormat?: ReadingFormat
  ownershipStatus?: OwnershipStatus
}

export interface ParserPreviewPayload {
  rawText: string
}

export interface ParsedNoteResult {
  type: NoteBlockType
  pageStart: number | null
  pageEnd: number | null
  tags: string[]
  concepts: string[]
  cleanText: string
  rawText: string
  warnings: string[]
}

export interface SourceReferenceRecord {
  id: number
  sourceType: string
  bookId: number
  noteId: number | null
  noteBlockId: number | null
  chapterId?: number | null
  rawCaptureId: number | null
  pageStart: number | null
  pageEnd: number | null
  locationLabel: string | null
  sourceText: string | null
  sourceConfidence: SourceConfidence
  createdAt?: string | null
}

export interface NoteBlockRecord {
  id: number
  noteId: number
  bookId: number
  blockType: NoteBlockType
  rawText: string
  markdown: string
  plainText: string
  sortOrder: number
  pageStart: number | null
  pageEnd: number | null
  parserWarnings: string[]
  sourceReferences: SourceReferenceRecord[]
  createdAt: string
  updatedAt: string
}

export interface BookNoteRecord {
  id: number
  bookId: number
  bookTitle: string
  title: string
  markdown: string
  threeSentenceSummary: string | null
  visibility: Visibility
  archived: boolean
  blocks: NoteBlockRecord[]
  createdAt: string
  updatedAt: string
}

export interface BookNotePayload {
  title: string
  markdown: string
  visibility: Visibility
  threeSentenceSummary?: string | null
}

export interface NoteBlockPayload {
  rawText: string
  sortOrder?: number | null
}

export interface RawCapturePayload {
  bookId: number
  rawText: string
}

export interface RawCaptureUpdatePayload {
  rawText: string
}

export interface RawCaptureRecord {
  id: number
  bookId: number
  bookTitle: string
  rawText: string
  cleanText: string
  parsedType: NoteBlockType
  pageStart: number | null
  pageEnd: number | null
  tags: string[]
  concepts: string[]
  parserWarnings: string[]
  status: CaptureStatus
  convertedEntityType: string | null
  convertedEntityId: number | null
  sourceReferences: SourceReferenceRecord[]
  createdAt: string
  updatedAt: string
}

export interface RawCaptureConvertPayload {
  targetType?: CaptureConversionTarget
  title?: string | null
}

export interface RawCaptureConversionRecord {
  capture: RawCaptureRecord
  targetType: string
  targetId: number
}

export interface ConceptReviewItemPayload {
  rawName: string
  finalName?: string | null
  action: ConceptReviewAction
  existingConceptId?: number | null
  tags?: string[]
}

export interface ConceptReviewPayload {
  concepts: ConceptReviewItemPayload[]
}

export interface ConceptPayload {
  name: string
  description?: string | null
  visibility?: Visibility | null
  bookId?: number | null
  sourceReferenceId?: number | null
  ontologyLayer?: string | null
  tags?: string[]
}

export interface ConceptRecord {
  id: number
  name: string
  slug: string
  description: string | null
  visibility: Visibility
  ontologyLayer: string | null
  sourceConfidence: SourceConfidence | null
  createdBy: string
  tags: string[]
  bookId: number | null
  bookTitle: string | null
  mentionCount: number
  firstSourceReference: SourceReferenceRecord | null
  sourceReferences: SourceReferenceRecord[]
  createdAt: string
  updatedAt: string
}

export interface ReviewedConceptRecord {
  rawName: string
  finalName: string
  action: ConceptReviewAction
  concept: ConceptRecord | null
  knowledgeObject: KnowledgeObjectRecord | null
  sourceReference: SourceReferenceRecord | null
  tags: string[]
}

export interface ConceptReviewRecord {
  capture: RawCaptureRecord
  concepts: ReviewedConceptRecord[]
}

export interface KnowledgeObjectPayload {
  type: KnowledgeObjectType
  title: string
  description?: string | null
  visibility?: Visibility | null
  bookId?: number | null
  noteId?: number | null
  conceptId?: number | null
  sourceReferenceId?: number | null
  ontologyLayer?: string | null
  tags?: string[]
}

export interface KnowledgeObjectRecord {
  id: number
  type: KnowledgeObjectType
  title: string
  slug: string
  description: string | null
  visibility: Visibility
  ontologyLayer: string | null
  sourceConfidence: SourceConfidence | null
  createdBy: string
  bookId: number | null
  bookTitle: string | null
  noteId: number | null
  noteTitle: string | null
  conceptId: number | null
  conceptName: string | null
  sourceReference: SourceReferenceRecord | null
  tags: string[]
  createdAt: string
  updatedAt: string
}

export type DailyTarget = 'SENTENCE' | 'PROMPT'

export interface DailySentenceRecord {
  id: number
  day: string
  text: string
  attribution: string | null
  bookId: number | null
  bookTitle: string | null
  sourceType: string | null
  sourceId: number | null
  pageStart: number | null
  pageEnd: number | null
  sourceBacked: boolean
  skipped: boolean
  sourceReference: SourceReferenceRecord | null
  createdAt: string
  updatedAt: string
}

export interface DailyDesignPromptRecord {
  id: number
  day: string
  question: string
  sourceTitle: string | null
  bookId: number | null
  bookTitle: string | null
  sourceType: string | null
  sourceId: number | null
  knowledgeObjectId: number | null
  templatePrompt: boolean
  skipped: boolean
  sourceReference: SourceReferenceRecord | null
  createdAt: string
  updatedAt: string
}

export interface DailyReflectionRecord {
  id: number
  day: string
  target: DailyTarget
  dailySentenceId: number | null
  dailyDesignPromptId: number | null
  reflectionText: string
  createdAt: string
  updatedAt: string
}

export interface DailyHistoryRecord {
  id: number
  day: string
  target: DailyTarget
  action: string
  sourceType: string | null
  sourceId: number | null
  dailySentenceId: number | null
  dailyDesignPromptId: number | null
  createdAt: string
}

export interface DailyTodayRecord {
  day: string
  sentence: DailySentenceRecord | null
  prompt: DailyDesignPromptRecord
  reflections: DailyReflectionRecord[]
}

export interface DailyReflectionPayload {
  target: DailyTarget
  dailySentenceId?: number | null
  dailyDesignPromptId?: number | null
  reflectionText: string
}

export interface DailyPrototypeTaskPayload {
  dailyDesignPromptId?: number | null
  title?: string | null
  description?: string | null
}

export interface ForumCategoryPayload {
  name: string
  description?: string | null
  sortOrder?: number | null
}

export interface ForumCategoryRecord {
  id: number
  name: string
  slug: string
  description: string | null
  sortOrder: number
  threadCount: number
  createdAt: string
  updatedAt: string
}

export interface ForumThreadPayload {
  categoryId: number
  title: string
  bodyMarkdown: string
  relatedEntityType?: string | null
  relatedEntityId?: number | null
  relatedBookId?: number | null
  relatedConceptId?: number | null
  sourceReferenceId?: number | null
  visibility?: Visibility | null
}

export interface ForumThreadRecord {
  id: number
  categoryId: number
  categoryName: string
  categorySlug: string
  authorId: number
  authorDisplayName: string
  title: string
  bodyMarkdown: string
  relatedEntityType: string | null
  relatedEntityId: number | null
  relatedBookId: number | null
  relatedBookTitle: string | null
  relatedConceptId: number | null
  relatedConceptName: string | null
  sourceReferenceId: number | null
  sourceReference: SourceReferenceRecord | null
  status: ForumThreadStatus
  visibility: Visibility
    commentCount: number
    likeCount: number
    bookmarkCount: number
    reportCount: number
    likedByCurrentUser: boolean
    bookmarkedByCurrentUser: boolean
    canEdit: boolean
    canModerate: boolean
    sourceContextUnavailable: boolean
    createdAt: string
    updatedAt: string
  }

export interface ForumCommentPayload {
  bodyMarkdown: string
  parentCommentId?: number | null
}

export interface ForumCommentRecord {
  id: number
  threadId: number
  authorId: number
  authorDisplayName: string
  bodyMarkdown: string
  parentCommentId: number | null
  canEdit: boolean
  createdAt: string
  updatedAt: string
}

  export interface ForumReportPayload {
    reason: string
    details?: string | null
  }

  export interface ForumModerationPayload {
    status: 'OPEN' | 'LOCKED' | 'HIDDEN'
  }

  export interface ForumReportRecord {
    id: number
    threadId: number
    threadTitle: string
    reporterId: number
    reporterDisplayName: string
    reason: string
    details: string | null
    status: ForumReportStatus
    resolved: boolean
    createdAt: string
    updatedAt: string
  }

export interface StructuredPostTemplateRecord {
  id: number
  name: string
  slug: string
  description: string | null
  bodyMarkdownTemplate: string
  defaultRelatedEntityType: string | null
  sortOrder: number
}

export interface EntityLinkPayload {
  sourceType: string
  sourceId: number
  targetType: string
  targetId: number
  relationType: string
  sourceReferenceId?: number | null
}

export interface EntityLinkRecord {
  id: number
  sourceType: string
  sourceId: number
  targetType: string
  targetId: number
  relationType: string
  sourceReferenceId: number | null
  createdAt: string
}

export interface BacklinkRecord {
  linkId: number
  direction: 'OUTGOING' | 'INCOMING' | string
  relationType: string
  entityType: string
  entityId: number
  title: string
  excerpt: string | null
  sourceReference: SourceReferenceRecord | null
  createdAt: string
}

export interface QuotePayload {
  bookId: number
  text: string
  attribution?: string | null
  sourceReferenceId?: number | null
  pageStart?: number | null
  pageEnd?: number | null
  tags?: string[]
  concepts?: string[]
  visibility?: Visibility | null
}

export interface QuoteRecord {
  id: number
  bookId: number
  bookTitle: string
  noteId: number | null
  noteBlockId: number | null
  rawCaptureId: number | null
  text: string
  attribution: string | null
  pageStart: number | null
  pageEnd: number | null
  tags: string[]
  concepts: string[]
  visibility: Visibility
  sourceReference: SourceReferenceRecord | null
  createdAt: string
  updatedAt: string
}

export interface ActionItemPayload {
  bookId: number
  title: string
  description?: string | null
  priority?: ActionPriority | null
  sourceReferenceId?: number | null
  pageStart?: number | null
  pageEnd?: number | null
  visibility?: Visibility | null
}

export interface ActionItemRecord {
  id: number
  bookId: number
  bookTitle: string
  noteId: number | null
  noteBlockId: number | null
  rawCaptureId: number | null
  title: string
  description: string | null
  priority: ActionPriority
  pageStart: number | null
  pageEnd: number | null
  completed: boolean
  completedAt: string | null
  visibility: Visibility
  sourceReference: SourceReferenceRecord | null
  createdAt: string
  updatedAt: string
}

export interface OntologySeedBookPayload {
  title: string
  subtitle?: string | null
  summary?: string | null
  publisher?: string | null
  publicationYear?: number | null
  category?: string | null
  visibility?: Visibility | null
  authors?: string[]
  tags?: string[]
}

export interface OntologySeedConceptPayload {
  title: string
  description?: string | null
  layer: string
  tags?: string[]
  sourceBookTitle?: string | null
  sourceConfidence?: SourceConfidence | null
}

export interface OntologySeedKnowledgeObjectPayload {
  type: KnowledgeObjectType
  title: string
  description?: string | null
  layer: string
  tags?: string[]
  sourceBookTitle?: string | null
  conceptTitle?: string | null
  pageStart?: number | null
  pageEnd?: number | null
  sourceConfidence?: SourceConfidence | null
}

export interface OntologyImportPayload {
  books?: OntologySeedBookPayload[]
  concepts?: OntologySeedConceptPayload[]
  knowledgeObjects?: OntologySeedKnowledgeObjectPayload[]
}

export interface OntologyImportResult {
  dryRun: boolean
  booksCreated: number
  booksExisting: number
  conceptsCreated: number
  conceptsUpdated: number
  conceptsExisting: number
  knowledgeObjectsCreated: number
  knowledgeObjectsUpdated: number
  knowledgeObjectsExisting: number
  sourceReferencesCreated: number
  warnings: string[]
}

export interface BookFilterState {
  q: string
  category: string
  tag: string
}

export const readingStatusOptions: ReadingStatus[] = [
  'BACKLOG',
  'CURRENTLY_READING',
  'COMPLETED',
  'PAUSED',
  'DROPPED',
  'REFERENCE',
  'ANTI_LIBRARY',
]

export const visibilityOptions: Visibility[] = ['PRIVATE', 'SHARED', 'PUBLIC']
