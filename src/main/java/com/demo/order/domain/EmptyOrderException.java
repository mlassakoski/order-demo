package com.demo.order.domain;

/** An order must have at least one order line. */
public final class EmptyOrderException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public EmptyOrderException() {
        super("An order needs at least one order line");
    }
}
