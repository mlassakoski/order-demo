#!/usr/bin/env bash
# DORA metrics and first-pass acceptance from this repo's own GitHub data.
# The service never deploys, so a merge to main stands in for a deployment.
#
# Usage: scripts/dora.sh [--since YYYY-MM-DD] [-R owner/repo] [--save DIR | --from DIR]
#   --save DIR   also write the fetched data to DIR (prs.json, runs.json)
#   --from DIR   read DIR instead of calling GitHub (offline, e.g. on stage)
# Needs jq, and gh (logged in) unless --from is used.
set -euo pipefail

usage() { sed -n '2,8p' "$0" | sed 's/^# \{0,1\}//' >&2; exit 2; }

since="" repo="" save="" from=""
while [ $# -gt 0 ]; do
  case "$1" in
    --since) since="${2:-}"; shift 2 ;;
    -R) repo="${2:-}"; shift 2 ;;
    --save) save="${2:-}"; shift 2 ;;
    --from) from="${2:-}"; shift 2 ;;
    -h | --help) usage ;;
    *) echo "Unknown argument: $1" >&2; usage ;;
  esac
done
if [ -n "$since" ] && ! [[ "$since" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
  echo "--since must be YYYY-MM-DD, got: $since" >&2; exit 2
fi
command -v jq > /dev/null || { echo "jq is required" >&2; exit 2; }

if [ -n "$from" ]; then
  prs="$from/prs.json" runs="$from/runs.json"
  [ -f "$prs" ] && [ -f "$runs" ] || { echo "Missing $prs or $runs" >&2; exit 2; }
else
  command -v gh > /dev/null || { echo "gh is required (or use --from DIR)" >&2; exit 2; }
  tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
  prs="$tmp/prs.json" runs="$tmp/runs.json"
  [ -n "$repo" ] || repo="$(gh repo view --json owner,name --jq '.owner.login + "/" + .name')"
  # Own query: `gh pr list --json commits` asks for every commit's authors and
  # exceeds GraphQL's node limit. The first commit is all lead time needs.
  gh api graphql --paginate -f owner="${repo%%/*}" -f name="${repo#*/}" -f query='
    query($owner: String!, $name: String!, $endCursor: String) {
      repository(owner: $owner, name: $name) {
        pullRequests(first: 100, after: $endCursor, states: MERGED, baseRefName: "main") {
          pageInfo { hasNextPage endCursor }
          nodes {
            number title headRefName mergedAt
            labels(first: 20) { nodes { name } }
            commits(first: 1) { nodes { commit { authoredDate } } }
          }
        }
      }
    }' --jq '.data.repository.pullRequests.nodes[]' \
    | jq -s 'map({number, title, headRefName, mergedAt, labels: .labels.nodes,
        commits: (.commits.nodes | map(.commit))})' > "$prs"
  gh run list -R "$repo" --workflow ci.yml --branch main --event push --limit 1000 \
    --json databaseId,conclusion,status,createdAt,updatedAt,displayTitle > "$runs"
  if [ -n "$save" ]; then mkdir -p "$save"; cp "$prs" "$runs" "$save/"; fi
fi

jq -rn --slurpfile prs "$prs" --slurpfile runs "$runs" --arg since "$since" '
  # GitHub keeps the committer UTC offset on commit dates; fromdateiso8601 only reads Z.
  def ts: sub("\\.[0-9]+"; "") | if test("Z$") then fromdateiso8601
    else capture("^(?<d>.{19})(?<s>[+-])(?<h>[0-9]{2}):(?<m>[0-9]{2})$")
      | (.d + "Z" | fromdateiso8601) - (if .s == "+" then 1 else -1 end) * ((.h | tonumber) * 3600 + (.m | tonumber) * 60)
    end;
  def dur: if . == null then "n/a"
    elif . < 3600 then "\(. / 60 | floor) min"
    elif . < 172800 then "\(. / 360 | floor / 10) h"
    else "\(. / 8640 | floor / 10) d" end;
  def pct(p): sort | if length == 0 then null else .[[(length * p | ceil) - 1, 0] | max] end;
  def keys_of: [scan("ORD-[0-9]+")] | unique;
  def is_fix: (.title | test("^(fix(\\(.*\\))?!?:|revert)"; "i"));

  ($since | if . == "" then 0 else (. + "T00:00:00Z" | ts) end) as $start
  | ($prs[0] | map(select(.mergedAt != null and (.mergedAt | ts) >= $start))
     | sort_by(.mergedAt)) as $merged
  | ($merged | length) as $n
  | (if $start > 0 then $start elif $n > 0 then ($merged[0].mergedAt | ts) else now end) as $from
  | ((now - $from) / 86400 | if . < 1 then 1 else . end) as $days

  # Lead time: first commit on the branch to the merge.
  | ($merged | map(select((.commits | length) > 0)
      | (.mergedAt | ts) - (.commits | map(.authoredDate | ts) | min))) as $lead

  # Change failure: a fix/revert PR blames the latest earlier merge on the same ticket
  # key (or the #number a revert names).
  | ($merged | to_entries | map(select(.value | is_fix) | .key as $j | .value as $fix
      | ($fix.title + " " + $fix.headRefName | keys_of) as $fk
      | [$merged[:$j][] | .number as $num | select(
          ((.title + " " + .headRefName | keys_of) as $k | $fk | any(. as $x | $k | index($x)))
          or ($fix.title | test("^revert"; "i")) and ($fix.title | test("#\($num)\\b")))]
      | last | .number? // empty) | unique) as $failed
  | ($merged | map(select(is_fix)) | length) as $fixes

  # Recovery: first red ci run on main to the next green one (cancelled runs ignored).
  | ($runs[0] | map(select(.status == "completed" and (.createdAt | ts) >= $start
        and (.conclusion == "success" or .conclusion == "failure")))
     | sort_by(.createdAt)
     | reduce .[] as $r ({red: null, out: []};
         if $r.conclusion == "failure" then (.red //= ($r.updatedAt | ts))
         elif .red != null then .out += [($r.updatedAt | ts) - .red] | .red = null
         else . end)) as $rec
  | ($runs[0] | map(select(.status == "completed" and (.createdAt | ts) >= $start
        and .conclusion == "failure")) | length) as $red

  | ($merged | map([.labels[].name]) ) as $lbl
  | def count(l): $lbl | map(select(index(l))) | length;
  (count("ai-first-pass")) as $fp | (count("ai-small-fixes")) as $sf | (count("ai-reworked")) as $rw
  | ($fp + $sf + $rw) as $rated

  | "DORA (proxies: no deploys, a merge to main counts as one)",
    "Window: \(if $start > 0 then $since else "all history" end) to today, \($days * 10 | floor / 10) days, \($n) merged PRs",
    "",
    "Lead time for changes   median \($lead | pct(0.5) | dur), p90 \($lead | pct(0.9) | dur)   (first branch commit to merge)",
    "Deployment frequency    \($n / $days * 10 | floor / 10) merges/day, \($merged | map(.mergedAt[:10]) | unique | length) active day(s)",
    "Change failure rate     \(if $n == 0 then "n/a" else "\($failed | length * 100 / $n | floor)%" end) (\($failed | length)/\($n))\(if ($failed | length) > 0 then "   PRs \($failed | map("#\(.)") | join(", "))" else "" end)   (later fix/revert PR on the same ticket)",
    "Recovery time           \(if ($rec.out | length) == 0 then (if $rec.red != null then "main still red" else "n/a, no red ci run on main" end) else "median \($rec.out | pct(0.5) | dur) over \($rec.out | length) incident(s)" end)   (\($red) red runs; red ci on main to next green)",
    "Rework rate             \(if $n == 0 then "n/a" else "\($fixes * 100 / $n | floor)%" end) (\($fixes)/\($n))   (fix/revert PRs among merges)",
    "",
    "First-pass acceptance (PR labels)",
    "ai-first-pass \($fp) · ai-small-fixes \($sf) · ai-reworked \($rw) · unrated \($n - $rated)",
    "First-pass rate         \(if $rated == 0 then "n/a" else "\($fp * 100 / $rated | floor)% of \($rated) rated PRs" end)"
'
