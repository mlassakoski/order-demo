package com.demo.order.domain;

/** Only a Pending or Paid order can be cancelled. */
public final class OrderNotCancellableException extends DomainConflictException {

    private static final long serialVersionUID = 1L;

    public OrderNotCancellableException(OrderStatus status) {
        super("An order that is " + status + " cannot be cancelled");
    }
}
