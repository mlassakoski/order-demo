---
paths:
  - "src/main/java/**/adapter/in/**"
  - "src/test/java/**/adapter/in/**"
---

# Web adapter

- Controllers depend only on `*UseCase` interfaces (enforced).
- Request and response bodies are records in this package; domain objects never leave as JSON.
- Bean Validation on request bodies (`@Valid`, `@NotBlank`, `@NotEmpty`, `@Min`).
- Errors map only in `GlobalExceptionHandler` (`@RestControllerAdvice` extending `ResponseEntityExceptionHandler`), returning RFC 9457 `ProblemDetail`: `DomainValidationException` → 400, `NotFoundException` → 404, `DomainRuleViolationException` → 409.
- Create → `201` + `Location`. Action on a resource (`POST /orders/{id}/cancel`) → `200` with the updated resource.
