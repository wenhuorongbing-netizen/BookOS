# BookOS PO Decision Report

Last reviewed: 2026-05-01.

Reviewed branch: `main`.

Reviewed SHA: `5ef591275c83b3f22ca7d83cd88d2ca7fdb6c578`.

## Research Boundary

This report is based on repository inspection, existing usability documentation, and automated verification planning. It does not claim external user research. Human sessions still need to be run using the usability study package.

## Executive Summary

BookOS is strongest when it is explained as a practical loop:

Read -> capture -> process -> open source -> review or apply to project.

The engineering foundation is broad and capable. The product risk is no longer whether modules exist; it is whether users can find the right first action without being overwhelmed by advanced modules.

## What Is Now Easier

- New users have onboarding, modes, use cases, help, and demo workspace surfaces.
- Reader and Note-Taker flows are understandable enough for controlled beta.
- Game Designer Mode has a differentiated value proposition through Apply to Project.
- Quick Capture is more learnable through examples, parser preview, and beginner structure.
- Source links are visible enough to make converted records feel trustworthy.
- MockAIProvider is framed as draft-only rather than authoritative automation.

## What Remains Bloated

- Too many advanced capabilities still exist near the first-day mental model.
- Graph, Analytics, Import/Export, AI, Admin Ontology, and global Knowledge management should not compete with Add Book and Capture.
- Project Mode is valuable but still has many sub-workflows.
- Researcher and Community flows are less direct than Reader and Game Designer flows.
- Some labels remain implementation-oriented.

## Features That Should Be Hidden by Default

- Knowledge Graph workspace.
- Analytics.
- Import/Export.
- AI provider/settings.
- Ontology Import, admin-only.
- Learning Progress before review data exists.
- Global Design Knowledge management before concepts exist.
- Forum popular/recent sections before real activity exists.

## Terms That Should Be Renamed or Kept User-Friendly

| Current Term | Recommended User Label |
| --- | --- |
| Capture Inbox | Process Captures |
| Action Items | Actions |
| Knowledge Objects | Design Knowledge |
| Source Reference | Source Link |
| Entity Links | Relationships |
| Backlinks | Related Links |
| Mastery | Learning Progress |
| AI Suggestions | Draft Assistant |
| Admin Ontology | Ontology Import |
| Graph | Knowledge Graph |

## Strongest User Modes

| Mode | Current Verdict | Reason |
| --- | --- | --- |
| Reader | PASS | Clear first loop: add book, capture, convert, open source. |
| Note-Taker | PASS | Capture and conversion workflows directly match user intent. |
| Game Designer | PASS with caveats | Strong differentiated loop, but Project Mode still needs guided framing. |

## Modes Needing More Work

| Mode | Current Verdict | Primary Issue |
| --- | --- | --- |
| Researcher | PARTIAL | Concepts, graph, review, and learning progress need a simpler start path. |
| Community | PARTIAL | Forum is strongest from source context, not as an empty destination. |
| Advanced | PARTIAL by design | Useful for power users, but should not dominate default navigation. |

## Top 10 Next UX Changes

1. Make Guided First Loop the default empty-account CTA.
2. Rename "Capture Inbox" consistently to "Process Captures."
3. Collapse advanced Dashboard cards until data exists.
4. Make source links the trust anchor on quote, action, note, concept, project, and forum pages.
5. Make Project Wizard the default project onboarding path for Game Designer Mode.
6. Move global Knowledge Graph behind contextual graph entry points.
7. Merge Review and Learning Progress into a single learning loop for non-advanced users.
8. Show Forum creation primarily from book, quote, concept, project, and source contexts.
9. Keep Import/Export in More and position it as backup/portability.
10. Run real moderated usability sessions before expanding beta claims.

## Top 10 Bugs or Friction Points

1. Users may not understand why captures need processing.
2. Users may confuse note, note block, raw capture, quote, and action.
3. Users may see Graph too early and interpret an empty graph as product failure.
4. Users may not understand Knowledge Objects without a concept-review path.
5. Users may not know when to use Review versus Learning Progress.
6. Forum may feel empty unless started from source context.
7. Project subpages may feel like too many decisions for a first project.
8. Draft Assistant copy must keep reinforcing no overwrite.
9. Demo data must remain visibly separate from real data.
10. The dirty worktree needs commit slicing before release-management confidence improves.

## P0 Usability Blockers

None verified in current documentation review. Human testing may reveal blockers.

## P1 Usability Issues

- Researcher, Community, and Advanced modes are still less self-explanatory than Reader, Note-Taker, and Game Designer modes.
- Advanced features need stronger data-dependent hiding and contextual entry points.
- Terminology cleanup should continue in page headers, empty states, breadcrumbs, and help text.
- The Product Wizard should be observed with real users to confirm final review and failure recovery copy.

## P2 Polish Issues

- Add stronger examples in empty states for source-linked forum and review flows.
- Improve "why am I seeing this?" explanations for mode-specific dashboards.
- Add clearer completion language to executable use cases and demo tutorials.
- Keep scoped graph actions closer to entity detail pages.

## Recommended Next Milestone

Product Slimming 0.2.

Controlled beta may continue with caveats, but broad public-beta expansion should wait until:

- The human usability study is run.
- Product Slimming 0.2 reduces first-day cognitive load.
- Reader, Note-Taker, and Game Designer flows pass with real participants.
- Researcher and Community paths improve from partial to clear.

## Release Recommendation

Release with caveats for controlled beta only.

Do not broaden release messaging until real user sessions confirm that a new user can complete the first source-backed loop in 15 minutes without explanation.

