#!/usr/bin/env bash
# Scores a demo run with the same "judge" for both setups:
# copies the team's architecture tests into the target and runs ./mvnw verify.
#
# Usage: scripts/score-run.sh <project-dir>
set -uo pipefail

src="$(cd "$(dirname "$0")/.." && pwd)"
dst="${1:?usage: score-run.sh <project-dir>}"

mkdir -p "$dst/src/test/java/com/demo/order/architecture" "$dst/src/test/resources"
cp "$src"/src/test/java/com/demo/order/architecture/*.java "$dst/src/test/java/com/demo/order/architecture/"
cp "$src/src/test/resources/archunit.properties" "$dst/src/test/resources/"

cd "$dst" || exit 1
if [ -x ./mvnw ]; then MVN=./mvnw; else MVN=mvn; fi

echo "== Scoring $dst =="
"$MVN" -B verify > score.log 2>&1
result=$?

echo
echo "Tests run summary:"
grep -E "Tests run: [0-9]+, Failures" score.log | tail -n 1
echo
echo "Architecture / convention violations:"
grep -E "Rule '|was violated" score.log | sed 's/^/  /' | head -n 40
echo
echo "Quality gates:"
grep -E "Coverage checks have not been met|violates|Rule .* failed|BUILD (SUCCESS|FAILURE)" score.log | sed 's/^/  /' | head -n 20
echo
if [ $result -eq 0 ]; then echo "RESULT: PASS"; else echo "RESULT: FAIL (see $dst/score.log)"; fi
