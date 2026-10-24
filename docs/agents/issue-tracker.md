# Issue tracker: Jira (simulated in `jira/ORD-board.md`)

Issues live in `jira/ORD-board.md`, one `### ORD-NN` section per issue, grouped under their epic. In a real team this is Jira, reached through the Atlassian MCP server or the Jira CLI; here it is one markdown file so the demo runs offline. The project skill `jira-board` reads and updates it.

## Conventions

- **Fetch a ticket**: read its `### ORD-NN` section. The user passes the key, e.g. `ORD-12`.
- **Publish tickets**: append new `### ORD-NN` sections under the right epic, using the next free key and the same fields as the existing stories, with `Status: Draft`. A human moves them to `Ready for Agent`.
- **Specs**: files in `docs/specs/<EPIC-KEY>-<slug>.md`, linked from the epic.
- **Triage state**: the `Status` field, not labels. `Ready for Agent` = ready-for-agent, `Ready for Human` = ready-for-human, `Refinement` = needs-triage (see `triage-labels.md` for the role names).
- **Blocking**: the `Blocked by` field. A ticket is unblocked when every ticket it lists is `Done`.
- **Comment**: append to the ticket's `Comments` list as `- YYYY-MM-DD <author>: <text>`.
- **Technical tasks**: tick `T1`, `T2`... as they go green; propose changes in Comments, never silently skip or add tasks.
- **Close**: set `Status: In Review` when the PR is open; a human sets `Done` after merge. Agents never push or merge.

## When a skill says "publish to the issue tracker"

Append `Status: Draft` ticket sections to `jira/ORD-board.md` under the epic.

## When a skill says "fetch the relevant ticket"

Read the `### ORD-NN` section of `jira/ORD-board.md`.
