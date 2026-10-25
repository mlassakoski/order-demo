#!/usr/bin/env bash
# Creates the "vanilla" copy for demo run A: same code, no harness.
# Removes: AGENTS.md, CLAUDE.md, CODING_STANDARDS.md, GLOSSARY.md, CONTRIBUTING.md, .claude/, jira/, docs/,
# .github/, harness-setup/, scripts/, .vscode/, the architecture tests and the Claude lines in
# .gitignore. Disables the mattpocock-skills plugin
# (kept aside so we can score the result afterwards with score-run.sh).
#
# Usage: scripts/make-vanilla.sh [target-dir]   (default: ../order-demo-vanilla)
set -euo pipefail

src="$(cd "$(dirname "$0")/.." && pwd)"
dst="${1:-$src/../order-demo-vanilla}"

if [ -e "$dst" ]; then
  echo "Target $dst already exists. Remove it or pass another path." >&2
  exit 1
fi

mkdir -p "$dst"
rsync -a \
  --exclude '.git' --exclude 'target' \
  --exclude 'AGENTS.md' --exclude 'CLAUDE.md' --exclude 'CODING_STANDARDS.md' --exclude 'GLOSSARY.md' --exclude 'CONTRIBUTING.md' --exclude '.mcp.json' \
  --exclude '.claude' --exclude 'jira' --exclude 'docs' --exclude '.github' --exclude 'harness-setup' \
  --exclude '/scripts' --exclude '.vscode' --exclude '.classpath' --exclude '.project' --exclude '.settings' \
  --exclude 'src/test/java/com/demo/order/architecture' \
  --exclude 'src/test/resources/archunit.properties' \
  "$src/" "$dst/"

# The ignore entries for Claude Code files and the score log would hint at the harness.
sed -i.bak -e '/^# Claude Code local files$/d' -e '/^# Claude Code subagent worktrees$/d' \
  -e '/^\.claude\//d' -e '/^score\.log$/d' "$dst/.gitignore"
rm -f "$dst/.gitignore.bak"

# The plugin is installed per user, so switch it off for this project only.
mkdir -p "$dst/.claude"
cat > "$dst/.claude/settings.json" <<'JSON'
{
  "enabledPlugins": {
    "mattpocock-skills@claude-plugins-official": false
  }
}
JSON

# Plain README so the agent gets no hints.
printf '# order-demo\n\nSpring Boot order service.\n' > "$dst/README.md"

(cd "$dst" && git init -q && git add -A && git commit -qm "Vanilla baseline")

echo "Vanilla copy ready at: $dst"
echo "Run the demo prompt there with: claude"
echo "Score it afterwards with: $src/scripts/score-run.sh $dst"
