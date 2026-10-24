---
paths:
  - "src/main/java/**/application/**"
  - "src/test/java/**/application/**"
---

# Application layer

- One input port per use case: `<Verb><Noun>UseCase` with one method; its command or query is a record next to it (`CancelOrderCommand`).
- One `@Service` per use case: `<Verb><Noun>Service implements <Verb><Noun>UseCase` (enforced).
- Services orchestrate only: load through an output port → call the aggregate → save. No business rules here.
- Output ports are interfaces named `<Noun><Capability>Port` (`OrderRepositoryPort`) (enforced).
- Missing aggregate → a `NotFoundException` subclass (`OrderNotFoundException`).
- No HTTP types (`ResponseEntity`, `HttpStatus`) in this layer (enforced).
