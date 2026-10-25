#!/usr/bin/env bash
# PreToolUse hook (enforcement) for Bash.
# The only push an agent may run is `git push [-u] origin HEAD` from a feature/* or chore/* branch.
# Anything else that mentions git push is blocked, including compound commands (fail closed).
# Exit code 2 = block the tool call; stderr is shown to the agent.

input="$(cat)"

if ! command -v jq > /dev/null 2>&1; then
  # Cannot read the command reliably: block anything that looks like a push.
  if printf '%s' "$input" | grep -q 'push'; then
    echo "Blocked by hook: jq is missing, so git push cannot be checked." >&2
    exit 2
  fi
  exit 0
fi

command="$(printf '%s' "$input" | jq -r '.tool_input.command // empty')"
cwd="$(printf '%s' "$input" | jq -r '.cwd // empty')"

# Not a push: nothing to check.
printf '%s' "$command" | grep -Eq '(^|[^[:alnum:]_.-])git([[:space:]]+[^[:space:];&|]+)*[[:space:]]+push([[:space:]]|$)' || exit 0

block() {
  {
    echo "Blocked by hook: $1"
    echo "Agents push only with: git push -u origin HEAD (or git push origin HEAD) from a feature/* or chore/* branch."
  } >&2
  exit 2
}

trimmed="$(printf '%s' "$command" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//; s/[[:space:]]+/ /g')"
case "$trimmed" in
  "git push origin HEAD" | "git push -u origin HEAD" | "rtk git push origin HEAD" | "rtk git push -u origin HEAD") ;;
  *) block "only the exact form is allowed, got: $trimmed" ;;
esac

branch="$(git -C "${cwd:-${CLAUDE_PROJECT_DIR:-.}}" rev-parse --abbrev-ref HEAD 2> /dev/null)"
case "$branch" in
  feature/* | chore/*) exit 0 ;;
  *) block "current branch '${branch:-unknown}' is not feature/* or chore/*." ;;
esac
