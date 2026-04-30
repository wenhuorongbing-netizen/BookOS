# BookOS Help System

## Purpose

The help system reduces cognitive load without hiding advanced capabilities. It explains advanced terms at the point of use and links users to concrete workflows.

## Implementation

- `frontend/src/data/helpTopics.ts` is the source of truth for in-app glossary content.
- `/help` lists all help topics.
- `/help/:topic` shows one topic, related topics, and route/use-case links.
- `HelpTooltip` is a small keyboard-focusable trigger for contextual definitions.
- `HelpDrawer` opens when the current route has `?help=<topic>`.

## UX Rules

- Tooltips should explain terms, not repeat visible labels.
- Help should be short, practical, and action-oriented.
- Empty states should explain the page, name the first action, and link to a workflow when useful.
- Help must not hide real errors. Error states should continue to show actionable failure messages.
- Examples may populate inputs only when explicitly requested by the user; they must not create fake records automatically.

## Safety Rules

- Do not present demo content as user data.
- Do not invent page numbers.
- Label template prompts and low-confidence source material honestly.
- Keep AI wording draft-only and no-overwrite.

## Topic Coverage

The current in-app glossary covers:

- Source Reference
- Quick Capture
- Capture Inbox
- Quote
- Action Item
- Concept
- Knowledge Object
- Design Lens
- Daily Prompt
- Project Application
- Design Decision
- Playtest Finding
- Graph
- Backlink
- Review Session
- Mastery
- AI Draft
- Import Preview
