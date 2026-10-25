# ORD-20: Cancel orders

| Epic | Status | Owner | Board |
|---|---|---|---|
| ORD-20 | Draft (awaiting approval) | Mauricio | `jira/ORD-board.md` → ORD-22, ORD-23 |

Produced in refinement ORD-21 (grilling → spec → tickets).

## Problem Statement

Customers change their mind, but once an order is placed they cannot stop it. They need to cancel an order as long as it has not been shipped.

## Solution

A customer cancels an order by its identifier. Pending and Paid orders become Cancelled and the updated order is returned. Shipped orders, and orders that are already Cancelled, are refused with a conflict that says why and gives the current state; the order stays as it was. Unknown identifiers get "not found".

## User Stories

1. As a customer, I want to cancel a Pending order, so that I don't pay for something I no longer want.
2. As a customer, I want to cancel a Paid order before it ships, so that I can change my mind after paying.
3. As a customer, I want to see the updated order after cancelling, so that I know it worked without asking again.
4. As a customer, I want cancelling a Shipped order to be refused, so that I understand it is too late.
5. As a customer, I want cancelling an already Cancelled order to be refused, so that a duplicate request doesn't look like a fresh success.
6. As a customer, I want a refused cancellation to leave my order unchanged, so that nothing half-happens.
7. As a customer, I want the refusal to tell me the order's current state, so that I know why it was refused.
8. As a customer, I want a clear "not found" for an unknown or malformed identifier, so that I know I used the wrong one.
9. As an API consumer, I want refusals in the same problem format as other errors, so that I handle them in one place.
10. As an API consumer, I want to cancel without sending a body, so that the call is as simple as possible.
11. As a developer, I want the cancellation rule to hold however the order is reached, so that a future entry point can't cancel a Shipped order.

## Implementation Decisions

- **Rule in the aggregate:** "cancellable only while Pending or Paid" belongs to the Order aggregate (`cancel`). Pending and Paid become Cancelled; Shipped and Cancelled are refused and the state is left unchanged.
- **Error model:** a new abstract domain rule violation type, with an order-cannot-be-cancelled exception carrying the order identifier and the current state. Mapped to 409 problem detail with a `currentState` property and the detail "Order <id> cannot be cancelled because it is <STATE>". This epic introduces the 409 mapping (see `CODING_STANDARDS.md`).
- **Use case:** a cancel-order use case behind an input port: load → cancel → save. Reuses the storage port and the not-found error from ORD-10.
- **HTTP contract:** `POST /orders/{id}/cancel`, no request body. 200 + the updated order (same shape as `GET /orders/{id}`); 409 for refusals; unknown or malformed identifier → the same 404 problem detail as `GET`, never echoing the raw value.
- **Decisions from grilling (ORD-21):**
  - Cancelling twice is a conflict (409), not an idempotent success: Cancelled is final, and a client bug stays visible.
  - 409 rather than 422/400: the request is valid, the order's state forbids it.
  - No request body and no cancellation reason, so no new input needing a business limit.
  - 200 with the order rather than 204, so the client sees the new state at once.
- **Glossary:** Cancelled is final (cannot be cancelled again or resumed). No new terms; no ADR (the decision is easy to reverse).

## Testing Decisions

- A good test checks external behaviour through a public seam; expected values come from this spec, never recomputed like the code does.
- Seams as in `CODING_STANDARDS.md`: the Order aggregate (`cancel` from every state, states set up with `rehydrate`); the cancel use case through its input port with an in-memory fake of the storage port; HTTP via `@WebMvcTest` with the use case mocked; one acceptance test (`*IT`) per story, written first (ORD-18).
- No API exists to pay or ship an order yet, so acceptance tests seed Paid and Shipped orders through the storage port (arrange only; assertions go through the HTTP API).
- Prior art: the ORD-10 tests (`PlaceOrderIT`, `ViewOrderIT`, `OrderControllerTest`, `FakeOrderRepository`).

## Out of Scope

- Refunds for Paid orders (the service has no payment concept).
- Cancellation reason, audit trail, domain events.
- Paying or shipping orders.
- Authentication: anyone who knows an order's identifier can cancel it, as with viewing.

## Further Notes

- Concurrency is not handled: two cancels at once, or a cancel racing a future ship, can both read the old state. Acceptable with in-memory storage and no ship API; revisit with a real database (optimistic locking).

## Delivery plan

| Key | Story | Points | Blocked by | Technical tasks |
|---|---|---|---|---|
| ORD-22 | Cancel a Pending or Paid order | 3 | ORD-21 | T1 IT → T2 `Order.cancel()` → T3 use case → T4 `POST /orders/{id}/cancel` → T5 close |
| ORD-23 | Refuse to cancel Shipped or Cancelled orders | 2 | ORD-22 | T1 IT → T2 rule + exception → T3 409 mapping → T4 close |
