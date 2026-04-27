export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export type RoleName = 'ADMIN' | 'USER'
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
  lensesCount?: number | null
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
  rawCaptureId: number | null
  pageStart: number | null
  pageEnd: number | null
  locationLabel: string | null
  sourceText: string | null
  sourceConfidence: SourceConfidence
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
