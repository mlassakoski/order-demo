# Contributing

How humans and AI agents work in this repo. Agents read `AGENTS.md`; this file is for people.

## Roles
| Who | Owns |
|---|---|
| Product / domain expert | The problem, decisions during grilling, acceptance of the spec |
| Engineer (human) | Ticket breakdown approval, seam approval, PR review and merge, guardrail changes |
| Agent | Exploration, spec and ticket drafts, code and tests, self-review (`/code-review`), commits on a feature branch |
| Tech lead | Architecture rules, `CODING_STANDARDS.md`, agent config (CODEOWNERS) |

Agents push feature branches and open PRs; they never merge, push to `main`, force-push or change guardrails (`.claude/settings.json` denies those; branch protection on `main` requires a PR with code-owner approval; a hook blocks edits to architecture rules and agent config; CODEOWNERS requires a tech lead).

## Workflow
1. `/grill-with-docs` with the domain expert until every decision is made (glossary and ADRs update inline).
2. `/to-spec` → spec in the tracker. `/to-tickets` → vertical-slice tickets with blocking edges. An engineer approves the breakdown.
3. Per ticket: `git switch -c feature/ORD-NN-slug`, `/clear`, `/implement ORD-NN`. The agent follows the ticket's technical tasks (T1, T2...) and ticks them.
4. Open a PR (the agent fills the body with `/pr`; you tick the first-pass acceptance box). A human reviews and merges.
5. After anything that went sideways: `/retro`, and turn its findings into checks or standards.

Tracker: `jira/ORD-board.md` simulates Jira for this demo (specs in `docs/specs/`). In a team repo, point `/setup-matt-pocock-skills` at Jira or GitHub Issues through MCP.

## Definition of Ready (ticket)
- Status `ready-for-agent`, written in `GLOSSARY.md` terms.
- Describes end-to-end behaviour (a vertical slice) with Given/When/Then acceptance criteria.
- Technical tasks listed: one red→green cycle per task at a named seam, approved by an engineer. T1 is the story's acceptance test (`*IT`), written first and red until the whole slice is wired; layer tasks follow, outside-in.
- Blocking edges listed; all blockers done.

## Definition of Done (PR)
- CI green: `spotless:check` and `./mvnw verify` (unit + integration tests, ArchUnit, coverage ≥ 80% on domain and application).
- `/code-review` Standards and Spec axes: no open findings. `security-reviewer` subagent: PASS.
- Every acceptance criterion of the ticket has a test.
- First-pass acceptance box ticked in the PR template.
- Approved by a code owner. Squash-merged.

## Agent setup (what a new engineer installs)
- JDK 21, git, Claude Code; `brew install jdtls` for Java code intelligence.
- `bash scripts/setup-harness.sh` once, then `scripts/install-matt-pocock-skills.sh` (plugins `mattpocock-skills`, `jdtls-lsp`).
- Approve the `context7` MCP server (`.mcp.json`) on first use.
- In a team repo also connect the Atlassian (Jira) and GitHub MCP servers instead of the simulated board.

## Conventions
- Branches: `feature/ORD-NN-slug`, `fix/ORD-NN-slug`.
- Commits: [Conventional Commits](https://www.conventionalcommits.org/) with the ticket key: `feat(ORD-22): cancel an order`, `fix(ORD-23): ...`.
- Formatting: Palantir Java Format via Spotless, applied on every build (`./mvnw spotless:apply`).

## Changing a guardrail
Architecture rules, coverage threshold, hooks and agent permissions change only through a human PR approved by a tech lead. If an agent keeps hitting the same rule, discuss whether the rule or the code is wrong, then record the outcome in `docs/adr/` or `CODING_STANDARDS.md`.

## Repository settings (GitHub)
- Branch protection on `main`: require PR, 1 code-owner approval, status check `ci / verify`, linear history, no force pushes.
- Dependabot enabled (`.github/dependabot.yml`).

## Metrics we track
- **First-pass acceptance** per PR (template labels).
- **DORA**: lead time for changes, deployment frequency, change failure rate, failed deployment recovery time, rework rate.
