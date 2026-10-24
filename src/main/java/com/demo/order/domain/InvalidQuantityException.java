package com.demo.order.domain;

/** The quantity of an order line must be at least 1. */
public final class InvalidQuantityException extends DomainValidationException {

    public InvalidQuantityException() {
        super("Quantity must be at least 1");
    }
}
