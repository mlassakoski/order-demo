---
paths:
  - "src/main/java/**/adapter/out/**"
  - "src/test/java/**/adapter/out/**"
---

# Persistence adapter

- `<Noun>InMemoryAdapter` implements the output port, annotated `@Component` (enforced placement).
- Store and return separate instances (rebuild with `rehydrate`), so changes persist only through `save`, as with a real database.
- Thread-safe storage (`ConcurrentHashMap`).
