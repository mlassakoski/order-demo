# Jira: ORD (Order Service)

Simulated Jira board for the demo. In a real team this lives in Jira and agents reach it through the Atlassian MCP server or the Jira CLI; here it is one markdown file so the demo runs offline. **This file is the issue tracker**: there is no other copy of the tickets.

## Board conventions

| Field | Values |
|---|---|
| Issue types | Epic · Story · Task · Spike |
| Workflow | `Backlog` → `Refinement` → `Ready for Agent` / `Ready for Human` → `In Progress` → `In Review` → `Done` (also `Draft` for tickets generated but not yet approved) |
| Assignee | `Claude (agent)` or a person. Agents only take `Ready for Agent` tickets. |
| Labels | `ai-agent`, `human`, `refinement`, `demo`; at review: `ai-first-pass` · `ai-small-fixes` · `ai-reworked` |
| Points | Fibonacci (1, 2, 3, 5, 8). Anything above 5 gets split. |
| Branch / commit / PR | `feature/ORD-NN-slug` · `feat(ORD-NN): summary` (Conventional Commits) · PR title = commit summary |
| Ready / Done | Definition of Ready and Definition of Done: `CONTRIBUTING.md` |
| Technical tasks | Each story lists its **technical tasks** (T1, T2...): the agent's implementation plan, proposed in refinement and approved by the engineer. One task = one red→green cycle at a named seam. **T1 is the acceptance test** (`*IT`): written first, red until the slice is wired, green when the last layer task lands. The agent ticks them as it goes and may propose changes in Comments; it does not silently skip or add tasks. |

## How agents use this board

Paste this paragraph when `/setup-matt-pocock-skills` asks for the issue tracker (choose **Other**):

> Issues live in `jira/ORD-board.md`, one `### ORD-NN` section per issue, grouped under their epic. To **fetch** a ticket, read its section (the user passes the key, e.g. `ORD-12`). To **publish** tickets, append new `### ORD-NN` sections under the right epic using the next free key and the same fields as the existing stories, with `Status: Draft`; a human moves them to `Ready for Agent`. Specs are files in `docs/specs/<EPIC-KEY>-<slug>.md`, linked from the epic. The triage role is the `Status` field (`Ready for Agent` = ready-for-agent, `Ready for Human` = ready-for-human, `Refinement` = needs-triage). Comments go in a `Comments` list at the end of the ticket.

Starting a ticket (agent or human): `git switch -c feature/ORD-NN-slug` → `/clear` → `/implement ORD-NN` → set `Status: In Review`, open PR.

## Sprint plan

| Sprint | Goal | Tickets |
|---|---|---|
| Sprint 1 (prep week) | Working harness and the place/view feature, built only by the agent | ORD-2 to ORD-6, ORD-11 to ORD-15 |
| Sprint 2 (demo) | Cancel orders built live, measured against a vanilla run | ORD-21 to ORD-24, ORD-31 to ORD-34 |

---

## ORD-1 · Epic: Engineering foundation and AI harness

Status: Done · Owner: Mauricio · Spec: `docs/specs/ORD-1-engineering-foundation.md`

Repository, build, quality gates and the AI harness (guides + sensors) that every feature ticket relies on.

### ORD-2 · Task: Bootstrap repository skeleton

- **Status:** Done
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio
- **Labels:** ai-agent · **Points:** 3 · **Sprint:** 1
- **Blocked by:** none

**Description**
Create the project skeleton with no feature code: Spring Boot 4.1 / Java 21 / Maven, hexagonal packages with `package-info.java`, ArchUnit rules, quality gates (Enforcer, Spotless, Surefire/Failsafe, JaCoCo), Actuator, CI, Dependabot, CODEOWNERS, agent files (`AGENTS.md`, `CODING_STANDARDS.md`, `GLOSSARY.md`, hooks, settings).

**Acceptance criteria**
- [x] Packages `domain`, `application.port.in|out`, `application.service`, `adapter.in.web`, `adapter.out.persistence` exist and document their rules
- [x] ArchUnit rules cover layers, naming, Spring placement, setters, generic exceptions, test names
- [x] Agent harness: short `AGENTS.md` + `CLAUDE.md`, `CODING_STANDARDS.md`, path-scoped rules (`.claude/rules/`), project skill `jira-board`, subagent `security-reviewer`, Stop hook, protect hook, permissions, `.mcp.json` (Context7), plugins `mattpocock-skills` + `jdtls-lsp`
- [x] CI: `ci.yml` (required check) and `claude.yml` (AI PR review and `@claude` assist; removed in ORD-5)
- [x] Governance: CODEOWNERS, CONTRIBUTING, PR template with first-pass acceptance

**Comments**
- 2026-10-22 Claude: generated from the talk plan; not built yet (no Maven Central access in the generating environment). Build check moved to ORD-3.

### ORD-3 · Task: Install the harness and get the first green build

- **Status:** Done
- **Assignee:** Mauricio · **Labels:** human · **Points:** 2 · **Sprint:** 1
- **Blocked by:** ORD-2

**Description**
Install the harness on the dev machine and prove the skeleton builds. If the build fails, hand the error to the agent as a bug fix (paste the log, ask for the root cause, never weaken a gate).

**Acceptance criteria**
- [x] `bash scripts/setup-harness.sh` moved `harness-setup/` into `.claude/` and `.github/`
- [x] `git init`, first commit `chore(ORD-3): skeleton and AI harness` on `main`
- [x] `./mvnw verify` green on JDK 21 (enforcer, spotless, ArchUnit, `ApplicationHealthIT`, JaCoCo)
- [x] `./mvnw spotless:check` green after the first build

**How to run with AI (if the build fails)**
> The build fails with this error: [paste]. Find the root cause and fix it. Don't skip or weaken any check. Run `./mvnw verify` and show me the output.

**Comments**
- 2026-10-24 Claude: `./mvnw verify` and `spotless:check` green on JDK 21.0.1 and 25.0.2. Fixes: Palantir Java Format pinned to 2.102.0 and sources reformatted (no rule changed); `.mvn/jvm.config` flag `--sun-misc-unsafe-memory-access=allow` crashed Maven on JDK 21 ("Unrecognized option"), now prefixed with `-XX:+IgnoreUnrecognizedVMOptions` so 21 ignores it and 25 keeps the warning silenced. Open: the repo's first commit is `Skeleton + AI harness` on `master`, not `chore(ORD-3): ...` on `main`; renaming the branch (and rewording that commit, if wanted) is left to Mauricio.
- 2026-10-24 Claude: `main` now exists on GitHub (renamed from `master`; first commit kept its original message `Skeleton + AI harness`). All criteria met; Done.

### ORD-4 · Task: Install Matt Pocock's skills and configure them for this repo

- **Status:** Done
- **Assignee:** Mauricio · **Labels:** human, ai-agent · **Points:** 1 · **Sprint:** 1
- **Blocked by:** ORD-3

**Description**
Install the `mattpocock-skills` plugin the way Matt recommends for Claude Code and run his one-time repo setup, pointing the tracker at this board.

**Acceptance criteria**
- [x] `scripts/install-matt-pocock-skills.sh` ran (plugins `mattpocock-skills`, `jdtls-lsp`; `brew install jdtls`); `/` in Claude Code lists `grill-with-docs`, `to-spec`, `to-tickets`, `implement`, `tdd`, `code-review`, `retro`, `jira-board`
- [x] `context7` MCP server approved; `/mcp` shows it connected
- [x] `/setup-matt-pocock-skills` done: tracker **Other** (paragraph from "How agents use this board"), default triage labels, single-context docs
- [x] `CLAUDE.md` has the `## Agent skills` block and `docs/agents/*.md` exist; committed as `chore(ORD-4): configure agent skills`

**Comments**
- 2026-10-24 Claude: plugins `mattpocock-skills` 1.3.1 and `jdtls-lsp` were already installed; `brew install jdtls` added jdtls 1.63.0. All listed skills ship in the plugin (`grill-with-docs`, `to-spec`, `to-tickets`, `retro` and `setup-matt-pocock-skills` are user-invoked only, so agents don't see them in their own skill list). `context7` tools are live in the session. `/setup-matt-pocock-skills` can't be run by the agent (`disable-model-invocation`), so its output was written by hand from the skill's templates with the answers above: `docs/agents/issue-tracker.md` (from the board paragraph, plus blocking/close rules), `triage-labels.md` (defaults), `domain.md` (single-context), `## Agent skills` block in `CLAUDE.md`. `needs-info` and `wontfix` have no board status yet. Branch `feature/ORD-4-configure-agent-skills`.
- 2026-10-24 Claude: merged to `main` (PR #7 history). Done.

### ORD-5 · Task: Create the GitHub repository and protect main

- **Status:** Done
- **Assignee:** Mauricio · **Labels:** human · **Points:** 2 · **Sprint:** 1
- **Blocked by:** ORD-3

**Description**
Agents never push or change repository settings, so this is human work.

**Acceptance criteria**
- [x] Repo created, `main` pushed, CI run green
- [x] Branch protection on `main`: PR required, 1 code-owner approval, required check `ci / verify`, linear history, no force push
- [x] `@your-org/...` handles in `.github/CODEOWNERS` replaced with real teams
- [x] Dependabot enabled
- ~~Repository secret `ANTHROPIC_API_KEY` added, so the `claude` workflow reviews PRs~~ Dropped 2026-10-25 (Mauricio): no AI review in CI, `claude.yml` removed

**Comments**
- 2026-10-24 Claude: repo `mlassakoski/order-demo` exists and `main` is pushed (PR #7 merged); CODEOWNERS uses `@mlassakoski`. Not verifiable by the agent: CI run green, branch protection, Dependabot, `ANTHROPIC_API_KEY` secret. Mauricio: tick those and set Done.
- 2026-10-24 Claude: checked read-only via the GitHub API. Public repo `mlassakoski/order-demo`, default branch `main`; `ci` green on push to `main` and on PR #7; Dependabot opened PRs #1–#6. **Not met: branch protection.** `main` reports `protected: false` and no rulesets, so PRs, code-owner approval, the `ci / verify` check, linear history and force-push blocking are all unenforced. Left for Mauricio: Settings → Branches (or Rulesets) → protect `main` as in the criterion, then set Done.
- 2026-10-24 Claude: ruleset on `main` verified via the API: pull request with 1 code-owner approval, required check `verify`, linear history, no force push (`non_fast_forward`), no deletion. Done.

### ORD-6 · Spike: Verify the harness behaves as designed

- **Status:** Done
- **Assignee:** Mauricio (pairing with Claude) · **Labels:** human · **Points:** 2 (timebox 2h) · **Sprint:** 1
- **Blocked by:** ORD-4

**Description**
Before relying on the harness in the demo, prove each guide and sensor actually works. Findings go in Comments; anything broken becomes a Task.

Run the **concept tour** in `docs/demo/RUNBOOK.md`: one check per concept.

**Acceptance criteria**
- [x] `/memory` (or `/context`) shows `CLAUDE.md` → `AGENTS.md` loaded; `.claude/rules/domain.md` loads only after a domain file is opened
- [x] `/agents` lists `security-reviewer`; `/mcp` shows `context7`; `/plugin` shows `mattpocock-skills` and `jdtls-lsp`; `/hooks` shows PreToolUse and Stop
- [x] Asking the agent to edit `ArchitectureTest` is blocked by the PreToolUse hook
- [x] A deliberately failing test makes the Stop hook send the agent back to fix it (and it gives up after 3 attempts)
- [x] `git push` from the agent is denied
- [x] Plugin skill names noted (short `/grill-with-docs` or namespaced `/mattpocock-skills:grill-with-docs`)
- [x] `scripts/make-vanilla.sh` copy shows no Matt skills (plugin disabled per project)

**Comments**
- 2026-10-24 Claude: concept tour run from the agent side on branch `feature/ORD-6-verify-harness` (on top of ORD-4). Evidence per check:
  1. Memory: `CLAUDE.md` → `AGENTS.md` in the agent's context. Path rules: `domain.md` loaded only when `domain/package-info.java` was read, `tests.md` only when a test was read.
  2. Slash UIs not visible to the agent, so checked the same things another way: `security-reviewer` offered as a subagent type; `context7` tools answered a query; `claude plugin list` shows both plugins enabled; both hooks fired (below). Mauricio: glance at `/agents`, `/mcp`, `/plugin`, `/hooks` once.
  3. Edit to `ArchitectureTest` blocked: "Blocked by hook: this file is owned by the team".
  4. Stop hook script run 3× against a failing `StopHookProbeTest`: attempts 1 and 2 exit 2 with the failure, attempt 3 "Build still failing after 3 attempts" exit 0, counter reset. Real wiring checked by ending a turn with the probe in place (see next comment).
  5. `git push origin HEAD` denied by permissions.
  6. Skills show namespaced: `/mattpocock-skills:implement-spec`, `/mattpocock-skills:implement` (README already says both forms work).
  7. Vanilla copy (built in a temp dir, not `../order-demo-vanilla`, which stays for ORD-31): no harness files, plugin disabled; `claude -p` there lists no Matt skills.
  Extra: LSP (jdtls) answers `workspaceSymbol OrderDemoApplication`. context7 docs name the module `spring-boot-webmvc-test` but not the package; package `org.springframework.boot.webmvc.test.autoconfigure` confirmed from the jar. CI (#13) not verifiable: ORD-5 skipped, no remote.
  Findings, most severe first: (1) vanilla copy still contains `scripts/` (`install-matt-pocock-skills.sh`, `score-run.sh`, `setup-harness.sh`), which tips off the vanilla agent: ORD-7. (2) Stop hook only checks uncommitted `src`/`pom.xml` changes, so a failing build already committed passes silently: acceptable for the demo, noted only.
- 2026-10-24 Claude: real Stop hook wiring confirmed: ending a turn with `StopHookProbeTest` failing returned "./mvnw verify failed (attempt 1 of 3)" with the assertion; probe deleted, build green. Also gitignored jdtls workspace files (`.classpath`, `.project`, `.settings/`) that the LSP plugin creates in the repo root.
- 2026-10-24 Claude: merged to `main` (PR #7 history). Done; finding (1) became ORD-7.

### ORD-7 · Task: Keep harness scripts out of the vanilla copy

- **Status:** Done
- **Assignee:** Claude (agent) · **Labels:** ai-agent, demo · **Points:** 1 · **Sprint:** 2
- **Blocked by:** none

**Description**
Found in ORD-6: `scripts/make-vanilla.sh` copies `scripts/` into the vanilla repo, so the Run A agent can see `install-matt-pocock-skills.sh`, `setup-harness.sh` and `score-run.sh`. That leaks harness hints and skews the comparison.

**Acceptance criteria**
- [x] `make-vanilla.sh` excludes `scripts/` (and `.vscode/`)
- [x] A fresh vanilla copy has no file mentioning Matt Pocock, the harness or scoring; `./mvnw verify` still green there

**Comments**
- 2026-10-24 Claude: excludes `scripts/`, `.vscode/` and jdtls files (`.classpath`, `.project`, `.settings/`); strips the `.claude/.verify-*` and `score.log` entries from the copy's `.gitignore`. Checked by building a fresh copy: grep for `pocock|harness|score|ORD-NN|first-pass|claude` over its tracked files finds nothing (except `.claude/settings.json`, which must name the plugin to disable it); `./mvnw verify` green there. Left as is: the ArchUnit dependency in `pom.xml` (unused once the architecture tests are removed; a weak hint, removing it changes the build).
- 2026-10-24 Claude: merged to `main` (PR #7 history). Done.

### ORD-8 · Task: Agents push feature branches and open PRs

- **Status:** In Review
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio · **Labels:** ai-agent · **Points:** 1 · **Sprint:** 2
- **Blocked by:** ORD-5

**Description**
Mauricio's decision (2026-10-24): agents push their feature branch and open the PR; humans review and approve on GitHub. Safe now that `main` is protected (ORD-5: PR, code-owner approval, `verify`, no force push). Agents still never merge, push to `main` or force-push.

**Acceptance criteria**
- [x] `.claude/settings.json`: allow pushing `feature/*` and `gh pr create|view|edit`; deny force-push, pushes to `main` and `gh pr merge` (protected file: patch applied by Mauricio)
- [x] `AGENTS.md`, `CONTRIBUTING.md`, `README.md`, `docs/agents/issue-tracker.md` say the same
- [x] Checked: agent pushes a feature branch and opens a PR; `git push origin main` and `gh pr merge` are denied

**Comments**
- 2026-10-24 Claude: settings patch applied by Mauricio (bda160a). Checked: `git push origin main` and `gh pr merge` denied by permissions; feature branch pushed and PR opened by the agent. In Review.
- 2026-10-24 Claude: automated security review of bda160a: (High) wildcard allow `git push origin feature/*` also matched `--mirror`, `--delete`, refspecs like `x:refs/heads/main`; (Medium) `gh pr edit *` let the agent change the base or set its own first-pass labels. Follow-up patch: allow only `git push [-u] origin HEAD`; deny force, mirror, all, delete, prune, `+`/`:` refspecs, `main`/`master`, `gh pr edit` of base or labels; `gh pr edit` no longer allowed. Server-side ruleset on `main` stays the real control. Back to In Progress until the patch is applied.
- 2026-10-24 Claude: tightening patch applied by Mauricio (96f9b2f). Checked denied: `git push --mirror`, `git push origin HEAD:refs/heads/main` (dry runs). Pushed with `git push origin HEAD` to update PR #8. In Review.

---

## ORD-10 · Epic: Place and view orders

Status: Done · Owner: Mauricio · Spec: `docs/specs/ORD-10-place-and-view-orders.md`

Customers can place an order with one or more order lines and look it up later. First feature: sets the patterns (aggregate, ports, error model, tests) for everything after it.

### ORD-11 · Task: Refinement: place and view orders

- **Status:** Done
- **Assignee:** Mauricio (with Claude) · **Labels:** refinement · **Points:** 2 · **Sprint:** 1
- **Blocked by:** none

**Description**
Refinement session with the agent: `/grill-with-docs` → `/to-spec` → `/to-tickets`, in one context window. Human makes the decisions; agent records glossary terms and drafts spec and stories; human approves the breakdown.

**Acceptance criteria**
- [x] Spec `docs/specs/ORD-10-place-and-view-orders.md`
- [x] `GLOSSARY.md` has Order, Order line, Customer, Product and the lifecycle terms
- [x] Stories ORD-12, ORD-13, ORD-14 as vertical slices with blocking edges, approved and moved to `Ready for Agent`

### ORD-12 · Story: Place an order

- **Status:** Done
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio
- **Labels:** ai-agent · **Points:** 5 · **Sprint:** 1
- **Blocked by:** ORD-3, ORD-4

**Story**
As a customer, I want to place an order with one or more products, so that I can buy them.

**What to build**
A customer sends a customer identifier and one or more order lines and gets back a new Pending order with a service-generated identifier and its location. Cuts through every layer: the Order aggregate's `place`, the place-order use case, in-memory storage, `POST /orders`.

**Acceptance criteria**
- [x] Given a customer and one order line, when the order is placed, then the response is `201` with `Location: /orders/{id}` and the order in state Pending
- [x] Given several order lines, when the order is placed, then all lines are kept in order
- [x] Two placed orders never share an identifier; the client never sends one
- [x] The placed order is stored and can be found again through the storage port
- [x] Acceptance test (`*IT`) for the happy path through the running application

**Execution**
`git switch -c feature/ORD-12-place-an-order` → `/clear` → `/implement ORD-12`. Seams: as in `CODING_STANDARDS.md` (aggregate, use case with fake port, web slice, one IT).

**Technical tasks**
- [x] **T1 Domain: the Order aggregate can be placed.** Value objects `OrderId` (with `newId()`), `CustomerId`, `ProductId`, `OrderLine`, enum `OrderStatus` (Pending, Paid, Shipped, Cancelled); `Order.place(customerId, lines)` and `Order.rehydrate(...)`. Seam: aggregate. Done when: a placed order is Pending, has an id, keeps lines in order as an unmodifiable copy; two placements get different ids.
- [x] **T2 Application: place-order use case.** `PlaceOrderUseCase` + `PlaceOrderCommand`, output port `OrderRepositoryPort` (`save`, `findById`), `PlaceOrderService`. Seam: use case with an in-memory fake port. Done when: placing saves the order and returns it.
- [x] **T3 Persistence: in-memory adapter.** `OrderInMemoryAdapter` implements the port and stores/returns copies via `rehydrate`. Seam: the port. Done when: save then find returns an equal order that is a different instance.
- [x] **T4 Web: `POST /orders`.** `OrderController`, `PlaceOrderRequest`, `OrderLineRequest`, `OrderResponse`. Seam: `@WebMvcTest` with the use case mocked. Done when: 201, `Location: /orders/{id}`, body with id, customer, lines, state Pending.
- [x] **T5 Acceptance: `PlaceOrderIT`.** Seam: running application (`@SpringBootTest` + MockMvc). Done when: the happy path passes end to end.
- [x] **T6 Close.** `./mvnw verify` green (Stop hook), `/code-review main` Standards + Spec clean, commit `feat(ORD-12): place an order`, status `In Review`, PR with the first-pass box ticked by the reviewer.

**Comments**
- 2026-10-24 Claude: T1–T5 built test-first on `feature/ORD-12-place-an-order` (8a4177d), merged into `feature/ORD-10-place-and-view-orders`; `./mvnw verify` green. T5 `PlaceOrderIT` was green on first run (layers already wired by T1–T4). It checks storage through `OrderRepositoryPort` since `GET` arrives in ORD-14. JSON contract: `{id, customerId, lines:[{productId, quantity}], status:"PENDING"}`, `Location: /orders/{id}` (relative). Value objects only reject null for now; blank/quantity/empty rules come in ORD-13. T6 runs at epic close.
- 2026-10-24 Claude: epic close. Code review since c5fa7c9 (Standards + Spec) and `security-reviewer` (first FAIL: unbounded input, raw id echoed) addressed in 89fe242 `fix(ORD-10): address review findings` with Mauricio's decisions: ≤100 lines, ids ≤64 chars, quantity 1–1000, order id is a UUID (malformed → 404 without echo), fixed English messages, `rehydrate` keeps invariants. Re-review: PASS. Open, Low: Spring's default 404 for unmapped paths (`/orders/a/b`) echoes the path; in-memory store has no cap (placeholder adapter). `./mvnw verify` green on `feature/ORD-10-place-and-view-orders`. No PR: no remote yet (ORD-5), nothing pushed.
- 2026-10-24 Claude: merged to `main` in PR #7 (65d3f62); status Done.

### ORD-13 · Story: Refuse invalid orders

- **Status:** Done
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio
- **Labels:** ai-agent · **Points:** 3 · **Sprint:** 1
- **Blocked by:** ORD-12

**Story**
As a customer, I want invalid orders to be refused with a clear explanation, so that I never create a broken order by mistake.

**What to build**
Invalid orders are refused with a `400` problem detail and nothing is stored. Introduces the domain validation error type and the single exception handler.

**Acceptance criteria**
- [x] No order lines → `400` problem detail, nothing stored
- [x] An order line with quantity 0 or negative → `400` problem detail, nothing stored
- [x] Missing or blank customer identifier → `400` problem detail
- [x] Missing or blank product identifier → `400` problem detail
- [x] The same rules hold when the domain is used directly (aggregate tests), not only over HTTP

**Execution**
`git switch -c feature/ORD-13-refuse-invalid-orders` → `/clear` → `/implement ORD-13`.

**Technical tasks**
- [x] **T1 Domain: invariants.** Abstract `DomainValidationException`; `EmptyOrderException`, `InvalidQuantityException`, `InvalidIdentifierException`. Seam: aggregate and value objects. Done when: each rule has a failing-then-passing test (no lines, quantity 0 and -1, blank customer id, blank product id).
- [x] **T2 Web: request validation.** Bean Validation on `PlaceOrderRequest` / `OrderLineRequest` (`@NotBlank`, `@NotEmpty`, `@Valid`, `@NotNull`, `@Min(1)`). Seam: `@WebMvcTest`. Done when: each invalid body returns 400 and the use case is never called.
- [x] **T3 Web: error model.** `GlobalExceptionHandler` (`@RestControllerAdvice` extending `ResponseEntityExceptionHandler`) maps `DomainValidationException` → 400 `ProblemDetail` with a readable `detail`. Only the 400 mapping now: 404 comes with ORD-14, 409 with ORD-23. Seam: `@WebMvcTest` with the use case throwing. Done when: a domain validation error returns a 400 problem detail.
- [x] **T4 Acceptance: `RefuseInvalidOrderIT`.** Done when: an order with no lines returns 400 problem detail and the storage port holds no order.
- [x] **T5 Close.** `./mvnw verify` green, `/code-review main` clean, commit `feat(ORD-13): refuse invalid orders`, PR.

**Comments**
- 2026-10-24 Claude: T1–T4 built test-first on `feature/ORD-13-refuse-invalid-orders` (ffbfd72, merged with ORD-14 in d5c1b93); `./mvnw verify` green. `RefuseInvalidOrderIT` green on first run; checks "nothing stored" with a spy on `OrderRepositoryPort.save`. Choices: `OrderLineRequest.quantity` is `Integer` so `@NotNull` catches a missing quantity; blank/null ids throw `InvalidIdentifierException`; empty-lines rule sits in the `Order` constructor so `rehydrate` enforces it too; bean-validation 400 `detail` lists failing fields sorted by name (message wording follows the JVM locale, tests assert field names). T5 runs at epic close.
- 2026-10-24 Claude: epic close. Code review since c5fa7c9 (Standards + Spec) and `security-reviewer` (first FAIL: unbounded input, raw id echoed) addressed in 89fe242 `fix(ORD-10): address review findings` with Mauricio's decisions: ≤100 lines, ids ≤64 chars, quantity 1–1000, order id is a UUID (malformed → 404 without echo), fixed English messages, `rehydrate` keeps invariants. Re-review: PASS. Open, Low: Spring's default 404 for unmapped paths (`/orders/a/b`) echoes the path; in-memory store has no cap (placeholder adapter). `./mvnw verify` green on `feature/ORD-10-place-and-view-orders`. No PR: no remote yet (ORD-5), nothing pushed.
- 2026-10-24 Claude: merged to `main` in PR #7 (65d3f62); status Done.

### ORD-14 · Story: View an order

- **Status:** Done
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio
- **Labels:** ai-agent · **Points:** 2 · **Sprint:** 1
- **Blocked by:** ORD-12

**Story**
As a customer, I want to see an order by its identifier, so that I can check its state and lines.

**What to build**
`GET /orders/{id}` returns the order; an unknown identifier gets a `404` problem detail. Adds the get-order use case and the not-found error type.

**Acceptance criteria**
- [x] Given a placed order, when it is fetched, then the response is `200` with id, customer, order lines and state
- [x] Given an unknown identifier, when it is fetched, then the response is `404` problem detail
- [x] Reading returns a separate instance from the stored one (storage behaves like a database)

**Execution**
`git switch -c feature/ORD-14-view-an-order` → `/clear` → `/implement ORD-14`. Can run in parallel with ORD-13 (separate worktree).

**Technical tasks**
- [x] **T1 Application: get-order use case.** `GetOrderUseCase` + `GetOrderQuery`, `GetOrderService`; abstract `NotFoundException` and `OrderNotFoundException` (message includes the id). Seam: use case with in-memory fake port. Done when: a stored order is returned; an unknown id throws `OrderNotFoundException`.
- [x] **T2 Web: `GET /orders/{id}`.** New controller method returning `OrderResponse`; `GlobalExceptionHandler` maps `NotFoundException` → 404 `ProblemDetail`. Seam: `@WebMvcTest`. Done when: 200 with body for a known id, 404 problem detail for an unknown one.
- [x] **T3 Acceptance: `ViewOrderIT`.** Done when: an order placed with `POST /orders` is returned by `GET` on its `Location`.
- [x] **T4 Close.** `./mvnw verify` green, `/code-review main` clean, commit `feat(ORD-14): view an order`, PR.

**Comments**
- 2026-10-24 Claude: T1–T3 built test-first on `feature/ORD-14-view-an-order` (51f6c7d), merged into `feature/ORD-10-place-and-view-orders`; `./mvnw verify` green. `NotFoundException` / `OrderNotFoundException` live in `application.port.in` (part of the use case contract; the web adapter may only see input ports and domain). `ViewOrderIT` green on first run. "Separate instance" criterion covered by `OrderInMemoryAdapterTest`. Created `GlobalExceptionHandler` with the 404 mapping; ORD-13 merges its 400 mapping in. T4 runs at epic close.
- 2026-10-24 Claude: epic close. Code review since c5fa7c9 (Standards + Spec) and `security-reviewer` (first FAIL: unbounded input, raw id echoed) addressed in 89fe242 `fix(ORD-10): address review findings` with Mauricio's decisions: ≤100 lines, ids ≤64 chars, quantity 1–1000, order id is a UUID (malformed → 404 without echo), fixed English messages, `rehydrate` keeps invariants. Re-review: PASS. Open, Low: Spring's default 404 for unmapped paths (`/orders/a/b`) echoes the path; in-memory store has no cap (placeholder adapter). `./mvnw verify` green on `feature/ORD-10-place-and-view-orders`. No PR: no remote yet (ORD-5), nothing pushed.

Note: if ORD-13 and ORD-14 run in parallel, both touch `GlobalExceptionHandler`; the second PR rebases and keeps both mappings.
- 2026-10-24 Claude: merged to `main` in PR #7 (65d3f62); status Done.

### ORD-15 · Task: Retro: place and view orders

- **Status:** Done
- **Assignee:** Mauricio (with Claude) · **Labels:** human · **Points:** 1 · **Sprint:** 1
- **Blocked by:** ORD-12, ORD-13, ORD-14

**Description**
Run `/retro` on the epic's sessions. Mechanical mistakes become checks (ArchUnit rule, hook, CI); judgement calls become `CODING_STANDARDS.md` entries; each change is its own Task in this epic.

**Acceptance criteria**
- [x] First-pass acceptance recorded for ORD-12, ORD-13, ORD-14 (labels on the PRs)
- [x] Retro findings listed in Comments, most severe first
- [x] Follow-up Tasks created for accepted findings

**Comments**
- 2026-10-24 Claude: `/retro` run on the ORD-3 → ORD-10 session; 9 findings proposed (unbounded request input → ArchUnit rule; `requireNonNull` in domain → ArchUnit rule; Stop hook skips subagents → `SubagentStop` hook; ITs never red → acceptance IT first; rtk breaks git in worktrees; jdtls diagnostics noise; `/code-review main` pointer (fixed by `main`); one board writer in parallel runs; `tests.md` vs "nothing stored"). Waiting for Mauricio to accept findings; then follow-up Tasks. First-pass labels on PR #7: ORD-12/13/14 all `ai-small-fixes` (one review-fix round).
- 2026-10-24 Claude: Mauricio accepted findings 1, 3, 4 → Draft ORD-16, ORD-17, ORD-18; rejected 5, 6, 8, 9; 7 fixed by renaming to `main`. Left: first-pass labels on PR #7 (all three stories `ai-small-fixes`): needs Mauricio, the agent has no GitHub write access.
- 2026-10-24 Claude: label `ai-small-fixes` created and applied to PR #7 (carries ORD-12, ORD-13, ORD-14). All criteria met; Done.

### ORD-16 · Task: Every request input has a limit

- **Status:** In Review
- **Assignee:** Mauricio (architecture tests are team-owned) · **Labels:** human · **Points:** 2 · **Sprint:** 2
- **Blocked by:** none

**Description**
From the ORD-15 retro: ORD-12/13/14 shipped request records with no size limits; only `security-reviewer` caught it, at epic close, and the limits had to be decided late. Mechanical part becomes a check, the decision moves to refinement.

**Acceptance criteria**
- [x] ArchUnit rule: every `String` or collection field of a `..adapter.in.web..*Request` record carries `@Size(max = ...)`, every numeric field `@Max`; passes on current code
- [x] `CODING_STANDARDS.md` judgement line: every public input has a business limit, named in the spec (refinement asks for it)

**Comments**
- 2026-10-24 Claude: rule written as new `architecture/RequestLimitsTest.java` (3 rules: strings and collections need `@Size`, numbers need `@Max`, on fields of `*Request` classes). Built and run in a scratch worktree, since agents can't edit `architecture/`: green on current code; with the three limits removed, all three rules fail naming `PlaceOrderRequest.customerId`, `PlaceOrderRequest.lines`, `OrderLineRequest.quantity`. Patch handed to Mauricio to review and apply. `CODING_STANDARDS.md` line added on `feature/ORD-16-request-input-limits`.
- 2026-10-24 Claude: patch applied by Mauricio and committed; `./mvnw verify` green with `RequestLimitsTest` (3 rules). In Review.

### ORD-17 · Task: Verify the build when a subagent stops

- **Status:** In Review
- **Assignee:** Mauricio (`.claude/settings.json` and hooks are protected) · **Labels:** human · **Points:** 1 · **Sprint:** 2
- **Blocked by:** none

**Description**
From the ORD-15 retro: the Stop hook only fires for the main session and runs `./mvnw verify` in the main repo. Implementer subagents (`SubagentStop`, working in `.claude/worktrees/*`) were verified only because their prompts said so.

**Acceptance criteria**
- [x] `SubagentStop` hook runs `verify-on-stop.sh` in the subagent's own working directory, same 3-attempt rule
- [x] `.claude/worktrees/` in `.gitignore`
- [x] ~~Checked as in ORD-6: a subagent leaving a failing test is sent back~~ changed (Mauricio, option 1): the hook detects a red worktree but cannot hold a background subagent; the orchestrator checks the worktree before merging (`docs/agents/issue-tracker.md`)

**Comments**
- 2026-10-24 Claude: `verify-on-stop.sh` now reads `cwd` from the hook input and runs verify in that repo (subagent worktree or project); a `SubagentStop` in the project dir itself is skipped (the main Stop hook covers it); attempts counter also resets when nothing changed (it went stale before). Settings add `SubagentStop`. Simulated inputs in scratch worktrees: failing test in a fake subagent worktree → exit 2 naming the test, counter kept in that worktree; subagent in project dir → exit 0; main Stop, clean → exit 0; fixed → exit 0 and counter reset. Patch handed to Mauricio (hooks and settings are protected). Needs `jq`; without it the hook falls back to the project dir. Left: live check with a real subagent once applied.
- 2026-10-24 Claude: patch applied by Mauricio, committed 6c554ad; `./mvnw verify` green. Live check with a background worktree subagent that planted a failing test: the hook fired in the subagent's worktree, verify failed on the probe, counter = 1, exit 2. But the subagent was **not** sent back: its result was returned (~23s) before the build finished. So the hook detects but does not yet block background subagents. Open: rerun the check with a foreground subagent, or treat the hook as a detector and have the orchestrator read `.claude/.verify-attempts` in the worktree before merging. Also seen: the worktree was created from `origin/main`, not the current branch, so implementer prompts must keep the "confirm base, reset if not" step.
- 2026-10-24 Claude: automated security review of 6c554ad (Medium): the hook trusted `cwd` from its input, so it could run `./mvnw` from any directory. Follow-up patch: build only in the project or one of `git worktree list` entries (resolved with `pwd -P`), else exit 0. Tested: an outside repo with a planted `mvnw` is never run (SubagentStop and Stop); registered worktree with failing test → exit 2; project dir cases unchanged. Patch handed to Mauricio.
- 2026-10-24 Claude: Mauricio chose option 1 (hook as detector + orchestrator check before merge); rule added to `docs/agents/issue-tracker.md`. Waiting for Mauricio to apply the directory-restriction patch, then In Review.
- 2026-10-24 Claude: directory-restriction patch applied by Mauricio and committed; `./mvnw verify` green. In Review.

### ORD-18 · Task: Acceptance test first in technical tasks

- **Status:** In Review
- **Assignee:** Claude (agent) · **Labels:** ai-agent · **Points:** 1 · **Sprint:** 2
- **Blocked by:** none

**Description**
From the ORD-15 retro: `PlaceOrderIT`, `ViewOrderIT` and `RefuseInvalidOrderIT` were the last technical task and passed on their first run, so TDD never saw them red. Writing the acceptance test first (outside-in) keeps it red until the last layer lands.

**Acceptance criteria**
- [x] Board conventions ("Technical tasks" row) and `CONTRIBUTING.md` Definition of Ready say: T1 is the story's acceptance test (`*IT`), red until the slice is wired; layer tasks follow
- [x] ORD-22 and ORD-23 drafts reordered to that shape

**Comments**
- 2026-10-24 Claude: rule added to the board conventions and `CONTRIBUTING.md` Definition of Ready; ORD-22 and ORD-23 drafts now start with their `*IT` (renumbered, close task last). Branch `feature/ORD-18-acceptance-test-first`.

---

## ORD-20 · Epic: Cancel orders (live demo)

Status: Refinement · Owner: Mauricio · Spec: created in ORD-21 as `docs/specs/ORD-20-cancel-orders.md` (expected result: `docs/demo/reference/ORD-20-cancel-orders.md`)

Customers can cancel an order until it ships. Built live in the talk; measured against a vanilla run.

### ORD-21 · Task: Refinement: cancel orders

- **Status:** Ready for Human
- **Assignee:** Mauricio (with Claude) · **Labels:** refinement, demo · **Points:** 2 · **Sprint:** 2
- **Blocked by:** ORD-14

**Description**
`/grill-with-docs` with the request below, then `/to-spec` and `/to-tickets` in the same session. Recorded for the talk.

> Customers want to cancel an order. Only PENDING or PAID orders can be cancelled. Cancelling a SHIPPED order must fail. Expose it as POST /orders/{id}/cancel.

Decisions to expect: cancelling twice (error or success?), refunds for Paid orders, cancellation reason, HTTP status for refusals.

**Acceptance criteria**
- [ ] Spec `docs/specs/ORD-20-cancel-orders.md` (reference: `docs/demo/reference/ORD-20-cancel-orders.md`)
- [ ] Glossary updated if new terms appear; ADR only if a decision is hard to reverse
- [ ] Stories ORD-22 and ORD-23 generated (shape below), reviewed and moved to `Ready for Agent`

### ORD-22 · Story: Cancel a Pending or Paid order

- **Status:** Draft (expected output of ORD-21; regenerated live)
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio
- **Labels:** ai-agent, demo · **Points:** 3 · **Sprint:** 2
- **Blocked by:** ORD-21

**Story**
As a customer, I want to cancel my order before it ships, so that I don't pay for something I no longer want.

**What to build**
Cancelling a Pending or Paid order returns the updated order as Cancelled. Cuts through every layer: `Order.cancel`, cancel-order use case, `POST /orders/{id}/cancel`.

**Acceptance criteria**
- [ ] Given a Pending order, when it is cancelled, then the response is `200` with state Cancelled and the stored order is Cancelled
- [ ] Given a Paid order, when it is cancelled, then the response is `200` with state Cancelled
- [ ] Given an unknown identifier, when it is cancelled, then the response is `404` problem detail
- [ ] Acceptance test (`*IT`) for the happy path

**Execution (live)**
`git switch -c feature/ORD-22-cancel-order` → `/clear` → `/implement ORD-22`.

**Technical tasks**
- [ ] **T1 Acceptance: `CancelOrderIT`.** Red until T2–T4 land. Done when: place → cancel → `GET` shows Cancelled.
- [ ] **T2 Domain: `Order.cancel()`.** Allowed from Pending and Paid; sets Cancelled. Seam: aggregate (states set up with `Order.rehydrate`). Done when: Pending → Cancelled and Paid → Cancelled.
- [ ] **T3 Application: cancel-order use case.** `CancelOrderUseCase` + `CancelOrderCommand`, `CancelOrderService`: load → `cancel()` → save; unknown id → `OrderNotFoundException`. Seam: use case with in-memory fake port. Done when: the stored order is Cancelled after the call.
- [ ] **T4 Web: `POST /orders/{id}/cancel`.** Returns 200 with `OrderResponse`. Seam: `@WebMvcTest`. Done when: 200 body with state Cancelled; 404 for unknown id (existing mapping).
- [ ] **T5 Close.** `./mvnw verify` green, `/code-review main` clean, commit `feat(ORD-22): cancel an order`, PR.

### ORD-23 · Story: Refuse to cancel Shipped or Cancelled orders

- **Status:** Draft (expected output of ORD-21; regenerated live)
- **Assignee:** Claude (agent) · **Reviewer:** Mauricio
- **Labels:** ai-agent, demo · **Points:** 2 · **Sprint:** 2
- **Blocked by:** ORD-22

**Story**
As a customer, I want a clear refusal when my order can no longer be cancelled, so that I understand it is too late.

**What to build**
Cancelling a Shipped or already Cancelled order is refused with `409` and the order is unchanged. Introduces `OrderCannotBeCancelledException` (a domain rule violation).

**Acceptance criteria**
- [ ] Given a Shipped order, when it is cancelled, then the response is `409` problem detail and it is still Shipped
- [ ] Given a Cancelled order, when it is cancelled, then the response is `409` problem detail and it is still Cancelled
- [ ] The problem detail says why and includes the current state

**Technical tasks**
- [ ] **T1 Acceptance: `RefuseCancellationIT`.** Seed a Shipped order through `OrderRepositoryPort` (no ship API yet), then cancel. Red until T2–T3 land. Done when: 409, and `GET` still shows Shipped.
- [ ] **T2 Domain: cancellation rule.** Abstract `DomainRuleViolationException`; `OrderCannotBeCancelledException` carrying the order id and current state; `cancel()` throws it from Shipped and Cancelled and leaves the state unchanged. Seam: aggregate. Done when: both refusals tested, state asserted unchanged.
- [ ] **T3 Web: 409 mapping.** `GlobalExceptionHandler` maps `DomainRuleViolationException` → 409 `ProblemDetail` with a `currentState` property. Seam: `@WebMvcTest`. Done when: 409 problem detail with `currentState`.
- [ ] **T4 Close.** `./mvnw verify` green, `/code-review main` clean, commit `feat(ORD-23): refuse to cancel shipped or cancelled orders`, PR.

**Execution (live)**
`git switch -c feature/ORD-23-refuse-cancellation` → `/clear` → `/implement ORD-23`.

### ORD-24 · Task: Retro: cancel orders

- **Status:** Backlog
- **Assignee:** Mauricio (with Claude) · **Labels:** human, demo · **Points:** 1 · **Sprint:** 2
- **Blocked by:** ORD-22, ORD-23

**Acceptance criteria**
- [ ] `/retro` run in the implementation session; findings in Comments
- [ ] First-pass acceptance recorded for ORD-22 and ORD-23 and compared with the vanilla run (ORD-32)

---

## ORD-30 · Epic: Demo enablement

Status: Backlog · Owner: Mauricio

Everything needed to present the comparison reliably. Not product work, tracked here so it is visible and estimated.

### ORD-31 · Task: Create the vanilla baseline

- **Status:** Backlog
- **Assignee:** Mauricio · **Labels:** human, demo · **Points:** 1 · **Sprint:** 2
- **Blocked by:** ORD-14

**Acceptance criteria**
- [ ] `scripts/make-vanilla.sh` created `../order-demo-vanilla` from `main` after ORD-12 to ORD-14 merged
- [ ] No harness files, no architecture tests, plugin disabled; `./mvnw verify` green there

### ORD-32 · Task: Rehearse both runs and fill the scorecard

- **Status:** Backlog
- **Assignee:** Mauricio · **Labels:** human, demo · **Points:** 3 · **Sprint:** 2
- **Blocked by:** ORD-21, ORD-31

**Acceptance criteria**
- [ ] 3 rehearsals each of Run A (vanilla prompt) and Run B (ORD-22 and ORD-23 via `/implement`)
- [ ] `scripts/score-run.sh` results, time and `/cost` recorded in `docs/demo/RUNBOOK.md` scorecard
- [ ] Slides 18 and 19 updated with the real numbers

### ORD-33 · Task: Record backup videos

- **Status:** Backlog
- **Assignee:** Mauricio · **Labels:** human, demo · **Points:** 1 · **Sprint:** 2
- **Blocked by:** ORD-32

**Acceptance criteria**
- [ ] Recordings: grilling + spec + tickets (ORD-21), Run A, Run B implementation, both scorings

### ORD-34 · Task: Dry run with a colleague

- **Status:** Backlog
- **Assignee:** Mauricio · **Labels:** human, demo · **Points:** 1 · **Sprint:** 2
- **Blocked by:** ORD-33

**Acceptance criteria**
- [ ] Full talk in under 45 minutes, demo included
- [ ] Feedback captured and applied to `presentation-draft.md`
