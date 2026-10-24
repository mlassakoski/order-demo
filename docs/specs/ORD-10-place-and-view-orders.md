# ORD-10: Place and view orders

| Epic | Status | Owner | Board |
|---|---|---|---|
| ORD-10 | Approved (ready-for-agent) | Mauricio | `jira/ORD-board.md` → ORD-12, ORD-13, ORD-14 |

Produced in refinement ORD-11 (`/grill-with-docs` → `/to-spec` → `/to-tickets`).

## Problem Statement

Customers have no way to buy anything: the service has no orders yet. They need to place an order for one or more products and look it up afterwards to see its state.

## Solution

A customer places an order by sending their customer identifier and one or more order lines (product and quantity). The service creates the order as Pending, gives it a service-generated identifier and returns it with its location. The customer can later fetch the order by its identifier and see its customer, lines and state. Invalid requests are refused with a clear problem description and create nothing.

## User Stories

1. As a customer, I want to place an order with one product, so that I can buy it.
2. As a customer, I want to place an order with several order lines, so that I can buy several products at once.
3. As a customer, I want my new order to start as Pending, so that I know it still needs payment.
4. As a customer, I want the service to give my order its identifier, so that I never have to invent one.
5. As a customer, I want to receive the order's location when I place it, so that I can fetch it right away.
6. As a customer, I want to see my order by its identifier, so that I can check its state and lines.
7. As a customer, I want a clear "not found" answer for an unknown identifier, so that I know the order doesn't exist.
8. As a customer, I want an order with no order lines to be refused, so that I don't create an empty order by mistake.
9. As a customer, I want an order line with a quantity below 1 to be refused, so that quantities always make sense.
10. As a customer, I want a missing or blank customer or product identifier to be refused, so that every order is traceable.
11. As an API consumer, I want every refusal to use the same problem format, so that I can handle errors in one place.
12. As a developer, I want the order's rules to hold no matter how the order is created, so that a future entry point can't create an invalid order.

## Implementation Decisions

- **Order aggregate** in the domain, with value objects for the order identifier, customer identifier, product identifier and order line, and a lifecycle state (Pending, Paid, Shipped, Cancelled). Only Pending is reachable in this epic; the full lifecycle is modelled now because it is part of the domain language and ORD-20 depends on it.
- **Creation and rebuild:** a `place` factory enforces the invariants (at least one order line, quantity ≥ 1, non-blank identifiers); a `rehydrate` factory rebuilds an order from storage.
- **Error model, introduced incrementally:** domain validation → 400 (ORD-13), not found → 404 (ORD-14), domain rule violation → 409 (ORD-23). One exception handler, RFC 9457 problem details.
- **Use cases** behind input ports: place an order, get an order. One output port for order storage (save, find by identifier).
- **Storage:** in-memory adapter that stores and returns separate instances, so it behaves like a database and later swapping it is safe.
- **HTTP contract:** `POST /orders` → 201 + `Location: /orders/{id}` + order; `GET /orders/{id}` → 200 + order; refusals → 400 / 404 problem details.
- **Validation in two places, on purpose:** bean validation at the HTTP edge for fast field-level feedback; the domain stays the source of truth for invariants.

## Testing Decisions

- A good test checks external behaviour through a public seam and survives refactoring; expected values come from this spec, never recomputed like the code does.
- Seams (from `CODING_STANDARDS.md`): the Order aggregate's public methods; each use case through its input port with an in-memory fake of the storage port; the HTTP contract via a `@WebMvcTest` slice with the use case mocked; one acceptance test (`*IT`) per story through the running application.
- Prior art: `ApplicationHealthIT` for the acceptance-test setup. These stories set the pattern for later features.

## Out of Scope

- Prices, totals, payment, shipping.
- A real database.
- Authentication and customer accounts.
- Merging order lines for the same product (two lines stay two lines).

## Further Notes

First feature of the service; it sets the patterns (aggregate, ports, error model, tests) that later epics follow. Each story carries its technical tasks on the board.

## Delivery plan

| Key | Story | Points | Blocked by | Technical tasks |
|---|---|---|---|---|
| ORD-12 | Place an order | 5 | ORD-3, ORD-4 | T1 domain → T2 use case → T3 storage → T4 `POST /orders` → T5 IT → T6 close |
| ORD-13 | Refuse invalid orders | 3 | ORD-12 | T1 invariants → T2 request validation → T3 400 mapping → T4 IT → T5 close |
| ORD-14 | View an order | 2 | ORD-12 | T1 get use case + not found → T2 `GET` + 404 mapping → T3 IT → T4 close |

ORD-13 and ORD-14 can run in parallel (separate worktrees) after ORD-12 merges.
