# Coding standards

Read during implementation and by `/code-review` (Standards axis) and the `security-reviewer` subagent.
**Mechanical** rules are enforced by ArchUnit (`src/test/java/**/architecture/`) or the build and marked ⚙️; the rest are **judgement** rules for the reviewer.

## Layer rules (single source of truth, loaded automatically by Claude Code when you touch matching files)
| Layer | Rules |
|---|---|
| `domain` | `.claude/rules/domain.md` |
| `application` | `.claude/rules/application.md` |
| `adapter.in.web` | `.claude/rules/web-adapter.md` |
| `adapter.out.persistence` | `.claude/rules/persistence-adapter.md` |
| Tests | `.claude/rules/tests.md` |

Other agents (Copilot, Cursor, Codex): read the matching file before editing that layer.

## Layers (hexagonal) ⚙️
| Package | Contains | May depend on |
|---|---|---|
| `domain` | Aggregates, value objects, domain exceptions | Java only |
| `application.port.in` | `<Verb><Noun>UseCase` + command/query records | domain |
| `application.port.out` | `<Noun><Capability>Port` | domain |
| `application.service` | `<Verb><Noun>Service`, one per use case | ports, domain |
| `adapter.in.web` | Controllers, request/response records, `GlobalExceptionHandler` | input ports, domain |
| `adapter.out.persistence` | `<Noun>InMemoryAdapter` | output ports, domain |

## Errors → HTTP (RFC 9457 `ProblemDetail`, only in `GlobalExceptionHandler`)
| Exception | HTTP |
|---|---|
| Bean validation failure on a request body | 400 |
| `DomainValidationException` subclass | 400 |
| `NotFoundException` subclass | 404 |
| `DomainRuleViolationException` subclass | 409 |

Throw specific exceptions only ⚙️. Problem details never contain stack traces or internal class names.

## General
- Spring Boot 4: `spring-boot-starter-webmvc` (not the deprecated `-web`), Jackson 3. Unsure about a Boot 4 API: look it up (Context7 MCP) instead of guessing.
- Constructor injection ⚙️. SLF4J ⚙️. No Lombok in the domain ⚙️. Add dependencies only after asking.
- Formatting is automatic (Spotless, Palantir Java Format) ⚙️.
- Never log personal data or full request bodies.
- Every public input has a business limit, named in the spec: max length for strings, max size for collections, max value for numbers. Request records carry the annotations ⚙️; the domain enforces the same limits.
