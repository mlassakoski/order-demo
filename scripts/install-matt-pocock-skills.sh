#!/usr/bin/env bash
# Installs the Claude Code plugins this repo uses (all from the official marketplace).
# .claude/settings.json lists them under enabledPlugins, so teammates are prompted for them too.
set -euo pipefail

# Workflow skills: grilling, spec, tickets, TDD, code review, retro. https://github.com/mattpocock/skills
claude plugin install mattpocock-skills@claude-plugins-official

# Code intelligence for Java (go-to-definition, references, live compile errors). Needs the jdtls binary.
claude plugin install jdtls-lsp@claude-plugins-official
if ! command -v jdtls > /dev/null 2>&1; then
  echo "jdtls not found: install it with 'brew install jdtls' (macOS) for the Java code-intelligence plugin."
fi

cat <<'MSG'

Installed. Next, inside Claude Code in this repo, run once:
  /setup-matt-pocock-skills
Answers for this repo:
  Issue tracker  -> Other: paste the paragraph from "How agents use this board" in jira/ORD-board.md
  Triage labels  -> yes, keep the defaults
  Domain docs    -> single-context (GLOSSARY.md + docs/adr/), written without asking
It adds an "## Agent skills" block to CLAUDE.md and writes docs/agents/*.md. Commit them.

The Context7 MCP server (.mcp.json) asks for approval the first time; approve it.
Not sure which skill to use? Run /ask-matt.
MSG
