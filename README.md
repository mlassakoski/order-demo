# order-demo

A small order service built **AI-first** with [Matt Pocock's skills](https://github.com/mattpocock/skills): humans make the decisions, agents write the code, deterministic checks keep it inside our architecture.

Java 21 · Spring Boot 4.1 · Maven · DDD + hexagonal · ArchUnit · Spotless · JaCoCo · GitHub Actions · Claude Code

> Starting point: only the skeleton (packages, build, architecture rules) and the AI setup. All feature code is written by agents from the specs in `docs/specs/` and the tickets on the board `jira/ORD-board.md`.

## Setup (once)

```bash
# prerequisites: JDK 21, git, Claude Code (Maven comes with ./mvnw)
bash scripts/setup-harness.sh              # moves harness-setup/ into .claude/ and .github/
git init && git add -A && git commit -m "Skeleton + AI harness"
./mvnw verify                              # green on the empty skeleton
scripts/install-matt-pocock-skills.sh      # claude plugin install mattpocock-skills@claude-plugins-official
claude                                     # then, once: /setup-matt-pocock-skills
```

`/setup-matt-pocock-skills` answers for this repo: tracker **Other** (paste the paragraph from "How agents use this board" in `jira/ORD-board.md`), **default** triage labels, **single-context** domain docs. Commit what it writes (`CLAUDE.md` block, `docs/agents/`).

## How we build a feature (Matt's main flow)

| Step | Skill | Output |
|---|---|---|
| 1. Align | `/grill-with-docs` | Decisions; `GLOSSARY.md` and `docs/adr/` updated inline |
| 2. Spec | `/to-spec` | `docs/specs/ORD-NN-<slug>.md` |
| 3. Tickets | `/to-tickets` | Stories on `jira/ORD-board.md`: vertical slices, Given/When/Then, blocking edges, technical tasks |
| 4. Build | `git switch -c feature/ORD-NN-slug`, `/clear`, `/implement ORD-NN` (one per ticket) | Code via `/tdd` per technical task, `/code-review`, commit `feat(ORD-NN): ...` |
| 5. Improve | `/retro` | Changes to the environment: checks, standards, pointers |

Keep steps 1 to 3 in one context window; `/clear` before each `/implement`. Unsure which skill fits: `/ask-matt`.
Plugin skills may show namespaced in Claude Code (`/mattpocock-skills:grill-with-docs`); both forms work the same.

## Guides, sensors and the metric

| File | Kind | Role |
|---|---|---|
| `AGENTS.md` / `CLAUDE.md` | Guide | Short: commands + pointers only (always loaded, so kept tiny). |
| `CODING_STANDARDS.md` | Guide | Cross-cutting rules (layers, errors → HTTP) + index of layer rules. Read by `/implement`, `/code-review`, `security-reviewer`. |
| `.claude/rules/*.md` | Guide | **Path-scoped project rules**: domain, application, web, persistence, tests. Loaded only when the agent touches matching files. |
| `.claude/skills/jira-board/` | Guide | **Project skill**: how to fetch a ticket, move status, tick technical tasks, comment with evidence. |
| `.mcp.json` | Guide | **MCP**: Context7 for up-to-date library docs (Spring Boot 4). |
| `jdtls-lsp` plugin | Guide | **Code intelligence**: Java language server (definitions, references, compile errors after edits). |
| `GLOSSARY.md`, `docs/adr/` | Guide | Ubiquitous language and decisions, kept up to date by `/grill-with-docs`. |
| `jira/ORD-board.md` | Guide | Simulated Jira board: epics, stories, technical tasks, status, assignee. |
| `docs/specs/` | Guide | One spec per epic (Matt's `/to-spec` format + delivery plan). |
| `mattpocock-skills` plugin | Guide + sensor | Grilling, spec, tickets, TDD, two-axis code review, retro. |
| `src/test/java/**/architecture/` | Sensor | ArchUnit: layers, naming, no setters, no generic exceptions, test names. |
| `.claude/hooks/verify-on-stop.sh` | Sensor | Stop hook: `./mvnw verify` when code changed; failures go back to the agent. |
| `.claude/hooks/protect-team-owned-files.sh` | Sensor | Blocks agent edits to architecture rules and harness config (settings, hooks, rules, agents, skills, MCP). |
| `.claude/agents/security-reviewer.md` | Sensor | **Subagent**: read-only security review of the diff in a fresh context. |
| `.claude/settings.json` | Sensor | **Permissions** (feature-branch push and PRs only, no merge, no secrets), hooks, enabled plugins and MCP servers. |
| `pom.xml` gates | Sensor | Enforcer (JDK/Maven), Spotless format, Surefire `*Test` + Failsafe `*IT`, JaCoCo ≥ 80% on domain/application. |
| `.github/workflows/ci.yml` | Sensor | Required check: `spotless:check` + `./mvnw verify`, reports on failure. |
| `.github/dependabot.yml` | Sensor | Weekly dependency and Actions updates. |
| `.github/CODEOWNERS` | Governance | Tech leads approve guardrails and agent config. |
| `CONTRIBUTING.md` | Governance | Roles, workflow, Definition of Ready / Done, conventions, branch protection. |
| `.github/pull_request_template.md` | Metric | First-pass acceptance per PR. |

## Epics and specs

| Feature | Where | Use |
|---|---|---|
| Engineering foundation (ORD-1) | `docs/specs/ORD-1-engineering-foundation.md` · ORD-2 to ORD-6 | Mostly human setup tasks; ORD-2 done |
| Place and view orders (ORD-10) | `docs/specs/ORD-10-place-and-view-orders.md` · ORD-12 to ORD-14 | Built by the agent during prep: the baseline |
| Cancel orders (ORD-20) | created live in ORD-21; reference `docs/demo/reference/ORD-20-cancel-orders.md` · ORD-22, ORD-23 | Live demo |

## Operations
- Health: `GET /actuator/health` (liveness/readiness probes enabled), `GET /actuator/info`.
- Logs: plain locally; JSON (ECS) with `--spring.profiles.active=prod`.
- Graceful shutdown enabled.

## Demo
`docs/demo/RUNBOOK.md`. How we work as a team: `CONTRIBUTING.md`.
