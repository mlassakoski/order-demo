# ORD-1: Engineering foundation and AI harness

| Epic | Status | Owner | Board |
|---|---|---|---|
| ORD-1 | In Progress | Mauricio | `jira/ORD-board.md` → ORD-2 to ORD-6 |

## Problem Statement

The team wants agents to write most of the code. Without a foundation, agent output is plausible but inconsistent: it ignores the architecture, drifts from the domain language, and lands in big PRs that need several review rounds. Reviewers become the only safety net, and first-pass acceptance stays low.

## Solution

A repository where the agent cannot easily do the wrong thing and immediately learns when it did. It starts with no feature code, only: a hexagonal skeleton; a short set of **guides** the agent reads (pointers, standards, glossary, decisions, tickets); **sensors** that check every change deterministically (architecture tests, build gates, hooks, CI); and **governance** that keeps humans in charge of decisions, merges and the guardrails themselves.

## User Stories

1. As an engineer, I want the build to fail when the architecture is broken, so that I don't have to catch layer violations in review.
2. As an engineer, I want formatting applied automatically, so that diffs only contain real changes.
3. As an engineer, I want a coverage gate on business code, so that untested rules don't reach `main`.
4. As an engineer, I want the agent's work checked before it says "done", so that I review finished work, not drafts.
5. As a tech lead, I want the architecture rules and agent configuration protected from agent edits, so that guardrails only change through a reviewed human PR.
6. As a tech lead, I want code owners on guardrail files, so that changes to them always need my approval.
7. As an engineer, I want the agent to read short pointers and load details only when needed, so that its context stays focused.
8. As an engineer, I want one shared domain vocabulary, so that code, tickets and conversations use the same words.
9. As a team, I want agents to commit on feature branches but never push or merge, so that a human is always the last gate.
10. As a team, I want each PR to record how much AI output we kept, so that we can measure first-pass acceptance.
11. As an operator, I want health endpoints and structured logs, so that the service is observable from day one.
12. As a team, I want dependency updates proposed automatically, so that we don't drift onto unsupported versions.

## Implementation Decisions

- **Stack:** Java 21, Spring Boot 4.1 (`spring-boot-starter-webmvc`, `-validation`, `-actuator`), Maven with wrapper. Boot 3.5 rejected: open-source support ended June 2026.
- **Architecture:** hexagonal with a DDD core, packages `domain`, `application.port.in|out`, `application.service`, `adapter.in.web`, `adapter.out.persistence`. See ADR 0001.
- **Sensors in the build:** ArchUnit (`onionArchitecture()` plus naming, Spring placement, no setters, no generic exceptions, test names) via `archunit-junit6` (required with JUnit 6); Maven Enforcer (JDK 21+, Maven 3.9+); Spotless with Palantir Java Format, applied on build and checked in CI; Surefire for `*Test`, Failsafe for `*IT`; JaCoCo line coverage ≥ 80% on `domain` and `application`.
- **Sensors around the agent:** Stop hook runs `./mvnw verify` when code changed and sends failures back (3 attempts); PreToolUse hook blocks edits to architecture tests, `archunit.properties`, `.claude/settings.json`, `.claude/hooks/`; permissions deny `git push`, hard resets and reading secrets.
- **Guides:** `AGENTS.md` with commands and pointers only; `CODING_STANDARDS.md` read by `/implement` and `/code-review`; `GLOSSARY.md` and `docs/adr/` maintained by `/grill-with-docs`; Matt Pocock's skills as the workflow (ADR 0002).
- **Tracker:** this repo's simulated Jira board (`jira/ORD-board.md`); in a team repo, Jira or GitHub Issues through MCP.
- **Governance:** CODEOWNERS (tech leads own guardrails), CONTRIBUTING (roles, Definition of Ready/Done), CI as required check, branch protection, Dependabot, PR template with first-pass acceptance.
- **Operations:** Actuator health with probes, graceful shutdown, ECS JSON logs in the `prod` profile.

## Testing Decisions

- The guardrails are tested by using them: `ArchitectureTest` and `TestConventionsTest` run on every build; `ApplicationHealthIT` proves the application context starts and sets the integration-test pattern.
- The harness itself is verified by the ORD-6 spike (hook blocks a protected edit, Stop hook loops on a failing test, push denied, plugin disabled in the vanilla copy).
- A good test checks behaviour through a public seam; the default seams per layer are defined in `CODING_STANDARDS.md`.

## Out of Scope

- A real database and migrations (in-memory adapter for now).
- Authentication and authorization.
- Containers, deployment pipeline, environments.
- Observability backend (metrics and tracing exporters).
- Claude running in CI (for example automated PR review); possible follow-up.

## Further Notes

The skeleton (ORD-2) was generated without access to Maven Central, so ORD-3 is the first real build. Treat any failure there as a normal bug ticket for the agent, with the rule that no gate may be weakened to make it pass.

## Delivery plan

| Key | Type | Summary | Owner | Blocked by |
|---|---|---|---|---|
| ORD-2 | Task | Bootstrap repository skeleton | Claude (agent) | none |
| ORD-3 | Task | Install the harness and get the first green build | Mauricio | ORD-2 |
| ORD-4 | Task | Install Matt Pocock's skills and configure them | Mauricio | ORD-3 |
| ORD-5 | Task | Create the GitHub repository and protect main | Mauricio | ORD-3 |
| ORD-6 | Spike | Verify the harness behaves as designed | Mauricio + Claude | ORD-4 |
