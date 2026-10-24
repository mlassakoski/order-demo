# ORD-20: Cancel orders

| Epic | Status | Owner | Board |
|---|---|---|---|
| ORD-20 | Reference (expected output of ORD-21) | Mauricio | `jira/ORD-board.md` → ORD-22, ORD-23 |

> Reference copy for the demo. Live, refinement ORD-21 (`/grill-with-docs` → `/to-spec` → `/to-tickets`) produces `docs/specs/ORD-20-cancel-orders.md` and the stories. Short on time: `cp docs/demo/reference/ORD-20-cancel-orders.md docs/specs/` and move ORD-22 and ORD-23 to `Ready for Agent`.

## Problem Statement

Customers change their mind, but once an order is placed they cannot stop it. They need to cancel an order as long as it has not been shipped.

## Solution

A customer cancels an order by its identifier. Pending and Paid orders become Cancelled and the updated order is returned. Shipped orders, and orders that are already Cancelled, are refused with a conflict and stay as they were. Unknown identifiers get "not found".

## User Stories

1. As a customer, I want to cancel a Pending order, so that I don't pay for something I no longer want.
2. As a customer, I want to cancel a Paid order before it ships, so that I can change my mind after paying.
3. As a customer, I want to see the updated order after cancelling, so that I know it worked.
4. As a customer, I want cancelling a Shipped order to be refused, so that I understand it is too late.
5. As a customer, I want cancelling an already Cancelled order to be refused, so that a duplicate request doesn't look like a fresh success.
6. As a customer, I want a refused cancellation to leave my order unchanged, so that nothing half-happens.
7. As a customer, I want a clear "not found" for an unknown order, so that I know I used the wrong identifier.
8. As an API consumer, I want refusals in the same problem format as other errors, so that I handle them in one place.

## Implementation Decisions

- The rule "cancellable only while Pending or Paid" belongs to the Order aggregate (`cancel`). Violating it raises a domain rule violation, mapped to 409; this epic introduces that mapping.
- New cancel-order use case behind an input port; reuses the storage port and the not-found error from ORD-10.
- HTTP contract: `POST /orders/{id}/cancel` → 200 + updated order; 409 / 404 problem details. The 409 problem detail carries the current state.
- Decision from grilling: cancelling twice is a conflict (409), not an idempotent success, so client bugs stay visible. Idempotency keys can come later.
- Decision from grilling: no refund for Paid orders and no cancellation reason in this epic.

## Testing Decisions

- Seams as in `CODING_STANDARDS.md`: Order aggregate (`cancel` from every state, set up with `rehydrate`); cancel use case with in-memory fake storage; HTTP via `@WebMvcTest`; one acceptance test (`*IT`) per story.
- No API exists to pay or ship an order yet, so acceptance tests seed Paid and Shipped orders through the storage port (arrange only; assertions go through the HTTP API).
- Prior art: the ORD-10 tests.

## Out of Scope

- Refunds for Paid orders.
- Cancellation reason, audit trail, domain events.
- Paying or shipping orders.

## Further Notes

None.

## Delivery plan

| Key | Story | Points | Blocked by | Technical tasks |
|---|---|---|---|---|
| ORD-22 | Cancel a Pending or Paid order | 3 | ORD-21 | T1 `Order.cancel()` → T2 use case → T3 `POST /orders/{id}/cancel` → T4 IT → T5 close |
| ORD-23 | Refuse to cancel Shipped or Cancelled orders | 2 | ORD-22 | T1 rule + exception → T2 409 mapping → T3 IT → T4 close |
