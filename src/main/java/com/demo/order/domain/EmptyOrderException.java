package com.demo.order.domain;

/** An order must have at least one order line. */
public final class EmptyOrderException extends DomainValidationException {

    public EmptyOrderException() {
        super("An order needs at least one order line");
    }
}
