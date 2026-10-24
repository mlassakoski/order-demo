---
paths:
  - "src/test/**"
---

# Tests

- Names: `should_<behavior>_when_<condition>` (enforced). Unit tests end in `Test` (Surefire), integration tests in `IT` (Failsafe).
- Test behaviour through public seams, one red→green cycle per technical task:
  - **Domain:** the aggregate's public methods. Plain JUnit + AssertJ, no Spring (enforced).
  - **Use case:** the service through its `*UseCase`, real domain, in-memory fake of the output port. No mocks of our own domain.
  - **HTTP slice:** `@WebMvcTest` (`org.springframework.boot.webmvc.test.autoconfigure`) + `@MockitoBean` for the use case; assert status, JSON and `ProblemDetail`.
  - **Acceptance (`*IT`):** one happy path per ticket through the running app, `@SpringBootTest` + `@AutoConfigureMockMvc`. Pattern: `ApplicationHealthIT`.
- Expected values come from the ticket or a literal, never recomputed the way the code does it.
- Constructor injection in tests: `MyIT(@Autowired MockMvc mockMvc)`.
- Coverage gate: ≥ 80% lines in `domain` and `application` (JaCoCo).
