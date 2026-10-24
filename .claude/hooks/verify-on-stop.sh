#!/usr/bin/env bash
# Stop hook (feedback sensor).
# When the agent tries to finish and code changed, run the full build (tests + ArchUnit).
# Exit code 2 = do not stop; stderr goes back to the agent so it fixes the failures.
# Gives up after 3 failed attempts so a human can look.

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
cat > /dev/null # hook input not needed

# Only run when source or build files changed (skips planning / Q&A turns).
if git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
  if [ -z "$(git status --porcelain -- src pom.xml)" ]; then
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
