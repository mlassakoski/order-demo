# AGENTS.md

Order service. Java 21, Spring Boot 4.1, Maven. DDD + hexagonal architecture.
Built AI-first with Matt Pocock's skills (plugin `mattpocock-skills`): humans make the decisions, agents write the code.

## Commands
- All checks (format, unit + integration tests, ArchUnit, coverage): `./mvnw verify`
- One test: `./mvnw test -Dtest=ClassName#methodName`
- Run: `./mvnw spring-boot:run` (port 8080)

Work on `feature/ORD-NN-slug`, commit as `feat(ORD-NN): summary`; humans push and merge.

## Pointers
- **Before writing or reviewing code**: `CODING_STANDARDS.md` (cross-cutting) and the layer rules in `.claude/rules/` (Claude Code loads them automatically for the files you touch; other agents read the matching file).
- **Domain words**: `GLOSSARY.md`. Decisions: `docs/adr/`.
- **Tickets**: `jira/ORD-board.md` (simulated Jira; fetch a ticket by key, e.g. `ORD-12`). Follow its **Technical tasks** in order and tick them. **Specs**: `docs/specs/<EPIC>-<slug>.md`.
- **Library docs**: Spring Boot 4 / Jackson 3 APIs changed recently. Look them up with the `context7` MCP server instead of guessing.
- **Before moving a ticket to In Review**: `./mvnw verify` green, `/code-review main` clean, `security-reviewer` subagent PASS.
- **Architecture rules are code**: `src/test/java/**/architecture/`. Team-owned; a hook blocks agent edits. When a rule blocks you, change the production code or explain to the human why the rule is wrong.
