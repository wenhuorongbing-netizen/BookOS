# Project Mode Hands-On Guide

BookOS Project Mode turns reading knowledge into game design action. The guided workflow is:

```text
Quote or Concept
-> Apply to Project
-> Create Project Problem
-> Create Design Decision
-> Create Playtest Plan
-> Record Playtest Finding
-> Create Iteration Note
```

## What The Wizard Does

The route `/projects/:id/wizard/apply-knowledge` teaches a practical source-backed workflow:

1. Choose a real source: quote, concept, knowledge object, source reference, or daily design prompt.
2. Define the design problem the source helps with.
3. Apply the source to the project as a project application.
4. Record a design decision with rationale and tradeoffs.
5. Plan a playtest with hypothesis, task, and success criteria.
6. Record a finding and iteration note.
7. Confirm all records at the end.

## Safety Rules

- No project records are created until the final confirmation step.
- Users can skip optional steps.
- Users can save a local draft without writing server records.
- Source references are passed into project problems, applications, decisions, findings, and iteration-note links where available.
- Unknown page numbers remain null. The wizard never invents pages.
- Template daily prompts are labeled as template/source-free when no source reference exists.

## How To Start

From a project:

1. Open `/projects`.
2. Open a project cockpit.
3. Click `Apply Knowledge Guided Flow`.

From a quote or concept:

1. Open a quote or concept detail page.
2. Click `Apply to Project Guided Flow`.
3. BookOS opens the first available project with that source preselected.
4. If no project exists, create a project first.

## What Gets Created

Depending on which steps are skipped, the wizard can create:

- `ProjectProblem`
- `ProjectApplication`
- `DesignDecision`
- `PlaytestPlan`
- `PlaytestFinding`
- `ProjectKnowledgeLink` with relationship `ITERATION_NOTE`

The iteration note uses the existing project knowledge link model so it can remain traceable without adding a separate backend module.

## Verification Checklist

- Cancel before final confirmation and verify no records are created.
- Save draft, reload the wizard, and verify draft content returns.
- Confirm the wizard and verify created records appear in Project Detail.
- Open source-backed project records and verify source references are preserved.
- Verify source-free daily template prompts do not pretend to have source references.
