# BookOS API Contract Audit

Date: 2026-04-30

Current SHA at audit start: `9951c85b42b170c54b009c3c6a8e4bf50336e73a`

Verification basis:

- Application controller mappings were verified with `EndpointContractIntegrationTest`.
- Existing integration suites cover runtime behavior for auth, library, notes, captures, quotes, action items, source/security boundaries, knowledge, daily, forum, search/graph/AI, projects, learning, import/export, and migrations.
- `GET /actuator/health` is supplied by Spring Boot Actuator, not an application controller.

Status values:

- `VERIFIED`: Controller mapping exists and is covered by the endpoint contract test or framework actuator mapping.
- `DOCUMENTED_ONLY`: Documentation claims an endpoint that has no matching controller mapping.
- `CODE_ONLY`: Controller mapping exists but is missing from endpoint inventory documentation.
- `BROKEN`: Mapping exists but runtime verification fails.
- `NOT_TESTED`: Mapping exists but was not runtime verified in this audit pass.

Audit result summary:

- Documented-only endpoints after cleanup: none found.
- Code-only endpoints after cleanup: none found.
- Broken endpoint mappings after cleanup: none found.
- Runtime risk: this audit verifies controller mappings; behavior-level coverage relies on existing module integration tests plus the full backend suite.

## Auth, Users, Health

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | `AuthController` | `register` | Public | `RegisterRequest` | `ApiResponse<AuthResponse>` | VERIFIED |
| POST | `/api/auth/login` | `AuthController` | `login` | Public | `LoginRequest` | `ApiResponse<AuthResponse>` | VERIFIED |
| GET | `/api/auth/me` | `AuthController` | `me` | Authenticated | none | `ApiResponse<CurrentUserResponse>` | VERIFIED |
| GET | `/api/users/me/profile` | `UserController` | `profile` | Authenticated | none | `ApiResponse<UserProfileResponse>` | VERIFIED |
| PUT | `/api/users/me/onboarding` | `UserController` | `updateOnboarding` | Authenticated | `OnboardingPreferenceRequest` | `ApiResponse<CurrentUserResponse>` | VERIFIED |
| GET | `/api/users` | `UserController` | `listUsers` | ADMIN | none | `ApiResponse<List<UserAdminResponse>>` | VERIFIED |
| GET | `/api/health` | `HealthController` | `health` | Public | none | `ApiResponse<Map<String,String>>` | VERIFIED |
| GET | `/actuator/health` | Spring Boot Actuator | `HealthEndpoint` | Public/management config | none | Actuator health payload | VERIFIED |

## Demo Workspace

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/demo/status` | `DemoWorkspaceController` | `status` | USER/ADMIN | none | `ApiResponse<DemoWorkspaceStatusResponse>` | VERIFIED |
| POST | `/api/demo/start` | `DemoWorkspaceController` | `start` | USER/ADMIN | none | `ApiResponse<DemoWorkspaceStatusResponse>` | VERIFIED |
| POST | `/api/demo/reset` | `DemoWorkspaceController` | `reset` | USER/ADMIN | none | `ApiResponse<DemoWorkspaceStatusResponse>` | VERIFIED |
| DELETE | `/api/demo` | `DemoWorkspaceController` | `delete` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |

## Books And User Library

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/books` | `BookController` | `listBooks` | USER/ADMIN | query params | `ApiResponse<List<BookResponse>>` | VERIFIED |
| POST | `/api/books` | `BookController` | `createBook` | USER/ADMIN | `BookRequest` | `ApiResponse<BookResponse>` | VERIFIED |
| GET | `/api/books/{id}` | `BookController` | `getBook` | USER/ADMIN | none | `ApiResponse<BookResponse>` | VERIFIED |
| PUT | `/api/books/{id}` | `BookController` | `updateBook` | USER/ADMIN | `BookRequest` | `ApiResponse<BookResponse>` | VERIFIED |
| DELETE | `/api/books/{id}` | `BookController` | `deleteBook` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/books/{id}/add-to-library` | `BookController` | `addToLibrary` | USER/ADMIN | `AddToLibraryRequest` | `ApiResponse<UserBookResponse>` | VERIFIED |
| GET | `/api/user-books` | `UserBookController` | `listLibrary` | USER/ADMIN | none | `ApiResponse<List<UserBookResponse>>` | VERIFIED |
| PUT | `/api/user-books/{id}/status` | `UserBookController` | `updateStatus` | USER/ADMIN | `UpdateUserBookStatusRequest` | `ApiResponse<UserBookResponse>` | VERIFIED |
| PUT | `/api/user-books/{id}/progress` | `UserBookController` | `updateProgress` | USER/ADMIN | `UpdateUserBookProgressRequest` | `ApiResponse<UserBookResponse>` | VERIFIED |
| PUT | `/api/user-books/{id}/rating` | `UserBookController` | `updateRating` | USER/ADMIN | `UpdateUserBookRatingRequest` | `ApiResponse<UserBookResponse>` | VERIFIED |
| GET | `/api/user-books/currently-reading` | `UserBookController` | `currentlyReading` | USER/ADMIN | none | `ApiResponse<List<UserBookResponse>>` | VERIFIED |
| GET | `/api/user-books/five-star` | `UserBookController` | `fiveStar` | USER/ADMIN | none | `ApiResponse<List<UserBookResponse>>` | VERIFIED |
| GET | `/api/user-books/anti-library` | `UserBookController` | `antiLibrary` | USER/ADMIN | none | `ApiResponse<List<UserBookResponse>>` | VERIFIED |

## Notes, Blocks, Parser, Captures

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/books/{bookId}/notes` | `BookNoteController` | `listBookNotes` | USER/ADMIN | none | `ApiResponse<List<BookNoteResponse>>` | VERIFIED |
| POST | `/api/books/{bookId}/notes` | `BookNoteController` | `createNote` | USER/ADMIN | `BookNoteRequest` | `ApiResponse<BookNoteResponse>` | VERIFIED |
| GET | `/api/notes/{id}` | `BookNoteController` | `getNote` | USER/ADMIN | none | `ApiResponse<BookNoteResponse>` | VERIFIED |
| PUT | `/api/notes/{id}` | `BookNoteController` | `updateNote` | USER/ADMIN | `BookNoteRequest` | `ApiResponse<BookNoteResponse>` | VERIFIED |
| DELETE | `/api/notes/{id}` | `BookNoteController` | `archiveNote` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/notes/{id}/blocks` | `BookNoteController` | `addBlock` | USER/ADMIN | `NoteBlockRequest` | `ApiResponse<NoteBlockResponse>` | VERIFIED |
| PUT | `/api/note-blocks/{id}` | `BookNoteController` | `updateBlock` | USER/ADMIN | `NoteBlockRequest` | `ApiResponse<NoteBlockResponse>` | VERIFIED |
| DELETE | `/api/note-blocks/{id}` | `BookNoteController` | `deleteBlock` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/parser/preview` | `ParserController` | `preview` | USER/ADMIN | `ParserPreviewRequest` | `ApiResponse<ParsedNoteResponse>` | VERIFIED |
| POST | `/api/captures` | `RawCaptureController` | `createCapture` | USER/ADMIN | `RawCaptureRequest` | `ApiResponse<RawCaptureResponse>` | VERIFIED |
| GET | `/api/captures/inbox` | `RawCaptureController` | `listInbox` | USER/ADMIN | query params | `ApiResponse<List<RawCaptureResponse>>` | VERIFIED |
| GET | `/api/captures` | `RawCaptureController` | `listCaptures` | USER/ADMIN | query params | `ApiResponse<List<RawCaptureResponse>>` | VERIFIED |
| GET | `/api/captures/{id}` | `RawCaptureController` | `getCapture` | USER/ADMIN | none | `ApiResponse<RawCaptureResponse>` | VERIFIED |
| PUT | `/api/captures/{id}` | `RawCaptureController` | `updateCapture` | USER/ADMIN | `RawCaptureUpdateRequest` | `ApiResponse<RawCaptureResponse>` | VERIFIED |
| POST | `/api/captures/{id}/convert` | `RawCaptureController` | `convertCapture` | USER/ADMIN | `RawCaptureConvertRequest` | `ApiResponse<CaptureConversionResponse>` | VERIFIED |
| POST | `/api/captures/{id}/review/concepts` | `RawCaptureController` | `reviewCaptureConcepts` | USER/ADMIN | `ConceptReviewRequest` | `ApiResponse<ConceptReviewResponse>` | VERIFIED |
| PUT | `/api/captures/{id}/archive` | `RawCaptureController` | `archiveCapture` | USER/ADMIN | none | `ApiResponse<RawCaptureResponse>` | VERIFIED |

## Quotes And Action Items

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/quotes` | `QuoteController` | `listQuotes` | USER/ADMIN | query params | `ApiResponse<List<QuoteResponse>>` | VERIFIED |
| POST | `/api/quotes` | `QuoteController` | `createQuote` | USER/ADMIN | `QuoteRequest` | `ApiResponse<QuoteResponse>` | VERIFIED |
| GET | `/api/quotes/{id}` | `QuoteController` | `getQuote` | USER/ADMIN | none | `ApiResponse<QuoteResponse>` | VERIFIED |
| PUT | `/api/quotes/{id}` | `QuoteController` | `updateQuote` | USER/ADMIN | `QuoteRequest` | `ApiResponse<QuoteResponse>` | VERIFIED |
| DELETE | `/api/quotes/{id}` | `QuoteController` | `archiveQuote` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/captures/{id}/convert/quote` | `QuoteController` | `convertCaptureToQuote` | USER/ADMIN | none | `ApiResponse<CaptureConversionResponse>` | VERIFIED |
| GET | `/api/action-items` | `ActionItemController` | `listActionItems` | USER/ADMIN | query params | `ApiResponse<List<ActionItemResponse>>` | VERIFIED |
| POST | `/api/action-items` | `ActionItemController` | `createActionItem` | USER/ADMIN | `ActionItemRequest` | `ApiResponse<ActionItemResponse>` | VERIFIED |
| GET | `/api/action-items/{id}` | `ActionItemController` | `getActionItem` | USER/ADMIN | none | `ApiResponse<ActionItemResponse>` | VERIFIED |
| PUT | `/api/action-items/{id}` | `ActionItemController` | `updateActionItem` | USER/ADMIN | `ActionItemRequest` | `ApiResponse<ActionItemResponse>` | VERIFIED |
| PUT | `/api/action-items/{id}/complete` | `ActionItemController` | `completeActionItem` | USER/ADMIN | none | `ApiResponse<ActionItemResponse>` | VERIFIED |
| PUT | `/api/action-items/{id}/reopen` | `ActionItemController` | `reopenActionItem` | USER/ADMIN | none | `ApiResponse<ActionItemResponse>` | VERIFIED |
| DELETE | `/api/action-items/{id}` | `ActionItemController` | `archiveActionItem` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/captures/{id}/convert/action-item` | `ActionItemController` | `convertCaptureToActionItem` | USER/ADMIN | `CaptureToActionItemRequest` | `ApiResponse<CaptureConversionResponse>` | VERIFIED |

## Source References, Entity Links, Backlinks

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/source-references/{id}` | `SourceReferenceController` | `getSourceReference` | USER/ADMIN | none | `ApiResponse<SourceReferenceResponse>` | VERIFIED |
| GET | `/api/source-references` | `SourceReferenceController` | `listSourceReferences` | USER/ADMIN | query params | `ApiResponse<List<SourceReferenceResponse>>` | VERIFIED |
| GET | `/api/books/{bookId}/source-references` | `SourceReferenceController` | `listBookSources` | USER/ADMIN | none | `ApiResponse<List<SourceReferenceResponse>>` | VERIFIED |
| GET | `/api/notes/{noteId}/source-references` | `SourceReferenceController` | `listNoteSources` | USER/ADMIN | none | `ApiResponse<List<SourceReferenceResponse>>` | VERIFIED |
| GET | `/api/captures/{captureId}/source-references` | `SourceReferenceController` | `listCaptureSources` | USER/ADMIN | none | `ApiResponse<List<SourceReferenceResponse>>` | VERIFIED |
| GET | `/api/entity-links` | `EntityLinkController` | `listEntityLinks` | USER/ADMIN | query params | `ApiResponse<List<EntityLinkResponse>>` | VERIFIED |
| POST | `/api/entity-links` | `EntityLinkController` | `createEntityLink` | USER/ADMIN | `EntityLinkRequest` | `ApiResponse<EntityLinkResponse>` | VERIFIED |
| PUT | `/api/entity-links/{id}` | `EntityLinkController` | `updateEntityLink` | USER/ADMIN | `EntityLinkRequest` | `ApiResponse<EntityLinkResponse>` | VERIFIED |
| DELETE | `/api/entity-links/{id}` | `EntityLinkController` | `deleteEntityLink` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/backlinks` | `BacklinkController` | `listBacklinks` | USER/ADMIN | query params | `ApiResponse<List<BacklinkResponse>>` | VERIFIED |

## Concepts, Knowledge Objects, Admin Ontology

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/concepts` | `ConceptController` | `listConcepts` | USER/ADMIN | query params | `ApiResponse<List<ConceptResponse>>` | VERIFIED |
| POST | `/api/concepts` | `ConceptController` | `createConcept` | USER/ADMIN | `ConceptRequest` | `ApiResponse<ConceptResponse>` | VERIFIED |
| GET | `/api/concepts/{id}` | `ConceptController` | `getConcept` | USER/ADMIN | none | `ApiResponse<ConceptResponse>` | VERIFIED |
| PUT | `/api/concepts/{id}` | `ConceptController` | `updateConcept` | USER/ADMIN | `ConceptRequest` | `ApiResponse<ConceptResponse>` | VERIFIED |
| DELETE | `/api/concepts/{id}` | `ConceptController` | `archiveConcept` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/books/{bookId}/concepts` | `BookConceptController` | `listBookConcepts` | USER/ADMIN | none | `ApiResponse<List<ConceptResponse>>` | VERIFIED |
| GET | `/api/knowledge-objects` | `KnowledgeObjectController` | `listKnowledgeObjects` | USER/ADMIN | query params | `ApiResponse<List<KnowledgeObjectResponse>>` | VERIFIED |
| POST | `/api/knowledge-objects` | `KnowledgeObjectController` | `createKnowledgeObject` | USER/ADMIN | `KnowledgeObjectRequest` | `ApiResponse<KnowledgeObjectResponse>` | VERIFIED |
| GET | `/api/knowledge-objects/{id}` | `KnowledgeObjectController` | `getKnowledgeObject` | USER/ADMIN | none | `ApiResponse<KnowledgeObjectResponse>` | VERIFIED |
| PUT | `/api/knowledge-objects/{id}` | `KnowledgeObjectController` | `updateKnowledgeObject` | USER/ADMIN | `KnowledgeObjectRequest` | `ApiResponse<KnowledgeObjectResponse>` | VERIFIED |
| DELETE | `/api/knowledge-objects/{id}` | `KnowledgeObjectController` | `archiveKnowledgeObject` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/admin/ontology/default` | `OntologyAdminController` | `defaultSeed` | ADMIN | none | `ApiResponse<OntologyImportRequest>` | VERIFIED |
| POST | `/api/admin/ontology/import/default` | `OntologyAdminController` | `importDefault` | ADMIN | query params | `ApiResponse<OntologyImportResponse>` | VERIFIED |
| POST | `/api/admin/ontology/import` | `OntologyAdminController` | `importSeed` | ADMIN | `OntologyImportRequest` | `ApiResponse<OntologyImportResponse>` | VERIFIED |

## Daily, Learning, Analytics

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/daily/today` | `DailyController` | `today` | USER/ADMIN | none | `ApiResponse<DailyTodayResponse>` | VERIFIED |
| POST | `/api/daily/regenerate` | `DailyController` | `regenerate` | USER/ADMIN | `DailyRegenerateRequest` | `ApiResponse<DailyTodayResponse>` | VERIFIED |
| POST | `/api/daily/skip` | `DailyController` | `skip` | USER/ADMIN | `DailySkipRequest` | `ApiResponse<DailyTodayResponse>` | VERIFIED |
| POST | `/api/daily/reflections` | `DailyController` | `saveReflection` | USER/ADMIN | `DailyReflectionRequest` | `ApiResponse<DailyReflectionResponse>` | VERIFIED |
| GET | `/api/daily/history` | `DailyController` | `history` | USER/ADMIN | none | `ApiResponse<List<DailyHistoryResponse>>` | VERIFIED |
| POST | `/api/daily/create-prototype-task` | `DailyController` | `createPrototypeTask` | USER/ADMIN | `DailyPrototypeTaskRequest` | `ApiResponse<KnowledgeObjectResponse>` | VERIFIED |
| GET | `/api/reading-sessions` | `ReadingSessionController` | `list` | Authenticated | none | `ApiResponse<List<ReadingSessionResponse>>` | VERIFIED |
| POST | `/api/reading-sessions/start` | `ReadingSessionController` | `start` | Authenticated | `ReadingSessionStartRequest` | `ApiResponse<ReadingSessionResponse>` | VERIFIED |
| PUT | `/api/reading-sessions/{id}/finish` | `ReadingSessionController` | `finish` | Authenticated | `ReadingSessionFinishRequest` | `ApiResponse<ReadingSessionResponse>` | VERIFIED |
| GET | `/api/books/{bookId}/reading-sessions` | `ReadingSessionController` | `listForBook` | Authenticated | none | `ApiResponse<List<ReadingSessionResponse>>` | VERIFIED |
| GET | `/api/review/sessions` | `ReviewController` | `list` | Authenticated | none | `ApiResponse<List<ReviewSessionResponse>>` | VERIFIED |
| POST | `/api/review/sessions` | `ReviewController` | `create` | Authenticated | `ReviewSessionRequest` | `ApiResponse<ReviewSessionResponse>` | VERIFIED |
| GET | `/api/review/sessions/{id}` | `ReviewController` | `get` | Authenticated | none | `ApiResponse<ReviewSessionResponse>` | VERIFIED |
| POST | `/api/review/sessions/{id}/items` | `ReviewController` | `addItem` | Authenticated | `ReviewItemRequest` | `ApiResponse<ReviewItemResponse>` | VERIFIED |
| PUT | `/api/review/items/{id}` | `ReviewController` | `updateItem` | Authenticated | `ReviewItemUpdateRequest` | `ApiResponse<ReviewItemResponse>` | VERIFIED |
| POST | `/api/review/generate-from-book` | `ReviewController` | `generateFromBook` | Authenticated | `ReviewGenerateRequest` | `ApiResponse<ReviewSessionResponse>` | VERIFIED |
| POST | `/api/review/generate-from-concept` | `ReviewController` | `generateFromConcept` | Authenticated | `ReviewGenerateRequest` | `ApiResponse<ReviewSessionResponse>` | VERIFIED |
| POST | `/api/review/generate-from-project` | `ReviewController` | `generateFromProject` | Authenticated | `ReviewGenerateRequest` | `ApiResponse<ReviewSessionResponse>` | VERIFIED |
| GET | `/api/mastery` | `MasteryController` | `list` | Authenticated | none | `ApiResponse<List<KnowledgeMasteryResponse>>` | VERIFIED |
| GET | `/api/mastery/target` | `MasteryController` | `getTarget` | Authenticated | query params | `ApiResponse<KnowledgeMasteryResponse>` | VERIFIED |
| PUT | `/api/mastery/target` | `MasteryController` | `updateTarget` | Authenticated | `KnowledgeMasteryRequest` | `ApiResponse<KnowledgeMasteryResponse>` | VERIFIED |
| GET | `/api/analytics/reading` | `AnalyticsController` | `reading` | Authenticated | none | `ApiResponse<ReadingAnalyticsResponse>` | VERIFIED |
| GET | `/api/analytics/knowledge` | `AnalyticsController` | `knowledge` | Authenticated | none | `ApiResponse<KnowledgeAnalyticsResponse>` | VERIFIED |
| GET | `/api/analytics/books/{bookId}` | `AnalyticsController` | `book` | Authenticated | none | `ApiResponse<BookAnalyticsResponse>` | VERIFIED |

## Import, Export, Forum

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/export/json` | `DataExportController` | `exportJson` | USER/ADMIN | none | `ApiResponse<Map<String,Object>>` | VERIFIED |
| GET | `/api/export/book/{bookId}/json` | `DataExportController` | `exportBookJson` | USER/ADMIN | none | `ApiResponse<Map<String,Object>>` | VERIFIED |
| GET | `/api/export/book/{bookId}/markdown` | `DataExportController` | `exportBookMarkdown` | USER/ADMIN | none | Markdown text | VERIFIED |
| GET | `/api/export/quotes/csv` | `DataExportController` | `exportQuotesCsv` | USER/ADMIN | none | CSV text | VERIFIED |
| GET | `/api/export/action-items/csv` | `DataExportController` | `exportActionItemsCsv` | USER/ADMIN | none | CSV text | VERIFIED |
| GET | `/api/export/concepts/csv` | `DataExportController` | `exportConceptsCsv` | USER/ADMIN | none | CSV text | VERIFIED |
| POST | `/api/import/preview` | `DataImportController` | `previewImport` | USER/ADMIN | `ImportRequest` | `ApiResponse<ImportPreviewResponse>` | VERIFIED |
| POST | `/api/import/commit` | `DataImportController` | `commitImport` | USER/ADMIN | `ImportRequest` | `ApiResponse<ImportCommitResponse>` | VERIFIED |
| GET | `/api/forum/categories` | `ForumController` | `categories` | USER/ADMIN/MODERATOR | none | `ApiResponse<List<ForumCategoryResponse>>` | VERIFIED |
| POST | `/api/forum/categories` | `ForumController` | `createCategory` | ADMIN/MODERATOR | `ForumCategoryRequest` | `ApiResponse<ForumCategoryResponse>` | VERIFIED |
| GET | `/api/forum/templates` | `ForumController` | `templates` | USER/ADMIN/MODERATOR | none | `ApiResponse<List<StructuredPostTemplateResponse>>` | VERIFIED |
| GET | `/api/forum/threads` | `ForumController` | `threads` | USER/ADMIN/MODERATOR | query params | `ApiResponse<List<ForumThreadResponse>>` | VERIFIED |
| POST | `/api/forum/threads` | `ForumController` | `createThread` | USER/ADMIN/MODERATOR | `ForumThreadRequest` | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| GET | `/api/forum/threads/{id}` | `ForumController` | `thread` | USER/ADMIN/MODERATOR | none | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| PUT | `/api/forum/threads/{id}` | `ForumController` | `updateThread` | USER/ADMIN/MODERATOR | `ForumThreadRequest` | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| DELETE | `/api/forum/threads/{id}` | `ForumController` | `deleteThread` | USER/ADMIN/MODERATOR | none | `ApiResponse<Void>` | VERIFIED |
| PUT | `/api/forum/threads/{id}/moderation` | `ForumController` | `moderateThread` | ADMIN/MODERATOR | `ForumModerationRequest` | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| GET | `/api/forum/threads/{id}/comments` | `ForumController` | `comments` | USER/ADMIN/MODERATOR | none | `ApiResponse<List<ForumCommentResponse>>` | VERIFIED |
| POST | `/api/forum/threads/{id}/comments` | `ForumController` | `createComment` | USER/ADMIN/MODERATOR | `ForumCommentRequest` | `ApiResponse<ForumCommentResponse>` | VERIFIED |
| PUT | `/api/forum/comments/{id}` | `ForumController` | `updateComment` | USER/ADMIN/MODERATOR | `ForumCommentRequest` | `ApiResponse<ForumCommentResponse>` | VERIFIED |
| DELETE | `/api/forum/comments/{id}` | `ForumController` | `deleteComment` | USER/ADMIN/MODERATOR | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/forum/threads/{id}/bookmark` | `ForumController` | `bookmark` | USER/ADMIN/MODERATOR | none | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| DELETE | `/api/forum/threads/{id}/bookmark` | `ForumController` | `removeBookmark` | USER/ADMIN/MODERATOR | none | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| POST | `/api/forum/threads/{id}/like` | `ForumController` | `like` | USER/ADMIN/MODERATOR | none | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| DELETE | `/api/forum/threads/{id}/like` | `ForumController` | `removeLike` | USER/ADMIN/MODERATOR | none | `ApiResponse<ForumThreadResponse>` | VERIFIED |
| POST | `/api/forum/threads/{id}/report` | `ForumController` | `report` | USER/ADMIN/MODERATOR | `ForumReportRequest` | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/forum/reports` | `ForumController` | `reports` | ADMIN/MODERATOR | query params | `ApiResponse<List<ForumReportResponse>>` | VERIFIED |
| PUT | `/api/forum/reports/{id}/resolve` | `ForumController` | `resolveReport` | ADMIN/MODERATOR | none | `ApiResponse<ForumReportResponse>` | VERIFIED |

## Search, Graph, AI

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/search` | `SearchController` | `search` | USER/ADMIN | query params | `ApiResponse<List<SearchResultResponse>>` | VERIFIED |
| GET | `/api/graph` | `GraphController` | `getWorkspaceGraph` | USER/ADMIN | query params | `ApiResponse<GraphResponse>` | VERIFIED |
| GET | `/api/graph/book/{bookId}` | `GraphController` | `getBookGraph` | USER/ADMIN | query params | `ApiResponse<GraphResponse>` | VERIFIED |
| GET | `/api/graph/concept/{conceptId}` | `GraphController` | `getConceptGraph` | USER/ADMIN | query params | `ApiResponse<GraphResponse>` | VERIFIED |
| GET | `/api/graph/project/{projectId}` | `GraphController` | `getProjectGraph` | USER/ADMIN | query params | `ApiResponse<GraphResponse>` | VERIFIED |
| GET | `/api/ai/status` | `AISuggestionController` | `providerStatus` | USER/ADMIN | none | `ApiResponse<AIProviderStatusResponse>` | VERIFIED |
| POST | `/api/ai/suggestions/note-summary` | `AISuggestionController` | `createNoteSummary` | USER/ADMIN | `AISuggestionRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| POST | `/api/ai/suggestions/extract-actions` | `AISuggestionController` | `extractActions` | USER/ADMIN | `AISuggestionRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| POST | `/api/ai/suggestions/extract-concepts` | `AISuggestionController` | `extractConcepts` | USER/ADMIN | `AISuggestionRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| POST | `/api/ai/suggestions/design-lenses` | `AISuggestionController` | `suggestDesignLenses` | USER/ADMIN | `AISuggestionRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| POST | `/api/ai/suggestions/project-applications` | `AISuggestionController` | `suggestProjectApplications` | USER/ADMIN | `AISuggestionRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| POST | `/api/ai/suggestions/forum-thread` | `AISuggestionController` | `suggestForumThread` | USER/ADMIN | `AISuggestionRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| GET | `/api/ai/suggestions` | `AISuggestionController` | `listSuggestions` | USER/ADMIN | none | `ApiResponse<List<AISuggestionResponse>>` | VERIFIED |
| PUT | `/api/ai/suggestions/{id}/accept` | `AISuggestionController` | `acceptSuggestion` | USER/ADMIN | none | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| PUT | `/api/ai/suggestions/{id}/reject` | `AISuggestionController` | `rejectSuggestion` | USER/ADMIN | none | `ApiResponse<AISuggestionResponse>` | VERIFIED |
| PUT | `/api/ai/suggestions/{id}/edit` | `AISuggestionController` | `editSuggestion` | USER/ADMIN | `AISuggestionEditRequest` | `ApiResponse<AISuggestionResponse>` | VERIFIED |

## Projects

| Method | Path | Controller class | Controller method | Auth requirement | Request DTO | Response DTO | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/projects` | `ProjectController` | `listProjects` | USER/ADMIN | none | `ApiResponse<List<GameProjectResponse>>` | VERIFIED |
| POST | `/api/projects` | `ProjectController` | `createProject` | USER/ADMIN | `GameProjectRequest` | `ApiResponse<GameProjectResponse>` | VERIFIED |
| GET | `/api/projects/{id}` | `ProjectController` | `getProject` | USER/ADMIN | none | `ApiResponse<GameProjectResponse>` | VERIFIED |
| PUT | `/api/projects/{id}` | `ProjectController` | `updateProject` | USER/ADMIN | `GameProjectRequest` | `ApiResponse<GameProjectResponse>` | VERIFIED |
| DELETE | `/api/projects/{id}` | `ProjectController` | `deleteProject` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| PUT | `/api/projects/{id}/archive` | `ProjectController` | `archiveProject` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/problems` | `ProjectController` | `listProblems` | USER/ADMIN | none | `ApiResponse<List<ProjectProblemResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/problems` | `ProjectController` | `createProblem` | USER/ADMIN | `ProjectProblemRequest` | `ApiResponse<ProjectProblemResponse>` | VERIFIED |
| PUT | `/api/project-problems/{id}` | `ProjectController` | `updateProblem` | USER/ADMIN | `ProjectProblemRequest` | `ApiResponse<ProjectProblemResponse>` | VERIFIED |
| DELETE | `/api/project-problems/{id}` | `ProjectController` | `deleteProblem` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/applications` | `ProjectController` | `listApplications` | USER/ADMIN | none | `ApiResponse<List<ProjectApplicationResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/applications` | `ProjectController` | `createApplication` | USER/ADMIN | `ProjectApplicationRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
| PUT | `/api/project-applications/{id}` | `ProjectController` | `updateApplication` | USER/ADMIN | `ProjectApplicationRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
| DELETE | `/api/project-applications/{id}` | `ProjectController` | `deleteApplication` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/decisions` | `ProjectController` | `listDecisions` | USER/ADMIN | none | `ApiResponse<List<DesignDecisionResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/decisions` | `ProjectController` | `createDecision` | USER/ADMIN | `DesignDecisionRequest` | `ApiResponse<DesignDecisionResponse>` | VERIFIED |
| PUT | `/api/design-decisions/{id}` | `ProjectController` | `updateDecision` | USER/ADMIN | `DesignDecisionRequest` | `ApiResponse<DesignDecisionResponse>` | VERIFIED |
| DELETE | `/api/design-decisions/{id}` | `ProjectController` | `deleteDecision` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/playtest-plans` | `ProjectController` | `listPlaytestPlans` | USER/ADMIN | none | `ApiResponse<List<PlaytestPlanResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/playtest-plans` | `ProjectController` | `createPlaytestPlan` | USER/ADMIN | `PlaytestPlanRequest` | `ApiResponse<PlaytestPlanResponse>` | VERIFIED |
| PUT | `/api/playtest-plans/{id}` | `ProjectController` | `updatePlaytestPlan` | USER/ADMIN | `PlaytestPlanRequest` | `ApiResponse<PlaytestPlanResponse>` | VERIFIED |
| DELETE | `/api/playtest-plans/{id}` | `ProjectController` | `deletePlaytestPlan` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/playtest-findings` | `ProjectController` | `listPlaytestFindings` | USER/ADMIN | none | `ApiResponse<List<PlaytestFindingResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/playtest-findings` | `ProjectController` | `createPlaytestFinding` | USER/ADMIN | `PlaytestFindingRequest` | `ApiResponse<PlaytestFindingResponse>` | VERIFIED |
| PUT | `/api/playtest-findings/{id}` | `ProjectController` | `updatePlaytestFinding` | USER/ADMIN | `PlaytestFindingRequest` | `ApiResponse<PlaytestFindingResponse>` | VERIFIED |
| DELETE | `/api/playtest-findings/{id}` | `ProjectController` | `deletePlaytestFinding` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/knowledge-links` | `ProjectController` | `listKnowledgeLinks` | USER/ADMIN | none | `ApiResponse<List<ProjectKnowledgeLinkResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/knowledge-links` | `ProjectController` | `createKnowledgeLink` | USER/ADMIN | `ProjectKnowledgeLinkRequest` | `ApiResponse<ProjectKnowledgeLinkResponse>` | VERIFIED |
| DELETE | `/api/project-knowledge-links/{id}` | `ProjectController` | `deleteKnowledgeLink` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| GET | `/api/projects/{projectId}/lens-reviews` | `ProjectController` | `listLensReviews` | USER/ADMIN | none | `ApiResponse<List<ProjectLensReviewResponse>>` | VERIFIED |
| POST | `/api/projects/{projectId}/lens-reviews` | `ProjectController` | `createLensReview` | USER/ADMIN | `ProjectLensReviewRequest` | `ApiResponse<ProjectLensReviewResponse>` | VERIFIED |
| PUT | `/api/project-lens-reviews/{id}` | `ProjectController` | `updateLensReview` | USER/ADMIN | `ProjectLensReviewRequest` | `ApiResponse<ProjectLensReviewResponse>` | VERIFIED |
| DELETE | `/api/project-lens-reviews/{id}` | `ProjectController` | `deleteLensReview` | USER/ADMIN | none | `ApiResponse<Void>` | VERIFIED |
| POST | `/api/projects/{projectId}/apply/source-reference` | `ProjectController` | `applySourceReference` | USER/ADMIN | `ApplySourceRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
| POST | `/api/projects/{projectId}/apply/quote` | `ProjectController` | `applyQuote` | USER/ADMIN | `ApplySourceRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
| POST | `/api/projects/{projectId}/apply/concept` | `ProjectController` | `applyConcept` | USER/ADMIN | `ApplySourceRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
| POST | `/api/projects/{projectId}/apply/knowledge-object` | `ProjectController` | `applyKnowledgeObject` | USER/ADMIN | `ApplySourceRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
| POST | `/api/projects/{projectId}/create-prototype-task-from-daily` | `ProjectController` | `createPrototypeTaskFromDaily` | USER/ADMIN | `ProjectPrototypeTaskFromDailyRequest` | `ApiResponse<ProjectApplicationResponse>` | VERIFIED |
