---
paths:
  - "src/main/java/**/domain/**"
  - "src/test/java/**/domain/**"
---

# Domain layer

- Pure Java 21: no Spring, Jakarta or Lombok (ArchUnit enforces).
- Business rules live in the aggregate. State changes are intention-revealing methods (`order.cancel()`), never setters (enforced).
- Aggregate constructor is private. Create with a factory (`Order.place(...)`); rebuild from storage with `Order.rehydrate(...)`.
- Value objects are `record`s that validate in the compact constructor (`OrderId`, `CustomerId`).
- Exposed collections are unmodifiable (`List.copyOf`).
- Invalid input → a `DomainValidationException` subclass. Operation not allowed in the current state → a `DomainRuleViolationException` subclass. Name exceptions after the rule: `OrderCannotBeCancelledException`.
- Names come from `GLOSSARY.md`.
