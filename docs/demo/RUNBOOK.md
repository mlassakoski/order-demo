# Demo runbook

Same task, two setups, one judge.

| Run | Folder | Setup |
|---|---|---|
| A. Vanilla | `../order-demo-vanilla` | Code only. No AGENTS.md, standards, glossary, hooks, board or specs; Matt's plugin disabled. |
| B. Harnessed | `order-demo` | Matt's skills + guides + sensors. |
| Judge | `scripts/score-run.sh <dir>` | Same ArchUnit rules + test conventions on both. |

## Prep

1. Setup from `README.md` (harness, git, `./mvnw verify`, plugin, `/setup-matt-pocock-skills`). Commit.
2. Build the baseline from the board, one ticket per fresh context (ORD-12, then ORD-13 and ORD-14):
   ```
   git switch -c feature/ORD-12-place-an-order
   /implement ORD-12          # agent works T1..T6, ticks them, commits feat(ORD-12): ...
   # review the diff, tick first-pass acceptance, merge to main
   /clear
   git switch main && git switch -c feature/ORD-13-refuse-invalid-orders
   /implement ORD-13
   /clear
   git switch main && git switch -c feature/ORD-14-view-an-order
   /implement ORD-14
   /retro                     # ORD-15
   ```
   Update ticket status on the board as you go (`In Progress` → `In Review` → `Done`). This is your first rehearsal: note time, tokens (`/cost`), what `/code-review` and `/retro` found.
3. `scripts/make-vanilla.sh` → `../order-demo-vanilla` from the baseline, no harness, plugin disabled.
   Check there with `/` that no Matt skills are listed.
4. Rehearse both runs 3 times. Record the best ones as backup. Fill the scorecard.
   Both runs start from the **baseline** `c199963` (ORD-12 to ORD-14, no cancel code): `main` already contains the cancel feature since PR #12.
   - Run A: work in a copy of `../order-demo-vanilla` (`cp -R ../order-demo-vanilla /tmp/run-a`), never in `make-vanilla.sh` output from today's `main`.
   - Run B: `git switch -c rehearsal/run-b c199963`, then restore today's harness on it (`git checkout main -- .claude CODING_STANDARDS.md CONTRIBUTING.md docs/agents scripts src/test/java/com/demo/order/architecture`). Keep the baseline's `jira/` so ORD-22 and ORD-23 are not Done yet.

## Concept tour (5 min, proves every AI concept works)

Run in Claude Code inside `order-demo` after setup (also ticket ORD-6). Each line: concept → how to show it → what you should see.

| # | Concept | Show it | Expected |
|---|---|---|---|
| 1 | Project memory (`AGENTS.md` via `CLAUDE.md`) | `/memory` or `/context` | `CLAUDE.md` and imported `AGENTS.md` loaded; small token cost |
| 2 | Path-scoped rules | Ask: "open the domain package-info and tell me which rules apply" | `.claude/rules/domain.md` gets loaded only now |
| 3 | Plugins / skills | Type `/` | `grill-with-docs`, `to-spec`, `to-tickets`, `implement`, `tdd`, `code-review`, `retro`, `jira-board` |
| 4 | Project skill | "Show me ORD-12" | Agent uses `jira-board`, states key + summary, lists technical tasks |
| 5 | Subagent | `/agents` | `security-reviewer` listed (read-only tools) |
| 6 | MCP | `/mcp`, then "Using context7, how do I import @WebMvcTest in Spring Boot 4?" | `context7` connected; answer cites `org.springframework.boot.webmvc.test.autoconfigure` |
| 7 | Code intelligence (LSP) | "Find all references to OrderDemoApplication" | Answer via the Java language server, not grep |
| 8 | Permissions | "Push this branch" | Denied by `settings.json` |
| 9 | PreToolUse hook (enforcement) | "Relax the setter rule in ArchitectureTest" | Blocked: "owned by the team" |
| 10 | Stop hook (feedback loop) | Ask for a test that fails on purpose, then "done" | Hook runs `./mvnw verify`, sends the failure back, agent fixes it |
| 11 | Architecture as code | `./mvnw verify` | ArchUnit, Spotless, JaCoCo, `ApplicationHealthIT` all green |
| 12 | Spec-driven tickets | Open `jira/ORD-board.md` + `docs/specs/` | Epics → spec → stories with Given/When/Then + technical tasks |
| 13 | CI gate | Open a PR on GitHub | `ci / verify` runs as the required check |
| 14 | Metric | PR template | First-pass acceptance box |

## Demo prompt

> Implement order cancellation. Only PENDING or PAID orders can be cancelled. Cancelling a SHIPPED order must fail. Expose it as POST /orders/{id}/cancel and add tests.

**Run A (vanilla):** paste the prompt. Then `scripts/score-run.sh ../order-demo-vanilla`.

**Run B (harnessed):** refinement ORD-21, then stories ORD-22 and ORD-23.
1. `/grill-with-docs` + the prompt → answer the rounds (pre-recorded, ~3 min). Good live question: *"Is cancelling twice an error or a success?"*
2. `/to-spec` → `docs/specs/ORD-20-cancel-orders.md`
3. `/to-tickets` → ORD-22, ORD-23 on `jira/ORD-board.md` with technical tasks; approve, set `Ready for Agent`
4. `git switch -c feature/ORD-22-cancel-order`, `/clear`, `/implement ORD-22` live. `/tdd` asks to confirm seams: accept the defaults from `CODING_STANDARDS.md`.
5. Stop hook runs `./mvnw verify`; `/code-review main` runs Standards + Spec in parallel subagents.
6. `scripts/score-run.sh .`

Short on time: `cp docs/demo/reference/ORD-20-cancel-orders.md docs/specs/`, set ORD-22 and ORD-23 to `Ready for Agent` on the board (they're already there as Draft) and start at step 4.

## Scorecard

Agent rehearsal, 2026-10-25 (A: headless `claude -p` with the demo prompt in a copy of `../order-demo-vanilla`; B: the ORD-21 → ORD-23 build in this repo, PRs #11 and #12). Judge: `scripts/score-run.sh`.

| Check | A. Vanilla | B. Harnessed |
|---|---|---|
| `./mvnw verify` with judge rules | ❌ FAIL | ✅ PASS |
| ArchUnit / convention violations | 1 (`should_cancel_a_pending_order_and_refuse_cancelling_it_again` breaks `should_..._when_...`) | 0 |
| Rule inside aggregate (`order.cancel()`) | ✅ | ✅ |
| Specific domain exception, 409 / 404 | ⚠️ `OrderNotCancellableException extends RuntimeException`, mapped by its own handler; no domain-rule hierarchy | ✅ `OrderCannotBeCancelledException` under `DomainRuleViolationException`, generic 409 with `currentState` |
| Test names `should_..._when_...` | ❌ 1 violation | ✅ |
| `/code-review` Spec findings | not run (no spec) | 2 IT gaps + 1 design point, fixed in one agent round |
| Manual fixes needed | 1 (rename the test) to pass the judge | 0 |
| Time / tokens (`/cost`) | 229 s, 49 turns, $1.21 | ~14 min of agent time (2 implementers + 1 fixer + 4 reviewers), plus refinement |
| **Accepted on first pass?** | No: fails the judge | `ai-small-fixes`: one review-fix round, green throughout |

Notes: A was strong because the ORD-10 code gave it good patterns to copy; the harness made the difference on conventions, the error model and the acceptance tests (Shipped case end to end, written first). Human rehearsals: add columns or replace these numbers.

## Talking points
- Each harness file: **guide** or **sensor**.
- Grilling: decisions are the human's, facts are the agent's (Matt's `grilling` rule).
- Tickets are **vertical slices** (tracer bullets), not layers.
- Hook blocks an edit to `ArchitectureTest`: "a rule in a file is a request, a hook is enforcement".
- `/code-review` two axes: following the standards and building the right thing are different questions.
- `/retro`: mistakes become checks, judgement becomes standards. That's how first-pass acceptance improves over time.
