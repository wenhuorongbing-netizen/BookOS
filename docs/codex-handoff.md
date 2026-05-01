# BookOS Codex 可迁移交接文档

生成时间：2026-05-01

当前仓库：`D:\【指挥中心】\节操都市\项目\BookOS`

当前分支：`main`

当前已验证 SHA：`c62e9eaa163e9ae7192046dceda09a6bf2470091`

GitHub remote：`https://github.com/wenhuorongbing-netizen/BookOS.git`

注意：当前工作树不是干净状态。存在大量已修改文件和少量未跟踪文件。这些是 Product Slimming / usability / E2E / demo / use-case progress 等多轮工作的本地成果。下一台机器接手时必须先确认哪些改动已提交、哪些仍是本地未提交改动，不要假设当前本地状态已经推送到 GitHub。

本次迁移前 Git 状态摘要：

- 当前目录已确认是 BookOS 项目根目录，包含 `.git`、`backend/`、`frontend/`、`docs/`、`README.md`、`docker-compose.yml`。
- 当前分支是 `main`，已有提交记录，保留当前分支。
- `origin` 已指向 `https://github.com/wenhuorongbing-netizen/BookOS.git`。
- 工作树包含多轮 Product Slimming、Demo Workspace、Use Case Progress、E2E、文档更新等本地修改。
- 未跟踪但应迁移的文件包括 `docs/codex-handoff.md`、`docs/product-slimming-0.2-report.md`、`docs/usability-scorecard.md`、`backend/src/main/java/com/bookos/backend/usecase/dto/UseCaseStepVerificationResponse.java`。
- `.env.example`、`backend/.env.example`、`frontend/.env.example` 是模板文件，已检查为空 secret / mock local values；真实 `.env`、`.env.*`、私钥、日志、缓存、归档文件必须被 `.gitignore` 排除。

## 1. 项目目标和当前状态

BookOS 的产品目标是把“阅读”转化为可追溯的知识、行动和项目设计决策。核心链路是：

1. 添加书籍。
2. 设置阅读状态。
3. 快速捕获阅读想法。
4. 解析 emoji、页码、标签和 `[[Concept]]`。
5. 将捕获内容处理为 Note、Quote、Action、Concept。
6. 通过 Source Link 回到来源。
7. 进入 Review、Project、Forum、Graph、Export、Draft Assistant 等后续工作流。

当前工程底座已经很强，包含后端、前端、Flyway 迁移、E2E、Docker、CI、发布文档、安全文档和 Product Slimming 文档。当前阶段已经从“功能是否存在”转向“新用户是否能在 15 分钟内完成第一个有价值闭环”。

当前 PO 建议：

- Continue controlled beta with caveats。
- Pause broad feature expansion。
- Start Workflow Hardening 0.3。
- 不要继续扩功能，优先降低认知负担、压缩入口、加强可用性路径。

当前验证结果：

- Backend tests：PASS，71 tests。
- Frontend `npm ci`：PASS。
- Frontend typecheck：PASS。
- Frontend production build：PASS。
- Full Playwright E2E：PASS，31 tests。
- Product Slimming usability E2E：PASS，8 tests。
- Docker local compose config：PASS。
- Full-stack compose config：必须提供 `JWT_SECRET`；使用本地临时 secret 做 config 验证时 PASS。

## 2. 已完成的改动

### 产品与 UX 方向

- 建立 Product Slimming 0.2 方向：减少默认入口，围绕 first-day loop、Reader、Note-Taker、Game Designer 优先。
- 明确 Advanced Mode 不应主导默认体验。
- 明确 Graph、Import/Export、Draft Assistant、Analytics、Ontology Import 应作为 Advanced / data-dependent surface。
- 建立 First Valuable Loop：Add Book -> Capture Thought -> Process Capture -> Open Source -> Choose Next Path。
- 建立 mode-aware Dashboard：Reader、Note-Taker、Game Designer、Researcher、Community、Advanced。
- Dashboard 经过简化：每个模式上方只展示三个主行动。
- 导航已渐进披露：Primary、Secondary、Advanced / More。
- 术语已做 UI 简化：例如 Capture Inbox -> Process Captures，Action Items -> Actions，Mastery -> Learning Progress，AI Suggestions -> Draft Assistant。

### 用例、引导与学习

- 已有 onboarding。
- 已有 Use Cases 页面。
- 已有 executable use-case checklist 和 progress tracking。
- 已有 First Valuable Loop route。
- 已有 Demo Workspace，且 E2E 覆盖 start/reset/delete。
- 已有 Quick Capture guide、parser preview、one-click examples。
- 已有 Project Apply Knowledge Wizard，并做过 transaction safety 方向的强化。
- 已有 help/glossary/contextual empty state 相关文档和 UI。

### 后端能力

后端已覆盖大量模块，包括但不限于：

- Auth / user profile。
- Books / user library。
- Notes / note blocks。
- Captures / parser / conversion。
- Quotes / Actions。
- Concepts / Design Knowledge。
- Source references / entity links / backlinks。
- Daily。
- Forum。
- Search。
- Knowledge Graph。
- Mock AI / optional OpenAI-compatible provider safety。
- Game Projects / project applications / design decisions / playtests。
- Reading analytics / review / learning progress。
- Import/export。
- Demo workspace。
- Use-case progress。

### 前端能力

前端已覆盖大量页面，包括但不限于：

- Dashboard。
- Library。
- Book Detail。
- Process Captures。
- Notes。
- Quotes。
- Actions。
- Concepts。
- Design Knowledge。
- Daily。
- Projects。
- Project Detail。
- Project Apply Knowledge Wizard。
- Review。
- Learning Progress。
- Import/Export。
- Knowledge Graph。
- Forum。
- Demo Workspace。
- Use Cases。
- Help。
- Onboarding。

### 文档

关键文档已经建立或更新：

- `docs/current-state.md`
- `docs/product-slimming-baseline.md`
- `docs/product-slimming-0.2-report.md`
- `docs/product-slimming-0.2-roadmap.md`
- `docs/po-decision-report.md`
- `docs/first-15-minutes.md`
- `docs/manual-release-qa.md`
- `docs/usability-scorecard.md`
- `docs/demo-workspace.md`
- `docs/hands-on-use-cases.md`
- `docs/help-system.md`
- `docs/terminology-guide.md`
- `docs/security-authorization-matrix.md`
- `docs/api-endpoint-inventory.md`
- `docs/data-model-overview.md`
- `docs/deployment-runbook.md`
- `docs/backup-restore.md`

### E2E / 测试

- `npm run e2e` 当前通过，31 tests。
- `npm run e2e:usability` 当前通过，8 tests。
- E2E 包含 onboarding、dashboard、demo、guided first loop、navigation progressive disclosure、project wizard、core loop、use cases 和 first-15-minutes modes。

## 3. 关键设计决策及原因

### 决策 1：停止扩功能，进入 Product Slimming / Workflow Hardening

原因：BookOS 功能已经很多，新用户主要问题不再是“功能不够”，而是“不知道先做什么”。继续扩功能会加重认知负担。

### 决策 2：First Valuable Loop 是第一优先级

原因：PO 关心的是新用户能否在 15 分钟内完成第一个有价值闭环。当前最小闭环是 add book、capture、process、open source。

### 决策 3：Reader、Note-Taker、Game Designer 是最强 first-day modes

原因：E2E 和 heuristic score 显示这三个模式路径最清晰。Researcher、Community、Advanced 虽然可用，但需要更多术语教育和真实用户验证。

### 决策 4：Graph / Import / Draft Assistant 归入 Advanced

原因：这些功能强大但容易压倒新用户。只有用户选择 Advanced Mode 或已有足够 source-backed data 时才适合突出展示。

### 决策 5：所有 derived objects 必须保留 Source Link

原因：BookOS 的核心价值是 source-backed knowledge。Quote、Action、Concept、Project Application、Forum discussion、Daily item、AI draft 等都必须能追溯来源。

### 决策 6：未知页码必须保持 null

原因：不能伪造页码。没有真实来源页码时显示 page unknown。

### 决策 7：AI 只能是 Draft Assistant

原因：外部 AI 可选，MockAIProvider 默认，AI 不得自动覆盖用户内容。所有 AI 输出都必须是草稿，用户必须 accept/edit/reject。

### 决策 8：Demo Workspace 必须安全隔离

原因：用户需要练习空间，但 demo 数据不能污染真实 analytics、不能混入他人数据、不能用版权文本或假页码。

## 4. 尚未完成的任务

高优先级：

- Workflow Hardening 0.3：压缩 first-choice actions，减少每页并列 CTA。
- Advanced Mode containment：Graph、Import/Export、Draft Assistant 默认继续折叠。
- Researcher Mode hardening：降低 `[[Concept Name]]` 学习门槛，强化 concept review 成功反馈。
- Community Mode hardening：让 source-linked discussion 成为默认路径，弱化 generic forum dump。
- Review / Learning Progress unification：普通用户不应需要理解过多学习系统术语。
- Project Mode compression：把多个 project submodules 继续压缩成更少的明确行动。
- 开始真实 human usability sessions，不要把内部 heuristic score 当成外部用户研究。

中优先级：

- 给 forum thread cards 增加 source-context badges。
- 给 concept review 后增加清晰 success summary。
- 给 researcher 入口增加 inline concept examples。
- 给 advanced collapsed sections 增加“为什么隐藏”的说明。
- 增加人工浏览器截图证据。

低优先级：

- 清理旧术语在技术组件名、测试名、chunk 名中的残留；这些不应影响用户界面。
- 继续减少 docs/report 中 Windows PowerShell 显示 UTF-8 字符时的 mojibake 风险。

## 5. 当前问题、风险、坑点

### 工作树风险

当前工作树很脏。存在大量修改文件和未跟踪文件。不要直接 `git reset --hard`，不要直接覆盖。下一台机器必须先做 diff review。

当前未跟踪文件包括：

- `backend/src/main/java/com/bookos/backend/usecase/dto/UseCaseStepVerificationResponse.java`
- `docs/product-slimming-0.2-report.md`
- `docs/usability-scorecard.md`

### 文档显示编码风险

PowerShell `Get-Content` 读取部分 Markdown 时可能显示 `—` 为 mojibake，例如 `鈥?`。Git diff 中看起来仍是正确 UTF-8。下一台机器如果要修复，应统一确认编码，不要盲目重写 report。

### Product readiness 风险

- Product Slimming score：82/100。
- First 15 Minutes readiness：84/100。
- 这些分数是内部 QA / heuristic，不是外部用户研究。
- P0：当前未发现。
- P1：Researcher、Community、Advanced 仍不够 first-day friendly。

### E2E 风险

- Full E2E 大约两分钟，本地可跑，CI 是否默认启用需要看预算。
- 之前 E2E 曾因旧文案断言失败，不是产品 runtime failure。后续改 UI 文案时要同步 E2E labels。

### Docker 风险

- `docker compose -f docker-compose.full.yml config` 不提供 `JWT_SECRET` 会失败，这是预期行为。
- 用本地临时 `JWT_SECRET` 验证 config 即可，不要提交 secret。

### Demo 风险

- Demo 数据必须清楚标记。
- Demo 数据默认不应污染真实 analytics。
- Demo reset/delete 只能影响当前用户 demo records。

### Source / page 风险

- 不要发明页码。
- Unknown page 必须保持 null 或显示 page unknown。
- 所有 source-backed object 要保留 Source Link。

### AI 风险

- 不要调用真实外部 AI。
- 不要把 OpenAI-compatible provider 变成必需。
- MockAIProvider 是本地默认。
- AI 只允许生成 draft，不得自动覆盖 notes、quotes、actions、concepts、projects、forum content。

## 6. 下一台机器继续时应该先检查哪些文件

先检查仓库状态：

- `git status -sb`
- `git rev-parse HEAD`
- `git rev-parse origin/main`
- `git diff --stat`
- `git diff --name-only`

优先检查这些文档：

- `report.md`
- `docs/current-state.md`
- `docs/product-slimming-0.2-report.md`
- `docs/product-slimming-0.2-roadmap.md`
- `docs/po-decision-report.md`
- `docs/first-15-minutes.md`
- `docs/manual-release-qa.md`
- `docs/usability-scorecard.md`
- `docs/product-slimming-baseline.md`
- `docs/demo-workspace.md`
- `docs/terminology-guide.md`

优先检查这些前端文件：

- `frontend/package.json`
- `frontend/src/views/DashboardView.vue`
- `frontend/src/views/GuidedFirstLoopView.vue`
- `frontend/src/views/DemoWorkspaceView.vue`
- `frontend/src/views/UseCasesView.vue`
- `frontend/src/views/CaptureInboxView.vue`
- `frontend/src/views/BookDetailView.vue`
- `frontend/src/views/ProjectDetailView.vue`
- `frontend/src/views/ForumView.vue`
- `frontend/src/views/ConceptsView.vue`
- `frontend/src/views/ConceptDetailView.vue`
- `frontend/src/views/ReviewView.vue`
- `frontend/src/views/GraphView.vue`
- `frontend/src/components/use-case/UseCaseStepList.vue`
- `frontend/src/data/useCases.ts`
- `frontend/src/data/helpTopics.ts`
- `frontend/e2e/usability-first-15-minutes.spec.ts`
- `frontend/e2e/demo-workspace.spec.ts`
- `frontend/e2e/use-cases.spec.ts`
- `frontend/e2e/mvp-core-loop.spec.ts`

优先检查这些后端文件：

- `backend/src/main/java/com/bookos/backend/usecase/service/UseCaseProgressService.java`
- `backend/src/main/java/com/bookos/backend/usecase/dto/UseCaseProgressResponse.java`
- `backend/src/main/java/com/bookos/backend/usecase/dto/UseCaseStepVerificationResponse.java`
- `backend/src/main/java/com/bookos/backend/demo/service/DemoWorkspaceService.java`
- `backend/src/main/java/com/bookos/backend/search/service/SearchService.java`
- `backend/src/main/java/com/bookos/backend/graph/service/GraphService.java`
- `backend/src/main/java/com/bookos/backend/project/repository/DesignDecisionRepository.java`
- `backend/src/test/java/com/bookos/backend/usecase/UseCaseProgressIntegrationTest.java`
- `backend/src/test/java/com/bookos/backend/demo/DemoWorkspaceIntegrationTest.java`

## 7. 推荐下一步命令

在下一台机器上先同步和确认：

```powershell
cd "D:\【指挥中心】\节操都市\项目\BookOS"
git fetch origin main
git branch --show-current
git rev-parse HEAD
git rev-parse origin/main
git status -sb
git diff --stat
git diff --name-only
```

确认文档和 handoff：

```powershell
Get-Content docs\codex-handoff.md
Get-Content -Tail 160 report.md
Get-Content docs\product-slimming-0.2-report.md
Get-Content docs\po-decision-report.md
```

跑核心验证：

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm ci
npm run typecheck
npm run build
npm run e2e
npm run e2e:usability
```

验证 Docker config：

```powershell
cd "D:\【指挥中心】\节操都市\项目\BookOS"
docker compose config
$env:JWT_SECRET='bookos-local-config-check-secret-bookos-local-config-check-secret-1234'
docker compose -f docker-compose.full.yml config
Remove-Item Env:\JWT_SECRET
```

检查 Markdown whitespace：

```powershell
git diff --check
```

下一轮推荐任务：

```text
Start Workflow Hardening 0.3. Do not add new modules. Focus on Advanced Mode containment, Researcher path hardening, Community source-linked discussion hardening, Review/Learning Progress language unification, and Project Mode action compression.
```

## 8. 之前明确要求过的约束

这些约束在后续 Codex 对话中继续有效，除非用户明确改写：

- 不要使用 cached summaries。
- 不要使用任何 `.7z` archive。
- 使用 current GitHub main / current workspace 作为事实来源，先验证 branch、SHA、tree。
- 不要删除 `report.md`。
- 不要覆盖 `report.md`；只能 append。
- 不要提交 secrets。
- 不要提交 generated artifacts，例如 logs、target、dist、node_modules、archives。
- 不要创建 release tag，除非用户明确要求。
- 不要添加外部 AI provider，除非任务明确要求。
- 不要调用真实外部 AI。
- MockAIProvider 必须能在无 key 情况下工作。
- AI 输出必须是 draft-only。
- AI 不得自动覆盖用户内容。
- 不要使用 fake production data。
- 不要用假数据冒充真实用户数据。
- Demo data 必须清楚标记、可 reset/delete、不能污染真实 analytics 默认结果。
- 不要使用 copyrighted book passages。
- 不要 scrape books。
- 不要伪造页码。
- Unknown page 必须是 null 或显示 page unknown。
- Source Link / Source Reference 必须保留。
- 所有 user-owned private data 必须 user-scoped。
- Search、Graph、Source、Import/Export、Demo、AI suggestions 都不能跨用户泄漏。
- Admin-only 功能不能只靠前端隐藏，后端必须鉴权。
- Markdown 渲染必须 sanitize，不能执行 unsafe HTML/scripts。
- 不要删除现有 routes，不要破坏 deep links。
- 不要移除现有模块；通过 progressive disclosure 降低认知负担。
- 不要把 unfinished feature 伪装成 complete。
- 不要声称 human user research 已完成，除非真的进行了外部用户访谈/测试。
- 当前 Product Slimming 阶段不要继续扩功能，除非是修 P0 usability blocker。
- 所有文档和报告要诚实标注 PASS、PARTIAL、FAIL、MISSING、NOT VERIFIED。
- 如果 endpoint 或 UI 只是 placeholder，不要算完成。
- 如果 static UI 没有实际 API/state/persistence，不要算 working functionality。
- Windows 下不要使用 destructive git/file commands；不要 `git reset --hard` 或 `git checkout --`，除非用户明确批准。

## 9. 当前交接结论

BookOS 当前不是“缺功能”的阶段，而是“需要把强功能变成低认知负担产品”的阶段。下一步应围绕 Workflow Hardening 0.3 做收敛：

- 默认只展示用户当下最可能需要的三件事。
- 把高级功能继续藏到数据足够或用户主动选择 Advanced 后。
- 让 Reader / Note-Taker / Game Designer 成为稳定 beta 入口。
- 让 Researcher / Community 从 PARTIAL 变为 first-day usable。
- 通过真实 human usability sessions 验证，不再只依赖内部 heuristic 和 E2E。
