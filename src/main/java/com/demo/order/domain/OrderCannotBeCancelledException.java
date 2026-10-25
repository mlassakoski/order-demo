package com.demo.order.domain;

import java.util.Map;

/** An order can be cancelled only while Pending or Paid: Shipped is too late, and Cancelled is final. */
public final class OrderCannotBeCancelledException extends DomainRuleViolationException {

    private static final long serialVersionUID = 1L;

    private final transient OrderId orderId;
    private final OrderStatus currentStatus;

    public OrderCannotBeCancelledException(OrderId orderId, OrderStatus currentStatus) {
        super("Order " + orderId.value() + " cannot be cancelled because it is " + currentStatus.name());
        this.orderId = orderId;
        this.currentStatus = currentStatus;
    }

    public OrderId orderId() {
        return orderId;
    }

    /** The status the order is in, which forbids cancelling it. */
    public OrderStatus currentStatus() {
        return currentStatus;
    }

    /** {@code currentState}: the name of the status that forbids cancelling. */
    @Override
    public Map<String, String> problemProperties() {
        return Map.of("currentState", currentStatus.name());
    }
}
