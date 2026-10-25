# Ordering

Receives and tracks customer orders from placement until they are shipped or cancelled.

## Language

**Order**:
A customer's request to buy one or more products, tracked through its lifecycle.
_Avoid_: Purchase, cart, basket

**Order line**:
One product and its quantity inside an order.
_Avoid_: Item, entry, row

**Customer**:
The person who places an order, known to us only by an external identifier.
_Avoid_: Client, buyer, user, account

**Product**:
Something a customer can order, known to us only by an external identifier.
_Avoid_: Article, SKU

## Lifecycle

**Place**:
To create a new order; it starts as Pending.
_Avoid_: Create, submit

**Pending**:
An order that has been placed but not paid.

**Paid**:
An order whose payment has been received.

**Shipped**:
An order handed to the carrier.

**Cancel**:
To stop an order before it ships.
_Avoid_: Delete, void, abort

**Cancelled**:
The final state of an order that was stopped before shipping. Final: a cancelled order cannot be cancelled again or resumed.
