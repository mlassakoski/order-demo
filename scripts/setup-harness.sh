#!/usr/bin/env bash
# One-time setup: installs the AI harness config into .claude/ and .github/.
# (The files ship in harness-setup/ because they were generated remotely and
#  remote tools are not allowed to write into .claude/ directly.)
# Run once from the project root, then commit.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -d harness-setup ]; then
  echo "harness-setup/ not found: harness already installed?" >&2
  exit 1
fi

mkdir -p .claude .github
cp -R harness-setup/dot-claude/. .claude/
cp -R harness-setup/dot-github/. .github/
chmod +x .claude/hooks/*.sh scripts/*.sh mvnw 2> /dev/null || true

# Remove the staging copy so agents only see one version of the config.
rm -rf harness-setup

echo "Harness installed:"
find .claude .github -type f | sort
echo
echo "Next: git add -A && git commit -m 'Install AI harness'"
