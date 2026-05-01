# BookOS Codex Handoff

Generated for migration on 2026-05-01.

## Project Name

BookOS

## Project Path

D:\【指挥中心】\节操都市\项目\BookOS

## Project Goal

This repository is one of the six local projects being migrated from the old Codex app machine to a new machine. Use the existing README, source tree, tests, and docs as the source of truth for the detailed product or engineering goal.

## Current Project Status

- Git root: D:/【指挥中心】/节操都市/项目/BookOS
- Branch at migration: main
- Latest commit at migration: 
- Origin remote at migration: https://github.com/wenhuorongbing-netizen/BookOS.git
- Top-level structure: .env.example, .git, .gitattributes, .github, .gitignore, AGENTS.md, backend, CHANGELOG.md, docker-compose.full.yml, docker-compose.yml, docs, frontend, README.md, report.md, tools

## Completed Migration Changes

- Confirmed or initialized Git repository.
- Added or updated .gitignore with migration-safe secret, dependency, build, cache, and archive exclusions.
- Checked tracked filenames for obvious sensitive files and removed only clearly sensitive tracked files from the Git index when needed.
- Added or updated AGENTS.md for Codex continuation rules.
- Generated this handoff document.
- Prepared encrypted private archive staging outside the repository.

## Important Design Decisions

- GitHub should contain normal source, tests, documentation, and templates only.
- Local secrets, credentials, private config, local databases, and Codex state belong only in encrypted archives.
- New-machine work should inspect the current tree before relying on old conversation summaries.
- Existing user work should be preserved; do not use destructive Git or filesystem operations unless explicitly requested.

## Current Unfinished Tasks

- Review git status -sb after restoring the repository and private archive.
- Read README and project-specific docs to decide the next development task.
- Run the verification commands listed below.

## Known Issues Or Risks

- Keyword-only sensitive scan hits found: 120. Values were not printed.

## Files To Read First On The New Machine

- README.md, if present
- AGENTS.md
- docs/codex-handoff.md
- Project build files such as package.json, pom.xml, pyproject.toml, requirements.txt, or equivalent

## Commands To Run First On The New Machine

~~~powershell
git status -sb
git remote -v
git branch --show-current
git rev-parse HEAD
~~~

Install:

~~~powershell
cd frontend; npm ci
cd backend; .\mvnw.cmd test
~~~

Start:

~~~powershell
cd frontend; npm run dev
cd backend; .\mvnw.cmd spring-boot:run
~~~

Test:

~~~powershell
cd frontend; npm run e2e
cd backend; .\mvnw.cmd test
~~~

Lint / typecheck:

~~~powershell
cd frontend; npm run typecheck
cd frontend; npm run build
~~~

## Git Status Before Migration Commit

~~~text
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean
~~~

## Files Kept Out Of GitHub And Put Into The Encrypted Private Package

- .env.example
- backend\.env.example
- frontend\.env.example

## User Preferences And Codex Rules

- Do not commit .env, tokens, passwords, secrets, private keys, auth.json, local Codex state, logs, caches, dependencies, build outputs, or migration archives.
- Do not delete local project files or old Codex state.
- Continue processing other projects if one project fails.
- Make direct, pragmatic code changes when requested; avoid stopping at suggestions unless blocked.
- Summarize failures with concrete paths and next steps.