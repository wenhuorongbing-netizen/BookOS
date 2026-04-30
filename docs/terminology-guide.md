# BookOS Terminology Guide

BookOS keeps technical API names stable while using task-focused labels in the UI. Backend entity names, database tables, API paths, and existing routes should not be renamed for copy polish.

## User-Facing Terms

| Technical term | Preferred UI label | Notes |
| --- | --- | --- |
| Capture Inbox | Process Captures | Use for the workflow where raw captures become notes, quotes, actions, or reviewed concepts. Existing route remains `/captures/inbox`; `/capture` is an alias. |
| ActionItem / Action Items | Action / Actions | Use `Actions` for navigation and page titles. Technical docs and API names may still say `ActionItem`. Existing route remains `/action-items`; `/actions` is an alias. |
| KnowledgeObject / Knowledge Objects | Design Knowledge or Knowledge | Use `Design Knowledge` when the context is game design or structured lenses, principles, methods, exercises, and patterns. |
| Mastery | Learning Progress | Use for user-facing review and confidence progress. Existing route remains `/mastery`; `/learning-progress` is an alias. |
| EntityLink / Entity Links | Relationship / Relationships | Use when users create or inspect links between records. Technical docs may use `EntityLink`. |
| SourceReference / Source Reference | Source Link or Source | Use `Source Link` for traceability panels and help text. Use `Open Source` for actions. Technical docs may use `SourceReference` or `Source Reference`. |
| AISuggestion / AI Suggestions | Draft Assistant | Use for the right rail assistant and optional AI workflow. Always state drafts do not overwrite user content. |
| Admin Ontology | Ontology Import | Use for admin seed/import screens. Existing route remains `/admin/ontology`; `/admin/ontology-import` is an alias. |
| Graph | Knowledge Graph | Use especially under Advanced navigation. The graph must be real-data only. |
| Backlinks | Related Links | Use on detail pages when showing records that point to the current record. Technical docs may mention backlinks. |

## Copy Rules

- Use task language first: `Process Captures`, `Open Source`, `Apply to Project`, `Start Review`.
- Keep advanced terms available but explain them with one sentence and a first action.
- Do not rename backend packages, entity classes, DTOs, API adapters, or persisted enum values for wording changes.
- Do not claim unfinished features are complete. If a capability is advanced or limited, label it honestly.
- Unknown pages must remain `Page unknown`; never infer page numbers for clearer copy.

## Route Stability

Primary routes remain stable for existing links and tests:

- `/captures/inbox` remains the canonical Process Captures route.
- `/action-items` remains the canonical Actions route.
- `/mastery` remains the canonical Learning Progress route.
- `/graph` remains the canonical Knowledge Graph route.
- `/admin/ontology` remains the canonical Ontology Import route.

Friendly aliases may be added when they reduce confusion, but aliases must not replace canonical routes until a deliberate migration plan exists.
