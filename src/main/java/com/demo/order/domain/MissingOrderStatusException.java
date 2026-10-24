package com.demo.order.domain;

/** An order always has a lifecycle state. */
public final class MissingOrderStatusException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public MissingOrderStatusException() {
        super("Order status must not be null");
    }
}
