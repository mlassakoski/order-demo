#!/usr/bin/env bash
# Stop and SubagentStop hook (feedback sensor).
# When the agent tries to finish and code changed, run the full build (tests + ArchUnit)
# in the agent's own working directory (a subagent's worktree, or the project).
# Exit code 2 = do not stop; stderr goes back to the agent so it fixes the failures.
# Gives up after 3 failed attempts so a human can look.

input="$(cat)"
event="$(printf '%s' "$input" | jq -r '.hook_event_name // empty' 2> /dev/null)"
dir="$(printf '%s' "$input" | jq -r '.cwd // empty' 2> /dev/null)"
project="${CLAUDE_PROJECT_DIR:-.}"
dir="$(git -C "${dir:-$project}" rev-parse --show-toplevel 2> /dev/null || echo "${dir:-$project}")"

# Build only in the project or one of its own worktrees: never run an untrusted ./mvnw.
project_dir="$(cd "$project" && pwd -P)"
dir="$(cd "$dir" 2> /dev/null && pwd -P)" || exit 0
known=0
while IFS= read -r line; do
  case "$line" in
    "worktree "*) [ "$(cd "${line#worktree }" 2> /dev/null && pwd -P)" = "$dir" ] && known=1 ;;
  esac
done <<< "$(git -C "$project_dir" worktree list --porcelain 2> /dev/null)"
[ "$known" = 1 ] || exit 0

# A subagent in the project directory itself is covered by the main session's Stop hook.
if [ "$event" = "SubagentStop" ] && [ "$dir" = "$project_dir" ]; then
  exit 0
fi

cd "$dir" || exit 0

# Only run when source or build files changed (skips planning / Q&A turns).
if git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
  if [ -z "$(git status --porcelain -- src pom.xml)" ]; then
    rm -f .claude/.verify-attempts # nothing to check: start the next failure at attempt 1
    exit 0
  fi
fi

if [ -x ./mvnw ]; then MVN=./mvnw; else MVN=mvn; fi

counter=".claude/.verify-attempts"
log=".claude/.verify.log"
attempts="$(cat "$counter" 2> /dev/null || echo 0)"

if "$MVN" -q -B verify > "$log" 2>&1; then
  rm -f "$counter"
  exit 0
fi

attempts=$((attempts + 1))
echo "$attempts" > "$counter"

if [ "$attempts" -ge 3 ]; then
  rm -f "$counter"
  echo "Build still failing after 3 attempts. Stopping so a human can review. Log: $log" >&2
  exit 0
fi

{
  echo "./mvnw verify failed (attempt $attempts of 3). Fix the failures before finishing."
  echo "Do not edit architecture tests; change the production code."
  echo "----- last lines of the build log -----"
  tail -n 80 "$log"
} >&2
exit 2
