# AI-first workflow with Matt Pocock's skills plus deterministic guardrails

Agents write the code; humans own decisions. We use Matt Pocock's composable skills (`/grill-with-docs` → `/to-spec` → `/to-tickets` → `/implement` with `/tdd` and `/code-review`, then `/retro`) with Jira as the tracker (simulated by `jira/ORD-board.md` for the demo) and specs in `docs/specs/`, instead of process-owning frameworks such as Spec Kit, to keep control of each step. Mechanical rules are deterministic checks (ArchUnit, a Stop hook running `./mvnw verify`, CI); judgement rules live in `CODING_STANDARDS.md` for the review agent; `AGENTS.md` stays a short list of pointers. Every PR records first-pass acceptance.

## Considered Options
- Spec Kit / Kiro style specs (requirements → design → tasks): heavier, owns the process, verbose files to review.
- Custom project skills: duplicated what the plugin already does well.
