package com.demo.order.domain;

/** An order can be cancelled only while Pending or Paid: Shipped is too late, and Cancelled is final. */
public final class OrderCannotBeCancelledException extends DomainRuleViolationException {

    private static final long serialVersionUID = 1L;

    private final transient OrderId orderId;
    private final OrderStatus currentState;

    public OrderCannotBeCancelledException(OrderId orderId, OrderStatus currentState) {
        super("Order " + orderId.value() + " cannot be cancelled because it is " + currentState.name());
        this.orderId = orderId;
        this.currentState = currentState;
    }

    public OrderId orderId() {
        return orderId;
    }

    @Override
    public OrderStatus currentState() {
        return currentState;
    }
}
