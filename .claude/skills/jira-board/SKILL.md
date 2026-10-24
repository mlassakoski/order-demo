---
name: jira-board
description: Read and update tickets on the ORD board (jira/ORD-board.md, our simulated Jira). Use whenever work mentions a ticket key like ORD-12: to fetch it, move its status, tick its technical tasks or add a comment.
---

# Jira board (simulated)

The board is `jira/ORD-board.md`. Each ticket is a `### ORD-NN · <Type>: <Summary>` section. In a real team these operations go through the Atlassian MCP server; the rules are the same.

## Fetch
Read the ticket's section and its epic's spec (`docs/specs/<EPIC>-*.md`). State the key and summary before starting.

## Status transitions (edit the `**Status:**` line)
| From | To | When |
|---|---|---|
| Ready for Agent | In Progress | You start the ticket (only if every "Blocked by" ticket is Done) |
| In Progress | In Review | All technical tasks ticked, `./mvnw verify` green, `/code-review` clean |
| any | Draft | Tickets you create with `/to-tickets` |

Never set `Ready for Agent` or `Done`: those are human decisions.

## While working
- Tick a technical task (`- [ ]` → `- [x]`) right after its test goes green.
- To change the plan (add, split or drop a task), add a comment and ask; don't edit the task list silently.

## Comments
Append to the ticket's `**Comments**` list (create it if missing), newest last:
`- YYYY-MM-DD Claude: <what happened> · evidence: <test command + result, or commit>`

Add one comment when moving to In Review, with the test summary and the commit hash.
