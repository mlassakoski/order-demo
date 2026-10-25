package com.demo.order.domain;

/** Every order line of an order must be present. */
public final class InvalidOrderLineException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderLineException() {
        super("An order line must not be null");
    }
}
