#!/usr/bin/env bash
# PreToolUse hook (feedback sensor / enforcement).
# Blocks agent edits to files the team owns: architecture rules and the harness itself
# (settings, hooks, rules, subagents, project skills, MCP config).
# Exit code 2 = block the tool call; stderr is shown to the agent.

input="$(cat)"

protected='(src/test/java/[^"]*/architecture/|archunit\.properties|\.claude/(settings\.json|hooks/|rules/|agents/|skills/)|\.mcp\.json)'

if printf '%s' "$input" | grep -Eq "\"file_path\"[[:space:]]*:[[:space:]]*\"[^\"]*${protected}"; then
  cat >&2 <<'MSG'
Blocked by hook: this file is owned by the team (architecture rules or harness config).
Change the production code so it satisfies the rule.
If you believe the rule itself is wrong, stop and explain why to the human.
MSG
  exit 2
fi

exit 0
